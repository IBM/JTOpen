///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileOutputStreamImpl.java
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
 Specifies the methods which the implementation objects for the IFSFileOutputStream
 and IFSTextFileOutputStream classes need to support.
 **/
interface IFSFileOutputStreamImpl
{
  void close()  throws IOException;
  void connectAndOpen(int ccsid)
    throws AS400SecurityException, IOException;
  void flush()  throws IOException;
  IFSKey lock(int length)  throws IOException;
  void open(int fileDataCCSID)  throws IOException;
  void setAppend(boolean append);

  // Note: This must be the first method called on a new object:
  void setFD(IFSFileDescriptorImpl fd);

  void unlock(IFSKey key)  throws IOException;
  void write(byte[] data,
                    int    dataOffset,
                    int    length)  throws IOException;

  // Provided for use by IFSTextFileOutputStream:
  void writeText(String data, int ccsid)  throws IOException;

}
