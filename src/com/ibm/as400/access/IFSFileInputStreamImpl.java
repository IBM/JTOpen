///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileInputStreamImpl.java
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
 Specifies the methods which the implementation objects for the IFSFileInputStream
 and IFSTextFileInputStream classes need to support.
 **/
interface IFSFileInputStreamImpl
{
  int available()  throws IOException;
  void close()  throws IOException;
  void connectAndOpen()  throws AS400SecurityException, IOException;
  IFSKey lock(int length)  throws IOException;
  void open()  throws IOException;
  int read(byte[] data,
                  int    dataOffset,
                  int    length)     throws IOException;
  // Used only by subclass IFSTextFileInputStream:
  String readText(int length)  throws IOException;

  // Note: This must be the first method called on a new object:
  void setFD(IFSFileDescriptorImpl fd);

  long skip(long bytesToSkip)  throws IOException;
  void unlock(IFSKey key)  throws IOException;

}
