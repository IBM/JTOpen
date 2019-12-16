package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400SwappableCredential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
/**
 * The AS400SwappableCredential interface defines IBM i
 * credentials that can be exploited by authentication services
 * to swap the operating system thread identity when running
 * on the local IBM i system.
 *
 */
public interface AS400SwappableCredential {

/**
 * Attempts to swap the thread identity based on the
 * credential.
 *
 * <p> No return credential is generated.
 *
 * @exception Exception
 *		If an exception occurs.
 *
 */
public void swap() throws Exception;
}
