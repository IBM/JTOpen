///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.SQLClientInfoException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Savepoint;                        // @E10a                
import java.sql.Struct;
import java.util.Enumeration;               // @DAA
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;



/**
<p>The AS400JDBCConnection class provides a JDBC connection
to a specific DB2 for i5/OS database.  Use
DriverManager.getConnection() to create new AS400JDBCConnection
objects.

<p>There are many optional properties that can be specified
when the connection is created.  Properties can be specified either
as part of the URL or in a java.util.Properties object.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a> for a complete
list of properties supported by the AS400JDBCDriver.

<p>Note that a connection may contain at most 9999 open
statements.
**/
//
// Implementation notes:
//
// 1.  Each connection and statement has an "id" associated with
//     it.  All ids are unique within a connection, and this
//     uniqueness is maintained by the id table for each
//     connection.
//
//     The id is used as a convention for assigning each
//     connection and statement its own ORS (Operation Result
//     Set) on the i5/OS as well as assigning each statement
//     its own RPB (Request Parameter Block).
//
//     Every communication to the database requires a connection
//     and an id within that connection.
//
// 2.  It is a requirement that no finalize() methods need to
//     receive a reply from the i5/OS system.  Because of the way the
//     AS400Server class is implemented, certain scenarios where
//     this is the case will result in deadlock.  The AS400Server
//     class provides sendAndDiscardReply() specifically to avoid
//     this problem.
//
//     Within the JDBC driver, finalize() usually calls one or more
//     close() methods.  Therefore, this requirement is also
//     imposed on close() methods.
//
// 3.  All requests for the connection and the related objects in
//     its context should be sent via a variation of one of the
//     sendXXX() methods.  This makes debugging cleaner.
//    
public class AS400JDBCConnection extends ToolboxWrapper //@pdc jdbc40
implements Connection
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Turn this flag on to prevent this Connection object from establishing an actual connection to the i5/OS system.  This is useful when doing multi-threaded stress testing on the Toolbox's built-in JDBC connection pool manager, where we create/delete massive numbers of connections.
    // For production, this flag _must_ be set to 'false'.
    private static final boolean TESTING_THREAD_SAFETY = false;             //@CPMa

    // This is a compile time flag for doing simple
    // communications traces.
    //
    // The choices are:
    //   0 = No communication trace (for production code).
    //   1 = Only request and reply ids.
    //   2 = Request and reply ids and contents.
    //
    // Note that the LL (length) and parameter count for
    // requests will not be accurate, since they have not yet
    // been set at the time when the request is dumped.
    //
    private static final int            DEBUG_COMM_TRACE_       = 0;



    // This is a compile time flag for temporarily disabling
    // request chaining.  This can be useful when a request
    // is failing, but all we see is an error class == 7,
    // return code == -1000.  This means a chain request
    // failed.
    //
    // The choices are:
    //   true  = Enable request chaining (for production code).
    //   false = Disable request chaining.
    //
    // @E5D private static final boolean        DEBUG_REQUEST_CHAINING_ = true;



    // This is a compile time flag for forcing the use of
    // extended datastream formats.  This can be useful when
    // testing extended formats, but the i5/OS system is not reporting
    // the correct VRM.
    //
    // The choices are:
    //   true  = Force extended datastream formats.
    //   false = Decide based on system VRM (for production code).
    //
    // @E9D private static final boolean        FORCE_EXTENDED_FORMATS_ = false;
    
    // @F8 -- the key change is to put a 1 in the 7th position.  That 1 is the "ODBC" flag.
    //        The i5/OS passes it along to database to enable correct package caching of
    //        "where current of" statements.  This flag affects only package caching. 
    private static final String         CLIENT_FUNCTIONAL_LEVEL_= "V6R1M01   "; // @EDA F8c H2c pdc 610

    private static final int            DRDA_SCROLLABLE_CUTOFF_ = 129;        // @B1A
    private static final int            DRDA_SCROLLABLE_MAX_    = 255;        // @DAA
    private static final int            INITIAL_STATEMENT_TABLE_SIZE_ = 256;    // @DAA
    static final int            UNICODE_CCSID_          = 13488;      // @E3C

    // The max number of open statements per connection.  If this          @DAA
    // changes, then change the relevant sentence in the javadoc, too.     @DAA
    static final int            MAX_STATEMENTS_         = 9999;         // @DAC
    private final boolean[] assigned_ = new boolean[MAX_STATEMENTS_]; //@P0C

    static final int            DATA_COMPRESSION_NONE_  = 0;            // @ECA
    static final int            DATA_COMPRESSION_OLD_   = 1;            // @ECA
    static final int            DATA_COMPRESSION_RLE_   = 0x3832;       // @ECA @EIC @EJC


    // Private data.
    private AS400ImplRemote             as400_;
    private AS400                       as400PublicClassObj_; // Prevents garbage collection.
    //@P0D private BitSet                      assigned_;                      // @DAC
    private boolean                     cancelling_;                    // @E8A
    private Object                      cancelLock_ = new Object();     // @E8A
    private String                      catalog_;
    private boolean                     checkStatementHoldability_ = false;     // @F3A
    private boolean                     closing_;            // @D4A
            ConvTable                   converter_; //@P0C
    private int                         dataCompression_            = -1;               // @ECA
    private JDDataSourceURL             dataSourceUrl_;
    private boolean                     drda_;                          // @B1A
    private String                      defaultSchema_;
    private boolean                     extendedFormats_;
    // @E2D private ConverterImplRemote          graphicConverter_;
    // @E2D private boolean                     graphicConverterLoaded_;
    private Vector                      heldRequests_;                                  // @E5A
    private Object                      heldRequestsLock_           = new Object();     // @E5A
    private int                 holdability_  = AS400JDBCResultSet.HOLDABILITY_NOT_SPECIFIED; // @G4A
    private int                         id_;
    private AS400JDBCDatabaseMetaData   metaData_;
    private JDPackageManager            packageManager_;
    private JDProperties                properties_;
    private boolean                     readOnly_;
    //@P0D private BitSet                      requestPending_;                // @DAC
    //@P1Dprivate final boolean[] requestPending_ = new boolean[MAX_STATEMENTS_]; //@P0A
    private AS400Server                 server_;
    private int                         serverFunctionalLevel_;         // @E7A
    private String                      serverJobIdentifier_ = null;    // @E8A
    private SQLWarning                  sqlWarning_;
    private Vector                      statements_;                    // @DAC
    JDTransactionManager        transactionManager_;            //      @E10c
    static final ConvTable      unicodeConverter_ = new ConvTable13488();              // @E3A @P0C
    int                         vrm_;                           // @D0A @E10c

    // declare the user-supplied value for server trace.  The constants for
    // the definition of each bit in the bit map are defined in Trace.java
    private int                         traceServer_ = 0;               // @j1a

    // set to true if database host server tracing is started via the setDBHostServerTrace method
    private boolean databaseHostServerTrace_ = false;       // @2KR

    private boolean mustSpecifyForUpdate_ = true;                       // @j31

    //counter to keep track of number of open statements
    private int statementCount_ = 0;                                    //@K1A
    private boolean thousandStatements_ = false;                        //@K1A

    private String qaqqiniLibrary_ = null;                              //@K2A
                                                                                   
    //@KBA Specifies level of autocommit support to use.  
    // If V5R2 or earlier use old support of running SET TRANSACTION STATEMENTS (0)
    // If "true autocommit" connection property is false - run autocommit under *NONE isolation (1)
    // If "true autocommit" connection property is true - run with specified isolation (2)
    int newAutoCommitSupport_ = 1;                                      //@KBA  

    private boolean wrappedInsert_ = false;                             // @GKA
    //pda jdbc40 client info
    //Decided to not include these with JDProperties, but could be in future if needed, 
    //but would need to clone properties from datasource
    //Names for clientInfo identifiers.  DatabaseMetadata also will use these names
    static final String applicationNamePropertyName_ = "ApplicationName";
    static final String clientUserPropertyName_ = "ClientUser";
    static final String clientHostnamePropertyName_ = "ClientHostname";
    static final String clientAccountingPropertyName_ = "ClientAccounting";
    static final String clientProgramIDPropertyName_ = "ClientProgramID"; //@pda
    
    //clientInfo values
    private String applicationName_ = "";   //@pdc so can be added to Properties object in getClientInfo()
    private String clientUser_ = ""; //@pdc
    private String clientHostname_ = ""; //@pdc
    private String clientAccounting_ = ""; //@pdc
    private String clientProgramID_ = ""; //@pdc

    /**
    Static initializer.  Initializes the reply data streams
    that we expect to receive.
    **/
    static
    {
        // The database server will only return 1 type of reply.
        //@P0D         AS400Server.addReplyStream (new DBReplyRequestedDS (),
        AS400Server.addReplyStream(DBDSPool.getDBReplyRequestedDS(), //@P0A
                                   AS400.DATABASE);
    }



    // The default constructor reserved for use within the package.
    AS400JDBCConnection ()  //@A3A
    {
    }



    // @A3D  Deleted constructor:
    //    AS400JDBCConnection (JDDataSourceURL dataSourceUrl, JDProperties properties)
    //        throws SQLException



    // @E8A
    /**
    Cancels a statement within this connection.
    
    @param id   The ID of the statement.
    
    @exception              SQLException    If the statement cannot be executed.
    **/
    void cancel(int id)
    throws SQLException
    {
        // Lock out all other operations for this connection.
        synchronized(cancelLock_)
        {
            if (TESTING_THREAD_SAFETY) return; // in certain testing modes, don't contact the system
            cancelling_ = true;
            AS400JDBCConnection cancelConnection = null;
            try
            {
                // If the server job identifier was returned, and the system is at a
                // functional level 5 or greater, then use the job identifier to issue
                // the cancel from another connection.  Otherwise, do nothing.
                if ((serverJobIdentifier_ != null) && (serverFunctionalLevel_ >= 5))
                {

                    if (JDTrace.isTraceOn())
                        JDTrace.logInformation (this, "Cancelling statement " + id);

                    // Create another connection to issue the cancel.
                    cancelConnection = new AS400JDBCConnection();
                    
                    //AS400 system = new AS400(as400PublicClassObj_);
                    //cancelConnection.setSystem(system);

                    cancelConnection.setProperties(dataSourceUrl_, properties_, as400_, true);

                    // Send the cancel request.
                    DBSQLRequestDS request = null;
                    DBReplyRequestedDS reply = null;
                    try
                    {
                        request = DBDSPool.getDBSQLRequestDS(DBSQLRequestDS.FUNCTIONID_CANCEL, id_,
                                                             DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
                        request.setJobIdentifier(serverJobIdentifier_, converter_);
                        reply = cancelConnection.sendAndReceive (request);

                        int errorClass = reply.getErrorClass();
                        int returnCode = reply.getReturnCode();
                        if (errorClass != 0)
                            JDError.throwSQLException(this, id_, errorClass, returnCode);
                    }
                    catch (DBDataStreamException e)
                    {
                        JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
                    }
                    finally
                    {
                        if (request != null) request.inUse_ = false;
                        if (reply != null) reply.inUse_ = false;
                    }
                }
                else
                {
                    if (JDTrace.isTraceOn())
                        JDTrace.logInformation (this, "Cancel of statement " + id + " requested, but is not supported by system");
                }
            }
            finally
            {
                // always need to close the connection
                if (cancelConnection != null) {
                  try { cancelConnection.close(); }
                  catch (Throwable e) {}  // ignore any exceptions
                }

                // Let others back in.
                cancelling_ = false;
                cancelLock_.notifyAll();
            }
        }
    }



    /**
    Checks that the specified SQL statement can be executed.
    This decision is based on the access specified by the caller
    and the read only mode.
    
    @param   sqlStatement   The SQL statement.
    
    @exception              SQLException    If the statement cannot be executed.
    **/
    void checkAccess (JDSQLStatement sqlStatement)
    throws SQLException
    {
        String access = properties_.getString (JDProperties.ACCESS);

        // If we only have read only access, then anything other
        // than a SELECT can not be executed.
        if ((access.equalsIgnoreCase (JDProperties.ACCESS_READ_ONLY))
            && (! sqlStatement.isSelect ()))
            JDError.throwSQLException (this, JDError.EXC_ACCESS_MISMATCH);

        // If we have read call access, then anything other than
        // a SELECT or CALL can not be executed.
        if (((readOnly_)
             || ((access.equalsIgnoreCase (JDProperties.ACCESS_READ_CALL))))
            && (! sqlStatement.isSelect())
            && (! sqlStatement.isProcedureCall()))
            JDError.throwSQLException (this, JDError.EXC_ACCESS_MISMATCH);
    }



    // @E8A
    /**
    Checks to see if we are cancelling a statement.  If so, wait until the
    cancel is done.  If not, go ahead.
    **/
    private void checkCancel()
    {
        synchronized(cancelLock_)
        {
            if (cancelling_)
            {
                try
                {
                    cancelLock_.wait();
                }
                catch (InterruptedException e)
                {
                    // Ignore.
                }
            }
        }
    }


    //@F3A
    /**
    Checks if what the user passed in for holdability is valid.
    **/
    private boolean checkHoldabilityConstants (int holdability)
    {
        if ((holdability == AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT) || 
            (holdability == AS400JDBCResultSet.CLOSE_CURSORS_AT_COMMIT)  ||
            (holdability == AS400JDBCResultSet.HOLDABILITY_NOT_SPECIFIED))
        {
            return true;
        }
        return false;
    }


    /**
    Checks that the connection is open.  Public methods
    that require an open connection should call this first.
    
    @exception  SQLException    If the connection is not open.
    **/
    void checkOpen ()
    throws SQLException
    {
        if (TESTING_THREAD_SAFETY) return; // in certain testing modes, don't contact i5/OS system
        if (server_ == null)
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_NONE);
    }



    /**
    Clears all warnings that have been reported for the connection.
    After this call, getWarnings() returns null until a new warning
    is reported for the connection.
    
    @exception SQLException If an error occurs.
    **/
    public void clearWarnings ()
    throws SQLException
    {
        sqlWarning_ = null;
    }



    /**
    Releases the connection's resources immediately instead of waiting
    for them to be automatically released.  This rolls back any active
    transactions, closes all statements that are running in the context
    of the connection, and disconnects from the i5/OS system.
    
    @exception SQLException If an error occurs.
    **/
    //
    // Implementation notes:
    //
    // 1. We do not have to worry about thread synchronization here,
    //    since the AS400Server object handles it.
    //
    // 2. It is a requirement to not get replies during a finalize()
    //    method.  Since finalize() calls this method, this requirement
    //    applies here, too.
    //
    public void close ()
    throws SQLException
    {
        // @D4A
        // Avoid recursion.  When we close associated statements, they try
        // to close this connection.
        if (closing_) return;
        closing_ = true;

        // If this is already closed, then just do nothing.
        //
        // The spec does not define what happens when a connection
        // is closed multiple times.  The official word from the Sun
        // JDBC team is that "the driver's behavior in this case
        // is implementation defined.   Applications that do this are
        // non-portable."
        if (isClosed ())
            return;

        // partial close (moved rollback and closing of all the statements).     @E1
        pseudoClose();

        // Disconnect from the system.
        if (server_ != null)
        {

            // @B3  It turns out that we were closing the connection,
            // @B3  then the AS400Server object was in its disconnectServer()
            // @B3  method.  Since the AS400Server object needs to do other
            // @B3  cleanup, we still need to call it.

            // @B3D try {
            // @B3D   DBSQLEndCommDS request = new DBSQLEndCommDS (
            // @B3D       DBSQLEndCommDS.FUNCTIONID_END_COMMUNICATION,
            // @B3D       id_, 0, 0);
            // @B3D   send (request);
            // @B3D }
            // @B3D catch (Exception e) {
            // @B3D   JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
            // @B3D }




            as400_.disconnectServer (server_);
            server_ = null;
        }

        if (JDTrace.isTraceOn())
            JDTrace.logClose (this);
    }



    // @E4C
    /**
    Commits all changes made since the previous commit or
    rollback and releases any database locks currently held by
    the connection.  This has no effect when the connection
    is in auto-commit mode.
    
    <p>This method can not be called when the connection is part
    of a distributed transaction.  See <a href="AS400JDBCXAResource.html">
    AS400JDBCXAResource</a> for more information.
    
    @exception SQLException     If the connection is not open
                                or an error occurs.
    **/
    public void commit ()
    throws SQLException
    {
        checkOpen ();

        if (!transactionManager_.isLocalTransaction())                      // @E4A
            JDError.throwSQLException (this, JDError.EXC_TXN_STATE_INVALID);      // @E4A

        // Note: CPS 72CSHT support
        if (transactionManager_.getAutoCommit () && properties_.getBoolean(JDProperties.AUTOCOMMIT_EXCEPTION))  //@CE1
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);    //@CE1
        
        // Note:  Intuitively, it seems like if we are in
        //        auto-commit mode, that we should not need to
        //        do anything for an explicit commit.  However,
        //        somewhere along the line, the system gets
        //        confused, so we go ahead an send the commit
        //        anyway.

        transactionManager_.commit ();        

        // @F3 If cursor hold property is false, then mark the cursors closed.  Don't worry here 
        // @F3 about whether their statement level holdability is different; we will check that
        // @F3 within AS400JDBCStatement.markCursorsClosed().
        // @F3 If the user has changed any statement's holdability, then we need to go through
        // @F3 the enumeration to see if there are ones where we may need to close our cursors
        // @F3 or internal result sets.
        // @F3 Passing true to markCursorsClosed means we called this method from rollback().
        if (transactionManager_.getHoldIndicator() == JDTransactionManager.CURSOR_HOLD_FALSE // @B4A
            || (checkStatementHoldability_ && getVRM() >= JDUtilities.vrm520))  // @F3A
            markCursorsClosed(false);                                           // @B4A

        if(!getAutoCommit() && properties_.getBoolean(JDProperties.HOLD_STATEMENTS ))        //@KBL if auto commit is off, check to see if any statements have been partially closed //@PDA additional HOLD_STATEMENTS check
            markStatementsClosed(); //@KBL

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Transaction commit");
    }


    //@F3A
    /**
    Sets a flag for whether the user has changed the holdability for any of the statements that
    came from this connection.  As of JDBC 3.0, the user can specify a statement-level holdability 
    that is different from the statement-level holdability.  Rather than always going through all 
    of the statements to see if any of their holidabilities is different, we will mark this flag if 
    the user changes any of the statement holdabilities.

    @exception SQLException     If the connection is not open
                                or an error occurs.
    **/
    void setCheckStatementHoldability(boolean check)
    {
        checkStatementHoldability_ = check;
    }



    /**
    Corrects the result set type based on the result set concurrency
    and posts a warning.
    
    @param resultSetType            The result set type.
    @param resultSetConcurrency     The result set concurrency.
    @return                         The correct result set type.
    **/
    private int correctResultSetType (int resultSetType,
                                      int resultSetConcurrency)
    throws SQLException // @EGA
    {
        int newResultSetType = (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
                               ? ResultSet.TYPE_SCROLL_SENSITIVE : ResultSet.TYPE_SCROLL_INSENSITIVE;
        postWarning (JDError.getSQLWarning (JDError.WARN_OPTION_VALUE_CHANGED));
        return newResultSetType;
    }



    /**
    Creates a Statement object for executing SQL statements without
    parameters.  If the same SQL statement is executed many times, it
    is more efficient to use prepareStatement().
    
    <p>Result sets created using the statement will be type
    ResultSet.TYPE_FORWARD_ONLY and concurrency
    ResultSet.CONCUR_READ_ONLY.
    
    @return     The statement object.
    
    @exception  SQLException    If the connection is not open,
                            the maximum number of statements
                            for this connection has been reached, or an
                                error occurs.
    **/
    public Statement createStatement ()
    throws SQLException
    {
        return createStatement (ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY, getInternalHoldability());  //@G4C
    }



    // JDBC 2.0
    /**
    Creates a Statement object for executing SQL statements without
    parameters.  If the same SQL statement is executed many times, it
    is more efficient to use prepareStatement().
    
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @return                         The statement object.
    
    @exception      SQLException    If the connection is not open,
                                    the maximum number of statements
                                    for this connection has been reached, the
                                    result type or currency is not supported,
                                    or an error occurs.
    **/
    public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency)
    throws SQLException
    {
        return createStatement (resultSetType,                         //@G4A
                                resultSetConcurrency, getInternalHoldability());         //@G4A
        //@G4M Moved code to createStatement (int, int, int) 
    }


    //@G4A JDBC 3.0
    /**
    Creates a Statement object for executing SQL statements without
    parameters.  If the same SQL statement is executed many times, it
    is more efficient to use prepareStatement().
    
    <p>Full functionality of this method requires support in OS/400 V5R2  
    or i5/OS.  If connecting to OS/400 V5R1 or earlier, the value for 
    resultSetHoldability will be ignored.
        
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @param resultSetHoldability     The result set holdability.  Valid values are:
                                    <ul>
                                      <li>ResultSet.HOLD_CURSORS_OVER_COMMIT 
                                      <li>ResultSet.CLOSE_CURSORS_AT_COMMIT
                                    </ul>
    @return                         The statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                                for this connection has been reached, the
                                    result type, currency, or holdability is not supported,
                                    or an error occurs.
    @since Modification 5
    **/
    public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency,
                                      int resultSetHoldability)
    throws SQLException
    {
        // Validation.
        checkOpen ();
        if (! metaData_.supportsResultSetConcurrency (resultSetType, resultSetConcurrency))
            resultSetType = correctResultSetType (resultSetType, resultSetConcurrency);

        if (!checkHoldabilityConstants (resultSetHoldability))                  //@F3A
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);    //@F3A

        // Create the statement.
        int statementId = getUnusedId (resultSetType); // @B1C
        AS400JDBCStatement statement = new AS400JDBCStatement (this,
                                                               statementId, transactionManager_, packageManager_,
                                                               properties_.getString (JDProperties.BLOCK_CRITERIA),
                                                               properties_.getInt (JDProperties.BLOCK_SIZE),
                                                               properties_.getBoolean (JDProperties.PREFETCH),
                                                               properties_.getString (JDProperties.PACKAGE_CRITERIA), // @A2A
                                                               resultSetType, resultSetConcurrency, resultSetHoldability,  //@G4A
                                                               AS400JDBCStatement.GENERATED_KEYS_NOT_SPECIFIED);           //@G4A
        statements_.addElement(statement);          // @DAC
        statementCount_++;                           //@K1A
        if(thousandStatements_ == false && statementCount_ == 1000)              //@K1A
        {                                                                       //@K1A
            thousandStatements_ = true;                                         //@K1A
            //post warning                                                      //@K1A
            postWarning(JDError.getSQLWarning(JDError.WARN_1000_OPEN_STATEMENTS));  //@K1A
        }                                                                       //@K1A

        if (JDTrace.isTraceOn())                                            //@F4A
        {                                                                   //@F4A
            int size = statements_.size();                                  //@F4A
            if (size % 256 == 0)                                            //@F4A
            {                                                               //@F4A
                JDTrace.logInformation (this, "Warning: Open handle count now: " + size); //@F4A
            }                                                               //@F4A
        }                                                                   //@F4A

        return statement;
    }




    /**
    Outputs debug information for a request.  This should only be used
    for debugging the JDBC driver and is not intended for production code.
    
    @param   request     The request.
    **/
    private void debug (DBBaseRequestDS request)
    {
        if (DEBUG_COMM_TRACE_ >= 1)
            System.out.println ("Server request: "
                                + Integer.toString (request.getServerID(), 16).toUpperCase()
                                + ":" + Integer.toString (request.getReqRepID(), 16).toUpperCase()
                                + ".");
        if (DEBUG_COMM_TRACE_ >= 2)
            request.dump (System.out);
    }



    /**
    Outputs debug information for a reply.  This should only be used
    for debugging the JDBC driver and is not intended for production code.
    
    @param   reply     The reply.
    **/
    private void debug (DBReplyRequestedDS reply)
    {
        if (DEBUG_COMM_TRACE_ >= 1)
            System.out.println ("Server reply:   "
                                + Integer.toString (reply.getServerID(), 16).toUpperCase()
                                + ":" + Integer.toString (reply.getReturnDataFunctionId(), 16).toUpperCase()
                                + ".");
        if (DEBUG_COMM_TRACE_ >= 2)
            reply.dump (System.out);

        int errorClass = ((DBReplyRequestedDS) reply).getErrorClass();
        int returnCode = ((DBReplyRequestedDS) reply).getReturnCode();

        if (DEBUG_COMM_TRACE_ >= 1)
            if ((errorClass != 0) || (returnCode != 0))
                System.out.println ("Server error = " + errorClass + ":"
                                    + returnCode + ".");
    }



    /**
    Closes the connection if not explicitly closed by the caller.
    
    @exception   Throwable      If an error occurs.
    **/
    protected void finalize ()
    throws Throwable
    {
        if (! isClosed ())
            close ();

        super.finalize ();
    }



    /**
    Returns the AS400 object for this connection.
    
    @return     The AS400 object.
    **/
    AS400Impl getAS400 ()
    throws SQLException // @EGA
    {
        return as400_;
    }



    /**
    Returns the auto-commit state.
    
    @return     true if the connection is in auto-commit mode;
                false otherwise.
    
    @exception  SQLException    If the connection is not open.
    **/
    public boolean getAutoCommit ()
    throws SQLException
    {
        checkOpen ();
        return transactionManager_.getAutoCommit ();
    }



    /**
    Returns the catalog name.
    
    @return     The catalog name.
    
    @exception  SQLException    If the connection is not open.
    **/
    public String getCatalog ()
    throws SQLException
    {
        checkOpen ();
        return catalog_;
    }



    /**
    Returns the converter for this connection.
    
    @return     The converter.
    **/
    //@P0D    ConverterImplRemote getConverter ()
    //@P0D    throws SQLException // @EGA
    //@P0D    {
    //@P0D        return converter_;
    //@P0D    }



    /**
    Returns the converter for the specified CCSID, unless
    it is 0 or 65535 (i.e. probably set for a non-text field), in
    which case it returns the converter for this connection.
    This is useful for code that handles all types of fields
    in a generic manner.
    
    @param      ccsid       The CCSID.
    @return     The converter.
    
    @exception  SQLException    If the CCSID is not valid.
    **/
    ConvTable getConverter (int ccsid) //@P0C
    throws SQLException
    {
        try
        {
            if (ccsid == 0 || ccsid == 1 || ccsid == 65535 || ccsid == -1) return converter_; //@P0C
            //@P0D      switch (ccsid)
            //@P0D      {                                                                 // @E3A
            //@P0D        case 65535:   //@ELC                                                            // @E3A
            //@P0D        case 0:                                                                         // @E3A
            //@P0D        case 1:                                                                         // @E3A
            //@P0D          return converter_;                                                          // @E3A
            //@P0D        case UNICODE_CCSID_:                                                            // @E3A
            //@P0D          if (unicodeConverter_ == null)                                              // @E3A
            //@P0D            unicodeConverter_ = ConverterImplRemote.getConverter(13488, as400_);    // @E3A
            //@P0D          return unicodeConverter_;                                                   // @E3A
            //@P0D        default:                                                                        // @E3A
            //@P0D          return ConverterImplRemote.getConverter (ccsid, as400_);                    // @E3C
            //@P0D      }                                                                               // @E3A
            return ConvTable.getTable(ccsid, null); //@P0A
        }
        catch (UnsupportedEncodingException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }


    // @ECA
    /**
    Returns the style of data compression.
    
    @return The style of data compression.  Possible values are DATA_COMPRESSION_NONE_,
            DATA_COMPRESSION_OLD_, and DATA_COMPRESSION_RLE_.
    **/
    int getDataCompression()                                                                // @ECA
    {                                                                                       // @ECA
        return dataCompression_;                                                            // @ECA
    }                                                                                       // @ECA



    /**
    Returns the default schema.
    
    @return     The default schema, or QGPL if none was
                specified.
    **/
    String getDefaultSchema ()
    throws SQLException // @EGA
    {
        return((defaultSchema_ == null) ? "QGPL" : defaultSchema_);
    }


    //@DELIMa
    /**
    Returns the default schema.

    @param returnRawValue Indicates what to return if default schema has not been set.  If true, return raw value; if false, then return QGPL rather than null.
    @return     The default schema.  If returnRawValue==false and no default schema was specified, then return QGPL rather than null.
    **/
    String getDefaultSchema (boolean returnRawValue)
    throws SQLException
    {
      return((returnRawValue || defaultSchema_ != null) ? defaultSchema_ : "QGPL");
    }


    //@G4A JDBC 3.0
    /**
    Returns the holdability of ResultSets created from this connection.
    
    @return     The cursor holdability.  Valid values are ResultSet.HOLD_CURSORS_OVER_COMMIT and 
                ResultSet.CLOSE_CURSORS_AT_COMMIT.  The holdability is derived in this order
                of precedence:
                <ul>
                <li>1.  The holdability specified using the method setHoldability(int)
                if this method was called.
                <li>2.  The value of the <code> cursor hold </code> 
                <a href="doc-files/JDBCProperties.html" target="_blank">driver property</a>. </ul>  
                Full functionality of #1 requires support in OS/400 
                V5R2 or i5/OS.  If connecting to OS/400 V5R1 or earlier, 
                the value specified on this method will be ignored and the default holdability
                will be the value of #2.
    
    @exception  SQLException    If the connection is not open.
    @since Modification 5
    **/
    public int getHoldability ()
    throws SQLException
    {
        checkOpen ();
        // If holdability has been set, return its value. 
        if ((holdability_ == AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT) || 
            (holdability_ == AS400JDBCResultSet.CLOSE_CURSORS_AT_COMMIT))
        {
            return holdability_;
        }
        // Else, holdability either equals AS400JDBCResultSet.HOLDABILITY_NOT_SPECIFIED
        // or has an incorrect value (shouldn't be able to happen).
        // Return the holdability determined by seeing what the cursor hold driver property
        // was set to.  Default is HOLD_CURSORS_AT_COMMIT.
        else
        {
            if (transactionManager_.getHoldIndicator() == JDTransactionManager.CURSOR_HOLD_TRUE)
                return AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT;
            else if (transactionManager_.getHoldIndicator() == JDTransactionManager.CURSOR_HOLD_FALSE)
                return AS400JDBCResultSet.CLOSE_CURSORS_AT_COMMIT;
            // Hold indicator will be set to -1 if the user gave us a bad number in setHoldIndicator().
            // We threw an exception there, so throw another exception here, then return default
            // value for driver.
            else
            {
                JDError.throwSQLException (this, JDError.EXC_INTERNAL);
                return AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT;
            }
        }
    }


    //@DELIMa
    /**
     Returns the ID of the connection.
     @return The connection ID.
     **/
    int getID()
    {
      return id_;
    }


    //@G4A JDBC 3.0
    /**
    Returns the holdability of ResultSets created from this connection.  
    Use this method internally to return the value specified if the user has called
    setHoldability(int), or HOLDABILITY_NOT_SPECIFIED if that 
    method hasn't been called, meaning to use the old behavior and not the new code 
    point for cursor holdability.
    
    @return     The cursor holdability.  Valid values are 
                AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT,  
                AS400JDBCResultSet.CLOSE_CURSORS_AT_COMMIT, 
                and AS400JDBCResultSet.HOLDABILITY_NOT_SPECIFIED.
    
    @since Modification 5
    **/
    int getInternalHoldability ()
    {
        return holdability_;
    }



    // @E2D /**
    // @E2D Returns the graphic converter for this connection.
    // @E2D
    // @E2D @return     The graphic converter.
    // @E2D
    // @E2D @exception  SQLException    If no graphic converter was loaded.
    // @E2D **/
    // @E2D //
    // @E2D // Implementation note:
    // @E2D //
    // @E2D // * Graphic data is pure double-byte, so we will need a
    // @E2D //   different converter for that.  If there is no associated
    // @E2D //   double-byte CCSID, or the converter can not be loaded,
    // @E2D //   then we should throw an exception.  We wait to load this,
    // @E2D //   since the majority of callers do not need this converter.
    // @E2D //
    // @E2D     ConverterImplRemote getGraphicConverter ()
    // @E2D         throws SQLException
    // @E2D     {
    // @E2D         // If the graphic converter has not yet been loaded,
    // @E2D         // then do so.
    // @E2D         if (graphicConverterLoaded_ == false) {
    // @E2D             int serverGraphicCCSID = ExecutionEnvironment.getAssociatedDbcsCcsid (converter_.getCcsid ());
    // @E2D             if (serverGraphicCCSID != -1) {
    // @E2D                 try {
    // @E2D                       graphicConverter_ = ConverterImplRemote.getConverter (serverGraphicCCSID, as400_);
    // @E2D                  }
    // @E2D                  catch (UnsupportedEncodingException e) {
    // @E2D                      graphicConverter_ = null;
    // @E2D                  }
    // @E2D             }
    // @E2D
    // @E2D             if (JDTrace.isTraceOn ()) {
    // @E2D                 if (graphicConverter_ != null)
    // @E2D                     JDTrace.logInformation (this, "Server graphic CCSID = " + serverGraphicCCSID);
    // @E2D                 else
    // @E2D                     JDTrace.logInformation (this, "No graphic CCSID was loaded");
    // @E2D             }
    // @E2D         }
    // @E2D
    // @E2D         // Return the graphic converter, or throw an exception.
    // @E2D         if (graphicConverter_ == null)
    // @E2D             JDError.throwSQLException (this, JDError.EXC_CCSID_INVALID);
    // @E2D         return graphicConverter_;
    // @E2D     }



    /**
    Returns the DatabaseMetaData object that describes the
    connection's tables, supported SQL grammar, stored procedures,
    capabilities and more.
    
    @return     The metadata object.
    
    @exception  SQLException    If an error occurs.
    **/
    public DatabaseMetaData getMetaData ()
    throws SQLException
    {
        // We allow a user to get this object even if the
        // connection is closed.

        return metaData_;
    }



    /**
    Returns the connection properties.
    
    @return    The connection properties.
    **/
    JDProperties getProperties ()
    throws SQLException // @EGA
    {
        return properties_;
    }



    // @E8A
    /**
    Returns the job identifier of the host server job corresponding to this connection.
    Every JDBC connection is associated with a host server job on the i5/OS system.  The
    format is:
    <ul>
      <li>10 character job name
      <li>10 character user name
      <li>6 character job number
    </ul>
    
    <p>Note: Since this method is not defined in the JDBC Connection interface,
    you typically need to cast a Connection object to AS400JDBCConnection in order
    to call this method:
    <blockquote><pre>
    String serverJobIdentifier = ((AS400JDBCConnection)connection).getServerJobIdentifier();
    </pre></blockquote>
    
    @return The server job identifier, or null if not known.
    **/
    public String getServerJobIdentifier()                              // @E8A
    {                                                                   // @E8A
        return serverJobIdentifier_;                                    // @E8A
    }                                                                   // @E8A




    int getServerFunctionalLevel()                                      // @EEA
    {                                                                   // @EEA
        return serverFunctionalLevel_;                                  // @EEA
    }                                                                   // @EEA


    // @EHA
    /**
    Returns the system object which is managing the connection to the system.
    
    <p>Note: Since this method is not defined in the JDBC Connection interface,
    you typically need to cast a Connection object to AS400JDBCConnection in order
    to call this method:
    <blockquote><pre>
    AS400 system = ((AS400JDBCConnection)connection).getSystem();
    </pre></blockquote>
    
    @return The system.
    **/
    // Implementation note:  Don't use this object internally because we could be running in a proxy environment
    // The purpose of this method is to simply hold the full AS400 object so it can be retrieved from the Connection
    public AS400 getSystem()                                            // @EHA
    {                                                                   // @EHA
        return as400PublicClassObj_;                                    // @EHA
    }                                                                   // @EHA





    /**
    Returns the transaction isolation level.
    
    @return     The transaction isolation level.  Possible
                values are:
                <ul>
                <li>TRANSACTION_NONE
                <li>TRANSACTION_READ_UNCOMMITTED
                <li>TRANSACTION_READ_COMMITTED
                   <li>TRANSACTION_REPEATABLE_READ
                </ul>
    
    @exception  SQLException    If the connection is not open.
    **/
    public int getTransactionIsolation ()
    throws SQLException
    {
        checkOpen ();
        return transactionManager_.getIsolation ();
    }



    JDTransactionManager getTransactionManager()                                // @E4A
    {                                                                           // @E4A
        return transactionManager_;                                             // @E4A
    }                                                                           // @E4A



    // JDBC 2.0
    /**
    Returns the type map.
    
    <p>This driver does not support the type map.
    
    @return     The type map.
    
    @exception  SQLException    This exception is always thrown.
    **/
    public Map getTypeMap ()
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }



    // @B1C
    /**
    Returns the next unused id.
    
    @param      resultSetType       The result set type.  This is
                                    relevant only when the connection
                                    is being used for DRDA.
    @return                         The next unused id.
    **/
    //
    // Implementation note:  This method needs to be synchronized
    // so that the same id does not get assigned twice.
    //
    private int getUnusedId (int resultSetType) //@P0C
    throws SQLException
    {
      synchronized(assigned_) //@P1A
      {
        // Note: We will always assume id 0 is being used,
        // since that represents the connection itself.

        // If this connection is being used for DRDA, then we
        // must use statement ids of 1-128 for non-scrollable
        // cursors and 129-255 for scrollable cursors.
        if (drda_)
        {
            if (resultSetType == ResultSet.TYPE_FORWARD_ONLY)
            {
                for (int i = 1; i < DRDA_SCROLLABLE_CUTOFF_; ++i)
                {
                    //@P0Dif (assigned_.get(i) == false)
                    //@P0D{                                    // @DAC
                    //@P0D  assigned_.set(i);                                               // @DAC
                    //@P0D  return i;
                    //@P0D}
                    if (!assigned_[i]) //@P0A
                    {
                        assigned_[i] = true; //@P0A
                        return i; //@P0A
                    }
                }
            }
            else
            {
                for (int i = DRDA_SCROLLABLE_CUTOFF_; i < DRDA_SCROLLABLE_MAX_; ++i)
                {  // @DAC
                    //@P0Dif (assigned_.get(i) == false)
                    //@P0D{                                    // @DAC
                    //@P0D  assigned_.set(i);                                               // @DAC
                    //@P0D  return i;
                    //@P0D}
                    if (!assigned_[i]) //@P0A
                    {
                        assigned_[i] = true; //@P0A
                        return i; //@P0A
                    }
                }
            }
        }

        // If this connection is NOT being used for DRDA, then
        // we can use any statement id.
        else
        {
            for (int i = 1; i < MAX_STATEMENTS_; ++i)
            {
                //@P0Dif (assigned_.get(i) == false)
                //@P0D{                                    // @DAC
                //@P0D  assigned_.set(i);                                               // @DAC
                //@P0D  return i;
                //@P0D}
                if (!assigned_[i]) //@P0A
                {
                    assigned_[i] = true; //@P0A
                    return i; //@P0A
                }
            }
        }

        // All ids are being used.
        JDError.throwSQLException (this, JDError.EXC_MAX_STATEMENTS_EXCEEDED);
        return -1;                 
      }  
    }



    // @j31a new method -- Must the user have "for update" on their
    //       SQL statement to guarantee an updatable cursor?  The answer is
    //       no for v5r2 and v5r1 systems with a PTF.  For V5R1 systems
    //       without the PTF, v4r5, and earlier, the answer is yes.  
    boolean getMustSpecifyForUpdate ()
    {
       return mustSpecifyForUpdate_;
    }





    /**
    Returns the URL for the connection's database.
    
    @return      The URL for the database.
    **/
    String getURL ()
    throws SQLException // @EGA
    {
        return dataSourceUrl_.toString ();
    }



    /**
    Returns the user name as currently signed on to the system.
    
    @return      The user name.
    **/
    String getUserName ()
    throws SQLException // @EGA
    {
        if (TESTING_THREAD_SAFETY) // in certain testing modes, don't contact i5/OS system
        {
          String userName = as400_.getUserId ();
          if (userName == null || userName.length() == 0) {
            userName = as400PublicClassObj_.getUserId();
          }
          return userName;
        }

        return as400_.getUserId ();
    }



    int getVRM()                                            // @D0A
    throws SQLException // @EGA
    {                                                       // @D0A
        return vrm_;                                        // @D0A
    }                                                       // @D0A



    /**
    Returns the first warning reported for the connection.
    Subsequent warnings may be chained to this warning.
    
    @return     The first warning or null if no warnings
                have been reported.
    
    @exception  SQLException    If an error occurs.
    **/
    public SQLWarning getWarnings ()
    throws SQLException
    {
        return sqlWarning_;
    }



    /**
    Indicates if the specified cursor name is already used
    in the connection.
    
    @return     true if the cursor name is already used;
                false otherwise.
    **/
    boolean isCursorNameUsed (String cursorName)
    throws SQLException // @EGA
    {
        Enumeration list = statements_.elements();                                                          // @DAA
        while (list.hasMoreElements())
        {                                                                     // @DAC
            if (((AS400JDBCStatement)list.nextElement()).getCursorName().equalsIgnoreCase(cursorName))      // @DAC
                return true;
        }
        return false;
    }



    /**
    Indicates if the connection is closed.
    
    @return     true if the connection is closed; false
                otherwise.
    
    @exception  SQLException    If an error occurs.
    **/
    public boolean isClosed ()
    throws SQLException
    {
        if (TESTING_THREAD_SAFETY) return false; // in certain testing modes, don't contact i5/OS system

        if (server_ == null)                        // @EFC
            return true;                            // @EFA
        if (!server_.isConnected())
        {               // @EFA
            server_ = null;                         // @EFA
            return true;                            // @EFA
        }                                           // @EFA
        else                                        // @EFA
            return false;                           // @EFA
    }



    /**
    Indicates if the connection is in read-only mode.
    
    @return     true if the connection is in read-only mode;
                false otherwise.
    
    @exception  SQLException    If the connection is not open.
    **/
    public boolean isReadOnly ()
    throws SQLException
    {
        checkOpen ();
        return((readOnly_) || isReadOnlyAccordingToProperties());    // @CPMc
    }

    // Called by AS400JDBCPooledConnection.
    boolean isReadOnlyAccordingToProperties()
      throws SQLException
    {
        checkOpen ();
        return((properties_.getString (JDProperties.ACCESS).equalsIgnoreCase (JDProperties.ACCESS_READ_ONLY))
            || (properties_.getString (JDProperties.ACCESS).equalsIgnoreCase (JDProperties.ACCESS_READ_CALL)));
    }


    // @B4A
    /**
    Marks all of the cursors as closed.
    
    @param  isRollback True if we called this from rollback(), false if we called this from commit().
    **/
    private void markCursorsClosed(boolean isRollback)  //@F3C
    throws SQLException                  //@F2A
    {
        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Testing to see if cursors should be held.");  //@F3C

        Enumeration list = statements_.elements();                              // @DAA
        while (list.hasMoreElements())                                           // @DAC
        {                                                                       //@KBL
            AS400JDBCStatement statement = (AS400JDBCStatement)list.nextElement();  //@KBL
            //@KBLD ((AS400JDBCStatement)list.nextElement()).markCursorClosed(isRollback); // @DAC @F3C 
            // If the statement is held open, all of the result sets have already been closed
            if(!statement.isHoldStatement())                                        //@KBL
                statement.markCursorClosed(isRollback);                             //@KBL
        }                                                                           //@KBL
    }

    //@KBL
    /*
    If a statement associated with locators has been partially closed, finish closing the statement object.
    A statement may become partially closed if the user closed the statement and set the "hold statements" connection 
    property to true when making the connection.  Additionally, the statement must have been used to access a locator.
    */
    private void markStatementsClosed()
    {
        if(!statements_.isEmpty())                                                           
            {                                                                                    
                // Make a clone of the vector, since it will be modified as each statement       
                // closes itself.                                                                
                // @KBL Close any statements the user called close on that were associated with locators.
                Vector statements = (Vector)statements_.clone();                                 
                Enumeration list = statements.elements();                                        
                while (list.hasMoreElements())                                                    
                {                                                                                
                    AS400JDBCStatement statement = (AS400JDBCStatement)list.nextElement();       
                    try                                                                          
                    {                                                                            
                        if(statement.isHoldStatement())                                          
                        {                                                                        
                            statement.setAssociatedWithLocators(false);                          
                            statement.finishClosing();                                                    
                        }                                                                        
                    }                                                                            
                    catch (SQLException e)                                                       
                    {                                                                            
                        if (JDTrace.isTraceOn())                                                  
                            JDTrace.logInformation (this, "Closing statement after rollback failed: " + e.getMessage()); 
                    }                                                                            
                }                                                                                
            }                                                                                    
    }

    //@GKA
    // Note:  This method is used when the user supplies either the column indexes or names
    // to the execute/executeUpdate/prepareStatement method.
    /*
    * Prepares and executes the statement needed to retrieve generated keys.
    */
    String makeGeneratedKeySelectStatement(String sql, int[] columnIndexes, String[] columnNames)
    throws SQLException
    {
        if(columnIndexes != null)
        {
            //verify there is a column index in the specified array
            if(columnIndexes.length == 0)
                JDError.throwSQLException(JDError.EXC_ATTRIBUTE_VALUE_INVALID);

            //Prepare a statement in order to retrieve the column names associated with the indexes specified in the array
            //wrapper the statement with a select * from final table
            StringBuffer selectAll = new StringBuffer("SELECT * FROM FINAL TABLE(");
            selectAll.append(sql);
            selectAll.append(")");
            PreparedStatement genPrepStat = prepareStatement(selectAll.toString());

            // retrieve the JDServerRow object associated with this statement.  It contains the column name info.
            JDServerRow results = ((AS400JDBCPreparedStatement)genPrepStat).getResultRow();
            columnNames = new String[columnIndexes.length];
            try{
                for(int j=0; j<columnIndexes.length; j++)
                {
                    columnNames[j] = results.getFieldName(columnIndexes[j]);
                }
            }
            catch(SQLException e){
                // If this occurs there is not a column name for the index, throw an exception
                genPrepStat.close();
                JDError.throwSQLException(JDError.EXC_ATTRIBUTE_VALUE_INVALID);
            }

            // close the PreparedStatement
            genPrepStat.close();
        }

        //verify there is a column name  specified in the array
        if(columnNames == null || columnNames.length == 0)
            JDError.throwSQLException(JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //wrapper the statement with a select xxx from final table where xxx is replaced with the appropriate column(s)
        StringBuffer selectFrom = new StringBuffer("SELECT " + columnNames[0]);  //we verified above that there is at least one name
        for(int i=1; i<columnNames.length; i++)
        {
            selectFrom.append(",");
            selectFrom.append(columnNames[i]);
        }
        selectFrom.append(" FROM FINAL TABLE(");
        selectFrom.append(sql);
        selectFrom.append(")");

        return selectFrom.toString();
    }

    //@GKA
    // Note:  This method is used when the user supplies ResultSet.RETURN_GENERATED_KEYS 
    // to the execute/executeUpdate method.
    /*
    * Prepares and executes the statement needed to retrieve generated keys
    */ 
    String makeGeneratedKeySelectStatement(String sql)
    throws SQLException
    {
        StringBuffer selectFrom = new StringBuffer("SELECT *SQLGENCOLUMNS FROM FINAL TABLE(");
        selectFrom.append(sql);
        selectFrom.append(")");

        return selectFrom.toString();
        
    }

    /**
    Returns the native form of an SQL statement without
    executing it. The JDBC driver converts all SQL statements
    from the JDBC SQL grammar into the native DB2 for i5/OS
    SQL grammar prior to executing them.
    
    @param  sql     The SQL statement in terms of the JDBC SQL grammar.
    @return         The translated SQL statement in the native
                    DB2 for i5/OS SQL grammar.
    
    @exception      SQLException    If the SQL statement has a syntax error.
    **/
    public String nativeSQL (String sql)
    throws SQLException
    {
        JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                          properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                          properties_.getString (JDProperties.PACKAGE_CRITERIA), this); // @A2A @G4A
        return sqlStatement.toString ();
    }



    /**
    Notifies the connection that a statement in its context has
    been closed.
    
    @param   statement   The statement.
    @param   id          The statement's id.
    **/
    void notifyClose (AS400JDBCStatement statement, int id)
    throws SQLException // @EGA
    {
        statements_.removeElement(statement);           // @DAC
        statementCount_--;                              //@K1A  Decrement statement counter
        //@P0D assigned_.clear(id);                            // @DAC
        synchronized(assigned_) //@P1A
        {
        assigned_[id] = false; //@P0A
    }
    }


    // @A3D - Moved this logic up into AS400JDBCDriver:
    //    private void open ()
    //        throws SQLException



    /**
    Posts a warning for the connection.
    
    @param   sqlWarning  The warning.
    **/
    void postWarning (SQLWarning sqlWarning)
    throws SQLException // @EGA
    {
        if (sqlWarning_ == null)
            sqlWarning_ = sqlWarning;
        else
            sqlWarning_.setNextWarning (sqlWarning);
    }



    /**
    Precompiles an SQL stored procedure call with optional input
    and output parameters and stores it in a CallableStatement
    object.  This object can be used to efficiently call the SQL
    stored procedure multiple times.
    
    <p>Result sets created using the statement will be type
    ResultSet.TYPE_FORWARD_ONLY and concurrency
    ResultSet.CONCUR_READ_ONLY.
    
    @param  sql     The SQL stored procedure call.
    @return         The callable statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                               for this connection has been reached,  or an
                                    error occurs.
    **/
    public CallableStatement prepareCall (String sql)
    throws SQLException
    {
        return prepareCall (sql, ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY, getInternalHoldability()); //@G4A
    }



    // JDBC 2.0
    /**
    Precompiles an SQL stored procedure call with optional input
    and output parameters and stores it in a CallableStatement
    object.  This object can be used to efficiently call the SQL
    stored procedure multiple times.
    
    @param sql                      The SQL statement.
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @return                         The prepared statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                                for this connection has been reached, the
                                    result type or currency is not valid,
                                    or an error occurs.
    **/
    public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency)
    throws SQLException
    {
        return prepareCall(sql, resultSetType, resultSetConcurrency, 
                           getInternalHoldability());   //@G4A
        //@G4M Moved code below
    }


    //@G4A JDBC 3.0
    /**
    Precompiles an SQL stored procedure call with optional input
    and output parameters and stores it in a CallableStatement
    object.  This object can be used to efficiently call the SQL
    stored procedure multiple times.
    
    <p>Full functionality of this method requires support in OS/400 V5R2  
    or i5/OS.  If connecting to OS/400 V5R1 or earlier, the value for 
    resultSetHoldability will be ignored.
    
    @param sql                      The SQL statement.
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @return                         The prepared statement object.
    @param resultSetHoldability     The result set holdability.  Valid values are:
                                    <ul>
                                      <li>ResultSet.HOLD_CURSORS_OVER_COMMIT 
                                      <li>ResultSet.CLOSE_CURSORS_AT_COMMIT
                                    </ul>
    @exception      SQLException    If the connection is not open,
                                    the maximum number of statements
                                    for this connection has been reached, the
                                    result type, currency, or holdability is not valid,
                                    or an error occurs.
    @since Modification 5
    **/
    public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency,
                                          int resultSetHoldability)
    throws SQLException
    {
        // Validation.
        checkOpen ();
        if (! metaData_.supportsResultSetConcurrency (resultSetType, resultSetConcurrency))
            resultSetType = correctResultSetType (resultSetType, resultSetConcurrency);

        if (!checkHoldabilityConstants(resultSetHoldability))                   //@F3A
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);    //@F3A

        // Create the statement.
        JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                          properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                          properties_.getString (JDProperties.PACKAGE_CRITERIA), this); // @A2A @G4A
        int statementId = getUnusedId (resultSetType); // @B1C
        AS400JDBCCallableStatement statement = new AS400JDBCCallableStatement (this,
                                                                               statementId, transactionManager_, packageManager_,
                                                                               properties_.getString (JDProperties.BLOCK_CRITERIA),
                                                                               properties_.getInt (JDProperties.BLOCK_SIZE),
                                                                               sqlStatement,
                                                                               properties_.getString (JDProperties.PACKAGE_CRITERIA),
                                                                               resultSetType, resultSetConcurrency, resultSetHoldability,       //@G4A
                                                                               AS400JDBCStatement.GENERATED_KEYS_NOT_SPECIFIED);                //@G4A
        statements_.addElement(statement);                  // @DAC
        statementCount_++;                           //@K1A
        if(thousandStatements_ == false && statementCount_ == 1000)              //@K1A
        {                                                                       //@K1A
            thousandStatements_ = true;                                         //@K1A
            //post warning                                                      //@K1A
            postWarning(JDError.getSQLWarning(JDError.WARN_1000_OPEN_STATEMENTS));  //@K1A
        }                                                                       //@K1A

        if (JDTrace.isTraceOn())                                            //@F4A
        {                                                                   //@F4A
            int size = statements_.size();                                  //@F4A
            if (size % 256 == 0)                                            //@F4A
            {                                                               //@F4A
                JDTrace.logInformation (this, "Warning: Open handle count now: " + size); //@F4A
            }                                                               //@F4A
        }                                                                   //@F4A

        return statement;
    }




    /**
    Precompiles an SQL statement with optional input parameters
    and stores it in a PreparedStatement object.  This object can
    be used to efficiently execute this SQL statement
    multiple times.
    
    <p>Result sets created using the statement will be type
    ResultSet.TYPE_FORWARD_ONLY and concurrency
    ResultSet.CONCUR_READ_ONLY.
    
    @param  sql     The SQL statement.
    @return         The prepared statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                               for this connection has been reached,  or an
                                    error occurs.
    **/
    public PreparedStatement prepareStatement (String sql)
    throws SQLException
    {
        return prepareStatement (sql, ResultSet.TYPE_FORWARD_ONLY,
                                 ResultSet.CONCUR_READ_ONLY, 
                                 getInternalHoldability());     //@G4A
    }



    //@G4A
    //JDBC 3.0
    /**
    Precompiles an SQL statement with optional input parameters
    and stores it in a PreparedStatement object.  This object can
    be used to efficiently execute this SQL statement
    multiple times.
    
    <p>This method requires OS/400 V5R2 or i5/OS.  If connecting to OS/400 V5R1 or earlier, an exception will be 
    thrown. 
    
    <p>Result sets created using the statement will be type
    ResultSet.TYPE_FORWARD_ONLY and concurrency
    ResultSet.CONCUR_READ_ONLY.
    
    @param  sql                 The SQL statement.
    @param  autoGeneratedKeys   Whether to return auto generated keys.  Valid values are:
                                <ul>
                                  <li>Statement.RETURN_GENERATED_KEYS
                                  <li>Statement.NO_GENERATED_KEYS
                                </ul>
    @return         The prepared statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                               for this connection has been reached,  
                               if connecting to OS/400 V5R1 or earlier,
                               an error occurs.
    @since Modification 5
    **/
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        if (getVRM() < JDUtilities.vrm520)                                         //@F5A
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);   //@F5A

        // Validation.
        checkOpen ();

        // Create the statement.
        JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                          properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                          properties_.getString (JDProperties.PACKAGE_CRITERIA), this);  // @A2A @G4A

        if(getVRM() >= JDUtilities.vrm610 && autoGeneratedKeys==Statement.RETURN_GENERATED_KEYS)    //@GKA added new generated key support
        {
            // check if it is an insert statement.  
            // Note:  this should be false if the statement was wrappered with a SELECT
            // when prepareStatement(String sql, int[] columnIndex) or
            // prepareStatement(String sql, String[] columnNames) was called.
            if(sqlStatement.isInsert_)
            {
                //wrapper the statement
                String selectStatement = makeGeneratedKeySelectStatement(sql);
                sqlStatement = new JDSQLStatement (selectStatement, properties_.getString(JDProperties.DECIMAL_SEPARATOR), true,
                                                   properties_.getString(JDProperties.PACKAGE_CRITERIA), this);                               
                wrappedInsert_ = true;

            }
        }
        int statementId = getUnusedId (ResultSet.TYPE_FORWARD_ONLY); // @B1C

        if(wrappedInsert_)
        {
            sqlStatement.setSelectFromInsert(true);
            wrappedInsert_ = false;
        }

        AS400JDBCPreparedStatement statement = new AS400JDBCPreparedStatement (this,
                                                                               statementId, transactionManager_, packageManager_,
                                                                               properties_.getString (JDProperties.BLOCK_CRITERIA),
                                                                               properties_.getInt (JDProperties.BLOCK_SIZE),
                                                                               properties_.getBoolean (JDProperties.PREFETCH),
                                                                               sqlStatement, false,
                                                                               properties_.getString (JDProperties.PACKAGE_CRITERIA),
                                                                               ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 
                                                                               getInternalHoldability(), autoGeneratedKeys);    //@G4A
        statements_.addElement(statement);                      // @DAC
        statementCount_++;                           //@K1A
        if(thousandStatements_ == false && statementCount_ == 1000)              //@K1A
        {                                                                       //@K1A
            thousandStatements_ = true;                                         //@K1A
            //post warning                                                      //@K1A
            postWarning(JDError.getSQLWarning(JDError.WARN_1000_OPEN_STATEMENTS));  //@K1A
        }                                                                       //@K1A

        if (JDTrace.isTraceOn())                                            //@F4A
        {                                                                   //@F4A
            int size = statements_.size();                                  //@F4A
            if (size % 256 == 0)                                            //@F4A
            {                                                               //@F4A
                JDTrace.logInformation (this, "Warning: Open handle count now: " + size); //@F4A
            }                                                               //@F4A
        }                                                                   //@F4A

        return statement;
    }




    // JDBC 2.0
    /**
    Precompiles an SQL statement with optional input parameters
    and stores it in a PreparedStatement object.  This object can
    be used to efficiently execute this SQL statement
    multiple times.
    
    <p>Result sets created using the statement will be holdability
    ResultSet.CLOSE_CURSORS_AT_COMMIT.
    
    @param sql                      The SQL statement.
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @return                         The prepared statement object.
    
    @exception      SQLException    If the connection is not open,
                               the maximum number of statements
                                for this connection has been reached, the
                                    result type or currency is not valid,
                                    or an error occurs.
    **/
    public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency)
    throws SQLException
    {
        return prepareStatement (sql, resultSetType,     //@G4A
                                 resultSetConcurrency, getInternalHoldability());  //@G4A
        //@G4M Moved code to next method.
    }


    //@G4A
    // JDBC 3.0
    /**
    Precompiles an SQL statement with optional input parameters
    and stores it in a PreparedStatement object.  This object can
    be used to efficiently execute this SQL statement
    multiple times.
    
    @param sql                      The SQL statement.
    @param resultSetType            The result set type.  Valid values are:
                                    <ul>
                                      <li>ResultSet.TYPE_FORWARD_ONLY
                                      <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                      <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                    </ul>
    @param resultSetConcurrency     The result set concurrency.  Valid values are:
                                    <ul>
                                      <li>ResultSet.CONCUR_READ_ONLY
                                      <li>ResultSet.CONCUR_UPDATABLE
                                    </ul>
    @param resultSetHoldability     The result set holdability.  Valid values are:
                                    <ul>
                                      <li>ResultSet.HOLD_CURSORS_OVER_COMMIT 
                                      <li>ResultSet.CLOSE_CURSORS_AT_COMMIT
                                    </ul>
    @return                         The prepared statement object.
    
    @exception      SQLException    If the connection is not open,
                                    the maximum number of statements
                                    for this connection has been reached, the
                                    result type, currency, or holdability is not valid,
                                    or an error occurs.
    **/
    public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency, 
                                               int resultSetHoldability)
    throws SQLException
    {
        // Validation.
        checkOpen ();
        if (! metaData_.supportsResultSetConcurrency (resultSetType, resultSetConcurrency))
            resultSetType = correctResultSetType (resultSetType, resultSetConcurrency);

        if (!checkHoldabilityConstants(resultSetHoldability))                   //@F3A
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);    //@F3A

        // Create the statement.
        JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                          properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                          properties_.getString (JDProperties.PACKAGE_CRITERIA), this);  // @A2A @G4A
        int statementId = getUnusedId (resultSetType); // @B1C
        AS400JDBCPreparedStatement statement = new AS400JDBCPreparedStatement (this,
                                                                               statementId, transactionManager_, packageManager_,
                                                                               properties_.getString (JDProperties.BLOCK_CRITERIA),
                                                                               properties_.getInt (JDProperties.BLOCK_SIZE),
                                                                               properties_.getBoolean (JDProperties.PREFETCH),
                                                                               sqlStatement, false,
                                                                               properties_.getString (JDProperties.PACKAGE_CRITERIA),
                                                                               resultSetType, resultSetConcurrency, resultSetHoldability, //@G4A
                                                                               AS400JDBCStatement.GENERATED_KEYS_NOT_SPECIFIED);                        //@G4A
        statements_.addElement(statement);                      // @DAC
        statementCount_++;                           //@K1A
        if(thousandStatements_ == false && statementCount_ == 1000)              //@K1A
        {                                                                       //@K1A
            thousandStatements_ = true;                                         //@K1A
            //post warning                                                      //@K1A
            postWarning(JDError.getSQLWarning(JDError.WARN_1000_OPEN_STATEMENTS));  //@K1A
        }                                                                       //@K1A

        if (JDTrace.isTraceOn())                                            //@F4A
        {                                                                   //@F4A
            int size = statements_.size();                                  //@F4A
            if (size % 256 == 0)                                            //@F4A
            {                                                               //@F4A
                JDTrace.logInformation (this, "Warning: Open handle count now: " + size); //@F4A
            }                                                               //@F4A
        }                                                                   //@F4A

        return statement;
    }

    // @G4 new method
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported when connecting to i5/OS V5R4 or earlier systems.</B>
     *
     * @param  sql     The SQL statement.                                  
     * @param  columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows.
     * @return         The prepared statement object.
     * @exception      java.sql.SQLException - If connecting to i5/OS V5R4 or earlier systems, 
     *                 the connection is not open,
     *                 the maximum number of statements for this connection has been reached,
     *                 or an error occurs.
     * @since Modification 5
    **/
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
    throws SQLException
    {
        if(getVRM() >= JDUtilities.vrm610)   //@GKA added support for generated keys
        {
            // Validation
            checkOpen();

            //Create a JDSQLStatement
            JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                              properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                              properties_.getString (JDProperties.PACKAGE_CRITERIA), this);
            //Check if the statement is an insert
            if(sqlStatement.isInsert_){
                wrappedInsert_ = true;
                return prepareStatement(makeGeneratedKeySelectStatement(sql, columnIndexes, null), Statement.RETURN_GENERATED_KEYS);
            }
            else    // treat like prepareStatement(sql) was called
                return prepareStatement(sql);
        }
        else        //@GKA Throw an exception.  V5R4 and earlier does not support retrieving generated keys by column index.
        {
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
    }


    // @G4 new method
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported when connecting to i5/OS V5R4 or earlier systems.</B>
     *
     * @param  sql     The SQL statement.                                  
     * @param  columnNames An array of column names indicating the columns that should be returned from the inserted row or rows.
     * @return         The prepared statement object.
     * @exception      java.sql.SQLException - If connecting to i5/OS V5R4 or earlier systems, 
     *                 the connection is not open,
     *                 the maximum number of statements for this connection has been reached,
     *                 or an error occurs.
     * @since Modification 5
    **/
    public PreparedStatement prepareStatement (String sql, String[] columnNames)
    throws SQLException
    {
        if(getVRM() >= JDUtilities.vrm610)  //@GKA added generated key support
        {
            //Validation
            checkOpen();

            //Create a JDSQLStatement
            JDSQLStatement sqlStatement = new JDSQLStatement (sql,
                                                              properties_.getString (JDProperties.DECIMAL_SEPARATOR), true,
                                                              properties_.getString (JDProperties.PACKAGE_CRITERIA), this);
            //Check if the statement is an insert
            if(sqlStatement.isInsert_){
                wrappedInsert_ = true;
                return prepareStatement(makeGeneratedKeySelectStatement(sql, null, columnNames), Statement.RETURN_GENERATED_KEYS);
            }
            else    // treat like prepareStatement(sql) was called
                return prepareStatement(sql);
        }
        else        //@GKA Throw an exception.  V5R4 and earlier does not support retrieving generated keys by column name.
        {
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
    }



    //@E10a new method
    void processSavepointRequest(String savepointStatement)
    throws SQLException
    {                                                                       
        // must be OS/400 v5r2 or i5/OS
        if (vrm_ < JDUtilities.vrm520)
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);

        // cannot do savepoints on XA transactions
        if (!transactionManager_.isLocalTransaction())
            JDError.throwSQLException (this, JDError.EXC_TXN_STATE_INVALID);

        // cannot do savepoints if autocommit on 
        if (getAutoCommit())
            JDError.throwSQLException(this, JDError.EXC_TXN_STATE_INVALID);

        Statement statement = createStatement();

        statement.executeUpdate(savepointStatement);
        statement.close();
    }







    /**
    Partial closing of the connection.
    @exception SQLException If a database error occurs.
    **/
    void pseudoClose() throws SQLException                      // @E1
    {
        // Rollback before closing.
        if ((transactionManager_.isLocalTransaction()) && (transactionManager_.isLocalActive()))  // @E4A
            rollback ();

        // Close all statements that are running in the context of this connection.
        // Make a clone of the vector, since it will be modified as each statement          @DAA
        // closes itself.                                                                // @DAA
        // @j4 change -- close may throw a SQLException.  Log that error and keep going.
        // Since the user called close we won't return until we tried to close all
        // statements.  
        Vector statements = (Vector)statements_.clone();                                 // @DAA
        Enumeration list = statements.elements();                                        // @DAA
        while (list.hasMoreElements())                                                    // @DAC
        {
            // @DAC
            AS400JDBCStatement statement = (AS400JDBCStatement)list.nextElement();       // @DAA
            try
            {                                                                          // @J4a
                if(statement.isHoldStatement())                                           //@KBL user already called close, now completely close it
                {                                                                       //@KBL
                    statement.setAssociatedWithLocators(false);                          //@KBL
                    statement.finishClosing();                                          //@KBL
                }                                                                       //@KBL
                // @J4a
                if (! statement.isClosed())                                               // @DAC
                    statement.close();                                                    // @DAC
            }                                                                            // @J4a
            catch (SQLException e)                                                       // @J4a
            {
                // @J4a
                if (JDTrace.isTraceOn())                                                  // @J4a
                    JDTrace.logInformation (this, "Closing statement while closing connection failed: " + e.getMessage()); // @j4a
            }                                                                            // @J4a
        }

        // @j1a clean up any i5/OS debug that is going on.  This entire block
        //      is new for @J1
        if (traceServer_ > 0 || databaseHostServerTrace_)                             // @2KRC
        {
            // Get the job identifier because we need the id (it is part of some
            // of our trace files).  I know I could have saved it from
            // the start-trace code but tracing is not performance critical so
            // why make the object bigger by storing trace stuff as member data.
            String serverJobIdentifier = getServerJobIdentifier();

            // Same for this flag.  Don't want to grow the object by saving
            // this as member data.
            boolean preV5R1 = true;        
            boolean SQLNaming = properties_.getString(JDProperties.NAMING).equals(JDProperties.NAMING_SQL);

            try
            {
                preV5R1 = getVRM() <= JDUtilities.vrm450;
            }
            catch (Exception e)
            {
                JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed, could not get server VRM");
            }

            boolean endedTraceJob = false;        //@540  Used to determine if ENDTRC has already been done.
            // End trace-job
            if ((traceServer_ & ServerTrace.JDBC_TRACE_SERVER_JOB) > 0)
            {
                try
                {
                    if (preV5R1)
                        JDUtilities.runCommand(this, "QSYS/TRCJOB SET(*OFF) OUTPUT(*PRINT)", SQLNaming);
                    else
                    {
                        JDUtilities.runCommand(this, "QSYS/ENDTRC SSNID(QJT" +
                                               serverJobIdentifier.substring(20) +
                                               ") DTAOPT(*LIB) DTALIB(QUSRSYS) RPLDTA(*YES) PRTTRC(*YES)", SQLNaming );

                        JDUtilities.runCommand(this, "QSYS/DLTTRC DTAMBR(QJT" +
                                               serverJobIdentifier.substring(20) +
                                               ") DTALIB(QUSRSYS)", SQLNaming );
                    }
                    endedTraceJob = true; //@540
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed");
                }
            }

            //@540 End database host server trace job
            // Database Host Server Trace is supported on V5R3+
            if(getVRM() >= JDUtilities.vrm530  && !endedTraceJob)
            {
                // Only issue ENDTRC if not already done.
                if(((traceServer_ & ServerTrace.JDBC_TRACE_DATABASE_HOST_SERVER) > 0) || databaseHostServerTrace_)        // @2KRC
                {
                    // end database host server trace
                    try{
                        JDUtilities.runCommand(this, "QSYS/ENDTRC SSNID(QJT" +
                                               serverJobIdentifier.substring(20) +
                                               ") DTAOPT(*LIB) DTALIB(QUSRSYS) RPLDTA(*YES) PRTTRC(*YES)", SQLNaming );

                        JDUtilities.runCommand(this, "QSYS/DLTTRC DTAMBR(QJT" +
                                               serverJobIdentifier.substring(20) +
                                               ") DTALIB(QUSRSYS)", SQLNaming );
                    }
                    catch(Exception e){
                        JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end database host server tracing failed.");
                    }
                }
            }

            // End debug-job
            if ((traceServer_ & ServerTrace.JDBC_DEBUG_SERVER_JOB) > 0)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/ENDDBG", SQLNaming);
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed, could not end debug on server job ");
                }
            }

            // End the database monitor
            if ((traceServer_ & ServerTrace.JDBC_START_DATABASE_MONITOR) > 0)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/ENDDBMON", SQLNaming);
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed, could not end database monitor");
                }
            }

            // Dump out SQL information
            if (((traceServer_ & ServerTrace.JDBC_SAVE_SQL_INFORMATION) > 0) && !preV5R1)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/PRTSQLINF *JOB", SQLNaming);
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed, could not print SQL information");
                }
            }

            // Dump the joblog
            if ((traceServer_ & ServerTrace.JDBC_SAVE_SERVER_JOBLOG) > 0)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/DSPJOBLOG JOB(*) OUTPUT(*PRINT)", SQLNaming);
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end server job tracing failed, could not save job log");
                }
            }

            // If the user set our flag to turn on client tracing then turn it back off.
            // This may turn off tracing even though the user wanted it on for some other
            // reason but there is no way for this code to know why tracing was turned on.
            // It does know this interface is one reason it is on so we will assume it
            // is the only reason and will turn it off here.
            if ((traceServer_ & ServerTrace.JDBC_TRACE_CLIENT) > 0)
                JDTrace.setTraceOn(false);
        }
    }


    // @E10a new method
    /**
     * Removes the given Savepoint object from the current transaction. 
     * Any reference to the savepoint after it has been removed will 
     * cause an SQLException to be thrown.
     *
     * @param savepoint the savepoint to be removed.
     *
     * @exception SQLException if a database access error occurs or the given Savepoint 
     *                         is not a valid savepoint in the current transaction.
     *
     * @since Modification 5
    **/
    public void releaseSavepoint(Savepoint savepoint)
    throws SQLException
    {      
        if (savepoint == null)
            throw new NullPointerException("savepoint");

        AS400JDBCSavepoint sp = (AS400JDBCSavepoint) savepoint;

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Releasing savepoint " + sp.getName());

        if (sp.getStatus() != AS400JDBCSavepoint.ACTIVE)
            JDError.throwSQLException(this, JDError.EXC_SAVEPOINT_DOES_NOT_EXIST);

        String SQLCommand = "RELEASE SAVEPOINT " + sp.getName();

        processSavepointRequest(SQLCommand);

        sp.setStatus(AS400JDBCSavepoint.CLOSED);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Savepoint " + sp.getName() + " released.");
    }







    // @E4C
    /**
    Drops all changes made since the previous commit or
    rollback and releases any database locks currently held by
    the connection.  This has no effect when the connection
    is in auto-commit mode.
    
    <p>This method can not be called when the connection is part
    of a distributed transaction.  See <a href="AS400JDBCXAResource.html">
    AS400JDBCXAResource</a> for more information.
    
    @exception SQLException     If the connection is not open
                                or an error occurs.
    **/
    public void rollback ()
    throws SQLException
    {
        checkOpen ();

        if (!transactionManager_.isLocalTransaction())                      // @E4A
            JDError.throwSQLException (this, JDError.EXC_TXN_STATE_INVALID);      // @E4A

        if (! transactionManager_.getAutoCommit ())
        {
            transactionManager_.rollback ();

            // @F3 Mark all cursors closed on a rollback.  Don't worry here 
            // @F3 about whether their statement level holdability is different; we will check 
            // @F3 that within Statement.markCursorsClosed().

            // @F3D if (transactionManager_.getHoldIndicator() == JDTransactionManager.CURSOR_HOLD_FALSE   // @B4A
            // @F3 Passing true means we called markCursorClosed from rollback.
            markCursorsClosed(true);                                        // @B4A @F3C

            if(properties_.getBoolean(JDProperties.HOLD_STATEMENTS )) //@PDA additional HOLD_STATEMENTS check
                markStatementsClosed(); //@KBL

            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, "Transaction rollback");
        }
    }

    // @E10 new method
    /**
     * Undoes all changes made after the specified Savepoint was set. 
     *
     * @param savepoint the savepoint to be rolled back to.
     *
     * @exception SQLException if a database access error occurs, the Savepoint 
     *                         is no longer valid, or this Connection 
     *                         is currently in auto-commit mode.
     * @since Modification 5
    **/
    public void rollback(Savepoint savepoint)
    throws SQLException
    {
        if (savepoint == null)
            throw new NullPointerException("savepoint");


        AS400JDBCSavepoint sp = (AS400JDBCSavepoint) savepoint;

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Rollback with savepoint " + sp.getName());

        if (sp.getStatus() != AS400JDBCSavepoint.ACTIVE)
            JDError.throwSQLException(this, JDError.EXC_SAVEPOINT_DOES_NOT_EXIST);

        String SQLCommand = "ROLLBACK TO SAVEPOINT " + sp.getName();

        processSavepointRequest(SQLCommand);

        sp.setStatus(AS400JDBCSavepoint.CLOSED);                  

        if(properties_.getBoolean(JDProperties.HOLD_STATEMENTS )) //@PDA additional HOLD_STATEMENTS check
            markStatementsClosed();         //@KBL

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Rollback with savepoint " + sp.getName() + " complete.");
    }






    /**
    Sends a request data stream to the system using the
    connection's id and does not expect a reply.
    
    @param   request     The request.
    
    @exception           SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
    void send (DBBaseRequestDS request)
    throws SQLException
    {
        send (request, id_, true);
    }



    /**
    Sends a request data stream to the system and does not
    expect a reply.
    
    @param   request     The request.
    @param   id          The id.
    
    @exception           SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
    void send (DBBaseRequestDS request, int id)
    throws SQLException
    {
        send (request, id, true);
    }



    /**
    Sends a request data stream to the system and does not
    expect a reply.
    
    @param   request        The request.
    @param   id             The id.
    @param   leavePending   Indicates if the request should
                            be left pending.  This indicates
                            whether or not to base the next
                            request on this one.
    
    @exception              SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
    void send (DBBaseRequestDS request, int id, boolean leavePending)
    throws SQLException
    {
        checkCancel();                                                                      // @E8A
        checkOpen();      // @W1a

        try
        {
            // Since we are just calling send() (instead of sendAndReceive()),              // @EAA
            // make sure we are not asking the system for a reply.  Otherwise,              // @EAA
            // the reply will come back and it will get held in the AS400                   // @EAA
            // read daemon indefinitely - - a memory leak.                                  // @EAA
            if (JDTrace.isTraceOn())
            {                                                      // @EAA
                if (request.getOperationResultBitmap() != 0)                                // @EAA
                    JDTrace.logInformation (this, "Reply requested but not collected:" + request.getReqRepID()); // @EAA
            }                                                                               // @EAA

            request.setBasedOnORSHandle (0);                 // @DAC @EKC
            DBReplyRequestedDS reply = null;

            if (dataCompression_ == DATA_COMPRESSION_RLE_)
            {                                // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REQUEST_RLE_COMPRESSION); // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REPLY_RLE_COMPRESSION); // @ECA
                request.compress();                                                         // @ECA
            }                                                                               // @ECA

            DataStream actualRequest;                                                       // @E5A
            synchronized(heldRequestsLock_)
            {                                               // @E5A
                if (heldRequests_ != null)                                                  // @E5A
                    actualRequest = new DBConcatenatedRequestDS(heldRequests_, request);    // @E5A
                else                                                                        // @E5A
                    actualRequest = request;                                                // @E5A
                heldRequests_ = null;                                                       // @E5A
                                                                      
                server_.send(actualRequest);                // @E5A @F7M
//@P1D                requestPending_[id] = leavePending; //@P0A @F7M
            }                                                                               // @E5A

            // @E5D if (DEBUG_REQUEST_CHAINING_ == true) {
            //@P0D                if (leavePending)                                                           // @DAA
            //@P0D                    requestPending_.set(id);                                                // @DAC
            //@P0D                else                                                                        // @DAA
            //@P0D                    requestPending_.clear(id);                                              // @DAA

            // @E5D }
            // @E5D else {
            // @E5D     request.addOperationResultBitmap (DBBaseRequestDS.ORS_BITMAP_RETURN_DATA);
            // @E5D     reply = (DBReplyRequestedDS) server_.sendAndReceive (request);
            // @E5D     requestPending_[id] = false;
            // @E5D }

            if (DEBUG_COMM_TRACE_ > 0)
            {
                debug (request);
                // @E5D if (DEBUG_REQUEST_CHAINING_ == false)
                // @E5D     debug (reply);
            }
        }
        // @J5D catch (ConnectionDroppedException e) {                               // @C1A
        // @J5D    server_ = null;                                                  // @D8
        // @J5D    request.freeCommunicationsBuffer();                              // @EMa
        // @J5D    JDError.throwSQLException (this, JDError.EXC_CONNECTION_NONE, e);      // @C1A
        // @J5D }                                                                    // @C1A
        catch (IOException e)
        {                                              // @J5A
            server_ = null;                                                  // @J5A
            //@P0D request.freeCommunicationsBuffer();                              // @J5A
            JDError.throwSQLException (this, JDError.EXC_COMMUNICATION_LINK_FAILURE, e); // @J5A
        }                                                                    // @J5A
        catch (Exception e)
        {
            //@P0D request.freeCommunicationsBuffer();                              // @EMa
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }
    }



    // @EBD /**
    // @EBD Sends a request data stream to the system and discards
    // @EBD the reply.
    // @EBD
    // @EBD @param   request        The request.
    // @EBD @param   id             The id.
    // @EBD @param   leavePending   Indicates if the request should
    // @EBD                         be left pending.  This indicates
    // @EBD                         whether or not to base the next
    // @EBD                         request on this one.
    // @EBD
    // @EBD @exception              SQLException   If an error occurs.
    // @EBD **/
    // @EBD //
    // @EBD // See implementation notes for sendAndReceive().
    // @EBD //
    // @EBD     void sendAndDiscardReply (DBBaseRequestDS request, int id)
    // @EBD        throws SQLException
    // @EBD   {
    // @EBD         checkCancel();                                                                      // @E8A
    // @EBD
    // @EBD       try {
    // @EBD           request.setBasedOnORSHandle (0);          //@EKC
    // @EBD
    // @EBD             DataStream actualRequest;                                                       // @E5A
    // @EBD             synchronized(heldRequestsLock_) {                                               // @E5A
    // @EBD                 if (heldRequests_ != null)                                                  // @E5A
    // @EBD                     actualRequest = new DBConcatenatedRequestDS(heldRequests_, request);    // @E5A
    // @EBD                 else                                                                        // @E5A
    // @EBD                     actualRequest = request;                                                // @E5A
    // @EBD                 heldRequests_ = null;                                                       // @E5A
    // @EBD             }                                                                               // @E5A
    // @EBD
    // @EBD                 server_.sendAndDiscardReply(actualRequest);                                     // @E5C
    // @EBD             requestPending_[id] = false;
    // @EBD
    // @EBD             if (DEBUG_COMM_TRACE_ > 0)
    // @EBD                 debug (request);
    // @EBD         }
    // @EBD         catch (ConnectionDroppedException e) {                               // @C1A
    // @EBD             server_ = null;                                                  // @D8
    // @EBD             JDError.throwSQLException (this, JDError.EXC_CONNECTION_NONE, e);      // @C1A
    // @EBD         }                                                                    // @C1A
    // @EBD        catch (Exception e) {
    // @EBD             JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
    // @EBD        }
    // @EBD   }



    // @E5A
    /**
    Holds a request until the next explicit request.  It will
    be concatenated at the beginning of the next request.
    
    @param   request     The request.
    @param   id          The id.
    
    @exception           SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
    void sendAndHold(DBBaseRequestDS request, int id)
    throws SQLException
    {
        checkCancel();                                                                      // @E8A
        checkOpen();      // @W1a

        try
        {
            // Since we are just calling send() (instead of sendAndReceive()),              // @EAA
            // make sure we are not asking the system for a reply.  Otherwise,              // @EAA
            // the reply will come back and it will get held in the AS400                   // @EAA
            // read daemon indefinitely - - a memory leak.                                  // @EAA
            if (JDTrace.isTraceOn())
            {                                                      // @EAA
                if (request.getOperationResultBitmap() != 0)                                // @EAA
                    JDTrace.logInformation (this, "Reply requested but not collected:" + request.getReqRepID()); // @EAA
            }                                                                               // @EAA

            request.setBasedOnORSHandle(0);                  // @DAC @EKC

            if (dataCompression_ == DATA_COMPRESSION_RLE_)
            {                                // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REQUEST_RLE_COMPRESSION); // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REPLY_RLE_COMPRESSION); // @ECA
                request.compress();                                                         // @ECA
            }                                                                               // @ECA

            synchronized(heldRequestsLock_)
            {
                if (heldRequests_ == null)
                    heldRequests_ = new Vector();
                heldRequests_.addElement(request);
            }

            //@P0D requestPending_.set(id);                                                        // @DAC
//@P1D            requestPending_[id] = true; //@P0A

            if (DEBUG_COMM_TRACE_ > 0)
            {
                debug (request);
                System.out.println("This request was HELD.");
            }
        }
        // Note: No need to check for an IOException in this method, since we don't contact the system.       @J5A
        catch (Exception e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }
    }



    /**
    Sends a request data stream to the system using the
    connection's id and returns the corresponding reply from
    the system.
    
    @param   request     The request.
    @return              The reply.
    
    @exception           SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
    DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request)
    throws SQLException
    {
        return sendAndReceive (request, id_);
    }



    /**
    Sends a request data stream to the system and returns the
    corresponding reply from the system.
    
    @param   request     The request.
    @param   id          The id.
    @return              The reply.
    
    @exception           SQLException   If an error occurs.
    **/
    //
    // Implementation notes:
    //
    // 1. We do not have to worry about thread synchronization
    //    here, since the AS400Server object handles it.
    //
    // 2. The based on id is used to chain requests for the
    //    same ORS without needing to get a reply for each one.
    //    If a request fails, then all subsequent requests will
    //    too, and the results from the original failure will
    //    ultimately be returned.
    //
    //    Initially, the based on id is set to 0.  After a
    //    request is sent the based on id is set to the statement's
    //    id, so that subsequent requests will base on this id.
    //    Finally, when a reply is retrieved, the based on id
    //    is reset to 0.
    //
    //    The status of the based on id depends on whether a
    //    request is pending, which is maintained in the id table.
    //
    DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request, int id)
    throws SQLException
    {
        checkCancel();                                                                      // @E8A
        checkOpen();      // @W1a

        DBReplyRequestedDS reply = null;

        try
        {
            request.setBasedOnORSHandle (0);                 // @DAC @EKC

            if (dataCompression_ == DATA_COMPRESSION_RLE_)
            {                                // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REQUEST_RLE_COMPRESSION); // @ECA
                request.addOperationResultBitmap(DBBaseRequestDS.ORS_BITMAP_REPLY_RLE_COMPRESSION); // @ECA
                request.compress();                                                         // @ECA
            }                                                                               // @ECA

            DataStream actualRequest;                                                       // @E5A
            synchronized(heldRequestsLock_)
            {                                               // @E5A
                if (heldRequests_ != null)                                                  // @E5A
                    actualRequest = new DBConcatenatedRequestDS(heldRequests_, request);    // @E5A
                else                                                                        // @E5A
                    actualRequest = request;                                                // @E5A
                heldRequests_ = null;                                                       // @E5A
                                                                      
                reply = (DBReplyRequestedDS)server_.sendAndReceive(actualRequest);          // @E5C @F7M
                //@P0D requestPending_.clear(id);
//@P1D                requestPending_[id] = false; //@P0A @F7M
            }                                                                               // @E5A

            reply.parse(dataCompression_);                                                  // @E5A
                                                       // @DAC

            if (DEBUG_COMM_TRACE_ > 0)
            {
                debug (request);
                debug (reply);
            }
        }
        // @J5D catch (ConnectionDroppedException e) {                              // @C1A
        // @J5D    server_ = null;                                                  // @D8
        // @J5D    request.freeCommunicationsBuffer();                              // @EMa
        // @J5D    JDError.throwSQLException (this, JDError.EXC_CONNECTION_NONE, e);      // @C1A
        // @J5D }                                                                   // @C1A
        catch (IOException e)
        {                                             // @J5A
            server_ = null;                                                  // @J5A
            //@P0D request.freeCommunicationsBuffer();                              // @J5A
            JDError.throwSQLException (this, JDError.EXC_COMMUNICATION_LINK_FAILURE, e); // @J5A
        }                                                                   // @J5A
        catch (Exception e)
        {
            //@P0D request.freeCommunicationsBuffer();                              // @EMa
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }

        return(DBReplyRequestedDS) reply;
    }



    // @E4C
    /**
    Sets the auto-commit mode.   If the connection is in auto-commit
    mode, then all of its SQL statements are executed and committed
    as individual transactions.  Otherwise, its SQL statements are
    grouped into transactions that are terminated by either a commit
    or rollback.
    
    <p>By default, the connection is in auto-commit mode.  The
    commit occurs when the statement execution completes or the
    next statement execute occurs, whichever comes first.  In the
    case of statements returning a result set, the statement
    execution completes when the last row of the result set has
    been retrieved or the result set has been closed.  In advanced
    cases, a single statement may return multiple results as well
    as output parameter values.  Here the commit occurs when all results
    and output parameter values have been retrieved.
    
    <p>The auto-commit mode is always false when the connection is part
    of a distributed transaction.  See <a href="AS400JDBCXAResource.html">
    AS400JDBCXAResource</a> for more information.
    
    @param  autoCommit  true to turn on auto-commit mode, false to
                        turn it off.
    
    @exception          SQLException    If the connection is not open
                                        or an error occurs.
    **/
    public void setAutoCommit (boolean autoCommit)
    throws SQLException
    {
        checkOpen ();
        if (TESTING_THREAD_SAFETY) return; // in certain testing modes, don't contact i5/OS system

        transactionManager_.setAutoCommit (autoCommit);

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "Auto commit", transactionManager_.getAutoCommit ());
    }



    /**
    This method is not supported.
    
    @exception          SQLException    If the connection is not open.
    **/
    public void setCatalog (String catalog)
    throws SQLException
    {
        checkOpen ();

        // No-op.
    }

    /**
    Sets the eWLM Correlator.  It is assumed a valid correlator value is used.
    If the value is null, all ARM/eWLM implementation will be turned off.
    eWLM correlators require i5/OS V5R3 or later systems.  This request is ignored when running to OS/400 V5R2 or earlier systems.
    
    @param bytes The eWLM correlator value
    **/
    public void setDB2eWLMCorrelator(byte[] bytes)
    throws SQLException //@eWLM
    {
        if(vrm_ >= JDUtilities.vrm530)
        {
            DBSQLAttributesDS request = null;
            DBReplyRequestedDS reply = null;
            try                                                                                 
            {          
                if(bytes == null)
                {
                    if(JDTrace.isTraceOn())
                        JDTrace.logInformation(this, "Correlator is null");
                }
                request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                            id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                            + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);   
                request.seteWLMCorrelator(bytes);                                
                reply = sendAndReceive(request);                 
                int errorClass = reply.getErrorClass();
                if(errorClass != 0)
                    JDError.throwSQLException(this, id_, errorClass, reply.getReturnCode());        
            }                                                                                   
            catch(DBDataStreamException e)                                                      
            {                                                                                   
                JDError.throwSQLException(JDError.EXC_INTERNAL, e);                             
            }   
            finally
            {
                if (request != null) request.inUse_ = false;
                if (reply != null) reply.inUse_ = false;
            }
        }
    }


    // @B1A
    /**
    Sets whether the connection is being used for DRDA.
    
    @param  drda        true if the connection is being used for DRDA,
                        false otherwise.
    **/
    void setDRDA (boolean drda)
    throws SQLException // @EGA
    {
        drda_ = drda;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "DRDA", drda_);
    }


    //@G4A JDBC 3.0
    /**
    Sets the holdability of ResultSets created from this connection.
    
    <p>Full functionality of this method requires OS/400 V5R2
    or i5/OS.  If connecting to OS/400 V5R1 or earlier, all
    cursors for the connection will be changed to the value of the variable
    <i>holdability</i>.
    
    @param  holdability   The cursor holdability.
                          Valid values are ResultSet.HOLD_CURSORS_OVER_COMMIT or
                          ResultSet.CLOSE_CURSORS_AT_COMMIT.
    
    @exception          SQLException    If the connection is not open
                                        or the value passed in is not valid.
    @since Modification 5
    **/
    public void setHoldability (int holdability)
    throws SQLException
    {
        checkOpen ();
        if (TESTING_THREAD_SAFETY) return; // in certain testing modes, don't contact i5/OS system

        if (!checkHoldabilityConstants(holdability))                            //@F3A
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);    //@F3A

        holdability_ = holdability;

        if (holdability == AS400JDBCResultSet.CLOSE_CURSORS_AT_COMMIT)           //@F5A
            transactionManager_.setHoldIndicator(JDProperties.CURSORHOLD_FALSE); //@F5A 
        else if (holdability == AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT)     //@F5A
            transactionManager_.setHoldIndicator(JDProperties.CURSORHOLD_TRUE);  //@F5A

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "Holdability", holdability_);
    }



    //@D4A
    void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties,
                        AS400 as400)
    throws SQLException
    {
        if (TESTING_THREAD_SAFETY) // in certain testing modes, don't contact i5/OS system
        {
          as400PublicClassObj_ = as400;
        }
        else
        {
          try
          {
            as400.connectService (AS400.DATABASE);
          }
          catch (AS400SecurityException e)
          {                            //@D5C
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_REJECTED, e);
          }
          catch (IOException e)
          {                                       //@D5C
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_UNABLE, e);
          }
        }

        setProperties (dataSourceUrl, properties, as400.getImpl());
    }


    void setProperties(JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400)
    throws SQLException
    {
        setProperties(dataSourceUrl, properties, as400, false);
    }

    //@A3A - This logic formerly resided in the ctor.
    void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400, boolean newServer)
    throws SQLException
    {
        // Initialization.
        as400_                  = (AS400ImplRemote) as400;           //@A3A
        //@P0D assigned_               = new BitSet(INITIAL_STATEMENT_TABLE_SIZE_);          // @DAC
        dataSourceUrl_          = dataSourceUrl;
        extendedFormats_        = false;
        properties_             = properties;
        //@P0D requestPending_         = new BitSet(INITIAL_STATEMENT_TABLE_SIZE_);         // @DAC
        statements_             = new Vector(INITIAL_STATEMENT_TABLE_SIZE_);         // @DAC
        if(!TESTING_THREAD_SAFETY && as400_.getVRM() <= JDUtilities.vrm520)                                    //@KBA         //if V5R2 or less use old support of issuing set transaction statements
            newAutoCommitSupport_ = 0;                                               //@KBA
        else if(!properties_.getBoolean(JDProperties.TRUE_AUTO_COMMIT))              //@KBA //@true     //run autocommit with *NONE isolation level
            newAutoCommitSupport_ = 1;                                               //@KBA
        else                                                                         //@KBA
            newAutoCommitSupport_ = 2;                                               //@KBA         //run autocommit with specified isolation level

        // Issue any warnings.
        if (dataSourceUrl_.isExtraPathSpecified ())
            postWarning (JDError.getSQLWarning (JDError.WARN_URL_EXTRA_IGNORED));
        if (dataSourceUrl_.isPortSpecified ())
            postWarning (JDError.getSQLWarning (JDError.WARN_URL_EXTRA_IGNORED));
        if (properties.isExtraPropertySpecified ())
            postWarning (JDError.getSQLWarning (JDError.WARN_PROPERTY_EXTRA_IGNORED));

        // Initialize the library list.
        String urlSchema = dataSourceUrl_.getSchema ();
        if (urlSchema == null)
            JDError.throwSQLException (this, JDError.WARN_URL_SCHEMA_INVALID);

        JDLibraryList libraryList = new JDLibraryList (
                                                      properties_.getString (JDProperties.LIBRARIES), urlSchema,
                                                      properties_.getString (JDProperties.NAMING)); // @B2C
        defaultSchema_ = libraryList.getDefaultSchema ();

        // The connection gets an id automatically, but never
        // creates an RPB on the system.  There should never be a need
        // to create an RPB on the system for a connection, but an
        // id is needed for retrieving Operational Result Sets (ORS)
        // for errors, etc.

        // Initialize a transaction manager for this connection.
        transactionManager_ = new JDTransactionManager (this, id_,
                                                        properties_.getString (JDProperties.TRANSACTION_ISOLATION));

        transactionManager_.setHoldIndicator(properties_.getString(JDProperties.CURSOR_HOLD));       // @D9

        // Initialize the read-only mode to true if the access
        // property says read only.
        readOnly_ = (properties_.equals (JDProperties.ACCESS,
                                         JDProperties.ACCESS_READ_ONLY));


        // Determine the amount of system tracing that should be started.  Trace
        // can be started by either a JDBC property or the ServerTrace class.  Our value
        // will be the combination of the two (instead of one overriding the other).
        traceServer_ = properties_.getInt(JDProperties.TRACE_SERVER) +
                       ServerTrace.getJDBCServerTraceCategories();  // @j1a

        // Determine if a QAQQINI library name was specified.  The library can be set using                     //@K2A
        // a JDBC property.                                                                                     //@k2A
        qaqqiniLibrary_ = properties_.getString(JDProperties.QAQQINILIB);                                       //@K2A

        //@A3D
        // Initialize the conversation.
        //open ();

        //@A3A
        // Connect.        
        if (JDTrace.isTraceOn())                                                      // @F6a
        {                                                                             // @F6a
            JDTrace.logInformation("Toolbox for Java - " + Copyright.version);        // @F6a
            JDTrace.logInformation("JDBC Level: " + JDUtilities.JDBCLevel_);          // @F6a
        }                                                                             // @F6a

        if (!TESTING_THREAD_SAFETY) // in certain testing modes, we don't contact i5/OS system
        {
          try
          {
            server_ = as400_.getConnection (AS400.DATABASE, newServer);
          }
          catch (AS400SecurityException e)
          {
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_REJECTED, e);
          }
          catch (IOException e)
          {
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_UNABLE, e);
          }
        }

        // Initialize the catalog name at this point to be the system
        // name.  After we exchange attributes, we can change it to
        // the actual name.
        catalog_ = dataSourceUrl.getServerName();                              // @D7A
        if (catalog_.length() == 0)                                            // @D7A
            catalog_ = as400_.getSystemName ().toUpperCase ();                   // @A3A

        setServerAttributes ();
        libraryList.addOnServer (this, id_);

        // @E7D // Initialize a transaction manager for this connection.  Turn on                                @E7A
        // @E7D // new auto-commit support when the server functional level is                                   @E7A
        // @E7D // greater than or equal to 3.                                                                   @E7A
        // @E7D boolean newAutoCommitSupport = (serverFunctionalLevel_ >= 3);                                 // @E7A
        // @E7D transactionManager_.setNewAutoCommitSupport(newAutoCommitSupport);                            // @E7A

        // We keep a metadata object around for quick access.
        // The metadata object should share the id of the
        // connection, since it operates on a connection-wide
        // scope.
        metaData_ = new AS400JDBCDatabaseMetaData (this, id_);

        // The conversation was initialized to a certain
        // transaction isolation.  It is now time to turn on auto-
        // commit by default.
        if(newAutoCommitSupport_ == 0)          //KBA  V5R2 or less so do what we always have
            transactionManager_.setAutoCommit (true);

        // Initialize the package manager.
        packageManager_ = new JDPackageManager (this, id_, properties_,
                                                transactionManager_.getCommitMode ());

        // Trace messages.
        if (JDTrace.isTraceOn())
        {
            JDTrace.logOpen (this, null);                                              // @J33a
            JDTrace.logProperty (this, "Auto commit", transactionManager_.getAutoCommit ());
            JDTrace.logProperty (this, "Read only", readOnly_);
            JDTrace.logProperty (this, "Transaction isolation", transactionManager_.getIsolation ());
            if (packageManager_.isEnabled ())
                JDTrace.logInformation (this, "SQL package = "
                                        + packageManager_.getLibraryName() + "/"
                                        + packageManager_.getName ());
        }


        // @j1a Trace the server job if the user asked us to.  Tracing
        // can be turned on via a URL property, the Trace class, the DataSource
        // object, or a system property.
        if (traceServer_ > 0)
        {
            // Get the server job id.  We will both dump this to the trace
            // and use it to uniquely label some of the files.
            String serverJobIdentifier = getServerJobIdentifier();
            String serverJobId = serverJobIdentifier.substring(20).trim() + "/" +
                                 serverJobIdentifier.substring(10, 19).trim() + "/" +
                                 serverJobIdentifier.substring( 0, 10).trim();

            // Dump the server job id
            JDTrace.logDataEvenIfTracingIsOff(this, Copyright.version);
            JDTrace.logDataEvenIfTracingIsOff(this, serverJobId);
            JDTrace.logDataEvenIfTracingIsOff(this, "Server functional level:  " + getServerFunctionalLevel());          // @E7A


            // Determine system level.  Some commands are slightly different
            // to v5r1 machines.
            boolean preV5R1 = true;
            boolean SQLNaming = properties_.getString(JDProperties.NAMING).equals(JDProperties.NAMING_SQL);
            try
            {
                preV5R1 = getVRM() <= JDUtilities.vrm450;
            }
            catch (Exception e)
            {
                JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job tracing failed, could not get server VRM");
            }

            // Start client tracing if the flag is on and trace isn't already running
            if (((traceServer_ & ServerTrace.JDBC_TRACE_CLIENT) > 0) && (! JDTrace.isTraceOn()))
                JDTrace.setTraceOn(true);

            // No matter what type of tracing is turned on, alter the server
            // job so more stuff is saved in the job log.
            try
            {
                JDUtilities.runCommand(this, "QSYS/CHGJOB LOG(4 00 *SECLVL) LOGCLPGM(*YES)", SQLNaming);
            }
            catch (Exception e)
            {
                JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job tracing failed, could not change log level");
            }

            // Optionally start debug on the database server job
            if ((traceServer_ & ServerTrace.JDBC_DEBUG_SERVER_JOB) > 0)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/STRDBG UPDPROD(*YES)", SQLNaming);
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job tracing failed, could not start debug on server job ");
                }
            }

            // Optionally start the database monitor
            if ((traceServer_ & ServerTrace.JDBC_START_DATABASE_MONITOR) > 0)
            {
                try
                {
                    JDUtilities.runCommand(this, "QSYS/STRDBMON OUTFILE(QUSRSYS/QJT"  +
                                           serverJobIdentifier.substring(20) +
                                           ") JOB(*) TYPE(*DETAIL)", SQLNaming );
                }
                catch (Exception e)
                {
                    JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job tracing failed, could not start database monitor");
                }
            }

            boolean traceServerJob = ((traceServer_ & ServerTrace.JDBC_TRACE_SERVER_JOB) > 0);  //@540                                                        
            //@540 Database Host Server Trace is supported on V5R3 and later systems
            boolean traceDatabaseHostServer = ((getVRM() >= JDUtilities.vrm530) && ((traceServer_ & ServerTrace.JDBC_TRACE_DATABASE_HOST_SERVER) > 0)); //@540
            // Optionally start trace on the database server job or database host server
            //@540D if ((traceServer_ & ServerTrace.JDBC_TRACE_SERVER_JOB) > 0)
            if(traceServerJob || traceDatabaseHostServer)   //@540
            {
                try
                {
                    if (preV5R1 && traceServerJob)  //@540 added check for traceServerJob
                        JDUtilities.runCommand(this, "QSYS/TRCJOB MAXSTG(16000)", SQLNaming);
                    else{
                        if(!traceDatabaseHostServer){  //@540 trace only server job
                            JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +
                                               serverJobIdentifier.substring(20) +
                                               ") JOB(*) MAXSTG(128000)", SQLNaming);
                        }
                        else if(!traceServerJob){ //@540 trace only database host server
                            if(getVRM() == JDUtilities.vrm530){  //@540 run command for V5R3
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                //@540
                                               serverJobIdentifier.substring(20) +                    //@540
                                               ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*TRCTYPE) " +      //@540
                                               "TRCTYPE((TESTA *INFO))", SQLNaming);                  //@540
                            }
                            else{   //@540 run command for V5R4 and higher                                    
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                 //@540
                                               serverJobIdentifier.substring(20) +                     //@540
                                               ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*TRCTYPE) " +       //@540
                                               "TRCTYPE((*DBHSVR *INFO))", SQLNaming);                 //@540
                            }
                        }                                                                              //@540
                        else{ //@540 start both server job and database host server trace
                            if(getVRM() == JDUtilities.vrm530){  //@540 run command for V5R3
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                //@540
                                               serverJobIdentifier.substring(20) +                    //@540
                                               ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*ALL) " +          //@540
                                               "TRCTYPE((TESTA *INFO))", SQLNaming);                  //@540
                            }
                            else{    //@540 run V5R4 and higher command
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                //@540
                                               serverJobIdentifier.substring(20) +                    //@540
                                               ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*ALL) " +          //@540
                                               "TRCTYPE((*DBHSVR *INFO))", SQLNaming);                //@540
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    if(traceServerJob && !traceDatabaseHostServer)  //@540
                        JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job tracing failed, could not trace server job");
                    else if(traceDatabaseHostServer && !traceServerJob)  //@540
                        JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start database host server tracing failed, could not trace server job");    //@540
                    else                                                                                                                                //@540
                        JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start server job and database host server tracing failed, could not trace server job");  //@540
                }
            }
        }

        //@K2A    Issue Change Query Attributes command if user specified QAQQINI library name
        if(qaqqiniLibrary_.length() > 0 && !qaqqiniLibrary_.equals("null"))
        {
            boolean SQLNaming = properties_.getString(JDProperties.NAMING).equals(JDProperties.NAMING_SQL);
            try
            {
                JDUtilities.runCommand(this, "CHGQRYA QRYOPTLIB(" + qaqqiniLibrary_ + ")", SQLNaming );
            }
            catch (Exception e)
            {
                JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to issue Change Query Attributes command using QAQQINI Library name failed.");
            }
        }
    }



    /**
    Sets the read-only mode.  This will provide read-only
    access to the database.  Read-only mode can be useful by
    enabling certain database optimizations. If the caller
    specified "read only" or "read call" for the "access" property,
    then the read-only mode cannot be set to false.  The read-only
    mode cannot be changed while in the middle of a transaction.
    
    <p>This method can not be called when the connection is part
    of a distributed transaction.  See <a href="AS400JDBCXAResource.html">
    AS400JDBCXAResource</a> for more information.
    
    @exception          SQLException    If the connection is not open,
                                        a transaction is active, or the
                                        "access" property is set to "read
                                        only".
    **/
    public void setReadOnly (boolean readOnly)
    throws SQLException
    {
        checkOpen ();

        if (transactionManager_.isLocalActive () || transactionManager_.isGlobalActive()) // @E4C
            JDError.throwSQLException (this, JDError.EXC_TXN_STATE_INVALID);      // @E4C

        if ((readOnly == false)
            && ((properties_.getString (JDProperties.ACCESS).equalsIgnoreCase (JDProperties.ACCESS_READ_ONLY))
                || (properties_.getString (JDProperties.ACCESS).equalsIgnoreCase (JDProperties.ACCESS_READ_CALL))))
            JDError.throwSQLException (this, JDError.EXC_ACCESS_MISMATCH);

        readOnly_ = readOnly;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "Read only", readOnly_);
    }




    // @E10 new method
    /**
     * Creates an unnamed savepoint in the current transaction and returns the new Savepoint object that represents it.
     * <UL>
     * <LI>Named savepoints must be unique.  A savepoint name cannot be reused until the savepoint is released, committed, or rolled back.
     * <LI>Savepoints are valid only if autocommit is off.  An exception is thrown if autocommit is enabled.                                                                              
     * <LI>Savepoints are not valid across XA connections.  An exception is thrown if the connection is an XA connection.
     * <LI>Savepoints require OS/400 V5R2 or i5/OS.  An exception is thrown if connecting to OS/400 V5R1 or earlier.
     * <LI>If the connection option is set to keep cursors open after a traditional rollback, cursors will remain open after a rollback to a savepoint.
     * </UL>
     *
     * @return     The new Savepoint object.
     * @exception  SQLException if a database access error occurs or this Connection object is currently in auto-commit mode.
     * @since Modification 5
    **/
    public Savepoint setSavepoint()
    throws SQLException
    {      
        return setSavepoint(null, AS400JDBCSavepoint.getNextId());
    }  

    // @E10 new method
    /**
     * Creates a named savepoint in the current transaction and returns the new Savepoint object that represents it.
     * <UL>
     * <LI>Named savepoints must be unique.  A savepoint name cannot be reused until the savepoint is released, committed, or rolled back.
     * <LI>Savepoints are valid only if autocommit is off.  An exception is thrown if autocommit is enabled.   
     * <LI>Savepoints are not valid across XA connections.  An exception is thrown if the connection is an XA connection.
     * <LI>Savepoints require OS/400 V5R2 or i5/OS.  An exception is thrown if connecting to OS/400 V5R1 or earlier.
     * <LI>If the connection option is set to keep cursors open after a traditional rollback, cursors will remain open after a rollback to a savepoint.
     * </UL>
     * @param      name A String containing the name of the savepoint
     * @return     The new Savepoint object.
     * @exception  SQLException if a database access error occurs or this Connection object is currently in auto-commit mode.
     * @since Modification 5
    **/
    public Savepoint setSavepoint(String name)
    throws SQLException
    {                           
        if (name == null)
            throw new NullPointerException("name");

        return setSavepoint(name, 0);
    }  

    // @E10 new method
    private Savepoint setSavepoint(String name, int id)
    throws SQLException
    {
        if (id > 0)
            name = "T_JDBCINTERNAL_" + id;

        // When creating the savepoint specify retain cursors.  That is the
        // only option supported by the i5/OS system at this time.  We have to specify
        // it because the SQL default is close cursors.  Since we need to use 
        // an option other than the default we have to specify it on the statement.
        // Plus, the system will return an error if we don't specify it.  
        processSavepointRequest("SAVEPOINT " + name + " ON ROLLBACK RETAIN CURSORS" );

        return(Savepoint)(Object) new AS400JDBCSavepoint(name, id);
    }







    /**
    Sets the server attributes.
    
    @param      libraryList     The library list.
    
    @exception  SQLException    If an error occurs.
    **/
    private void setServerAttributes ()
    throws SQLException
    {
        if (TESTING_THREAD_SAFETY) return; // in certain testing modes, don't contact i5/OS system
        try
        {
            vrm_ = as400_.getVRM();                                     // @D0A @ECM

            //@P0C
            DBSQLAttributesDS request = null;
            DBReplyRequestedDS reply = null;
            int decimalSeparator, dateFormat, dateSeparator, timeFormat, timeSeparator;
            DBReplyServerAttributes serverAttributes = null;
            try
            {
                request = DBDSPool.getDBSQLAttributesDS (DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES,
                                                         id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                         + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0); //@P0C

                // We need to set a temporary CCSID just for this
                // request, since we use this request to get the
                // actual CCSID.
                //@P0D ConverterImplRemote tempConverter =
                //@P0D ConverterImplRemote.getConverter (as400_.getCcsid(), as400_);
                ConvTable tempConverter = ConvTable.getTable(as400_.getCcsid(), null); //@P0A


                // @E2D // Do not set the client CCSID.  We do not want
                // @E2D // the system to convert data, since we are going
                // @E2D // to do all conersion on the client.  By not telling
                // @E2D // the system our CCSID, then we achieve this.
                // @E2D //
                // @E2D // Note that the database host server documentation
                // @E2D // states that when we do this, the CCSID values
                // @E2D // in data formats may be incorrect and that we
                // @E2D // should always use the server job's CCSID.

                // Set the client CCSID to Unicode.                             // @E2A
                
                // @M0C - As of v5r3m0 we allow the client CCSID to be 1200 (UTF-16) which   
                // will cause our statement to flow in 1200 and our package to be 1200
                if(vrm_ >= JDUtilities.vrm530 && properties_.getInt(JDProperties.PACKAGE_CCSID) == 1200)
                {
                    request.setClientCCSID(1200);

                    if(JDTrace.isTraceOn())
                        JDTrace.logInformation(this, "Client CCSID = 1200");
                }
                else
                {
                    request.setClientCCSID(13488);

                    if(JDTrace.isTraceOn())
                        JDTrace.logInformation(this, "Client CCSID = 13488");
                }

                // This language feature code is used to tell the
                // system what language to send error messages in.
                // If that language is not installed on the system,
                // we get messages back in the default language that
                // was installed on the system.
                //
                String nlv = as400_.getNLV();  // @F1C
                request.setLanguageFeatureCode(nlv);                            // @EDC

                if (JDTrace.isTraceOn ())
                    JDTrace.logInformation (this, "Setting server NLV = " + nlv);

                // Client functional level.
                request.setClientFunctionalLevel(CLIENT_FUNCTIONAL_LEVEL_);       // @EDC

                if (JDTrace.isTraceOn ())                                                                   // @EDC
                    JDTrace.logInformation (this, "Client functional level = " + CLIENT_FUNCTIONAL_LEVEL_); // @EDC

                // Sort sequence.
                if (! properties_.equals (JDProperties.SORT, JDProperties.SORT_HEX))  //@pdc only send if not default (hex)
                {
                    JDSortSequence sortSequence = new JDSortSequence (
                                                                     properties_.getString (JDProperties.SORT),
                                                                     properties_.getString (JDProperties.SORT_LANGUAGE),
                                                                     properties_.getString (JDProperties.SORT_TABLE),
                                                                     properties_.getString (JDProperties.SORT_WEIGHT));
                    request.setNLSSortSequence (sortSequence.getType (),
                                                sortSequence.getTableFile (),
                                                sortSequence.getTableLibrary (),
                                                sortSequence.getLanguageId (),
                                                tempConverter);
                }

                request.setTranslateIndicator (0xF0);                       // @E2C
                request.setDRDAPackageSize (1);
                if(!(newAutoCommitSupport_ == 0))                           //@KBA  V5R3 or greater so run with new support
                    request.setAutoCommit(0xE8);                            //@KBA  Turn on auto commit

                if(newAutoCommitSupport_ == 1)                              //@KBA
                    request.setCommitmentControlLevelParserOption(0);       //@KBA Run under *NONE when in autocommit
                else                                                        //@KBA Run under default isolation level
                    request.setCommitmentControlLevelParserOption (transactionManager_.getCommitMode ());

                // Server attributes based on property values.
                // These all match the index within the property's
                // choices.
                dateFormat = properties_.getIndex (JDProperties.DATE_FORMAT);
                if (dateFormat != -1)
                    request.setDateFormatParserOption (dateFormat);

                dateSeparator = properties_.getIndex (JDProperties.DATE_SEPARATOR);
                if (dateSeparator != -1)
                    request.setDateSeparatorParserOption (dateSeparator);

                timeFormat = properties_.getIndex (JDProperties.TIME_FORMAT);
                if (timeFormat != -1)
                    request.setTimeFormatParserOption (timeFormat);

                timeSeparator = properties_.getIndex (JDProperties.TIME_SEPARATOR);
                if (timeSeparator != -1)
                    request.setTimeSeparatorParserOption (timeSeparator);

                decimalSeparator = properties_.getIndex (JDProperties.DECIMAL_SEPARATOR);
                if (decimalSeparator != -1)
                    request.setDecimalSeparatorParserOption (decimalSeparator);

                request.setNamingConventionParserOption (properties_.getIndex (JDProperties.NAMING));

                // Do not set the ignore decimal data error parser option.

                // If the system supports RLE data compression, then use it.               @ECA
                // Otherwise, use the old-style data compression.                          @ECA
                if (properties_.getBoolean(JDProperties.DATA_COMPRESSION))
                {            // @ECA
                    if (vrm_ >= JDUtilities.vrm510)
                    {                           // @ECA
                        dataCompression_ = DATA_COMPRESSION_RLE_;                       // @ECA
                        request.setDataCompressionOption(0);                            // @ECA
                        if (JDTrace.isTraceOn ())                                       // @ECA
                            JDTrace.logInformation (this, "Data compression = RLE");    // @ECA
                    }                                                                   // @ECA
                    else
                    {                                                              // @ECA
                        dataCompression_ = DATA_COMPRESSION_OLD_;                       // @ECA
                        request.setDataCompressionOption(1);                            // @D3A @ECC
                        if (JDTrace.isTraceOn ())                                       // @ECA
                            JDTrace.logInformation (this, "Data compression = old");    // @ECA
                    }                                                                   // @ECA
                }                                                                       // @ECA
                else
                {                                                                  // @ECA
                    dataCompression_ = DATA_COMPRESSION_NONE_;                          // @ECA
                    request.setDataCompressionOption(0);                                // @ECA
                    if (JDTrace.isTraceOn ())                                           // @ECA
                        JDTrace.logInformation (this, "Data compression = none");       // @ECA
                }                                                                       // @ECA

                // Default schema.
                if (defaultSchema_ != null)
                    request.setDefaultSQLLibraryName (defaultSchema_, tempConverter);

                // There is no need to tell the system what our code
                // page is, nor is there any reason to get a translation
                // table back from the system at this point.  This
                // will be handled later by the Converter class.

                // I haven't found a good reason to set the ambiguous select
                // option.  ODBC sets it only when block criteria is "unless
                // FOR UPDATE OF", but it causes some problems for JDBC.
                // The difference is that ODBC has the luxury of setting cursor
                // concurrency.

                request.setPackageAddStatementAllowed (properties_.getBoolean (JDProperties.PACKAGE_ADD) ? 1 : 0);

                // If the system is at V4R4 or later, then set some more attributes.
                if (vrm_ >= JDUtilities.vrm440)
                {                  // @D0C @E9C
                    // @E9D || (FORCE_EXTENDED_FORMATS_)) {

                    if(vrm_ >= JDUtilities.vrm540)          //@540 use new Super Extended Formats 
                        request.setUseExtendedFormatsIndicator(0xF2);   //@540 
                    else                                                //@540 
                        request.setUseExtendedFormatsIndicator (0xF1);

                    // Although we publish a max lob threshold of 16777216,                   @E6A
                    // the system can only handle 15728640.  We do it this                    @E6A
                    // way to match ODBC.                                                     @E6A
                    int lobThreshold = properties_.getInt (JDProperties.LOB_THRESHOLD);    // @E6A
                    if (lobThreshold <= 0)                                                 // @E6A
                        request.setLOBFieldThreshold(0);                                   // @E6A
                    else if (lobThreshold >= 15728640)                                     // @E6A
                        request.setLOBFieldThreshold(15728640);                            // @E6A
                    else                                                                   // @E6A
                        request.setLOBFieldThreshold(lobThreshold);                        // @E6C

                    extendedFormats_ = true;
                }

                // Set the default select statement type to be read-only (OS/400 v5r1
                // and earlier the default was updatable).  If the app requests updatable
                // statements we will now specify "updatable" on the RPB.  Do this
                // only to V5R1 systems with the needed PTF, and V5R2 and later systems 
                // because they have the fix needed to support 
                // altering the cursor type in the RPB.  (AmbiguousSelectOption(1)
                // means read-only)
                if (vrm_ >= JDUtilities.vrm520)                              // @J3a
                {                                                            // @J3a
                    request.setAmbiguousSelectOption(1);                     // @J3a
                    mustSpecifyForUpdate_ = false;                           // @J31a
                
                    if(vrm_ >= JDUtilities.vrm540){                         //@540 for i5/OS V5R4 and later, 128 byte column names are supported
                        //@540 - Client support information - indicate our support for ROWID data type, true autocommit
                        // and 128 byte column names
                        request.setClientSupportInformation(0xE0000000);
                        if(JDTrace.isTraceOn()){
                            JDTrace.logInformation(this, "ROWID supported = true");
                            JDTrace.logInformation(this, "True auto-commit supported = true");
                            JDTrace.logInformation(this, "128 byte column names supported = true");
                        }

                    }
                    else if (vrm_ >= JDUtilities.vrm530)                          //@KBA  For i5/OS V5R3 and later true auto commit support is supported.
                    {
                        // @KBA - Client support information - indicate our support for ROWID data type and
                        // true auto-commit
                        request.setClientSupportInformation(0xC0000000);    //@KBC
                        if(JDTrace.isTraceOn())                             //@KBA
                        {                                                   //@KBA
                            JDTrace.logInformation(this, "ROWID supported = true");             //@KBA
                            JDTrace.logInformation(this, "True auto-commit supported = true");  //@KBA
                        }                                                                       //@KBA
                   }                                                                           //@KBA
                    else                                                                        //@KBA
                    {                                                                           //@KBA
                        // @M0A - Client support information - indicate our support for ROWID data type
                        request.setClientSupportInformation(0x80000000);
                        if(JDTrace.isTraceOn())
                            JDTrace.logInformation(this, "ROWID supported = true");
                    }                                                                           //@KBA
                }

                // @M0A - added support for 63 digit decimal precision
                if(vrm_ >= JDUtilities.vrm530)
                {
                    int maximumPrecision = properties_.getInt(JDProperties.MAXIMUM_PRECISION);
                    int maximumScale = properties_.getInt(JDProperties.MAXIMUM_SCALE);
                    int minimumDivideScale = properties_.getInt(JDProperties.MINIMUM_DIVIDE_SCALE);
                    
                    // make sure that if scale is >31 we set precision to 63
                    // this is a requirement of host server to avoid a PWS0009
                    if(maximumScale > 31)
                        maximumPrecision = 63;

                    request.setDecimalPrecisionIndicators(maximumPrecision, maximumScale, minimumDivideScale);

                    if(JDTrace.isTraceOn())
                    {
                        JDTrace.logInformation(this, "Maximum decimal precision = " + maximumPrecision);
                        JDTrace.logInformation(this, "Maximum decimal scale = " + maximumScale);
                        JDTrace.logInformation(this, "Minimum divide scale = " + minimumDivideScale);
                    }

                    // @M0A - added support of hex constant parser option
                    int parserOption = properties_.getIndex(JDProperties.TRANSLATE_HEX);
                    if(parserOption != -1)
                    {
                        request.setHexConstantParserOption(parserOption);
                        if(JDTrace.isTraceOn())
                        {
                            String msg = (parserOption == 0) ? "Translate hex = character" : "Translate hex = binary";
                            JDTrace.logInformation(this, msg);
                        }
                    }

                    //@KBL - added support for hold/not hold locators
                    // Specifies whether input locators should be allocated as type hold locators or not hold locators.  
                    // If the locators are of type hold, they will not be released when a commit is done.
                    boolean holdLocators = properties_.getBoolean(JDProperties.HOLD_LOCATORS);
                    if(!holdLocators)       // Only need to set it if it is false, by default host server sets them to hold.
                    {
                        request.setInputLocatorType(0xD5);
                        if(JDTrace.isTraceOn())
                            JDTrace.logInformation(this, "Hold Locators = " + holdLocators);
                    }

                    //@KBL - added support for locator persistance.  The JDBC specification says locators should be 
                    // scoped to the transaction (ie. commit, rollback, or connection.close()) if auto commit is off
                    // host server added two options for the optional Locator Persistence ('3830'x') connection attribute:
                    // 0 -- Locators without the hold property are freed when cursor closed (locators scoped to the cursor).
                    // 1 -- Locators without the hold property are freed when the transaction is completed (locators scoped to the transaction).
                    //
                    // By default this is set to 0 by the host server, but to comply with the JDBC specification, 
                    // we should always set it to 1.
                    // Note:  this only applies when auto commit is off.  The property has no effect if auto commit is on.
                    // Locators are always scoped to the cursor when auto-commit is on.
                    request.setLocatorPersistence(1);
                }

                //@540
                if(vrm_ >= JDUtilities.vrm540){

                    //Set the query optimization goal 
                    // 0 = Optimize query for first block of data (*ALLIO) when extended dynamic packages are used; Optimize query for entire result set (*FIRSTIO) when packages are not used (default) //@PDC update comment to reflect host server default
                    // 1 = Optimize query for first block of data (*FIRSTIO)
                    // 2 = Optimize query for entire result set (*ALLIO)
                    int queryOptimizeGoal = properties_.getInt (JDProperties.QUERY_OPTIMIZE_GOAL);    
                    if(queryOptimizeGoal != 0){      // Only need to send if we are not using the default
                        if(queryOptimizeGoal == 1)
                            request.setQueryOptimizeGoal(0xC6);
                        else if(queryOptimizeGoal == 2)
                            request.setQueryOptimizeGoal(0xC1);
                    }
                    if(JDTrace.isTraceOn())
                            JDTrace.logInformation(this, "query optimize goal = " + queryOptimizeGoal);
                }

                //@550  Query Storage Limit Support
                if(vrm_ >= JDUtilities.vrm610){
                    //Set the query storage limit
                    int queryStorageLimit = properties_.getInt(JDProperties.QUERY_STORAGE_LIMIT);
                    if(queryStorageLimit != -1) // Only need to send if we are not using the default of *NOMAX (-1)
                    {
                        if(queryStorageLimit < -1)
                            request.setQueryStorageLimit(-1);
                        else if(queryStorageLimit > AS400JDBCDataSource.MAX_STORAGE_LIMIT)         // if larger than the max just set to max
                            request.setQueryStorageLimit(2147352578);
                        else
                            request.setQueryStorageLimit(queryStorageLimit);
                    }
                    if(JDTrace.isTraceOn())
                        JDTrace.logInformation(this, "query storage limit = " + queryStorageLimit);
                }

                if (JDTrace.isTraceOn ())
                {
                    if (extendedFormats_)
                        JDTrace.logInformation (this, "Using extended datastreams");
                    else
                        JDTrace.logInformation (this, "Using original datastreams");
                }

                // Send an RDB name to the system only if connecting to 
                // v5r2 and newer versions of i5/OS
                if (vrm_ >= JDUtilities.vrm520)                                                                   // @J2a
                {
                    // @J2a
                    StringBuffer RDBName = new StringBuffer(properties_.getString (JDProperties.DATABASE_NAME));  // @J2a
                    if (RDBName.length() > 0)                                                                     // @J2a
                    {
                        // @J2a
                        RDBName.append("                  ");                                                     // @J2a
                        RDBName.setLength(18);                                                                    // @J2a
                        request.setRDBName(RDBName.toString().toUpperCase(), tempConverter);                      // @J2a
                        if (JDTrace.isTraceOn ())                                                                 // @J2a
                            JDTrace.logInformation (this, "RDB Name = -->" + RDBName + "<--");                    // @J2a
                    }                                                                                             // @J2a
                }                                                                                                 // @J2a

                //@PDA jdbc40 client interface info settings
                //These three settings cannot be updated by user apps.
                //This gives driver information to host server for any logging or future diagnostics.
                if (vrm_ >= JDUtilities.vrm610)
                {
                    //these strings are not mri translated for future diagnostic tools, searching etc on host server
                    request.setInterfaceType( "JDBC", tempConverter); 
                    request.setInterfaceName( "IBM Toolbox for Java", tempConverter); 
                    request.setInterfaceLevel( AS400JDBCDriver.DRIVER_LEVEL_, tempConverter);
                    
                    //@DFA 550 decfloat rounding mode
                    short roundingMode = 0;                                                               //@DFA
                    String roundingModeStr = properties_.getString(JDProperties.DECFLOAT_ROUNDING_MODE);  //@DFA
                    if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_HALF_EVEN))    //@DFA
                        roundingMode = 0;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_UP))      //@DFA
                        roundingMode = 6;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_DOWN))    //@DFA
                        roundingMode = 2;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_CEILING)) //@DFA
                        roundingMode = 3;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_FLOOR))   //@DFA
                        roundingMode = 4;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_HALF_UP)) //@DFA
                        roundingMode = 1;                                                          //@DFA
                    else if ( roundingModeStr.equals(JDProperties.DECFLOAT_ROUNDING_MODE_HALF_DOWN))  //@DFA
                        roundingMode = 5;                                                             //@DFA
                   
                    //only need to send request if not default 0 (half even)
                    if(roundingMode != 0)                                                             //@DFA
                        request.setDecfloatRoundingMode(roundingMode);                                //@DFA
                    
                    //@eof Close on EOF
                    request.setCloseEOF( 0xE8) ;
                    
                }

                // Send the request and process the reply.
                reply = sendAndReceive (request);

                int errorClass = reply.getErrorClass();
                int returnCode = reply.getReturnCode();

                // Sort sequence attribute cannot be set.
                if ((errorClass == 7)
                    && ((returnCode == 301) || (returnCode == 303)))
                    postWarning (JDError.getSQLWarning (this, id_, errorClass, returnCode));

                // Language feature code id was not changed.   This is caused
                // when the secondary language can not be added to the library
                // list, and shows up as a PWS0003.
                else if ((errorClass == 7) && (returnCode == 304))
                    postWarning (JDError.getSQLWarning (this, id_, errorClass, returnCode));
                                                                                             
                // -704 is RDB (IASP) does not exist.  We do not go back to the system to get
                // error info since they are sending an invalid attribute exception when the
                // IASP is not found.  We can create a better error than that. 
                else if ((errorClass == 7) && (returnCode == -704))                    // @J2a
                {                                                                      // @J2a
                    try                                                                // @J2a
                    {                                                                  // @J2a
                       close();                                                        // @J2a
                    }                                                                  // @J2a
                    catch (Exception e) {} // eat errors on close                      // @J2a
                    JDError.throwSQLException(this, JDError.EXC_RDB_DOES_NOT_EXIST);   // @J2a
                }                                                                      // @J2a

                // Other system errors.
                else if (errorClass != 0)
                    JDError.throwSQLException (this, this, id_, errorClass, returnCode);

                // Process the returned server attributes.
                serverAttributes = reply.getServerAttributes ();
            }
            finally
            {
                if (request != null) request.inUse_ = false;
                if (reply != null) reply.inUse_ = false;
            }

            // The CCSID that comes back is a mixed CCSID (i.e. mixed
            // SBCS and DBCS).  This will be the CCSID that all
            // non-graphic data will be returned as for this
            // connection, so we own the converter here.
            int serverCCSID = serverAttributes.getServerCCSID();
            //@P0D converter_ = ConverterImplRemote.getConverter (serverCCSID, as400_);
            converter_ = ConvTable.getTable(serverCCSID, null); //@P0A
   
            // Get the server functional level.  It comes back as in the                           @E7A
            // format VxRxMx9999.                                                                  @E7A
            String serverFunctionalLevelAsString = serverAttributes.getServerFunctionalLevel(converter_); // @E7A
            try
            {                                                     // @E7A
                serverFunctionalLevel_ = Integer.parseInt(serverFunctionalLevelAsString.substring(6)); // @E7A
            }                                                                                   // @E7A
            catch (NumberFormatException e)
            {                                                    // @E7A
                serverFunctionalLevel_ = 0;                                                     // @E7A
            }                                                                                   // @E7A

            // Get the job number, but only if .                                                   @E8A
            if (serverFunctionalLevel_ >= 5)                                                    // @E8A
                serverJobIdentifier_ = serverAttributes.getServerJobIdentifier(converter_);     // @E8A

            // User no longer needs to specify "for update" on their SQL 
            // statements if running to v5r1 with a PTF. (V5R2 and later
            // is handled in another piece of code)           
            if ((vrm_ == JDUtilities.vrm510) &&                     //@J31a
                ( serverFunctionalLevel_ >= 10))                    //@J31a
                mustSpecifyForUpdate_ = false;                      //@J31a

            if (JDTrace.isTraceOn ())
            {                                     // @C2C
                int v = (vrm_ & 0xffff0000) >>> 16;                             // @D1A
                int r = (vrm_ & 0x0000ff00) >>>  8;                             // @D1A
                int m = (vrm_ & 0x000000ff);                                    // @D1A
                JDTrace.logInformation (this, "JDBC driver major version = "    // @C2A
                                        + AS400JDBCDriver.MAJOR_VERSION_);      // @C2A
                //Check version - V5R2 and earlier run on OS/400, V5R3 and later run on i5/OS
                if(((v==5) && (r>=3)) || (v>5))
                    JDTrace.logInformation(this, "i5/OS VRM = V" + v
                                           + "R" + r + "M" + m);
                else
                    JDTrace.logInformation (this, "OS/400 VRM = V" + v              // @C2A
                                        + "R" + r + "M" + m);                   // @C2A
                JDTrace.logInformation (this, "Server CCSID = " + serverCCSID);
                JDTrace.logInformation(this, "Server functional level = "       // @E7A
                                       + serverFunctionalLevelAsString          // @E7A
                                       + " (" + serverFunctionalLevel_ + ")");  // @E7A

                StringBuffer buffer = new StringBuffer();                                           // @E8A
                if (serverJobIdentifier_ == null)                                                   // @E8A
                    buffer.append("Not available");                                                 // @E8A
                else
                {                                                                              // @E8A
                    buffer.append(serverJobIdentifier_.substring(20, 26).trim());  // job number    // @E8A
                    buffer.append('/');                                                             // @E8A
                    buffer.append(serverJobIdentifier_.substring(10, 20).trim());  // user name     // @E8A
                    buffer.append('/');                                                             // @E8A
                    buffer.append(serverJobIdentifier_.substring(0, 10).trim());   // job name      // @E8A
                }                                                                                   // @E8A
                JDTrace.logInformation(this, "Server job identifier = " + buffer);                  // @E8A
            }                                                                   // @C2A

            // @E2D // Wait to load graphic converter until it is needed.
            // @E2D graphicConverter_ = null;
            // @E2D graphicConverterLoaded_ = false;

            // Get the catalog name from the RDB entry.  If no RDB entry is
            // set on the system, then use the system name from the AS400 object
            // (which originally came from the URL).
            String rdbEntry = serverAttributes.getRelationalDBName (converter_).trim();
            if ((rdbEntry.length() > 0) && (! rdbEntry.equalsIgnoreCase ("*N")))
                catalog_ = rdbEntry;

            // In the cases where defaults come from the server
            // job, get the defaults for properties that were not set.
            if (decimalSeparator == -1)
            {
                switch (serverAttributes.getDecimalSeparatorPO ())
                {
                case 0:
                    properties_.setString (JDProperties.DECIMAL_SEPARATOR, JDProperties.DECIMAL_SEPARATOR_PERIOD);
                    break;
                case 1:
                    properties_.setString (JDProperties.DECIMAL_SEPARATOR, JDProperties.DECIMAL_SEPARATOR_COMMA);
                    break;
                }
            }

            if (dateFormat == -1)
            {
                switch (serverAttributes.getDateFormatPO ())
                {
                case 0:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_JULIAN);
                    break;
                case 1:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_MDY);
                    break;
                case 2:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_DMY);
                    break;
                case 3:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_YMD);
                    break;
                case 4:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_USA);
                    break;
                case 5:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_ISO);
                    break;
                case 6:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_EUR);
                    break;
                case 7:
                    properties_.setString (JDProperties.DATE_FORMAT, JDProperties.DATE_FORMAT_JIS);
                    break;
                }
            }

            if (dateSeparator == -1)
            {
                switch (serverAttributes.getDateSeparatorPO ())
                {
                case 0:
                    properties_.setString (JDProperties.DATE_SEPARATOR, JDProperties.DATE_SEPARATOR_SLASH);
                    break;
                case 1:
                    properties_.setString (JDProperties.DATE_SEPARATOR, JDProperties.DATE_SEPARATOR_DASH);
                    break;
                case 2:
                    properties_.setString (JDProperties.DATE_SEPARATOR, JDProperties.DATE_SEPARATOR_PERIOD);
                    break;
                case 3:
                    properties_.setString (JDProperties.DATE_SEPARATOR, JDProperties.DATE_SEPARATOR_COMMA);
                    break;
                case 4:
                    properties_.setString (JDProperties.DATE_SEPARATOR, JDProperties.DATE_SEPARATOR_SPACE);
                    break;
                }
            }

            if (timeFormat == -1)
            {
                switch (serverAttributes.getTimeFormatPO ())
                {
                case 0:
                    properties_.setString (JDProperties.TIME_FORMAT, JDProperties.TIME_FORMAT_HMS);
                    break;
                case 1:
                    properties_.setString (JDProperties.TIME_FORMAT, JDProperties.TIME_FORMAT_USA);
                    break;
                case 2:
                    properties_.setString (JDProperties.TIME_FORMAT, JDProperties.TIME_FORMAT_ISO);
                    break;
                case 3:
                    properties_.setString (JDProperties.TIME_FORMAT, JDProperties.TIME_FORMAT_EUR);
                    break;
                case 4:
                    properties_.setString (JDProperties.TIME_FORMAT, JDProperties.TIME_FORMAT_JIS);
                    break;
                }
            }

            if (timeSeparator == -1)
            {
                switch (serverAttributes.getTimeSeparatorPO ())
                {
                case 0:
                    properties_.setString (JDProperties.TIME_SEPARATOR, JDProperties.TIME_SEPARATOR_COLON);
                    break;
                case 1:
                    properties_.setString (JDProperties.TIME_SEPARATOR, JDProperties.TIME_SEPARATOR_PERIOD);
                    break;
                case 2:
                    properties_.setString (JDProperties.TIME_SEPARATOR, JDProperties.TIME_SEPARATOR_COMMA);
                    break;
                case 3:
                    properties_.setString (JDProperties.TIME_SEPARATOR, JDProperties.TIME_SEPARATOR_SPACE);
                    break;
                }
            }
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }
        // @J5D catch (IOException e) {
        catch (UnsupportedEncodingException e)
        {                      // @J5C
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }
    }



    //@A3A
    // Implementation note:  Don't use this object internally because we could be running in a proxy environment
    // The purpose of this method is to simply hold the full AS400 object so it can be retrieved from the Connection
    void setSystem (AS400 as400)
    throws SQLException // @EGA
    {
        as400PublicClassObj_    = as400;
    }



    // @D2C
    /**
    Sets the transaction isolation level.  The transaction
    isolation level cannot be changed while in the middle of
    a transaction.
    
    <p>JDBC and DB2 for i5/OS use different terminology for transaction
    isolation levels.  The following table provides a terminology
    mapping:
    
    <p><table border>
    <tr><th>i5/OS isolation level</th><th>JDBC transaction isolation level</th></tr>
    <tr><td>*CHG</td> <td>TRANSACTION_READ_UNCOMMITTED</td></tr>
    <tr><td>*CS</td>  <td>TRANSACTION_READ_COMMITTED</td></tr>
    <tr><td>*ALL</td> <td>TRANSACTION_READ_REPEATABLE_READ</td></tr>
    <tr><td>*RR</td>  <td>TRANSACTION_SERIALIZABLE</td></tr>
    </table>
    
    @param      level   The transaction isolation level.  Possible
                        values are:
                        <ul>
                        <li>TRANSACTION_READ_UNCOMMITTED
                        <li>TRANSACTION_READ_COMMITTED
                           <li>TRANSACTION_REPEATABLE_READ
                        <li>TRANSACTION_SERIALIZABLE
                        </ul>
    
    @exception      SQLException    If the connection is not open,
                                    the input level is not valid
                                    or unsupported, or a transaction
                                    is active.
    **/
    public void setTransactionIsolation (int level)
    throws SQLException
    {
        checkOpen ();

        transactionManager_.setIsolation (level);

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "Transaction isolation", transactionManager_.getIsolation ());
    }



    // JDBC 2.0
    /**
    Sets the type map to be used for distinct and structured
    types.
    
    <p>Note: Distinct types are supported by DB2 for i5/OS, but
    are not externalized by the IBM Toolbox for Java JDBC driver.
    In other words, distinct types behave as if they are the underlying
    type.  Structured types are not supported by DB2 for i5/OS.
    Consequently, this driver does not support the type map.
    
    @param typeMap  The type map.
    
    @exception  SQLException    This exception is always thrown.
    **/
    public void setTypeMap (Map typeMap)
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }



    /**
    Returns the connection's catalog name.  This is the
    name of the i5/OS system.
    
    @return     The catalog name.
    **/
    public String toString ()
    {
        return catalog_;
    }



    /**
    Indicates if the connection is using extended formats.
    
    @return     true if the connection is using extended formats, false
                otherwise.
    **/
    boolean useExtendedFormats ()
    throws SQLException // @EGA
    {
        return extendedFormats_;
    }
 
    
    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCConnection", "java.sql.Connection" };
    }

    //@PDA jdbc40
    /**
     * Returns true if the connection has not been closed and is still valid.  
     * The driver shall submit a query on the connection or use some other 
     * mechanism that positively verifies the connection is still valid when 
     * this method is called.
     * <p>
     * The query submitted by the driver to validate the connection shall be 
     * executed in the context of the current transaction.
     * 
     * @param timeout -     The time in seconds to wait for the database operation 
     *                      used to validate the connection to complete.  If 
     *                      the timeout period expires before the operation 
     *                      completes, this method returns false.  A value of 
     *                      0 indicates a timeout is not applied to the 
     *                      database operation.  Note that currently the timeout
     *                      value is not used.
     * <p>
     * @return true if the connection is valid, false otherwise
     * @exception SQLException if a database access error occurs.
     */ 
    public boolean isValid(int timeout) throws SQLException 
    { 
        DBSQLRequestDS request = null;
        DBReplyRequestedDS reply = null; 
        int errorClass = 0; 
        int returnCode = 0; 
        ReentrantLock lock = new ReentrantLock();

        try 
        { 
            /* inner class to run timer in sep thread */
            class CommTimer implements Runnable 
            {      
       
                Thread otherThread;
                ReentrantLock lock;
                int timeout;
                
                public void run() 
                { 
                    try 
                    { 
                        Thread.sleep(timeout * 1000);
                        lock.lockInterruptibly(); //lock, so only one thread can call interrupt
                        otherThread.interrupt();
                        
                    }catch(InterruptedException ie)
                    { 
                        //interrupted from notifyThread because request/reply is done.  just return from run()
                        if (JDTrace.isTraceOn())
                            JDTrace.logInformation (this, "Connection.isValid timer interrupted and stopped");
                    } 
                    
                }
                
                public CommTimer(Thread otherThread, int timeout, ReentrantLock lock ) 
                { 
                    this.otherThread = otherThread;
                    this.timeout = timeout;
                    this.lock = lock;
                } 
            };
            
            CommTimer timer = new CommTimer( Thread.currentThread(), timeout, lock); //pass in ref to main thread so timer can interrupt if blocked on IO
            Thread t = new  Thread(timer);
            t.start(); //sleeps for timeout and then interrupts main thread if it is still blocked on IO
            
            try
            {
                request = DBDSPool.getDBSQLRequestDS(DBSQLRequestDS.FUNCTIONID_TEST_CONNECTION, id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0); 
                reply = sendAndReceive(request); 

                lock.lockInterruptibly(); //lock, so only one thread can call interrupt
                t.interrupt(); //stop timer thread
                errorClass = reply.getErrorClass(); 
                returnCode = reply.getReturnCode();
             
            }catch(Exception ex)
            {
                //interruptedException is wrapped in sqlException
                //if exception occurs, just return false since connection is not valid
                //this happens if timer ends before sendAndReceive returns
                if (JDTrace.isTraceOn())
                    JDTrace.logInformation (this, "Connection.isValid timed out or could not verify valid connection");
                return false;
            } 
            
            if(errorClass == 7 && returnCode == -201) 
                return true; 
            else
                return false; 
            
        }
        catch(Exception e) 
        { 
            //implmentation note:  if any exception happens, just return false, since conn is not valid
            return false;  
        } 
        finally
        { 
            if (request != null) 
                request.inUse_ =   false; 
            if (reply != null)
                reply.inUse_ = false; 
            
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, "Connection.isValid call complete");
        } 
         
    }
          
    //@PDA jdbc40
    /**
     * Sets the value of the client info property specified by name to the 
     * value specified by value.  
     * <p>
     * Applications may use the <code>DatabaseMetaData.getClientInfoProperties</code> 
     * method to determine the client info properties supported by the driver 
     * and the maximum length that may be specified for each property.
     * <p>
     * The driver stores the value specified in a suitable location in the 
     * database.  For example in a special register, session parameter, or 
     * system table column.  For efficiency the driver may defer setting the 
     * value in the database until the next time a statement is executed or 
     * prepared.  Other than storing the client information in the appropriate 
     * place in the database, these methods shall not alter the behavior of 
     * the connection in anyway.  The values supplied to these methods are 
     * used for accounting, diagnostics and debugging purposes only.
     * <p>
     * The driver shall generate a warning if the client info name specified 
     * is not recognized by the driver.
     * <p>
     * If the value specified to this method is greater than the maximum 
     * length for the property the driver may either truncate the value and 
     * generate a warning or generate a <code>SQLException</code>.  If the driver 
     * generates a <code>SQLException</code>, the value specified was not set on the 
     * connection.
     * <p>
     * The following client info properties are supported in Toobox for Java.  
     * <p>
     * <ul>
     * <li>ApplicationName  -   The name of the application currently utilizing 
     *                          the connection</li>
     * <li>ClientUser       -   The name of the user that the application using 
     *                          the connection is performing work for.  This may 
     *                          not be the same as the user name that was used 
     *                          in establishing the connection.</li>
     * <li>ClientHostname   -   The hostname of the computer the application 
     *                          using the connection is running on.</li>
     * <li>ClientAccounting -   Client accounting information.</li>
     * <li>ClientProgramID  -   The client program identification.</li>
     * </ul>
     * <p>
     * @param name      The name of the client info property to set 
     * @param value     The value to set the client info property to.  If the 
     *                  value is null, the current value of the specified
     *                  property is cleared.
     * <p>
     * @throws  SQLClientInfoException if the database server returns an error while 
     *          setting the client info value on the database server.
     * <p>
     */
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {

        DBSQLAttributesDS request = null;
        DBReplyRequestedDS reply = null;
        ConvTable tempConverter = null;

        String oldValue = null;  //save in case we get error from host db
        
        // in order to reset if null value is passed in, use empty string
        if (value == null)
            value = "";
        
        try
        {
            if (getVRM() >= JDUtilities.vrm610)
            {
                request = DBDSPool.getDBSQLAttributesDS(DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES, id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);
                tempConverter = ConvTable.getTable(as400_.getCcsid(), null);
            }

            if (name.equals(applicationNamePropertyName_))
            {
                oldValue = applicationName_;
                applicationName_ = value;
                if (getVRM() >= JDUtilities.vrm610)
                    request.setClientInfoApplicationName(value, tempConverter);

            } else if (name.equals(clientUserPropertyName_))
            {
                oldValue = clientUser_;
                clientUser_ = value;
                if (getVRM() >= JDUtilities.vrm610)
                    request.setClientInfoClientUser(value, tempConverter);

            } else if (name.equals(clientAccountingPropertyName_))
            {
                oldValue = clientAccounting_;
                clientAccounting_ = value;
                if (getVRM() >= JDUtilities.vrm610)
                    request.setClientInfoClientAccounting(value, tempConverter);

            } else if (name.equals(clientHostnamePropertyName_))
            {
                oldValue = clientHostname_;
                clientHostname_ = value;
                if (getVRM() >= JDUtilities.vrm610)
                    request.setClientInfoClientHostname(value, tempConverter);

            } else if (name.equals(clientProgramIDPropertyName_))  //@PDA add block for ProgramID
            {
                oldValue = clientProgramID_;
                clientProgramID_ = value;
                if (getVRM() >= JDUtilities.vrm610)
                    request.setClientInfoProgramID(value, tempConverter);

            } else
            {
                oldValue = null;
                // post generic syntax error for invalid clientInfo name
                postWarning(JDError.getSQLWarning(JDError.EXC_SYNTAX_ERROR));
            }

            if ((getVRM() >= JDUtilities.vrm610) && (oldValue != null))
            {
                reply = sendAndReceive(request);
                int errorClass = reply.getErrorClass();
                //throw SQLException and wrap in SQLClientInfoException below
                if (errorClass != 0)     
                    JDError.throwSQLException(this, id_, errorClass, reply.getReturnCode());
                
            }
        } catch (Exception e)
        {
            //reset old value
            if (name.equals(applicationNamePropertyName_))
                applicationName_ = oldValue;
            else if (name.equals(clientUserPropertyName_))
                clientUser_ = oldValue;
            else if (name.equals(clientAccountingPropertyName_))
                clientAccounting_ = oldValue;
            else if (name.equals(clientHostnamePropertyName_))
                clientHostname_ = oldValue;
            else if (name.equals(clientProgramIDPropertyName_)) //@pda
                clientProgramID_ = oldValue;

            //@PDD jdbc40 merge HashMap<String,ClientInfoStatus> m = new HashMap<String,ClientInfoStatus>();
            HashMap m = new HashMap();
            m.put(name, ClientInfoStatus.REASON_UNKNOWN);
            JDError.throwSQLClientInfoException( this, JDError.EXC_INTERNAL, e, m );
        } finally
        {
            if (request != null)
                request.inUse_ = false;
            if (reply != null)
                reply.inUse_ = false;
        }
    }

    // @PDA jdbc40
    /**
     * Sets the value of the connection's client info properties. The
     * <code>Properties</code> object contains the names and values of the
     * client info properties to be set. The set of client info properties
     * contained in the properties list replaces the current set of client info
     * properties on the connection. If a property that is currently set on the
     * connection is not present in the properties list, that property is
     * cleared. Specifying an empty properties list will clear all of the
     * properties on the connection. See
     * <code>setClientInfo (String, String)</code> for more information.
     * <p>
     * If an error occurs in setting any of the client info properties, a
     * <code>ClientInfoException</code> is thrown. The
     * <code>ClientInfoException</code> contains information indicating which
     * client info properties were not set. The state of the client information
     * is unknown because some databases do not allow multiple client info
     * properties to be set atomically. For those databases, one or more
     * properties may have been set before the error occurred.
     * <p>
     * 
     * The following client info properties are supported in Toobox for Java.  
     * <p>
     * <ul>
     * <li>ApplicationName  -   The name of the application currently utilizing 
     *                          the connection</li>
     * <li>ClientUser       -   The name of the user that the application using 
     *                          the connection is performing work for.  This may 
     *                          not be the same as the user name that was used 
     *                          in establishing the connection.</li>
     * <li>ClientHostname   -   The hostname of the computer the application 
     *                          using the connection is running on.</li>
     * <li>ClientAccounting -   Client accounting information.</li>
     * <li>ClientProgramID  -   The client program identification.</li>
     * </ul>
     * <p>
     * 
     * @param properties
     *            the list of client info properties to set
     *            <p>
     * @throws SQLClientInfoException
     *             if the database server returns an error while setting the
     *             clientInfo values on the database server
     *             <p>
     */
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        String newApplicationName = properties.getProperty(applicationNamePropertyName_);
        String newClientHostname = properties.getProperty(clientHostnamePropertyName_);
        String newClientUser = properties.getProperty(clientUserPropertyName_);
        String newClientAccounting = properties.getProperty(clientAccountingPropertyName_);
        String newClientProgramID = properties.getProperty(clientProgramIDPropertyName_); //@pda
        
        //In order to reset if null value is passed in, use empty string
        //per javadoc, clear its value if not specified in properties 
        if (newApplicationName == null)
            newApplicationName = "";
        if (newClientHostname == null)
            newClientHostname = "";
        if (newClientUser == null)
            newClientUser = "";
        if (newClientAccounting == null)
            newClientAccounting = "";
        if (newClientProgramID == null)  //@PDA
            newClientProgramID = "";
        
        DBSQLAttributesDS request = null;
        DBReplyRequestedDS reply = null;
        ConvTable tempConverter = null;
        try
        {
            if (getVRM() >= JDUtilities.vrm610)
            {
                request = DBDSPool.getDBSQLAttributesDS(DBSQLAttributesDS.FUNCTIONID_SET_ATTRIBUTES, id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA + DBBaseRequestDS.ORS_BITMAP_SERVER_ATTRIBUTES, 0);
                tempConverter = ConvTable.getTable(as400_.getCcsid(), null);
                
                request.setClientInfoApplicationName(newApplicationName, tempConverter);
                
                request.setClientInfoClientUser(newClientUser, tempConverter);
                
                request.setClientInfoClientAccounting(newClientAccounting, tempConverter);
                
                request.setClientInfoClientHostname(newClientHostname, tempConverter);
                
                request.setClientInfoProgramID(newClientProgramID, tempConverter); //@pda
                
                reply = sendAndReceive(request);
                int errorClass = reply.getErrorClass();
                if (errorClass != 0)
                    JDError.throwSQLException(this, id_, errorClass, reply.getReturnCode());
            }
            
            //update local values after request/reply in case of exception
            applicationName_ = newApplicationName;
            clientHostname_ = newClientHostname;
            clientUser_ = newClientUser;
            clientAccounting_ = newClientAccounting;
            clientProgramID_ = newClientProgramID;
            
        } catch (Exception e)
        {
            //create Map<String,ClientInfoStatus> for exception constructor
            //@PDD jdbc40 merge HashMap<String,ClientInfoStatus> m = new HashMap<String,ClientInfoStatus>();
            HashMap m = new HashMap();
            Enumeration clientInfoNames = properties.keys();
            while( clientInfoNames.hasMoreElements())
            {
                String clientInfoName = (String)clientInfoNames.nextElement();
                m.put(clientInfoName, ClientInfoStatus.REASON_UNKNOWN);
            }
            JDError.throwSQLClientInfoException( this, JDError.EXC_INTERNAL, e, m);
          
        } finally
        {
            if (request != null)
                request.inUse_ = false;
            if (reply != null)
                reply.inUse_ = false;
        }
        
    }

    //@PDA jdbc40
    /**
     * Returns the value of the client info property specified by name.  This 
     * method may return null if the specified client info property has not 
     * been set and does not have a default value.  This method will also 
     * return null if the specified client info property name is not supported 
     * by the driver.
     * <p>
     * Applications may use the <code>DatabaseMetaData.getClientInfoProperties</code>
     * method to determine the client info properties supported by the driver.
     * <p>
     * 
     * The following client info properties are supported in Toobox for Java.  
     * <p>
     * <ul>
     * <li>ApplicationName  -   The name of the application currently utilizing 
     *                          the connection</li>
     * <li>ClientUser       -   The name of the user that the application using 
     *                          the connection is performing work for.  This may 
     *                          not be the same as the user name that was used 
     *                          in establishing the connection.</li>
     * <li>ClientHostname   -   The hostname of the computer the application 
     *                          using the connection is running on.</li>
     * <li>ClientAccounting -   Client accounting information.</li>
     * <li>ClientProgramID  -   The client program identification.</li>
     * </ul>
     * <p>
     * @param name      The name of the client info property to retrieve
     * <p>
     * @return          The value of the client info property specified
     * <p>
     * @throws SQLException     if the database server returns an error when 
     *                          fetching the client info value from the database.
     * <p>
     * see java.sql.DatabaseMetaData#getClientInfoProperties
     */
    public String getClientInfo(String name) throws SQLException
    {
        if (name.equals(applicationNamePropertyName_))
            return applicationName_;
        else if (name.equals(clientUserPropertyName_))
            return clientUser_;
        else if (name.equals(clientAccountingPropertyName_))
            return clientAccounting_;
        else if (name.equals(clientHostnamePropertyName_))
            return clientHostname_;
        else if (name.equals(clientProgramIDPropertyName_))  //@pda
            return clientProgramID_;
        else
        {
            //post generic syntax error for invalid clientInfo name
            //since javadoc for setClientInfo(String,String) says to generate warning, we will do same here and return null
            postWarning(JDError.getSQLWarning(JDError.EXC_SYNTAX_ERROR));
            return null;
        }
    }

    //@PDA jdbc40
    /**
     * Returns a list containing the name and current value of each client info 
     * property supported by the driver.  The value of a client info property 
     * may be null if the property has not been set and does not have a 
     * default value.
     * <p>
     * 
     * The following client info properties are supported in Toobox for Java.  
     * <p>
     * <ul>
     * <li>ApplicationName  -   The name of the application currently utilizing 
     *                          the connection</li>
     * <li>ClientUser       -   The name of the user that the application using 
     *                          the connection is performing work for.  This may 
     *                          not be the same as the user name that was used 
     *                          in establishing the connection.</li>
     * <li>ClientHostname   -   The hostname of the computer the application 
     *                          using the connection is running on.</li>
     * <li>ClientAccounting -   Client accounting information.</li>
     * <li>ClientProgramID  -   The client program identification.</li>
     * </ul>
     * <p>
     * @return  A <code>Properties</code> object that contains the name and current value of 
     *          each of the client info properties supported by the driver.  
     * <p>
     * @throws  SQLException if the database server returns an error when 
     *          fetching the client info values from the database
     */
    public Properties getClientInfo() throws SQLException
    {
        Properties props = new Properties();
        props.setProperty(applicationNamePropertyName_, applicationName_);
        props.setProperty(clientAccountingPropertyName_, clientAccounting_);
        props.setProperty(clientHostnamePropertyName_, clientHostname_);
        props.setProperty(clientUserPropertyName_, clientUser_);
        props.setProperty(clientProgramIDPropertyName_, clientProgramID_); //@pda
        return props;
    }
    
    //@PDA jdbc40
    /**
     * Constructs an object that implements the <code>Clob</code> interface. The object
     * returned initially contains no data.  The <code>setAsciiStream</code>,
     * <code>setCharacterStream</code> and <code>setString</code> methods of 
     * the <code>Clob</code> interface may be used to add data to the <code>Clob</code>.
     * @return An object that implements the <code>Clob</code> interface
     * @throws SQLException if an object that implements the
     * <code>Clob</code> interface can not be constructed.
     *
     */
    public Clob createClob() throws SQLException
    {
        return new AS400JDBCClob("", AS400JDBCClob.MAX_LOB_SIZE);
    }
    
    //@PDA jdbc40
    /**
     * Constructs an object that implements the <code>Blob</code> interface. The object
     * returned initially contains no data.  The <code>setBinaryStream</code> and
     * <code>setBytes</code> methods of the <code>Blob</code> interface may be used to add data to
     * the <code>Blob</code>.
     * @return  An object that implements the <code>Blob</code> interface
     * @throws SQLException if an object that implements the
     * <code>Blob</code> interface can not be constructed
     *
     */
    public Blob createBlob() throws SQLException
    {
        return new AS400JDBCBlob(new byte[0], AS400JDBCBlob.MAX_LOB_SIZE);  //@pdc 0 len array
    }
  
    //@PDA jdbc40
    /**
     * Constructs an object that implements the <code>NClob</code> interface. The object
     * returned initially contains no data.  The <code>setAsciiStream</code>,
     * <code>setCharacterStream</code> and <code>setString</code> methods of the <code>NClob</code> interface may
     * be used to add data to the <code>NClob</code>.
     * @return An object that implements the <code>NClob</code> interface
     * @throws SQLException if an object that implements the
     * <code>NClob</code> interface can not be constructed.
     *
     */
    public NClob createNClob() throws SQLException
    {
        return new AS400JDBCNClob("", AS400JDBCNClob.MAX_LOB_SIZE);
    }

    //@PDA jdbc40
    /**
     * Constructs an object that implements the <code>SQLXML</code> interface. The object
     * returned initially contains no data. The <code>createXmlStreamWriter</code> object and
     * <code>setString</code> method of the <code>SQLXML</code> interface may be used to add data to the <code>SQLXML</code>
     * object.
     * @return An object that implements the <code>SQLXML</code> interface
     * @throws SQLException if an object that implements the <code>SQLXML</code> interface can not
     * be constructed
     */
    public SQLXML createSQLXML() throws SQLException
    {
        return new AS400JDBCSQLXML("", AS400JDBCSQLXML.MAX_XML_SIZE); 
    }

    //@PDA jdbc40
    /**
     * Factory method for creating Array objects.
     *
     * @param typeName the SQL name of the type the elements of the array map to. The typeName is a
     * database-specific name which may be the name of a built-in type, a user-defined type or a standard  SQL type supported by this database. This
     *  is the value returned by <code>Array.getBaseTypeName</code>
     * @param elements the elements that populate the returned object
     * @return an Array object whose elements map to the specified SQL type
     * @throws SQLException if a database error occurs, the typeName is null or this method is called on a closed connection
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this data type
     */
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    //@PDA jdbc40
    /**
     * Factory method for creating Struct objects.
     *
     * @param typeName the SQL type name of the SQL structured type that this <code>Struct</code> 
     * object maps to. The typeName is the name of  a user-defined type that
     * has been defined for this database. It is the value returned by
     * <code>Struct.getSQLTypeName</code>.
     * @param attributes the attributes that populate the returned object
     *  @return a Struct object that maps to the given SQL type and is populated with the given attributes
     * @throws SQLException if a database error occurs, the typeName is null or this method is called on a closed connection
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this data type
     */
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {   
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }
    

    //@2KRA
    /**
     * Starts or stops the Database Host Server trace for this connection.
     * Note:  This method is only supported when running to i5/OS V5R3 or later 
     * and is ignored if you specified to turn on database host server tracing
     * using the 'server trace' connection property.
     * @param trace true to start database host server tracing, false to end it.
     */
    public void setDBHostServerTrace(boolean trace){
        try{
            if(getVRM() >= JDUtilities.vrm530){
                // See if tracing was specified by server trace property
                // Server Job Trace
                boolean traceServerJob = ((traceServer_ & ServerTrace.JDBC_TRACE_SERVER_JOB) > 0);  
                // Database Host Server Trace
                boolean traceDatabaseHostServer = (((traceServer_ & ServerTrace.JDBC_TRACE_DATABASE_HOST_SERVER) > 0)); 
                String serverJobIdentifier = getServerJobIdentifier();
                boolean SQLNaming = properties_.getString(JDProperties.NAMING).equals(JDProperties.NAMING_SQL);

                if(!traceDatabaseHostServer){   // database host server trace was not already started
                    if(trace)   // user requested tracing be turned on
                    {
                        try{
                            if(getVRM() == JDUtilities.vrm530){  // run command for V5R3
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                
                                                       serverJobIdentifier.substring(20) +                    
                                                       ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*TRCTYPE) " +
                                                       "TRCTYPE((TESTA *INFO))", SQLNaming);                 
                            }
                            else{   // run command for V5R4 and higher                                    
                                JDUtilities.runCommand(this, "QSYS/STRTRC SSNID(QJT" +                 
                                                       serverJobIdentifier.substring(20) +                     
                                                       ") JOB(*) MAXSTG(128000) JOBTRCTYPE(*TRCTYPE) " +       
                                                       "TRCTYPE((*DBHSVR *INFO))", SQLNaming);
                            }
                            databaseHostServerTrace_ = true;
                        }catch(Exception e){
                            JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to start database host server tracing failed, could not trace server job");    
                        }
                    }
                    else // user requested tracing be turned off
                    {
                        // Only issue ENDTRC if not already done.
                        if(!traceServerJob)     // turn off it we don't have to wait to turn off server job tracing
                        {
                            try{
                                JDUtilities.runCommand(this, "QSYS/ENDTRC SSNID(QJT" +
                                                       serverJobIdentifier.substring(20) +
                                                       ") DTAOPT(*LIB) DTALIB(QUSRSYS) RPLDTA(*YES) PRTTRC(*YES)", SQLNaming );

                                JDUtilities.runCommand(this, "QSYS/DLTTRC DTAMBR(QJT" +
                                                       serverJobIdentifier.substring(20) +
                                                       ") DTALIB(QUSRSYS)", SQLNaming );
                                databaseHostServerTrace_ = false;
                            }
                            catch(Exception e){
                                JDTrace.logDataEvenIfTracingIsOff(this, "Attempt to end database host server tracing failed.");
                            }
                        }
                    }
                }
            }
        }catch(SQLException e){
            if(JDTrace.isTraceOn())
                JDTrace.logInformation(this, "Attempt to start/stop database host server tracing failed.");
        }

    }

}
