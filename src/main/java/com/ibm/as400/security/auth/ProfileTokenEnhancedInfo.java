////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenCredential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The ProfileTokenEnhandeInfo class represents the additional information used
 * by an enhanced profile token. 
 *
 *        verificationID  The verification ID is the label that identifies the
 *                        specific application, service, or action associated
 *                        with the profile handle request. This value must be
 *                        30-characters or less. This value will be passed to
 *                        the authentication exit program registered under the
 *                        QIBM_QSY_AUTH exit point if the specified user profile
 *                        has *REGFAC as an authentication method. The
 *                        authentication exit program may use the verification
 *                        ID as a means to restrict the use of the user profile.
 *                        If running on an IBM i, the verification ID should be
 *                        the DCM application ID or a similar value that
 *                        identifies the application or service. 
 * 
 *        remoteIPAddress If the API is used by a server to provide access to a
 *                        the system, the remote IP address should be obtained
 *                        from the socket connection (i.e. using
 *                        Socket.getInetAddress). Otherwise, null should be
 *                        passed. 
 * 
 *        remotePort      If the API is used by a server to provide access to a
 *                        the system, the remote port should be obtained from
 *                        the socket connection (i.e. using Socket.getPort ).
 *                        Otherwise, use 0 if there is not an associated
 *                        connection. 
 * 
 *        localIPAddress  If the API is used by a server to provide access to a
 *                        the system, the local IP address should be obtained
 *                        from the socket connection (i.e. using
 *                        Socket.getLocalAddress). Otherwise, null should be
 *                        passed. 
 * 
 *        localPort       If the API is used by a server to provide access to a
 *                        the system, the local port should be obtained from the
 *                        socket connection (Socket.getLocalPort). Otherwise,
 *                        use 0 if there is not an associated connection.
 *                        
 */
public final class ProfileTokenEnhancedInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean enhancedTokenCreated_ = false;
    private boolean createEnhancedIfPossible_ = true;

    private String verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
    private String remoteIPAddress_ = "";
    private int remotePort_ = 0;
    private String localIPAddress_ = null;
    private int localPort_ = 0;

    /* Create the default profile token info. The creates a profile token with the TOOLBOX default verification ID */ 
    public ProfileTokenEnhancedInfo() {
    }

    public ProfileTokenEnhancedInfo(String verificationID, String remoteIPAddress, int remotePort,
            String localIPAddress, int localPort) {
        initialize(false, verificationID, remoteIPAddress, remotePort, localIPAddress, localPort);
    }


    public ProfileTokenEnhancedInfo(ProfileTokenEnhancedInfo enhancedInfo) {
        initialize(false, 
                enhancedInfo.verificationID_,
                enhancedInfo.remoteIPAddress_, 
                enhancedInfo.remotePort_, 
                enhancedInfo.localIPAddress_, 
                enhancedInfo.localPort_);
    }

    public String getVerificationID() {
        return verificationID_ ;
    }
    public String getRemoteIPAddress() { return remoteIPAddress_; } 
    public int    getRemotePort() { return remotePort_; }
    public String getLocalIPAddress() { return localIPAddress_; } 
    public int    getLocalPort() { return localPort_; }
    public boolean getCreateEnhancedIfPossible() { return createEnhancedIfPossible_;} 
    
    public void setVerificationID(String verificationID ) { 
        verificationID_ = (verificationID == null) ? ProfileTokenCredential.DEFAULT_VERIFICATION_ID :verificationID;
    }
    public void setRemoteIPAddress(String remoteIPAddress) { remoteIPAddress_ = remoteIPAddress; } 
    public void setRemotePort(int remotePort ) { remotePort_ = remotePort; }
    public void setLocalIPAddress(String localIPAddress ) { localIPAddress_ = localIPAddress; } 
    public void setLocalPort(int localPort ) {  localPort_ = localPort; }
    public void setCreateEnhancedIfPossible(boolean ifPossible) { createEnhancedIfPossible_ = ifPossible;}
    public boolean wasEnhancedTokenCreated() { return enhancedTokenCreated_; }
    public void setEnhancedTokenCreated(boolean enhancedTokenCreated) { 
      if (enhancedTokenCreated) {
        checkEnhancedTokenForValidity(); 
      }
      enhancedTokenCreated_ = enhancedTokenCreated; 
    }

    public void reset() {
        enhancedTokenCreated_ = false;
        verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
        localIPAddress_ = null;
        remoteIPAddress_ = "";
        localPort_ = 0;
        remotePort_ = 0;
    }

    void checkEnhancedTokenForValidity() {
      if (verificationID_ == null) {
        /* Set to blanks */ 
        verificationID_ = ""; 
      } 
      
      if (remoteIPAddress_ == null) {
        remoteIPAddress_=""; 
      }
    }

    public void initialize(boolean enhancedTokenCreated, String verificationID, String remoteIPAddress, int remotePort,
        String localIPAddress, int localPort) {
      verificationID_ = verificationID;
      remoteIPAddress_ = remoteIPAddress;
      if (enhancedTokenCreated) {
        checkEnhancedTokenForValidity();
      }
        enhancedTokenCreated_ = enhancedTokenCreated;
        remotePort_ = remotePort;
        localIPAddress_ = localIPAddress;
        localPort_ = localPort;

      
        
    }
    
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ProfileTokenEnhancedInfo {");
        sb.append("enhancedTokenCreated: ").append(enhancedTokenCreated_);
        if (enhancedTokenCreated_)
        {
            sb.append(",").append("verificationID=").append(verificationID_);
            sb.append(",").append("remoteIPAddress=").append(remoteIPAddress_);
        }
        sb.append("}");
        
        return sb.toString();
    }

    /* Make sure the enhanced information is valid for an MFA user */
    /* The system requires that the verificationId and remoteIPAddress be set to some values */ 
    /* This must be called before calling QSYGENPT */ 
    public void ensureValidEnhancedForMfaUser(String defaultRemoteIpAddress) {
        if (verificationID_ == null || verificationID_.length() == 0) {
          verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
        }
        if (remoteIPAddress_ == null || remoteIPAddress_.length() == 0) {
            remoteIPAddress_ = defaultRemoteIpAddress; 
        }
    }
}
