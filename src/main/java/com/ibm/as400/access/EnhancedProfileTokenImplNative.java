///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: EnhancedProfieTokenImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2023-2024 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;

import com.ibm.as400.security.auth.*;

/**
 * The EnhancedProfileTokenImplNative class provides access to native methods on the IBM
 * that work with an enhanced profile token.  These methods will only work when run on an 
 * IBM i system and the native code is on the IBM i system.  It is the responsibility of 
 * the caller to verify that the native methods are available. 
 **/
public class EnhancedProfileTokenImplNative 
{
    private static final String CLASSNAME = "com.ibm.as400.access.ProfileTokenImplNative";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
        NativeMethods.loadNativeLibraryQyjspart();
    }

    /**
     * Generates and returns a new profile token based on a user profile, password,
     * and additional authentication factor.
     * 
     * @param user                           The name of the user profile for which
     *                                       the token is to be generated.
     * 
	 * @param password                       The password for the user.  Must not be a password
	 *                                       special value. 
     * 
     * @param additionalAuthenticationFactor The additional authentication factor
     *                                       for the user
     * 
     * @param verificationId                 The verification ID is the label that
     *                                       identifies the specific application,
     *                                       service, or action associated with the
     *                                       profile handle request. This value must
     *                                       be 30-characters or less. This value
     *                                       will be passed to the authentication
     *                                       exit program registered under the
     *                                       QIBM_QSY_AUTH exit point if the
     *                                       specified user profile has *REGFAC as
     *                                       an authentication method. The
     *                                       authentication exit program may use the
     *                                       verification ID as a means to restrict
     *                                       the use of the user profile. If running
     *                                       on an IBM i, the verification ID should
     *                                       be the DCM application ID or a similar
     *                                       value that identifies the application
     *                                       or service.
     * 
     * @param remoteIpAddress                If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       remote IP address should be obtained
     *                                       from the socket connection (i.e. using
     *                                       Socket.getInetAddress). Otherwise, null
     *                                       should be passed.
     * 
     * @param remotePort                     If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       remote port should be obtained from the
     *                                       socket connection (i.e. using
     *                                       Socket.getPort ). Otherwise, use 0 if
     *                                       there is not an associated connection.
     * 
     * @param localIpAddress                 If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       local IP address should be obtained
     *                                       from the socket connection (i.e. using
     *                                       Socket.getLocalAddress). Otherwise,
     *                                       null should be passed.
     * @param localPort                      If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       local port should be obtained from the
     *                                       socket connection
     *                                       (Socket.getLocalPort). Otherwise, use 0
     *                                       if there is not an associated
     *                                       connection.
     * 
     * 
     * @param type                           The type of token. Possible types are
     *                                       defined as fields on the
     *                                       ProfileTokenCredential class:
     *                                       <ul>
     *                                       <li>ProfileTokenCredential.TYPE_SINGLE_USE
     *                                       <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                                       <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_RENEWABLE
     *                                       </ul>
     *                                       
     * @param timeoutInterval                The number of seconds to expiration.
     * 
     * @return The token bytes.
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     */
    public static native byte[] nativeCreateToken(
            String user, 
            char[] password, 
            char[] additionalAuthenticationFactor,
            String verificationId,
            String remoteIpAddress,
            int    remotePort, 
            String localIpAddress,
            int    localPort,
            int type,
            int timeoutInterval) throws RetrieveFailedException;

   /**
    * Generates and returns a new profile token based on a user profile, password special value,
    * and additional authentication factor.   
    * 
    * @param user                           The name of the user profile for which
    *                                       the token is to be generated.
    * 
    * @param password                       The password for the user.  Must be a password
    *                                       special value. 
    * 
    * @param additionalAuthenticationFactor The additional authentication factor
    *                                       for the user
    * 
    * @param authenticationIndicator        Indicates how the caller authenticated the user.
    *                                       @see com.ibm.as400.access.AuthenticationIndicator
    * 
    * @param verificationId                 The verification ID is the label that
    *                                       identifies the specific application,
    *                                       service, or action associated with the
    *                                       profile handle request. This value must
    *                                       be 30-characters or less. This value
    *                                       will be passed to the authentication
    *                                       exit program registered under the
    *                                       QIBM_QSY_AUTH exit point if the
    *                                       specified user profile has *REGFAC as
    *                                       an authentication method. The
    *                                       authentication exit program may use the
    *                                       verification ID as a means to restrict
    *                                       the use of the user profile. If running
    *                                       on an IBM I, the verification ID should
    *                                       be the DCM application ID or a similar
    *                                       value that identifies the application
    *                                       or service.
    * 
    * @param remoteIpAddress                If the API is used by a server to
    *                                       provide access to a the system, the
    *                                       remote IP address should be obtained
    *                                       from the socket connection (i.e. using
    *                                       Socket.getInetAddress). Otherwise, null
    *                                       should be passed.
    * 
    * @param remotePort                     If the API is used by a server to
    *                                       provide access to a the system, the
    *                                       remote port should be obtained from the
    *                                       socket connection (i.e. using
    *                                       Socket.getPort ). Otherwise, use 0 if
    *                                       there is not an associated connection.
    * 
    * @param localIpAddress                 If the API is used by a server to
    *                                       provide access to a the system, the
    *                                       local IP address should be obtained
    *                                       from the socket connection (i.e. using
    *                                       Socket.getLocalAddress). Otherwise,
    *                                       null should be passed.
    * @param localPort                      If the API is used by a server to
    *                                       provide access to a the system, the
    *                                       local port should be obtained from the
    *                                       socket connection
    *                                       (Socket.getLocalPort). Otherwise, use 0
    *                                       if there is not an associated
    *                                       connection.
    * 
    * 
    * @param type                           The type of token. Possible types are
    *                                       defined as fields on the
    *                                       ProfileTokenCredential class:
    *                                       <ul>
    *                                       <li>ProfileTokenCredential.TYPE_SINGLE_USE
    *                                       <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_NON_RENEWABLE
    *                                       <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_RENEWABLE
    *                                       </ul>
    * @param timeoutInterval                The number of seconds to expiration.
    * 
    * @return The token bytes.
    * @exception RetrieveFailedException If errors occur while generating the
    *                                    token.
    */
    public static native byte[] nativeCreateTokenSpecialPassword(
            String user, 
            char[] password, 
            char[] additionalAuthenticationFactor,
            int    authenticationIndicator, 
            String verificationId,
            String remoteIpAddress,
            int    remotePort, 
            String localIpAddress,
            int    localPort,
            int type,
            int timeoutInterval) throws RetrieveFailedException;    
    
    /**
     * Attempt to swap the thread identity based on the given profile token.
     * 
     * @param token           The token bytes.
     * @param verificationId  The verification ID is the label that identifies the
     *                        specific application, service, or action associated
     *                        with the profile handle request. See verificationId
     *                        parameter of nativeCreateToken.
     * 
     * @param remoteIpAddress If the API is used by a server to provide access to a
     *                        the system, the remote IP address should be obtained
     *                        from the socket connection (i.e. using
     *                        Socket.getInetAddress). Otherwise, null should be
     *                        passed.
     * 
     * @throws SwapFailedException If errors occur while swapping thread identity.
     */
    public static native void nativeSwap(
            byte[] token,
            String verificationId,
            String remoteIpAddress) throws SwapFailedException;

    /**
     * Generate and return a new profile token based on an existing enhanced profile
     * token.
     * 
     * @param token           Token to be used to create the new token. This must be a
     *                        valid multiple use, regenerable profile token.
     *                        
     * @param verificationId  The verification ID is the label that identifies the
     *                        specific application, service, or action associated
     *                        with the profile handle request. See verificationId
     *                        parameter of nativeCreateToken.
     * 
     * @param remoteIpAddress If the API is used by a server to provide access to a
     *                        the system, the remote IP address should be obtained
     *                        from the socket connection (i.e. using
     *                        Socket.getInetAddress). Otherwise, null should be
     *                        passed.
     * 
     * @param type            The type of token. Possible types are defined as
     *                        fields on the ProfileTokenCredential class:
     *                        <ul>
     *                        <li>ProfileTokenCredential.TYPE_SINGLE_USE
     *                        <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                        <li>ProfileTokenCredential.TYPE_MULTIPLE_USE_RENEWABLE
     *                        </ul>
     *                        
     * @param timeoutInterval The number of seconds to expiration.
     * 
     * @return The token bytes.
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     */
    public static native byte[] nativeCreateTokenFromToken(
            byte[] token, 
            String verificationId,
            String remoteIpAddress,
            int type,
            int timeoutInterval) throws RetrieveFailedException;
}


