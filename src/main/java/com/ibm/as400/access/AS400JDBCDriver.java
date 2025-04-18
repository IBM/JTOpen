///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCDriver.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.InetAddress;				
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

/* endif */ 
import java.util.Properties;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
A JDBC 3.0/4.0/4.2/4.3 driver that accesses DB2 for IBM i databases.

<p>To use this driver, the application or caller must register 
the driver with the JDBC DriverManager prior to JDBC 4.0.  
This class also registers 
itself automatically when it is loaded.
When using the JDBC 4.0 or later versions, the driver is 
automatically registered. 

<p>After registering the driver, applications make connection
requests to the DriverManager, which dispatches them to the
appropriate driver.  This driver accepts connection requests
for databases specified by the URLs that match the following syntax:

<pre>
jdbc:as400://<em>system-name</em>/<em>default-schema</em>;<em>properties</em>
</pre>

<p>The driver uses the specified system name to connect
to a corresponding IBM i system.  
If an IPV6 address is used as the system name, it must be enclosed within braces, 
i.e. [fd13:ac12:18:17::16].   
If a system name is not specified, then the user will be prompted.  

<p>The default SQL schema is optional and the driver uses it to resolve 
unqualified names in SQL statements.  If no default SQL schema is set, then
the driver resolves unqualified names based on the naming convention
for the connection.  If SQL naming is being used, and no default SQL schema
is set, then the driver resolves unqualified names using the schema with 
the same name as the user.  If system naming is being used, and no
default SQL schema is set, then the driver resolves unqualified names using
the server job's library list.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
for more details on how to set the naming convention
and library list.

<p>Several properties can optionally be set within the URL.  They are 
separated by semicolons and are in the form:
<pre>
<em>name1</em>=<em>value1</em>;<em>name2</em>=<em>value2</em>;<em>...</em>
</pre>
See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
for a complete list of properties supported by this driver.

<p>The following example URL specifies a connection to the
database on system <em>mysystem.helloworld.com</em> with
<em>mylibrary</em> as the default SQL schema.  The connection will
use the system naming convention and return full error messages:
<pre>
jdbc:as400://mysystem.helloworld.com/mylibrary;naming=system;errors=full
</pre>
**/
//
// Implementation note:
//
// 1. A goal stated in the JDBC specification is to keep
//    the Driver class as small and standalone as possible,
//    so that it can be quickly loaded when choosing a
//    driver for a particular database.
//
// 2. It was proposed that we also accept URLs with the
//    "db2" subprotocol.  This would make us consistent with
//    other IBM drivers.  In addition, it would also allow
//    developers to hardcode URLs in programs and they would
//    run as-is with both this driver and the "native" driver.
//
//    We realized, though, that if running on a client with
//    both this driver and another DB2 client for that platform,
//    how do the drivers differentiate themselves?  Therefore
//    we are chosing NOT to recognized the "db2" subprotocol.
//    Instead, suggest to developers to externalize the URL
//    to users, rather than hardcoding it.
//

//
// Note:  Change log is now in Copyright.java
// 


public class AS400JDBCDriver
implements java.sql.Driver
{
	// Constants.

	static final int    MAJOR_VERSION_          = Copyright.MAJOR_VERSION;
	static final int    MINOR_VERSION_          = Copyright.MINOR_VERSION;
	static final String DATABASE_PRODUCT_NAME_  = "DB2 UDB for AS/400";  // @D0A
	static final String DRIVER_NAME_            = "AS/400 Toolbox for Java JDBC Driver"; // @D0C @C5C @C6C
	static final String DRIVER_LEVEL_            = Copyright.DRIVER_LEVEL;
	
	public static final String PROPERTY_SSL_SOCKET_FACTORY = "property.ssl-socket-factory";

/* ifdef JDBC40 */
    public static final int JDBC_MAJOR_VERSION_ = 4; // JDBC spec version: 4.0
/* endif */ 
/* ifndef JDBC40 
    public static final int JDBC_MAJOR_VERSION_ = 3; // JDBC spec version: 3.0
 endif */ 
    
/* ifdef JAVA9
    public static final int JDBC_MINOR_VERSION_ = 3; 
endif JAVA9 */
    
/* ifndef JAVA9 */    
/* ifdef JDBC42 */
    public static final int JDBC_MINOR_VERSION_ = 2;
/* endif */ 
/* ifndef JDBC42 
    public static final int JDBC_MINOR_VERSION_ = 0;
 endif */ 
  
    
/* endif JAVA9 */ 

	// This string "9999:9999" is returned when resource
	// bundle errors occur.  No significance to this string,
	// except that Client Access used to use it.  It would
	// probably be more helpful to return some other string.
	//
	private static final String MRI_NOT_FOUND_  = "9999:9999";



	// Private data.

	// Toolbox resources needed in proxy jar file.            @A1C
	private static ResourceBundle resources_;
	// Toolbox resources NOT needed in proxy jar file.        @A1A
	private static ResourceBundle resources2_;

  private static final String CLASSNAME = "com.ibm.as400.access.AS400JDBCDriver";

  private static Driver nativeDriver = null;


	/**
	Static initializer.  Registers the JDBC driver with the JDBC
	driver manager and loads the appropriate resource bundle for
	the current locale.
	**/
	static {
		try
		{
			// Log where the toolbox is loaded from @B1A
	     if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME, Trace.JDBC);

	        DriverManager.registerDriver (new AS400JDBCDriver ());
			resources_  = ResourceBundle.getBundle ("com.ibm.as400.access.JDMRI");
			resources2_ = ResourceBundle.getBundle ("com.ibm.as400.access.JDMRI2");
			// Note: When using the proxy jar file, we do not expect to find JDMRI2.
		}
		catch (MissingResourceException e)
		{

			// Catch the exception.  This is because exceptions
			// thrown from static initializers are hard to debug.
			// Instead, we will handle the error when the
			// driver needs to get at particular methods.
			// See getResource().
		}
		catch (SQLException e)
		{
			// Ignore.
		}
	}



	/**
	Indicates if the driver understands how to connect
	to the database named by the URL.
	
	@param  url     The URL for the database.
	@return         true if the driver understands how
					to connect to the database; false
					otherwise.
	
	@exception SQLException If an error occurs.
	**/
	public boolean acceptsURL (String url)
	throws SQLException
	{
		JDDataSourceURL dataSourceUrl = new JDDataSourceURL (url);
		return dataSourceUrl.isValid ();
	}

	
	 /**
  Connects to the database named by the specified URL using the 
  specified userid and password. 
  
  @param  url     The URL for the database.
  @param  userid   The userid for the connection
  @param  password  The password for the connection. The caller should clear the
                    password from the array after the method returns. 
  @return         The connection to the database or null if
          the driver does not understand how to connect
          to the database.
  
  @exception SQLException If the driver is unable to make the connection.
  **/
  public java.sql.Connection connect (String url, String userid, char[] password)
  throws SQLException
  {
    return connect(url, userid, password, (char[])null); 
  }


  /**
  Connects to the database named by the specified URL using the 
  specified userid, password, and additional authentication factor. 
  
  @param  url       The URL for the database.
  @param  userid    The userid for the connection
  @param  password  The password for the connection. The caller should clear the
                    password from the array after the method returns. 
  @param  additionalAuthenticationFactor The additional authentication factor, or null
                    if not providing one
  @return           The connection to the database or null if
                    the driver does not understand how to connect
                    to the database.
  
  @exception SQLException If the driver is unable to make the connection.
  **/
  public java.sql.Connection connect (String url, String userid, char[] password, char[] additionalAuthenticationFactor)
  throws SQLException
  {
    Properties properties = new Properties(); 
    properties.put("user",  userid); 
    return connect(url, properties, password, additionalAuthenticationFactor); 
  }
	
 
  /**
  Connects to the database named by the specified URL.
  There are many optional properties that can be specified.
  Properties can be specified either as part of the URL or in
  a java.util.Properties object.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
  for a complete list of properties
  supported by this driver.
  
  @param  url     The URL for the database.
  @param  info    The connection properties.
  @return         The connection to the database or null if
          the driver does not understand how to connect
          to the database.
  
  @exception SQLException If the driver is unable to make the connection.
  **/
  public java.sql.Connection connect (String url,
                    Properties info)
  throws SQLException
  {
    
    return connect(url, info, null); 
  }

	/**
	Connects to the database named by the specified URL.
	There are many optional properties that can be specified.
	Properties can be specified either as part of the URL or in
	a java.util.Properties object.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
	for a complete list of properties
	supported by this driver.
	
	@param  url     The URL for the database.
	@param  info    The connection properties.
	@param  password  The password as a char array.  The caller should clear the
	                 char array after returning. 
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (String url,
										Properties info,
										char[] password)
	throws SQLException
	{
		return connect(url, info, password, (char[])null);
	}


	/**
	Connects to the database named by the specified URL.
	There are many optional properties that can be specified.
	Properties can be specified either as part of the URL or in
	a java.util.Properties object.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
	for a complete list of properties
	supported by this driver.
	
    @param  url     The URL for the database.
    @param  info    The connection properties.
    @param  password  The password as a char array.  The caller should clear the
	                 char array after returning. 
    @param  additionalAuthenticationFactor The additional authentication factor (or null
	                 if not providing one)
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (String url,
										Properties info,
										char[] password,
										char[] additionalAuthenticationFactor)
	throws SQLException
	{
		// Check first thing to see if the trace property is
		// turned on.  This way we can trace everything, including
		// the important stuff like loading the properties.
		JDDataSourceURL   dataSourceUrl = new JDDataSourceURL (url);
		Properties urlProperties = dataSourceUrl.getProperties ();

		// If trace property was set to true, turn on tracing.  If trace property was set to false,
		// turn off tracing.  If trace property was not set, do not change.
		if (JDProperties.isTraceSet (urlProperties, info) == JDProperties.TRACE_SET_ON)
		{	  //@B5C
			if (! JDTrace.isTraceOn ())
				JDTrace.setTraceOn (true);
		}
		else if (JDProperties.isTraceSet (urlProperties, info) == JDProperties.TRACE_SET_OFF) //@B5A
		{
			//@B5A
			if (JDTrace.isTraceOn ())										 //@B5A
				JDTrace.setTraceOn (false);									   //@B5A
		}																   //@B5A
		//@B4D Deleted lines because trace should not be set off just because property
		//@B4D not specified.
		//@B4D else
		//@B4D JDTrace.setTraceOn (false);

                // If toolbox trace is set to datastream.  Turn on datastream tracing.
                if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_DATASTREAM)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDatastreamOn(true);
                }
                // If toolbox trace is set to diagnostic.  Turn on diagnostic tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_DIAGNOSTIC)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDiagnosticOn(true);
                }
                // If toolbox trace is set to error.  Turn on error tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_ERROR)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceErrorOn(true);
                }
                // If toolbox trace is set to information.  Turn on information tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_INFORMATION)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceInformationOn(true);
                }
                // If toolbox trace is set to warning.  Turn on warning tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_WARNING)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceWarningOn(true);
                }
                // If toolbox trace is set to conversion.  Turn on conversion tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_CONVERSION)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceConversionOn(true);
                }
                // If toolbox trace is set to proxy.  Turn on proxy tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_PROXY)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceProxyOn(true);
                }
                // If toolbox trace is set to pcml.  Turn on pcml tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_PCML)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTracePCMLOn(true);
                }
                // If toolbox trace is set to jdbc.  Turn on jdbc tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_JDBC)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceJDBCOn(true);
                }
                // If toolbox trace is set to all.  Turn on tracing for all categories.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_ALL)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceAllOn(true);
                }
                // If toolbox trace is set to thread.  Turn on thread tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_THREAD)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceThreadOn(true);
                }
                // If toolbox trace is set to none.  Turn off tracing.
                else if (JDProperties.isToolboxTraceSet (urlProperties, info) == JDProperties.TRACE_TOOLBOX_NONE)
                {
                    //@K1A
                    if (Trace.isTraceOn())
                    {
                        Trace.setTraceOn(false);
                    }
                }

                
    if (JDTrace.isTraceOn()) {
      String traceUrl = url; 
      int passwordIndex = url.indexOf("password=");
      if (passwordIndex >= 0) {
        int semicolonIndex = url.indexOf(";",passwordIndex); 
        if (semicolonIndex < 0) {
          traceUrl = url.substring(0,passwordIndex)+"password=******"; 
        } else {
          traceUrl = url.substring(0,passwordIndex)+"password=******"+url.substring(semicolonIndex); 
        }
      }
      JDTrace.logInformation (this,"connect called with URL: "+traceUrl);  
    }

		JDProperties jdProperties = new JDProperties (urlProperties, info, password, additionalAuthenticationFactor);

		// Initialize the connection if the URL is valid.
		Connection connection = null;										 //@A0C
		if (dataSourceUrl.isValid ())
			connection = initializeConnection (dataSourceUrl, jdProperties);  //@A0C

		return connection;
	}


	//@B5A
	/**
	Connects to the database on the specified system.
	<p>Note: Since this method is not defined in the JDBC Driver interface,
	you typically need to create a Driver object in order
	to call this method:
	<blockquote><pre>
	AS400JDBCDriver d = new AS400JDBCDriver();
	AS400 o = new AS400(myAS400, myUserId, myPwd);
	Connection c = d.connect (o);
	</pre></blockquote>
	
	
	@param  system   The IBM i system to connect.
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (AS400 system)
	throws SQLException
	{
		if (system == null)
			throw new NullPointerException("system");

        AS400 newAs400 = AS400.newInstance(system.isSecure(), system);
        newAs400.setStayAlive(system.getStayAlive());
        
		return initializeConnection(newAs400);

		// Initialize the connection.
		//@B7D Connection connection = null;                                        
		//@B7D connection = initializeConnection (o); 
		//@B7D return connection;
	}

        //@KKB
	/**
	Connects to the database on the specified system.
	<p>Note: Since this method is not defined in the JDBC Driver interface,
	you typically need to create a Driver object in order
	to call this method:
	<blockquote><pre>
	AS400JDBCDriver d = new AS400JDBCDriver();
	AS400 o = new AS400(myAS400, myUserId, myPwd);
	Connection c = d.connect (o, false);
	</pre></blockquote>
	
	
	@param  system   The IBM i system to connect.
        @param  clone    True if the AS400 object should be cloned, false otherwise.
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (AS400 system, boolean clone)
	throws SQLException
	{
		if (system == null)
			throw new NullPointerException("system");

                if(!clone)  //Do not clone the AS400 object, use the one passed in
                    return initializeConnection(system);
                else        //clone the AS400 object
                    return connect(system);
	}


        //@D4A
	/**
	Connects to the database on the specified system.
	<p>Note: Since this method is not defined in the JDBC Driver interface,
	you typically need to create a Driver object in order
	to call this method:
	<blockquote><pre>
	AS400JDBCDriver d = new AS400JDBCDriver();
	AS400 o = new AS400(myAS400, myUserId, myPwd);
        String mySchema = "defaultSchema";
        Properties prop = new Properties();
	Connection c = d.connect (o, prop, mySchema, false);
	</pre></blockquote>
	
	
	@param  system   The IBM i system to connect.
        @param  info     The connection properties.
        @param  schema   The default SQL schema or null meaning no default SQL schema specified.
        @param  clone    True if the AS400 object should be cloned, false otherwise.
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (AS400 system, Properties info, String schema, boolean clone)
	throws SQLException
	{
		if (system == null)
			throw new NullPointerException("system");

		if (info == null)
			throw new NullPointerException("properties");

        //@PDD not needed, just pass in null to isXTraceSet(null, info) below
        //Properties urlProperties = new Properties();

		// Check first thing to see if the trace property is
		// turned on.  This way we can trace everything, including
		// the important stuff like loading the properties.

		// If trace property was set to true, turn on tracing.  If trace property was set to false,
		// turn off tracing.  If trace property was not set, do not change.
		if (JDProperties.isTraceSet (null, info) == JDProperties.TRACE_SET_ON)
		{
			if (! JDTrace.isTraceOn ())
				JDTrace.setTraceOn (true);
		}
		else if (JDProperties.isTraceSet (null, info) == JDProperties.TRACE_SET_OFF)
		{
			if (JDTrace.isTraceOn ())
				JDTrace.setTraceOn (false);
		}

                // If toolbox trace is set to datastream.  Turn on datastream tracing.
                if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_DATASTREAM)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDatastreamOn(true);
                }
                // If toolbox trace is set to diagnostic.  Turn on diagnostic tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_DIAGNOSTIC)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDiagnosticOn(true);
                }
                // If toolbox trace is set to error.  Turn on error tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_ERROR)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceErrorOn(true);
                }
                // If toolbox trace is set to information.  Turn on information tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_INFORMATION)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceInformationOn(true);
                }
                // If toolbox trace is set to warning.  Turn on warning tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_WARNING)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceWarningOn(true);
                }
                // If toolbox trace is set to conversion.  Turn on conversion tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_CONVERSION)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceConversionOn(true);
                }
                // If toolbox trace is set to proxy.  Turn on proxy tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_PROXY)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceProxyOn(true);
                }
                // If toolbox trace is set to pcml.  Turn on pcml tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_PCML)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTracePCMLOn(true);
                }
                // If toolbox trace is set to jdbc.  Turn on jdbc tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_JDBC)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceJDBCOn(true);
                }
                // If toolbox trace is set to all.  Turn on tracing for all categories.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_ALL)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceAllOn(true);
                }
                // If toolbox trace is set to thread.  Turn on thread tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_THREAD)
                {
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceThreadOn(true);
                }
                // If toolbox trace is set to none.  Turn off tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_NONE)
                {
                    if (Trace.isTraceOn())
                    {
                        Trace.setTraceOn(false);
                    }
                }

                if(!clone)  //Do not clone the AS400 object, use the one passed in
                    return initializeConnection(schema, info, system);
                else        //clone the AS400 object
                {
                    AS400 newAs400 = AS400.newInstance(system.isSecure(), system);
                    newAs400.setStayAlive(system.getStayAlive());
                    return initializeConnection(schema, info, newAs400);
                }
	}

	//@B5A
	/**
	Connects to the database on the specified system.
	There are many optional properties that can be specified.
	Properties can be specified in
	a java.util.Properties object.  See
<a href="doc-files/JDBCProperties.html" target="_blank">JDBC properties</a>
	for a complete list of properties
	supported by this driver.  
	
	<p>Note: Since this method is not defined in the JDBC Driver interface,
	you typically need to create a Driver object in order
	to call this method:
	<blockquote><pre>
	AS400JDBCDriver d = new AS400JDBCDriver();
	String mySchema = "defaultSchema";
	Properties p = new Properties();
	AS400 o = new AS400(myAS400, myUserId, myPwd);
	Connection c = d.connect (o, p, mySchema);
	</pre></blockquote>
	
	@param  system  The IBM i system to connect.
	@param  info    The connection properties.
	@param  schema  The default SQL schema or null meaning no default SQL schema specified.
	@return         The connection to the database or null if
					the driver does not understand how to connect
					to the database.
	
	@exception SQLException If the driver is unable to make the connection.
	**/
	public java.sql.Connection connect (AS400 system, Properties info, String schema)
	throws SQLException
	{
		if (system == null)
			throw new NullPointerException("system");

		if (info == null)
			throw new NullPointerException("properties");

		//@B7D AS400 o = new AS400(system);
        //@PDD not needed, just pass in null to isXTraceSet(null, info) below
		//Properties urlProperties = new Properties();

		// Check first thing to see if the trace property is
		// turned on.  This way we can trace everything, including
		// the important stuff like loading the properties.

		// If trace property was set to true, turn on tracing.  If trace property was set to false,
		// turn off tracing.  If trace property was not set, do not change.
		if (JDProperties.isTraceSet (null, info) == JDProperties.TRACE_SET_ON)
		{
			if (! JDTrace.isTraceOn ())
				JDTrace.setTraceOn (true);
		}
		else if (JDProperties.isTraceSet (null, info) == JDProperties.TRACE_SET_OFF)
		{
			if (JDTrace.isTraceOn ())
				JDTrace.setTraceOn (false);
		}

                // If toolbox trace is set to datastream.  Turn on datastream tracing.
                if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_DATASTREAM)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDatastreamOn(true);
                }
                // If toolbox trace is set to diagnostic.  Turn on diagnostic tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_DIAGNOSTIC)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceDiagnosticOn(true);
                }
                // If toolbox trace is set to error.  Turn on error tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_ERROR)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceErrorOn(true);
                }
                // If toolbox trace is set to information.  Turn on information tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_INFORMATION)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceInformationOn(true);
                }
                // If toolbox trace is set to warning.  Turn on warning tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_WARNING)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceWarningOn(true);
                }
                // If toolbox trace is set to conversion.  Turn on conversion tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_CONVERSION)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceConversionOn(true);
                }
                // If toolbox trace is set to proxy.  Turn on proxy tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_PROXY)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceProxyOn(true);
                }
                // If toolbox trace is set to pcml.  Turn on pcml tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_PCML)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTracePCMLOn(true);
                }
                // If toolbox trace is set to jdbc.  Turn on jdbc tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_JDBC)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceJDBCOn(true);
                }
                // If toolbox trace is set to all.  Turn on tracing for all categories.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_ALL)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceAllOn(true);
                }
                // If toolbox trace is set to thread.  Turn on thread tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_THREAD)
                {
                    //@K1A
                    if (! Trace.isTraceOn())
                    {
                        Trace.setTraceOn(true);
                    }
                    Trace.setTraceThreadOn(true);
                }
                // If toolbox trace is set to none.  Turn off tracing.
                else if (JDProperties.isToolboxTraceSet (null, info) == JDProperties.TRACE_TOOLBOX_NONE)
                {
                    //@K1A
                    if (Trace.isTraceOn())
                    {
                        Trace.setTraceOn(false);
                    }
                }

		//@PDD not used JDProperties jdProperties = new JDProperties (null, info);

        AS400 newAs400 = AS400.newInstance(system.isSecure(), system);
        newAs400.setStayAlive(system.getStayAlive());
        return initializeConnection(schema, info, newAs400);

		// Initialize the connection if the URL is valid.
		//@B7D Connection connection = null;                                        
		//@B7D connection = initializeConnection (schema, info, o);  

		//@B7D return connection;
	}




	/**
	Returns the driver's major version number.
	
	@return         The major version number.
	**/
	public int getMajorVersion ()
	{
		return MAJOR_VERSION_;
	}



	/**
	Returns the driver's minor version number.
	
	@return         The minor version number.
	**/
	public int getMinorVersion ()
	{
		return MINOR_VERSION_;
	}



	/**
	Returns an array of DriverPropertyInfo objects that
	describe the properties that are supported by this
	driver.
	
	@param  url     The URL for the database.
	@param  info    The connection properties.
	@return         The descriptions of all possible properties or null if
					the driver does not understand how to connect to the
					database.
	
	@exception SQLException If an error occurs.
	**/
	public DriverPropertyInfo[] getPropertyInfo (String url,
												 Properties info)
	throws SQLException
	{
		JDDataSourceURL dataSourceUrl = new JDDataSourceURL (url);

		DriverPropertyInfo[] dpi = null;
		if (dataSourceUrl.isValid ())
		{
			JDProperties properties = new JDProperties (dataSourceUrl.getProperties(), info, null, null);
			dpi = properties.getInfo ();
		}

		return dpi;
	}



	/**
	Returns a resource from the resource bundle.
	
	@param  key     The resource key.
	@param replacementVariables -- replacement variables for the message
	@return         The resource String.
	**/
	static String getResource (String key, String[]  replacementVariables)
	{
		// If the resource bundle or resource is not found,
		// do not thrown an exception.  Instead, return a
		// default string.  This is because some JVMs will
		// not recover quite right from such errors, and
		// claim a security exception (e.g. Netscape starts
		// looking in the client class path, which is
		// not allowed.)
		//
		String resource;
		if (resources_ == null)
			resource = MRI_NOT_FOUND_;
		else
		{
			try
			{
			  
				resource = resources_.getString (key);
				
				
			}
			catch (MissingResourceException e)
			{
				if (resources2_ == null)					   //@A1A
					resource = MRI_NOT_FOUND_;					 //@A1A
				else
				{										  //@A1A
					try
					{										 //@A1A
						resource = resources2_.getString (key);	   //@A1A
					}											 //@A1A
					catch (MissingResourceException e1)
					{		 //@A1A
						JDTrace.logInformation (AS400JDBCDriver.class,
												"Missing resource [" + key + "]"); //@A1A
						resource = MRI_NOT_FOUND_;
					}
				}
			}
		}
		if (replacementVariables != null) { 
		  resource = substitute(resource, replacementVariables); 
		}
		return resource;
	}

  // Replaces substitution variables in a string.
  // @param  text  The text string, with substitution variables (e.g. "Error &0 in table &1.")
  // @param  values  The replacement values.
  // @return  The text string with all substitution variables replaced.
  static String substitute (String text, Object[] values)
  {
      String result = text;
      if (values != null) { 
      for (int i = 0; i < values.length; ++i) {
          String variable = "&" + i;
          int j = result.indexOf (variable);
          if (j >= 0) {
              StringBuilder buffer = new StringBuilder();
              buffer.append(result.substring(0, j));
              buffer.append(values[i].toString ());
              buffer.append(result.substring(j + variable.length ()));
              result = buffer.toString ();
          }
      }
      }
      return result;
  }


	//@B3A  - This logic was formerly in the initializeConnection() method.
	static AS400 initializeAS400(JDDataSourceURL dataSourceUrl,
								 JDProperties jdProperties)
	throws SQLException //@pw1
	{
		// We must handle the different combinations of input
		// user names and passwords.
		String serverName = dataSourceUrl.getServerName();
		String userName   = jdProperties.getString (JDProperties.USER);
		char[] clearPassword   = jdProperties.getClearPassword(); 
		char[] additionalAuthenticationFactor = jdProperties.getAdditionalAuthenticationFactor(); 
		String prompt     = jdProperties.getString (JDProperties.PROMPT);	// @B8C
		boolean secure    = jdProperties.getBoolean (JDProperties.SECURE);
		boolean useThreads = jdProperties.getBoolean(JDProperties.THREAD_USED);

		// Updated 2023 to not pass old Properties information.
		// Everything should be in the JDProperties object
		// The JDProperties object was updated to also allow the use of null values. 
		// 
		
        //@pw1 Decided to leave connections via AS400() as-is and just implement to mimic Native JDBC
        //@pw1 info contains args from DriverMangager.getConnection(args)
        //@pw1 jdProperties does not represent null values.  Both null and "" have a value of "".
        //@pw1 if info contains id/pass of "" then they must not be "" in jdProperties (allowing jdProperties to override)
        //@pw1 throw exception if info id/pass == ""  and change info id/pass to "" if they are null
        //@pw3 Add way to get old behavior allowing "" (!but also need to allow new behavior of allowing null is/passwd so customers can slowly migrate)
        //check if "".  
        
        String secureCurrentUser = SystemProperties.getProperty (SystemProperties.JDBC_SECURE_CURRENT_USER); //@pw3
        boolean isSecureCurrentUser = true;                                                                  //@pw3
        //if system property or jdbc property is set to false then secure current user code is not used
        //null value for system property means not specified...so true by default
        if(((secureCurrentUser != null) && (Boolean.valueOf(secureCurrentUser).booleanValue() == false)) || !jdProperties.getBoolean(JDProperties.SECURE_CURRENT_USER))            //@pw3
            isSecureCurrentUser = false;                                                                      //@pw3
                
        boolean forcePrompt = false;     //@prompt
        if ("".equals(userName))                                                  //@pw1 //@pw2
        {                                                                         //@pw1
            if(isSecureCurrentUser)//@pw3
            {  //@pw3
                if (JDTrace.isTraceOn()) //jdbc category trace                        //@pw1
                    JDTrace.logInformation (AS400JDBCDriver.class, "Userid/password cannot be \"\" or *CURRENT due to security constraints.  Use null instead");  //@pw1
                //JDError.throwSQLException(JDError.EXC_CONNECTION_REJECTED);           //@pw1 //@prompt
                forcePrompt = true;  //@prompt
            }  //@pw3
        }                                                                         //@pw1
        if (clearPassword != null && clearPassword.length==0)                                              //@pw1 //@pw2
        {                                                                         //@pw1
            if(isSecureCurrentUser)//@pw3
            {  //@pw3
                if (JDTrace.isTraceOn()) //jdbc category trace                        //@pw1
                    JDTrace.logInformation (AS400JDBCDriver.class, "Userid/password cannot be \"\" or *CURRENT due to security constraints.  Use null instead");  //@pw1
                //JDError.throwSQLException(JDError.EXC_CONNECTION_REJECTED);           //@pw1 //@prompt
                forcePrompt = true;  //@prompt
            }  //@pw3
        }                                                                         //@pw1
                
        if(userName != null)                                                      //@pw1
        {                                                                         //@pw1
            //check for *current                                                  //@pw1
            if (userName.compareToIgnoreCase("*CURRENT") == 0)                    //@pw1
            {                                                                     //@pw1
                if(isSecureCurrentUser)//@pw3
                {  //@pw3
                    if (JDTrace.isTraceOn()) //jdbc category trace                    //@pw1
                        JDTrace.logInformation (AS400JDBCDriver.class, "Userid/password cannot be \"\" or *CURRENT due to security constraints.  Use null instead");  //@pw1
                    //JDError.throwSQLException(JDError.EXC_CONNECTION_REJECTED);       //@pw1
                    forcePrompt = true;  //@prompt
                }  //@pw3
            }                                                                     //@pw1
        }                                                                         //@pw1
        
        if(clearPassword!= null)                                                  //@pw1
        {                                                                         //@pw1
           /* check for *CURRENT. Be sure to check the length */ 
            if (clearPassword.length == 8 &&
            	clearPassword[0] == '*' &&
                (clearPassword[1] == 'C' || clearPassword[1] == 'c') &&
                (clearPassword[2] == 'U' || clearPassword[2] == 'u') &&
                (clearPassword[3] == 'R' || clearPassword[3] == 'r') &&
                (clearPassword[4] == 'R' || clearPassword[4] == 'r') &&
                (clearPassword[5] == 'E' || clearPassword[5] == 'e') &&
                (clearPassword[6] == 'N' || clearPassword[6] == 'n') &&
                (clearPassword[7] == 'T' || clearPassword[7] == 't') )                //@pw1
            {                                                                         //@pw1
                if(isSecureCurrentUser)//@pw3
                {  //@pw3
                    if (JDTrace.isTraceOn()) //jdbc category trace                        //@pw1
                        JDTrace.logInformation (AS400JDBCDriver.class, "Userid/password cannot be \"\" or *CURRENT due to security constraints.  Use null instead");  //@pw1
                    //JDError.throwSQLException(JDError.EXC_CONNECTION_REJECTED);           //@pw1
                    forcePrompt = true;  //@prompt
                }  //@pw3
            }                                                                         //@pw1
        }                                                                         //@pw1
        
        
		// Create the AS400 object, so we can create a Connection via loadImpl2.
		AS400 as400 = null;
		try 
		{
            if (serverName.length() == 0)
                as400 = AS400.newInstance(secure);
            else if ((userName == null) || (userName.length() == 0))
                as400 = AS400.newInstance(secure, serverName);
            else if (clearPassword == null)
                as400 = AS400.newInstance(secure, serverName, userName);
            else
                as400 = AS400.newInstance(secure, serverName, userName, clearPassword, additionalAuthenticationFactor);
			SSLSocketFactory sslSocketFactoryObject = jdProperties.getCustomSSLSocketFactory();
		    if (null != sslSocketFactoryObject) {
		        as400.setSSLSocketFactory(sslSocketFactoryObject);
		    }
		}
		catch (AS400SecurityException e)
        {                           
          JDError.throwSQLException (as400, JDError.EXC_CONNECTION_REJECTED, e);
        }
        catch (IOException e)
        {                                      
          JDError.throwSQLException (as400, JDError.EXC_CONNECTION_UNABLE, e);
        }
		finally {
		if (clearPassword != null) { 
		  CredentialVault.clearArray(clearPassword);
		}
		}
		// Determine when the signon GUI can be presented..
		try
		{       
			if (!prompt.equals(JDProperties.NOT_SPECIFIED))							  // @B8A
				as400.setGuiAvailable(jdProperties.getBoolean(JDProperties.PROMPT));  // @B8C
		}
		catch (java.beans.PropertyVetoException e)
		{
			// This will never happen, as there are no listeners.
		}

                //Determine if threads should be used in communication with the host servers
                try{
                    if(!useThreads)
                        as400.setThreadUsed(useThreads);
                }
                catch(java.beans.PropertyVetoException e){
                }
                if(forcePrompt)             //@prompt
                    as400.forcePrompt(); //@prompt
                return as400;
	}


	//@A0A  - This logic was formerly in the AS400JDBCConnection ctor and open() method.
	private Connection initializeConnection (JDDataSourceURL dataSourceUrl,
											 JDProperties jdProperties)
	throws SQLException
	{
		//@B7D Connection connection                       = null;
		AS400 as400                                 = null;
		boolean proxyServerWasSpecifiedInUrl        = false;
		boolean proxyServerWasSpecifiedInProperties = false;
		boolean proxyServerWasSpecified             = false;

		//@C4A Check for native driver only if driver property is not set to Toolbox
		String driverImplementation = jdProperties.getString(JDProperties.DRIVER);	//@C4M
		if (!driverImplementation.equals(JDProperties.DRIVER_TOOLBOX))              //@C4A
		{									   						                //@C4A
			// @B2A
			// Determine whether the native driver is available.
		  
      if (nativeDriver == null) {
        try {
          nativeDriver = (Driver) Class.forName(
              "com.ibm.db2.jdbc.app.DB2Driver").newInstance();
          if (JDTrace.isTraceOn()) // @C2A
            JDTrace
                .logInformation(this,
                    "Native IBM Developer Kit for Java JDBC driver implementation was loaded"); // @C2A
        } catch (Throwable e) {
          nativeDriver = null;
        }
      }

			// @B2A
			// Decide which JDBC driver implementation to use.  If the
			// native driver is available AND there is no secondary URL
			// available AND the "driver" property was not set to "toolbox",
			// then use the native driver implementation.
			//@C4M String driverImplementation = jdProperties.getString(JDProperties.DRIVER);
			if ((nativeDriver != null) 
				&& (dataSourceUrl.getSecondaryURL().length() == 0))
				//@C4D Already checked above && (!driverImplementation.equals(JDProperties.DRIVER_TOOLBOX)))
			{            
				//@C3M
				boolean isLocal = false;													// @C2A
				String serverName = dataSourceUrl.getServerName();							// @C2A
				if (serverName.length() == 0 || serverName.equalsIgnoreCase("localhost"))	// @C2A //@locala
					isLocal = true;															// @C2A
				else {																		// @C2A
					try {																	// @C2A
						InetAddress localInet = InetAddress.getLocalHost();					// @C2A
						InetAddress[] remoteInet = InetAddress.getAllByName(serverName);	// @C2A
						for (int i = 0; i < remoteInet.length; ++i) {						// @C2A
							if (localInet.equals(remoteInet[i])) {							// @C2A
								isLocal = true;												// @C2A
							}																// @C2A
						}																	// @C2A
					}																		// @C2A
					catch (Throwable e) {													 // @C2A
						// Ignore.  We will just assume that we are not local.                 @C2A
					}																		// @C2A
				}

				if (isLocal) {																// @C2A
					if (JDTrace.isTraceOn())												// @C2A                                              
						JDTrace.logInformation(this, "Connection is local");				// @C2A
					String nativeURL = dataSourceUrl.getNativeURL();                       
					if (JDTrace.isTraceOn())
						JDTrace.logInformation(this, "Using native IBM Developer Kit for Java JDBC driver implementation");//@native don't print passwd
					return nativeDriver.connect(nativeURL, jdProperties.getOriginalInfo());
				}																			// @C2A
			}
		}//@C4A
		// @C2D else
		// @C2D {
		if (JDTrace.isTraceOn())
			JDTrace.logInformation (this, "Using IBM Toolbox for Java JDBC driver implementation");
		// @C2D }

		// @A0A
		// See if a proxy server was specified.
		//if (jdProperties.getIndex (JDProperties.PROXY_SERVER) != -1)         //@A3D
		if (jdProperties.getString(JDProperties.PROXY_SERVER).length() != 0) //@A3C
			proxyServerWasSpecifiedInUrl = true;
		if (SystemProperties.getProperty (SystemProperties.AS400_PROXY_SERVER) != null)
			proxyServerWasSpecifiedInProperties = true;
		if (proxyServerWasSpecifiedInUrl || proxyServerWasSpecifiedInProperties)
			proxyServerWasSpecified = true;

		// If no proxy server was specified, and there is a secondary URL,
		// simply pass the secondary URL to the DriverManager and ask it for
		// an appropriate Connection object.
		if (!proxyServerWasSpecified)
		{
			String secondaryUrl = dataSourceUrl.getSecondaryURL ();
			if (secondaryUrl.length() != 0)
			{
				if (JDTrace.isTraceOn())
					JDTrace.logInformation (this,
											"Secondary URL [" + secondaryUrl + "]");
				return DriverManager.getConnection (secondaryUrl, jdProperties.getOriginalInfo());
			}
		}

		as400 = initializeAS400(dataSourceUrl, jdProperties);				   // @B3C

		if (proxyServerWasSpecifiedInUrl)
		{
			// A proxy server was specified in URL,
			// so we need to inform the AS400 object.

			//boolean proxyServerSecure = jdProperties.getBoolean (JDProperties.PROXY_SERVER_SECURE);   // TBD
			String proxyServerNameAndPort = jdProperties.getString (JDProperties.PROXY_SERVER);
			// Note: The PROXY_SERVER property is of the form:
			//       hostName[:portNumber]
			//       where portNumber is optional.
			try
			{
				as400.setProxyServer (proxyServerNameAndPort);
				//as400.setProxyServerSecure (proxyServerSecure);  // TBD
			}
			catch (java.beans.PropertyVetoException e)
			{
			} // Will never happen.
		}

		//@B6C Moved common code to prepareConnection.
		return prepareConnection(as400, dataSourceUrl,  jdProperties); 
	}


	//@B5A
	private Connection initializeConnection (AS400 as400)
	throws SQLException
	{
		JDDataSourceURL dataSourceUrl = new JDDataSourceURL(null);
		Properties info = new Properties();
		JDProperties jdProperties = new JDProperties(null, info, null, null);

		//@B6C Moved common code to prepareConnection.
		return prepareConnection(as400, dataSourceUrl, jdProperties); 
	}


	//@B5A
	private Connection initializeConnection (String schema, Properties info, AS400 as400)
	throws SQLException
	{
		boolean proxyServerWasSpecifiedInUrl        = false;

		String url = null;
		if (schema != null)										//@B6A
			url = "jdbc:as400://" + as400.getSystemName() + "/" + schema;
		else													//@B6A
			url	= "jdbc:as400://" + as400.getSystemName();		//@B6A
		JDDataSourceURL dataSourceUrl = new JDDataSourceURL(url);

		JDProperties jdProperties = new JDProperties(null, info, null, null);

		if (JDTrace.isTraceOn())
			JDTrace.logInformation (this, "Using IBM Toolbox for Java JDBC driver implementation");

		// See if a proxy server was specified.
		if (jdProperties.getString(JDProperties.PROXY_SERVER).length() != 0)
			proxyServerWasSpecifiedInUrl = true;

		if (proxyServerWasSpecifiedInUrl)
		{
			// A proxy server was specified in URL, so we need to inform the AS400 object.

			//boolean proxyServerSecure = jdProperties.getBoolean (JDProperties.PROXY_SERVER_SECURE);// TBD
			String proxyServerNameAndPort = jdProperties.getString (JDProperties.PROXY_SERVER);
			// Note: The PROXY_SERVER property is of the form:
			//       hostName[:portNumber]
			//       where portNumber is optional.
			try
			{
				as400.setProxyServer (proxyServerNameAndPort);
				//as400.setProxyServerSecure (proxyServerSecure);  // TBD
			}
			catch (java.beans.PropertyVetoException e)
			{
			} // Will never happen.
		}

		//@B6C Moved common code to prepareConnection.
		return prepareConnection(as400, dataSourceUrl, jdProperties);      
	}


	/**
	Indicates if the driver is a genuine JDBC compliant driver.
	
	@return         Always true.
	**/
	public boolean jdbcCompliant ()
	{
		return true;
	}


	//@B6A -- This logic was formerly in the initializeConnection() method.
	private Connection prepareConnection(AS400 as400, JDDataSourceURL dataSourceUrl, 
			  JDProperties jdProperties) throws SQLException  {
		return prepareConnection(as400,dataSourceUrl, jdProperties, false); 
	}


	private Connection prepareConnection(AS400 as400, JDDataSourceURL dataSourceUrl, 
										  JDProperties jdProperties,
										  boolean vrmSet)
	throws SQLException
	{

        // set socket properties
        SocketProperties sockProps = null;

        //if == "", then take platform defaults...do not set 
        //only get/set properties is one is updated
        if( jdProperties.getString(JDProperties.KEEP_ALIVE).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setKeepAlive(jdProperties.getBoolean(JDProperties.KEEP_ALIVE));
        }
        if( jdProperties.getString(JDProperties.RECEIVE_BUFFER_SIZE).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setReceiveBufferSize( jdProperties.getInt(JDProperties.RECEIVE_BUFFER_SIZE));
        }
        if( jdProperties.getString(JDProperties.SEND_BUFFER_SIZE).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setSendBufferSize(jdProperties.getInt(JDProperties.SEND_BUFFER_SIZE));
        }
        
        //@timeout2
        //First get setting from DriverManager, then override with property updates
        if(!as400.arePropertiesFrozen()) //@timeout3 AS400JDBCDriver.connect(clone=false) cannot update props. We don't know if DriverManager.setLoginTimeout() has been updated.
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setLoginTimeout(DriverManager.getLoginTimeout() * 1000); //@STIMEOUT
        }
        
        //@timeout
        if( jdProperties.getString(JDProperties.LOGIN_TIMEOUT).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setLoginTimeout(jdProperties.getInt(JDProperties.LOGIN_TIMEOUT) * 1000); //@STIMEOUT
        }

        //@STIMEOUT
        if( jdProperties.getString(JDProperties.SOCKET_TIMEOUT).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setSoTimeout(jdProperties.getInt(JDProperties.SOCKET_TIMEOUT)); //@STIMEOUT already in milliseconds
        }

              
        if( jdProperties.getBoolean(JDProperties.TCP_NO_DELAY) == true)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            
            sockProps.setTcpNoDelay(true);
                      if (JDTrace.isTraceOn())
            JDTrace.logInformation(this, "Setting sockProps.setTcpNoDelay(true)");
                      
        }

        String defaultImpl = "com.ibm.as400.access.AS400JDBCConnectionImpl"; 
        if (jdProperties.getInt(JDProperties.ENABLE_CLIENT_AFFINITIES_LIST) == 1) {
          defaultImpl = "com.ibm.as400.access.AS400JDBCConnectionRedirect"; 
        }
        
        if(sockProps != null)
            as400.setSocketProperties(sockProps);

        // Create the appropriate kind of Connection object.
		Connection connection = (Connection) as400.loadImpl2 (
														 defaultImpl,                 
	    												 "com.ibm.as400.access.JDConnectionProxy");
                                       
		// Set the properties on the Connection object.
		if (connection != null)
		{
		    // If we get an exception, make sure the connection is closed.
		    // The common case is when an exit program prevents access to the system.
			// @AB1A
		    try { 
		      if (connection instanceof JDConnectionProxy) { 
            ((JDConnectionProxy)connection).setSystem(as400);
            ((JDConnectionProxy)connection).setProperties(dataSourceUrl, jdProperties, as400);
		      } else { 
		        ((AS400JDBCConnection)connection).setSystem(as400);
		        ((AS400JDBCConnection)connection).setProperties(dataSourceUrl, jdProperties, as400);
		      }
		    } catch (SQLException sqlex) {
		      try { 
		      connection.close();
		      } catch (Exception e) { 
		        // Just ignore 
		      }
		      throw sqlex; 
		    }
		}
		//
		// If the signon server was skipped, we need to manually determine the release
		// This is important for boolean support
		// 
		if (as400.skipSignonServer_ && ! vrmSet) {
			try { 
			  Statement s = connection.createStatement(); 
			  ResultSet rs = s.executeQuery("SELECT OS_VERSION,OS_RELEASE FROM SYSIBMADM.ENVSYSINFO"); 
			  if (rs.next()) {
				  int version = rs.getInt(1); 
				  int release = rs.getInt(2); 
				  as400.setVRM(version,release,0); 
			  }
			  rs.close(); 
			  s.close(); 
			} catch (SQLException sqlex) {
				// Log and ignore 
			}
			//
			// Connect again to get the correct settings
			// This didn't work!!!! TODO; 
			//
			connection.close(); 
			return prepareConnection(as400,dataSourceUrl, jdProperties, true); 
		}
		return connection;
	}




	/**
	Returns the name of the driver.
	
	@return        The driver name.
	**/
	public String toString ()
	{
		return DRIVER_NAME_;	// @D0C
	}
	
/* ifdef JDBC40 */
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException(); 
  }
/* endif */ 

}
