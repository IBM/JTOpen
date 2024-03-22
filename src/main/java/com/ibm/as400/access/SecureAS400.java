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
// Note:  This class was moved 10/20/2010 from the include tree to the 
//        src tree.  
// 
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;

import java.io.IOException;

import com.ibm.as400.security.auth.ProfileTokenCredential;

/**
 Represents a secure system sign-on.  Secure Sockets Layer (SSL) connections are used to provide encrypted communications.  This function requires an SSL capable system at release V4R4 or later.
 **/
public class SecureAS400 extends AS400
{
    static final long serialVersionUID = 4L;


    private void construct()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SecureAS400 object.");

        useSSLConnection_ = new SSLOptions();

        // Check for proxy encryption mode system property, if not set or not valid retain default of 3.
        String prop = SystemProperties.getProperty(SystemProperties.SECUREAS400_PROXY_ENCRYPTION_MODE);
        if (prop != null && (prop.equals("1") || prop.equals("2")))
        {
            useSSLConnection_.proxyEncryptionMode_ = Integer.parseInt(prop);
        }

    }

    /**
     Constructs a SecureAS400 object.
     **/
    public SecureAS400()
    {
        super();
        construct();
    }

    /**
     Constructs a SecureAS400 object.  It uses the specified system name.
     @param  systemName  The name of the system.
     **/
    public SecureAS400(String systemName)
    {
        super(systemName);
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
        construct();
    }

    /**
     Constructs a SecureAS400 object. It uses the specified system name, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
     @deprecated
     **/
    public SecureAS400(String systemName, String userId, String password)
    {
        super(systemName, userId, password);
        construct();
    }

        /**
     Constructs a SecureAS400 object. It uses the specified system name, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
    
     **/
    public SecureAS400(String systemName, String userId, char[] password)
    {
        super(systemName, userId, password);
        construct();
    }
    /**
     Constructs a SecureAS400 object.  It uses the specified system name, user ID, , and additional authentication
     factor.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the IBM i system.  Use <code>localhost</code> to access data locally.
     @param  userId  The user profile name to use to authenticate to the system.  If running on IBM i, *CURRENT may be used to specify the current user ID.
     @param  password  The user profile password to use to authenticate to the system.
     @param  additionalAuthenticationFactor Additional authentication factor (or null if not providing one).
     The caller is responsible for clearing the password array to keep the password from residing in memory. 
                     
     **/
    public SecureAS400(String systemName, String userId, char[] password, char[] additionalAuthenticationFactor) throws IOException, AS400SecurityException
    {
        super(systemName, userId, password, additionalAuthenticationFactor);
        construct();
    }
    /**
     Constructs a SecureAS400 object.  It uses the specified system, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
     @param  proxyServer  The name and port in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     @deprecated
     **/
    public SecureAS400(String systemName, String userId, String password, String proxyServer)
    {
        super(systemName, userId, password, proxyServer);
        construct();
    }


        /**
     Constructs a SecureAS400 object.  It uses the specified system, user ID, and password.  No sign-on prompt is displayed unless the sign-on fails.
     @param  systemName  The name of the system.
     @param  userId  The user profile name to use to authenticate to the system.
     @param  password  The user profile password to use to authenticate to the system.
     @param  proxyServer  The name and port in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.

     **/
    public SecureAS400(String systemName, String userId, char[] password, String proxyServer)
    {
        super(systemName, userId, password, proxyServer);
        construct();
    }
    
    /**
     Constructs a SecureAS400 object.  It uses the same system name and user ID.  This does not create a clone.  The new SecureAS400 object has the same behavior, but results in a new set of socket connections.
     @param  system  A previously instantiated AS400 or SecureAS400 object.
     **/
    public SecureAS400(AS400 system)
    {
        super(system);
        construct();

        // If passed in system has SSL options, deep copy them.
        if (system.useSSLConnection_ != null)
        {
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
     @deprecated Use addPasswordCacheEntry(String systemName, String userId, char[] password, boolean useSSL) instead    
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password) throws AS400SecurityException, IOException
    {
        addPasswordCacheEntry(systemName, userId, password == null ? (char[])null : password.toCharArray(), true);
    }

        /**
     Validates the user ID and password against the system, and if successful, adds the information to the password cache.
     @param  systemName  The name of the system.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the system.
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, char[] password) throws AS400SecurityException, IOException
    {
        addPasswordCacheEntry(systemName, userId, password, true);
    }


    /**
     Validates the user ID and password against the system, and if successful, adds the information to the password cache.
     @param  systemName  The name of the system.
     @param  userId  The user profile name.
     @param  password  The user profile password.
     @param  proxyServer  The name and port in the format <code>serverName[:port]</code>.  If no port is specified, a default will be used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the system.
     @deprecated Use addPasswordCacheEntry(String systemName, String userId, char[] password, String proxyServer) instead. 
     **/
    public static void addPasswordCacheEntry(String systemName, String userId, String password, String proxyServer) throws AS400SecurityException, IOException
    {
        addPasswordCacheEntry(systemName, userId, password == null ? (char[])null : password.toCharArray(), proxyServer, true);
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
    public static void addPasswordCacheEntry(String systemName, String userId, char[] password, String proxyServer) throws AS400SecurityException, IOException
    {
        addPasswordCacheEntry(systemName, userId, password, proxyServer, true);
    }
    
    /**
    Checks whether an additional authentication factor is accepted for the given system. 
    @param  systemName  The IP address or hostname of the target system
    @return  whether the server accepts the additional authentication factor
    @exception  IOException  If an error occurs while communicating with the system.
    @throws AS400SecurityException  If an error occurs exchanging client/server information
    **/
    public static boolean  isAdditionalAuthenticationFactorAccepted(String systemName) throws IOException, AS400SecurityException {
       return isAdditionalAuthenticationFactorAccepted(systemName, true);
    }
}
