package com.ibm.as400.security.auth;

import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.CharConverter;
import com.ibm.eim.*;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * Represents a Token Manifest within an Authentication Token.
 **/
final class TokenManifest implements Serializable {

/* From /osxpf/v5r2m0f.xpf/cur/cmvc/base.pgm/sy.xpf/atkn/atknAuthToken.H :

typedef struct tokenManifest
{
    int version;                   // Version of this token manifest.
    int totalLength;               // Total length of the token
                                   // manifest. This includes this
                                   // structure and all of the data
                                   // fields.
    int counter;                   // Token manifest counter.
    int senderEimIdLength;         // Length of the senders EIM ID.
    int senderEimIdOffset;         // Offset from the start of this
                                   // structure to the EIM ID.
    int senderAppIdLength;         // Length of the senders application
                                   // ID.
    int senderAppIdOffset;         // Offset from the start of this
                                   // structure to the senders
                                   // application ID.
    int senderTimestampLength;     // Length of the senders timestamp
                                   // used to identify the public key.
    int senderTimestampOffset;     // Offset from the start of this
                                   // structure to the senders timestamp.
    int receiverEimIdLength;       // Length of the EIM ID of expected
                                   // receiver.
    int receiverEimIdOffset;       // Offset from the start of this
                                   // structure to the EIM ID of
                                   // expected receiver.
    int receiverAppIdLength;       // Length of the application id of
                                   // expected receiver.
    int receiverAppIdOffset;       // Offset from the start of this
                                   // structure to the application ID 
                                   // expected receiver.
  //char fields[];                 // Array of char for data fields:
                                   // - senderEimId
                                   // - senderAppId
                                   // - senderTimestamp
                                   // - receiverEimId
                                   // - receiverAppId
} tokenManifest_t;

*/

  static final int FIXED_FIELDS_LENGTH = 13*4;  // 13 'int' fields (each is 4 bytes)
  private static final int OFFSET_TO_VARIABLE_LENGTH_FIELDS = FIXED_FIELDS_LENGTH;

  private int    version_;       // version of this token manifest
  private int    counter_;
  private String sndEidName_;
  private String sndAppID_;
  private String sndTimestamp_;  // in format "YYYYMMDDHHMMSS"
  private String rcvEidName_;
  private String rcvAppID_;

  TokenManifest(int counter, String sndEidName, String sndAppID, String sndTimestamp, String rcvEidName, String rcvAppID)
  {
    // Assume caller has validated args.
    this(counter, sndEidName, sndAppID, sndTimestamp, rcvEidName, rcvAppID, AuthenticationToken.TOKEN_VERSION_1);
  }

  private TokenManifest(int counter, String sndEidName, String sndAppID, String sndTimestamp, String rcvEidName, String rcvAppID, int version)
  {
    counter_ = counter;
    sndEidName_ = sndEidName;
    sndAppID_ = sndAppID;
    sndTimestamp_ = sndTimestamp;
    rcvEidName_ = rcvEidName;
    rcvAppID_ = rcvAppID;
    version_ = version;
  }

  int getCounter() {
    return counter_;
  }

  /**
   Returns the total length (in bytes) of this Token Manifest.
   **/
  int getLength() {
    // Note: In CCSID 1200, there are always 2 bytes per char.
    return (FIXED_FIELDS_LENGTH +
            2 * (sndEidName_.length() +
                 sndAppID_.length() +
                 sndTimestamp_.length() +
                 rcvEidName_.length() +
                 rcvAppID_.length()));
  }

  String getSenderEidName() {
    return sndEidName_;
  }

  String getSenderAppInstanceID() {
    return sndAppID_;
  }

  String getSenderTimestamp() {
    return sndTimestamp_;
  }

  String getReceiverEidName() {
    return rcvEidName_;
  }

  String getReceiverAppInstanceID() {
    return rcvAppID_;
  }

  void writeTo(OutputStream out)
    throws IOException
  {
    // Assume caller has validated arg.

    // Note: In CCSID 1200, there are always 2 bytes per char.
    int sndEidLength       = 2*(sndEidName_.length());
    int sndAppIdLength     = 2*(sndAppID_.length());
    int sndTimestampLength = 2*(sndTimestamp_.length());
    int rcvEidLength       = 2*(rcvEidName_.length());
    int rcvAppIdLength     = 2*(rcvAppID_.length());
    int totalLength = FIXED_FIELDS_LENGTH + sndEidLength + sndAppIdLength + sndTimestampLength + rcvEidLength + rcvAppIdLength;

    out.write(BinaryConverter.intToByteArray(AuthenticationToken.TOKEN_VERSION_1)); // version
    out.write(BinaryConverter.intToByteArray(totalLength));
    out.write(BinaryConverter.intToByteArray(counter_));

    int offset = OFFSET_TO_VARIABLE_LENGTH_FIELDS;
    out.write(BinaryConverter.intToByteArray(sndEidLength));
    out.write(BinaryConverter.intToByteArray(offset));
    offset += sndEidLength;

    out.write(BinaryConverter.intToByteArray(sndAppIdLength));
    out.write(BinaryConverter.intToByteArray(offset));
    offset += sndAppIdLength;

    out.write(BinaryConverter.intToByteArray(sndTimestampLength));
    out.write(BinaryConverter.intToByteArray(offset));
    offset += sndTimestampLength;

    out.write(BinaryConverter.intToByteArray(rcvEidLength));
    out.write(BinaryConverter.intToByteArray(offset));
    offset += rcvEidLength;

    out.write(BinaryConverter.intToByteArray(rcvAppIdLength));
    out.write(BinaryConverter.intToByteArray(offset));

    out.write(CharConverter.stringToByteArray(Constants.CCSID,sndEidName_));
    out.write(CharConverter.stringToByteArray(Constants.CCSID,sndAppID_));
    out.write(CharConverter.stringToByteArray(Constants.CCSID,sndTimestamp_));
    out.write(CharConverter.stringToByteArray(Constants.CCSID,rcvEidName_));
    out.write(CharConverter.stringToByteArray(Constants.CCSID,rcvAppID_));
  }

  static TokenManifest parse(ByteArrayInputStream0 in)
    throws IOException, ParseException, EimException
  {
    // Assume caller has validated arg.
    byte[] intBuf = new byte[4];

    // Check the version.
    in.read(intBuf);
    int version = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (version != AuthenticationToken.TOKEN_VERSION_1) {
      throw new EimException("Unsupported Token Manifest version: " + version, Constants.ATKNERR_TKN_VERSION_NOT_SUPPORTED);
    }

    // Sanity-check the totalLength field.
    in.read(intBuf);
    int totalLength = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (totalLength <= 0 || totalLength > in.available()) {
      throw new ParseException("Incorrect totalLength field in Token Manifest: " + totalLength, in.getPos()-4);
    }

    in.read(intBuf);
    int counter = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.read(intBuf);
    int sndEidLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the sndEidOffset field, we don't need it.

    in.read(intBuf);
    int sndAppIdLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the sndAppIdOffset field, we don't need it.

    in.read(intBuf);
    int sndTimestampLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the sndTimestampOffset field, we don't need it.

    in.read(intBuf);
    int rcvEidLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the rcvEidOffset field, we don't need it.

    in.read(intBuf);
    int rcvAppIdLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the rcvAppIdOffset field, we don't need it.

    byte[] sndEidBytes = new byte[sndEidLength];
    in.read(sndEidBytes);
    String sndEid = CharConverter.byteArrayToString(Constants.CCSID, sndEidBytes);

    byte[] sndAppIdBytes = new byte[sndAppIdLength];
    in.read(sndAppIdBytes);
    String sndAppId = CharConverter.byteArrayToString(Constants.CCSID, sndAppIdBytes);

    byte[] sndTimestampBytes = new byte[sndTimestampLength];
    in.read(sndTimestampBytes);
    String sndTimestamp = CharConverter.byteArrayToString(Constants.CCSID, sndTimestampBytes);

    byte[] rcvEidBytes = new byte[rcvEidLength];
    in.read(rcvEidBytes);
    String rcvEid = CharConverter.byteArrayToString(Constants.CCSID, rcvEidBytes);

    byte[] rcvAppIdBytes = new byte[rcvAppIdLength];
    in.read(rcvAppIdBytes);
    String rcvAppId = CharConverter.byteArrayToString(Constants.CCSID, rcvAppIdBytes);

    return new TokenManifest(counter, sndEid, sndAppId, sndTimestamp, rcvEid, rcvAppId, version);
  }

}
