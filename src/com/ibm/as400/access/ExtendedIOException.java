///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ExtendedIOException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


import java.io.IOException;


/**
   The ExtendedIOException class represents an exception
   that indicates that an error has occurred while 
   communicating with the server.
**/

public class ExtendedIOException extends IOException
     implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  
    static final long serialVersionUID = 4L;

  private int rc_;  // Return code associated with this exception

  //  Handles loading the appropriate resource bundle
  private static ResourceBundleLoader loader_;
   
  // Return code values used by this class. 
  // If a value is added here, it must also be added to MRI.properties.
  // The numbers assigned to these constants are significant
  // and should not be changed.

  /**
      The return code indicating that
      the file is in use.
  **/
  public final static int FILE_IN_USE = 1;
  /**
      The return code indicating that
      the file was not found.
  **/
  public final static int FILE_NOT_FOUND = 2;
  /**
     The return code indicating that
     the path is not found.
  **/
  public final static int PATH_NOT_FOUND = 3;
  /**
      The return code indicating that
      the directory entry exists.
  **/
  public final static int DIR_ENTRY_EXISTS = 4;
  /**
     The return code indicating that
     the access to the request was denied. 
  **/
  public final static int ACCESS_DENIED = 5;
  /**
     The return code indicating that
     the handle is not valid.
  **/
  public final static int INVALID_HANDLE = 6;
  /**
     The return code indicating that
     the directory entry name is not valid.
  **/
  public final static int INVALID_DIR_ENTRY_NAME = 7;
  /**
     The return code indicating that
     the attribute name is not valid.
  **/
  public final static int INVALID_ATTRIBUTE_NAME = 8;
  /**
     The return code indicating that
     the directory is not empty.
  **/
  public final static int DIR_NOT_EMPTY = 9;
  /**
     The return code indicating that
     the file substream is in use.
  **/
  public final static int FILE_SUBSTREAM_IN_USE = 10;
  /**
     The return code indicating that
     the resource limit was exceeded.
  **/
  public final static int RESOURCE_LIMIT_EXCEEDED = 11;
  /**
     The return code indicating that
     the resource is not available.
  **/
  public final static int RESOURCE_NOT_AVAILABLE = 12;
  /**
     The return code indicating that
     the request was denied.
  **/
  public final static int REQUEST_DENIED = 13;
  /**
     The return code indicating that
     the directory entry is damaged.
  **/
  public final static int DIR_ENTRY_DAMAGED = 14;
  /**
     The return code indicating that
     the connection is not valid.
  **/
  public final static int INVALID_CONNECTION = 15;
  /**
     The return code indicating that
     the request is not valid.
  **/
  public final static int INVALID_REQUEST = 16;
  /**
     The return code indicating that
     there is a syntax error in the data stream.
  **/
  public final static int DATA_STREAM_SYNTAX_ERROR = 17;
  /**
     The return code indicating that
     no more files are available.
  **/
  public final static int NO_MORE_FILES = 18;
  /**
     The return code indicating that
     the parameter is not supported.
  **/
  public final static int PARM_NOT_SUPPORTED = 19;
  /**
     The return code indicating that
     the parameter value is not supported.
  **/
  public final static int PARM_VALUE_NOT_SUPPORTED = 20;
  /**
     The return code indicating that
     the value cannot be converted.
  **/
  public final static int CANNOT_CONVERT_VALUE = 21;
  /**
     The return code indicating that
     the end of file has been reached.
  **/
  public final static int END_OF_FILE = 22;
  /**
     The return code indicating that
     the request is not supported.
  **/
  public final static int REQUEST_NOT_SUPPORTED = 23;
  /**
     The return code indicating that
     the user ID is not valid.
  **/
  public final static int INVALID_USER = 24;
  /**
     The return code indicating that
     an unknown problem has occurred.
  **/
  public final static int UNKNOWN_ERROR = 25;
  /**
     The return code indicating that
     a sharing violation has occurred.
  **/
  public final static int SHARING_VIOLATION = 32;
  /**
     The return code indicating that
     a lock violation has occurred.
  **/
  public final static int LOCK_VIOLATION = 33;
  /**
    The return code indicating that
    no certificate was found.
   "Certificate was not found."
  **/
  public final static int CERTIFICATE_NOT_FOUND = 34; 
  /**
      The return code indicating that
      the certificate was already added.
   "Certificate association already exists."
  **/
  public final static int CERTIFICATE_ALREADY_ADDED = 35;
    /**
      The return code indicating that
     the certificate or certificate format was not valid.
     "Certificate or certificate type is not valid."
  **/
  public final static int INVALID_CERTIFICATE = 36; 
 
  


  /**
     Constructs a ExtendedIOException object. It indicates 
     that an IO Exception occurred.
     Exception message will look like this: End of file reached.
     @param returnCode The return code which identifies the message to be returned.
  **/
   
  ExtendedIOException(int returnCode)
  {
    // Create the message
    super(loader_.getText(getMRIKey(returnCode)));
    rc_ =  returnCode;        
   
  }



  /**
     Constructs a ExtendedIOException object. It indicates 
     that an IO Exception occurred.
     Exception message will look like this:  myuserid: User ID not valid.
     @param objectName The name of the object.
     @param returnCode The return code which identifies the message to be returned.
  **/
  ExtendedIOException(String objectName, int returnCode)
  {
    // Create the message
    super(objectName + ": " + loader_.getText(getMRIKey(returnCode)));
    rc_ =  returnCode;
 
  }



  /**
     Returns the translatable text associated with the
     return code.
     @param returnCode  The return code associated with this exception.
     @return The translatable text string which describes the error. 
  **/
  // This method is required so the message can be created and sent in super()
  static String getMRIKey (int returnCode)
   {
     switch(returnCode)
     {
	 case ACCESS_DENIED :
            return "EXC_ACCESS_DENIED";
         case CANNOT_CONVERT_VALUE:
	     return "EXC_VALUE_CANNOT_CONVERT";
	 case CERTIFICATE_ALREADY_ADDED:
	     return "EXC_CERTIFICATE_ALREADY_ADDED";
	 case CERTIFICATE_NOT_FOUND:
	     return "EXC_CERTIFICATE_NOT_FOUND";        
	 case DATA_STREAM_SYNTAX_ERROR:
            return "EXC_DATA_STREAM_SYNTAX_ERROR";
         case DIR_ENTRY_DAMAGED:
            return "EXC_DIRECTORY_ENTRY_DAMAGED";
	 case DIR_ENTRY_EXISTS :
            return "EXC_DIRECTORY_ENTRY_EXISTS";
         case DIR_NOT_EMPTY :
            return "EXC_DIRECTORY_NOT_EMPTY";
	 case END_OF_FILE:
            return "EXC_FILE_END";
         case FILE_IN_USE :
            return "EXC_FILE_IN_USE";
	 case FILE_NOT_FOUND:
            return "EXC_FILE_NOT_FOUND";
         case FILE_SUBSTREAM_IN_USE :
            return "EXC_FILE_SUBSTREAM_IN_USE";
	 case INVALID_ATTRIBUTE_NAME:
	     return "EXC_ATTRIBUTE_NOT_VALID";
	 case INVALID_CERTIFICATE:
	     return "EXC_CERTIFICATE_NOT_VALID";	     
         case INVALID_CONNECTION:
            return "EXC_CONNECTION_NOT_VALID";
	 case INVALID_DIR_ENTRY_NAME:
            return "EXC_DIRECTORY_NAME_NOT_VALID";
         case INVALID_HANDLE:
            return "EXC_HANDLE_NOT_VALID";
	 case INVALID_REQUEST:
            return "EXC_REQUEST_NOT_VALID";
         case INVALID_USER:
            return "EXC_USERID_UNKNOWN";
         case LOCK_VIOLATION:
            return "EXC_LOCK_VIOLATION";
         case NO_MORE_FILES:
            return "EXC_FILES_NOT_AVAILABLE";
	 case PARM_NOT_SUPPORTED:
            return "EXC_PARAMETER_NOT_SUPPORTED";
         case PARM_VALUE_NOT_SUPPORTED:
            return "EXC_PARAMETER_VALUE_NOT_SUPPORTED";
	 case PATH_NOT_FOUND:
            return "EXC_PATH_NOT_FOUND";
         case REQUEST_DENIED:
            return "EXC_REQUEST_DENIED";
	 case RESOURCE_LIMIT_EXCEEDED:
            return "EXC_RESOURCE_LIMIT_EXCEEDED";
         case RESOURCE_NOT_AVAILABLE :
            return "EXC_RESOURCE_NOT_AVAILABLE";
	 case REQUEST_NOT_SUPPORTED :
            return "EXC_REQUEST_NOT_SUPPORTED";
         case SHARING_VIOLATION :
            return "EXC_SHARE_VIOLATION";
         case UNKNOWN_ERROR  :
            return "EXC_UNKNOWN";
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

 

} // End of ExtendedIOException class




