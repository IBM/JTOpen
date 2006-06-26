///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableReader.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 A ConvTableReader represents a Toolbox converter that uses stateful character conversion.  That is, it wraps an underlying InputStream and reads/caches the appropriate number of bytes to return the requested number of Unicode characters.  This is especially useful for mixed byte tables where the number of converted Unicode characters is almost never the same as the number of underlying EBCDIC bytes.  This class exists primarily for use with the IFSText classes, but other components are free to use it as well.
 @see  com.ibm.as400.access.ConvTableWriter
 @see  com.ibm.as400.access.ReaderInputStream
 **/
public class ConvTableReader extends InputStreamReader
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    private BufferedInputStream is_ = null;

    private int ccsid_ = -1;
    private ConvTable table_ = null;

    private BidiConversionProperties properties_ = new BidiConversionProperties();

    // The mode is used for mixed-byte tables only.
    static final int DB_MODE = 1;
    static final int SB_MODE = 2;
    private int mode_ = SB_MODE;  // default to single-byte mode unless we receive a shift-out

    // The different table types, based on the instance of the ConvTable.
    private static final int SB_TABLE = 10;
    private static final int DB_TABLE = 11;
    private static final int MB_TABLE = 12;
    private static final int JV_TABLE = 13;
    private static final int UTF8_TABLE = 14;
    private int tableType_ = SB_TABLE;

    private char[] cache_ = new char[1024];  // The character cache.
    private byte[] b_cache_ = new byte[2562];  // ((1024*5)+3)/2 == worst case mixed-byte array size +1 for extra shift byte, just in case.
    private boolean isCachedByte_ = false;  // Used for double-byte tables.
    private byte cachedByte_ = 0;  // Used for double-byte tables.

    private byte[] leftovers = new byte[3];  // Used for portions of utf-8 characters at the cache_ boundary.
    private int  leftoverCount = 0;  // How many bytes are in the leftovers.

    private int nextRead_ = 0;  // Cache needs to be filled when nextRead_ >= nextWrite_
    private int nextWrite_ = 0;

    /**
     Creates a ConvTableReader that uses the default character encoding.  The CCSID this reader uses may be set if a known mapping exists for this platform's default character encoding.
     @param  in  The InputStream from which to read characters.
     @exception  UnsupportedEncodingException  If the default character encoding or its associated CCSID is not supported.
     **/
    public ConvTableReader(InputStream in) throws UnsupportedEncodingException
    {
        super(in);
        is_ = new BufferedInputStream(in);
        initializeCcsid();
        initializeTable();
    }

    /**
     Creates a ConvTableReader that uses the specified character encoding.  The CCSID this reader uses may be set if a known mapping exists for the given encoding.
     @param  in  The InputStream from which to read characters.
     @param  encoding  The name of a supported character encoding.
     @exception  UnsupportedEncodingException  If the specified character encoding or its associated CCSID is not supported.
     **/
    public ConvTableReader(InputStream in, String encoding) throws UnsupportedEncodingException
    {
        super(in, encoding);
        is_ = new BufferedInputStream(in);
        initializeCcsid();
        initializeTable();
    }

    /**
     Creates a ConvTableReader that uses the specified CCSID.
     @param  in  The InputStream from which to read characters.
     @param  ccsid  The CCSID.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableReader(InputStream in, int ccsid) throws UnsupportedEncodingException
    {
        super(in);
        if (ccsid < 0 || ccsid > 65535)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        is_ = new BufferedInputStream(in);
        ccsid_ = ccsid;
        initializeTable();
    }

    /**
     Creates a ConvTableReader that uses the specified CCSID and bi-directional string type.
     @param  in  The InputStream from which to read characters.
     @param  ccsid  The CCSID.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableReader(InputStream in, int ccsid, int bidiStringType) throws UnsupportedEncodingException
    {
        this(in, ccsid, new BidiConversionProperties(bidiStringType));
    }

    /**
     Creates a ConvTableReader that uses the specified CCSID and bi-directional string type.
     @param  in  The InputStream from which to read characters.
     @param  ccsid  The CCSID.
     @param  properties  The bidi conversion properties.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableReader(InputStream in, int ccsid, BidiConversionProperties properties) throws UnsupportedEncodingException
    {
        super(in);
        if (ccsid < 0 || ccsid > 65535)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        is_ = new BufferedInputStream(in);
        ccsid_ = ccsid;
        properties_ = properties;
        initializeTable();
    }

    /**
     Creates a ConvTableReader that uses the specified CCSID, bi-directional string type, and internal cache size.
     @param  in  The InputStream from which to read characters.
     @param  ccsid  The CCSID.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @param  cacheSize  The number of characters to store in the internal buffer.  The default is 1024.  This number must be greater than zero.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableReader(InputStream in, int ccsid, int bidiStringType, int cacheSize) throws UnsupportedEncodingException
    {
        this(in, ccsid, bidiStringType);
        if (cacheSize < 1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'cacheSize' is not valid:", cacheSize);
            throw new ExtendedIllegalArgumentException("cacheSize", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        cache_ = new char[cacheSize]; // The character cache.
        b_cache_ = new byte[((cacheSize*5)+3)/2]; // ((1024*5)+3)/2 == worst case mixed-byte array size +1 for extra shift byte, just in case.
    }

    /**
     Closes this ConvTableReader and its underlying input stream.  Calling close() multiple times will not throw an exception.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void close() throws IOException
    {
        synchronized (lock)
        {
            if (table_ == null) return; //we are already closed
            table_ = null;
            cache_ = null;
            b_cache_ = null;
            super.close();
            is_.close();
        }
    }

    private boolean fillCache() throws IOException
    {
        synchronized (lock)
        {
            checkOpen();
            if (nextRead_ >= nextWrite_)
            {
                int numRead = 0;
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Filling cache for reader " + ccsid_ + "/" + tableType_ + " [" + toString() + "]: " + nextRead_ + "," + nextWrite_ + "," + cache_.length);
                if (tableType_ == SB_TABLE || tableType_ == JV_TABLE)
                {
                    numRead = is_.read(b_cache_, 0, cache_.length);
                }
                else if (tableType_ == DB_TABLE)
                {
                    if (isCachedByte_)
                    {
                        numRead = is_.read(b_cache_, 1, cache_.length-1);
                        if (numRead == -1)
                        {
                            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
                            return false;
                        }
                        b_cache_[0] = cachedByte_;
                        if (numRead % 2 == 0)  // Read an even number, need to proliferate the last byte.
                        {
                            cachedByte_ = b_cache_[numRead];
                            isCachedByte_ = true;
                        }
                        else
                        {
                            isCachedByte_ = false;
                        }
                    }
                    else
                    {
                        numRead = is_.read(b_cache_, 0, cache_.length);
                        if (numRead % 2 == 1)  // Read an odd number of characters.
                        {
                            cachedByte_ = b_cache_[numRead-1];
                            isCachedByte_ = true;
                            --numRead;
                        }
                    }
                }
                else if (tableType_ == MB_TABLE)
                {
                    // Max number of bytes for worst-case mixed-byte data scenario is (5x+3)/2.
                    int c = 0;
                    if (mode_ == DB_MODE)
                    {
                        // Begin with a shift-out since we left off in DB_MODE last time.
                        b_cache_[numRead++] = ConvTableMixedMap.shiftOut_;
                        if (isCachedByte_)  // Note that we don't ever cache a shift byte or a single-byte char - we only cache half of a double-byte char.
                        {
                            b_cache_[numRead++] = cachedByte_;
                            isCachedByte_ = false;
                        }
                    }
                    // Don't read too much, we only want to read enough that will fit in our character cache after conversion.
                    int curRead = is_.read(b_cache_, numRead, cache_.length - 1);
                    if (curRead == -1 && numRead == 0)
                    {
                        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
                        return false;  // End-of-stream.
                    }
                    if (curRead > -1) numRead += curRead;

                    // Find out which mode we are in when we stopped reading.
                    boolean needToCache = false;
                    for (int i = 0; i < numRead; ++i)
                    {
                        if (mode_ == SB_MODE)
                        {
                            if (b_cache_[i] == ConvTableMixedMap.shiftOut_) mode_ = DB_MODE;
                        }
                        else
                        {
                            // In DB_MODE.
                            if (b_cache_[i] == ConvTableMixedMap.shiftIn_)
                            {
                                mode_ = SB_MODE;
                                needToCache = false;
                            }
                            else
                            {
                                needToCache = !needToCache;
                            }
                        }
                    }
                    if (mode_ == DB_MODE)
                    {
                        if (needToCache)
                        {
                            cachedByte_ = b_cache_[--numRead];
                            isCachedByte_ = true;
                        }

                        // Need to finish with a shift-in.
                        b_cache_[numRead++] = ConvTableMixedMap.shiftIn_;
                    }
                }
                else if (tableType_ == UTF8_TABLE)
                {
                    // Were there leftovers from the previous read?
                    if (leftoverCount > 0)
                    {
                        // Move the leftovers into the cache prior to reading in any more.
                        System.arraycopy(leftovers, 0, b_cache_, 0, leftoverCount);
                        // Fill in the rest of the cache bytes read from the stream.
                        numRead = is_.read(b_cache_, leftoverCount, cache_.length-leftoverCount);

                        // It's possible that the numRead is -1, we still have to pretend to read the number of bytes indicated by leftoverCount.
                        numRead = numRead == -1 ? leftoverCount : numRead + leftoverCount;
                    }
                    else
                    {
                        // No leftovers, try to fill the entire cache with bytes from the stream.
                        numRead = is_.read(b_cache_, 0, cache_.length);
                    }

                    // If no bytes were read, then we have reached the end.
                    if (numRead == -1)
                    {
                        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
                        return false;
                    }

                    // If fewer bytes were read than requested, can we assume there are no characaters remaining to be read?
                    if (numRead < cache_.length) 
                    {
                        leftoverCount = 0;
                    }
                    else
                    {
                        // This is where we figure out if a utf-8 character (1-4 bytes) is straddling the cache boundary (|)
                        // The leftoverCount is how many bytes we need to carryover to the next read.
                        // case 1:    0xxxxxxx |
                        // case 2:    110xxxxx 10xxxxxx |
                        // case 3:    1110xxxx 10xxxxxx 10xxxxxx |
                        // case 4:    11110xxx 10xxxxxx 10xxxxxx 10xxxxxx |
                        // leftoverCount = 0
                        //
                        // case 5:    110xxxxx |
                        // case 6:    1110xxxx |
                        // case 7:    11110xxx |
                        // leftoverCount = 1
                        //
                        // case 8:    1110xxxx 10xxxxxx |
                        // case 9:    11110xxx 10xxxxxx |
                        // leftoverCount = 2
                        //
                        // case 10:   11110xxx 10xxxxxx 10xxxxxx |
                        // leftoverCount = 3

                        int n = cache_.length - 1;
                        if ((b_cache_[n] & 0x80) == 0 ) 
                        {
                            leftoverCount = 0;  // Case 1.
                        }
                        else if ((b_cache_[n] & 0xC0) == 0xC0)
                        {
                            leftoverCount = 1;  // Case 5, 6, and 7.
                            leftovers[0] = b_cache_[n];
                        }
                        else if ((b_cache_[n-1] & 0xE0) == 0xE0)
                        {
                            leftoverCount = 2;  // Case 8, and 9.
                            System.arraycopy(b_cache_, n-1, leftovers, 0, leftoverCount);
                        }
                        else if ((b_cache_[n-2] & 0xF0) == 0xF0)
                        {
                            leftoverCount = 3;  // Case 10.
                            System.arraycopy(b_cache_, n-2, leftovers, 0, leftoverCount);
                        }
                        else
                        {
                            leftoverCount = 0;  // Case 2, 3, and 4.
                        }
                        // Adjust the numRead, so it appears the the leftovers aren't in the cache yet.
                        numRead -= leftoverCount;
                    }
                }
                else
                {
                    if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unknown table type during conversion: " + tableType_);
                    throw new InternalErrorException(InternalErrorException.UNKNOWN);
                }

                if (numRead == -1)
                {
                    if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Cache not filled, end of stream reached.");
                    return false;
                }

                String s = table_.byteArrayToString(b_cache_, 0, numRead, properties_);
                nextWrite_ = s.length();
                s.getChars(0, nextWrite_, cache_, 0);
                nextRead_ = 0;

                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Filled cache for reader: " + nextRead_ + "," + nextWrite_ + "," + cache_.length, ConvTable.dumpCharArray(cache_, nextWrite_));
            }

            if (nextRead_ >= nextWrite_) // Still didn't read enough, so try again.
            {
                // This should never happen, but the javadoc for InputStream is unclear if the read(byte[],int,int) method will sometimes return 0 or always read at least 1 byte.
                return fillCache();
            }
            return true;
        }
    }

    /**
     Returns the maximum number of bytes that may be stored in the internal buffer.  This number represents the number of bytes that may be read from the underlying InputStream any time a read() method is called on this ConvTableReader.
     @return  The size of the byte cache in use by this ConvTableReader.
     **/
    public int getByteCacheSize()
    {
        return b_cache_.length;
    }

    /**
     Returns the maximum number of characters that may be stored in the internal buffer.  This number represents the number of characters that may be converted from the underlying InputStream any time a read() method is called on this ConvTableReader.  Note that this is not the number of bytes actually read from the InputStream.  The maximum number of bytes that can be read is determined by {@link #getByteCacheSize getByteCacheSize()}.
     @return  The size of the character cache in use by this ConvTableReader.
     **/
    public int getCacheSize()
    {
        return cache_.length;
    }

    /**
     Returns the CCSID used by this ConvTableReader.
     @return  The CCSID, or -1 if the CCSID is not known.
     **/
    public int getCcsid()
    {
        return ccsid_;
    }

    /**
     Returns the encoding used by this ConvTableReader. If the CCSID is not known, the superclass encoding is returned.  Otherwise, the corresponding encoding for the CCSID is returned, which may be null if no such mapping exists.
     @return  The encoding, or null if the encoding is not known.
     **/
    public String getEncoding()
    {
        if (ccsid_ == -1)
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
        if (enc != null)
        {
            String ccsidStr = ConversionMaps.encodingToCcsidString(enc);
            if (ccsidStr != null)
            {
                ccsid_ = Integer.parseInt(ccsidStr);
            }
        }
    }

    private void initializeTable() throws UnsupportedEncodingException
    {
        try
        {
            if (ccsid_ == -1)
            {
                table_ = ConvTable.getTable(getEncoding());
            }
            else
            {
                table_ = ConvTable.getTable(ccsid_, null);
            }
            if (table_ instanceof ConvTableSingleMap || table_ instanceof ConvTableBidiMap || table_ instanceof ConvTableAsciiMap)
            {
                tableType_ = SB_TABLE;
            }
            else if (table_ instanceof ConvTableDoubleMap || table_ instanceof ConvTable1202 || table_ instanceof ConvTableUnicodeBigMap)
            {
                tableType_ = DB_TABLE;
            }
            else if (table_ instanceof ConvTableMixedMap)
            {
                tableType_ = MB_TABLE;
            }
            else if (table_ instanceof ConvTableJavaMap)
            {
                tableType_ = JV_TABLE;
            }
            else if (table_ instanceof ConvTable1208)
            {
                tableType_ = UTF8_TABLE;
            }
            else
            {
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unknown conversion table type: " + table_.getClass());
                throw new InternalErrorException(InternalErrorException.UNKNOWN);
            }
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "ConvTableReader initialized with CCSID " + ccsid_ + ", encoding " + getEncoding() + ", string type " + properties_.getBidiStringType() + ", and table type " + tableType_ + ".");
        }
        catch (UnsupportedEncodingException uee)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "The specified CCSID is not supported in the current JVM nor by the Toolbox: " + ccsid_ + "/" + getEncoding(), uee);
            throw uee;
        }
    }

    private void checkOpen() throws IOException
    {
        if (table_ == null)  // If we are explicitly closed.
        {
            is_.available(); // Will hopefully throw an IOException.
            // If not, we'll throw our own.
            throw new IOException();
        }
    }

    /**
     ConvTableReader does not support the mark() operation.
     @return  false.
     **/
    public boolean markSupported()
    {
        return false;
    }

    /**
     Reads a single character.  If close() is called prior to calling this method, an exception will be thrown.
     @return  The character read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public int read() throws IOException
    {
        synchronized (lock)
        {
            if (fillCache())
            {
                return cache_[nextRead_++];
            }
        }
        return -1;
    }

    /**
     Reads characters into the specified array.  If close() is called prior to calling this method, an exception will be thrown.
     @param  buffer  The destination buffer.
     @return  The number of characters read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public int read(char[] buffer) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }
        if (buffer.length == 0) return 0;
        synchronized (lock)
        {
            if (fillCache())
            {
                int max = buffer.length > (nextWrite_ - nextRead_) ? (nextWrite_ - nextRead_) : buffer.length;
                System.arraycopy(cache_, nextRead_, buffer, 0, max);
                nextRead_ += max;
                return max;
            }
        }
        return -1;
    }

    /**
     Reads characters into a portion of the specified array.  If close() is called prior to calling this method, an exception will be thrown.
     @param  buffer  The destination buffer.
     @param  offset  The offset into the buffer at which to begin storing data.
     @param  length  The maximum number of characters to store.  If a zero length is specified, this method does nothing and returns 0.
     @return  The number of characters read, or -1 if the end of the stream has been reached.
     @exception  IOException  If an I/O exception occurs.
     **/
    public int read(char[] buffer, int offset, int length) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }
        if (length == 0) return 0; // The JDK doesn't throw exceptions when the length is 0.
        if (offset < 0 || offset >= buffer.length)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'offset' is not valid:", offset);
            throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length < 0 || (offset + length) > buffer.length)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        synchronized (lock)
        {
            if (fillCache())
            {
                int max = length > (nextWrite_ - nextRead_) ? (nextWrite_ - nextRead_) : length;
                System.arraycopy(cache_, nextRead_, buffer, offset, max);
                nextRead_ += max;
                return max;
            }
        }
        return -1;
    }

    /**
     Reads up to <tt>length</tt> characters out of the underlying stream.  If close() is called prior to calling this method, an exception will be thrown.
     @param  length  The maximum number of Unicode characters to return as a String.  Note that the number of <i>bytes</i> read from the underlying InputStream could be greater than <tt>length</tt>.  If a zero length is specified, this method does nothing and returns "".
     @return  A String of up to <tt>length</tt> Unicode characters, or null if the end of the stream has been reached.  The actual number of characters returned may be less than the specified <tt>length</tt> if the end of the underlying InputStream is reached while reading.
     @exception  IOException  If an I/O exception occurs.
     **/
    public String read(int length) throws IOException
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length == 0) return "";
        synchronized (lock)
        {
            StringBuffer buf = new StringBuffer();
            if (fillCache())
            {
                while (fillCache() && buf.length() < length)
                {
                    buf.append(cache_, nextRead_++, 1);
                }
                return buf.toString();
            }
        }
        return null;
    }

    /**
     Tells whether this ConvTableReader is ready to be read.  A ConvTableReader is ready if its input buffer is not empty or if bytes are available to be read from the underlying input stream.  If close() is called, a call to ready() will always return false.
     @return  true if the ConvTableReader is ready to read characters; false otherwise.
     @exception  IOException  If an I/O exception occurs.
     **/
    public boolean ready() throws IOException
    {
        synchronized (lock)
        {
            if (table_ == null) // We are closed.
            {
                return super.ready();  // This should throw an IOException.
            }
            return (nextRead_ < nextWrite_) || is_.available() > 0;
        }
    }

    /**
     Skips the specified number of characters in the underlying stream.  If close() is called prior to calling this method, an exception will be thrown.
     @param  length  The number of characters to skip.
     @return  The number of characters actually skipped.
     @exception  IOException  If an I/O exception occurs.
     **/
    public long skip(long length) throws IOException
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid: " + length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length == 0) return 0;
        long total = 0;
        synchronized (lock)
        {
            // Note: We can't just call is_.skip(length), since that would just skip 'length' bytes, but we need to skip 'length' characters.
            checkOpen();
            char[] buf = new char[length < cache_.length ? (int)length : cache_.length];
            int r = read(buf);
            total += r;
            while (r > 0 && total < length)
            {
                r = read(buf);
                if (r > 0) total += r;
            }
        }
        return total;
    }
}
