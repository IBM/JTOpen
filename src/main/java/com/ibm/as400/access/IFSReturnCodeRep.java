///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSReturnCodeRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
Return code reply.
**/
class IFSReturnCodeRep extends IFSDataStream
{

  static final int FALSE                         = 0;
  static final int SUCCESS                       = 0;
  static final int TRUE                          = 1;

  static final int FILE_IN_USE                   = /*  1 */ ExtendedIOException.FILE_IN_USE;
  static final int FILE_NOT_FOUND                = /*  2 */ ExtendedIOException.FILE_NOT_FOUND;
  static final int PATH_NOT_FOUND                = /*  3 */ ExtendedIOException.PATH_NOT_FOUND;
  static final int DUPLICATE_DIR_ENTRY_NAME      = /*  4 */ ExtendedIOException.DIR_ENTRY_EXISTS;
  static final int ACCESS_DENIED_TO_DIR_ENTRY    = /*  5 */ ExtendedIOException.ACCESS_DENIED;
  static final int INVALID_HANDLE                = /*  6 */ ExtendedIOException.INVALID_HANDLE;
  static final int INVALID_DIR_ENTRY_NAME        = /*  7 */ ExtendedIOException.INVALID_DIR_ENTRY_NAME;
  static final int INVALID_ATTRIBUTE_NAME        = /*  8 */ ExtendedIOException.INVALID_ATTRIBUTE_NAME;
  static final int DIR_IS_NOT_EMPTY              = /*  9 */ ExtendedIOException.DIR_NOT_EMPTY;
  static final int FILE_SUBSTREAM_IN_USE         = /* 10 */ ExtendedIOException.FILE_SUBSTREAM_IN_USE;
  static final int RESOURCE_LIMIT_EXCEEDED       = /* 11 */ ExtendedIOException.RESOURCE_LIMIT_EXCEEDED;
  static final int RESOURCE_NOT_AVAILABLE        = /* 12 */ ExtendedIOException.RESOURCE_NOT_AVAILABLE;
  static final int ACCESS_DENIED_TO_REQUEST      = /* 13 */ ExtendedIOException.REQUEST_DENIED;
  static final int DIRECTORY_ENTRY_DAMAGED       = /* 14 */ ExtendedIOException.DIR_ENTRY_DAMAGED;
  static final int INVALID_CONNECTION            = /* 15 */ ExtendedIOException.INVALID_CONNECTION;
  static final int INVALID_REQUEST               = /* 16 */ ExtendedIOException.INVALID_REQUEST;
  static final int DATA_STREAM_SYNTAX_ERROR      = /* 17 */ ExtendedIOException.DATA_STREAM_SYNTAX_ERROR;
  static final int NO_MORE_FILES                 = /* 18 */ ExtendedIOException.NO_MORE_FILES;
  static final int PARM_NOT_SUPPORTED            = /* 19 */ ExtendedIOException.PARM_NOT_SUPPORTED;
  static final int PARM_VALUE_NOT_SUPPORTED      = /* 20 */ ExtendedIOException.PARM_VALUE_NOT_SUPPORTED;
  static final int CANNOT_CONVERT_VALUE          = /* 21 */ ExtendedIOException.CANNOT_CONVERT_VALUE;
  static final int NO_MORE_DATA                  = /* 22 */ ExtendedIOException.END_OF_FILE;
  static final int REQUEST_NOT_SUPPORTED         = /* 23 */ ExtendedIOException.REQUEST_NOT_SUPPORTED;
  static final int INVALID_USER                  = /* 24 */ ExtendedIOException.INVALID_USER;
  static final int UNKNOWN_ERROR                 = /* 25 */ ExtendedIOException.UNKNOWN_ERROR;
  static final int SHARING_VIOLATION             = /* 32 */ ExtendedIOException.SHARING_VIOLATION;
  static final int LOCK_VIOLATION                = /* 33 */ ExtendedIOException.LOCK_VIOLATION;
  static final int STALE_HANDLE                  = /* 34 */ ExtendedIOException.STALE_HANDLE;

  private static final int RETURN_CODE_OFFSET = 22;

/**
Construct a return code reply.
**/
  IFSReturnCodeRep()
  {
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSReturnCodeRep();
  }

/**
Get the return code.
@return the return code
**/
  int getReturnCode()
  {
    return get16bit( RETURN_CODE_OFFSET);
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8001;
  }
}




