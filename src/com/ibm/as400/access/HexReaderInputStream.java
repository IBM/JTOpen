///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HexReaderInputStream.java
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
 * Converts a stream of hexadecimal characters to their actual
 * integer (byte) values.
 * For example, if the underlying Reader contains the data "123ABC",
 * this InputStream will return the following bytes when read() is called:
 * 0x12, 0x3A, 0xBC.
 * @see com.ibm.as400.access.HexReader
**/
class HexReaderInputStream extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private Reader reader_;
  private boolean cached_ = false;
  private char cachedChar_;

  public HexReaderInputStream(Reader reader)
  {
    super();
    reader_ = reader;
  }

  public int available() throws IOException
  {
    if (cached_ && reader_.ready()) return 1;
    return 0;
  }

  public void close() throws IOException
  {
    reader_.close();
  }

  public synchronized int read() throws IOException
  {
    if (cached_)
    {
      cached_ = false;
      char lo = (char)reader_.read();
      if (lo == -1) return -1;

      try
      {
          return BinaryConverter.charsToByte(cachedChar_, lo);
      }
      catch(NumberFormatException e)
      {
          throw new ExtendedIOException(ExtendedIOException.CANNOT_CONVERT_VALUE);
      }
    }
    else
    {
      char hi = (char)reader_.read();
      if (hi == -1) return -1;
      char lo = (char)reader_.read();
      if (lo == -1) return -1;

      try
      {
          return BinaryConverter.charsToByte(hi, lo);
      }
      catch(NumberFormatException e)
      {
          throw new ExtendedIOException(ExtendedIOException.CANNOT_CONVERT_VALUE);
      }
    }
  }

  public synchronized int read(byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }

  public synchronized int read(byte[] b, int off, int len) throws IOException
  {
    if (b == null) throw new NullPointerException("b");
    if (len == 0) return 0;
    if (cached_)
    {
      cached_ = false;
      char lo = (char)reader_.read();
      if (lo == -1) return -1;
      try
      {
          b[off] = BinaryConverter.charsToByte(cachedChar_, lo);
      }
      catch(NumberFormatException e)
      {
          throw new ExtendedIOException(ExtendedIOException.CANNOT_CONVERT_VALUE);
      }
      return 1;
    }
    else
    {
      char[] buf = new char[len*2];
      int numRead = reader_.read(buf);
      if (numRead % 2 == 1)
      {
        cached_ = true;
        cachedChar_ = buf[numRead-1];
        --numRead;
      }
      try
      {
          return BinaryConverter.stringToBytes(buf, 0, numRead, b, off);
      }
      catch(NumberFormatException e)
      {
          throw new ExtendedIOException(ExtendedIOException.CANNOT_CONVERT_VALUE);
      }
    }
  }

  public synchronized long skip(long n) throws IOException
  {
    if (n == 0) return 0;
    if (cached_) cached_ = false;
    long skipped = reader_.skip(n*2);
    return skipped / 2;
  }
}

