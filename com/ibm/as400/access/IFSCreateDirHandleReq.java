///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCreateDirHandleRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2004-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;



/**
Create Working Directory Handle request.
**/
class IFSCreateDirHandleReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";

  private static final int CCSID_OFFSET = 22;
  private static final int WORKING_DIR_HANDLE_OFFSET = 24;
  private static final int FILE_NAME_LL_OFFSET = 28;
  private static final int FILE_NAME_CP_OFFSET = 32;
  private static final int FILE_NAME_OFFSET = 34;
  private static final int TEMPLATE_LENGTH = 8;

/**
Construct a Create Working Directory Handle reqeust.
@param name the directory name
@param fileNameCCSID file name CCSID
**/
  IFSCreateDirHandleReq(byte[] name,
                  int    fileNameCCSID)
  {
    super(20 + TEMPLATE_LENGTH + 6 + name.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0012);
    set16bit(fileNameCCSID, CCSID_OFFSET);
    set32bit(1, WORKING_DIR_HANDLE_OFFSET);

    // Set the LL.
    set32bit(name.length + 6, FILE_NAME_LL_OFFSET);

    // Set the code point.
    set16bit(0x0001, FILE_NAME_CP_OFFSET);

    // Set the file name characters.
    System.arraycopy(name, 0, data_, FILE_NAME_OFFSET, name.length);
  }

}




