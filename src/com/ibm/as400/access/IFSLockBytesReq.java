///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSLockBytesReq.java
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
Lock bytes request.
**/
class IFSLockBytesReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int LOCK_FLAGS_OFFSET = 26;
  private static final int LOCK_LIST_LL_OFFSET = 28;
  private static final int LOCK_LIST_CP_OFFSET = 32;
  private static final int LOCK_TYPE_OFFSET = 34;
  private static final int LOCK_BASE_OFFSET_OFFSET = 36;
  private static final int LOCK_RELATIVE_OFFSET_OFFSET = 40;
  private static final int LOCK_LENGTH_OFFSET = 44;
  private static final int TEMPLATE_LENGTH = 8;

/**
Construct a lock bytes request.
@param fileHandle the file handle
@param isMandatory if True the lock is mandatory, otherwise it is advisory
@param isShared if True the lock is shared, otherwise it is exclusive
@param offset the byte offset of the start of the lock in the file
@param length the number of bytes to be locked
**/
  IFSLockBytesReq(int     fileHandle,
                  boolean isMandatory,
                  boolean isShared,
                  int     offset,
                  int     length)
  {
    super(20 + TEMPLATE_LENGTH + 20);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0007);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set16bit((isMandatory ? 0 : 1), LOCK_FLAGS_OFFSET);
    set32bit(20, LOCK_LIST_LL_OFFSET);
    set16bit(6, LOCK_LIST_CP_OFFSET);
    set16bit((isShared ? 0 : 1), LOCK_TYPE_OFFSET);
    set32bit(0, LOCK_BASE_OFFSET_OFFSET);
    set32bit(offset, LOCK_RELATIVE_OFFSET_OFFSET);
    set32bit(length, LOCK_LENGTH_OFFSET);
  }

}
    




