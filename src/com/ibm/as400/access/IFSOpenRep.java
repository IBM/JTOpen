///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSOpenRep.java
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
Open file reply.
**/
class IFSOpenRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int FILE_ID_OFFSET = 26;
  private static final int FILE_DATA_CCSID_OFFSET = 34;
  private static final int ACTION_TAKEN_OFFSET = 36;
  private static final int CREATE_DATE_OFFSET = 38;
  private static final int MODIFY_DATE_OFFSET = 46;
  private static final int ACCESS_DATE_OFFSET = 54;
  private static final int FILE_SIZE_OFFSET = 62;
  private static final int FIXED_ATTRS_OFFSET = 66;
  private static final int NEED_EXT_ATTRS_OFFSET = 70;
  private static final int NUM_EXT_ATTRS_OFFSET = 72;
  private static final int CHARS_EA_NAMES_OFFSET = 74;
  private static final int BYTES_EA_VALUES_OFFSET = 78;
  private static final int VERSION_OFFSET = 82;
  private static final int AMOUNT_ACCESSED_OFFSET = 86;
  private static final int ACCESS_HISTORY_OFFSET = 88;

/**
Get the date/time that the file was last accessed.
@return the date/time (measured as milliseconds since midnight January 1, 1970) of the last access
**/
  long getAccessDate()
  {
    return getDate(ACCESS_DATE_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
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
Get the file handle.
@return the file handle.
**/
  int getFileHandle()
  {
    return get32bit( FILE_HANDLE_OFFSET);
  }

/**
Get the file size.
@return the number of bytes in the file
**/
  int getFileSize()
  {
    return get32bit( FILE_SIZE_OFFSET);
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
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSOpenRep();
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8002;
  }
}




