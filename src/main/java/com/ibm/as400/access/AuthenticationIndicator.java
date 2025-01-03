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
 password special value.  This should indicate the type of authentication that was performed by the caller.
 Most applications will probably use APPLICATION_AUTHENTICATION.  
 */
public class AuthenticationIndicator {
	/** Indicates that Kerberos was used to authenticate the user. */
	public final static int KERBEROS = 1; 
	/** Indicates that a successful SSH authentication with a key was used to authenticate the user. */
	public final static int SSH_KEY = 2; 
	/** Indicates that a successful identity token mapping to a user profile was used to authenticate the user. */
	public final static int IDENTITY_TOKEN_MAPPING = 3; 
	/** Indicates that DDM (Distributed Data Management) performed user ID only authentication */ 
	public final static int DDM_USER_ID = 4; 
	/** Indicates that a successful application authentication was previously performed. This is used when the
	 * application has assumed full responsibility for authenticating the user. */ 
	public final static int APPLICATION_AUTHENTICATION = 5;  

}
