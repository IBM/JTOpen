///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

// The SystemProperties class contains constants representing names of all system properties recognized by the IBM Toolbox for Java.
class SystemProperties
{
    // System property constants.
    private static final String ACCESS_PREFIX = "com.ibm.as400.access.";
    private static final String PROPERTIES_CLASS_NAME = ACCESS_PREFIX + "Properties";
    private static final String PROPERTIES_FILE_NAME = ACCESS_PREFIX + "jt400.properties";
    private static final String PROPERTIES_FILE_NAME_WITH_SLASHES = "com/ibm/as400/access/jt400.properties";

    // System property names.
    public static final String AS400_PROXY_SERVER = ACCESS_PREFIX + "AS400.proxyServer";
    public static final String SECUREAS400_PROXY_ENCRYPTION_MODE = ACCESS_PREFIX + "SecureAS400.proxyEncryptionMode";
    public static final String SECUREAS400_USE_SSLIGHT = ACCESS_PREFIX + "SecureAS400.useSslight";
    public static final String TRACE_CATEGORY = ACCESS_PREFIX + "Trace.category";
    public static final String TRACE_FILE = ACCESS_PREFIX + "Trace.file";
    public static final String TRACE_JDBC_SERVER = ACCESS_PREFIX + "ServerTrace.JDBC";     // @j1a
    public static final String TRACE_ENABLED = ACCESS_PREFIX + "Trace.enabled";
    public static final String COMMANDCALL_THREADSAFE = ACCESS_PREFIX + "CommandCall.threadSafe";
    public static final String PROGRAMCALL_THREADSAFE = ACCESS_PREFIX + "ProgramCall.threadSafe";
    public static final String TUNNELPROXYSERVER_CLIENTCLEANUPINTERVAL = ACCESS_PREFIX + "TunnelProxyServer.clientCleanupInterval"; //@A2A
    public static final String TUNNELPROXYSERVER_CLIENTLIFETIME	= ACCESS_PREFIX + "TunnelProxyServer.clientLifetime"; //@A2A
    public static final String FTP_REUSE_SOCKET = ACCESS_PREFIX + "FTP.reuseSocket";
    public static final String AS400_SIGNON_HANDLER = ACCESS_PREFIX + "AS400.signonHandler";
    public static final String JDBC_STATEMENT_LISTENERS = ACCESS_PREFIX + "JDBC.statementListeners";
    //  *** Note: ***
    //
    // If you add a new system property, remember to add them to the following other files:
    //     - SystemProperties.htmlsrc
    //     - SystemPropertiesSample1.htmlsrc
    //     - SystemPropertiesSample2.htmlsrc
    //     - the "near mirror-image" SystemProperties.java in pkg 'test'

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
    // @param  propertyName  The system property name.
    // @return  The system property value, or null if the system property is not specified.
    // Implementation note:  There is currently no reason to make this method public.
    static String getProperty(String propertyName)
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

    // Loads the properties from the Properties class.
    private static void loadPropertiesFromClass()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading Properties class: '" + PROPERTIES_CLASS_NAME + "'");

        try
        {
            propertiesFromClass_ = (Properties)Class.forName(PROPERTIES_CLASS_NAME).newInstance();
        }
        catch (Throwable e)
        {
            // We catch Throwable here because certain browsers (to remain nameless) throw ClassFormatError instead of an exception.
            propertiesClassLoadFailed_ = true;
            if (Trace.isTraceWarningOn()) Trace.log(Trace.WARNING, "Unable to load class: " + PROPERTIES_CLASS_NAME, e);
        }
    }

    // Loads the properties from the jt400.properties file.
    private static void loadPropertiesFromFile()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading jt400.properties file: '" + PROPERTIES_FILE_NAME + "'");

        propertiesFromFile_ = new Properties();
        try
        {
            // Try to load the properties file using two different approaches.  The first works on some environments, the second on others.
            InputStream input = SystemProperties.class.getResourceAsStream(PROPERTIES_FILE_NAME);
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
            propertiesFromFile_.load(new BufferedInputStream(input));
            input.close();
        }
        catch (Exception e)
        {
            // We catch Exception here (rather than IOException) because browsers throw SecurityException when we try to read a local file.
            propertiesFileLoadFailed_ = true;
            if (Trace.isTraceWarningOn()) Trace.log(Trace.WARNING, "Unable to load jt400.properties file: " + PROPERTIES_FILE_NAME, e);
        }
    }
}
