///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SecureAS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import com.ibm.sslight.SSLightKeyRing;

/**
 Represents a secure system sign-on.  Secure Sockets Layer (SSL) connections are used to provide encrypted communications.  This function requires an SSL capable system at release V4R4 or later.
 **/
public class SecureAS400 extends AS400
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others."; // Leave this in place, since .class files under /include/ don't get copyright-stamped in the build process.

    static final long serialVersionUID = 4L;
    /**
     Constant indicating that encryption should only be done on the connection between the client and the proxy server.
     **/
    public static final int CLIENT_TO_PROXY_SERVER = 1;

    /**
     Constant indicating that encryption should only be done on the connection between the proxy server and the system.
     **/
    public static final int PROXY_SERVER_TO_SERVER = 2;

    /**
     @deprecated Use CLIENT_TO_SERVER instead.
     **/
    public static final int CLINT_TO_SERVER = 3;

    /**
     Constant indicating that encryption should be done in both the connection between the client and the proxy server and the connection between the proxy server and the system.
     **/
    public static final int CLIENT_TO_SERVER = 3;

    private void construct()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Turning SSL connections on.");
        useSSLConnection_ = new SSLOptions();

        // Check for proxy encryption mode system property, if not set or not valid retain default of 3.
        String prop = SystemProperties.getProperty(SystemProperties.SECUREAS400_PROXY_ENCRYPTION_MODE);
        if (prop != null && (prop.equals("1") || prop.equals("2")))
        {
            useSSLConnection_.proxyEncryptionMode_ = Integer.parseInt(prop);
        }

        // Check for use sslight system property, if not set or not valid retain default of false.
        prop = SystemProperties.getProperty(SystemProperties.SECUREAS400_USE_SSLIGHT);
        if (prop != null && prop.equalsIgnoreCase("true"))
        {
            useSSLConnection_.useSslight_ = true;
        }
    }

    /**
     Constructs a SecureAS400 object.
     **/
    public SecureAS400()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object.  It uses the specified system name.
     @param  systemName  The name of the system.
     **/
    public SecureAS400(String systemName)
    {
        super(systemName);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object. It uses the specified system name and user ID.  When the sign-on prompt is displayed, the user is able to specify the password.  Note that the user ID may be overridden.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     **/
    public SecureAS400(String systemName, String userId)
    {
        super(systemName, userId);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object.  It uses the specified system name and profile token.
     @param  systemName  The name of the system.  Use localhost to access data locally.
     @param  profileToken  The profile token to use to authenticate to the system.
     **/
    public SecureAS400(String systemName, ProfileTokenCredential profileToken)
    {
        super(systemName, profileToken);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object. It uses the specified system name, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
     **/
    public SecureAS400(String systemName, String userId, String password)
    {
        super(systemName, userId, password);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object.  It uses the specified system, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
     @param  proxyServer  The name and port in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     **/
    public SecureAS400(String systemName, String userId, String password, String proxyServer)
    {
        super(systemName, userId, password, proxyServer);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();
    }

    /**
     Constructs a SecureAS400 object.  It uses the same system name and user ID.  This does not create a clone.  The new SecureAS400 object has the same behavior, but results in a new set of socket connections.
     @param  system  A previously instantiated AS400 or SecureAS400 object.
     **/
    public SecureAS400(AS400 system)
    {
        super(system);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");
        construct();

        // If passed in system has SSL options, deep copy them.
        if (system.useSSLConnection_ != null)
        {
            useSSLConnection_.keyRingName_ = system.useSSLConnection_.keyRingName_;
            useSSLConnection_.keyRingPassword_ = system.useSSLConnection_.keyRingPassword_;
            useSSLConnection_.proxyEncryptionMode_ = system.useSSLConnection_.proxyEncryptionMode_;
        }
    }

    /**
     Validates the user ID and password against the system, and if successful, adds the information to the password cache.
     @param  systemName  The name of the system.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the system.
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry, system name: '" + systemName + "' user ID: '" + userId + "'");
        addPasswordCacheEntry(new SecureAS400(systemName, userId, password));
    }

    /**
     Validates the user ID and password against the system, and if successful, adds the information to the password cache.
     @param  systemName  The name of the system.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @param  proxyServer  The name and port in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the system.
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password, String proxyServer) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding password cache entry, system name: '" + systemName + "' user ID: '" + userId + "' proxy server: '" + proxyServer + "'");
        addPasswordCacheEntry(new SecureAS400(systemName, userId, password, proxyServer));
    }

    /**
     Returns the key ring class name used for SSL communications with the system.  The class <i>com.ibm.as400.access.KeyRing</i> is the default and will be returned if not overridden.
     @return  The key ring class name.
     **/
    public String getKeyRingName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting key ring name: " + useSSLConnection_.keyRingName_);
        return useSSLConnection_.keyRingName_;
    }

    /**
     Returns the proxy encryption mode.  The proxy encryption mode specifies which portions of the communications between the client, proxy server, and IBM i system are encrypted.
     @return  The proxy encryption mode.
     **/
    public int getProxyEncryptionMode()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting proxy encryption mode:", useSSLConnection_.proxyEncryptionMode_);
        return useSSLConnection_.proxyEncryptionMode_;
    }

    /**
     Sets the key ring class name used for SSL communications with the system.  The default class name that will be used if this method is not called is <i>com.ibm.as400.access.KeyRing</i>.
     @param  keyRingName  The key ring class name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setKeyRingName(String keyRingName) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting key ring name: " + keyRingName);
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set key ring class name after connection has been made.");
            throw new ExtendedIllegalStateException("keyRingName", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        if (keyRingName == null) {
            throw new NullPointerException("keyRingName");
        }

        String oldValue = useSSLConnection_.keyRingName_;
        String newValue = keyRingName;
        if (vetoableChangeListeners_ != null)
        {
          vetoableChangeListeners_.fireVetoableChange("keyRingName", oldValue, newValue);
        }

        useSSLConnection_.keyRingName_ = keyRingName;
        try
        {
            SSLightKeyRing ring = (SSLightKeyRing)Class.forName(keyRingName).newInstance();
            useSSLConnection_.keyRingData_ = ring.getKeyRingData();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'keyRingName' is not valid: " + keyRingName, e);
            throw new ExtendedIllegalArgumentException("keyRingName (" + keyRingName + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (propertyChangeListeners_ != null)
        {
          propertyChangeListeners_.firePropertyChange("keyRingName", oldValue, newValue);
        }
    }

    /**
     Sets the key ring class name used for SSL communications with the system.  The default class name that will be used if this method is not called is <i>com.ibm.as400.access.KeyRing</i>.
     @param  keyRingName  The key ring class name.
     @param  keyRingPassword  The password for the key ring class.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setKeyRingName(String keyRingName, String keyRingPassword) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting key ring name: " + keyRingName);
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set key ring class name after connection has been made.");
            throw new ExtendedIllegalStateException("keyRingName", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        if (keyRingName == null) {
            throw new NullPointerException("keyRingName");
        }

        String oldValue = useSSLConnection_.keyRingName_;
        String newValue = keyRingName;
        if (vetoableChangeListeners_ != null)
        {
          vetoableChangeListeners_.fireVetoableChange("keyRingName", oldValue, newValue);
        }

        useSSLConnection_.keyRingName_ = keyRingName;
        useSSLConnection_.keyRingPassword_ = keyRingPassword;
        try
        {
            SSLightKeyRing ring = (SSLightKeyRing)Class.forName(keyRingName).newInstance();
            useSSLConnection_.keyRingData_ = ring.getKeyRingData();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'keyRingName' is not valid: " + keyRingName, e);
            throw new ExtendedIllegalArgumentException("keyRingName (" + keyRingName + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (propertyChangeListeners_ != null)
        {
          propertyChangeListeners_.firePropertyChange("keyRingName", oldValue, newValue);
        }
    }

    /**
     Sets the key ring password used for SSL communications with the system.
     @param  keyRingPassword  The password for the key ring class.
     **/
    public void setKeyRingPassword(String keyRingPassword)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting key ring password.");
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set key ring class password after connection has been made.");
            throw new ExtendedIllegalStateException("keyRingPassword", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        useSSLConnection_.keyRingPassword_ = keyRingPassword;
    }

    /**
     Sets the proxy encryption mode.  The proxy encryption mode specifies which portions of the communications between the client, proxy server, and IBM i system are encrypted.  The default is to encrypt all communications.  This value is ignored if a proxy server is not used.
     <br>Valid proxy encryption modes are:
     <br>{@link #CLIENT_TO_PROXY_SERVER CLIENT_TO_PROXY_SERVER} - encrypt between client and proxy server.
     <br>{@link #PROXY_SERVER_TO_SERVER PROXY_SERVER_TO_SERVER} - encrypt between proxy server and IBM i system.
     <br>{@link #CLIENT_TO_SERVER CLIENT_TO_SERVER} - encrypt both portions of connection.
     @param  proxyEncryptionMode  The proxy encryption mode.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setProxyEncryptionMode(int proxyEncryptionMode) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting proxy encryption mode:", proxyEncryptionMode);
        // Validate parameter.
        if (proxyEncryptionMode < CLIENT_TO_PROXY_SERVER ||
            proxyEncryptionMode > CLIENT_TO_SERVER)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'proxyEncryptionMode' is not valid:", proxyEncryptionMode);
            throw new ExtendedIllegalArgumentException("proxyEncryptionMode (" + proxyEncryptionMode + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (propertiesFrozen_)
        {
            Trace.log(Trace.ERROR, "Cannot set proxy encryption mode after connection has been made.");
            throw new ExtendedIllegalStateException("proxyEncryptionMode", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        Integer oldValue = new Integer(useSSLConnection_.proxyEncryptionMode_);
        Integer newValue = new Integer(proxyEncryptionMode);
        if (vetoableChangeListeners_ != null)
        {
          vetoableChangeListeners_.fireVetoableChange("proxyEncryptionMode", oldValue, newValue);
        }

        useSSLConnection_.proxyEncryptionMode_ = proxyEncryptionMode;

        if (propertyChangeListeners_ != null)
        {
          propertyChangeListeners_.firePropertyChange("proxyEncryptionMode", oldValue, newValue);
        }
    }
}
