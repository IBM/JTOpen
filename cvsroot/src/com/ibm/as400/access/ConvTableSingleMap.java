///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableSingleMap.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;

// This is the parent class for all ConvTableXXX classes that represent single-byte ccsids.
abstract class ConvTableSingleMap extends ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    char[] toUnicode_ = null;
    byte[] fromUnicode_ = null;

    // Constructor.
    ConvTableSingleMap(int ccsid, char[] toUnicode, char[] fromUnicode)
    {
        super(ccsid);
        ccsid_ = ccsid;
        toUnicode_ = toUnicode;
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Decompressing single-byte conversion table for ccsid: " + ccsid_, fromUnicode.length);
        //Moved decompression algorithm to parent.
        fromUnicode_ = decompressSB(fromUnicode, (byte)0x3F);
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Successfully loaded single-byte map for ccsid: " + ccsid_);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] dest = new char[length];
        // The 0x00FF is so we don't get any negative indices.
        for (int i = 0; i < length; dest[i] = toUnicode_[0x00FF & buf[offset + (i++)]]);
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest);
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        // Call char[] method.
        return stringToByteArray(src, 0, src.length);
    }

    final byte[] stringToByteArray(char[] src, int offset, int length)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, offset, length));
        byte[] dest = new byte[length];
        for (int i = offset; i < length; dest[i] = fromUnicode_[src[i++]]);
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
        return dest;
    }

    final void stringToByteArray(String source, byte[] buf, int offset) throws CharConversionException
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        try
        {
            for (int i = 0; i < src.length; buf[i + offset] = fromUnicode_[src[i++]]);
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, src.length);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        try
        {
            for (int i = 0; i < src.length && i < length; buf[i + offset] = fromUnicode_[src[i++]]);
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, src.length);
    }
}
