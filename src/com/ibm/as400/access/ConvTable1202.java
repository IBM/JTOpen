///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable1202.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// The 1202 ccsid is just little endian Unicode (13488) so all we do is flip the high and low bytes.  This used to be CCSID 1200.
class ConvTable1202 extends ConvTable // Instead of ConvTableDoubleMap.
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable1202()
    {
        super(1202);
    }

    // Perform a CCSID to Unicode conversion.
    String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] dest = new char[length / 2];
        for (int i = 0; i < length / 2; ++i)
        {
            dest[i] = (char)(((0x00FF & buf[(i * 2) + 1 + offset]) << 8) + (0x00FF & buf[(i * 2) + offset]));
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest);
    }

    // Perform a Unicode to CCSID conversion.
    byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + encoding_, ConvTable.dumpCharArray(src));
        byte[] dest = new byte[src.length * 2];
        for (int i = 0; i < src.length; ++i)
        {
            dest[i * 2 + 1] = (byte)(src[i] >>> 8);
            dest[i * 2] = (byte)(0x00FF & src[i]);
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
        return dest;
    }
}
