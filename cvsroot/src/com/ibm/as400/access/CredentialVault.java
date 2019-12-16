///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CredentialVault.java
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
 * A vault which holds an authentication credential to one or more IBM i host servers.
 * The type of credential stored varies, depending on the sub-class implementation.
 */
abstract class CredentialVault implements Cloneable, Serializable
{
  static final boolean PASSWORD_TRACE = false;

  // Random number generator for seeds.
  static java.util.Random rng = new java.util.Random();

  /** The credential, which is always encoded before being stored */
  protected byte[] encodedCredential_;

  /**
   * A credential is encoded with an algorithm that requires a set of seeds.
   * These seeds can be generated internally or they can be provided
   * by an external source.  If they are provided by an external source,
   * then seeds to decode the credential must be provided for us to decode it.
   */
  private boolean externalSeedsWereUsed_;

  /**
   * Constructs a CredentialVault object that does not contain a credential.
   */
  protected CredentialVault() {
    this(null);
  }

  /**
   * Constructs a CredentialVault object that contains the provided credential.
   *
   * @param credential The credential to store within the vault
   */
  protected CredentialVault(byte[] credential)
  {
    if (credential == null) {
      encodedCredential_ = null;
    }
    else {
      // Never store the credential "as is"
      encodedCredential_ = store(credential);
    }
    // We have encoded the credential using internally generated seeds.
    externalSeedsWereUsed_ = false;
  }

  /**
   * Creates and returns a copy of the credential vault.  Implementation of this
   * method is specific to the type of credential vault being copied.
   * Some vaults will copy the credential itself into the new vault,
   * others will not.
   *
   * @return A newly created credential vault that is a copy of this one
   */
  public Object clone()
  {
    CredentialVault vaultClone;
    try {
      vaultClone = (CredentialVault)super.clone();
    }
    catch (CloneNotSupportedException e) {
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
    synchronized(this)
    {
      if (encodedCredential_ != null)
      {
        byte[] credentialCopy = new byte[encodedCredential_.length];
        System.arraycopy(encodedCredential_, 0, credentialCopy, 0, credentialCopy.length);
        vaultClone.encodedCredential_ = credentialCopy;
      }
      vaultClone.externalSeedsWereUsed_ = externalSeedsWereUsed_;
    }
    return vaultClone;
  }

  /**
   * Retrieves the type of credential stored in the vault.
   *
   * @return The type of credential stored in the vault.
   * Possible values are:
   * <ul>
   * <li>AS400.AUTHENTICATION_SCHEME_PASSWORD
   * <li>AS400.AUTHENTICATION_SCHEME_GSS_TOKEN
   * <li>AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
   * <li>AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN
   * <ul>
   */
  protected abstract int getType();

  /**
   * Purges the credential inside the vault.  All resources consumed by the
   * credential vault are freed.  If this method is invoked and the vault is
   * already empty, the method simply returns and no exception is thrown.
   */
  protected synchronized void empty() {
    disposeOfCredential();
  }

  // Cleans up the credential information.
  protected void finalize() throws Throwable
  {
    try {
      empty();
    }
    finally {
      super.finalize();
    }
  }

  /**
   * Queries the vault to see if it contains a credential.
   *
   * @return true if the vault does not contain a credential, false if it does
   */
  protected boolean isEmpty() {
    return (encodedCredential_ == null);
  }

  /**
   * Retrieves the unencoded credential from the vault.  Note that the
   * credential will only be "clear" in the sense that any encoding done
   * internally by the CredentialVault is removed.  Any encoding done
   * externally to the credential will remain intact.
   *
   * @return The unencoded credential from the vault.
   */
  protected synchronized byte[] getClearCredential() {
    // Make sure we have a credential to give them.
    if (isEmpty()) {
      Trace.log(Trace.ERROR, "Credential vault is empty");
      throw new ExtendedIllegalStateException("credential", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    return resolve(encodedCredential_);
  }

  /**
   * Store the credential, after encoding using the provided external seeds.
   * Any internal encoding previously done by the CredentialVault will be undone
   * prior to encoding the credential with the provided external seeds.
   *
   * @param firstSeed The first seed to use for encoding
   * @param secondSeed The second seed to use for encoding
   */
  protected synchronized void storeEncodedUsingExternalSeeds(byte[] firstSeed, byte[] secondSeed)
  {
    // If the credential was already encoded using externally-supplied seeds, then we must not encode it again.
    if (externalSeedsWereUsed_) {
      Trace.log(Trace.ERROR, "Called storeEncodedUsingExternalSeeds() when credential was already encoded using external seeds.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    else {
      externalSeedsWereUsed_ = true;
    }

    if (!isEmpty()) {
      encodedCredential_ = CredentialVault.encode(firstSeed, secondSeed, getClearCredential());
    }
  }

  /**
   * Store the credential, after encoding using internally generated seeds.
   * The credential is first decoded using the provided external seeds.
   * The decoded credential will then be encoded using internally generated seeds
   * and remain in the vault.
   *
   * @param firstSeed The first seed to use for decoding
   * @param secondSeed The second seed to use for decoding
   */
  protected synchronized void storeEncodedUsingInternalSeeds(byte[] firstSeed, byte[] secondSeed)
  {
    // If the credential was not encoded using externally-supplied seeds, then we cannot decode it.
    if (!externalSeedsWereUsed_) {
      Trace.log(Trace.ERROR, "Called storeEncodedUsingInternalSeeds() when credential was not previously encoded using external seeds.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    else {
      externalSeedsWereUsed_ = false;
    }

    if (!isEmpty()) {
      encodedCredential_ = store(CredentialVault.decode(firstSeed, secondSeed, encodedCredential_));
    }
  }

  /**
   * Disposes of the credential, thus emptying the vault.
   */
  protected void disposeOfCredential() {
    encodedCredential_ = null;
  }

  /**
   * Encodes the credential, represented as a array of bytes, using
   * internally generated random seeds.  The provided credential
   * byte array is never modified by this method.
   *
   * @param credential The credential to encode
   * @return The encoded credential
   */
  protected byte[] store(byte[] credential)
  {
    byte[] newAdder = new byte[9];
    rng.nextBytes(newAdder);

    byte[] newMask = new byte[7];
    rng.nextBytes(newMask);

    byte[] infoBytes = encode(newAdder, newMask, credential);
    byte[] returnBytes = new byte[infoBytes.length + 16];

    //
    // The format for the stored bytes is
    //
    // adder : mask : encodedBytes
    //

    // First store the adder
    System.arraycopy(newAdder, 0, returnBytes, 0, 9);

    // Next store the mask
    System.arraycopy(newMask, 0, returnBytes, 9, 7);

    // Finally, store the encoded bytes
    System.arraycopy(infoBytes, 0, returnBytes, 16, infoBytes.length);

    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "AS400 object store, bytes:", returnBytes);
    }

    return returnBytes;
  }

  /**
   * Decodes and returns the internally encoded credential.
   * The encoded credential byte array is never modified by this method.
   *
   * @return The credential stored in the vault, with all internal encoding removed
   */
  protected synchronized byte[] resolve(byte[] encodedBytes)
  {
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "AS400 object resolve:", encodedBytes);
    }

    if (encodedBytes == null)
    {
      return null;
    }

    // The first 9 bytes in the array are the adder
    byte[] adder = new byte[9];
    System.arraycopy(encodedBytes, 0, adder, 0, 9);

    // The next 7 bytes are the mask
    byte[] mask = new byte[7];
    System.arraycopy(encodedBytes, 9, mask, 0, 7);

    // And everything that is left is the encoded bytes
    byte[] infoBytes = new byte[encodedBytes.length - 16];
    System.arraycopy(encodedBytes, 16, infoBytes, 0, encodedBytes.length - 16);

    return decode(adder, mask, infoBytes);
  }

  /**
   * Encodes the given credential bytes using the provided adder and mask.
   * The encoded credential is stored and returned in a newly allocated byte array.
   * The provided credential byte array is never modified by this method.
   *
   * @param adder Used for encoding
   * @param mask Used for encoding
   * @param credential The credential to encode, represented as an array of bytes
   *
   * @return A newly allocated array of bytes representing the encoded credential
   */
  protected static byte[] encode(byte[] adder, byte[] mask, final byte[] credential)
  {
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "AS400 object encode:");
      Trace.log(Trace.DIAGNOSTIC, "     adder:", adder);
      Trace.log(Trace.DIAGNOSTIC, "     mask:", mask);
      Trace.log(Trace.DIAGNOSTIC, "     bytes:", credential);
    }
    if (credential == null) return null;
    int length = credential.length;
    byte[] buf = new byte[length];
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (byte)(credential[i] + adder[i % 9]);
    }
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (byte)(buf[i] ^ mask[i % 7]);
    }
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "     return:", buf);
    }
    return buf;
  }

  /**
   * Decodes the given credential bytes using the provided adder and mask.
   * The decoded credential is returned in a newly allocated byte array.
   * The provided credential byte array is never modified by this method.
   *
   * @param adder Used for encoding
   * @param mask Used for encoding
   * @param credential The credential to encode, represented as an array of bytes
   *
   * @return A newly allocated array of bytes representing the decoded credential
   */
  protected static byte[] decode(byte[] adder, byte[] mask, byte[] credential)
  {
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "AS400 object decode:");
      Trace.log(Trace.DIAGNOSTIC, "     adder:", adder);
      Trace.log(Trace.DIAGNOSTIC, "     mask:", mask);
      Trace.log(Trace.DIAGNOSTIC, "     bytes:", credential);
    }
    int length = credential.length;
    byte[] buf = new byte[length];
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (byte)(mask[i % 7] ^ credential[i]);
    }
    for (int i = 0; i < length; ++i)
    {
      buf[i] = (byte)(buf[i] - adder[i % 9]);
    }
    if (PASSWORD_TRACE)
    {
      Trace.log(Trace.DIAGNOSTIC, "     return:", buf);
    }
    return buf;
  }

  /**
   * Decodes the credential bytes using the provided adder and mask.
   * The decoded credential is returned in a newly allocated byte array.
   *
   * @param adder Used for encoding
   * @param mask Used for encoding
   *
   * @return A newly allocated array of bytes representing the decoded credential
   */
  protected byte[] decode(byte[] adder, byte[] mask) {
    return CredentialVault.decode(adder, mask, encodedCredential_);
  }

  /**
   * Provides a minimal amount of tracing information about the credential
   * vault in a nicely formatted string.
   *
   * @return The tracing information represented as a string
   */
  protected String trace()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("type=");
    sb.append(getType());
    sb.append(" : bytes=");
    Trace.printByteArray(sb, encodedCredential_);  // accepts null arg

    return sb.toString();
  }
}
