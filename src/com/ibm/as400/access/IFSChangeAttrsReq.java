///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSChangeAttrsReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
Change attributes request.
**/
class IFSChangeAttrsReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int CCSID_OFFSET = 26;  // CCSID of the filename
  private static final int WORKING_DIR_HANDLE_OFFSET = 28;
  private static final int ATTR_LIST_LEVEL_OFFSET = 32;
  private static final int CREATE_DATE_OFFSET = 34;
  private static final int MODIFY_DATE_OFFSET = 42;
  private static final int ACCESS_DATE_OFFSET = 50;
  private static final int SET_FLAGS_OFFSET = 58;
  private static final int FIXED_ATTRS_OFFSET = 60;
  private static final int FILE_SIZE_OFFSET = 64;
  private static final int FILE_NAME_LL_OFFSET = 68;
  private static final int FILE_NAME_CP_OFFSET = 72;
  private static final int FILE_NAME_OFFSET = 74;
  private static final int TEMPLATE_LENGTH = 48;

/**
Construct a change attributes request.  Use this request to change
the size of the file by file handle (the file is open)
@param fileHandle handle of file to change
@param fileSize the desired file size in bytes
**/
  IFSChangeAttrsReq(int  fileHandle,
                    int  fileSize)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000b);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(1, ATTR_LIST_LEVEL_OFFSET);
    setData(0L, CREATE_DATE_OFFSET);
    setData(0L, MODIFY_DATE_OFFSET);
    setData(0L, ACCESS_DATE_OFFSET);
    set16bit(1, SET_FLAGS_OFFSET);
    set32bit(fileSize, FILE_SIZE_OFFSET);
  }

/**
Construct a change attributes request.  Use this form to change the
date/time stamps of the file by handle (the file is open).
@param fileHandle handle of file to change
@param createDate the desired creation date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
@param modifyDate the desired last modification date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
@param accessDate the desired last access date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
**/
  IFSChangeAttrsReq(int  fileHandle,
                    long createDate,
                    long modifyDate,
                    long accessDate)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000b);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(1, ATTR_LIST_LEVEL_OFFSET);
    setDate(createDate, CREATE_DATE_OFFSET);
    setDate(modifyDate, MODIFY_DATE_OFFSET);
    setDate(accessDate, ACCESS_DATE_OFFSET);
    set16bit(0, SET_FLAGS_OFFSET);
  }

/**
Construct a change attributes request.  Use this form to change
the size of the file by file name.
@param fileName the name of the file to change
@param fileNameCCSID file name CCSID
@param fileSize the desired file size in bytes
**/
  IFSChangeAttrsReq(byte[] fileName,
                    int    fileNameCCSID,
                    int    fileSize)
  {
    super(20 + TEMPLATE_LENGTH + 6 + fileName.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000b);
    set32bit(0, FILE_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(1, ATTR_LIST_LEVEL_OFFSET);
    setData(0L, CREATE_DATE_OFFSET);
    setData(0L, MODIFY_DATE_OFFSET);
    setData(0L, ACCESS_DATE_OFFSET);
    set16bit(1, SET_FLAGS_OFFSET);
    set32bit(fileSize, FILE_SIZE_OFFSET);

    // Set the LL.
    set32bit(fileName.length + 6, FILE_NAME_LL_OFFSET);

    // Set the code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the file name characters.
    System.arraycopy(fileName, 0, data_, FILE_NAME_OFFSET, fileName.length);
  }

/**
Construct a change attributes request.  Use this form to change the fixed attributes
of the file.
@param fileName the name of the file to change
@param fileNameCCSID file name CCSID
@param fixedAttributes the fixed attributes to set
@param stuff          an extra boolean that is ignored.  It is needed
                      so the signature of this constructor is unique.
**/
  IFSChangeAttrsReq(byte[]  fileName,                                      //@D1a
                    int     fileNameCCSID,                                 //@D1a
                    int     fixedAttributes,                               //@D1a
                    boolean extraneousDataToMakeSignatureUnique)           //@D1a
  {                                                                        //@D1a
    super(20 + TEMPLATE_LENGTH + 6 + fileName.length);                     //@D1a
    setLength(data_.length);                                               //@D1a
    setTemplateLen(TEMPLATE_LENGTH);                                       //@D1a
    setReqRepID(0x000b);                                                   //@D1a
    set32bit(0, FILE_HANDLE_OFFSET);                                       //@D1a
    set16bit(fileNameCCSID, CCSID_OFFSET);                                 //@D1a
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);                                //@D1a
    set16bit(1, ATTR_LIST_LEVEL_OFFSET);                                   //@D1a
    setData(0L, CREATE_DATE_OFFSET);                                       //@D1a
    setData(0L, MODIFY_DATE_OFFSET);                                       //@D1a
    setData(0L, ACCESS_DATE_OFFSET);                                       //@D1a
    set16bit(2, SET_FLAGS_OFFSET);                                         //@D1a
    set32bit(fixedAttributes, FIXED_ATTRS_OFFSET);                         //@D1a
    set32bit(0, FILE_SIZE_OFFSET);                                         //@D1a
                                                                           //@D1a
    // Set the LL.                                                         //@D1a
    set32bit(fileName.length + 6, FILE_NAME_LL_OFFSET);                    //@D1a
                                                                           //@D1a
    // Set the code point.                                                 //@D1a
    set16bit(0x0002, FILE_NAME_CP_OFFSET);                                 //@D1a
                                                                           //@D1a
    // Set the file name characters.                                       //@D1a
    System.arraycopy(fileName, 0, data_, FILE_NAME_OFFSET, fileName.length);  //@D1a
  }                                                                        //@D1a
                                                                           //@D1a
                                                                           //@D1a

/**
Construct a change attributes request.  Use this form to change the
date/time stamp of the file by file name.
@param fileName the name of the file to change
@param fileNameCCSID file name CCSID
@param createDate the desired creation date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
@param modifyDate the desired last modification date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
@param accessDate the desired last access date (measured in milliseconds since
January 1, 1970 00:00:00 GMT)
**/
  IFSChangeAttrsReq(byte[] fileName,
                    int    fileNameCCSID,
                    long   createDate,
                    long   modifyDate,
                    long   accessDate)
  {
    super(20 + TEMPLATE_LENGTH + 6 + fileName.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000b);
    set32bit(0, FILE_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(1, ATTR_LIST_LEVEL_OFFSET);
    setDate(createDate, CREATE_DATE_OFFSET);
    setDate(modifyDate, MODIFY_DATE_OFFSET);
    setDate(accessDate, ACCESS_DATE_OFFSET);
    set16bit(0, SET_FLAGS_OFFSET);

    // Set the LL.
    set32bit(fileName.length + 6, FILE_NAME_LL_OFFSET);

    // Set the code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the file name characters.
    System.arraycopy(fileName, 0, data_, FILE_NAME_OFFSET, fileName.length);
  }


/**
Construct a change attributes request.  Use this form to change the file data CCSID.
@param fileName the name of the file to change
@param fileNameCCSID file name CCSID
@param oa2Structure The updated OA2x structure.  This includes the LLCP.
**/
  IFSChangeAttrsReq(byte[] fileName,
                    int    fileNameCCSID,
                    byte[] oa2Structure)
  {
    super(20 + TEMPLATE_LENGTH + 6 + fileName.length + oa2Structure.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000b);
    set32bit(0, FILE_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(5, ATTR_LIST_LEVEL_OFFSET);   // we're specifying an OA2b or OA2c
    set16bit(0, SET_FLAGS_OFFSET);

    // Set the filename LL.
    set32bit(fileName.length + 6, FILE_NAME_LL_OFFSET);

    // Set the filename code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the filename characters.
    System.arraycopy(fileName, 0, data_, FILE_NAME_OFFSET, fileName.length);

    // Set the OA2 structure (includes the LLCP).
    int offset = FILE_NAME_OFFSET + fileName.length;
    System.arraycopy(oa2Structure, 0, data_, offset, oa2Structure.length);
  }

}




