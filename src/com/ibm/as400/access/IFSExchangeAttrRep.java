///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

  private static final int MAX_DATA_BLOCK_OFFSET = 26;
  private static final int CCSID_LL_OFFSET = 30;
  private static final int CCSID_CP_OFFSET = 34;
  private static final int CCSID_OFFSET = 36;

/**
Construct an exchange attributes reply.
**/
  IFSExchangeAttrRep()
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
    return new IFSExchangeAttrRep();
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
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8009;
  }
}


