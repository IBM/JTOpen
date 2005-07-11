///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSConfig.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;



/**
The PSConfig class represents the configuration
of a proxy server.
**/
class PSConfig
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private static final String                 OPTION_BALANCE_THRESHOLD    = "balanceThreshold";
    private static final String                 OPTION_CONFIGURATION        = "configuration";
    private static final String                 OPTION_JDBC_DRIVERS         = "jdbcDrivers";
    private static final String                 OPTION_MAX_CONNECTIONS      = "maxConnections";
    private static final String                 OPTION_PEERS                = "peers";
    private static final String                 OPTION_VERBOSE              = "verbose";

    static final Vector                         expectedOptions_            = new Vector ();
    static final Hashtable                      shortcuts_                  = new Hashtable ();



    private PSLoad                     load_;
    private PSLoadBalancer             loadBalancer_;
    private String                              name_;
    private Properties                          properties_;



/**
Static initializer.
**/
    static 
    {        
        // Expected options for the ProxyServer application.
        expectedOptions_.addElement ("-balanceThreshold");
        expectedOptions_.addElement ("-configuration");
        expectedOptions_.addElement ("-jdbcDrivers");
        expectedOptions_.addElement ("-maxConnections");
        expectedOptions_.addElement ("-peers");
        expectedOptions_.addElement ("-port");
        expectedOptions_.addElement ("-securePort");
        expectedOptions_.addElement ("-keyringName");                               //$B1A
        expectedOptions_.addElement ("-keyringPassword");                           //$B1A
        expectedOptions_.addElement ("-verbose");
        expectedOptions_.addElement ("-help");

        // Shortcuts for the ProxyServer application.
        // Note: These are also listed in usage().                                  // @A1A
        shortcuts_.put ("-bt", "-balanceThreshold");
        shortcuts_.put ("-c", "-configuration");
        shortcuts_.put ("-jd", "-jdbcDrivers");
        shortcuts_.put ("-mc", "-maxConnections");
        shortcuts_.put ("-pe", "-peers");
        shortcuts_.put ("-po", "-port");
        shortcuts_.put ("-sp", "-securePort");
        shortcuts_.put ("-kn", "-keyringName");                                     //$B1A
        shortcuts_.put ("-kp", "-keyringPassword");                                 //$B1A
        shortcuts_.put ("-v", "-verbose");
        shortcuts_.put ("-h", "-help");
        shortcuts_.put ("-?", "-help");
    }



/**
Constructs a PSConfig object.

@param load         The load.
@param loadBalancer The load balancer.
**/
    public PSConfig (PSLoad load, PSLoadBalancer loadBalancer)
    {
        load_           = load;
        loadBalancer_   = loadBalancer;

        name_           = null;
        properties_     = new Properties ();
    }



/**
Applies the command line arguments to the configuration.

@param cla  The command line arguments.
**/
    public void apply (CommandLineArguments cla)
        throws IOException
    {        
        Trace.loadTraceProperties ();
        String optionValue;

        // Apply the verbose option first, so that
        // we see messages.
        optionValue = cla.getOptionValue (OPTION_VERBOSE);
        if (optionValue != null) {
            setVerbose (optionValue);
            properties_.put (OPTION_VERBOSE, optionValue);
        }
                   
        // Load the configuration file first, so that command 
        // line arguments override any settings.
        optionValue = cla.getOptionValue (OPTION_CONFIGURATION);
        if (optionValue != null) {
            setName (optionValue);
            load ();
        }
                             
        // Re-apply the verbose option first, just in case
        // the condiguration file overrode it.
        optionValue = cla.getOptionValue (OPTION_VERBOSE);
        if (optionValue != null) {
            setVerbose (optionValue);
            properties_.put (OPTION_VERBOSE, optionValue);
        }
                   
        // Other options.
        optionValue = cla.getOptionValue (OPTION_BALANCE_THRESHOLD);
        if (optionValue != null) {
            load_.setBalanceThreshold (Integer.parseInt (optionValue));
            properties_.put (OPTION_BALANCE_THRESHOLD, optionValue);
        }
        
        optionValue = cla.getOptionValue (OPTION_JDBC_DRIVERS);
        if (optionValue != null) {
            registerJDBCDrivers (optionValue);
            properties_.put (OPTION_JDBC_DRIVERS, optionValue);
        }
        
        optionValue = cla.getOptionValue (OPTION_MAX_CONNECTIONS);
        if (optionValue != null) {
            load_.setMaxConnections (Integer.parseInt (optionValue));
            properties_.put (OPTION_MAX_CONNECTIONS, optionValue);
        }
        
        optionValue = cla.getOptionValue (OPTION_PEERS);
        if (optionValue != null) {
            loadBalancer_.setPeers (optionValue);
            properties_.put (OPTION_PEERS, optionValue);
        }
    }



/**
Applies a set of properties to the configuration.

@param configurationProperties  The configuration properties.
**/
    public void apply (Properties configurationProperties)
    {
        Trace.loadTraceProperties ();

        // Take note of the verbose state before changing it.
        boolean verboseBefore = Verbose.isVerbose ();

        // Iterate through the changed configuration properties.
        Enumeration list = configurationProperties.propertyNames ();
        while (list.hasMoreElements ()) {        

            String optionName = (String) list.nextElement ();
            String optionValue = configurationProperties.getProperty (optionName);
            if (Trace.isTraceProxyOn ()) 
                Trace.log (Trace.PROXY, "Changing option " + optionName + " to " + optionValue + ".");
           
            else if (optionName.equalsIgnoreCase (OPTION_BALANCE_THRESHOLD)) {
                load_.setBalanceThreshold (Integer.parseInt (optionValue));
                properties_.put (OPTION_BALANCE_THRESHOLD, optionValue);
            }

            else if (optionName.equalsIgnoreCase (OPTION_JDBC_DRIVERS)) {
                registerJDBCDrivers (optionValue);
                properties_.put (OPTION_JDBC_DRIVERS, optionValue);
            }

            else if (optionName.equalsIgnoreCase (OPTION_MAX_CONNECTIONS)) {
                load_.setMaxConnections (Integer.parseInt (optionValue));
                properties_.put (OPTION_MAX_CONNECTIONS, optionValue);
            }

            else if (optionName.equalsIgnoreCase (OPTION_PEERS)) {
                loadBalancer_.setPeers (optionValue);
                properties_.put (OPTION_PEERS, optionValue);
            }

            else if (optionName.equalsIgnoreCase (OPTION_VERBOSE)) {
                setVerbose (optionValue);
                properties_.put (OPTION_VERBOSE, optionValue);
            }

            else if (optionName.trim().length() > 0)
                throw new IllegalArgumentException (ResourceBundleLoader.getText ("PROXY_OPTION_NOT_VALID", optionName));            
        }

        // Print this message if verbose used to be on or is on now.
        boolean verboseAfter = Verbose.isVerbose ();
        if (verboseBefore || verboseAfter)
            Verbose.forcePrintln (ResourceBundleLoader.getText ("PROXY_CONFIGURATION_UPDATED"));
    }



/**
Returns the list of registered JDBC drivers.

@return The list of registered JDBC drivers, delimited
        by semicolons.
**/
    /* 
    public String getJDBCDrivers ()
    {
        StringBuffer buffer = new StringBuffer ();
        Enumeration list = DriverManager.getDrivers ();
        while (list.hasMoreElements ()) {
            buffer.append (((String) list.nextElement ()).getClass ().getName ());
            buffer.append (';');
        }
        return buffer.toString ();
    }
    */



/**
Returns the name of configuration, if any.

@return The name of configuration, or null if none.
**/
    public String getName ()
    {
        return name_;
    }



/**
Returns the properties object used to store the 
configuation.

@return The properties object used to store the 
        configuation.
**/
    public Properties getProperties ()
    {   
        return properties_;
    }



/**
Loads the configuration from a file.
**/
    public void load ()
        throws IOException
    {
        Properties configurationProperties = new Properties ();
        InputStream input = new BufferedInputStream (new FileInputStream (name_));
        configurationProperties.load (input);
        apply (configurationProperties);
    }



/**
Registers the specified JDBC drivers.

@param jdbcDrivers  The semicolon delimited list of
                    JDBC drivers to register
**/
    private void registerJDBCDrivers (String jdbcDrivers)
    {
        StringTokenizer tokenizer = new StringTokenizer (jdbcDrivers, ";, ");
        while (tokenizer.hasMoreTokens ()) {
            String token = tokenizer.nextToken ();
            try {                    
                // Load the class.  It will register itself the first time
                // (when the class is loaded).  We register it also to make
                // sure, since the driver could have been deregistered.
                Class driverClass = Class.forName (token);
                Driver driver = (Driver) driverClass.newInstance ();
                boolean found = false;
                Enumeration list = DriverManager.getDrivers ();
                while ((list.hasMoreElements ()) && (found == false)) {
                    Driver enumDriver = (Driver) list.nextElement ();
                    if (enumDriver.getClass ().equals (driverClass))
                        found = true;
                }
                if (! found)
                    DriverManager.registerDriver ((Driver) Class.forName (token).newInstance ());
                Verbose.println (ResourceBundleLoader.getText ("PROXY_JDBC_DRIVER_REGISTERED", token));
            }
            catch (Exception e) {  
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, "JDBC driver not found.", e);
                Verbose.println (ResourceBundleLoader.getText ("PROXY_JDBC_DRIVER_NOT_REGISTERED", token));
            }
        }
    }



/**
Sets the name of the configuration.

@param name The configuration name.
**/
    public void setName (String name)
    {
        name_ = name;        
    }



/**
Sets whether verbose output is printed.

@param verbose "true" or "" if verbose output is printed; "false" otherwise.
**/
    private void setVerbose (String verbose)
    {
        if ((verbose.length () == 0) || (verbose.equalsIgnoreCase ("true"))) 
            Verbose.setVerbose (true);
        else if (verbose.equalsIgnoreCase ("false")) 
            Verbose.setVerbose (false); 
        else 
            throw new IllegalArgumentException (ResourceBundleLoader.getText ("PROXY_OPTION_VALUE_NOT_VALID", new String[] { OPTION_VERBOSE, verbose })); 
    }



/**
Prints the application usage information.

@param out  The print stream for usage information.
**/
    static void usage (PrintStream out)
    {
        final String usage      = ResourceBundleLoader.getText ("PROXY_SERVER_USAGE");
        final String optionslc  = ResourceBundleLoader.getText ("PROXY_SERVER_OPTIONSLC");
        final String optionsuc  = ResourceBundleLoader.getText ("PROXY_SERVER_OPTIONSUC");
        final String shortcuts  = ResourceBundleLoader.getText ("PROXY_SERVER_SHORTCUTS");  // @A1A

        out.println (usage + ":");
        out.println ();
        out.println ("  com.ibm.as400.access.ProxyServer [ " + optionslc + " ]");
        out.println ();
        out.println (optionsuc + ":");
        out.println ();
        out.println ("  -balanceThreshold balanceThreshold");
        out.println ("  -configuration configuration");
        out.println ("  -jdbcDrivers jdbcDriver1[;jdbcDriver2;...]");       // @B2C
        out.println ("  -maxConnections maxConnections");
        out.println ("  -peers hostname1[:port1][;hostname2[:port2];...");
        out.println ("  -port port");
        out.println ("  -securePort securePort");                           //$B1C
        out.println ("  -keyringName ProxyServerKeyringName");              //$B1A
        out.println ("  -keyringPassword ProxyServerKeyringPassword");      //$B1A
        out.println ("  -verbose [true|false]");
        out.println ("  -help");
        out.println ();                                                     // @A1A
        out.println (shortcuts + ":");                                      // @A1A
        out.println ();                                                     // @A1A
        out.println ("  -bt balanceThreshold");                             // @A1A
        out.println ("  -c configuration");                                 // @A1A
        out.println ("  -jd jdbcDriver1[;jdbcDriver2;...]");                // @A1A @B2C
        out.println ("  -mc maxConnections");                               // @A1A
        out.println ("  -pe hostname1[:port1][;hostname2[:port2];...");     // @A1A
        out.println ("  -po port");                                         // @A1A
        out.println ("  -sp securePort");                                   // @A1A  $B1C
        out.println ("  -kn ProxyServerKeyringName");                       //$B1A
        out.println ("  -kp ProxyServerKeyringPassword");                   //$B1A
        out.println ("  -v [true|false]");                                  // @A1A
        out.println ("  -h");                                               // @A1A
        out.println ("  -?");                                               // @A1A
    }



}
