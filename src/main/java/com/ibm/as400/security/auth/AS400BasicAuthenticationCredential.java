package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400BasicAuthenticationCredential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
/**
 * The AS400BasicAuthenticationCredential interface defines IBM i credentials
 * that can be exploited by authentication services that rely on basic user and
 * password authentication.
 *
 */
public interface AS400BasicAuthenticationCredential
{

    /**
     * Returns text that can be displayed to prompt for the basic user and password
     * information used to initialize the credential.
     *
     * @return An array of two Strings. The first string is the text to prompt for
     *         the user name; the second is the text to prompt for the password.
     *
     */
    public String[] basicAuthenticationPrompt();

    /**
     * Initializes and validates a credential for the local IBM i system.
     *
     * @param principal       The principal identifying the authenticated user.
     *
     * @param password        The password for the authenticated user.
     *
     * @param isPrivate       Indicates whether the credential is considered
     *                        private.
     *
     * @param isReusable      true if the credential can be used to swap thread
     *                        identity multiple times; otherwise false.
     *
     * @param isRenewable     true if the validity period of the credential can be
     *                        programmatically updated or extended; otherwise false.
     *
     * @param timeoutInterval The number of seconds to expiration when the
     *                        credential is initially created; ignored if the
     *                        credential does not expire based on time.
     *
     * @exception Exception If an exception occurs.
     *
     * @deprecated Use
     *             {{@link #initialize(AS400BasicAuthenticationPrincipal, char[], boolean, boolean, boolean, int)}
     *             instead
     */
    @Deprecated
    public void initialize(AS400BasicAuthenticationPrincipal principal, String password, boolean isPrivate,
            boolean isReusable, boolean isRenewable, int timeoutInterval) throws Exception;

    /**
     * Initializes and validates a credential for the local IBM i system.
     *
     * @param principal       The principal identifying the authenticated user.
     *
     * @param password        The password for the authenticated user.
     *
     * @param isPrivate       Indicates whether the credential is considered
     *                        private.
     *
     * @param isReusable      true if the credential can be used to swap thread
     *                        identity multiple times; otherwise false.
     *
     * @param isRenewable     true if the validity period of the credential can be
     *                        programmatically updated or extended; otherwise false.
     *
     * @param timeoutInterval The number of seconds to expiration when the
     *                        credential is initially created; ignored if the
     *                        credential does not expire based on time.
     *
     * @exception Exception If an exception occurs.
     *
     */
    public void initialize(AS400BasicAuthenticationPrincipal principal, char[] password, boolean isPrivate,
            boolean isReusable, boolean isRenewable, int timeoutInterval) throws Exception;

    /**
     * Initializes and validates a credential for the local IBM i system.
     *
     * @param principal               The principal identifying the authenticated
     *                                user.
     *
     * @param password                The password for the authenticated user.
     * 
     * @param additionalAuthFactor    The additional authentication factor for the
     *                                user
     * @param authenticationIndicator Indicates how the caller authenticated the
     *                                user. Ignored for IBM i 7.5 and older
     *                                releases.  @see com.ibm.as400.access.AuthenticationIndicator
     * 
     * @param verificationID  The verification ID is the label that identifies the
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
     *                        identifies the application or service. Ignored for IBM
     *                        i 7.5 and older releases.
     * 
     * @param remoteIPAddress If the API is used by a server to provide access to a
     *                        the system, the remote IP address should be obtained
     *                        from the socket connection (i.e. using
     *                        Socket.getInetAddress). Otherwise, null should be
     *                        passed. Ignored for IBM i 7.5 and older releases.
     * 
     * @param remotePort      If the API is used by a server to provide access to a
     *                        the system, the remote port should be obtained from
     *                        the socket connection (i.e. using Socket.getPort ).
     *                        Otherwise, use 0 if there is not an associated
     *                        connection. Ignored for IBM i 7.5 and older releases.
     * 
     * @param localIPAddress  If the API is used by a server to provide access to a
     *                        the system, the local IP address should be obtained
     *                        from the socket connection (i.e. using
     *                        Socket.getLocalAddress). Otherwise, null should be
     *                        passed. Ignored for IBM i 7.5 and older releases.
     * 
     * @param localPort       If the API is used by a server to provide access to a
     *                        the system, the local port should be obtained from the
     *                        socket connection (Socket.getLocalPort). Otherwise,
     *                        use 0 if there is not an associated connection.
     *                        Ignored for IBM i 7.5 and older releases.
     *
     * @param isPrivate       Indicates whether the credential is considered
     *                        private.
     *
     * @param isReusable      true if the credential can be used to swap thread
     *                        identity multiple times; otherwise false.
     *
     * @param isRenewable     true if the validity period of the credential can be
     *                        programmatically updated or extended; otherwise false.
     *
     * @param timeoutInterval The number of seconds to expiration when the
     *                        credential is initially created; ignored if the
     *                        credential does not expire based on time.
     * 
     * @exception Exception If an exception occurs.
     *
     */
    public void initialize(AS400BasicAuthenticationPrincipal principal, char[] password, char[] additionalAuthFactor,
            int authenticationIndicator, String verificationID, String remoteIPAddress, int remotePort,
            String localIPAddress, int localPort, boolean isPrivate, boolean isReusable, boolean isRenewable,
            int timeoutInterval) throws Exception;

    /**
     * Indicates whether the credential is considered private.
     *
     * <p>
     * This value can be referenced by authentication services as an indication of
     * when to check permissions or otherwise protect access to sensitive
     * credentials.
     *
     * @return true if private; false if public.
     */
    public boolean isPrivate();
}
