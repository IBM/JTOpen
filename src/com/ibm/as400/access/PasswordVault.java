///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  PasswordVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.AS400;

/**
 * A vault which holds a password for a user profile on an IBM i system.  The password
 * can be used for authenticating to one or more IBM i host servers.
 */
class PasswordVault extends CredentialVault implements Cloneable
{
  private static final boolean PASSWORD_TRACE = false;


  /**
   * Constructs a PasswordVault object that does not contain a credential.
   */
  protected PasswordVault() {
    super();
  }

  /**
   * Constructs a PasswordVault object that contains the provided password
   * credential.  The password is stored internally as encoded raw bytes.
   *
   * @param thePassword The password
   */
  protected PasswordVault(String thePassword) {
    super();
    encodedCredential_ = store(thePassword);
  }

  /**
   * Constructs a PasswordVault object that contains the provided password
   * credential.  The password is stored internally as encoded raw bytes.
   *
   * @param rawBytes
   */
  protected PasswordVault(byte[] rawBytes) {
    super();
    encodedCredential_ = store(rawBytes);
  }

  /**
   * Returns a copy of this PasswordVault.  The new copy will be
   * an exact copy of this vault, which means the new copy will
   * contain the same encoded password credential as this vault.
   *
   * @return A newly created PasswordVault that is a copy of this one
   */
  public Object clone() {
    PasswordVault vaultClone = (PasswordVault)super.clone();
    return vaultClone;
  }

  /**
   * {@inheritDoc}
   */
  protected int getType() {
    return AS400.AUTHENTICATION_SCHEME_PASSWORD;
  }

  /**
   * Encodes the String credential using the parent class encode method.
   * The credential string is converted into an array of bytes
   * and, using this representation, is encoded and stored internally.
   *
   * @param credential The credential to encode
   * @return The encoded credential
   */
  private byte[] store(String credential)
  {
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "AS400 object store, password: '" + credential + "'");
    }
    if (AS400.onAS400) {
      if (credential.equalsIgnoreCase("*CURRENT") ||
          credential.equals("")) {
        return null;
      }
    }

    return super.store(BinaryConverter.charArrayToByteArray(credential.toCharArray()));
  }

}
