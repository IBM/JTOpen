///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ErrorCompletingRequestException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
   The ErrorCompletingRequestException class represents
   an exception that indicates an error occurred
   that prevented the request from completing.
**/
public class ErrorCompletingRequestException extends Exception
                                    implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

   private int rc_;  // Return code associated with this exception
    
   //  Handles loading the appropriate resource bundle
   private static ResourceBundleLoader loader_;  
  

    // Return code values used by this class. 
    // If a value is added here, it must also be added to MRI.properties.


    /**
       The return code indicating that 
       an error has occurred on the system.
    **/
    public static final int AS400_ERROR = 1;
    /**
       The return code indicating that 
       an error occurred while processing the exit point.
    **/
    public static final int EXIT_POINT_PROCESSING_ERROR = 2;
    /**
       The return code indicating that 
       an error occurred with the user exit program call.
    **/
    public static final int EXIT_PROGRAM_CALL_ERROR = 3; 
    /**
       The return code indicating that 
       the user exit program associated with the server job rejected
       the request.
    **/
    public static final int EXIT_PROGRAM_DENIED_REQUEST = 4;
    /**
       The return code indicating that 
       the user exit program associated with the server job failed.
    **/
    public static final int EXIT_PROGRAM_ERROR = 5;
    /**
       The return code indicating that 
       the user exit program associated with the server job could
       not be found.
    **/
    public static final int EXIT_PROGRAM_NOT_FOUND = 6;
    /**
       The return code indicating that 
       the number of user exit programs associated
       with the server job is not valid.
    **/
    public static final int EXIT_PROGRAM_NUMBER_NOT_VALID = 7;
    /**
       The return code indicating that 
       an error occurred when resolving to the exit program.
    **/
    public static final int EXIT_PROGRAM_RESOLVE_ERROR = 8;
    /**
       The return code indicating that 
       the system resource has a length that is not valid or cannot
       be handled through this interface.
    **/
    public static final int LENGTH_NOT_VALID = 9;
    /**
       The return code indicating that 
       the exact cause of the failure is not known.  The detailed message
       may contain additional information.
    **/
    public static final int UNKNOWN = 11;
   /**
      The return code indicating that
      the spooled file does not have a
      message waiting.
   **/
   public static final int SPOOLED_FILE_NO_MESSAGE_WAITING = 12;
   /**
      The return code indicating that
      the writer job has ended.
   **/
   public static final int WRITER_JOB_ENDED = 13;




   
   
    //   Required for AS400Exception class.
    /**
       Constructs an ErrorCompletingRequestException object.  It indicates
       an error has occurred that prevented the request from completing. 
       @param  returnCode     The return code associated with this error.
       @param  message        The detailed message describing this error.
    **/
    ErrorCompletingRequestException(int returnCode,
                                           String message)
    {
      super(message);
      rc_ = returnCode;
    }


    


    /**
       Constructs an ErrorCompletingRequestException object. It indicates 
       an error has occurred that prevented the request from completing.
       Exception message will look like this: User ID is not known.
       @param returnCode The return code which identifies the message to be returned.
    **/
    ErrorCompletingRequestException(int returnCode)
    {
      // Create the message
      super(loader_.getText(getMRIKey(returnCode)));
      rc_ =  returnCode;        
    }



    
   /**
      Returns the text associated with the return code.
      @param returnCode  The return code associated with this exception.
      @return The text string which describes the error. 
   **/
   // This method is required so the message can be created and sent in super()
   static String getMRIKey (int returnCode)
   {
    switch(returnCode)
    {	 case AS400_ERROR :
            return "EXC_AS400_ERROR";
         case EXIT_POINT_PROCESSING_ERROR :
            return "EXC_EXIT_POINT_PROCESSING_ERROR";
         case EXIT_PROGRAM_CALL_ERROR:
            return "EXC_EXIT_PROGRAM_CALL_ERROR";
         case EXIT_PROGRAM_DENIED_REQUEST :
            return "EXC_EXIT_PROGRAM_DENIED_REQUEST";
         case EXIT_PROGRAM_ERROR:
            return "EXC_EXIT_PROGRAM_ERROR";
         case EXIT_PROGRAM_NOT_FOUND :
            return "EXC_EXIT_PROGRAM_NOT_FOUND";
         case EXIT_PROGRAM_NUMBER_NOT_VALID:
            return "EXC_EXIT_PROGRAM_NUMBER_NOT_VALID";
         case EXIT_PROGRAM_RESOLVE_ERROR:
            return "EXC_EXIT_PROGRAM_RESOLVE_ERROR";
         case LENGTH_NOT_VALID:
            return "EXC_LENGTH_NOT_VALID";
         case UNKNOWN:
            return "EXC_UNKNOWN";
         case SPOOLED_FILE_NO_MESSAGE_WAITING:
            return "EXC_SPOOLED_FILE_NO_MESSAGE_WAITING";
         case WRITER_JOB_ENDED:
            return "EXC_WRITER_JOB_ENDED"; 
         default:
             return "EXC_UNKNOWN";   // Bad return code was provided.
     }
    }  


 
    /**
       Returns the return code associated with this exception.
       @return The return code.
    **/
    public int getReturnCode ()
    {
      return rc_;		
    }


  
  } // End of ErrorCompletingRequestException class
