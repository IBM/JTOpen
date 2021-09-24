///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ReaderInputStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 A ReaderInputStream represents a Toolbox converter that uses stateful character conversion to convert characters into bytes.  That is, it wraps an underlying Reader and reads/caches the appropriate number of characters to return the requested number of bytes.  This is especially useful for mixed byte tables where the number of converted bytes is almost never the same as the number of underlying Unicode characters.  This class exists primarily for use with JDBC CLOBs, but other components are free to use it as well.
 <p>For example, the following code shows two methods that perform essentially the same conversion, except one uses character converters that are part of the Java runtime, and the other uses character converters that are part of the Toolbox:
 <pre>
 *  public static InputStream getJavaConversionStream(String data, String encoding)
 *  {
 *    byte[] b = data.getBytes(encoding);
 *    return new java.io.ByteArrayInputStream(b);
 *  }
 *
 *  public static InputStream getToolboxConversionStream(String data, String encoding)
 *  {
 *    StringReader r = new StringReader(data);
 *    return new com.ibm.as400.access.ReaderInputStream(r, encoding);
 *  }
 </pre>
 @see  com.ibm.as400.access.ConvTableReader
 @see  java.io.InputStreamReader
 **/
public class ReaderInputStream extends InputStream
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    private Reader reader_;
    private ConvTable table_;

    private int ccsid_ = -1;
    private String encoding_ = null;
    private BidiConversionProperties properties_ = new BidiConversionProperties();

    private char[] cache_ = new char[1024];
    private byte[] b_cache_ = new byte[2562]; // ((1024*5)+3)/2 == worst case mixed-byte array size +1 for extra shift byte, just in case.
    private int nextRead_ = 0; // Cache needs to be filled when nextRead_ >= nextWrite_.
    private int nextWrite_ = 0;

    private int mode_ = ConvTableReader.SB_MODE; // Default to single-byte mode unless we receive a shift-out.


    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified encoding.
     @param  reader  The Reader from which to read characters.
     @param  encoding  The name of a supported Java character encoding.
     @exception  UnsupportedEncodingException  If the specified character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, String encoding) throws UnsupportedEncodingException
    {
        if (reader == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'reader' is null.");
            throw new NullPointerException("reader");
        }
        if (encoding == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'reader' is null.");
            throw new NullPointerException("encoding");
        }

        reader_ = reader;
        encoding_ = encoding;
        initializeTable();
    }

    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified CCSID.
     @param  reader  The Reader from which to read characters.
     @param  ccsid  The CCSID used to convert characters into bytes.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, int ccsid) throws UnsupportedEncodingException
    {
        if (reader == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'reader' is null.");
            throw new NullPointerException("reader");
        }
        if (ccsid < 0 || ccsid > 65535)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        reader_ = reader;
        ccsid_ = ccsid;
        initializeTable();
    }

    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified CCSID and bi-directional string type.
     @param  reader  The Reader from which to read characters.
     @param  ccsid  The CCSID used to convert characters into bytes.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, int ccsid, int bidiStringType) throws UnsupportedEncodingException
    {
        this(reader, ccsid, new BidiConversionProperties(bidiStringType));
    }

    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified CCSID and bi-directional string type.
     @param  reader  The Reader from which to read characters.
     @param  ccsid  The CCSID used to convert characters into bytes.
     @param  properties  The bidi conversion properties.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, int ccsid, BidiConversionProperties properties) throws UnsupportedEncodingException
    {
        if (reader == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'reader' is null.");
            throw new NullPointerException("reader");
        }
        if (ccsid < 0 || ccsid > 65535)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        reader_ = reader;
        ccsid_ = ccsid;
        properties_ = properties;
        initializeTable();
    }

    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified CCSID and bi-directional string type.
     @param  reader  The Reader from which to read characters.
     @param  ccsid  The CCSID used to convert characters into bytes.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @param  cacheSize  The number of characters to store in the internal buffer.  The default is 1024.  This number must be greater than zero.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, int ccsid, int bidiStringType, int cacheSize) throws UnsupportedEncodingException
    {
        this(reader, ccsid, new BidiConversionProperties(bidiStringType), cacheSize);
    }

    /**
     Constructs a ReaderInputStream that will convert Unicode characters into bytes of the specified CCSID and bi-directional string type.
     @param  reader  The Reader from which to read characters.
     @param  ccsid  The CCSID used to convert characters into bytes.
     @param  properties  The bidi conversion properties.
     @param  cacheSize  The number of characters to store in the internal buffer.  The default is 1024.  This number must be greater than zero.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ReaderInputStream(Reader reader, int ccsid, BidiConversionProperties properties, int cacheSize) throws UnsupportedEncodingException
    {
        this(reader, ccsid, properties);
        if (cacheSize < 1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'cacheSize' is not valid:", cacheSize);
            throw new ExtendedIllegalArgumentException("cacheSize", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        cache_ = new char[cacheSize];  // The character cache.
        b_cache_ = new byte[((cacheSize*5)+3)/2]; // ((1024*5)+3)/2 == worst case mixed-byte array size +1 for extra shift byte, just in case.
    }

    /**
     Returns the number of bytes stored in this ReaderInputStream's internal cache.
     @return  The number of bytes available to be read without calling the underlying Reader.
     **/
    public synchronized int available() throws IOException
    {
        return nextWrite_ - nextRead_;
    }

    /**
     Closes this ReaderInputStream and its underlying Reader.  Calling close() multiple times will not throw an exception.
     @exception  IOException  If an I/O exception occurs.
     **/
    public synchronized void close() throws IOException
    {
        if (table_ == null) return;
        reader_.close();
        table_ = null;
    }

    private synchronized boolean fillCache() throws IOException
    {
        if (nextRead_ >= nextWrite_)
        {
            int numRead = reader_.read(cache_, 0, cache_.length);
            if (numRead == -1)
            {
                return false;
            }
            String s = new String(cache_, 0, numRead);
            byte[] b = table_.stringToByteArray(s, properties_);
            int firstByte = b[0] & 0x00FF;
            int lastByte = b[b.length - 1] & 0x00FF;
            if (mode_ == ConvTableReader.SB_MODE)
            {
                if (firstByte == ConvTableMixedMap.shiftOut_)
                {
                    if (lastByte == ConvTableMixedMap.shiftIn_)
                    {
                        mode_ = ConvTableReader.DB_MODE;
                        System.arraycopy(b, 0, b_cache_, 0, b.length - 1);
                        nextRead_ = 0;
                        nextWrite_ = b.length - 1;
                    }
                    else
                    {
                        System.arraycopy(b, 0, b_cache_, 0, b.length);
                        nextRead_ = 0;
                        nextWrite_ = b.length;
                    }
                }
                else if (lastByte == ConvTableMixedMap.shiftIn_)
                {
                    mode_ = ConvTableReader.DB_MODE;
                    System.arraycopy(b, 0, b_cache_, 0, b.length - 1);
                    nextRead_ = 0;
                    nextWrite_ = b.length - 1;
                }
                else
                {
                    System.arraycopy(b, 0, b_cache_, 0, b.length);
                    nextRead_ = 0;
                    nextWrite_ = b.length;
                }
            }
            else
            {
                if (firstByte == ConvTableMixedMap.shiftOut_)
                {
                    if (lastByte == ConvTableMixedMap.shiftIn_)
                    {
                        System.arraycopy(b, 1, b_cache_, 0, b.length-2);
                        nextRead_ = 0;
                        nextWrite_ = b.length - 2;
                    }
                    else
                    {
                        mode_ = ConvTableReader.SB_MODE;
                        System.arraycopy(b, 1, b_cache_, 0, b.length - 1);
                        nextRead_ = 0;
                        nextWrite_ = b.length - 1;
                    }
                }
                else if (lastByte == ConvTableMixedMap.shiftIn_)
                {
                    b_cache_[0] = ConvTableMixedMap.shiftIn_;
                    System.arraycopy(b, 0, b_cache_, 1, b.length - 1);
                    nextRead_ = 0;
                    nextWrite_ = b.length;
                }
                else
                {
                    mode_ = ConvTableReader.SB_MODE;
                    b_cache_[0] = ConvTableMixedMap.shiftIn_;
                    System.arraycopy(b, 0, b_cache_, 1, b.length);
                    nextRead_ = 0;
                    nextWrite_ = b.length + 1;
                }
            }
        }
        if (nextRead_ >= nextWrite_) // Still didn't read enough, so try again.
        {
            // This should never happen, but just in case.
            return fillCache();
        }
        return true;
    }

    /**
     Returns the bi-directional string type in use by this ReaderInputStream.
     @return  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     **/
    public int getBidiStringType()
    {
        return properties_.getBidiStringType();
    }

    /**
     Returns the maximum number of characters that may be stored in the internal buffer.  This number represents the number of characters that may be read out of the underlying Reader any time a read() method is called on this ReaderInputStream.
     @return  The size of the character cache in use by this ReaderInputStream.
     **/
    public int getCacheSize()
    {
        return cache_.length;
    }

    /**
     Returns the CCSID used by this ReaderInputStream.
     @return  The CCSID, or -1 if the CCSID is not known.
     **/
    public int getCcsid()
    {
        return ccsid_;
    }

    /**
     Returns the encoding used by this ReaderInputStream.  If the CCSID is not known, the encoding provided on the constructor is returned.  Otherwise, the corresponding encoding for the CCSID is returned, which may be null if no such mapping exists.
     @return  The encoding, or null if the encoding is not known.
     **/
    public String getEncoding()
    {
        if (ccsid_ == -1 || encoding_ != null)
        {
            return encoding_;
        }
        else
        {
            return ConversionMaps.ccsidToEncoding(ccsid_);
        }
    }

    private void initializeTable() throws UnsupportedEncodingException
    {
        if (encoding_ != null)
        {
            String ccsidStr = ConversionMaps.encodingToCcsidString(encoding_);
            if (ccsidStr != null)
            {
                ccsid_ = Integer.parseInt(ccsidStr);
            }
        }
        if (ccsid_ == -1)
        {
            table_ = ConvTable.getTable(encoding_);
        }
        else
        {
            table_ = ConvTable.getTable(ccsid_, null);
        }
    }

    /**
     Reads a single byte.  If close() is called prior to calling this method, an exception will be thrown.
     @return  The byte read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public synchronized int read() throws IOException
    {
        if (fillCache())
        {
            return b_cache_[nextRead_++];
        }
        return -1;
    }

    /**
     Reads bytes into the specified array.  If close() is called prior to calling this method, an exception will be thrown.
     @param  buffer  The destination buffer.
     @return  The number of bytes read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public synchronized int read(byte[] buffer) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }
        if (buffer.length == 0) return 0;

        if (fillCache())
        {
            int max = buffer.length > (nextWrite_-nextRead_) ? (nextWrite_-nextRead_) : buffer.length;
            System.arraycopy(b_cache_, nextRead_, buffer, 0, max);
            nextRead_ += max;
            return max;
        }
        return -1;
    }

    /**
     Reads bytes into a portion of the specified array. If close() is called prior to calling this method, an exception will be thrown.
     @param  buffer  The destination buffer.
     @param  offset  The offset into the buffer at which to begin storing data.
     @param  length  The maximum number of bytes to store.
     @return  The number of bytes read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public synchronized int read(byte[] buffer, int offset, int length) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }

        if (offset < 0 || offset > buffer.length)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'offset' is not valid:", offset);
            throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length < 0 || length > (buffer.length - offset))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length == 0) return 0;

        if (fillCache())
        {
            int max = length > (nextWrite_-nextRead_) ? (nextWrite_ - nextRead_) : length;
            System.arraycopy(b_cache_, nextRead_, buffer, offset, max);
            nextRead_ += max;
            return max;
        }
        return -1;
    }

    /**
     Skips the specified number of bytes.  If close() is called prior to calling this method, an exception will be thrown.
     @param  length  The number of bytes to skip.
     @return  The number of bytes actually skipped.
     @exception  IOException  If an I/O exception occurs.
     **/
    public synchronized long skip(long length) throws IOException
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid: " + length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length == 0) return 0;
        long total = 0;
        byte[] buf = new byte[length < b_cache_.length ? (int)length : b_cache_.length];
        int r = read(buf);
        if (r < 0) return 0;
        total += r;
        while (r > 0 && total < length)
        {
            r = read(buf);
            if (r > 0) total += r;
        }
        return total;
    }
}
