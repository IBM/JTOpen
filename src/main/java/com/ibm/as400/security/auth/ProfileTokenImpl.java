package com.ibm.as400.security.auth;

import java.beans.PropertyVetoException;

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
     * @param profileTokenCred               The profile token credential to be initialized
     *                                       with the token bytes.
     * 
     * 
     * @return The profile token credential that was passed in with the token bytes initialized.
     * 
     * @exception RetrieveFailedException   If errors occur while generating the token.
     * @exception PropertyVetoException     If errors occur while setting the profile token credential.
     *
     */
    ProfileTokenCredential generateProfileToken(String uid, int pwdSpecialValue, ProfileTokenCredential profileTokenCred)
            throws RetrieveFailedException, PropertyVetoException;

    
    /**
     * Generates and returns a new profile token based on the provided information
     * using a password string and an additional authentication fatore
     *
     * @param uid             The name of the user profile for which the token is to
     *                        be generated.
     *
     * @param pwd             The user profile password (encoded). Special values
     *                        are not supported by this method.
     *
     * @param additionalAuthenticationFactor The additional authentication factor
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
     * @param enhancedInfo   Input / output.  Indicate the settings used to create an enhanced profile token.
     *                                        On output, the enhancedInfo can be examined to determine if an enhanced
     *                                        token was created. 

     *
     * @return The token bytes.
     *
     * @exception RetrieveFailedException If errors occur while generating the
     *                                    token.
     *
     */
    byte[] generateRawTokenExtended(String uid, char[] pwd, char[] additionalAuthenticationFactor, int type, int timeoutInterval, ProfileTokenEnhancedInfo enhancedInfo) throws RetrieveFailedException;
    
    /**
     * Generates and returns a new profile token based on a user profile and password.
     * 
     * @param uid                            The name of the user profile for which
     *                                       the token is to be generated.
     * 
     * @param password                       The password for the user
     * 
     * @param profileTokenCred               The profile token credential to be initialized
     *                                       with the token bytes.
     * 
     * 
     * @return The profile token credential that was passed in with the token bytes initialized.
     * 
     * @exception RetrieveFailedException   If errors occur while generating the token.
     * @exception PropertyVetoException     If errors occur while setting the profile token credential.
     */
    ProfileTokenCredential generateProfileTokenExtended(String uid, char[] password, char[] additionalAuthenticationFactor, ProfileTokenCredential profileTokenCred)
            throws RetrieveFailedException, PropertyVetoException;
    
    /**
     * Updates or extends the validity period for the credential.
     *
     * <p>
     * Generates a new profile token based on the currently established
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
