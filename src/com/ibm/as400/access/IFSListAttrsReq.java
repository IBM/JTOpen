///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSListAttrsReq.java
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
List file attributes request.
**/
class IFSListAttrsReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

/**
Construct a list attributes request.
@param name the file name (may contain wildcard characters * and ?)
**/
  static final int NO_AUTHORITY_REQUIRED = 0;
  static final int READ_AUTHORITY_REQUIRED = 1;
  static final int WRITE_AUTHORITY_REQUIRED = 2;
  static final int EXEC_AUTHORITY_REQUIRED = 4;
  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int CCSID_OFFSET = 26;
  private static final int WORKING_DIR_HANDLE_OFFSET = 28;
  private static final int CHECK_AUTHORITY_OFFSET = 32;
  private static final int MAX_GET_COUNT_OFFSET = 34;
  private static final int FILE_ATTR_LIST_LEVEL_OFFSET = 36;
  private static final int PATTERN_MATCHING_OFFSET = 38;
  private static final int FILE_NAME_LL_OFFSET = 40;
  private static final int FILE_NAME_CP_OFFSET = 44;
  private static final int FILE_NAME_OFFSET = 46;
  private static final int TEMPLATE_LENGTH = 20;

  private String x = Copyright.copyright;

/**
Construct a list file attributes request.
@param name the file name
@param fileNameCCSID file name CCSID
@param authority bit 0: on = client must have read authority, bit 1: on = client must have write authority, bit 2: on = client must have execute authority
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID,
                  int    authority)
  {
    super(20 + TEMPLATE_LENGTH + 6 + name.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000A);
    set32bit(0, FILE_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(authority, CHECK_AUTHORITY_OFFSET);
    set16bit(0xffff, MAX_GET_COUNT_OFFSET);
    set16bit(1, FILE_ATTR_LIST_LEVEL_OFFSET);
    set16bit(0, PATTERN_MATCHING_OFFSET);

    // Set the LL.
    set32bit(name.length + 6, FILE_NAME_LL_OFFSET);

    // Set the code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the file name characters.
    System.arraycopy(name, 0, data_, FILE_NAME_OFFSET, name.length);
  }

/**
Construct a list file attributes request.
@param name the file name
@param fileNameCCSID file name CCSID
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID)
  {
    this(name, fileNameCCSID, NO_AUTHORITY_REQUIRED);
  }

/**
Construct a list file attributes request.
@param handle the file handle
**/
  IFSListAttrsReq(int handle)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000A);
    set32bit(handle, FILE_HANDLE_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(NO_AUTHORITY_REQUIRED, CHECK_AUTHORITY_OFFSET);
    set16bit(0xffff, MAX_GET_COUNT_OFFSET);
    set16bit(1, FILE_ATTR_LIST_LEVEL_OFFSET);
    set16bit(0, PATTERN_MATCHING_OFFSET);
  }

/**
Construct a list file attributes request.
@param handle the file handle
@param attributeListLevel the file attribute list level
**/
  IFSListAttrsReq(int handle, short attributeListLevel)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000A);
    set32bit(handle, FILE_HANDLE_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(NO_AUTHORITY_REQUIRED, CHECK_AUTHORITY_OFFSET);
    set16bit(0xffff, MAX_GET_COUNT_OFFSET);
    set16bit(attributeListLevel, FILE_ATTR_LIST_LEVEL_OFFSET);
    set16bit(0, PATTERN_MATCHING_OFFSET);
  }

}




