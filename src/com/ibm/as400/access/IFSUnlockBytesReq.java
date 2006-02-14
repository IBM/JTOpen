///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSUnlockBytesReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
Unlock file bytes request.
**/
class IFSUnlockBytesReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int HEADER_LENGTH = 20;

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;     // field must be 0 if DSL<16
  private static final int RELATIVE_OFFSET_OFFSET = 30; // field must be 0 if DSL<16
  private static final int UNLOCK_LENGTH_OFFSET = 34;   // field must be 0 if DSL<16
  private static final int UNLOCK_FLAGS_OFFSET = 38;

  // Additional fields if datastreamLevel >= 16:
  private static final int LARGE_BASE_OFFSET_OFFSET = 40;
  private static final int LARGE_RELATIVE_OFFSET_OFFSET = 48;
  private static final int LARGE_UNLOCK_LENGTH_OFFSET = 56;

/**
Construct an unlock bytes request.
@param fileHandle the file handle
@param offset the byte offset of the start of the lock in the file
@param length the number of bytes that are locked
@param isMandatory if True the lock is mandatory, otherwise it is advisory
**/
  IFSUnlockBytesReq(int     fileHandle,
                    long    offset,
                    long    length,
                    boolean isMandatory,
                    int     datastreamLevel)
  {
    super(HEADER_LENGTH + getTemplateLength(datastreamLevel));  // no optional/variable section
    setLength(data_.length);
    setTemplateLen(getTemplateLength(datastreamLevel));
    setReqRepID(0x0008);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set16bit((isMandatory ? 0 : 1), UNLOCK_FLAGS_OFFSET);

    if (datastreamLevel < 16)
    { // set old fields
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit((int)offset, RELATIVE_OFFSET_OFFSET);
      set32bit((int)length, UNLOCK_LENGTH_OFFSET);
    }
    else
    {  // old fields must be zero
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit(0, RELATIVE_OFFSET_OFFSET);
      set32bit(0, UNLOCK_LENGTH_OFFSET);

      // new "large" fields
      set64bit(0L, LARGE_BASE_OFFSET_OFFSET);
      set64bit(offset, LARGE_RELATIVE_OFFSET_OFFSET);
      set64bit(length, LARGE_UNLOCK_LENGTH_OFFSET);
    }
  }

  private final static int getTemplateLength(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 20 : 44);
  }

}



