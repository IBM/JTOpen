///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSGetFileSystemReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
'Get file system information' request.
**/
class IFSGetFileSystemReq extends IFSDataStreamReq
{

/**
Construct a list attributes request.
@param name the file name (may contain wildcard characters * and ?)
**/
  static final int NO_AUTHORITY_REQUIRED = 0;
  static final int READ_AUTHORITY_REQUIRED = 1;
  static final int WRITE_AUTHORITY_REQUIRED = 2;
  static final int EXEC_AUTHORITY_REQUIRED = 4;

  static final short DEFAULT_ATTR_LIST_LEVEL = 1;  // default value for File Attribute List Level

  static final int OA_NONE = 0;
  static final int OA1     = 1;
  static final int OA2     = 2;

  private static final int Object_HANDLE_OFFSET = 22;
  private static final int CCSID_OFFSET = 26;
  private static final int WORKING_DIR_HANDLE_OFFSET = 28;
  private static final int CHECK_AUTHORITY_OFFSET = 32;
  private static final int MAX_GET_COUNT_OFFSET = 34;
  private static final int FILE_ATTR_LIST_LEVEL_OFFSET = 36;
  private static final int PATTERN_MATCHING_OFFSET = 38;

  private static final int OPTIONAL_SECTION_OFFSET = 40;

  private static final int FILE_NAME_LL_OFFSET = OPTIONAL_SECTION_OFFSET;
  private static final int FILE_NAME_CP_OFFSET = OPTIONAL_SECTION_OFFSET + 4;
  private static final int FILE_NAME_OFFSET    = OPTIONAL_SECTION_OFFSET + 6;

  private static final int OA1_FLAGS_LL_OFFSET = OPTIONAL_SECTION_OFFSET;
  private static final int OA1_FLAGS_CP_OFFSET = OPTIONAL_SECTION_OFFSET + 4;
  private static final int OA1_FLAGS_OFFSET1   = OPTIONAL_SECTION_OFFSET + 6;
  private static final int OA1_FLAGS_OFFSET2   = OPTIONAL_SECTION_OFFSET + 10;
  // Note: We never specify a 'file name' when we specify 'OA1 flags'.

  private static final int HEADER_LENGTH = 20;
  private static final int TEMPLATE_LENGTH = 6;
  private static final int LLCP_LENGTH = 6;            // @A1a


  IFSGetFileSystemReq(int objectHandler)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH); // @A1A
                       // Note: EA list fixed header is 8 bytes; repeating header is 10 bytes for each name-only EA structure.
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0021);
    
    
    set32bit(objectHandler, Object_HANDLE_OFFSET);
    
  }
  
  IFSGetFileSystemReq(int objectHandler, int userHandler)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH); 
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0021);
    setCSInstance(userHandler);
    
    set32bit(objectHandler, Object_HANDLE_OFFSET);
    
  }

 

}




