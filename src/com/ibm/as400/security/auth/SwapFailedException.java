package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SwapFailedException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

import com.ibm.as400.access.AS400Message;
/**
 * The SwapFailedException class represents an exception
 * issued when errors occur while attempting to change
 * thread identity on the IBM i system.
 *
 * <p> If available, one or more AS400Message objects
 * may be included in the exception.
 */
public class SwapFailedException extends AS400AuthenticationException {

    static final long serialVersionUID = 4L;



/**
 * Constructs a SwapFailedException.
 *
 */
SwapFailedException() {
	super();
}
/**
 * Constructs a SwapFailedException.
 *
 * @param list
 *		The AS400Message objects to be associated
 *		with the exception.
 *
 */
SwapFailedException(AS400Message[] list) {
	super(list);
}
/**
 * Constructs a SwapFailedException.
 *
 * @param rc
 *		The return code identifying the detail text
 *		to assign to the exception.
 *
 */
SwapFailedException(int rc) {
	super(rc);
}
}
