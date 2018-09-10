///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RequestNotSupportedException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;


/**
   The RequestNotSupportedException class represents an exception 
   that indicates that the requested function is not supported
   because the system is not at the correct level.
**/
public class RequestNotSupportedException extends Exception
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
       the requested function is not supported due to the
       system level not being correct.
    **/
    public static final int SYSTEM_LEVEL_NOT_CORRECT = 1;
    
   
   


    /**
       Constructs a RequestNotSupportedException object. It indicates 
       that the requested function is not supported due to the system
       level not being correct. This constructor should be used when
       the required system level is known.
       Exception message will look like this: V2R1M0: Correct system level is required.
       @param requiredLevel The required system level.  This needs to be in the
                            format VvRrMm where v is the version, r is the release
                            and m is the modification.              
       @param returnCode The return code which identifies the message to be returned.
    **/
    RequestNotSupportedException(String requiredLevel, int returnCode)
    {
      // Create the message
      super(requiredLevel + ": " + loader_.getText(getMRIKey(returnCode)));
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
    {
         case SYSTEM_LEVEL_NOT_CORRECT :
            return "EXC_SYSTEM_LEVEL_NOT_CORRECT";
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


 

  
  
  } // End of RequestNotSupportedException class
