///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400BasicAuthenticationPrincipal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2005 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import java.security.Principal;

/**
 * The AS400BasicAuthenticationPrincipal interface defines i5/OS
 * principals that can be exploited by authentication services
 * that rely on basic user and password authentication.
 *
 */
public interface AS400BasicAuthenticationPrincipal extends Principal {

/**
 * Returns the user profile name.
 *
 * @return
 *    A String containing the name; empty if not assigned.
 *
 */
public String getUserProfileName();
/**
 * Initializes a principal for the local i5/OS system
 * based on the given user profile name.
 *
 * @param name
 *		The profile name.
 *
 * @exception Exception
 *		If an exception occurs.
 *
 */
public void initialize(String name) throws Exception;
}
