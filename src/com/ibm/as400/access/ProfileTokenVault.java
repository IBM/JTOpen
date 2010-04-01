///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ProfileTokenVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import java.io.Serializable;

/**
 * A vault which holds a profile token.  The profile token represents the credentials
 * of an IBM i user profile for a system, and can be used for authenticating
 * to one or more IBM i host servers on that system.
 */
class ProfileTokenVault extends CredentialVault implements Cloneable, Serializable
{
  /**
   * Constructs an ProfileTokenVault object that does not contain a credential
   */
  protected ProfileTokenVault() {
    super();
  }

  /**
   * Constructs an ProfileTokenVault object that contains the provided profile token credential.
   *
   * @param existingToken The profile token bytes
   */
  protected ProfileTokenVault(byte[] existingToken) {
    super(existingToken);
  }

  /**
   * Constructs an ProfileTokenVault object that contains the provided profile token credential.
   *
   * @param existingCredential The profile token credential.  The raw bytes from the profile
   *                           token object are extracted and stored in the vault.
   */
  protected ProfileTokenVault(ProfileTokenCredential existingCredential) {
    super();
    encodedCredential_ = store(existingCredential.getToken());
  }

  /**
   * Returns a copy of this ProfileTokenVault.  The new copy will be
   * an exact copy of this vault, which means the new copy will
   * contain the same profile token credential as this vault.
   *
   * @return A newly created ProfileTokenVault that is a copy of this one
   */
  public Object clone() {
    ProfileTokenVault vaultClone = (ProfileTokenVault)super.clone();
    return vaultClone;
  }

  /**
   * {@inheritDoc}
   */
  protected int getType() {
    return AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN;
  }

}
