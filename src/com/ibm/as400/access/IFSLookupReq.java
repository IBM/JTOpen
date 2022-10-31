///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSLookupReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
'Get file system information' request.
**/
class IFSLookupReq extends IFSDataStreamReq
{

  private static final int Parent_HANDLE_OFFSET = 22;
  private static final int Object_HANDLE_OFFSET = 26;
  private static final int CCSID_OFFSET = 30;
  private static final int File_Mode_OFFSET = 32;
  private static final int FILE_ATTR_LIST_LEVEL_OFFSET = 36;

  private static final int OPTIONAL_SECTION_OFFSET = 42;

  private static final int Object_NAME_LL_OFFSET = OPTIONAL_SECTION_OFFSET;
  private static final int Object_NAME_CP_OFFSET = OPTIONAL_SECTION_OFFSET + 4;
  private static final int Object_NAME_OFFSET    = OPTIONAL_SECTION_OFFSET + 6;
  private static final int TEMPLATE_LENGTH = 22;
  private static final int LLCP_LENGTH = 6;            // @A1a
  
  static final int OA_NONE = 0;
  static final int OA1     = 1;
  static final int OA2     = 2;
  static final int ASP_FLAG = 0x00100000;
  static final int OA12    = 12;  //@AC7 
  static final int OA3     = 3;   //@AC7 

  /**
  Construct a list attributes request.
  @param name the file name (may contain wildcard characters * and ?)
  **/

  IFSLookupReq(byte[] name,int fileNameCCSID)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + name.length); // @A1A
                       // Note: EA list fixed header is 8 bytes; repeating header is 10 bytes for each name-only EA structure.
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x001A);
    
    set32bit(0, Parent_HANDLE_OFFSET);
    
    set32bit(0, Object_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    
    set32bit(0x00, File_Mode_OFFSET);
    //set16bit(0x4, FILE_ATTR_LIST_LEVEL_OFFSET);
    

    
    // Set the 'filename' LL.
    set32bit(name.length + LLCP_LENGTH, Object_NAME_LL_OFFSET);

    // Set the 'filename' code point.
    set16bit(0x0002, Object_NAME_CP_OFFSET);

    // Set the 'filename' value.
    System.arraycopy(name, 0, data_, Object_NAME_OFFSET, name.length);

  }

  IFSLookupReq(byte[] name,int fileNameCCSID, int UserHandle)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + name.length); // @A1A
                       // Note: EA list fixed header is 8 bytes; repeating header is 10 bytes for each name-only EA structure.
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x001A);
    setCSInstance(UserHandle);
    
    set32bit(0, Parent_HANDLE_OFFSET);
    set32bit(0, Object_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    
    set32bit(0x00, File_Mode_OFFSET);
    //set16bit(0x4, FILE_ATTR_LIST_LEVEL_OFFSET);
    

    
    // Set the 'filename' LL.
    set32bit(name.length + LLCP_LENGTH, Object_NAME_LL_OFFSET);

    // Set the 'filename' code point.
    set16bit(0x0002, Object_NAME_CP_OFFSET);

    // Set the 'filename' value.
    System.arraycopy(name, 0, data_, Object_NAME_OFFSET, name.length);

  }
 
  IFSLookupReq(byte[] name,int fileNameCCSID, int UserHandle, int attrsType, int flags1, int flags2)
  {
	  super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + name.length+ (attrsType == OA1 ? 14 : 0) + (attrsType == OA12 ? 14 : 0)); //@AC7 
                       // Note: EA list fixed header is 8 bytes; repeating header is 10 bytes for each name-only EA structure.
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x001A);
    setCSInstance(UserHandle);
    
    set32bit(0, Parent_HANDLE_OFFSET);
    
    set32bit(0, Object_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    
    set32bit(0x00, File_Mode_OFFSET);
    set16bit(0x0, FILE_ATTR_LIST_LEVEL_OFFSET);
    
    // Set the 'filename' LL.
    set32bit(name.length + LLCP_LENGTH, Object_NAME_LL_OFFSET);

    // Set the 'filename' code point.
    set16bit(0x0002, Object_NAME_CP_OFFSET);

    // Set the 'filename' value.
    System.arraycopy(name, 0, data_, Object_NAME_OFFSET, name.length);
    
    int offset = Object_NAME_OFFSET + name.length; // Offset for next field.        @A1a

    switch (attrsType)
    {
      case OA1:  // return an OA1* structure
        set16bit((short)0x0002, FILE_ATTR_LIST_LEVEL_OFFSET); // get OA1, and use open instance of file handle
        // Set the 'Flags' LL.
        set32bit(LLCP_LENGTH + 4 + 4,  offset);  // LL/CP length, plus two 4-byte fields
        // Set the 'Flags' CP.
        set16bit(0x0010, offset+4);
        // Set the 'Flags' values.
        set32bit(flags1, offset+6);  // Flags(1)
        set32bit(flags2, offset+10);  // Flags(2)
        break;
      case OA2:
        set16bit((short)0x0004,FILE_ATTR_LIST_LEVEL_OFFSET);
        break;
      //@AC7 Start
      case OA12:
    	set16bit((short)0x0006,FILE_ATTR_LIST_LEVEL_OFFSET);
    	set32bit(LLCP_LENGTH + 4 + 4,  offset);  // LL/CP length, plus two 4-byte fields
        // Set the 'Flags' CP.
        set16bit(0x0010, offset+4);
        // Set the 'Flags' values.
        set32bit(flags1, offset+6);  // Flags(1)
        set32bit(flags2, offset+10);  // Flags(2)
        break;
      //@AC7 Start
     }

  }

}




