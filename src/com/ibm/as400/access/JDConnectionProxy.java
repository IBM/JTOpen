///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDConnectionProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;


class JDConnectionProxy
extends AbstractProxyImpl
implements Connection
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  // Private data.
 
  private JDDatabaseMetaDataProxy metaData_;
  private AS400 as400PublicClassObj_; // Prevents garbage collection.



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

      try { as400.connectService (AS400.DATABASE); }
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
    else  // A secondary URL was specified, so get a generic connection.
    {
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

}
