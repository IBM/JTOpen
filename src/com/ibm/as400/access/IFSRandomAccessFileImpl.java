///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSRandomAccessFileImpl.java
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
 Specifies the methods which the implementation objects for the IFSRandomAccessFile class
 need to support.
 **/
interface IFSRandomAccessFileImpl
{
  void close()  throws IOException;
  void connectAndOpen()  throws AS400SecurityException, IOException;
  void flush()  throws IOException;
  long length()  throws IOException;
  IFSKey lock(int offset,
              int length)  throws IOException;
  void open()  throws IOException;
  int read(byte[] data,
           int    dataOffset,
           int    length,
           boolean readFully)  throws IOException;
  String readLine()  throws IOException;
  String readUTF()  throws IOException;
  void setExistenceOption(int existenceOption);

  // Note: This must be the first method called on a new object:
  void setFD(IFSFileDescriptorImpl fd);

  void setForceToStorage(boolean forceToStorage);
  void setLength(int length)  throws IOException;
  void setMode(String mode);
  void unlock(IFSKey key)  throws IOException;
  void writeBytes(byte[]  data,
                  int dataOffset,
                  int length)  throws IOException;
  void writeUTF(String s)  throws IOException;
}




