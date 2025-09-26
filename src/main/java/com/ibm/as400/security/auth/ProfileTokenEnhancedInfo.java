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

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;

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
    private boolean isEnhancedProfileToken_ = false;

    private String verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
    private String remoteIPAddress_            = null;
    private int remotePort_ = 0;
    private String localIPAddress_ = null;
    private int localPort_ = 0;

    /* Create the default profile token info. The creates a profile token with the TOOLBOX default verification ID */ 
    public ProfileTokenEnhancedInfo() {
    }

    public ProfileTokenEnhancedInfo(String verificationID, String remoteIPAddress, int remotePort,
            String localIPAddress, int localPort) {
        initialize(verificationID, remoteIPAddress, remotePort, localIPAddress, localPort);
    }


    public ProfileTokenEnhancedInfo(ProfileTokenEnhancedInfo enhancedInfo) {
        initialize(
                enhancedInfo.verificationID_,
                enhancedInfo.remoteIPAddress_, 
                enhancedInfo.remotePort_, 
                enhancedInfo.localIPAddress_, 
                enhancedInfo.localPort_);
    }

    public String getVerificationID() { return verificationID_ ; }
    public String getRemoteIPAddress() { return remoteIPAddress_; } 
    public int    getRemotePort() { return remotePort_; }
    public String getLocalIPAddress() { return localIPAddress_; } 
    public int    getLocalPort() { return localPort_; }
    
    public void setVerificationID(String verificationID ) { 
        verificationID_ = (verificationID == null) ? ProfileTokenCredential.DEFAULT_VERIFICATION_ID :verificationID;
    }
    public void setRemoteIPAddress(String remoteIPAddress) { remoteIPAddress_ = remoteIPAddress; } 
    public void setRemotePort(int remotePort ) { remotePort_ = remotePort; }
    public void setLocalIPAddress(String localIPAddress ) { localIPAddress_ = localIPAddress; } 
    public void setLocalPort(int localPort ) {  localPort_ = localPort; }
    public boolean isEnhancedProfileToken() { return isEnhancedProfileToken_; }
    /* indicate that an enhanced token was created.  The extended information must be correct or an
     * exception will be thrown. 
     */
    public void setEnhancedTokenCreated(boolean enhancedTokenCreated) throws AS400AuthenticationException { 
      if (enhancedTokenCreated) {
        checkEnhancedTokenForValidity(true); 
      }
      isEnhancedProfileToken_ = enhancedTokenCreated; 
    }

    public void reset() {
        isEnhancedProfileToken_ = false;
        verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
        localIPAddress_ = null;
        remoteIPAddress_        = null;
        localPort_ = 0;
        remotePort_ = 0;
    }

    
    void checkEnhancedTokenForValidity(boolean assertOnInvalid) throws AS400AuthenticationException {
      if (verificationID_ == null) {
        if (assertOnInvalid) { 
           throw new AS400AuthenticationException(AS400SecurityException.REQUEST_NOT_SUPPORTED);
        }
        /* Set to blanks */ 
        verificationID_ = ""; 
      } 
    
      if (remoteIPAddress_ == null) {
        if (assertOnInvalid) { 
            throw new AS400AuthenticationException(AS400SecurityException.REQUEST_NOT_SUPPORTED);
        }
        remoteIPAddress_=""; 
      }
    }

    public void initialize(boolean enhancedTokenCreated, String verificationID, String remoteIPAddress, int remotePort,
        String localIPAddress, int localPort) throws AS400AuthenticationException {
      verificationID_ = verificationID;
      remoteIPAddress_ = remoteIPAddress;
      if (enhancedTokenCreated) {
        checkEnhancedTokenForValidity(true);
      }
        isEnhancedProfileToken_ = enhancedTokenCreated;
       remotePort_ = remotePort;
        localIPAddress_ = localIPAddress;
        localPort_ = localPort;
   
        
    }

    /* initialize with profile token created is false */ 
    public void initialize(String verificationID, String remoteIPAddress, int remotePort,
        String localIPAddress, int localPort) {
      verificationID_ = verificationID;
      remoteIPAddress_ = remoteIPAddress;
       remotePort_ = remotePort;
        localIPAddress_ = localIPAddress;
        localPort_ = localPort;
   
        
    }

    
    
    public void ensureRequiredFieldsSet(String remoteIPAddress)
    {
        if (verificationID_ == null || verificationID_.isEmpty())
            verificationID_ = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
        
        if (remoteIPAddress_ == null || remoteIPAddress_.isEmpty())
        {
            if (remoteIPAddress == null || remoteIPAddress.isEmpty())
                remoteIPAddress = AS400.getDefaultLocalIPAddress();

            remoteIPAddress_ = remoteIPAddress;
        }
    }

    
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ProfileTokenEnhancedInfo {");
        sb.append("enhancedTokenCreated: ").append(isEnhancedProfileToken_);
        sb.append(",").append("verificationID=").append(verificationID_);
        if (remoteIPAddress_ != null) { 
          sb.append(",").append("remoteIPAddress=").append(remoteIPAddress_);
        }
        sb.append("}");
        
        return sb.toString();
    }

    
}
