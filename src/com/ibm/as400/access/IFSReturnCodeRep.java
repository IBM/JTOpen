///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSReturnCodeRep.java
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
Return code reply.
**/
class IFSReturnCodeRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final int FALSE                         = 0;
  static final int SUCCESS                       = 0;
  static final int TRUE                          = 1;
  static final int FILE_IN_USE                   = 1;
  static final int FILE_NOT_FOUND                = 2;
  static final int PATH_NOT_FOUND                = 3;
  static final int DUPLICATE_DIR_ENTRY_NAME      = 4;
  static final int ACCESS_DENIED_TO_DIR_ENTRY    = 5;
  static final int INVALID_HANDLE                = 6;
  static final int INVALID_DIR_ENTRY_NAME        = 7;
  static final int INVALID_ATTRIBUTE_NAME        = 8;
  static final int DIR_IS_NOT_EMPTY              = 9;
  static final int FILE_SUBSTREAM_IN_USE         = 10;
  static final int RESOURCE_LIMIT_EXCEEDED       = 11;
  static final int RESOURCE_NOT_AVAILABLE        = 12;
  static final int ACCESS_DENIED_TO_REQUEST      = 13;
  static final int DIRECTORY_ENTRY_DAMAGED       = 14;
  static final int INVALID_CONNECTION            = 15;
  static final int INVALID_REQUEST               = 16;
  static final int DATA_STREAM_SYNTAX_ERROR      = 17;
  static final int NO_MORE_FILES                 = 18;
  static final int PARM_NOT_SUPPORTED            = 19;
  static final int PARM_VALUE_NOT_SUPPORTED      = 20;
  static final int CANNOT_CONVERT_VALUE          = 21;
  static final int NO_MORE_DATA                  = 22;
  static final int REQUEST_NOT_SUPPORTED         = 23;
  static final int INVALID_USER                  = 24;
  static final int UNKNOWN_ERROR                 = 25;
  static final int SHARING_VIOLATION             = 32;
  static final int LOCK_VIOLATION                = 33;
  private static final int RETURN_CODE_OFFSET = 22;

/**
Construct a return code reply.
**/
  IFSReturnCodeRep()
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




