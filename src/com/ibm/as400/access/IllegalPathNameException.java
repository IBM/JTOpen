///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IllegalPathNameException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.MissingResourceException;
import java.util.ResourceBundle;




/**
   The IllegalPathNameException class represents an exception
   that indicates that the integrated file system path name
   is not valid.
**/

// This exception should be used when a problem occurs
// while using the IFS classes.
public class IllegalPathNameException extends RuntimeException
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
       the object type is not valid.
   **/
   public static final int OBJECT_TYPE_NOT_VALID = 1;
   /**
      The return code indicating that
      the length of the library name is not valid.
   **/
   public static final int LIBRARY_LENGTH_NOT_VALID = 2;
   /**
      The return code indicating that
      the length of the object name is not valid.
   **/
   public static final int OBJECT_LENGTH_NOT_VALID = 3;
   /**
      The return code indicating that
      the length of the member name is not valid.
   **/
   public static final int MEMBER_LENGTH_NOT_VALID = 4;
   /**
      The return code indicating that
      the length of the object type is not valid.
   **/
   public static final int TYPE_LENGTH_NOT_VALID = 5;
   /**
      The return code indicating that
      the object is required to be in the QSYS file system, but the
      integrated file system name does not begin with /QSYS.LIB/.
   **/
   public static final int QSYS_PREFIX_MISSING = 6;
   /**
      The return code indicating that
      the path starts with /QSYS.LIB/QSYS.LIB.  Objects in library
       QSYS should not repeat the library specification.
   **/
   public static final int QSYS_SYNTAX_NOT_VALID = 7;
   /**
      The return code indicating that
      the path name represents an object of type .MBR, but
      does not contain a valid .FILE specification.
   **/
   public static final int MEMBER_WITHOUT_FILE = 8;
   /**
      The return code indicating that
      the path is not specified correctly, and the most likely cause
      is a library specification that does not have the .LIB
      extension.
   **/
   public static final int LIBRARY_SPECIFICATION_NOT_VALID = 9;



   /**
       Constructs an IllegalPathNameException object.
       It indicates that the path name is not valid.
       Exception message will look like this: Path name was not found.
       @param returnCode the return code which identifies the message to be returned.
   **/
   IllegalPathNameException(int returnCode)
   {
     // Create the message
     super(loader_.getText(getMRIKey(returnCode)));
     rc_ =  returnCode;
   }



   /**
      Constructs an IncorrectPathNameException object.
      It indicates that the path name is not valid.
      Exception message will look like this:
      /QSYS.LIB/mylib.lib/myfile.FILE: Path name is not valid.
      @param pathName The path name.
      @param returnCode The return code which identifies the message to be returned.
   **/
   IllegalPathNameException(String pathName, int returnCode)
   {
     // Create the message
     super(pathName + ": " + loader_.getText(getMRIKey(returnCode)));
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
       case OBJECT_TYPE_NOT_VALID:
            return "EXC_OBJECT_TYPE_NOT_VALID";
       case LIBRARY_LENGTH_NOT_VALID:
          return "EXC_LIBRARY_LENGTH_NOT_VALID";
       case OBJECT_LENGTH_NOT_VALID:
          return "EXC_OBJECT_LENGTH_NOT_VALID";
       case MEMBER_LENGTH_NOT_VALID:
          return "EXC_MEMBER_LENGTH_NOT_VALID";
       case TYPE_LENGTH_NOT_VALID:
          return "EXC_TYPE_LENGTH_NOT_VALID";
       case QSYS_PREFIX_MISSING:
          return "EXC_QSYS_PREFIX_MISSING";
       case QSYS_SYNTAX_NOT_VALID:
          return "EXC_QSYS_SYNTAX_NOT_VALID";
       case MEMBER_WITHOUT_FILE:
          return "EXC_MEMBER_WITHOUT_FILE";
       case LIBRARY_SPECIFICATION_NOT_VALID:
          return "EXC_LIBRARY_SPECIFICATION_NOT_VALID";
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





}  // End of IllegalPathNameException
