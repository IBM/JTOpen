///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConvTableReader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;

/**
 * A ConvTableReader represents a Toolbox converter that uses
 * stateful character conversion. That is, it wraps an underlying
 * InputStream and reads/caches the appropriate number of bytes
 * to return the requested number of Unicode characters. This is
 * especially useful for mixed byte tables where the number of
 * converted Unicode characters is almost never the same as the number of
 * underlying EBCDIC bytes. This class exists primarily for use
 * with the IFSText classes, but other components are free to use it
 * as well.
 * @see ConvTableWriter
**/
public class ConvTableReader extends InputStreamReader
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

//@B0D  InputStream lock_ = null;
  BufferedInputStream is_ = null;

  int ccsid_ = -1;
  ConvTable table_ = null;

  int type_ = BidiStringType.DEFAULT;

  //StringBuffer cache_ = new StringBuffer();

  // The mode is used for mixed-byte tables only.
  static final int DB_MODE = 1;
  static final int SB_MODE = 2;
  int mode_ = SB_MODE; // default to single-byte mode unless we receive a shift-out

  // The different table types, based on the instance of the ConvTable.
  static final int SB_TABLE = 10;
  static final int DB_TABLE = 11;
  static final int MB_TABLE = 12;
  static final int JV_TABLE = 13;
  int tableType_ = SB_TABLE;

  char[] cache_ = new char[1024]; // the character cache
  byte[] b_cache_ = new byte[2562]; // ((1024*5)+3)/2 == worst case mixed-byte array size +1 for extra shift byte, just in case.
  boolean isCachedByte_ = false; // used for double-byte tables
  byte cachedByte_ = 0; // used for double-byte tables

  int nextRead_ = 0; // cache needs to be filled when nextRead_ >= nextWrite_
  int nextWrite_ = 0;


  /**
   * Creates a ConvTableReader that uses the default character encoding. The CCSID this reader uses may be set if
   * a known mapping exists for this platform's default character encoding.
   * @param in The InputStream from which to read characters.
   * @exception UnsupportedEncodingException If the default character encoding or its associated CCSID is not supported.
  **/
  public ConvTableReader(InputStream in) throws UnsupportedEncodingException
  {
    super(in);
//@B0D    lock_ = in;
    is_ = new BufferedInputStream(in);
    initializeCcsid();
    initializeTable();
  }


  /**
   * Creates a ConvTableReader that uses the specified character encoding. The CCSID this reader uses may be set if
   * a known mapping exists for the given encoding.
   * @param in The InputStream from which to read characters.
   * @param encoding The name of a supported character encoding.
   * @exception UnsupportedEncodingException If the specified character encoding or its associated CCSID is not supported.
  **/
  public ConvTableReader(InputStream in, String encoding) throws UnsupportedEncodingException
  {
    super(in, encoding);
//@B0D    lock_ = in;
    is_ = new BufferedInputStream(in);
    initializeCcsid();
    initializeTable();
  }


  /**
   * Creates a ConvTableReader that uses the specified CCSID.
   * @param in The InputStream from which to read characters.
   * @param ccsid The CCSID.
   * @exception UnsupportedEncodingException If the specified CCSID or its corresponding character encoding is not supported.
  **/
  public ConvTableReader(InputStream in, int ccsid) throws UnsupportedEncodingException
  {
    super(in);
//@B0D    lock_ = in;
    if(ccsid < 0 || ccsid > 65535)
    {
      throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    is_ = new BufferedInputStream(in);
    ccsid_ = ccsid;
    initializeTable();
  }


  /**
   * Creates a ConvTableReader that uses the specified CCSID and bi-directional string type.
   * @param in The InputStream from which to read characters.
   * @param ccsid The CCSID.
   * @param bidiStringType The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
   * @exception UnsupportedEncodingException If the specified CCSID or its corresponding character encoding is not supported.
  **/
  public ConvTableReader(InputStream in, int ccsid, int bidiStringType) throws UnsupportedEncodingException
  {
    super(in);
//@B0D    lock_ = in;
    if(ccsid < 0 || ccsid > 65535)
    {
      throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if(bidiStringType != 0 && (bidiStringType < 4 || bidiStringType > 11))
    {
      throw new ExtendedIllegalArgumentException("bidiStringType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    is_ = new BufferedInputStream(in);
    ccsid_ = ccsid;
    type_ = bidiStringType;
    initializeTable();
  }


  /**
   * Closes this ConvTableReader and its underlying input stream. Calling close() multiple times will not throw an exception.
   * @exception IOException If an I/O exception occurs.
  **/
  public void close() throws IOException
  {
    synchronized(lock) //@B0C
    {
      if(table_ == null) return; //we are already closed
      table_ = null;
      cache_ = null;
      b_cache_ = null;
      super.close();
      is_.close();
    }
  }


  private boolean fillCache() throws IOException
  {
    synchronized(lock) //@B0C
    {
      checkOpen();
      if(nextRead_ >= nextWrite_)
      {
        if(Trace.isTraceOn() && Trace.isTraceConversionOn())
        {
          Trace.log(Trace.CONVERSION, "Filling cache for reader "+ccsid_+" ["+toString()+"]: "+nextRead_+","+nextWrite_+","+cache_.length);
        }
        if(tableType_ == SB_TABLE)
        {
          int numRead = is_.read(b_cache_, 0, 1024);
          if(numRead == -1)
          {
            if(Trace.isTraceOn() && Trace.isTraceConversionOn())
            {
              Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
            }
            return false;
          }
          String s = table_.byteArrayToString(b_cache_, 0, numRead, type_);
          s.getChars(0, s.length(), cache_, 0);
          nextWrite_ = s.length();
          nextRead_ = 0;
        }
        else if(tableType_ == DB_TABLE)
        {
          if(isCachedByte_)
          {
            int numRead = is_.read(b_cache_, 1, 2048);
            if(numRead == -1)
            {
              if(Trace.isTraceOn() && Trace.isTraceConversionOn())
              {
                Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
              }
              return false;
            }
            b_cache_[0] = cachedByte_;
            if(numRead % 2 == 0) // read an even number, need to proliferate the last byte
            {
              cachedByte_ = b_cache_[numRead];
              isCachedByte_ = true;
            }
            else
            {
              isCachedByte_ = false;
            }
            String s = table_.byteArrayToString(b_cache_, 0, numRead, type_);
            s.getChars(0, s.length(), cache_, 0);
            nextWrite_ = s.length();
            nextRead_ = 0;
          }
          else
          {
            int numRead = is_.read(b_cache_, 0, 2048);
            if(numRead == -1)
            {
              if(Trace.isTraceOn() && Trace.isTraceConversionOn())
              {
                Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
              }
              return false;
            }
            if(numRead % 2 == 1) // read an odd number of characters
            {
              cachedByte_ = b_cache_[numRead-1];
              isCachedByte_ = true;
              --numRead;
            }
            String s = table_.byteArrayToString(b_cache_, 0, numRead, type_);
            s.getChars(0, s.length(), cache_, 0);
            nextWrite_ = s.length();
            nextRead_ = 0;
          }
        }
        else if(tableType_ == MB_TABLE)
        {
          // Max number of bytes for worst-case mixed-byte data scenario is (5x+3)/2
          int numRead = 0;
          int c = 0;
          if(mode_ == DB_MODE)
          {
            b_cache_[numRead++] = ConvTableMixedMap.shiftOut_; // begin with a shift-out since we left off in DB_MODE last time
            if(isCachedByte_) // note that we don't ever cache a shift byte or a single-byte char - we only cache half of a double-byte char
            {
              b_cache_[numRead++] = cachedByte_;
              isCachedByte_ = false;
            }
            else
            {
              if(Trace.isTraceOn() && Trace.isTraceConversionOn())
              {
                Trace.log(Trace.ERROR, "Error in mixed-byte cache algorithm.");
              }
              // We should ALWAYS have a cached byte if we are starting in DB_MODE
              throw new InternalErrorException(InternalErrorException.UNKNOWN);
            }
          }
          for(; numRead<b_cache_.length-1 && c != -1; ++numRead)
          {
            if(mode_ == SB_MODE)
            {
              c = is_.read();
              if(c != -1)
              {
                b_cache_[numRead] = (byte)c;
                if(c == ConvTableMixedMap.shiftOut_)
                {
                  mode_ = DB_MODE;
                }
              }
              else
              {
                --numRead;
              }
            }
            else if(mode_ == DB_MODE)
            {
              c = is_.read();
              if(c != -1)
              {
                b_cache_[numRead++] = (byte)c;
                if(c == ConvTableMixedMap.shiftIn_)
                {
                  mode_ = SB_MODE;
                  --numRead;
                }
                else
                {
                  c = is_.read();
                  if(c != -1)
                  {
                    b_cache_[numRead] = (byte)c;
                  }
                }
              }
            }
          }
          if(mode_ == DB_MODE)
          {
            // need to finished with a shift-in
            b_cache_[numRead++] = ConvTableMixedMap.shiftIn_;
            c = is_.read();
            if(c != ConvTableMixedMap.shiftIn_)
            {
              cachedByte_ = (byte)c;
              isCachedByte_ = true;
            }
            else
            {
              mode_ = SB_MODE;
            }
          }

          if(numRead == 0 && c == -1)
          {
            if(Trace.isTraceOn() && Trace.isTraceConversionOn())
            {
              Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
            }
            return false; // end-of-stream
          }
          String s = table_.byteArrayToString(b_cache_, 0, numRead, type_);

          s.getChars(0, s.length(), cache_, 0);
          nextWrite_ = s.length();
          nextRead_ = 0;

        }
        else if(tableType_ == JV_TABLE)
        {
          // Let Java handle it...
          int numRead = is_.read(b_cache_, 0, b_cache_.length);
          if(numRead == -1)
          {
            if(Trace.isTraceOn() && Trace.isTraceConversionOn())
            {
              Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
            }
            return false;
          }
          String s = table_.byteArrayToString(b_cache_, 0, numRead, type_);
          s.getChars(0, s.length(), cache_, 0);
          nextWrite_ = s.length();
          nextRead_ = 0;
        }
        if(Trace.isTraceOn() && Trace.isTraceConversionOn())
        {
          Trace.log(Trace.CONVERSION, "Filled cache for reader: "+nextRead_+","+nextWrite_+","+cache_.length, ConvTable.dumpCharArray(cache_, nextWrite_));
        }
      }
      if(nextRead_ >= nextWrite_) // Still didn't read enough, so try again.
      {
        // This should never happen, but the javadoc for InputStream is unclear if the read(byte[],int,int)
        // method will sometimes return 0 or always read at least 1 byte.
        return fillCache();
      }
      return true;
    }
  }


  /**
   * Returns the CCSID used by this ConvTableReader.
   * @return  The CCSID, or -1 if the CCSID is not known.
  **/
  public int getCcsid()
  {
    return ccsid_;
  }


  /**
   * Returns the encoding used by this ConvTableReader. If the CCSID is not known, the superclass encoding is returned. Otherwise,
   * the corresponding encoding for the CCSID is returned, which may be null if no such mapping exists.
   * @return The encoding, or null if the encoding is not known.
  **/
  public String getEncoding()
  {
    if(ccsid_ == -1)
    {
      return super.getEncoding();
    }
    else
    {
      return ConversionMaps.ccsidToEncoding(ccsid_);
    }
  }


  private void initializeCcsid()
  {
    String enc = super.getEncoding();
    if(enc != null)
    {
      String ccsidStr = ConversionMaps.encodingToCcsidString(enc);
      if(ccsidStr != null)
      {
        ccsid_ = Integer.parseInt(ccsidStr);
      }
    }
  }


  private void initializeTable() throws UnsupportedEncodingException
  {
    try
    {
      if(ccsid_ == -1)
      {
        table_ = ConvTable.getTable(getEncoding());
      }
      else
      {
        table_ = ConvTable.getTable(ccsid_, null);
      }
      if(table_ instanceof ConvTableSingleMap ||
         table_ instanceof ConvTableBidiMap   ||
         table_ instanceof ConvTableAsciiMap)
      {
        tableType_ = SB_TABLE;
      }
      else if(table_ instanceof ConvTableDoubleMap ||
              table_ instanceof ConvTable1200 ||
              table_ instanceof ConvTable13488)
      {
        tableType_ = DB_TABLE;
      }
      else if(table_ instanceof ConvTableMixedMap)
      {
        tableType_ = MB_TABLE;
      }
      else if(table_ instanceof ConvTableJavaMap)
      {
        tableType_ = JV_TABLE;
      }
      else
      {
        if(Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "Unknown conversion table type: "+table_.getClass());
        }
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
      if(Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.CONVERSION, "ConvTableReader initialized with CCSID "+ccsid_+", encoding "+getEncoding()+", string type "+type_+", and table type "+tableType_+".");
      }
    }
    catch(UnsupportedEncodingException uee)
    {
      if(Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "The specified CCSID is not supported in the current JVM nor by the Toolbox: "+ccsid_+"/"+getEncoding(), uee);
      }
      throw uee;
    }
  }


  private void checkOpen() throws IOException
  {
    if(table_ == null) // if we are explicitly closed
    {
      is_.available(); // will hopefully throw an IOException
      // if not, we'll throw our own
      throw new IOException();
    }
  }


  /**
   * ConvTableReader does not support the mark() operation.
   * @return false
  **/
  public boolean markSupported()
  {
    return false;
  }


  /**
   * Reads a single character. If close() is called prior to calling this method, an exception will be thrown.
   * @return The character read, or -1 if the end of the stream has been reached.
   * @exception IOException If an I/O exception occurs.
  **/
  public int read() throws IOException
  {
    synchronized(lock) //@B0C
    {
      if(fillCache())
      {
        return cache_[nextRead_++];
      }
    }
    return -1;
  }


  /**
   * Reads characters into the specified array. If close() is called prior to calling this method, an exception will be thrown.
   * @param buffer The destination buffer.
   * @return The number of characters read, or -1 if the end of the stream has been reached.
   * @exception IOException If an I/O exception occurs.
  **/
  public int read(char[] buffer) throws IOException
  {
    if(buffer == null)
    {
      throw new NullPointerException("buffer");
    }
    synchronized(lock) //@B0C
    {
      if(fillCache())
      {
        int max = buffer.length > (nextWrite_-nextRead_) ? (nextWrite_-nextRead_) : buffer.length;
        System.arraycopy(cache_, nextRead_, buffer, 0, max);
        nextRead_ += max;
        return max;
      }
    }
    return -1;
  }


  /**
   * Reads characters into a portion of the specified array. If close() is called prior to calling this method, an exception will be thrown.
   * @param buffer The destination buffer.
   * @param offset The offset into the buffer at which to begin storing data.
   * @param length The maximum number of characters to store.
   * @return The number of characters read, or -1 if the end of the stream has been reached.
   * @exception IOException If an I/O exception occurs.
  **/
  public int read(char[] buffer, int offset, int length) throws IOException
  {
    if(buffer == null)
    {
      throw new NullPointerException("buffer");
    }
    if(offset < 0 || offset >= buffer.length)
    {
      throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if(length < 0 || length > buffer.length)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    synchronized(lock) //@B0C
    {
      if(fillCache())
      {
        int max = length > (nextWrite_-nextRead_) ? (nextWrite_-nextRead_) : length;
        System.arraycopy(cache_, nextRead_, buffer, offset, max);
        nextRead_ += max;
        return max;
      }
    }
    return -1;
  }


  /**
   * Reads up to <I>length</I> characters out of the underlying stream. If close() is called prior to calling this method, an exception will be thrown.
   * @param length The number of Unicode characters to return as a String. The
   *   number of bytes read from the underlying InputStream could be greater than
   *   <I>length</I>. 
   * @return A String of up to <I>length</I> Unicode characters, or null if the end of the
   *   stream has been reached. The actual number of
   *   characters returned may be less than the specified <I>length</I> if the end of
   *   the underlying InputStream is reached while reading.
   * @exception IOException If an I/O exception occurs.
  **/
  public String read(int length) throws IOException
  {
    if(length < 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    synchronized(lock) //@B0C
    {
      StringBuffer buf = new StringBuffer();
      if(fillCache())
      {
        while(fillCache() && buf.length() < length)
        {
          buf.append(cache_, nextRead_++, 1);
        }
        return buf.toString();
      }
    }
    return null;
  }


  /**
   * Tells whether this ConvTableReader is ready to be read. A ConvTableReader is ready if its input buffer is not empty or if bytes
   * are available to be read from the underlying input stream. If close() is called, a call to ready() will always return false.
   * @return true if the ConvTableReader is ready to read characters; false otherwise.
   * @exception IOException If an I/O exception occurs.
  **/
  public boolean ready() throws IOException
  {
    synchronized(lock) //@B0C
    {
      if (table_ == null) // we are closed
      {
        return super.ready(); // this should throw an IOException
      }
      return(nextRead_ < nextWrite_) || is_.available() > 0;
    }
  }


  /**
   * Skips the specified number of characters in the underlying stream. If close() is called prior to calling this method, an exception will be thrown.
   * @param length The number of characters to skip.
   * @return The number of characters actually skipped.
   * @exception IOException If an I/O exception occurs.
  **/
  public long skip(long length) throws IOException
  {
    if(length < 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    long total = 0;
    synchronized(lock) //@B0C
    {
      checkOpen();  
      char[] buf = new char[length < cache_.length ? (int)length : cache_.length];
      int r = read(buf);
      total += r;
      while(r > 0 && total < length)
      {
        r = read(buf);
        if(r > 0) total += r;
      }
    }
    return total;
  }
}
