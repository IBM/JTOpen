///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDCursor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.ResultSet;
import java.sql.SQLException;



/**
This class implements a cursor on the server.  NOTE: Creating this
object does not explicitly create the cursor on the server.
**/
class JDCursor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Open attributes.
    private static final int    OPEN_READONLY_      = 0x80;
    private static final int    OPEN_ALL_           = 0xF0;
    private static final int    FULL_OPEN_          = 0x01;   // @W1a



    // Reuse flags.  These describe how to close a cursor.
    static final int            REUSE_NO            = 0xF0;
    static final int            REUSE_YES           = 0xF1;
    static final int            REUSE_RESULT_SET    = 0xF2;



    private boolean             closed_;
    private AS400JDBCConnection connection_;
    private int                 id_;
    private boolean             lazyClose_;                         // @E2A
    private String              name_;
    private int                 concurrency_;                       // @E4A



/**
Constructs a JDCursor object.

@param  connection      Connection to the server.
@param   id             The id.
@param  name            Cursor name.
@param  concurrency     The concurrency
**/
    JDCursor (AS400JDBCConnection connection,
              int id,
              String name,
              // @E3D boolean lazyClose,
              int concurrency)                                      // @E4A
    throws  SQLException                                            // @E6A
    {
        closed_     = true;
        concurrency_ = concurrency;                                 // @E4A
        connection_ = connection;
        id_         = id;
        name_       = name;

        lazyClose_  = connection_.getProperties().getBoolean(JDProperties.LAZY_CLOSE); // @E2A
        // @E3D lazyClose_  = lazyClose;                                                            // @E3A
    }



/**
Closes the cursor.

@param  reuseFlag   One of REUSE_*.

@exception  SQLException    If an error occurs.
**/
//
// Implementation note:
//
// It is a requirement to not get replies during a finalize()
// method.  Since Statement.finalize() calls this method,
// this requirement applies here, too.
//
    void close (int reuseFlag)
        throws SQLException
    {
        // Close the cursor for good.  This makes sure that
        // the cursor is not left around.
        try {
      	    DBSQLRequestDS request = new DBSQLRequestDS (
	            DBSQLRequestDS.FUNCTIONID_CLOSE, id_, 0, 0);
       		request.setReuseIndicator (reuseFlag);

            if (lazyClose_)                                             // @E2A
                connection_.sendAndHold(request, id_);                  // @E2A
            else                                                        // @E2A
   	       	    connection_.send (request, id_);
	    }
       	catch (DBDataStreamException e) {
           	JDError.throwSQLException (JDError.EXC_INTERNAL, e);        // @E5C
	    }

	    closed_ = true;

        if (JDTrace.isTraceOn()) {
            JDTrace.logInformation (this, "Closing with reuse flag = " + reuseFlag);
            JDTrace.logClose  (this);
        }
    }



/**
Returns the cursor name.

@return     The cursor name.
**/
    String getName ()
    {
        return name_;
    }



/**
Returns the open attributes appropriate for the the
SQL statement.

@param  sqlStatement        a SQL statement.
@param  blockCriteria       block criteria.

@return the open attributes.

@exception  SQLException    If an error occurrs.
**/
//
// Implementation note: Cursors perform better on
// the server when read only, so set the open
// attributes to read only in as many cases as
// possible.  (Of course, not when the user needs
// to update using the cursor.)
//
    int getOpenAttributes (JDSQLStatement sqlStatement,
                           String blockCriteria)
        throws SQLException
    {
      int returnValue = OPEN_ALL_;                     // @W1a

      // If we are opening a cursor on a subsequent
      // result set returned by a stored procedure,
      // then it is read only.
      if (sqlStatement == null)
          returnValue = OPEN_READONLY_;                // @W1c

      // If we are opening a cursor on a result set
      // returned by a stored procedure, then it is
      // read only.
      else if (sqlStatement.isProcedureCall ())        // @W1c
          returnValue = OPEN_READONLY_;                // @W1c

      // For SELECTs, the cursor is read only when
      // the cursor or connection is read only and
      // when we are record blocking.  Note that record
      // blocking implies a read only cursor.
      else if ((sqlStatement.isSelect ())              // @W1c
          && (! blockCriteria.equalsIgnoreCase (JDProperties.BLOCK_CRITERIA_NONE))
          && ((connection_.isReadOnly())
              || (! sqlStatement.isForUpdate ())))
          returnValue = OPEN_READONLY_;                // @W1c

      // the "ServerLevel > 9" in the following check makes sure we are running 
      // to a v5r1 or later version of the AS/400.                                                   
      if ( (connection_.getProperties().getBoolean(JDProperties.FULL_OPEN)) && // @W1a
           (connection_.getServerFunctionalLevel() >= 9))                      // @W1a
         returnValue = returnValue | FULL_OPEN_;       // @W1a

      // return OPEN_ALL_;                             // @W1d
      return returnValue;                              // @W1a
    }



// @E1A @E4C
/**
Returns the cursor concurrency.

@return The cursor concurrency.
**/
    int getConcurrency()
    {
        return concurrency_;
    }



/**
Indicates if the cursor is closed.

@return     true if the cursor is closed; false otherwise.
**/
    boolean isClosed ()
    {
        return closed_;
    }



/**
Opens the cursor and describes the data format.

@param      openAttributes  The open attributes.
@param      scrollable      true if the cursor should be
                            scrollable, false otherwise.
@return     The data format.

@exception  SQLException    If an error occurs.
**/
    DBDataFormat openDescribe (int openAttributes,
                               boolean scrollable)
        throws SQLException
    {
        DBDataFormat dataFormat = null;

        try {
        	DBSQLRequestDS request = new DBSQLRequestDS (
		        DBSQLRequestDS.FUNCTIONID_OPEN_DESCRIBE,
		        id_, DBSQLRequestDS.ORS_BITMAP_RETURN_DATA
		        + DBSQLRequestDS.ORS_BITMAP_DATA_FORMAT              
                + DBSQLRequestDS.ORS_BITMAP_SQLCA,                                  // @E1A
                0);

            request.setOpenAttributes (openAttributes);
            request.setScrollableCursorFlag (scrollable ? 1 : 0);

            DBReplyRequestedDS reply = connection_.sendAndReceive (request, id_);

	  		int errorClass = reply.getErrorClass();
	    	int returnCode = reply.getReturnCode();

       		if (errorClass != 0)
	    		JDError.throwSQLException (connection_, id_, errorClass, returnCode);

            processConcurrencyOverride(openAttributes, reply);                            // @E1A @E4C
            dataFormat = reply.getDataFormat ();
	    }
    	catch (DBDataStreamException e) {
           	JDError.throwSQLException (JDError.EXC_INTERNAL, e);                            // @E5C
	    }

        closed_ = false;

        if (JDTrace.isTraceOn())
            JDTrace.logOpen (this);

        return dataFormat;
    }



// @E1A @E4C
/**
Processes a potential cursor concurrency override from a reply.
It is assumed that the reply contains a SQLCA.  This means
that you have to include the SQLCA bit as part of the ORS
bitmap in the request.

@param openAttributes   The requested open attributes.                          
@param reply            The reply.
**/
    void processConcurrencyOverride(int openAttributes, DBBaseReplyDS reply)
        throws DBDataStreamException
    {
        // If the server overrides our open attributes, reflect that fact.            
        DBReplySQLCA sqlca = reply.getSQLCA ();                             
        switch(sqlca.getWarn5()) {                                          
        case (byte)0xF1:  // EBCDIC'1' Read only                                  
        case (byte)0xF2:  // EBCDIC'2' Read and deleteable                        
            concurrency_ = ResultSet.CONCUR_READ_ONLY;
            break;                                                          
        case (byte)0xF4:  // EBCDIC'4' Read, deleteable, and updateable           
            concurrency_ = ResultSet.CONCUR_UPDATABLE;
            break;                                   
        /* @E7D - We should not do an override if this is an old server.
        default:    // Old server (without override indication)
            switch(openAttributes) {
            case OPEN_READONLY_:
                concurrency_ = ResultSet.CONCUR_READ_ONLY;
                break;
            case OPEN_ALL_:
            default:
                concurrency_ = ResultSet.CONCUR_UPDATABLE;
                break;
            }
            break;
        */
        }
    }
                                                                           

    
/**
Set the cursor name.

@param  name    The cursor name.
**/
    void setName (String name)
    {
        name_ = name;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "Name", name_);
    }



/**
Sets the state of the cursor.  This is useful when a
request implicitly opens or closes the cursor.

@param  closed  true for close, or false for open
**/
    void setState (boolean closed)
    {
        closed_ = closed;

        if (JDTrace.isTraceOn()) {
            if (closed_)
                JDTrace.logClose (this);
            else
                JDTrace.logOpen (this);
        }
    }



/**
Returns the cursor name.

@return     the cursor name.
**/
    public String toString ()
    {
        return name_;
    }



}

