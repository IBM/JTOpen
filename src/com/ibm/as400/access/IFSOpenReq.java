///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSOpenReq.java
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
Open file request.
**/
class IFSOpenReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final int PROGRAM_LOAD_ACCESS = 4; // access intent
  static final int READ_ACCESS = 1;         // access intent
  static final int WRITE_ACCESS = 2;        // access intent
  static final int DENY_NONE = 0;           // share option
  static final int DENY_READERS = 1;        // share option
  static final int DENY_WRITERS = 2;        // share option
  static final int NO_CONVERSION = 0;           // file data conv. opt.
  static final int CONVERT_TO_CLIENT_CCSID = 1; // file data conv. opt.
  static final int CONVERT_TO_SERVER_CCSID = 2; // file data conv. opt.

                                                    // @D1a: add the following open options.
                                                    //     IF FILE DOES NOT EXIST    |   IF FILE DOES EXIST
                                                    // ----------------------------- |  ---------------------
  static final int OPEN_OPTION_CREATE_OPEN    =  1; // create if file does not exist |  open existing file
  static final int OPEN_OPTION_CREATE_REPLACE =  2; // create if file does not exist |  replace existing file
  static final int OPEN_OPTION_CREATE_FAIL    =  4; // create if file does not exist |  fail if file exists
  static final int OPEN_OPTION_FAIL_OPEN      =  8; // fail if file does not exist   |  open if file exists
  static final int OPEN_OPTION_FAIL_REPLACE   = 16; // fail if file does not exist   |  replace if file exists

  private static final int FILE_NAME_CCSID_OFFSET = 22;
  private static final int WORKING_DIR_HANDLE_OFFSET = 24;
  private static final int FILE_DATA_CCSID_OFFSET = 28;
  private static final int ACCESS_OFFSET = 30;
  private static final int FILE_SHARING_OFFSET = 32;
  private static final int DATA_CONVERSION_OFFSET = 34;
  private static final int DUPLICATE_FILE_OPT_OFFSET = 36;
  private static final int CREATE_SIZE_OFFSET = 38;
  private static final int FIXED_ATTRS_OFFSET = 42;
  private static final int ATTRS_LIST_LEVEL_OFFSET = 46;
  private static final int PRE_READ_OFFSET_OFFSET = 48;
  private static final int PRE_READ_LENGTH_OFFSET = 52;
  private static final int FILE_NAME_LL_OFFSET = 56;
  private static final int FILE_NAME_CP_OFFSET = 60;
  private static final int FILE_NAME_OFFSET = 62;
  private static final int TEMPLATE_LENGTH = 36;

/**
Construct an open file request.
@param fileName the name of the file to open
@param fileDataCCSID CCSID of the file data
@param accessIntent the intended access
@param fileSharing how the file can be shared with other users
@param dataConversionOption indicates the type of conversion the server performs
@param duplicateFileOption bit 4: on = fail the open if the file doesn't exist, replace the file if it does, bit 3: on = fail the open if the file doesn't exist, open the file if it does, bit 2: on = create then open the file if it doesn't exist, fail the open if it does, bit 1: on = create then open the file if it doesn't exist, replace the file if it does, bit 0: on = create then open the file if it doesn't exist, open the file if it does
**/
  IFSOpenReq(byte[] fileName,
             int    fileNameCCSID,
             int    fileDataCCSID,
             int    accessIntent,
             int    fileSharing,
             int    dataConversionOption,
             int    duplicateFileOption)
  {
    super(20 + TEMPLATE_LENGTH + 6 + fileName.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0002);
    set16bit(fileNameCCSID, FILE_NAME_CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(fileDataCCSID, FILE_DATA_CCSID_OFFSET);
    set16bit(accessIntent, ACCESS_OFFSET);
    set16bit(fileSharing, FILE_SHARING_OFFSET);
    set16bit(dataConversionOption, DATA_CONVERSION_OFFSET);
    set16bit(duplicateFileOption, DUPLICATE_FILE_OPT_OFFSET);
    set32bit(0, CREATE_SIZE_OFFSET);
    set32bit(0, FIXED_ATTRS_OFFSET);
    set16bit(1, ATTRS_LIST_LEVEL_OFFSET);
    set32bit(0, PRE_READ_OFFSET_OFFSET);
    set32bit(0, PRE_READ_LENGTH_OFFSET);

    // Set the LL.
    set32bit(fileName.length + 6, FILE_NAME_LL_OFFSET);

    // Set the code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the file name characters.
    System.arraycopy(fileName, 0, data_, FILE_NAME_OFFSET, fileName.length);
  }

}




