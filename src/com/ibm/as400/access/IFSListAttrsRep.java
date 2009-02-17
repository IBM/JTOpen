///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSListAttrsRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


/**
List file attributes reply.
**/
class IFSListAttrsRep extends IFSDataStream
{
  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.     @A3a
  private static final boolean DEBUG = false;

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

  static final int OA_NONE = IFSListAttrsReq.OA_NONE;
  static final int OA1     = IFSListAttrsReq.OA1;
  static final int OA2     = IFSListAttrsReq.OA2;

  private static final int TEMPLATE_LENGTH_OFFSET = 16;

  private static final int HEADER_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;

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
  private static final int NAME_CCSID_OFFSET = 73;  // CCSID of the file/path name
  private static final int CHECKOUT_CCSID_OFFSET = 75;
  private static final int RESTART_ID_OFFSET = 77;

  // Additional fields if datastreamLevel >= 8:
  private static final int LARGE_FILE_SIZE_OFFSET = 81;
  private static final int SYMBOLIC_LINK_OFFSET = 91;


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
   Get the CCSID value for the IFS file on the server.
   @return the CCSID value for the IFS file on the server
   **/
  int getCCSID(int datastreamLevel)  // @A1A
  {
    // Get the 'CCSID of the object' field from the OA2 structure in the reply.
    return getObjAttrs2().getCCSID(datastreamLevel);
  }

/**
Get the date/time that the file was created.
@return the date/time (measured as milliseconds since midnight January 1, 1970) of creation
**/
  long getCreationDate()
  {
    return getDate(CREATE_DATE_OFFSET);
  }


/**
Get the extended attribute values, as a hashtable.
@return The extended attribute values.
**/
  Hashtable getExtendedAttributeValues()
  {
    Hashtable results = new Hashtable();

    // The offset to the start of the "optional/variable section" depends on the datastream level.
    int optionalSectionOffset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET);

    // Step through the optional fields, looking for the "EA list" field (code point 0x0009).

    int curLL_offset = optionalSectionOffset;
    int curLL = get32bit(curLL_offset);   // list length
    int curCP = get16bit(curLL_offset+4); // code point
    int eaListOffset;  // offset to start of Extended Attr list
    while (curCP != 0x0009 && (curLL_offset+curLL+6 <= data_.length))
    {
      curLL_offset += curLL;
      curLL = get32bit(curLL_offset);
      curCP = get16bit(curLL_offset+4);
    }

    if (curCP == 0x0009)
    {
      // We found the start of the Extended Attributes list.
      eaListOffset = curLL_offset;  // offset to "EA List Length" field
    }
    else
    {
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "No Extended Attributes were returned.");
      return results;  // empty hashtable
    }      

    byte[] eaVal = null;
    int eaCount = get16bit(eaListOffset+6);  // number of EA structures returned
    if (DEBUG) System.out.println("DEBUG Number of EA structures returned: " + eaCount);

    // Advance the offset, to point to the start of first repeating EA struct.
    int offset = eaListOffset+8;

    for (int i=0; i<eaCount; i++)
    {
      int eaCcsid = get16bit(offset);      // The 2-byte CCSID for the EA name.
      int eaNameLL= get16bit(offset+2);    // The 2-byte length of the EA name.
      // Note: eaNameLL does *not* include length of the LL field itself.
      //int eaFlags = get16bit(offset+4);  // The flags for the EA.
      int eaValLL = get32bit(offset+6);    // The 4-byte length of the EA value.
      // Note: eaValLL includes the 4 "mystery bytes" that precede the name.
      byte[] eaName = new byte[eaNameLL];  // The EA name.
      System.arraycopy(data_, offset+10, eaName, 0, eaNameLL);
      if (eaValLL <= 4)
      {
        if (DEBUG) System.out.println("DEBUG Warning: eaValLL<=4: " + eaValLL);
      }
      else
      {
        eaVal = new byte[eaValLL-4];  // omit the 4 leading mystery bytes
        System.arraycopy(data_, offset+10+eaNameLL+4, eaVal, 0, eaValLL-4);
        try
        {
          String eaNameString = CharConverter.byteArrayToString(eaCcsid, eaName);
          results.put(eaNameString, eaVal);
        }
        catch (java.io.UnsupportedEncodingException e) { Trace.log(Trace.ERROR, e); }
      }
      // Advance the offset, to point to the start of next EA struct.
      offset += (10 + eaNameLL + eaValLL);
    }

    return results;
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
    // Assume that the "File Name" field is at the beginning of the Optional Section.
    int file_name_LL_offset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET);

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
   Returns the first OA1* structure (minus the LLCP) returned in the reply.
   @parm type Type of attributes (OA1 or OA2).
   @exception InternalErrorException If the reply contains no OA1* structure.
   **/
  IFSObjAttrs1 getObjAttrs1()
  {
    return new IFSObjAttrs1(getObjAttrBytes(OA1));
  }


  /**
   Returns the first OA2* structure (minus the LLCP) returned in the reply.
   @parm type Type of attributes (OA1 or OA2).
   @exception InternalErrorException If the reply contains no OA2* structure.
   **/
  IFSObjAttrs2 getObjAttrs2()
  {
    return new IFSObjAttrs2(getObjAttrBytes(OA2));
  }


  /**
   Returns the first OA* structure of the specified type (minus the LLCP) returned in the reply.
   @parm type Type of attributes (OA1 or OA2).
   @exception InternalErrorException If the reply contains no OA* structure of the specified type.
   **/
  private final byte[] getObjAttrBytes(int type)
  {
    if (type == OA_NONE) return null;

    // Find the first OA structure in the reply's "Optional/Variable Section".
    int offset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET);
    int oaCodePoint = (type == OA1 ? 0x0010 : 0x000F); // OA1: CP == 0x0010; OA2: CP == 0x000F
    byte[] buf = null;
    while (buf == null && offset < data_.length)
    {
      // Look for an LLCP with a CP value that specifies an OA* structure.
      int length = get32bit(offset);           // Get the LL value.
      short codePoint = (short)get16bit(offset + 4);  // Get the CP value.
      if (codePoint == oaCodePoint) {  // We found an OA* of the desired type.
        int OAlength = length - LLCP_LENGTH;  // Exclude the LLCP.
        buf = new byte[OAlength];
        System.arraycopy(data_, offset + LLCP_LENGTH, buf, 0, OAlength);
      }
      else {  // not what we're looking for, so keep looking
        offset += length;  // skip to next LLCP
      }
    }

    if (buf == null) {
      Trace.log(Trace.ERROR, "The reply does not contain an OA"+type+ " structure.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    return buf;
  }


/**
Determine the object type (file, directory, etc.)
@return the object type
**/
  int getObjectType()
  {
    return get16bit(OBJECT_TYPE_OFFSET);
  }


  /**
   Get the value of the file's "Owner Name" attribute.
   **/
  String getOwnerName(int systemCcsid) throws UnsupportedEncodingException
  {
    // Assume that this reply has an "Optional/Variable" section, and that it contains an "Object Attribute 1" structure.
    return getObjAttrs1().getOwnerName(systemCcsid);
  }


// @B7a
/**
Get the owner's "user ID" number for the IFS file on the server.
@return the owner's user ID number for the IFS file on the server
**/
  long getOwnerUID()  // @C0c
  {
    // Get the 'Owner user ID' field from the OA2 structure in the reply.
    return getObjAttrs2().getOwnerUID();
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
  long getSize8Bytes(/*int datastreamLevel*/)
  {
    long fileSize = 0L;

    int optionalSectionOffset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET);

    // Step through the optional fields, looking for the "8-byte file size" field (code point 0x0014).
    int curLL_offset = optionalSectionOffset;
    int curLL = get32bit(curLL_offset);
    int curCP = get16bit(curLL_offset+4);
    while (curCP != 0x0014 && (curLL_offset+curLL+6 <= data_.length))
    {
      curLL_offset += curLL;
      curLL = get32bit(curLL_offset);
      curCP = get16bit(curLL_offset+4);
    }
    if (curCP == 0x0014)
    {
      // We found the 8-byte file size optional attribute that we requested.
      fileSize = get64bit(curLL_offset+6);
    }
    else
    {
      // Didn't find it. The server goofed up and didn't send it to us.
        Trace.log(Trace.ERROR, "Error getting 8-byte file size: Optional field was not returned.");
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
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




