///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ObjectAlreadyExistsException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;



/**
   The ObjectAlreadyExistsException class represents an exception 
   that indicates that a server object already exists.
**/
public class ObjectAlreadyExistsException extends Exception
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
       The return code indicating that the 
       object already exists.
   **/
   public static final int OBJECT_ALREADY_EXISTS = 1;


  

   
   /**
       Constructs an ObjectAlreadyExistsException object.
       It indicates that a server object already exists.
       Exception message will look like this: Object already exists. 
       @param returnCode The return code which identifies the message to be returned.
   **/
   ObjectAlreadyExistsException(int returnCode)
   {
     // Create the message
     super(loader_.getText(getMRIKey(returnCode)));
     rc_ =  returnCode;        
   }



   /**
      Constructs an ObjectAlreadyExistsException object.
      It indicates that a server object already exists.
      Exception message will look like this:  dataQueue (mydataqueue): Object already exists. 
      @param objectName The object that already exists.
                        It should be in the format: type (value).
                        For example: dataQueue (mydataqueue).
      @param returnCode The return code which identifies the message to be returned.
   **/
   ObjectAlreadyExistsException(String objectName, int returnCode)
  {
    // Create the message
    super(objectName + ": " + loader_.getText(getMRIKey(returnCode)));
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
	 case OBJECT_ALREADY_EXISTS:
            return "EXC_OBJECT_ALREADY_EXISTS";
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



}  // End of ObjectAlreadyExistsException
