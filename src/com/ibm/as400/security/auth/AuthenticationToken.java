package com.ibm.as400.security.auth;

import com.ibm.eim.*;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.InternalErrorException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.text.ParseException;

/**
 * Represents an authentication token.
 **/
public final class AuthenticationToken implements Serializable {

  // For sanity-checking when parsing a byte array into a new AuthenticationToken object.
  // The total length (bytes) of all fixed fields for the smallest possible AuthToken.
  private static final int FIXED_FIELDS_LENGTH = SignatureHeader.FIXED_FIELDS_LENGTH + TokenManifest.FIXED_FIELDS_LENGTH + UserToken.FIXED_FIELDS_LENGTH;

  static final int TOKEN_VERSION_1 = 1;      // version of the token layout

  private SignatureHeader signatureHeader_;  // latest signature header
  private TokenManifest tokenManifest_;      // latest token manifest
  private byte[] priorManifests_ = null;     // prior token manifests (from delegations)
  private UserToken userToken_;

  AuthenticationToken(SignatureHeader signatureHeader, TokenManifest tokenManifest, byte[] priorManifests, UserToken userToken)
  {
    // Assume caller has validated args.
    signatureHeader_ = signatureHeader;
    tokenManifest_ = tokenManifest;
    priorManifests_ = priorManifests;
    userToken_ = userToken;
  }

  byte[] getPriorManifests()
  {
    return priorManifests_;
  }

  // For debugging.
  int getLength()
  {
    int length = signatureHeader_.getLength() + tokenManifest_.getLength() + userToken_.getLength();
    if (priorManifests_ != null) length += priorManifests_.length;
    return length;
  }


  UserToken getUserToken()
  {
    return userToken_;
  }

  // Prepends a new Signature Header and Token Manifest to the token.
  // This is done when delegating a token.
  void addNewSignatureAndManifest(SignatureHeader sigHeader, TokenManifest manifest) throws IOException
  {
    // Assume caller has validated args.

    // Merge current (old) sigHeader/tokenManifest into the priorManifests array.
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
    signatureHeader_.writeTo(outStream);
    tokenManifest_.writeTo(outStream);
    if (priorManifests_ != null) {
      outStream.write(priorManifests_, 0, priorManifests_.length);
    }
    priorManifests_ = outStream.toByteArray();

    signatureHeader_ = sigHeader;
    tokenManifest_ = manifest;
  }

  SignatureHeader getSignatureHeader()
  {
    return signatureHeader_;
  }

  TokenManifest getManifest()
  {
    return tokenManifest_;
  }


  /**
   Converts this authentication token into a new byte array, which can then be sent in a datastream.
   @return The authentication token in the form of a byte array.
   **/
  public byte[] toBytes()
  {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

    try
    {
      signatureHeader_.writeTo(outStream);
      tokenManifest_.writeTo(outStream);
      if (priorManifests_ != null) {
        outStream.write(priorManifests_, 0, priorManifests_.length);
      }
      userToken_.writeTo(outStream);
      return outStream.toByteArray();
    }
    catch (IOException e) {
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
  }


  /**
   Parses the byte-array argument into a new AuthenticationToken object.
   **/
  public static AuthenticationToken parse(byte[] tokenBytes)  // TBD: Leave this public for testing; make it private before shipping???
    throws IOException, ParseException, EimException
  {
    if (tokenBytes == null) throw new NullPointerException("tokenBytes");

    // Sanity-check the array length.  It should accommodate the smallest possible token.
    if (tokenBytes.length < FIXED_FIELDS_LENGTH) {
      throw new ParseException("The specified array is too short to contain an authentication token.", 0);
    }

    // Sanity-check the "token total length" field.  Its value should exactly match the length of the array.
    int totalLength = BinaryConverter.byteArrayToInt(tokenBytes,SignatureHeader.OFFSET_TO_TOKEN_LENGTH);
    if (totalLength != tokenBytes.length) {
      throw new ParseException("The value of the \"length\" field ("+totalLength+") does not match the length of the array ("+tokenBytes.length+").", SignatureHeader.OFFSET_TO_TOKEN_LENGTH);
    }

    // Get a sneak preview of the length and offset of the User Token.
    // This is stored in the final 4 bytes of tokenBytes.
    int userTokenLength = BinaryConverter.byteArrayToInt(tokenBytes,totalLength-4);
    int userTokenOffset = totalLength - userTokenLength;

    // Note: ByteArrayInputStream0 provides a getter for the protected "pos" (position) field of ByteArrayInputStream.
    ByteArrayInputStream0 inStream = new ByteArrayInputStream0(tokenBytes);

    try
    {
      // Parse the Signature Header.
      SignatureHeader sigHeader = SignatureHeader.parse(inStream);

      TokenManifest latestManifest = TokenManifest.parse(inStream);
      int counter = latestManifest.getCounter();

      if (counter == 1) {  // This authToken has a single manifest.
        // Parse the User Token.
        UserToken userToken = UserToken.parse(inStream);
        // Compose a new Auth Token.
        return new AuthenticationToken(sigHeader, latestManifest, null, userToken);
      }
      else { // This authToken has some intermediate manifests.

        // We don't bother parsing the intermediate manifests, we don't care about their contents.
        // Just save them as a byte array, and shortcut to the User Token and parse that.

        // Consume the bytes representing the intermediate manifests.
        int manifestsLength = userTokenOffset - inStream.getPos(); // number of bytes
        byte[] priorManifests = new byte[manifestsLength];
        int bytesCopied = inStream.read(priorManifests, 0, manifestsLength);
        if (Trace.isTraceOn() && bytesCopied != manifestsLength) {
          Trace.log(Trace.ERROR, "Manifests length == " + manifestsLength + "; bytes copied == " + bytesCopied);
          throw new ParseException("Unexpected length for intermediate manifists.", inStream.getPos());
        }

        // Parse the User Token.
        UserToken userToken = UserToken.parse(inStream);

        // Compose a new Auth Token.
        return new AuthenticationToken(sigHeader, latestManifest, priorManifests, userToken);
      }
    }
    finally { inStream.close(); }
  }

  public boolean equals(Object otherToken)
  {
    // We will consider AuthTokens to be equal if their (latest) signature headers match.
    try {
      return (signatureHeader_.equals(((AuthenticationToken)otherToken).getSignatureHeader()));
    }
    catch (Throwable e) { return false; }
  }

}
