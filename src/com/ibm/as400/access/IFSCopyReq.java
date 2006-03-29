///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCopyReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;


/**
Copy file request.
**/
class IFSCopyReq extends IFSDataStreamReq
{
  private static final int SOURCE_CCSID_OFFSET = 22; // CCSID of source filename
  private static final int TARGET_CCSID_OFFSET = 24; // CCSID of target filename
  private static final int SOURCE_WORKING_DIR_HANDLE_OFFSET = 26;
  private static final int TARGET_WORKING_DIR_HANDLE_OFFSET = 30;
  private static final int DUPLICATE_TARGET_OPTION_OFFSET = 34;
  private static final int TEMPLATE_LENGTH = 16;
  private static final int SOURCE_NAME_LL_OFFSET = 36;

/**
Construct a copy request. This request uses a standard IFSReturnCodeRep datastream for its reply.
**/
  IFSCopyReq(String sourcePath, String destinationPath, boolean replace)
  {
    // 20-byte header, plus template length, plus two 6-byte LLCP's, plus (sourcePath length plus targetPath length)*(2 bytes per Unicode char).
    super(20 + TEMPLATE_LENGTH + 12 + (sourcePath.length()+destinationPath.length())*2);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0001);
    // We always use UCS-2 for filenames; this could be upgraded to UTF-16 when the server supports it.
    set16bit(13488, SOURCE_CCSID_OFFSET);
    set16bit(13488, TARGET_CCSID_OFFSET);
    set32bit(1, SOURCE_WORKING_DIR_HANDLE_OFFSET); // '1' is always the system root "/"
    set32bit(1, TARGET_WORKING_DIR_HANDLE_OFFSET); // '1' is always the system root "/"

    // Duplicate target option
    // Bits 3,2 = "deep copy" versus "shallow copy"
    //  11 = not allowed
    //  10 = copy the directory, its immediate children, and all of its subdirs
    //  01 = copy the directory and its immediate children only
    //  00 = create the directory name in the target; copy no children
    // Bits 1,0 = "append" versus "replace"
    //  11 = append data from the source file to the target file if the target exists
    //  10 = same as 00 (bit 1 is ignored if bit 0 is off)
    //  01 = replace the target file with the source file, if the target exists
    //  00 = do not overwrite the destination if it exists, unless source is a dir
    int dupTargetOpt = 0x08 + (replace ? 0x01 : 0x00); // We always do a deep copy.
    set16bit(dupTargetOpt, DUPLICATE_TARGET_OPTION_OFFSET);
    
    int offset = SOURCE_NAME_LL_OFFSET;
    set32bit(6+sourcePath.length()*2, offset); // source name LL
    set16bit(0x0003, offset+4);                // source name CP

    for (int i=0; i<sourcePath.length(); ++i)
    {
      set16bit(sourcePath.charAt(i), offset+6+(i*2)); // source name
    }

    offset += 6+(sourcePath.length()*2);
    set32bit(6+destinationPath.length()*2, offset); // target name LL
    set16bit(0x0004, offset+4);                     // target name CP
    for (int i=0; i<destinationPath.length(); ++i)
    {
      set16bit(destinationPath.charAt(i), offset+6+(i*2)); // target name
    }
  }
}





