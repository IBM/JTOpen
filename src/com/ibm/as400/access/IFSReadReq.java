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


/**
Read data request.
**/
class IFSReadReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int HEADER_LENGTH = 20;

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;
  private static final int RELATIVE_OFFSET_OFFSET = 30;
  private static final int READ_LENGTH_OFFSET = 34;
  private static final int PREREAD_LENGTH_OFFSET = 38;

  // Additional fields if datastreamLevel >= 16:
  private static final int LARGE_BASE_OFFSET_OFFSET = 42;
  private static final int LARGE_RELATIVE_OFFSET_OFFSET = 50;


/**
Construct a read request.
@param fileHandle the file handle
@param offset the offset (in bytes) of the file data
@param length the number of bytes to read
@param datastreamLevel the datastream level of the server
**/
  IFSReadReq(int fileHandle,
             long offset,
             int length,
             int  datastreamLevel)
  {
    super(HEADER_LENGTH + getTemplateLength(datastreamLevel));
    setLength(data_.length);
    setTemplateLen(getTemplateLength(datastreamLevel));
    setReqRepID(0x0003);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);

    // Set the offset fields.
    if (datastreamLevel < 16)
    { // Just set the old fields.
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit((int)offset, RELATIVE_OFFSET_OFFSET);
    }
    else
    {
      // The old fields must be zero.
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit(0, RELATIVE_OFFSET_OFFSET);

      // Also set the new "large" fields.
      set64bit(0L, LARGE_BASE_OFFSET_OFFSET);  // must be zero, even for DSL 16
      set64bit(offset, LARGE_RELATIVE_OFFSET_OFFSET);
    }

    set32bit(length, READ_LENGTH_OFFSET);
    set32bit(0, PREREAD_LENGTH_OFFSET);
  }

  private final static int getTemplateLength(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 22 : 38);
  }

}





