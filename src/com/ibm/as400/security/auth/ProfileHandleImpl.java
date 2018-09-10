package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileHandleImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

/**
 * The ProfileHandleImpl interface provides the template for
 * classes implementing behavior delegated by a
 * ProfileHandleCredential.
 *
 */
public interface ProfileHandleImpl extends AS400CredentialImpl {

/**
 * Generates and returns a profile handle based on
 * the current thread identity.
 *
 * @return
 *		The handle bytes.
 *
 * @exception RetrieveFailedException
 *		If errors occur while generating the handle.
 *
 */
byte[] getCurrentHandle() throws RetrieveFailedException;
}
