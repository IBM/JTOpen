///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDPackageManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



/**
<p>This class manages a SQL packages.
**/
class JDPackageManager
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  //private static final String SUFFIX_INVARIANT_ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; //@G0D
  // because we only send unicode to the system anyway we don't check if the system         //@G0A
  // supports unicode first like ODBC does                                                  //@G0A
  private static final String SUFFIX_INVARIANT_ = "9876543210ZYXWVUTSRQPONMLKJIHGFEDCBA";   //@G0A

  private boolean             cache_;
  private DBReplyPackageInfo  cachedPackage_;
  private ConvTable           cachedPackageConverter_; //@P0C
  private int                 cachedStatementCount_;
  private boolean             clear_;
  private AS400JDBCConnection connection_;
  private boolean             created_;
  private boolean             enabled_;
  private String              error_;
  private int                 id_;
  private String              libraryName_;
  private String              name_;
  private int                 threshold_;



  /**
  Constructs a JDPackageManager object.
  
  @param  connection      the connection to the system.
  @param  id              the id.
  @param  properties      the connection properties.
  @param  commitMode      the current commit mode for connection.
  
  @exception  SQLException    If an error occurs.
  **/
  JDPackageManager (AS400JDBCConnection connection,
                    int id,
                    JDProperties properties,
                    int commitMode)
  throws SQLException
  {
    // Determine if extended support is enabled.
    enabled_ = properties.getBoolean (JDProperties.EXTENDED_DYNAMIC);

    if (enabled_)
    {

      // Initialization.
      cache_      = properties.getBoolean (JDProperties.PACKAGE_CACHE);
      clear_      = properties.getBoolean (JDProperties.PACKAGE_CLEAR);
      connection_ = connection;
      created_    = false;
      error_      = properties.getString (JDProperties.PACKAGE_ERROR);
      id_         = id;
      threshold_  = clear_ ? 512 : 0;

      // Determine the library name.
      String packageLibrary = properties.getString (JDProperties.PACKAGE_LIBRARY);
      if (packageLibrary.length () > 0)
        libraryName_ = packageLibrary.toUpperCase ();
      else
        libraryName_ = "QGPL";

      // Make sure a package was specified.
      String packageName = properties.getString (JDProperties.PACKAGE);
      if (packageName.length() > 0)
      {

        // Normalize the package name to 7 characters and tack
        // on the appropriate 3 character suffix.
        StringBuffer buffer = new StringBuffer (10);
        String normalizedName;
        if (packageName.length() >= 7)
          normalizedName = packageName.substring (0, 7);
        else
          normalizedName = packageName;
        buffer.append (normalizedName.toUpperCase().replace (' ', '_'));
        buffer.append (getSuffix (properties, commitMode));
        name_ = buffer.toString ();
      }
      else
      {
        enabled_ = false;
        postError (JDError.WARN_EXTENDED_DYNAMIC_DISABLED);
      }
    }
  }



  /**
  Downloads the package from the system and caches it in memory.
  
  @exception  SQLException    If an error occurs.
  **/
  private void cache ()
  throws SQLException
  {
    if (enabled_)
    {
      try
      {
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (connection_,
                                  "Caching package [" + name_ + "]");

        DBSQLRequestDS request = null; //@P0A
        DBReplyRequestedDS reply = null; //@P0A
        try
        {
          request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                                 DBSQLRequestDS.FUNCTIONID_RETURN_PACKAGE, id_,
                                                 DBSQLRequestDS.ORS_BITMAP_RETURN_DATA
                                                 + DBSQLRequestDS.ORS_BITMAP_PACKAGE_INFO, 0);

          request.setPackageName (name_, connection_.converter_); //@P0C
          request.setLibraryName (libraryName_, connection_.converter_); //@P0C
          request.setReturnSize (0);

          reply = connection_.sendAndReceive (request, id_); //@P0C

          int errorClass = reply.getErrorClass();
          int returnCode = reply.getReturnCode();

          if (errorClass != 0)
          {
            cache_ = false;
            postError (JDError.WARN_PACKAGE_CACHE_DISABLED);
          }

          else
          {
            cachedPackage_ = reply.getPackageInfo ();
            cachedPackageConverter_ = connection_.getConverter (cachedPackage_.getCCSID());
            cachedStatementCount_ = cachedPackage_.getStatementCount ();
          }
        }
        finally
        {
          if (request != null) request.inUse_ = false;
          if (reply != null) reply.inUse_ = false;
        }
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException (JDError.EXC_INTERNAL, e);
      }
    }
  }



  /**
  Creates the package on the system, if extended dynamic support is
  enabled.  If the package already existed on the system, and the
  user has enabled package caching, then this downloads and caches
  the package.
  
  @exception  SQLException    If an error occurs.
  **/
  void create ()
  throws SQLException
  {
    if (enabled_)
    {

      try
      {
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (connection_,
                                  "Creating package [" + name_ + "]");

        DBSQLRequestDS request = null; //@P0A
        DBReplyRequestedDS reply = null; //@P0A
        try
        {
          request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                                 DBSQLRequestDS.FUNCTIONID_CREATE_PACKAGE, id_,
                                                 DBSQLRequestDS.ORS_BITMAP_RETURN_DATA, 0);

          request.setPackageName (name_, connection_.converter_); //@P0C
          request.setLibraryName (libraryName_, connection_.converter_); //@P0C

          // $F1 This parameter no longer needs to be passed on a data stream.  Package clearing
          // $F1 and the decision for the threshold where package clearing is needed is now handled
          // $F1 automatically by the database.  Passing this code point results in a no-op.
          // @F1D  request.setPackageThresholdValue (threshold_);

          reply = connection_.sendAndReceive (request, id_); //@P0C

          int errorClass = reply.getErrorClass();
          int returnCode = reply.getReturnCode();

          // If the package already exists, then download and
          // cache it if needed.
          if ((errorClass == 1) && (returnCode == -601))
          {
            if (JDTrace.isTraceOn())
              JDTrace.logInformation (connection_,
                                      "Package [" + name_ + "] already exists");

            if (cache_)
              cache ();
          }

          // If the default collection in the package is
          // different than the one for this connection, then
          // we can not use the package because strange and
          // unexpected things may happen to data.
          else if (returnCode == -999999)
          {
            enabled_ = false;
            postError (JDError.WARN_EXTENDED_DYNAMIC_DISABLED);
          }

          else if (errorClass != 0)
          {
            enabled_ = false;
            postError (connection_, id_, errorClass, returnCode);
          }
        }
        finally
        {
          if (request != null) request.inUse_ = false;
          if (reply != null) reply.inUse_ = false;
        }
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException (JDError.EXC_INTERNAL, e);
      }
    }

    created_ = true;
  }



  /**
  Returns a cached data format.
  
  @param  statementIndex  the cached statement index.
  @return                 the data format, or null if not cached
  
  @exception  SQLException    If the statement index
                is not valid.
  **/
  DBDataFormat getCachedDataFormat (int statementIndex)
  throws SQLException
  {
    try
    {
      if (isCached ())
        return(cachedPackage_.getDataFormat (statementIndex));
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    return null;
  }



  /**
  Returns a cached parameter marker format.
  
  @param  statementIndex  the cached statement index.
  @return                 the parameter marker format.
  
  @exception  SQLException    If the statement index
                is not valid.
  **/
  DBDataFormat getCachedParameterMarkerFormat (int statementIndex)
  throws SQLException
  {
    try
    {
      if (isCached ())
        return(cachedPackage_.getParameterMarkerFormat (statementIndex));
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    return null;
  }



  /**
  Returns an index identifying the cached statement.
  
  @param  sqlStatement the SQL statement.
  @return              the index, or -1 if not cached.
  
  @exception  SQLException    If the data stream is problematic.
  **/
  int getCachedStatementIndex (JDSQLStatement sqlStatement)
  throws SQLException
  {
    if (! isCached())
      return -1;

    String statementText = sqlStatement.toString ();
    int statementTextLength = statementText.length ();

    //@G1M Moved this if statement.  It should not have been in the for loop.
    //@F2A If we are running to a v5r1 or higher system, the cached 
    //@F2A package is now in unicode.
    if (connection_.getVRM() >= JDUtilities.vrm510)              //@F2A
    {
      //@F2A
      statementTextLength = statementTextLength * 2;           //@F2A
    }                                                            //@F2A

    for (int i = 0; i < cachedStatementCount_; ++i)
    {

      try
      {
        // Compare lengths first, before going to the
        // expense of a full String comparison.
        if (statementTextLength == cachedPackage_.getStatementTextLength (i))
        {

          // The lengths match, so now do the String
          // comparison.
          if (statementText.equals (cachedPackage_.getStatementText (i,
                                                                     cachedPackageConverter_)))
            return i;
        }
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException (JDError.EXC_INTERNAL, e);
      }

    }

    return -1;
  }



  /**
  Returns a cached statement name.
  
  @param  statementIndex  the cached statement index.
  @return                 the statement name, or null if not cached.
  
  @exception  SQLException    If the statement index
                is not valid.
  **/
  String getCachedStatementName (int statementIndex)
  throws SQLException
  {
    try
    {
      if (isCached ())
        return(cachedPackage_.getStatementName (statementIndex,
                                                        null));  //@F2C
            //@F2D return(cachedPackage_.getStatementName (statementIndex, cachedPackageConverter_));
            //@F2D Statement name is in jobCCSID_, not unicode which is what 
            //@F2D cachedPackageConverter_ converts.
        }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    return null;
  }



  /**
  Returns the library name for the package.
  
  @return the library name.
  **/
  String getLibraryName ()
  {
    return libraryName_;
  }



  /**
  Returns the name of the package.
  
  @return the package name.
  **/
  String getName ()
  {
    return name_;
  }



  /**
  Returns the suffix (the last 3 characters) of the package name.
  These are generated based on various property values, which
  influence the effectiveness of the package.  This is necessary
  because the properties in question affect the contents of the
  packages in a way that if the property value changes, we want
  to use a different package.  This mechanism is exactly the same
  as is used in Client Access/ODBC.
  
  @param  properties  the connection properties.
  @param  commitMode  the current commit mode for the connection.
  @return the suffix of the package name.
  **/
  private String getSuffix (JDProperties properties,
                            int commitMode)
  {
    // The last three characters of the package name (the
    // suffix) are generated based on various property
    // values.  This is necessary because the properties
    // in question affect the contents of the packages
    // in a way that if the property value changes, we
    // want to use a different package.
    //
    // This mechanism is exactly the same as is used in
    // Client Access/ODBC.
    //
    StringBuffer suffix = new StringBuffer (3);
    int index;

    // Base the 1st suffix character on:
    //
    //     commit mode (c) - values 0-3
    //     date format (f) - values 0-7
    //
    // The index into the invariant is formed as 000ccfff.
    //

        int dateSep = properties.getIndex(JDProperties.DATE_SEPARATOR);                                             //@G0A

        // if commitMode is 4 (*RR), it won't fit in 2 bits, use part of DateSep to store the commitMode            //@G0A
        // and use commitMode to store part of DateSep. This takes advantage of the fact that DateSep               //@G0A
        // values only range from 0-4.  We still have a DateSep of 5 available for future expansion.                //@G0A
        if (commitMode == 4) {  // we dont use a constant for the RR commit mode because there isnt one available   //@G0A
            // the different DateSep are: 0=/, 1=-, 2=., 3=comma, 4=blank                                           //@G0A
            switch(dateSep) {                                                                                       //@G0A
            case 0: case 1: case 2:                                                                                 //@G0A
                commitMode = dateSep;                                                                               //@G0A
                dateSep = 6;                                                                                        //@G0A
                break;                                                                                              //@G0A
            case 3: case 4:                                                                                         //@G0A
                commitMode = dateSep - 2;                                                                           //@G0A
                dateSep = 7;                                                                                        //@G0A
                break;                                                                                              //@G0A
            }                                                                                                       //@G0A
        }                                                                                                           //@G0A

    index = (commitMode << 3)
            | (properties.getIndex (JDProperties.DATE_FORMAT));
    suffix.append (SUFFIX_INVARIANT_.charAt (index));

    // Base the 2nd suffix character on:
    //
    //     decimal separator (d) - values 0-1
    //     naming (n)            - values 0-1
    //     date separator (s)    - values 0-4
    //
    // The index into the invariant is formed as 000dnsss.
    index = (properties.getIndex (JDProperties.DECIMAL_SEPARATOR) << 4)
            | (properties.getIndex (JDProperties.NAMING) << 3)
                | (dateSep);                                                                                        //@G0A
    suffix.append (SUFFIX_INVARIANT_.charAt (index));

    // Base the 3rd suffix character on:
    //
    //     time format (f)       - values 0-4
    //     time separator (s)    - values 0-3
    //
    // The index into the invariant is formed as 000fffss.
    index = (properties.getIndex (JDProperties.TIME_FORMAT) << 2)
            | (properties.getIndex (JDProperties.TIME_SEPARATOR));
    suffix.append (SUFFIX_INVARIANT_.charAt (index));

    return suffix.toString ();
  }



  /**
  Returns the package threshold.  This is the total number
  of statements that the package can contain before being
  cleared.
  
  @return the package threshold; or 0 for unlimited number
      of statements.
  **/
  int getThreshold ()
  {
    return threshold_;
  }



  /**
  Indicates if the package is cached.
  
  @return true if the package is cached; false
      otherwise.
  **/
  boolean isCached ()
  {
    return(cachedPackage_ != null);
  }



  /**
  Indicates if the package was created.
  
  @return true if the package was created; false otherwise.
  **/
  boolean isCreated ()
  {
    return created_;
  }



  /**
  Indicates if extended dynamic support is enabled.
  
  @return true if extended dynamic support is enabled; false
      otherwise.
  **/
  boolean isEnabled ()
  {
    return enabled_;
  }



  /**
  Posts an error for the connection.  This will post the error
  as a warning or exception as specified in the properties.
  
  @param  sqlState    the SQL state.
  
  @exception  SQLException    The generated exception, if
                the error is to be an exception.
  **/
  private void postError (String sqlState)
  throws SQLException
  {
    if (error_.equalsIgnoreCase (JDProperties.PACKAGE_ERROR_EXCEPTION))
    {
      JDError.throwSQLException (sqlState);
    }

    else if (error_.equalsIgnoreCase (JDProperties.PACKAGE_ERROR_WARNING))
      connection_.postWarning (JDError.getSQLWarning (sqlState));
  }



  /**
  Posts an error for the connection.  This will post the error
  as a warning or exception as specified in the properties.
  
  @param  connection  connection to the system.
  @param  id          id for the last operation.
  @param  errorClass  error class from the system reply.
  @param  returnCode  return code from the system reply.
  
  @exception  SQLException    The generated exception, if
                the error is to be an exception.
  **/
  private void postError (AS400JDBCConnection connection,
                          int id,
                          int errorClass,
                          int returnCode)
  throws SQLException
  {
    if (error_.equalsIgnoreCase (JDProperties.PACKAGE_ERROR_EXCEPTION))
    {
      JDError.throwSQLException (connection, id, errorClass, returnCode);
    }

    else if (error_.equalsIgnoreCase (JDProperties.PACKAGE_ERROR_WARNING))
      connection_.postWarning (JDError.getSQLWarning (connection, id, errorClass, returnCode));
  }



}
