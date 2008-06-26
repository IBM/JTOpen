///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDLibraryList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.StringTokenizer;



/**
<p>This class represents a system library list for database access.
**/
class JDLibraryList
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  private static final String        LIBL_         = "*LIBL";

  private char[]                  indicators_;
  private String                  defaultSchema_;
  private String[]                   list_;



/**
Constructor.

@param  list                The list of libraries.
@param  defaultSchema       The default schema.
@param  naming              The naming convention.
**/
  JDLibraryList (String list, String defaultSchema, String naming) // @C1C
  {
    boolean startsWithComma = false;                              // @E2a
    String incomingList = list;

    // Initialize.
    defaultSchema_ = null;
    if (!defaultSchema.equals("") || defaultSchema != null)
      if (defaultSchema.length() > 0)
        defaultSchema_ = defaultSchema.toUpperCase ();

    // @F1: through v4r5 the system automatically added the default schema
    //      to the front of the library list.  In v5r1 they stopped adding it.
    //      ODBC Customers complained their apps no longer worked so ODBC changed
    //      to add the default schema to the library list.  We will do the same
    //      here because we will eventually have broken customers as well.  The
    //      rules are add the default schema to the front of the library list
    //      (1) for both SQL and system naming, (2) if it isn't already in the
    //      list, (3) only if a default is specified (don't add the userid if SQL
    //      naming).  Note we won't break the comma started list (@E2) since
    //      that applies only if no default schema is specified.  If no default
    //      is specified we don't change the list given to us by the user.
    if (defaultSchema_ != null)                                      //@F1a
    {                                                                //@F1a
       if ((list != null) && (list.length() > 0))                    //@F1a
       {                                                             //@F1a
          // assume the deafult schema is not in the list            //@F1a
          boolean alreadyInList = false;                             //@F1a
                                                                     //@F1a
          // does something in the list start with the default schema? //@F1a
          if (list.toUpperCase().indexOf(defaultSchema_) >= 0)       //@F1a
          {                                                          //@F1a
             // Since something close is already in the list         //@F1a
             // look at each token for an exact match.  The          //@F1a
             // .indexOf() check will return a false positive if     //@F1a
             // the default schema is DAW and a library in the       //@F1a
             // list is DAWJDBC.  We do this extra processing        //@F1a
             // only if there is a close match for performance       //@F1a
             StringTokenizer tokenizer = new StringTokenizer (list, " ,:;");  //@F1a
             while (tokenizer.hasMoreTokens ())                      //@F1a
             {                                                       //@F1a
                if (tokenizer.nextToken().toUpperCase().equals(defaultSchema_)) //@F1a
                   alreadyInList = true;                             //@F1a
             }                                                       //@F1a
          }                                                          //@F1a
          if (! alreadyInList)                                       //@F1a
             list = defaultSchema_ + "," + list;                     //@F1a
       }                                                             //@F1a
       else                                                          //@F1a
          list = defaultSchema_;                                     //@F1a
    }                                                                //@F1a

    list_ = null;
    int liblPosition = -1;

    // If a list is specified, then construct the
    // internal list array.
    if (list.length() != 0)
    {

      // OpNav asked that there be a way to specify a library list without
      // getting a default schema.  We agreed that if the library list starts
      // with a comma we would not set a default schema.  We do this
      // only if no schema is in the url.  A schema on the url
      // will be sent as the default schema no matter what is listed
      // in the library list.
      if (defaultSchema_ == null)                // @E2a
      {                                          // @E2a
         String newList = list.trim();           // @E2a
         if (newList.length() > 0)               // @E2a
         {                                       // @E2a
            if (newList.startsWith(","))         // @E2a
               startsWithComma = true;           // @E2a
         }                                       // @E2a
      }

      // Determine if the *LIBL token is included.
      boolean includesLibl = (list.toUpperCase().indexOf (LIBL_) != -1);

      // Parse the list into tokens.
      StringTokenizer tokenizer = new StringTokenizer (list, " ,:;", true);  //@delim2
      String token;
      int count = tokenizer.countTokens ();
      if (includesLibl)
        --count;
      indicators_ = new char[count];
      list_ = new String[count];
      int i = 0;
      while (tokenizer.hasMoreTokens ())
      {
        token = tokenizer.nextToken(); //.toUpperCase();            //@delim2
        
        if((token.compareTo(" ") == 0) || (token.compareTo(",") == 0) || (token.compareTo(":") == 0) || (token.compareTo(";") == 0))      //@delim2
            continue;                                               //@delim2

        if(token.startsWith("\""))                                  //@delim2
        {                                                           //@delim2

            //check if ending quote is in current token
            int nextQuote = token.indexOf("\"", 1);                 //@delim2

            //get next token and search for ending quote
            while(tokenizer.hasMoreTokens() && (nextQuote == -1))   //@delim2
            {                                                       //@delim2
                token += tokenizer.nextToken();                     //@delim2
                nextQuote = token.indexOf("\"", 1);                 //@delim2
            }                                                       //@delim2
        }                                                           //@delim2
        else                                                        //@delim2
        {                                                           //@delim2
            token = token.toUpperCase();                            //@delim2
        }                                                           //@delim2
            

        // Mark the position of *LIBL.  Only mark
        // the first occurence.  The system will
        // return an error later if it occurs
        // more than once.
        if (token.equalsIgnoreCase (LIBL_))
        {
          if (liblPosition == -1)
            liblPosition = i;
        }
        else
        {
          if (includesLibl)                                                 //IF *LIBL was in the libraries connection property
            indicators_[i] = (liblPosition == -1) ? 'F' : 'L';
          else if(incomingList.equals("") && defaultSchema != null && !defaultSchema.equals(""))     //if libraries connection property was not specified and a default collection was put on the URL        //@K1A
            indicators_[i] = (i == 0) ? 'F' : 'L';                                                                                                                                  //@K1A
          else
            indicators_[i] = (i == 0) ? 'C' : 'L';
          list_[i] = token;
          ++i;
        }
      }
      
      if(i != count)                                            //@delim2
      {                                                         //@delim2
          String[] tmpList = list_;                             //@delim2
          list_ = new String[i];                                //@delim2
          System.arraycopy(tmpList, 0, list_, 0, i);            //@delim2
      }                                                         //@delim2

      // If no default schema was specified, then
      // derive it from the list.
      // @E1 do this only for SQL naming!  If we tell the system to use a default schema
      //     then the library list is ignored.  That would break some apps using system naming.
      //     For example, suppose the libraries property is "libraries=lib1,lib2,lib3", and the
      //     file is in lib3.  If we take the first one off the list and set it to be the default
      //     (remember the others will be ignored when a default is set) the apps that
      //     used to find a file in lib3 no longer work.  The behavior as now coded
      //     consistent through v5r1.  In v5r2 we changed to make the first
      //     item the default.  The user can override this behavior by starting the
      //     list with a comma.
      if ((defaultSchema_ == null)
          && (! startsWithComma)                         // @E2a don't set default schema if first char is a comma
          && (naming.equals (JDProperties.NAMING_SQL)))  // @E1c @C1C
      {
        //@KBA  Fix for JTOpen Bug 4025 - *LIBL is not added to the list_ array.  Therefore, the first thing in the
        // list_ array should become the default schema, provided that there was a libraries list.
        //@KBD if (liblPosition != 0)
        //@KBD   defaultSchema_ = list_[0];
        //@KBD else if (list_.length > 1)
        //@KBD   defaultSchema_ = list_[1];
          if(list_.length > 0)                   //@KBA
              defaultSchema_ = list_[0];        //@KBA
      }
     
      //if default schema is longer than 10, then it cannot be in the library list
      //users must use SET PATH
      if((list_[0].length() > 10) && list_[0].equals(defaultSchema_))//@128sch
      {                                                              //@128sch
          String[] tmpList = list_;                                  //@128sch
          list_ = new String[tmpList.length-1];                      //@128sch
          if(list_.length > 0)
          System.arraycopy(tmpList, 0, list_, 1, list_.length);  //@128sch
         
          if (JDTrace.isTraceOn())  //@128sch
              JDTrace.logInformation (this, "Schema " + defaultSchema_ + " is too long to be in library list, but will still be set as default schema"); //@128sch
      }//@128sch
      
      // Reverse the order of the 'F' libraries, so that
      // they get added to the library in the right order.
      if (liblPosition > 1)
      {
        int halfPoint = ((int) (liblPosition / 2)) - 1;
        String temp;
        for (int j = 0; j <= halfPoint ; ++j)
        {
          int j2 = liblPosition - j - 1;
          temp = list_[j];
          list_[j] = list_[j2];
          list_[j2] = temp;
        }

      }
    }

    // Otherwise, no list was specified.
    else
    {
      list_ = new String[0];
    }
  }



/**
Add the libraries in this list to the server job's library
list for the specified connection.  If the caller specified
*LIBL in the list, then add libraries listed before
*LIBL to the front of the server job's library list.
Likewise, add any libraries listed after *LIBL to the
back of the server job's library list.  If *LIBL is not
specified in the list, then all libraries are replaced
in the server job's library list.

@param      connection      Connection to the system.
@param      id              The id.

@exception  SQLException    If an error occurs.
**/
  void addOnServer (AS400JDBCConnection connection, int id)
  throws SQLException
  {
    if (list_.length > 0)
    {

      DBNativeDatabaseRequestDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try
      {
        request = DBDSPool.getDBNativeDatabaseRequestDS(DBNativeDatabaseRequestDS.FUNCTIONID_ADD_LIBRARY_LIST, id, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0); //@P0C
        request.setListOfLibraries (indicators_, list_, connection.converter_); //@P0C

        reply = connection.sendAndReceive (request); //@P0C

        int errorClass = reply.getErrorClass();
        int returnCode = reply.getReturnCode();

        if (errorClass != 0)                                                 // @D1C
        {
          if ((errorClass == 5) && (returnCode == 1301))                         // @D1A
          {
            // if the error class is NDB and ret code is library not added       @D1A
            //   continue because library being added is already in *LIBL        @D1A
            //   or library does not exist and either case is OK to continue     @D1A
            connection.postWarning(JDError.getSQLWarning(connection, id, errorClass, returnCode));  // @D1A
          }                                                                        // @D1A
          else
          {
            JDError.throwSQLException (connection, id, errorClass, returnCode);
          }                                                                  // @D1A
        }                                                                    // @D1A
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException (JDError.EXC_INTERNAL, e);
      }
      finally //@P0A
      {
        if (request != null) request.inUse_ = false; //@P0A
        if (reply != null) reply.inUse_ = false; //@P0A
      }
    }
  }



/**
Get the default schema.

@return     The default schema, or null if none specified.
**/
  String getDefaultSchema ()
  {
    return defaultSchema_;
  }

  /**
   *  Returns the string representation of the object.
   *  @return The string representation.
   **/
   public String toString()
   {
       /*
       * Implementation note: Used only for tracing information.
       */
       
       return "";
   }
}


