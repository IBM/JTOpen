///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable13488.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;

class ConvTable13488 extends ConvTable  // Instead of ConvTableDoubleMap.
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable13488()
    {
        super(13488);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] dest = new char[length / 2];
        for (int i = 0; i < length / 2; ++i)
        {
            dest[i] = (char)(((0x00FF & buf[(i*2)+offset]) << 8) + (0x00FF & buf[(i*2)+1+offset]));
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest);
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        return stringToByteArray(src, 0, src.length);
    }

    final byte[] stringToByteArray(char[] src, int offset, int length)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, offset, length));
        byte[] dest = new byte[length * 2];
        for (int i = offset; i < length; ++i)
        {
            dest[i*2] = (byte)(src[i] >>> 8);
            dest[i*2+1] = (byte)(0x00FF & src[i]);
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
            for (int i = 0; i < src.length; ++i)
            {
                buf[i * 2 + offset] = (byte)(src[i] >>> 8);
                buf[i * 2 + 1 + offset] = (byte)(0x00FF & src[i]);
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, src.length*2);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        try
        {
            for (int i = 0; i < src.length && i < length; ++i)
            {
                buf[i * 2 + offset] = (byte)(src[i] >>> 8);
                buf[i * 2 + 1 + offset] = (byte)(0x00FF & src[i]);
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, length);
    }

    final void stringToByteArray(String source, byte[] buf, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        try
        {
            for (int i = 0; i < src.length && i < length; ++i)
            {
                buf[i * 2 + offset] = (byte)(src[i] >>> 8);
                buf[i * 2 + 1 + offset] = (byte)(0x00FF & src[i]);
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, length);
    }
}
