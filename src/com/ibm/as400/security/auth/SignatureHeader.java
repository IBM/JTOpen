package com.ibm.as400.security.auth;

import com.ibm.as400.access.BinaryConverter;
import com.ibm.eim.*;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.text.ParseException;


/**
 * Represents a Token Signature Header within an Authentication Token.
 **/
final class SignatureHeader implements Serializable {

/* From /osxpf/v5r2m0f.xpf/cur/cmvc/base.pgm/sy.xpf/atkn/atknAuthToken.H :

typedef struct tokenSignatureHeader
{
    int version;                   // Version of this signature header.
    int totalLength;               // Total length of the token
                                   // signature.  This includes this
                                   // structure and the data fields.
    int authTokenLength;           // Length of the entire
                                   // authentication token starting at
                                   // this signature header.
    int signedLength;              // Length of the data that was
                                   // signed.  This would start at the
                                   // token manifest immediately after
                                   // this header and continue to the
                                   // end of the user token.
    int signatureLength;           // Length of the signature.
    int signatureOffset;           // Offset from the start of this
                                   // structure to the signature.
  //char fields[];                 // Array of char for data fields:
                                   // - signature
} tokenSignatureHeader_t;
*/

  static final int FIXED_FIELDS_LENGTH = 6*4;  // 6 'int' fields (each is 4 bytes)
  private static final int OFFSET_TO_VARIABLE_LENGTH_FIELDS = FIXED_FIELDS_LENGTH;

  static final int OFFSET_TO_HEADER_LENGTH = 4; // offset to signature header length field.
  static final int OFFSET_TO_TOKEN_LENGTH = 8; // offset to authTokenLength_ field.

  // Version of this token signature header
  private int version_;

  // Length of the entire authentication token starting at this signature header.
  private int authTokenLength_;

  // Length of the data that was signed.  This would start at the token manifest immediately after this header and continue to the end of the user token.
  private int signedLength_;

  // The signature.
  private byte[] signature_;




  private SignatureHeader(byte[] signature, int tokenLength, int signedLength, int version)
  {
    signature_ = signature;
    authTokenLength_ = tokenLength;
    signedLength_ = signedLength;
    version_ = version;
  }

  static SignatureHeader getInstance(TokenManifest newManifest, /*SignatureAndManifest[]*/byte[] priorManifests, UserToken userToken, PrivateKey privateKey)
    throws EimException, IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
  {
    ///System.out.println("DEBUG SignatureHeader.getInstance(): priorManifests " + (priorManifests==null ? "null" : "not null"));

    // Assume caller has validated args.

    // Combine the Token Manifest, manifest list, and User Token into a single byte array that we can generate a signature for.
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
    newManifest.writeTo(outStream);
    if (priorManifests != null) {
///      for (int i=0; i<priorManifests.length; i++) {
///        priorManifests[i].writeTo(outStream);
///      }
      outStream.write(priorManifests,0,priorManifests.length);  // write priorManifests to stream
    }
    userToken.writeTo(outStream);
    byte[] manifestsPlusUT = outStream.toByteArray();
    int signedLength = manifestsPlusUT.length;
    ///System.out.println("DEBUG SigHeader.getInstance(): New signedLength == " + signedLength);

    // Generate a signature.
    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initSign(privateKey);
    signer.update(manifestsPlusUT);
    byte[] signature = signer.sign();

    // Determine new "total length of auth token".

///    // If priorManifests is null, this is the sum of (length of current Signature Header) + (length of Token Manifest) + (length of User Token)
///    // If priorManifests is non-null, this is the sum of (length of current Signature Header) + (length of Token Manifest) + (tokenLength from first SignatureHeader in priorManifests)

    // Total token length is the sum of (length of current Signature Header) + (length of Token Manifest) + (length of prior manifests) + (length of User Token).
    int tokenLength = (FIXED_FIELDS_LENGTH + signature.length) + newManifest.getLength();
///    if (priorManifests == null) {
///      tokenLength += userToken.getLength();   // add length of User Token
///    }
///    else {
///      tokenLength += priorManifests[0].getSignatureHeader().getTokenLength(); // add prior total tokenLength
    if (priorManifests != null) {
      tokenLength += priorManifests.length;
    }
    tokenLength += userToken.getLength();

    return new SignatureHeader(signature, tokenLength, signedLength, AuthenticationToken.TOKEN_VERSION_1);
  }


  static SignatureHeader getInstance(TokenManifest newManifest, AuthenticationToken authToken, PrivateKey privateKey)
    throws EimException, IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
  {
    // Assume caller has validated args.

    // Combine the token's current Sig Header and Token Manifest with any prior manifests.
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
    authToken.getSignatureHeader().writeTo(outStream);
    authToken.getManifest().writeTo(outStream);
    byte[] priorManifests = authToken.getPriorManifests();
    if (priorManifests != null) {
      outStream.write(priorManifests, 0, priorManifests.length);
    }
    byte[] currentPlusPriorManifests = outStream.toByteArray();
    return getInstance(newManifest, currentPlusPriorManifests, authToken.getUserToken(), privateKey);
  }

  /**
   Returns the total length (in bytes) of this Signature Header.
   **/
  int getLength() {
    return (FIXED_FIELDS_LENGTH + signature_.length);
  }


  int getTokenLength()
  {
    return authTokenLength_;
  }
  byte[] getSignature()
  {
    return signature_;
  }

  int getSignedLength()
  {
    return signedLength_;
  }

  void writeTo(OutputStream out)
    throws IOException
  {
    // Assume caller has validated arg.

    out.write(BinaryConverter.intToByteArray(AuthenticationToken.TOKEN_VERSION_1)); // version
    out.write(BinaryConverter.intToByteArray(FIXED_FIELDS_LENGTH + signature_.length)); // length of this signature header
    out.write(BinaryConverter.intToByteArray(authTokenLength_)); // total length of entire auth token
    out.write(BinaryConverter.intToByteArray(signedLength_));
    out.write(BinaryConverter.intToByteArray(signature_.length));
    out.write(BinaryConverter.intToByteArray(OFFSET_TO_VARIABLE_LENGTH_FIELDS));  // offset to signature
    out.write(signature_);
  }

  static SignatureHeader parse(ByteArrayInputStream0 in)
    throws IOException, ParseException, EimException
  {
    // Assume caller has validated arg.
    byte[] intBuf = new byte[4];

    // Check the version.
    in.read(intBuf);
    int version = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (version != AuthenticationToken.TOKEN_VERSION_1) {
      throw new EimException("Unsupported Token Signature Header version: " + version, Constants.ATKNERR_TKN_VERSION_NOT_SUPPORTED);
    }

    // Sanity-check the totalLength field.  This indicates the length of the signature header.
    in.read(intBuf);
    int totalLength = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (totalLength <= 0 || totalLength > in.available()) {
      throw new ParseException("Incorrect totalLength field in Token Signature Header: " + totalLength, in.getPos()-4);
    }

    in.read(intBuf);
    int authTokenLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.read(intBuf);
    int signedLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.read(intBuf);
    int signatureLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the signatureOffset field, we don't need it.

    byte[] signature = new byte[signatureLength];
    in.read(signature);

    return new SignatureHeader(signature, authTokenLength, signedLength, version);
  }

  boolean equals(SignatureHeader other)
  {
    return (other != null &&
            authTokenLength_ == other.getTokenLength() &&
            signedLength_ == other.getSignedLength() &&
            java.util.Arrays.equals(signature_, other.getSignature()));
  }

}
