///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServerStartupException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
  The ServerStartupException class represents an exception that indicates that a server could not be started.
**/
public class ServerStartupException extends IOException implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4;
    

    private int rc_;  // Return code associated with this exception

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

    // Return code values used by this class:
    //   If a value is added here, it must also be added to MRI.properties.

    /**
      The return code indicating that it was unable to establish a connection.
     **/
    public static final int CONNECTION_NOT_ESTABLISHED = 1;
    /**
      The return code indicating that it was unable to connect to the port.
     **/
    public static final int CONNECTION_PORT_CANNOT_CONNECT_TO = 2;
    /**
      The return code indicating that it was unable to start the server.
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
      The return code indicating that it was password encryption indicator is not valid.
     **/
    public static final int PASSWORD_ENCRYPT_INVALID = 9;
    /**
      The return code indicating that it was user ID length is not valid.
     **/
    public static final int USERID_LENGTH_NOT_VALID = 10;
    /**
      The return code indicating that it was password length is not valid.
     **/
    public static final int PASSWORD_LENGTH_NOT_VALID = 11;
    /**
      The return code indicating that it was send reply indicator is not valid.
     **/
    public static final int SEND_REPLY_INVALID = 12;
    /**
      The return code indicating that it was start server request is not valid.
     **/
    public static final int START_SERVER_REQUEST_NOT_VALID = 13;
    /**
      The return code indicating that it was an error in request data.
     **/
    public static final int REQUEST_DATA_ERROR = 14;
    /**
      The return code indicating that it was an unknown error starting server.
     **/
    public static final int START_SERVER_UNKNOWN_ERROR = 15;

    /**
      Constructs a ServerStartupException object.  It indicates that the server can not be started.  Exception message will look like this: Unable to connect to the port.
      @param  returnCode  The return code which identifies the message to be returned.
     **/
    ServerStartupException(int returnCode)
    {
        // Create the message
	super(loader_.getText(getMRIKey(returnCode)));
	rc_ =  returnCode;
    }

    /**
      Constructs a ServerStartupException object.  It indicates that server can not be started.  Exception message will look like this:  AS/400 ROI Server: Unable to start the server.
      @param  name  The name of the server which could not be started.
      @param  returnCode  The return code which identifies the message to be returned.
     **/
    ServerStartupException(String name, int returnCode)
    {
        // Create the message
	super(name + ": " + loader_.getText(getMRIKey(returnCode)));
	rc_ =  returnCode;
    }

    /**
      Returns the text associated with the return code.
      @param  returnCode  The return code associated with this exception.
      @return  The text string which describes the error.
     **/
    static String getMRIKey(int returnCode)  // This method is required so the message can be created and sent in super()
    {
	switch(returnCode)
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
