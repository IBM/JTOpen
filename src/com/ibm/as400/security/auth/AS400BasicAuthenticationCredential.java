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
 * The AS400BasicAuthenticationCredential interface defines iSeries
 * credentials that can be exploited by authentication services
 * that rely on basic user and password authentication.
 *
 */
public interface AS400BasicAuthenticationCredential {

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
/**
 * Returns text that can be displayed to prompt for the basic user
 * and password information used to initialize the credential.
 *
 * @return
 *		An array of two Strings. The first string is the text to
 *		prompt for the user name; the second is the text to
 *		prompt for the password.
 *
 */
public String[] basicAuthenticationPrompt();
/**
 * Initializes and validates a credential for the local iSeries system.
 *
 * @param principal
 *		The principal identifying the authenticated user.
 *
 * @param password
 *		The password for the authenticated user.
 *
 * @param isPrivate
 * 		Indicates whether the credential is considered private.
 *
 * @param isReusable
 *		true if the credential can be used to swap
 *		OS/400 thread identity multiple times;
 *		otherwise false.
 *
 * @param isRenewable
 *		true if the validity period of the credential
 *		can be programmatically updated or extended;
 *		otherwise false.
 *
 * @param timeoutInterval
 * 		The number of seconds to expiration when the credential
 *		is initially created; ignored if the credential
 *		does not expire based on time.
 *
 * @exception Exception
 *		If an exception occurs.
 *
 */
public void initialize(AS400BasicAuthenticationPrincipal principal, String password,
	boolean isPrivate, boolean isReusable, boolean isRenewable,
	int timeoutInterval) throws Exception;
/**
 * Indicates whether the credential is considered private.
 *
 * <p> This value can be referenced by authentication services
 * as an indication of when to check permissions or otherwise
 * protect access to sensitive credentials.
 *
 * @return
 *		true if private; false if public.
 */
public boolean isPrivate();
}
