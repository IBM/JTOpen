///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDataStreamReq.java
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
Base class for all IFS server request data streams.
**/
class IFSDataStreamReq extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

/**
Construct a byte stream server request.
@param initialDataBufferLength the initial buffer size in bytes
@param chainIndicator the chain indicator
**/
  protected IFSDataStreamReq(int initialDataBufferLength,
                             int chainIndicator)
  {
    data_ = new byte[initialDataBufferLength];
    setHeaderID(0);
    setServerID(0xE002);
    setCSInstance(0);
    set16bit(chainIndicator, CHAIN_INDICATOR_OFFSET);
  }

/**
Construct a byte stream server request.
@param initialDataBufferLength the initial buffer size in bytes
**/
  protected IFSDataStreamReq(int initialDataBufferLength)
  {
    this(initialDataBufferLength, 0);
  }


/**
Set the chain indicator.
@param value the desired chain indicator value
**/
  void setChainIndicator(int value)
  {
    set16bit(value, CHAIN_INDICATOR_OFFSET);
  }

/**
Set a long (8 byte) data value.
@param value the value
@param offset the data stream offset
**/
  protected void setData(long value,
                         int  offset)
  {
    long v = value;
    for (int i = 0; i < 8; i++, v >>>= 8)
    {
      data_[offset + i] = (byte) v;
    }
  }

/**
Set a String value.
@param value the value
@param offset the data stream offset
**/
  protected void setData(String value,
                         int    offset)
  {
    for (int i = 0, j = 0; i < value.length(); i++, j += 2)
    {
      int character = (int) value.charAt(i) & 0xffff;
      data_[offset + j + 1] = (byte) (character & 0xff);
      data_[offset + j] = (byte) (character >>> 8);
    }
  }

/**
Set a date/time value.
@param date number of milliseconds since January 1, 1970, 00:00:00 GMT
@param offset the data stream offset
**/
  protected void setDate(long date,
                         int  offset)
  {
    int seconds = (int) (date / 1000);
    long s = seconds * 1000L;

    // @A1C
    // Since the second byte needs to be in microseconds, need to multiply by 1000.
    //
    // Original code:
    // int ms = (int) (date - s);
    int ms = ((int) (date - s)) * 1000;

    set32bit(seconds, offset);
    set32bit(ms, offset + 4);
  }
}




