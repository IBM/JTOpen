///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSKey.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
The IFSKey class represents an opaque handle to a byte lock on an integrated file system object.  IFSKey objects are constructed by the lock methods of various IFS classes.
@see IFSFileInputStream#lock
@see IFSFileInputStream#unlock
@see IFSFileOutputStream#lock
@see IFSFileOutputStream#unlock
@see IFSRandomAccessFile#lock
@see IFSRandomAccessFile#unlock
**/
public class IFSKey 
  implements java.io.Serializable //@A1A
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;
  

  int fileHandle_;      // the file
  int offset_;          // the start offset in the file
  int length_;          // the number of bytes locked
  boolean isMandatory_; // true if mandatory, otherwise advisory

/**
Constructs as IFSKey object.
@param fileHandle The file handle of the file to which this key belongs.
@param offset The byte offset of the start of the lock.
@param length The number of bytes that are locked.
**/
  IFSKey(int fileHandle,
         int offset,
         int length,
         boolean isMandatory)
  {
    fileHandle_ = fileHandle;
    offset_ = offset;
    length_ = length;
    isMandatory_ = isMandatory;
  }

  //@A1A
  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException If an error occurs while communicating with the server.
   @exception ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();
  }

}




