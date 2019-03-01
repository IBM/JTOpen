///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  GSSTokenVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * A vault which holds a GSS Token.  The token can be used for authenticating
 * to one or more IBM i host servers.
 */
class GSSTokenVault extends CredentialVault implements Cloneable
{
  /**
   * Constructs a GSSTokenVault object that does not contain a credential
   */
  protected GSSTokenVault() {
    super();
  }

  /**
   * Constructs a GSSTokenVault object that contains the provided GSS Token credential.
   *
   * @param tokenBytes The GSS Token bytes
   */
  protected GSSTokenVault(byte[] tokenBytes) {
    super();
    encodedCredential_ = tokenBytes;
  }

  /**
   * Returns a copy of this GSSTokenVault.  The new copy will be
   * an exact copy of this vault, which means the new copy will
   * contain the same identity token credential as this vault.
   *
   * @return A newly created IdentityTokenVault that is a copy of this one
   */
  public Object clone() {
    GSSTokenVault vaultClone = (GSSTokenVault)super.clone();
    return vaultClone;
  }

  /**
   * {@inheritDoc}
   */
  protected int getType() {
    return AS400.AUTHENTICATION_SCHEME_GSS_TOKEN;
  }

  /**
   * {@inheritDoc}
   */
  protected synchronized byte[] getClearCredential() {
    // GSS token bytes are never encoded or clear, so just return the token.
    return encodedCredential_;
  }

  /**
   * {@inheritDoc}
   */
  protected synchronized void storeEncodedUsingExternalSeeds(byte[] firstSeed, byte[] secondSeed) {
    // GSS token bytes are never encoded, so nothing to do.
  }

  /**
   * {@inheritDoc}
   */
  protected synchronized void storeEncodedUsingInternalSeeds(byte[] firstSeed, byte[] secondSeed) {
    // GSS token bytes are never encoded, so nothing to do.
  }

  /**
   * {@inheritDoc}
   */
  protected byte[] store(byte[] credential) {
    // GSS token bytes are never encoded, so just store them directly.
    encodedCredential_ = credential;
    return credential;
  }

  /**
   * {@inheritDoc}
   */
  protected synchronized byte[] resolve(byte[] info) {
    // GSS token bytes are never encoded or clear, so just return the token.
    return info;
  }

}
