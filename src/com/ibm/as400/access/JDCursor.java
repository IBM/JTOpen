///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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



    // Reuse flags.  These describe how to close a cursor.
    static final int            REUSE_NO            = 0xF0;
    static final int            REUSE_YES           = 0xF1;
    static final int            REUSE_RESULT_SET    = 0xF2;



    private boolean             closed_;
    private AS400JDBCConnection connection_;
    private int                 id_;
    private String              name_;



/**
Constructs a JDCursor object.

@param  connection      Connection to the server.
@param   id             The id.
@param  name            Cursor name.
**/
    JDCursor (AS400JDBCConnection connection,
              int id,
              String name)
    {
        closed_     = true;
        connection_ = connection;
        id_         = id;
        name_       = name;
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

   	       	connection_.send (request, id_);
	    }
       	catch (DBDataStreamException e) {
           	JDError.throwSQLException (JDError.EXC_INTERNAL);
	    }

	    closed_ = true;

        if (JDTrace.isTraceOn()) {
            JDTrace.logInformation (this, "Closing with reuse flag = " + reuseFlag);
            JDTrace.logClose  (this);
        }
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
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
        // If we are opening a cursor on a subsequent
        // result set returned by a stored procedure,
        // then it is read only.
        if (sqlStatement == null)
            return OPEN_READONLY_;

        // If we are opening a cursor on a result set
        // returned by a stored procedure, then it is
        // read only.
        if (sqlStatement.isProcedureCall ())
            return OPEN_READONLY_;

        // For SELECTs, the cursor is read only when
        // the cursor or connection is read only and
        // when we are record blocking.  Note that record
        // blocking implies a read only cursor.
        if ((sqlStatement.isSelect ())
            && (! blockCriteria.equalsIgnoreCase (JDProperties.BLOCK_CRITERIA_NONE))
            && ((connection_.isReadOnly())
                || (! sqlStatement.isForUpdate ())))
            return OPEN_READONLY_;

        return OPEN_ALL_;
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
		        + DBSQLRequestDS.ORS_BITMAP_DATA_FORMAT, 0);

            request.setOpenAttributes (openAttributes);
            request.setScrollableCursorFlag (scrollable ? 1 : 0);

            DBReplyRequestedDS reply = connection_.sendAndReceive (request, id_);

	  		int errorClass = reply.getErrorClass();
	    	int returnCode = reply.getReturnCode();

       		if (errorClass != 0)
	    		JDError.throwSQLException (connection_, id_, errorClass, returnCode);

            dataFormat = reply.getDataFormat ();
	    }
    	catch (DBDataStreamException e) {
           	JDError.throwSQLException (JDError.EXC_INTERNAL);
	    }

        closed_ = false;

        if (JDTrace.isTraceOn())
            JDTrace.logOpen (this);

        return dataFormat;
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

