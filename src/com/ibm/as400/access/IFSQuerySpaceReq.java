///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSQuerySpaceReq.java
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
Query available file system space request.
**/
class IFSQuerySpaceReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int WORKING_DIR_HANDLE_OFFSET = 22;
  private static final int TEMPLATE_LENGTH = 6;

/**
Construct a query file system space request.
@parm userAttributes if true the space attributes for the user are requested, otherwise the space attributes for the entire file system are requested
**/
  IFSQuerySpaceReq(boolean userAttributes)
  {
    super(20 + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0015);
    set32bit((userAttributes ? 0 : 1), WORKING_DIR_HANDLE_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}



