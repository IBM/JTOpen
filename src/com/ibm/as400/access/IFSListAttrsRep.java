///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSListAttrsRep.java
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
List file attributes reply.
**/
class IFSListAttrsRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
  private static final int FILE_NAME_LL_OFFSET = 81;
  private static final int FILE_NAME_CP_OFFSET = 85;
  private static final int FILE_NAME_OFFSET = 87;

  // The following offset is valid only if the reply contains an OA2 structure.
  private static final int CODE_PAGE_OFFSET_INTO_OA2  = 126;

  // The following offset is valid only if the reply contains an OA2a structure.   @A2a
  private static final int CODE_PAGE_OFFSET_INTO_OA2a = 142;

  // The following offset is valid only if the reply contains an OA2b structure.   @A2a
  private static final int CCSID_OFFSET_INTO_OA2b = 134;

  private static final int HEADER_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;

  //private int serverDatastreamLevel_; // @A1A @B6d
  private IFSFileDescriptorImplRemote fd_; // @B6a

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
  int getCCSID()  // @A1A
  {
    if (DEBUG) System.out.println("DEBUG IFSListAttrsRep.getCCSID(): " +
                                  "requestedDatastreamLevel_ = " + fd_.requestedDatastreamLevel_ +
                                  ", serverDatastreamLevel_ = " + fd_.serverDatastreamLevel_);
    // Note: Only if the server is reporting Datastream Level 2 will have a CCSID field.
    // If other than Level 2, we must make do with the codepage value.

    /* @B6a

     Note: To figure out the format of the returned information, we need to
     consider both the requested and reported Datastream Levels:

     DSL requested     DSL reported    OA format sent
     by client         by server       by server
     _____________     ____________    _______________

     0                 any             OA2

     2                 0               OA2

     2                 F4F4            OA2a

     2                 2               OA2b

     Note: Since we only ever request level 0 or 2,
           the server will never report level 1.
     */
    int offset_into_OA;  // offset into OA* structure for CCSID or codepage field
    switch (fd_.requestedDatastreamLevel_)     // @B6a
    {
      case 0:
        offset_into_OA = CODE_PAGE_OFFSET_INTO_OA2;
        break;
      case 1: case 2:
        switch (fd_.serverDatastreamLevel_)
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
            Trace.log(Trace.ERROR, "Unexpected server datastream level: " +
                      fd_.serverDatastreamLevel_);
            throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }
        break;
      default:
        Trace.log(Trace.ERROR, "Unexpected requested datastream level: " +
                  fd_.requestedDatastreamLevel_);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    return get16bit(HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                    LLCP_LENGTH + offset_into_OA);

/* @B6d
    if (fd_.serverDatastreamLevel_ == 2)                                  // @B6c
      return get16bit(HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                      LLCP_LENGTH + CCSID_OFFSET_INTO_OA2b);
    else {
      // Interpret the code page as a CCSID.  For now just assume that the
      // code page and CCSID are the same.
      return getCodePage();
    }
*/
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
Returns null the the reply contains no extended attribute.
@return extended attribute value
**/
  byte[] getExtendedAttributeValue()
  {
    // Determine length and starting offset of the EA.

    // The field preceding the "Extended Attributes" field
    // is the "File Name" field.
    int fileNameFieldLength = get32bit(FILE_NAME_LL_OFFSET);

    // Verify that we got at least one EA back.  A "full EA" is at least 20 bytes long.
    int eaOffset = FILE_NAME_LL_OFFSET + fileNameFieldLength;
    byte[] eaVal = null;
    if (eaOffset+20 > data_.length)
    {
      if (DEBUG) System.out.println("DEBUG eaOffset+20 > data_.length: " + eaOffset+20);
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
  byte[] getName()
  {
    int length = get32bit( FILE_NAME_LL_OFFSET) - 6;

    byte[] name = new byte[length];
    System.arraycopy(data_, FILE_NAME_OFFSET, name, 0, length);

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

/**
Determine the file size (in bytes).
@return the file size
**/
  int getSize()
  {
    return get32bit( FILE_SIZE_OFFSET);
  }

/**
Set the server datastream level.  This enables us to correctly parse the reply.
@param datastreamLevel the server datastream level
**/
/* B6d
  void setServerDataStreamLevel(int datastreamLevel)
  {
    serverDatastreamLevel_ = dataStreamLevel;
  }
*/

/**
Set the file descriptor.  We need info from the descriptor to correctly parse the reply.
@param fd the file descriptor.
**/
  void setFD(IFSFileDescriptorImplRemote fd)
  {
    fd_ = fd;
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




