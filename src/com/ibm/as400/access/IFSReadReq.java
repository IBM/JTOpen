///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSReadReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
Read data request.
**/
class IFSReadReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;
  private static final int RELATIVE_OFFSET_OFFSET = 30;
  private static final int READ_LENGTH_OFFSET = 34;
  private static final int PREREAD_LENGTH_OFFSET = 38;
  private static final int TEMPLATE_LENGTH = 22;

/**
Construct a read request.
@param fileHandle the file handle
@param offset the offset (in bytes) of the file data
@param length the number of bytes to read
**/
  IFSReadReq(int fileHandle,
             int offset,
             int length)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0003);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set32bit(0, BASE_OFFSET_OFFSET);
    set32bit(offset, RELATIVE_OFFSET_OFFSET);
    set32bit(length, READ_LENGTH_OFFSET);
    set32bit(0, PREREAD_LENGTH_OFFSET);
  }

}





