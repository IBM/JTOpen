///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ProxyException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The ProxyException class represents an exception that indicates 
an error occurred while communicating with the proxy server.
**/
public class ProxyException 
extends RuntimeException
implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private int                             returnCode_; 
    
  

    // Return code values used by this class. 
    // If a value is added here, it must also be added to MRI.properties.

/**
The return code indicating that a connection to the proxy server
cannot be established.
**/
    public static final int CONNECTION_NOT_ESTABLISHED = 1;
   
/**
The return code indicating that a connection to the proxy server
was dropped.
**/
    public static final int CONNECTION_DROPPED = 2;
   
/**
The return code indicating that a connection to the proxy server
was rejected.
**/
    public static final int CONNECTION_REJECTED = 3;
   


/**
Constructs a ProxyException object.
       
@param  returnCode     The return code associated with this exception.
@param  message        The detailed message describing this exception.
**/
    ProxyException (int returnCode, String message)
    {
        super(message);
        returnCode_ = returnCode;
        
    }


    
/**
Constructs a ProxyException object.
       
@param  returnCode     The return code associated with this exception.
**/
    ProxyException (int returnCode)
    {
        super (ResourceBundleLoader.getText (getMRIKey (returnCode)));
        returnCode_ = returnCode;        
    }



/**
Returns the text associated with the return code.
     
@param returnCode  The return code associated with this exception.
@return The text string which describes the error. 
**/
   private static String getMRIKey (int returnCode)
   {
        switch(returnCode) {
            case CONNECTION_NOT_ESTABLISHED:
                return "EXC_PROXY_CONNECTION_NOT_ESTABLISHED";
            case CONNECTION_DROPPED:
                return "EXC_PROXY_CONNECTION_DROPPED";
            case CONNECTION_REJECTED:
                return "EXC_PROXY_CONNECTION_REJECTED";
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
        return returnCode_;		
    }


  
}
