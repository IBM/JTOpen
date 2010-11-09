///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

/**
 Contains constants representing names of all Java system properties recognized by the IBM Toolbox for Java.
 <p>Note: This class is reserved for internal use within the Toolbox, and is subject to change without notice.
 **/
public class SystemProperties
{
    // System property constants.
    private static final String ACCESS_PREFIX = "com.ibm.as400.access.";
    private static final String PROPERTIES_CLASS_NAME = ACCESS_PREFIX + "Properties";
    private static final String PROPERTIES_FILE_NAME = ACCESS_PREFIX + "jt400.properties";
    private static final String PROPERTIES_FILE_NAME_WITH_SLASHES = "com/ibm/as400/access/jt400.properties";


    // System property names.

    /**
     Specifies whether GUI support is available in the current execution environment.
     If set to <tt>true</tt>, then the {@link AS400 AS400} class may prompt during sign-on to display error conditions, to obtain additional signon information, or to change the password.
     If set to <tt>false</tt>, then connection error conditions or missing information will result in exceptions.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.guiAvailable
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>true</tt>
     <li>Overridden by: {@link AS400#setGuiAvailable AS400.setGuiAvailable()}
     </ul>
     **/
    public static final String AS400_GUI_AVAILABLE = ACCESS_PREFIX + "AS400.guiAvailable";

    /**
     Specifies the proxy server host name and port number.
     The port number is optional.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.proxyServer
     <li>Values/syntax: <i>hostName:portNumber</i>
     <li>Default: (no default)
     <li>Overridden by: {@link AS400#setProxyServer AS400.setProxyServer()}
     </ul>
     **/
    public static final String AS400_PROXY_SERVER = ACCESS_PREFIX + "AS400.proxyServer";

    /**
     Specifies the name of the default signon handler class used by the {@link AS400 AS400} class.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.signonHandler
     <li>Values/syntax: <i>packageName.classname</i>
     <li>Default: An internal Toolbox class is used.
     <li>Overridden by: {@link AS400#setSignonHandler AS400.setSignonHandler()} and {@link AS400#setDefaultSignonHandler AS400.setDefaultSignonHandler()}
     </ul>
     **/
    public static final String AS400_SIGNON_HANDLER = ACCESS_PREFIX + "AS400.signonHandler";

    /**
     Specifies whether the {@link AS400 AS400} class should attempt to add the appropriate secondary language library to the library list.
     This property is ignored if not running on the IBM i system.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.mustAddLanguageLibrary
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link AS400#setMustAddLanguageLibrary AS400.setMustAddLanguageLibrary()}
     </ul>
     **/
    public static final String AS400_MUST_ADD_LANGUAGE_LIBRARY = ACCESS_PREFIX + "AS400.mustAddLanguageLibrary";

    /**
     Specifies whether sockets must be used when communicating with the system.  Setting this property to <tt>true</tt> directs the Toolbox to refrain from exploiting native optimizations, when running directly on the system.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.mustUseSockets
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link AS400#setMustUseSockets AS400.setMustUseSockets()}
     </ul>
     **/
    public static final String AS400_MUST_USE_SOCKETS = ACCESS_PREFIX + "AS400.mustUseSockets";

    /**
     Specifies whether only Internet domain sockets must be used when communicating with the system.  Setting this property to <tt>true</tt> directs the Toolbox to refrain from exploiting Unix sockets, when running directly on the system.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.mustUseNetSockets
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link AS400#setMustUseNetSockets AS400.setMustUseNetSockets()}
     </ul>
     **/
    public static final String AS400_MUST_USE_NET_SOCKETS = ACCESS_PREFIX + "AS400.mustUseNetSockets";

    /**
     Specifies whether the explicitly supplied profile must be used when communicating with the system.  Setting this property to <tt>true</tt> directs the Toolbox to refrain from exploiting the currently signed-on profile by default, when running directly on the system.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.mustUseSuppliedProfile
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link AS400#setMustUseSuppliedProfile AS400.setMustUseSuppliedProfile()}
     </ul>
     **/
    public static final String AS400_MUST_USE_SUPPLIED_PROFILE = ACCESS_PREFIX + "AS400.mustUseSuppliedProfile";

    /**
     Specifies whether threads are used when communicating with the host servers.
     By default, the AS400 object creates separate threads to listen on communication sockets to the host servers.  Setting this property to <tt>false</tt> directs the Toolbox to refrain from creating separate threads for host server communications.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.threadUsed
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>true</tt>
     <li>Overridden by: {@link AS400#setThreadUsed AS400.setThreadUsed()}
     </ul>
     **/
    public static final String AS400_THREAD_USED = ACCESS_PREFIX + "AS400.threadUsed";

    /*public*/ static final String SECUREAS400_PROXY_ENCRYPTION_MODE = ACCESS_PREFIX + "SecureAS400.proxyEncryptionMode";
    /*public*/ static final String SECUREAS400_USE_SSLIGHT = ACCESS_PREFIX + "SecureAS400.useSslight";

    /**
     Specifies which trace categories to enable. This is a comma-delimited
     list containing any combination of trace categories.
     <ul>
     <li>Property name: com.ibm.as400.access.Trace.category
     <li>Values/syntax: <tt>datastream</tt>, <tt>diagnostic</tt>, <tt>error</tt>, <tt>information</tt>, ...
     <br>(Refer to the {@link Trace Trace} class for complete list.)
     <li>Default: (no default)
     <li>Overridden by: Various <tt>setTrace...</tt> methods in the <code>Trace</code> class.
     </ul>
     **/
    public static final String TRACE_CATEGORY   = ACCESS_PREFIX + "Trace.category";
           static final String TRACE_CATEGORIES = ACCESS_PREFIX + "Trace.categories"; // tolerate misspelled property name

    /**
     Specifies the file to which the {@link Trace Trace} class writes output.
     <ul>
     <li>Property name: com.ibm.as400.access.Trace.file
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: {@link System#out System.out}.
     <li>Overridden by: Any of the <tt>Trace.setFileName()</tt> or <tt>Trace.setPrintWriter()</tt> methods.
     </ul>
     **/
    public static final String TRACE_FILE = ACCESS_PREFIX + "Trace.file";

    /**
     Specifies which trace categories to start on the JDBC server job.
     <ul>
     <li>Property name: com.ibm.as400.access.ServerTrace.JDBC
     <li>Values/syntax: Refer to the javadoc for class {@link AS400JDBCDriver AS400JDBCDriver}. Follow the link labeled "JDBC properties", and search for the "server trace" property.
     <li>Default: (no default)
     <li>Overridden by: Specifying property values in either the connection URL or via one of the <tt>connect()</tt> methods of class {@link AS400JDBCDriver AS400JDBCDriver}
     </ul>
     **/
    public static final String TRACE_JDBC_SERVER = ACCESS_PREFIX + "ServerTrace.JDBC";     // @j1a

    /*public*/ static final String TRACE_ENABLED = ACCESS_PREFIX + "Trace.enabled";

    /**
     Specifies whether the {@link CommandCall CommandCall} class should assume that called commands are threadsafe.
     If <tt>true</tt>, all called commands are assumed to be threadsafe.
     If <tt>false</tt>, all called commands are assumed to be non-threadsafe.
     <ul>
     <li>Property name: com.ibm.as400.access.CommandCall.threadSafe
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link CommandCall#setThreadSafe(Boolean) CommandCall.setThreadSafe()}
     </ul>
     **/
    public static final String COMMANDCALL_THREADSAFE = ACCESS_PREFIX + "CommandCall.threadSafe";

    /**
     Specifies whether the {@link ProgramCall ProgramCall} class should assume that called programs are threadsafe.
     If <tt>true</tt>, all called programs are assumed to be thread-safe.
     If <tt>false</tt>, all called programs are assumed to be non-thread-safe.
     <ul>
     <li>Property name: com.ibm.as400.access.ProgramCall.threadSafe
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link ProgramCall#setThreadSafe ProgramCall.setThreadSafe()}
     </ul>
     **/
    public static final String PROGRAMCALL_THREADSAFE = ACCESS_PREFIX + "ProgramCall.threadSafe";

    /**
     Specifies how often, in seconds, the proxy server looks for idle connections.
     The proxy server starts a thread to look for clients that are no longer 
     communicating. Use this property to set how often the thread looks for idle
     connections.
     <ul>
     <li>Property name: com.ibm.as400.access.TunnelProxyServer.clientCleanupInterval
     <li>Values/syntax: <i>numberOfSeconds</i>
     <li>Default: 2 hours
     <li>Overridden by: (none)
     </ul>
     **/
    public static final String TUNNELPROXYSERVER_CLIENTCLEANUPINTERVAL = ACCESS_PREFIX + "TunnelProxyServer.clientCleanupInterval"; //@A2A

    /**
     Specifies how long, in seconds, a client can be idle before the proxy
     server removes references to the objects.
     Removing the references allows the JVM to garbage collect the objects. 
     The proxy server starts a thread to look for clients that are no longer
     communicating. Use this property to set how long a client can be idle before
     performing garbage collection on it.
     <ul>
     <li>Property name: com.ibm.as400.access.TunnelProxyServer.clientLifetime
     <li>Values/syntax: <i>numberOfSeconds</i>
     <li>Default: 30 minutes
     <li>Overridden by: (none)
     </ul>
     **/
    public static final String TUNNELPROXYSERVER_CLIENTLIFETIME	= ACCESS_PREFIX + "TunnelProxyServer.clientLifetime"; //@A2A

    /**
     Specifies whether the socket is reused for multiple file transfers when in "active" mode. This property is referenced by classes {@link FTP FTP} and {@link AS400FTP AS400FTP}.
     If <tt>true</tt>, the socket is reused.
     If <tt>false</tt>, a new socket is created for each file transfer.
     This property is ignored for a given FTP object if <tt>FTP.setReuseSocket(true/false)</tt> has
     been performed on the object.
     <ul>
     <li>Property name: com.ibm.as400.access.FTP.reuseSocket
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>true</tt>
     <li>Overridden by: {@link FTP#setReuseSocket FTP.setReuseSocket()}
     </ul>
     **/
    public static final String FTP_REUSE_SOCKET = ACCESS_PREFIX + "FTP.reuseSocket";

    /*public*/ static final String JDBC_STATEMENT_LISTENERS = ACCESS_PREFIX + "JDBC.statementListeners";
    /*public*/ static final String JDBC_SECURE_CURRENT_USER = ACCESS_PREFIX + "JDBC.secureCurrentUser"; //@pw3 not documented in html
    /*public*/ static final String JDBC_JVM16_SYNCHRONIZE = ACCESS_PREFIX + "JDBC.jvm16Synchronize"; //@dmy temp fix for jvm 1.6 memory stomping
    /*public*/ static final String TRACE_MONITOR = ACCESS_PREFIX + "Trace.monitor";
    /*public*/ static final String TRACE_MONITOR_PORT = ACCESS_PREFIX + "Trace.monitorPort";

    /**
     Specifies the fallback CCSID to use in cases where a text data field with CCSID 65535 is encountered and must be converted.
     Sometimes, especially in non-English environments, the default CCSID is left at 65535. That causes classes such as {@link AS400Text AS400Text} to make a "best guess" on the CCSID, based on the default locale.
     This option overrides the best guess with a specific CCSID value.
     This is useful where the Locale of a client system is different from that of the IBM i system.
     <ul>
     <li>Property name: com.ibm.as400.access.AS400.fallbackCCSID
     <li>Values/syntax: <tt>0-65535</tt>
     <li>Default: (no default)
     <li>Overridden by: (none)
     </ul>
     **/
    public static final String FALLBACK_CCSID = ACCESS_PREFIX + "AS400.fallbackCCSID";

    /**
     Specifies whether pooled connections are to be pretested before being allocated to a requester by the connection pool manager.
     <ul>
     <li>Property name: com.ibm.as400.access.ConnectionPool.pretest
     <li>Values/syntax: <tt>true</tt> or <tt>false</tt>
     <li>Default: <tt>false</tt>
     <li>Overridden by: {@link ConnectionPool#setPretestConnections(boolean) ConnectionPool.setPretestConnections()}
     </ul>
     **/
    public static final String CONNECTIONPOOL_PRETEST = ACCESS_PREFIX + "ConnectionPool.pretest";



    //  *** Note: ***
    //
    // If you add a new system property, remember to also add it to the
    // following other files:
    //     - SystemProperties.htm
    //     - SystemPropertiesSample1.htm
    //     - SystemPropertiesSample2.htm
    // The *.htm files are owned/maintained by the InfoCenter support people.

    // Private data.
    private static Vector ignored_ = new Vector();
    private static Properties propertiesFromClass_ = null;
    private static Properties propertiesFromFile_ = null;
    private static boolean propertiesClassLoadFailed_ = false;
    private static boolean propertiesFileLoadFailed_ = false;
    private static boolean systemPropertiesLoadFailed_ = false;

    // Private constructor - since this class never needs to be instantiated.
    private SystemProperties()
    {
        // Nothing.
    }

    // Returns the value of a system property.
    // This method is reserved for internal use within the Toolbox.
    // @param  propertyName  The system property name.
    // @return  The system property value, or null if the system property is not specified.
    public static String getProperty(String propertyName)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting system property: '" + propertyName + "'");

        String propertyValue = null;

        // Check to see if any specified value should be ignored.
        if (ignored_.contains(propertyName))
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "System property on ignore list, value remains null.");
            return null;
        }

        // Look in the system property.  This will get the value if it was set programmatically using java.lang.System.setProperties() or using the -D option of the java command.
        if (systemPropertiesLoadFailed_ == false)
        {
            try
            {
                propertyValue = System.getProperty(propertyName);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Value found in system properties:  '" + propertyValue + "'");
            }
            catch (SecurityException e)
            {
                // This will get thrown in a browser, since it is not cool to get properties from an applet.
                systemPropertiesLoadFailed_ = true;
                if (Trace.isTraceErrorOn()) Trace.log(Trace.ERROR, "Browser security exception:", e);
            }
        }

        // If it is not found, then look in the Properties class.
        if (propertyValue == null)
        {
            // If the Properties class has not yet been loaded, and no previous attempt failed, then load the Properties class.
            if ((propertiesFromClass_ == null) && (propertiesClassLoadFailed_ == false))
            {
                loadPropertiesFromClass();
            }

            if (propertiesFromClass_ != null)
            {
                propertyValue = propertiesFromClass_.getProperty(propertyName);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Value found in Properties class: '" + propertyValue + "'");
            }
        }

        // If it is still not found, then look in the properties file.
        if (propertyValue == null)
        {
            // If the jt400.properties file has not yet been loaded, and no previous attempt failed, then load the properties file.
            if ((propertiesFromFile_ == null) && (propertiesFileLoadFailed_ == false))
            {
                loadPropertiesFromFile();
            }

            if (propertiesFromFile_ != null)
            {
                propertyValue = propertiesFromFile_.getProperty(propertyName);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Value found in jt400.properties file: '" + propertyValue + "'");
            }
        }

        if (Trace.isTraceOn()) if (propertyValue == null) Trace.log(Trace.DIAGNOSTIC, "Value not found.");

        return propertyValue;
    }

    // Tells this object to ignore any settings for a system property.
    // @param  propertyName  The system property name.
    static void ignoreProperty(String propertyName)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.WARNING, "Adding system property to ignore list: '" + propertyName + "'");
        ignored_.addElement(propertyName);
    }

    // Loads the properties from the Properties class (if one exists on the classpath).
    private static void loadPropertiesFromClass()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading Properties class: '" + PROPERTIES_CLASS_NAME + "'");

        try
        {
            propertiesFromClass_ = (Properties)Class.forName(PROPERTIES_CLASS_NAME).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            propertiesClassLoadFailed_ = true;
            if (Trace.isTraceDiagnosticOn()) Trace.log(Trace.DIAGNOSTIC, "Class not found: " + PROPERTIES_CLASS_NAME);
        }
        catch (Throwable e)
        {
            // We catch Throwable here because certain browsers (to remain nameless) throw ClassFormatError instead of an exception.
            propertiesClassLoadFailed_ = true;
            if (Trace.isTraceDiagnosticOn()) Trace.log(Trace.DIAGNOSTIC, "Unable to load class: " + PROPERTIES_CLASS_NAME, e);
        }
    }

    // Loads the properties from the jt400.properties file.
    private static void loadPropertiesFromFile()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading jt400.properties file: '" + PROPERTIES_FILE_NAME + "'");

        propertiesFromFile_ = new Properties();
        InputStream input = null;
        BufferedInputStream bis = null;
        try
        {
            // Try to load the properties file using two different approaches.  The first works on some environments, the second on others.
            input = SystemProperties.class.getResourceAsStream(PROPERTIES_FILE_NAME);
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Trying with Class.getResourceAsStream(): " + (input != null));

            if (input == null)
            {
                input = ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE_NAME_WITH_SLASHES);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Trying with ClassLoader.getSystemResourceAsStream(): " + (input != null));
            }

            // If that does not work, then we can't find the jt400.properties file.
            if (input == null)
            {
                propertiesFileLoadFailed_ = true;
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Load of jt400.properties failed.");
                return;
            }

            // Load the properties from the jt400.properties file.
            bis = new BufferedInputStream(input);
            propertiesFromFile_.load(bis);
            bis.close();  // this also closes the underlying stream ('input')
            bis = null;
            input = null;
        }
        catch (Exception e)
        {
            // We catch Exception here (rather than IOException) because browsers throw SecurityException when we try to read a local file.
            propertiesFileLoadFailed_ = true;
            if (Trace.isTraceWarningOn()) Trace.log(Trace.WARNING, "Unable to load jt400.properties file: " + PROPERTIES_FILE_NAME, e);
        }
        finally
        {
          if (bis != null) {
            try { bis.close(); input = null; }
            catch (Throwable t) { if (Trace.traceOn_) Trace.log(Trace.ERROR, t); }
          }
          if (input != null) {
            try { input.close(); }
            catch (Throwable t) { if (Trace.traceOn_) Trace.log(Trace.ERROR, t); }
          }
        }
    }
}
