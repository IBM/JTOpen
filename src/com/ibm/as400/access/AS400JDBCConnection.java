///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2018 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
/* ifdef JDBC40
import java.sql.ClientInfoStatus;
import java.sql.SQLClientInfoException;
import java.sql.SQLPermission;
endif */
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
/* ifdef JDBC40
import java.sql.NClob;
endif */
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
/* ifdef JDBC40
import java.sql.SQLXML;
endif */
import java.sql.Statement;
import java.sql.Savepoint;                        // @E10a
import java.sql.Struct;
/* ifdef JDBC40
import java.util.HashMap;
endif */
import java.util.Map;
import java.util.Properties;
/* ifdef JDBC40
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executor;
endif */


/**
<p>The AS400JDBCConnection interface provides base class 
used by all JDBC connection objects in JTOpen. 
**/
public 
abstract class  AS400JDBCConnection
/*ifdef JDBC40
extends ToolboxWrapper
endif */

implements Connection
{

  static final int            DATA_COMPRESSION_NONE_  = 0;            // @ECA
  static final int            DATA_COMPRESSION_OLD_   = 1;            // @ECA
  static final int            DATA_COMPRESSION_RLE_   = 0x3832;       // @ECA @EIC @EJC
  protected boolean inFinalizer_;


    /**
    Cancels a statement within this connection.

    @param id   The ID of the statement.

    @exception              SQLException    If the statement cannot be executed.
    **/
    abstract void cancel(int id) throws SQLException;
    

    /**
    Checks that the specified SQL statement can be executed.
    This decision is based on the access specified by the caller
    and the read only mode.

    @param   sqlStatement   The SQL statement.

    @exception              SQLException    If the statement cannot be executed.
    **/
    abstract void checkAccess (JDSQLStatement sqlStatement)
    throws SQLException;



    // @E8A
    /**
    Checks to see if we are cancelling a statement.  If so, wait until the
    cancel is done.  If not, go ahead.
    **/
     abstract void checkCancel();


    //@F3A
    /**
    Checks if what the user passed in for holdability is valid.
    **/
     abstract boolean checkHoldabilityConstants (int holdability);


    /**
    Checks that the connection is open.  Public methods
    that require an open connection should call this first.

    @exception  SQLException    If the connection is not open.
    **/
     abstract void checkOpen ()
    throws SQLException;



    /**
    Clears all warnings that have been reported for the connection.
    After this call, getWarnings() returns null until a new warning
    is reported for the connection.

    @exception SQLException If an error occurs.
    **/
     abstract public void clearWarnings ()
    throws SQLException;



    /**
    Releases the connection's resources immediately instead of waiting
    for them to be automatically released.  This rolls back any active
    transactions, closes all statements that are running in the context
    of the connection, and disconnects from the IBM i system.

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
     abstract public void close ()
    throws SQLException;

    /*
     * handle the processing of the abort.   @D7A
     */
     abstract void handleAbort();

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
     abstract public void commit ()
    throws SQLException;


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
     abstract void setCheckStatementHoldability(boolean check);



    /**
    Corrects the result set type based on the result set concurrency
    and posts a warning.

    @param resultSetType            The result set type.
    @param resultSetConcurrency     The result set concurrency.
    @return                         The correct result set type.
    **/
     abstract int correctResultSetType (int resultSetType,
                                      int resultSetConcurrency)
    throws SQLException;



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
     abstract public Statement createStatement ()
    throws SQLException;



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
     abstract  public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency)
    throws SQLException;


    //@G4A JDBC 3.0
    /**
    Creates a Statement object for executing SQL statements without
    parameters.  If the same SQL statement is executed many times, it
    is more efficient to use prepareStatement().

    <p>Full functionality of this method requires support in OS/400 V5R2
    or IBM i.  If connecting to OS/400 V5R1 or earlier, the value for
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
     abstract public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency,
                                      int resultSetHoldability)
    throws SQLException;




    /**
    Outputs debug information for a request.  This should only be used
    for debugging the JDBC driver and is not intended for production code.

    @param   request     The request.
    **/
     abstract void debug (DBBaseRequestDS request);



    /**
    Outputs debug information for a reply.  This should only be used
    for debugging the JDBC driver and is not intended for production code.

    @param   reply     The reply.
    **/
     abstract void debug (DBReplyRequestedDS reply);



   


    /**
    Returns the AS400 object for this connection.

    @return     The AS400 object.
    **/
     abstract  AS400Impl getAS400 ()
    throws SQLException;



    /**
    Returns the auto-commit state.

    @return     true if the connection is in auto-commit mode;
                false otherwise.

    @exception  SQLException    If the connection is not open.
    **/
     abstract public boolean getAutoCommit ()
    throws SQLException;



    /**
    Returns the catalog name.

    @return     The catalog name.

    @exception  SQLException    If the connection is not open.
    **/
     abstract public String getCatalog ()
    throws SQLException;

    //@cc1
    /**
     * This method returns the concurrent access resolution setting.
     * This method has no effect on IBM i V6R1 or earlier.
     * The possible values for this property are {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME} and
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS},
     * with the property defaulting to {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET}.
     * Setting this property to default exhibits the default behavior on the servers
     * i.e., the semantic applied for read
     * transactions to avoid locks will be determined by the server.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED} specifies that driver will flow USE CURRENTLY COMMITTED
     * to server.  Whether CURRENTLY COMMITTED will actually be in effect is
     * ultimately determined by server.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME} specifies that driver will flow WAIT FOR OUTCOME
     * to server.  This will disable the CURRENTLY COMMITTED behavior at the server,
     * if enabled, and the server will wait for the commit or rollback of data in the process of
     * being updated.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS} specifies that driver will flow SKIP LOCKS
     * to server.  This directs the database manager to skip records in the case of record lock conflicts.
     *
     * @return  The concurrent access resolution setting.    Possible return valuse:
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME}, or
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS}
     */
     abstract public int getConcurrentAccessResolution ();

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
     abstract ConvTable getConverter (int ccsid) //@P0C
    throws SQLException;


    // @ECA
    /**
    Returns the style of data compression.

    @return The style of data compression.  Possible values are DATA_COMPRESSION_NONE_,
            DATA_COMPRESSION_OLD_, and DATA_COMPRESSION_RLE_.
    **/
     abstract int getDataCompression();                                                                                       // @ECA



    /**
    Returns the default SQL schema.

    @return     The default SQL schema, or QGPL if none was
                specified.
    **/
     abstract String getDefaultSchema ()
    throws SQLException;


    //@DELIMa
    /**
    Returns the default SQL schema.

    @param returnRawValue Indicates what to return if default SQL schema has not been set.  If true, return raw value; if false, then return QGPL rather than null.
    @return     The default SQL schema.  If returnRawValue==false and no default SQL schema was specified, then return QGPL rather than null.
    **/
     abstract String getDefaultSchema (boolean returnRawValue)
    throws SQLException;


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
                V5R2 or IBM i.  If connecting to OS/400 V5R1 or earlier,
                the value specified on this method will be ignored and the default holdability
                will be the value of #2.

    @exception  SQLException    If the connection is not open.
    @since Modification 5
    **/
     abstract public int getHoldability ()
    throws SQLException;


    //@DELIMa
    /**
     Returns the ID of the connection.
     @return The connection ID.
     **/
     abstract int getID();


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
     abstract int getInternalHoldability ();



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
    // @E2D         throws SQLException; 



    /**
    Returns the DatabaseMetaData object that describes the
    connection's tables, supported SQL grammar, stored procedures,
    capabilities and more.

    @return     The metadata object.

    @exception  SQLException    If an error occurs.
    **/
     abstract public DatabaseMetaData getMetaData ()
    throws SQLException;



    /**
    Returns the connection properties.

    @return    The connection properties.
     * @throws SQLException 
    **/
     abstract public JDProperties getProperties ()
    throws SQLException;



    // @E8A
    /**
    Returns the job identifier of the host server job corresponding to this connection.
    Every JDBC connection is associated with a host server job on the IBM i system.  The
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
     abstract public String getServerJobIdentifier();                                                                   // @E8A




     abstract int getServerFunctionalLevel();                                                                   // @EEA


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
     abstract public AS400 getSystem();                                                                   // @EHA





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
     abstract public int getTransactionIsolation ()
    throws SQLException;



     abstract JDTransactionManager getTransactionManager();                                                                           // @E4A



    // JDBC 2.0
    /**
    Returns the type map.

    <p>This driver does not support the type map.

    @return     The type map.

    @exception  SQLException    This exception is always thrown.
    **/
     abstract public Map getTypeMap ()
    throws SQLException;



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
     abstract int getUnusedId (int resultSetType) //@P0C
    throws SQLException;



    // @j31a new method -- Must the user have "for update" on their
    //       SQL statement to guarantee an updatable cursor?  The answer is
    //       no for v5r2 and v5r1 systems with a PTF.  For V5R1 systems
    //       without the PTF, v4r5, and earlier, the answer is yes.
     abstract boolean getMustSpecifyForUpdate ();





    /**
    Returns the URL for the connection's database.

    @return      The URL for the database.
    **/
     abstract String getURL ()
    throws SQLException;



    /**
    Returns the user name as currently signed on to the system.

    @return      The user name.
    **/
     abstract String getUserName ()
    throws SQLException;



     abstract int getVRM()                                            // @D0A
    throws SQLException;                                                       // @D0A



    /**
    Returns the first warning reported for the connection.
    Subsequent warnings may be chained to this warning.

    @return     The first warning or null if no warnings
                have been reported.

    @exception  SQLException    If an error occurs.
    **/
     abstract public SQLWarning getWarnings ()
    throws SQLException;



    /**
    Indicates if the specified cursor name is already used
    in the connection.

    @return     true if the cursor name is already used;
                false otherwise.
    **/
     abstract boolean isCursorNameUsed (String cursorName)
    throws SQLException;



    /**
    Indicates if the connection is closed.

    @return     true if the connection is closed; false
                otherwise.

    @exception  SQLException    If an error occurs.
    **/
     abstract public boolean isClosed ()
    throws SQLException;



    /**
    Indicates if the connection is in read-only mode.

    @return     true if the connection is in read-only mode;
                false otherwise.

    @exception  SQLException    If the connection is not open.
    **/
     abstract public boolean isReadOnly ()
    throws SQLException;

    // Called by AS400JDBCPooledConnection.
     abstract boolean isReadOnlyAccordingToProperties()
      throws SQLException;


    // @B4A
    /**
    Marks all of the cursors as closed.

    @param  isRollback True if we called this from rollback(), false if we called this from commit().
    **/
     abstract void markCursorsClosed(boolean isRollback)  //@F3C //@XAC
    throws SQLException;

    //@KBL
    /*
    If a statement associated with locators has been partially closed, finish closing the statement object.
    A statement may become partially closed if the user closed the statement and set the "hold statements" connection
    property to true when making the connection.  Additionally, the statement must have been used to access a locator.
    */
     abstract void markStatementsClosed();

    //@GKA
    // Note:  This method is used when the user supplies either the column indexes or names
    // to the execute/executeUpdate/prepareStatement method.
    /*
    * Prepares and executes the statement needed to retrieve generated keys.
    */
     abstract String makeGeneratedKeySelectStatement(String sql, int[] columnIndexes, String[] columnNames)
    throws SQLException;

    //@GKA
    // Note:  This method is used when the user supplies ResultSet.RETURN_GENERATED_KEYS
    // to the execute/executeUpdate method.
    /*
    * Prepares and executes the statement needed to retrieve generated keys
    */
     abstract String makeGeneratedKeySelectStatement(String sql)
    throws SQLException;

    /**
    Returns the native form of an SQL statement without
    executing it. The JDBC driver converts all SQL statements
    from the JDBC SQL grammar into the native DB2 for IBM i
    SQL grammar prior to executing them.

    @param  sql     The SQL statement in terms of the JDBC SQL grammar.
    @return         The translated SQL statement in the native
                    DB2 for IBM i SQL grammar.

    @exception      SQLException    If the SQL statement has a syntax error.
    **/
     abstract     public String nativeSQL (String sql)
    throws SQLException;



    /**
    Notifies the connection that a statement in its context has
    been closed.

    @param   statement   The statement.
    @param   id          The statement's id.
    **/
     abstract     void notifyClose (AS400JDBCStatement statement, int id)
    throws SQLException;


    // @A3D - Moved this logic up into AS400JDBCDriver:
    //    private void open ()
    //        throws SQLException



    /**
    Posts a warning for the connection.

    @param   sqlWarning  The warning.
    **/
     abstract void postWarning (SQLWarning sqlWarning)
    throws SQLException;



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
     abstract public CallableStatement prepareCall (String sql)
    throws SQLException;



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
     abstract public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency)
    throws SQLException;


    //@G4A JDBC 3.0
    /**
    Precompiles an SQL stored procedure call with optional input
    and output parameters and stores it in a CallableStatement
    object.  This object can be used to efficiently call the SQL
    stored procedure multiple times.

    <p>Full functionality of this method requires support in OS/400 V5R2
    or IBM i.  If connecting to OS/400 V5R1 or earlier, the value for
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
     abstract public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency,
                                          int resultSetHoldability)
    throws SQLException;




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
     abstract public PreparedStatement prepareStatement (String sql)
    throws SQLException;



    //@G4A
    //JDBC 3.0
    /**
    Precompiles an SQL statement with optional input parameters
    and stores it in a PreparedStatement object.  This object can
    be used to efficiently execute this SQL statement
    multiple times.

    <p>This method requires OS/400 V5R2 or IBM i.  If connecting to OS/400 V5R1 or earlier, an exception will be
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
     abstract public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
    throws SQLException;




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
     abstract public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency)
    throws SQLException;


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
     abstract public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency,
                                               int resultSetHoldability)
    throws SQLException;

    // @G4 new method
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported when connecting to IBM i V5R4 or earlier systems.</B>
     *
     * @param  sql     The SQL statement.
     * @param  columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows.
     * @return         The prepared statement object.
     * @exception      java.sql.SQLException - If connecting to IBM i V5R4 or earlier systems,
     *                 the connection is not open,
     *                 the maximum number of statements for this connection has been reached,
     *                 or an error occurs.
     * @since Modification 5
    **/
     abstract public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
    throws SQLException;


    // @G4 new method
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported when connecting to IBM i V5R4 or earlier systems.</B>
     *
     * @param  sql     The SQL statement.
     * @param  columnNames An array of column names indicating the columns that should be returned from the inserted row or rows.
     * @return         The prepared statement object.
     * @exception      java.sql.SQLException - If connecting to IBM i V5R4 or earlier systems,
     *                 the connection is not open,
     *                 the maximum number of statements for this connection has been reached,
     *                 or an error occurs.
     * @since Modification 5
    **/
     abstract public PreparedStatement prepareStatement (String sql, String[] columnNames)
    throws SQLException;



    //@E10a new method
     abstract void processSavepointRequest(String savepointStatement)
    throws SQLException;







    /**
    Partial closing of the connection.
    @exception SQLException If a database error occurs.
    **/
     abstract void pseudoClose() throws SQLException;


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
     abstract public void releaseSavepoint(Savepoint savepoint)
    throws SQLException;







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
     abstract public void rollback ()
    throws SQLException;

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
     abstract public void rollback(Savepoint savepoint)
    throws SQLException;






    /**
    Sends a request data stream to the system using the
    connection's id and does not expect a reply.

    @param   request     The request.

    @exception           SQLException   If an error occurs.
    **/
    //
    // See implementation notes for sendAndReceive().
    //
     abstract void send (DBBaseRequestDS request)
    throws SQLException;



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
     abstract void send (DBBaseRequestDS request, int id)
    throws SQLException;



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
     abstract void send (DBBaseRequestDS request, int id, boolean leavePending)
    throws SQLException;






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
     abstract void sendAndHold(DBBaseRequestDS request, int id)
    throws SQLException;



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
     abstract DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request)
    throws SQLException;



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
     abstract DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request, int id)
    throws SQLException;


    //@D2A
    abstract DBReplyRequestedDS sendAndMultiReceive (DBBaseRequestDS request)
    throws SQLException;

    //@DA2 - sew added new receive method.
    abstract DBReplyRequestedDS receiveMoreData()
    throws SQLException;


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
    abstract public void setAutoCommit (boolean autoCommit)
    throws SQLException;



    /**
    This method is not supported.

    @exception          SQLException    If the connection is not open.
    **/
    abstract public void setCatalog (String catalog)
    throws SQLException;

    //@cc1
    /**
     * This method sets concurrent access resolution.  This method overrides the setting of ConcurrentAccessResolution on the datasource or connection
     * URL properties.  This changes the setting for this connection only.  This method has no effect on
     * IBM i V6R1 or earlier.
     * The possible values for this property are {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED},
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME} and
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS},
     * with the property defaulting to {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET}.
     * Setting this property to default exhibits the default behavior on the servers
     * i.e., the semantic applied for read
     * transactions to avoid locks will be determined by the server.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED} specifies that driver will flow USE CURRENTLY COMMITTED
     * to server.  Whether CURRENTLY COMMITTED will actually be in effect is
     * ultimately determined by server.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME} specifies that driver will flow WAIT FOR OUTCOME
     * to server.  This will disable the CURRENTLY COMMITTED behavior at the server,
     * if enabled, and the server will wait for the commit or rollback of data in the process of
     * being updated.
     *
     * {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS} specifies that driver will flow SKIP LOCKS
     * to server.  This directs the database manager to skip records in the case of record lock conflicts.
     *
     *  @param concurrentAccessResolution The current access resolution setting.  Possible valuse:
     *  {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_NOT_SET},
     *  {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_USE_CURRENTLY_COMMITTED},
     *  {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_WAIT_FOR_OUTCOME}, or
     *  {@link com.ibm.as400.access.AS400JDBCDataSource#CONCURRENTACCESS_SKIP_LOCKS}
     * @throws SQLException 
     */
    abstract public void setConcurrentAccessResolution (int concurrentAccessResolution) throws SQLException;


    /**
    Sets the eWLM Correlator.  It is assumed a valid correlator value is used.
    If the value is null, all ARM/eWLM implementation will be turned off.
    eWLM correlators require IBM i V5R3 or later systems.  This request is ignored when running to OS/400 V5R2 or earlier systems.

    @param bytes The eWLM correlator value
     * @throws SQLException 
    **/
    abstract public void setDB2eWLMCorrelator(byte[] bytes)
    throws SQLException;


    // @B1A
    /**
    Sets whether the connection is being used for DRDA.

    @param  drda        true if the connection is being used for DRDA,
                        false otherwise.
    **/
    abstract void setDRDA (boolean drda)
    throws SQLException;


    //@G4A JDBC 3.0
    /**
    Sets the holdability of ResultSets created from this connection.

    <p>Full functionality of this method requires OS/400 V5R2
    or IBM i.  If connecting to OS/400 V5R1 or earlier, all
    cursors for the connection will be changed to the value of the variable
    <i>holdability</i>.

    @param  holdability   The cursor holdability.
                          Valid values are ResultSet.HOLD_CURSORS_OVER_COMMIT or
                          ResultSet.CLOSE_CURSORS_AT_COMMIT.

    @exception          SQLException    If the connection is not open
                                        or the value passed in is not valid.
    @since Modification 5
    **/
    abstract public void setHoldability (int holdability)
    throws SQLException;



    //@D4A
    abstract void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties,
                        AS400 as400, Properties info)
    throws SQLException;


    abstract void setProperties(JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400)
    throws SQLException;

    /* Backwards compatible method for iAccess */ 
    void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties,
        AS400 as400) throws SQLException {
      setProperties(dataSourceUrl, properties, as400, new Properties());
    }

    /* Should the warning be ignored  @Q1A*/
    abstract boolean ignoreWarning(String sqlState);
    abstract boolean ignoreWarning(SQLWarning warning);
    //@A3A - This logic formerly resided in the ctor.
    abstract void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400, boolean newServer, boolean skipSignonServer)
    throws SQLException;



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
    abstract public void setReadOnly (boolean readOnly)
    throws SQLException;




    // @E10 new method
    /**
     * Creates an unnamed savepoint in the current transaction and returns the new Savepoint object that represents it.
     * <UL>
     * <LI>Named savepoints must be unique.  A savepoint name cannot be reused until the savepoint is released, committed, or rolled back.
     * <LI>Savepoints are valid only if autocommit is off.  An exception is thrown if autocommit is enabled.
     * <LI>Savepoints are not valid across XA connections.  An exception is thrown if the connection is an XA connection.
     * <LI>Savepoints require OS/400 V5R2 or IBM i.  An exception is thrown if connecting to OS/400 V5R1 or earlier.
     * <LI>If the connection option is set to keep cursors open after a traditional rollback, cursors will remain open after a rollback to a savepoint.
     * </UL>
     *
     * @return     The new Savepoint object.
     * @exception  SQLException if a database access error occurs or this Connection object is currently in auto-commit mode.
     * @since Modification 5
    **/
    abstract public Savepoint setSavepoint()
    throws SQLException;

    // @E10 new method
    /**
     * Creates a named savepoint in the current transaction and returns the new Savepoint object that represents it.
     * <UL>
     * <LI>Named savepoints must be unique.  A savepoint name cannot be reused until the savepoint is released, committed, or rolled back.
     * <LI>Savepoints are valid only if autocommit is off.  An exception is thrown if autocommit is enabled.
     * <LI>Savepoints are not valid across XA connections.  An exception is thrown if the connection is an XA connection.
     * <LI>Savepoints require OS/400 V5R2 or IBM i.  An exception is thrown if connecting to OS/400 V5R1 or earlier.
     * <LI>If the connection option is set to keep cursors open after a traditional rollback, cursors will remain open after a rollback to a savepoint.
     * </UL>
     * @param      name A String containing the name of the savepoint
     * @return     The new Savepoint object.
     * @exception  SQLException if a database access error occurs or this Connection object is currently in auto-commit mode.
     * @since Modification 5
    **/
    abstract public Savepoint setSavepoint(String name)
    throws SQLException;

    // @E10 new method
    abstract Savepoint setSavepoint(String name, int id)
    throws SQLException;







    /**
    Sets the server attributes.

    @param      libraryList     The library list.

    @exception  SQLException    If an error occurs.
    **/
    abstract void setServerAttributes ()
    throws SQLException;



    //@A3A
    // Implementation note:  Don't use this object internally because we could be running in a proxy environment
    // The purpose of this method is to simply hold the full AS400 object so it can be retrieved from the Connection
    abstract void setSystem (AS400 as400)
    throws SQLException;



    // @D2C
    /**
    Sets the transaction isolation level.  The transaction
    isolation level cannot be changed while in the middle of
    a transaction.

    <p>JDBC and DB2 for IBM i use different terminology for transaction
    isolation levels.  The following table provides a terminology
    mapping:

    <p><table border summary="">
    <tr><th>IBM i isolation level</th><th>JDBC transaction isolation level</th></tr>
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
    abstract public void setTransactionIsolation (int level)
    throws SQLException;



    // JDBC 2.0
    /**
    Sets the type map to be used for distinct and structured
    types.

    <p>Note: Distinct types are supported by DB2 for IBM i, but
    are not externalized by the IBM Toolbox for Java JDBC driver.
    In other words, distinct types behave as if they are the underlying
    type.  Structured types are not supported by DB2 for IBM i.
    Consequently, this driver does not support the type map.

    @param typeMap  The type map.

    @exception  SQLException    This exception is always thrown.
    **/
    abstract public void setTypeMap (Map typeMap)
    throws SQLException;



    /**
    Returns the connection's catalog name.  This is the
    name of the IBM i system.

    @return     The catalog name.
    **/
    abstract public String toString ();



    /**
    Indicates if the connection is using extended formats.

    @return     true if the connection is using extended formats, false
                otherwise.
    **/
    abstract boolean useExtendedFormats ()
    throws SQLException;


   



    //@PDA jdbc40
  //JDBC40DOC    /**
  //JDBC40DOC     * Returns true if the connection has not been closed and is still valid.
  //JDBC40DOC     * The driver shall submit a query on the connection or use some other
  //JDBC40DOC     * mechanism that positively verifies the connection is still valid when
  //JDBC40DOC     * this method is called.
  //JDBC40DOC     * <p>
  //JDBC40DOC     * The query submitted by the driver to validate the connection shall be
  //JDBC40DOC     * executed in the context of the current transaction.
  //JDBC40DOC     *
  //JDBC40DOC     * @param timeout -     The time in seconds to wait for the database operation
  //JDBC40DOC     *                      used to validate the connection to complete.  If
  //JDBC40DOC     *                      the timeout period expires before the operation
  //JDBC40DOC     *                      completes, this method returns false.  A value of
  //JDBC40DOC     *                      0 indicates a timeout is not applied to the
  //JDBC40DOC     *                      database operation.
  //JDBC40DOC     * <p>
  //JDBC40DOC     * @return true if the connection is valid, false otherwise
  //JDBC40DOC     * @exception SQLException if a database access error occurs.
  //JDBC40DOC */
    /* ifdef JDBC40
    abstract public boolean isValid(int timeout) throws SQLException;
    endif */



    //@PDA 550 client info
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
//JDBC40DOC      * @throws  SQLClientInfoException if the database returns an error while
//JDBC40DOC      *          setting the client info value on the database server.
     * <p>
     * @throws SQLException 
     */
    abstract public void setClientInfo(String name, String value)
/* ifdef JDBC40
    throws SQLClientInfoException;
endif */
    /* ifndef JDBC40 */
    throws SQLException;
    /* endif  */

    //@PDA 550 client info
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
//JDBC40DOC      * @throws SQLClientInfoException
//JDBC40DOC      *             if the database returns an error while setting the
//JDBC40DOC      *             clientInfo values on the database
     *             <p>
     * @throws SQLException 
     */
    abstract public void setClientInfo(Properties properties)
/* ifdef JDBC40
    throws SQLClientInfoException;
 endif */
    /* ifndef JDBC40 */
    throws SQLException;
    /* endif */

    //@PDA 550 client info
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
     * @throws SQLException     if the database returns an error when
     *                          fetching the client info value from the database.
     * <p>
     * see java.sql.DatabaseMetaData#getClientInfoProperties
     */
    abstract public String getClientInfo(String name) throws SQLException;

    //@PDA 550 client info
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
     * @throws  SQLException if the database returns an error when
     *          fetching the client info values from the database
     */
    abstract public Properties getClientInfo() throws SQLException;





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
    abstract public Clob createClob() throws SQLException;

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
    abstract public Blob createBlob() throws SQLException;

    //@PDA jdbc40
  //JDBC40DOC    /**
  //JDBC40DOC     * Constructs an object that implements the <code>NClob</code> interface. The object
  //JDBC40DOC     * returned initially contains no data.  The <code>setAsciiStream</code>,
  //JDBC40DOC     * <code>setCharacterStream</code> and <code>setString</code> methods of the <code>NClob</code> interface may
  //JDBC40DOC     * be used to add data to the <code>NClob</code>.
  //JDBC40DOC     * @return An object that implements the <code>NClob</code> interface
  //JDBC40DOC     * @throws SQLException if an object that implements the
  //JDBC40DOC     * <code>NClob</code> interface can not be constructed.
  //JDBC40DOC     *
  //JDBC40DOC     */
     /*ifdef JDBC40
    abstract public NClob createNClob() throws SQLException;
endif */

    //@PDA jdbc40
  //JDBC40DOC    /**
  //JDBC40DOC     * Constructs an object that implements the <code>SQLXML</code> interface. The object
  //JDBC40DOC     * returned initially contains no data. The <code>createXMLStreamWriter</code> object and
  //JDBC40DOC     * <code>setString</code> method of the <code>SQLXML</code> interface may be used to add data to the <code>SQLXML</code>
  //JDBC40DOC     * object.
  //JDBC40DOC     * @return An object that implements the <code>SQLXML</code> interface
  //JDBC40DOC     * @throws SQLException if an object that implements the <code>SQLXML</code> interface can not
  //JDBC40DOC     * be constructed
  //JDBC40DOC     */
     /*ifdef JDBC40
    abstract public SQLXML createSQLXML() throws SQLException; 
    endif */

    //@PDA //@array
    /**
     * Factory method for creating Array objects.
     *
     * @param typeName the SQL name of the type the elements of the array map to. The typeName is a
     * database-specific name which may be the name of a built-in type, a user-defined type or a standard  SQL type supported by this database. This
     *  is the value returned by <code>Array.getBaseTypeName</code>
     *  For Toolbox, the typeName will correspond to a typename in java.sql.Types.
     *
     * @param elements the elements that populate the returned object
     * @return an Array object whose elements map to the specified SQL type
     * @throws SQLException if a database error occurs, the typeName is null or this method is called on a closed connection
     */
    abstract public Array createArrayOf(String typeName, Object[] elements) throws SQLException;

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
     */
    abstract public Struct createStruct(String typeName, Object[] attributes) throws SQLException;


    //@2KRA
    /**
     * Starts or stops the Database Host Server trace for this connection.
     * Note:  This method is only supported when running to IBM i V5R3 or later
     * and is ignored if you specified to turn on database host server tracing
     * using the 'server trace' connection property.
     * @param trace true to start database host server tracing, false to end it.
     */
    abstract public void setDBHostServerTrace(boolean trace);


    //@A2A
    abstract public boolean doUpdateDeleteBlocking();

	// @A6A
	abstract public int getMaximumBlockedInputRows();

  //JDBC40DOC    /**
  //JDBC40DOC     * Terminates an open connection. Calling abort results in:
  //JDBC40DOC     * <ul>
  //JDBC40DOC     * <li> The connection marked as closed
  //JDBC40DOC     * <li>   Closes any physical connection to the database
  //JDBC40DOC     * <li>   Releases resources used by the connection
  //JDBC40DOC     * <li>   Insures that any thread that is currently accessing the connection will
  //JDBC40DOC     *      	either progress to completion or throw an SQLException.
  //JDBC40DOC     * </ul>
  //JDBC40DOC     * <p>
  //JDBC40DOC     * Calling abort marks the connection closed and releases any resources.
  //JDBC40DOC     * Calling abort on a closed connection is a no-op.
  //JDBC40DOC     * <p>It is possible that the aborting and releasing of the resources that are
  //JDBC40DOC     * 	held by the connection can take an extended period of time.
  //JDBC40DOC     * 	When the abort method returns, the connection will have been marked as closed
  //JDBC40DOC     * 	and the Executor that was passed as a parameter to abort may still be executing
  //JDBC40DOC     * 	tasks to release resources.
  //JDBC40DOC     * <p>
  //JDBC40DOC     * This method checks to see that there is an SQLPermission object before
  //JDBC40DOC     * 	allowing the method to proceed. If a SecurityManager exists and its
  //JDBC40DOC     * 	checkPermission method denies calling abort, this method throws a
  //JDBC40DOC     * 	java.lang.SecurityException.
  //JDBC40DOC    	* @param executor The Executor implementation which will be used by abort.
  //JDBC40DOC      * @throws  SQLException - if a database access error occurs or the executor is null
  //JDBC40DOC      * @throws  SecurityException - if a security manager exists and its checkPermission
  //JDBC40DOC      *	method denies calling abort
  //JDBC40DOC     */
/* ifdef JDBC40
  abstract public void abort(Executor executor) throws SQLException ;
    
endif */



  /**
   * Retrieves this <code>Connection</code> object's current schema name.
   * @return  the current schema name or null if there is none
   * @throws  SQLException if a database access error occurs or this method is called on a closed connection
   */
	abstract public String getSchema() throws SQLException;

   /**
    * Sets the maximum period a Connection or objects created from the Connection will wait for the database to
    * reply to any one request. If any request remains unanswered, the waiting method will return with a
    * SQLException, and the Connection or objects created from the Connection will be marked as closed.
    * Any subsequent use of the objects, with the exception of the close, isClosed or Connection.isValid methods,
    * will result in a SQLException.
    *
    *<p>In the JTOpen JDBC driver, this is implemented by setting the SoTimeout of the underlying socket.
    *<p>Currently, setting the network timeout is only supported when the "thread used" property is false.
    *<p>When the driver determines that the setNetworkTimeout timeout value has expired, the JDBC driver marks
    * the connection closed and releases any resources held by the connection.
    *<p>This method checks to see that there is an SQLPermission object before allowing the method to proceed.
    * If a SecurityManager exists and its checkPermission method denies calling setNetworkTimeout, this method
    * throws a java.lang.SecurityException.
    *@param timeout - The time in milliseconds to wait for the database operation to complete. If the
    * JDBC driver does not support milliseconds, the JDBC driver will round the value up to the nearest second.
    * If the timeout period expires before the operation completes, a SQLException will be thrown. A value of
    * 0 indicates that there is not timeout for database operations.
   * @throws SQLException
    * @throws  SQLException - if a database access error occurs, this method is called on a closed connection,
    *  or the value specified for seconds is less than 0.
    * @throws  SecurityException - if a security manager exists and its checkPermission method denies calling
    *  setNetworkTimeout.
    * @see SecurityManager#checkPermission(java.security.Permission)
    * @see Statement#setQueryTimeout(int)
    * @see #getNetworkTimeout()
//JDBC40DOC     * @see #abort(java.util.concurrent.Executor)
//JDBC40DOC     * @see Executor
    */

	abstract public void setNetworkTimeout(int timeout) throws SQLException;

   /**
    * Retrieves the number of milliseconds the driver will wait for a database request to complete. If the limit is exceeded, a SQLException is thrown.
    * @return The current timeout limit in milliseconds; zero means there is no limit
    * @throws SQLException - if a database access error occurs or this method is called on a closed Connection
    * @since JTOpen 7.X
//JDBC40DOC     * @see #setNetworkTimeout(java.util.concurrent.Executor, int)
    */
	abstract public int getNetworkTimeout() throws SQLException;

//JDBC40DOC    /**
//JDBC40DOC     * Sets the maximum period a Connection or objects created from the Connection will wait for the database to
//JDBC40DOC     * reply to any one request. If any request remains unanswered, the waiting method will return with a
//JDBC40DOC     * SQLException, and the Connection or objects created from the Connection will be marked as closed.
//JDBC40DOC     * Any subsequent use of the objects, with the exception of the close, isClosed or Connection.isValid methods,
//JDBC40DOC     * will result in a SQLException.
//JDBC40DOC     *<p>Note: This method is intended to address a rare but serious condition where network partitions can
//JDBC40DOC     * cause threads issuing JDBC calls to hang uninterruptedly in socket reads, until the OS TCP-TIMEOUT
//JDBC40DOC     * (typically 10 minutes). This method is related to the abort() method which provides an administrator
//JDBC40DOC     * thread a means to free any such threads in cases where the JDBC connection is accessible to the
//JDBC40DOC     * administrator thread. The setNetworkTimeout method will cover cases where there is no administrator
//JDBC40DOC     * thread, or it has no access to the connection. This method is severe in it's effects, and should be
//JDBC40DOC     * given a high enough value so it is never triggered before any more normal timeouts,
//JDBC40DOC     * such as transaction timeouts.
//JDBC40DOC     * <p>JDBC driver implementations may also choose to support the setNetworkTimeout method to impose
//JDBC40DOC     * a limit on database response time, in environments where no network is present.
//JDBC40DOC     *<p>Drivers may internally implement some or all of their API calls with multiple internal driver-database
//JDBC40DOC     * transmissions, and it is left to the driver implementation to determine whether the limit will be
//JDBC40DOC     *  applied always to the response to the API call, or to any single request made during the API call.
//JDBC40DOC     *<p>This method can be invoked more than once, such as to set a limit for an area of JDBC code,
//JDBC40DOC     * and to reset to the default on exit from this area. Invocation of this method has no impact on
//JDBC40DOC     * already outstanding requests.
//JDBC40DOC     *<p>The Statement.setQueryTimeout() timeout value is independent of the timeout value specified in
//JDBC40DOC     * setNetworkTimeout. If the query timeout expires before the network timeout then the statement execution
//JDBC40DOC     * will be canceled. If the network is still active the result will be that both the statement and connection
//JDBC40DOC     * are still usable. However if the network timeout expires before the query timeout or if the statement timeout
//JDBC40DOC     * fails due to network problems, the connection will be marked as closed, any resources held by the connection
//JDBC40DOC     * will be released and both the connection and statement will be unusable.
//JDBC40DOC     *<p>When the driver determines that the setNetworkTimeout timeout value has expired, the JDBC driver marks
//JDBC40DOC     * the connection closed and releases any resources held by the connection.
//JDBC40DOC     *<p>This method checks to see that there is an SQLPermission object before allowing the method to proceed.
//JDBC40DOC     * If a SecurityManager exists and its checkPermission method denies calling setNetworkTimeout, this method
//JDBC40DOC     * throws a java.lang.SecurityException.
//JDBC40DOC     *@param executor - The Executor implementation which will be used by setNetworkTimeout.
//JDBC40DOC     *@param milliseconds - The time in milliseconds to wait for the database operation to complete. If the
//JDBC40DOC     * JDBC driver does not support milliseconds, the JDBC driver will round the value up to the nearest second.
//JDBC40DOC     * If the timeout period expires before the operation completes, a SQLException will be thrown. A value of
//JDBC40DOC     * 0 indicates that there is not timeout for database operations.
//JDBC40DOC     * @throws  SQLException - if a database access error occurs, this method is called on a closed connection,
//JDBC40DOC     *  the executor is null, or the value specified for seconds is less than 0.
//JDBC40DOC     * @throws  SecurityException - if a security manager exists and its checkPermission method denies calling
//JDBC40DOC     *  setNetworkTimeout.
//JDBC40DOC     * @see  SecurityManager#checkPermission(java.security.Permission)
//JDBC40DOC     * @see  Statement#setQueryTimeout(int)
//JDBC40DOC     * @see  #getNetworkTimeout()
//JDBC40DOC     * @see  #abort(java.util.concurrent.Executor)
//JDBC40DOC     * @see  Executor
//JDBC40DOC     **/
/* ifdef JDBC40
  abstract public void setNetworkTimeout(Executor executor, int milliseconds)
      throws SQLException ;
      
endif */

  /**
   *Sets the given schema name to access.
   *<p>
   * Calling setSchema has no effect on previously created or prepared Statement objects.
   * For the toolbox driver, the DBMS prepare operation takes place immediately when the
   * Connection method prepareStatement or prepareCall is invoked.
   * For maximum portability, setSchema should be called before a Statement is created or prepared.
   *
   * @param schema The name of the schema to use for the connection
   * @throws SQLException If a database access error occurs or this method is
   * called on a closed connection
   *
   */
	abstract public void setSchema(String schema) throws SQLException;

  /**
   * Is SQL cancel used for the query timeout mechanism
   * @return true if cancel will be used as the query timeout mechanism
   */
	abstract boolean isQueryTimeoutMechanismCancel();


  /**
   * Setup the variableFieldCompression flags @K3A
   */
	abstract void setupVariableFieldCompression();

	abstract boolean useVariableFieldCompression();

	abstract boolean useVariableFieldInsertCompression();

//@L9A
	abstract public void setDisableCompression(boolean disableCompression_);



	abstract public void dumpStatementCreationLocation();

  
  /**
   * Tests if a DataTruncation occurred on the write of a piece of data and
   * throws a DataTruncation exception if so. The data truncation flag is also
   * taken into consideration for string data. The rules are: 1) If updating
   * database with numeric data and data truncated, throw exception 2) If
   * numeric data is part of a query and data truncated, post warning 3) If
   * string data and suppress truncation, return 4) If updating database with
   * string data and check truncation and data truncated, throw exception 5) If
   * string data is part of a query and check truncation and data truncated,
   * post warning
   * 
   * @param index
   *          The index (1-based).
   * @param data
   *          The data that was written or null for SQL NULL.
   **/
	abstract void testDataTruncation(AS400JDBCStatement statementWarningObject, 
        AS400JDBCResultSet resultSetWarningObject, 
        int parameterIndex, boolean isParameter, SQLData data, JDSQLStatement sqlStatement)
      throws SQLException;

	abstract ConvTable  getConverter() ;


	abstract void setLastServerSQLState(String lastSqlState);


	abstract String getLastServerSQLState();


	abstract ConvTable getPackageCCSID_Converter();


	abstract boolean getReadOnly();


	abstract boolean getCheckStatementHoldability();


  abstract  int getNewAutoCommitSupport() ;


  public void setInFinalizer(boolean setting) {
    inFinalizer_ = setting; 
  }
  
}
