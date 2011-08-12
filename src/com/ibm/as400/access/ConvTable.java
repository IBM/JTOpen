///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

// Internal class representing a character set conversion table.
abstract class ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    final static char cic_ = '\uFFFF';  // Used for decompression.
    final static char ric_ = '\uFFFE';  // Used for decompression.
    final static char hbic_ = '\u0000';  // Used for decompression.
    final static char pad_ = '\u0000';  // Used for decompression.
    final static byte sbSubChar_ =  0x003F;  // Single-byte EBCDIC substitution character.
    final static char dbSubChar_ = '\uFEFE'; // Double-byte EBCDIC substitution character.
    final static char sbSubUnic_ = '\u001A'; // Single-byte Unicode substitution character.
    final static char dbSubUnic_ = '\uFFFD'; // Double-byte Unicode substitution character.
    final static char euro_ = '\u20AC'; // Euro character.

    String encoding_;
    int ccsid_ = -1;
    int bidiStringType_ = BidiStringType.DEFAULT;
    int clientBidiStringType = BidiStringType.DEFAULT;	//@Bidi-HCG3

    // The highest number of all our supported CCSIDs.  There's no point in making the pool larger than it needs to be.  We only have a handful of CCSIDs in the 62000 range, so we could use a smaller number to save space and those CCSIDs outside the range just wouldn't get cached.  However, 61952 is used extensively, so we might as well max it out.
    private static final int LARGEST_CCSID = 62251;
    private static final ConvTable[] ccsidPool_ = new ConvTable[LARGEST_CCSID + 1];
    private static final Hashtable converterPool_ = new Hashtable();
    private static final String prefix_ = "com.ibm.as400.access.ConvTable";

    ConvTable(int ccsid)
    {
        ccsid_ = ccsid;
        encoding_ = ConversionMaps.ccsidToEncoding(ccsid_);
        if (encoding_ == null) encoding_ = "" + ccsid_;

        if (Trace.traceOn_)
        {
            Trace.log(Trace.CONVERSION, "Constructing conversion table for ccsid/encoding: " + ccsid_ + "/" + encoding_);
            if (ccsid_ == 0) // See ConvTableJavaMap.
            {
                if (this instanceof ConvTableJavaMap)
                {
                    Trace.log(Trace.CONVERSION, "This table is a wrapper around a Java table.");
                }
                else
                {
                    Trace.log(Trace.CONVERSION, "Warning: 0 specified for CCSID when table is not a Java table.");
                }
            }
        }
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    String byteArrayToString(byte[] source, int offset, int length, int type)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(type));
    }

    abstract String byteArrayToString(byte[] source, int offset, int length, BidiConversionProperties properties);

    // This method can be overridden by subclasses for better performance.
    String byteArrayToString(byte[] source, int offset, int length)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(bidiStringType_));
    }

    // Helper method used to decompress conversion tables when they are initialized.  Note that this method also converts the char[] into a byte[] since these are single-byte tables.
    byte[] decompressSB(char[] arr, byte subPad)
    {
        byte[] buf = new byte[65536];
        int c = 0;

        for (int i = 0; i < arr.length; ++i)
        {
            if (arr[i] == cic_)
            {
                if (arr.length > i+1 && arr[i+1] == pad_)
                {
                    buf[c++] = (byte)(arr[i] / 256);
                    buf[c++] = (byte)(arr[i++] % 256);
                }
                else
                {
                    long max = (0xFFFF & arr[i + 1] * 2) + (0xFFFF & c);
                    char ch = arr[i + 2];
                    while (c < max)
                    {
                        buf[c++] = (byte)(ch / 256);
                        buf[c++] = (byte)(ch % 256);
                    }
                    i = i + 2;
                }
            }
            else if (arr[i] == ric_)
            {
                if (arr.length > i + 1 && arr[i + 1] == pad_)
                {
                    buf[c++] = (byte)(arr[i] / 256);
                    buf[c++] = (byte)(arr[i++] % 256);
                }
                else
                {
                    int start = (0xFFFF & arr[i + 2]);
                    int num = (0xFFFF & arr[i + 1]);
                    for (int j = start; j < (num + start); ++j)
                    {
                        buf[c++] = (byte)(j / 256);
                        buf[c++] = (byte)(j % 256);
                    }
                    i = i + 2;
                }
            }
            else if (arr[i] == hbic_)
            {
                if (arr.length > i + 1 && arr[i + 1] == pad_)
                {
                    buf[c++] = (byte)(arr[i] / 256);
                    buf[c++] = (byte)(arr[i++] % 256);
                }
                else
                {
                    int hbNum = (0x0000FFFF & arr[++i]);
                    char firstChar = arr[++i];
                    char highByteMask = (char)(0xFF00 & firstChar);
                    buf[c++] = (byte)(firstChar / 256);
                    buf[c++] = (byte)(firstChar % 256);
                    ++i;
                    for (int j = 0; j < hbNum; ++j)
                    {
                        char both = arr[i + j];
                        char c1 = (char)(highByteMask + ((0xFF00 & both) >>> 8));
                        char c2 = (char)(highByteMask + (0x00FF & both));
                        buf[c++] = (byte)(c1 / 256);
                        buf[c++] = (byte)(c1 % 256);
                        buf[c++] = (byte)(c2 / 256);
                        buf[c++] = (byte)(c2 % 256);
                    }
                    i = i + hbNum - 1;
                }
            }
            else
            {
                // Regular character.
                buf[c++] = (byte)(arr[i] / 256);
                buf[c++] = (byte)(arr[i] % 256);
            }
        }
        for (int i = c; i < buf.length; ++i)
        {
            buf[i] = subPad;
        }
        return buf;
    }

    // Convenience function for tracing character strings.
    static final byte[] dumpCharArray(char[] charArray, int offset, int length)
    {
        byte[] retData = new byte[length * 2];
        int inPos = offset;
        int outPos = 0;
        while(inPos < length)
        {
            retData[outPos++] = (byte)(charArray[inPos] >> 8);
            retData[outPos++] = (byte)charArray[inPos++];
        }
        return retData;
    }

    static final byte[] dumpCharArray(char[] charArray, int numChars)
    {
        return dumpCharArray(charArray, 0, numChars);
    }

    static final byte[] dumpCharArray(char[] charArray)
    {
        return dumpCharArray(charArray, charArray.length);
    }

    // Returns the ccsid of this conversion object.
    // @return  The ccsid.
    int getCcsid()
    {
        return ccsid_;
    }

    // Returns the encoding of this conversion object.
    // @return  The encoding.
    String getEncoding()
    {
        return encoding_;
    }

    // Factory method for finding appropriate table based on encoding name.
    static final ConvTable getTable(String encoding) throws UnsupportedEncodingException
    {
        String className = (NLS.forceJavaTables_) ? encoding : prefix_ + ConversionMaps.encodingToCcsidString(encoding);

        // First, see if we've already loaded the table.
        ConvTable newTable = (ConvTable)converterPool_.get(className);
        if (newTable != null)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Reusing previously loaded conversion table for encoding: " + encoding);
            return newTable;
        }

        // If we haven't, then we need to load it now.
        try
        {
            if (NLS.forceJavaTables_)
            {
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "User set to force loading Java tables.");
                throw new CharConversionException();
            }
            newTable = (ConvTable)Class.forName(className).newInstance();
        }
        catch (Throwable e)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Could not load conversion table class for encoding: " + encoding + ". Will attempt to let Java do the conversion.", e);
            // Need to load a JavaMap.
            className = encoding;
            newTable = (ConvTable)converterPool_.get(className);
            if (newTable != null)
            {
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Reusing previously loaded Java conversion table for encoding: " + encoding);
                return newTable;
            }
            // It's not cached, so we can try to instantiate one.
            newTable = new ConvTableJavaMap(encoding);
        }

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Successfully loaded conversion table for encoding: " + encoding);
        converterPool_.put(className, newTable);
        return newTable;
    }

    // Factory method for finding appropriate table based on ccsid number.  System may be null if no system was provided.
    static final ConvTable getTable(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
        ccsid = ccsid & 0x00FFFF; // Remove sign-extended shorts that JDBC gives us.

        if (ccsid <= LARGEST_CCSID)  //If it's negative, too bad...
        {
            ConvTable cachedTable = ccsidPool_[ccsid];
            if (cachedTable != null) return cachedTable;
        }

        String className = null;
        if (NLS.forceJavaTables_)
        {
            className = ConversionMaps.ccsidToEncoding(ccsid);
            if (className == null) className = "";
        }
        else
        {
            className = prefix_ + String.valueOf(ccsid);
        }

        // First, see if we've already loaded the table.
        ConvTable newTable = (ConvTable)converterPool_.get(className);
        if (newTable != null)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Reusing previously loaded conversion table for ccsid: " + ccsid);
            if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable;
            return newTable;
        }

        // If we haven't, then we need to load it now.
        try
        {
            if (NLS.forceJavaTables_)
            {
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "User set to force loading Java tables.");
                throw new CharConversionException();
            }
            newTable = (ConvTable)Class.forName(className).newInstance();
        }
        catch (Throwable e)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Could not load conversion table class for ccsid: " + ccsid + ". Will attempt to let Java do the conversion.", e);
            // Need to load a JavaMap.
            className = ConversionMaps.ccsidToEncoding(ccsid);
            if (className == null)
            {
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Could not find an encoding that matches ccsid: " + ccsid);
                throw new UnsupportedEncodingException("CCSID " + ccsid);
            }
            newTable = (ConvTable)converterPool_.get(className);
            if (newTable != null)
            {
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Reusing previously loaded Java conversion table for ccsid: " + ccsid);
                if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable;
                return newTable;
            }
            // It's not cached, so we can try to instantiate one.
            newTable = new ConvTableJavaMap(className);
        }

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Successfully loaded conversion table for ccsid: " + ccsid);
        converterPool_.put(className, newTable);
        if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable;
        
        if(system != null)											//@Bidi-HCG3        
        	newTable.clientBidiStringType = system.getBidiStringType();//@Bidi-HCG3
        
        return newTable;
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    byte[] stringToByteArray(String source, int type)
    {
        return stringToByteArray(source, new BidiConversionProperties(type));
    }
    abstract byte[] stringToByteArray(String source, BidiConversionProperties properties);

    // This method can be overridden by subclasses for better performance.
    byte[] stringToByteArray(String source)
    {
        return stringToByteArray(source, new BidiConversionProperties(bidiStringType_));
    }

    // Subclasses should override this to avoid creating superflous String objects and char arrays.
    byte[] stringToByteArray(char[] source, int offset, int length)
    {
        return stringToByteArray(new String(source, offset, length));
    }

    // Subclasses should override this to avoid creating superfluous byte arrays.
    void stringToByteArray(String source, byte[] buf, int offset) throws CharConversionException
    {
        byte[] b = stringToByteArray(source, new BidiConversionProperties(bidiStringType_));
        try
        {
            System.arraycopy(b, 0, buf, offset, b.length);
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
    }

    // This method can be overridden by subclasses for better performance.
    void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException
    {
        stringToByteArray(source, buf, offset, length, new BidiConversionProperties(bidiStringType_));
    }

    // Subclasses should override this to avoid creating superfluous byte arrays.
    void stringToByteArray(String source, byte[] buf, int offset, int length, int type) throws CharConversionException
    {
        stringToByteArray(source, buf, offset, length, new BidiConversionProperties(type));
    }

    int stringToByteArray(String source, byte[] buf, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        int truncated = 0; //@trnc
        byte[] b = stringToByteArray(source, properties);
        if (length > b.length) 
            length = b.length;
        else if (length < stringToByteArray(source.trim(), properties).length){ //@trnc
            truncated = b.length - length; //@trnc
        }

        try
        {
            System.arraycopy(b, 0, buf, offset, length);
            return truncated;  //@trnc
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            Trace.log(Trace.CONVERSION, "Source length: " + b.length + "; Source offset: 0; Destination length: " + buf.length + "; Destination offset: " + offset + "; Number of bytes to copy: " + length, aioobe);
            throw new CharConversionException();
        }
    }
}
