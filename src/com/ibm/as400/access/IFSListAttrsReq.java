///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSListAttrsReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
'List file attributes' request.
**/
class IFSListAttrsReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

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

  private static final int FILE_HANDLE_OFFSET = 22;
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
  private static final int TEMPLATE_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;            // @A1a

  // @C1a - Added longFileSize parameter.
/**
Construct a 'list file attributes' request.
@param name the file name
@param fileNameCCSID file name CCSID
@param authority bit 0: on = client must have read authority,
                 bit 1: on = client must have write authority,
                 bit 2: on = client must have execute authority
@param maximumGetCount The maximum get count, or -1 to return all entries.
@param restartNameOrID The restart name or ID, or null to return all entries.
@param isRestartName  true: interpret restartNameOrID as a restart Name,
                      false: interpret restartNameOrID as a restart ID.
@param extendedAttrName The extended attribute name, or null to return
                        no extended attributes.
@param longFileSize true: return file size as 8-byte value (as optional field),
                    false: do not return 8-byte file-size field.
**/
  IFSListAttrsReq(byte[] name,
                  int    fileNameCCSID,
                  int    authority,
                  int    maximumGetCount,                                      // @D2A
                  byte[] restartNameOrID,                                      // @D2A @C3C
                  boolean isRestartName,                                       // @C3a
                  byte[] extendedAttrName,                                     // @A1a
                  boolean longFileSize,                                        // @C1a
                  int patternMatching)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + name.length
         + ((restartNameOrID != null) ? (LLCP_LENGTH + restartNameOrID.length) : 0)    // @D2A @C3C
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
        set16bit(0xffff, MAX_GET_COUNT_OFFSET);  // set to -1, "no maximum"
    else                                                                                // @D2A
        set16bit(maximumGetCount, MAX_GET_COUNT_OFFSET);                                // @D2A

    // Set the file attribute list level.
    if (longFileSize == true) {                                                         // @C1a
      set16bit(0x0101, FILE_ATTR_LIST_LEVEL_OFFSET);  // set the "return 8-byte file size" flag
    }
    else {
      set16bit(0x0001, FILE_ATTR_LIST_LEVEL_OFFSET);  // just set the "reserved" flag
    }

    set16bit(0, PATTERN_MATCHING_OFFSET);  // default is POSIX

    // Set the 'filename' LL.
    set32bit(name.length + LLCP_LENGTH, FILE_NAME_LL_OFFSET);

    // Set the 'filename' code point.
    set16bit(0x0002, FILE_NAME_CP_OFFSET);

    // Set the 'filename' value.
    System.arraycopy(name, 0, data_, FILE_NAME_OFFSET, name.length);

    int offset = FILE_NAME_OFFSET + name.length; // Offset for next field.        @A1a

    // Set the "restart name", if specified.      @D2A
    if (restartNameOrID != null)                        // @C3c
    {                                                   // @D2A
        int codePoint;                                                    // @C3a
        if (isRestartName) codePoint = 0x0007;  // it's a Restart Name    // @C3a
        else               codePoint = 0x000E;  // it's a Restart ID      // @C3a
        set32bit(restartNameOrID.length + LLCP_LENGTH, offset); // LL                 // @D2A @C3c
        set16bit(codePoint, offset + 4);                        // CP                 // @D2A @C3c
        System.arraycopy(restartNameOrID, 0, data_, offset + LLCP_LENGTH, restartNameOrID.length);         // @D2A @C3c
        // It would would be more efficient to use the ordinal values                    // @D2A
        // (which work outside of QSYS and QDLS).  I chose to keep it                    // @D2A
        // simple for now, but we should make it more sophisticated later.               // @D2A

        // Design note: As of 02/05/01, according to the File Server team:
        // "The vnode architecture allows a file system to use the cookie
        // (Restart Number) or a Restart Name to find the entry
        // that processing should start at.
        // QDLS and QSYS allow Restart Name, but /root (EPFS) does not."

        offset += LLCP_LENGTH + restartNameOrID.length;  // next field     @A1a @C3c
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

    setPatternMatching(patternMatching);
  }



/**
Construct a 'list file attributes' request.
@param handle the file handle
**/
  IFSListAttrsReq(int handle)
  {
    this(handle, OA_NONE, 0, 0);  // we don't need an OA* structure back in the reply
  }


  /**
   Construct a 'list file attributes' request.
   @param handle the file handle
   @param attrsType The type of attributes (OA1 or OA2)
   @param flags1 Bitmap for the Flags(1) field.  Ignored if attrsType != OA1.
   @param flags2 Bitmap for the Flags(2) field.  Ignored if attrsType != OA1.
   **/
  IFSListAttrsReq(int handle, int attrsType, int flags1, int flags2)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + (attrsType == OA1 ? 14 : 0));
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000A);
    set32bit(handle, FILE_HANDLE_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);
    set16bit(NO_AUTHORITY_REQUIRED, CHECK_AUTHORITY_OFFSET);
    set16bit(0xffff, MAX_GET_COUNT_OFFSET);
    set16bit(0, PATTERN_MATCHING_OFFSET);  // default is POSIX

    // Specify appropriate "Object attribute" flag value (OA1, OA2, or neither).
    switch (attrsType)
    {
      case OA1:  // return an OA1* structure
        set16bit((short)0x42, FILE_ATTR_LIST_LEVEL_OFFSET); // get OA1, and use open instance of file handle
        // Set the 'Flags' LL.
        set32bit(LLCP_LENGTH + 4 + 4,  OA1_FLAGS_LL_OFFSET);  // LL/CP length, plus two 4-byte fields
        // Set the 'Flags' CP.
        set16bit(0x0010, OA1_FLAGS_CP_OFFSET);
        // Set the 'Flags' values.
        set32bit(flags1, OA1_FLAGS_OFFSET1);  // Flags(1)
        set32bit(flags2, OA1_FLAGS_OFFSET2);  // Flags(2)
        break;
      case OA2:  // return an OA2* structure
        set16bit((short)0x44, FILE_ATTR_LIST_LEVEL_OFFSET); // get OA2, and use open instance of file handle
        break;
      default:  // do not use a file handle, and do not return an OA* structure
        set16bit((short)0x01, FILE_ATTR_LIST_LEVEL_OFFSET); // just set the required 'reserved' bit
    }

  }


  /**
   Sets the value of the "pattern matching" field.
   Valid values are "POSIX" (0), "POSIX-all" (1), and "OS/2" (2).
   **/
  final void setPatternMatching(int patternMatching)
  {
    // "POSIX" pattern matching: Using POSIX semantics, return all files that match the pattern and do not begin with a period unless the pattern begins with a period.  In that case, names beginning with a period will be returned.

    // "POSIX-all" pattern matching: Using POSIX semantics, return all files that match the pattern, including those that begin with a period.

    // "OS/2" pattern matching: Using "DOS" semantics, return all files that match the pattern.

    set16bit(patternMatching, PATTERN_MATCHING_OFFSET);
  }


  /**
   Sets whether directory information is to be returned in sorted order.
   @param sort If true: Return the directory information in sorted order.
   If false: Return the directory information in the order the file system provides.
   **/
  final void setSorted(boolean sort)
  {
    int attributeListLevel = get16bit(FILE_ATTR_LIST_LEVEL_OFFSET);
    // Bit 7 of the "File Attribute List Level" field is the "Directory Sort" flag.
    // (The right-most bit is bit 0.)
    if (sort) {  // Turn on bit 7.
      attributeListLevel = attributeListLevel | 0x0080;
    }
    else {  // Turn off bit 7.
      attributeListLevel = attributeListLevel & 0xFF7F;
    }
    set16bit(attributeListLevel, FILE_ATTR_LIST_LEVEL_OFFSET);
  }

}




