///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDataStream.java
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
Base class for all IFS server data streams.
**/
class IFSDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  protected static final int CHAIN_INDICATOR_OFFSET = 20;

/**
Construct an IFSDataStream object.
**/
  protected IFSDataStream()
  {
  }


/**
Get a date value.
@param offset the data stream offset of the date information
@return the number of milliseconds since January 1, 1970 00:00:00 GMT
**/
  protected long getDate(int offset)
  {
    long seconds = (long) get32bit( offset);
    long ms = (long) get32bit( offset + 4);
    
    // @A1C
    // Interpret the second 4 byte of data as microseconds instead of
    // milliseconds.
    //
    // Original code:
    // return (seconds * 1000L + ms);
    return (seconds * 1000L + ms/1000);
  }

/**
Determine if there are more data streams chained to this one.
@return true if the chain continues; false otherwise.
**/
  boolean isEndOfChain()
  {
    return ((get16bit( CHAIN_INDICATOR_OFFSET) & 1) == 0);
  }
}
