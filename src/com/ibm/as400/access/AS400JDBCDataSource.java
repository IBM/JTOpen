///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.sql.DataSource;					// JDBC2.0 std-ext
import javax.naming.NamingException;			// JNDI
import javax.naming.Reference;					// JNDI
import javax.naming.Referenceable;				// JNDI
import javax.naming.StringRefAddr;				// JNDI

/**
*  The AS400JDBCDataSource class represents a factory for AS/400 or iSeries database connections.
*
*  <P>The following is an example that creates an AS400JDBCDataSource object and creates a 
*  connection to the database.
*
*  <pre><blockquote>
*  // Create a data source for making the connection.
*  AS400JDBCDataSource datasource = new AS400JDBCDataSource("myAS400");
*  datasource.setUser("myUser");
*  datasource.setPassword("MYPWD");

*  // Create a database connection to the AS/400.
*  Connection connection = datasource.getConnection();
*  </blockquote></pre>
*
*  <P>The following example registers an AS400JDBCDataSource object with JNDI and then
*  uses the object returned from JNDI to obtain a database connection.
*  <pre><blockquote>
*  // Create a data source to the AS/400 database.
*  AS400JDBCDataSource dataSource = new AS400JDBCDataSource();
*  dataSource.setServerName("myAS400");
*  dataSource.setDatabaseName("myAS400 Database");
*
*  // Register the datasource with the Java Naming and Directory Interface (JNDI).
*  Hashtable env = new Hashtable();
*  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
*  Context context = new InitialContext(env);
*  context.bind("jdbc/customer", dataSource);
*
*  // Return an AS400JDBCDataSource object from JNDI and get a connection.
*  AS400JDBCDataSource datasource = (AS400JDBCDataSource) context.lookup("jdbc/customer");
*  Connection connection = datasource.getConnection("myUser", "MYPWD");
*  </pre></blockquote>
**/
public class AS400JDBCDataSource implements DataSource, Referenceable, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


	static final long serialVersionUID = 4L;



	/**
	*  Implementation notes:  
	*  The properties listed in com.ibm.as400.access.JDProperties should also be included here.
	**/

	// Constants
	private static final String DATABASE_NAME = "databaseName";
	private static final String DATASOURCE_NAME = "dataSourceName";
	private static final String DESCRIPTION = "description";
	private static final String SERVER_NAME = "serverName";
	private static final String USER = "userName";
	private static final String TRUE_ = "true";
	private static final String FALSE_ = "false";
	private static final String TOOLBOX_DRIVER = "jdbc:as400:";
	private static final int MAX_THRESHOLD = 16777216;			   // Maximum threshold (bytes). @A3C, @A4A

	// Data source properties.
	transient private AS400 as400_;								   // AS/400 object used to store and encrypt the password. 
	private String databaseName_ = "";						   // Database name. @A6C
	private String dataSourceName_ = "";						   // Data source name. @A6C
	private String description_ = "";					   // Data source description. @A6C
	private JDProperties properties_;							   // AS/400 connection properties.
	private PrintWriter writer_;								   // The EventLog print writer.
	private EventLog log_;

	private String serialServerName_;							   // Server name used in serialization.
	private String serialUserName_;								   // User used in serialization.
	private String serialKeyRingName_;     //@B4A   					   // Key ring name used in serialization.
	transient private PropertyChangeSupport changes_;
	private boolean isSecure_ = false;  //@B4A

	// Handles loading the appropriate resource bundle
	private static ResourceBundleLoader loader_;		//@A9A


  /**
    Start tracing the JDBC client.  This is the same as setting
    property "trace=true";  Note the constant is not public.
    It is defined only to be compatible with ODBC
    The numeric value of this constant is 1.
   **/
  static final int TRACE_CLIENT = 1;                // @j1a

  /**
    Start the database monitor on the JDBC server job.
    This constant is used when setting the level of tracing for the JDBC server job.
    The numeric value of this constant is 2.
   **/
  public static final int SERVER_TRACE_START_DATABASE_MONITOR = 2;           // @j1a

  /**
    Start debug on the JDBC server job.
    This constant is used when setting the level of tracing for the JDBC server job.
    The numeric value of this constant is 4.
   **/
  public static final int SERVER_TRACE_DEBUG_SERVER_JOB = 4;           // @j1a

  /**
    Save the joblog when the JDBC server job ends.
    This constant is used when setting the level of tracing for the JDBC server job.
    The numeric value of this constant is 8.
   **/
  public static final int SERVER_TRACE_SAVE_SERVER_JOBLOG = 8;           // @j1a

  /**
    Start job trace on the JDBC server job.
    This constant is used when setting the level of tracing for the JDBC server job.
    The numeric value of this constant is 16.
   **/
  public static final int SERVER_TRACE_TRACE_SERVER_JOB = 16;           // @j1a

  /**
    Save SQL information.
    This constant is used when setting the level of tracing for the JDBC server job.
    The numeric value of this constant is 32.  This option is valid
    when connecting to V5R1 and newer versions of OS/400.
   **/
  public static final int SERVER_TRACE_SAVE_SQL_INFORMATION = 32;           // @j1a






	/**
	*  Constructs a default AS40JDBCDataSource object.
	**/
	public AS400JDBCDataSource()
	{
		initializeTransient();
		properties_ = new JDProperties(null, null);
	}

	/**
	*  Constructs an AS400JDBCDataSource object to the specified <i>serverName</i>.
	*  @param serverName The name of the AS/400 server.
	**/
	public AS400JDBCDataSource(String serverName)
	{
		this();

		setServerName(serverName);
	}

	/**
	*  Constructs an AS400JDBCDataSource object with the specified signon information.
	*  @param serverName The name of the AS/400 server.
	*  @param user The user id.
	*  @param password The user password.
	**/
	public AS400JDBCDataSource(String serverName, String user, String password)
	{
		this();

		setServerName(serverName);
		setUser(user);
		setPassword(password);
	}

	//@B4A
	/**
	*  Constructs an AS400JDBCDataSource object with the specified signon information
	*  to use for SSL communications with the server.
	*  @param serverName The name of the AS/400 server.
	*  @param user The user id.
	*  @param password The user password.
        *  @param keyRingName The key ring class name to be used for SSL communications with the server.
        *  @param keyRingPassword The password for the key ring class to be used for SSL communications with the server.
	**/
	public AS400JDBCDataSource(String serverName, String user, String password,
				   String keyRingName, String keyRingPassword)
	{
		this();

		isSecure_ = true;
		try 
		{
		    as400_ = new SecureAS400(as400_);
		    ((SecureAS400)as400_).setKeyRingName(keyRingName, keyRingPassword);
		}
		catch (PropertyVetoException pe)
		{ /* will never happen */}
		serialKeyRingName_ = keyRingName;

		setServerName(serverName);
		setUser(user);
		setPassword(password);
	}

	/**
	*  Adds a PropertyChangeListener.  The specified PropertyChangeListener's
	*  <b>propertyChange</b> method is called each time the value of any bound
	*  property is changed.
	*  @see #removePropertyChangeListener
	*  @param listener The PropertyChangeListener.
	**/
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
			throw new NullPointerException("listener");
		changes_.addPropertyChangeListener(listener);

		as400_.addPropertyChangeListener(listener);
	}

	/**
	*  Returns the level of database access for the AS/400 connection.
	*  @return The access level.  Valid values include: "all" (all SQL statements allowed),
	*  "read call" (SELECT and CALL statements allowed), and "read only" (SELECT statements only).
	*  The default value is "all".
	**/
	public String getAccess()
	{
		return properties_.getString(JDProperties.ACCESS);
	}


	//@B2A
	/**
	*  Returns the output string type of bidi data, as defined by the CDRA 
	*  (Character Data Representation Architecture). See <a href="BidiStringType.html">
	*  BidiStringType</a> for more information and valid values.  -1 will be returned 
	*  if the value has not been set. 
	**/
	public int getBidiStringType()			 									   //@B3C
	{    
		String value = properties_.getString(JDProperties.BIDI_STRING_TYPE);	   //@B3C 
		try																		   //@B3A
		{																		   //@B3A
			return Integer.parseInt (value);									   //@B3A
		}																		   //@B3A
		catch (NumberFormatException nfe)  // if value is "", that is, not set	   //@B3A
		{																		   //@B3A
			return -1;															   //@B3A
		}																		   //@B3A
	}


	/**
	*  Returns the criteria for retrieving data from the AS/400 server in 
	*  blocks of records.  Specifying a non-zero value for this property
	*  will reduce the frequency of communication to the server, and 
	*  therefore increase performance.
	*  @return The block criteria.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> 0 (no record blocking)
	*    <li> 1 (block if FOR FETCH ONLY is specified)
	*    <li> 2 (block if FOR UPDATE is specified) - The default value.
	*  </ul>
	**/
	public int getBlockCriteria()
	{
		return properties_.getInt(JDProperties.BLOCK_CRITERIA);
	}

	/**
	*  Returns the block size in kilobytes to retrieve from the AS/400 server and
	*  cache on the client.  This property has no effect unless the block criteria
	*  property is non-zero.  Larger block sizes reduce the frequency of 
	*  communication to the server, and therefore may increase performance.
	*  @return The block size in kilobytes.
	*  <p>Valid values include:
	*  <ul>
	*    <li> 0
	*    <li> 8
	*    <li> 16
	*    <li> 32   - The default value.
	*    <li> 64
	*    <li> 128
	*    <li> 256
	*    <li> 512
	*  </ul>
	**/
	public int getBlockSize()
	{
		return properties_.getInt(JDProperties.BLOCK_SIZE);
	}

	/**
	*  Returns the database connection.
	*  @return The connection.
	*  @exception SQLException If a database error occurs.
	**/
	public Connection getConnection() throws SQLException
	{	
    		if (isSecure_)					     //@B4A
		    return getConnection(new SecureAS400(as400_));   //@B4A
		else						     //@B4A
		    return getConnection(new AS400(as400_));
	}

	/**
	*  Returns the database connection using the specified <i>user</i> and <i>password</i>.
	*  @param user The database user.
	*  @param password The database password.
	*  @return The connection
	*  @exception SQLException If a database error occurs.
	**/
	public Connection getConnection(String user, String password) throws SQLException
	{
		// Validate the parameters.
		if (user == null)
			throw new NullPointerException("user");
		if (password == null)
			throw new NullPointerException("password");

		return getConnection(new AS400(getServerName(), user, password));
	}

	/**
	*  Creates the database connection based on the signon and property information.
	*  @param as400 The AS400 object used to make the connection.
	*  @exception SQLException If a database error occurs.
	**/
	private Connection getConnection(AS400 as400) throws SQLException
	{
		AS400JDBCConnection connection = new AS400JDBCConnection();
		connection.setSystem(as400);
    connection.setProperties(new JDDataSourceURL(TOOLBOX_DRIVER + "//" + as400.getSystemName()), properties_, as400); //@C1C
		log(loader_.getText("AS400_JDBC_DS_CONN_CREATED"));	 //@A9C
		return connection;
	}

	/**
	*  Returns the database name property.
	*  @return The database name.
	**/
	public String getDatabaseName()
	{
		return databaseName_;
	}

	/**
	*  Returns the data source name property.
	*  This property is used to name an underlying data source when connection pooling is used.
	*  @return The data source name.
	**/
	public String getDataSourceName()
	{
		return dataSourceName_;
	}

	/**
	*  Returns the AS/400 date format used in date literals within SQL statements.
	*  @return The date format.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> "mdy"
	*    <li> "dmy"
	*    <li> "ymd"
	*    <li> "usa"
	*    <li> "iso"
	*    <li> "eur"
	*    <li> "jis"
	*    <li> "julian"
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default is based on the server job.
	**/
	public String getDateFormat()
	{
		return properties_.getString(JDProperties.DATE_FORMAT);
	}

	/**
	*  Returns the AS/400 date separator used in date literals within SQL statements.
	*  This property has no effect unless the "data format" property is set to:
	*  "julian", "mdy", "dmy", or "ymd".
	*  @return The date separator.
	*  <p>Valid values include:
	*  <ul> 
	*    <li> "/" (slash)
	*    <li> "-" (dash)
	*    <li> "." (period)
	*    <li> "," (comma)
	*    <li> " " (space)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public String getDateSeparator()
	{
		return properties_.getString(JDProperties.DATE_SEPARATOR);
	}

	/**
	*  Returns the AS/400 decimal separator used in numeric literals within SQL statements.
	*  @return The decimal separator.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> "." (period)
	*    <li> "," (comma)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public String getDecimalSeparator()
	{
		return properties_.getString(JDProperties.DECIMAL_SEPARATOR);
	}

	/**
	*  Returns the description of the data source.
	*  @return The description.
	**/
	public String getDescription()
	{
		return description_;
	}

	// @A2A
	/**
	* Returns the JDBC driver implementation.
	* The AS/400 Toolbox for Java JDBC driver
	* chooses which JDBC driver implementation
	* to use based on the environment. If the
	* environment is an AS/400 Java Virtual
	* Machine on the same AS/400 as the
	* database to which the program is connecting,
	* the native AS/400 Developer Kit for Java
	* JDBC driver is used. In any other
	* environment, the AS/400 Toolbox for Java
	* JDBC driver is used. This property has no
	* effect if the "secondary URL" property is set.
	* This property cannot be set to "native" if the
	* environment is not an AS/400 Java Virtual
	* Machine.
	*  <p>Valid values include: 
	*  <ul> 
	*  <li>"default" (base the implementation on the environment) 
	*  <li>"toolbox" (use the AS/400 Toolbox for Java JDBC driver) 
	*  <li>"native" (use the AS/400 Developer Kit for Java JDBC driver)
	*  </ul>
	*  The default value is "default".
	**/
	public String getDriver()
	{
		return properties_.getString(JDProperties.DRIVER);
	}

	/**
	*  Returns the amount of detail for error messages originating from 
	*  the AS/400 server.
	*  @return The error message level.
	*  Valid values include: "basic" and "full".  The default value is "basic".
	**/
	public String getErrors()
	{
		return properties_.getString(JDProperties.ERRORS);
	}

	/**
	*  Returns the AS/400 libraries to add to the server job's library list.
	*  The libraries are delimited by commas or spaces, and
	*  "*LIBL" may be used as a place holder for the server job's
	*  current library list.  The library list is used for resolving
	*  unqualified stored procedure calls and finding schemas in
	*  DatabaseMetaData catalog methods.  If "*LIBL" is not specified, 
	*  the specified libraries will replace the server job's current library list.
	*  @return The library list.
	**/
	public String getLibraries()
	{
		return properties_.getString(JDProperties.LIBRARIES);
	}

	/**
	*  Returns the AS/400 maximum LOB (large object) size in bytes that
	*  can be retrieved as part of a result set.  LOBs that are larger
	*  than this threshold will be retrieved in pieces using extra
	*  communication to the server.  Larger LOB thresholds will reduce
	*  the frequency of communication to the server, but will download
	*  more LOB data, even if it is not used.  Smaller LOB thresholds may
	*  increase frequency of communication to the server, but will only
	*  download LOB data as it is needed.
	*  @return The lob threshold.  Valid range is 0-16777216.
	*  The default value is 0.
	**/
	public int getLobThreshold()
	{
		return properties_.getInt(JDProperties.LOB_THRESHOLD);
	}

	/**
	*  Returns the timeout value in seconds.
	*  Note: This value is not used or supported.  
	*  The timeout value is determined by the AS/400.
	*  @return Always returns 0.
	**/
	public int getLoginTimeout()
	{
		return 0;
	}

	/**
	*  Returns the log writer for this data source.
	*  @return The 
	*  @exception SQLException If a database error occurs.
	**/
	public PrintWriter getLogWriter() throws SQLException
	{
		return writer_;
	}

	/**
	*  Returns the AS/400 naming convention used when referring to tables.
	*  @return The naming convention.  Valid values include: "sql" (e.g. schema.table)
	*  and "system" (e.g. schema/table).  The default value is "sql".
	**/
	public String getNaming()
	{
		return properties_.getString(JDProperties.NAMING);
	}

	/**
	*  Returns the base name of the SQL package.  Extended dynamic support works
	*  best when this is derived from the application name.  Note that only the
	*  first seven characters are significant.  This property has no effect unless
	*  the extended dynamic property is set to true.  In addition, this property
	*  must be set if the extended dynamic property is set to true.
	*  @return The base name of the SQL package.
	**/
	public String getPackage()
	{
		return properties_.getString(JDProperties.PACKAGE);
	}

	/**
	*  Returns the type of SQL statement to be stored in the SQL package.  This can
	*  be useful to improve the performance of complex join conditions.  This
	*  property has no effect unless the extended dynamic property is set to true.
	*  @return The type of SQL statement.
	*  Valid values include: "default" (only store SQL statements with parameter
	*  markers in the package) and "select" (store al SQL SELECT statements to be
	*  stored in the package).  The default value is "default".
	**/
	public String getPackageCriteria()
	{
		return properties_.getString(JDProperties.PACKAGE_CRITERIA);
	}

	/**
	*  Returns the action to take when SQL package errors occur.  When an SQL package
	*  error occurs, the driver will optionally throw an SQLException or post a
	*  warning to the Connection, based on the value of this property.  This property
	*  has no effect unless the extended dynamic property is set to true.
	*  @return The action to take when SQL errors occur.
	*  Valid values include: "exception", "warning", and "none".  The default value is "warning".
	**/
	public String getPackageError()
	{
		return properties_.getString(JDProperties.PACKAGE_ERROR);
	}
	/**
	*  Returns the library for the SQL package.  This property has no effect unless
	*  the extended dynamic property is set to true.
	*  @return The SQL package library.  The default library is "QGPL".
	**/
	public String getPackageLibrary()
	{
		return properties_.getString(JDProperties.PACKAGE_LIBRARY);
	}

	/**
	*  Returns the name of the proxy server.
	*  @return The proxy server.
	**/
	public String getProxyServer()
	{
		return properties_.getString(JDProperties.PROXY_SERVER);
	}

	/**
	*  Returns the Reference object for the data source object.
	*  This is used by JNDI when bound in a JNDI naming service.
	*  Contains the information necessary to reconstruct the data source
	*  object when it is later retrieved from JNDI via an object factory.
	*  
	*  @return A Reference object of the data source object.
	*  @exception NamingException If a naming error occurs in resolving the object. 
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

		// Add the data source properties.  (unique constant identifiers for storing in JNDI).
		if (getDatabaseName() != null)
			ref.add(new StringRefAddr(DATABASE_NAME, getDatabaseName()));
		if (getDataSourceName() != null)
			ref.add(new StringRefAddr(DATASOURCE_NAME, getDataSourceName()));
		if (getDescription() != null)
			ref.add(new StringRefAddr(DESCRIPTION, getDescription()));
		ref.add(new StringRefAddr(SERVER_NAME, getServerName()));  
		ref.add(new StringRefAddr(USER, getUser()));

		return ref;
	}

	/**
	*  Returns the source of the text for REMARKS columns in ResultSets returned
	*  by DatabaseMetaData methods.
	*  @return The text source.
	*  Valid values include: "sql" (SQL object comment) and "system" (OS/400 object description).
	*  The default value is "system".
	**/
	public String getRemarks()
	{
		return properties_.getString(JDProperties.REMARKS);
	}

	/**
	*  Returns the secondary URL.
	*  @return The secondary URL.
	**/
	public String getSecondaryUrl()
	{
		return properties_.getString(JDProperties.SECONDARY_URL);
	}

	/**
	*  Returns the name of the AS400 server property.
	*  @return The server name.
	**/
	public String getServerName()
	{
		return as400_.getSystemName();
	}


  // @j1 new method
  /**
  *  Returns the level of tracing started on the JDBC server job.
  *  If tracing is enabled, tracing is started when
  *  the client connects to the server and ends when the connection
  *  is disconnected.  Tracing must be started before connecting to
  *  the server since the client enables server tracing only at connect time.
  *  Trace data is collected in spooled files on the server.  Multiple
  *  levels of server tracing can be turned on in combination by adding
  *  the constants and passing that sum on the set method.  For example,
  *  <pre>
  *  dataSource.setServerTraceCategories(AS400JDBCDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
  *  </pre>
  *  @return The server tracing level.
  *  <p>The value is a combination of the following:
  *  <ul>
  *  <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
  *                               The numeric value of this constant is 2.
  *  <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
  *                         The numeric value of this constant is 4.
  *  <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
  *                           The numeric value of this constant is 8.
  *  <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
  *                         The numeric value of this constant is 16.
  *  <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
  *                             The numeric value of this constant is 32.
  *  </ul>
  *
  *  <P>
  *  Tracing the JDBC server job will use significant amounts of server resources.
  *  Additional processor resource is used to collect the data, and additional
  *  storage is used to save the data.  Turn on server tracing only to debug
  *  a problem as directed by IBM service.
  *
  **/
  public int getServerTraceCategories()
  {
    return properties_.getInt(JDProperties.TRACE_SERVER);
  }

	/**
	*  Returns how the AS/400 server sorts records before sending them to the client.
	*  @return The sort value.
	*  <p>Valid values include: 
	*  <ul>
	*    <li>"hex" (base the sort on hexadecimal values)
	*    <li>"job" (base the sort on the setting for the server job)
	*    <li>"language" (base the sort on the language set in the sort language property)
	*    <li> "table" (base the sort on the sort sequence table set in the sort table property)
	*  </ul>
	*  The default value is "job".
	**/
	public String getSort()
	{
		return properties_.getString(JDProperties.SORT);
	}

	/**
	*  Returns the three-character language id to use for selection of a sort sequence.
	*  @return The three-character language id.
	*  The default value is based on the locale.
	**/
	public String getSortLanguage()
	{
		return properties_.getString(JDProperties.SORT_LANGUAGE);
	}

	/**
	*  Returns the library and file name of a sort sequence table stored on the AS/400 server.
	*  @return The qualified sort table name.
	**/
	public String getSortTable()
	{
		return properties_.getString(JDProperties.SORT_TABLE);
	}

	/**
	*  Returns how the AS/400 server treats case while sorting records.     
	*  @return The sort weight.
	*  Valid values include: "shared" (upper- and lower-case characters are sorted as the
	*  same character) and "unique" (upper- and lower-case characters are sorted as 
	*  different characters).  The default value is "shared".
	**/
	public String getSortWeight()
	{
		return properties_.getString(JDProperties.SORT_WEIGHT);
	}

	/**
	*  Returns the AS/400 time format used in time literals with SQL statements.
	*  @return The time format.
	*  <p>Valid values include: 
	*  <ul>
	*    <li> "hms"
	*    <li> "usa"
	*    <li> "iso"
	*    <li> "eur"
	*    <li> "jis"
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public String getTimeFormat()
	{
		return properties_.getString(JDProperties.TIME_FORMAT);
	}

	/**
	*  Returns the AS/400 time separator used in time literals within SQL statements.
	*  @return The time separator. 
	*  <p>Valid values include:
	*  <ul>
	*    <li> ":" (colon)
	*    <li> "." (period)
	*    <li> "," (comma)
	*    <li> " " (space)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public String getTimeSeparator()
	{
		return properties_.getString(JDProperties.TIME_SEPARATOR);
	}

	/**
	*  Returns the AS/400 server's transaction isolation.
	*  @return The transaction isolation level.
	*  <p>Valid values include:
	*  <ul>
	*    <li> "none"
	*    <li> "read uncommitted"  - The default value.
	*    <li> "read committed"
	*    <li> "repeatable read"
	*    <li> "serializable"
	*  </ul>
	**/
	public String getTransactionIsolation()
	{
		return properties_.getString(JDProperties.TRANSACTION_ISOLATION);
	}

	/** 
	*  Returns the database user property.
	*  @return The user.
	**/
	public String getUser()
	{
		return as400_.getUserId();
	}

	/**
	*  Initializes the transient data for object de-serialization.
	**/
	private void initializeTransient()
	{      
		changes_ = new PropertyChangeSupport(this);
		
		if (isSecure_)			     //@B4A
		    as400_ = new SecureAS400();	     //@B4A
		else				     //@B4A
		    as400_ = new AS400();

		// Reinitialize the serverName, user, and keyRingName.
		if (serialServerName_ != null)
			setServerName(serialServerName_);

		if (serialUserName_ != null)
			setUser(serialUserName_);

		try 
		{
		    if (serialKeyRingName_ != null && isSecure_)	 //@B4A
			((SecureAS400)as400_).setKeyRingName(serialKeyRingName_); //@B4A
		}
		catch (PropertyVetoException pve)
		{ /* Will never happen */ }
	}

	/**
	*  Indicates whether a big decimal value is returned. 
	*  @return true if a big decimal is returned; false otherwise.  
	*  The default value is true.
	**/
	public boolean isBigDecimal()
	{
		return properties_.getBoolean(JDProperties.BIG_DECIMAL);
	}

	/**
	*  Indicates whether the cursor is held.
	*  @return true if the cursor is held; false otherwise.
	*  The default value is true.
	**/
	public boolean isCursorHold()
	{
		return properties_.getBoolean(JDProperties.CURSOR_HOLD);
	}

	/**
	*  Indicates whether data compression is used.
	*  @return true if data compression is used; false otherwise.
	*  The default value is true.
	**/
	public boolean isDataCompression()
	{
		return properties_.getBoolean(JDProperties.DATA_COMPRESSION);
	}

	/**
	*  Indicates whether data truncation is used.
	*  @return true if data truncation is used; false otherwise.
	*  The default value is true.
	**/
	public boolean isDataTruncation()
	{
		return properties_.getBoolean(JDProperties.DATA_TRUNCATION);
	}

	/**
	*  Indicates whether extended dynamic support is used.  Extended dynamic
	*  support provides a mechanism for caching dynamic SQL statements on 
	*  the server.  The first time a particular SQL statement is run, it is
	*  stored in an SQL package on the server.  On subsequent runs of the
	*  same SQL statement, the server can skip a significant part of the 
	*  processing by using information stored in the SQL package.
	*  @return true if extended dynamic support is used; false otherwise.
	*  The default value is not to use extended dynamic support.
	**/
	public boolean isExtendedDynamic()
	{
		return properties_.getBoolean(JDProperties.EXTENDED_DYNAMIC);
	}

  // @W1a
  /**
  *  Indicates whether the server fully opens a file when performing a query.
  *  By default the server optimizes opens so they perform better.  In 
  *  certain cases an optimized open will fail.  In some
  *  cases a query will fail when a database performance monitor
  *  is turned on even though the same query works with the monitor
  *  turned off.  In this case set the full open property to true.
  *  This disables optimization on the server.
  *  @return true if files are fully opened; false otherwise.
  *  The default value is false.
  **/
  public boolean isFullOpen()
  {
       return properties_.getBoolean(JDProperties.FULL_OPEN);
  }

	// @A1A
	/**
	*  Indicates whether to delay closing cursors until subsequent requests.
	*  @return true to delay closing cursors until subsequent requests; false otherwise.
	*  The default value is false.
	**/
	public boolean isLazyClose()
	{
		return properties_.getBoolean(JDProperties.LAZY_CLOSE);
	}

	/**
	*  Indicates whether to add statements to an existing SQL package.  This property
	*  has no effect unless the extended dynamic property is set to true;
	*  @return true if statement can be added to an existing SQL package; false otherwise.
	*  The default value is true.
	**/
	public boolean isPackageAdd()
	{
		return properties_.getBoolean(JDProperties.PACKAGE_ADD);
	}

	/**
	*  Indicates whether SQL packages are cached in memory.  Caching SQL packages locally
	*  reduces the amount of communication to the server in some cases.  This 
	*  property has no effect unless the extended dynamic property is set to true.
	*  @return true if caching is used; false otherwise.
	*  The defalut value is false.
	**/
	public boolean isPackageCache()
	{
		return properties_.getBoolean(JDProperties.PACKAGE_CACHE);
	}

	/**
	*  Indicates whether SQL packages are cleared when they become full.  Clearing an SQL
	*  package results in removing all SQL statements that have been stored in the
	*  SQL package.  This property has no effect unless the extended dynamic property
	*  is set to true.
	*  @return true if the SQL package are cleared when full; false otherwise.
	*  The default value if false.
	**/
	public boolean isPackageClear()
	{
		return properties_.getBoolean(JDProperties.PACKAGE_CLEAR);
	}

	/**
	*  Indicates whether data is prefetched upon executing a SELECT statement.
	*  This will increase performance when accessing the initial rows in the result set.
	*  @return If prefetch is used; false otherwise.
	*  The default value is prefetch data.
	**/
	public boolean isPrefetch()
	{
		return properties_.getBoolean(JDProperties.PREFETCH);
	}

	/**
	*  Indicates whether the user is prompted if a user name or password is
	*  needed to connect to the AS/400 server.  If a connection can not be made
	*  without prompting the user, and this property is set to false, then an 
	*  attempt to connect will fail throwing an exception.
	*  @return true if the user is prompted for signon information; false otherwise.
	*  The default value is false.
	**/
	public boolean isPrompt()
	{
		return properties_.getBoolean(JDProperties.PROMPT);
	}

	/**
	*  Indicates whether a Secure Socket Layer (SSL) connection is used to communicate
	*  with the server.  SSL connections are only available when connecting to servers
	*  at V4R4 or later.
	*  @return true if Secure Socket Layer connection is used; false otherwise.
	*  The default value is false.
	**/
	public boolean isSecure()
	{
		return properties_.getBoolean(JDProperties.SECURE);
	}


	/**
	*  Indicates whether a thread is used.  
	*  @return true if a thread is used; false otherwise.
	*  The default value is true.
	**/
	public boolean isThreadUsed()
	{
		return properties_.getBoolean(JDProperties.THREAD_USED);
	}

	/**
	*  Indicates whether trace messages should be logged.  
	*  @return true if trace message are logged; false otherwise.
	*  The default value is false.
	**/
	public boolean isTrace()
	{
		return properties_.getBoolean(JDProperties.TRACE);
	}

	/**
	*  Indicates whether binary data is translated.  If this property is set
	*  to true, then BINARY and VARBINARY fields are treated as CHAR and
	*  VARCHAR fields.
	*  @return true if binary data is translated; false otherwise.
	*  The default value is false.
	**/
	public boolean isTranslateBinary()
	{
		return properties_.getBoolean(JDProperties.TRANSLATE_BINARY);
	}

	/**
	*  Logs a message to the event log.
	*  @param message The message to log.
	**/
	void log(String message)
	{
		if (JDTrace.isTraceOn())
			JDTrace.logInformation (this, message);

		if (log_ != null)
			log_.log(message);
	}

	/**
	*  Logs an exception and message to the event log.
	*  @param property The property to log.
	*  @param value The property value to log.
	**/
	private void logProperty(String property, String value)
	{
		if (Trace.isTraceOn())
			JDTrace.logProperty (this, property, value);

		//@A8D if (log_ != null) 
		//@A8D log_.log(property + ": " + value);
	}

	/**
	*  Deserializes and initializes transient data.
	*  @exception ClassNotFoundException If the class cannot be found.
	*  @exception IOException If an I/O exception occurs.
	**/
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
	{
		in.defaultReadObject();
		initializeTransient();
	}

	/**
	*  Removes the PropertyChangeListener.
	*  If the PropertyChangeListener is not in the list, nothing is done.
	*  @param listener The PropertyChangeListener.
	*  @see #addPropertyChangeListener
	**/
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (listener == null)
			throw new NullPointerException("listener");
		changes_.removePropertyChangeListener(listener);

		as400_.addPropertyChangeListener(listener);
	}

	/**
	*  Sets the level of database access for the AS/400 connection.
	*  @param access The access level.  
	*  <p>Valid values include: 
	*  <ul> 
	*    <li> "all" (all SQL statements allowed)
	*    <li> "read call" (SELECT and CALL statements allowed)
	*    <li> "read only" (SELECT statements only)
	*  </ul>
	*  The default value is "all".
	**/
	public void setAccess(String access)
	{
		String property = "access";

		if (access == null)
			throw new NullPointerException(property);
		validateProperty(property, access, JDProperties.ACCESS);

		String old = getAccess();
		properties_.setString(JDProperties.ACCESS, access);

		changes_.firePropertyChange(property, old, access);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + access);  //@A8C
	}


	//@B2A
	/**
	 *  Sets the output string type of bidi data, as defined by the CDRA (Character Data 
	 *  Representation Architecture). See <a href="BidiStringType.html">
	 *  BidiStringType</a> for more information and valid values.
	 **/
	public void setBidiStringType(int bidiStringType)						//@B3C
	{
		String property = "bidiStringType";	  								//@B3C

		//@B3D if (bidiStringType == null) 
		//@B3D    throw new NullPointerException(property);
		Integer oldBidiStringType = new Integer(getBidiStringType());	  	//@B3A
		Integer newBidiStringType = new Integer(bidiStringType);			//@B3A

		validateProperty(property, newBidiStringType.toString(), JDProperties.BIDI_STRING_TYPE); //@B3C

		properties_.setString(JDProperties.BIDI_STRING_TYPE, newBidiStringType.toString());	 //@B3C

		changes_.firePropertyChange(property, oldBidiStringType, newBidiStringType);  //@B3C

		if (JDTrace.isTraceOn())
			JDTrace.logInformation (this, property + ": " + bidiStringType);
	}



	/**
	*  Sets whether a big decimal value is returned. 
	*  @param value true if a big decimal is returned; false otherwise.
	*  The default value is true.
	**/
	public void setBigDecimal(boolean value)
	{
		String property = "bigDecimal";
		Boolean oldValue = new Boolean(isBigDecimal());
		Boolean newValue = new Boolean(value);

		if (value)
			properties_.setString(JDProperties.BIG_DECIMAL, TRUE_);
		else
			properties_.setString(JDProperties.BIG_DECIMAL, FALSE_);

		changes_.firePropertyChange(property, oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + value);	 //@A8C
	}

	/**
	*  Sets the criteria for retrieving data from the AS/400 server in 
	*  blocks of records.  Specifying a non-zero value for this property
	*  will reduce the frequency of communication to the server, and 
	*  therefore increase performance.
	*  @param blockCriteria The block criteria.
	*  <p>Valid values include:
	*  <ul>
	*    <li> 0 (no record blocking)
	*    <li> 1 (block if FOR FETCH ONLY is specified)
	*    <li> 2 (block if FOR UPDATE is specified) - The default value.
	*  </ul>
	**/
	public void setBlockCriteria(int blockCriteria)
	{
		String property = "blockCriteria";
		Integer oldCriteria = new Integer(getBlockCriteria());
		Integer newCriteria = new Integer(blockCriteria);

		validateProperty(property, newCriteria.toString(), JDProperties.BLOCK_CRITERIA);

		properties_.setString(JDProperties.BLOCK_CRITERIA, newCriteria.toString());
		changes_.firePropertyChange(property, oldCriteria, newCriteria);  

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + blockCriteria);	 //@A8C
	}

	/**
	*  Sets the block size in kilobytes to retrieve from the AS/400 server and
	*  cache on the client.  This property has no effect unless the block criteria
	*  property is non-zero.  Larger block sizes reduce the frequency of 
	*  communication to the server, and therefore may increase performance.
	*  @param blockSize The block size in kilobytes.
	*  <p>Valid values include:
	*  <ul>
	*    <li> 0
	*    <li> 8
	*    <li> 16
	*    <li> 32  - The default value.
	*    <li> 64
	*    <li> 128
	*    <li> 256
	*    <li> 512
	*  </ul>
	**/
	public void setBlockSize(int blockSize)
	{
		String property = "blockSize";

		Integer oldBlockSize = new Integer(getBlockSize());
		Integer newBlockSize = new Integer(blockSize);

		validateProperty(property, newBlockSize.toString(), JDProperties.BLOCK_SIZE);

		properties_.setString(JDProperties.BLOCK_SIZE, new Integer(blockSize).toString());
		changes_.firePropertyChange(property, oldBlockSize, newBlockSize);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + blockSize);	 //@A8C
	}

	/**
	*  Sets whether the cursor is held.
	*  @param cursorHold true if the cursor is held; false otherwise.  The default value is true.
	**/
	public void setCursorHold(boolean cursorHold)
	{
		String property = "cursorHold";
		Boolean oldHold = new Boolean(isCursorHold());
		Boolean newHold = new Boolean(cursorHold);

		if (cursorHold)
			properties_.setString(JDProperties.CURSOR_HOLD, TRUE_);
		else
			properties_.setString(JDProperties.CURSOR_HOLD, FALSE_);

		changes_.firePropertyChange(property, oldHold, newHold);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + cursorHold);  //@A8C
	}

	/**
	*  Sets the database name.
	*  @param databaseName The database name.
	**/
	public void setDatabaseName(String databaseName)
	{
		String property = DATABASE_NAME;

		if (databaseName == null)
			throw new NullPointerException(property);

		String old = getDatabaseName();

		databaseName_ = databaseName;

		changes_.firePropertyChange(property, old, databaseName);

		logProperty("database", databaseName_);
	}

	/**
	*  Sets whether to use data compression.  The default value is true.
	*  @param compression true if data compression is used; false otherwise.
	**/
	public void setDataCompression(boolean compression)
	{
		Boolean oldCompression = new Boolean(isDataCompression());
		Boolean newCompression = new Boolean(compression);

		if (compression)
			properties_.setString(JDProperties.DATA_COMPRESSION, TRUE_);
		else
			properties_.setString(JDProperties.DATA_COMPRESSION, FALSE_);

		changes_.firePropertyChange("dataCompression", oldCompression, newCompression);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "dataCompression: " + compression);  //@A8C
	}

	/**
	*  Sets the data source name.
	*  This property can be used for connection pooling implementations.
	*  @param dataSourceName The data source name.
	**/
	public void setDataSourceName(String dataSourceName)
	{
		String property = DATASOURCE_NAME;

		if (dataSourceName == null)
			throw new NullPointerException(property);

		String old = getDataSourceName();

		dataSourceName_ = dataSourceName;

		changes_.firePropertyChange(property, old, dataSourceName);

		logProperty("dataSource", dataSourceName_);
	}

	/**
	*  Sets whether to use data truncation.  The default value is true.
	*  @param truncation true if data truncation is used; false otherwise.
	**/
	public void setDataTruncation(boolean truncation)
	{
		Boolean oldTruncation = new Boolean(isDataTruncation());
		Boolean newTruncation = new Boolean(truncation);

		if (truncation)
			properties_.setString(JDProperties.DATA_TRUNCATION, TRUE_);
		else
			properties_.setString(JDProperties.DATA_TRUNCATION, FALSE_);

		changes_.firePropertyChange("dataTruncation", oldTruncation, newTruncation);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "dataTruncation: " + truncation);	 //@A8C
	}

	/**
	*  Sets the AS/400 date format used in date literals within SQL statements.
	*  @param dateFormat The date format.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> "mdy"
	*    <li> "dmy"
	*    <li> "ymd"
	*    <li> "usa"
	*    <li> "iso"
	*    <li> "eur"
	*    <li> "jis"
	*    <li> "julian"
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default is based on the server job.
	**/
	public void setDateFormat(String dateFormat)
	{
		String property = "dateFormat";

		if (dateFormat == null)
			throw new NullPointerException(property);
		validateProperty(property, dateFormat, JDProperties.DATE_FORMAT);

		String old = getDateFormat();

		properties_.setString(JDProperties.DATE_FORMAT, dateFormat);

		changes_.firePropertyChange(property, old, dateFormat);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + dateFormat);  //@A8C
	}

	/**
	*  Sets the AS/400 date separator used in date literals within SQL statements.
	*  This property has no effect unless the "data format" property is set to:
	*  "julian", "mdy", "dmy", or "ymd".
	*  @param dateSeparator The date separator.
	*  <p>Valid values include: 
	*  <ul>
	*    <li> "/" (slash)
	*    <li> "-" (dash)
	*    <li> "." (period)
	*    <li> "," (comma)
	*    <li> " " (space)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public void setDateSeparator(String dateSeparator)
	{
		String property = "dateSeparator";
		if (dateSeparator == null)
			throw new NullPointerException(property);
		validateProperty(property, dateSeparator, JDProperties.DATE_SEPARATOR);

		String old = getDateSeparator();

		properties_.setString(JDProperties.DATE_SEPARATOR, dateSeparator);

		changes_.firePropertyChange(property, old, dateSeparator);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + dateSeparator);	 //@A8C
	}

	/**
	*  Sets the AS/400 decimal separator used in numeric literals within SQL statements.
	*  @param decimalSeparator The decimal separator.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> "." (period) 
	*    <li> "," (comma)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public void setDecimalSeparator(String decimalSeparator)
	{
		String property = "decimalSeparator";
		if (decimalSeparator == null)
			throw new NullPointerException(property);
		validateProperty(property, decimalSeparator, JDProperties.DECIMAL_SEPARATOR);

		String old = getDecimalSeparator();

		properties_.setString(JDProperties.DECIMAL_SEPARATOR, decimalSeparator);

		changes_.firePropertyChange(property, old, decimalSeparator);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + decimalSeparator);	//@A8C
	}

	/**
	*  Sets the data source description.
	*  @param description The description.
	**/
	public void setDescription(String description)
	{
		String property = DESCRIPTION;
		if (description == null)
			throw new NullPointerException(property);

		String old = getDescription();

		description_ = description;

		changes_.firePropertyChange(property, old, description);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + description);  //@A8C
	}

	/**
	*  Sets how the AS/400 server sorts records before sending them to the client.
	*  @param sort The sort value.
	*  <p>Valid values include: 
	*  <ul>
	*    <li> "hex" (base the sort on hexadecimal values)
	*    <li> "job" (base the sort on the setting for the server job)
	*    <li> "language" (base the sort on the language set in the sort language property)
	*    <li> "table" (base the sort on the sort sequence table set in the sort table property).  
	*  </ul>
	*  The default value is "job".
	**/
	public void setSort(String sort)
	{
		String property = "sort";
		if (sort == null)
			throw new NullPointerException(property);

		validateProperty(property, sort, JDProperties.SORT);
		String old = getSort();

		properties_.setString(JDProperties.SORT, sort);

		changes_.firePropertyChange(property, old, sort);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + sort);	//@A8C
	}

	/**
	*  Sets the amount of detail to be returned in the message for errors
	*  occurring on the AS/400 server.
	*  @param errors The error message level.
	*  Valid values include: "basic" and "full".  The default value is "basic".
	**/
	public void setErrors(String errors)
	{
		String property = "errors";
		if (errors == null)
			throw new NullPointerException(property);
		validateProperty(property, errors, JDProperties.ERRORS);

		String old = getErrors();
		properties_.setString(JDProperties.ERRORS, errors);

		changes_.firePropertyChange(property, old, errors);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + errors);  //@A8C
	}

	/**
	*  Sets whether to use extended dynamic support.  Extended dynamic
	*  support provides a mechanism for caching dynamic SQL statements on 
	*  the server.  The first time a particular SQL statement is run, it is
	*  stored in an SQL package on the server.  On subsequent runs of the
	*  same SQL statement, the server can skip a significant part of the 
	*  processing by using information stored in the SQL package.  If this
	*  is set to "true", then a package name must be set using the "package"
	*  property.
	*  @param extendedDynamic If extended dynamic support is used; false otherwise.
	*  The default value is not to use extended dynamic support.
	**/
	public void setExtendedDynamic(boolean extendedDynamic)
	{
		Boolean oldValue = new Boolean(isExtendedDynamic());
		Boolean newValue = new Boolean(extendedDynamic);

		if (extendedDynamic)
			properties_.setString(JDProperties.EXTENDED_DYNAMIC, TRUE_);
		else
			properties_.setString(JDProperties.EXTENDED_DYNAMIC, FALSE_);

		changes_.firePropertyChange("extendedDynamic", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "extendedDynamic: " + extendedDynamic);  //@A8C
	}

  // @W1a new method
  /**
  *  Sets whether to fully open a file when performing a query.
  *  By default the server optimizes opens so they perform better.  
  *  In most cases optimization functions correctly and improves
  *  performance.  Running a query repeatedly 
  *  when a database performance monitor is turned on may fail
  *  because of the optimization, however.
  *  Leave this property set to its default (false) until
  *  you experience errors running queries with monitors 
  *  turned on.  At that time set the property to true which
  *  will disable the optimization. 
  *  @param fullOpen True to fully open a file (turn off optimizations), false
  *          to allow optimizations.  The default value is false.
  **/     
  public void setFullOpen(boolean fullOpen)
  {
       Boolean oldValue = new Boolean(isFullOpen());
       Boolean newValue = new Boolean(fullOpen);

       if (fullOpen)
            properties_.setString(JDProperties.FULL_OPEN, TRUE_);
       else
            properties_.setString(JDProperties.FULL_OPEN, FALSE_);

       changes_.firePropertyChange("fullOpen", oldValue, newValue);

       if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "fullOpen: " + fullOpen);
  }

	// @A1A
	/**
	*  Sets whether to delay closing cursors until subsequent requests.
	*  @param lazyClose true to delay closing cursors until subsequent requests; false otherwise.  
			 The default value is false.
	**/
	public void setLazyClose(boolean lazyClose)
	{
		Boolean oldValue = new Boolean(isLazyClose());
		Boolean newValue = new Boolean(lazyClose);

		if (lazyClose)
			properties_.setString(JDProperties.LAZY_CLOSE, TRUE_);
		else
			properties_.setString(JDProperties.LAZY_CLOSE, FALSE_);

		changes_.firePropertyChange("lazyClose", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "lazyClose: " + lazyClose);  //@A8C
	}

	/**
	*  Sets the AS/400 libraries to add to the server job's library list.
	*  The libraries are delimited by commas or spaces, and
	*  "*LIBL" may be used as a place holder for the server job's
	*  current library list.  The library list is used for resolving
	*  unqualified stored procedure calls and finding schemas in
	*  DatabaseMetaData catalog methods.  If "*LIBL" is not specified, 
	*  the specified libraries will replace the server job's
	*  current library list.
	*  @param libraries The library list.
	**/
	public void setLibraries(String libraries)
	{
		String property = "libraries";
		if (libraries == null)
			throw new NullPointerException("libraries");

		String old = getLibraries();
		properties_.setString(JDProperties.LIBRARIES, libraries);

		changes_.firePropertyChange(property, old, libraries);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + libraries);	 //@A8C
	}

	/**
	*  Sets the AS/400 maximum LOB (large object) size in bytes that
	*  can be retrieved as part of a result set.  LOBs that are larger
	*  than this threshold will be retrieved in pieces using extra
	*  communication to the server.  Larger LOB thresholds will reduce
	*  the frequency of communication to the server, but will download
	*  more LOB data, even if it is not used.  Smaller LOB thresholds may
	*  increase frequency of communication to the server, but will only
	*  download LOB data as it is needed.
	*
	*  @param threshold The lob threshold.  Valid range is 0-16777216.
	*  The default value is 0.
	**/
	public void setLobThreshold(int threshold)
	{
		String property = "threshold";
		if (threshold < 0 || threshold > MAX_THRESHOLD)
			throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

		Integer oldValue = new Integer(getLobThreshold());
		Integer newValue = new Integer(threshold);

		properties_.setString(JDProperties.LOB_THRESHOLD, new Integer(threshold).toString()); 

		changes_.firePropertyChange(property, oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + threshold);	 //@A8C
	}

	/**
	*  Sets the maximum time in seconds that this data source can wait while attempting to connect to a database. 
	*  A value of zero specifies that the timeout is the system default if one exists; otherwise it specifies that 
	*  there is no timeout. The default value is initially zero.
	*  Note: This value is not used or supported.
	*  @param timeout The login timeout in seconds.
	*  @exception SQLException The timeout parameter is not supported.
	**/
	public void setLoginTimeout(int timeout) throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_FUNCTION_NOT_SUPPORTED); 
	}

	/**
	*  Sets the log writer for this data source.
	*  @param writer The log writer; to disable, set to null.
	*  @exception SQLException If a database error occurs.
	**/
	public void setLogWriter(PrintWriter writer) throws SQLException
	{
		String property = "writer";
		if (writer == null)
			throw new NullPointerException(property);

		PrintWriter old = getLogWriter();
		writer_ = writer;
		changes_.firePropertyChange(property, old, writer);

		log_ = new EventLog(writer);
	}

	/**
	*  Sets the AS/400 naming convention used when referring to tables.
	*  @param naming The naming convention.  Valid values include: "sql" (e.g. schema.table)
	*  and "system" (e.g. schema/table).  The default value is "sql".
	**/
	public void setNaming(String naming)
	{
		String property = "naming";
		if (naming == null)
			throw new NullPointerException("naming");
		validateProperty(property, naming, JDProperties.NAMING);

		String old = getNaming();
		properties_.setString(JDProperties.NAMING, naming);

		changes_.firePropertyChange(property, old, naming);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + naming);  //@A8C
	}

	/**
	*  Sets the base name of the SQL package.  Extended dynamic support works
	*  best when this is derived from the application name.  Note that only the
	*  first seven characters are significant.  This property has no effect unless
	*  the extended dynamic property is set to true.  In addition, this property
	*  must be set if the extended dynamic property is set to true.
	*  @param packageName The base name of the SQL package.
	**/
	public void setPackage(String packageName)
	{
		String property = "packageName";
		if (packageName == null)
			throw new NullPointerException(property);

		String old = getPackage();
		properties_.setString(JDProperties.PACKAGE, packageName);

		changes_.firePropertyChange(property, old, packageName);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + packageName);  //@A8C
	}

	/**
	*  Sets whether to add statements to an existing SQL package.  This property
	*  has no effect unless the extended dynamic property is set to true.
	*  @param add If statement can be added to an existing SQL package; false otherwise.
	*  The default value is true.
	**/
	public void setPackageAdd(boolean add)
	{
		Boolean oldValue = new Boolean(isPackageAdd());
		Boolean newValue = new Boolean(add);

		if (add)
			properties_.setString(JDProperties.PACKAGE_ADD, TRUE_);
		else
			properties_.setString(JDProperties.PACKAGE_ADD, FALSE_);

		changes_.firePropertyChange("packageAdd", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "packageAdd: " + add);  //@A8C
	}

	/**
	*  Sets whether to cache SQL packages in memory.  Caching SQL packages locally
	*  reduces the amount of communication to the server in some cases.  This 
	*  property has no effect unless the extended dynamic property is set to true.
	*  @param cache If caching is used; false otherwise.  The default value is false.
	**/
	public void setPackageCache(boolean cache)
	{
		Boolean oldValue = new Boolean(isPackageCache());
		Boolean newValue = new Boolean(cache);

		if (cache)
			properties_.setString(JDProperties.PACKAGE_CACHE, TRUE_);
		else
			properties_.setString(JDProperties.PACKAGE_CACHE, FALSE_);

		changes_.firePropertyChange("packageCache", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "packageCache: " + cache);  //@A8C
	}

	/**
	*  Sets whether to clear SQL packages when they become full.  Clearing an SQL
	*  package results in removing all SQL statements that have been stored in the
	*  SQL package.  This property has no effect unless the extended dynamic property
	*  is set to true.
	*  @param clear If the SQL package are cleared when full; false otherwise.
	*  The default value if false.
	**/
	public void setPackageClear(boolean clear)
	{
		Boolean oldValue = new Boolean(isPackageClear());
		Boolean newValue = new Boolean(clear);

		String value = null;
		if (clear)
			properties_.setString(JDProperties.PACKAGE_CLEAR, TRUE_);
		else
			properties_.setString(JDProperties.PACKAGE_CLEAR, FALSE_);

		changes_.firePropertyChange("packageClear", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "packageClear: " + clear);  //@A8C
	}


	/**
	*  Sets the type of SQL statement to be stored in the SQL package.  This can
	*  be useful to improve the performance of complex join conditions.  This
	*  property has no effect unless the extended dynamic property is set to true.
	*  @param packageCriteria The type of SQL statement.
	*  Valid values include: "default" (only store SQL statements with parameter
	*  markers in the package), and "select" (store all SQL SELECT statements to be
	*  stored in the package).  The default value is "default".
	**/
	public void setPackageCriteria(String packageCriteria)
	{
		String property = "packageCriteria";

		if (packageCriteria == null)
			throw new NullPointerException(property);
		validateProperty(property, packageCriteria, JDProperties.PACKAGE_CRITERIA);

		String old = getPackageCriteria();
		properties_.setString(JDProperties.PACKAGE_CRITERIA, packageCriteria);

		changes_.firePropertyChange(property, old, packageCriteria);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + packageCriteria);  //@A8C
	}

	/**
	*  Sets the action to take when SQL package errors occur.  When an SQL package
	*  error occurs, the driver will optionally throw an SQLException or post a
	*  warning to the Connection, based on the value of this property.  This property
	*  has no effect unless the extended dynamic property is set to true.
	*  @param packageError The action when SQL errors occur.
	*  Valid values include: "exception", "warning", and "none".  The default value is "warning".
	**/
	public void setPackageError(String packageError)
	{
		String property = "packageError";
		if (packageError == null)
			throw new NullPointerException(property);
		validateProperty(property, packageError, JDProperties.PACKAGE_ERROR);

		String old = getPackageError();
		properties_.setString(JDProperties.PACKAGE_ERROR, packageError);

		changes_.firePropertyChange(property, old, packageError);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + packageError);	//@A8C
	}
	/**
	*  Sets the library for the SQL package.  This property has no effect unless
	*  the extended dynamic property is set to true.
	*  @param packageLibrary The SQL package library.  The default library is "QGPL".
	**/
	public void setPackageLibrary(String packageLibrary)
	{
		String property = "packageLibrary";
		if (packageLibrary == null)
			throw new NullPointerException(property);

		String old = getPackageLibrary();
		properties_.setString(JDProperties.PACKAGE_LIBRARY, packageLibrary);

		changes_.firePropertyChange(property, old, packageLibrary);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + packageLibrary);  //@A8C
	}

	/**
	*  Sets the database password.
	*  @param password The password.
	**/
	public void setPassword(String password)
	{
		as400_.setPassword(password);

		log(loader_.getText("AS400_JDBC_DS_PASSWORD_SET"));	 //@A9C
	}

	/**
	*  Sets whether to prefetch data upon executing a SELECT statement.
	*  This will increase performance when accessing the initial rows in the result set.
	*  @param prefetch If prefetch is used; false otherwise.
	*  The default value is to prefectch data.
	**/
	public void setPrefetch(boolean prefetch)
	{
		Boolean oldValue = new Boolean(isPrefetch());
		Boolean newValue = new Boolean(prefetch);

		if (prefetch)
			properties_.setString(JDProperties.PREFETCH, TRUE_);
		else
			properties_.setString(JDProperties.PREFETCH, FALSE_);

		changes_.firePropertyChange("prefetch", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "prefetch: " + prefetch);	 //@A8C
	}

	/**
	*  Sets whether the user should be prompted if a user name or password is
	*  needed to connect to the AS/400 server.  If a connection can not be made
	*  without prompting the user, and this property is set to false, then an 
	*  attempt to connect will fail.
	*  @param prompt true if the user is prompted for signon information; false otherwise.
	*  The default value is false.
	**/
	public void setPrompt(boolean prompt)
	{
		Boolean oldValue = new Boolean(isPrompt());
		Boolean newValue = new Boolean(prompt);

		if (prompt)
			properties_.setString(JDProperties.PROMPT, TRUE_);
		else
			properties_.setString(JDProperties.PROMPT, FALSE_);

		changes_.firePropertyChange("prompt", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "prompt: " + prompt);	 //@A8C
	}

	/**
	*  Sets the JDBC properties.
	*  @param Properties The JDBC properties list.
	**/
	void setProperties(Reference reference)
	{
		/*
		*  Implementation note:  This method is called from AS400JDBCObjectFactory.getObjectInstance
		*/
		if (reference == null)
			throw new NullPointerException("reference");

		Properties properties = new Properties();

		Enumeration list = reference.getAll();
		while (list.hasMoreElements())
		{
			StringRefAddr refAddr = (StringRefAddr)list.nextElement();
			String property = refAddr.getType();
			String value = (String)reference.get(property).getContent();

			if (property.equals(DATABASE_NAME))					// constant identifiers were used to store in JNDI.
				setDatabaseName(value);
			else if (property.equals(DATASOURCE_NAME))
				setDataSourceName(value);
			else if (property.equals(DESCRIPTION))
				setDescription(value);
			else if (property.equals(SERVER_NAME))
				setServerName(value);
			else if (property.equals(USER))
				setUser(value);
			else
			{
				properties.put(property, value);
			}         
		}
		properties_ = new JDProperties(properties, null);
	}

	/**
	*  Sets the name of the proxy server.
	*  @param proxyServer The proxy server.
	**/
	public void setProxyServer(String proxyServer)
	{
		String property = "proxyServer";
		if (proxyServer == null)
			throw new NullPointerException(property);

		String old = getProxyServer();
		properties_.setString(JDProperties.PROXY_SERVER, proxyServer);

		changes_.firePropertyChange(property, old, proxyServer);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + proxyServer);  //@A8C
	}

	/**
	*  Sets the source of the text for REMARKS columns in ResultSets returned
	*  by DatabaseMetaData methods.
	*  @param remarks The text source.
	*  Valid values include: "sql" (SQL object comment) and "system" (OS/400 object description).
	*  The default value is "system".
	**/
	public void setRemarks(String remarks)
	{
		String property = "remarks";
		if (remarks == null)
			throw new NullPointerException(remarks);
		validateProperty(property, remarks, JDProperties.REMARKS);

		String old = getRemarks();
		properties_.setString(JDProperties.REMARKS, remarks);

		changes_.firePropertyChange(property, old, remarks);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + remarks);  //@A8C
	}

	/**
	*  Sets the secondary URL to be used for a connection on the middle-tier's
	*  DriverManager in a multiple tier environment, if it is different than 
	*  already specified.  This property allows you to use this driver to connect 
	*  to databases other than the AS/400. Use a backslash as an escape character 
	*  before backslashes and semicolons in the URL.
	*  @param url The secondary URL.
	**/
	public void setSecondaryUrl(String url)
	{
		if (url == null)
			throw new NullPointerException("url");

		String old = getSecondaryUrl();
		properties_.setString(JDProperties.SECONDARY_URL, url);

		changes_.firePropertyChange("secondaryUrl", old, url);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "secondaryUrl: " + url);	//@A8C
	}

	/**
	*  Sets whether a Secure Socket Layer (SSL) connection is used to communicate
	*  with the server.  SSL connections are only available when connecting to servers
	*  at V4R4 or later.
	*  @param secure true if Secure Socket Layer connection is used; false otherwise.
	*  The default value is false.
	**/
	public void setSecure(boolean secure)
	{
		Boolean oldValue = new Boolean(isSecure());
		Boolean newValue = new Boolean(secure);

		if (secure)
			properties_.setString(JDProperties.SECURE, TRUE_);
		else
			properties_.setString(JDProperties.SECURE, FALSE_);

		changes_.firePropertyChange("secure", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "secure: " + secure);	 //@A8C
	}

	/**
	*  Sets the AS/400 server name.
	*  @param serverName The server name.
	**/
	public void setServerName(String serverName)
	{
		String property = SERVER_NAME;
		if (serverName == null)
			throw new NullPointerException(property);

		String old = getServerName();

		try
		{
			as400_.setSystemName(serverName);
		}
		catch (PropertyVetoException pv)
		{ /* ignore */
		}

		changes_.firePropertyChange(property, old, serverName);

		logProperty ("server name", as400_.getSystemName());
	}


  // @j1 new method
  /**
  *  Enables tracing of the JDBC server job.
  *  If tracing is enabled, tracing is started when
  *  the client connects to the server, and ends when the connection
  *  is disconnected.  Tracing must be started before connecting to
  *  the server since the client enables server tracing only at connect time.
  *
  *  <P>
  *  Trace data is collected in spooled files on the server.  Multiple
  *  levels of server tracing can be turned on in combination by adding
  *  the constants and passing that sum on the set method.  For example,
  *  <pre>
  *  dataSource.setServerTraceCategories(AS400JDBCDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
  *  </pre>
  *  @param traceCategories level of tracing to start.
  *  <p>Valid values include:
  *  <ul>
  *  <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
  *                               The numeric value of this constant is 2.
  *  <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
  *                         The numeric value of this constant is 4.
  *  <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
  *                           The numeric value of this constant is 8.
  *  <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
  *                         The numeric value of this constant is 16.
  *  <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
  *                             The numeric value of this constant is 32.
  *  </ul>
  *  <P>
  *  Tracing the JDBC server job will use significant amounts of server resources.
  *  Additional processor resource is used to collect the data, and additional
  *  storage is used to save the data.  Turn on server tracing only to debug
  *  a problem as directed by IBM service.
  *
  *
  **/
  public void setServerTraceCategories(int traceCategories)
  {
    String property = "serverTrace";

    Integer oldValue = new Integer(getServerTraceCategories());
    Integer newValue = new Integer(traceCategories);

    properties_.setString(JDProperties.TRACE_SERVER, newValue.toString());

    changes_.firePropertyChange(property, oldValue, newValue);

    if (JDTrace.isTraceOn()) //@A8C
      JDTrace.logInformation (this, property + ": " + traceCategories);
  }




	// @A2A
	/**
	* Sets the JDBC driver implementation.
	* The AS/400 Toolbox for Java JDBC driver
	* chooses which JDBC driver implementation
	* to use based on the environment. If the
	* environment is an AS/400 Java Virtual
	* Machine on the same AS/400 as the
	* database to which the program is connecting,
	* the native AS/400 Developer Kit for Java
	* JDBC driver is used. In any other
	* environment, the AS/400 Toolbox for Java
	* JDBC driver is used. This property has no
	* effect if the "secondary URL" property is set.
	* This property cannot be set to "native" if the
	* environment is not an AS/400 Java Virtual
	* Machine.
	* param driver The driver value.
	*  <p>Valid values include: 
	*  <ul> 
	*  <li>"default" (base the implementation on the environment) 
	*  <li>"toolbox" (use the AS/400 Toolbox for Java JDBC driver) 
	*  <li>"native" (use the AS/400 Developer Kit for Java JDBC driver)
	*  </ul>
	*  The default value is "default".
	**/
	public void setDriver(String driver)
	{
		String property = "driver";
		if (driver == null)
			throw new NullPointerException(property);

		validateProperty(property, driver, JDProperties.DRIVER);
		String old = getDriver();

		properties_.setString(JDProperties.DRIVER, driver);

		changes_.firePropertyChange(property, old, driver);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + driver);  //@A8C
	}

	/**
	*  Sets the three-character language id to use for selection of a sort sequence.
	*  This property has no effect unless the sort property is set to "language".
	*  @param language The three-character language id.
	*  The default value is based on the locale.
	**/
	public void setSortLanguage(String language)
	{
		if (language == null)
			throw new NullPointerException("language");

		String old = getSortLanguage();
		properties_.setString(JDProperties.SORT_LANGUAGE, language);

		changes_.firePropertyChange("sortLanguage", old, language);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "sortLanguage: " + language);	 //@A8C
	}

	/**
	*  Sets the library and file name of a sort sequence table stored on the AS/400 server.
	*  This property has no effect unless the sort property is set to "table".
	*  The default is an empty String ("").
	*  @param table The qualified sort table name.
	**/
	public void setSortTable(String table)
	{
		if (table == null)
			throw new NullPointerException("table");

		String old = getSortTable();
		properties_.setString(JDProperties.SORT_TABLE, table);

		changes_.firePropertyChange("sortTable", old, table);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "sortTable: " + table);  //@A8C
	}

	/**
	*  Sets how the AS/400 server treats case while sorting records.  This property has no
	*  effect unless the sort property is set to "language".
	*  @param sortWeight The sort weight.
	*  Valid values include: "shared" (upper- and lower-case characters are sorted as the
	*  same character) and "unique" (upper- and lower-case characters are sorted as 
	*  different characters).  The default value is "shared".
	**/
	public void setSortWeight(String sortWeight)
	{
		String property = "sortWeight";
		if (sortWeight == null)
			throw new NullPointerException(property);

		validateProperty(property, sortWeight, JDProperties.SORT_WEIGHT);

		String old = getSortWeight();
		properties_.setString(JDProperties.SORT_WEIGHT, sortWeight);

		changes_.firePropertyChange(property, old, sortWeight);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + sortWeight);  //@A8C
	}

	/**
	*  Sets whether a thread is used.  
	*  @param threadUsed true if a thread is used; false otherwise.
	*  The default value is true.
	**/
	public void setThreadUsed(boolean threadUsed)
	{
		Boolean oldValue = new Boolean(isThreadUsed());
		Boolean newValue = new Boolean(threadUsed);

		if (threadUsed)
			properties_.setString(JDProperties.THREAD_USED, TRUE_);
		else
			properties_.setString(JDProperties.THREAD_USED, FALSE_);

		changes_.firePropertyChange("threadUsed", oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "threadUsed: " + threadUsed);	 //@A8C
	}

	/**
	*  Sets the AS/400 time format used in time literals with SQL statements.
	*  @param timeFormat The time format.
	*  <p>Valid values include: 
	*  <ul>
	*    <li> "hms"
	*    <li> "usa"
	*    <li> "iso"
	*    <li> "eur"
	*    <li> "jis"
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public void setTimeFormat(String timeFormat)
	{
		String property = "timeFormat";
		if (timeFormat == null)
			throw new NullPointerException(property);
		validateProperty(property, timeFormat, JDProperties.TIME_FORMAT);

		String old = getTimeFormat();
		properties_.setString(JDProperties.TIME_FORMAT, timeFormat);

		changes_.firePropertyChange(property, old, timeFormat);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + timeFormat);  //@A8C
	}

	/**
	*  Sets the AS/400 time separator used in time literals within SQL statements.
	*  This property has no effect unless the time format property is set to "hms".
	*  @parm timeSeparator The time separator.  
	*  <p>Valid values include:
	*  <ul>
	*    <li> ":" (colon)
	*    <li> "." (period)
	*    <li> "," (comma)
	*    <li> " " (space)
	*    <li> ""  (server job value) - default.
	*  </ul>
	*  The default value is based on the server job.
	**/
	public void setTimeSeparator(String timeSeparator)
	{
		String property = "timeSeparator";
		if (timeSeparator == null)
			throw new NullPointerException(property);
		validateProperty(property, timeSeparator, JDProperties.TIME_SEPARATOR);

		String old = getTimeSeparator(); 
		properties_.setString(JDProperties.TIME_SEPARATOR, timeSeparator);

		changes_.firePropertyChange(property, old, timeSeparator);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + timeSeparator);	 //@A8C
	}

	/**
	*  Sets whether trace messages should be logged.  Trace messages are
	*  useful for debugging programs that call JDBC.  However, there is a 
	*  performance penalty associated with logging trace messages, so this
	*  property should only be set to true for debugging.  Trace messages
	*  are logged to System.out.
	*  @param trace true if trace message are logged; false otherwise.
	*  The default value is false.
	**/
	public void setTrace(boolean trace)
	{
		Boolean oldValue = new Boolean(isTrace());
		Boolean newValue = new Boolean(trace);

		if (trace)
			properties_.setString(JDProperties.TRACE, TRUE_);
		else
			properties_.setString(JDProperties.TRACE, FALSE_);

		changes_.firePropertyChange("trace", oldValue, newValue);

		if (trace)
		{
			if (!JDTrace.isTraceOn ())
				JDTrace.setTraceOn (true);
		}
		else
			JDTrace.setTraceOn (false);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, "trace: " + trace);  //@A8C
	}

	/**
	*  Sets the AS/400 server's transaction isolation.
	*  @param String transactionIsolation The transaction isolation level.
	*  <p>Valid values include:
	*  <ul>
	*    <li> "none"
	*    <li> "read uncommitted"  - The default value.
	*    <li> "read committed"
	*    <li> "repeatable read"
	*    <li> "serializable"
	*  </ul>
	**/
	public void setTransactionIsolation(String transactionIsolation)
	{
		String property = "transactionIsolation";

		if (transactionIsolation == null)
			throw new NullPointerException(property);
		validateProperty(property, transactionIsolation, JDProperties.TRANSACTION_ISOLATION);

		String old = getTransactionIsolation();

		properties_.setString(JDProperties.TRANSACTION_ISOLATION, transactionIsolation);

		changes_.firePropertyChange(property, old, transactionIsolation);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + transactionIsolation);	//@A8C
	}

	/**
	*  Sets whether binary data is translated.  If this property is set
	*  to true, then BINARY and VARBINARY fields are treated as CHAR and
	*  VARCHAR fields.
	*  @param translate true if binary data is translated; false otherwise.
	*  The default value is false.
	**/
	public void setTranslateBinary(boolean translate)
	{
		String property = "translateBinary";

		Boolean oldValue = new Boolean(isTranslateBinary());
		Boolean newValue = new Boolean(translate);

		if (translate)
			properties_.setString(JDProperties.TRANSLATE_BINARY, TRUE_);
		else
			properties_.setString(JDProperties.TRANSLATE_BINARY, FALSE_);

		changes_.firePropertyChange(property, oldValue, newValue);

		if (JDTrace.isTraceOn()) //@A8C 
			JDTrace.logInformation (this, property + ": " + translate);	 //@A8C
	}

	/**
	*  Sets the database user.
	*  @param user The user.
	**/
	public void setUser(String user)
	{
		String property = "user";

		String old = getUser();

		try
		{
			as400_.setUserId(user);
		}
		catch (PropertyVetoException vp)
		{ /* ignore */
		}

		changes_.firePropertyChange(property, old, user);

		logProperty ("user", as400_.getUserId());
	}

	/**
	*  Validates the property value.
	*  @param property The property name.
	*  @param value The property value.
	*  @param index The property index.
	**/
	private void validateProperty(String property, String value, int index)
	{
		if (value.length() != 0)
		{											// @A7A
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
		}																	// @A7A
	}

	/**
	*  Serializes the server and user information.
	*  @param out The output stream.
	*  @exception IOException If a file I/O error occurs.
	**/
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		String server = getServerName();
		if (!server.equals(""))
			serialServerName_ = server;

		String user = getUser();
		if (!user.equals(""))
			serialUserName_ = user;

		// Serialize the object.
		out.defaultWriteObject();
	}

	/**
	*  Returns the string representation of the object.
	*  @return The string representation.
	**/
	public String toString()
	{
		/*
		* Implementation note: Used only for tracing information.
		*/
		String name = getDataSourceName();
		if (name == null)
			name = "";
		return name;
	}
}
