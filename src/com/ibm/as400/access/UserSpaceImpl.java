///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserSpaceImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
The UserSpaceImpl is the abstract class.  The three
ways to get to a user space is via the remote path, the
native path and the proxy path.
**/

abstract class UserSpaceImpl extends Object
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



   /**
     Constants
   **/
   final static int SPACE_SIZE = 1;      // Key option for the size attribute.
   final static int INITIAL_VALUE = 2;   // Key option for the initial value attribute.
   final static int AUTO_EXTEND = 3;     // Key option for the auto extendibility attribute.

   /**
     Variables
   **/


   AS400Impl system_ = null;           // The AS400 where the user space is located.
   String userSpacePathName_ = null;   // The full path name of the user space.
   String library_ = null;             // The library that contains the user space.
   String name_ = null;                // The name of the user space.
   byte[] userSpaceSystemPathName_;    // The name and library of the user space used in program call.

   boolean mustUseProgramCall_ = false; // Use ProgramCall instead of IFS @E1a

   ConverterImpl converter_;            // The string to AS400 data converter.              // @C1C

   // Removed close method to implement in UserSpace.class and avoid updates that would be necessary in UserSpaceImplNative.
   //abstract void close()               // $B2, $B3
   //         throws IOException;

   void close() throws IOException       //$C0A
   { }

   abstract void create(String domain, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority)
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException;

   abstract void delete()
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException;



   abstract byte getInitialValue()
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;

   abstract int getLength()
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;

   abstract boolean isAutoExtendible()
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;

   abstract int read(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length)
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException;

   abstract void setAutoExtendible(boolean autoExtendibility)
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;

   void setConverter(ConverterImpl converter)               // @C1C
   {
     converter_ = converter;                              //$C0C
   }


   abstract void setInitialValue(byte initialValue)
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;

   abstract void setLength(int length)
          throws AS400SecurityException,
                      ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                 ObjectDoesNotExistException;


   /**
     Sets the user space name used in API Program Call.
   **/
   void setName()
          throws UnsupportedEncodingException
   {
      StringBuffer pathName = new StringBuffer("                    ");
      pathName.insert(0, name_);
      pathName.insert(10, library_);
      pathName.setLength(20);
      String newString = pathName.toString();
      userSpaceSystemPathName_ = converter_.stringToByteArray(newString);
   }


   // @E1a new method
   void setMustUseProgramCall(boolean value)
   {
      mustUseProgramCall_ = value;
   }

   /**
     Sets the path for the user space.                                           //$C0A
     The path can only be set before a connection has been established.

        @param path  The fully qualified integrated file system path name.
        @exception PropertyVetoException If the change is vetoed.
   **/
   void setPath(String path)
   {
      // Verify name is valid integrated file system path name.
      QSYSObjectPathName userSpacePath = null;

      userSpacePath = new QSYSObjectPathName(path);

      userSpacePathName_ = path;
      library_ = userSpacePath.getLibraryName();
      name_ = userSpacePath.getObjectName();
   }



   /**
     Sets the AS/400 to run the program.                         //$C0A

     @param system  The AS/400 on which to run the program.

     @exception PropertyVetoException If a change is vetoed.
     **/
   void setSystem (AS400Impl system)
   {
      system_ = system;
   }

   abstract void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary)
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException;
}
