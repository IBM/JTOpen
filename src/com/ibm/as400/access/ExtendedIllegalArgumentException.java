///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ExtendedIllegalArgumentException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  The ExtendedIllegalArgumentException class represents an exception that indicates that an argument is not valid.
**/
public class ExtendedIllegalArgumentException  extends IllegalArgumentException implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private int rc_;  // Return code associated with this exception

    // Return code values used by this class.  If a value is added here, it must also be added to CoreMRI.java.

    /**
      The return code indicating that the length is not valid.
     **/
    public static final int LENGTH_NOT_VALID = 1;
    /**
      The return code indicating that the parameter value is not valid.
     **/
    public static final int PARAMETER_VALUE_NOT_VALID = 2;
    /** 
      The return code indicating that the path is not valid.
     **/
    public static final int PATH_NOT_VALID = 3;
    /**
      The return code indicating that the parameter value is out of the allowed range.
     **/
    public static final int RANGE_NOT_VALID = 4;
    /**
      The return code indicating that the field was not found.
     **/
    public static final int FIELD_NOT_FOUND = 5;
    /**
      The return code indicating that the user ID or password contains a character that is not valid.
     **/
    public static final int SIGNON_CHAR_NOT_VALID = 6;

    /**
      Constructs an ExtendedIllegalArgumentException object.  It indicates that a method has been passed an illegal argument.
      @param  argument  The type and value of the argument that was illegal.  It should be in the format: argument (value).  For example: library (mylib).
      @param  returnCode  The return code which identifies the message to be returned.
     **/
    public ExtendedIllegalArgumentException(String argument, int returnCode)
    {
        // Create the message
	super(argument + ": " + ResourceBundleLoader.getCoreText(getMRIKey(returnCode)));
	rc_ =  returnCode;
    }

    /**
      Returns the text associated with the return code.
      @param  returnCode  The return code associated with this exception.
      @return  The text string which describes the error.
     **/
    static String getMRIKey(int returnCode)  // This method is required so the message can be created and sent in super()
    {
	switch(returnCode)
	{
	    case LENGTH_NOT_VALID:
		return "EXC_LENGTH_NOT_VALID";
	    case PARAMETER_VALUE_NOT_VALID:
		return "EXC_PARAMETER_VALUE_NOT_VALID";
	    case PATH_NOT_VALID:
		return "EXC_PATH_NOT_VALID";
	    case RANGE_NOT_VALID:
		return "EXC_RANGE_NOT_VALID";
	    case FIELD_NOT_FOUND:
		return "EXC_FIELD_NOT_FOUND";
	    case SIGNON_CHAR_NOT_VALID:
		return "EXC_SIGNON_CHAR_NOT_VALID";
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
	return rc_;
    }
}
