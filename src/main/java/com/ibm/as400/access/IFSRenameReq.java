///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSRenameReq.java
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
Rename file/directory request.
**/
class IFSRenameReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int SOURCE_CCSID_OFFSET = 22;
  private static final int TARGET_CCSID_OFFSET = 24;
  private static final int SOURCE_WORKING_DIR_HANDLE_OFFSET = 26;
  private static final int TARGET_WORKING_DIR_HANDLE_OFFSET = 30;
  private static final int RENAME_FLAGS_OFFSET = 34;
  private static final int SOURCE_NAME_LL_OFFSET = 36;
  private static final int SOURCE_NAME_CP_OFFSET = 40;
  private static final int SOURCE_NAME_OFFSET = 42;
  private static final int TEMPLATE_LENGTH = 16;

/**
Construct a file rename request.
@param sourceName the current file name
@param targetName the new file name
@param fileNameCCSID file name CCSID
@param replaceTarget determines what to do if the new file name is already in use
**/
  IFSRenameReq(byte[]  sourceName,
               byte[]  targetName,
               int     fileNameCCSID,
               boolean replaceTarget)
  {
    super(20 + TEMPLATE_LENGTH + 6 + sourceName.length + 6 +
          targetName.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x000F);
    set16bit(fileNameCCSID, SOURCE_CCSID_OFFSET);
    set16bit(fileNameCCSID, TARGET_CCSID_OFFSET);
    set32bit(1, SOURCE_WORKING_DIR_HANDLE_OFFSET);
    set32bit(1, TARGET_WORKING_DIR_HANDLE_OFFSET);
    set16bit((replaceTarget ? 1 : 0), RENAME_FLAGS_OFFSET);
    
    // Set the source LL.
    set32bit(sourceName.length + 6, SOURCE_NAME_LL_OFFSET);

    // Set the source code point.
    set16bit(0x0003, SOURCE_NAME_CP_OFFSET);

    // Set the source file name characters.
    System.arraycopy(sourceName, 0, data_, SOURCE_NAME_OFFSET,
                     sourceName.length);
    
    // Set the target LL.
    int targetNameLLOffset = SOURCE_NAME_OFFSET + sourceName.length;
    set32bit(targetName.length + 6, targetNameLLOffset);

    // Set the target code point.
    set16bit(0x0004, targetNameLLOffset + 4);

    // Set the target file name characters.
    System.arraycopy(targetName, 0, data_, targetNameLLOffset + 6,
                     targetName.length);
  }

}




