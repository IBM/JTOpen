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
    //encodedCredential_ = store(existingCredential.getToken());
    storeProfileTokenCredential(existingCredential);
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
  
  public void storeProfileTokenCredential(ProfileTokenCredential credential) {
    this.profileTokenCrendential_ = credential; //@D3C cache profile token credential in a vault
  }
  
  private ProfileTokenCredential profileTokenCrendential_;

  //@D3A - Start
  /**
   * Retrieves unencoded credential from a vault
   */
  protected synchronized byte[] getClearCredential() {
    if (profileTokenCrendential_!=null){
      encodedCredential_ = store(profileTokenCrendential_.getToken());
      return resolve(encodedCredential_);
    } else {
      return super.getClearCredential();
    }
  }
  
  /**
   * Block the thread to refresh profile token credential from the vault.
   */
  public void preventRefresh() {
    if (profileTokenCrendential_!=null){
      try {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault preventRefresh called");
        profileTokenCrendential_.preventRefresh();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Notify the wait thread to refresh profile token credential from the vault.
   */
  public void allowRefresh() {
    if (profileTokenCrendential_!=null){
      if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault allowRefresh called");
      profileTokenCrendential_.allowRefresh();
    }
  }
  
  /**
   * Queries the vault to see if it contains a profile token credential.
   */
  protected boolean isEmpty() {
    return (profileTokenCrendential_ == null);
  }
  
  //@D3A - End
}
