///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableWriter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 A ConvTableWriter represents a Toolbox converter that uses stateful character conversion.  That is, it wraps an underlying OutputStream and caches/writes the appropriate number of bytes for the given Unicode characters and CCSID/encoding.  This is especially useful when converting Strings and characters to a mixed-byte CCSID, and you don't want to have to keep track of which shift state the conversion stream is in between writes.
 @see  ConvTableReader
 **/
public class ConvTableWriter extends OutputStreamWriter
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    private BufferedOutputStream os_ = null;

    private int ccsid_ = -1;
    private ConvTable table_ = null;

    private BidiConversionProperties properties_ = new BidiConversionProperties();

    // The table type, based on the instance of the ConvTable.
    private boolean isMixedByte_ = false;

    private char[] cache_ = new char[1024];  // The character cache.
    private boolean isCachedByte_ = false;  // Used for double-byte tables.
    private byte cachedByte_ = 0;  // Used for double-byte tables.

    private int nextWrite_ = 0;

    /**
     Creates a ConvTableWriter that uses the default character encoding.  The CCSID this writer uses may be set if a known mapping exists for this platform's default character encoding.
     @param  out  The OutputStream to which to write characters.
     @exception  UnsupportedEncodingException  If the default character encoding or its associated CCSID is not supported.
     **/
    public ConvTableWriter(OutputStream out) throws UnsupportedEncodingException
    {
        super(out);
        os_ = new BufferedOutputStream(out);
        initializeCcsid();
        initializeTable();
    }

    /**
     Creates a ConvTableWriter that uses the specified character encoding.  The CCSID this writer uses may be set if a known mapping exists for the given encoding.
     @param  out  The OutputStream to which to write characters.
     @param  encoding  The name of a supported character encoding.
     @exception  UnsupportedEncodingException  If the specified character encoding or its associated CCSID is not supported.
     **/
    public ConvTableWriter(OutputStream out, String encoding) throws UnsupportedEncodingException
    {
        super(out, encoding);
        os_ = new BufferedOutputStream(out);
        initializeCcsid();
        initializeTable();
    }

    /**
     Creates a ConvTableWriter that uses the specified CCSID.
     @param  out  The OutputStream to which to write characters.
     @param  ccsid  The CCSID.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableWriter(OutputStream out, int ccsid) throws UnsupportedEncodingException
    {
        super(out);
        if (ccsid < 0 || ccsid > 65535)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        os_ = new BufferedOutputStream(out);
        ccsid_ = ccsid;
        initializeTable();
    }

    /**
     Creates a ConvTableWriter that uses the specified CCSID and bi-directional string type.
     @param  out  The OutputStream to which to write characters.
     @param  ccsid  The CCSID.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableWriter(OutputStream out, int ccsid, int bidiStringType) throws UnsupportedEncodingException
    {
        this(out, ccsid, new BidiConversionProperties(bidiStringType));
    }

    /**
     Creates a ConvTableWriter that uses the specified CCSID and bi-directional string type.
     @param  out  The OutputStream to which to write characters.
     @param  ccsid  The CCSID.
     @param  properties  The bidi conversion properties.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableWriter(OutputStream out, int ccsid, BidiConversionProperties properties) throws UnsupportedEncodingException
    {
        super(out);
        if (ccsid < 0 || ccsid > 65535)
        {
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        os_ = new BufferedOutputStream(out);
        ccsid_ = ccsid;
        properties_ = properties;
        initializeTable();
    }

    /**
     Creates a ConvTableWriter that uses the specified CCSID, bi-directional string type, and internal cache size.
     @param  out  The OutputStream to which to write characters.
     @param  ccsid  The CCSID.
     @param  bidiStringType  The {@link com.ibm.as400.access.BidiStringType bi-directional string type}.
     @param  cacheSize  The number of characters to store in the internal buffer. The default is 1024.  This number must be greater than zero.
     @exception  UnsupportedEncodingException  If the specified CCSID or its corresponding character encoding is not supported.
     **/
    public ConvTableWriter(OutputStream out, int ccsid, int bidiStringType, int cacheSize) throws UnsupportedEncodingException
    {
        this(out, ccsid, bidiStringType);
        if (cacheSize < 1) throw new ExtendedIllegalArgumentException("cacheSize", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        cache_ = new char[cacheSize];
    }

    private void addToCache(char c) throws IOException
    {
        synchronized (lock)
        {
            checkOpen();
            if (nextWrite_ == cache_.length)
            {
                flush();
            }
            cache_[nextWrite_++] = c;
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Added to ConvTableWriter(" + this.toString() + ") cache: " + c + "," + nextWrite_ + "," + cache_.length, ConvTable.dumpCharArray(cache_, nextWrite_));
        }
    }

    private void addToCache(char[] c, int off, int len) throws IOException
    {
        if (len == 0) return;  // nothing to cache
        synchronized (lock)
        {
            checkOpen();
            int reps = len / cache_.length;
            for (int i = 0; i < reps; ++i)
            {
                flush();
                System.arraycopy(c, off+(i*cache_.length), cache_, 0, cache_.length);
                nextWrite_ = cache_.length;
            }
            int leftover = len % cache_.length;
            flush();
            System.arraycopy(c, off+(reps*cache_.length), cache_, 0, leftover);
            nextWrite_ = leftover;
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Added to ConvTableWriter(" + this.toString() + ") cache: " + off + "," + len + "," + nextWrite_ + "," + cache_.length, ConvTable.dumpCharArray(cache_, nextWrite_));
        }
    }

    private void checkOpen() throws IOException
    {
        if (table_ == null)  // If we are explicitly closed.
        {
            super.flush();  // Will hopefully throw an IOException.
            // If not, we'll throw our own.
            throw new IOException();
        }
    }

    /**
     Closes this ConvTableWriter and its underlying output stream.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void close() throws IOException
    {
        synchronized (lock)
        {
            if (table_ == null) return;  // We are already closed.
            flush();
            if (isCachedByte_)
            {
                // We were saving the last byte in case we had another mixed byte conversion to do.
                // Write it out now since the user wants us to close.
                os_.write(cachedByte_);
                os_.flush();
            }
            table_ = null;
            cache_ = null;
            super.close();
            os_.close();
        }
    }

    /**
     Flushes the underlying output stream.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void flush() throws IOException
    {
        synchronized (lock)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Flushing cache for ConvTableWriter(" + this.toString() + ") with size " + nextWrite_ + ".");
            if (nextWrite_ > 0 && table_ != null)  // In case we've been closed.
            {
                String s = new String(cache_, 0, nextWrite_);
                nextWrite_ = 0;

                byte[] b = table_.stringToByteArray(s, properties_);

                if (isMixedByte_) // Mixed-byte tables only.
                {
                    // Always save off the last byte in b for the next time around.
                    if (cachedByte_ == ConvTableMixedMap.shiftIn_ && b[0] == ConvTableMixedMap.shiftOut_)
                    {
                        os_.write(b, 1, b.length - 2);
                        cachedByte_ = b[b.length - 1];
                        isCachedByte_ = true;
                    }
                    else
                    {
                        if (isCachedByte_)
                        {
                            os_.write(cachedByte_);
                        }
                        os_.write(b, 0, b.length - 1);
                        cachedByte_ = b[b.length - 1];
                        isCachedByte_ = true;
                    }
                }
                else  // All other table types... single, double, java.
                {
                    // Don't have any shift characters for these table types, so we don't need to cache anything for the next time around.
                    os_.write(b);
                }
                os_.flush();
            }
            super.flush(); // this will throw our IOException if we've been closed
        }
    }

    /**
     Returns the CCSID used by this ConvTableWriter.
     @return  The CCSID, or -1 if the CCSID is not known.
     **/
    public int getCcsid()
    {
        return ccsid_;
    }

    /**
     Returns the encoding used by this ConvTableWriter.  If the CCSID is not known, the superclass encoding is returned. Otherwise, the corresponding encoding for the CCSID is returned, which may be null if no such mapping exists.
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
            if (table_ instanceof ConvTableMixedMap)
            {
                isMixedByte_ = true;
            }
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "ConvTableWriter initialized with CCSID " + ccsid_ + ", encoding " + getEncoding() + ", string type " + properties_.getBidiStringType() + ", and table type " + isMixedByte_ + ".");
        }
        catch(UnsupportedEncodingException uee)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "The specified CCSID is not supported in the current JVM nor by the Toolbox: " + ccsid_ + "/" + getEncoding(), uee);
            throw uee;
        }
    }

    /**
     Writes a single character.
     @param  c  The character to write.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void write(int c) throws IOException
    {
        addToCache((char)c);
    }

    /**
     Writes the specified array of characters.
     @param  buffer  The characters to be written.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void write(char[] buffer) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }
        addToCache(buffer, 0, buffer.length);
    }

    /**
     Writes a portion of the specified array of characters.
     @param  buffer  The characters to be written.
     @param  offset  The offset into the array from which to begin extracting characters to write.
     @param  length  The number of characters to write.  If zero is specified, this method does nothing.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void write(char[] buffer, int offset, int length) throws IOException
    {
        if (buffer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'buffer' is null.");
            throw new NullPointerException("buffer");
        }
        if (length == 0) return;  // nothing to write
        if (offset < 0 || offset >= buffer.length)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'offset' is not valid:", offset);
            throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length < 0 || length > buffer.length)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        addToCache(buffer, offset, length);
    }

    /**
     Writes the specified String.
     @param  data  The String to write.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void write(String data) throws IOException
    {
        if (data == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'data' is null.");
            throw new NullPointerException("data");
        }
        addToCache(data.toCharArray(), 0, data.toCharArray().length);
    }

    /**
     Writes a portion of the specified String.
     @param  data  The String to write.
     @param  offset  The offset into the String from which to begin extracting characters to write.
     @param  length  The number of characters to write.  If zero is specified, this method does nothing.
     @exception  IOException  If an I/O exception occurs.
     **/
    public void write(String data, int offset, int length) throws IOException
    {
        if (data == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'data' is null.");
            throw new NullPointerException("data");
        }
        if (length == 0) return;  // nothing to write
        if (offset < 0 || offset >= data.length())
        {
            Trace.log(Trace.ERROR, "Value of parameter 'offset' is not valid:", offset);
            throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (length < 0 || length > data.length())
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        addToCache(data.toCharArray(), offset, length);
    }
}
