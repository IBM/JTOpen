///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400Impl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.ietf.jgss.GSSCredential;

import com.ibm.as400.security.auth.ProfileTokenCredential;

// AS400Impl defines the implementation interface for the AS400 object.
interface AS400Impl
{
    // Hook AS400 object up with connection events generated in ImplRemote.
    void addConnectionListener(ConnectionListener listener);
    // Map from CCSID to encoding string.
    String ccsidToEncoding(int ccsid);
    // Change password.
    SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes) throws AS400SecurityException, IOException;
    // Change password with AAF
    SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes, char[] additionalAuthenticationFactor) throws AS400SecurityException, IOException;
    // Connect to service.
    void connect(int service, int overridePort, boolean skipSignonServer) throws AS400SecurityException, IOException;  /*@V1C*/
    // Connect to service.
    void connect(int service) throws AS400SecurityException, IOException;  
    // Disconnect from service.
    void disconnect(int service);
    // Exchange seeds with remote implementation.
    byte[] exchangeSeed(byte[] proxySeed);

    // Get the jobs with which we are connected.
    String[] getJobs(int service);
    // Sets the raw bytes for the provided profile token.
    void generateProfileToken(ProfileTokenCredential profileToken, String userIdentity) throws AS400SecurityException, IOException;
    // Sets the raw bytes for the provided profile token.
    void generateProfileToken(ProfileTokenCredential profileToken, String userId, CredentialVault vault, char[] additionalAuthenticationFactor, String gssName) throws AS400SecurityException, IOException, InterruptedException;
    // Get the port for a service.
    int getServicePort(String systemName, int service);
    // Check service connection.
    boolean isConnected(int service);
    // Check connection's current status.
    boolean isConnectionAlive();
    // Check connection's current status.
    boolean isConnectionAlive(int service);
    // Load converter into converter pool.
    void newConverter(int ccsid) throws UnsupportedEncodingException;
    // Remove the connection event dispatcher.
    void removeConnectionListener(ConnectionListener listener);
    // Set the GSS credential.
    void setGSSCredential(GSSCredential gssCredential);
    // Set the port for a service.
    void setServicePort(String systemName, int service, int port);
    // Set the service ports to their default values.
    void setServicePortsToDefault(String systemName);
    // Set significant instance variables into implementation object.
    void setState(SSLOptions useSSLConnection, boolean canUseNativeOptimization, boolean threadUsed, boolean virtualThreads, int ccsid, String nlv, SocketProperties socketProperties, String ddmRDB, boolean mustUseNetSockets, boolean mustUseSuppliedProfile, boolean mustAddLanguageLibrary);
    SignonInfo setState(AS400Impl impl, CredentialVault credVault);
    // Sign-on to system.
    SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName) throws AS400SecurityException, IOException;
    SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName, char[] additionalAuthenticationFactor) throws AS400SecurityException, IOException;
    
    /**
     * Sets bidi string type of the connection. 
     * See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     * @param bidiStringType  The bidi string type to use for the connection.
     */
    public void setBidiStringType(int bidiStringType);
    
    void setAdditionalAuthenticationFactor(char[] additionalAuthFactor);
    
    /**
     * Returns bidi string type of the connection. 
     * See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     * @return bidi string type
     */
    public int getBidiStringType();        
    
    /* connect to the system, but skip using the signon server */ 
    SignonInfo skipSignon(String systemName, boolean systemNameLocal, String userId_, CredentialVault tempVault, String gssName) throws AS400SecurityException, IOException;
    
    String getSystemName();
    /* Set the VRM for the object.  Only set for the remote Impl */ 
    void setVRM(int v, int r, int m); 


    void setKerbTicket(byte[] ticket);
}
