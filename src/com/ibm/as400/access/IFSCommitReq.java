///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSCommitReq.java
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
Commit changes request.
**/
class IFSCommitReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int FILE_HANDLE_OFFSET = 22;
  private static final int TEMPLATE_LENGTH = 6;

/**
Construct a commit changes request.
@param fileHandle the file handle returned from an open request
**/
  IFSCommitReq(int fileHandle)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0006);
    set32bit(fileHandle, FILE_HANDLE_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}





