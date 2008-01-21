///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCDriver.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;	// @B9A
import java.net.InetAddress;				// @C2A
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
<p>The AS400JDBCDriver class is a JDBC 4.0 driver that accesses
DB2 for i5/OS databases.

<p>To use this driver, the application or caller must register 
the driver with the JDBC DriverManager.  This class also registers 
itself automatically when it is loaded.

<p>After registering the driver, applications make connection
requests to the DriverManager, which dispatches them to the
appropriate driver.  This driver accepts connection requests
for databases specified by the URLs that match the following syntax:

<pre>
jdbc:as400://<em>system-name</em>/<em>default-schema</em>;<em>properties</em>
</pre>

<p>The driver uses the specified system name to connect
to a corresponding i5/OS system.  If a system name is not
specified, then the user will be prompted.  

<p>The default schema is optional and the driver uses it to resolve 
unqualified names in SQL statements.  If no default schema is set, then
the driver resolves unqualified names based on the naming convention
for the connection.  If SQL naming is being used, and no default schema
is set, then the driver resolves unqualified names using the schema with 
the same name as the user.  If system naming is being used, and no
default schema is set, then the driver resolves unqualified names using
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
<em>mylibrary</em> as the default schema.  The connection will
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
public class AS400JDBCDriver
implements java.sql.Driver
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";



	// Constants.
        // Update each release.  Was 4 for v5r1.
	static final int    MAJOR_VERSION_          = 8; //(ex: 8->V6R1)  @B1C @C5C @D1C @540 @610
	static final int    MINOR_VERSION_          = 2; //(ex: 2->PTF#2) @610 @jt61
	static final String DATABASE_PRODUCT_NAME_  = "DB2 UDB for AS/400";  // @D0A
	static final String DRIVER_NAME_            = "AS/400 Toolbox for Java JDBC Driver"; // @D0C @C5C @C6C
    static final String DRIVER_LEVEL_           = "06010002"; // example V5R4M0.0 -> 05040000 (needed for hidden clientInfo in Brent's spec) (each # is 2 digits in length), should match version in Copyright.java  //@PDA jdbc40 @jt61


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


	/**
	Static initializer.  Registers the JDBC driver with the JDBC
	driver manager and loads the appropriate resource bundle for
	the current locale.
	**/
	static {
		try
		{
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

		JDProperties jdProperties = new JDProperties (urlProperties, info);

		// Initialize the connection if the URL is valid.
		Connection connection = null;										 //@A0C
		if (dataSourceUrl.isValid ())
			connection = initializeConnection (dataSourceUrl, jdProperties,
											   info);  //@A0C

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
	
	
	@param  system   The i5/OS system to connect.
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

		if (system instanceof SecureAS400)
			return initializeConnection(new SecureAS400(system));
		else
			return initializeConnection(new AS400(system));

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
	
	
	@param  system   The i5/OS system to connect.
        @param  clone    True if the AS400 object should be cloned, false otherwises
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
	
	
	@param  system   The i5/OS system to connect.
        @param  info     The connection properties.
        @param  schema   The default schema or null meaning no default schema specified.
        @param  clone    True if the AS400 object should be cloned, false otherwises
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
                    if(system instanceof SecureAS400)
                        return initializeConnection(schema, info, new SecureAS400(system));
                    else
                        return initializeConnection(schema, info, new AS400(system));
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
	
	@param  system  The i5/OS system to connect.
	@param  info    The connection properties.
	@param  schema  The default schema or null meaning no default schema specified.
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

		if (system instanceof SecureAS400)
			return initializeConnection(schema, info, new SecureAS400(system));
		else
			return initializeConnection(schema, info, new AS400(system));
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
			JDProperties properties = new JDProperties (dataSourceUrl.getProperties(), info);
			dpi = properties.getInfo ();
		}

		return dpi;
	}



	/**
	Returns a resource from the resource bundle.
	
	@param  key     The resource key.
	@return         The resource String.
	**/
	static String getResource (String key)
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

		return resource;
	}



	//@B3A  - This logic was formerly in the initializeConnection() method.
	static AS400 initializeAS400(JDDataSourceURL dataSourceUrl,
								 JDProperties jdProperties,
								 Properties info)
	{
		// We must handle the different combinations of input
		// user names and passwords.
		String serverName = dataSourceUrl.getServerName();
		String userName   = jdProperties.getString (JDProperties.USER);
		String password   = jdProperties.getString (JDProperties.PASSWORD);
		String prompt     = jdProperties.getString (JDProperties.PROMPT);	// @B8C
		boolean secure    = jdProperties.getBoolean (JDProperties.SECURE);
		String keyRingName     = jdProperties.getString (JDProperties.KEY_RING_NAME);  //@B9A
		String keyRingPassword = jdProperties.getString (JDProperties.KEY_RING_PASSWORD); //@B9A
                boolean useThreads = jdProperties.getBoolean(JDProperties.THREAD_USED);

		// Create the AS400 object, so we can create a Connection via loadImpl2.
		AS400 as400 = null;
		if (secure)
		{
			if (serverName.length() == 0)
				as400 = new SecureAS400 ();
			else if (userName.length() == 0)
				as400 = new SecureAS400 (serverName);
			else if (password.length() == 0)
				as400 = new SecureAS400 (serverName, userName);
			else
				as400 = new SecureAS400 (serverName, userName, password);
			if (keyRingName != null && keyRingPassword != null && keyRingName != "") //@B9A	@C1C
			{   
				try
				{															  //@B9A
					((SecureAS400)as400).setKeyRingName(keyRingName, keyRingPassword); //@B9A
				}									  //@B9A
				catch (PropertyVetoException pve)					  //@B9A
				{ /*Will never happen*/
				}						   //@B9A
			}																  //@B9A
		}
		else
		{
			if (serverName.length() == 0)
				as400 = new AS400 ();
			else if (userName.length() == 0)
				as400 = new AS400 (serverName);
			else if (password.length() == 0)
				as400 = new AS400 (serverName, userName);
			else
				as400 = new AS400 (serverName, userName, password);
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

                return as400;
	}


	//@A0A  - This logic was formerly in the AS400JDBCConnection ctor and open() method.
	private Connection initializeConnection (JDDataSourceURL dataSourceUrl,
											 JDProperties jdProperties,
											 Properties info)
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
			Driver nativeDriver;
			try
			{
				nativeDriver = (Driver)Class.forName("com.ibm.db2.jdbc.app.DB2Driver").newInstance();
				if (JDTrace.isTraceOn())																							// @C2A
					JDTrace.logInformation(this, "Native IBM Developer Kit for Java JDBC driver implementation was loaded");		// @C2A
			}
			catch (Throwable e)
			{
				nativeDriver = null;
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
				if (serverName.length() == 0)												// @C2A
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
						JDTrace.logInformation(this, "Using native IBM Developer Kit for Java JDBC driver implementation (" + nativeURL + ")");
					return nativeDriver.connect(nativeURL, info);
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
				return DriverManager.getConnection (secondaryUrl, info);
			}
		}

		as400 = initializeAS400(dataSourceUrl, jdProperties, info);				   // @B3C

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
		return prepareConnection(as400, dataSourceUrl, info, jdProperties); 
	}


	//@B5A
	private Connection initializeConnection (AS400 as400)
	throws SQLException
	{
		JDDataSourceURL dataSourceUrl = new JDDataSourceURL(null);
		Properties info = new Properties();
		JDProperties jdProperties = new JDProperties(null, info);

		//@B6C Moved common code to prepareConnection.
		return prepareConnection(as400, dataSourceUrl, info, jdProperties); 
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

		JDProperties jdProperties = new JDProperties(null, info);

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
		return prepareConnection(as400, dataSourceUrl, info, jdProperties);      
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
										 Properties info, JDProperties jdProperties)
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
        //timeout2
        if(sockProps == null)
            sockProps = as400.getSocketProperties();
        sockProps.setSoTimeout(DriverManager.getLoginTimeout() * 1000);
        
        //@timeout
        if( jdProperties.getString(JDProperties.LOGIN_TIMEOUT).equals("") == false)
        {
            if(sockProps == null)
                sockProps = as400.getSocketProperties();
            sockProps.setSoTimeout(jdProperties.getInt(JDProperties.LOGIN_TIMEOUT) * 1000);
        }
        
        if(sockProps != null)
            as400.setSocketProperties(sockProps);
        
		// Create the appropriate kind of Connection object.
		Connection connection = (Connection) as400.loadImpl2 (
														 "com.ibm.as400.access.AS400JDBCConnection",                 
	    												 "com.ibm.as400.access.JDConnectionProxy");
                                       
		// Set the properties on the Connection object.
		if (connection != null)
		{

			// @A2D Class[] argClasses = new Class[] { JDDataSourceURL.class,
			// @A2D                                    JDProperties.class,
			// @A2D                                    AS400.class };
			// @A2D Object[] argValues = new Object[] { dataSourceUrl,
			// @A2D                                     jdProperties,
			// @A2D                                     as400 };
			// @A2D try {
			// Hand off the public AS400 object to keep it from getting
			// garbage-collected.
			Class clazz = connection.getClass ();          
			// @A2D Method method = clazz.getDeclaredMethod ("setSystem",
			// @A2D                                   new Class[] { AS400.class });
			// @A2D method.invoke (connection, new Object[] { as400 });

			// @A2D method = clazz.getDeclaredMethod ("setProperties", argClasses);
			// @A2D method.invoke (connection, argValues);

			String className = clazz.getName();
			if (className.equals("com.ibm.as400.access.AS400JDBCConnection"))
			{
				((AS400JDBCConnection)connection).setSystem(as400);
				((AS400JDBCConnection)connection).setProperties(dataSourceUrl, jdProperties, as400);
			}
			else if (className.equals("com.ibm.as400.access.JDConnectionProxy"))
			{
				((JDConnectionProxy)connection).setSystem(as400);
				((JDConnectionProxy)connection).setProperties(dataSourceUrl, jdProperties, as400);
			}
			// @A2D }
			// @A2D catch (NoSuchMethodException e) {
			// @A2D   JDTrace.logInformation (this,
			// @A2D                           "Could not resolve setProperties() method");
			// @A2D   throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
			// @A2D }
			// @A2D catch (IllegalAccessException e) {
			// @A2D   JDTrace.logInformation (this,
			// @A2D                           "Could not access setProperties() method");
			// @A2D   throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
			// @A2D }
			// @A2D catch (InvocationTargetException e) {
			// @A2D   Throwable e2 = e.getTargetException ();
			// @A2D   if (e2 instanceof SQLException)
			// @A2D     throw (SQLException) e2;
			// @A2D   else if (e2 instanceof RuntimeException)
			// @A2D     throw (RuntimeException) e2;
			// @A2D   else if (e2 instanceof Error)
			// @A2D     throw (Error) e2;
			// @A2D   else {
			// @A2D     JDTrace.logInformation (this,
			// @A2D                             "Could not invoke setProperties() method");
			// @A2D     throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
			// @A2D   }
			// @A2D }
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



}
