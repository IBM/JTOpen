///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  IllegalObjectTypeException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The IllegalObjectTypeException class represents an exception that indicates that the IBM i system object is not the required type.
 **/
public class IllegalObjectTypeException extends Exception implements ReturnCodeException
{
    static final long serialVersionUID = 4L;

    // Return code associated with this exception.
    private int returnCode_;

    // Return code values used by this class.
    // If a value is added here, it must also be added to MRI.java.

    /**
     The return code indicating that an attempt was made to use a keyed queue with a non-keyed data queue object.
     **/
    public static final int DATA_QUEUE_KEYED = 1;
    /**
     The return code indicating that an attempt was made to use a non-keyed data queue with a keyed data queue object.
     **/
    public static final int DATA_QUEUE_NOT_KEYED = 2;
    /**
     The return code indicating that an attempt was made to use a character data area with a non-character data area object.
     **/
    public static final int DATA_AREA_CHARACTER = 3;
    /**
     The return code indicating that an attempt was made to use a decimal data area with a non-decimal data area object.
     **/
    public static final int DATA_AREA_DECIMAL = 4;
    /**
     The return code indicating that an attempt was made to use a logical data area with a non-logical data area object.
     **/
    public static final int DATA_AREA_LOGICAL = 5;

    // Constructs an IllegalObjectTypeException object.  It indicates that an object is not the required type.  Exception message will look like this: Object type is not valid.
    // @param  returnCode  The return code which identifies the message to be returned.
    IllegalObjectTypeException(int returnCode)
    {
        // Create the message.
        super(ResourceBundleLoader.getText(getMRIKey(returnCode)));
        returnCode_ = returnCode;
    }

    // Constructs an IllegalObjectTypeException object.  It indicates that an object is not the required type.  Exception message will look like this: /QSYS.LIB/MYLIB.LIB/MYQUEUE.DTAQ: Object type is not valid.
    // @param  objectName  The object that is the wrong type.
    // @param  returnCode  The return code which identifies the message to be returned.
    IllegalObjectTypeException(String objectName, int returnCode)
    {
        // Create the message.
        super(objectName + ": " + ResourceBundleLoader.getText(getMRIKey(returnCode)));
        returnCode_ = returnCode;
    }

    // Returns the text associated with the return code.
    // @param  returnCode  The return code associated with this exception.
    // @return  The text string which describes the error.
    // This method is required so the message can be created and sent in super().
    static String getMRIKey(int returnCode)
    {
        switch (returnCode)
        {
            case DATA_QUEUE_KEYED:
                return "EXC_DATA_QUEUE_KEYED";
            case DATA_QUEUE_NOT_KEYED:
                return "EXC_DATA_QUEUE_NOT_KEYED";
            case DATA_AREA_CHARACTER:
                return "EXC_DATA_AREA_CHARACTER";
            case DATA_AREA_DECIMAL:
                return "EXC_DATA_AREA_DECIMAL";
            case DATA_AREA_LOGICAL:
                return "EXC_DATA_AREA_LOGICAL";
            default:
                return "EXC_UNKNOWN";   // Bad return code was provided.
        }
    }

    /**
     Returns the return code associated with this exception.
     @return  The return code.
     **/
    public int getReturnCode()
    {
        return returnCode_;
    }
}
