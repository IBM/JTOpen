///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: MRI.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.ListResourceBundle;

/**
 Locale-specific objects for the IBM Toolbox for Java.
 **/
//
// @B0A - Implementation note:
//        This MRI contains ONLY resources that are needed in the
//        proxy jar file.  Resources not needed in the proxy jar
//        file belong in MRI2.
//
public class MRI extends ListResourceBundle
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    public Object[][] getContents()
    {
        return resources;
    }

    private final static Object[][] resources =
    {
        // #TRANNOTE Before you add a new resource, please make
        // #TRANNOTE sure you are not duplicating another.  The
        // #TRANNOTE goal is to keep the amount of translatable
        // #TRANNOTE text down.
        // #TRANNOTE
        // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
        // #TRANNOTE    is { "KEY", "value" },
        // #TRANNOTE
        // #TRANNOTE The key must be left alone so translate only the value.
        // #TRANNOTE

        // #TRANNOTE #####################################################
        // #TRANNOTE Property values.
        // #TRANNOTE Start with PROP_VALUE_ prefix.
        // #TRANNOTE #####################################################

        // #TRANNOTE #####################################################
        // #TRANNOTE Dialog MRI.
        // #TRANNOTE #####################################################

        { "DLG_CONFIRM_LABEL", "Confirm:" },
        { "DLG_PASSWORDS_LABEL", "Passwords" },
        { "DLG_NEW_LABEL", "New:" },
        { "DLG_OLD_LABEL", "Old:" },

        { "DLG_ABORT_BUTTON", "Abort" },
        { "DLG_HELP_BUTTON", "Help" },
        { "DLG_IGNORE_BUTTON", "Ignore" },
        { "DLG_NO_BUTTON", "No" },
        { "DLG_RETRY_BUTTON", "Retry" },
        { "DLG_YES_BUTTON", "Yes" },

        { "DLG_DEFAULT_USER_EXISTS", "A default user already exists for this server." },
        { "DLG_SET_DEFAULT_USER_FAILED", "Default user has not been changed." },
        { "DLG_SIGNON_TITLE", "Signon to the Server" },
        { "DLG_CHANGE_PASSWORD_TITLE", "Change Password" },
        { "DLG_MISSING_USERID", "Missing user ID or password." },
        { "DLG_MISSING_PASSWORD", "Missing user ID, old or new password." },
        { "DLG_INVALID_USERID", "User ID is not valid." },
        { "DLG_CHANGE_PASSWORD_PROMPT", "Would you like to change your password now?" },


        // #TRANNOTE
        // #TRANNOTE Please place the &0 in the appropriate place in the message.
        // #TRANNOTE It (&0) will be substituted with the days to password
        // #TRANNOTE expiration at runtime.
        // #TRANNOTE
        { "DLG_PASSWORD_EXP_WARNING", "Password will expire in &0 days." },


        // #TRANNOTE #####################################################
        // #TRANNOTE Short descriptions and display names for events.
        // #TRANNOTE Descriptions start with EVT_DESC_ prefix, display
        // #TRANNOTE names start with EVT_NAME.
        // #TRANNOTE #####################################################

        { "EVT_DESC_ACTION_COMPLETED", "The action has completed." },
        { "EVT_NAME_ACTION_COMPLETED", "actionCompleted" },
        { "EVT_DESC_AS400FILE_RECORD_DESCRIPTION", "An AS400FileRecordDescription event has occurred." },

        // #TRANNOTE An AS400FileRecordDescription event has occurred.
        { "EVT_NAME_AS400FILE_RECORD_DESCRIPTION", "AS400FileRecordDescription" },
        { "EVT_DESC_CONNECTION_CONNECTED", "Connected to the server." },
        { "EVT_NAME_CONNECTION_CONNECTED", "connected" },
        { "EVT_DESC_CONNECTION_EVENT", "A connection event has occurred." },
        { "EVT_NAME_CONNECTION_EVENT", "connection" },
        { "EVT_DESC_FIELD_MODIFIED", "A field has changed." },
        { "EVT_NAME_FIELD_MODIFIED", "fieldModified" },
        { "EVT_DESC_FILE_EVENT", "A file event has occurred." },

        // #TRANNOTE A file event has occurred.
        { "EVT_NAME_FILE_EVENT", "fileEvent" },
        { "EVT_DESC_OUTQ_EVENT", "An output queue event has occurred." },
        { "EVT_NAME_OUTQ_EVENT", "outputQueue" },

        // #TRANNOTE The meaning of 'property' here is like 'attribute'.
        { "EVT_DESC_PROPERTY_CHANGE", "A bound property has changed." },

        // #TRANNOTE A bound property has changed.
        // #TRANNOTE The meaning of 'property' here is like 'attribute'.
        { "EVT_NAME_PROPERTY_CHANGE", "propertyChange" },

        // #TRANNOTE The meaning of 'property' here is like 'attribute'.
        { "EVT_DESC_PROPERTY_VETO", "A constrained property has changed." },

        // #TRANNOTE A constrained property has changed.
        // #TRANNOTE The meaning of 'property' here is like 'attribute'.
        { "EVT_NAME_PROPERTY_VETO", "vetoableChange" },
        { "EVT_DESC_RECORD_DESCRIPTION_EVENT", "A field description was added." },

        // #TRANNOTE A field description was added.
        { "EVT_NAME_RECORD_DESCRIPTION_EVENT", "recordDescription" },
        { "EVT_DESC_DQ_DATA_EVENT", "A data queue data event has occurred." },
        { "EVT_NAME_DQ_DATA_EVENT", "dataQueueDataEvent" },
        { "EVT_DESC_DQ_OBJECT_EVENT", "A data queue object event has occurred." },
        { "EVT_NAME_DQ_OBJECT_EVENT", "dataQueueObjectEvent" },
        { "EVT_DESC_PRINT_OBJECT_EVENT", "A print object list event has occurred." },
        { "EVT_NAME_PRINT_OBJECT_EVENT", "printObjectList" },
        { "EVT_DESC_US_EVENT", "A user space event has occurred." },

        // #TRANNOTE A user space event has occurred.
        { "EVT_NAME_US_EVENT", "userSpaceEvent" },
        { "EVT_DESC_DA_EVENT", "A data area event has occurred." },

        // #TRANNOTE A data area event has occurred.
        { "EVT_NAME_DA_EVENT", "dataAreaEvent" },

        // #TRANNOTE #####################################################
        // #TRANNOTE Common exception messages.
        // #TRANNOTE #####################################################
        // #TRANNOTE Each key starts with the prefix EXC_ and
        // #TRANNOTE then a short identifier to describe the
        // #TRANNOTE message.
        // #TRANNOTE
        { "EXC_ACCESS_DENIED", "Access to request was denied." },
        { "EXC_AS400_ERROR", "An error occurred on the server." },
        { "EXC_ATTRIBUTE_NOT_VALID", "Attribute name not valid." },

        { "EXC_COMMUNICATIONS_ERROR", "Error occurred in communications." },
        { "EXC_CONNECT_FAILED", "Failed to connect." },
        { "EXC_CONNECTION_DROPPED", "Connection was dropped unexpectedly." },
        { "EXC_CONNECTION_NOT_ACTIVE", "Connection is not active." },
        { "EXC_CONNECTION_NOT_ESTABLISHED", "Unable to establish a connection." },
        { "EXC_CONNECTION_NOT_PASSED_AUTHORITY", "Not able to pass connection to server job.  User profile for server job does not have enough authority." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_LENGTH", "Not able to pass connection to server job.  Program data length is incorrect." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY", "Not able to pass connection to server job. Daemon job is not authorized to server job library." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED", "Not able to pass connection to server job.  Prestart job could not be started." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_PROFILE", "Not able to pass connection to server job.  User profile for server job does not exist." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY", "Not able to pass connection to server job.  Daemon job is not authorized to server job program." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND", "Not able to pass connection to server job.  Server job program was not found." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_RECEIVER_AREA", "Not able to pass connection to server job.  Receiver area is too small." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_SERVER_ENDING", "Not able to pass connection to server job. Server job is ending." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_SERVER_NOT_STARTED", "Not able to pass connection to server job.  Server job could not be started." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_SUBSYSTEM", "Not able to pass connection to server job.  Subsystem problem detected." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_TIMEOUT", "Not able to pass connection to server job.  Server job timed out." },  // @F1A
        { "EXC_CONNECTION_NOT_PASSED_UNKNOWN", "Not able to pass connection to server job.  Unknown or unrecoverable error occured." },  // @F1A
        { "EXC_CONNECTION_NOT_VALID", "Connection is not valid." },
        { "EXC_CONNECTION_PORT_CANNOT_CONNECT_TO", "Unable to connect to the port." },

        { "EXC_DATA_AREA_CHARACTER", "Attempted to use a character data area with a non-character data area object." }, //@A1A
        { "EXC_DATA_AREA_DECIMAL", "Attempted to use a decimal data area with a non-decimal data area object." }, //@A1A
        { "EXC_DATA_AREA_LOGICAL", "Attempted to use a logical data area with a non-logical data area object." }, //@A1A
        { "EXC_DATA_NOT_VALID", "Data is not valid." }, //@A1A
        { "EXC_DATA_QUEUE_KEYED", "Attempted to use keyed queue with a non-keyed data queue object." },
        { "EXC_DATA_QUEUE_NOT_KEYED", "Attempted to use a non-keyed data queue with a keyed data queue object." },
        { "EXC_DATA_STREAM_LEVEL_NOT_VALID", "Data stream level is not valid." },
        { "EXC_DATA_STREAM_SYNTAX_ERROR", "Syntax error in the data stream." },
        { "EXC_DATA_STREAM_UNKNOWN", "Data stream is not known." },

        { "EXC_DIRECTORY_ENTRY_ACCESS_DENIED", "Directory entry access denied." },
        { "EXC_DIRECTORY_ENTRY_DAMAGED", "Directory entry is damaged." },
        { "EXC_DIRECTORY_ENTRY_EXISTS", "Directory entry exists." },
        { "EXC_DIRECTORY_NAME_NOT_VALID", "Directory name not valid." },
        { "EXC_DIRECTORY_NOT_EMPTY", "Directory is not empty." },
        { "EXC_DISCONNECT_RECEIVED", "Disconnect request received, connection terminated." },

        { "EXC_EXIT_POINT_PROCESSING_ERROR", "Error occurred when processing exit point." },
        { "EXC_EXIT_PROGRAM_CALL_ERROR", "Error occurred when calling exit program." },
        { "EXC_EXIT_PROGRAM_DENIED_REQUEST", "Exit program denied request." },
        { "EXC_EXIT_PROGRAM_ERROR", "Error occurred in the exit program." },
        { "EXC_EXIT_PROGRAM_NOT_AUTHORIZED", "User is not authorized to exit program." },
        { "EXC_EXIT_PROGRAM_NOT_FOUND", "Exit program could not be found." },
        { "EXC_EXIT_PROGRAM_NUMBER_NOT_VALID", "Number of exit programs is not valid." },
        { "EXC_EXIT_PROGRAM_RESOLVE_ERROR", "Error occurred when resolving to exit program." },

        { "EXC_FILE_END", "End of file reached." },
        { "EXC_FILE_IN_USE", "File in use." },
        { "EXC_FILE_NOT_FOUND", "File was not found." },
        { "EXC_FILES_NOT_AVAILABLE", "No more files are available." },
        { "EXC_FILE_SUBSTREAM_IN_USE", "Substream in use." },

        { "EXC_GENERATE_TOKEN_AUTHORITY_INSUFFICIENT", "User is not authorized to generate a profile token for another user." },  // @F2A
        { "EXC_GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID", "Can not change the CCSID to use for EIM requests." },  // @F2A
        { "EXC_GENERATE_TOKEN_CAN_NOT_CONNECT", "Can not connect to the system EIM domain." },  // @F2A
        { "EXC_GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME", "Can not obtain the EIM registry name." },  // @F2A
        { "EXC_GENERATE_TOKEN_NO_MAPPING", "No mapping exists." },  // @F2A
        { "EXC_GENERATE_TOKEN_REQUEST_NOT_VALID", "Generate token request is not valid." },  // @F1A

        { "EXC_HANDLE_NOT_VALID", "Handle is not valid." },

        { "EXC_INTERNAL_ERROR", "Internal error occurred." },
        // @E5D { "EXC_IMPLEMENTATION_NOT_FOUND", "Implementation class not found." },

        { "EXC_KERBEROS_TICKET_NOT_VALID_CONSISTENCY", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_CREDANTIAL_STRUCTURE", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_EIM", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_MECHANISM", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_RETRIEVE", "Kerberos service ticket could not be retreived." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_SIGNATURE", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE", "Kerberos ticket is not valid." },  // @F1A
        { "EXC_KERBEROS_TICKET_NOT_VALID_VERIFICATION", "Kerberos ticket is not valid." },  // @F1A

        { "EXC_LIBRARY_AUTHORITY_INSUFFICIENT", "User is not authorized to library." },
        { "EXC_LIBRARY_DOES_NOT_EXIST", "Library does not exist." },
        { "EXC_LIBRARY_LENGTH_NOT_VALID", "Length of the library name is not valid." },
        { "EXC_LIBRARY_SPECIFICATION_NOT_VALID", "Library not specified correctly." },
        { "EXC_LOCK_VIOLATION", "Lock violation occurred." },

        { "EXC_MEMBER_LENGTH_NOT_VALID", "Length of the member name is not valid." },
        { "EXC_MEMBER_WITHOUT_FILE", "Member is not contained in a file." },

        { "EXC_OBJECT_ALREADY_EXISTS", "Object already exists." },
        { "EXC_OBJECT_AUTHORITY_INSUFFICIENT", "User is not authorized to object." },
        { "EXC_OBJECT_DOES_NOT_EXIST", "Object does not exist." },
        { "EXC_OBJECT_LENGTH_NOT_VALID", "Length of the object name is not valid." },
        { "EXC_OBJECT_TYPE_NOT_VALID", "Object type is not valid." },
        { "EXC_OBJECT_TYPE_UNKNOWN", "Object type is unknown." }, //@A1A

        { "EXC_PARAMETER_NOT_SUPPORTED", "Parameter is not supported." },
        { "EXC_PARAMETER_VALUE_NOT_SUPPORTED", "Parameter value is not supported." },
        { "EXC_PASSWORD_CHANGE_REQUEST_NOT_VALID", "Password change request is not valid." },
        { "EXC_PASSWORD_ERROR", "Password error." },
        { "EXC_PASSWORD_ENCRYPT_INVALID", "Password encryption indicator is not valid." },
        { "EXC_PASSWORD_EXPIRED", "Password is expired." },
        { "EXC_PASSWORD_IMPROPERLY_ENCRYPTED", "Password is improperly encrypted." },
        { "EXC_PASSWORD_INCORRECT", "Password is incorrect." },
        { "EXC_PASSWORD_INCORRECT_USERID_DISABLE", "Password is incorrect. User ID will be disabled after next incorrect sign-on." },
        { "EXC_PASSWORD_LENGTH_NOT_VALID", "Password length is not valid." },
        { "EXC_PASSWORD_NEW_ADJACENT_DIGITS", "New password has adjacent digits." },
        { "EXC_PASSWORD_NEW_CHARACTER_NOT_VALID", "New password contains a character that is not valid." },  // @F1A
        { "EXC_PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER", "New password contains a character repeated consecutively." },
        { "EXC_PASSWORD_NEW_DISALLOWED", "New password is not allowed." },
        { "EXC_PASSWORD_NEW_NO_ALPHABETIC", "New password must contain at least one alphabetic character." },
        { "EXC_PASSWORD_NEW_NO_NUMERIC", "New password must contain at least one numeric character." },
        { "EXC_PASSWORD_NEW_NOT_VALID", "New password is not valid." },
        { "EXC_PASSWORD_NEW_PREVIOUSLY_USED", "New password was previously used." },
        { "EXC_PASSWORD_NEW_REPEAT_CHARACTER", "New password contains a character used more than once." },
        { "EXC_PASSWORD_NEW_SAME_POSITION", "New password contains the same character in the same position as the previous password." },  // @E2A
        { "EXC_PASSWORD_NEW_TOO_LONG", "New password is too long." },
        { "EXC_PASSWORD_NEW_TOO_SHORT", "New password is too short." },
        { "EXC_PASSWORD_NEW_USERID", "New password contains user ID as part of the password." },
        { "EXC_PASSWORD_NEW_VALIDATION_PROGRAM", "Password validation program failed the request." },  // @F2A
        { "EXC_PASSWORD_NONE", "Password is *NONE." },  // @F1A
        { "EXC_PASSWORD_NOT_MATCH", "New password and confirm password are not the same." },
        { "EXC_PASSWORD_NOT_SET", "Password is not set." },
        { "EXC_PASSWORD_OLD_NOT_VALID", "Old password is not valid." },
        { "EXC_PASSWORD_PRE_V2R2", "Password has pre-V2R2 encryption." },  // @F1A
        { "EXC_PATH_NOT_FOUND", "Path name was not found." },
        { "EXC_PROFILE_TOKEN_NOT_VALID", "Profile token is not valid." },  // @F1A
        { "EXC_PROFILE_TOKEN_NOT_VALID_MAXIMUM", "Profile token is not valid.  Maximum number of profile tokens for the system already generated." },  // @F1A
        { "EXC_PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE", "Profile token is not valid.  Profile token is not regenerable." },  // @F1A
        { "EXC_PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID", "Profile token is not valid.  Timeout interval is not valid." },  // @F1A
        { "EXC_PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID", "Profile token is not valid.  Type of profile token is not valid." },  // @F1A
        { "EXC_PROTOCOL_ERROR", "Protocol error occurred." },

        { "EXC_QSYS_PREFIX_MISSING", "Object not in QSYS file system." },
        { "EXC_QSYS_SYNTAX_NOT_VALID", "Object in library QSYS specified incorrectly." },

        { "EXC_RANDOM_SEED_REQUIRED", "Random seed required when doing password substitute." },
        { "EXC_RANDOM_SEED_EXCHANGE_INVALID", "Invalid exchange random seed request." },
        { "EXC_RANDOM_SEED_INVALID", "Random seed is not valid." },
        { "EXC_REQUEST_DATA_ERROR", "Error in request data." },
        { "EXC_REQUEST_DENIED", "Request was denied." },
        { "EXC_REQUEST_ID_NOT_VALID", "Request ID is not valid." },
        { "EXC_REQUEST_NOT_SUPPORTED", "Request is not supported." },
        { "EXC_REQUEST_NOT_VALID", "Request is not valid." },
        { "EXC_RESOURCE_LIMIT_EXCEEDED", "Resource limit was exceeded." },
        { "EXC_RESOURCE_NOT_AVAILABLE", "Resource not available." },

        { "EXC_SECURITY_GENERAL", "General security error." },
        { "EXC_SECURITY_INVALID_STATE", "Internal error in the security manager." },
        { "EXC_SEND_REPLY_INVALID", "Send reply indicator is not valid." },
        { "EXC_SERVER_ID_NOT_VALID", "Server ID is not valid." },  // @F1A
        { "EXC_SERVER_NOT_STARTED", "Unable to start the server." },
        { "EXC_SHARE_VIOLATION", "Sharing violation occurred." },
        { "EXC_SIGNON_CANCELED", "Signon was canceled." },
        { "EXC_SIGNON_CONNECT_FAILED", "Failed to connect to signon server." },
        { "EXC_SIGNON_REQUEST_NOT_VALID", "Signon request is not valid." },
        { "EXC_SPECIAL_AUTHORITY_INSUFFICIENT", "User is not authorized for operation." },    // @E4A
        { "EXC_SPOOLED_FILE_NO_MESSAGE_WAITING", "Spooled file does not have a message waiting." },
        { "EXC_START_SERVER_REQUEST_NOT_VALID", "Start server request is not valid.  User ID or password may be missing." },
        { "EXC_START_SERVER_UNKNOWN_ERROR", "Unknown error starting server." },
        { "EXC_SYNTAX_ERROR", "Syntax error occurred." },
        { "EXC_SYSTEM_LEVEL_NOT_CORRECT", "Correct server level is required." },

        { "EXC_TOKEN_LENGTH_NOT_VALID", "Token length is not valid." },  // @F1A
        { "EXC_TOKEN_TYPE_NOT_VALID", "Token type is not valid." },  // @F1A
        { "EXC_TYPE_LENGTH_NOT_VALID", "Length of the object type is not valid." },

        { "EXC_UNEXPECTED_RETURN_CODE", "Unexpected return code." },
        { "EXC_UNEXPECTED_EXCEPTION", "Unexpected exception." }, // @B0A
        { "EXC_USERID_DISABLE", "User ID is disabled." },
        { "EXC_USERID_ERROR", "User ID error." },
        { "EXC_USERID_LENGTH_NOT_VALID", "User ID length is not valid." },
        { "EXC_USERID_MISMATCH", "User ID does not match authentication token." },  // @F2A
        { "EXC_USERID_NOT_SET", "User ID is not set." },
        { "EXC_USERID_UNKNOWN", "User ID is not known." },

        { "EXC_VALUE_CANNOT_CONVERT", "Value cannot be converted." },
        { "EXC_VRM_NOT_VALID", "Version Release Modification is not valid." },
        { "EXC_WRITER_JOB_ENDED", "Writer job has ended." },



        // #TRANNOTE #####################################################
        // #TRANNOTE Short descriptions and display names for properties.
        // #TRANNOTE Descriptions start with PROP_DESC_ prefix, display
        // #TRANNOTE names start with PROP_NAME.
        // #TRANNOTE #####################################################

        { "PROP_DESC_AFPR_FONTPELDENSITY_FILTER", "The filter that selects font resources by their pel density." },
        { "PROP_NAME_AFPR_FONTPELDENSITY_FILTER", "fontPelDensityFilter" },
        { "PROP_DESC_AFPR_NAME_FILTER", "The filter that selects Advanced Function Print resources by their integrated file system name." },
        { "PROP_NAME_AFPR_NAME_FILTER", "resourceFilter" },
        { "PROP_DESC_AFPR_SPLF_FILTER", "The filter that selects Advanced Function Print resources for the specified spooled file." },
        { "PROP_NAME_AFPR_SPLF_FILTER", "spooledFileFilter" },
        { "PROP_DESC_APPEND", "Indicates if an existing file is appended or replaced." },

        // #TRANNOTE Indicates if an existing file is appended or replaced.
        { "PROP_NAME_APPEND_PROP", "append" },
        { "PROP_NAME_AS400_CCSID", "characterSetID" },
        { "PROP_DESC_AS400_CCSID", "Code character set identifier." },
        { "PROP_NAME_AS400_GUI", "guiAvailable" },
        { "PROP_DESC_AS400_GUI", "User interface available." },
        { "PROP_NAME_AS400_SYSTEM", "systemName" },
        { "PROP_DESC_AS400_SYSTEM", "The name of the server." },
        { "PROP_NAME_AS400_DEFUSER", "useDefaultUserID" },
        { "PROP_DESC_AS400_DEFUSER", "Use the default user ID for signon." },
        { "PROP_NAME_AS400_PWCACHE", "usePasswordCache" },
        { "PROP_DESC_AS400_PWCACHE", "Use the password cache." },
        { "PROP_NAME_AS400_USERID", "userID" },
        { "PROP_DESC_AS400_USERID", "User ID." },
        { "PROP_NAME_AS400_PASSWORD", "password" },
        { "PROP_DESC_AS400_PASSWORD", "Password." },
        { "PROP_NAME_AS400_PROFILETOKEN", "profileToken" },  // @F1A
        { "PROP_DESC_AS400_PROFILETOKEN", "Profile token." },  // @F1A
        { "PROP_NAME_AS400_PROXYSERVER", "proxyServer" },  // @E2A
        { "PROP_DESC_AS400_PROXYSERVER", "Proxy server." },  // @E2A
        { "PROP_NAME_AS400_MUSTUSESOCKETS", "mustUseSockets" },  // @E2A
        { "PROP_DESC_AS400_MUSTUSESOCKETS", "Must use sockets." },  // @E2A
        { "PROP_NAME_AS400_SHOWCHECKBOXES", "showCheckboxes" },  // @E2A
        { "PROP_DESC_AS400_SHOWCHECKBOXES", "Show checkboxes." },  // @E2A
        { "PROP_NAME_AS400_THREADUSED", "threadUsed" },  // @E2A
        { "PROP_DESC_AS400_THREADUSED", "Thread used." },  // @E2A
        { "PROP_NAME_SECUREAS400_KEYRINGNAME", "keyRingName" },  // @E2A
        { "PROP_DESC_SECUREAS400_KEYRINGNAME", "Key ring name." },  // @E2A
        { "PROP_NAME_SECUREAS400_KEYRINGPASSWORD", "keyRingPassword" },  // @E2A
        { "PROP_DESC_SECUREAS400_KEYRINGPASSWORD", "Key ring password." },  // @E2A
        { "PROP_NAME_SECUREAS400_PROXYENCRYPTIONMODE", "proxyEncryptionMode" },  // @E2A
        { "PROP_DESC_SECUREAS400_PROXYENCRYPTIONMODE", "Proxy encryption mode." },  // @E2A
        { "PROP_DESC_AS400ARRAY_SIZE", "The number of elements in the array." },
        { "PROP_NAME_AS400ARRAY_SIZE", "numberOfElements" },
        { "PROP_DESC_AS400ARRAY_TYPE", "The type of the array." },
        { "PROP_NAME_AS400ARRAY_TYPE", "type" },
        { "PROP_DESC_AS400STRUCTURE_MEMBERS", "The data types of the structure members." },
        { "PROP_NAME_AS400STRUCTURE_MEMBERS", "members" },
        { "PROP_DESC_EXISTENCE_OPTION", "Specifies what to do if the file exists." },

        // #TRANNOTE Specifies what to do if the file exists.
        { "PROP_NAME_EXISTENCE_OPTION", "existenceOption" },
        { "PROP_NAME_FD", "FD" },
        { "PROP_DESC_FIELD_DESCRIPTIONS", "The field descriptions for this record format." },

        // #TRANNOTE The field descriptions for this record format.
        { "PROP_NAME_FIELD_DESCRIPTIONS", "fieldDescriptions" },
        { "PROP_DESC_FIELD_NAMES", "The field names of the fields in this record format." },

        // #TRANNOTE The field names of the fields in this record format.
        { "PROP_NAME_FIELD_NAMES", "fieldNames" },
        { "PROP_DESC_FIELDS", "The field values for the fields in this record." },

        // #TRANNOTE The field values for the fields in this record.
        { "PROP_NAME_FIELDS", "fields" },
        { "PROP_DESC_FILE_DESCRIPTOR", "A file descriptor for an open file." },

        // #TRANNOTE A file descriptor for an open file.
        { "PROP_NAME_FILE_DESCRIPTOR", "FD" },
        { "PROP_DESC_FILE_NAME", "The name of the file." },
        { "PROP_NAME_FILE_NAME", "fileName" },
        { "PROP_DESC_KEY_FIELD_DESCRIPTIONS", "The key field descriptions for this record format." },

        // #TRANNOTE The key field descriptions for this record format.
        { "PROP_NAME_KEY_FIELD_DESCRIPTIONS", "keyFieldDescriptions" },
        { "PROP_DESC_KEY_FIELD_NAMES", "The field names of the key fields in this record format." },

        // #TRANNOTE The field names of the key fields in this record format.
        { "PROP_NAME_KEY_FIELD_NAMES", "keyFieldNames" },
        { "PROP_DESC_KEY_FIELDS", "The field values for the key fields in this record." },

        // #TRANNOTE The field values for the key fields in this record.
        { "PROP_NAME_KEY_FIELDS", "keyFields" },
        { "PROP_DESC_LIBRARY", "The name of the library in which this object resides." },

        // #TRANNOTE The name of the library.
        { "PROP_NAME_LIBRARY", "libraryName" },
        { "PROP_DESC_MEMBER", "The name of the file member." },

        // #TRANNOTE The name of the file member.
        { "PROP_NAME_MEMBER", "memberName" },
        { "PROP_DESC_MODE", "The access mode." },

        // #TRANNOTE The access mode.
        { "PROP_NAME_MODE", "mode" },
        { "PROP_DESC_OBJECT", "The name of the object." },

        // #TRANNOTE The name of the object.
        { "PROP_NAME_OBJECT", "objectName" },
        { "PROP_DESC_OUTQ_NAME_FILTER", "The filter that selects output queues by their integrated file system name." },
        { "PROP_NAME_OUTQ_NAME_FILTER", "queueFilter" },
        { "PROP_DESC_PATH", "The integrated file system name of the object." },

        // #TRANNOTE The integrated file system name of the object.
        { "PROP_NAME_PATH", "path" },
        { "PROP_DESC_PRTD_NAME_FILTER", "The filter that selects printers by their name." },
        { "PROP_NAME_PRTD_NAME_FILTER", "printerFilter" },
        { "PROP_NAME_PRTD_NAME", "printerName" },
        { "PROP_DESC_PRTD_NAME", "The name of the printer." },
        { "PROP_DESC_PRTF_NAME_FILTER", "The filter that selects printer files by their integrated file system name." },
        { "PROP_NAME_PRTF_NAME_FILTER", "printerFileFilter" },
        { "PROP_DESC_RECORD_FORMAT", "The record format for the object." },

        // #TRANNOTE The record format for the object.
        { "PROP_NAME_RECORD_FORMAT", "recordFormat" },
        { "PROP_DESC_RECORD_FORMAT_NAME", "The name of the record format." },

        // #TRANNOTE The name of the record format.
        { "PROP_NAME_RECORD_FORMAT_NAME", "name" },
        { "PROP_DESC_RECORD_NAME", "The name of the record." },

        // #TRANNOTE The name of the record.
        { "PROP_NAME_RECORD_NAME", "recordName" },
        { "PROP_DESC_RECORD_NUMBER", "The record number of the record." },

        // #TRANNOTE The record number of the record.
        { "PROP_NAME_RECORD_NUMBER", "recordNumber" },
        { "PROP_DESC_SHARE_OPTION", "Specifies how the file is shared." },

        // #TRANNOTE Specifies how the file is shared.
        { "PROP_NAME_SHARE_OPTION", "shareOption" },
        { "PROP_DESC_SPLF_FORMTYPE_FILTER", "The filter that selects spooled files by their form type." },
        { "PROP_NAME_SPLF_FORMTYPE_FILTER", "formTypeFilter" },
        { "PROP_DESC_SPLF_OUTQ_FILTER", "The filter that selects spooled files by the integrated file system name of the output queue containing them." },
        { "PROP_NAME_SPLF_OUTQ_FILTER", "queueFilter" },
        { "PROP_DESC_SPLF_USER_FILTER", "The filter that selects spooled files by the user that created them." },
        { "PROP_NAME_SPLF_USER_FILTER", "userFilter" },
        { "PROP_DESC_SPLF_USERDATA_FILTER", "The filter that selects spooled files by their user data." },
        { "PROP_NAME_SPLF_USERDATA_FILTER", "userDataFilter" },
        { "PROP_DESC_SYSTEM", "The system on which the object resides." },
        { "PROP_NAME_SYSTEM", "system" },
        { "PROP_DESC_TYPE", "The type of object." },

        // #TRANNOTE The type of object.
        { "PROP_NAME_TYPE", "objectType" },
        { "PROP_DESC_WRTJ_NAME_FILTER", "The filter that selects writer jobs by their name." },
        { "PROP_NAME_WRTJ_NAME_FILTER", "writerFilter" },
        { "PROP_DESC_WRTJ_OUTQ_FILTER", "The filter that selects writer jobs by the integrated file system name of the output queue being processed." },
        { "PROP_NAME_WRTJ_OUTQ_FILTER", "queueFilter" },
        { "PROP_DESC_COMMAND", "The command to run on the server." },
        { "PROP_NAME_COMMAND", "command" },
        { "PROP_DESC_PROGRAM", "The integrated file system name of the program to run." },
        { "PROP_NAME_PROGRAM", "program" },
        { "PROP_DESC_SUCCESSFUL", "Indicates if the requested action was successful." },
        { "PROP_NAME_SUCCESSFUL", "successful" },
        { "PROP_DESC_PARMLIST", "The list of parameters for the program." },
        { "PROP_NAME_PARMLIST", "parameterList" },
        { "PROP_DESC_PARMINPUT", "The input data for a parameter." },
        { "PROP_NAME_PARMINPUT", "inputData" },
        { "PROP_DESC_PARMOUTPUT", "The output data for a parameter." },
        { "PROP_NAME_PARMOUTPUT", "outputData" },
        { "PROP_DESC_PARMOUTPUTLEN", "The length of the output data returned for a parameter." },
        { "PROP_NAME_PARMOUTPUTLEN", "outputDataLength" },
        { "PROP_DESC_NAME", "The name of the object." },
        { "PROP_NAME_NAME", "name" },
        { "PROP_DESC_DQATTRIBUTES", "The attributes of the data queue." },
        { "PROP_NAME_DQATTRIBUTES", "attributes" },
        { "PROP_DESC_ENTRYLENGTH", "The maximum number of bytes per data queue entry." },
        { "PROP_NAME_ENTRYLENGTH", "maxEntryLength" },
        { "PROP_DESC_AUTHORITY", "The public authority for the data queue." },
        { "PROP_NAME_AUTHORITY", "authority" },
        { "PROP_DESC_SAVESENDERINFO", "Indicates if information about the origin of each entry is saved." },
        { "PROP_NAME_SAVESENDERINFO", "saveSenderInfo" },
        { "PROP_DESC_FIFO", "Indicates if entries on the queue are read in FIFO or LIFO order." },
        { "PROP_NAME_FIFO", "FIFO" },
        { "PROP_DESC_FORCETOAUX", "Indicates if data is forced to auxiliary storage before returning." },
        { "PROP_NAME_FORCETOAUX", "forceToAuxiliaryStorage" },
        { "PROP_DESC_DESCRIPTION", "The text description for the data queue." },
        { "PROP_NAME_DESCRIPTION", "description" },
        { "PROP_DESC_KEYLENGTH", "The number of bytes in the data queue key." },
        { "PROP_NAME_KEYLENGTH", "keyLength" },

        { "PROP_DESC_PARMTYPE", "The program parameter type." },                //@C1A
        { "PROP_NAME_PARMTYPE", "parameterType" },                              //@C1A
        { "PROP_DESC_PARMPROCEDURE", "The name of procedure." },                //@C1A
        { "PROP_NAME_PARMPROCEDURE", "procedureName" },                         //@C1A
        { "PROP_DESC_PARMRETURNFORMAT", "The format of the returned value." },  //@C1A
        { "PROP_NAME_PARMRETURNFORMAT", "returnValueFormat" },                  //@C1A

        // @E1 new for user space
        { "PROP_DESC_US_MUSTUSEPGMCALL", "Use ProgramCall to read and write user space data." }, //@E1a
        { "PROP_NAME_US_MUSTUSEPGMCALL", "mustUseProgramCall" },                 //@E1A


        // Proxy support MRI.     @B0A
        // #TRANNOTE ################################################################
        // #TRANNOTE The following are error and informational (verbose) messages.
        // #TRANNOTE ################################################################
        { "PROXY_CONNECTION_CLOSED",          "Connection &0 closed."},
        { "PROXY_CONNECTION_ACCEPTED",        "&0 accepted connection requested by &1 as connection &2." },
        { "PROXY_CONNECTION_REDIRECTED",      "&0 rejected connection requested by &1 and redirected to peer &2." },
        { "PROXY_CONNECTION_REJECTED",        "&0 rejected connection requested by &1.  No peer was suggested." },

        { "EXC_PROXY_CONNECTION_NOT_ESTABLISHED", "A connection to the proxy server cannot be established." },
        { "EXC_PROXY_CONNECTION_DROPPED",         "The connection to the proxy server was dropped." },
        { "EXC_PROXY_CONNECTION_REJECTED",        "The connection to the proxy server was not accepted by the proxy server." },
        { "EXC_PROXY_VERSION_MISMATCH",           "The client and proxy server are running different versions of code." }, // @E6A
    };
}
