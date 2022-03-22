package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400CredentialImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
/**
 * The AS400CredentialImplRemote class provides the
 * default implementation for most behavior delegated by
 * an AS400Credential with no native optimization.
 *
 * <p> This class should be subclassed as necessary to
 * provide specific credential implementations. Each class
 * can contain credential data that may be security-sensitive.
 * It is up to each class to provide appropriate access
 * controls to sensitive data.
 *
 */
class AS400CredentialImplRemote implements AS400CredentialImpl {

	private AS400Credential credential_ = null;
/**
 * Constructs an implementation object to receive
 * delegated credential behavior.
 *
 * <p> The <i>credential</i> property must be set prior to
 * accessing host information or invoking actions.
 *
 */
AS400CredentialImplRemote() {
	super();
}
/**
 * Destroy or clear sensitive information maintained
 * by the credential implementation.
 * 
 * <p> Subsequent requests may result in a NullPointerException.
 *
 * <p> Subclasses should override as necessary to
 * destroy or clear class-specific data.
 *
 * @exception DestroyFailedException
 *		If errors occur while destroying or clearing
 *		credential implementation data.
 *
 */
public void destroy() throws DestroyFailedException {
	credential_ = null;
	if (Trace.isTraceOn())
		Trace.log(Trace.INFORMATION,
			new StringBuffer("Credential implementation destroyed >> "
				).append(toString()).toString());
}
/**
 * Returns the credential delegating behavior to
 * the implementation object.
 * 
 * @return
 *		The associated credential.
 *
 */
AS400Credential getCredential() {
	return credential_;
}
/**
 * Returns the number of seconds before the
 * credential is due to expire.
 *
 * <p> Subclasses implementing timed credentials
 * must override.
 *
 * @return
 *		The number of seconds before expiration;
 *		zero (0) if already expired.
 *
 * @exception RetrieveFailedException
 *		If errors occur while retrieving
 *		timeout information.
 *
 */
public int getTimeToExpiration() throws RetrieveFailedException {
	Trace.log(Trace.ERROR, "Subclass must provide getTimeToExpiration() implementation");
	throw new RetrieveFailedException(
		RetrieveFailedException.REQUEST_NOT_SUPPORTED);
}
/**
 * Returns the version number for the implementation.
 *
 * <p> Used to ensure the implementation is valid for
 * specific functions.
 *
 * @return
 *		The version number.
 *
 */
public int getVersion() {
	return 1; // mod 3
}
/**
 * Indicates if the credential is still considered valid
 * for authenticating to associated server services
 * or performing related actions.
 *
 * <p> An exception is not thrown on failure to remain
 * consistent with the Refreshable interface (even
 * though some credential classes currently avoid the
 * dependency established by implementing the
 * interface).
 *
 * @return
 *		true if valid; false if not valid or if the
 *		operation fails.
 *
 */
public boolean isCurrent() {
	try {
		return (!getCredential().isTimed()
			|| getTimeToExpiration()>0);
	}
	catch (RetrieveFailedException e) {
		Trace.log(Trace.ERROR, "Unable to retrieve credential time to expiration", e);
		return false;
	}
}
/**
 * Updates or extends the validity period for the
 * credential.
 *
 * <p> Subclasses implementing renewable credentials
 * must override.
 *
 * @exception RefreshFailedException
 *		If errors occur during refresh.
 *
 */
public void refresh() throws RefreshFailedException {
	Trace.log(Trace.ERROR, "Subclass must provide refresh() implementation");
	throw new RefreshFailedException(
		RefreshFailedException.REQUEST_NOT_SUPPORTED);
}
/**
 * Sets the credential delegating behavior to
 * the implementation object.
 * 
 * @param credential
 *		The associated credential.
 *
 */
public void setCredential(AS400Credential credential) {
	if (credential == null) {
		Trace.log(Trace.ERROR, "Parameter 'credential' is null.");
		throw new NullPointerException("credential");
	}
	credential_ = credential;
}
/**
 * Attempts to swap the IBM i thread identity based on this
 * credential.
 *
 * @param genRtnCr
 *		Indicates whether a return credential should be
 *		generated, even if supported. When appropriate,
 *		not generating a return credential can
 *		improve performance and avoid potential problems
 *		in creating the credential.
 *
 * @return
 *		A credential capable of swapping back to
 *		the original identity; classes not supporting
 *		this capability will return null. This value
 *		will also be null if genRtnCr is false.
 *
 * @exception SwapFailedException
 *		If errors occur while swapping thread identity.
 *
 * @exception SecurityException
 *		If the caller does not have permission to
 *		modify the OS thread identity.
 *
 */
public AS400Credential swap(boolean genRtnCr) throws SwapFailedException {
	Trace.log(Trace.ERROR, "Unsupported remote operation");
	throw new SwapFailedException(
		SwapFailedException.REQUEST_NOT_SUPPORTED);
}
}
