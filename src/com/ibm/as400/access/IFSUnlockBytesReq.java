///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSUnlockBytesReq.java
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
Unlock file bytes request.
**/
class IFSUnlockBytesReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;
  private static final int RELATIVE_OFFSET_OFFSET = 30;
  private static final int UNLOCK_LENGTH_OFFSET = 34;
  private static final int UNLOCK_FLAGS_OFFSET = 38;
  private static final int TEMPLATE_LENGTH = 20;

/**
Construct an unlock bytes request.
@param fileHandle the file handle
@param offset the byte offset of the start of the lock in the file
@param length the number of bytes that are locked
@param isMandatory if True the lock is mandatory, otherwise it is advisory
**/
  IFSUnlockBytesReq(int     fileHandle,
                    int     offset,
                    int     length,
                    boolean isMandatory)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0008);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set32bit(0, BASE_OFFSET_OFFSET);
    set32bit(offset, RELATIVE_OFFSET_OFFSET);
    set32bit(length, UNLOCK_LENGTH_OFFSET);
    set16bit((isMandatory ? 0 : 1), UNLOCK_FLAGS_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}



