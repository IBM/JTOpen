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
  
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

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
    connection_.checkAccess(sqlStatement);
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
    connection_.checkOpen();
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

    // Rollback and close the open statements.
    connection_.pseudoClose();

    // notify the pooled connection.
    pooledConnection_.fireConnectionCloseEvent(new ConnectionEvent(pooledConnection_));

    connection_ = null;
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
    connection_.commit();
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
    or later.  If connecting to a V5R1 or earlier version of OS/400, the value for 
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
  *  Returns the default schema.
  *
  *  @return     The default schema, or QGPL if none was specified.
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
                <a href="http://publib.boulder.ibm.com/iseries/v5r2/ic2924/info/rzahh/javadoc/JDBCProperties.html" target="_blank">driver property</a>. </ul>  
                Full functionality of #1 requires support in OS/400 
                V5R2 or later.  If connecting to a V5R1 or earlier version of OS/400, 
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
  *  Every JDBC connection is associated with an OS/400 host server job on the server.  The
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
  *  Returns the user name as currently signed on to the server.
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
  *  Invalidates the connection.
  **/
  public void invalidate()
  {
    connection_ = null;
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
  *  from the JDBC SQL grammar into the native DB2 UDB for iSeries
  *  SQL grammar prior to executing them.
  *
  *  @param  sql     The SQL statement in terms of the JDBC SQL grammar.
  *  @return         The translated SQL statement in the native
  *                  DB2 UDB for iSeries SQL grammar.
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
    or later.  If connecting to a V5R1 or earlier version of OS/400, the value for 
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

<p>This method requires OS/400 V5R2 or later.  If connecting to a V5R1 or earlier version of OS/400, an exception will be 
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
                           if connecting to a V5R1 or earlier version of OS/400,
                           or an error occurs.
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
     * @parm   columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows.
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
     * @parm   columnNames An array of column names indicating the columns that should be returned from the inserted row or rows.
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
        connection_.releaseSavepoint(savepoint);
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
    connection_.rollback();
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
        connection_.rollback(savepoint);
    }


  /**
  *  Sends a request data stream to the server using the
  *  connection's id and does not expect a reply.
  *
  *  @param   request     The request.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  void send (DBBaseRequestDS request) throws SQLException
  {
    validateConnection();
    connection_.send (request);
  }

  /**
  *  Sends a request data stream to the server and does not
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
    connection_.send(request, id);
  }

  /**
  *  Sends a request data stream to the server and does not
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
    connection_.send(request, id, leavePending);
  }

  // @A2D   /**
  // @A2D   *  Sends a request data stream to the server and discards the reply.
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
  *  Sends a request data stream to the server using the
  *  connection's id and returns the corresponding reply from
  *  the server.
  *
  *  @param   request     The request.
  *  @return              The reply.
  *
  *  @exception           SQLException   If an error occurs.
  **/
  DBReplyRequestedDS sendAndReceive (DBBaseRequestDS request) throws SQLException
  {
    validateConnection();
    return connection_.sendAndReceive(request);
  }

  /**
  *  Sends a request data stream to the server and returns the
  *  corresponding reply from the server.
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
    return connection_.sendAndReceive(request, id);
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
    connection_.setAutoCommit(autoCommit);
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
    or later.  If connecting to a V5R1 or earlier version of OS/400, all
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
    connection_.setProperties(dataSourceUrl, properties, as400);
  }


  /**
  *  Sets the properties.
  **/
  void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties, AS400Impl as400)
  throws SQLException
  {
    validateConnection();
    connection_.setProperties(dataSourceUrl, properties, as400);
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
     * <LI>Savepoints require OS/400 V5R2 or later.  An exception is thrown if connecting to a V5R1 or earlier version of OS/400.
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
     * <LI>Savepoints require OS/400 V5R2 or later.  An exception is thrown if connecting to a V5R1 or earlier version of OS/400.
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
  *  <p>JDBC and DB2 UDB for iSeries use different terminology for transaction 
  *  isolation levels.  The following table provides a terminology 
  *  mapping:
  *
  *  <p><table border>
  *  <tr><th>AS/400 or iSeries isolation level</th><th>JDBC transaction isolation level</th></tr>
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
  *  <p>Note: Distinct types are supported by DB2 UDB for iSeries, but
  *  are not externalized by the IBM Toolbox for Java JDBC driver.
  *  In other words, distinct types behave as if they are the underlying
  *  type.  Structured types are not supported by DB2 UDB for iSeries.
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
  *  This is the name of the server.
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
            JDTrace.logInformation (this, "The connection is closed.");  // @A7C
      JDError.throwSQLException (JDError.EXC_CONNECTION_NONE); // @A3C
    }
  }
}
