///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTransactionManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.SQLException;



/**
<p>This class manages transaction operations.

<p>In addition to issuing commits and rollbacks to the database,
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
// to the value as set on the system.
//
// External to this class, only the transaction isolation level
// is used.  Internal to this class, the transaction isolation
// level is mapped to the appropriate commit mode.  Note that
// not all transaction isolation levels are supported.
//
class JDTransactionManager
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // @C6A Server commit mode constants
  // @C6A *CS and *CHG are different numbers on the system than in the client; these are the 
  // @C6A numbers we need to send to the system in an AS400JDBCConnection.setServerAttributes().
  private static final int COMMIT_SERVER_MODE_CS_    = 1;  // TRANSACTION_READ_COMMITTED   //@C6A
  private static final int COMMIT_SERVER_MODE_CHG_   = 2;  // TRANSACTION_READ_UNCOMMITTED //@C6A

  // Client commit mode constants
  static final int    COMMIT_MODE_NOT_SET_  = -1;
  private static final int    COMMIT_MODE_NONE_   = 0;    // TRANSACTION_NONE
  private static final int    COMMIT_MODE_CHG_    = 1;    // TRANSACTION_READ_UNCOMMITTED
  private static final int    COMMIT_MODE_CS_     = 2;    // TRANSACTION_READ_COMMITTED
  private static final int    COMMIT_MODE_ALL_    = 3;    // TRANSACTION_REPEATABLE_READ
  private static final int        COMMIT_MODE_RR_         = 4;    // TRANSACTION_SERIALIZABLE

  private static final String[] COMMIT_MODE_          = { "NONE",
    "CHG",
    "CS",
    "ALL",
    "RR"};

  static final int      CURSOR_HOLD_FALSE = 0;     // @C1 @B1C
  static final int      CURSOR_HOLD_TRUE  = 1;     // @C1 @B1C

  private boolean             activeLocal_;               // Is a local transaction active?       @C4C
  private boolean             activeGlobal_;              // Is a global transaction active?      @C4A
  private boolean             autoCommit_;                // Is auto-commit on?
  private AS400JDBCConnection connection_;
  private int                 holdIndicator_;             // Current cursor hold indicator.  @C1
  private int                 currentCommitMode_;         // Current commit mode.
  private int                 currentIsolationLevel_;     // Current isolation level.
  private int                 id_;
  private int                 initialCommitMode_;         // Initial commit mode.
  private boolean             localAutoCommit_    = true;  // @C4A
  private boolean             localTransaction_   = true;  // @C4A
  // @C5D private boolean             newAutoCommitSupport_ = false;                             // @C5A
  private int                 serverCommitMode_;          // Commit mode on the system. Always base off of JDBC transaction isolation level (currentCommitMode_)
  private int                currentLocatorPersistence_=-1;  /*@ABA*/
  private int                requestedLocatorPersistence_ = -1; /*@ABA*/
  private boolean            serverAllowsLocatorPersistenceChange_ = true; 


/**
Constructor.  The transaction isolation level should
be initialized independently of this class.  However, since
commits and rollbacks always cause the database to revert
back to the initial commit mode, we need to remember what
this initial commit mode is, so we can predict how the
system is behaving.

@param  connection              Connection to the system.
@param  id                      The id.
@param  initialLevel            One of the Connection.TRANSACTION_*
                                values.

@exception      SQLException    If an invalid or unsupported
                                level is input.
**/
  JDTransactionManager (AS400JDBCConnection connection,
                        int id,
                        String initialLevel,
                        boolean autoCommit)    //@AC1
  throws SQLException
  {
    activeLocal_            = false;                                                // @C4C
    activeGlobal_           = false;                                                // @C4A
    autoCommit_             = autoCommit;   //@AC1
    connection_             = connection;
    holdIndicator_          = CURSOR_HOLD_TRUE;                                     // @C1
    id_                     = id;

    currentIsolationLevel_  = mapStringToLevel (initialLevel);
    currentCommitMode_      = mapLevelToCommitMode (currentIsolationLevel_);
    initialCommitMode_    = currentCommitMode_;
    //@AC1 (only set to *NONE if autocommit is on)
    if((connection_.newAutoCommitSupport_ == 1) && (autoCommit))          //@K64  If running under new auto commit support (V5R3 and higher), by default, auto commit is run under the *NONE isolation level
        serverCommitMode_ = COMMIT_MODE_NONE_;          //@K64
    else                                                //@K64
        serverCommitMode_   = currentCommitMode_;
  }



/**
Commit the current transaction.

@exception  SQLException    If an error occurs.
**/
  void commit ()
  throws SQLException
  {
    try
    {
      DBSQLRequestDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try
      {
        request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                               DBSQLRequestDS.FUNCTIONID_COMMIT, id_,
                                               DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);

        // Set cursor hold.
        //request.setHoldIndicator (1);                     // @C1
        request.setHoldIndicator(getHoldIndicator());       // @C1

        reply = connection_.sendAndReceive (request); //@P0C

        int errorClass = reply.getErrorClass();

        if (errorClass != 0) { 
            int returnCode = reply.getReturnCode();
          JDError.throwSQLException (connection_, id_, errorClass, returnCode);
        }
      }
      finally
      {
        if (request != null) { request.returnToPool(); request =null; } 
        if (reply != null) { reply.returnToPool(); reply = null; } // Only errorClass used from reply 
      }

    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    resetServer ();
    activeLocal_ = false;               // @C4C
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
    if (serverCommitMode_ == COMMIT_MODE_CHG_)         //@C6A
      return COMMIT_SERVER_MODE_CHG_;                //@C6A
    else if (serverCommitMode_ == COMMIT_MODE_CS_)     //@C6A
      return COMMIT_SERVER_MODE_CS_;                 //@C6A
    else                                               //@C6A
      return serverCommitMode_;
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



  boolean isGlobalActive ()               // @C4A
  {                                       // @C4A
    return activeGlobal_;               // @C4A
  }                                       // @C4A



// @C4C
/**
Is a local transaction active?

@return     true if a local transaction is active.
**/
  boolean isLocalActive ()
  {
    return activeLocal_;
  }



// @C4A
  boolean isLocalTransaction()
  {
    return localTransaction_;
  }


  // @C5D boolean isNewAutoCommitSupport()                     // @C5A
  // @C5D {                                                    // @C5A
  // @C5D     return newAutoCommitSupport_;                    // @C5A
  // @C5D }                                                    // @C5A


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
    switch (level)
    {
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
    else
    {
      JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      result = -1;
    }
    return result;
  }



// @C4A
/**
Marks a global transaction boundary.
**/
  void markGlobalTransactionBoundary()
  {
    activeGlobal_ = false;
  }




// @C2A
/**
Processes a commit on return indicator from a reply.
If this indicator is set, it means that the transaction
was committed or rolled back on the system and we should
mark the transaction as not being active.

@param reply            The reply.
**/
  void processCommitOnReturn(DBBaseReplyDS reply)
  throws DBDataStreamException
  {
    // If the system indicates commit-on-, reflect that fact.            
    DBReplySQLCA sqlca = reply.getSQLCA ();                             
    if (sqlca.getEyecatcherBit54())
      activeLocal_ = false;                                           // @C4C
  }



/**
Reset the system to the current commit mode.  This is useful
since after commits and rollbacks, the database automatically
reverts back to its initial commit mode.

@exception  SQLException    In an error occurs.
**/
  private void resetServer ()
  throws SQLException
  {
      if(connection_.newAutoCommitSupport_ == 0)                        //@KBA  If V5R2 or earlier do what we always have
      {
        // Model the database automatically reverting back to
        // its initial commit mode.
        serverCommitMode_ = initialCommitMode_;

        // Reset the server's commit mode.
        setCommitMode (currentCommitMode_);
      }
  }


  void resetXAServer()
  throws SQLException
  {
        resetServer();
  }


/**
Rollback the current transaction.  If auto-commit mode is
enabled, then do nothing.

@exception  SQLException    If an error occurs.
**/
  void rollback ()
  throws SQLException
  {
    try
    {
      DBSQLRequestDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try
      {
        request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                               DBSQLRequestDS.FUNCTIONID_ROLLBACK, id_,
                                               DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);

        // Set cursor hold.
        //request.setHoldIndicator (1);                     // @C1
        request.setHoldIndicator(getHoldIndicator());       // @C1

        reply = connection_.sendAndReceive (request); //@P0C

        int errorClass = reply.getErrorClass();

        if (errorClass != 0) { 
        	int returnCode = reply.getReturnCode();
          JDError.throwSQLException (connection_, id_, errorClass, returnCode);
        }
      }
      finally
      {
        if (request != null) { request.returnToPool(); request =null; } 
        if (reply != null) { reply.returnToPool(); reply = null; } // Only errorClass used from reply 
      }
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    resetServer ();
    activeLocal_ = false;   // @C4C
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
    // If we are in a distributed transaction, then reject a request           @C4A
    // to turn on auto-commit.  If we are supposed to turn it off,             @C4A
    // then just remember for when we are out of the distributed               @C4A
    // transaction, since the database won't let us do any transaction           @C4A
    // stuff during the distributed transaction.                               @C4A
    if (!localTransaction_)
    {                                               // @C4A
      if (autoCommit == true)                                             // @C4A
        JDError.throwSQLException (JDError.EXC_TXN_STATE_INVALID);      // @C4A
      else                                                                // @C4A
        localAutoCommit_ = false;                                       // @C4A
    }                                                                       // @C4A

    // If we are in the local transaction, just go ahead and set it.           @C4A
    else
    {                                                                  // @C4A

      // If going from false to true, then commit any outstanding
      // transaction.
      if (!autoCommit_ && autoCommit && activeLocal_)
      {                   // @C4C
        commit ();
        connection_.postWarning (JDError.getSQLWarning (JDError.WARN_TXN_COMMITTED));
      }

      // Save the auto commit state.
      autoCommit_ = autoCommit;

      // @C5D if (newAutoCommitSupport_) {                                                    // @C5A
      // @C5D     try {                                                                       // @C5A
      // @C5D 		DBSQLAttributesDS request = new DBSQLAttributesDS(                      // @C5A
      // @C5D 		    DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,                        // @C5A
      // @C5D 		    id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0, 0);                 // @C5A
      // @C5D         request.setAutoCommit(autoCommit ? 1 : 0);                              // @C5A
      // @C5D 		DBReplyRequestedDS reply = connection_.sendAndReceive(request);         // @C5A
      // @C5D 		int errorClass = reply.getErrorClass();                                 // @C5A
      // @C5D 		int returnCode = reply.getReturnCode();                                 // @C5A
      // @C5D 		if (errorClass != 0)                                                    // @C5A
      // @C5D 			JDError.throwSQLException(connection_, id_, errorClass, returnCode);// @C5A
      // @C5D 	}                                                                           // @C5A
      // @C5D 	catch (DBDataStreamException e) {                                           // @C5A
      // @C5D 		JDError.throwSQLException (JDError.EXC_INTERNAL, e);                    // @C5A
      // @C5D     }                                                                           // @C5A
      // @C5D }                                                                               // @C5A
      // @C5D else                                                                            // @C5A

      if(connection_.newAutoCommitSupport_ == 0)                                          //@KBA OS/400 V5R2 or earlier do what we always have
          setCommitMode (currentCommitMode_);
      else                                                                                //@KBA use new auto commit support
      {                                                                                   //@KBA
          DBSQLAttributesDS request = null;                                               //@KBA
          DBReplyRequestedDS reply = null;                                                //@KBA
          try                                                                                 //@KBA
          {                                                                                   //@KBA
              request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                         id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                         + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
              request.setAutoCommit(autoCommit ? 0xE8 : 0xD5);                                //@KBA  Set auto commit to on or off
              request.setCommitmentControlLevelParserOption(getIsolationLevel());             //@KBA  Set isolation level
              if(autoCommit_ && connection_.newAutoCommitSupport_ == 1)
                serverCommitMode_ = COMMIT_MODE_NONE_;
              else
                serverCommitMode_ = currentCommitMode_;
              
              boolean changed = setRequestLocatorPersistence(request, getIsolationLevel()); /*@ABA*/
              reply = connection_.sendAndReceive(request);                 //@KBA
              int errorClass = reply.getErrorClass();                                         //@KBA
              int returnCode = reply.getReturnCode();                                         //@KBA
              if(errorClass != 0 && changed) {
            	  // Try again for the case where the server does not support the change
            	  serverAllowsLocatorPersistenceChange_ = false; 
            	  request.returnToPool(); request =null; 
            	  reply.returnToPool(); reply = null; 
                  request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                          id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                          + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
                  request.setAutoCommit(autoCommit ? 0xE8 : 0xD5);                                //@KBA  Set auto commit to on or off
                  request.setCommitmentControlLevelParserOption(getIsolationLevel());             //@KBA  Set isolation level
                  if(autoCommit_ && connection_.newAutoCommitSupport_ == 1)
                	  serverCommitMode_ = COMMIT_MODE_NONE_;
                  else
                	  serverCommitMode_ = currentCommitMode_;
                  reply = connection_.sendAndReceive(request);                 //@KBA
                  errorClass = reply.getErrorClass();                                         //@KBA
                  returnCode = reply.getReturnCode();                                         //@KBA
                  
              }                  
            	  
              if (errorClass != 0) // @KBA
            	  JDError.throwSQLException(connection_, id_,
            			  errorClass, returnCode); // @KBA
              persistenceUpdated();
              
          }                                                                                   //@KBA
          catch(DBDataStreamException e)                                                      //@KBA
          {                                                                                   //@KBA
              JDError.throwSQLException(JDError.EXC_INTERNAL, e);                             //@KBA
          }                                                                                   //@KBA
          finally                                                                             //@KBA
          {                                                                                   //@KBA
              if (request != null) { request.returnToPool();  request =null;}                 //@KBA
              if (reply != null) { reply.returnToPool(); reply = null; }                      //@KBA
          }                                                                                   //@KBA
      }                                                                                       //@KBA
    }                                                                       // @C4A
  }

	/*
	 * Sets the locator persistence for a request. The persistence is typically
	 * set to 1 (Scoped to transaction) but must be set to 0 (scoped to cursor)
	 * if Auto commit is off and transaction isolation level isNONE.
	 * 
	 * Should be called whenever autocommit level or isolation level is changed.
	 * 
	 * @ABA
	 * 
	 * This should be followed by a call to persistenceUpdated() after the
	 * request returns successfully. This returns true if the request was
	 * changed
	 */
	private boolean setRequestLocatorPersistence(DBSQLAttributesDS request,
			int commitMode) throws DBDataStreamException {
		
		// transaction isolation is none, make sure locator persistence is
		// scoped to the cursor
		requestedLocatorPersistence_ = -1;
		if (serverAllowsLocatorPersistenceChange_) {
			if (commitMode == COMMIT_MODE_NONE_) {
				if (currentLocatorPersistence_ != 0) {
					request.setLocatorPersistence(0);
					requestedLocatorPersistence_ = 0;
					return true;
				}
			} else {
				if (currentLocatorPersistence_ != 1) {
					request.setLocatorPersistence(1);
					requestedLocatorPersistence_ = 1;
					return true;
				}
			}
		}
		return false; 
	}
  
  /* Indicate that the request to update the persistence was successful
   * 
   */
  private void persistenceUpdated() {
	  if (serverAllowsLocatorPersistenceChange_ && (requestedLocatorPersistence_ != -1)) { 
	     currentLocatorPersistence_ = requestedLocatorPersistence_;
	  }
  }

/**
Set the commit mode on the system.

@param      commitMode      The commit mode.

@exception  SQLException    If an error occurs.
**/
  private void setCommitMode (int commitMode)
  throws SQLException
  {
    // If auto-commit is on, then override the commit mode
    // to "NONE".
    if (autoCommit_) //@C5D && (!newAutoCommitSupport_))                                  // @C5C
      commitMode = COMMIT_MODE_NONE_;

    // Act only if the server commit mode is something other
    // then the what was requested.
    if (commitMode != serverCommitMode_)
    {

      JDSQLStatement sqlStatement = new JDSQLStatement (
                                                       "SET TRANSACTION ISOLATION LEVEL " + COMMIT_MODE_[commitMode]);

      // Send the execute immediate data stream.
      try
      {
        DBSQLRequestDS request = null; //@P0A
        DBReplyRequestedDS reply = null; //@P0A
        try
        {
          request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                                 DBSQLRequestDS.FUNCTIONID_EXECUTE_IMMEDIATE, id_,
                                                 DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                 + DBBaseRequestDS.ORS_BITMAP_SQLCA, 0);

          boolean extended = false;         //@540
          if(connection_.getVRM() >= JDUtilities.vrm540) extended = true;   //@540        
          //Bidi-HCG request.setStatementText (sqlStatement.toString (), connection_.unicodeConverter_, extended); // @C3C @P0C @540C
          request.setStatementText (sqlStatement.toString (), connection_.packageCCSID_Converter, extended); //Bidi-HCG
          request.setStatementType (sqlStatement.getNativeType ());

          // This statement certainly does not need a cursor, but some
          // versions of the system choke when none is specified.
          request.setCursorName ("MURCH", connection_.converter_); //@P0C

          reply = connection_.sendAndReceive (request); //@P0C

          int errorClass = reply.getErrorClass();
          int returnCode = reply.getReturnCode();

          if (errorClass != 0)
            JDError.throwSQLException (connection_, id_, errorClass, returnCode);
        }
        finally
        {
          if (request != null) { request.returnToPool(); request = null; } 
          if (reply != null) { reply.returnToPool(); reply = null; } // Only errorClass Used from reply 
        }
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException (JDError.EXC_INTERNAL, e);
      }

      serverCommitMode_   = commitMode;
    }

  }

/**
*  Sets the cursor hold indicator.
*  @param hold The cursor hold value.
*  @exception SQLException If a database error occurs.
**/
  void setHoldIndicator(String hold) throws SQLException         // @C1
  {
    if (hold.equalsIgnoreCase (JDProperties.CURSORHOLD_TRUE))
      holdIndicator_ = CURSOR_HOLD_TRUE;
    else if (hold.equalsIgnoreCase (JDProperties.CURSORHOLD_FALSE))
      holdIndicator_ = CURSOR_HOLD_FALSE;
    else
    {
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
    if (activeLocal_ && connection_.newAutoCommitSupport_ == 0)                                               // @C4C   //@KBC
       JDError.throwSQLException (JDError.EXC_TXN_STATE_INVALID);  // @C4C

    // @C7D We do not allow TRANSACTION_NONE at this time.
    // @C7D if (level == Connection.TRANSACTION_NONE)
    // @C7D   JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

    // Save the isolation level.
    currentCommitMode_     = mapLevelToCommitMode (level);
    currentIsolationLevel_ = level;

    // Set the commit mode on the system.
    if(connection_.newAutoCommitSupport_ == 0)                                  //@KBA OS/400 V5R2 or earlier do what we always have
        setCommitMode (currentCommitMode_);
    else                                                                        //@KBA use new auto commit and commit level support
    {                                                                                         //@KBA
        DBSQLAttributesDS request = null;                                               //@KBA
        DBReplyRequestedDS reply = null;                                                //@KBA
        int commitMode = currentCommitMode_;
          try                                                                                 //@KBA
          {                                                                                   //@KBA
              if(autoCommit_ && connection_.newAutoCommitSupport_ == 1)
                commitMode = COMMIT_MODE_NONE_;
              if(serverCommitMode_ != commitMode)                                            //@KBA
              {                                                                               //@KBA
                  request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                         id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                         + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
                  request.setCommitmentControlLevelParserOption(getIsolationLevel());             //@KBA
                  boolean changed = setRequestLocatorPersistence(request, getIsolationLevel()); /*@ABA*/

                  reply = connection_.sendAndReceive(request);                 //@KBA
                  int errorClass = reply.getErrorClass();                                         //@KBA
                  int returnCode = reply.getReturnCode();                                         //@KBA
                  if (errorClass != 0 && changed) {
                	  // Retry request if this fails. 
                	  serverAllowsLocatorPersistenceChange_ = false; 
                	  request.returnToPool(); request =null; 
                	  reply.returnToPool(); reply =null; 
                      request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                              id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                              + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
                      request.setCommitmentControlLevelParserOption(getIsolationLevel());             //@KBA

                      reply = connection_.sendAndReceive(request);                 //@KBA
                      errorClass = reply.getErrorClass();                                         //@KBA
                      returnCode = reply.getReturnCode();                                         //@KBA
                  }
                  if(errorClass != 0)                                                             //@KBA
                      JDError.throwSQLException(connection_, id_, errorClass, returnCode);        //@KBA
                  persistenceUpdated(); 
              }                                                                               //@KBA
          }                                                                                   //@KBA
          catch(DBDataStreamException e)                                                      //@KBA
          {                                                                                   //@KBA
              JDError.throwSQLException(JDError.EXC_INTERNAL, e);                             //@KBA
          }                                                                                   //@KBA
          finally                                                                             //@KBA
          {                                                                                   //@KBA
              if (request != null) { request.returnToPool(); request = null; }                //@KBA
              if (reply != null) { reply.returnToPool(); reply = null; }                      //@KBA
          }                                                                                   //@KBA
          serverCommitMode_ = commitMode;                                            //@KBA    Note:  This may not be what the user set it to, if the user want to always run auto commit with the *NONE isolation level
    }                                                                                         //@KBA
  }



// @C4A
/**
Sets whether to enable the local transaction. 
XA support needs this to be false so that commit(), rollback(), etc. 
can not be called directly on this object.

@param enableLocalTransaction    true to enable the local transaction, false otherwise.

@exception          SQLException    If the connection is not open
                                    or an error occurs.
**/
  void setLocalTransaction(boolean enableLocalTransaction) 
  throws SQLException
  {
    localTransaction_ = enableLocalTransaction;

    // Auto commit is disabled while in a distributed transaction.
    if (localTransaction_)
    {
      autoCommit_ = localAutoCommit_;  //prior to XA_START autocommit setting
      if(connection_.newAutoCommitSupport_ == 0)
          setCommitMode(currentCommitMode_);              // turn back on auto-commit
      else{
          DBSQLAttributesDS request = null;                                               
          DBReplyRequestedDS reply = null;                                                
          try                                                                                 
          {                                                                                   
              request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                         id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                         + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);  
              
              request.setAutoCommit( autoCommit_ ? 0xE8 : 0xD5);  //@PDC change autocommit setting to prior setting before XA_START  
             
              boolean changed = false; 
              if(connection_.newAutoCommitSupport_ == 1 && autoCommit_ == true)  //@PDC
              {           
                  request.setCommitmentControlLevelParserOption(COMMIT_MODE_NONE_);     
                  changed = setRequestLocatorPersistence(request, COMMIT_MODE_NONE_); /*@ABA*/

                  serverCommitMode_ = COMMIT_MODE_NONE_;
              }

              reply = connection_.sendAndReceive(request);                 
              int errorClass = reply.getErrorClass();                                         
              int returnCode = reply.getReturnCode();
              if (errorClass != 0 && changed) {
            	  // Retry request if this fails. 
            	  serverAllowsLocatorPersistenceChange_ = false; 
            	  request.returnToPool(); request = null; 
            	  reply.returnToPool();   reply =null; 
            	  
                  request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                          id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                          + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);  

                  request.setAutoCommit( autoCommit_ ? 0xE8 : 0xD5);  //@PDC change autocommit setting to prior setting before XA_START  

                  if(connection_.newAutoCommitSupport_ == 1 && autoCommit_ == true)  //@PDC
                  {           
                	  request.setCommitmentControlLevelParserOption(COMMIT_MODE_NONE_);     
                	  serverCommitMode_ = COMMIT_MODE_NONE_;
                  }
                  reply = connection_.sendAndReceive(request);                 
                  errorClass = reply.getErrorClass();                                         
                  returnCode = reply.getReturnCode();
              }
              if(errorClass != 0)                                                             
                  JDError.throwSQLException(connection_, id_, errorClass, returnCode); 
              persistenceUpdated();
          }                                                                                   
          catch(DBDataStreamException e)                                                      
          {                                                                                   
              JDError.throwSQLException(JDError.EXC_INTERNAL, e);                             
          }                                                                                   
          finally                                                                             
          {                                                                                   
              if (request != null) { request.returnToPool(); request = null; }                                     
              if (reply != null) { reply.returnToPool(); reply = null; } // Only errorClass used from reply                                         
          }                                                                                     
      }

    }
    else
    {
      localAutoCommit_ = autoCommit_;
      autoCommit_ = false;
      if(connection_.newAutoCommitSupport_ == 0)            //@KBA   System is v5r2 or less, do what we always have
          setCommitMode(currentCommitMode_);
      else                                                  //@KBA
      {
          DBSQLAttributesDS request = null;                                                   //@KBA
          DBReplyRequestedDS reply = null;                                                 //@KBA
          try                                                                                 //@KBA
          {                                                                                   //@KBA
              //auto commit is always false when we are in here so we will run under default or specified isolation level
              request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                     id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                     + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
              request.setAutoCommit(0xD5);                                                    //@KBA turn off auto commit
              if(serverCommitMode_ != currentCommitMode_)                                     //@KBA
                  request.setCommitmentControlLevelParserOption(getIsolationLevel());         //@KBA
              boolean changed = setRequestLocatorPersistence(request, getIsolationLevel());  /*@ABA*/

              reply = connection_.sendAndReceive(request);                 //@KBA
              int errorClass = reply.getErrorClass();                                         //@KBA
              int returnCode = reply.getReturnCode();                                         //@KBA
              if (errorClass != 0 && changed) {
            	  // Retry request if this fails. 
            	  serverAllowsLocatorPersistenceChange_ = false; 
            	  request.returnToPool(); request = null; 
            	  reply.returnToPool(); reply = null; 
                  request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                          id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                          + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);    //@KBA
                  request.setAutoCommit(0xD5);                                                    //@KBA turn off auto commit
                  if(serverCommitMode_ != currentCommitMode_)                                     //@KBA
                	  request.setCommitmentControlLevelParserOption(getIsolationLevel());         //@KBA

                  reply = connection_.sendAndReceive(request);                 //@KBA
                  errorClass = reply.getErrorClass();                                         //@KBA
                  returnCode = reply.getReturnCode();                                         //@KBA
              }            	  
              if(errorClass != 0)                                                             //@KBA
                  JDError.throwSQLException(connection_, id_, errorClass, returnCode);        //@KBA
              persistenceUpdated();
          }                                                                                   //@KBA
          catch(DBDataStreamException e)                                                      //@KBA
          {                                                                                   //@KBA
              JDError.throwSQLException(JDError.EXC_INTERNAL, e);                             //@KBA
          }                                                                                   //@KBA
          finally                                                                             //@KBA
          {                                                                                   //@KBA
              if (request != null) { request.returnToPool();  request = null; }               //@KBA
              if (reply != null) { reply.returnToPool(); reply = null; }                      //@KBA
          }                                                                                   //@KBA
            serverCommitMode_ = currentCommitMode_;                                             //@KBA
      }
    }
  }



  // @C5D void setNewAutoCommitSupport(boolean newAutoCommitSupport)          // @C5A
  // @C5D     throws SQLException                                             // @C5A
  // @C5D {                                                                   // @C5A
  // @C5D     newAutoCommitSupport_ = newAutoCommitSupport;                   // @C5A
  // @C5D                                                                     // @C5A
  // @C5D     if (newAutoCommitSupport)                                       // @C5A                              
  // @C5D         setAutoCommit (true);                                       // @C5A        
  // @C5D                     // The default - but we have to send it now     // @C5A   
  // @C5D                     // so that the system nows we want to use       // @C5A
  // @C5D                     // the new support.                             // @C5A
  // @C5D }                                                                   // @C5A



/**
Take note that a statement has been executed.
**/
  void statementExecuted ()
  {
    if (localTransaction_)                                                  // @C4A
      activeLocal_ = ! autoCommit_;
    else                                                                    // @C4A
      activeGlobal_ = true;                                               // @C4A
  }


  //@KBA
  /**
  Returns the isolation/commit level to send to the system.
  **/
  private int getIsolationLevel()           
  {
      int isolationLevel = currentCommitMode_;                                        
      //Server commit mode level is different than clients so map client commit level to appropriate server commit level
      if(isolationLevel == COMMIT_MODE_CHG_)                                          
          isolationLevel = COMMIT_SERVER_MODE_CHG_;                                   
      else if(isolationLevel == COMMIT_MODE_CS_)                                      
          isolationLevel = COMMIT_SERVER_MODE_CS_;                                    
      
      //if auto commit is on and user specified false for 'true autocommit property' or by default run under *NONE isolation level
      if(autoCommit_ && connection_.newAutoCommitSupport_ == 1)                         
          isolationLevel = COMMIT_MODE_NONE_;                                           

      return isolationLevel;
  }

}
