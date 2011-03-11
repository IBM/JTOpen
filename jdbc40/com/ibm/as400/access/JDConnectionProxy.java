///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDConnectionProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLXML;
import java.sql.Savepoint;   
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



class JDConnectionProxy
extends AbstractProxyImpl
implements Connection
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


  // Private data.
 
  private JDDatabaseMetaDataProxy metaData_;
  private AS400 as400PublicClassObj_; // Prevents garbage collection.


    // Copied from JDError:
    private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";


  private Object callFactoryMethod (String methodName,
                           Class[] argClasses,
                           Object[] argValues,
                           ProxyFactoryImpl proxyObject)
    throws SQLException
  {
    try {
      if (argClasses == null)
        return connection_.callFactoryMethod (
                                              pxId_,
                                              methodName,
                                              proxyObject);
      else
        return connection_.callFactoryMethod (
                                              pxId_,
                                              methodName,
                                              argClasses,
                                              argValues,
                                              proxyObject);
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }

  private void callMethod (String methodName)
    throws SQLException
  {
    try {
      connection_.callMethod (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }

  private void callMethod (String methodName,
                           Class[] argClasses,
                           Object[] argValues)
    throws SQLException
  {
    try {
      connection_.callMethod (pxId_, methodName, argClasses, argValues);
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }

  private boolean callMethodRtnBool (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }

  private Object callMethodRtnObj (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsObject (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }

    //@PDA jdbc40
    private Object callMethodRtnObj(String methodName, Class[] argClasses, Object[] argValues) throws SQLException
    {
        try
        {
            return connection_.callMethod(pxId_, methodName, argClasses, argValues).getReturnValue();
        } catch (InvocationTargetException e)
        {
            throw rethrow1(e);
        }
    }
  
  public void clearWarnings ()
    throws SQLException
  {
    callMethod ("clearWarnings");
  }


  public void close ()
    throws SQLException
  {
    callMethod ("close");
  }


  public void commit ()
    throws SQLException
  {
    callMethod ("commit");
  }


 // This implements the ProxyImpl interface.
  public void construct  (ProxyClientConnection connection)
  {
    connection_ = connection;
    // Note: We need to check for a secondary URL (in setProperties)
    //       before we know what kind of remote object to create,
    //       therefore we simply capture the 'connection' value here.
  }



    public Statement createStatement ()
        throws SQLException
    {
    JDStatementProxy newStatement = new JDStatementProxy (this);
    return (JDStatementProxy) callFactoryMethod (
                                         "createStatement", null, null,
                                         newStatement);
    }



  // JDBC 2.0
  public Statement createStatement (int resultSetType,
                                    int resultSetConcurrency)
    throws SQLException
  {
    JDStatementProxy newStatement = new JDStatementProxy (this);
    return (JDStatementProxy) callFactoryMethod (
                               "createStatement",
                               new Class[] { Integer.TYPE, Integer.TYPE },
                               new Object[] { new Integer(resultSetType),
                                              new Integer(resultSetConcurrency) },
                               newStatement);
  }



// JDBC 3.0
    public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency,
                                      int resultSetHoldability)
    throws SQLException
    {
        JDStatementProxy newStatement = new JDStatementProxy (this);
        return(JDStatementProxy) callFactoryMethod (
                                                   "createStatement",
                                                   new Class[] { Integer.TYPE, Integer.TYPE, 
                                                       Integer.TYPE},
                                                   new Object[] { new Integer(resultSetType),
                                                       new Integer(resultSetConcurrency),
                                                       new Integer(resultSetHoldability)},
                                                   newStatement);
    }



  public boolean getAutoCommit ()
    throws SQLException
  {
    return callMethodRtnBool ("getAutoCommit");
  }



  public String getCatalog ()
    throws SQLException
  {
    return (String) callMethodRtnObj ("getCatalog");
  }



// JDBC 3.0
    public int getHoldability ()
    throws SQLException
    {
        return ((Integer)callMethodRtnObj ("getHoldability")).intValue();
    }



  public DatabaseMetaData getMetaData ()
    throws SQLException
  {
    if (metaData_ == null)
    {
      JDDatabaseMetaDataProxy newMetaData = new JDDatabaseMetaDataProxy (this);
      metaData_ = (JDDatabaseMetaDataProxy) callFactoryMethod (
                                                    "getMetaData", null, null,
                                                    newMetaData);
    }
    return metaData_;
  }



  public int getTransactionIsolation ()
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "getTransactionIsolation");
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }



  // JDBC 2.0
  public Map getTypeMap ()
    throws SQLException
  {
    return (Map) callMethodRtnObj ("getTypeMap");
  }



  public SQLWarning getWarnings ()
    throws SQLException
  {
    return (SQLWarning) callMethodRtnObj ("getWarnings");
  }



  public boolean isClosed ()
    throws SQLException
  {
    return callMethodRtnBool ("isClosed");
  }



  public boolean isReadOnly ()
    throws SQLException
  {
    return callMethodRtnBool ("isReadOnly");
  }



  public String nativeSQL (String sql)
    throws SQLException
  {
    try {
      return (String) connection_.callMethod (pxId_, "nativeSQL",
                                   new Class[] { String.class },
                                   new Object[] { sql })
                             .getReturnValue ();
    }
    catch (InvocationTargetException e) {
      throw rethrow1 (e);
    }
  }



  public CallableStatement prepareCall (String sql)
    throws SQLException
  {
    JDCallableStatementProxy newStatement = new JDCallableStatementProxy (this);
    return (JDCallableStatementProxy) callFactoryMethod (
                                       "prepareCall",
                                       new Class[] { String.class },
                                       new Object[] { sql },
                                       newStatement);
  }



  // JDBC 2.0
  public CallableStatement prepareCall (String sql,
                                        int resultSetType,
                                        int resultSetConcurrency)
    throws SQLException
  {
    JDCallableStatementProxy newStatement = new JDCallableStatementProxy (this);
    return (JDCallableStatementProxy) callFactoryMethod (
                                       "prepareCall",
                                       new Class[] { String.class, Integer.TYPE,
                                                     Integer.TYPE },
                                       new Object[] { sql,
                                                      new Integer(resultSetType),
                                                      new Integer(resultSetConcurrency) },
                                       newStatement);
  }



// JDBC 3.0
    public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency,
                                          int resultSetHoldability)
    throws SQLException
    {
        JDCallableStatementProxy newStatement = new JDCallableStatementProxy (this);
        return(JDCallableStatementProxy) callFactoryMethod (
                                                           "prepareCall",
                                                           new Class[] { String.class, Integer.TYPE,
                                                               Integer.TYPE, Integer.TYPE},
                                                           new Object[] { sql,
                                                               new Integer(resultSetType),
                                                               new Integer(resultSetConcurrency),
                                                               new Integer(resultSetHoldability)},
                                                           newStatement);
    }




  public PreparedStatement prepareStatement (String sql)
    throws SQLException
  {
      JDPreparedStatementProxy newStatement = new JDPreparedStatementProxy (this);
      return (JDPreparedStatementProxy) callFactoryMethod (
                            "prepareStatement",
                            new Class[] { String.class },
                            new Object[] { sql },
                            newStatement);
  }



// JDBC 3.0
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        JDPreparedStatementProxy newStatement = new JDPreparedStatementProxy (this);
        return(JDPreparedStatementProxy) callFactoryMethod (
                                                           "prepareStatement",
                                                           new Class[] { String.class, Integer.TYPE},
                                                           new Object[] { sql, 
                                                               new Integer(autoGeneratedKeys)},
                                                           newStatement);
    }



// JDBC 3.0
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



// JDBC 3.0
    public PreparedStatement prepareStatement (String sql, String[] columnNames)
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



  // JDBC 2.0
  public PreparedStatement prepareStatement (String sql,
                                             int resultSetType,
                                             int resultSetConcurrency)
    throws SQLException
  {
      JDPreparedStatementProxy newStatement = new JDPreparedStatementProxy (this);
      return (JDPreparedStatementProxy) callFactoryMethod (
                            "prepareStatement",
                            new Class[] { String.class, Integer.TYPE,
                                          Integer.TYPE },
                            new Object[] { sql,
                                           new Integer(resultSetType),
                                           new Integer(resultSetConcurrency) },
                            newStatement);
  }



// JDBC 3.0
    public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency,
                                               int resultSetHoldability)
    throws SQLException
    {
        JDPreparedStatementProxy newStatement = new JDPreparedStatementProxy (this);
        return(JDPreparedStatementProxy) callFactoryMethod (
                                                           "prepareStatement",
                                                           new Class[] { String.class, Integer.TYPE,
                                                               Integer.TYPE, Integer.TYPE},
                                                           new Object[] { sql,
                                                               new Integer(resultSetType),
                                                               new Integer(resultSetConcurrency),
                                                               new Integer(resultSetHoldability)},
                                                           newStatement);
    }



// JDBC 3.0
    public void releaseSavepoint (Savepoint savepoint)
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



  static InternalErrorException rethrow1 (InvocationTargetException e)
    throws SQLException
  {
    Throwable e2 = e.getTargetException ();
    if (e2 instanceof SQLException)
      throw (SQLException) e2;
    else
      return ProxyClientConnection.rethrow (e);
  }



  public void rollback ()
    throws SQLException
  {
    callMethod ("rollback");
  }



// JDBC 3.0
    public void rollback (Savepoint savepoint)
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



  public void setAutoCommit (boolean autoCommit)
    throws SQLException
  {
    callMethod ("setAutoCommit",
                new Class[] { Boolean.TYPE },
                new Object[] { new Boolean(autoCommit) });
  }



  public void setCatalog (String catalog)
    throws SQLException
  {
    callMethod ("setCatalog",
                new Class[] { String.class },
                new Object[] { catalog });
  }



// JDBC 3.0
    public void setHoldability (int holdability)
    throws SQLException
    {
        callMethod ("setHoldability",
                    new Class[] { Integer.TYPE},
                    new Object[] { new Integer(holdability)});
    }



  // Copied from JDError:
  private static final String EXC_CONNECTION_REJECTED = "08004";
  private static final String EXC_CONNECTION_UNABLE   = "08001";

  // Note: This method is used by AS400JDBCDriver.
  void setProperties (JDDataSourceURL dataSourceUrl, JDProperties properties,
                      AS400 as400)
    throws SQLException
  {
    String remoteClassName;
    Class[] argClasses;
    Object[] argValues;
    String secondaryUrl = dataSourceUrl.getSecondaryURL ();
    if (secondaryUrl.length() == 0) {
      remoteClassName = "AS400JDBCConnection";
      argClasses = new Class[] { JDDataSourceURL.class,
                                 JDProperties.class,
                                 AS400Impl.class };
      argValues = new Object[] { dataSourceUrl, properties, as400.getImpl() };

            try { 
                as400.connectService (AS400.DATABASE);
            }
      catch (AS400SecurityException e) {                             //@A0C
        // Avoid dragging in JDError:
        if (JDTrace.isTraceOn ()) {
          synchronized (DriverManager.class) {
            e.printStackTrace (DriverManager.getLogStream ());
          }
        }
        throw new SQLException (
                AS400JDBCDriver.getResource("JD" + EXC_CONNECTION_REJECTED),
                EXC_CONNECTION_REJECTED, -99999);
      }
      catch (java.io.IOException e) {                                //@A0C
        // Avoid dragging in JDError:
        if (JDTrace.isTraceOn ()) {
          synchronized (DriverManager.class) {
            e.printStackTrace (DriverManager.getLogStream ());
          }
        }
        throw new SQLException (
                AS400JDBCDriver.getResource("JD" + EXC_CONNECTION_UNABLE),
                EXC_CONNECTION_UNABLE, -99999);
      }
    }
        else
        {  // A secondary URL was specified, so get a generic connection.
      remoteClassName = "JDGenericConnection";
      argClasses = new Class[] { String.class, Properties.class };
      argValues = new Object[] { secondaryUrl, properties.getOriginalInfo() };
    }
    try {
      pxId_ = connection_.callConstructor (remoteClassName, false);
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
    callMethod ("setProperties", argClasses, argValues);
  }



  public void setReadOnly (boolean readOnly)
    throws SQLException
  {
    callMethod ("setReadOnly",
                new Class[] { Boolean.TYPE },
                new Object[] { new Boolean(readOnly) });
  }



// JDBC 3.0
    public Savepoint setSavepoint ()
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



// JDBC 3.0
    public Savepoint setSavepoint (String name)
    throws SQLException
    {
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    } 



  void setSystem (AS400 as400)
  {
    as400PublicClassObj_    = as400;
  }



  public void setTransactionIsolation (int level)
    throws SQLException
  {
    callMethod ("setTransactionIsolation",
                new Class[] { Integer.TYPE },
                new Object[] { new Integer(level) });
  }



  // JDBC 2.0
  public void setTypeMap (Map typeMap)
    throws SQLException
  {
    callMethod ("setTypeMap",
                new Class[] { Map.class },
                new Object[] { typeMap });
  }



  // This method is not required by java.sql.Connection,
  // but it is used by the JDBC testcases, and is implemented
  // in the public class.
  public String toString ()
  {
    try {
      return (String) connection_.callMethodReturnsObject (pxId_, "toString");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }
  
  
  //@pda jdbc40
  protected String[] getValidWrappedList()
  {
      return new String[] {  "com.ibm.as400.access.AS400JDBCConnection", "java.sql.Connection"  };
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
      Object[] oa = new Object[] {new Integer(timeout)};
      return ((Boolean)callMethodRtnObj("isValid", new Class[] {Integer.TYPE}, oa)).booleanValue();
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
   * The following are standard client info properties.  Drivers are not 
   * required to support these properties however if the driver supports a 
   * client info property that can be described by one of the standard 
   * properties, the standard property name should be used.
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
  public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        try
        {
            callMethod("setClientInfo", 
                    new Class[] { String.class, String.class }, 
                    new Object[] { name, value });
        } catch (SQLException e)
        {
            //may be SQLException or SQLClientInfoException
            if(e instanceof SQLClientInfoException)
                throw (SQLClientInfoException)e;
            else
            {
                //HashMap<String,ClientInfoStatus> m = new HashMap<String,ClientInfoStatus>(); //@pdd jdbc40 merge
            	//@PDC jdbc40 merge.  code hashmap without generic references for pre-jdk1.6
            	HashMap m = new HashMap();
                m.put(name, ClientInfoStatus.REASON_UNKNOWN);
                SQLClientInfoException clientIE = new SQLClientInfoException(e.getMessage(), e.getSQLState(), m);
                throw clientIE;
            }
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
   * @param properties
   *            the list of client info properties to set
   *            <p>
   * @throws ClientInfoException
   *             if the database server returns an error while setting the
   *             clientInfo values on the database server
   *             <p>
   * @see java.sql.Connection#setClientInfo(String, String)
   *      setClientInfo(String, String)
   */
  public void setClientInfo(Properties properties) throws SQLClientInfoException
  {
      try
      {
          callMethod ("setClientInfo",
                  new Class[] { Properties.class },
                  new Object[] { properties });
      }catch(SQLException e)
      {
          //may be SQLException or SQLClientInfoException
          if(e instanceof SQLClientInfoException)
              throw (SQLClientInfoException)e;
          else
          {
              //create Map<String,ClientInfoStatus> for exception constructor
              //HashMap<String,ClientInfoStatus> m = new HashMap<String,ClientInfoStatus>(); //@pdd jdbc40 merge
        	  //@PDC jdbc40 merge.  code hashmap without generic references for pre-jdk1.6
        	  HashMap m = new HashMap();
              Enumeration clientInfoNames = properties.keys();
              while( clientInfoNames.hasMoreElements())
              {
                  String clientInfoName = (String)clientInfoNames.nextElement();
                  m.put(clientInfoName, ClientInfoStatus.REASON_UNKNOWN);
              }
              SQLClientInfoException clientIE = new SQLClientInfoException(e.getMessage(), e.getSQLState(), m);
              throw clientIE;
          }
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
   * @param name      The name of the client info property to retrieve
   * <p>
   * @return          The value of the client info property specified
   * <p>
   * @throws SQLException     if the database server returns an error when 
   *                          fetching the client info value from the database.
   * <p>
   * @see java.sql.DatabaseMetaData#getClientInfoProperties
   */
  public String getClientInfo(String name) throws SQLException
  {
      return (String) callMethodRtnObj("getClientInfo",
              new Class[] { String.class },
              new Object[] { name });
  }

  //@PDA jdbc40
  /**
   * Returns a list containing the name and current value of each client info 
   * property supported by the driver.  The value of a client info property 
   * may be null if the property has not been set and does not have a 
   * default value.
   * <p>
   * @return  A <code>Properties</code> object that contains the name and current value of 
   *          each of the client info properties supported by the driver.  
   * <p>
   * @throws  SQLException if the database server returns an error when 
   *          fetching the client info values from the database
   */
  public Properties getClientInfo() throws SQLException
  {
      return (Properties) callMethodRtnObj("getClientInfo");
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
      return (Clob) callMethodRtnObj("createClob");
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
      return (Blob) callMethodRtnObj("createBlob");
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
      return (NClob) callMethodRtnObj("createNClob");
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
      return (SQLXML) callMethodRtnObj("createSQLXML");
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
      return (Array) callMethodRtnObj("createArrayOf",
              new Class[] { String.class, Object[].class },
              new Object[] { typeName, elements });
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
      return (Struct) callMethodRtnObj("createStruct",
              new Class[] { String.class, Object[].class },
              new Object[] { typeName, attributes });
  }
}
