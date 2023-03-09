///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ServerStartupException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 The ServerStartupException class represents an exception that indicates that a host server could not be started.
 **/
public class ServerStartupException extends IOException implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4;

    private int rc_;  // Return code associated with this exception.

    /**
     The return code indicating that it was unable to establish a connection.
     **/
    public static final int CONNECTION_NOT_ESTABLISHED = 1;
    /**
     The return code indicating that it was unable to connect to the port.
     **/
    public static final int CONNECTION_PORT_CANNOT_CONNECT_TO = 2;
    /**
     The return code indicating that it was unable to start the host server.
     **/
    public static final int SERVER_NOT_STARTED = 3;
    /**
     The return code indicating that it was unable to connect to signon server.
     **/
    public static final int SIGNON_CONNECT_FAILED = 4;
    /**
     The return code indicating that it was an invalid exchange random seed request.
     **/
    public static final int RANDOM_SEED_EXCHANGE_INVALID = 5;
    /**
     The return code indicating that it was a request ID not valid.
     **/
    public static final int REQUEST_ID_NOT_VALID = 6;
    /**
     The return code indicating that it was a random seed not valid.
     **/
    public static final int RANDOM_SEED_INVALID = 7;
    /**
     The return code indicating that it was a random seed required when doing password substitute.
     **/
    public static final int RANDOM_SEED_REQUIRED = 8;
    /**
     The return code indicating that the password encryption indicator is not valid.
     **/
    public static final int PASSWORD_ENCRYPT_INVALID = 9;
    /**
     The return code indicating that the user ID length is not valid.
     **/
    public static final int USERID_LENGTH_NOT_VALID = 10;
    /**
     The return code indicating that the password length is not valid.
     **/
    public static final int PASSWORD_LENGTH_NOT_VALID = 11;
    /**
     The return code indicating that the send reply indicator is not valid.
     **/
    public static final int SEND_REPLY_INVALID = 12;
    /**
     The return code indicating that the start host server request is not valid.
     **/
    public static final int START_SERVER_REQUEST_NOT_VALID = 13;
    /**
     The return code indicating that it was an error in request data.
     **/
    public static final int REQUEST_DATA_ERROR = 14;
    /**
     The return code indicating that there was an unknown error when starting the host server.
     **/
    public static final int START_SERVER_UNKNOWN_ERROR = 15;
    /**
     The return code indicating that a host server ID is not valid.
     **/
    public static final int SERVER_ID_NOT_VALID = 16;
    /**
     The return code indicating that there was an error passing the connection to the server job. Program data length is incorrect.
     **/
    public static final int CONNECTION_NOT_PASSED_LENGTH = 17;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Server job timed out.
     **/
    public static final int CONNECTION_NOT_PASSED_TIMEOUT = 18;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Server job could not be started.
     **/
    public static final int CONNECTION_NOT_PASSED_SERVER_NOT_STARTED = 19;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Prestart job could not be started.
     **/
    public static final int CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED = 20;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Subsystem problem detected.
     **/
    public static final int CONNECTION_NOT_PASSED_SUBSYSTEM = 21;
    /**
     The return code indicating that there was an error passing the connection to the server job. Server job is ending.
     **/
    public static final int CONNECTION_NOT_PASSED_SERVER_ENDING = 22;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Receiver area is too small.
     **/
    public static final int CONNECTION_NOT_PASSED_RECEIVER_AREA = 23;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Unknown or unrecoverable error occured.
     **/
    public static final int CONNECTION_NOT_PASSED_UNKNOWN = 24;
    /**
     The return code indicating that there was an error passing the connection to the server job.  User profile for server job does not exist.
     **/
    public static final int CONNECTION_NOT_PASSED_PROFILE = 25;
    /**
     The return code indicating that there was an error passing the connection to the server job.  User profile for server job does not have enough authority.
     **/
    public static final int CONNECTION_NOT_PASSED_AUTHORITY = 26;
    /**
     The return code indicating that there was an error passing the connection to the server job. Server job program was not found.
     **/
    public static final int CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND = 27;
    /**
     The return code indicating that there was an error passing the connection to the server job. Daemon job is not authorized to server job library.
     **/
    public static final int CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY = 28;
    /**
     The return code indicating that there was an error passing the connection to the server job.  Daemon job is not authorized to server job program.
     **/
    public static final int CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY = 29;

    // Constructs a ServerStartupException object.  It indicates that the host server can not be started.  Exception message will look like this: Unable to connect to the port.
    // @param  returnCode  The return code which identifies the message to be returned.
    ServerStartupException(int returnCode)
    {
        // Create the message.
        super(ResourceBundleLoader.getText(getMRIKey(returnCode)));
        rc_ =  returnCode;
    }

    // Returns the text associated with the return code.
    // @param  returnCode  The return code associated with this exception.
    // @return  The text string which describes the error.
    // This method is required so the message can be created and sent in super().
    static String getMRIKey(int returnCode)
    {
        switch (returnCode)
        {
            case CONNECTION_NOT_ESTABLISHED:
                return "EXC_CONNECTION_NOT_ESTABLISHED";
            case CONNECTION_PORT_CANNOT_CONNECT_TO:
                return "EXC_CONNECTION_PORT_CANNOT_CONNECT_TO";
            case SERVER_NOT_STARTED:
                return "EXC_SERVER_NOT_STARTED";
            case SIGNON_CONNECT_FAILED:
                return "EXC_SIGNON_CONNECT_FAILED";
            case RANDOM_SEED_EXCHANGE_INVALID:
                return "EXC_RANDOM_SEED_EXCHANGE_INVALID";
            case REQUEST_ID_NOT_VALID:
                return "EXC_REQUEST_ID_NOT_VALID";
            case RANDOM_SEED_INVALID:
                return "EXC_RANDOM_SEED_INVALID";
            case RANDOM_SEED_REQUIRED:
                return "EXC_RANDOM_SEED_REQUIRED";
            case PASSWORD_ENCRYPT_INVALID:
                return "EXC_PASSWORD_ENCRYPT_INVALID";
            case USERID_LENGTH_NOT_VALID:
                return "EXC_USERID_LENGTH_NOT_VALID";
            case PASSWORD_LENGTH_NOT_VALID:
                return "EXC_PASSWORD_LENGTH_NOT_VALID";
            case SEND_REPLY_INVALID:
                return "EXC_SEND_REPLY_INVALID";
            case START_SERVER_REQUEST_NOT_VALID:
                return "EXC_START_SERVER_REQUEST_NOT_VALID";
            case REQUEST_DATA_ERROR:
                return "EXC_REQUEST_DATA_ERROR";
            case START_SERVER_UNKNOWN_ERROR:
                return "EXC_START_SERVER_UNKNOWN_ERROR";
            case SERVER_ID_NOT_VALID:
                return "EXC_SERVER_ID_NOT_VALID";
            case CONNECTION_NOT_PASSED_LENGTH:
                return "EXC_CONNECTION_NOT_PASSED_LENGTH";
            case CONNECTION_NOT_PASSED_TIMEOUT:
                return "EXC_CONNECTION_NOT_PASSED_TIMEOUT";
            case CONNECTION_NOT_PASSED_SERVER_NOT_STARTED:
                return "EXC_CONNECTION_NOT_PASSED_SERVER_NOT_STARTED";
            case CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED:
                return "EXC_CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED";
            case CONNECTION_NOT_PASSED_SUBSYSTEM:
                return "EXC_CONNECTION_NOT_PASSED_SUBSYSTEM";
            case CONNECTION_NOT_PASSED_SERVER_ENDING:
                return "EXC_CONNECTION_NOT_PASSED_SERVER_ENDING";
            case CONNECTION_NOT_PASSED_RECEIVER_AREA:
                return "EXC_CONNECTION_NOT_PASSED_RECEIVER_AREA";
            case CONNECTION_NOT_PASSED_UNKNOWN:
                return "EXC_CONNECTION_NOT_PASSED_UNKNOWN";
            case CONNECTION_NOT_PASSED_PROFILE:
                return "EXC_CONNECTION_NOT_PASSED_PROFILE";
            case CONNECTION_NOT_PASSED_AUTHORITY:
                return "EXC_CONNECTION_NOT_PASSED_AUTHORITY";
            case CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND:
                return "EXC_CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND";
            case CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY:
                return "EXC_CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY";
            case CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY:
                return "EXC_CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY";
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
