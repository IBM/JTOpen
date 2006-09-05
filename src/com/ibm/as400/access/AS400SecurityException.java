///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400SecurityException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The AS400SecurityException class represents an exception that indicates that a security or authority error has occurred.
 **/
public class AS400SecurityException extends Exception implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    private int rc_;  // Return code associated with this exception.

    /**
     The return code indicating that the user of this program does not have enough authority to access the directory entry.
     **/
    public static final int DIRECTORY_ENTRY_ACCESS_DENIED = 1;
    /**
     The return code indicating that the user is not authorized to the exit program.
     **/
    public static final int EXIT_PROGRAM_NOT_AUTHORIZED = 2;
    /**
     The return code indicating that the user of this program does not have enough authority to the library to perform the requested action.
     **/
    public static final int LIBRARY_AUTHORITY_INSUFFICIENT = 3;
    /**
     The return code indicating that the user of this program does not have enough authority to the resource to perform the requested action.
     **/
    public static final int OBJECT_AUTHORITY_INSUFFICIENT = 4;
    /**
     The return code indicating that the change password request is missing user ID, old password, or new password.
     **/
    public static final int PASSWORD_CHANGE_REQUEST_NOT_VALID = 5;
    /**
     The return code indicating that there is a general password error.
     **/
    public static final int PASSWORD_ERROR = 6;
    /**
     The return code indicating that the password has expired.
     **/
    public static final int PASSWORD_EXPIRED = 7;
    /**
     The return code indicating that the password is not correct.
     **/
    public static final int PASSWORD_INCORRECT = 8;
    /**
     The return code indicating that the password is not correct and that the user ID will be disabled on the next sign-on attempt if the password is incorrect again.
     **/
    public static final int PASSWORD_INCORRECT_USERID_DISABLE = 9;
    /**
     The return code indicating that the password length is not valid.
     **/
    public static final int PASSWORD_LENGTH_NOT_VALID = 10;
    /**
     The return code indicating that the new password has adjacent digits.
     **/
    public static final int PASSWORD_NEW_ADJACENT_DIGITS = 11;
    /**
     The return code indicating that the new password contains a character repeated consecutively.
     **/
    public static final int PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER = 12;
    /**
     The return code indicating that the new password is not allowed.
     **/
    public static final int PASSWORD_NEW_DISALLOWED = 13;
    /**
     The return code indicating that the new password must contain at least one alphabetic.
     **/
    public static final int PASSWORD_NEW_NO_ALPHABETIC = 14;
    /**
     The return code indicating that the new password must contain at least one numeric.
     **/
    public static final int PASSWORD_NEW_NO_NUMERIC = 15;
    /**
     The return code indicating that the new password is not valid.
     **/
    public static final int PASSWORD_NEW_NOT_VALID = 16;
    /**
     The return code indicating that the new password was previously used.
     **/
    public static final int PASSWORD_NEW_PREVIOUSLY_USED = 17;
    /**
     The return code indicating that the new password contains a character used more than once.
     **/
    public static final int PASSWORD_NEW_REPEAT_CHARACTER = 18;
    /**
     The return code indicating that the new password is longer than maximum accepted length.
     **/
    public static final int PASSWORD_NEW_TOO_LONG = 19;
    /**
     The return code indicating that the new password is shorter than minimum accepted length.
     **/
    public static final int PASSWORD_NEW_TOO_SHORT = 20;
    /**
     The return code indicating that the new password contains a user ID as part of the password.
     **/
    public static final int PASSWORD_NEW_USERID = 21;
    /**
     The return code indicating that the password is not set.
     **/
    public static final int PASSWORD_NOT_SET = 22;
    /**
     The return code indicating that the old password is not valid.
     **/
    public static final int PASSWORD_OLD_NOT_VALID = 23;
    /**
     The return code indicating that a general security failure occurred.
     **/
    public static final int SECURITY_GENERAL = 24;
    /**
     The return code indicating that the user canceled out of the sign-on prompt.
     **/
    public static final int SIGNON_CANCELED = 25;
    /**
     The return code indicating that the sign-on request is missing either the user ID or the password.
     **/
    public static final int SIGNON_REQUEST_NOT_VALID = 26;
    /**
     The return code indicating that the exact cause of the failure is not known.  The detailed message may contain additional information.
     **/
    public static final int UNKNOWN = 27;
    /**
     The return code indicating that a general user ID error occurred.
     **/
    public static final int USERID_ERROR = 28;
    /**
     The return code indicating that the user ID length is not valid.
     **/
    public static final int USERID_LENGTH_NOT_VALID = 29;
    /**
     The return code indicating that the user ID is not set.
     **/
    public static final int USERID_NOT_SET = 30;
    /**
     The return code indicating that the user ID has been disabled by the system.
     **/
    public static final int USERID_DISABLE = 31;
    /**
     The return code indicating that the user ID is not known by the system.
     **/
    public static final int USERID_UNKNOWN = 32;
    /**
     The return code indicating that an error occurred processing the exit point.
     **/
    public static final int EXIT_POINT_PROCESSING_ERROR = 33;
    /**
     The return code indicating that an error occurred resolving to the exit program.
     **/
    public static final int EXIT_PROGRAM_RESOLVE_ERROR = 34;
    /**
     The return code indicating that the user exit program associated with the server job rejected the request.
     **/
    public static final int EXIT_PROGRAM_DENIED_REQUEST = 35;
    /**
     The return code indicating that an error occurred with the user exit program call.
     **/
    public static final int EXIT_PROGRAM_CALL_ERROR = 36;
    /**
     The return code indicating that the requested action is not supported.
     **/
    public static final int REQUEST_NOT_SUPPORTED = 37;
    /**
     The return code indicating that the requested action cannot be performed due to the system level not being correct.
     **/
    public static final int SYSTEM_LEVEL_NOT_CORRECT = 38;
    /**
     The return code indicating that the new password contains the same character in the same position as the previous password.
     **/
    public static final int PASSWORD_NEW_SAME_POSITION = 39;
    /**
     The return code indicating that the user of this program does not have enough special authority to perform the requested action.
     **/
    public static final int SPECIAL_AUTHORITY_INSUFFICIENT = 40;
    /**
     The return code indicating that the token type is not valid.
     **/
    public static final int TOKEN_TYPE_NOT_VALID = 41;
    /**
     The return code indicating that the generate token request is not valid.
     **/
    public static final int GENERATE_TOKEN_REQUEST_NOT_VALID = 42;
    /**
     The return code indicating that the token length is not valid.
     **/
    public static final int TOKEN_LENGTH_NOT_VALID = 43;
    /**
     The return code indicating that the new password contains a character that is not valid.
     **/
    public static final int PASSWORD_NEW_CHARACTER_NOT_VALID = 44;
    /**
     The return code indicating that the password has pre-V2R2 encryption.
     **/
    public static final int PASSWORD_PRE_V2R2 = 45;
    /**
     The return code indicating that the password is *NONE.
     **/
    public static final int PASSWORD_NONE = 46;
    /**
     The return code indicating that the profile token or identity token is not valid.
     **/
    public static final int PROFILE_TOKEN_NOT_VALID = 47;
    /**
     The return code indicating that the profile token is not valid.  Maximum number of profile tokens for the system already generated.
     **/
    public static final int PROFILE_TOKEN_NOT_VALID_MAXIMUM = 48;
    /**
     The return code indicating that the profile token is not valid.  Timeout interval is not valid.
     **/
    public static final int PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID = 49;
    /**
     The return code indicating that the profile token is not valid.  Type of profile token is not valid.
     **/
    public static final int PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID = 50;
    /**
     The return code indicating that the profile token is not valid.  Profile token is not regenerable.
     **/
    public static final int PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE = 51;
    /**
     The return code indicating that the authentication token is not valid.  Consistency checks failed.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_CONSISTENCY = 52;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Requested mechanisms are not supported by local system.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_MECHANISM = 53;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Credentials are not available or are not valid for this context.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID = 54;
    /**
     The return code indicating that the authentication token is not valid. Kerberos token or identity token contains incorrect signature.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_SIGNATURE = 55;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Credentials are no longer valid.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID = 56;
    /**
     The return code indicating that the Kerberos ticket is not valid. Consistency checks on the credantial structure failed.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_CREDANTIAL_STRUCTURE = 57;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Verification routine failed.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_VERIFICATION = 58;
    /**
     The return code indicating that the authentication token is not valid.  EIM configuration error detected.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_EIM = 59;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Kerberos principal maps to a system profile which can not be used to sign on.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE = 60;
    /**
     The return code indicating that the authentication token is not valid.  Token maps to multiple user profile names.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES = 61;
    /**
     The return code indicating that the Kerberos ticket is not valid.  Kerberos service ticket could not be retrieved.
     **/
    public static final int KERBEROS_TICKET_NOT_VALID_RETRIEVE = 62;
    /**
      The return code indicating that the user ID or password contains a character that is not valid.
     **/
    public static final int SIGNON_CHAR_NOT_VALID = 63;
    /**
      The return code indicating that the user ID does not match the user profile associated with the authentication token.
     **/
    public static final int USERID_MISMATCH = 64;
    /**
      The return code indicating that the password validation program failed the request.
     **/
    public static final int PASSWORD_NEW_VALIDATION_PROGRAM = 65;
    /**
     The return code indicating that the user of this program does not have enough authority to generate a profile token for another user.
     **/
    public static final int GENERATE_TOKEN_AUTHORITY_INSUFFICIENT = 66;
    /**
     The return code indicating that can not connect to the system EIM domain.
     **/
    public static final int GENERATE_TOKEN_CAN_NOT_CONNECT = 67;
    /**
     The return code indicating that can not change the CCSID.
     **/
    public static final int GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID = 68;
    /**
     The return code indicating that can not obtain the EIM registry name.
     **/
    public static final int GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME = 69;
    /**
     The return code indicating that no mapping exists.
     **/
    public static final int GENERATE_TOKEN_NO_MAPPING = 70;
    /**
     The return code indicating that the system was not able to allocate space needed for authorization.
     **/
    public static final int SERVER_NO_MEMORY = 71;
    /**
     The return code indicating that an error occurred on the system while converting data between code pages.
     **/
    public static final int SERVER_CONVERSION_ERROR = 72;
    /**
     The return code indicating that an error occurred on the system while using EIM interfaces.
     **/
    public static final int SERVER_EIM_ERROR = 73;
    /**
     The return code indicating that an error occurred on the system while using cryptographic interfaces.
     **/
    public static final int SERVER_CRYPTO_ERROR = 74;
    /**
     The return code indicating that the system version does support the token version.
     **/
    public static final int SERVER_TOKEN_VERSION = 75;
    /**
     The return code indicating that the system could not find the public key.
     **/
    public static final int SERVER_KEY_NOT_FOUND = 76;


    /**
     Constructs an AS400SecurityException.
     <p>An AS400SecurityException indicates that a security error has occurred.
     @param  returnCode  The return code which identifies the message to be returned.  Possible values are defined as constants on this class.
     **/
    protected AS400SecurityException(int returnCode)
    {
        super(ResourceBundleLoader.getText(getMRIKey(returnCode)));
        rc_ =  returnCode;
    }

    // Constructs an AS400SecurityException object. It indicates that a security exception occurred.  Exception message will look like this:  objectName: User is not authorized to object.
    // @param  objectName  The name of the object.
    // @param  returnCode  The return code which identifies the message to be returned.
    AS400SecurityException(String objectName, int returnCode)
    {
        // Create the message.
        super(objectName + ": " + ResourceBundleLoader.getText(getMRIKey(returnCode)));
        rc_ =  returnCode;
    }

    // Returns the text associated with the return code.
    // @param  returnCode  The return code associated with this exception.
    // @return  The text string which describes the error.
    // This method is required so the message can be created and sent in super().
    static String getMRIKey(int returnCode)
    {
        switch(returnCode)
        {
            case DIRECTORY_ENTRY_ACCESS_DENIED:
                return "EXC_DIRECTORY_ENTRY_ACCESS_DENIED";
            case EXIT_PROGRAM_NOT_AUTHORIZED:
                return "EXC_EXIT_PROGRAM_NOT_AUTHORIZED";
            case LIBRARY_AUTHORITY_INSUFFICIENT:
                return "EXC_LIBRARY_AUTHORITY_INSUFFICIENT";
            case OBJECT_AUTHORITY_INSUFFICIENT:
                return "EXC_OBJECT_AUTHORITY_INSUFFICIENT";
            case PASSWORD_CHANGE_REQUEST_NOT_VALID:
                return "EXC_PASSWORD_CHANGE_REQUEST_NOT_VALID";
            case PASSWORD_ERROR:
                return "EXC_PASSWORD_ERROR";
            case PASSWORD_EXPIRED:
                return "EXC_PASSWORD_EXPIRED";
            case PASSWORD_INCORRECT:
                return "EXC_PASSWORD_INCORRECT";
            case PASSWORD_INCORRECT_USERID_DISABLE:
                return "EXC_PASSWORD_INCORRECT_USERID_DISABLE";
            case PASSWORD_LENGTH_NOT_VALID:
                return "EXC_PASSWORD_LENGTH_NOT_VALID";
            case PASSWORD_NEW_ADJACENT_DIGITS:
                return "EXC_PASSWORD_NEW_ADJACENT_DIGITS";
            case PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER:
                return "EXC_PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER";
            case PASSWORD_NEW_DISALLOWED:
                return "EXC_PASSWORD_NEW_DISALLOWED";
            case PASSWORD_NEW_NO_ALPHABETIC:
                return "EXC_PASSWORD_NEW_NO_ALPHABETIC";
            case PASSWORD_NEW_NO_NUMERIC:
                return "EXC_PASSWORD_NEW_NO_NUMERIC";
            case PASSWORD_NEW_NOT_VALID:
                return "EXC_PASSWORD_NEW_NOT_VALID";
            case PASSWORD_NEW_PREVIOUSLY_USED:
                return "EXC_PASSWORD_NEW_PREVIOUSLY_USED";
            case PASSWORD_NEW_REPEAT_CHARACTER:
                return "EXC_PASSWORD_NEW_REPEAT_CHARACTER";
            case PASSWORD_NEW_TOO_LONG:
                return "EXC_PASSWORD_NEW_TOO_LONG";
            case PASSWORD_NEW_TOO_SHORT:
                return "EXC_PASSWORD_NEW_TOO_SHORT";
            case PASSWORD_NEW_USERID:
                return "EXC_PASSWORD_NEW_USERID";
            case PASSWORD_NOT_SET:
                return "EXC_PASSWORD_NOT_SET";
            case PASSWORD_OLD_NOT_VALID:
                return "EXC_PASSWORD_OLD_NOT_VALID";
            case SECURITY_GENERAL:
                return "EXC_SECURITY_GENERAL";
            case SIGNON_CANCELED:
                return "EXC_SIGNON_CANCELED";
            case SIGNON_REQUEST_NOT_VALID:
                return "EXC_SIGNON_REQUEST_NOT_VALID";
            case UNKNOWN:
                return "EXC_UNKNOWN";
            case USERID_ERROR:
                return "EXC_USERID_ERROR";
            case USERID_LENGTH_NOT_VALID:
                return "EXC_USERID_LENGTH_NOT_VALID";
            case USERID_NOT_SET:
                return "EXC_USERID_NOT_SET";
            case USERID_DISABLE:
                return "EXC_USERID_DISABLE";
            case USERID_UNKNOWN:
                return "EXC_USERID_UNKNOWN";
            case EXIT_POINT_PROCESSING_ERROR:
                return "EXC_EXIT_POINT_PROCESSING_ERROR";
            case EXIT_PROGRAM_DENIED_REQUEST:
                return "EXC_EXIT_PROGRAM_DENIED_REQUEST";
            case EXIT_PROGRAM_RESOLVE_ERROR:
                return "EXC_EXIT_PROGRAM_RESOLVE_ERROR";
            case EXIT_PROGRAM_CALL_ERROR:
                return "EXC_EXIT_PROGRAM_CALL_ERROR";
            case REQUEST_NOT_SUPPORTED:
                return "EXC_REQUEST_NOT_SUPPORTED";
            case SYSTEM_LEVEL_NOT_CORRECT:
                return "EXC_SYSTEM_LEVEL_NOT_CORRECT";
            case PASSWORD_NEW_SAME_POSITION:
                return "EXC_PASSWORD_NEW_SAME_POSITION";
            case SPECIAL_AUTHORITY_INSUFFICIENT:
                return "EXC_SPECIAL_AUTHORITY_INSUFFICIENT";
            case TOKEN_TYPE_NOT_VALID:
                return "EXC_TOKEN_TYPE_NOT_VALID";
            case GENERATE_TOKEN_REQUEST_NOT_VALID:
                return "EXC_GENERATE_TOKEN_REQUEST_NOT_VALID";
            case TOKEN_LENGTH_NOT_VALID:
                return "EXC_TOKEN_LENGTH_NOT_VALID";
            case PASSWORD_NEW_CHARACTER_NOT_VALID:
                return "EXC_PASSWORD_NEW_CHARACTER_NOT_VALID";
            case PASSWORD_PRE_V2R2:
                return "EXC_PASSWORD_PRE_V2R2";
            case PASSWORD_NONE:
                return "EXC_PASSWORD_NONE";
            case PROFILE_TOKEN_NOT_VALID:
                return "EXC_PROFILE_TOKEN_NOT_VALID";
            case PROFILE_TOKEN_NOT_VALID_MAXIMUM:
                return "EXC_PROFILE_TOKEN_NOT_VALID_MAXIMUM";
            case PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID:
                return "EXC_PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID";
            case PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID:
                return "EXC_PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID";
            case PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE:
                return "EXC_PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE";
            case KERBEROS_TICKET_NOT_VALID_CONSISTENCY:
                return "EXC_KERBEROS_TICKET_NOT_VALID_CONSISTENCY";
            case KERBEROS_TICKET_NOT_VALID_MECHANISM:
                return "EXC_KERBEROS_TICKET_NOT_VALID_MECHANISM";
            case KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID:
                return "EXC_KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID";
            case KERBEROS_TICKET_NOT_VALID_SIGNATURE:
                return "EXC_KERBEROS_TICKET_NOT_VALID_SIGNATURE";
            case KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID:
                return "EXC_KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID";
            case KERBEROS_TICKET_NOT_VALID_CREDANTIAL_STRUCTURE:
                return "EXC_KERBEROS_TICKET_NOT_VALID_CREDANTIAL_STRUCTURE";
            case KERBEROS_TICKET_NOT_VALID_VERIFICATION:
                return "EXC_KERBEROS_TICKET_NOT_VALID_VERIFICATION";
            case KERBEROS_TICKET_NOT_VALID_EIM:
                return "EXC_KERBEROS_TICKET_NOT_VALID_EIM";
            case KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE:
                return "EXC_KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE";
            case KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES:
                return "EXC_KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES";
            case KERBEROS_TICKET_NOT_VALID_RETRIEVE:
                return "EXC_KERBEROS_TICKET_NOT_VALID_RETRIEVE";
            case SIGNON_CHAR_NOT_VALID:
                return "EXC_SIGNON_CHAR_NOT_VALID";
            case USERID_MISMATCH:
                return "EXC_USERID_MISMATCH";
            case PASSWORD_NEW_VALIDATION_PROGRAM:
                return "EXC_PASSWORD_NEW_VALIDATION_PROGRAM";
            case GENERATE_TOKEN_AUTHORITY_INSUFFICIENT:
                return "EXC_GENERATE_TOKEN_AUTHORITY_INSUFFICIENT";
            case GENERATE_TOKEN_CAN_NOT_CONNECT:
                return "EXC_GENERATE_TOKEN_CAN_NOT_CONNECT";
            case GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID:
                return "EXC_GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID";
            case GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME:
                return "EXC_GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME";
            case GENERATE_TOKEN_NO_MAPPING:
                return "EXC_GENERATE_TOKEN_NO_MAPPING";
            case SERVER_NO_MEMORY:
                return "EXC_SERVER_NO_MEMORY";
            case SERVER_CONVERSION_ERROR:
                return "EXC_SERVER_CONVERSION_ERROR";
            case SERVER_EIM_ERROR:
                return "EXC_SERVER_EIM_ERROR";
            case SERVER_CRYPTO_ERROR:
                return "EXC_SERVER_CRYPTO_ERROR";
            case SERVER_TOKEN_VERSION:
                return "EXC_SERVER_TOKEN_VERSION";
            case SERVER_KEY_NOT_FOUND:
                return "EXC_SERVER_KEY_NOT_FOUND";
            default:
                return "EXC_UNKNOWN";   // Bad return code was provided.
        }
    }

    /**
     Returns the return code associated with this exception.
     @return  The return code.
     **/
    public int getReturnCode()
    {
        return rc_;
    }
}
