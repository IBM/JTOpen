package com.ibm.as400.security.auth;

import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.CharConverter;
import com.ibm.eim.*;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * Represents a User Token within an Authentication Token.
 **/
final class UserToken implements Serializable {

/* From /osxpf/v5r2m0f.xpf/cur/cmvc/base.pgm/sy.xpf/atkn/atknAuthToken.H :

typedef struct userToken
{
    int version;                   // Version of this user token.
    int totalLength;               // Total length of the user token.
                                   // This includes this structure and
                                   // all of the data fields.
    int useridLength;              // Length of the registry user.
    int useridOffset;              // Offset from the start of this
                                   // structure to the registry user.
    int userRegistryLength;        // Length of the user registry.
    int userRegistryOffset;        // Offset from the start of this
                                   // structure to the user registry.
    //char fields[];               // Array of char for data fields:
                                   // - userid
                                   // - userRegistry
    //int  shortcutLength;         // Hidden length.
} userToken_t;
*/

  static final int FIXED_FIELDS_LENGTH = 7*4;  // 7 'int' fields (each is 4 bytes)
  private static final int OFFSET_TO_VARIABLE_LENGTH_FIELDS = 6*4;  // don't count the final "shortcut length" field

  private int    version_; // version of this user token
  private String userName_;
  private String registryName_;


  UserToken(String userName, String registryName)
  {
    // Assume caller has validated args.
    this(userName, registryName, AuthenticationToken.TOKEN_VERSION_1);
  }

  private UserToken(String userName, String registryName, int version)
  {
    userName_ = userName;
    registryName_ = registryName;
    version_ = version;
  }

  String getUserName()
  {
    return userName_;
  }

  String getRegistryName()
  {
    return registryName_;
  }

  int getLength()
  {
    return FIXED_FIELDS_LENGTH + 2*(userName_.length() + registryName_.length()); // 2 bytes per char
  }

  void writeTo(OutputStream out)
    throws IOException
  {
    // Assume caller has validated arg.

    int userNameLength = 2*(userName_.length());      // 2 bytes per char
    int registryNameLength = 2*(registryName_.length());  // 2 bytes per char
    int totalLength = FIXED_FIELDS_LENGTH + userNameLength + registryNameLength; // total length of UT

    out.write(BinaryConverter.intToByteArray(AuthenticationToken.TOKEN_VERSION_1)); // version
    out.write(BinaryConverter.intToByteArray(totalLength));
    out.write(BinaryConverter.intToByteArray(userNameLength));
    out.write(BinaryConverter.intToByteArray(OFFSET_TO_VARIABLE_LENGTH_FIELDS)); // offset to userName field
    out.write(BinaryConverter.intToByteArray(registryNameLength));
    out.write(BinaryConverter.intToByteArray(OFFSET_TO_VARIABLE_LENGTH_FIELDS + userNameLength)); // offset to registryName field
    out.write(CharConverter.stringToByteArray(Constants.CCSID,userName_));
    out.write(CharConverter.stringToByteArray(Constants.CCSID,registryName_));
    out.write(BinaryConverter.intToByteArray(totalLength));  // final redundant "shortcut length"

///    // DEBUG
///    byte[] bytes = CharConverter.stringToByteArray(Constants.CCSID,userName_);
///    System.out.println("userName_.length() == " + userName_.length() + "; converted bytearray length == " + bytes.length);
///    bytes = CharConverter.stringToByteArray(Constants.CCSID,registryName_);
///    System.out.println("registryName_.length() == " + registryName_.length() + "; converted bytearray length == " + bytes.length);
  }

  static UserToken parse(ByteArrayInputStream0 in)
    throws IOException, ParseException, EimException
  {
    // Assume caller has validated arg.
    byte[] intBuf = new byte[4];

    // Check the version.
    in.read(intBuf);
    int version = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (version != AuthenticationToken.TOKEN_VERSION_1) {
      throw new EimException("Unsupported User Token version: " + version, Constants.ATKNERR_TKN_VERSION_NOT_SUPPORTED);
    }

    // Sanity-check the totalLength field.
    in.read(intBuf);
    int totalLength = BinaryConverter.byteArrayToInt(intBuf, 0);
    if (totalLength <= 0 ||
        totalLength-8 > in.available())  // we've already consumed 8 bytes
    {
      System.out.println("DEBUG totalLength == " + totalLength + "; in.available() == " + in.available());
      ///throw new ParseException("Incorrect totalLength field in User Token: " + totalLength, in.getPos()-4);
    }

    in.read(intBuf);
    int useridLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the useridOffset field, we don't need it.

    in.read(intBuf);
    int userRegistryLength = BinaryConverter.byteArrayToInt(intBuf, 0);

    in.skip(4);  // Skip the userRegistryOffset field, we don't need it.

    byte[] useridBytes = new byte[useridLength];
    in.read(useridBytes);
    String userid = CharConverter.byteArrayToString(Constants.CCSID, useridBytes);

    byte[] userRegistryBytes = new byte[userRegistryLength];
    in.read(userRegistryBytes);
    String userRegistry = CharConverter.byteArrayToString(Constants.CCSID, userRegistryBytes);

    // Consume the final 4 bytes, they contain the "shortcut length".
    in.skip(4);

    return new UserToken(userid, userRegistry, version);
  }

}
