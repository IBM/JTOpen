///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSExchangeAttrReq.java
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
Exchange server attributes request.
**/
class IFSExchangeAttrReq extends IFSDataStreamReq
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final int POSIX_PATTERN_MATCH = 0;
  static final int POSIX_ALL_PATTERN_MATCH = 1;
  static final int PC_PATTERN_MATCH = 2;
  private static final int DATA_STREAM_LEVEL_OFFSET = 22;
  private static final int FLAGS_OFFSET = 24;
  private static final int MAX_DATA_BLOCK_OFFSET = 26;
  private static final int CCSID_LL_OFFSET = 30;
  private static final int CCSID_CP_OFFSET = 34;
  private static final int CCSID_OFFSET = 36;
  private static final int TEMPLATE_LENGTH = 10;

/**
Construct an exchange attributes request.
@param useGMT if true date/time values are GMT standard
@param usePosixReturnCodes if true posix style return codes are used
@param patternMatchStyle pattern matching semantics for all requests that allow wildcards (* and ?) in the file name except the list attributes request [POSIX_PATTERN_MATCH | POSIX_ALL_PATTERN_MATCH | PC_PATTERN_MATCH]
@param maxDataBlock the maximum data transfer (in bytes) that the client can handle
@param preferredCCSID the preferred CCSID of the client
**/
  IFSExchangeAttrReq(boolean useGMT,
                     boolean usePosixReturnCodes,
                     int     patternMatchStyle,
                     int     maxDataBlock,
                     int     preferredCCSID)
  {
    super(20 + TEMPLATE_LENGTH + 8);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0016);
    set16bit(0, DATA_STREAM_LEVEL_OFFSET);
    int flags = 0;
    if (useGMT)
    {
      flags |= 4;
    }
    if (usePosixReturnCodes)
    {
      flags |= 8;
    }
    flags |= patternMatchStyle;
    set16bit(flags, FLAGS_OFFSET);
    set32bit(maxDataBlock, MAX_DATA_BLOCK_OFFSET);
    set32bit(8, CCSID_LL_OFFSET);
    set16bit(0x000a, CCSID_CP_OFFSET);
    set16bit(preferredCCSID, CCSID_OFFSET);
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }
}



