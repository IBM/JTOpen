package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DestroyFailedException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400Message;
/**
 * The DestroyFailedException class represents an exception
 * issued when error occur when destroying IBM i system
 * authentication information.
 *
 * <p> If available, one or more AS400Message objects may
 * be included in the exception.
 *
 */
public class DestroyFailedException extends AS400AuthenticationException {

    static final long serialVersionUID = 4L;


/**
 * Constructs a DestroyFailedException.
 *
 */
DestroyFailedException() {
	super();
}
/**
 * Constructs a DestroyFailedException.
 *
 * @param list
 *		The AS400Message objects to be associated
 *		with the exception.
 *
 */
DestroyFailedException(AS400Message[] list) {
	super(list);
}
/**
 * Constructs a DestroyFailedException.
 *
 * @param rc
 *		The return code identifying the detail text
 *		to assign to the exception.
 *
 */
DestroyFailedException(int rc) {
	super(rc);
}
}
