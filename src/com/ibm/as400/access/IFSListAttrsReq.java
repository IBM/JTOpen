///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

  private static final int HEADER_LENGTH = 20;
  private static final int TEMPLATE_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;            // @A1a

  // @C1a - Added longFileSize parameter.
/**
Construct a list file attributes request.
@param name the file name
@param fileNameCCSID file name CCSID
@param authority bit 0: on = client must have read authority,
                 bit 1: on = client must have write authority,
                 bit 2: on = client must have execute authority
@param maximumGetCount The maximum get count, or -1 to return all entries.
@param restartName The restart name, or null to return all entries.
@param extendedAttrName The extended attribute name, or null to return
                        no extended attributes.
@param longFileSize true: return file size as 8-byte value (as optional field),
                    false: do not return 8-byte file-size field.
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID,
                  int    authority,
                  int    maximumGetCount,                                      // @D2A
                  byte[] restartName,                                          // @D2A
                  byte[] extendedAttrName,                                     // @A1a
                  boolean longFileSize)                                        // @C1a
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + name.length
         + ((restartName != null) ? (LLCP_LENGTH + restartName.length) : 0)    // @D2A
         + ((extendedAttrName != null) ? (18 + extendedAttrName.length) : 0)); // @A1A
                       // Note: 18 is length of fixed "header" of name-only EA structure.
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000A);
    set32bit(0, FILE_HANDLE_OFFSET);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(authority, CHECK_AUTHORITY_OFFSET);
    if (maximumGetCount <= 0)                                                           // @D2A
        set16bit(0xffff, MAX_GET_COUNT_OFFSET);
    else                                                                                // @D2A
        set16bit(maximumGetCount, MAX_GET_COUNT_OFFSET);                                // @D2A
    if (longFileSize == true) {                                                         // @C1a
      set16bit(0x0101, FILE_ATTR_LIST_LEVEL_OFFSET);
    }
    else {
      set16bit(0x0001, FILE_ATTR_LIST_LEVEL_OFFSET);                                    // @C1c
    }
    set16bit(0, PATTERN_MATCHING_OFFSET);

    // Set the LL.
    set32bit(name.length + LLCP_LENGTH, FILE_NAME_LL_OFFSET);

    // Set the filename code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the filename characters.
    System.arraycopy(name, 0, data_, FILE_NAME_OFFSET, name.length);

    int offset = FILE_NAME_OFFSET + name.length; // Offset for next field.        @A1a

    // Set the "restart name", if specified.      @D2A
    if (restartName != null)
    {                                                   // @D2A
        set32bit(restartName.length + LLCP_LENGTH, offset); // LL                            // @D2A
        set16bit(0x0007, offset + 4);                       // CP                            // @D2A
        System.arraycopy(restartName, 0, data_, offset + LLCP_LENGTH, restartName.length);         // @D2A
        // It would would be more efficient to use the ordinal values                    // @D2A
        // (which work outside of QSYS and QDLS).  I chose to keep it                    // @D2A
        // simple for now, but we should make it more sophisticated later.               // @D2A

        // Design note: As of 02/05/01, according to the File Server team:
        // "The vnode architecture allows a file system to use the cookie
        // (Restart Number) or a Restart Name to find the entry
        // that processing should start at.
        // QDLS and QSYS allow Restart Name, but /root (EPFS) does not."

        offset += LLCP_LENGTH + restartName.length;  // next field     @A1a
    }                                                                                    // @D2A

    // Set the "extended attribute name", if specified.             @A1a
    if (extendedAttrName != null)
    {
        set32bit(18 + extendedAttrName.length, offset+0); // EA name list length
        set16bit(0x0008, offset+4);         // EA name list code point
        set16bit(0x0001, offset+6);         // EA count
        set16bit(fileNameCCSID, offset+8);  // ccsid for EA name
        set16bit(extendedAttrName.length, offset+10); // length of EA name
        set16bit(0x0000, offset+12);        // flags for the EA
        set32bit(0x0000, offset+14);        // length of the EA value
        System.arraycopy(extendedAttrName, 0, data_, offset + 18,
                         extendedAttrName.length);         // @D2A
    }

  }

  // @D2A
/**
Construct a list file attributes request.
@param name the file name
@param fileNameCCSID file name CCSID
@param authority bit 0: on = client must have read authority,
                 bit 1: on = client must have write authority,
                 bit 2: on = client must have execute authority
@param maximumGetCount The maximum get count, or -1 to return all entries.
@param restartName The restart name, or null to return all entries.
@param extendedAttrName The extended attribute name, or null to return
                        no extended attributes.
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID,
                  int    authority,
                  int    maximumGetCount,                                      // @D2A
                  byte[] restartName,                                          // @D2A
                  byte[] extendedAttrName)                                     // @A1a
  {
    this(name, fileNameCCSID, authority, maximumGetCount, restartName, extendedAttrName, false); // @C1c
  }

/**
Construct a list file attributes request.
@param name the file name
@param fileNameCCSID file name CCSID
@param authority bit 0: on = client must have read authority,
                 bit 1: on = client must have write authority,
                 bit 2: on = client must have execute authority
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID,
                  int    authority)
  {
      this(name, fileNameCCSID, authority, -1, null, null);    // @D2C @A1c
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
    this(handle, (short) 1);
  }

/**
Construct a list file attributes request.
@param handle the file handle
@param attributeListLevel the file attribute list level
**/
  IFSListAttrsReq(int handle, short attributeListLevel)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH);
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




