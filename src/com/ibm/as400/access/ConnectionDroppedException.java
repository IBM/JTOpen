///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConnectionDroppedException.java
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
  The ConnectionDroppedException class represents an exception that indicates that the AS/400 connection was dropped unexpectedly.
**/
public class ConnectionDroppedException extends IOException implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    private int rc_;  // Return code associated with this exception

    //  Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

    // Return code values used by this class.
    // If a value is added here, it must also be added to MRI.java

    /**
      The return code indicating that an error occurred in communications.
     **/
    public final static int COMMUNICATIONS_ERROR = 1;
    /**
      The return code indicating that the connection was dropped unexpectedly.
     **/
    public final static int CONNECTION_DROPPED = 2;
    /**
      The return code indicating that the connection is not active.
     **/
    public final static int CONNECTION_NOT_ACTIVE = 3;
    /**
       The return code indicating that disconnect was received.
     **/
    public final static int DISCONNECT_RECEIVED = 4;


    // Constructs a ConnectionDroppedException object.
    // It indicates that a connection was dropped unexpectedly.
    // Exception message will look like this: Connection not active.
    // @param returnCode The return code which identifies the message to be returned.
    ConnectionDroppedException(int returnCode)
    {
        // Create the message 
	super(loader_.getText(getMRIKey(returnCode)));
	rc_ =  returnCode;
    }

    // Returns the text associated with the return code.
    // @param  returnCode  The return code associated with this exception.
    // @return  The text string which describes the error.
    // This method is required so the message can be created and sent in super()
    static String getMRIKey(int returnCode)
    {
	switch(returnCode)
	{
	    case COMMUNICATIONS_ERROR:
		return "EXC_COMMUNICATIONS_ERROR";
	    case CONNECTION_DROPPED:
		return "EXC_CONNECTION_DROPPED";
	    case CONNECTION_NOT_ACTIVE:
		return "EXC_CONNECTION_NOT_ACTIVE";
	    case DISCONNECT_RECEIVED:
		return "EXC_DISCONNECT_RECEIVED";
	    default:
		return "EXC_UNKNOWN";  // Bad return code was provided.
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
