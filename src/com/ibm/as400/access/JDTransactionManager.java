///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDTransactionManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.SQLException;



/**
<p>This class manages transaction operations.

<p>In addition to issuing commits and rollbacks to the server,
it manages auto-commit and the transaction isolation level.
Auto-commit and the transaction isolation level interact in the
following ways:

<ul>

<li>Auto-commit is on by default.

<li>The transaction isolation level has no effect when auto-
commit is on.  (Auto-commit on is essentially the same thing
as a transaction isolation level of Connection.TRANSACTION_NONE.)
However, even when auto-commit is on, the current transaction
isolation level is stored and returned upon request via
getIsolation.

</ul>
**/
//
// Implementation notes:
//
// In the code and documentation, "transaction isolation level"
// and "level" refer to the JDBC transaction isolation level (i.e.
// Connection.TRANSACTION_* constants) and "commit mode" refers
// to the value as set on the server.
//
// External to this class, only the transaction isolation level
// is used.  Internal to this class, the transaction isolation
// level is mapped to the appropriate commit mode.  Note that
// not all transaction isolation levels are supported.
//
class JDTransactionManager
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




	private static final int		COMMIT_MODE_NOT_SET_	= -1;
	private static final int		COMMIT_MODE_NONE_		= 0;    // TRANSACTION_NONE
	private static final int		COMMIT_MODE_CHG_		= 1;    // TRANSACTION_READ_UNCOMMITTED
	private static final int		COMMIT_MODE_CS_		= 2;    // TRANSACTION_READ_COMMITTED
	private static final int		COMMIT_MODE_ALL_		= 3;    // TRANSACTION_REPEATABLE_READ
	private static final int      COMMIT_MODE_RR_      = 4;    // TRANSACTION_SERIALIZABLE

	private static final String[]	COMMIT_MODE_	      = { "NONE",
	                                                       "CHG",
	                                                       "CS",
	                                                       "ALL",
	                                                       "RR" };

            static final int      CURSOR_HOLD_FALSE = 0;     // @C1 @B1C
            static final int      CURSOR_HOLD_TRUE  = 1;     // @C1 @B1C

	private boolean             active_;                    // Is a transaction active?
   private boolean             autoCommit_;                // Is auto-commit on?
   private AS400JDBCConnection connection_;
   private int                 currentCommitMode_;         // Current commit mode.
	private int                 currentIsolationLevel_;     // Current isolation level.
   private int                 holdIndicator_;             // Current cursor hold indicator.  @C1
   private int                 id_;
	private int                 initialCommitMode_;         // Initial commit mode.
	private int                 serverCommitMode_;          // Commit mode on the server.



/**
Constructor.  The transaction isolation level should
be initialized independently of this class.  However, since
commits and rollbacks always cause the server to revert
back to the initial commit mode, we need to remember what
this initial commit mode is, so we can predict how the
server is behaving.

@param  connection          Connection to the server.
@param  id                  The id.
@param  initialLevel        One of the Connection.TRANSACTION_*
                            values.

@exception      SQLException    If an invalid or unsupported
                                level is input.
**/
	JDTransactionManager (AS400JDBCConnection connection,
	                      int id,
      	                  String initialLevel)
        throws SQLException
	{
		active_                 = false;
	   autoCommit_             = true;
	   connection_             = connection;
      holdIndicator_          = CURSOR_HOLD_TRUE;                       // @C1
	   id_                     = id;
	   currentIsolationLevel_  = mapStringToLevel (initialLevel);
	   currentCommitMode_      = mapLevelToCommitMode (currentIsolationLevel_);
		initialCommitMode_		= currentCommitMode_;
		serverCommitMode_		= currentCommitMode_;
	}



/**
Commit the current transaction.

@exception  SQLException    If an error occurs.
**/
    void commit ()
        throws SQLException
    {
  		try {
		    DBSQLRequestDS request = new DBSQLRequestDS (
			    DBSQLRequestDS.FUNCTIONID_COMMIT, id_,
			    DBBaseRequestDS.ORS_BITMAP_RETURN_DATA,	0);

           // Set cursor hold.
	        // request.setHoldIndicator (1);                    // @C1
           request.setHoldIndicator(getHoldIndicator());       // @C1

	    	DBReplyRequestedDS reply = connection_.sendAndReceive (request);

		    int errorClass = reply.getErrorClass();
			int returnCode = reply.getReturnCode();

    		if (errorClass != 0)
	    		JDError.throwSQLException (connection_, id_, errorClass, returnCode);
	    }
   		catch (DBDataStreamException e) {
    		JDError.throwSQLException (JDError.EXC_INTERNAL, e);
	    }

   		resetServer ();
        active_ = false;
    }



/**
Is auto-commit mode enabled?.

@return     true if auto-commit mode is enabled.
**/
    boolean getAutoCommit ()
    {
        return autoCommit_;
    }



/**
Return the current commit mode.

@return     The commit mode.
**/
//
// Note:  This is the only place where the commit mode is
//        externalized outside of this class.  This is
//        necessary for other data streams that need to
//        pass a commit mode (e.g. set server attributes).
//
    int getCommitMode ()
    {
        return serverCommitMode_;
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }

/**
*  Returns the hold indicator.
*  @return The hold indicator.
**/
int getHoldIndicator()           // @C1
{
   return holdIndicator_;
}

/**
Return the current transaction isolation level.

@return     One of the Connection.TRANSACTION_* values.
**/
	int getIsolation ()
	{
		return currentIsolationLevel_;
	}



/**
Is a transaction active?

@return     true if a transaction is active.
**/
    boolean isActive ()
    {
        return active_;
    }



/**
Map a transaction isolation level to its corresponding
commit mode.

@param  level   One of the java.sql.Connection.TRANSACTION_*
                values.
@return         The commit mode.

@exception      SQLException    If the level does not map
                                to any commit mode.
**/
	private static int mapLevelToCommitMode (int level)
		throws SQLException
	{
		int result;
		switch (level) {
		case Connection.TRANSACTION_NONE:
		    result = COMMIT_MODE_NONE_;
		    break;
		case Connection.TRANSACTION_READ_UNCOMMITTED:
			result = COMMIT_MODE_CHG_;
			break;
		case Connection.TRANSACTION_READ_COMMITTED:
			result = COMMIT_MODE_CS_;
			break;
		case Connection.TRANSACTION_REPEATABLE_READ:
			result = COMMIT_MODE_ALL_;
			break;
		case Connection.TRANSACTION_SERIALIZABLE:
			result = COMMIT_MODE_RR_;
			break;
		default:
			JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
			result = -1;
		}
		return result;
	}



/**
Map a transaction isolation level specified as a String
to its corresponding int value.

@param  levelAsString   A transaction isolation level
                        specified as a String.
@return                 The int value.

@exception      SQLException    If the String level does not map
                                to an int value.
**/
    static int mapStringToLevel (String levelAsString)
        throws SQLException
    {
        int result;
        if (levelAsString.equalsIgnoreCase (JDProperties.TRANSACTION_ISOLATION_NONE))
            result = Connection.TRANSACTION_NONE;
        else if (levelAsString.equalsIgnoreCase (JDProperties.TRANSACTION_ISOLATION_READ_COMMITTED))
            result = Connection.TRANSACTION_READ_COMMITTED;
        else if (levelAsString.equalsIgnoreCase (JDProperties.TRANSACTION_ISOLATION_READ_UNCOMMITTED))
            result = Connection.TRANSACTION_READ_UNCOMMITTED;
        else if (levelAsString.equalsIgnoreCase (JDProperties.TRANSACTION_ISOLATION_REPEATABLE_READ))
            result = Connection.TRANSACTION_REPEATABLE_READ;
        else if (levelAsString.equalsIgnoreCase (JDProperties.TRANSACTION_ISOLATION_SERIALIZABLE))
            result = Connection.TRANSACTION_SERIALIZABLE;
        else {
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
            result = -1;
        }
        return result;
    }



/**
Reset the server to the current commit mode.  This is useful
since after commits and rollbacks, the server automatically
reverts back to its initial commit mode.

@exception  SQLException    In an error occurs.
**/
	private void resetServer ()
	    throws SQLException
	{
	    // Model the server automatically reverting back to
	    // its initial commit mode.
		serverCommitMode_ = initialCommitMode_;

		// Reset the server's commit mode.
		setCommitMode (currentCommitMode_);
	}



/**
Rollback the current transaction.  If auto-commit mode is
enabled, then do nothing.

@exception  SQLException    If an error occurs.
**/
    void rollback ()
      throws SQLException
    {
  		try {
   	    	DBSQLRequestDS request = new DBSQLRequestDS (
		        DBSQLRequestDS.FUNCTIONID_ROLLBACK, id_,
		        DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);

            // Set cursor hold.
   			//request.setHoldIndicator (1);                     // @C1
            request.setHoldIndicator(getHoldIndicator());       // @C1

	    	DBReplyRequestedDS reply = connection_.sendAndReceive (request);

	    	int errorClass = reply.getErrorClass();
		    int returnCode = reply.getReturnCode();

   			if (errorClass != 0)
    			JDError.throwSQLException (connection_, id_, errorClass, returnCode);
   	    }
    	catch (DBDataStreamException e) {
	     	JDError.throwSQLException (JDError.EXC_INTERNAL, e);
   		}

    	resetServer ();
	    active_ = false;
    }



/**
Set the auto-commit mode.

@param  autoCommit  true to turn on auto-commit mode, false to
                    turn it off.

@exception          SQLException    If an error occurs.
**/
    void setAutoCommit (boolean autoCommit)
      throws SQLException
    {
		// If going from false to true, then commit any outstanding
		// transaction.
		if (!autoCommit_ && autoCommit && active_) {
			commit ();
			connection_.postWarning (JDError.getSQLWarning (JDError.WARN_TXN_COMMITTED));
		}

        // Save the auto commit state.
		autoCommit_ = autoCommit;

        setCommitMode (currentCommitMode_);
    }



/**
Set the commit mode on the server.

@param      commitMode      The commit mode.

@exception  SQLException    If an error occurs.
**/
	private void setCommitMode (int commitMode)
	    throws SQLException
	{
	    // If auto-commit is on, then override the commit mode
	    // to "NONE".
	    if (autoCommit_)
	        commitMode = COMMIT_MODE_NONE_;

	    // Act only if the server commit mode is something other
	    // then the what was requested.
		if (commitMode != serverCommitMode_) {

            JDSQLStatement sqlStatement = new JDSQLStatement (
                "SET TRANSACTION ISOLATION LEVEL " + COMMIT_MODE_[commitMode]);

    		// Send the execute immediate data stream.
	    	try {
	    		DBSQLRequestDS request = new DBSQLRequestDS (
		    	    DBSQLRequestDS.FUNCTIONID_EXECUTE_IMMEDIATE, id_,
		    	    DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
    		    	+ DBBaseRequestDS.ORS_BITMAP_SQLCA, 0);

    			request.setStatementText (sqlStatement.toString (), connection_.getConverter ());
        		request.setStatementType (sqlStatement.getNativeType ());

        		// This statement certainly does not need a cursor, but some
        		// versions of the server choke when none is specified.
        		request.setCursorName ("MURCH", connection_.getConverter ());

    			DBReplyRequestedDS reply = connection_.sendAndReceive (request);

    			int errorClass = reply.getErrorClass();
	    		int returnCode = reply.getReturnCode();

		    	if (errorClass != 0)
			    	JDError.throwSQLException (connection_, id_, errorClass, returnCode);
    		}
    		catch (DBDataStreamException e) {
	    		JDError.throwSQLException (JDError.EXC_INTERNAL, e);
		    }

			serverCommitMode_		= commitMode;
		}

	}

/**
*  Sets the cursor hold indicator.
*  @param hold The cursor hold value.
*  @exception SQLException If a database error occurs.
**/
void setHoldIndicator(String hold) throws SQLException         // @C1
{
   int result;
   if (hold.equalsIgnoreCase (JDProperties.CURSORHOLD_TRUE))
       holdIndicator_ = CURSOR_HOLD_TRUE;
   else if (hold.equalsIgnoreCase (JDProperties.CURSORHOLD_FALSE))
       holdIndicator_ = CURSOR_HOLD_FALSE;
   else {
       JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
       holdIndicator_ = -1;
   }
}

/**
Change the transaction isolation level using one of the
java.sql.Connection.TRANSACTION_* values.

@param  level   One of the java.sql.Connection.TRANSACTION_*
                values with the exception of TRANSACTION_NONE.

@exception      SQLException    If an error occurs, an
                                invalid or unsupported level
                                is input, or a transaction is
                                active.
**/
	void setIsolation (int level)
		throws SQLException
	{
	    // This is invalid if a transaction is active.
		if (active_)
			JDError.throwSQLException (JDError.EXC_TXN_ACTIVE);

        // We do not allow TRANSACTION_NONE at this time.
        if (level == Connection.TRANSACTION_NONE)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

		// Save the isolation level.
        currentCommitMode_     = mapLevelToCommitMode (level);
		currentIsolationLevel_ = level;

        // Set the commit mode on the server.
		setCommitMode (currentCommitMode_);
	}



/**
Take note that a statement has been executed.
**/
    void statementExecuted ()
    {
        // A transaction is now active if and only if auto-commit
        // mode is turned off.
        active_ = ! autoCommit_;
    }



}
