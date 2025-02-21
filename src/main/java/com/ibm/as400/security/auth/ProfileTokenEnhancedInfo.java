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

    private String verificationID_ = null;
    private String remoteIPAddress_ = null;
    private int remotePort_ = 0;
    private String localIPAddress_ = null;
    private int localPort_ = 0;

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
        return (verificationID_ != null) ? verificationID_ : ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
    }
    public String getRemoteIPAddress() { return remoteIPAddress_; } 
    public int    getRemotePort() { return remotePort_; }
    public String getLocalIPAddress() { return localIPAddress_; } 
    public int    getLocalPort() { return localPort_; }
    public boolean getCreateEnhancedIfPossible() { return createEnhancedIfPossible_;} 
    
    public void setVerificationID(String verificationID ) { verificationID_ = verificationID; }
    public void setRemoteIPAddress(String remoteIPAddress) { remoteIPAddress_ = remoteIPAddress; } 
    public void setRemotePort(int remotePort ) { remotePort_ = remotePort; }
    public void setLocalIPAddress(String localIPAddress ) { localIPAddress_ = localIPAddress; } 
    public void setLocalPort(int localPort ) {  localPort_ = localPort; }
    public void setCreateEnhancedIfPossible(boolean ifPossible) { createEnhancedIfPossible_ = ifPossible;}
    public boolean wasEnhancedTokenCreated() { return enhancedTokenCreated_; }
    public void setEnhancedTokenCreated(boolean enhancedTokenCreated) { enhancedTokenCreated_ = enhancedTokenCreated; }

    public void reset() {
        enhancedTokenCreated_ = false;
        verificationID_ = null;
        localIPAddress_ = null;
        remoteIPAddress_ = null;
        localPort_ = 0;
        remotePort_ = 0;
    }

    public void initialize(boolean enhancedTokenCreated, String verificationID, String remoteIPAddress, int remotePort,
            String localIPAddress, int localPort) {
        enhancedTokenCreated_ = enhancedTokenCreated;
        verificationID_ = verificationID;
        remoteIPAddress_ = remoteIPAddress;
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
}
