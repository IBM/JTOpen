package com.ibm.as400.security.auth;

////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
/**
 * The ProfileTokenImpl interface provides the template for classes implementing
 * behavior delegated by a ProfileTokenCredential.
 *
 */
public interface ProfileTokenImpl extends AS400CredentialImpl
{

    /**
     * String constant for *NOPWD special value. 10 character blank padded
     */
    public final static String PW_STR_NOPWD = "*NOPWD    ";

    /**
     * String constant for *NOPWDCHK special value. 10 character blank padded
     */
    public final static String PW_STR_NOPWDCHK = "*NOPWDCHK ";

    /**
     * Generates and returns a new profile token based on the provided information.
     *
     * @deprecated As of V5R3, replaced by
     *             {@link #generateTokenExtended(String,char[],int,int)} for
     *             password strings and {@link #generateToken(String,int,int,int)}
     *             for password special values
     *
     * @param uid             The name of the user profile for which the token is to
     *                        be generated.
     *
     * @param pwd             The user profile password (encoded).
     *
     * @param type            The type of token. Possible types are defined as
     *                        fields on the ProfileTokenCredential class:
     *                        <ul>
     *                        <li>TYPE_SINGLE_USE
     *                        <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                        <li>TYPE_MULTIPLE_USE_RENEWABLE
     *                        </ul>
     *                        <p>
     *
     * @param timeoutInterval The number of seconds to expiration.
     *
     * @return The token bytes.
     *
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     *
     */
    byte[] generateToken(String uid, String pwd, int type, int timeoutInterval) throws RetrieveFailedException;

    /**
     * Generates and returns a new profile token based on the provided information
     * using a password special value.
     *
     * @param uid             The name of the user profile for which the token is to
     *                        be generated.
     *
     * @param pwdSpecialValue A password special value. Possible types are defined
     *                        as fields on the ProfileTokenCredential class:
     *                        <ul>
     *                        <li>PW_NOPWD
     *                        <li>PW_NOPWDCHK
     *                        </ul>
     *                        <p>
     *
     * @param type            The type of token. Possible types are defined as
     *                        fields on the ProfileTokenCredential class:
     *                        <ul>
     *                        <li>TYPE_SINGLE_USE
     *                        <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                        <li>TYPE_MULTIPLE_USE_RENEWABLE
     *                        </ul>
     *                        <p>
     *
     * @param timeoutInterval The number of seconds to expiration.
     *
     * @return The token bytes.
     *
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     *
     */
    byte[] generateToken(String uid, int pwdSpecialValue, int type, int timeoutInterval) throws RetrieveFailedException;

    /**
     * Generates and returns a new profile token based on the provided information
     * using a password special value.
     *
     * @param uid                            The name of the user profile for which
     *                                       the token is to be generated.
     *
     * @param pwdSpecialValue                A password special value. Possible
     *                                       types are defined as fields on the
     *                                       ProfileTokenCredential class:
     *                                       <ul>
     *                                       <li>PW_NOPWD
     *                                       <li>PW_NOPWDCHK
     *                                       </ul>
     *                                       <p>
     *                                       
     * @param additionalAuthenticationFactor The additional authentication factor
     *                                       for the user.  
     *                                       Ignored for IBM i 7.5 and older releases.
     * 
     * @param authenticationIndicator        Indicates how the caller authenticated the user.
     *                                       Ignored for IBM i 7.5 and older releases.
     *                                       @see com.ibm.as400.access.AuthenticationIndicator
     *                                       
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
     *                                       Ignored for IBM i 7.5 and older releases.
     *                                       
     * 
     * @param remoteIpAddress                If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       remote IP address should be obtained
     *                                       from the socket connection (i.e. using
     *                                       Socket.getInetAddress). Otherwise, null
     *                                       should be passed.
     *                                       Ignored for IBM i 7.5 and older releases.
     * 
     * @param remotePort                     If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       remote port should be obtained from the
     *                                       socket connection (i.e. using
     *                                       Socket.getPort ). Otherwise, use 0 if
     *                                       there is not an associated connection.
     *                                       Ignored for IBM i 7.5 and older releases.
     * 
     * @param localIpAddress                 If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       local IP address should be obtained
     *                                       from the socket connection (i.e. using
     *                                       Socket.getLocalAddress). Otherwise,
     *                                       null should be passed.
     *                                       Ignored for IBM i 7.5 and older releases.
     *                                       
     * @param localPort                      If the API is used by a server to
     *                                       provide access to a the system, the
     *                                       local port should be obtained from the
     *                                       socket connection
     *                                       (Socket.getLocalPort). Otherwise, use 0
     *                                       if there is not an associated
     *                                       connection.
     *                                       Ignored for IBM i 7.5 and older releases.
     *
     * 
     * @param type                           The type of token. Possible types are
     *                                       defined as fields on the
     *                                       ProfileTokenCredential class:
     *                                       <ul>
     *                                       <li>TYPE_SINGLE_USE
     *                                       <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                                       <li>TYPE_MULTIPLE_USE_RENEWABLE
     *                                       </ul>
     *                                       <p>
     *
     * @param timeoutInterval                The number of seconds to expiration.
     *
     * @return The token bytes.
     *
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     *
     */
    byte[] generateToken(String uid, int pwdSpecialValue, char[] additionalAuthenticationFactor, int authenticationIndicator, String verificationId,
            String remoteIpAddress, int remotePort, String localIpAddress, int localPort, int type, int timeoutInterval)
            throws RetrieveFailedException;

    /**
     * Generates and returns a new profile token based on the provided information
     * using a password string.
     *
     * @param uid             The name of the user profile for which the token is to
     *                        be generated.
     *
     * @param pwd             The user profile password (encoded). Special values
     *                        are not supported by this method.
     *
     * @param type            The type of token. Possible types are defined as
     *                        fields on the ProfileTokenCredential class:
     *                        <ul>
     *                        <li>TYPE_SINGLE_USE
     *                        <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                        <li>TYPE_MULTIPLE_USE_RENEWABLE
     *                        </ul>
     *                        <p>
     *
     * @param timeoutInterval The number of seconds to expiration.
     *
     * @return The token bytes.
     *
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     *
     */
    byte[] generateTokenExtended(String uid, char[] pwd, int type, int timeoutInterval) throws RetrieveFailedException;

    /**
     * Generates and returns a new profile token based on a user profile, password,
     * and additional authentication factor.
     * 
     * @param uid                            The name of the user profile for which
     *                                       the token is to be generated.
     * 
     * @param password                       The password for the user
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
    byte[] generateTokenExtended(String uid, char[] password, char[] additionalAuthenticationFactor, String verificationId,
            String remoteIpAddress, int remotePort, String localIpAddress, int localPort, int type, int timeoutInterval)
            throws RetrieveFailedException;

    /**
     * Updates or extends the validity period for the credential.
     *
     * <p>
     * Generates a new profile token based on the previously established
     * <i>token</i> with the given <i>type</i> and <i>timeoutInterval</i>.
     *
     * <p>
     * This method is provided to handle cases where it is desirable to allow for a
     * more restrictive type of token or a different timeout interval when a new
     * token is generated during the refresh.
     *
     * @param type            The type of token. Possible types are defined as
     *                        fields on this class:
     *                        <ul>
     *                        <li>TYPE_SINGLE_USE
     *                        <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
     *                        <li>TYPE_MULTIPLE_USE_RENEWABLE
     *                        </ul>
     *                        <p>
     *
     * @param timeoutInterval The number of seconds before expiration.
     *
     * @return The new token.
     *
     * @exception RefreshFailedException If errors occur during refresh.
     *
     */
    byte[] refresh(int type, int timeoutInterval) throws RefreshFailedException;
}
