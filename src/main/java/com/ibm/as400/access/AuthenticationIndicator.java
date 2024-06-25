///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Authentication Indicator.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2024-2024 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 The authentication indicator specifies the reason why an authentication request is being made using a 
 password special value. 
 */
public class AuthenticationIndicator {
	/** Indicates that a successful Kerberos authentication was previously performed. */
	public final static int KERBEROSE_PREVIOUSLY_PERFORMED = 1; 
	/** Indicates that a successful SSH authentication with a key was previously performed. */
	public final static int SSH_KEY_PREVIOUSLY_PERFORMED = 2; 
	/** Indicates that a successful identity token mapping to a user profile was previously performed. */
	public final static int IDENTITY_TOKEN_MAPPING_PREVIOUSLY_PERFORMED = 3; 
	/** Indicates that a successful application authentication was previously performed. */ 
	public final static int APPLICATION_AUTHENTICATION_PREVIOUSLY_PERFORMED = 4;  

}
