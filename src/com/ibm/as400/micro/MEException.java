///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MEException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;


/**
 The MEException class represents an exception that indicates an error occured while processing the ToolboxME request.
 **/
public final class MEException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    /**
     *  This return code indicates that a security or authority error has occurred. 
     **/
    public static final int AS400_SECURITY_EXCEPTION = 1;

    /**
     *  The return code indicating that there is a general password error.
     **/
    public static final int PASSWORD_ERROR = 2;
    
    /**
     *  The return code indicating that the password has expired.
     **/
    public static final int PASSWORD_EXPIRED = 3;
    
    /**
     *  The return code indicating that the password is not correct.
     **/
    public static final int PASSWORD_INCORRECT = 4;

    /**
     *  The return code indicating that the user ID is not set.
     **/
    public static final int USERID_NOT_SET = 5;

    /**
     *  The return code indicating that the user ID has been disabled by the server.
     **/
    public static final int USERID_DISABLE = 6;
    
    /**
     *  The return code indicating that the user ID is not known by the server.
     **/
    public static final int USERID_UNKNOWN = 7;

    /**
     *  The return code indicating that the object already exists.
     **/
    public static final int OBJECT_ALREADY_EXISTS = 8;

    /**
     *  The return code indicating that the object does not exist.
     **/
    public static final int OBJECT_DOES_NOT_EXIST = 9;

    /**
     *  The return code indicating that the parameter value is not valid.
     **/
    public static final int PARAMETER_VALUE_NOT_VALID = 10;

    /**
     *  The return code indicating that the result set is closed.
     **/
     public static final int RESULT_SET_CLOSED = 11;

    /**
     *  The return code indicating that the pcml document was not registered with the MEServer. 
     **/
    public static final int PROGRAM_NOT_REGISTERED = 12;

    /**
     *  The return code indicating that the program call failed.  
     **/
    public static final int PROGRAM_FAILED = 13;

    /**
     *  The return code indicating that an attempt was made to use a data area with an invalid data area object type.  
     **/
    public static final int ILLEGAL_OBJECT_TYPE = 93;
    
    /**
     *  The return code indicating that the connection was dropped unexpectedly.
     **/
    public static final int CONNECTION_DROPPED = 94;

    /**
     *  The return code indicating that it was unable to start the server.
     **/
    public static final int SERVER_NOT_STARTED = 95;
    
    /**
     *  The return code indicating that the host is unknown.
     **/
    public static final int UNKNOWN_HOST = 96;

    /**
     *  The return code indicating that the proxy server has already been started.
     **/
    public static final int ME_SERVER_ALREADY_STARTED = 97;

   /**
     *   The return code indicating that the proxy server has not been started.
     **/
    public static final int ME_SERVER_NOT_STARTED = 98;

    /**
     *  The return code indicating that an unknown error occurred.
     **/
    public static final int UNKNOWN = 99;

    
    // Private Data

    private int returnCode_;  // Return code associated with this exception.


    /**
     *  Construct a MEException.
     *
     *  @param message The exception text.
     *  @param returnCode The return code which identifies the message to be returned. Possible values are defined as constants on this class. 
     **/
    MEException(String message, int returnCode)
    {
        super(message);
        returnCode_ = returnCode;
    }


    /**
     *  Returns the return code associated with this exception.
     *
     *  @return  The return code.
     **/
    public int getReturnCode()
    {
        return returnCode_;
    }
}
