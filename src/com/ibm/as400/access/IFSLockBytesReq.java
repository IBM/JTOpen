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


/**
Lock bytes request.
**/
class IFSLockBytesReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int HEADER_LENGTH = 20;
  private static final int TEMPLATE_LENGTH = 8;

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int LOCK_FLAGS_OFFSET = 26;
  private static final int LOCK_LIST_LL_OFFSET = 28;
  private static final int LOCK_LIST_CP_OFFSET = 32;
  private static final int LOCK_TYPE_OFFSET = 34;
  private static final int LOCK_BASE_OFFSET_OFFSET = 36;

  // Values if datastream level is less than 16:
  private static final int RELATIVE_OFFSET_OFFSET = 40;
  private static final int LENGTH_OFFSET = 44;

  // Values if datastream level is 16 (or higher):
  private static final int LARGE_RELATIVE_OFFSET_OFFSET = 44;
  private static final int LARGE_LENGTH_OFFSET = 52;

/**
Construct a lock bytes request.
@param fileHandle the file handle
@param isMandatory if True the lock is mandatory, otherwise it is advisory
@param isShared if True the lock is shared, otherwise it is exclusive
@param offset the byte offset of the start of the lock in the file
@param length the number of bytes to be locked
@param datastreamLevel the datastream level of the server
**/
  IFSLockBytesReq(int     fileHandle,
                  boolean isMandatory,
                  boolean isShared,
                  long    offset,
                  long    length,
                  int     datastreamLevel)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + getVariableSectionLength(datastreamLevel));
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0007);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set16bit((isMandatory ? 0 : 1), LOCK_FLAGS_OFFSET);
    set16bit(6, LOCK_LIST_CP_OFFSET);
    set16bit((isShared ? 0 : 1), LOCK_TYPE_OFFSET);

    if (datastreamLevel < 16)
    { // 4-byte lengths and offsets
      set32bit(20, LOCK_LIST_LL_OFFSET);
      set32bit(0, LOCK_BASE_OFFSET_OFFSET);
      set32bit((int)offset, RELATIVE_OFFSET_OFFSET);
      set32bit((int)length, LENGTH_OFFSET);
    }
    else
    { // 8-byte lengths and offsets
      set32bit(32, LOCK_LIST_LL_OFFSET);
      set64bit(0L, LOCK_BASE_OFFSET_OFFSET);
      set64bit(offset, LARGE_RELATIVE_OFFSET_OFFSET);
      set64bit(length, LARGE_LENGTH_OFFSET);
    }
  }

  private final static int getVariableSectionLength(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 20 : 32);
  }

}
    




