///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionHandle.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.sql.ConnectionEvent;
import java.sql.CallableStatement;
import java.sql.Connection;        //@A5A
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;         //@A6A
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties; //@pda client info

/**
*  The AS400JDBCConnectionHandle class represents an AS400JDBCConnection object
*  that can be used in a AS400JDBCPooledConnection.  
*
*  <p>Creating a handle to a connection object allows the connection to be reused.  
*  The connection handle acts like a normal Connection object and should be closed 
*  after an application is done using it so it can be reused.
*
*  <p>
*  The following example obtains a connection handle to a pooled connection.
*  <pre><blockquote>
*  // Create a data source for making the connection.
*  AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource("CheeseDataBase");
*  datasource.setServerName("myAS400");
*  datasource.setUser("Mickey Mouse");
*  datasource.setPassword("IAMNORAT");
*
*  // Get a PooledConnection and get the connection handle to the database.
*  AS400JDBCPooledConnection pooledConnection = datasource.getPooledConnection();
*  Connection connection = pooledConnection.getConnection();
*
*  ... work with the handle as if it is a normal connection.
*
*  // Close the connection handle to it make available again for reuse.
*  connection.close();
*  </blockquote></pre>
*
**/
public class AS400JDBCConnectionHandle 
implements Connection //@A5A
//@A5D extends AS400JDBCConnection
{
  private AS400JDBCPooledConnection pooledConnection_ = null;
  private AS400JDBCConnection connection_ = null;

  /**
  *  Constructs an AS400JDBCConnectionHandle object.
  *  @param pooledConnection The pooled connection from which the handle originated.
  *  @param connection The physical connection that the handle represents.
  **/
  AS400JDBCConnectionHandle(AS400JDBCPooledConnection pooledConnection, AS400JDBCConnection connection)
  {
    if (pooledConnection == null)
      throw new NullPointerException("pooledConnection");
    pooledConnection_ = pooledConnection;

    if (connection == null)
      throw new NullPointerException("connection");
    connection_ = connection;
  }

  //@pda handle
  /**
   *  Invalidates the connection.
   *  A AS400JDBCPooledConnection can get expired and moved back to available queue.  So this
   *  handle class needs a way for AS400JDBCPooledConnection to notify it of this change in state. 
   *  This way, when finalize() is called by GC we will not try to double-close the connection.
   *  Without this method, it is possible for two handles to have references to the same pooledConnection.
   **/
   void invalidate()
   {
      connection_ = null;
      pooledConnection_ = null;
   }
   
  /**
  *  Checks that the specified SQL statement can be executed.
  *  This decision is based on the access specified by the caller
  *  and the read only mode.
  *
  *  @param sqlStatement The SQL statement.
  *  @exception SQLException If the statement cannot be executed.
  **/
  void checkAccess (JDSQLStatement sqlStatement) throws SQLException
  {
    validateConnection();
    try {
      connection_.checkAccess(sqlStatement);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }



  /**
  *  Checks that the connection is open.  Public methods
  *  that require an open connection should call this first.
  *
  *  @exception  SQLException    If the connection is not open.
  **/
  void checkOpen() throws SQLException
  {
    validateConnection();
    try {
      connection_.checkOpen();
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }



  /**
  *  Clears all warnings that have been reported for the connection.
  *  After this call, getWarnings() returns null until a new warning
  *  is reported for the connection.
  *
  *  @exception SQLException If an error occurs.
  **/
  public void clearWarnings() throws SQLException
  {
    validateConnection();
    connection_.clearWarnings();
  }



  /**
  *  Closes the handle to the connection.  This does not close the
  *  underlying physical connection to the database.  The handle is
  *  set to an unuseable state.
  * 
  *  @exception SQLException If an error occurs.
  **/
  public synchronized void close() throws SQLException
  {
    if (connection_ == null) return;

    try {
      // Rollback and close the open statements.
      // Note: Leave the physical connection open, so it can get re-used.
      connection_.pseudoClose();
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
    finally {
      // notify the pooled connection.
      pooledConnection_.fireConnectionCloseEvent(new ConnectionEvent(pooledConnection_));

      connection_ = null;
      pooledConnection_ = null;
    }
  }

  /**
  *  Commits all changes made since the previous commit or
  *  rollback and releases any database locks currently held by
  *  the connection.  This has no effect when the connection
  *  is in auto-commit mode.
  *
  *  @exception SQLException     If the connection is not open
  *                              or an error occurs.
  **/
  public void commit() throws SQLException
  {
    validateConnection();
    try {
      connection_.commit();
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Creates a Statement object for executing SQL statements without
  *  parameters.  If the same SQL statement is executed many times, it
  *  is more efficient to use prepareStatement().
  *
  *  <p>Result sets created using the statement will be type
  *  ResultSet.TYPE_FORWARD_ONLY and concurrency
  *  ResultSet.CONCUR_READ_ONLY.
  *
  *  @return     The statement object.
  *
  *  @exception SQLException If the connection is not open, the maximum number
  * 				  of statements for this connection has been reached, or an error occured.
  **/
  public Statement createStatement() throws SQLException
  {
    validateConnection();
    return connection_.createStatement();
  }


  /**
  *  Creates a Statement object for executing SQL statements without
  *  parameters.  If the same SQL statement is executed many times, it
  *  is more efficient to use prepareStatement().
  *
  *  @param resultSetType            The result set type.  Valid values are:
  *                                  <ul>
  *                                    <li>ResultSet.TYPE_FORWARD_ONLY
  *                                    <li>ResultSet.TYPE_SCROLL_INSENSITIVE
  *                                    <li>ResultSet.TYPE_SCROLL_SENSITIVE
  *                                  </ul>
  *  @param resultSetConcurrency     The result set concurrency.  Valid values are:
  *                                  <ul>
  *                                    <li>ResultSet.CONCUR_READ_ONLY
  *                                    <li>ResultSet.CONCUR_UPDATABLE
  *                                  </ul>
  *  @return                         The statement object.
  *
  *  @exception      SQLException    If the connection is not open, the maximum number of statements
  *            		  	 	           for this connection has been reached, the result type or currency
  *                                  is not supported, or an error occured.
  **/
  public Statement createStatement (int resultSetType, int resultSetConcurrency) throws SQLException
  {
    validateConnection();
    return connection_.createStatement(resultSetType, resultSetConcurrency);
  }


    //@A6A
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
    public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency,
                                      int resultSetHoldability)
    throws SQLException
    {
        validateConnection();
        return connection_.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }


  /**
  *  Closes the connection if not explicitly closed by the caller.
  *
  *  @exception   Throwable      If an error occurs.
  **/
  protected void finalize ()
  throws Throwable
  {
        //@A5D Removed validateConnection() because it was throwing SQL08003 exceptions if 
        //@A5D connection_ was null by the time the garbage collector ran.  From a finalizer, 
        //@A5D we don't want to be throwing exceptions.
        //@A5D validateConnection();
        //@A5D connection_.finalize();
        try
        {                                 //@A5A                                   //@A5A
            close();                        //@A5A
        }                                   //@A5A
        catch (SQLException e)              //@A5A
        {                                   //@A5A
            if (JDTrace.isTraceOn())        //@A5A
               JDTrace.logInformation (this, "Finalize on a connection handle threw exception: " + e.getMessage()); //@A5A
        }                                   //@A5A
    }


  //@CPMa
  /**
  * If the exception is a fatal connection error, fires a connectionErrorOccurred event.
  * We're looking for any kind of error that would indicate that the connection
  * should not be re-used after it's returned to the connection pool.
  **/
  private final void fireEventIfErrorFatal(SQLException e)
  {
    String sqlState = e.getSQLState();
    if (sqlState.equals(JDError.EXC_ACCESS_MISMATCH) ||
        sqlState.equals(JDError.EXC_CONNECTION_NONE) ||
        sqlState.equals(JDError.EXC_CONNECTION_REJECTED) ||
        sqlState.equals(JDError.EXC_CONNECTION_UNABLE) ||
        sqlState.equals(JDError.EXC_COMMUNICATION_LINK_FAILURE) ||
        sqlState.equals(JDError.EXC_INTERNAL) ||
        sqlState.equals(JDError.EXC_SERVER_ERROR) ||
        sqlState.equals(JDError.EXC_RDB_DOES_NOT_EXIST))
    {
      pooledConnection_.fatalConnectionErrorOccurred_ = true;
      pooledConnection_.fireConnectionErrorEvent(new ConnectionEvent(pooledConnection_, e));
    }
  }

  /**
  *  Returns the AS400 object for this connection.
  *
  *  @return     The AS400 object.
  **/
  AS400Impl getAS400 ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.getAS400();
  }

  /**
  *  Returns the auto-commit state.
  *
  *  @return     true if the connection is in auto-commit mode; false otherwise.
  *  @exception  SQLException    If the connection is not open.
  **/
  public boolean getAutoCommit() throws SQLException
  {
    validateConnection();
    return connection_.getAutoCommit();
  }

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
  public int getConcurrentAccessResolution ()  throws SQLException
  {
      validateConnection();
      return connection_.getConcurrentAccessResolution();
  }
  
  /**
  *  Returns the catalog name.
  *
  *  @return     The catalog name.
  *  @exception  SQLException    If the connection is not open.
  **/
  public String getCatalog ()
  throws SQLException
  {
    validateConnection();
    return connection_.getCatalog();
  }



  /**
  *  Returns the converter for this connection.
  *
  *  @return     The converter.
  **/
  ConvTable getConverter () //@P0C
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.converter_; //@P0C
  }

  /**
  *  Returns the converter for the specified CCSID, unless
  *  it is 0 or -1 (i.e. probably set for a non-text field), in
  *  which case it returns the converter for this connection.
  *  This is useful for code that handles all types of fields
  *  in a generic manner.
  *
  *  @param      ccsid       The CCSID.
  *  @return     The converter.
  *  @exception  SQLException    If the CCSID is not valid.
  **/
  ConvTable getConverter (int ccsid) //@P0C
  throws SQLException
  {
    validateConnection();
    return connection_.getConverter(ccsid);
  }



  /**
  *  Returns the default SQL schema.
  *
  *  @return     The default SQL schema, or QGPL if none was specified.
  **/
  String getDefaultSchema ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.getDefaultSchema();
  }

  // @A1D /**
  // @A1D *  Returns the graphic converter for this connection.
  // @A1D *
  // @A1D *  @return     The graphic converter.
  // @A1D *  @exception  SQLException    If no graphic converter was loaded.
  // @A1D **/
  // @A1D ConverterImplRemote getGraphicConverter () throws SQLException
  // @A1D {
  // @A1D    validateConnection();
  // @A1D    return connection_.getGraphicConverter();
  // @A1D }


    //@A6A
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
    public int getHoldability ()
    throws SQLException
    {
        validateConnection();
        return connection_.getHoldability();
    }


  //@A4A
  /**
  *  Returns the DatabaseMetaData object that describes the
  *  connection's tables, supported SQL grammar, stored procedures,
  *  capabilities and more.
  *
  *  @return     The metadata object.
  *
  * @exception  SQLException    If an error occurs.
  **/
  public DatabaseMetaData getMetaData ()
  throws SQLException
  {
    // We allow a user to get this object even if the
    // connection is closed.
    //@pdc above comment not true anymore
    validateConnection();  //@pda since adding invalidate(), connection_ can be null
    return connection_.getMetaData();
  }


  /**
  *  Returns the connection properties.
  *
  *  @return    The connection properties.
  **/
  JDProperties getProperties ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.getProperties();
  }

  /**
  *  Returns the job identifier of the host server job corresponding to this connection.
  *  Every JDBC connection is associated with a host server job on the system.  The
  *  format is:
  *  <ul>
  *    <li>10 character job name
  *    <li>10 character user name
  *    <li>6 character job number
  *  </ul>
  *  
  *  <p>Note: Since this method is not defined in the JDBC Connection interface,
  *  you typically need to cast a Connection object returned from PooledConnection.getConnection()
  *  to an AS400JDBCConnectionHandle in order to call this method:
  *  <blockquote><pre>
  *  String serverJobIdentifier = ((AS400JDBCConnectionHandle)connection).getServerJobIdentifier();
  *  </pre></blockquote>
  *  
  *  @return The server job identifier, or null if not known.
  *  @exception  SQLException  If the connection is not open.
  **/
  public String getServerJobIdentifier() throws SQLException
  {
      validateConnection();
      return connection_.getServerJobIdentifier();
  }
  
  //@pda
  /**
  Returns the system object which is managing the connection to the system.
  <p>Warning: This method should be used with extreme caution.  This bypasses
  the normal connection pool's connection reclaiming mechanisms.  
  <p>Note: Since this method is not defined in the JDBC Connection interface,
  you typically need to cast a Connection object to AS400JDBCConnectionHandle in order
  to call this method:
  <blockquote><pre>
  AS400 system = ((AS400JDBCConnectionHandle)connection).getSystem();
  </pre></blockquote>
  
  @return The system.
  **/
  public AS400 getSystem()                                             
  {                                                                    
      return connection_.getSystem();                                    
  }                                                                   


  /**
  *  Returns the transaction isolation level.
  *
  *  @return     The transaction isolation level.  Possible
  *              values are:
  *              <ul>
  *              <li>TRANSACTION_NONE
  *              <li>TRANSACTION_READ_UNCOMMITTED
  *              <li>TRANSACTION_READ_COMMITTED
  *      			<li>TRANSACTION_REPEATABLE_READ
  *              </ul>
  *
  *  @exception  SQLException    If the connection is not open.
  **/
  public int getTransactionIsolation() throws SQLException
  {
    validateConnection();
    return connection_.getTransactionIsolation();
  }

  /**
  *  Returns the type map.
  *
  *  <p>This driver does not support the type map.
  *
  *  @return     The type map.
  *  @exception  SQLException    This exception is always thrown. 
  **/
  public Map getTypeMap() throws SQLException
  {
    validateConnection();
    return connection_.getTypeMap();
  }


  /**
  *  Returns the URL for the connection's database.
  *
  *  @return      The URL for the database.
  **/
  String getURL ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.getURL();
  }

  /**
  *  Returns the user name as currently signed on to the system.
  *
  *  @return      The user name.
  **/
  String getUserName ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.getUserName();
  }


  /**
  *  Returns the VRM.
  **/
  int getVRM()                   
  throws SQLException                                      // @A3A
  {                              
    validateConnection();
    return connection_.getVRM();
  }                          

  /**
  *  Returns the first warning reported for the connection.
  *  Subsequent warnings may be chained to this warning.
  *
  *  @return     The first warning or null if no warnings
  *              have been reported.
  *
  *  @exception  SQLException    If an error occurs.
  **/
  public SQLWarning getWarnings() throws SQLException
  {
    validateConnection();
    return connection_.getWarnings();
  }

  /**
  *  Indicates if the specified cursor name is already used
  *  in the connection.
  *
  *  @return     true if the cursor name is already used; false otherwise.
  **/
  boolean isCursorNameUsed (String cursorName)
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.isCursorNameUsed(cursorName);
  }

  /**
  *  Indicates if the connection is closed.
  *
  *  @return     true if the connection is closed; false otherwise.
  *  @exception  SQLException    If an error occurs.
  **/
  public boolean isClosed() throws SQLException
  {
    if (connection_ == null)
      return true;
    else
      return false;
  }



  /**
  *  Indicates if the connection is in read-only mode.
  *
  *  @return     true if the connection is in read-only mode; false otherwise.
  *  @exception  SQLException    If the connection is not open.
  **/
  public boolean isReadOnly() throws SQLException
  {
    validateConnection();
    return connection_.isReadOnly();
  }

  /**
  *  Returns the native form of an SQL statement without
  *  executing it. The JDBC driver converts all SQL statements
  *  from the JDBC SQL grammar into the native DB2 for IBM i
  *  SQL grammar prior to executing them.
  *
  *  @param  sql     The SQL statement in terms of the JDBC SQL grammar.
  *  @return         The translated SQL statement in the native
  *                  DB2 for IBM i SQL grammar.
  *
  *  @exception      SQLException    If the SQL statement has a syntax error.
  **/
  public String nativeSQL (String sql) throws SQLException
  {
    validateConnection();
    return connection_.nativeSQL(sql);
  }

  /**
  *  Notifies the connection that a statement in its context has been closed.
  *
  *  @param   statement   The statement.
  *  @param   id          The statement's id.
  **/
  void notifyClose (AS400JDBCStatement statement, int id)
  throws SQLException                                      // @A3A
  {
    validateConnection();
    connection_.notifyClose(statement, id);
  }

  /**
  *  Posts a warning for the connection.
  *
  *  @param   sqlWarning  The warning.
  **/
  void postWarning (SQLWarning sqlWarning)
  throws SQLException                                      // @A3A
  {
    validateConnection();
    connection_.postWarning(sqlWarning);
  }

  /**
  *  Precompiles an SQL stored procedure call with optional input
  *  and output parameters and stores it in a CallableStatement
  *  object.  This object can be used to efficiently call the SQL
  *  stored procedure multiple times.
  *
  *  <p>Result sets created using the statement will be type
  *  ResultSet.TYPE_FORWARD_ONLY and concurrency
  *  ResultSet.CONCUR_READ_ONLY.
  *
  *  @param  sql     The SQL stored procedure call.
  *  @return         The callable statement object.
  *
  *  @exception      SQLException    If the connection is not open,
  *  					  the maximum number of statements
  *      				  for this connection has been reached,  or an
  *                                error occurs.
  **/
  public CallableStatement prepareCall (String sql) throws SQLException
  {
    validateConnection();
    return connection_.prepareCall(sql);
  }

  /**
  *  Precompiles an SQL stored procedure call with optional input
  *  and output parameters and stores it in a CallableStatement
  *  object.  This object can be used to efficiently call the SQL
  *  stored procedure multiple times.
  *
  *  @param sql                      The SQL statement.
  *  @param resultSetType            The result set type.  Valid values are:
  *                                  <ul>
  *                                    <li>ResultSet.TYPE_FORWARD_ONLY
  *                                    <li>ResultSet.TYPE_SCROLL_INSENSITIVE
  *                                    <li>ResultSet.TYPE_SCROLL_SENSITIVE
  *                                  </ul>
  *  @param resultSetConcurrency     The result set concurrency.  Valid values are:
  *                                  <ul>
  *                                    <li>ResultSet.CONCUR_READ_ONLY
  *                                    <li>ResultSet.CONCUR_UPDATABLE
  *                                  </ul>
  *  @return                         The prepared statement object.
  * 
  *  @exception      SQLException    If the connection is not open, the maximum number of statements
  *	    	            		        for this connection has been reached, the result type or currency 
  *                                  is not valid, or an error occurs.
  **/
  public CallableStatement prepareCall (String sql, int resultSetType, int resultSetConcurrency)
  throws SQLException
  {
    validateConnection();
    return connection_.prepareCall(sql, resultSetType, resultSetConcurrency);
  }


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
    public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency,
                                          int resultSetHoldability)
    throws SQLException
    {
        validateConnection();
        return connection_.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }


  /**
  *  Precompiles an SQL statement with optional input parameters
  *  and stores it in a PreparedStatement object.  This object can
  *  be used to efficiently execute this SQL statement multiple times.
  *
  *  <p>Result sets created using the statement will be type
  *  ResultSet.TYPE_FORWARD_ONLY and concurrency
  *  ResultSet.CONCUR_READ_ONLY.
  *
  *  @param  sql     The SQL statement.
  *  @return         The prepared statement object.
  * 
  *  @exception      SQLException    If the connection is not open,
  *					  the maximum number of statements
  *   				    	  for this connection has been reached,  or an
  *                                error occurs.
  **/
  public PreparedStatement prepareStatement (String sql) throws SQLException
  {
    validateConnection();
    return connection_.prepareStatement(sql);
  }



    //@A6A
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
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        validateConnection();
        return connection_.prepareStatement(sql, autoGeneratedKeys);
    }


    // @A6A
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported.  An SQLException is always thrown. </B>
     *
     * @param  sql     The SQL statement.                                  
     * @param  columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows.
     * @return         An SQLException is always thrown. This method is not supported.
     * @exception      java.sql.SQLException - Always thrown because the Toolbox JDBC driver does does not support this method.
     * @since Modification 5
    **/
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }


  /**
  *  Precompiles an SQL statement with optional input parameters
  *  and stores it in a PreparedStatement object.  This object can
  *  be used to efficiently execute this SQL statement
  *  multiple times.
  *
  *  @param sql                      The SQL statement.
  *  @param resultSetType            The result set type.  Valid values are:
  *                                <ul>
  *                                  <li>ResultSet.TYPE_FORWARD_ONLY
  *                                  <li>ResultSet.TYPE_SCROLL_INSENSITIVE
  *                                  <li>ResultSet.TYPE_SCROLL_SENSITIVE
  *                                </ul>
  *  @param resultSetConcurrency     The result set concurrency.  Valid values are:
  *                                <ul>
  *                                  <li>ResultSet.CONCUR_READ_ONLY
  *                                  <li>ResultSet.CONCUR_UPDATABLE
  *                                </ul>
  *  @return                         The prepared statement object.
  *
  *  @exception      SQLException    If the connection is not open, 
  *					  the maximum number of statements
  *				        for this connection has been reached, the
  *                                result type or currency is not valid,
  *                                or an error occurs.
  **/
  public PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetConcurrency)
  throws SQLException
  {
    validateConnection();
    return connection_.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }


    //@A6A
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
        validateConnection();
        return connection_.prepareStatement (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }


    // @A6A 
    /**
     * Precompiles an SQL statement with optional input parameters
     * and stores it in a PreparedStatement object.  This object can
     * be used to efficiently execute this SQL statement
     * multiple times.
     *
     * <p><B>This method is not supported.  An SQLException is always thrown. </B>
     *
     * @param  sql     The SQL statement.                                  
     * @param  columnNames An array of column names indicating the columns that should be returned from the inserted row or rows.
     * @return         An SQLException is always thrown. This method is not supported.
     * @exception      java.sql.SQLException - Always thrown because the Toolbox JDBC driver does does not support this method.
     * @since Modification 5
    **/
    public PreparedStatement prepareStatement (String sql, String[] columnNames)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }


    // @A6A
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
      validateConnection();
      try {
        connection_.releaseSavepoint(savepoint);
      }
      catch (SQLException e) {
        fireEventIfErrorFatal(e);
        throw e;
      }
    }


  /**
  *  Drops all changes made since the previous commit or
  *  rollback and releases any database locks currently held by
  *  the connection.  This has no effect when the connection
  *  is in auto-commit mode.
  *
  *  @exception SQLException     If the connection is not open
  *                            or an error occurs.
  **/
  public void rollback () throws SQLException
  {
    validateConnection();
    try {
      connection_.rollback();
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }


    // @A6A
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
        validateConnection();
        try {
          connection_.rollback(savepoint);
        }
        catch (SQLException e) {
          fireEventIfErrorFatal(e);
          throw e;
        }
    }


  /**
  *  Sends a request data stream to the system using the
  *  connection's id and does not expect a reply.
  *
  *  @param   request     The request.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  void send (DBBaseRequestDS request) throws SQLException
  {
    validateConnection();
    try {
      connection_.send (request);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Sends a request data stream to the system and does not
  *  expect a reply.
  *
  *  @param   request     The request.
  *  @param   id          The id.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  void send (DBBaseRequestDS request, int id) throws SQLException
  {
    validateConnection();
    try {
      connection_.send(request, id);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Sends a request data stream to the system and does not
  *  expect a reply.
  *
  *  @param   request        The request.
  *  @param   id             The id.
  *  @param   leavePending   Indicates if the request should
  *                          be left pending.  This indicates
  *                          whether or not to base the next
  *                          request on this one.
  *
  *  @exception              SQLException   If an error occurs.
  **/
  void send (DBBaseRequestDS request, int id, boolean leavePending) throws SQLException
  {
    validateConnection();
    try {
      connection_.send(request, id, leavePending);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  // @A2D   /**
  // @A2D   *  Sends a request data stream to the system and discards the reply.
  // @A2D   *
  // @A2D   *  @param   request        The request.
  // @A2D   *  @param   id             The id.
  // @A2D   *  @param   leavePending   Indicates if the request should
  // @A2D   *                          be left pending.  This indicates
  // @A2D   *                          whether or not to base the next
  // @A2D   *                          request on this one.
  // @A2D   *
  // @A2D   *  @exception              SQLException   If an error occurs.
  // @A2D   **/
  // @A2D   void sendAndDiscardReply (DBBaseRequestDS request, int id) throws SQLException
  // @A2D  	{
  // @A2D      validateConnection();
  // @A2D      connection_.sendAndDiscardReply(request, id);
  // @A2D  	}

  /**
  *  Sends a request data stream to the system using the
  *  connection's id and returns the corresponding reply from
  *  the system.
  *
  *  @param   request     The request.
  *  @return              The reply.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request) throws SQLException
  {
    validateConnection();
    try {
      return connection_.sendAndReceive(request);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Sends a request data stream to the system and returns the
  *  corresponding reply from the system.
  *
  *  @param   request     The request.
  *  @param   id          The id.
  *  @return              The reply.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request, int id) throws SQLException
  {
    validateConnection();
    try {
      return connection_.sendAndReceive(request, id);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Sets the auto-commit mode.   If the connection is in auto-commit
  *  mode, then all of its SQL statements are executed and committed
  *  as individual transactions.  Otherwise, its SQL statements are
  *  grouped into transactions that are terminated by either a commit
  *  or rollback.
  *
  *  <p>By default, the connection is in auto-commit mode.  The
  *  commit occurs when the statement execution completes or the
  *  next statement execute occurs, whichever comes first.  In the
  *  case of statements returning a result set, the statement
  *  execution completes when the last row of the result set has
  *  been retrieved or the result set has been closed.  In advanced
  *  cases, a single statement may return multiple results as well
  *  as output parameter values.  Here the commit occurs when all results
  *  and output parameter values have been retrieved.
  *
  *  @param  autoCommit  true to turn on auto-commit mode, false to
  *                      turn it off.
  *
  *  @exception          SQLException    If the connection is not open
  *                                      or an error occurs.
  **/
  public void setAutoCommit (boolean autoCommit) throws SQLException
  {
    validateConnection();
    try {
      connection_.setAutoCommit(autoCommit);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }


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
   */
  public void setConcurrentAccessResolution (int concurrentAccessResolution) throws SQLException
  {  
      validateConnection();
      connection_.setConcurrentAccessResolution(concurrentAccessResolution);
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
        validateConnection();
        connection_.setDB2eWLMCorrelator(bytes);
    }

  /**
  *  This method is not supported.
  *  @exception          SQLException    If the connection is not open.
  **/
  public void setCatalog (String catalog) throws SQLException
  {
    validateConnection();
    connection_.setCatalog(catalog);
  }

  /**
  *  Sets whether the connection is being used for DRDA.
  *
  *  @param  drda        true if the connection is being used for DRDA; false otherwise.
  **/
  void setDRDA (boolean drda)
  throws SQLException                                      // @A3A
  {
    validateConnection();
    connection_.setDRDA(drda);
  }


    //@A6A
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
    public void setHoldability (int holdability)
    throws SQLException                                      
    {
        validateConnection();
        connection_.setHoldability(holdability);
    }


  /**
  *  Sets the connection properties.
  **/
  void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties, AS400 as400)
  throws SQLException
  {
    validateConnection();
    try {
      connection_.setProperties(dataSourceUrl, properties, as400);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }


  /**
  *  Sets the properties.
  **/
  void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400)
  throws SQLException
  {
    validateConnection();
    try {
      connection_.setProperties(dataSourceUrl, properties, as400);
    }
    catch (SQLException e) {
      fireEventIfErrorFatal(e);
      throw e;
    }
  }

  /**
  *  Sets the read-only mode.  This will provide read-only
  *  access to the database.  Read-only mode can be useful by
  *  enabling certain database optimizations. If the caller
  *  specified "read only" or "read call" for the "access" property,
  *  then the read-only mode cannot be set to false.  The read-only
  *  mode cannot be changed while in the middle of a transaction.
  *
  *  @param  readOnly    true to set the connection to read-only mode;
  *                      false to set the connection to read-write mode.
  *
  *  @exception          SQLException    If the connection is not open,
  *                                      a transaction is active, or the
  *                                      "access" property is set to "read
  *                                      only".
  **/
  public void setReadOnly (boolean readOnly)
  throws SQLException
  {
    validateConnection();
    connection_.setReadOnly(readOnly);
  }


    // @A6A
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
    public Savepoint setSavepoint()
    throws SQLException
    {
        validateConnection();
        return connection_.setSavepoint();
    }


    // @A6
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
    public Savepoint setSavepoint(String name)
    throws SQLException
    {   
        validateConnection();
        return connection_.setSavepoint(name);
    }  

  /**
  *
  **/
  void setSystem (AS400 as400)
  throws SQLException                                      // @A3A
  {
    validateConnection();
    connection_.setSystem(as400);
  }

  /**
  *  Sets the transaction isolation level.  The transaction
  *  isolation level cannot be changed while in the middle of
  *  a transaction.
  *
  *  <p>JDBC and DB2 for IBM i use different terminology for transaction 
  *  isolation levels.  The following table provides a terminology 
  *  mapping:
  *
  *  <p><table border>
  *  <tr><th>DB2 for IBM i isolation level</th><th>JDBC transaction isolation level</th></tr>
  *  <tr><td>*CHG</td> <td>TRANSACTION_READ_UNCOMMITTED</td></tr>
  *  <tr><td>*CS</td>  <td>TRANSACTION_READ_COMMITTED</td></tr>
  *  <tr><td>*ALL</td> <td>TRANSACTION_READ_REPEATABLE_READ</td></tr>
  *  <tr><td>*RR</td>  <td>TRANSACTION_SERIALIZABLE</td></tr>
  *  </table>
  *       
  *  @param level The transaction isolation level.  Possible values are:
  *               <ul>
  *                 <li>TRANSACTION_READ_UNCOMMITTED
  *                 <li>TRANSACTION_READ_COMMITTED
  *			         <li>TRANSACTION_REPEATABLE_READ
  *                 <li>TRANSACTION_SERIALIZABLE
  *               </ul>
  *
  *  @exception SQLException If the connection is not open, the input level is not valid
  *                          or unsupported, or a transaction is active.
  **/
  public void setTransactionIsolation (int level)
  throws SQLException
  {
    validateConnection();
    connection_.setTransactionIsolation(level);
  }



  /**
  *  Sets the type map to be used for distinct and structured types.
  *
  *  <p>Note: Distinct types are supported by DB2 for IBM i, but
  *  are not externalized by the IBM Toolbox for Java JDBC driver.
  *  In other words, distinct types behave as if they are the underlying
  *  type.  Structured types are not supported by DB2 for IBM i.
  *  Consequently, this driver does not support the type map.
  *
  *  @param typeMap  The type map.
  *  @exception  SQLException    This exception is always thrown. 
  **/
  public void setTypeMap (Map typeMap) throws SQLException
  {
    validateConnection();
    connection_.setTypeMap(typeMap);
  }



  /**
  *  Returns the connection's catalog name.  
  *  This is the name of the IBM i system.
  *  @return     The catalog name.
  **/
  public String toString ()
  {
    if (connection_ != null)                  // @A3C
      return connection_.toString();
    else                                     // @A3A
      return super.toString();             // @A3A
  }



  /**
  *  Indicates if the connection is using extended formats.
  *  @return true if the connection is using extended formats, false otherwise.
  **/
  boolean useExtendedFormats ()
  throws SQLException                                      // @A3A
  {
    validateConnection();
    return connection_.useExtendedFormats();
  }


  /**
  *  Validates that the connection has not been closed.
  **/
  private void validateConnection()
  throws SQLException                                          // @A3A
  {
    if (connection_ == null)
    {
      // This would indicate that close() has been called on this handle.
      // Note: It does _not_ indicate that the actual physical connection has experienced a fatal connection error.  Therefore, we don't call fireConnectionErrorEvent() in this case.
      JDTrace.logInformation (this, "The connection is closed.");  // @A7C
      JDError.throwSQLException (JDError.EXC_CONNECTION_NONE); // @A3C
    }
  }

  //@2KRA
    /**
     * Starts or stops the Database Host Server trace for this connection.
     * Note:  This method is only supported when running to i5/OS V5R3 or later 
     * and is ignored if you specified to turn on database host server tracing
     * using the 'server trace' connection property.
     * @param trace true to start database host server tracing, false to end it.
     */
    public void setDBHostServerTrace(boolean trace) throws SQLException { //@pdc
        validateConnection();
        connection_.setDBHostServerTrace(trace);
    }
    

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
     * @throws  SQLException if the database server returns an error while 
     *          setting the client info value on the database server.
     * <p>
     */
    public void setClientInfo(String name, String value) throws SQLException
    {
        validateConnection();
    
        connection_.setClientInfo(name, value);
    }

    // @PDA 550 client info
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
     * @throws SQLException
     *             if the database server returns an error while setting the
     *             clientInfo values on the database server
     *             <p>
     * see java.sql.Connection#setClientInfo(String, String)
     *      setClientInfo(String, String)
     */
    public void setClientInfo(Properties properties) throws SQLException
    {
        validateConnection();
        connection_.setClientInfo(properties);
    }

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
        validateConnection();
        return connection_.getClientInfo(name);
    }

    //@PDA 550 client info
    /**
     * Returns a list containing the name and current value of each client info 
     * property supported by the driver.  The value of a client info property 
     * may be null if the property has not been set and does not have a 
     * default value.
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
     * @return  A <code>Properties</code> object that contains the name and current value of 
     *          each of the client info properties supported by the driver.  
     * <p>
     * @throws  SQLException if the database server returns an error when 
     *          fetching the client info values from the database
     */
    public Properties getClientInfo() throws SQLException
    {
        validateConnection();
        return connection_.getClientInfo();
    }
    
}
