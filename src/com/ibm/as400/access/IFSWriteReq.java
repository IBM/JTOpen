///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSWriteReq.java
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
Write data request.
**/
class IFSWriteReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;
  private static final int RELATIVE_OFFSET_OFFSET = 30;
  private static final int DATA_FLAGS_OFFSET = 34;
  private static final int CCSID_OFFSET = 36;   
  private static final int FILE_DATA_LL_OFFSET = 38;
  private static final int FILE_DATA_CP_OFFSET = 42;
  private static final int FILE_DATA_OFFSET = 44;
  private static final int TEMPLATE_LENGTH = 18;

/**
Construct a write request.
@param fileHandle the file handle
@param fileOffset the offset (in bytes) in the file
@param data the data to write
@param dataOffset the offset (in bytes) in the data to write
@param dataLength the number of bytes to write
@param dataCCSID the code page and character set of data (0xffff if unknown)
**/
  IFSWriteReq(int    fileHandle,
              int    fileOffset,
              byte[] data,
              int    dataOffset,
              int    dataLength,
              int    dataCCSID)
  {
    super(20 + TEMPLATE_LENGTH + 6 + dataLength);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0004);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);

    // Set the beginning data offset.  Use a base offset of zero.
    set32bit(0, BASE_OFFSET_OFFSET);
    set32bit(fileOffset, RELATIVE_OFFSET_OFFSET);

    // Request a reply.
    set16bit(2 , DATA_FLAGS_OFFSET);

    // Set data CCSID
    set16bit(dataCCSID, CCSID_OFFSET);

    // Set the data LL.
    set32bit(dataLength + 6, FILE_DATA_LL_OFFSET);

    // Set the code point.
    set16bit(0x0020, FILE_DATA_CP_OFFSET);

    // Copy the data.
    System.arraycopy(data, dataOffset, data_, FILE_DATA_OFFSET, dataLength);
  }

/**
Construct a write request.
@param fileHandle the file handle
@param fileOffset the offset (in bytes) in the file
@param data the data to write
@param dataOffset the offset (in bytes) in the data to write
@param dataLength the number of bytes to write
@param dataCCSID the code page and character set of data (0xffff if unknown)
@param forceToStorage if the data must be written to disk before the server replies
**/
  IFSWriteReq(int    fileHandle,
              int    fileOffset,
              byte[] data,
              int    dataOffset,
              int    dataLength,
              int    dataCCSID,
              boolean forceToStorage)
  {
    this(fileHandle, fileOffset, data, dataOffset, dataLength, dataCCSID);

    // Request a reply.
    // If forceToStorage is true, need a sync write, thus both bits
    // need to be turned on. Otherwise, only bit 1 needs to be on.
    set16bit( (forceToStorage) ? 3 : 2 , DATA_FLAGS_OFFSET);
  }


  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}


