///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSExchangeAttrRep.java
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
Exchange server attributes reply.
**/
class IFSExchangeAttrRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int DATA_STREAM_LEVEL_OFFSET = 22; // @A1A
  private static final int MAX_DATA_BLOCK_OFFSET = 26;
  private static final int CCSID_LL_OFFSET = 30;
  private static final int CCSID_CP_OFFSET = 34;
  private static final int CCSID_OFFSET = 36;


/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSExchangeAttrRep();
  }

/**
Get the highest data stream level supported by this server.
@return the data stream level
**/
  int getDataStreamLevel()  // @A1A
  {
    return get16bit( DATA_STREAM_LEVEL_OFFSET);
  }

/**
Get the maximum data block size in bytes.
@return the maximum block size
**/
  int getMaxDataBlockSize()
  {
    return get32bit( MAX_DATA_BLOCK_OFFSET);
  }

/**
Get the server's preferred CCSID.
**/
  int getPreferredCCSID()
  {
    return get16bit(CCSID_OFFSET);
  }

/**
Get the server's preferred CCSIDs.  Used for debugging.
**/
  int[] getPreferredCCSIDs()  // @A1A
  {
    int llValue = get32bit(CCSID_LL_OFFSET);
    int count = (llValue - 6) / 2;
    int[] list = new int[count];
    for (int i=0, offset=CCSID_OFFSET;
         i<count;
         i++, offset+=2)
    {
      list[i] = get16bit(offset);
    }
    return list;
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8009;
  }
}


