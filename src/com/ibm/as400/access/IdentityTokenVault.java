///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  IdentityTokenVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 * A vault which holds an identity token.  The identity token can be used for
 * authenticating to one or more IBM i host servers.
 */
class IdentityTokenVault extends CredentialVault implements Cloneable, Serializable
{
  /**
   * Constructs an IdentityTokenVault object that does not contain a credential.
   */
  protected IdentityTokenVault() {
    super();
  }

  /**
   * Constructs an IdentityTokenVault object that contains the provided identity token credential.
   *
   * @param existingToken The identity token bytes
   */
  protected IdentityTokenVault(byte[] existingToken) {
    super(existingToken);
  }

  /**
   * Returns a copy of this IdentityTokenVault.  The new copy will be
   * an exact copy of this vault, which means the new copy will
   * contain the same identity token credential as this vault.
   *
   * @return A newly created IdentityTokenVault that is a copy of this one
   */
  public Object clone() {
    IdentityTokenVault vaultClone = (IdentityTokenVault)super.clone();
    return vaultClone;
  }

  /**
   * {@inheritDoc}
   */
  protected int getType() {
    return AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN;
  }

}
