///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileDescriptorImpl.java
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
 Specifies the methods which the implementation objects for the IFSFileDescriptor class
 need to support.
 **/
interface IFSFileDescriptorImpl
{
  void close();

  int getFileOffset();

  void incrementFileOffset(int fileOffsetIncrement); // used by IFSRandomAccessFile.skipBytes()

  // Note: This should be the first method called on a newly-constructed object.
  void initialize(int fileOffset, Object parentImpl, String path, int shareOption,
                  AS400Impl system);

  boolean isOpen();

  void setFileOffset(int fileOffset);

  void sync() throws IOException;

}




