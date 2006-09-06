package com.ibm.as400.access;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Thrown when an error occurs accessing resources on the system.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class PersistenceException extends Exception {

	AS400Message[] messageList_ = null;
/**
 * Default constructor.
 */
public PersistenceException() {
	super();
}
/**
 * Constructs an exception based on a list of system messages.
 *
 * @param messageList
 *		com.ibm.as400.access.AS400Message[]
 */
public PersistenceException(AS400Message[] messageList) {
	this(messageList, "");
}
/**
 * Constructs an exception based on a list of system messages
 * and detail string.
 *
 * @param messageList
 *		com.ibm.as400.access.AS400Message[]
 * @param s
 *		java.lang.String
 */
public PersistenceException(AS400Message[] messageList, String s) {
	this(s);
	setMessageList(messageList);
}
/**
 * Constructs an exception with the given detail string.
 *
 * @param s
 *		java.lang.String
 */
public PersistenceException(String s) {
	super(s);
}
/**
 * Constructs an exception with detail provided by the given throwable.
 *
 * @param t
 *		java.lang.Throwable
 */
public PersistenceException(Throwable t) {
	this(t.toString());
}
/**
 * Returns the list of associated system messages; null if not available.
 *
 * @return
 *		com.ibm.as400.access.AS400Message[]
 */
public AS400Message[] getMessageList() {
	return messageList_;
}
/**
 * Sets the list of associated system messages.
 *
 * @param messageList
 *		com.ibm.as400.access.AS400Message[]
 */
private void setMessageList(AS400Message[] messageList) {
	messageList_ = messageList;
}
/**
 * Returns a string representation of the object.
 *
 * @return
 *		java.lang.String
 */
public String toString() {
	StringBuffer sb = new StringBuffer(super.toString());
	AS400Message[] list = getMessageList();

	if (list != null)
		for(int i=0; i<list.length; i++)
			sb.append('\n'
			 ).append("		"
			 ).append(list[i].getID()
			 ).append("  "
			 ).append(list[i].getText());
	return sb.toString();
}
}
