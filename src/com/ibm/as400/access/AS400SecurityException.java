///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400SecurityException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The AS400SecuirytException class represents an exception that
   indicates that a security or authority error has occurred.
**/
public class AS400SecurityException extends Exception
									implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	private int rc_;  // Return code associated with this exception

	//  Handles loading the appropriate resource bundle
	private static ResourceBundleLoader loader_;


	// Return code values used by this class.
	// If a value is added here, it must also be added to MRI.properties.

	/**
	   The return code indicating that 
	   the user of this program does not have enough authority to
	   access the directory entry.
	**/
	public static final int DIRECTORY_ENTRY_ACCESS_DENIED = 1;
	/**
	   The return code indicating that 
	   the user is not authorized to the exit program.
	**/
	public static final int EXIT_PROGRAM_NOT_AUTHORIZED = 2;
	/**
	   The return code indicating that 
	   the user of this program does not have enough authority to
	   the library to perform the requested action.
	**/
	public static final int LIBRARY_AUTHORITY_INSUFFICIENT = 3;
	/**
	   The return code indicating that 
	   the user of this program does not have enough authority to
	   the AS/400 resource to perform the requested action.
	**/
	public static final int OBJECT_AUTHORITY_INSUFFICIENT = 4;
	/**
	   The return code indicating that 
	   the change password request is missing user ID, old password,
	   or new password.
	**/
	public static final int PASSWORD_CHANGE_REQUEST_NOT_VALID = 5;
	/**
	   The return code indicating that 
	   there is a general password error.
	**/
	public static final int PASSWORD_ERROR = 6;
	/**
	   The return code indicating that 
	   the password has expired.
	**/
	public static final int PASSWORD_EXPIRED = 7;
	/**
	   The return code indicating that 
	   the password is not correct.
	**/
	public static final int PASSWORD_INCORRECT = 8;
	/**
	   The return code indicating that 
	   the password is not correct and that the user ID will be disabled
	   on the next sign-on attempt if the password is incorrect again.
	**/
	public static final int PASSWORD_INCORRECT_USERID_DISABLE = 9;
	/**
	   The return code indicating that 
	   the password length is not valid.
	**/
	public static final int PASSWORD_LENGTH_NOT_VALID = 10;
	/**
	   The return code indicating that 
	   the new password has adjacent digits.
	**/
	public static final int PASSWORD_NEW_ADJACENT_DIGITS = 11;
	/**
	   The return code indicating that 
	   the new password contains a character repeated consecutively.
	**/
	public static final int PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER = 12;
	/**
	   The return code indicating that 
	   the new password is not allowed.
	**/
	public static final int PASSWORD_NEW_DISALLOWED = 13;
	/**
	   The return code indicating that 
	   the new password must contain at least one alphabetic.
	**/
	public static final int PASSWORD_NEW_NO_ALPHABETIC = 14;
	/**
	   The return code indicating that 
	   the new password must contain at least one numeric.
	**/
	public static final int PASSWORD_NEW_NO_NUMERIC = 15;
	/**
	   The return code indicating that 
	   the new password is not valid.
	**/
	public static final int PASSWORD_NEW_NOT_VALID = 16;
	/**
	   The return code indicating that 
	   the new password was previously used.
	**/
	public static final int PASSWORD_NEW_PREVIOUSLY_USED = 17;
   /**
	  The return code indicating that 
	  the new password contains a character used more than once.
	**/
	public static final int PASSWORD_NEW_REPEAT_CHARACTER = 18;
	/**
	   The return code indicating that 
	   the new password is longer than maximum accepted length.
	**/
	public static final int PASSWORD_NEW_TOO_LONG = 19;
	/**
	   The return code indicating that 
	   the new password is shorter than minimum accepted length.
	**/
	public static final int PASSWORD_NEW_TOO_SHORT = 20;
	/**
	   The return code indicating that 
	   the new password contains a user ID as part of the password.
	**/
	public static final int PASSWORD_NEW_USERID = 21;
	/**
	   The return code indicating that 
	   the password is not set.
	**/
	public static final int PASSWORD_NOT_SET = 22;

	/**
	   The return code indicating that 
	   the old password is not valid.
	**/
	public static final int PASSWORD_OLD_NOT_VALID = 23;
	/**
	   The return code indicating that 
	   a general security failure occurred.
	   Ensure that QUSER did not expire.
	**/
	public static final int SECURITY_GENERAL = 24;
	/**
	   The return code indicating that 
	   the user canceled out of the sign-on prompt.
	**/
	public static final int SIGNON_CANCELED = 25;
	/**
	   The return code indicating that 
	   the sign-on request is missing either the user ID or the password.
	**/
	public static final int SIGNON_REQUEST_NOT_VALID = 26;
	/**
	   The return code indicating that 
	   the exact cause of the failure is not known.  The detailed
	   message may contain additional information.
	**/
	public static final int UNKNOWN = 27;
	/**
	   The return code indicating that 
	   a general user ID error occurred.
	**/
	public static final int USERID_ERROR = 28;
	/**
	   The return code indicating that 
	   the user ID length is not valid.
	**/
	public static final int USERID_LENGTH_NOT_VALID = 29;
	/**
	   The return code indicating that 
	   the user ID is not set.
	**/
	public static final int USERID_NOT_SET = 30;
	/**
	   The return code indicating that 
	   the user ID has been disabled by the AS/400 system.
	**/
	public static final int USERID_DISABLE = 31;
	/**
	   The return code indicating that 
	   the user ID is not known by the AS/400 system.
	**/
	public static final int USERID_UNKNOWN = 32;
	/**
	   The return code indicating that 
	   an error occurred processing the exit point.
	**/
	public static final int EXIT_POINT_PROCESSING_ERROR = 33;
	/**
	   The return code indicating that 
	   an error occurred resolving to the exit program.
	**/
	public static final int EXIT_PROGRAM_RESOLVE_ERROR = 34;
	/**
	   The return code indicating that 
	   the user exit program associated with the server job rejected
	   the request.
	**/
	public static final int EXIT_PROGRAM_DENIED_REQUEST = 35;
	/**
	   The return code indicating that
	   an error occurred with the user exit program call.
	**/
	public static final int EXIT_PROGRAM_CALL_ERROR = 36;
	/**
	   The return code indicating that
	   the requested action is not supported.
	**/
	public static final int REQUEST_NOT_SUPPORTED = 37;			// @A2A
	/**
	   The return code indicating that
	   the requested action cannot be performed due to the
	   system level not being correct.
	**/
	public static final int SYSTEM_LEVEL_NOT_CORRECT = 38;		// @A2A


/**
 * Constructs an AS400SecurityException.
 *
 * <p> An AS400SecurityException indicates that a security
 * error has occurred.
 *
 * @param returnCode
 *		The return code which identifies the message to be returned.
 *		Possible values are defined as constants on this class.
 */
protected AS400SecurityException(int returnCode) {				// @A2C
	super(loader_.getText(getMRIKey(returnCode)));
	rc_ =  returnCode;
}
	/**
	 Constructs a AS400SecurityException object. It indicates 
	 that a security exception occurred.
	 Exception message will look like this:  objectName: User is not authorized to object.
	 @param objectName The name of the object.
	 @param returnCode The return code which identifies the message to be returned.
	 **/
	AS400SecurityException(String objectName, int returnCode)
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
		 case DIRECTORY_ENTRY_ACCESS_DENIED:
			return "EXC_DIRECTORY_ENTRY_ACCESS_DENIED";
		 case EXIT_PROGRAM_NOT_AUTHORIZED:
			return "EXC_EXIT_PROGRAM_NOT_AUTHORIZED";
		 case LIBRARY_AUTHORITY_INSUFFICIENT :
			return "EXC_LIBRARY_AUTHORITY_INSUFFICIENT";
		 case OBJECT_AUTHORITY_INSUFFICIENT:
			return "EXC_OBJECT_AUTHORITY_INSUFFICIENT";
		 case PASSWORD_CHANGE_REQUEST_NOT_VALID:
			return "EXC_PASSWORD_CHANGE_REQUEST_NOT_VALID";
		 case PASSWORD_ERROR :
			return "EXC_PASSWORD_ERROR";
		 case PASSWORD_EXPIRED :
			return "EXC_PASSWORD_EXPIRED";
		 case PASSWORD_INCORRECT:
			return "EXC_PASSWORD_INCORRECT";
		 case PASSWORD_INCORRECT_USERID_DISABLE:
			return "EXC_PASSWORD_INCORRECT_USERID_DISABLE";
		 case PASSWORD_LENGTH_NOT_VALID:
			return "EXC_PASSWORD_LENGTH_NOT_VALID";
		 case PASSWORD_NEW_ADJACENT_DIGITS:
			return "EXC_PASSWORD_NEW_ADJACENT_DIGITS";
		 case PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER:
			return "EXC_PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER";
		 case PASSWORD_NEW_DISALLOWED:
			return "EXC_PASSWORD_NEW_DISALLOWED";
		 case PASSWORD_NEW_NO_ALPHABETIC:
			return "EXC_PASSWORD_NEW_NO_ALPHABETIC";
		 case PASSWORD_NEW_NO_NUMERIC:
			return "EXC_PASSWORD_NEW_NO_NUMERIC";
		 case PASSWORD_NEW_NOT_VALID:
			return "EXC_PASSWORD_NEW_NOT_VALID";
		 case PASSWORD_NEW_PREVIOUSLY_USED:
			return "EXC_PASSWORD_NEW_PREVIOUSLY_USED";
		 case PASSWORD_NEW_REPEAT_CHARACTER:
			return "EXC_PASSWORD_NEW_REPEAT_CHARACTER";
		 case PASSWORD_NEW_TOO_LONG:
			return "EXC_PASSWORD_NEW_TOO_LONG";
		 case PASSWORD_NEW_TOO_SHORT:
			return "EXC_PASSWORD_NEW_TOO_SHORT";
		 case PASSWORD_NEW_USERID:
			return "EXC_PASSWORD_NEW_USERID";
		 case PASSWORD_NOT_SET:
			return "EXC_PASSWORD_NOT_SET";
		 case PASSWORD_OLD_NOT_VALID:
			return "EXC_PASSWORD_OLD_NOT_VALID";
		 case SECURITY_GENERAL:
			return "EXC_SECURITY_GENERAL";
		 case SIGNON_CANCELED :
			return "EXC_SIGNON_CANCELED";
		 case SIGNON_REQUEST_NOT_VALID:
			return "EXC_SIGNON_REQUEST_NOT_VALID";
		 case UNKNOWN:
			return "EXC_UNKNOWN";
		 case USERID_ERROR:
			return "EXC_USERID_ERROR";
		 case USERID_LENGTH_NOT_VALID:
			return "EXC_USERID_LENGTH_NOT_VALID";
		 case USERID_NOT_SET:
			return "EXC_USERID_NOT_SET";
		 case USERID_DISABLE:
			return "EXC_USERID_DISABLE";
		 case USERID_UNKNOWN :
			return "EXC_USERID_UNKNOWN";
		 case EXIT_POINT_PROCESSING_ERROR :
			return "EXC_EXIT_POINT_PROCESSING_ERROR";
		 case EXIT_PROGRAM_DENIED_REQUEST :
			return "EXC_EXIT_PROGRAM_DENIED_REQUEST";
		 case EXIT_PROGRAM_RESOLVE_ERROR:
			return "EXC_EXIT_PROGRAM_RESOLVE_ERROR";
		 case EXIT_PROGRAM_CALL_ERROR:
			return "EXC_EXIT_PROGRAM_CALL_ERROR";
		 case REQUEST_NOT_SUPPORTED :						// @A2A
			return "EXC_REQUEST_NOT_SUPPORTED";				// @A2A
		 case SYSTEM_LEVEL_NOT_CORRECT :					// @A2A
			return "EXC_SYSTEM_LEVEL_NOT_CORRECT";			// @A2A
		 default:
			 return "EXC_UNKNOWN";   // Bad return code was provided.
	 } // End of switch
   }   // End of getTextString method   
	/**
	   Returns the return code associated with this exception.
	   @return The return code.
	**/
	public int getReturnCode ()
	{
	  return rc_;
	}
}  // End of AS400SecurityException class
