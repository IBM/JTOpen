///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCloseReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
Close file request.
**/
class IFSCloseReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int DATA_FLAGS_OFFSET = 26;
  private static final int CCSID = 28;
  private static final int AMOUNT_ACCESSED_OFFSET = 30;
  private static final int ACCESS_HISTORY_OFFSET = 32;
  private static final int MODIFY_DATE_OFFSET = 33;
  private static final int TEMPLATE_LENGTH = 21;

/**
Construct a close request.
@param fileHandle the file handle returned from an open request
**/
  IFSCloseReq(int fileHandle)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0009);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set16bit(2, DATA_FLAGS_OFFSET);
    set16bit(0xffff, CCSID);
    set16bit(100, AMOUNT_ACCESSED_OFFSET);
    data_[ACCESS_HISTORY_OFFSET] = 0;
    setData(0L, MODIFY_DATE_OFFSET);
  }

/**
Construct a close request.
**/
  IFSCloseReq()
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0009);
    set16bit(0xffff, CCSID);
    set16bit(100, AMOUNT_ACCESSED_OFFSET);
    data_[ACCESS_HISTORY_OFFSET] = 0;
    setData(0L, MODIFY_DATE_OFFSET);
  }

  /**
   **/
  void setFileHandle(int fileHandle)
  {
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
  }

  /**
   **/
  void setDataFlags(int dataFlags)
  {
    set16bit(dataFlags, DATA_FLAGS_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}





