package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileHandleImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.Trace;
/**
 * The ProfileHandleImplRemote class provides an implementation for
 * behavior delegated by a ProfileHandleCredential object.
 *
 */
class ProfileHandleImplRemote extends AS400CredentialImplRemote implements ProfileHandleImpl {

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
/**
 * Generates and returns a profile handle based on
 * the current OS/400 thread identity.
 *
 * <p> The remote implementation always throws an
 * exception. The ProfileHandleCredential has little use
 * in remote environments and is introduced in a
 * limited capacity to support reestablishing thread
 * identity after performing a swap based on another
 * credential. Swapping the OS/400 thread ID is not
 * supported in remote environments.
 *
 * @return
 *		The handle bytes.
 *
 * @exception RetrieveFailedException
 *		If errors occur while generating the handle.
 *
 */
public byte[] getCurrentHandle() throws RetrieveFailedException {
	Trace.log(Trace.ERROR, "Unsupported remote operation");
	throw new RetrieveFailedException(
		RetrieveFailedException.REQUEST_NOT_SUPPORTED);
}
}
