///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSListAttrsRep.java
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
List file attributes reply.
**/
class IFSListAttrsRep extends IFSDataStream
{
  static final int FILE = 1;
  static final int DIRECTORY = 2;
  static final int SYMBOLIC_LINK = 3;
  static final int AS400_OBJECT = 4;
  static final int DEVICE_FIFO = 5;
  static final int DEVICE_CHAR = 6;
  static final int DEVICE_BLOCK = 7;
  static final int SOCKET = 8;

  static final int FA_READONLY  = 0x01;                               //@D1a
  static final int FA_HIDDEN    = 0x02;                               //@D1a
  static final int FA_SYSTEM    = 0x04;                               //@D1a
  static final int FA_DIRECTORY = 0x10;
  static final int FA_ARCHIVE   = 0x20;                               //@D1a

  private static final int TEMPLATE_LENGTH_OFFSET = 16;

  // Note: The following offsets are valid only if template length >= 61.
  private static final int CREATE_DATE_OFFSET = 22;
  private static final int MODIFY_DATE_OFFSET = 30;
  private static final int ACCESS_DATE_OFFSET = 38;
  private static final int FILE_SIZE_OFFSET = 46;
  private static final int FIXED_ATTRS_OFFSET = 50;
  private static final int OBJECT_TYPE_OFFSET = 54;
  private static final int NUM_EXT_ATTRS_OFFSET = 56;
  private static final int BYTES_EA_NAMES_OFFSET = 58;
  private static final int BYTES_EA_VALUES_OFFSET = 62;
  private static final int VERSION_NUMBER_OFFSET = 66;
  private static final int AMOUNT_ACCESSED_OFFSET = 70;
  private static final int ACCESS_HISTORY_OFFSET = 72;
  private static final int NAME_CCSID_OFFSET = 73;
  private static final int CHECKOUT_CCSID_OFFSET = 75;
  private static final int RESTART_ID_OFFSET = 77;
  // Note: Offets of fields beyond this point depend on the server datastream level (DSL).
  private static final int OPTIONAL_SECTION_OFFSET_OLD = 81; // if DSL < 8
  private static final int OPTIONAL_SECTION_OFFSET     = 113; // if DSL >= 8
  private static final int LARGE_FILE_SIZE_OFFSET = 81;      // if DSL >= 8
  private static final int SYMBOLIC_LINK_OFFSET = 91;        // if DSL >= 8

  // The following offset is valid only if the reply contains an OA2 structure.
  private static final int CODE_PAGE_OFFSET_INTO_OA2  = 126;

  // The following offset is valid only if the reply contains an OA2a structure.   @A2a
  private static final int CODE_PAGE_OFFSET_INTO_OA2a = 142;

  // Note: Beginning with OA2b, we no longer care about the codepage field.  Instead, we get the "CCSID of the object" field.

  // The following offset is valid only if the reply contains an OA2b structure.   @A2a
  private static final int CCSID_OFFSET_INTO_OA2b = 134;

  // The following offset is valid only if the reply contains an OA2c structure.
  private static final int CCSID_OFFSET_INTO_OA2c = 134;

  // Offset of the "owner user ID" field in the OA2* structures.    @B7a
  private static final int OWNER_OFFSET_INTO_OA2  = 64;

  private static final int HEADER_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;

  //private int serverDatastreamLevel_; // @A1A @B6d
  //private IFSFileDescriptorImplRemote fd_; // @B6a

  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.     @A3a
  private static final boolean DEBUG = false;

/**
Construct a list file attributes reply.
**/
  IFSListAttrsRep()
  {
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSListAttrsRep();
  }

/**
Get the date/time that the file was last accessed.
@return the date/time (measured as milliseconds since midnight January 1, 1970) of the last access
**/
  long getAccessDate()
  {
    return getDate(ACCESS_DATE_OFFSET);
  }

/**
Get the code page value for the IFS file on the AS/400.
@return the code page value for the IFS file on the AS/400
**/
/* @B6d
  private int getCodePage()
  {
    int codePageOffset;
    int 
    if (fd_.serverDatastreamLevel_ == 0xf4f4)          //@A2a @B6c
      codePageOffset = CODE_PAGE_OFFSET_INTO_OA2a;     //@A2a
    else
      codePageOffset = CODE_PAGE_OFFSET_INTO_OA2;

    return get16bit(HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                    LLCP_LENGTH + codePageOffset);
  }
*/

/**
Get the CCSID value for the IFS file on the AS/400.
@return the CCSID value for the IFS file on the AS/400
**/
  int getCCSID(int datastreamLevel)  // @A1A
  {
    // Note: Only if the server is reporting Datastream Level 2 (or later) will the reply have a CCSID field.
    // If prior to Level 2, we must make do with the codepage value.

    /* @B6a @B9c

     Note: To figure out the format of the returned information, we need to
     consider both the requested and reported Datastream Levels:

     DSL requested     DSL reported    OA format sent
     by client         by server       by server
     _____________     ____________    _______________

     0                 any             OA2

     2                 0               OA2

     2                 F4F4            OA2a

     2                 2               OA2b

     2                 3               OA2b

     8                 0               OA2

     8                 F4F4            OA2a

     8                 2               OA2b

     8                 3               OA2b

     8                 8               OA2c

     Note: Since we only ever request level 0, 2, or 8,
           the server will never report level 1.
     */
    int offset_into_OA;  // offset into OA* structure for CCSID or codepage field
    switch (datastreamLevel)
    {
      case 0:
        offset_into_OA = CODE_PAGE_OFFSET_INTO_OA2;
        break;
      case 0xF4F4:
        offset_into_OA = CODE_PAGE_OFFSET_INTO_OA2a;
        break;
      case 2:
        offset_into_OA = CCSID_OFFSET_INTO_OA2b;
        break;
      default:
        offset_into_OA = CCSID_OFFSET_INTO_OA2c;
        break;
    }
    return get16bit(HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                    LLCP_LENGTH + offset_into_OA);
  }

/**
Get the date/time that the file was created.
@return the date/time (measured as milliseconds since midnight January 1, 1970) of creation
**/
  long getCreationDate()
  {
    return getDate(CREATE_DATE_OFFSET);
  }

//@A3a
/**
Get the extended attribute value.
Returns null if the reply contains no extended attribute.
@return extended attribute value
**/
  byte[] getExtendedAttributeValue(int datastreamLevel)
  {
    // Determine length and starting offset of the EA.

    // The field preceding the "Extended Attributes" field
    // is the "File Name" field.
    int file_name_LL_offset;
    if (datastreamLevel < 8 || datastreamLevel == 0xF4F4) {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET_OLD; }
    else {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET; }
    int fileNameFieldLength = get32bit(file_name_LL_offset);

    // Verify that we got at least one EA back.  A "full EA" is at least 20 bytes long.
    int eaOffset = file_name_LL_offset + fileNameFieldLength;
    byte[] eaVal = null;
    if (eaOffset+20 > data_.length)
    {
      if (DEBUG) System.out.println("DEBUG eaOffset+20 > data_.length: " + (eaOffset+20));
    }
    else
    {
      int eaListLength = get32bit(eaOffset);
      int eaCodePoint = get16bit(eaOffset+4);
      if (eaCodePoint != 0x0009)
      {
        if (DEBUG) System.out.println("DEBUG Code point is not x0009: " + eaCodePoint);
      }
      else
      {
        int eaCount = get16bit(eaOffset+6);
        int eaCcsid = get16bit(eaOffset+8);
        int eaNameLL= get16bit(eaOffset+10);
           // Note: eaNameLL does *not* include length of the LL field itself.
        int eaFlags = get16bit(eaOffset+12);
        int eaValLL = get32bit(eaOffset+14);
           // Note: eaValLL includes the 4 "mystery bytes" that precede the name.
        byte[] eaName = new byte[eaNameLL];
        System.arraycopy(data_, eaOffset+18, eaName, 0, eaNameLL);
        if (eaValLL <= 4)
        {
          if (DEBUG) System.out.println("DEBUG eaValLL<=4: " + eaValLL);
        }
        else
        {
          eaVal = new byte[eaValLL-4];
          System.arraycopy(data_, eaOffset+18+eaNameLL+4, eaVal, 0, eaValLL-4);
        }
      }
    }

    return eaVal;
  }

/**
Get the fixed attributes.
@return fixed attributes
**/
  int getFixedAttributes()
  {
    return(get32bit(FIXED_ATTRS_OFFSET));
  }

/**
Get the date/time that the file was last modified.
@return the date/time (measured as milliseconds since midnight January 1, 1970) of the last modification
**/
  long getModificationDate()
  {
    return getDate(MODIFY_DATE_OFFSET);
  }

/**
Get the file name.
@return the file name
**/
  byte[] getName(int datastreamLevel)
  {
    int file_name_LL_offset;
    if (datastreamLevel < 8 || datastreamLevel == 0xF4F4) {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET_OLD; }
    else {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET; }
    int length = get32bit( file_name_LL_offset) - 6;
    if (DEBUG && length < 0) {
      Trace.log(Trace.ERROR, "Error getting file name: Value at name-length offset is too small.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    byte[] name = new byte[length];
    System.arraycopy(data_, 6+file_name_LL_offset, name, 0, length);
    // Note: Actual filename starts after the 6-byte LL CP, hence the "6+".

    return name;
  }

/**
Determine the object type (file, directory, etc.)
@return the object type
**/
  int getObjectType()
  {
    return get16bit( OBJECT_TYPE_OFFSET);
  }

// @B7a
/**
Get the owner's "user ID" number for the IFS file on the AS/400.
@return the owner's user ID number for the IFS file on the AS/400
**/
  long getOwnerUID()  // @C0c
  {
    int fieldOffset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                 LLCP_LENGTH + OWNER_OFFSET_INTO_OA2;
    return (long)get32bit(fieldOffset) & 0x0FFFFFFFFL;  // @C0c
  }

// @C3a
/**
Get the restart identifier.
@return the restart identifier
**/
  byte[] getRestartID()
  {
    byte[] restartID = new byte[4];
    System.arraycopy(data_, RESTART_ID_OFFSET, restartID, 0, restartID.length);

    return restartID;
  }

/**
Determine the file size (in bytes).
@return the file size
**/
  long getSize(int datastreamLevel)                             // @B8c
  {
    // Datastream level 8 added a "Large File Size" field.
    if (datastreamLevel < 8 || datastreamLevel == 0xF4F4) {
      // We need to suppress sign-extension, just in case the leftmost bit is on.
      int size = get32bit( FILE_SIZE_OFFSET);     // @B8c
      return ((long)size) & 0xffffffffL;          // @B8c
    }
    else {
      return (get64bit( LARGE_FILE_SIZE_OFFSET ));
    }
  }

//@C1a
/**
Get the length of the file (8 bytes).
@return length of the file
**/
  long getSize8Bytes(int datastreamLevel)
  {
    long fileSize = 0L;

    // Determine starting offset of the Optional field "8-byte file size".

    // The field preceding the "8-byte file size" field is the "File Name" field.
    int file_name_LL_offset;
    if (datastreamLevel < 8 || datastreamLevel == 0xF4F4) {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET_OLD; }
    else {
      file_name_LL_offset = OPTIONAL_SECTION_OFFSET; }
    int fileNameFieldLength = get32bit(file_name_LL_offset);

    // Verify that we got the optional "file size" field back.  The field's length should be 8 bytes.
    int fsOffset = file_name_LL_offset + fileNameFieldLength;
    byte[] fsVal = null;
    if (fsOffset+8 > data_.length)
    {
      if (DEBUG) System.out.println("DEBUG fsOffset+8 > data_.length: " + (fsOffset+8));
      Trace.log(Trace.ERROR, "Error getting 8-byte file size: Optional field was not returned.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    else
    {
      int fsCodePoint = get16bit(fsOffset+4);
      if (fsCodePoint != 0x0014)
      {
        Trace.log(Trace.ERROR, "Error getting 8-byte file size: Unexpected optional field code point value: " + fsCodePoint);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
      else
      {
        fileSize = get64bit(fsOffset+6);
      }
    }

    return fileSize;
  }

/**
Determine whether the object is a symbolic link.
@return true if object is a symbolic link;
        false if not a symbolic link (or if could not determine).
**/
  boolean isSymbolicLink(int datastreamLevel)
  {
    boolean result = false;
    // Note: The symlink field was added to the reply datastream in DSL 8.
    if (datastreamLevel < 8 || datastreamLevel == 0xF4F4) {
      Trace.log(Trace.WARNING, "Could not determine whether file is a symbolic link.");
    }
    else {
      byte fieldValue = data_[SYMBOLIC_LINK_OFFSET];
      switch (fieldValue)
      {
        case 0:
          result = false;
          break;
        case 1:
          result = true;
          break;
        default:
          Trace.log(Trace.ERROR, "Internal error, unexpected value in symbolic link field: ", fieldValue);
          break;
      }
    }
    return result;
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8005;
  }
}




