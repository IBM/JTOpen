///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ExtendedIllegalStateException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
   The ExtendedIllegalStateException class represents an exception
   that indicates that an object is not in the proper state to perform
   the requested operation.
**/
public class ExtendedIllegalStateException extends IllegalStateException
implements ReturnCodeException
{
    static final long serialVersionUID = 4L;


    private int rc_;  // Return code associated with this exception

    //  Handles loading the appropriate resource bundle
    //@E0D private static ResourceBundleLoader loader_;


    // Return code values used by this class.
    // If a value is added here, it must also be added to MRI.properties.


    /**
       The return code indicating that
       the commitment control has already been started.
    **/
    public static final int COMMITMENT_CONTROL_ALREADY_STARTED = 1;

    /**
       The return code indicating that an IBM Toolbox for Java implementation
       class was not found.
    **/
    public static final int IMPLEMENTATION_NOT_FOUND = 11;           // @D0A

    /**
       The return code indicating that the requested information
       is not available.
    **/
    public static final int INFORMATION_NOT_AVAILABLE = 14;           // @E2A

    /**
       The return code indicating that the license can not be
       requested.
    **/
    public static final int LICENSE_CAN_NOT_BE_REQUESTED = 15;           // @E2A

     /**
       The return code indicating that
       a request was made to find an object and that object
       was not found.
    **/
    public static final int OBJECT_CANNOT_BE_FOUND = 13; //@E1A

    /**
       The return code indicating that
       a request was made that requires the object to
       not be open and the object was in an open state.
    **/
    public static final int OBJECT_CAN_NOT_BE_OPEN = 2;
    
    /**
       The return code indicating that
       a request was made that requires the object to be
       writable and the object was in a read-only state.
    **/
    public static final int OBJECT_IS_READ_ONLY = 12; //@E0A
    
    /**
       The return code indicating that
       a request was made that requires the object to be
       open and the object was not in an open state.
    **/
    public static final int OBJECT_MUST_BE_OPEN = 3;
    
    /**
       The return code indicating that
       one or more properties have not been set.
    **/
    public static final int PROPERTY_NOT_SET = 4;
    
    /**
       The return code indicating that
       the property cannot be changed.  This usually
       results when an object
       is in a state where the property cannot be changed.
    **/
    public static final int PROPERTY_NOT_CHANGED = 5;   

    /**
       The return code indicating that the proxy server has already been
       started.
    **/
    public static final int PROXY_SERVER_ALREADY_STARTED = 7;            // @D0A

    /**
       The return code indicating that the proxy server has not been
       started.
    **/
    public static final int PROXY_SERVER_NOT_STARTED = 8;                // @D0A

    /**
       The return code indicating that the proxy server did
       not fire an event because the listener was not
       accessible.
    **/
    public static final int PROXY_SERVER_EVENT_NOT_FIRED = 10;           // @D0A

    /**
       The return code indicating that this object is not 
       allowed to start threads.
    **/
    public static final int OBJECT_CAN_NOT_START_THREADS = 9;            // @D1A

    /**
       The return code indicating that a signon is already in progress.
    **/
    public static final int SIGNON_ALREADY_IN_PROGRESS = 16;


    /**
       The return code indicating that
       the exact cause of the failure is not known.  The detailed message
       may contain additional information.
    **/
    public static final int UNKNOWN = 6;




    /**
       Constructs an ExtendedIllegalStateException object.
       It indicates that an object is not in the proper state
       to perform the requested operation.
       @param returnCode The return code which identifies the message to be returned.
    **/
    public ExtendedIllegalStateException(int returnCode) // @D2C
    {
        // Create the message
        super(ResourceBundleLoader.getCoreText(getMRIKey(returnCode))); // @D2C @E0C
        rc_ =  returnCode;

    }


    /**
       Constructs an ExtendedIllegalStateException object.
       It indicates that an object is not in the proper state
       to perform the requested operation.
       @param property The property that was not set.
       @param returnCode The return code which identifies the message to be returned.
    **/
    public ExtendedIllegalStateException(String property, int returnCode) // @D2C
    {
        // Create the message
        super(property + ": " + ResourceBundleLoader.getCoreText(getMRIKey(returnCode))); // @D2C @E0C
        rc_ =  returnCode;
    }


    
    /**
       Returns the text associated with the return code.
       @param returnCode  The return code associated with this exception.
       @return The text string which describes the error.
    **/
    // This method is required so the message can be created and sent in super()
    static String getMRIKey(int returnCode)
    {
        switch (returnCode)
        {
        case COMMITMENT_CONTROL_ALREADY_STARTED:
            return "EXC_COMMITMENT_CONTROL_ALREADY_STARTED";
        case OBJECT_CAN_NOT_BE_OPEN:
            return "EXC_OBJECT_CANNOT_BE_OPEN";
        case OBJECT_MUST_BE_OPEN:
            return "EXC_OBJECT_MUST_BE_OPEN";
        case PROPERTY_NOT_SET:
            return "EXC_PROPERTY_NOT_SET";
        case PROPERTY_NOT_CHANGED:
            return "EXC_PROPERTY_NOT_CHANGED";
        case PROXY_SERVER_ALREADY_STARTED:                  // @D0A
            return "PROXY_SERVER_ALREADY_STARTED";          // @D0A
        case PROXY_SERVER_NOT_STARTED:                      // @D0A
            return "PROXY_SERVER_NOT_STARTED";              // @D0A
        case PROXY_SERVER_EVENT_NOT_FIRED:                  // @D0A
            return "PROXY_SERVER_EVENT_NOT_FIRED";          // @D0A
        case IMPLEMENTATION_NOT_FOUND:                      // @D0A
            return "EXC_IMPLEMENTATION_NOT_FOUND";          // @D0A
        case OBJECT_CAN_NOT_START_THREADS:                  // @D1A
            return "EXC_OBJECT_CANNOT_START_THREADS";       // @D1A
        case OBJECT_IS_READ_ONLY:                           // @E0A
            return "EXC_OBJECT_IS_READ_ONLY";               // @E0A
	    case OBJECT_CANNOT_BE_FOUND:                        // @E1A
	        return "EXC_OBJECT_CANNOT_BE_FOUND";	        // @E1A
	    case INFORMATION_NOT_AVAILABLE:                     // @E2A
	        return "EXC_INFORMATION_NOT_AVAILABLE";	        // @E2A
	    case LICENSE_CAN_NOT_BE_REQUESTED:                  // @E2A
	        return "EXC_LICENSE_CAN_NOT_BE_REQUESTED";	    // @E2A
	    case SIGNON_ALREADY_IN_PROGRESS:                  // @E2A
	        return "EXC_SIGNON_ALREADY_IN_PROGRESS";
        case UNKNOWN:
            // @D0A - drop through to default case...      return "EXC_UNKNOWN";
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



}  // End of ExtendedIllegalStateException class
