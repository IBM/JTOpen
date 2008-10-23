// TBD - Cross-link with corresponding non-managed class.
///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCManagedDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import javax.sql.DataSource;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.InitialContext;
import javax.naming.Context;

// Note that there is no inheritance/extension relationship between DataSource and ConnectionPoolDataSource.  Applications code to DataSource, whereas ConnectionPoolDataSource is used internally by application servers, etc.

// Note: We currently have no requirement to provide a managed pooling implementation of javax.sql.XADataSource.

/**
 This implementation of <tt>javax.sql.DataSource</tt> can be used to produce Connection objects that will automatically participate in connection pooling, and are managed by the Toolbox's built-in connection pooling manager.

 <p>
 A DataSource is a factory for connections to the physical data source that this DataSource object represents. An alternative to the DriverManager facility, a DataSource object is the preferred means of getting a connection. An object that implements the DataSource interface will typically be registered with a naming service based on the Java Naming and Directory (JNDI) API.
 <p>
 A DataSource object has properties that can be modified when necessary. For example, if the data source is moved to a different system, the property for the system can be changed. The benefit is that because the data source's properties can be changed, any code accessing that data source does not need to be changed.
 <p>
 A driver that is accessed via a DataSource object does not register itself with the DriverManager. Rather, a DataSource object is retrieved though a lookup operation and then used to create a Connection object. With a basic implementation, the connection obtained through a DataSource object is identical to a connection obtained through the DriverManager facility.
 <p>
 <em>Caution:</em> To avoid the pitfalls of "double-managed" pools, do not use this class in conjunction with a separate connection pool manager, such as that available in WebSphere.  When a separate pool manager is provided, use {@link AS400JDBCDataSource AS400JDBCDataSource} instead.

 @see AS400JDBCManagedConnectionPoolDataSource
 @see AS400JDBCDataSource
 @see AS400JDBCConnectionPoolDataSource
 @see AS400JDBCXADataSource
 **/
public class AS400JDBCManagedDataSource extends ToolboxWrapper //@pdc jdbc40
implements DataSource, Referenceable, Serializable, Cloneable //@PDC 550
{
  private static final String copyright = "Copyright (C) 2005-2006 International Business Machines Corporation and others.";
  private static final boolean DEBUG = false;


  /**
   Implementation notes:
   The properties listed in com.ibm.as400.access.JDProperties should also be included here.
   **/

  // Note:
  // There are three kinds of data sources:
  //  1. Basic DataSource (javax.sql.DataSource).  Provides a getConnection() method that returns an instance of java.sql.Connection.
  //  2. Data source class implemented to provide connection pooling (javax.sql.ConnectionPoolDataSource).  Provides a getPooledConnection() method that returns an instance of javax.sql.PooledConnection.
  //  3. Data Source class implemented to provide distributed transactions (javax.sql.XADataSource).  Provides a getXAConnection() method that returns an instance of javax.sql.XAConnection.

  // Constants
  private static final String DATASOURCE_NAME = "dataSourceName";
  private static final String DESCRIPTION = "description";
  private static final String SERVER_NAME = "serverName";
  private static final String USER = "user";  // same as JDProperties.USER_
  private static final String KEY_RING_NAME = "key ring name";  // same as JDProperties.KEY_RING_NAME_
  private static final String PASSWORD = "pw";  // same as JDProperties.PASSWORD_
  private static final String KEY_RING_PASSWORD = "key ring password";  // same as JDProperties.KEY_RING_PASSWORD_
  private static final String SECURE = "secure";  // same as JDProperties.SECURE_
  private static final String SAVE_PASSWORD = "savepw";
  private static final String PLAIN_TEXT_PASSWORD = "pwd";
  private static final String TRUE_ = "true";
  private static final String FALSE_ = "false";
  private static final String TOOLBOX_DRIVER = "jdbc:as400:";
  private static final int MAX_THRESHOLD = 16777216;                  // Maximum threshold (bytes).

  // socket options to store away in JNDI
  private static final String SOCKET_KEEP_ALIVE = "soKeepAlive";
                                                  // JDProperties: "keep alive"
  private static final String SOCKET_RECEIVE_BUFFER_SIZE = "soReceiveBufferSize";
                                                  // JDProperties: "receive buffer size"
  private static final String SOCKET_SEND_BUFFER_SIZE = "soSendBufferSize";
                                                  // JDProperties: "send buffer size"
  private static final String SOCKET_LINGER = "soLinger";
  private static final String SOCKET_TIMEOUT = "soTimeout";
  private static final String SOCKET_TCP_NO_DELAY = "soTCPNoDelay";

  // Standard data source properties.  (See JDBC Tutorial p. 567, table 16.1)

  transient private AS400 as400_;                           // Object used to store and encrypt the password.
  private String dataSourceName_;                           // Data source name.
  private String description_;                              // Data source description.
  // Note: A "networkProtocol" property is not provided.  The Toolbox JDBC driver uses only 1 protocol, specified in the JDProperties.
  // Note: For security, password value is stored in serialPWBytes_, and no getPassword() method is provided.
  private char[]  serialPWBytes_;
  private int     pwHashcode_;       // hashed password
  // Note: A "portNumber" property is not provided.  It is specified in the JDProperties.
  // Note: A "roleName" property is not provided.
  private String serialServerName_;                         // system name used in serialization.
  private String serialUserName_;                           // User name used in serialization.

  // Additional properties.

  private String serialKeyRingName_;                        // Key ring name used in serialization.
  private boolean isSecure_;
  private char[]  serialKeyRingPWBytes_;
  private boolean savePasswordWhenSerialized_;              // By default, don't save password!!!!
  private JDProperties properties_ = new JDProperties();    // system connection properties.
  private SocketProperties sockProps_ = new SocketProperties(); // socket properties

  // Internal utility fields.

  transient private PrintWriter writer_;                    // The EventLog print writer.
  transient         EventLog log_;
  
  transient private AS400JDBCManagedConnectionPoolDataSource cpds_;

  // Handles loading the appropriate resource bundle
  private static ResourceBundleLoader loader_;

  private boolean dataSourceNameSpecified_;
  transient private JDConnectionPoolManager poolManager_;
  transient private boolean poolManagerInitialized_;

  transient private JDConnectionPoolKey defaultConnectionPoolKey_;
  transient private boolean connectionKeyNeedsUpdate_ = true;

  transient private boolean inUse_;

  /**
   * The maximum storage space that can be used to execute a query.
  **/
  public static final int MAX_STORAGE_LIMIT = AS400JDBCDataSource.MAX_STORAGE_LIMIT; // Maximum query storage limit @550

  /**
   Start tracing the JDBC client.  This is the same as setting
   property "trace=true";  Note the constant is not public.
   It is defined only to be compatible with ODBC
   The numeric value of this constant is 1.
   **/
  static final int TRACE_CLIENT = AS400JDBCDataSource.TRACE_CLIENT;

  /**
   Start the database monitor on the JDBC server job.
   This constant is used when setting the level of tracing for the JDBC server job.
   The numeric value of this constant is 2.
   **/
  public static final int SERVER_TRACE_START_DATABASE_MONITOR = AS400JDBCDataSource.SERVER_TRACE_START_DATABASE_MONITOR;

  /**
   Start debug on the JDBC server job.
   This constant is used when setting the level of tracing for the JDBC server job.
   The numeric value of this constant is 4.
   **/
  public static final int SERVER_TRACE_DEBUG_SERVER_JOB = AS400JDBCDataSource.SERVER_TRACE_DEBUG_SERVER_JOB;

  /**
   Save the joblog when the JDBC server job ends.
   This constant is used when setting the level of tracing for the JDBC server job.
   The numeric value of this constant is 8.
   **/
  public static final int SERVER_TRACE_SAVE_SERVER_JOBLOG = AS400JDBCDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG;

  /**
   Start job trace on the JDBC server job.
   This constant is used when setting the level of tracing for the JDBC server job.
   The numeric value of this constant is 16.
   **/
  public static final int SERVER_TRACE_TRACE_SERVER_JOB = AS400JDBCDataSource.SERVER_TRACE_TRACE_SERVER_JOB;

  /**
   Save SQL information.
   This constant is used when setting the level of tracing for the JDBC server job.
   The numeric value of this constant is 32.
   **/
  public static final int SERVER_TRACE_SAVE_SQL_INFORMATION = AS400JDBCDataSource.SERVER_TRACE_SAVE_SQL_INFORMATION;


  /**
   Constructs a default AS400JDBCManagedDataSource object.
   **/
  public AS400JDBCManagedDataSource()
  {
    initializeTransient();
  }

  /**
   Constructs an AS400JDBCManagedDataSource object to the specified <i>serverName</i>.
   @param serverName The name of the i5/OS system.
   **/
  public AS400JDBCManagedDataSource(String serverName)
  {
    this();

    setServerName(serverName);
  }

  /**
   Constructs an AS400JDBCManagedDataSource object with the specified signon information.
   @param serverName The name of the i5/OS system.
   @param user The user id.
   @param password The user password.
   **/
  public AS400JDBCManagedDataSource(String serverName, String user, String password)
  {
    this();

    setServerName(serverName);
    setUser(user);
    setPassword(password);
  }


  // Note: We do not provide a constructor that takes an AS400 object,
  // because we need to capture the password so we can use it as part of the pool key.


  /**
   Constructs an AS400JDBCManagedDataSource object with the specified signon information
   to use for SSL communications with the i5/OS system.
   @param serverName The name of the system.
   @param user The user id.
   @param password The user password.
   @param keyRingName The key ring class name to be used for SSL communications with the system.
   @param keyRingPassword The password for the key ring class to be used for SSL communications with the system.
   **/
  public AS400JDBCManagedDataSource(String serverName, String user, String password,
                                    String keyRingName, String keyRingPassword)
  {
    this();

    setSecure(true);

    try
    {
      as400_ = new SecureAS400(as400_);
      ((SecureAS400)as400_).setKeyRingName(keyRingName, keyRingPassword);
    }
    catch (PropertyVetoException pe) {} // will never happen

    serialKeyRingName_ = keyRingName;

    // There is no get/set keyring name / password methods so they really aren't bean
    // properties, but in v5r1 the keyring name is saved as if it is a property.  Since
    // the code saved the name we will also save the password.
    serialKeyRingPWBytes_ = xpwConfuse(keyRingPassword);

    setServerName(serverName);
    setUser(user);
    setPassword(password);
  }


  /**
   Constructs an AS400JDBCManagedDataSource object from the specified Reference object
   @param reference The reference to retrieve the DataSource properties from
   **/
  AS400JDBCManagedDataSource(Reference reference)
  {
    // Implementation note:  This method is called from AS400JDBCObjectFactory.getObjectInstance

    if (reference == null)
      throw new NullPointerException("reference");

    Properties properties = new Properties();

    // Set up the as400 object.
    if (((String)reference.get(SECURE).getContent()).equalsIgnoreCase(TRUE_))
    {
      isSecure_ = true;
      as400_ = new SecureAS400();

      // Since the as400 object is secure, get the key ring info.
      serialKeyRingName_ = (String)reference.get(KEY_RING_NAME).getContent();
      // Note: Even though JDProperties has a "key ring" properties, we choose not to add them.
      if (reference.get(KEY_RING_PASSWORD) != null)
        serialKeyRingPWBytes_ = ((String)reference.get(KEY_RING_PASSWORD).getContent()).toCharArray();
      else
        serialKeyRingPWBytes_ = null;

      try {
        if (serialKeyRingPWBytes_ != null && serialKeyRingPWBytes_.length > 0) {
          ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_, xpwDeconfuse(serialKeyRingPWBytes_));
        }
        else {
          ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_);
        }
      } catch (PropertyVetoException pve) {} // Will never happen

    }
    else
    {
      isSecure_ = false;
      as400_ = new AS400();
    }
    // Note that we allow the SECURE property to also get added to JDProperties in the loop below.

    boolean isConnectionPoolDataSource = (this instanceof AS400JDBCManagedConnectionPoolDataSource);

    Enumeration list = reference.getAll();
    while (list.hasMoreElements())
    {
      StringRefAddr refAddr = (StringRefAddr)list.nextElement();
      String property = refAddr.getType();
      String value = (String)reference.get(property).getContent();

      // Constant identifiers were used to store in JNDI.
      // Perform special handling for properties that don't get included in JDProperties.

      if (property.equals(DATASOURCE_NAME)) {
        setDataSourceName(value);
      }
      else if (property.equals(DESCRIPTION))
        setDescription(value);
      else if (property.equals(SERVER_NAME))
        setServerName(value);
      else if (property.equals(USER)) {
        setUser(value);
        properties.put(property, value);  // This value needs to go into JDProperties also.
      }
      else if (property.equals(PLAIN_TEXT_PASSWORD)) {
        //set the password
        setPassword(value);
      }
      else if (property.equals(PASSWORD))
      {
        if (reference.get(PLAIN_TEXT_PASSWORD) != null)
        {
          setPassword((String)reference.get(PLAIN_TEXT_PASSWORD).getContent());
        }
        else
        {
          // get the password back from the serialized char[]
          if (value != null) {
            serialPWBytes_ = value.toCharArray();
            pwHashcode_ = xpwDeconfuse(serialPWBytes_).hashCode();
            // decode the password and set it on the as400
            as400_.setPassword(xpwDeconfuse(serialPWBytes_));
          }
        }
      }
      else if (property.equals(SAVE_PASSWORD)) {
        // set the savePasswordWhenSerialized_ flag
        savePasswordWhenSerialized_ = value.equals(TRUE_) ? true : false;
      }
      else if (/*property.equals(SECURE) ||*/ property.equals(KEY_RING_NAME) || property.equals(KEY_RING_PASSWORD)) {
        // Do nothing for these keys.  They've already been handled prior to loop,
        // and we don't want them added to JDProperties.
      }
      else if (property.equals(SOCKET_KEEP_ALIVE)) {
        sockProps_.setKeepAlive((value.equals(TRUE_)? true : false));
      }
      else if (property.equals(SOCKET_RECEIVE_BUFFER_SIZE)) {
        sockProps_.setReceiveBufferSize(Integer.parseInt(value));
      }
      else if (property.equals(SOCKET_SEND_BUFFER_SIZE)) {
        sockProps_.setSendBufferSize(Integer.parseInt(value));
      }
      else if (property.equals(SOCKET_LINGER)) {
        sockProps_.setSoLinger(Integer.parseInt(value));
      }
      else if (property.equals(SOCKET_TIMEOUT)) {
        sockProps_.setSoTimeout(Integer.parseInt(value));
      }
      else if (property.equals(SOCKET_TCP_NO_DELAY)) {
        sockProps_.setTcpNoDelay((value.equals(TRUE_)? true : false));
      }
      else if (isConnectionPoolDataSource &&
               AS400JDBCManagedConnectionPoolDataSource.isConnectionPoolProperty(property)) {
        // Ignore this property, the subclass will consume it.
        if (DEBUG) logDiagnostic("AS400JDBCManagedDataSource(Reference) is ignoring connection pool property \"" + property + "\"");
      }
      else { // It's neither a "socket property" nor a property that needs special handling.
        properties.put(property, value);  // Assume it goes into JDProperties.
      }
    }  // 'while' loop

    properties_ = new JDProperties(properties, null);
    if (sockProps_.isAnyOptionSet()) {  // only need to set if not default
      as400_.setSocketProperties(sockProps_);
    }

    // Get the prompt property and set it back in the as400 object.
    String prmpt = properties_.getString(JDProperties.PROMPT);
    if (prmpt != null && prmpt.equalsIgnoreCase(FALSE_))
      setPrompt(false);
    else if (prmpt != null && prmpt.equalsIgnoreCase(TRUE_))
      setPrompt(true);
  }


  /**
   Verifies the health of the connection pool.  For example: That the connection counts are consistent with the connection list lengths, and that all the daemons are still running.
   <p>Note: This method is provided for use as a diagnostic tool <i>only</i>.  It temporarily "freezes" the connection pool while it collects statistics and examines the pool state.
   @param logStatistics If true, additional information is logged.
   @return true if connection pool exists and appears healthy; false otherwise.
   **/
  public boolean checkPoolHealth(boolean logStatistics)
  {
    if (poolManager_ == null) {
      logWarning("Connection pool does not exist");
      return false;
    }
    else return poolManager_.checkHealth(logStatistics);
  }


  // For exclusive use within this class and by AS400JDBCManagedConnectionPoolDataSource.
  /**
   Returns the database connection.
   @return The connection.
   @throws SQLException If a database error occurs.
   **/
  final AS400JDBCConnection createPhysicalConnection() throws SQLException
  {
    // If we have an AS400JDBCManagedConnectionPoolDataSource, delegate the connection creation.
    if (cpds_ != null) {
      return cpds_.createPhysicalConnection();
    }
    else
    {
      AS400 as400Object = null;

      // If the object was created with a keyring, or if the user asks for the object
      // to be secure, clone a SecureAS400 object; otherwise, clone an AS400 object.
      if (isSecure_ || isSecure()) {
        as400Object = new SecureAS400(as400_);
      }
      else {
        as400Object = new AS400(as400_);
      }
      if (sockProps_.isAnyOptionSet()) {  // only need to set if not default
        as400Object.setSocketProperties(sockProps_);
      }

      return createPhysicalConnection(as400Object);
    }
  }


  // For exclusive use within this class and by AS400JDBCManagedConnectionPoolDataSource.
  /**
   Returns the database connection using the specified <i>user</i> and <i>password</i>.
   @param user The database user.
   @param password The database password.
   @return The connection
   @throws SQLException If a database error occurs.
   **/
  final AS400JDBCConnection createPhysicalConnection(String user, String password) throws SQLException
  {
    // Validate the parameters.
    if (user == null)
      throw new NullPointerException("user");
    if (password == null)
      throw new NullPointerException("password");

    // If we have an AS400JDBCManagedConnectionPoolDataSource, delegate the connection creation.
    if (cpds_ != null) {
      return cpds_.createPhysicalConnection(user, password);
    }
    else
    {
      AS400 as400Object = null;

      // If the object was created with a keyring, or if the user asks for the object
      // to be secure, clone a SecureAS400 object; otherwise, clone an AS400 object.
      if (isSecure_ || isSecure()) {
        as400Object = new SecureAS400(as400_);
      }
      else {
        as400Object = new AS400(as400_);
      }
      try {
        as400Object.setUserId(user);
        as400Object.setPassword(password);
      }
      catch (PropertyVetoException pve) {}  // will never happen

      // Set GUI available on the new object to false if user turned prompting off.
      try {
        if (!isPrompt()) {
          as400Object.setGuiAvailable(false);
        }
      }
      catch (PropertyVetoException pve) {} // this will never happen

      if (sockProps_.isAnyOptionSet()) {  // only need to set if not default
        as400Object.setSocketProperties(sockProps_);
      }

      return createPhysicalConnection(as400Object);
    }
  }


  /**
   Utility method to creates the database connection based on the signon and property information.
   @param as400 The AS400 object used to make the connection.
   @throws SQLException If a database error occurs.
   **/
  private AS400JDBCConnection createPhysicalConnection(AS400 as400) throws SQLException
  {
    AS400JDBCConnection connection = new AS400JDBCConnection();

    connection.setProperties(new JDDataSourceURL(TOOLBOX_DRIVER + "//" + as400.getSystemName()), properties_, as400);  // Note: This also does an AS400.connectService() to the database host server.

    if (JDTrace.isTraceOn() || log_ != null) logInformation(loader_.getText("AS400_JDBC_DS_CONN_CREATED"));
    return connection;
  }


  //@PDA 550 - clone
  /**
   * Method to create a clone of AS400JDBCManagedDataSource. This does a shallow
   * copy, with the exception of JDProperties, which also gets cloned.
   */
  public Object clone()
  {
      try
      {
          AS400JDBCManagedDataSource clone = (AS400JDBCManagedDataSource) super.clone();
          clone.properties_ = (JDProperties) this.properties_.clone();
          return clone;
      } catch (CloneNotSupportedException e)
      { // This should never happen.
          Trace.log(Trace.ERROR, e);
          throw new UnsupportedOperationException("clone()");
      }
  }
  

  /**
   Shuts down the connection pool in an orderly manner.  Closes all connections in the pool.
   @throws Throwable If an error occurs.
   **/
  public void closePool()
  {
    if (poolManager_ != null) poolManager_.closePool();
  }


  /**
   Returns the level of database access for the connection.
   @return The access level.  Valid values include: "all" (all SQL statements allowed),
   "read call" (SELECT and CALL statements allowed), and "read only" (SELECT statements only).
   The default value is "all".
   **/
  public String getAccess()
  {
    return properties_.getString(JDProperties.ACCESS);
  }

  /**
   Returns what behaviors of the Toolbox JDBC driver have been overridden.
   Multiple behaviors can be overridden in combination by adding
   the constants and passing that sum on the setBehaviorOverride() method.
   @return The behaviors that have been overridden.
   <p>The return value is a combination of the following:
   <ul>
   <li>1 - Do not throw an exception if Statement.executeQuery() or
   PreparedStatement.executeQuery() do not return a result set.
   Instead, return null for the result set.
   </ul>
   **/
  public int getBehaviorOverride()
  {
    return properties_.getInt(JDProperties.BEHAVIOR_OVERRIDE);
  }

  /**
   Returns the output string type of bidi data, as defined by the CDRA
   (Character Data Representation Architecture). See <a href="BidiStringType.html">
   BidiStringType</a> for more information and valid values.  -1 will be returned
   if the value has not been set.
   **/
  public int getBidiStringType()
  {
    String value = properties_.getString(JDProperties.BIDI_STRING_TYPE);
    try
    {
      return Integer.parseInt (value);
    }
    catch (NumberFormatException nfe)  // if value is "", that is, not set
    {
      return -1;
    }
  }


  /**
   Returns the criteria for retrieving data from the system in
   blocks of records.  Specifying a non-zero value for this property
   will reduce the frequency of communication to the system, and
   therefore increase performance.
   @return The block criteria.
   <p>Valid values include:
   <ul>
   <li> 0 (no record blocking)
   <li> 1 (block if FOR FETCH ONLY is specified)
   <li> 2 (block if FOR UPDATE is specified) - The default value.
   </ul>
   **/
  public int getBlockCriteria()
  {
    return properties_.getInt(JDProperties.BLOCK_CRITERIA);
  }

  /**
   Returns the block size in kilobytes to retrieve from the i5/OS system and
   cache on the client.  This property has no effect unless the block criteria
   property is non-zero.  Larger block sizes reduce the frequency of
   communication to the system, and therefore may increase performance.
   @return The block size in kilobytes.
   <p>Valid values include:
   <ul>
   <li> 0
   <li> 8
   <li> 16
   <li> 32   - The default value.
   <li> 64
   <li> 128
   <li> 256
   <li> 512
   </ul>
   **/
  public int getBlockSize()
  {
    return properties_.getInt(JDProperties.BLOCK_SIZE);
  }


  // method required by javax.sql.DataSource
  /**
   Returns a database connection.
   <br>Note: If a dataSourceName has been specified (via {@link #setDataSourceName setDataSourceName()}, this method will return a pooled connection.  Otherwise it will return a non-pooled connection.
   <p>If pooling, the very first call to one of the getConnection() methods for this class will create and initialize the connection pool, and may have slow response.  Therefore it is advisable for the application to make an initial "dummy" call to getConnection().
   <br>If the connection pool is at or near capacity, a non-pooled connection may be returned.
   <p>It is the responsibility of the caller to ultimately call <tt>Connection.close()</tt> to release the connection, even if the connection has become unusable.
   @return The connection.
   @throws SQLException If a database error occurs.
   @see #setDataSourceName
   **/
  public Connection getConnection() throws SQLException
  {
    // Note: This method will return either an AS400JDBCConnection or an AS400JDBCConnectionHandle.
    Connection connection;

    if (dataSourceNameSpecified_)  // A datasource name has been specified, so use pooling.
    {
      connection = getConnectionFromPool(null, null);  // 'null' indicates "use default key".
      // Returns a connection handle or null.

      // The pooling implementation can return null if it can't produce a
      // connection for the user.  It is still our job to return a valid
      // connection so drop back to returning a non-pooled connection at
      // this point.
      if (connection == null)
      {
        // Future enhancement: Add a property to indicate whether we should return a non-pooled connection, or throw an exception.
        connection = createPhysicalConnection();
      }
    }
    else  // No datasource name has been specified, so return raw (unpooled) connection.
    {
      if (JDTrace.isTraceOn() || log_ != null) {
        logWarning("No datasource name was specified, so connections will not be pooled");
      }
      // The connection is not linked to a pooled connection. Return a non-pooled connection.
      connection = createPhysicalConnection();
    }

    if (DEBUG)
    {
      // Verify that the connection is for the correct user.
      if (connection instanceof AS400JDBCConnection)
      {
        AS400JDBCConnection conn = (AS400JDBCConnection)connection;
        String userForConn = conn.getUserName();
        if (!userForConn.equalsIgnoreCase(cpds_.getUser())) {
          logError("MDS.getConnection() is returning a connection with incorrect user: [" + userForConn +"]");
        }
      }
      else if (connection instanceof AS400JDBCConnectionHandle) {
        AS400JDBCConnectionHandle conn = (AS400JDBCConnectionHandle)connection;
        String userForConn = conn.getUserName();
        if (!userForConn.equalsIgnoreCase(cpds_.getUser())) {
          logError("MDS.getConnection() is returning a connectionHandle with incorrect user: [" + userForConn +"]");
        }
      }
      else {
        logError("MDS.getConnection() is returning an instance of " + connection.getClass().getName());
      }
    }

    return connection;
  }

  // method required by javax.sql.DataSource
  /**
   Returns a database connection using the specified <i>user</i> and <i>password</i>.
   <br>Note: If a dataSourceName has been specified (via {@link #setDataSourceName setDataSourceName()}, this method will return a pooled connection.  Otherwise it will return a non-pooled connection.
   <p>If pooling, the very first call to one of the getConnection() methods for this class will create and initialize the connection pool, and may have slow response.  Therefore it is advisable for the application to make an initial "dummy" call to getConnection().
   <br>If the connection pool is at or near capacity, a non-pooled connection may be returned.
   <p>It is the responsibility of the caller to ultimately call <tt>Connection.close()</tt> to release the connection, even if the connection has become unusable.
   @param user The database user.
   @param password The database password.
   @return The connection
   @throws SQLException If a database error occurs.
   @see #setDataSourceName
   **/
  public Connection getConnection(String user, String password) throws SQLException
  {
    // Note: This method will return either an AS400JDBCConnection or an AS400JDBCConnectionHandle.

    // Validate the parameters.
    if (user == null)
      throw new NullPointerException("user");
    if (password == null)
      throw new NullPointerException("password");

    Connection connection = null;
    if (dataSourceNameSpecified_)  // A datasource name has been specified, so use pooling.
    {
      // Note: xpwConfuse() generates different output each time it's called against the same password, so we can't use it bo build the pool key.
      JDConnectionPoolKey key = new JDConnectionPoolKey(user, password.hashCode());
      connection = getConnectionFromPool(key, password);  // Returns a connection handle or null.

      // The pooling implementation can return null if it can't produce a
      // connection for the user.  It is still our job to return a valid
      // connection so drop back to returning a non-pooled connection at
      // this point.
      if (connection == null)
      {
        // Future enhancement: Add a property to indicate whether we should return a non-pooled connection, or throw an exception.
        connection = createPhysicalConnection(user, password);
      }
    }
    else  // No datasource name has been specified, so return raw (unpooled) connection.
    {
      if (JDTrace.isTraceOn() || log_ != null) {
        logWarning("No datasource name was specified, so connections will not be pooled");
      }
      // The connection is not linked to a pooled connection. Return a non-pooled connection.
      connection = createPhysicalConnection(user, password);
    }

    if (DEBUG)
    {
      // Verify that the connection is for the correct user.
      if (connection instanceof AS400JDBCConnection) {
        AS400JDBCConnection conn = (AS400JDBCConnection)connection;
        String userForConn = conn.getUserName();
        if (!userForConn.equalsIgnoreCase(user)) {
          logError("MDS.getConnection("+user+") is returning a connection with incorrect user: [" + userForConn +"]");
        }
      }
      else if (connection instanceof AS400JDBCConnectionHandle) {
        AS400JDBCConnectionHandle conn = (AS400JDBCConnectionHandle)connection;
        String userForConn = conn.getUserName();
        if (!userForConn.equalsIgnoreCase(user)) {
          logError("MDS.getConnection("+user+") is returning a connectionHandle with incorrect user: [" + userForConn +"]");
        }
      }
      else {
        logError("MDS.getConnection() is returning an instance of " + connection.getClass().getName());
      }
    }

    return connection;
  }


  /**
   Returns a database connection from the pool, or null if the connection pool is at or near capacity.

   @param key The connection pool key.  'null' indicates that the default key is to be used.
   @return The connection.  May return null if pool is at or near capacity.
   @throws SQLException If a database error occurs.
   **/
  private final AS400JDBCConnectionHandle getConnectionFromPool(JDConnectionPoolKey key, String password) throws SQLException
  {
    // Note: This method generally returns an AS400JDBCPooledConnection.  If the connection pool is full or nonexistent, it may return an (unpooled) AS400JDBCConnection.

    AS400JDBCConnectionHandle connection = null;

    if (!poolManagerInitialized_) {
      initializeConnectionPool();  // this sets inUse_ to true
    }

    connection = poolManager_.getConnection(key, password);
    if (connection == null)
    {
      if (JDTrace.isTraceOn() || log_ != null)
        logWarning("Connection pool is at or near capacity, so returning a non-pooled connection");
    }

    return connection;
  }


  // For exclusive use within this class and by JDConnectionPoolManager.
  final JDConnectionPoolKey getConnectionPoolKey()
  {
    // See if we need to update our connection pool key.
    if (connectionKeyNeedsUpdate_)
    {
      if (defaultConnectionPoolKey_ == null)
      {
        defaultConnectionPoolKey_ = new JDConnectionPoolKey(serialUserName_, pwHashcode_);
      }
      else // key already exists, so just update it
      {
        defaultConnectionPoolKey_.update(serialUserName_, pwHashcode_);
      }
    }

    connectionKeyNeedsUpdate_ = false;
    return defaultConnectionPoolKey_;
  }


  /**
   Returns the value of the cursorSensitivity property.  If the resultSetType is
   ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_SENSITIVE, the value of this property
   will control what cursor sensitivity is requested from the database.  If the resultSetType
   is ResultSet.TYPE_SCROLL_INSENSITIVE, this property will be ignored.
   @return The cursor sensitivity.
   <p>Valid values include:
   <ul>
   <li> "asensitive"
   <li> "insensitive"
   <li> "sensitive"
   </ul>
   The default is "asensitive".
   This property is ignored when connecting to systems
   running V5R1 and earlier versions of OS/400.
   **/
  public String getCursorSensitivity()
  {
    return properties_.getString(JDProperties.CURSOR_SENSITIVITY);
  }


  /**
   Returns the value of the databaseName property.  For more information see
   the documentation for the setDatabaseName() method in this class.
   @return The database name.
   **/
  public String getDatabaseName()
  {
    return properties_.getString(JDProperties.DATABASE_NAME);
  }

  /**
   Returns the value of the dataSourceName property.
   This property is used to name an underlying data source when connection pooling is used.
   @return The data source name.
   **/
  public String getDataSourceName()
  {
    return (dataSourceName_ == null ? "" : dataSourceName_);
  }

  /**
   Returns the date format used in date literals within SQL statements.
   @return The date format.
   <p>Valid values include:
   <ul>
   <li> "mdy"
   <li> "dmy"
   <li> "ymd"
   <li> "usa"
   <li> "iso"
   <li> "eur"
   <li> "jis"
   <li> "julian"
   <li> ""  (server job value) - default.
   </ul>
   The default is based on the server job.
   **/
  public String getDateFormat()
  {
    return properties_.getString(JDProperties.DATE_FORMAT);
  }

  /**
   Returns the date separator used in date literals within SQL statements.
   This property has no effect unless the "data format" property is set to:
   "julian", "mdy", "dmy", or "ymd".
   @return The date separator.
   <p>Valid values include:
   <ul>
   <li> "/" (slash)
   <li> "-" (dash)
   <li> "." (period)
   <li> "," (comma)
   <li> " " (space)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public String getDateSeparator()
  {
    return properties_.getString(JDProperties.DATE_SEPARATOR);
  }

  //@DFA
  /**
     Returns the decfloat rounding mode.
     @return The decfloat rounding mode.
    <p>Valid values include:
    <ul>
    <li>"half even" - default
    <li>"half up" 
    <li>"down" 
    <li>"ceiling" 
    <li>"floor" 
    <li>"half down" 
    <li>"up" 
    </ul>
   **/
   public String getDecfloatRoundingMode()
   {
       return properties_.getString(JDProperties.DECFLOAT_ROUNDING_MODE);
   }
   
  /**
   Returns the decimal separator used in numeric literals within SQL statements.
   @return The decimal separator.
   <p>Valid values include:
   <ul>
   <li> "." (period)
   <li> "," (comma)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public String getDecimalSeparator()
  {
    return properties_.getString(JDProperties.DECIMAL_SEPARATOR);
  }

  //@igwrn
  /**
  *  Returns the ignore warnings property.
  *  Specifies a list of SQL states for which the driver should not create warning objects.
  *  @return The ignore warnings.
  **/
  public String getIgnoreWarnings()
  {
      return properties_.getString(JDProperties.IGNORE_WARNINGS);
  }
  
  /**
   Returns the description of the data source.
   @return The description.
   **/
  public String getDescription()
  {
    return (description_ == null ? "" : description_);
  }

  /**
   Returns the JDBC driver implementation.
   This property has no
   effect if the "secondary URL" property is set.
   This property cannot be set to "native" if the
   environment is not an i5/OS Java Virtual
   Machine.
   <p>Valid values include:
   <ul>
   <li>"toolbox" (use the IBM Toolbox for Java JDBC driver)
   <li>"native" (use the IBM Developer Kit for Java JDBC driver)
   </ul>
   The default value is "toolbox".
   **/
  public String getDriver()
  {
    return properties_.getString(JDProperties.DRIVER);
  }

  /**
   Returns the amount of detail for error messages originating from
   the i5/OS system.
   @return The error message level.
   Valid values include: "basic" and "full".  The default value is "basic".
   **/
  public String getErrors()
  {
    return properties_.getString(JDProperties.ERRORS);
  }

  /**
   Returns the libraries to add to the server job's library list.
   The libraries are delimited by commas or spaces, and
   "*LIBL" may be used as a place holder for the server job's
   current library list.  The library list is used for resolving
   unqualified stored procedure calls and finding schemas in
   DatabaseMetaData catalog methods.  If "*LIBL" is not specified,
   the specified libraries will replace the server job's current library list.
   @return The library list.
   **/
  public String getLibraries()
  {
    return properties_.getString(JDProperties.LIBRARIES);
  }

  /**
   Returns the maximum LOB (large object) size in bytes that
   can be retrieved as part of a result set.  LOBs that are larger
   than this threshold will be retrieved in pieces using extra
   communication to the i5/OS system.  Larger LOB thresholds will reduce
   the frequency of communication to the system, but will download
   more LOB data, even if it is not used.  Smaller LOB thresholds may
   increase frequency of communication to the system, but will only
   download LOB data as it is needed.
   @return The lob threshold.  Valid range is 0-16777216.
   The default value is 32768.
   **/
  public int getLobThreshold()
  {
    return properties_.getInt(JDProperties.LOB_THRESHOLD);
  }

  // method required by javax.sql.DataSource
  /**
   Returns the timeout value in seconds.
   <br><i>Note: This value is not used or supported by the Toolbox JDBC driver.</i>
   Rather, the timeout value is determined by the i5/OS system.
   @return the maximum time in seconds that this data source can wait while attempting to connect to a database.
   **/
  public int getLoginTimeout()
  {
    return properties_.getInt(JDProperties.LOGIN_TIMEOUT);
  }

  // method required by javax.sql.DataSource
  /**
   Returns the log writer for this data source.
   @return The log writer for this data source.
   @throws SQLException If a database error occurs.
   **/
  public PrintWriter getLogWriter() throws SQLException
  {
    return writer_;
  }
  
  //@PDA
  /**                                                               
  *  Indicates how to retrieve DatabaseMetaData.
  *  If set to 0, database metadata will be retrieved through the ROI data flow.  
  *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
  *  The methods that currently are available through stored procedures are:
  *  getColumnPrivileges
  *  @return the metadata setting.
  *  The default value is 1.
  **/
  public int getMetaDataSource()
  {
      return properties_.getInt(JDProperties.METADATA_SOURCE);
  }
  
  //@dup
  /**                                                               
   *  Indicates how to retrieve DatabaseMetaData.
   *  If set to 0, database metadata will be retrieved through the ROI data flow.  
   *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
   *  The methods that currently are available through stored procedures are:
   *  getColumnPrivileges
   *  @return the metadata setting.
   *  The default value is 1.
   *  Note:  this method is the same as getMetaDataSource() so that it corresponds to the connection property name
   **/
  public int getMetadataSource()
  {
      return getMetaDataSource();
  }
  

  /**
   Returns the naming convention used when referring to tables.
   @return The naming convention.  Valid values include: "sql" (e.g. schema.table)
   and "system" (e.g. schema/table).  The default value is "sql".
   **/
  public String getNaming()
  {
    return properties_.getString(JDProperties.NAMING);
  }

  /**
   Returns the base name of the SQL package.  Note that only the
   first seven characters are used to generate the name of the SQL package on the i5/OS system.
   This property has no effect unless
   the extended dynamic property is set to true.  In addition, this property
   must be set if the extended dynamic property is set to true.
   @return The base name of the SQL package.
   **/
  public String getPackage()
  {
    return properties_.getString(JDProperties.PACKAGE);
  }

  /**
   Returns the type of SQL statement to be stored in the SQL package.  This can
   be useful to improve the performance of complex join conditions.  This
   property has no effect unless the extended dynamic property is set to true.
   @return The type of SQL statement.
   Valid values include: "default" (only store SQL statements with parameter
   markers in the package) and "select" (store all SQL SELECT statements
   in the package).  The default value is "default".
   **/
  public String getPackageCriteria()
  {
    return properties_.getString(JDProperties.PACKAGE_CRITERIA);
  }

  /**
   Returns the action to take when SQL package errors occur.  When an SQL package
   error occurs, the driver will optionally throw an SQLException or post a
   warning to the Connection, based on the value of this property.  This property
   has no effect unless the extended dynamic property is set to true.
   @return The action to take when SQL errors occur.
   Valid values include: "exception", "warning", and "none".  The default value is "warning".
   **/
  public String getPackageError()
  {
    return properties_.getString(JDProperties.PACKAGE_ERROR);
  }

  /**
   Returns the library for the SQL package.  This property has no effect unless
   the extended dynamic property is set to true.
   @return The SQL package library.  The default package library is "QGPL".
   **/
  public String getPackageLibrary()
  {
    return properties_.getString(JDProperties.PACKAGE_LIBRARY);
  }


  // Note: This method must never be public.  It is provided for exclusive use by AS400JDBCManagedConnectionPoolDataSource.
  /**
   Returns the password bytes.
   @return The password bytes.
   **/
  final char[] getPWBytes()
  {
    return serialPWBytes_;
  }

  /**
   Returns the name of the proxy server.
   @return The proxy server.
   **/
  public String getProxyServer()
  {
    return properties_.getString(JDProperties.PROXY_SERVER);
  }

  /**
   Returns the "query optimize goal" property
   @return The optimization goal 
   <p>Possible values include:
   <ul>
   <li>0 = Optimize query for first block of data (*ALLIO) when extended dynamic packages are used; Optimize query for entire result set (*FIRSTIO) when packages are not used</li>
   <li>1 = Optimize query for first block of data (*FIRSTIO)</li>
   <li>2 = Optimize query for entire result set (*ALLIO) </li>
   </ul>
   **/
  public int getQueryOptimizeGoal()
  {
    return properties_.getInt(JDProperties.QUERY_OPTIMIZE_GOAL);
  }

  //@550
    /**
    * Returns the storage limit in megabytes, that should be used for statements executing a query in a connection.
    * Note, this setting is ignored when running to V5R4 i5/OS or earlier
    * <p> Valid values are -1 to MAX_STORAGE_LIMIT megabytes.  
    * The default value is -1 meaning there is no limit.
    **/
    public int getQueryStorageLimit()
    {
        return properties_.getInt(JDProperties.QUERY_STORAGE_LIMIT);
    }


  // method required by javax.naming.Referenceable
  /**
   Returns the Reference object for the data source object.
   This is used by JNDI when bound in a JNDI naming service.
   Contains the information necessary to reconstruct the data source
   object when it is later retrieved from JNDI via an object factory.
   *
   @return A Reference object of the data source object.
   @throws NamingException If a naming error occurs in resolving the object.
   **/
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "com.ibm.as400.access.AS400JDBCObjectFactory",
                                  null);

    // Add the JDBC properties.
    DriverPropertyInfo[] propertyList = properties_.getInfo();
    for (int i=0; i< propertyList.length; i++)
    {
      if (propertyList[i].value != null)
        ref.add(new StringRefAddr(propertyList[i].name, propertyList[i].value));
    }

    // Add the Socket options
    if (sockProps_.keepAliveSet_) ref.add(new StringRefAddr(SOCKET_KEEP_ALIVE, (sockProps_.keepAlive_ ? "true" : "false")));
    if (sockProps_.receiveBufferSizeSet_) ref.add(new StringRefAddr(SOCKET_RECEIVE_BUFFER_SIZE, Integer.toString(sockProps_.receiveBufferSize_)));
    if (sockProps_.sendBufferSizeSet_) ref.add(new StringRefAddr(SOCKET_SEND_BUFFER_SIZE, Integer.toString(sockProps_.sendBufferSize_)));
    if (sockProps_.soLingerSet_) ref.add(new StringRefAddr(SOCKET_LINGER, Integer.toString(sockProps_.soLinger_)));
    if (sockProps_.soTimeoutSet_) ref.add(new StringRefAddr(SOCKET_TIMEOUT, Integer.toString(sockProps_.soTimeout_)));
    if (sockProps_.tcpNoDelaySet_) ref.add(new StringRefAddr(SOCKET_TCP_NO_DELAY, (sockProps_.tcpNoDelay_ ? "true" : "false")));

    // Add the data source properties.  (unique constant identifiers for storing in JNDI).
    if (dataSourceName_ != null)
      ref.add(new StringRefAddr(DATASOURCE_NAME, dataSourceName_));
    if (description_ != null)
      ref.add(new StringRefAddr(DESCRIPTION, description_));
    ref.add(new StringRefAddr(SERVER_NAME, getServerName()));
    ref.add(new StringRefAddr(USER, getUser()));
    ref.add(new StringRefAddr(KEY_RING_NAME, serialKeyRingName_));
    if (savePasswordWhenSerialized_) {
      if (serialPWBytes_ != null)
        ref.add(new StringRefAddr(PASSWORD, new String(serialPWBytes_)));
      else
        ref.add(new StringRefAddr(PASSWORD, null));
      if (serialKeyRingPWBytes_ != null)
        ref.add(new StringRefAddr(KEY_RING_PASSWORD, new String(serialKeyRingPWBytes_)));
      else
        ref.add(new StringRefAddr(KEY_RING_PASSWORD, null));
    }
    ref.add(new StringRefAddr(SAVE_PASSWORD, (savePasswordWhenSerialized_ ? TRUE_ : FALSE_)));

    return ref;
  }

  /**
   Returns the source of the text for REMARKS columns in ResultSets returned
   by DatabaseMetaData methods.
   @return The text source.
   Valid values include: "sql" (SQL object comment) and "system" (i5/OS object description).
   The default value is "system".
   **/
  public String getRemarks()
  {
    return properties_.getString(JDProperties.REMARKS);
  }

  /**
   Returns the secondary URL.
   @return The secondary URL.
   **/
  public String getSecondaryUrl()
  {
    return properties_.getString(JDProperties.SECONDARY_URL);
  }
  
  
  //@dup
  /**
   *  Returns the secondary URL.
   *  @return The secondary URL.
   *  Note:  this method is the same as setSecondaryUrl() so that it corresponds to the connection property name
   **/
  public String getSecondaryURL()
  {
      return getSecondaryUrl();
  }
   

  /**
   Returns the value of the serverName property.
   @return The system name.
   **/
  public String getServerName()
  {
    return as400_.getSystemName();
  }


  /**
   Returns the level of tracing started on the JDBC server job.
   If tracing is enabled, tracing is started when
   the client connects to the i5/OS system and ends when the connection
   is disconnected.  Tracing must be started before connecting to
   the system since the client enables tracing only at connect time.
   Trace data is collected in spooled files on the system.  Multiple
   levels of tracing can be turned on in combination by adding
   the constants and passing that sum on the set method.  For example,
   <pre>
   dataSource.setServerTraceCategories(AS400JDBCManagedDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCManagedDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
   </pre>
   @return The tracing level.
   <p>The value is a combination of the following:
   <ul>
   <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
   The numeric value of this constant is 2.
   <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
   The numeric value of this constant is 4.
   <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
   The numeric value of this constant is 8.
   <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
   The numeric value of this constant is 16.
   <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
   The numeric value of this constant is 32.
   </ul>
   *
   <P>
   Tracing the JDBC server job will use significant amounts of system resources.
   Additional processor resource is used to collect the data, and additional
   storage is used to save the data.  Turn on tracing only to debug
   a problem as directed by IBM service.
   *
   **/
  public int getServerTraceCategories()
  {
    return properties_.getInt(JDProperties.TRACE_SERVER);
  }
  
  //@dup
  /**
   Returns the level of tracing started on the JDBC server job.
   If tracing is enabled, tracing is started when
   the client connects to the i5/OS system and ends when the connection
   is disconnected.  Tracing must be started before connecting to
   the system since the client enables tracing only at connect time.
   Trace data is collected in spooled files on the system.  Multiple
   levels of tracing can be turned on in combination by adding
   the constants and passing that sum on the set method.  For example,
   <pre>
   dataSource.setServerTraceCategories(AS400JDBCManagedDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCManagedDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
   </pre>
   @return The tracing level.
   <p>The value is a combination of the following:
   <ul>
   <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
   The numeric value of this constant is 2.
   <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
   The numeric value of this constant is 4.
   <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
   The numeric value of this constant is 8.
   <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
   The numeric value of this constant is 16.
   <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
   The numeric value of this constant is 32.
   </ul>
   *
   <P>
   Tracing the JDBC server job will use significant amounts of system resources.
   Additional processor resource is used to collect the data, and additional
   storage is used to save the data.  Turn on tracing only to debug
   a problem as directed by IBM service.
   *
   *  Note:  this method is the same as getServerTraceCategories() so that it corresponds to the connection property name
   **/
   public int getServerTrace()
   {
       return getServerTraceCategories();
   }

  /**
   Returns how the i5/OS system sorts records before sending them to the
   client.
   @return The sort value.
   <p>Valid values include:
   <ul>
   <li>"hex" (base the sort on hexadecimal values)
   <li>"language" (base the sort on the language set in the sort language property)
   <li> "table" (base the sort on the sort sequence table set in the sort table property)
   </ul>
   The default value is "hex".
   **/
  public String getSort()
  {
    return properties_.getString(JDProperties.SORT);
  }

  /**
   Returns the three-character language id to use for selection of a sort sequence.
   @return The three-character language id.
   The default value is ENU.
   **/
  public String getSortLanguage()
  {
    return properties_.getString(JDProperties.SORT_LANGUAGE);
  }

  /**
   Returns the library and file name of a sort sequence table stored on the
   i5/OS system.
   @return The qualified sort table name.
   **/
  public String getSortTable()
  {
    return properties_.getString(JDProperties.SORT_TABLE);
  }

  /**
   Returns how the i5/OS system treats case while sorting records.
   @return The sort weight.
   Valid values include: "shared" (upper- and lower-case characters are sorted as the
   same character) and "unique" (upper- and lower-case characters are sorted as
   different characters).  The default value is "shared".
   **/
  public String getSortWeight()
  {
    return properties_.getString(JDProperties.SORT_WEIGHT);
  }

  /**
   Returns the time format used in time literals with SQL statements.
   @return The time format.
   <p>Valid values include:
   <ul>
   <li> "hms"
   <li> "usa"
   <li> "iso"
   <li> "eur"
   <li> "jis"
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public String getTimeFormat()
  {
    return properties_.getString(JDProperties.TIME_FORMAT);
  }

  /**
   Returns the time separator used in time literals within SQL
   statements.
   @return The time separator.
   <p>Valid values include:
   <ul>
   <li> ":" (colon)
   <li> "." (period)
   <li> "," (comma)
   <li> " " (space)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public String getTimeSeparator()
  {
    return properties_.getString(JDProperties.TIME_SEPARATOR);
  }


  /**
   Returns the i5/OS system's transaction isolation.
   @return The transaction isolation level.
   <p>Valid values include:
   <ul>
   <li> "none"
   <li> "read uncommitted"  - The default value.
   <li> "read committed"
   <li> "repeatable read"
   <li> "serializable"
   </ul>
   **/
  public String getTransactionIsolation()
  {
    return properties_.getString(JDProperties.TRANSACTION_ISOLATION);
  }

  /**
   Returns the QAQQINI library name.
   @return The QAQQINI library name.
   **/
  public String getQaqqiniLibrary()
  {
    return properties_.getString(JDProperties.QAQQINILIB);
  }
  
  //@dup
  /**
   *  Returns the QAQQINI library name.
   *  @return The QAQQINI library name.
   *  Note:  this method is the same as getQaqqiniLibrary() so that it corresponds to the connection property name
   **/
  public String getQaqqinilib()
  {
      return getQaqqiniLibrary();
  }
   

  /**
   Returns the value of the 'user' property.
   @return The user.
   **/
  public String getUser()
  {
    //return properties_.getString(JDProperties.USER);  //PDD
      return as400_.getUserId();  //@PDA if running on host, could be default id
  }

  /**                                                               
   Returns the value of the "XA loosely couple support" property.
   This indicates whether lock sharing is allowed for loosely coupled transaction branches.
   @return The "XA loosely coupled support" setting.
   <p>Possible values include:
   <ul>
   <li>0 = Locks cannot be shared</li>
   <li>1 = Locks can be shared</li>
   </ul>
   **/
  public int getXALooselyCoupledSupport()
  {
    return properties_.getInt(JDProperties.XA_LOOSELY_COUPLED_SUPPORT);
  }


  /**
   Initializes the connection pool and the built-in pool manager.
   If <tt>dataSourceName</tt> property has not been set (via {@link #setDataSourceName setDataSourceName()}, this method does nothing.
   <br>This method gets called upon the first invocation of {@link #getConnection() getConnection()} or {@link getConnection(String,String) getConnection(user,password)}.
   @throws SQLException If a database error occurs.
   **/
  void initializeConnectionPool() throws SQLException
  {
    if (!dataSourceNameSpecified_)
    {
      logWarning("No datasource name was specified, so connections will not be pooled");
      return;
    }

    inUse_ = true;
    if (poolManager_ == null)
    {
      try
      {
        // Assume that the Context.INITIAL_CONTEXT_FACTORY system property has been set.
        Context ctx = new InitialContext();
        cpds_ = (AS400JDBCManagedConnectionPoolDataSource)ctx.lookup(dataSourceName_);
        if (cpds_ == null) {
          logError("Data source name is not bound in JNDI: " + dataSourceName_);
          JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
        }

        getConnectionPoolKey(); // initialize the default connection pool key
        poolManager_ = new JDConnectionPoolManager(this, cpds_);
        // Implementation note: The JNDI lookup() tends to lose the LogWriter value of cpds_, so we need to give the pool manager access to our own LogWriter.
      }
      catch (NamingException ne)
      {
        ne.printStackTrace();
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
      }
    }

    poolManagerInitialized_ = true;
  }

  /**
   Initializes the transient data for object de-serialization.
   **/
  private final void initializeTransient()
  {
    poolManager_ = null;
    poolManagerInitialized_ = false;
    defaultConnectionPoolKey_ = null;
    connectionKeyNeedsUpdate_ = true;
    inUse_ = false;

    if (isSecure_)
      as400_ = new SecureAS400();
    else
      as400_ = new AS400();

    if (sockProps_.isAnyOptionSet()) {  // only need to set if not default
      as400_.setSocketProperties(sockProps_);
    }

    // Reinitialize the serverName, user, password, keyRingName, etc.
    if (serialServerName_ != null)
      setServerName(serialServerName_);

    if (serialUserName_ != null)
    {
      setUser(serialUserName_);

      if ((serialPWBytes_ != null) &&
          (serialPWBytes_.length > 0))
      {
        as400_.setPassword(xpwDeconfuse(serialPWBytes_));
      }
    }

    try
    {
      if (serialKeyRingName_ != null && isSecure_)
      {
        if ((serialKeyRingPWBytes_ != null) &&
            (serialKeyRingPWBytes_.length > 0))
        {
          String keyRingPassword = xpwDeconfuse(serialKeyRingPWBytes_);
          ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_, keyRingPassword);
        }
        else
        {
          ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_);
        }
      }
    }
    catch (PropertyVetoException pve) {} // will never happen

    //     Make sure the prompt flag is correctly de-serialized.  The problem was
    //     the flag would get serialized with the rest of the properties
    //     (in the properties_ object), but the flag would never be applied
    //     to the AS400 object when de-serialzed.  De-serialization puts the
    //     flag back in properties_ but that does no good unless the value
    //     is passed on to the AS400 object.  That is what the new code does.
    //     There is no affect on normal "new" objects since at the time this
    //     method is called properties_ is null.
    try
    {
      if (properties_ != null)
      {
        if (!isPrompt()) {
          as400_.setGuiAvailable(false);
        }
      }
    }
    catch (PropertyVetoException pve) {} // will never happen

  }

  /**
   Invalidates all pooled connections for the specified user/password.
   All current available (unallocated) connections for this user/password are closed.
   As active connections with this user/password are returned to the pool, they are closed.
   @param user The database user.
   @param password The database password.
   **/
  public void invalidate(String user, String password)
  {
    invalidate(user, (password == null ? null : xpwConfuse(password)));
  }

  /**
   Invalidates pooled connections for the specified user/password.
   All current available (unallocated) connections for this user/password are closed.
   As active (in-use) connections with this user/password are returned to the pool, they are closed.
   @param user The database user.
   @param password The database password.
   **/
  private final void invalidate(String user, char[] pwBytes)
  {
    int hash = (pwBytes == null ? 0 : xpwDeconfuse(pwBytes).hashCode());
    JDConnectionPoolKey key = new JDConnectionPoolKey(user, hash);
    if (poolManager_ != null) poolManager_.invalidate(key);
  }

   //@AC1
   /**
   *  Returns whether auto-commit mode is the default connection mode for new connections.
   *  @return Auto commit.
   *  The default value is true.
   **/
   public boolean isAutoCommit()
   {
       return properties_.getBoolean(JDProperties.AUTO_COMMIT);
   }

  //@CE1
  /**
   *  Returns whether commit or rollback throws SQLException when autocommit is enabled.
   *  @return Autocommit Exception.
   *  The default value is false.
   **/
   public boolean isAutocommitException()
   {
       return properties_.getBoolean(JDProperties.AUTOCOMMIT_EXCEPTION);
   }

  /**
   Indicates whether bidi implicit reordering is used.
   @return true if bidi implicit reordering is used; false otherwise.
   The default value is true.
   **/
  public boolean isBidiImplicitReordering()
  {
    return properties_.getBoolean(JDProperties.BIDI_IMPLICIT_REORDERING);
  }

  /**
   Indicates whether bidi numeric ordering round trip is used.
   @return true if bidi numeric ordering round trip is used; false otherwise.
   The default value is false.
   **/
  public boolean isBidiNumericOrdering()
  {
    return properties_.getBoolean(JDProperties.BIDI_NUMERIC_ORDERING);
  }

  /**
   Indicates whether a big decimal value is returned.
   @return true if a big decimal is returned; false otherwise.
   The default value is true.
   **/
  public boolean isBigDecimal()
  {
    return properties_.getBoolean(JDProperties.BIG_DECIMAL);
  }

  /**
   Indicates whether the cursor is held.
   @return true if the cursor is held; false otherwise.
   The default value is true.
   **/
  public boolean isCursorHold()
  {
    return properties_.getBoolean(JDProperties.CURSOR_HOLD);
  }

  /**
   Indicates whether data compression is used.
   @return true if data compression is used; false otherwise.
   The default value is true.
   **/
  public boolean isDataCompression()
  {
    return properties_.getBoolean(JDProperties.DATA_COMPRESSION);
  }

  /**
   Indicates whether data truncation is used.
   @return true if data truncation is used; false otherwise.
   The default value is true.
   **/
  public boolean isDataTruncation()
  {
    return properties_.getBoolean(JDProperties.DATA_TRUNCATION);
  }

  /**
   Indicates whether extended dynamic support is used.  Extended dynamic
   support provides a mechanism for caching dynamic SQL statements on
   the i5/OS system.  The first time a particular SQL statement is prepared, it is
   stored in an SQL package on the system.
   If the package does not exist, it will be automatically created.
   On subsequent prepares of the
   same SQL statement, the system can skip a significant part of the
   processing by using information stored in the SQL package.
   @return true if extended dynamic support is used; false otherwise.
   The default value is not to use extended dynamic support.
   **/
  public boolean isExtendedDynamic()
  {
    return properties_.getBoolean(JDProperties.EXTENDED_DYNAMIC);
  }

  /**
   Indicates whether the driver should request extended metadata from the
   i5/OS system.  If this property is set to true, the accuracy of the information
   that is returned from ResultSetMetaData methods getColumnLabel(int),
   isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
   In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this
   property set to true.  However, performance will be slower with this
   property on.  Leave this property set to its default (false) unless you
   need more specific information from those methods.
   *
   For example, without this property turned on, isSearchable(int) will
   always return true even though the correct answer may be false because
   the driver does not have enough information from the system to make a judgment.  Setting
   this property to true forces the driver to get the correct data from the system.
   *
   @return true if extended metadata will be requested; false otherwise.
   The default value is false.
   **/

  public boolean isExtendedMetaData()
  {
    return properties_.getBoolean(JDProperties.EXTENDED_METADATA);
  }


  //@dup
  /**
   *  Indicates whether the driver should request extended metadata from the
   *  i5/OS system.  If this property is set to true, the accuracy of the information 
   *  that is returned from ResultSetMetaData methods getColumnLabel(int),
   *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
   *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
   *  property set to true.  However, performance will be slower with this 
   *  property on.  Leave this property set to its default (false) unless you
   *  need more specific information from those methods.
   *
   *  For example, without this property turned on, isSearchable(int) will 
   *  always return true even though the correct answer may be false because 
   *  the driver does not have enough information from the system to make a judgment.  Setting 
   *  this property to true forces the driver to get the correct data from the i5/OS system.
   *
   *  @return true if extended metadata will be requested; false otherwise.
   *  The default value is false.
   *  Note:  this method is the same as isExtendedMetaData() so that it corresponds to the connection property name
   **/

  public boolean isExtendedMetadata()
  {
      return isExtendedMetaData();
  }


  /**
   Indicates whether the i5/OS system fully opens a file when performing a query.
   By default the system optimizes opens so they perform better.  In
   certain cases an optimized open will fail.  In some
   cases a query will fail when a database performance monitor
   is turned on even though the same query works with the monitor
   turned off.  In this case set the full open property to true.
   This disables optimization on the system.
   @return true if files are fully opened; false otherwise.
   The default value is false.
   **/
  public boolean isFullOpen()
  {
    return properties_.getBoolean(JDProperties.FULL_OPEN);
  }

  /**
   Returns the value of the "hold input locators" property
   @return true If input locators are held.
   **/
  public boolean isHoldInputLocators()
  {
    return properties_.getBoolean(JDProperties.HOLD_LOCATORS);
  }


  /**
   Returns the value of the "hold statements" property
   @return true If statements are held.
   **/
  public boolean isHoldStatements()
  {
    return properties_.getBoolean(JDProperties.HOLD_STATEMENTS);
  }


  /**
   Indicates whether the pool is in use; that is, whether it contains any connections.
   Used for checking state conditions.  The default is false.
   @return true if the pool is in use; false otherwise.
   **/
  private final boolean isInUse()
  {
    return inUse_;
  }

  /**
   Indicates whether to delay closing cursors until subsequent requests.
   @return true to delay closing cursors until subsequent requests; false otherwise.
   The default value is false.
   **/
  public boolean isLazyClose()
  {
    return properties_.getBoolean(JDProperties.LAZY_CLOSE);
  }

  /**
   Indicates whether to add newly prepared statements to the
   SQL package specified on the "package" property.  This property
   has no effect unless the extended dynamic property is set to true;
   @return true If newly prepared statements should be added to the SQL package specified
   on the "package" property; false otherwise.
   The default value is true.
   **/
  public boolean isPackageAdd()
  {
    return properties_.getBoolean(JDProperties.PACKAGE_ADD);
  }

  /**
   Indicates whether a subset of the SQL package information is cached in client memory.
   Caching SQL packages locally
   reduces the amount of communication to the i5/OS system for prepares and describes.  This
   property has no effect unless the extended dynamic property is set to true.
   @return true if caching is used; false otherwise.
   The defalut value is false.
   **/
  public boolean isPackageCache()
  {
    return properties_.getBoolean(JDProperties.PACKAGE_CACHE);
  }

  /**
   Indicates whether SQL packages are cleared when they become full.  This method
   has been deprecated.  Package clearing and the decision for the
   threshold where package clearing is needed is now handled
   automatically by the database.
   @return Always false.  This method is deprecated.
   @deprecated
   **/
  public boolean isPackageClear()
  {
    return false;
  }

  /**
   Indicates whether data is prefetched upon executing a SELECT statement.
   This will increase performance when accessing the initial rows in the result set.
   @return If prefetch is used; false otherwise.
   The default value is prefetch data.
   **/
  public boolean isPrefetch()
  {
    return properties_.getBoolean(JDProperties.PREFETCH);
  }

  /**
   Indicates whether the user is prompted if a user name or password is
   needed to connect to the i5/OS system.  If a connection can not be made
   without prompting the user, and this property is set to false, then an
   attempt to connect will fail throwing an exception.
   @return true if the user is prompted for signon information; false otherwise.
   The default value is false.
   **/
  public boolean isPrompt()
  {
    return properties_.getBoolean(JDProperties.PROMPT);
  }

  /**
   Returns the value of the "rollback cursor hold" property.
   @return true if cursors are held across rollbacks; false otherwise.
   **/
  public boolean isRollbackCursorHold()
  {
    return properties_.getBoolean(JDProperties.ROLLBACK_CURSOR_HOLD);
  }

  /**
   Indicates whether the password is saved locally with the rest of
   the properties when this data source object is serialized.
   <P>
   If the password is saved, it is up to the application to protect
   the serialized form of the object because it contains all necessary
   information to connect to the i5/OS system.  The default is false.  It
   is a security risk to save the password with the rest of the
   properties so by default the password is not saved.  If the programmer
   chooses to accept this risk, call setSavePasswordWhenSerialized(true)
   to force the Toolbox to save the password with the other properties
   when the data source object is serialized.
   @return true if the password is saved with the rest of the properties when the
   data source object is serialized; false otherwise.
   The default value is false.
   **/
  public boolean isSavePasswordWhenSerialized()
  {
    return savePasswordWhenSerialized_;
  }

  /**
   Indicates whether a Secure Socket Layer (SSL) connection is used to communicate
   with the i5/OS system.  SSL connections are only available when connecting to systems
   at V4R4 or later.
   @return true if Secure Socket Layer connection is used; false otherwise.
   The default value is false.
   **/
  public boolean isSecure()
  {
    return properties_.getBoolean(JDProperties.SECURE);
  }

  //@pw3
  /**
   *  Returns the secure current user setting.  True indicates to disallow "" and *current for user name and password.
   *  @return The secure current user setting.
   **/
  public boolean isSecureCurrentUser()
  {
      return  properties_.getBoolean(JDProperties.SECURE_CURRENT_USER);
  }

  /**
   Indicates whether a thread is used.
   @return true if a thread is used; false otherwise.
   The default value is true.
   **/
  public boolean isThreadUsed()
  {
    return properties_.getBoolean(JDProperties.THREAD_USED);
  }

  /**
   Indicates whether trace messages should be logged.
   @return true if trace message are logged; false otherwise.
   The default value is false.
   **/
  public boolean isTrace()
  {
    return properties_.getBoolean(JDProperties.TRACE);
  }

  /**
   Indicates whether binary data is translated.  If this property is set
   to true, then BINARY and VARBINARY fields are treated as CHAR and
   VARCHAR fields.
   @return true if binary data is translated; false otherwise.
   The default value is false.
   **/
  public boolean isTranslateBinary()
  {
    return properties_.getBoolean(JDProperties.TRANSLATE_BINARY);
  }

  //@PDA
  /**
  *  Indicates how Boolean objects are interpreted when setting the value 
  *  for a character field/parameter using the PreparedStatement.setObject(), 
  *  CallableStatement.setObject() or ResultSet.updateObject() methods.  Setting the 
  *  property to "true", would store the Boolean object in the character field as either 
  *  "true" or "false".  Setting the property to "false", would store the Boolean object 
  *  in the character field as either "1" or "0".
  *  @return true if boolean data is translated; false otherwise.
  *  The default value is true.
  **/
  public boolean isTranslateBoolean()
  {
      return properties_.getBoolean(JDProperties.TRANSLATE_BOOLEAN);
  }
   
  
  /**
   Indicates whether true auto commit support is used.
   @return true if true auto commit support is used; false otherwise.
   The default value is false.
   **/
  public boolean isTrueAutoCommit()
  {
    return properties_.getBoolean(JDProperties.TRUE_AUTO_COMMIT); //@true
  }

  //@dup
  /**
   *  Indicates whether true auto commit support is used.
   *  @return true if true auto commit support is used; false otherwise.
   *  The default value is false.
   *  Note:  this method is the same as isTrueAutoCommit() so that it corresponds to the connection property name
   **/
  public boolean isTrueAutocommit()
  {
      return isTrueAutoCommit();
  }
  

  /**
   Logs an exception and message to the event log.
   @param property The property to log.
   @param value The property value to log.
   **/
  void logProperty(String property, String value)
  {
    // NOTE: JDTrace logs nothing unless JDTrace.isTraceOn() is true.
    if (JDTrace.isTraceOn())
      JDTrace.logProperty (this, property, value);
  }

  final void logDiagnostic(String text)
  {
    // Note: Currently this method is implemented identically to logInformation().
    // At some future point, we may want to add unique behavior to it.
    // In the meantime, it is a placeholder.
    if (JDTrace.isTraceOn())
    {
      JDTrace.logInformation (this, text);
      if (log_ != null) log_.log(text);
    }
  }

  final void logError(String text)
  {
    String msg = "ERROR: " + text;
    JDTrace.logInformation (this, msg);
    if (log_ != null) log_.log(msg);
    else if (DEBUG) System.out.println(msg);
  }

  final void logException(String text, Exception e)
  {
    JDTrace.logException (this, text, e);
    if (log_ != null) log_.log(text, e);
    else if (DEBUG) {
      System.out.println(text);
      e.printStackTrace();
    }
  }

 
  final void logInformation(String text)
  {
    // NOTE: JDTrace logs nothing unless JDTrace.isTraceOn() is true.
    if (JDTrace.isTraceOn())
    {
      JDTrace.logInformation (this, text);
      if (log_ != null) log_.log(text);
    }
  }

  final void logWarning(String text)
  {
    String msg = "WARNING: " + text;
    JDTrace.logInformation (this, msg);
    if (log_ != null) log_.log(msg);
    else if (DEBUG) System.out.println(msg);
  }

  /**
   Deserializes and initializes transient data.
   @throws ClassNotFoundException If the class cannot be found.
   @throws IOException If an I/O exception occurs.
   **/
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    in.defaultReadObject();
    initializeTransient();
  }


  /**
   Sets the level of database access for the connection.
   @param access The access level.
   <p>Valid values include:
   <ul>
   <li> "all" (all SQL statements allowed)
   <li> "read call" (SELECT and CALL statements allowed)
   <li> "read only" (SELECT statements only)
   </ul>
   The default value is "all".
   **/
  public void setAccess(String access)
  {
    final String property = "access";

    if (access == null)
      throw new NullPointerException(property);
    validateProperty(property, access, JDProperties.ACCESS);

    properties_.setString(JDProperties.ACCESS, access);
    // Note: The JDProperties.setString() logs the property change.
  }


   //@AC1
   /**
   *  Sets whether auto-commit mode is the default connection mode for new connections.
   *  @param value
   *  The default value is true.
   **/
   public void setAutoCommit(boolean value)
   {
       if (value)
           properties_.setString(JDProperties.AUTO_COMMIT, TRUE_);
       else
           properties_.setString(JDProperties.AUTO_COMMIT, FALSE_);

   }
   
  //@CE1
  /**
   *  Sets whether commit or rollback throws SQLException when autocommit is enabled.
   *  @param value
   *  The default value is false.
   **/
   public void setAutocommitException(boolean value)
   {
       if (value)
           properties_.setString(JDProperties.AUTOCOMMIT_EXCEPTION, TRUE_);
       else
           properties_.setString(JDProperties.AUTOCOMMIT_EXCEPTION, FALSE_);

   }
   
  /**
   Sets whether true auto commit support is used.
   @param value true if true auto commit support should be used; false otherwise.
   The default value is false.
   **/
  public void setTrueAutoCommit(boolean value)
  {
    if (value)
      properties_.setString(JDProperties.TRUE_AUTO_COMMIT, TRUE_); //@true
    else
      properties_.setString(JDProperties.TRUE_AUTO_COMMIT, FALSE_); //@true
  }
  

  //@dup
  /**
   *  Sets whether true auto commit support is used.
   *  @param value true if true auto commit support should be used; false otherwise.
   *  The default value is false.
   *  Note:  this method is the same as setTrueAutoCommit() so that it corresponds to the connection property nameproperty name
   **/
  public void setTrueAutocommit(boolean value)
  {
      setTrueAutoCommit(value); 
  }


  /**
   Sets the Toolbox JDBC Driver behaviors to override.  Multiple
   behaviors can be changed in combination by adding
   the constants and passing that sum on the this method.
   @param behaviors The driver behaviors to override.
   <p>Valid values include:
   <ul>
   <li>1 - Do not throw an exception if Statement.executeQuery() or
   PreparedStatement.executeQuery() do not return a result set.
   Instead, return null for the result set.
   </ul>
   *
   Carefully consider the result of overriding the default behavior of the
   driver.  For example, setting the value of this property to 1 means
   the driver will no longer thrown an exception even though the JDBC 3.0
   specification states throwing an exception is the correct behavior.
   Be sure your application correctly handles the altered behavior.
   *
   **/
  public void setBehaviorOverride(int behaviors)
  {
    properties_.setString(JDProperties.BEHAVIOR_OVERRIDE, Integer.toString(behaviors));
  }

  /**
   Sets the output string type of bidi data, as defined by the CDRA (Character Data
   Representation Architecture). See <a href="BidiStringType.html">
   BidiStringType</a> for more information and valid values.
   **/
  public void setBidiStringType(int bidiStringType)
  {
    final String property = "bidiStringType";

    validateProperty(property, Integer.toString(bidiStringType), JDProperties.BIDI_STRING_TYPE);

    properties_.setString(JDProperties.BIDI_STRING_TYPE, Integer.toString(bidiStringType));
  }

  /**
   Sets whether bidi implicit reordering is used.
   @param value true if implicit reordering should be used; false otherwise.
   The default value is true.
   **/
  public void setBidiImplicitReordering(boolean value)
  {
    if (value)
      properties_.setString(JDProperties.BIDI_IMPLICIT_REORDERING, TRUE_);
    else
      properties_.setString(JDProperties.BIDI_IMPLICIT_REORDERING, FALSE_);
  }

  /**
   Sets whether bidi numeric ordering round trip is used.
   @param value true if numeric ordering round trip should be used; false otherwise.
   The default value is false.
   **/
  public void setBidiNumericOrdering(boolean value)
  {
    if (value)
      properties_.setString(JDProperties.BIDI_NUMERIC_ORDERING, TRUE_);
    else
      properties_.setString(JDProperties.BIDI_NUMERIC_ORDERING, FALSE_);
  }

  /**
   Sets whether a big decimal value is returned.
   @param value true if a big decimal is returned; false otherwise.
   The default value is true.
   **/
  public void setBigDecimal(boolean value)
  {
    if (value)
      properties_.setString(JDProperties.BIG_DECIMAL, TRUE_);
    else
      properties_.setString(JDProperties.BIG_DECIMAL, FALSE_);
  }

  /**
   Sets the criteria for retrieving data from the i5/OS system in
   blocks of records.  Specifying a non-zero value for this property
   will reduce the frequency of communication to the system, and
   therefore increase performance.
   @param blockCriteria The block criteria.
   <p>Valid values include:
   <ul>
   <li> 0 (no record blocking)
   <li> 1 (block if FOR FETCH ONLY is specified)
   <li> 2 (block if FOR UPDATE is specified) - The default value.
   </ul>
   **/
  public void setBlockCriteria(int blockCriteria)
  {
    final String property = "blockCriteria";

    validateProperty(property, Integer.toString(blockCriteria), JDProperties.BLOCK_CRITERIA);

    properties_.setString(JDProperties.BLOCK_CRITERIA, Integer.toString(blockCriteria));
  }

  /**
   Sets the block size in kilobytes to retrieve from the i5/OS system and
   cache on the client.  This property has no effect unless the block criteria
   property is non-zero.  Larger block sizes reduce the frequency of
   communication to the system, and therefore may increase performance.
   @param blockSize The block size in kilobytes.
   <p>Valid values include:
   <ul>
   <li> 0
   <li> 8
   <li> 16
   <li> 32  - The default value.
   <li> 64
   <li> 128
   <li> 256
   <li> 512
   </ul>
   **/
  public void setBlockSize(int blockSize)
  {
    final String property = "blockSize";

    validateProperty(property, Integer.toString(blockSize), JDProperties.BLOCK_SIZE);

    properties_.setString(JDProperties.BLOCK_SIZE, new Integer(blockSize).toString());
  }

  /**
   Sets the cursor sensitivity to be requested from the database.  If the resultSetType is
   ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_SENSITIVE, the value of this property
   will control what cursor sensitivity is requested from the database.  If the resultSetType
   is ResultSet.TYPE_SCROLL_INSENSITIVE, this property will be ignored.
   <p>Valid values include:
   <ul>
   <li> "asensitive"
   <li> "insensitive"
   <li> "sensitive"
   </ul>
   The default is "asensitive".
   This property is ignored when connecting to systems
   running V5R1 and earlier versions of OS/400.
   **/
  public void setCursorSensitivity(String cursorSensitivity)
  {
    final String property = "cursorSensitivity";

    validateProperty(property, cursorSensitivity, JDProperties.CURSOR_SENSITIVITY);

    properties_.setString(JDProperties.CURSOR_SENSITIVITY, cursorSensitivity);
  }


  /**
   Sets whether the cursor is held.
   @param cursorHold true if the cursor is held; false otherwise.  The default value is true.
   **/
  public void setCursorHold(boolean cursorHold)
  {
    if (cursorHold)
      properties_.setString(JDProperties.CURSOR_HOLD, TRUE_);
    else
      properties_.setString(JDProperties.CURSOR_HOLD, FALSE_);
  }

  /**
   Sets the databaseName property.
   This property is ignored when connecting to systems
   running V5R1 and earlier versions of OS/400.
   If a database name is specified it must exist in the relational
   database directory on the i5/OS system.  Use i5/OS command WRKRDBDIRE
   to view the directory.
   The following criteria are used to determine
   which database is accessed:
   <OL>
   <LI>If a database name is specified, that database is used.  Attempts
   to connect will fail if the database does not exist.
   <LI>If special value *SYSBAS is specified, the system default database is used.
   <LI>If a database name is not specified, the database specified
   in the job description for the user profile is used.
   <LI>If a database name is not specified and a database is not specified
   in the job description for the user profile, the system default
   database is used.
   </OL>
   @param databaseName The database name or *SYSBAS.
   @throws ExtendedIllegalStateException If the data source is already in use.
   **/
  public void setDatabaseName(String databaseName)
  {
    final String property = "databaseName";

    if (isInUse()) {
      logError("Data source is already in use");
      throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    if (databaseName == null)
      throw new NullPointerException(property);

    properties_.setString(JDProperties.DATABASE_NAME, databaseName);

    connectionKeyNeedsUpdate_ = true;
  }

  /**
   Sets whether to use data compression.  The default value is true.
   @param compression true if data compression is used; false otherwise.
   **/
  public void setDataCompression(boolean compression)
  {
    if (compression)
      properties_.setString(JDProperties.DATA_COMPRESSION, TRUE_);
    else
      properties_.setString(JDProperties.DATA_COMPRESSION, FALSE_);
  }

  /**
   Sets the dataSourceName property.
   This property can be used for connection pooling implementations.
   <tt>dataSourceName</tt> is assumed to be bound (via JNDI) to an instance of {@link AS400JDBCManagedConnectionPoolDataSource AS400JDBCManagedConnectionPoolDataSource}.
   <p>
   Note: The properties of the specified datasource will override all similarly-named properties of this object.  For example, if the specified datasource has a "serverName" property, then that value will be used by {@link #getConnection getConnection()}, and any value set via {@link #setServerName setServerName()} will be disregarded.
   <p>
   Note: If a dataSourceName is not specified, the {@link #getConnection getConnection()} methods will simply return non-pooled connections.
   @param dataSourceName The data source name.
   @throws ExtendedIllegalStateException If the data source is already in use.
   **/
  public void setDataSourceName(String dataSourceName)
  {
    final String property = DATASOURCE_NAME;

    if (isInUse()) {
      logError("Data source is already in use");
      throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    if (dataSourceName == null)
      throw new NullPointerException(property);

    dataSourceName_ = dataSourceName;
    dataSourceNameSpecified_ = true;

    logProperty(property, dataSourceName_);
  }

  /**
   Sets whether to use data truncation.  The default value is true.
   @param truncation true if data truncation is used; false otherwise.
   **/
  public void setDataTruncation(boolean truncation)
  {
    if (truncation)
      properties_.setString(JDProperties.DATA_TRUNCATION, TRUE_);
    else
      properties_.setString(JDProperties.DATA_TRUNCATION, FALSE_);
  }

  /**
   Sets the date format used in date literals within SQL statements.
   @param dateFormat The date format.
   <p>Valid values include:
   <ul>
   <li> "mdy"
   <li> "dmy"
   <li> "ymd"
   <li> "usa"
   <li> "iso"
   <li> "eur"
   <li> "jis"
   <li> "julian"
   <li> ""  (server job value) - default.
   </ul>
   The default is based on the server job.
   **/
  public void setDateFormat(String dateFormat)
  {
    final String property = "dateFormat";

    if (dateFormat == null)
      throw new NullPointerException(property);
    validateProperty(property, dateFormat, JDProperties.DATE_FORMAT);

    properties_.setString(JDProperties.DATE_FORMAT, dateFormat);
  }

  /**
   Sets the date separator used in date literals within SQL statements.
   This property has no effect unless the "data format" property is set to:
   "julian", "mdy", "dmy", or "ymd".
   @param dateSeparator The date separator.
   <p>Valid values include:
   <ul>
   <li> "/" (slash)
   <li> "-" (dash)
   <li> "." (period)
   <li> "," (comma)
   <li> " " (space)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public void setDateSeparator(String dateSeparator)
  {
    final String property = "dateSeparator";
    if (dateSeparator == null)
      throw new NullPointerException(property);
    validateProperty(property, dateSeparator, JDProperties.DATE_SEPARATOR);

    properties_.setString(JDProperties.DATE_SEPARATOR, dateSeparator);
  }

  //@DFA
  /**
    Sets the decfloat rounding mode.
    @param decfloatRoundingMode The decfloat rounding mode.
     <p>Valid values include:
     <ul>
     <li>"half even" - default
     <li>"half up" 
     <li>"down" 
     <li>"ceiling" 
     <li>"floor" 
     <li>"half down" 
     <li>"up" 
     </ul>
  **/
  public void setDecfloatRoundingMode(String decfloatRoundingMode)
  {
      String property = "decfloatRoundingMode";
      if (decfloatRoundingMode == null)
          throw new NullPointerException(property);
      validateProperty(property, decfloatRoundingMode, JDProperties.DECFLOAT_ROUNDING_MODE);

      String old = getDecfloatRoundingMode();

      properties_.setString(JDProperties.DECFLOAT_ROUNDING_MODE, decfloatRoundingMode);
  }
  
  /**
   Sets the decimal separator used in numeric literals within SQL
   statements.
   @param decimalSeparator The decimal separator.
   <p>Valid values include:
   <ul>
   <li> "." (period)
   <li> "," (comma)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public void setDecimalSeparator(String decimalSeparator)
  {
    final String property = "decimalSeparator";
    if (decimalSeparator == null)
      throw new NullPointerException(property);
    validateProperty(property, decimalSeparator, JDProperties.DECIMAL_SEPARATOR);

    properties_.setString(JDProperties.DECIMAL_SEPARATOR, decimalSeparator);
  }

  //@igwrn
  /**
  *  Sets the ignore warnings property.
  *  @param ignoreWarnings Specifies a list of SQL states for which the driver should not create warning objects.
  **/
  public void setIgnoreWarnings(String ignoreWarnings)
  {
      String property = "ignoreWarnings";
      if (ignoreWarnings == null)
          throw new NullPointerException(property);
 
      properties_.setString(JDProperties.IGNORE_WARNINGS, ignoreWarnings);
  }
  
  /**
   Sets the data source description.
   @param description The description.
   **/
  public void setDescription(String description)
  {
    final String property = DESCRIPTION;
    if (description == null)
      throw new NullPointerException(property);

    description_ = description;
    logProperty(property, description_);
  }

  /**
   Sets how the i5/OS system sorts records before sending them to the client.
   @param sort The sort value.
   <p>Valid values include:
   <ul>
   <li> "hex" (base the sort on hexadecimal values)
   <li> "language" (base the sort on the language set in the sort language property)
   <li> "table" (base the sort on the sort sequence table set in the sort table property).
   </ul>
   The default value is "hex".
   **/
  public void setSort(String sort)
  {
    final String property = "sort";
    if (sort == null)
      throw new NullPointerException(property);

    //@JOB fix to allow "sort=job" but use default value
    if(sort.equals("job"))                 //@JOB
    {                                      //@JOB
        if (JDTrace.isTraceOn())           //@JOB
            JDTrace.logInformation (this, property + ": " + getSort() + " (warning: " + getSort() + " will be used since sort=job is not valid)");  //@JOB 
        return; //return and allow default setting to be used                                                  //@JOB
    }                                     //@JOB
    
    validateProperty(property, sort, JDProperties.SORT);

    properties_.setString(JDProperties.SORT, sort);
  }

  /**
   Sets the amount of detail to be returned in the message for errors
   occurring on the i5/OS system.
   @param errors The error message level.
   Valid values include: "basic" and "full".  The default value is "basic".
   **/
  public void setErrors(String errors)
  {
    final String property = "errors";
    if (errors == null)
      throw new NullPointerException(property);
    validateProperty(property, errors, JDProperties.ERRORS);

    properties_.setString(JDProperties.ERRORS, errors);
  }

  /**
   Sets whether to use extended dynamic support.  Extended dynamic
   support provides a mechanism for caching dynamic SQL statements on
   the i5/OS system.  The first time a particular SQL statement is prepared, it is
   stored in an SQL package on the system.
   If the package does not exist, it will be automatically created.
   On subsequent prepares of the
   same SQL statement, the system can skip a significant part of the
   processing by using information stored in the SQL package.  If this
   is set to "true", then a package name must be set using the "package"
   property.
   @param extendedDynamic If extended dynamic support is used; false otherwise.
   The default value is not to use extended dynamic support.
   **/
  public void setExtendedDynamic(boolean extendedDynamic)
  {
    if (extendedDynamic)
      properties_.setString(JDProperties.EXTENDED_DYNAMIC, TRUE_);
    else
      properties_.setString(JDProperties.EXTENDED_DYNAMIC, FALSE_);
  }

  /**
   Sets whether the driver should request extended metadata from the
   i5/OS system.  This property is ignored when connecting to systems
   running V5R1 and earlier versions of OS/400.
   If this property is set to true and connecting to a system running
   V5R2 or later version of i5/OS, the accuracy of the information
   that is returned from ResultSetMetaData methods getColumnLabel(int),
   isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
   In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this
   property set to true.  However, performance will be slower with this
   property on.  Leave this property set to its default (false) unless you
   need more specific information from those methods.
   *
   For example, without this property turned on, isSearchable(int) will
   always return true even though the correct answer may be false because
   the driver does not have enough information from the system to make a judgment.  Setting
   this property to true forces the driver to get the correct data from the system.
   *
   @param extendedMetaData True to request extended metadata from the system, false otherwise.
   The default value is false.
   **/
  public void setExtendedMetaData(boolean extendedMetaData)
  {
    if (extendedMetaData)
      properties_.setString(JDProperties.EXTENDED_METADATA, TRUE_);
    else
      properties_.setString(JDProperties.EXTENDED_METADATA, FALSE_);
  }



  //@dup
  /**
   *  Sets whether the driver should request extended metadata from the
   *  i5/OS system.  This property is ignored when connecting to systems
   *  running OS/400 V5R1 and earlier. 
   *  If this property is set to true and connecting to a system running
   *  OS/400 V5R2 or i5/OS, the accuracy of the information 
   *  that is returned from ResultSetMetaData methods getColumnLabel(int),
   *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
   *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
   *  property set to true.  However, performance will be slower with this 
   *  property on.  Leave this property set to its default (false) unless you
   *  need more specific information from those methods.
   *
   *  For example, without this property turned on, isSearchable(int) will 
   *  always return true even though the correct answer may be false because 
   *  the driver does not have enough information from the system to make a judgment.  Setting 
   *  this property to true forces the driver to get the correct data from the system.
   *
   *  @param extendedMetaData True to request extended metadata from the system, false otherwise.
   *  The default value is false.
   *  Note:  this method is the same as setExtendedMetaData() so that it corresponds to the connection property name
   **/
  public void setExtendedMetadata(boolean extendedMetaData)
  {
      setExtendedMetaData(extendedMetaData);
  }


  /**
   Sets whether to fully open a file when performing a query.
   By default the i5/OS system optimizes opens so they perform better.
   In most cases optimization functions correctly and improves
   performance.  Running a query repeatedly
   when a database performance monitor is turned on may fail
   because of the optimization, however.
   Leave this property set to its default (false) until
   you experience errors running queries with monitors
   turned on.  At that time set the property to true which
   will disable the optimization.
   @param fullOpen True to fully open a file (turn off optimizations), false
   to allow optimizations.  The default value is false.
   **/
  public void setFullOpen(boolean fullOpen)
  {
    if (fullOpen)
      properties_.setString(JDProperties.FULL_OPEN, TRUE_);
    else
      properties_.setString(JDProperties.FULL_OPEN, FALSE_);
  }


  /**
   Sets the "hold input locators" property
   @param hold True to hold input locators.  The default value is true.
   **/
  public void setHoldInputLocators(boolean hold)
  {
    if (hold)
      properties_.setString(JDProperties.HOLD_LOCATORS, TRUE_);
    else
      properties_.setString(JDProperties.HOLD_LOCATORS, FALSE_);
  }


  /**
   Sets the "hold statements" property
   @param hold True to hold statements.  The default value is false.
   **/
  public void setHoldStatements(boolean hold)
  {
    if (hold)
      properties_.setString(JDProperties.HOLD_STATEMENTS, TRUE_);
    else
      properties_.setString(JDProperties.HOLD_STATEMENTS, FALSE_);
  }


  /**
   Sets whether to delay closing cursors until subsequent requests.
   @param lazyClose true to delay closing cursors until subsequent requests; false otherwise.
   The default value is false.
   **/
  public void setLazyClose(boolean lazyClose)
  {
    if (lazyClose)
      properties_.setString(JDProperties.LAZY_CLOSE, TRUE_);
    else
      properties_.setString(JDProperties.LAZY_CLOSE, FALSE_);
  }

  /**
   Sets the libraries to add to the server job's library list.
   The libraries are delimited by commas or spaces, and
   "*LIBL" may be used as a place holder for the server job's
   current library list.  The library list is used for resolving
   unqualified stored procedure calls and finding schemas in
   DatabaseMetaData catalog methods.  If "*LIBL" is not specified,
   the specified libraries will replace the server job's
   current library list.
   @param libraries The library list.
   **/
  public void setLibraries(String libraries)
  {
    final String property = "libraries";
    if (libraries == null)
      throw new NullPointerException(property);
    properties_.setString(JDProperties.LIBRARIES, libraries);
  }

  /**
   Sets the maximum LOB (large object) size in bytes that
   can be retrieved as part of a result set.  LOBs that are larger
   than this threshold will be retrieved in pieces using extra
   communication to the i5/OS system.  Larger LOB thresholds will reduce
   the frequency of communication to the system, but will download
   more LOB data, even if it is not used.  Smaller LOB thresholds may
   increase frequency of communication to the system, but will only
   download LOB data as it is needed.
   *
   @param threshold The lob threshold.  Valid range is 0-16777216.
   The default value is 32768.
   **/
  public void setLobThreshold(int threshold)
  {
    final String property = "threshold";
    if (threshold < 0 || threshold > MAX_THRESHOLD)
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    properties_.setString(JDProperties.LOB_THRESHOLD, new Integer(threshold).toString());
  }

  // method required by javax.sql.DataSource
  /**
   Sets the maximum time in seconds that this data source can wait while attempting to connect to a database.
   A value of zero specifies that the timeout is the system default if one exists; otherwise it specifies that
   there is no timeout. The default value is initially zero.
   <br><i>Note: This value is not used or supported by the Toolbox JDBC driver.</i>
   Rather, the timeout value is determined by the i5/OS system.
   @param timeout The login timeout in seconds.
   **/
  public void setLoginTimeout(int timeout) throws SQLException
  {
    //This sets the socket timeout
    setSoTimeout(timeout * 1000);

    properties_.setString(JDProperties.LOGIN_TIMEOUT, Integer.toString(timeout));
  }

  // method required by javax.sql.DataSource
  /**
   Sets the log writer for this data source.
   <p><i>Note:</i> The specified PrintWriter might not be retained when an object is obtained via JNDI, that is, by a call to <tt>javax.naming.Context.lookup()</tt>.  Therefore, use this method <i>only</i> on the DataSource object that is used directly by your application (rather than on the "template" DataSource object that was bound in JNDI).
   @param writer The log writer; to disable, set to null.
   @throws SQLException If a database error occurs.
   **/
  public void setLogWriter(PrintWriter writer) throws SQLException
  {
    writer_ = writer;
    logProperty("logWriter", (writer_ == null ? "null" : writer_.toString()));

    if (writer == null)
    {
      log_ = null;
    }
    else
    {
      log_ = new EventLog(writer);
    }
  }
  
  //@PDA
  /**                                                               
  *  Sets how to retrieve DatabaseMetaData.
  *  If set to 0, database metadata will be retrieved through the ROI data flow.  
  *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
  *  The methods that currently are available through stored procedures are:
  *  getColumnPrivileges
  *  @param mds The setting for metadata source
  *  The default value is 1.
  **/
  public void setMetaDataSource(int mds)
  {
      Integer newValue = new Integer(mds);

      properties_.setString(JDProperties.METADATA_SOURCE, newValue.toString());

  }
  
  //@dup
  /**                                                               
   *  Sets how to retrieve DatabaseMetaData.
   *  If set to 0, database metadata will be retrieved through the ROI data flow.  
   *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
   *  The methods that currently are available through stored procedures are:
   *  getColumnPrivileges
   *  @param mds The setting for metadata source
   *  The default value is 1.
   *  Note:  this method is the same as setMetaDataSource() so that it corresponds to the connection property name
   **/
  public void setMetadataSource(int mds)
  {
      setMetaDataSource(mds);
  }

  
  /**
   Sets the naming convention used when referring to tables.
   @param naming The naming convention.  Valid values include: "sql" (e.g. schema.table)
   and "system" (e.g. schema/table).  The default value is "sql".
   **/
  public void setNaming(String naming)
  {
    final String property = "naming";
    if (naming == null)
      throw new NullPointerException(property);
    validateProperty(property, naming, JDProperties.NAMING);

    properties_.setString(JDProperties.NAMING, naming);
  }

  /**
   Sets the base name of the SQL package.  Note that only the
   first seven characters are used to generate the name of the SQL package on the i5/OS system.
   This property has no effect unless
   the extended dynamic property is set to true.  In addition, this property
   must be set if the extended dynamic property is set to true.
   @param packageName The base name of the SQL package.
   **/
  public void setPackage(String packageName)
  {
    final String property = "packageName";
    if (packageName == null)
      throw new NullPointerException(property);

    properties_.setString(JDProperties.PACKAGE, packageName);
  }

  /**
   Sets whether to add newly prepared statements to the SQL package
   specified on the "package" property.  This property
   has no effect unless the extended dynamic property is set to true.
   @param add True if newly prepared statements should be added to the SQL package specified on
   the "package" property; false otherwise.
   The default value is true.
   **/
  public void setPackageAdd(boolean add)
  {
    if (add)
      properties_.setString(JDProperties.PACKAGE_ADD, TRUE_);
    else
      properties_.setString(JDProperties.PACKAGE_ADD, FALSE_);
  }

  /**
   Sets whether to cache a subset of the SQL package information in client memory.
   Caching SQL packages locally
   reduces the amount of communication to the i5/OS system for prepares and describes.  This
   property has no effect unless the extended dynamic property is set to true.
   @param cache True if caching is used; false otherwise.  The default value is false.
   **/
  public void setPackageCache(boolean cache)
  {
    if (cache)
      properties_.setString(JDProperties.PACKAGE_CACHE, TRUE_);
    else
      properties_.setString(JDProperties.PACKAGE_CACHE, FALSE_);
  }

  /**
   Sets whether to clear SQL packages when they become full.  This method
   has been deprecated.  Package clearing and the decision for the 
   threshold where package clearing is needed is now handled
   automatically by the database.  
   @param clear If the SQL package are cleared when full; false otherwise.
   @deprecated
   **/
  public void setPackageClear(boolean clear)
  {
  }

  /**
   Sets the type of SQL statement to be stored in the SQL package.  This can
   be useful to improve the performance of complex join conditions.  This
   property has no effect unless the extended dynamic property is set to true.
   @param packageCriteria The type of SQL statement.
   Valid values include: "default" (only store SQL statements with parameter
   markers in the package), and "select" (store all SQL SELECT statements
   in the package).  The default value is "default".
   **/
  public void setPackageCriteria(String packageCriteria)
  {
    final String property = "packageCriteria";

    if (packageCriteria == null)
      throw new NullPointerException(property);
    validateProperty(property, packageCriteria, JDProperties.PACKAGE_CRITERIA);

    properties_.setString(JDProperties.PACKAGE_CRITERIA, packageCriteria);
  }

  /**
   Sets the action to take when SQL package errors occur.  When an SQL package
   error occurs, the driver will optionally throw an SQLException or post a
   warning to the Connection, based on the value of this property.  This property
   has no effect unless the extended dynamic property is set to true.
   @param packageError The action when SQL errors occur.
   Valid values include: "exception", "warning", and "none".  The default value is "warning".
   **/
  public void setPackageError(String packageError)
  {
    final String property = "packageError";
    if (packageError == null)
      throw new NullPointerException(property);
    validateProperty(property, packageError, JDProperties.PACKAGE_ERROR);

    properties_.setString(JDProperties.PACKAGE_ERROR, packageError);
  }

  /**
   Sets the library for the SQL package.  This property has no effect unless
   the extended dynamic property is set to true.
   @param packageLibrary The SQL package library.  The default package library is "QGPL".
   **/
  public void setPackageLibrary(String packageLibrary)
  {
    final String property = "packageLibrary";
    if (packageLibrary == null)
      throw new NullPointerException(property);

    properties_.setString(JDProperties.PACKAGE_LIBRARY, packageLibrary);
  }

  /**
   Sets the 'password' property.
   @param password The password.
   **/
  public void setPassword(String password)
  {
    final String property = "password";
    if (password == null)
      throw new NullPointerException(property);

    char[] newSerialPWBytes = xpwConfuse(password);
    if (!Arrays.equals(newSerialPWBytes, serialPWBytes_)) {
      as400_.setPassword(password);
      invalidate(getUser(), serialPWBytes_);  // invalidate any pooled connections with old password
      serialPWBytes_ = newSerialPWBytes;
      pwHashcode_    = password.hashCode();

      connectionKeyNeedsUpdate_ = true;
      // Note: We deliberately do _not_ store the password into properties_.
    }
    logInformation(loader_.getText("AS400_JDBC_DS_PASSWORD_SET"));
    logProperty(property, "***");
  }

  /**
   Sets whether to prefetch data upon executing a SELECT statement.
   This will increase performance when accessing the initial rows in the result set.
   @param prefetch If prefetch is used; false otherwise.
   The default value is to prefectch data.
   **/
  public void setPrefetch(boolean prefetch)
  {
    if (prefetch)
      properties_.setString(JDProperties.PREFETCH, TRUE_);
    else
      properties_.setString(JDProperties.PREFETCH, FALSE_);
  }

  /**
   Sets whether the user should be prompted if a user name or password is
   needed to connect to the i5/OS system.  If a connection can not be made
   without prompting the user, and this property is set to false, then an
   attempt to connect will fail.
   @param prompt true if the user is prompted for signon information; false otherwise.
   The default value is false.
   **/
  public void setPrompt(boolean prompt)
  {
    if (prompt)
      properties_.setString(JDProperties.PROMPT, TRUE_);
    else
      properties_.setString(JDProperties.PROMPT, FALSE_);

    try
    {
      as400_.setGuiAvailable(prompt);
    }
    catch (PropertyVetoException vp) {} // this will never happen
  }

  //@PDA
  /**
   * Sets the properties based on ";" delimited string of properties, in same
   * fashion as URL properties specified with
   * DriverManager.getConnection(urlProperties). This method simply parses
   * property string and then calls setPropertes(Properties). This method is
   * intended as an enhancement so that the user does not have to write new
   * code to call the setters for new/deleted properties.
   * 
   * @param propertiesString list of ";" delimited properties
   */
  public void setProperties(String propertiesString)
  {
      //use existing JDDatasourceURL to parse properties string like Connection does
      //but first have to add dummy protocol so we can re-use parsing code
      propertiesString = "jdbc:as400://dummyhost;" + propertiesString;
      JDDataSourceURL dsURL = new JDDataSourceURL(propertiesString);
      //returns only properties specified in propertyString.. (none of
      // JDProperties defaults)
      Properties properties = dsURL.getProperties();
      setProperties(properties);
  }

  //@PDA
  /**
   * Sets the properties for this datasource. This method is intended as an
   * enhancement so that the user does not have to write new code to call the
   * setters for new/deleted properties.
   * 
   * @param newProperties object containing updated property values
   */
  public void setProperties(Properties newProperties)
  {
      //1. turn on/off tracing per new props
      //2. set needed AS400JDBCManagedDataSource instance variables
      //3. set socket props
      //4. propagate newProperties to existing properties_ object

      // Check first thing to see if the trace property is
      // turned on. This way we can trace everything, including
      // the important stuff like loading the properties.

      // If trace property was set to true, turn on tracing. If trace property
      // was set to false,
      // turn off tracing. If trace property was not set, do not change.
      if (JDProperties.isTraceSet(newProperties, null) == JDProperties.TRACE_SET_ON)
      {
          if (!JDTrace.isTraceOn())
              JDTrace.setTraceOn(true);
      } else if (JDProperties.isTraceSet(newProperties, null) == JDProperties.TRACE_SET_OFF)
      {
          if (JDTrace.isTraceOn())
              JDTrace.setTraceOn(false);
      }

      // If toolbox trace is set to datastream. Turn on datastream tracing.
      if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_DATASTREAM)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceDatastreamOn(true);
      }
      // If toolbox trace is set to diagnostic. Turn on diagnostic tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_DIAGNOSTIC)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceDiagnosticOn(true);
      }
      // If toolbox trace is set to error. Turn on error tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_ERROR)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceErrorOn(true);
      }
      // If toolbox trace is set to information. Turn on information tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_INFORMATION)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceInformationOn(true);
      }
      // If toolbox trace is set to warning. Turn on warning tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_WARNING)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceWarningOn(true);
      }
      // If toolbox trace is set to conversion. Turn on conversion tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_CONVERSION)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceConversionOn(true);
      }
      // If toolbox trace is set to proxy. Turn on proxy tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_PROXY)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceProxyOn(true);
      }
      // If toolbox trace is set to pcml. Turn on pcml tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_PCML)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTracePCMLOn(true);
      }
      // If toolbox trace is set to jdbc. Turn on jdbc tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_JDBC)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceJDBCOn(true);
      }
      // If toolbox trace is set to all. Turn on tracing for all categories.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_ALL)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceAllOn(true);
      }
      // If toolbox trace is set to thread. Turn on thread tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_THREAD)
      {
          if (!Trace.isTraceOn())
          {
              Trace.setTraceOn(true);
          }
          Trace.setTraceThreadOn(true);
      }
      // If toolbox trace is set to none. Turn off tracing.
      else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_NONE)
      {
          if (Trace.isTraceOn())
          {
              Trace.setTraceOn(false);
          }
      }

      //next we need to set instance vars (via setX() methods)
      //or setup socket properties or set in properties_
      //Note: this is similar to AS400JDBCManagedDataSource(Reference reference)logic

      Enumeration e = newProperties.keys();
      while (e.hasMoreElements())
      {
          String propertyName = (String) e.nextElement();
          String propertyValue = (String) newProperties.getProperty(propertyName);

          int propIndex = JDProperties.getPropertyIndex(propertyName);

          //some of the setter methods also set the properties_ below
          if (propIndex == JDProperties.DATABASE_NAME)
              setDatabaseName(propertyValue);
          else if (propIndex == JDProperties.USER)
              setUser(propertyValue);
          else if (propIndex == JDProperties.PASSWORD)
              setPassword(properties_.getString(JDProperties.PASSWORD));
          else if (propIndex == JDProperties.SECURE)
              setSecure(propertyValue.equals(TRUE_) ? true : false);
          else if (propIndex == JDProperties.KEEP_ALIVE)
              setKeepAlive(propertyValue.equals(TRUE_) ? true : false);
          else if (propIndex == JDProperties.RECEIVE_BUFFER_SIZE)
              setReceiveBufferSize(Integer.parseInt(propertyValue));
          else if (propIndex == JDProperties.SEND_BUFFER_SIZE)
              setSendBufferSize(Integer.parseInt(propertyValue));
          else if (propIndex == JDProperties.PROMPT)
              setPrompt(propertyValue.equals(TRUE_) ? true : false);
          else if (propIndex == JDProperties.KEY_RING_NAME){
              //at this time, decided to not allow this due to security and fact that there is no setKeyRingName() method
              if (JDTrace.isTraceOn())
                  JDTrace.logInformation(this, "Property: " + propertyName + " can only be changed in AS400JDBCManagedDataSource constructor");  
          } else if (propIndex == JDProperties.KEY_RING_PASSWORD){
              //at this time, decided to not allow this due to security and fact that there is no setKeyRingPassword() method
              if (JDTrace.isTraceOn())
                  JDTrace.logInformation(this, "Property: " + propertyName + " can only be changed in AS400JDBCManagedDataSource constructor");  
          } else if (propIndex != -1)
          {
              properties_.setString(propIndex, propertyValue);
          }
      } 
  }
  
 
  /**
   Sets the name of the proxy server.
   @param proxyServer The proxy server.
   **/
  public void setProxyServer(String proxyServer)
  {
    final String property = "proxyServer";
    if (proxyServer == null)
      throw new NullPointerException(property);

    properties_.setString(JDProperties.PROXY_SERVER, proxyServer);
  }

  /**
   Sets the "query optimize goal" property
   @param goal - the optimization goal 
   <p>Valid values include:
   <ul>
   <li>0 = Optimize query for first block of data (*ALLIO) when extended dynamic packages are used; Optimize query for entire result set (*FIRSTIO) when packages are not used</li>
   <li>1 = Optimize query for first block of data (*FIRSTIO)</li>
   <li>2 = Optimize query for entire result set (*ALLIO) </li>
   </ul>
   The default value is 0.
   **/
  public void setQueryOptimizeGoal(int goal)
  {
    String property = "queryOptimizeGoal";
    validateProperty(property, Integer.toString(goal), JDProperties.QUERY_OPTIMIZE_GOAL);

    properties_.setString(JDProperties.QUERY_OPTIMIZE_GOAL, Integer.toString(goal));
  }

  //@550
    /**
    * Sets the storage limit in megabytes, that should be used for statements executing a query in a connection.
    * Note, this setting is ignored when running to V5R4 i5/OS or earlier
    * @param limit - the storage limit (in megabytes)
    * <p> Valid values are -1 to MAX_STORAGE_LIMIT megabytes.  
    * The default value is -1 meaning there is no limit.
    **/
    public void setQueryStorageLimit(int limit)
    {
        String property = "queryStorageLimit";

        if (limit < -1 || limit > MAX_STORAGE_LIMIT)
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        properties_.setString(JDProperties.QUERY_STORAGE_LIMIT, Integer.toString(limit));
    }

  /**
   Sets the source of the text for REMARKS columns in ResultSets returned
   by DatabaseMetaData methods.
   @param remarks The text source.
   Valid values include: "sql" (SQL object comment) and "system" (i5/OS object description).
   The default value is "system".
   **/
  public void setRemarks(String remarks)
  {
    final String property = "remarks";
    if (remarks == null)
      throw new NullPointerException(property);
    validateProperty(property, remarks, JDProperties.REMARKS);

    properties_.setString(JDProperties.REMARKS, remarks);
  }

  /**
   Sets the "rollback cursor hold" property
   @param hold True to hold cursor across rollbacks.  The default value is false.
   **/
  public void setRollbackCursorHold(boolean hold)
  {
    if (hold)
      properties_.setString(JDProperties.ROLLBACK_CURSOR_HOLD, TRUE_);
    else
      properties_.setString(JDProperties.ROLLBACK_CURSOR_HOLD, FALSE_);
  }

  /**
   Sets the secondary URL to be used for a connection on the middle-tier's
   DriverManager in a multiple tier environment, if it is different than
   already specified.  This property allows you to use this driver to connect
   to databases other than DB2 for i5/OS. Use a backslash as an escape character
   before backslashes and semicolons in the URL.
   @param url The secondary URL.
   **/
  public void setSecondaryUrl(String url)
  {
    if (url == null)
      throw new NullPointerException("url");

    properties_.setString(JDProperties.SECONDARY_URL, url);
  }
  
  //@dup
  /**
   *  Sets the secondary URL to be used for a connection on the middle-tier's
   *  DriverManager in a multiple tier environment, if it is different than
   *  already specified.  This property allows you to use this driver to connect
   *  to databases other than DB2 for i5/OS. Use a backslash as an escape character
   *  before backslashes and semicolons in the URL.
   *  @param url The secondary URL.
   *  Note:  this method is the same as setSecondaryUrl() so that it corresponds to the connection property name
   **/
  public void setSecondaryURL(String url)
  {
      setSecondaryUrl(url);
  }
  

  /**
   Sets whether a Secure Socket Layer (SSL) connection is used to communicate
   with the i5/OS system.  SSL connections are only available when connecting to systems
   at V4R4 or later.
   @param secure true if Secure Socket Layer connection is used; false otherwise.
   The default value is false.
   @throws ExtendedIllegalStateException If the data source was constructed with a keyring and <tt>secure</tt> is false.
   **/
  public void setSecure(boolean secure)
  {
    // Do not allow user to change to not secure if they constructed the data source with
    // a keyring.
    if (!secure && isSecure_)
    {
      throw new ExtendedIllegalStateException("secure",
                                              ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Keep local copy for convenience.
    isSecure_ = secure;

    if (secure)
      properties_.setString(JDProperties.SECURE, TRUE_);
    else
      properties_.setString(JDProperties.SECURE, FALSE_);
  }


  //@pw3
  /**
   *  Sets whether to disallow "" and *current as user name and password.  
   *  True indicates to disallow "" and *current for user name and password.
   *  @parm The secure current user setting.
   **/
  public void setSecureCurrentUser(boolean secureCurrentUser)
  {
      if (secureCurrentUser)
          properties_.setString(JDProperties.SECURE_CURRENT_USER, TRUE_);
      else
          properties_.setString(JDProperties.SECURE_CURRENT_USER, FALSE_);
  }
  
 
  /**
   Sets the serverName property.
   @param serverName The system name.
   @throws ExtendedIllegalStateException If the data source is already in use.
   **/
  public void setServerName(String serverName)
  {
    final String property = SERVER_NAME;

    if (isInUse()) {
      logError("Data source is already in use");
      throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    if (serverName == null)
      throw new NullPointerException(property);

    // Save away the name to serialize.
    serialServerName_ = serverName;  // Note: There is no associated JDProperties entry.

    try
    {
      as400_.setSystemName(serverName);
    }
    catch (PropertyVetoException pv) {} // will never happen

    connectionKeyNeedsUpdate_ = true;

    logProperty(property, as400_.getSystemName());
  }


  /**
   Enables tracing of the JDBC server job.
   If tracing is enabled, tracing is started when
   the client connects to the i5/OS system, and ends when the connection
   is disconnected.  Tracing must be started before connecting to
   the system since the client enables tracing only at connect time.
   *
   <P>
   Trace data is collected in spooled files on the system.  Multiple
   levels of tracing can be turned on in combination by adding
   the constants and passing that sum on the set method.  For example,
   <pre>
   dataSource.setServerTraceCategories(AS400JDBCManagedDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCManagedDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
   </pre>
   @param traceCategories level of tracing to start.
   <p>Valid values include:
   <ul>
   <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
   The numeric value of this constant is 2.
   <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
   The numeric value of this constant is 4.
   <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
   The numeric value of this constant is 8.
   <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
   The numeric value of this constant is 16.
   <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
   The numeric value of this constant is 32.
   </ul>
   <P>
   Tracing the JDBC server job will use significant amounts of system resources.
   Additional processor resource is used to collect the data, and additional
   storage is used to save the data.  Turn on tracing only to debug
   a problem as directed by IBM service.
   *
   *
   **/
  public void setServerTraceCategories(int traceCategories)
  {
    properties_.setString(JDProperties.TRACE_SERVER, Integer.toString(traceCategories));
  }

  /**
   Enables tracing of the JDBC server job.
   If tracing is enabled, tracing is started when
   the client connects to the i5/OS system, and ends when the connection
   is disconnected.  Tracing must be started before connecting to
   the system since the client enables tracing only at connect time.
   *
   <P>
   Trace data is collected in spooled files on the system.  Multiple
   levels of tracing can be turned on in combination by adding
   the constants and passing that sum on the set method.  For example,
   <pre>
   dataSource.setServerTraceCategories(AS400JDBCManagedDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCManagedDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
   </pre>
   @param traceCategories level of tracing to start.
   <p>Valid values include:
   <ul>
   <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
   The numeric value of this constant is 2.
   <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
   The numeric value of this constant is 4.
   <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
   The numeric value of this constant is 8.
   <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
   The numeric value of this constant is 16.
   <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
   The numeric value of this constant is 32.
   </ul>
   <P>
   Tracing the JDBC server job will use significant amounts of system resources.
   Additional processor resource is used to collect the data, and additional
   storage is used to save the data.  Turn on tracing only to debug
   a problem as directed by IBM service.
   * Note:  this method is the same as setServerTraceCategories() so that it corresponds to the connection property name
   **/
  public void setServerTrace(int traceCategories)
  {
    setServerTraceCategories(traceCategories);
  }
 
  /**
   Sets the JDBC driver implementation.
   This property has no
   effect if the "secondary URL" property is set.
   This property cannot be set to "native" if the
   environment is not an i5/OS Java Virtual
   Machine.
   param driver The driver value.
   <p>Valid values include:
   <ul>
   <li>"toolbox" (use the IBM Toolbox for Java JDBC driver)
   <li>"native" (use the IBM Developer Kit for Java JDBC driver)
   </ul>
   The default value is "toolbox".
   **/
  public void setDriver(String driver)
  {
    final String property = "driver";
    if (driver == null)
      throw new NullPointerException(property);

    validateProperty(property, driver, JDProperties.DRIVER);

    properties_.setString(JDProperties.DRIVER, driver);
  }

  /**
   Sets whether to save the password locally with the rest of the properties when
   this data source object is serialized.
   <P>
   If the password is saved, it is up to the application to protect
   the serialized form of the object because it contains all necessary
   information to connect to the i5/OS system.  The default is false.  It
   is a security risk to save the password with the rest of the
   properties so by default the password is not saved.  If the application
   programmer chooses to accept this risk, set this property to true
   to force the Toolbox to save the password with the other properties
   when the data source object is serialized.
   *
   @param savePassword true if the password is saved; false otherwise.
   The default value is false
   **/
  public void setSavePasswordWhenSerialized(boolean savePassword)
  {
    final String property = "savePasswordWhenSerialized";

    savePasswordWhenSerialized_ = savePassword;

    logProperty(property, Boolean.toString(savePasswordWhenSerialized_));
  }


  /**
   Sets the three-character language id to use for selection of a sort sequence.
   This property has no effect unless the sort property is set to "language".
   @param language The three-character language id.
   The default value is ENU.
   **/
  public void setSortLanguage(String language)
  {
    if (language == null)
      throw new NullPointerException("language");

    properties_.setString(JDProperties.SORT_LANGUAGE, language);
  }

  /**
   Sets the library and file name of a sort sequence table stored on the
   i5/OS system.
   This property has no effect unless the sort property is set to "table".
   The default is an empty String ("").
   @param table The qualified sort table name.
   **/
  public void setSortTable(String table)
  {
    if (table == null)
      throw new NullPointerException("table");

    properties_.setString(JDProperties.SORT_TABLE, table);
  }

  /**
   Sets how the i5/OS system treats case while sorting records.  This property
   has no effect unless the sort property is set to "language".
   @param sortWeight The sort weight.
   Valid values include: "shared" (upper- and lower-case characters are sorted as the
   same character) and "unique" (upper- and lower-case characters are sorted as
   different characters).  The default value is "shared".
   **/
  public void setSortWeight(String sortWeight)
  {
    final String property = "sortWeight";
    if (sortWeight == null)
      throw new NullPointerException(property);

    validateProperty(property, sortWeight, JDProperties.SORT_WEIGHT);

    properties_.setString(JDProperties.SORT_WEIGHT, sortWeight);
  }

  /**
   Sets whether a thread is used.
   @param threadUsed true if a thread is used; false otherwise.
   The default value is true.
   **/
  public void setThreadUsed(boolean threadUsed)
  {
    if (threadUsed)
      properties_.setString(JDProperties.THREAD_USED, TRUE_);
    else
      properties_.setString(JDProperties.THREAD_USED, FALSE_);
  }

  /**
   Sets the time format used in time literals with SQL statements.
   @param timeFormat The time format.
   <p>Valid values include:
   <ul>
   <li> "hms"
   <li> "usa"
   <li> "iso"
   <li> "eur"
   <li> "jis"
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public void setTimeFormat(String timeFormat)
  {
    final String property = "timeFormat";
    if (timeFormat == null)
      throw new NullPointerException(property);
    validateProperty(property, timeFormat, JDProperties.TIME_FORMAT);

    properties_.setString(JDProperties.TIME_FORMAT, timeFormat);
  }

  /**
   Sets the time separator used in time literals within SQL statements.
   This property has no effect unless the time format property is set to "hms".
   @param timeSeparator The time separator.
   <p>Valid values include:
   <ul>
   <li> ":" (colon)
   <li> "." (period)
   <li> "," (comma)
   <li> " " (space)
   <li> ""  (server job value) - default.
   </ul>
   The default value is based on the server job.
   **/
  public void setTimeSeparator(String timeSeparator)
  {
    final String property = "timeSeparator";
    if (timeSeparator == null)
      throw new NullPointerException(property);
    validateProperty(property, timeSeparator, JDProperties.TIME_SEPARATOR);

    properties_.setString(JDProperties.TIME_SEPARATOR, timeSeparator);
  }

  /**
   Sets whether trace messages should be logged.  Trace messages are
   useful for debugging programs that call JDBC.  However, there is a
   performance penalty associated with logging trace messages, so this
   property should only be set to true for debugging.  Trace messages
   are logged to System.out.
   @param trace true if trace message are logged; false otherwise.
   The default value is false.
   **/
  public void setTrace(boolean trace)
  {
    if (trace)
      properties_.setString(JDProperties.TRACE, TRUE_);
    else
      properties_.setString(JDProperties.TRACE, FALSE_);

    if (trace)
    {
      if (!JDTrace.isTraceOn ())
        JDTrace.setTraceOn (true);
    }
    else
      JDTrace.setTraceOn (false);
  }


  /**
   Sets the i5/OS system's transaction isolation.
   @param transactionIsolation The transaction isolation level.
   <p>Valid values include:
   <ul>
   <li> "none"
   <li> "read uncommitted"  - The default value.
   <li> "read committed"
   <li> "repeatable read"
   <li> "serializable"
   </ul>
   **/
  public void setTransactionIsolation(String transactionIsolation)
  {
    final String property = "transactionIsolation";

    if (transactionIsolation == null)
      throw new NullPointerException(property);
    validateProperty(property, transactionIsolation, JDProperties.TRANSACTION_ISOLATION);

    properties_.setString(JDProperties.TRANSACTION_ISOLATION, transactionIsolation);
  }

  /**
   Sets whether binary data is translated.  If this property is set
   to true, then BINARY and VARBINARY fields are treated as CHAR and
   VARCHAR fields.
   @param translate true if binary data is translated; false otherwise.
   The default value is false.
   **/
  public void setTranslateBinary(boolean translate)
  {
    if (translate)
      properties_.setString(JDProperties.TRANSLATE_BINARY, TRUE_);
    else
      properties_.setString(JDProperties.TRANSLATE_BINARY, FALSE_);
  }

  //@PDA
  /**
  *  Sets how Boolean objects are interpreted when setting the value 
  *  for a character field/parameter using the PreparedStatement.setObject(), 
  *  CallableStatement.setObject() or ResultSet.updateObject() methods.  Setting the 
  *  property to "true", would store the Boolean object in the character field as either 
  *  "true" or "false".  Setting the property to "false", would store the Boolean object 
  *  in the character field as either "1" or "0".
  *  @param translate if boolean data is translated; false otherwise.
  *  The default value is true.
  **/
  public void setTranslateBoolean(boolean translate)
  {
      if (translate)
          properties_.setString(JDProperties.TRANSLATE_BOOLEAN, TRUE_);
      else
          properties_.setString(JDProperties.TRANSLATE_BOOLEAN, FALSE_);
  }
  
  
  /**
   Sets the 'user' property.
   @param user The user.
   @throws ExtendedIllegalStateException If the data source is already in use.
   **/
  public void setUser(String user)
  {
    final String property = "user";

    if (isInUse()) {
      logError("Data source is already in use");
      throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // save away the user to serialize
    serialUserName_ = user;

    properties_.setString(JDProperties.USER, user);

    try {
      as400_.setUserId(user);
    }
    catch (PropertyVetoException vp) {} // this will never happen

    connectionKeyNeedsUpdate_ = true;
  }

  /**                                                               
   Sets whether lock sharing is allowed for loosely coupled transaction branches.
   Note, this setting is ignored when running to V5R3 i5/OS or earlier.  
   @param lcs - the "XA loosely coupled support" setting 
   <p>Valid values include:
   <ul>
   <li>0 = Locks cannot be shared</li>
   <li>1 = Locks can be shared</li>
   </ul>
   The default value is 0.
   **/
  public void setXALooselyCoupledSupport(int lcs)
  {
    String property = "xaLooselyCoupledSupport";
    validateProperty(property, Integer.toString(lcs), JDProperties.XA_LOOSELY_COUPLED_SUPPORT);

    properties_.setString(JDProperties.XA_LOOSELY_COUPLED_SUPPORT, Integer.toString(lcs));
  }


  /**
   Gets the socket 'keepalive' option.
   @return The value of the keepalive option.
   **/
  public boolean isKeepAlive()
  {
    return sockProps_.isKeepAlive();
  }

  /**
   Gets the socket 'receive buffer size' option.  NOTE: This does not get
   the actual receive buffer size, only the option which is used as a hint
   by the underlying socket code.
   @return The value of the receive buffer size option.
   **/
  public int getReceiveBufferSize()
  {
    return sockProps_.getReceiveBufferSize();
  }

  /**
   Gets the socket 'send buffer size' option.  NOTE: This does not get
   the actual send buffer size, only the option which is used as a hint
   by the underlying socket code.
   @return The value of the send buffer size option.
   **/
  public int getSendBufferSize()
  {
    return sockProps_.getSendBufferSize();
  }

  /**
   Gets the socket 'linger' option, in milliseconds.
   @return The value of the linger option.
   **/
  public long getSoLinger()
  {
    return (long)(1000*sockProps_.getSoLinger());
  }

  /**
   Gets the socket 'timeout' option in milliseconds.
   @return The value of the timeout option.
   **/
  public long getSoTimeout()
  {
    return (long)sockProps_.getSoTimeout();
  }

  /**
   Gets the socket 'TCP no delay' option.
   @return The value of the TCP no delay option.
   **/
  public boolean isTcpNoDelay()
  {
    return sockProps_.isTcpNoDelay();
  }

  /**
   Turns on the socket 'keepAlive' property.
   @param keepAlive The keepalive option value.
   **/
  public void setKeepAlive(boolean keepAlive)
  {
    sockProps_.setKeepAlive(keepAlive);
    logProperty("keepAlive", Boolean.toString(keepAlive));
  }

  /**
   Sets the socket 'receive buffer size' option to the
   specified value. The receive buffer size option is used as a hint
   for the size to set the underlying network I/O buffers. Increasing
   the receive buffer size can increase the performance of network
   I/O for high-volume connection, while decreasing it can help reduce
   the backlog of incoming data.  This value must be greater than 0.
   @param size The receive buffer size option value.
   **/
  public void setReceiveBufferSize(int size)
  {
    sockProps_.setReceiveBufferSize(size);
    logProperty("receiveBufferSize", Integer.toString(size));
  }

  /**
   Sets the socket 'send buffer size' option to the
   specified value. The send buffer size option is used by the
   platform's networking code as a hint for the size to set the
   underlying network I/O buffers.  This value must be greater
   than 0.
   @param size The send buffer size option value.
   **/
  public void setSendBufferSize(int size)
  {
    sockProps_.setSendBufferSize(size);
    logProperty("sendBufferSize", Integer.toString(size));
  }

  /**
   Sets the socket 'linger' property to the
   specified linger time in milliseconds.  The maximum value for this
   property is platform specific.
   @param milliseconds The linger option value.
   **/
  public void setSoLinger(long milliseconds)
  {
    final String property = "soLinger";

    if (milliseconds > Integer.MAX_VALUE)
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    sockProps_.setSoLinger((int)(milliseconds/1000));  // called method expects seconds
    logProperty(property, Long.toString(milliseconds));
  }

  /**
   Enables/disables socket timeout with the
   specified value in milliseconds.  A timeout value must be
   greater than zero, a value of zero for this property indicates
   infinite timeout.
   @param milliseconds The timeout option value.
   **/
  public void setSoTimeout(long milliseconds)
  {
    final String property = "soTimeout";

    if (milliseconds > Integer.MAX_VALUE)
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    sockProps_.setSoTimeout((int)milliseconds);
    logProperty(property, Long.toString(milliseconds));
  }

  /**
   Sets the socket 'TCP no delay' option.
   @param noDelay The TCP no delay option value.
   **/
  public void setTcpNoDelay(boolean noDelay)
  {
    sockProps_.setTcpNoDelay(noDelay);
    logProperty("tcpNoDelay", Boolean.toString(noDelay));
  }

  /**
   Gets the package CCSID property, which indicates the
   CCSID in which statements are sent to the i5/OS system and
   also the CCSID of the package they are stored in.
   Valid values:  1200 (UCS-2) and 13488 (UTF-16).  Default value: 13488
   @return The value of the package CCSID property.
   **/
  public int getPackageCCSID()
  {
    return properties_.getInt(JDProperties.PACKAGE_CCSID);
  }
  

  //@dup
  /**
   * Gets the package CCSID property, which indicates the
   * CCSID in which statements are sent to the i5/OS system and
   * also the CCSID of the package they are stored in.
   * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
   * Default value: 13488
   * @return The value of the package CCSID property.
   * Note:  this method is the same as getPackageCCSID() so that it corresponds to the connection property name
   **/
  public int getPackageCcsid()
  {
      return getPackageCCSID();
  }


  /**
   Sets the package CCSID property, which indicates the
   CCSID in which statements are sent to the i5/OS system and
   also the CCSID of the package they are stored in.
   Valid values:  1200 (UCS-2) and 13488 (UTF-16).  Default value: 13488
   @param ccsid The package CCSID.
   **/
  public void setPackageCCSID(int ccsid)
  {
    final String property = "packageCCSID";

    validateProperty(property, Integer.toString(ccsid), JDProperties.PACKAGE_CCSID);

    properties_.setString(JDProperties.PACKAGE_CCSID, Integer.toString(ccsid));
  }

  //@dup
  /**
   * Sets the package CCSID property, which indicates the
   * CCSID in which statements are sent to the i5/OS system and
   * also the CCSID of the package they are stored in.
   * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
   * Default value: 13488
   * @param ccsid The package CCSID.
   * Note:  this method is the same as setPackageCCSID() so that it corresponds to the connection property name
   **/
  public void setPackageCcsid(int ccsid)
  {
      setPackageCCSID(ccsid);
  }
  
  
  /**
   Gets the minimum divide scale property.  This property ensures the scale
   of the result of decimal division is never less than its specified value.
   Valid values: 0-9.  0 is default.
   @return The minimum divide scale.
   **/
  public int getMinimumDivideScale()
  {
    return properties_.getInt(JDProperties.MINIMUM_DIVIDE_SCALE);
  }

  /**
   Gets the maximum precision property. This property indicates the
   maximum decimal precision the i5/OS system should use.
   Valid values: 31 or 63.  31 is default.
   @return The maximum precision.
   **/
  public int getMaximumPrecision()
  {
    return properties_.getInt(JDProperties.MAXIMUM_PRECISION);
  }

  /**
   Gets the maximum scale property.  This property indicates the
   maximum decimal scale the i5/OS system should use.
   Valid values: 0-63.  31 is default.
   @return The maximum scale.
   **/
  public int getMaximumScale()
  {
    return properties_.getInt(JDProperties.MAXIMUM_SCALE);
  }

  /**
   Sets the minimum divide scale property.  This property ensures the scale
   of the result of decimal division is never less than its specified value.
   Valid values: 0-9.  0 is default.
   @param scale The minimum divide scale.
   **/
  public void setMinimumDivideScale(int scale)
  {
    final String property = "minimumDivideScale";

    validateProperty(property, Integer.toString(scale), JDProperties.MINIMUM_DIVIDE_SCALE);

    properties_.setString(JDProperties.MINIMUM_DIVIDE_SCALE, Integer.toString(scale));
  }

  /**
   Sets the maximum precision property. This property indicates the
   maximum decimal precision the i5/OS system should use.
   Valid values: 31 or 63.  31 is default.
   @param precision The maximum precision.
   **/
  public void setMaximumPrecision(int precision)
  {
    final String property = "maximumPrecision";

    validateProperty(property, Integer.toString(precision), JDProperties.MAXIMUM_PRECISION);

    properties_.setString(JDProperties.MAXIMUM_PRECISION, Integer.toString(precision));
  }

  /**
   Sets the maximum scale property.  This property indicates the
   maximum decimal scale the i5/OS system should use.
   Valid values: 0-63.  31 is default.
   @param scale The maximum scale.
   **/
  public void setMaximumScale(int scale)
  {
    final String property = "maximumScale";

    // validate the new value
    validateProperty(property, Integer.toString(scale), JDProperties.MAXIMUM_SCALE);

    properties_.setString(JDProperties.MAXIMUM_SCALE, Integer.toString(scale));
  }

  /**
   Gets the translate hex property, which indicates how
   the parser will treat hexadecimal literals.
   @return The value of the translate hex property.
   <p>Valid values include:
   <ul>
   <li>"character" (Interpret hexadecimal constants as character data)
   <li>"binary" (Interpret hexadecimal constants as binary data)
   </ul>
   The default value is "character".
   **/
  public String getTranslateHex()
  {
    return properties_.getString(JDProperties.TRANSLATE_HEX);
  }

  /**
   Sets the translate hex property, which indicates how
   the parser will treat hexadecimal literals.
   @param parseOption The hex constant parser option.
   <p>Valid values include:
   <ul>
   <li>"character" (Interpret hexadecimal constants as character data)
   <li>"binary" (Interpret hexadecimal constants as binary data)
   </ul>
   The default value is "character".
   **/
  public void setTranslateHex(String parseOption)
  {
    final String property = "translateHex";

    validateProperty(property, parseOption, JDProperties.TRANSLATE_HEX);

    properties_.setString(JDProperties.TRANSLATE_HEX, parseOption);
  }

  /**
   Sets the QAQQINI library name.
   @param libraryName The QAQQINI library name.
   **/
  public void setQaqqiniLibrary(String libraryName)
  {
    final String property = "qaqqiniLibrary";
    if (libraryName == null)
      throw new NullPointerException(property);

    properties_.setString(JDProperties.QAQQINILIB, libraryName);
  }
  
  //@dup
  /**
   *  Sets the QAQQINI library name.  
   *  @param libraryName The QAQQINI library name.
   *  Note:  this method is the same as setQaqqiniLibrary() so that it corresponds to the connection property name
   **/
  public void setQaqqinilib(String libraryName)
  {
      setQaqqiniLibrary(libraryName);
  }

  /**
   Returns the toolbox trace category.
   @return The toolbox trace category.
   <p>Valid values include:
   <ul>
   <li> "none" - The default value.
   <li> "datastream"
   <li> "diagnostic"
   <li> "error"
   <li> "information"
   <li> "warning"
   <li> "conversion"
   <li> "proxy"
   <li> "pcml"
   <li> "jdbc"
   <li> "all"
   <li> "thread"
   </ul>
   **/
  public String getToolboxTraceCategory()
  {
    return properties_.getString(JDProperties.TRACE_TOOLBOX);
  }
  

  //@dup
  /**
   *  Returns the toolbox trace category.
   *  @return The toolbox trace category.
   *  <p>Valid values include:
   *  <ul>
   *    <li> "none" - The default value.
   *    <li> "datastream"
   *    <li> "diagnostic"
   *    <li> "error"
   *    <li> "information"
   *    <li> "warning"
   *    <li> "conversion"
   *    <li> "proxy"
   *    <li> "pcml"
   *    <li> "jdbc"
   *    <li> "all"
   *    <li> "thread"
   *  </ul>
   *  Note:  this method is the same as getToolboxTraceCategory() so that it corresponds to the connection property name
   **/
  public String getToolboxTrace()
  {
      return getToolboxTraceCategory();
  }


  /**
   Sets the toolbox trace category, which indicates
   what trace points and diagnostic messages should be logged.
   @param traceCategory The category option.
   <p>Valid values include:
   <ul>
   <li> "none"
   <li> "datastream"
   <li> "diagnostic"
   <li> "error"
   <li> "information"
   <li> "warning"
   <li> "conversion"
   <li> "proxy"
   <li> "pcml"
   <li> "jdbc"
   <li> "all"
   <li> "thread"
   </ul>
   The default value is "none".
   **/
  public void setToolboxTraceCategory(String traceCategory)
  {
    final String property = "toolboxTrace";

    validateProperty(property, traceCategory, JDProperties.TRACE_TOOLBOX);

    properties_.setString(JDProperties.TRACE_TOOLBOX, traceCategory);
  }


  //@dup
  /**
   * Sets the toolbox trace category, which indicates 
   * what trace points and diagnostic messages should be logged.
   * @param traceCategory The category option.
   * <p>Valid values include:
   * <ul>
   *    <li> "none" 
   *    <li> "datastream"
   *    <li> "diagnostic"
   *    <li> "error"
   *    <li> "information"
   *    <li> "warning"
   *    <li> "conversion"
   *    <li> "proxy"
   *    <li> "pcml"
   *    <li> "jdbc"
   *    <li> "all"
   *    <li> "thread"    
   * </ul>
   * The default value is "none".
   * Note:  this method is the same as setToolboxTraceCategory() so that it corresponds to the connection property name
   **/
  public void setToolboxTrace(String traceCategory)
  {
      setToolboxTraceCategory(traceCategory);
  }
  
  /**
   Validates the property value.
   @param property The property name.
   @param value The property value.
   @param index The property index.
   **/
  private final void validateProperty(String property, String value, int index)
  {
    if (value.length() != 0)
    {
      DriverPropertyInfo[] info = properties_.getInfo();
      String[] choices = info[index].choices;

      boolean notValid = true;
      int current = 0;
      while (notValid && current < choices.length)
      {
        if (value.equalsIgnoreCase(choices[current]))
          notValid = false;
        else
          current++;
      }
      if (notValid)
        throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

  /**
   Serializes the i5/OS system and user information.
   @param out The output stream.
   @throws IOException If a file I/O error occurs.
   **/
  private void writeObject(ObjectOutputStream out) throws IOException
  {
    if (!savePasswordWhenSerialized_)
    {
      serialPWBytes_ = null;
      pwHashcode_ = 0;

      serialKeyRingPWBytes_ = null;
    }

    // Serialize the object.
    out.defaultWriteObject();
  }

  /**
   Returns the string representation of the object.
   @return The string representation.
   **/
  public String toString()
  {
    /*
     Implementation note: Used only for tracing information.
     */
    return getDataSourceName();
  }

  // Twiddle password bytes.
  // Note: This method generates different output each time it's called against the same password.
  private static final char[] xpwConfuse(String info)
  {
    Random rng = new Random();
    byte[] adderBytes = new byte[18];
    rng.nextBytes(adderBytes);
    char[] adder = BinaryConverter.byteArrayToCharArray(adderBytes);

    byte[] maskBytes = new byte[14];
    rng.nextBytes(maskBytes);
    char[] mask = BinaryConverter.byteArrayToCharArray(maskBytes);

    char[] infoBytes = xencode(adder, mask, info.toCharArray());
    char[] returnBytes = new char[info.length() + 16];
    System.arraycopy(adder, 0, returnBytes, 0, 9);
    System.arraycopy(mask, 0, returnBytes, 9, 7);
    System.arraycopy(infoBytes, 0, returnBytes, 16, info.length());

    return returnBytes;
  }

  // Get clear password bytes back.
  private static final String xpwDeconfuse(char[] info)
  {
    char[] adder = new char[9];
    System.arraycopy(info, 0, adder, 0, 9);
    char[] mask = new char[7];
    System.arraycopy(info, 9, mask, 0, 7);
    char[] infoBytes = new char[info.length - 16];
    System.arraycopy(info, 16, infoBytes, 0, info.length - 16);

    return new String(xdecode(adder, mask, infoBytes));
  }

  // Scramble some bytes.
  private static final char[] xencode(char[] adder, char[] mask, char[] bytes)
  {
    if (bytes == null) return null;
    int length = bytes.length;
    char[] buf = new char[length];
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (char)(bytes[i] + adder[i % 9]);
    }
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (char)(buf[i] ^ mask[i % 7]);
    }
    return buf;
  }

  private static final char[] xdecode(char[] adder, char[] mask, char[] bytes)
  {
    int length = bytes.length;
    char[] buf = new char[length];
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (char)(mask[i % 7] ^ bytes[i]);
    }
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (char)(buf[i] - adder[i % 9]);
    }
    return buf;
  }

  
  //@pda jdbc40
  protected String[] getValidWrappedList()
  {
      return new String[] {  "com.ibm.as400.access.AS400JDBCManagedDataSource", "javax.sql.DataSource" };
  } 

}
