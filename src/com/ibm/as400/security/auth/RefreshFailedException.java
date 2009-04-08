package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RefreshFailedException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400Message;
/**
 * The RefreshFailedException class represents an exception
 * issued when errors occur while refreshing system
 * authentication information.
 *
 * <p> If available, one or more AS400Message objects may
 * be included in the exception.
 *
 */
public class RefreshFailedException extends AS400AuthenticationException {

    static final long serialVersionUID = 4L;



/**
 * Constructs a RefreshFailedException.
 *
 */
RefreshFailedException() {
	super();
}
/**
 * Constructs a RefreshFailedException.
 *
 * @param list
 *		The AS400Message objects to be associated
 *		with the exception.
 *
 */
RefreshFailedException(AS400Message[] list) {
	super(list);
}
/**
 * Constructs a RefreshFailedException.
 *
 * @param rc
 *		The return code identifying the detail text
 *		to assign to the exception.
 *
 */
RefreshFailedException(int rc) {
	super(rc);
}
}
