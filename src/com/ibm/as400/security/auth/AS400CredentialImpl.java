package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400CredentialImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
/**
 * The AS400CredentialImpl interface provides the template for
 * classes implementing behavior delegated by an
 * AS400Credential.
 *
 * <p> This class should be implemented as necessary to provide
 * specific credential implementations. Each class can
 * contain credential data that may be security-sensitive.
 * It is up to each class to provide appropriate access
 * controls to sensitive data.
 *
 */
interface AS400CredentialImpl {

/**
 * Destroy or clear sensitive information maintained
 * by the credential implementation.
 * 
 * <p> Subsequent requests may result in a NullPointerException.
 *
 * @exception DestroyFailedException
 *		If errors occur while destroying or clearing
 *		credential implementation data.
 *
 */
void destroy() throws DestroyFailedException;
/**
 * Returns the number of seconds before the
 * credential is due to expire.
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
int getTimeToExpiration() throws RetrieveFailedException;
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
int getVersion();
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
boolean isCurrent();
/**
 * Updates or extends the validity period for the
 * credential.
 *
 * @exception RefreshFailedException
 *		If errors occur during refresh.
 *
 */
void refresh() throws RefreshFailedException;
/**
 * Sets the credential delegating behavior to
 * the implementation object.
 * 
 * @param credential
 *		The associated credential.
 *
 */
public void setCredential(AS400Credential credential);
/**
 * Attempts to swap the OS/400 thread identity based on this
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
AS400Credential swap(boolean genRtnCr) throws SwapFailedException;
}
