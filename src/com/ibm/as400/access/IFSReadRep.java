///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSReadRep.java
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
Read data reply.
**/
class IFSReadRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int CCSID_OFFSET = 22;
  private static final int FILE_DATA_LL_OFFSET = 24;
  private static final int FILE_DATA_OFFSET = 30;

/**
Construct a read reply.
**/
  IFSReadRep()
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
    return new IFSReadRep();
  }

/**
Get the data.
@return the bytes read
**/
  public byte[] getData()
  {
    int bytesRead = get32bit( FILE_DATA_LL_OFFSET) - 6;
    byte[] dataRead = new byte[bytesRead];
    for (int i = 0; i < bytesRead; i++)
    {
      dataRead[i] = data_[FILE_DATA_OFFSET + i];
    }

    return dataRead;
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8003;
  }

}




