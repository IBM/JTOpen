///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
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

  private static final int HEADER_LENGTH = 20; // @A1A
  private static final int TEMPLATE_LENGTH = 10;
  private static final int LL_CP_LENGTH = 6; // @A1A
  private static final int CCSID_LENGTH = 2; // @A1A

  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.
  private static final boolean DEBUG = false;


/**
Construct an exchange attributes request.
@param useGMT if true date/time values are GMT standard
@param usePosixReturnCodes if true posix style return codes are used
@param patternMatchStyle pattern matching semantics for all requests that allow wildcards (* and ?) in the file name except the list attributes request [POSIX_PATTERN_MATCH | POSIX_ALL_PATTERN_MATCH | PC_PATTERN_MATCH]
@param maxDataBlock the maximum data transfer (in bytes) that the client can handle
@param preferredCCSIDs a list of the preferred CCSIDs of the client
**/
  IFSExchangeAttrReq(boolean useGMT,             // @A1A
                     boolean usePosixReturnCodes,
                     int     patternMatchStyle,
                     int     maxDataBlock,
                     int     dataStreamLevel,      // @A2A
                     int[]   preferredCCSIDs)
  {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + LL_CP_LENGTH +
          CCSID_LENGTH*preferredCCSIDs.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0016);
    set16bit(dataStreamLevel, DATA_STREAM_LEVEL_OFFSET);
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
    set32bit(LL_CP_LENGTH + CCSID_LENGTH*preferredCCSIDs.length, CCSID_LL_OFFSET);
    set16bit(0x000a, CCSID_CP_OFFSET);
    for (int i=0, offset=CCSID_OFFSET;
         i<preferredCCSIDs.length;
         i++, offset+=CCSID_LENGTH)
    {
      set16bit(preferredCCSIDs[i], offset);
      if (DEBUG)
        System.out.println("DEBUG: IFSExchangeAttrReq(): client preferred CCSID = " +
                           preferredCCSIDs[i] + " at offset " + offset);
    }
  }
}



