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
 *  The MEException class represents an exception that indicates an error occured 
 *  while processing the ToolboxME for iSeries request.
 **/
public final class MEException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // THE SECURITY RETURN CODES (range 0 - 20).

    /**
     *  This return code indicates that a security or authority error has occurred. 
     **/
    public static final int AS400_SECURITY_EXCEPTION = 0x0001;

    /**
     *  The return code indicating that there is a general password error.
     **/
    public static final int PASSWORD_ERROR = 0x0002;
    
    /**
     *  The return code indicating that the password has expired.
     **/
    public static final int PASSWORD_EXPIRED = 0x0003;
    
    /**
     *  The return code indicating that the password is not correct.
     **/
    public static final int PASSWORD_INCORRECT = 0x0004;

    /**
     *  The return code indicating that the user ID is not set.
     **/
    public static final int USERID_NOT_SET = 0x0005;

    /**
     *  The return code indicating that the user ID has been disabled by the server.
     **/
    public static final int USERID_DISABLE = 0x0006;
    
    /**
     *  The return code indicating that the user ID is not known by the server.
     **/
    public static final int USERID_UNKNOWN = 0x0007;

    /**
     *  The return code indicating that the object already exists.
     **/
    public static final int OBJECT_ALREADY_EXISTS = 0x0008;

    /**
     *  The return code indicating that the object does not exist.
     **/
    public static final int OBJECT_DOES_NOT_EXIST = 0x0009;

    /**
     *  The return code indicating that the parameter value is not valid.
     **/
    public static final int PARAMETER_VALUE_NOT_VALID = 0x0010;

    /**
     *  The return code indicating that one or more properties have not been set.
     **/
    public static final int PROPERTY_NOT_SET= 0x0011;

    /**
     *  The return code indicating that the iSeries resource has a length that is not valid or cannot be handled through this interface.
     **/
    public static final int LENGTH_NOT_VALID = 0x0012;                                     
    
    // THE COMMUNICTION/SERVER RETURN CODES (range 20 - 39).

    /**
     *  The return code indicating that the connection was dropped unexpectedly.
     **/
    public static final int CONNECTION_DROPPED = 0x0020;

    /**
     *  The return code indicating that it was unable to start the server.
     **/
    public static final int SERVER_NOT_STARTED = 0x0021;
    
    /**
     *  The return code indicating that the host is unknown.
     **/
    public static final int UNKNOWN_HOST = 0x0022;

    /**
     *  The return code indicating that the MEServer has already been started.
     **/
    public static final int ME_SERVER_ALREADY_STARTED = 0x0023;

   /**
     *   The return code indicating that the MEServer has not been started.
     **/
    public static final int ME_SERVER_NOT_STARTED = 0x0024;

    /**
     *  The return code indicating that the pcml document was not registered with the MEServer. 
     **/
    public static final int PROGRAM_NOT_REGISTERED = 0x0025;

    /**
     *  The return code indicating that the program call failed.  
     **/
    public static final int PROGRAM_FAILED = 0x0026;

    /**
     *  The return code indicating that an attempt was made to use a data queue with an invalid data queue object type.  
     **/
    public static final int ILLEGAL_OBJECT_TYPE = 0x0027;

    /**
     *  The return code indicating that a pcml exception occurred.
     **/
    public static final int PCML_EXCEPTION = 0x0028;

    /**
     *  The return code indicating that an unknown error occurred.
     **/
    public static final int UNKNOWN = 0x0035;
    

    // THE SQL RETURN CODES (range 40 - 59).
    
    /**
     *  The return code indicating that the result set is closed.
     **/
     public static final int RESULT_SET_CLOSED = 0x0040;

    
    // Private Data

    private int returnCode_;  // Return code associated with this exception.


    /**
     *  Construct an MEException.
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
