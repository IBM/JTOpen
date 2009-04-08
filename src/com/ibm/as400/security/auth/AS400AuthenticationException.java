package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400AuthenticationException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
/**
 * The AS400AuthenticationException class and subclasses
 * represent exceptions issued when errors occur
 * during system authentication.
 *
 */
public class AS400AuthenticationException extends AS400SecurityException {

    static final long serialVersionUID = 4L;

	private AS400Message[] msgList_ = null;

/**
 * Constructs an AS400AuthenticationException with
 * unknown return code.
 *
 */
AS400AuthenticationException() {
	this(UNKNOWN);
}
/**
 * Constructs an AS400AuthenticationException.
 *
 * <p> An appropriate return code is assigned based on
 * the provided messages.
 *
 * @param list
 *		The AS400Message objects to be associated
 *		with the exception.
 *
 */
AS400AuthenticationException(AS400Message[] list) {
	this(getReturnCode(list));
	msgList_ = list;
}
/**
 * Constructs an AS400AuthenticationException.
 *
 * @param rc
 *		The return code identifying the detail text
 *		to assign to the exception.
 *
 */
AS400AuthenticationException(int rc) {
	super(rc);
}
/**
 * Returns the AS400Message causing the exception.
 *
 * @return
 *		The message causing the exception; null
 *		if not available.
 *
 */
public AS400Message getAS400Message() {
	if (msgList_ != null && msgList_.length > 0)
		return msgList_[0];
	return null;
}
/**
 * Returns the list of AS400Messages causing the exception.
 *
 * @return
 *		An array of messages causing the exception;
 *		null if not available.
 *
 */
public AS400Message[] getAS400MessageList() {
	return msgList_;
}
/**
 * Returns the text associated with the return code.
 *
 * @param returnCode
 *		The return code associated with this exception.
 *
 * @return
 * 		The text string which describes the error.
 *
 */
static int getReturnCode(AS400Message[] list) {
	for (int i=0; i<list.length; i++) {
		String id = list[i].getID().toUpperCase();
		if (id.equals("CPF22E2"))		// Password not correct
			return PASSWORD_INCORRECT;
		else if (id.equals("CPF22E3"))	// User profile disabled
			return USERID_DISABLE;
		else if (id.equals("CPF22E4"))	// User profile password has expired
			return PASSWORD_EXPIRED;
		else if (id.equals("CPF22E5"))	// No password assigned to profile
			return PASSWORD_ERROR;
		else if (id.equals("CPF22E9"))	// *USE auth required to profile
			return OBJECT_AUTHORITY_INSUFFICIENT;
		else if (id.equals("CPF2204"))	// User profile not found
			return USERID_UNKNOWN;
		else if (id.equals("CPF2217"))	// Not authorized to user profile
			return OBJECT_AUTHORITY_INSUFFICIENT;
	}
	return UNKNOWN;
}
/**
 * Returns a string representation of the object. 
 *
 * @return
 *		The string representation.
 *
 */
public String toString() {
	StringBuffer sb = new StringBuffer(super.toString());
	if (getReturnCode()==UNKNOWN && getAS400Message()!=null)
		sb.append(" >> " + getAS400Message().toString());
	return sb.toString();
}
}
