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
 * The ProfileTokenImpl interface provides the template for
 * classes implementing behavior delegated by a
 * ProfileTokenCredential.
 *
 */
public interface ProfileTokenImpl extends AS400CredentialImpl {

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    /**
     * String constant for *NOPWD special value. 10 character blank
     * padded
     */
    public final static String PW_STR_NOPWD    = "*NOPWD    ";

    /**
     * String constant for *NOPWDCHK special value. 10 character blank
     * padded
     */
    public final static String PW_STR_NOPWDCHK = "*NOPWDCHK ";
    
    /**
    * Generates and returns a new profile token based on
    * the provided information.
    *
    * @deprecated As of V5R3, replaced 
    * by {@link #generateTokenExtended(String,String,int,int)}
    * for password strings 
    * and {@link #generateToken(String,int,int,int)} for password
    * special values
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *
    * @param pwd
    *		The user profile password (encoded).
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *    The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    byte[] generateToken(String uid, String pwd, int type,
            int timeoutInterval) throws RetrieveFailedException;

    /**
    * Generates and returns a new profile token based on
    * the provided information using a password special value.
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *
    * @param pwdSpecialValue
    *	   A password special value.
    *      Possible types are defined as fields on the 
    *      ProfileTokenCredential class:
    *		  <ul>
    * 			<li>PW_NOPWD
    * 			<li>PW_NOPWDCHK
    *		  </ul>
    *		<p>
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *    The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    byte[] generateToken(String uid, int pwdSpecialValue, int type,
            int timeoutInterval) throws RetrieveFailedException;
 
    /**
    * Generates and returns a new profile token based on
    * the provided information using a password string.
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *
    * @param pwd
    *		The user profile password (encoded). 
    *       Special values are not supported by this method.
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *    The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    byte[] generateTokenExtended(String uid, String pwd, int type,
            int timeoutInterval) throws RetrieveFailedException;

    /**
    * Updates or extends the validity period for the credential.
    *
    * <p> Generates a new profile token based on the previously
    * established <i>token</i> with the given <i>type</i>
    * and <i>timeoutInterval</i>.
    *
    * <p> This method is provided to handle cases where it is
    * desirable to allow for a more restrictive type of token
    * or a different timeout interval when a new token is
    * generated during the refresh.
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on this class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *		The number of seconds before expiration.
    *
    * @return
    *		The new token.
    *
    * @exception RefreshFailedException
    *		If errors occur during refresh.
    *
    */
    byte[] refresh(int type, int timeoutInterval) 
            throws RefreshFailedException;
}
