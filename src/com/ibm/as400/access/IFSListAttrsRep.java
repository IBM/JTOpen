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
  static final int FA_DIRECTORY = 0x10;

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

  // Note: The following offsets are valid only if the reply contains an OA2 structure.
  private static final int CODE_PAGE_OFFSET_INTO_OA2 = 126;

  ///private static final int TEMPLATE_LENGTH = 61;
  private static final int HEADER_LENGTH = 20;
  private static final int LLCP_LENGTH = 6;

/**
Construct a list file attributes reply.
**/
  IFSListAttrsRep()
  {
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
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
  int getCodePage()
  {
    return get16bit(HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET) +
                    LLCP_LENGTH + CODE_PAGE_OFFSET_INTO_OA2);
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
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8005;
  }
}




