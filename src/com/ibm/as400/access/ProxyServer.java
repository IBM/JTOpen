///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProxyServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;



/**
<p>The ProxyServer class is used to fulfill requests from programs
using the proxy jar file.  The proxy server is responsible for
creating and invoking methods on Toolbox objects on behalf of the
program.  The proxy server is intended for use on a middle-tier in
a multiple tier environment.

<p>If there is already a proxy server active for the specified
port, then a new proxy server will not be started.  Instead,
the existing proxy server's configuration will be updated.

<p>A ProxyServer object can be created and run directly from a
program.  Alternately, the proxy server can be run as an
application, as follows:

<blockquote>
<pre>
<strong>java com.ibm.as400.access.ProxyServer</strong> [ options ]
</pre>
</blockquote>

<p>Options:
<dl>

<dt><b><code>-balanceThreshold </b></code><var>balanceThreshold</var></dt>
<dd>
Specifies the number of connections that must be active before
the peer server starts load balancing by dispatching requests to
peer proxy servers.  Specify 0 to start load balancing immediately or
-1 to never start load balancing.  This option may be abbreviated
<code>-bt</code>.  The default is -1.  This has no effect unless peer
proxy servers are specified.
</dd>

<dt><b><code>-configuration </b></code><var>configuration</var></dt>
<dd>
Specifies a properties file which lists configuration
properties in the following format:
<pre>
balanceThreshold=<var>balanceThreshold</var>
jdbcDrivers=<var>jdbcDriver1[;jdbcDriver2[;...]]</var>
maxConnections=<var>maxConnections</var>
peers=<var>hostname1[:port1][;hostname2[:port2][;...]]</var>
verbose=true|false
</pre>
This option may be abbreviated <code>-c</code>.  The default is
to not load a configuration.  If a property is loaded in
a configuration and specified in a command line argument,
then the command line argument takes precedence.
</dd>

<dt><b><code>-jdbcDrivers </b></code><var>jdbcDriver1[;jdbcDriver2;...]</var></dt>
<dd>
Specifies a list of JDBC driver class names to register with
the JDBC DriverManager.  Use this to register any JDBC drivers
to which clients might need to connect.  This option may be abbreviated
<code>-jd</code>.  The default is to load only the IBM Toolbox
for Java JDBC driver.
</dd>

<dt><b><code>-maxConnections </b></code><var>maxConnections</var></dt>
<dd>
Specifies the maximum number of connections which can be active
at any particular time.  This refers to connections to the proxy
server, that are initiated by clients. If the maximum number of
connections are active, then any further connection requests will
be rejected and an exception is thrown to the client program.
Specify 0 to not allow any connections, or -1 for no limit.
This option may be abbreviated <code>-mc</code>.  The default is
to allow an unlimited number of connections.
</dd>

<dt><b><code>-peers </b></code><var>hostname1[:port1][;hostname2[:port2];...]</var></dt>
<dd>
Specifies a list of peer proxy servers for use in load balancing.
In some cases, connections to this proxy server will be
reassigned to a peer.  This option may be abbreviated
<code>-pe</code>.  The default is not to do load balancing.
</dd>

<dt><b><code>-port </b></code><var>port</var></dt>
<dd>
Specifies the port to use for accepting connections from clients.
This option may be abbreviated <code>-po</code>.  The default port is 3470.
</dd>

<dt><b><code>-securePort </b></code><var>securePort</var></dt>
<dd>
Specifies the port to use for accepting Secure Sockets Layer (SSL)
connections from clients.  This option may be abbreviated <code>-sp</code>.
The proxy server will only accept SSL connections
when the com.ibm.sslight package is in the classpath.
The default port is 3471.
</dd>

<dt><b><code>-keyringName </b></code><var>keyringName</var></dt>
<dd>
Specifies the keyring to use for Secure Sockets Layer (SSL)
communications from clients.  This option may be abbreviated <code>-kn</code>.
The keyring specified by this option must be in the classpath for SSL
communications to work properly between the proxy server and client.
If the com.ibm.sslight package is not the classpath, this option will
be ignored.
</dd>

<dt><b><code>-keyringPassword </b></code><var>keyringPassword</var></dt>
<dd>
Specifies the password to the keyring.  This option may be
abbreviated <code>-kp</code>.  This option msut be used in conjunction
with the -keyringName option.  If the com.ibm.sslight package is not the
classpath, this option will be ignored.
</dd>

<dt><b><code>-verbose</b></code> [true|false]</dt>
<dd>
Specifies whether to print status and connection
information to System.out. This option may be abbreviated
<code>-v</code>.  The default is not to print status and
connection information.
</dd>

<dt><b><code>-help</b></code></dt>
<dd>
Prints usage information to System.out.  This option may be abbreviated
<code>-h</code> or <code>-?</code>.  The default is not to print usage
information.
</dd>

</dl>

<p>Example usage:

<p>To start the proxy server from a program:
<pre>
ProxyServer proxyServer = new ProxyServer ();
proxyServer.setMaxConnections (25);
proxyServer.run ();
</pre>

<p>Alternatively, the above action can be performed directly
from the command line as follows:
<pre>
java com.ibm.as400.access.ProxyServer -maxconnections 25
</pre>
**/
//
// Implementation notes:
//
// 1.  It was suggested that we provide a property which defines
//     whether we listen for non-secure connections, SSL connections,
//     or both.  A suggestion says that this is more of a firewall
//     issue, and the specific ports can be turned on/off at the
//     firewall.
//
public class ProxyServer
{
    // Private data.
    private static final PrintStream                errors_             = System.err;

    private PSConfig                configuration_;
    private PSLoad                         load_;
    private PSLoadBalancer                 loadBalancer_;
    private int                                     port_;
    private int                                     securePort_;        //$B1C
    private String                                  keyringName_;       //$B1A
    private String                                  keyringPwd_;        //$B1A
    private Vector                                  threadGroup_;



    static
    {
        // If the proxy server is running, it should ignore the proxy
        // server system property!  (Otherwise, it would make connections
        // to itself.)
        SystemProperties.ignoreProperty(SystemProperties.AS400_PROXY_SERVER);
    }



/**
Constructs a ProxyServer object.
**/
    public ProxyServer ()
    {
        port_                   = ProxyConstants.PORT_NUMBER;
        securePort_             = ProxyConstants.SECURE_PORT_NUMBER;       //$B1C
        keyringName_            = null;                                    //$B1A
        keyringPwd_             = null;                                    //$B1A
        threadGroup_            = null;

        load_                   = new PSLoad ();
        loadBalancer_           = new PSLoadBalancer (load_);
        configuration_          = new PSConfig (load_, loadBalancer_);
    }



/**
Returns the number of active connections.

@return The number of active connections.
**/
    public int getActiveConnections ()
    {
        return load_.getActiveConnections ();
    }



/**
Returns the balance threshold.  This is the number of
connections that must be active before the peer server starts
load balancing by dispatching requests to peer proxy servers.
Specify 0 to start load balancing immediately or -1 to never
start load balancing.

@return The balance threshold, or 0 to start load
        balancing immediately or -1 to never start load
        balancing.
**/
    public int getBalanceThreshold ()
    {
        return load_.getBalanceThreshold ();
    }



/**
Returns the name of the configuration properties.

@return The name of the configuration properties, or null
        if not set.
**/
    public String getConfiguration()
    {
        return configuration_.getName ();
    }


/**
Returns the keyring file that the proxy server will use during
SSL connections from clients.

@return The proxy server keyring file name.
**/
    public String getKeyring ()           //$B1A
    {
       return keyringName_;
    }



/**
Returns the password to the proxy server keyring file.

@return The proxy server keyring password.
**/
    String getKeyringPassword ()          //$B1A
    {
       return keyringPwd_;
    }



/**
Returns the maximum number of connections which can be
active at any particular time.

@return The maximum number of connections which can be
        active at any particular time, or -1 for
        unlimited connections.
**/
    public int getMaxConnections ()
    {
        return load_.getMaxConnections ();
    }



/**
Returns a list of peer proxy servers for use in load balancing.
Each peer proxy server is specified in the format
<var>hostname[:port]</var>.  This returns an empty array if
there are no peer proxy servers.

@return A list of peer proxy servers for use in load balancing.
**/
    public String[] getPeers ()
    {
        return loadBalancer_.getPeers ();
    }



/**
Returns the port to use for accepting connections from clients.

@return The port to use for accepting connections from clients.
**/
    public int getPort ()
    {
        return port_;
    }



/**
Returns the port to use for accepting Secure Sockets Layer (SSL)
connections from clients.

@return The port to use for accepting Secure Sockets Layer (SSL)
        connections from clients.
**/
    public int getSecurePort ()           //$B1C
    {
        return securePort_;
    }



/**
Indicates if the proxy server has been started.

@return true if the proxy server has been started, false otherwise.
**/
    public boolean isStarted ()
    {
        return (threadGroup_ != null);
    }



/**
Indicates whether to print status and connection information
to System.out.

@return true to print status and connection information to
        System.out, false otherwise.
**/
    public boolean isVerbose ()
    {
        return Verbose.isVerbose ();
    }



/**
Runs the proxy server as an application.

@param args The command line arguments.
**/
    public static void main (String args[])
    {
        ProxyServer proxyServer = new ProxyServer ();
        if (proxyServer.parseArgs (args)) {
            Verbose.forcePrintln (ResourceBundleLoader.getText ("PROXY_SERVER_STARTED"));
            proxyServer.start ();
        }
        else {
            PSConfig.usage (System.err);
        }
    }



/**
Parses the command line arguments and sets the properties
accordingly.

@param args     The command line arguments.
@return         true if the combination of command line
                arguments is valid, false otherwise.
**/
    private boolean parseArgs (String[] args)
    {
        CommandLineArguments cla = new CommandLineArguments (args,
            PSConfig.expectedOptions_,
            PSConfig.shortcuts_);

        if (cla.getOptionValue ("-help") != null)
            return false;

        try {
            configuration_.apply (cla);
        }
        catch (Exception e) {
            errors_.println (e.getMessage ());
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Exception while parsing command line arguments", e);
            return false;
        }

        // Options that are not part of the configuration.
        String portOptionValue = cla.getOptionValue ("-port");
        if (portOptionValue != null)
            if (portOptionValue.length() > 0)
                setPort (Integer.parseInt (portOptionValue));

        String securePortOptionValue = cla.getOptionValue ("-securePort");                              //$B1C
        if (securePortOptionValue != null)                                                              //$B1C
            if (securePortOptionValue.length() > 0)                                                     //$B1C
                setSecurePort (Integer.parseInt (securePortOptionValue));                               //$B1C

        String keyringNameOptionValue = cla.getOptionValue ("-keyringName");                            //$B1A
        String keyringPwdOptionValue = cla.getOptionValue ("-keyringPassword");                         //$B1A
        try                                                                                             //$B1A
        {                                                                                               //$B1A
           // Try to load the SSLight classes.  If they are not found then we do not need to            //$B1A
           // check for the keyring command line parms.                                                 //$B1A
                                                                                                        //$B1A
           Class.forName("com.ibm.sslight.SSLContext");                                                 //$B1A
           if (keyringNameOptionValue != null && keyringPwdOptionValue != null)                         //$B1A
           {                                                                                            //$B1A
              if (keyringNameOptionValue.length() > 0 && keyringPwdOptionValue.length() > 0)            //$B1A
              {                                                                                         //$B1A
                 setKeyringName (keyringNameOptionValue);                                               //$B1A
                 setKeyringPassword (keyringPwdOptionValue);                                            //$B1A
              }                                                                                         //$B1A
              else                                                                                      //$B1A
              {                                                                                         //$B1A
                 Verbose.forcePrintln ("\n" +                                                           //$B1A
                                       ResourceBundleLoader.getText("PROXY_SERVER_NO_KEYRING") +        //$B1A
                                       "\n");                                                           //$B1A
              }                                                                                         //$B1A
           }                                                                                            //$B1A
           else                                                                                         //$B1A
           {                                                                                            //$B1A
              Verbose.forcePrintln ("\n" +                                                              //$B1A
                                    ResourceBundleLoader.getText("PROXY_SERVER_KEYRING_EXPLAIN") +      //$B1A
                                    "\n");                                                              //$B1A
           }                                                                                            //$B1A
        }                                                                                               //$B1A
        catch (ClassNotFoundException e)                                                                //$B1A
        { /* No need to parse the keyring options since the SSLight classes are not in the CLASSPATH. */ //$B1A
        }                                                                                               //$B1A


        // Extra options.
        Enumeration list = cla.getExtraOptions ();
        while (list.hasMoreElements ()) {
            String extraOption = list.nextElement ().toString ();
            errors_.println (ResourceBundleLoader.getText ("PROXY_OPTION_NOT_VALID", extraOption));
        }

        // Values without options.                                                                         @A1A
        String noOptionValues = cla.getOptionValue("-");                                                // @A1A
        if (noOptionValues != null)                                                                     // @A1A
            if (noOptionValues.length() > 0)                                                            // @A1A
                errors_.println(ResourceBundleLoader.getText("PROXY_VALUE_NO_OPTION", noOptionValues)); // @A1A

        return true;
    }



/**
Sets the number of connections that must be active before
the peer server starts load balancing by dispatching requests to
peer proxy servers.  The default is -1.

@param balanceThreshold The number of connections that must be
                        active before the peer server starts load
                        balancing by dispatching requests to peer
                        proxy servers.  Specify 0 to start load
                        balancing immediately or -1 to never start
                        load balancing.
**/
    public void setBalanceThreshold (int balanceThreshold)
    {
        load_.setBalanceThreshold (balanceThreshold);
    }


/**
Sets and loads the properties file which lists
configuration properties.  The default is not to load
a configuration.

@param configuration    The properties file which list
                        configuration properties.

@exception IOException If the configuration can not be loaded.
**/
    public void setConfiguration (String configuration)
        throws IOException
    {
        if (configuration == null)
            throw new NullPointerException ("configuration");
        if (configuration.length() == 0)
            throw new ExtendedIllegalArgumentException ("configuration", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        configuration_.setName (configuration);
        configuration_.load ();
    }


/**
Sets the name of the keyring the proxy server
will use during SSL communications.  The proxy
server keyring name can be set only if the proxy
server is not running.

@param keyringName The proxy server keyring name.
**/
    public void setKeyringName(String keyringName)              //$B1A
    {
       if (keyringName == null)
          throw new NullPointerException ("keyringName");
       if (keyringName.length() == 0)
          throw new ExtendedIllegalArgumentException ("keyringName", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

       keyringName_ = keyringName;
    }


/**
Sets the password to the keyring the proxy server
will use during SSL communications.  The proxy
server keyring password can be set only if the proxy
server is not running.

@param keyringPassword The proxy server keyring password.
**/
    public void setKeyringPassword(String keyringPassword)      //$B1A
    {
       if (keyringPassword == null)
          throw new NullPointerException ("keyringPassword");
       if (keyringPassword.length() == 0)
          throw new ExtendedIllegalArgumentException ("keyringPassword", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

       keyringPwd_ = keyringPassword;
    }



/**
Sets the maximum number of connections which can be active
at any particular time.  If the maximum number of connections
are active, then any further connection requests will be
rejected.  The default is to allow an unlimited number of
connections.

<p>Setting this to a number less than the number of active
connections will not drop any active connections.  It will
simply prevent new connections from being accepted until the
number of active connections is less than the limit.

@param maxConnections   The maximum number of connections
                        which can be active at any particular
                        time.  Specify 0 to not allow any
                        connections or -1 for unlimited
                        connections.
**/
    public void setMaxConnections (int maxConnections)
    {
        load_.setMaxConnections (maxConnections);
    }



/**
Sets the list of peer proxy servers for use in load
balancing.  In some cases, connections to the proxy
server will be reassigned to a peer.  The default is
not to do load balancing.  Specify each peer proxy
server in the format <var>hostname[:port]</var>.

@param peers    The list of peer proxy servers for
                use in load balancing, or an empty
                array to not do load balancing.
**/
    public void setPeers (String[] peers)
    {
        if (peers == null)
            throw new NullPointerException ("peers");
        for (int i = 0; i < peers.length; ++i) {
            if (peers[i] == null)
                throw new NullPointerException ("peers[" + i + "]");
            if (peers[i].length() == 0)
                throw new ExtendedIllegalArgumentException ("peers[" + i + "]", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        loadBalancer_.setPeers (peers);
    }



/**
Sets the port to use for accepting connections from
clients.  The default is 3470.  Specify 0 to indicate
that any free port can be used.  The port number can
be set only if the proxy server is not running.

@param port The port to use for accepting connections
            from clients.
**/
    public void setPort (int port)
    {
        if (isStarted ())
            throw new ExtendedIllegalStateException ("port", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        if ((port < 0) || (port > 65535))
            throw new ExtendedIllegalArgumentException ("port", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        port_ = port;
    }


/**
Sets the port to use for accepting Secure Sockets
Layer (SSL) connections from clients.  The default
is 3471. Specify 0 to indicate that any free port
can be used.  The proxy server will only accept SSL
connections when the com.ibm.sslight package is in
the classpath.   The secure port number can be set
only if the proxy server is not running.

@param securePort The port to use for accepting Secure
            Sockets Layer (SSL) connections from
            clients.
**/
    public void setSecurePort (int securePort)                                             //$B1C
    {
        if (isStarted ())
            throw new ExtendedIllegalStateException ("port", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        if ((securePort < 0) || (securePort > 65535))
            throw new ExtendedIllegalArgumentException ("port", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        securePort_ = securePort;
    }



/**
Sets whether to print status and connection information
to System.out.  The default is false.

@param verbose  true to print status and connection
                information to System.out, false otherwise.
**/
    public void setVerbose (boolean verbose)
    {
        Verbose.setVerbose (verbose);
    }



/**
Starts the proxy server.  This starts several threads which process
client requests, and then return immediately.  The proxy server is still
running after the return.  Use <a href="#stop()">stop()</a> to stop all threads
for this proxy server.
**/
    public void start()
    {
        // Throw an exception if the proxy server is already started.
        if (threadGroup_ != null)
            throw new ExtendedIllegalStateException (ExtendedIllegalStateException.PROXY_SERVER_ALREADY_STARTED);

        // Initialize a thread group for this proxy server.  This will make
        // it much easier to stop all running threads.
        threadGroup_ = new Vector ();

        // Initialize the server socket.  If the port is in use, send the
        // existing proxy server a configure request.
        try {
            PSServerSocketContainerAdapter serverSocket = new PSServerSocketContainer (port_);
            port_ = serverSocket.getLocalPort ();
            PSController controller = new PSController (threadGroup_,
                                                                          this,
                                                                          load_,
                                                                          loadBalancer_,
                                                                          configuration_,
                                                                          serverSocket);

            controller.start ();
            threadGroup_.addElement (controller);
            Verbose.println (ResourceBundleLoader.getText ("PROXY_SERVER_LISTENING", serverSocket, Integer.toString (port_)));
        }
        catch (BindException e) {
            Verbose.println (ResourceBundleLoader.getText ("PROXY_ALREADY_LISTENING", Integer.toString (port_)));

            try {
                PxPeerConnection peerConnection = new PxPeerConnection (InetAddress.getLocalHost ().getHostName () + ":" + port_);
                peerConnection.configure (configuration_);
                peerConnection.close ();
                return;
            }
            catch (UnknownHostException e1) {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, "Peer host is unknown.", e);
                errors_.println (e.getMessage ());
            }
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error opening server socket.", e);
            errors_.println (e.getMessage ());
        }

        // Check to see if the sslight classes are in the classpath.
        // If so, we can handle SSL, otherwise we can not.
        //
        // Note that we do not need to send a configure request in
        // this case, since the server will already be reconfigured
        // using the non-secure socket (and it is the same server,
        // just listenening to 2 ports, not 2 servers).                    //$B1C
        try
        {
            Class.forName ("com.ibm.sslight.SSLContext");

            // The SSL Proxy port will only be started if the keyringName and keyringPwd are                       //$B1A
            // correctly specified.  If either command line option is bad, the Proxy Server
            // will not do SSL, but will continue to handle regular Proxy communications.
            if (keyringName_ != null && keyringPwd_ != null)                                                       //$B1A
            {
               PSServerSocketContainerAdapter serverSocket = new PSSecureServerSocketContainer (securePort_,       //$B1C
                                                                                                keyringName_,      //$B1C
                                                                                                keyringPwd_);      //$B1C

               try                                                                                                 //$B1A
               {
                  securePort_ = serverSocket.getLocalPort ();
                  PSController controller = new PSController (threadGroup_,
                                                              this,
                                                              load_,
                                                              loadBalancer_,
                                                              configuration_,
                                                              serverSocket);

                  controller.start ();
                  threadGroup_.addElement (controller);
                  Verbose.println (ResourceBundleLoader.getText ("PROXY_SERVER_LISTENING", serverSocket, Integer.toString (securePort_)));
               }
               catch (NullPointerException e)                                                                      //$B1A
               {                                                                                                   //$B1A
                  // If the proxy server failed to load the proxy keyring, then we don't                           //$B1A
                  // want to display a message saying the secure proxy server was started.  Instead display        //$B1A
                  // a message saying it didn't start and the details can be seeing by turning ERROR tracing on.   //$B1A
                  Verbose.println (ResourceBundleLoader.getText ("PROXY_SERVER_LISTENING",                         //$B1A
                                                                 "Secure proxy server NOT",                        //$B1A
                                                                 Integer.toString (securePort_)));                 //$B1A
               }
            }
        }
        catch (ClassNotFoundException e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "SSLight classes are not in the classpath, SSL support is not enabled.");
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error opening server secure socket.", e);
            errors_.println (e.getMessage ());
        }

    }



/**
Stops the proxy server.  This stops all threads relating to this
proxy server.
**/
    public void stop()
    {
        // Throw an exception if the proxy server is not started.
        if (threadGroup_ == null)
            throw new ExtendedIllegalStateException (ExtendedIllegalStateException.PROXY_SERVER_NOT_STARTED);

        // Force the load's active connection count to be accurate.
        load_.allConnectionsClosed ();

        // Stop all of the threads safely.
        Enumeration list = threadGroup_.elements ();
        while (list.hasMoreElements ())
            ((StoppableThread) list.nextElement ()).stopSafely ();

        // Clear the thread group.
        threadGroup_ = null;
    }



}
