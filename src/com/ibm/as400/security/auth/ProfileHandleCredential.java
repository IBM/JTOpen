package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileHandleCredential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
/**
 * The ProfileHandleCredential class represents an iSeries system profile handle.
 *
 * <p> This credential does not support all possible behavior
 * for iSeries system profile handles. It is provided to fill a secondary
 * role in support of other credentials when running on the
 * local iSeries system. A profile handle credential provides the ability
 * to store the current OS/400 thread identity and restore that
 * identity after performing a swap based on another
 * credential (i.e. ProfileTokenCredential).
 *
 * @see AS400Credential
 * @see ProfileTokenCredential
 *
 */
public final class ProfileHandleCredential extends AS400Credential {

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    static final long serialVersionUID = 4L;


	private byte[] handle_ = null;

	/**
	 Indicates the length of a profile handle (in bytes)
	 **/
	public static int HANDLE_LENGTH = 12;
/**
 * Constructs a ProfileHandleCredential object.
 *
 */
public ProfileHandleCredential() {
	super();
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
	if (!(o instanceof ProfileHandleCredential))
		return false;
	return
		hashCode() == ((ProfileHandleCredential)o).hashCode();
}
/**
 * Returns the actual bytes for the handle as it exists
 * on the iSeries system.
 *
 * @return
 *    The handle bytes; null if not set.
 *
 */
public byte[] getHandle() {
	return handle_;
}
/**
 * Returns a hash code for this credential.
 * 
 * @return a hash code for this credential.
 * 
 */
public int hashCode() {
	int hash = 19913;
	if (handle_ != null)
		for (int i=0; i<handle_.length; i++)
			hash ^= (int)handle_[i];
	hash ^= (isPrivate() ? 13431 : 14427);
	if (getPrincipal() != null)
		hash ^= getPrincipal().hashCode();
	if (getSystem() != null)
		hash ^= getSystem().getSystemName().hashCode();
	return hash;
}
/**
 * Returns the name of the class providing an implementation
 * for code delegated by the credential that performs native
 * optimization when running on an iSeries system.
 *
 * @return
 *		The qualified class name for native optimizations;
 *		null if not applicable.
 *
 */
String implClassNameNative() {
	return "com.ibm.as400.access.ProfileHandleImplNative";
}
/**
 * Returns the name of the class providing an implementation
 * for code delegated by the credential when no native
 * optimization is to be performed.
 *
 * @return
 *		The qualified class name.
 *
 */
String implClassNameRemote() {
	return "com.ibm.as400.security.auth.ProfileHandleImplRemote";
}
/**
 * Initializes transient data.
 *
 */
void initTransient() {
	super.initTransient();
	handle_ = null;
}
/**
 * Reset the value of all properties used to define
 * the credential.
 * 
 * <p> These are the values initialized prior to
 * accessing host information for or taking action against
 * the credential and not modified thereafter until
 * the credential is destroyed.
 *
 */
void invalidateProperties() {
	super.invalidateProperties();
	handle_ = null;
}
/**
 * Sets the handle based on the current OS/400 thread identity.
 *
 * <p> The <i>system</i> property must be set prior to
 * invoking this method.
 *
 * <p> If successful, this method results in a new profile
 * handle being created on the iSeries system.
 *
 * <p> This property cannot be changed once a request
 * initiates a connection for the object to the
 * iSeries system.
 *
 * @exception AS400SecurityException
 *		If an iSeries system security or authentication error occurs.
 *
 * @exception PropertyVetoException
 *		If the change is vetoed.
 *
 * @exception ExtendedIllegalStateException
 *		If the token cannot be initialized due
 *		to the current state.
 *
 */
public void setHandle() throws PropertyVetoException, AS400SecurityException {
	// Validate state
	validatePropertySet("system", getSystem());
	// Instantiate a new impl but do not yet set as the default impl_
	ProfileHandleImpl impl = (ProfileHandleImpl)getImplPrimitive();
	// Generate and set the handle value
	setHandle(impl.getCurrentHandle());
	// If successful, all defining attributes are now set.
	// Set the impl for subsequent references.
	setImpl(impl);
	// Indicate that a new handle was created.
	fireCreated();
}
/**
 * Sets the actual bytes for the handle as it exists
 * on the iSeries system.
 *
 * <p> This method allows a credential to be constructed
 * based on an existing handle (i.e. previously created using the
 * QSYGETPH system API).
 *
 * <p> This property cannot be changed once a request
 * initiates a connection for the object to the
 * iSeries system.
 *
 * @param bytes
 *		The handle bytes.
 *
 * @exception PropertyVetoException
 *		If the change is vetoed.
 *
 * @exception ExtendedIllegalArgumentException
 *		If the provided value exceeds the maximum
 *		allowed length.
 *
 * @exception ExtendedIllegalStateException
 *		If the property cannot be changed due
 *		to the current state.
 *
 */
public void setHandle(byte[] bytes) throws PropertyVetoException {
	// Validate state
	validatePropertyChange("handle");

	// Validate parms
	if ((bytes != null) && (bytes.length != HANDLE_LENGTH)) {
	    Trace.log(Trace.ERROR, "Handle of length " + bytes.length + " not valid ");
	    throw new ExtendedIllegalArgumentException(
		    "bytes", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
	}

	byte[] old = getHandle();
	fireVetoableChange("handle", old, bytes);
	handle_ = bytes;
	firePropertyChange("handle", old, bytes);
}
/**
 * Indicates if instances of the class are sufficient
 * by themselves to change the OS thread identity.
 *
 * <p> Typically this behavior is dictated by the type
 * of credential and need not be changed for
 * individual instances.
 * 
 * @return
 *		true
 *
 */
boolean typeIsStandalone() {
	return true;
}
/**
 * Validates that all properties required to define the
 * credential have been set.
 * 
 * <p> These are the values initialized prior to
 * accessing host information for or taking action against
 * the credential and not modified thereafter until
 * the credential is destroyed.
 *
 * @exception ExtendedIllegalStateException
 *		If a required property is not set.
 *
 */
void validateProperties() {
	super.validateProperties();
	validatePropertySet("handle", getHandle());
}
}
