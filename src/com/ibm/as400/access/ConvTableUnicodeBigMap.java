///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableUnicodeBigMap.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;

public class ConvTableUnicodeBigMap extends ConvTable
{
    ConvTableUnicodeBigMap(int ccsid)
    {
        super(ccsid);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, int type)
    {
        return byteArrayToString(buf, offset, length);
    }

    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        return byteArrayToString(buf, offset, length);
    }

    final String byteArrayToString(byte[] buf, int offset, int length)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] dest = new char[length / 2];
        for (int destPos = 0, bufPos = offset; destPos < dest.length; ++destPos)
        {
            dest[destPos] = (char)(((buf[bufPos++] & 0xFF) << 8) + (buf[bufPos++] & 0xFF));
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest);
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, int type)
    {
        char[] src = source.toCharArray();
        return stringToByteArray(src, 0, src.length);
    }

    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        return stringToByteArray(src, 0, src.length);
    }

    final byte[] stringToByteArray(char[] src, int offset, int length)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, offset, length));
        byte[] dest = new byte[length * 2];
        for (int destPos = 0, srcPos = offset; srcPos < length; ++srcPos)
        {
            dest[destPos++] = (byte)(src[srcPos] >>> 8);
            dest[destPos++] = (byte)src[srcPos];
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
        return dest;
    }

    final void stringToByteArray(String source, byte[] buf, int offset) throws CharConversionException
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        try
        {
            for (int bufPos = offset, srcPos = 0; srcPos < src.length; ++srcPos)
            {
                buf[bufPos++] = (byte)(src[srcPos] >>> 8);
                buf[bufPos++] = (byte)src[srcPos];
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Trace.log(Trace.ERROR, "Source length: " + src.length + "; Source offset: 0; Destination length: " + buf.length + "; Destination offset: " + offset + ";", e);
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, src.length*2);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException
    {
        char[] src = source.toCharArray();
        int copyLength = Math.min(src.length, length / 2);
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, 0, copyLength));
        try
        {
            for (int bufPos = offset, srcPos = 0; srcPos < copyLength; ++srcPos)
            {
                buf[bufPos++] = (byte)(src[srcPos] >>> 8);
                buf[bufPos++] = (byte)src[srcPos];
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Trace.log(Trace.CONVERSION, "Source length: " + src.length + "; Source offset: 0; Destination length: " + buf.length + "; Destination offset: " + offset + "; Number of bytes to copy: " + length, e);
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, copyLength * 2);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length, int type) throws CharConversionException
    {
        stringToByteArray(source, buf, offset, length);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        stringToByteArray(source, buf, offset, length);
    }
}
