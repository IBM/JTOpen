///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Frame;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.as400.security.auth.ProfileTokenCredential;

/**
 The AS400 class represents an iSeries server sign-on.
 <p>If running on an iSeries server to another iSeries server or to itself, the system name, user ID, and password do not need to be supplied.  These values default to the local system.  For the system name, the keyword localhost can be used to specify the local system.  For the user ID and password, *CURRENT can be used.
 <p>If running on another system to an iSeries server, the system name, user ID, and password need to be supplied.  If not supplied, the first open request associated with this object will prompt the workstation user.  Subsequent opens associated with the same object will not prompt the workstation user.  Keywords localhost and *CURRENT will not work when running from a system other than an iSeries server.
 <p>For example:
 <pre>
 *    AS400 system = new AS400();
 *    system.connectService(AS400.DATAQUEUE);   // This causes a password prompt.
 *    ...
 *    system.connectService(AS400.FILE);        // This does not cause a prompt.
 </pre>
 **/
public class AS400 implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    private static final boolean PASSWORD_TRACE = false;

    /**
     Constant indicating the File service.
     **/
    public static final int FILE = 0;
    /**
     Constant indicating the Print service.
     **/
    public static final int PRINT = 1;
    /**
     Constant indicating the Command service.
     **/
    public static final int COMMAND = 2;
    /**
     Constant indicating the Dataqueue service.
     **/
    public static final int DATAQUEUE = 3;
    /**
     Constant indicating the Database service.
     **/
    public static final int DATABASE = 4;
    /**
     Constant indicating the Record Access service.
     **/
    public static final int RECORDACCESS = 5;
    /**
     Constant indicating the Central service.
     **/
    public static final int CENTRAL = 6;
    /**
     Constant indicating the Sign-on service.
     **/
    public static final int SIGNON = 7;
    // Constants 8-15 reserved for SSL versions of the above servers.

    /**
     Special value indicating the service port should be retrieved from the port mapper server.
     **/
    public static final int USE_PORT_MAPPER = -1;

    /**
     Constant indicating the authentication scheme is password.
     **/
    public static final int AUTHENTICATION_SCHEME_PASSWORD = 0;
    /**
     Constant indicating the authentication scheme is GSS token.
     **/
    public static final int AUTHENTICATION_SCHEME_GSS_TOKEN = 1;
    /**
     Constant indicating the authentication scheme is profile token.
     **/
    public static final int AUTHENTICATION_SCHEME_PROFILE_TOKEN = 2;
    /**
     Constant indicating the authentication scheme is identity token.
     **/
    public static final int AUTHENTICATION_SCHEME_IDENTITY_TOKEN = 3;

    /**
     Constant indicating that the JGSS framework must be used when no password or profile token is set.  An object set to this option will not attempt to present a sign-on dialog or use the current user profile information.  A failure to retrieve the GSS token will result in an exception returned to the user.
     **/
    public static final int GSS_OPTION_MANDATORY = 0;
    /**
     Constant indicating that the JGSS framework will be attempted when no password or profile token is set.  An object set to this option will attempt to retrieve a GSS token, if that attempt fails, the object will present a sign-on dialog or use the current user profile information.  This option is the default.
     **/
    public static final int GSS_OPTION_FALLBACK = 1;
    /**
     Constant indicating that the JGSS framework will not be used when no password or profile token is set.  An object set to this option will only present a sign-on dialog or use the current user profile information.
     **/
    public static final int GSS_OPTION_NONE = 2;

    // Determine if we are running on an iSeries server.
    static boolean onAS400 = false;
    // VRM from system property, if we are native.
    static ServerVersion nativeVRM = null;
    static
    {
        try
        {
            String s = System.getProperty("os.name");
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Detected os.name: " + s);
            if (s != null && s.equalsIgnoreCase("OS/400"))
            {
                String version = System.getProperty("os.version");
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Detected os.version: " + version);
                if (version != null)
                {
                    char[] versionChars = version.toCharArray();
                    int vrm = ((versionChars[1] & 0x000F) << 16) +
                              ((versionChars[3] & 0x000F) <<  8) +
                               (versionChars[5] & 0x000F);
                    AS400.nativeVRM = new ServerVersion(vrm);
                }
                AS400.onAS400 = true;
            }
        }
        catch (SecurityException e)
        {
            Trace.log(Trace.WARNING, "Error retrieving os.name:", e);
        }
    }

    // System list:  elements are 3 element Object[]: systemName, userId, bytes.
    private static Vector systemList = new Vector();
    // Default users is a hash from systemName to userId.
    private static Hashtable defaultUsers = new Hashtable();
    // Number of days previous to password expiration to start to warn user.
    private static int expirationWarning = 7;
    // Random number generator for seeds.
    static Random rng = new Random();

    // System name.
    private String systemName_ = "";
    // User ID.
    private String userId_ = "";
    // Password, GSS token, or profile token bytes twiddled.
    private transient byte[] bytes_ = null;
    // Type of authentication bytes, start by default in password mode.
    private transient int byteType_ = AUTHENTICATION_SCHEME_PASSWORD;
    // GSS name string, for Kerberos.
    private String gssName_ = "";
    // How to use the GSS framework.
    int gssOption_ = GSS_OPTION_FALLBACK;

    // Proxy server system name.
    private transient String proxyServer_ = "";
    // Client side proxy connection information.
    private transient ProxyClientConnection proxyClientConnection_ = null;

    // This controls the prompting.  If set to true, then prompting will occur during sign-on if needed.  If set to false, no prompting will occur and all security errors are returned as exceptions.
    private boolean guiAvailable_ = true;
    // Use the password cache.
    private boolean usePasswordCache_ = true;
    // Use the default user.
    private boolean useDefaultUser_ = true;
    // Show the checkboxes on the password dialog.
    private boolean showCheckboxes_ = true;

    // SSL options, null value indicates SSL is not to be used.  Options set in SecureAS400 subclass.
    SSLOptions useSSLConnection_ = null;
    // Flag that indicates if we must use the host servers and no native optimizations.
    private boolean mustUseSockets_ = false;
    // Flag that indicates if we use threads in communication with the host servers.
    private boolean threadUsed_ = true;
    // Locale object to use for determining NLV.
    private Locale locale_ = Locale.getDefault();
    // Set of socket options to use when creating our connections to the server.
    private SocketProperties socketProperties_ = new SocketProperties();

    // No CCSID to start.
    private transient int ccsid_ = 0;

    // List of connection event bean listeners.
    private transient Vector connectionListeners_ = null;  // Set on first add.
    // Inner class that connects connection events that occur in the ImplRemote to this class.
    private transient ConnectionListener dispatcher_ = null;  // Set on first add.
    // List of property change event bean listeners.
    transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    // Flag for when object state is allowed to change.
    transient boolean propertiesFrozen_ = false;
    // Implementation object.
    private transient AS400Impl impl_ = null;

    // This object is created by the initial sign-on process.  It contains the information from the retrieve sign-on information flow with the sign-on server.
    private transient SignonInfo signonInfo_ = null;

    // The IASP name used for the RECORDACCESS service.
    private String ddmRDB_;

    /**
     Constructs an AS400 object.
     <p>If running on an iSeries server, the target is the local system.  This has the same effect as using localhost for the system name, *CURRENT for the user ID, and *CURRENT for the password.
     <p>If running on another system to an iSeries server, a sign-on prompt is displayed. The user is then able to specify the system name, user ID, and password.
     **/
    public AS400()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object.");
        construct();
        systemName_ = resolveSystem(systemName_);
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    /**
     Constructs an AS400 object.  It uses the specified system name.
     <p>If running on an iSeries server to another iSeries server or to itself, the user ID and password of the current job are used.
     <p>If running on another system to an iSeries server, the user is prompted for the user ID and password if a default user has not been established for this server.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     **/
    public AS400(String systemName)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object, system name: '" + systemName + "'");
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        construct();
        systemName_ = resolveSystem(systemName);
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    /**
     Constructs an AS400 object.  It uses the specified system name and user ID.  When the sign-on prompt is displayed, the user is able to specify the password.  Note that the user ID may be overridden.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     @param  userId  The user profile name to use to authenticate to the server.  If running on an iSeries server, *CURRENT may be used to specify the current user ID.
     **/
    public AS400(String systemName, String userId)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object, system name: '" + systemName + "' user ID: '" + userId + "'");
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        construct();
        systemName_ = resolveSystem(systemName);
        userId_ = userId.toUpperCase();
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    /**
     Constructs an AS400 object.  It uses the specified system name and profile token.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     @param  profileToken  The profile token to use to authenticate to the server.
     **/
    public AS400(String systemName, ProfileTokenCredential profileToken)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object with profile token, system name: '" + systemName + "'");
        if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "profile token: " + profileToken);
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (profileToken == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'profileToken' is null.");
            throw new NullPointerException("profileToken");
        }
        construct();
        systemName_ = resolveSystem(systemName);
        bytes_ = store(profileToken.getToken());
        byteType_ = AUTHENTICATION_SCHEME_PROFILE_TOKEN;
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    /**
     Constructs an AS400 object.  It uses the specified system name, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     @param  userId  The user profile name to use to authenticate to the server.  If running on an iSeries server, *CURRENT may be used to specify the current user ID.
     @param  password  The user profile password to use to authenticate to the server.  If running on an iSeries server, *CURRENT may be used to specify the current user ID.
     **/
    public AS400(String systemName, String userId, String password)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object, system name: '" + systemName + "' user ID: '" + userId + "'");
        if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "password: '" + password + "'");
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        construct();
        systemName_ = resolveSystem(systemName);
        userId_ = userId.toUpperCase();
        bytes_ = store(password);
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    // Private constructor for use when a new object is needed and the password is already twiddled.
    // Used by password cache and password verification code.
    private AS400(String systemName, String userId, byte[] bytes)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing internal AS400 object, system name: '" + systemName + "' user ID: '" + userId + "'");
        if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "bytes:", bytes);
        // System name and user ID validation has been deferred to here.
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        construct();
        systemName_ = resolveSystem(systemName);
        userId_ = userId.toUpperCase();
        bytes_ = bytes;
        proxyServer_ = resolveProxyServer(proxyServer_);
    }

    /**
     Constructs an AS400 object.  It uses the specified system name, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     @param  userId  The user profile name to use to authenticate to the server.  If running on an iSeries server, *CURRENT may be used to specify the current user ID.
     @param  password  The user profile password to use to authenticate to the server.  If running on an iSeries server, *CURRENT may be used to specify the current user ID.
     @param  proxyServer  The name and port of the proxy server in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     **/
    public AS400(String systemName, String userId, String password, String proxyServer)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object, system name: '" + systemName + "' user ID: '" + userId + "' proxy server: '" + proxyServer + "'");
        if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "password: '" + password + "'");
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (proxyServer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'proxyServer' is null.");
            throw new NullPointerException("proxyServer");
        }
        construct();
        systemName_ = resolveSystem(systemName);
        userId_ = userId.toUpperCase();
        bytes_ = store(password);
        proxyServer_ = resolveProxyServer(proxyServer);
    }

    /**
     Constructs an AS400 object.  It uses the same system name and user ID.  This does not create a clone.  The new object has the same behavior, but results in a new set of socket connections.
     @param  system  A previously instantiated AS400 object.
     **/
    public AS400(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400 object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        construct();
        systemName_ = system.systemName_;
        userId_ = system.userId_;
        bytes_ = system.bytes_;
        byteType_ = system.byteType_;

        proxyServer_ = system.proxyServer_;
        // proxyClientConnection_ is not copied.

        guiAvailable_ = system.guiAvailable_;
        usePasswordCache_ = system.usePasswordCache_;
        useDefaultUser_ = system.useDefaultUser_;
        showCheckboxes_ = system.showCheckboxes_;

        // useSSLConnection_ is handled by SecureAS400 subclass.
        mustUseSockets_ = system.mustUseSockets_;
        threadUsed_ = system.threadUsed_;
        locale_ = system.locale_;
        socketProperties_ = system.socketProperties_;

        ccsid_ = system.ccsid_;

        // connectionListeners_ is not copied.
        // dispatcher_ is not copied.
        // propertyChangeListeners_ is not copied.
        // vetoableChangeListeners_ is not copied.

        // propertiesFrozen_ is not copied.
        // impl_ is not copied.
        // signonInfo_ is not copied.
    }

    /**
     Adds a listener to be notified when a connection event occurs.
     @param  listener  The listener object.
     **/
    public void addConnectionListener(ConnectionListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding connection listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (connectionListeners_ == null)
            {
                connectionListeners_ = new Vector();
                dispatcher_ = new ConnectionListener()
                {
                    public void connected(ConnectionEvent event)
                    {
                        fireConnectEvent(event, true);
                    }
                    public void disconnected(ConnectionEvent event)
                    {
                        fireConnectEvent(event, false);
                    }
                };
            }
            // If this is the first add and we are already connected.
            if (impl_ != null && connectionListeners_.isEmpty())
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Enabling connection listener dispatcher.");
                impl_.addConnectionListener(dispatcher_);
            }
            connectionListeners_.addElement(listener);
        }
    }

    /**
     Validates the user ID and password on the server, and if successful, adds the information to the password cache.
     @param  systemName  The name of the server.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry, system name: '" + systemName + "' user ID: '" + userId + "'");
        addPasswordCacheEntry(new AS400(systemName, userId, password));
    }

    /**
     Validates the user ID and password on the server, and if successful, adds the information to the password cache.
     @param  systemName  The name of the server.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @param  proxyServer  The name and port of the proxy server in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password, String proxyServer) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry, system name: '" + systemName + "' user ID: '" + userId + "' proxy server: '" + proxyServer + "'");
        addPasswordCacheEntry(new AS400(systemName, userId, password, proxyServer));
    }

    // For use by AS400 and SecureAS400 objects.
    static void addPasswordCacheEntry(AS400 system) throws AS400SecurityException, IOException
    {
        system.validateSignon();  // Exception thrown if info not valid.
        setCacheEntry(system.systemName_, system.userId_, system.bytes_);
    }

    /**
     Adds a listener to be notified when the value of any property is changed.
     @param  listener  The listener object.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Adds a listener to be notified when the value of any constrained property is changed.  The vetoableChange method will be called.
     @param  listener  The listener object.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    /**
     Indicates if properties are frozen.  If this is true, property changes should not be made.  Properties are not the same thing as attributes.  Properties are basic pieces of information which must be set to make the object usable, such as the system name, user ID or other properties that identify the resource on the server.
     @return  true if properties are frozen, false otherwise.
     **/
    public boolean arePropertiesFrozen()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if properties are frozen:", propertiesFrozen_);
        return propertiesFrozen_;
    }

    /**
     Authenticates the user profile name and user profile password on the server.
     <p>This method is functionally equivalent to the <i>validateSignon()</i> method.  It does not alter the user profile assigned to this object, impact the status of existing connections, or otherwise impact the user and authorities under which the application is running.
     <p>The system name needs to be set prior to calling this method.
     <p><b>Note:</b> Providing an incorrect password increments the number of failed sign-on attempts for the user profile, and can result in the profile being disabled.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @return  true if successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public boolean authenticate(String userId, String password) throws AS400SecurityException, IOException
    {
        return validateSignon(userId, password);
    }

    // Indicates if the native optimizations code can be used.
    // return true if you are running on this system, the user has not told us specifically to use the servers, we are not using proxy, and the version of the native code matches the version we expect; false otherwise.
    boolean canUseNativeOptimizations()
    {
        try
        {
            if (AS400.onAS400 && !mustUseSockets_ && systemName_.equalsIgnoreCase("localhost") && proxyServer_.length() == 0 && byteType_ == AUTHENTICATION_SCHEME_PASSWORD && Class.forName("com.ibm.as400.access.NativeVersion").newInstance().hashCode() == 2)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Using native optimizations.");
                return true;
            }
        }
        catch (Exception e)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Not using native optimizations, unexpected exception while loading native version:", e);
        }
        return false;
    }

    /**
     Changes the user profile password.  The system name and user profile name need to be set prior to calling this method.
     @param  oldPassword  The old user profile password.
     @param  newPassword  The new user profile password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public void changePassword(String oldPassword, String newPassword) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Changing password.");
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "oldPassword: '" + oldPassword + "'");
            Trace.log(Trace.DIAGNOSTIC, "newPassword: '" + newPassword + "'");
        }
        if (oldPassword == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'oldPassword' is null.");
            throw new NullPointerException("oldPassword");
        }
        if (oldPassword.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'oldPassword' is not valid: " + oldPassword.length());
            throw new ExtendedIllegalArgumentException("oldPassword.length {" + oldPassword.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (newPassword == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'newPassword' is null.");
            throw new NullPointerException("newPassword");
        }
        if (newPassword.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'newPassword' is not valid: " + newPassword.length());
            throw new ExtendedIllegalArgumentException("newPassword.length {" + newPassword.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot change password before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        userId_ = resolveUserId(userId_);
        if (userId_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot change password before user ID is set.");
            throw new ExtendedIllegalStateException("userId", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        chooseImpl();

        // Synchronize to protect sign-on information.
        synchronized (this)
        {
            byte[] proxySeed = new byte[9];
            AS400.rng.nextBytes(proxySeed);
            byte[] remoteSeed = impl_.exchangeSeed(proxySeed);
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "AS400 object proxySeed:", proxySeed);
                Trace.log(Trace.DIAGNOSTIC, "AS400 object remoteSeed:", remoteSeed);
            }

            signonInfo_ = impl_.changePassword(systemName_, userId_, encode(proxySeed, remoteSeed, BinaryConverter.charArrayToByteArray(oldPassword.toCharArray())), encode(proxySeed, remoteSeed, BinaryConverter.charArrayToByteArray(newPassword.toCharArray())));
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Password changed successfully.");

            // Change instance variable to match.
            bytes_ = store(newPassword);
            byteType_ = AUTHENTICATION_SCHEME_PASSWORD;
        }
    }

    // Choose between remote and proxy implementation objects, set state information into remote implementation object.  Synchronized to protect impl_ and propertiesFrozen_ instance variables.  This method can safely be called multiple times because it checks its state before performing the code.
    private synchronized void chooseImpl()
    {
        if (impl_ == null)
        {
            impl_ = (AS400Impl)loadImpl2("com.ibm.as400.access.AS400ImplRemote", "com.ibm.as400.access.AS400ImplProxy");

            // If there is a connection listener.  Connect the remote implementation connection events to this object.
            if (connectionListeners_ != null && !connectionListeners_.isEmpty())
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Enabling connection listener dispatcher.");
                impl_.addConnectionListener(dispatcher_);
            }
        }
        if (!propertiesFrozen_)
        {
            impl_.setState(useSSLConnection_, canUseNativeOptimizations(), threadUsed_, ccsid_, locale_, socketProperties_, ddmRDB_);
            propertiesFrozen_ = true;
        }
    }

    /**
     Clears the password cache for all servers within this Java virtual machine.
     **/
    public static void clearPasswordCache()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Clearing password cache.");
        AS400.systemList.removeAllElements();
    }

    /**
     Clears all the passwords that are cached for the given system name within this Java virtual machine.
     @param  systemName  The name of the server.
     **/
    public static void clearPasswordCache(String systemName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Clearing password cache, system name: " + systemName);
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        systemName = resolveSystem(systemName);

        synchronized (AS400.systemList)
        {
            for (int i = AS400.systemList.size() - 1; i >= 0; i--)
            {
                if (systemName.equalsIgnoreCase((String)((Object[])AS400.systemList.elementAt(i))[0]))
                {
                    AS400.systemList.removeElementAt(i);
                }
            }
        }
    }

    /**
     Connects to a service on the iSeries server.  Security is validated and a connection is established to the server.
     <p>Services typically connect implicitly; therefore, this method does not have to be called to use a service.  This method can be used to control when the connection is established.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public void connectService(int service) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connecting service:", service);
        // Validate parameter.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        chooseImpl();
        signon(service == AS400.SIGNON);
        impl_.connect(service);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Service connected: " + AS400.getServerName(service));
    }

    // Common code for all the constuctors and readObject.
    private void construct()
    {
        // See if we are running on an iSeries server.
        if (AS400.onAS400)
        {
            // OK, we are running on an iSeries server.
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Running on iSeries server.");
            // Running on an iSeries server, don't prompt.
            guiAvailable_ = false;
        }
    }

    // Unscramble some bytes.
    private static byte[] decode(byte[] adder, byte[] mask, byte[] bytes)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object decode:");
            Trace.log(Trace.DIAGNOSTIC, "     adder:", adder);
            Trace.log(Trace.DIAGNOSTIC, "     mask:", mask);
            Trace.log(Trace.DIAGNOSTIC, "     bytes:", bytes);
        }
        int length = bytes.length;
        byte[] buf = new byte[length];
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(mask[i % 7] ^ bytes[i]);
        }
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(buf[i] - adder[i % 9]);
        }
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "     return:", buf);
        }
        return buf;
    }

    /**
     Disconnects from the iSeries server.  All socket connections associated with this object will be closed.
     **/
    public void disconnectAllServices()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Disconnecting all services...");
        if (impl_ != null)
        {
            chooseImpl();
            impl_.disconnect(AS400.FILE);
            impl_.disconnect(AS400.PRINT);
            impl_.disconnect(AS400.COMMAND);
            impl_.disconnect(AS400.DATAQUEUE);
            impl_.disconnect(AS400.DATABASE);
            impl_.disconnect(AS400.RECORDACCESS);
            impl_.disconnect(AS400.CENTRAL);
            impl_.disconnect(AS400.SIGNON);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "All services disconnected.");
    }

    /**
     Disconnects the service from the iSeries server.  All socket connections associated with this service and this object will be closed.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     **/
    public void disconnectService(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Disconnecting service:", service);
        // Validate parameter.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (impl_ == null) return;
        chooseImpl();
        impl_.disconnect(service);
    }

    // Scramble some bytes.
    private static byte[] encode(byte[] adder, byte[] mask, byte[] bytes)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object encode:");
            Trace.log(Trace.DIAGNOSTIC, "     adder:", adder);
            Trace.log(Trace.DIAGNOSTIC, "     mask:", mask);
            Trace.log(Trace.DIAGNOSTIC, "     bytes:", bytes);
        }
        if (bytes == null) return null;
        int length = bytes.length;
        byte[] buf = new byte[length];
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(bytes[i] + adder[i % 9]);
        }
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(buf[i] ^ mask[i % 7]);
        }
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "     return:", buf);
        }
        return buf;
    }

    // Fire connect events here so source is public object.
    private void fireConnectEvent(ConnectionEvent event, boolean connect)
    {
        // If we have made it this far, we know we have listeners.
        event.setSource(this);

        Vector targets = (Vector)connectionListeners_.clone();
        for (int i = 0; i < targets.size(); ++i)
        {
            ConnectionListener target = (ConnectionListener)targets.elementAt(i);
            if (connect)
            {
                target.connected(event);
            }
            else
            {
                target.disconnected(event);
            }
        }
    }

    /**
     Generates a profile token on behalf of the provided user identity.  This user identity must be associated with OS/400 user profile via EIM.
     <p>Invoking this method does not change the user ID and password assigned to the system or otherwise modify the user or authorities under which the application is running.  The profile associated with this system object must have enough authority to generate an authentication token for another user.
     <p>This function is only supported if the server is at release V5R3M0 or greater.
     @param  userIdentity  The LDAP distinguished name.
     @param  tokenType  The type of profile token to create.  Possible types are defined as fields on the ProfileTokenCredential class:
     <ul>
     <li>TYPE_SINGLE_USE
     <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     <li>TYPE_MULTIPLE_USE_RENEWABLE
     </ul>
     @param  timeoutInterval  The number of seconds to expiration when the token is created (1-3600).
     @return  A ProfileTokenCredential representing the provided user identity.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public ProfileTokenCredential generateProfileToken(String userIdentity, int tokenType, int timeoutInterval) throws AS400SecurityException, IOException
    {
        connectService(AS400.SIGNON);

        if (userIdentity == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userIdentity' is null.");
            throw new NullPointerException("userIdentity");
        }

        ProfileTokenCredential profileToken = new ProfileTokenCredential();
        try
        {
            profileToken.setSystem(this);
            profileToken.setTokenType(tokenType);
            profileToken.setTimeoutInterval(timeoutInterval);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        byte[] proxySeed = new byte[9];
        AS400.rng.nextBytes(proxySeed);
        chooseImpl();
        synchronized (this)
        {
            impl_.generateProfileToken(profileToken, userIdentity);
        }
        return profileToken;
    }

    /**
     Generates a VRM from a version, release, and modification.  This can then be used to compare against the VRM returned by getVRM().
     @param  version  The version.
     @param  release  The release.
     @param  modification  The modification level.
     @return  The generated VRM.
     **/
    public static int generateVRM(int version, int release, int modification)
    {
        // Check for valid input.
        if (version < 0 || version > 0xFFFF)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'version' is not valid:", version);
            throw new ExtendedIllegalArgumentException("version (" + version + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (release < 0 || release > 0xFF)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'release' is not valid:", release);
            throw new ExtendedIllegalArgumentException("release (" + release + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (modification < 0 || modification > 0xFF)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'modification' is not valid:", modification);
            throw new ExtendedIllegalArgumentException("modification (" + modification + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        return (version << 16) + (release << 8)  + modification;
    }

    /**
     Returns the authentication scheme for this object.  By default this object starts in password mode.  This value may not be correct before a connection to the server has been made.
     <br>Valid authentication schemes are:
     <br>   AUTHENTICATION_SCHEME_PASSWORD - passwords are used.
     <br>   AUTHENTICATION_SCHEME_GSS_TOKEN - GSS tokens are used.
     <br>   AUTHENTICATION_SCHEME_PROFILE_TOKEN - profile tokens are used.
     <br>   AUTHENTICATION_SCHEME_IDENTITY_TOKEN - identity tokens are used.
     @return  The authentication scheme in use for this object.
     **/
    public int getAuthenticationScheme()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting authentication scheme, scheme:", byteType_);
        return byteType_;
    }

    /**
     Returns the CCSID for this object.  The CCSID returned either is the one retrieved from the server based on the user profile or is set by the setCcsid() method.
     @return  The CCSID in use for this object.
     **/
    public int getCcsid()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting CCSID.");
        if (ccsid_ == 0)
        {
            try
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving CCSID from server...");
                chooseImpl();
                signon(false);
                ccsid_ = signonInfo_.serverCCSID;
            }
            catch (Exception e)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Taking best guess CCSID:", e);
                ccsid_ = ExecutionEnvironment.getBestGuessAS400Ccsid();
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CCSID:", ccsid_);
        return ccsid_;
    }

    // Calculate number of days until user's password expires.
    private int getDaysToExpiration()
    {
        if (signonInfo_ != null)
        {
            GregorianCalendar expirationDate = signonInfo_.expirationDate;
            GregorianCalendar now = signonInfo_.currentSignonDate;
            if (expirationDate != null && now != null)
            {
                long lExpiration = expirationDate.getTime().getTime();
                long lNow = now.getTime().getTime();

                // Divide by number of seconds in day, round up.
                int days = (int)(((lExpiration - lNow) / 0x5265C00) + 1);

                return days;
            }
        }
        // No expiration date.
        return 365;
    }

    /**
     * Returns the relational database name (RDB name) used for record-level access (DDM) connections.
     * The RDB name corresponds to the independent auxiliary storage pool (IASP) that it is using on the server.
     * @return The name of the IASP or RDB that is in use by this AS400 object's RECORDACCESS service, or null if
     * the IASP used will be the default system pool (*SYSBAS).
     * @see #setDDMRDB
    **/
    public String getDDMRDB()
    {
      return ddmRDB_;
    }

    /**
     Returns the default user ID for this system name.  This user ID is used to connect to the server if a user ID was not used to construct the object.
     @param  systemName  The name of the server.
     @return  The default user ID for this system.  A null is returned if there is not a default user.
     **/
    public static String getDefaultUser(String systemName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting the default user, system name: " + systemName);
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        String defaultUser = (String)AS400.defaultUsers.get(resolveSystem(systemName));
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Default user: " + defaultUser);
        return defaultUser;
    }

    /**
     Returns the GSS name string.  This method will only return the information provided on the setGSSName() method.
     @return  The GSS name string, or an empty string ("") if not set.
     **/
    public String getGSSName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting GSS name: " + gssName_);
        return gssName_;
    }

    /**
     Returns the option for how the JGSS framework will be used.
     @return  A constant indicating how the JGSS framework will be used.  Valid values are:
     <ul>
     <li>AS400.GSS_OPTION_MANDATORY
     <li>AS400.GSS_OPTION_FALLBACK
     <li>AS400.GSS_OPTION_NONE
     </ul>
     **/
    public int getGSSOption()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting GSS option:", gssOption_);
        return gssOption_;
    }

    // Get underlying AS400Impl object.
    AS400Impl getImpl()
    {
        chooseImpl();
        return impl_;
    }

    /**
     Returns the Locale used to set the National Language Version (NLV) on the server.  Only the COMMAND, PRINT, and DATABASE services accept an NLV.
     @return  The Locale object.
     **/
    public Locale getLocale()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting locale: " + locale_);
        return locale_;
    }

    // Returns the job CCSID.
    int getJobCcsid() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job CCSID.");
        chooseImpl();
        signon(false);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Job CCSID: " + signonInfo_.serverCCSID);
        return signonInfo_.serverCCSID;
    }

    /**
     Returns the encoding that corresponds to the job CCSID of the server.
     @return  The encoding.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public String getJobCCSIDEncoding() throws AS400SecurityException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job CCSID encoding.");
        chooseImpl();
        signon(false);
        int ccsid = signonInfo_.serverCCSID;
        String encoding = impl_.ccsidToEncoding(ccsid);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Job CCSID encoding: " + encoding);
        return encoding;
    }

    /**
     Returns an array of Job objects representing the iSeries jobs to which this object is connected.  This information is only available when connecting to V5R2 and later iSeries servers.  The array will be of length zero if no connections are currently active.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     @return  The array of job objects.
     **/
    public Job[] getJobs(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting jobs, service:", service);
        // Validate parameter.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (impl_ == null) return new Job[0];
        chooseImpl();
        String[] jobStrings = impl_.getJobs(service);
        Job[] jobs = new Job[jobStrings.length];
        for (int i = 0; i < jobStrings.length; ++i)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job for job: " + jobStrings[i]);
            if (jobStrings[i].length() == 0) return new Job[0];
            StringTokenizer tokenizer = new StringTokenizer(jobStrings[i], "/");
            String jobNumber = tokenizer.nextToken();
            String jobUser = tokenizer.nextToken();
            String jobName = tokenizer.nextToken();

            jobs[i] = new Job(this, jobName, jobUser, jobNumber);
        }
        return jobs;
    }

    /**
     Returns the modification level of the server.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The modification level of the server.  For example, version 5, release 1, modification level 0 returns 0.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public int getModification() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting modification level.");

        chooseImpl();
        signon(false);

        int modification = signonInfo_.version.getModificationLevel();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Modification level:", modification);

        return modification;
    }

    /**
     Returns the password expiration date for the signed-on user.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The password expiration date.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public GregorianCalendar getPasswordExpirationDate() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting password expiration date.");

        chooseImpl();
        signon(false);

        GregorianCalendar expire = signonInfo_.expirationDate;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Password expiration date: " + expire);

        return (expire == null) ? null : (GregorianCalendar)expire.clone();
    }

    /**
     Returns the number of days before password expiration to start warning the user.
     @return  The number of days before expiration to warn the user.
     **/
    public static int getPasswordExpirationWarningDays()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting the password expiration warning days:", AS400.expirationWarning);
        return AS400.expirationWarning;
    }

    /**
     Returns the date of the last successful sign-on.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The date of the last successful sign-on.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public GregorianCalendar getPreviousSignonDate() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting previous signon date.");

        chooseImpl();
        signon(false);

        GregorianCalendar last = signonInfo_.lastSignonDate;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Previous signon date: " + last);

        return (last == null) ? null : (GregorianCalendar)last.clone();
    }

    /**
     Returns a profile token representing the signed-on user profile.
     <p>The returned token will be created single-use with a one hour time to expiration. Subsequent method calls will return the same token, regardless of the token status.
     <p>This function is not supported if the assigned password is *CURRENT.
     <p>This function is only supported if the server is at release V4R5M0 or greater.
     @return  A ProfileTokenCredential representing the currently signed on user.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     @deprecated  Use getProfileToken(int, int) instead.
     **/
    public ProfileTokenCredential getProfileToken() throws AS400SecurityException, IOException, InterruptedException
    {
        connectService(AS400.SIGNON);

        if (signonInfo_.profileToken != null)
        {
            return (ProfileTokenCredential)signonInfo_.profileToken;
        }
        if (getVRM() < 0x00040500)
        {
            Trace.log(Trace.ERROR, "Requests for profile tokens require V4R5M0 or greater.");
            throw new AS400SecurityException(AS400SecurityException.SYSTEM_LEVEL_NOT_CORRECT);
        }
        // If the password is not set and we are not using Kerberos.
        try
        {
            // If the system name is set, we're not using proxy, and the password is not set, and the user has not told us not to.
            if (bytes_ == null && gssOption_ != AS400.GSS_OPTION_NONE)
            {
                // Try for Kerberos.
                bytes_ = TokenManager.getGSSToken(systemName_, gssName_);
                byteType_ = AUTHENTICATION_SCHEME_GSS_TOKEN;
            }
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
            if (gssOption_ == AS400.GSS_OPTION_MANDATORY)
            {
                throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE);
            }
        }
        if (bytes_ == null && byteType_ != AUTHENTICATION_SCHEME_GSS_TOKEN)
        {
            Trace.log(Trace.ERROR, "Password is null.");
            throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
        }

        ProfileTokenCredential profileToken = new ProfileTokenCredential();
        try
        {
            profileToken.setSystem(this);
            profileToken.setTokenType(ProfileTokenCredential.TYPE_SINGLE_USE);
            profileToken.setTimeoutInterval(3600);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        byte[] proxySeed = new byte[9];
        AS400.rng.nextBytes(proxySeed);
        synchronized (this)
        {
            impl_.generateProfileToken(profileToken, userId_, encode(proxySeed, impl_.exchangeSeed(proxySeed), resolve(bytes_)), byteType_);
        }
        signonInfo_.profileToken = profileToken;
        return profileToken;
    }

    /**
     Authenticates the assigned user profile and password and returns a corresponding ProfileTokenCredential if successful.
     <p>This function is not supported if the assigned password is *CURRENT and cannot be used to generate a renewable token.  This function is only supported if the server is at release V4R5M0 or greater.
     @param  tokenType  The type of profile token to create.  Possible types are defined as fields on the ProfileTokenCredential class:
     <ul>
     <li>TYPE_SINGLE_USE
     <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     </ul>
     @param  timeoutInterval  The number of seconds to expiration when the token is created (1-3600).
     @return  A ProfileTokenCredential representing the signed-on user.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public ProfileTokenCredential getProfileToken(int tokenType, int timeoutInterval) throws AS400SecurityException, IOException, InterruptedException
    {
        connectService(AS400.SIGNON);

        if (getVRM() < 0x00040500)
        {
            Trace.log(Trace.ERROR, "Requests for profile tokens require V4R5M0 or greater.");
            throw new AS400SecurityException(AS400SecurityException.SYSTEM_LEVEL_NOT_CORRECT);
        }
        // If the password is not set and we are not using Kerberos.
        try
        {
            // If the system name is set, we're not using proxy, and the password is not set, and the user has not told us not to.
            if (bytes_ == null && gssOption_ != AS400.GSS_OPTION_NONE)
            {
                // Try for Kerberos.
                bytes_ = TokenManager.getGSSToken(systemName_, gssName_);
                byteType_ = AUTHENTICATION_SCHEME_GSS_TOKEN;
            }
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
            if (gssOption_ == AS400.GSS_OPTION_MANDATORY)
            {
                throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE);
            }
        }
        if (bytes_ == null && byteType_ != AUTHENTICATION_SCHEME_GSS_TOKEN)
        {
            Trace.log(Trace.ERROR, "Password is null.");
            throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
        }
        if (tokenType == ProfileTokenCredential.TYPE_MULTIPLE_USE_RENEWABLE)
        {
            Trace.log(Trace.ERROR, "Request not supported for renewable token type.");
            throw new AS400SecurityException(AS400SecurityException.REQUEST_NOT_SUPPORTED);
        }

        ProfileTokenCredential profileToken = new ProfileTokenCredential();
        try
        {
            profileToken.setSystem(this);
            profileToken.setTokenType(tokenType);
            profileToken.setTimeoutInterval(timeoutInterval);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        byte[] proxySeed = new byte[9];
        AS400.rng.nextBytes(proxySeed);
        synchronized (this)
        {
            impl_.generateProfileToken(profileToken, userId_, encode(proxySeed, impl_.exchangeSeed(proxySeed), resolve(bytes_)), byteType_);
        }
        return profileToken;
    }

    /**
     Authenticates the given user profile and password and returns a corresponding ProfileTokenCredential if successful.
     <p>Invoking this method does not change the user ID and password assigned to the system or otherwise modify the user or authorities under which the application is running.
     <p>This method generates a single use token with a timeout of one hour.
     <p>This function is only supported if the server is at release V4R5M0 or greater.
     <p><b>Note:</b> Providing an incorrect password increments the number of failed sign-on attempts for the user profile, and can result in the profile being disabled.  Refer to documentation on the <i>ProfileTokenCredential</i> class for additional restrictions.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @return  A ProfileTokenCredential representing the authenticated profile and password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public ProfileTokenCredential getProfileToken(String userId, String password) throws AS400SecurityException, IOException, InterruptedException
    {
        return getProfileToken(userId, password, ProfileTokenCredential.TYPE_SINGLE_USE, 3600);
    }

    /**
     Authenticates the given user profile and password and returns a corresponding ProfileTokenCredential if successful.
     <p>Invoking this method does not change the user ID and password assigned to the system or otherwise modify the user or authorities under which the application is running.
     <p>This function is only supported if the server is at release V4R5M0 or greater.
     <p><b>Note:</b> Providing an incorrect password increments the number of failed sign-on attempts for the user profile, and can result in the profile being disabled.  Refer to documentation on the <i>ProfileTokenCredential</i> class for additional restrictions.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @param  tokenType  The type of profile token to create.  Possible types are defined as fields on the ProfileTokenCredential class:
     <ul>
     <li>TYPE_SINGLE_USE
     <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     <li>TYPE_MULTIPLE_USE_RENEWABLE
     </ul>
     @param  timeoutInterval  The number of seconds to expiration when the token is created (1-3600).
     @return  A ProfileTokenCredential representing the authenticated profile and password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public ProfileTokenCredential getProfileToken(String userId, String password, int tokenType, int timeoutInterval) throws AS400SecurityException, IOException, InterruptedException
    {
        connectService(AS400.SIGNON);

        // Validate parms.
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of 'userId' parameter is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of 'password' parameter is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (getVRM() < 0x00040500)
        {
            Trace.log(Trace.ERROR, "Requests for profile tokens require V4R5M0 or greater.");
            throw new AS400SecurityException(AS400SecurityException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        userId = resolveUserId(userId.toUpperCase());

        ProfileTokenCredential profileToken = new ProfileTokenCredential();
        try
        {
            profileToken.setSystem(this);
            profileToken.setTokenType(tokenType);
            profileToken.setTimeoutInterval(timeoutInterval);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        byte[] proxySeed = new byte[9];
        AS400.rng.nextBytes(proxySeed);
        synchronized (this)
        {
            impl_.generateProfileToken(profileToken, userId, encode(proxySeed, impl_.exchangeSeed(proxySeed), BinaryConverter.charArrayToByteArray(password.toCharArray())), 0);
        }
        return profileToken;
    }

    /**
     Returns the name of the middle-tier machine where the proxy server is running.
     @return  The name of the middle-tier machine where the proxy server is running, or an empty string ("") if not set.
     **/
    public String getProxyServer()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting proxy server: " + proxyServer_);
        return proxyServer_;
    }

    /**
     Returns the release of the server.
     <p>A connection is required to the server in order to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The release of the server.  For example, version 5, release 1, modification level 0, returns 1.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public int getRelease() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting release level.");
        chooseImpl();
        signon(false);

        int release = signonInfo_.version.getRelease();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Release level:", release);

        return release;
    }

    // Converts a service constant to a server name.
    static String getServerName(int service)
    {
        switch (service)
        {
            case AS400.FILE:
                return "as-file";
            case AS400.PRINT:
                return "as-netprt";
            case AS400.COMMAND:
                return "as-rmtcmd";
            case AS400.DATAQUEUE:
                return"as-dtaq";
            case AS400.DATABASE:
                return "as-database";
            case AS400.RECORDACCESS:
                return "as-ddm";
            case AS400.CENTRAL:
                return "as-central";
            case AS400.SIGNON:
                return "as-signon";
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
                throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

    /**
     Returns the service port stored in the service port table for the specified service.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     @return  The port specified in the service port table.  The value USE_PORT_MAPPER will be returned if the service has not been set, and the service has not been connected.
     **/
    public int getServicePort(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting service port, service:", service);
        // Validate parameter.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Validate state.
        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot get service port before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        chooseImpl();
        int port = impl_.getServicePort(systemName_, service);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Service port:", port);
        return port;
    }

    /**
     Returns the date for the current sign-on.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The date for the current sign-on.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public GregorianCalendar getSignonDate() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting signon date.");

        chooseImpl();
        signon(false);

        GregorianCalendar current = signonInfo_.currentSignonDate;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signon date: " + current);

        return (current == null) ? null : (GregorianCalendar)current.clone();
    }

    /**
     Returns a copy of the socket options object.
     @return  The socket options object.
     **/
    public SocketProperties getSocketProperties()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting socket properties.");
        SocketProperties socketProperties = new SocketProperties();
        socketProperties.copyValues(socketProperties_);
        return socketProperties;
    }

    /**
     Returns the name of the system.  The system name is provided on the constructor or may have been provided by the user at the sign-on prompt.
     @return  The name of the system, or an empty string ("") if not set.
     **/
    public String getSystemName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system name: " + systemName_);
        return systemName_;
    }

    /**
     Returns the user ID.  The user ID returned may be set as a result of the constructor, or it may be what the user typed in at the sign-on prompt.
     @return  The user ID, or an empty string ("") if not set.
     **/
    public String getUserId()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user ID: " + userId_);
        userId_ = resolveUserId(userId_, byteType_);
        return userId_;
    }

    /**
     Returns the version of the server.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The version of the server.  For example, version 5, release 1, modification level 0, returns 5.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public int getVersion() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting version level.");
        chooseImpl();
        signon(false);

        int version = signonInfo_.version.getVersion();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Version level:", version);

        return version;
    }

    /**
     Returns the version, release, and modification level for the server.
     <p>A connection is required to the server to retrieve this information.  If a connection has not been established, one is created to retrieve the server information.
     @return  The high 16-bit is the version, the next 8 bits is the release, and the low 8 bits is the modification level.  Thus version 5, release 1, modification level 0, returns 0x00050100.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public int getVRM() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting VRM.");
        chooseImpl();
        signon(false);

        int vrm = signonInfo_.version.getVersionReleaseModification();
        if (Trace.traceOn_)
        {
            byte[] vrmBytes = new byte[4];
            BinaryConverter.intToByteArray(vrm, vrmBytes, 0);
            Trace.log(Trace.DIAGNOSTIC, "VRM:",  vrmBytes);
        }

        return vrm;
    }

    /**
     Initialize conversion table for the given CCSID.  The default EBCDIC to unicode converters are not shipped with some browsers.  This method can be used to check and download converters if they are not available locally.
     @param  ccsid  the CCSID for the conversion table to initialize.
     **/
    public void initializeConverter(int ccsid) throws UnsupportedEncodingException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Initializing converter for CCSID:", ccsid);
        chooseImpl();
        try
        {
            signon(false);
            impl_.newConverter(ccsid);
        }
        catch (Exception e)
        {
            Trace.log(Trace.WARNING, "Error initializing converter:", e);
            throw new UnsupportedEncodingException();
        }
    }

    /**
     Indicates if any service is currently connected through this object.
     <p>A service is connected if connectService() has been called, or an implicit connect has been done by the service, and disconnectService() or disconnectAllServices() has not been called.
     @return  true if any service is connected; false otherwise.
     **/
    public boolean isConnected()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking for any service connection...");
        if (isConnected(AS400.FILE) || isConnected(AS400.PRINT) || isConnected(AS400.COMMAND) || isConnected(AS400.DATAQUEUE) || isConnected(AS400.DATABASE) || isConnected(AS400.RECORDACCESS) || isConnected(AS400.CENTRAL)|| isConnected(AS400.SIGNON))
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "A service is connected.");
            return true;
        }
        else
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "No service is connected.");
            return false;
        }
    }

    /**
     Indicates if a service is currently connected through this object.
     <p>A service is connected if connectService() has been called, or an implicit connect has been done by the service, and disconnectService() or disconnectAllServices() has not been called.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     @return  true if service is connected; false otherwise.
     **/
    public boolean isConnected(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking for service connection:", service);
        // Validate parameter.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (impl_ == null) return false;
        chooseImpl();
        boolean connected = impl_.isConnected(service);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Service connection:", connected);
        return connected;
    }

    /**
     Returns the sign-on prompting mode for this object.  If true, then messages are displayed.  If warnings or errors occur, the sign-on and change password dialogs are displayed if needed.  If false, warnings and errors result in exceptions, and password dialogs are not displayed.  The caller has to provide the user ID and password.
     @return  true if using GUI; false otherwise.
     **/
    public boolean isGuiAvailable()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if GUI is available:", guiAvailable_);
        return guiAvailable_;
    }

    /**
     Indicates if this object is representing the system you are currently running on.
     @return  true if you are running on the local system; false otherwise.
     **/
    public boolean isLocal()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if Local:");
        boolean isLocal = AS400.onAS400 && systemName_.equalsIgnoreCase("localhost");
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Local:", isLocal);
        return isLocal;
    }

    /**
     When your Java program runs on an iSeries server, some Toolbox classes access data via a call to an API instead of making a socket call to a server.  There are minor differences in the behavior of the classes when they use API calls instead of socket calls.  If your program is affected by these differences you can check whether the Toolbox classes will use socket calls instead of API calls by using this method.
     @return  true if you have indicated that the services must use sockets; false otherwise.
     **/
    public boolean isMustUseSockets()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if must use sockets:", mustUseSockets_);
        return mustUseSockets_;
    }

    /**
     Indicates if checkboxes should be shown on the sign-on dialog.
     @return  true if checkboxes should be shown; false otherwise.
     **/
    public boolean isShowCheckboxes()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if checkboxes are shown:", showCheckboxes_);
        return showCheckboxes_;
    }

    // Check if systemName refers to the system we are running on.
    private static boolean isSystemNameLocal(String systemName)
    {
        if (systemName.equalsIgnoreCase("localhost"))
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "System name is localhost.");
            return true;
        }
        else
        {
            try
            {
                InetAddress localInet = InetAddress.getLocalHost();
                InetAddress[] remoteInet = InetAddress.getAllByName(systemName);

                for (int i = 0; i < remoteInet.length; ++i)
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Comparing local address " + localInet + " to " + remoteInet[i]);
                    if (localInet.equals(remoteInet[i]))
                    {
                        return true;
                    }
                }
            }
            catch (UnknownHostException e)
            {
                Trace.log(Trace.ERROR, "Error retrieving host address information:", e);
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "System name is not local.");
        return false;
    }

    /**
     Indicates whether threads are used in communication with the host servers.
     @return  true if threads are used; false otherwise.
     **/
    public boolean isThreadUsed()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if thread is used:", threadUsed_);
        return threadUsed_;
    }

    /**
     Indicates if the default user should be used by this object.  If the default user is not used and a user ID was not specified on the constructor, then the user will be prompted for a user ID.
     @return  true if default user should be used; false otherwise.
     **/
    public boolean isUseDefaultUser()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if default user is used:", useDefaultUser_);
        return useDefaultUser_;
    }

    /**
     Indicates if the password cache is being used by this object.  If the password cache is not used, the user will always be prompted for password if one was not provided.
     @return  true if password cache is being used; false otherwise.
     **/
    public boolean isUsePasswordCache()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if password cache is used:", usePasswordCache_);
        return usePasswordCache_;
    }

    // Load the specified implementation object.  Exceptions are swallowed, null is returned if the object cannot be loaded.
    static Object loadImpl(String impl)
    {
        if (impl.indexOf ('.') == -1)
        {
            impl = "com.ibm.as400.access." + impl;
        }

        try
        {
            return Class.forName(impl).newInstance();
        }
        catch (ClassNotFoundException e1)
        {
            Trace.log(Trace.ERROR, "Unexpected ClassNotFoundException:", e1);
        }
        catch (IllegalAccessException e2)
        {
            Trace.log(Trace.ERROR, "Unexpected IllegalAccessException:", e2);
        }
        catch (InstantiationException e3)
        {
            Trace.log(Trace.ERROR, "Unexpected InstantiationException:", e3);
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Load of implementation failed: " + impl);

        return null;
    }

    // Load the appropriate implementation object.
    // param  impl1  fully package named class name for native implementation.
    // param  impl2  fully package named class name for remote implementation.
    Object loadImpl(String impl1, String impl2)
    {
        if (canUseNativeOptimizations())
        {
            Object impl = loadImpl(impl1);
            if (impl != null) return impl;
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Load of native implementation '" + impl1 + "' failed, attempting to load remote implementation.");
        }
        Object impl = loadImpl(impl2);
        if (impl != null) return impl;

        Trace.log(Trace.DIAGNOSTIC, "Load of remote implementation '" + impl2 + "' failed.");
        throw new ExtendedIllegalStateException(impl2, ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
    }

    // Load the appropriate implementation object when only remote or proxy implementations are involved.
    // param  impl1  fully package named class name for remote implementation.
    // param  impl2  fully package named class name for proxy implementation.
    Object loadImpl2(String impl1, String impl2)
    {
        if (proxyServer_.length() > 0)
        {
            synchronized (this)
            {
                if (proxyClientConnection_ == null)
                {
                    proxyClientConnection_ = new ProxyClientConnection(proxyServer_, useSSLConnection_);
                }
            }
            ProxyImpl proxyImpl = (ProxyImpl)loadImpl(impl2);
            if (proxyImpl != null)
            {
                proxyImpl.construct(proxyClientConnection_);
                return proxyImpl;
            }
        }

        Object impl = loadImpl(impl1);
        if (impl != null) return impl;

        Trace.log(Trace.DIAGNOSTIC, "Load of remote implementation '" + impl1 + "' failed.");
        throw new ExtendedIllegalStateException(impl1, ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
    }

    // Load the appropriate implementation object when a remote, proxy, or native implementations are involved.
    // param  impl1  fully package named class name for native implementation.
    // param  impl2  fully package named class name for remote implementation.
    // param  impl3  fully package named class name for proxy implementation.
    Object loadImpl3(String impl1, String impl2, String impl3)
    {
        if (proxyServer_.length() > 0)
        {
            synchronized (this)
            {
                if (proxyClientConnection_ == null)
                {
                    proxyClientConnection_ = new ProxyClientConnection(proxyServer_, useSSLConnection_);
                }
            }
            ProxyImpl proxyImpl = (ProxyImpl)loadImpl(impl3);
            if (proxyImpl != null)
            {
                proxyImpl.construct(proxyClientConnection_);
                return proxyImpl;
            }
        }

        if (canUseNativeOptimizations())
        {
            Object impl = loadImpl(impl1);
            if (impl != null) return impl;
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Load of native implementation '" + impl1 + "' failed, attempting to load remote implementation.");
        }

        Object impl = loadImpl(impl2);
        if (impl != null) return impl;

        Trace.log(Trace.DIAGNOSTIC, "Load of remote implementation '" + impl2 + "' failed.");
        throw new ExtendedIllegalStateException(impl2, ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
    }

    // State machine constants for running through sign-on prompts.
    private static final int FINISHED = 0;
    private static final int VALIDATE = 1;
    private static final int PROMPT = 2;
    private static final int CHANGE = 3;
    // Run through the various prompts for signon.
    private void promptSignon() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signing-on with prompting turned on.");

        // Start in validate state.
        int pwState = VALIDATE;
        // If something isn't set, go to prompt state.
        if (systemName_.length() == 0 || userId_.length() == 0 || bytes_ == null) pwState = PROMPT;

        MessageDialog md = null;
        PasswordDialog pd = null;
        boolean signonAttempt = false;

        do
        {
            try
            {
                switch (pwState)
                {
                    case VALIDATE:
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Validate security...");
                        signonAttempt = true;
                        sendSignonRequest();
                        break;

                    case PROMPT:
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prompt for signon information...");
                        String systemName = "";
                        String userId = "";
                        String password = "";
                        boolean invalidInformation = true;
                        do
                        {
                            pd = setupPasswordDialog();
                            signonAttempt = pd.prompt();
                            if (signonAttempt)
                            {
                                // User did not cancel.
                                systemName = resolveSystem(pd.getSystemName().trim());
                                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "System name: '" + systemName + "'");
                                userId = pd.getUserId().trim().toUpperCase();
                                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "User ID: '" + userId + "'");
                                password = pd.getPassword();
                                if (PASSWORD_TRACE)
                                {
                                    Trace.log(Trace.DIAGNOSTIC, "Password: '" + password + "'");
                                }

                                if (systemName.equals("") || userId.equals("") || password.equals(""))
                                {
                                    // A field is not filled in.
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("DLG_MISSING_USERID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else if (userId.length() > 10)
                                {
                                    Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid: '" + userId + "'");
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_USERID_LENGTH_NOT_VALID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else if (password.length() > 128)
                                {
                                    Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid: " + password.length());
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_PASSWORD_LENGTH_NOT_VALID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else
                                {
                                    // Dialog information is all valid.
                                    invalidInformation = false;
                                }
                            }
                        }
                        while (signonAttempt && invalidInformation);

                        if (signonAttempt)
                        {
                            systemName_ = systemName;
                            userId_ = resolveUserId(userId);
                            bytes_ = store(password);
                            sendSignonRequest();

                            // Check to see if we should set the default user.
                            if (pd.getDefaultState())
                            {
                                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting default user from dialog...");
                                // Set the default user.
                                if (!setDefaultUser(systemName_, userId_))
                                {
                                    if (Trace.traceOn_) Trace.log(Trace.WARNING, "Failed to set default user.");
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("DLG_DEFAULT_USER_EXISTS") + "\n\n" + ResourceBundleLoader.getText("DLG_SET_DEFAULT_USER_FAILED"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                            }
                            // Also see if we should cache the password.
                            if (pd.getPasswordCacheState())
                            {
                                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting password cache entry from dialog...");
                                setCacheEntry(systemName_, userId_, bytes_);
                            }
                        }
                        break;

                    case CHANGE:
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prompt for change password information...");
                        String oldPassword = "";
                        String newPassword = "";
                        String confirmPassword = "";

                        boolean changeInformationInvalid = true;
                        do
                        {
                            ChangePasswordDialog cpd = new ChangePasswordDialog(new Frame(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
                            signonAttempt = cpd.prompt(systemName_, userId_);
                            if (signonAttempt)
                            {
                                // User did not cancel.
                                oldPassword = cpd.getOldPassword();
                                newPassword = cpd.getNewPassword();
                                confirmPassword = cpd.getConfirmPassword();
                                if (PASSWORD_TRACE)
                                {
                                    Trace.log(Trace.DIAGNOSTIC, "Old password: '" + oldPassword + "'");
                                    Trace.log(Trace.DIAGNOSTIC, "New password: '" + newPassword + "'");
                                    Trace.log(Trace.DIAGNOSTIC, "Confirm password: '" + confirmPassword + "'");
                                }

                                if (oldPassword.equals("") || newPassword.equals("") || confirmPassword.equals(""))
                                {
                                    // A field is not filled in.
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("DLG_MISSING_PASSWORD"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else if (!newPassword.equals(confirmPassword))
                                {
                                    // New and confirm are not the same.
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_PASSWORD_NOT_MATCH"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else if (oldPassword.length() > 128)
                                {
                                    Trace.log(Trace.ERROR, "Length of parameter 'oldPassword' is not valid: " + oldPassword.length());
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_PASSWORD_LENGTH_NOT_VALID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else if (newPassword.length() > 128)
                                {
                                    Trace.log(Trace.ERROR, "Length of parameter 'newPassword' is not valid: " + newPassword.length());
                                    md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_PASSWORD_NEW_NOT_VALID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                                    md.display();
                                }
                                else
                                {
                                    // Dialog information is all valid.
                                    changeInformationInvalid = false;
                                }
                            }
                        }
                        while (signonAttempt && changeInformationInvalid);

                        if (signonAttempt)
                        {
                            byte[] proxySeed = new byte[9];
                            AS400.rng.nextBytes(proxySeed);
                            byte[] remoteSeed = impl_.exchangeSeed(proxySeed);
                            if (PASSWORD_TRACE)
                            {
                                Trace.log(Trace.DIAGNOSTIC, "AS400 object proxySeed:", proxySeed);
                                Trace.log(Trace.DIAGNOSTIC, "AS400 object remoteSeed:", remoteSeed);
                            }

                            signonInfo_ = impl_.changePassword(systemName_, userId_, encode(proxySeed, remoteSeed, BinaryConverter.charArrayToByteArray(oldPassword.toCharArray())), encode(proxySeed, remoteSeed, BinaryConverter.charArrayToByteArray(newPassword.toCharArray())));

                            bytes_ = store(newPassword);  // Change instance variable to match.
                        }
                        break;

                    default:
                        // Invalid state should never happen.
                        Trace.log(Trace.ERROR, "Invalid password prompt state:", pwState);
                        throw new InternalErrorException(InternalErrorException.SECURITY_INVALID_STATE, pwState);
                }
                if (signonAttempt)
                {
                    // Check for number of days to expiration, and warn if within threshold.
                    int daysToExpiration = getDaysToExpiration();
                    if (daysToExpiration < AS400.expirationWarning)
                    {
                        // Put up warning.
                        md = new MessageDialog(new Frame(), ResourceBundleLoader.substitute(ResourceBundleLoader.getText("DLG_PASSWORD_EXP_WARNING"), new Integer(daysToExpiration).toString()) + "  " + ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_PROMPT"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), true);
                        if (md.display())
                        {
                            pwState = CHANGE;
                        }
                        else
                        {
                            pwState = FINISHED;
                        }
                    }
                    else
                    {
                        pwState = FINISHED;
                    }
                }
                else
                {
                    // User canceled.
                    Trace.log(Trace.DIAGNOSTIC, "User canceled.");
                    throw new AS400SecurityException(AS400SecurityException.SIGNON_CANCELED);
                }
            }
            catch (AS400SecurityException e)
            {
                Trace.log(Trace.ERROR, "Security exception in sign-on:", e);
                switch (e.getReturnCode())
                {
                    case AS400SecurityException.USERID_LENGTH_NOT_VALID:
                    case AS400SecurityException.PASSWORD_LENGTH_NOT_VALID:
                    case AS400SecurityException.USERID_DISABLE:
                    case AS400SecurityException.PASSWORD_INCORRECT:
                    case AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE:
                    case AS400SecurityException.SIGNON_REQUEST_NOT_VALID:
                    case AS400SecurityException.USERID_UNKNOWN:
                    case AS400SecurityException.SIGNON_CHAR_NOT_VALID:
                        md = new MessageDialog(new Frame(), e.getMessage(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                        md.display();
                        pwState = PROMPT;
                        break;

                    case AS400SecurityException.PASSWORD_CHANGE_REQUEST_NOT_VALID:
                    case AS400SecurityException.PASSWORD_OLD_NOT_VALID:
                    case AS400SecurityException.PASSWORD_NEW_NOT_VALID:
                    case AS400SecurityException.PASSWORD_NEW_TOO_LONG:
                    case AS400SecurityException.PASSWORD_NEW_TOO_SHORT:
                    case AS400SecurityException.PASSWORD_NEW_REPEAT_CHARACTER:
                    case AS400SecurityException.PASSWORD_NEW_ADJACENT_DIGITS:
                    case AS400SecurityException.PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER:
                    case AS400SecurityException.PASSWORD_NEW_PREVIOUSLY_USED:
                    case AS400SecurityException.PASSWORD_NEW_NO_NUMERIC:
                    case AS400SecurityException.PASSWORD_NEW_NO_ALPHABETIC:
                    case AS400SecurityException.PASSWORD_NEW_DISALLOWED:
                    case AS400SecurityException.PASSWORD_NEW_USERID:
                    case AS400SecurityException.PASSWORD_NEW_SAME_POSITION:
                    case AS400SecurityException.PASSWORD_NEW_CHARACTER_NOT_VALID:
                    case AS400SecurityException.PASSWORD_NEW_VALIDATION_PROGRAM:
                        md = new MessageDialog(new Frame(), e.getMessage(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"), false);
                        md.display();
                        pwState = CHANGE;
                        break;
                    case AS400SecurityException.PASSWORD_EXPIRED:
                        md = new MessageDialog(new Frame(), ResourceBundleLoader.getText("EXC_PASSWORD_EXPIRED") + "\n" + ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_PROMPT"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), true);
                        if (md.display())
                        {
                            pwState = CHANGE;
                        }
                        else
                        {
                            // Don't want to change password, then fail it.
                            pwState = FINISHED;
                            throw e;
                        }
                        break;
                    case AS400SecurityException.SECURITY_GENERAL:
                    case AS400SecurityException.EXIT_POINT_PROCESSING_ERROR:
                    case AS400SecurityException.EXIT_PROGRAM_RESOLVE_ERROR:
                    case AS400SecurityException.EXIT_PROGRAM_CALL_ERROR:
                    case AS400SecurityException.EXIT_PROGRAM_DENIED_REQUEST:
                        md = new MessageDialog(new Frame(), e.getMessage(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), false);
                        md.display();
                        pwState = FINISHED;
                        throw e;
                    default:
                        throw e;
                }
            }
        }
        while (pwState != FINISHED);
    }

    // Help de-serialize the object.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "De-serializing AS400 object.");
        in.defaultReadObject();

        construct();

        systemName_ = resolveSystem(systemName_);

        proxyServer_ = resolveProxyServer("");
        // proxyClientConnection_ can stay null.
        ccsid_ = 0;
        // connectionListeners_ can stay null.
        // dispatcher_ can stay null.
        // propertyChangeListeners_ can stay null.
        // vetoableChangeListeners_ can stay null.

        propertiesFrozen_ = false;
        // impl_ can stay null.
        // signonInfo_ can stay null.
    }

    /**
     Removes a listener from the connection event list.
     @param  listener  The listener object.
     **/
    public void removeConnectionListener(ConnectionListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing connection listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If we have listeners.
            if (connectionListeners_ != null)
            {
                connectionListeners_.removeElement(listener);
                // If we have a connection, and we're now out of listeners.
                if (impl_ != null && connectionListeners_.isEmpty())
                {
                    // Remove the dispatcher.
                    impl_.removeConnectionListener(dispatcher_);
                }
            }
        }
    }

    /**
     Removes the default user for the given system name.
     @param  systemName  The name of the server.
     **/
    public static void removeDefaultUser(String systemName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing the default user, system name: " + systemName);
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        AS400.defaultUsers.remove(resolveSystem(systemName));
    }

    /**
     Removes the password cache entry associated with this system name and user ID.  Only applies within this Java virtual machine.
     @param  systemName  The name of the server.
     @param  userId  The user profile name.
     **/
    public static void removePasswordCacheEntry(String systemName, String userId)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing password cache entry, system name: " + systemName + " user ID: " + userId);
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of 'userId' parameter is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        systemName = resolveSystem(systemName);
        userId = resolveUserId(userId.toUpperCase());

        synchronized (AS400.systemList)
        {
            for (int i = AS400.systemList.size() - 1; i >= 0; i--)
            {
                Object[] secobj = (Object[])AS400.systemList.elementAt(i);
                if (systemName.equalsIgnoreCase((String)secobj[0]) && userId.equals(secobj[1]))
                {
                    AS400.systemList.removeElementAt(i);
                }
            }
        }
    }

    /**
     Removes a property changed listener from the listener list.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes a listener from the veto list.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If we have listeners.
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.removeVetoableChangeListener(listener);
            }
        }
    }

    /**
     Disconnect all services and clear sign-on information.
     **/
    public synchronized void resetAllServices()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Resetting all services.");
        disconnectAllServices();
        signonInfo_ = null;
        propertiesFrozen_ = false;
        ccsid_ = 0;
    }

    // Get clear password bytes back.
    private static byte[] resolve(byte[] info)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object resolve:", info);
        }

        if (info == null)
        {
            return null;
        }

        byte[] adder = new byte[9];
        System.arraycopy(info, 0, adder, 0, 9);
        byte[] mask = new byte[7];
        System.arraycopy(info, 9, mask, 0, 7);
        byte[] infoBytes = new byte[info.length - 16];
        System.arraycopy(info, 16, infoBytes, 0, info.length - 16);

        return decode(adder, mask, infoBytes);
    }

    // Resolves the proxy server name.  If it is not specified, then look it up in the system properties.  Returns empty string if not set.
    private static String resolveProxyServer(String proxyServer)
    {
        if (proxyServer.length() == 0)
        {
            proxyServer = SystemProperties.getProperty(SystemProperties.AS400_PROXY_SERVER);
            if (proxyServer == null) return "";
        }
        return proxyServer;
    }

    // If connecting to local system, make systemName "localhost".
    static String resolveSystem(String systemName)
    {
        // First, see if we are running on an iSeries server.
        if (AS400.onAS400)
        {
            // If system name is null, then make it a localhost.
            if (systemName.length() == 0)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Resolving initial system name to localhost.");
                return "localhost";
            }
            else if (isSystemNameLocal(systemName))
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Resolving system name to localhost.");
                return "localhost";
            }
        }
        return systemName;
    }

    // If on an iSeries server, resolve user ID to current user ID.
    static String resolveUserId(String userId)
    {
        // Resolve user ID, for someone using user ID/password.
        return resolveUserId(userId, 0);
    }

    // If on an iSeries server, resolve user ID to current user ID.
    static String resolveUserId(String userId, int byteType)
    {
        // First, see if we are running on an iSeries server.
        if (AS400.onAS400)
        {
            boolean tryToGetCurrentUserID = false;
            // If user ID is not set and we're using user ID/password, then we get it and set it up.
            if (userId.length() == 0 && byteType == 0)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Resolving initial user ID.");
                tryToGetCurrentUserID = true;
            }
            // If we are running on an iSeries server, then *CURRENT for user ID means we want to connect using current user ID.
            if (userId.equals("*CURRENT"))
            {
                // Get current user ID and use it.
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Replacing *CURRENT as user ID.");
                tryToGetCurrentUserID = true;
            }
            if (tryToGetCurrentUserID)
            {
                String currentUserID = CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification());
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Current user ID: "  + currentUserID);
                if (currentUserID != null) return currentUserID;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Current user ID information not available, user ID: '"  + userId + "'");
            }
        }

        // Prepend Q to numeric user ID.
        if (userId.length() > 0 && Character.isDigit(userId.charAt(0)))
        {
            char[] userIdChars = userId.toCharArray();
            for (int i = 0; i < userIdChars.length; ++i)
            {
                if (userIdChars[i] < '\u0030' || userIdChars[i] > '\u0039')
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "User ID: '"  + userId + "'");
                    return userId;
                }
            }
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric user ID.");
            userId = "Q" + userId;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "User ID: '"  + userId + "'");
        return userId;
    }

    // Send sign-on request to sign-on server.
    private void sendSignonRequest() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signing-on without prompting...");
        // No prompting.
        if (bytes_ == null && !userIdMatchesLocal(userId_))
        {
            Trace.log(Trace.ERROR, "Password is null.");
            throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
        }

        byte[] proxySeed = new byte[9];
        AS400.rng.nextBytes(proxySeed);
        byte[] remoteSeed = impl_.exchangeSeed(proxySeed);
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object proxySeed:", proxySeed);
            Trace.log(Trace.DIAGNOSTIC, "AS400 object remoteSeed:", remoteSeed);
        }

        // If using GSS tokens.
        if (byteType_ == AUTHENTICATION_SCHEME_GSS_TOKEN)
        {
            signonInfo_ = impl_.signon(systemName_, userId_, bytes_, byteType_, gssName_, gssOption_);
            bytes_ = null;  // GSSToken is single use only.
        }
        else
        {
            signonInfo_ = impl_.signon(systemName_, userId_, encode(proxySeed, remoteSeed, resolve(bytes_)), byteType_, gssName_, gssOption_);
        }
        if (userId_.length() == 0) userId_ = signonInfo_.userId;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sign-on completed.");
    }

    /**
     Sets or resets the identity token for this object.  Using this method will clear any set password.
     <p><i>Note: Authentication via IdentityToken is not currently supported.  Support will become available in a future PTF for OS/400 V5R2 and V5R1.</i>
     @param  identityToken  The identity token.
     **/
    public void setIdentityToken(byte[] identityToken)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting identity token.");

        if (identityToken == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'identityToken' is null.");
            throw new NullPointerException("identityToken");
        }

        synchronized (this)
        {
            bytes_ = store(identityToken);
            byteType_ = AUTHENTICATION_SCHEME_IDENTITY_TOKEN;
            signonInfo_ = null;
        }
    }

    // Store information in password cache.
    private static void setCacheEntry(String systemName, String userId, byte[] bytes)
    {
        synchronized (AS400.systemList)
        {
            // Remove any duplicates in the list and add to the list.
            for (int i = AS400.systemList.size() - 1; i >= 0; i--)
            {
                Object[] curPtr = (Object[])AS400.systemList.elementAt(i);

                if (systemName.equalsIgnoreCase((String)curPtr[0]) && userId.equals(curPtr[1]))
                {
                    // Found duplicate, remove it.
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry, removing previous entry.");
                    AS400.systemList.removeElementAt(i);
                }
            }

            // Now add the new one.
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry.");
            AS400.systemList.addElement(new Object[] {systemName, userId, bytes} );
        }
    }

    /**
     Sets the CCSID to be used for this object.  The CCSID property cannot be changed once a connection to the server has been established.
     @param  ccsid  The CCSID to use for this object.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setCcsid(int ccsid) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting CCSID:", ccsid);
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set CCSID after connection has been made.");
            throw new ExtendedIllegalStateException("ccsid", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            ccsid_ = ccsid;
        }
        else
        {
            Integer oldValue = new Integer(ccsid_);
            Integer newValue = new Integer(ccsid);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("ccsid", oldValue, newValue);
            }
            ccsid_ = ccsid;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("ccsid", oldValue, newValue);
            }
        }
    }

    /**
     * Sets the relational database name (RDB name) used for record-level access (DDM) connections.
     * The RDB name corresponds to the independent auxiliary storage pool (IASP) that it is using on the server.
     * The RDB name cannot be changed when this AS400 object is currently
     * connected to the {@link #RECORDACCESS RECORDACCESS} service; you must call {@link #disconnectService(int) AS400.disconnectService(AS400.RECORDACCESS)} first.
     * @param iaspName The name of the IASP or RDB to use, or null to indicate the default system ASP should be used.
     * @see #isConnected(int)
     * @see #getDDMRDB
    **/
    public void setDDMRDB(String iaspName)
    {
      if (iaspName.length() > 18)
      {
        throw new ExtendedIllegalArgumentException("iaspName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      if (isConnected(RECORDACCESS))
      {
        throw new ExtendedIllegalStateException("iaspName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      ddmRDB_ = (iaspName == null ? null : iaspName.toUpperCase());
    }

    /**
     Sets the default user for a given system name.  The default user is the user ID that is used to connect if a user ID is not provided for that system name.  There can be only one default user per system name.  Once the default user is set, it cannot be overridden.  To change the default user, the caller should remove the default user and then set it.
     @param  systemName  The name of the server.
     @param  userId  The user profile name.
     @return  true if default user has been set; false otherwise.
     **/
    public static boolean setDefaultUser(String systemName, String userId)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting the default user, system name: '" + systemName + "' user ID: '" + userId + "'");
        if (systemName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemName' is null.");
            throw new NullPointerException("systemName");
        }
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of 'userId' parameter is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        systemName = resolveSystem(systemName);
        userId = resolveUserId(userId.toUpperCase());

        synchronized (AS400.defaultUsers)
        {
            if (AS400.defaultUsers.get(systemName) == null)
            {
                AS400.defaultUsers.put(systemName, userId);
                return true;
            }
        }
        // Already have a default user, fail the op.
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Default user already set, set default user failed.");
        return false;
    }

    /**
     Sets the option for how the JGSS framework will be used to retrieve a GSS token for authenticating to the server.  By default, if no password or profile token is set on this object, it will attempt to retrieve a GSS token.  If that retrieval fails, a sign-on dialog can be presented, or on an iSeries server, the current user profile information can be used.  This option can also be set to only do the GSS token retrieval or to skip the GSS token retrieval.
     @param  gssOption  A constant indicating how GSS will be used.  Valid values are:
     <ul>
     <li>AS400.GSS_OPTION_MANDATORY
     <li>AS400.GSS_OPTION_FALLBACK
     <li>AS400.GSS_OPTION_NONE
     </ul>
     **/
    public void setGSSOption(int gssOption)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting GSS option:", gssOption);
        if (gssOption < 0 || gssOption > 2)
        {
            Trace.log(Trace.ERROR, "Parameter 'gssOption' is not valid.");
            throw new ExtendedIllegalArgumentException("gssOption (" + gssOption + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        gssOption_ = gssOption;
    }

    /**
     Sets the GSS name for this object.  The GSS name cannot be changed once a connection to the server has been established.  Using this method will set the authentication scheme to AUTHENTICATION_SCHEME_GSS_TOKEN.  Only one authentication means (Kerberos ticket, profile token, or password) can be used at a single time.  Using this method will clear any set profile token or password.
     @param  name  The GSS name string.
     **/
    public void setGSSName(String gssName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting GSS name: '" + gssName + "'");

        if (gssName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'gssName' is null.");
            throw new NullPointerException("gssName");
        }

        if (signonInfo_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set gssName after connection has been made.");
            throw new ExtendedIllegalStateException("gssName", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        synchronized (this)
        {
            gssName_ = gssName;
            bytes_ = null;
            byteType_ = AUTHENTICATION_SCHEME_GSS_TOKEN;
        }
    }

    /**
     Sets the environment you are running in.  If guiAvailable is set to true, then prompting may occur during sign-on to display error conditions, to prompt for additional information, or to prompt for change password.  If guiAvailable is set to false, then these conditions will result in return codes and exceptions.  Applications that are running as server applications or want to control the sign-on user interface may want to run with prompting mode set to false.  Prompting mode is set to true by default.
     @param  guiAvailable  true to prompt; false otherwise.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setGuiAvailable(boolean guiAvailable) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting GUI available:", guiAvailable);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            guiAvailable_ = guiAvailable;
        }
        else
        {
            Boolean oldValue = new Boolean(guiAvailable_);
            Boolean newValue = new Boolean(guiAvailable);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("guiAvailable", oldValue, newValue);
            }
            guiAvailable_ = guiAvailable;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("guiAvailable", oldValue, newValue);
            }
        }
    }

    /**
     Sets the Locale used to set the National Language Version (NLV) on the server.  Only the COMMAND, PRINT, and DATABASE services accept an NLV.
     @param  locale  The Locale object.
     **/
    public void setLocale(Locale locale)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting locale: " + locale);
        if (locale == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'locale' is null.");
            throw new NullPointerException("locale");
        }
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set locale after connection has been made.");
            throw new ExtendedIllegalStateException("locale", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null)
        {
            locale_ = locale;
        }
        else
        {
            Locale oldValue = locale_;
            Locale newValue = locale;

            locale_ = locale;
            propertyChangeListeners_.firePropertyChange("locale", oldValue, newValue);
        }
    }

    /**
     Sets this object to using sockets.  When your Java program runs on an iSeries server, some Toolbox classes access data via a call to an API instead of making a socket call to a server.  There are minor differences in the behavior of the classes when they use API calls instead of socket calls.  If your program is affected by these differences you can force the Toolbox classes to use socket calls instead of API calls by using this method.  The default is false. The must use sockets property cannot be changed once a connection to the server has been established.
     @param  mustUseSockets  true to use sockets; false otherwise.
     **/
    public void setMustUseSockets(boolean mustUseSockets)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting must use sockets:", mustUseSockets);
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set must use sockets after connection has been made.");
            throw new ExtendedIllegalStateException("mustUseSockets", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        mustUseSockets_ = mustUseSockets;
    }

    /**
     Sets the password for this object.  Only one authentication means (Kerberos ticket, profile token, or password) can be used at a single time.  Using this method will clear any set Kerberos ticket or profile token.
     @param  password  The user profile password.
     **/
    public void setPassword(String password)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting password.");

        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of 'password' parameter is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        synchronized (this)
        {
            bytes_ = store(password);
            byteType_ = AUTHENTICATION_SCHEME_PASSWORD;
            signonInfo_ = null;
        }
    }

    /**
     Sets the number of days before password expiration to warn the user.
     @param  days  The number of days before expiration to start the warning.  Set to -1 to turn off warning.
     **/
    public static void setPasswordExpirationWarningDays(int days)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting the password expiration warning days:", days);
        AS400.expirationWarning = days;
    }

    /**
     Sets or resets the profile token for this object.  Using this method will clear any set password.
     @param  profileToken  The profile token.
     **/
    public void setProfileToken(ProfileTokenCredential profileToken)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting profile token.");

        if (profileToken == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'profileToken' is null.");
            throw new NullPointerException("profileToken");
        }

        synchronized (this)
        {
            bytes_ = store(profileToken.getToken());
            byteType_ = AUTHENTICATION_SCHEME_PROFILE_TOKEN;
            signonInfo_ = null;
        }
    }

    /**
     Sets the name and port of the middle-tier machine where the proxy server is running.  If this is not set, then the name is retrieved from the <em>com.ibm.as400.access.AS400.proxyServer</em> <a href="doc-files/SystemProperties.html">system property</a>.  The <a href="ProxyServer.html">ProxyServer</a> must be running on the middle-tier machine.
     <p>The name of the middle-tier machine is ignored in a two-tier environment.  If no middle-tier machine is specified, then it is assumed that no middle-tier will be accessed.  The name of the middle-tier machine cannot be changed once a connection to this machine has been established.
     @param  proxyServer  The name and port of the proxy server in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setProxyServer(String proxyServer) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting proxy server: " + proxyServer);

        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set proxy server after connection has been made.");
            throw new ExtendedIllegalStateException("proxyServer", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            proxyServer_ = resolveProxyServer(proxyServer);
        }
        else
        {
            String oldValue = proxyServer_;
            String newValue = resolveProxyServer(proxyServer);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("proxyServer", oldValue, newValue);
            }
            proxyServer_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("proxyServer", oldValue, newValue);
            }
        }
    }

    /**
     Sets the service port in the service port table for the specified service for this system name.
     @param  service  The name of the service.
     <br>Valid services are:
     <br>   FILE - IFS file classes.
     <br>   PRINT - print classes.
     <br>   COMMAND - command and program call classes.
     <br>   DATAQUEUE - data queue classes.
     <br>   DATABASE - JDBC classes.
     <br>   RECORDACCESS - record level access classes.
     <br>   CENTRAL - licence management classes.
     <br>   SIGNON - sign-on classes.
     @param  port  The port to use for this service.  The value USE_PORT_MAPPER can be used to specify that the next connection to this service should ask the port mapper server for the port number.
     **/
    public void setServicePort(int service, int port)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting service port, service " + service + ", port " + port);

        // Validate parameters.
        if (service < 0 || service > 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'service' is not valid:", service);
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (port < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'port' is not valid:", port);
            throw new ExtendedIllegalArgumentException("port (" + port + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Validate state.
        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot set service port before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        chooseImpl();
        impl_.setServicePort(systemName_, service, port);
    }

    /**
     Sets the ports in the service port table for all the services for this system name to their default values.
     **/
    public void setServicePortsToDefault()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting service ports to default.");

        // Validate state.
        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot set service port to default before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        chooseImpl();
        impl_.setServicePortsToDefault(systemName_);
    }

    /**
     Indicates if checkboxes should be shown on the sign-on dialog.
     @param  showCheckboxes  true to show checkboxes; false otherwise.
     **/
    public void setShowCheckboxes(boolean showCheckboxes)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting show checkboxes:", showCheckboxes);
        showCheckboxes_ = showCheckboxes;
    }

    /**
     Sets the socket options the IBM Toolbox for Java will set on its client side sockets.  The socket properties cannot be changed once a connection to the server has been established.
     @param  socketProperties  The set of socket options to set.  The options are copied from this object, not shared.
     **/
    public void setSocketProperties(SocketProperties socketProperties)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting socket properties: " + socketProperties);
        if (socketProperties == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'socketProperties' is null.");
            throw new NullPointerException("socketProperties");
        }
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set socket properties after connection has been made.");
            throw new ExtendedIllegalStateException("socketProperties", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        socketProperties_.copyValues(socketProperties);
    }

    /**
     Sets the system name for this object.  The system name cannot be changed once a connection to the server has been established.
     @param  systemName  The name of the server.  Use localhost to access data locally.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setSystemName(String systemName) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system name: " + systemName);
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set system name after connection has been made.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            systemName_ = resolveSystem(systemName);
        }
        else
        {
            String oldValue = systemName_;
            String newValue = resolveSystem(systemName);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("systemName", oldValue, newValue);
            }
            systemName_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("systemName", oldValue, newValue);
            }
        }
    }

    /**
     Sets whether the IBM Toolbox for Java uses threads in communication with the host servers.  The default is true. Letting the IBM Toolbox for Java use threads may be beneficial to performance, turning threads off may be necessary if your application needs to be compliant with the Enterprise Java Beans specification. The thread used property cannot be changed once a connection to the server has been established.
     @param  useThreads  true to use threads; false otherwise.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setThreadUsed(boolean useThreads) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting thread used:", useThreads);

        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set thread used after connection has been made.");
            throw new ExtendedIllegalStateException("threadUsed", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            threadUsed_ = useThreads;
        }
        else
        {
            Boolean oldValue = new Boolean(threadUsed_);
            Boolean newValue = new Boolean(useThreads);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("threadUsed", oldValue, newValue);
            }
            threadUsed_ = useThreads;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("threadUsed", oldValue, newValue);
            }
        }
    }

    // Setup password dialog based on information already set.
    private PasswordDialog setupPasswordDialog()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "No password to try, putting up dialog.");

        PasswordDialog pd = new PasswordDialog(new Frame(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), showCheckboxes_);

        // If system name is not set.
        if (systemName_.length() == 0)
        {
            // Enable default user checkbox.
            pd.enableDefaultUserCheckbox();
            // But uncheck it.
            pd.setDefaultUserState(false);
        }
        else
        {
            // Put system name in dialog.
            pd.setSystemName(systemName_);
            // Do we already have a default user for this system
            if (AS400.getDefaultUser(systemName_) == null)
            {
                // Enable the check box.
                pd.enableDefaultUserCheckbox();
                // And check it.
                pd.setDefaultUserState(true);
            }
            else
            {
                // Disable the check box.
                pd.disableDefaultUserCheckbox();
            }
        }
        // Check the use cache checkbox.
        pd.setPasswordCacheState(true);
        // If user ID set, put it in dialog.
        if (userId_.length() != 0) pd.setUserId(userId_);
        return pd;
    }

    /**
     Sets the indicator for whether the default user is used.  The default user is used if a system name is provided, but a user ID is not.  If a default user is set for that system, then the default user is used.
     @param  useDefaultUser  The value indicating if the default user should be used.  Set to true if default user should be used; false otherwise.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setUseDefaultUser(boolean useDefaultUser) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting use default user:", useDefaultUser);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            useDefaultUser_ = useDefaultUser;
        }
        else
        {
            Boolean oldValue = new Boolean(useDefaultUser_);
            Boolean newValue = new Boolean(useDefaultUser);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("useDefaultUser", oldValue, newValue);
            }
            useDefaultUser_ = useDefaultUser;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("useDefaultUser", oldValue, newValue);
            }
        }
    }

    /**
     Sets the indicator for whether the password cache is used.  If password cache is used, then the user would only have to enter password once within a Java virtual machine.  The default is to use the cache.
     @param  usePasswordCache  The value indicating whether the password cache should be used.  Set to true to use the password cache; false otherwise.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setUsePasswordCache(boolean usePasswordCache) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting use password cache:", usePasswordCache);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            usePasswordCache_ = usePasswordCache;
        }
        else
        {
            Boolean oldValue = new Boolean(usePasswordCache_);
            Boolean newValue = new Boolean(usePasswordCache);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("usePasswordCache", oldValue, newValue);
            }
            usePasswordCache_ = usePasswordCache;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("usePasswordCache", oldValue, newValue);
            }
        }
    }

    /**
     Sets the user ID for this object.  The user ID cannot be changed once a connection to the server has been established.  If this method is used in conjunction with a Kerberos ticket or profile token, the user profile associated with the authentication token must match this user ID.
     @param  userId  The user profile name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setUserId(String userId) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user ID: '" + userId + "'");
        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of 'userId' parameter is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (signonInfo_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set user ID after connection has been made.");
            throw new ExtendedIllegalStateException("userId", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            userId_ = userId.toUpperCase();
        }
        else
        {
            String oldValue = userId_;
            String newValue = userId.toUpperCase();

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("userId", oldValue, newValue);
            }
            userId_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("userId", oldValue, newValue);
            }
        }
    }

    // Initiate sign-on to the server.  This method is syncronized to prevent more than one thread from needlessly signing-on.  This method can safely be called multiple times because it checks for a previous sign-on before performing the sign-on code.
    synchronized void signon(boolean keepConnection) throws AS400SecurityException, IOException
    {
        // If we haven't already signed on.
        if (signonInfo_ == null)
        {
            chooseImpl();
            userId_ = resolveUserId(userId_, byteType_);
            // If system name is set.
            if (systemName_.length() != 0)
            {
                // If user ID is not set and we can use the default user.
                if (userId_.length() == 0 && useDefaultUser_)
                {
                    // Get the default user ID.
                    String defaultUserId = getDefaultUser(systemName_);
                    // If we have a default user ID for this system, set the user ID to it.
                    if (defaultUserId != null) userId_ = defaultUserId;
                }
                // If the user ID is set and the password is not set and we can use the password cache.
                if (userId_.length() != 0 && bytes_ == null && usePasswordCache_)
                {
                    // Get password from password cache.
                    synchronized (AS400.systemList)
                    {
                        for (int i = AS400.systemList.size() - 1; i >= 0; i--)
                        {
                            Object[] secobj = (Object[])AS400.systemList.elementAt(i);
                            if (systemName_.equalsIgnoreCase((String)secobj[0]) && userId_.equals(secobj[1]))
                            {
                                bytes_ = (byte[])secobj[2];
                            }
                        }
                    }
                }
            }

            try
            {
                // If the system name is set, we're not using proxy, and the password is not set, and the user has not told us not to.
                if (systemName_.length() != 0 && proxyServer_.length() == 0 && bytes_ == null && gssOption_ != AS400.GSS_OPTION_NONE && !canUseNativeOptimizations())
                {
                    // Try for Kerberos.
                    bytes_ = TokenManager.getGSSToken(systemName_, gssName_);
                    byteType_ = AUTHENTICATION_SCHEME_GSS_TOKEN;
                }
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
                if (gssOption_ == AS400.GSS_OPTION_MANDATORY)
                {
                    throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE);
                }
            }

            // If we can use the prompts, use them, else go right to server flows.
            if (guiAvailable_ && byteType_ == AUTHENTICATION_SCHEME_PASSWORD)
            {
                promptSignon();
            }
            else
            {
                sendSignonRequest();
            }
            if (!keepConnection) impl_.disconnect(AS400.SIGNON);
        }
    }

    // Twiddle profile token bytes.
    private static byte[] store(byte[] info)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object store, profile token: '" + info + "'");
        }
        byte[] adder = new byte[9];
        AS400.rng.nextBytes(adder);

        byte[] mask = new byte[7];
        AS400.rng.nextBytes(mask);

        byte[] infoBytes = encode(adder, mask, info);
        byte[] returnBytes = new byte[infoBytes.length + 16];
        System.arraycopy(adder, 0, returnBytes, 0, 9);
        System.arraycopy(mask, 0, returnBytes, 9, 7);
        System.arraycopy(infoBytes, 0, returnBytes, 16, infoBytes.length);
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object store, bytes:", returnBytes);
        }
        return returnBytes;
    }

    // Twiddle password bytes.
    private static byte[] store(String info)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object store, password: '" + info + "'");
        }
        if (AS400.onAS400) if (info.equalsIgnoreCase("*CURRENT") || info.equals("")) return null;

        byte[] adder = new byte[9];
        AS400.rng.nextBytes(adder);

        byte[] mask = new byte[7];
        AS400.rng.nextBytes(mask);

        byte[] infoBytes = encode(adder, mask, BinaryConverter.charArrayToByteArray(info.toCharArray()));
        byte[] returnBytes = new byte[infoBytes.length + 16];
        System.arraycopy(adder, 0, returnBytes, 0, 9);
        System.arraycopy(mask, 0, returnBytes, 9, 7);
        System.arraycopy(infoBytes, 0, returnBytes, 16, infoBytes.length);
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "AS400 object store, bytes:", returnBytes);
        }
        return returnBytes;
    }

    /**
     Returns the text representation of this AS400 object.
     @return  The string representing this AS400 object.
     **/
    public String toString()
    {
        return "AS400 (system name: '" + systemName_ + "' user ID: '" + userId_ + "'):" + super.toString();
    }

    // Determine if user ID matches current user ID.
    private static boolean userIdMatchesLocal(String userId)
    {
        // First, see if we are running on an iSeries.
        if (AS400.onAS400)
        {
            String currentUserID = CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification());
            if (currentUserID == null)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Current user ID information not available.");
                return false;
            }

            return userId.equals(currentUserID);
        }
        return false;
    }

    /**
     Validates the user ID and password on the server but does not add to the signed-on list.  The system name, user ID, and password need to be set prior to calling this method.
     @return  true if successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public boolean validateSignon() throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Validating Signon.");

        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot validate signon before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        userId_ = resolveUserId(userId_);
        if (userId_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot validate signon before user ID is set.");
            throw new ExtendedIllegalStateException("userId", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        return validateSignon(userId_, bytes_);
    }

    /**
     Validates the user ID and password on the server but does not add to the signed-on list.  The user ID and system name need to be set before calling this method.
     @param  password  The user profile password to validate.
     @return  true if successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public boolean validateSignon(String password) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Validating Signon, with password.");

        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of 'password' parameter is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot validate signon before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        userId_ = resolveUserId(userId_);
        if (userId_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot validate signon before user ID is set.");
            throw new ExtendedIllegalStateException("userId", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        return validateSignon(userId_, store(password));
    }

    /**
     Validates the user ID and password on the server but does not add to the signed-on list.  The system name needs to be set prior to calling this method.
     @param  userId  The user profile name to validate.
     @param  password  The user profile password to validate.
     @return  true if successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the server.
     **/
    public boolean validateSignon(String userId, String password) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Validating signon, user ID: '" + userId + "'");

        if (userId == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userId' is null.");
            throw new NullPointerException("userId");
        }
        if (userId.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of 'userId' parameter is not valid: '" + userId + "'");
            throw new ExtendedIllegalArgumentException("userId (" + userId + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'password' is null.");
            throw new NullPointerException("password");
        }
        if (password.length() > 128)
        {
            Trace.log(Trace.ERROR, "Length of 'password' parameter is not valid: " + password.length());
            throw new ExtendedIllegalArgumentException("password.length {" + password.length() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (systemName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot validate signon before system name is set.");
            throw new ExtendedIllegalStateException("systemName", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        return validateSignon(userId.toUpperCase(), store(password));
    }

    // Internal version of validate sign-on takes checked user ID and twiddled password bytes.
    private boolean validateSignon(String userId, byte[] bytes) throws AS400SecurityException, IOException
    {
        AS400 validationSystem = new AS400(systemName_, userId, bytes);
        validationSystem.proxyServer_ = proxyServer_;
        // proxyClientConnection_ is not needed.
        validationSystem.guiAvailable_ = false;
        validationSystem.usePasswordCache_ = false;
        validationSystem.useDefaultUser_ = false;
        // showCheckboxes_ is not needed.
        validationSystem.useSSLConnection_ = useSSLConnection_;
        validationSystem.mustUseSockets_ = true;
        // threadUsed_ is not needed.
        // locale_ in not needed.
        validationSystem.socketProperties_ = socketProperties_;
        // ccsid_ is not needed.
        // connectionListeners_ is not needed.
        // dispatcher_ is not needed.
        // propertyChangeListeners_ is not needed.
        // vetoableChangeListeners_ is not needed.
        // propertiesFrozen_ is not needed.
        // impl_ is not copied.
        // signonInfo_ is not copied.

        validationSystem.signon(false);
        return true;
    }
}
