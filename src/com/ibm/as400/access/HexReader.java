///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HexReader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;

/**
 * Converts a stream of bytes to their character
 * hexadecimal representation.
 * For example, if the underlying InputStream contains the data:
 * 0xFF, 0xAB, 0x40, 0x20
 * this Reader will return the following characters when read() is called:
 * "FFAB4020"
 * @see com.ibm.as400.access.HexReaderInputStream
**/
class HexReader extends InputStreamReader
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private InputStream in_;
  private boolean cached_ = false;
  private byte cachedByte_;

  public HexReader(InputStream in)
  {
    super(in);
    in_ = in;
  }

  public int read() throws IOException
  {
    synchronized(lock)
    {
      if (cached_)
      {
        cached_ = false;
        return BinaryConverter.loNibbleToChar(cachedByte_);
      }
      else
      {
        cachedByte_ = (byte)in_.read();
        cached_ = true;
        return BinaryConverter.hiNibbleToChar(cachedByte_);
      }
    }
  }

  public int read(char[] cbuf, int offset, int length) throws IOException
  {
    if (cbuf == null) throw new NullPointerException("cbuf");
    if (length == 0) return 0;
    synchronized(lock)
    {
      if (cached_)
      {
        cached_ = false;
        cbuf[offset] = BinaryConverter.loNibbleToChar(cachedByte_);
        return 1;
      }
      else
      {
        byte[] buf = new byte[length/2];
        int numRead = in_.read(buf);
        int numConverted = BinaryConverter.bytesToString(buf, 0, numRead, cbuf, offset);
        return numConverted;
      }
    }
  }
}

  

