///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ProfileTokenProvider.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import com.ibm.as400.access.AS400SecurityException;

/**
 * Defines an interface for providing a {@link ProfileTokenCredential ProfileTokenCredential} to an {@link com.ibm.as400.access.AS400 AS400} object.
 * This interface is used when an AS400 object is given the responsibility of managing the life cycle of
 * a user's profile token credential.  Management of this credential by the AS400 object may require a new
 * profile token to be created; this is accomplished via a class that implements this interface.
 *
 * @see com.ibm.as400.access.AS400#AS400(String, ProfileTokenProvider)
 * @see com.ibm.as400.access.AS400#AS400(String, ProfileTokenProvider, int)
 */
public interface ProfileTokenProvider
{
  /**
   * Creates and returns a new profile token credential.  The attributes
   * of the profile token, such as timeout interval, user, etc are
   * determined by the class that implements this interface.
   *
   * @return A newly created profile token credential.
   * @throws AS400SecurityException If an IBM i system security or authentication error occurs
   */
  public ProfileTokenCredential create() throws AS400SecurityException;
}
