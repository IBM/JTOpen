package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UserProfilePrincipal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
/**
 * The UserProfilePrincipal class represents an i5/OS system user profile.
 *
 * @see AS400Principal
 *
 */
public class UserProfilePrincipal extends AS400Principal implements AS400BasicAuthenticationPrincipal {


    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;



	static final private int maxLen_ = 10; // Maximum profile length

	private String userProfileName_ = "";
/**
 * Constructs a UserProfilePrincipal object.
 *
 */
public UserProfilePrincipal() {
	super();
}

/**
 * Constructs a UserProfilePrincipal object with the principal
 * name set to the supplied argument.
 *
 * @param name
 *              The user profile name.
 *
 */
 /* @A1A*/
public UserProfilePrincipal(String name) {
    super();
    try {
        setUserProfileName(name);
    }
    catch (PropertyVetoException pve) {
        AuthenticationSystem.handleUnexpectedException(pve);
    }
}

/**
 * Constructs a UserProfilePrincipal object.
 *
 * <p> The <i>system</i> and <i>name</i> properties
 * are set to the specified values.
 *
 * @param system
 *		The server associated with the principal.
 *
 * @param name
 *		The user profile name.
 */
public UserProfilePrincipal(AS400 system, String name) {
	super(system);
	try {
		setUserProfileName(name); }
	catch (PropertyVetoException pve) {
		AuthenticationSystem.handleUnexpectedException(pve); }
}
/**
 * Compares the specified Object with the credential
 * for equality.
 * 
 * @param o
 *		Object to be compared for equality.
 * 
 * @return
 *		true if equal; otherwise false.
 *
 */
public boolean equals(Object o) {
	if (o == null)
		return false;
	if (this == o)
		return true;
	if (!(o instanceof UserProfilePrincipal))
		return false;
	return
		hashCode() == ((UserProfilePrincipal)o).hashCode();
}
/**
 * Returns the user profile name.
 *
 * @return
 *    A String containing the name; empty if not assigned.
 *
 */
public String getUserProfileName() {
	return userProfileName_;
}
/**
 * Returns a hash code for this principal.
 * 
 * @return a hash code for this principal.
 * 
 */
public int hashCode() {
	if (userProfileName_ == null || userProfileName_.equals(""))
		return super.hashCode();
	int hash = userProfileName_.hashCode();
	if (getSystem() != null)
		hash += getSystem().getSystemName().hashCode();
	return hash;
}
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
public void initialize(String name) throws Exception {
	setSystem(AuthenticationSystem.localHost());
	setUserProfileName(name);
}
/**
 * Sets the user profile name.
 *
 * @param name
 *		The profile name.
 *
 * @exception PropertyVetoException
 *		If the change is vetoed.
 *
 * @exception ExtendedIllegalArgumentException
 *		If the provided value exceeds the maximum
 *		allowed length or contains non-valid
 *		characters.
 *
 */
public void setUserProfileName(String name) throws PropertyVetoException {
	// Validate parms
	if (name == null) {
	    Trace.log(Trace.ERROR, "User profile name is null");
	    throw new NullPointerException("name");
	}
	String usr = name.trim().toUpperCase();
	if (usr.length() > maxLen_) {
	    Trace.log(Trace.ERROR, "User profile name exceeds maximum length of " + maxLen_);
		throw new ExtendedIllegalArgumentException("name",
			ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
	}
	
	String old = getUserProfileName();
	fireVetoableChange("userProfileName", old, usr);
	userProfileName_ = usr;
	firePropertyChange("userProfileName", old, usr);
}
/**
 * Returns a string representation of the object
 *
 * @return a string representation of the object.
 */
public String toString() {
	return new StringBuffer(256
		).append(super.toString()
		).append('['
		).append(getUserProfileName()
		).append(']'
		).toString();
}
}
