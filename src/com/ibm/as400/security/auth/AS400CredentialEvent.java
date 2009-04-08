package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400CredentialEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import java.util.EventObject;
/**
 * The AS400CredentialEvent class represents a credential event.
 *
 */
public class AS400CredentialEvent extends EventObject {

    static final long serialVersionUID = 4L;

	/**
	 Event ID indicating that a credential has been created.
	 **/
	public static final int CR_CREATE  = 0;
	/**
	 Event ID indicating that a credential has been destroyed.
	 **/
	public static final int CR_DESTROY = 1;
	/**
	 Event ID indicating that a credential has been refreshed.
	 **/
	public static final int CR_REFRESH = 2;
	/**
	 Event ID indicating that a credential was used to change the thread identity.
	 **/
	public static final int CR_SWAP = 3;

	private int id_; // event identifier
/**
 * Constructs a AS400CredentialEvent object.
 *
 * @param source
 *		The object where the event originated.
 *
 * @param id
 *		The event identifier.
 *
 */
  public AS400CredentialEvent(Object source, int id) {
	super(source);
	id_ = id;
  }  
/**
 * Returns the identifier for the event.
 *
 * <p> Possible identifiers are available as fields
 * on this class.
 * 
 * @return
 *		The event identifier.
 */
public int getID() {
	return id_;
}
}
