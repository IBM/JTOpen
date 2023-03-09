///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDGenericConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.*; //@A2C
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
/* ifdef JDBC40 
import java.sql.SQLClientInfoException;
endif */ 
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
/* ifdef JDBC40 
import java.sql.NClob;
endif */ 
import java.sql.PreparedStatement;
/* ifdef JDBC40 
import java.sql.SQLXML;
import java.util.concurrent.Executor;
endif */ 
import java.sql.Savepoint; //@A1A
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;



class JDGenericConnection
/*ifdef JDBC40
extends ToolboxWrapper
endif */ 
implements Connection
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Private data.
  private Connection actualConnection_;



  public void clearWarnings ()
    throws SQLException
  {
    actualConnection_.clearWarnings ();
  }


  public void close ()
    throws SQLException
  {
    actualConnection_.close ();
  }


  public void commit ()
    throws SQLException
  {
    actualConnection_.commit ();
  }



  public Statement createStatement ()
    throws SQLException
  {
    return actualConnection_.createStatement ();
  }



  // JDBC 2.0
  public Statement createStatement (int resultSetType,
                                    int resultSetConcurrency)
    throws SQLException
  {
    return actualConnection_.createStatement (resultSetType, resultSetConcurrency);
  }


    //@A1A
    public Statement createStatement (int resultSetType,
                                      int resultSetConcurrency,
                                      int resultSetHoldability)
    throws SQLException
    {
//@A2D        return actualConnection_.createStatement (resultSetType, resultSetConcurrency, resultSetHoldability);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("createStatement", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE}); //@A2A
      return(Statement)m.invoke(actualConnection_, new Object[] { new Integer(resultSetType), new Integer(resultSetConcurrency), new Integer(resultSetHoldability)}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }


  protected void finalize ()
    throws Throwable
  {
    if (actualConnection_ != null &&
        ! actualConnection_.isClosed ())
      actualConnection_.close ();

    super.finalize ();
  }



  public boolean getAutoCommit ()
    throws SQLException
  {
    return actualConnection_.getAutoCommit ();
  }



  public String getCatalog ()
    throws SQLException
  {
    return actualConnection_.getCatalog ();
  }



    //@A1A
    public int getHoldability ()
    throws SQLException
    {
//@A2D        return actualConnection_.getHoldability ();
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("getHoldability", new Class[] {}); //@A2A
      return((Integer)m.invoke(actualConnection_, new Object[] {})).intValue(); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return -1; //@A2A
  }



  public DatabaseMetaData getMetaData ()
    throws SQLException
  {
    return actualConnection_.getMetaData ();
  }



  public int getTransactionIsolation ()
    throws SQLException
  {
    return actualConnection_.getTransactionIsolation ();
  }



  // JDBC 2.0
  public Map getTypeMap ()
    throws SQLException
  {
    return actualConnection_.getTypeMap ();
  }



  public SQLWarning getWarnings ()
    throws SQLException
  {
    return actualConnection_.getWarnings ();
  }



  public boolean isClosed ()
    throws SQLException
  {
    return actualConnection_.isClosed ();
  }



  public boolean isReadOnly ()
    throws SQLException
  {
    return actualConnection_.isReadOnly ();
  }



  public String nativeSQL (String sql)
    throws SQLException
  {
    return actualConnection_.nativeSQL (sql);
  }



  public CallableStatement prepareCall (String sql)
    throws SQLException
  {
    return actualConnection_.prepareCall (sql);
  }



  // JDBC 2.0
  public CallableStatement prepareCall (String sql,
                                        int resultSetType,
                                        int resultSetConcurrency)
    throws SQLException
  {
    return actualConnection_.prepareCall (sql, resultSetType, resultSetConcurrency);
  }



    //@A1A
    public CallableStatement prepareCall (String sql,
                                          int resultSetType,
                                          int resultSetConcurrency,
                                          int resultSetHoldability)
    throws SQLException
    {
//@A2D        return actualConnection_.prepareCall (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("prepareCall", new Class[] { String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE}); //@A2A
      return(CallableStatement)m.invoke(actualConnection_, new Object[] { sql, new Integer(resultSetType), new Integer(resultSetConcurrency), new Integer(resultSetHoldability)}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }



  public PreparedStatement prepareStatement (String sql)
    throws SQLException
  {
    return actualConnection_.prepareStatement (sql);
  }


    //@A1A
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
    throws SQLException
    {
//@A2D        return actualConnection_.prepareStatement (sql, autoGeneratedKeys);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("prepareStatement", new Class[] { String.class, Integer.TYPE}); //@A2A
      return(PreparedStatement)m.invoke(actualConnection_, new Object[] { sql, new Integer(autoGeneratedKeys)}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }


    //@A1A
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
    throws SQLException
    {
//@A2D        return actualConnection_.prepareStatement(sql, columnIndexes);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("prepareStatement", new Class[] { String.class, columnIndexes.getClass()}); //@A2A
      return(PreparedStatement)m.invoke(actualConnection_, new Object[] { sql, columnIndexes}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }


  // JDBC 2.0
  public PreparedStatement prepareStatement (String sql,
                                             int resultSetType,
                                             int resultSetConcurrency)
    throws SQLException
  {
    return actualConnection_.prepareStatement (sql, resultSetType, resultSetConcurrency);
  }


    //@A1A
    public PreparedStatement prepareStatement (String sql,
                                               int resultSetType,
                                               int resultSetConcurrency, 
                                               int resultSetHoldability)
    throws SQLException
    {
//@A2D        return actualConnection_.prepareStatement (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("prepareStatement", new Class[] { String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE}); //@A2A
      return(PreparedStatement)m.invoke(actualConnection_, new Object[] { sql, new Integer(resultSetType), new Integer(resultSetConcurrency), new Integer(resultSetHoldability)}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }



    //@A1A
    public PreparedStatement prepareStatement (String sql, String[] columnNames)
    throws SQLException
    {
//@A2D        return actualConnection_.prepareStatement (sql, columnNames);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("prepareStatement", new Class[] { String.class, columnNames.getClass()}); //@A2A
      return(PreparedStatement)m.invoke(actualConnection_, new Object[] { sql, columnNames}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }



    //@A1A
    public void releaseSavepoint(Savepoint savepoint)
    throws SQLException
    {   
//@A2D        actualConnection_.releaseSavepoint(savepoint);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("releaseSavepoint", new Class[] { Savepoint.class}); //@A2A
      m.invoke(actualConnection_, new Object[] { savepoint}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
  }



  public void rollback ()
    throws SQLException
  {
    actualConnection_.rollback ();
  }



    //@A1A
    public void rollback(Savepoint savepoint)
    throws SQLException
    {
//@A2D        actualConnection_.rollback(savepoint);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("rollback", new Class[] { Savepoint.class}); //@A2A
      m.invoke(actualConnection_, new Object[] { savepoint}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
  }



  public void setAutoCommit (boolean autoCommit)
    throws SQLException
  {
    actualConnection_.setAutoCommit (autoCommit);
  }



  public void setCatalog (String catalog)
    throws SQLException
  {
    actualConnection_.setCatalog (catalog);
  }


    //@A1A
    public void setHoldability (int holdability)
    throws SQLException
    {
//@A2D        actualConnection_.setHoldability (holdability);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("setHoldability", new Class[] { Integer.TYPE}); //@A2A
      m.invoke(actualConnection_, new Object[] { new Integer(holdability)}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
  }



  void setProperties (String url, Properties properties)
    throws SQLException
  {
    // Set actualConnection_ to point to an appropriate Connection object
    // for the specified database.
    actualConnection_ = java.sql.DriverManager.getConnection (url,
                                                              properties);
  }



  public void setReadOnly (boolean readOnly)
    throws SQLException
  {
    actualConnection_.setReadOnly (readOnly);
  }



    //@A1A
    public Savepoint setSavepoint()
    throws SQLException
    {      
//@A2D        return actualConnection_.setSavepoint();
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("setSavepoint", new Class[] {}); //@A2A
      return(Savepoint)m.invoke(actualConnection_, new Object[] {}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }



    //@A1A
    public Savepoint setSavepoint(String name)
    throws SQLException
    { 
//@A2D        return actualConnection_.setSavepoint(name);
    try
    {
      Method m = actualConnection_.getClass().getDeclaredMethod("setSavepoint", new Class[] { String.class}); //@A2A
      return(Savepoint)m.invoke(actualConnection_, new Object[] { name}); //@A2A
    }
    catch (NoSuchMethodException nsme) //@A2A
    {
    }
    catch (IllegalAccessException iae) //@A2A
    {
    }
    catch (InvocationTargetException iae) //@A2A
    {
    }
    return null; //@A2A
  }



  public void setTransactionIsolation (int level)
    throws SQLException
  {
    actualConnection_.setTransactionIsolation (level);
  }



  // JDBC 2.0
  public void setTypeMap (Map typeMap)
    throws SQLException
  {
    actualConnection_.setTypeMap (typeMap);
  }



  // Note - This method is not required by java.sql.Connection,
  //        but it is used by the JDBC testcases.
  public String toString ()
  {
    return actualConnection_.toString ();
  }


  
  //@pda jdbc40
  protected String[] getValidWrappedList()
  {
      return new String[] { "java.sql.Connection" };
  } 
  
  
  //@PDA jdbc40
//JDBC40DOC   /**
//JDBC40DOC    * Returns true if the connection has not been closed and is still valid.  
//JDBC40DOC    * The driver shall submit a query on the connection or use some other 
//JDBC40DOC    * mechanism that positively verifies the connection is still valid when 
//JDBC40DOC    * this method is called.
//JDBC40DOC    * <p>
//JDBC40DOC    * The query submitted by the driver to validate the connection shall be 
//JDBC40DOC    * executed in the context of the current transaction.
//JDBC40DOC    * 
//JDBC40DOC    * @param timeout -     The time in seconds to wait for the database operation 
//JDBC40DOC    *                      used to validate the connection to complete.  If 
//JDBC40DOC    *                      the timeout period expires before the operation 
//JDBC40DOC    *                      completes, this method returns false.  A value of 
//JDBC40DOC    *                      0 indicates a timeout is not applied to the 
//JDBC40DOC    *                      database operation.  Note that currently the timeout
//JDBC40DOC    *                      value is not used.
//JDBC40DOC    * <p>
//JDBC40DOC    * @return true if the connection is valid, false otherwise
//JDBC40DOC    * @exception SQLException if a database access error occurs.
//JDBC40DOC    */ 
  /* ifdef JDBC40 
  public boolean isValid(int timeout) throws SQLException 
  { 
      return actualConnection_.isValid(timeout);
  }
   endif */ 
        
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
   * @param name      The name of the client info property to set 
   * @param value     The value to set the client info property to.  If the 
   *                  value is null, the current value of the specified
   *                  property is cleared.
   * <p>
   * @throws  SQLException if the database server returns an error while 
   *          setting the client info value on the database server.
   * <p>
   */
  public void setClientInfo(String name, String value) 
  /* ifdef JDBC40 
  throws SQLClientInfoException
  endif */ 
  /* ifndef JDBC40 */ 
  throws SQLException
  /* endif */ 
  {
	  ((AS400JDBCConnection)actualConnection_).setClientInfo(name, value);
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
//JDBC40DOC    * @throws ClientInfoException If a client info property cannot be set.
   * @throws SQLException  If a database error occurs.
   *             if the database server returns an error while setting the
   *             clientInfo values on the database server
   *             <p>
//JDBC40DOC    * @see java.sql.Connection#setClientInfo(String, String)
//JDBC40DOC    *      setClientInfo(String, String)
   */
  public void setClientInfo(Properties properties) 
  /* ifdef JDBC40 
  throws SQLClientInfoException
  endif */ 
  /* ifndef JDBC40 */ 
  throws SQLException 
  /* endif */ 
  {
	  ((AS400JDBCConnection)actualConnection_).setClientInfo(properties);
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
//JDBC40DOC    * @see java.sql.DatabaseMetaData#getClientInfoProperties
   */
  public String getClientInfo(String name) throws SQLException
  {
      return ((AS400JDBCConnection)actualConnection_).getClientInfo(name);
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
      return ((AS400JDBCConnection)actualConnection_).getClientInfo();
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
      return ((AS400JDBCConnection)actualConnection_).createClob();
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
      return ((AS400JDBCConnection)actualConnection_).createBlob();
  }

  //@PDA jdbc40
//JDBC40DOC   /**
//JDBC40DOC    * Constructs an object that implements the <code>NClob</code> interface. The object
//JDBC40DOC    * returned initially contains no data.  The <code>setAsciiStream</code>,
//JDBC40DOC    * <code>setCharacterStream</code> and <code>setString</code> methods of the <code>NClob</code> interface may
//JDBC40DOC    * be used to add data to the <code>NClob</code>.
//JDBC40DOC    * @return An object that implements the <code>NClob</code> interface
//JDBC40DOC    * @throws SQLException if an object that implements the
//JDBC40DOC    * <code>NClob</code> interface can not be constructed.
//JDBC40DOC    *
//JDBC40DOC    */
  /* ifdef JDBC40 
  public NClob createNClob() throws SQLException
  {
      return actualConnection_.createNClob();
  }
  endif */ 
  //@PDA jdbc40
//JDBC40DOC   /**
//JDBC40DOC    * Constructs an object that implements the <code>SQLXML</code> interface. The object
//JDBC40DOC    * returned initially contains no data. The <code>createXmlStreamWriter</code> object and
//JDBC40DOC    * <code>setString</code> method of the <code>SQLXML</code> interface may be used to add data to the <code>SQLXML</code>
//JDBC40DOC    * object.
//JDBC40DOC    * @return An object that implements the <code>SQLXML</code> interface
//JDBC40DOC    * @throws SQLException if an object that implements the <code>SQLXML</code> interface can not
//JDBC40DOC    * be constructed
//JDBC40DOC    */
  /* ifdef JDBC40 
  public SQLXML createSQLXML() throws SQLException
  {
      return actualConnection_.createSQLXML();
  }
  endif */ 
  

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
   */
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException
  {
      return ((AS400JDBCConnection)actualConnection_).createArrayOf(typeName, elements);
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
   */
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException
  {   
      return ((AS400JDBCConnection)actualConnection_).createStruct(typeName, attributes);
  }
  

/* ifdef JDBC40 
  // JDBC 4.1
  public void abort(Executor executor) throws SQLException {
    
    ((AS400JDBCConnection)actualConnection_).abort(executor); 
  }
endif */ 
  
/* ifdef JDBC40 
  public int getNetworkTimeout() throws SQLException {
    return ((AS400JDBCConnection)actualConnection_).getNetworkTimeout(); 
    
  }
endif */ 

  // JDBC 4.1
  public String getSchema() throws SQLException {
    return ((AS400JDBCConnection)actualConnection_).getSchema(); 
  }

  public void setSchema(String schema) throws SQLException {
    ((AS400JDBCConnection)actualConnection_).setSchema(schema); 
  }

/* ifdef JDBC40 
  // JDBC 4.1
  public void setNetworkTimeout(Executor executor, int milliseconds)
      throws SQLException {
    ((AS400JDBCConnection)actualConnection_).setNetworkTimeout(executor, milliseconds); 
    
  }
endif */ 

  
}
