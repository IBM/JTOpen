///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: InternalErrorException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   The InternalErrorException class represents an exception
   that indicates that an internal error has occurred.  Instances
   of this class represent problems in the supplied code.  Contact your
   service representative to report the problem.
**/
public class InternalErrorException extends RuntimeException
implements ReturnCodeException {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

    private int rc_;  // Return code associated with this exception

    //  Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;


    // Return code values used by this class.
    // If a value is added here, it must also be added to MRI.properties.
    /**
       The return code indicating that  
       the server job cannot support the level of data streams that the client
       is using.
    **/
    public static final int DATA_STREAM_LEVEL_NOT_VALID = 1;
    /**
       The return code indicating that  
       the data stream received from the server job was not of the
       type or format expected.
    **/
    public static final int DATA_STREAM_UNKNOWN = 2;
    /**
       The return code indicating that  
       the password has not been encrypted properly (pre-V2R2 encryption may
       have been used).
    **/
    public static final int PASSWORD_IMPROPERLY_ENCRYPTED = 3;
    /**
       The return code indicating that  
       a protocol error occurred while communicating with the system.
    **/
    public static final int PROTOCOL_ERROR = 4;
    /**
       The return code indicating that  
       the server job received a data stream that had 
       data or a format that was not valid.
    **/
    public static final int SYNTAX_ERROR = 5;
    /**
       The return code indicating that  
       the exact cause of the failure is not known.  The detailed message
       may contain additional information.
    **/
    public static final int UNKNOWN = 6;
    /**
       The return code indicating that  
       the server job cannot support this version of the client.
    **/
    public static final int VRM_NOT_VALID = 7;
    /**
       The return code indicating that  
       the security manager is in a state that is not valid.
    **/
    public static final int SECURITY_INVALID_STATE = 8;
    /**
       The return code indicating that  
       an unexpected return code was received.
    **/
    public static final int UNEXPECTED_RETURN_CODE = 9;    
    /**
       The return code indicating that  
       an unexpected exception was received.
    **/
    public static final int UNEXPECTED_EXCEPTION = 10;  // @D0A



    /**
       Constructs an InternalErrorException object.
       It indicates that an internal error has occurred.
       @param returnCode The return code which identifies the message to be returned.
    **/
    public InternalErrorException(int returnCode) {	// @D1C
        // Create the message
        super(loader_.getText(getMRIKey(returnCode)));
        rc_ =  returnCode;

    }

    /**
      Constructs an InternalErrorException object. It indicates that
      an internal error has occurred.  The value is displayed at the end
      of the message.
      @param returnCode The return code which identifies the message to be displayed.
      @param value  The value to display at the end of the message
    **/
    InternalErrorException(int returnCode, int value) {
        // Create the message
        super(loader_.getText(getMRIKey(returnCode)) + " - " + String.valueOf(value));
        rc_ = returnCode;
    }

    /**
      Constructs an InternalErrorException object. It indicates that
      an internal error has occurred.  The string and value are displayed
      at the end of the message.
      @param returnCode The return code which identifies the message to be displayed.
      @param text  The string to add to the end of the message
      @param value The value to display at the end of the message
    **/
    InternalErrorException(int returnCode, String text, int value) {
        super(loader_.getText(getMRIKey(returnCode)) + " " + text + " - " + String.valueOf(value));
        rc_ = returnCode;
    }

    /**
       Constructs an InternalErrorException object.
       It indicates that an internal error has occurred.
       @param errorInfo Additional error information.
       @param returnCode The return code which identifies the message to be returned.
    **/
    InternalErrorException(String pathname, int returnCode) { 
        // Create the message
        super(pathname + ": " + loader_.getText(getMRIKey(returnCode)));
        rc_ =  returnCode;
    }



    
    /**
       Returns the text associated with the return code.
       @param returnCode  The return code associated with this exception.
       @return The text string which describes the error.
    **/
    // This method is required so the message can be created and sent in super()
    static String getMRIKey (int returnCode) {
        switch (returnCode) {
        case DATA_STREAM_LEVEL_NOT_VALID:
            return "EXC_DATA_STREAM_LEVEL_NOT_VALID";
        case DATA_STREAM_UNKNOWN:
            return "EXC_DATA_STREAM_UNKNOWN";
        case PASSWORD_IMPROPERLY_ENCRYPTED:
            return "EXC_PASSWORD_IMPROPERLY_ENCRYPTED";
        case PROTOCOL_ERROR:
            return "EXC_PROTOCOL_ERROR";
        case SYNTAX_ERROR:
            return "EXC_SYNTAX_ERROR";
        case UNKNOWN :
            return "EXC_UNKNOWN";
        case VRM_NOT_VALID:
            return "EXC_VRM_NOT_VALID";
        case SECURITY_INVALID_STATE:
            return "EXC_SECURITY_INVALID_STATE";
        case UNEXPECTED_RETURN_CODE:
            return "EXC_UNEXPECTED_RETURN_CODE";
        case UNEXPECTED_EXCEPTION:                        // @D0A
            return "EXC_UNEXPECTED_EXCEPTION";         // @D0A
        default:
            return "EXC_UNKNOWN";   // Bad return code was provided.
        }
    }


    /**
       Returns the return code associated with this exception.
       @return The return code.
    **/
    public int getReturnCode () {
        return rc_;
    }




}  // End of InternalErrorException
