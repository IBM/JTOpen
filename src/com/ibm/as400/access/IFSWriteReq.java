///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSWriteReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
Write data request.
**/
class IFSWriteReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int HEADER_LENGTH = 20;

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int BASE_OFFSET_OFFSET = 26;
  private static final int RELATIVE_OFFSET_OFFSET = 30;
  private static final int DATA_FLAGS_OFFSET = 34;
  private static final int CCSID_OFFSET = 36;   

  // Additional fields if datastreamLevel >= 16:
  private static final int LARGE_BASE_OFFSET_OFFSET = 38;
  private static final int LARGE_RELATIVE_OFFSET_OFFSET = 46;


/**
Construct a write request.
@param fileHandle the file handle
@param fileOffset the offset (in bytes) in the file
@param data the data to write
@param dataOffset the offset (in bytes) in the data to write
@param dataLength the number of bytes to write
@param dataCCSID the code page and character set of data (0xffff if unknown)
@param forceToStorage if the data must be written to disk before the server replies
@param datastreamLevel the datastream level of the server
**/
  IFSWriteReq(int    fileHandle,
              long   fileOffset,
              byte[] data,
              int    dataOffset,
              int    dataLength,
              int    dataCCSID,
              boolean forceToStorage,
              int     datastreamLevel)
  {
    super(HEADER_LENGTH + getTemplateLength(datastreamLevel) + 6 + dataLength);
    setLength(data_.length);
    setTemplateLen(getTemplateLength(datastreamLevel));
    setReqRepID(0x0004);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);

    // Set the beginning data offset.  Use a base offset of zero.
    setOffsetFields(fileOffset, datastreamLevel);

    // Request a reply.
    // If forceToStorage is true, need a sync write, thus both bits
    // need to be turned on. Otherwise, only bit 1 needs to be on.
    set16bit( (forceToStorage) ? 3 : 2 , DATA_FLAGS_OFFSET);

    // Set data CCSID
    set16bit(dataCCSID, CCSID_OFFSET);

    // Set the data LL.
    set32bit(dataLength + 6, getFileDataLLOffset(datastreamLevel));

    // Set the code point.
    set16bit(0x0020, getFileDataCPOffset(datastreamLevel));

    // Copy the data to be written, into the "file data" field of the request.
    System.arraycopy(data, dataOffset, data_, getFileDataOffset(datastreamLevel), dataLength);

  }

  private final static int getTemplateLength(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 18 : 34);
  }


  // Determine offset (from beginning of request) to the 4-byte File Data "LL" field.
  private final static int getFileDataLLOffset(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 38 : 54);
  }

  // Determine offset (from beginning of request) to the 2-byte File Data "CP" field.
  private final static int getFileDataCPOffset(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 42 : 58);
  }

  // Determine offset (from beginning of request) to the File Data value field (follows LL/CP).
  private final static int getFileDataOffset(int datastreamLevel)
  {
    return (datastreamLevel < 16 ? 44 : 60);
  }

  // Sets the values of the fields relating to data offset.
  private final void setOffsetFields(long fileOffset, int datastreamLevel)
  {
    if (datastreamLevel < 16)
    { // Just set the old fields.
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit((int)fileOffset, RELATIVE_OFFSET_OFFSET);
    }
    else
    {
      // Old fields must be zero.
      set32bit(0, BASE_OFFSET_OFFSET);
      set32bit(0, RELATIVE_OFFSET_OFFSET);

      // Also set the new "large" fields.
      set64bit(0L, LARGE_BASE_OFFSET_OFFSET);
      set64bit(fileOffset, LARGE_RELATIVE_OFFSET_OFFSET);
    }
  }

}


