///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableBidiMap.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// This is the parent class for all ConvTableXXX classes that represent bidi ccsids.
abstract class ConvTableBidiMap extends ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    char[] toUnicode_ = null;
    byte[] fromUnicode_ = null;

    // Constructor.
    ConvTableBidiMap(int ccsid, char[] toUnicode, char[] fromUnicode)
    {
        super(ccsid);
        ccsid_ = ccsid;
        toUnicode_ = toUnicode;
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Decompressing bidi single-byte conversion table for ccsid: " + ccsid_, fromUnicode.length);
        fromUnicode_ = decompressSB(fromUnicode, (byte)0x3F);

        bidiStringType_ = AS400BidiTransform.getStringType(ccsid);

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Successfully loaded bidi single-byte map for ccsid: " + ccsid_);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        int type = properties.getBidiStringType();
        if (Trace.traceOn_)
        {
            Trace.log(Trace.CONVERSION, "Bidi String Type: " + type);
            Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        }

        char[] dest = new char[length];
        // The 0x00FF is so we don't get any negative indices.
        for (int i = 0; i < length; dest[i] = toUnicode_[0x00FF & buf[offset + (i++)]]);
        if (type == BidiStringType.NONE)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string (no java layout was applied) for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
            return String.copyValueOf(dest);
        }

        AS400BidiTransform abt = new AS400BidiTransform(ccsid_);
        if (type == BidiStringType.DEFAULT && bidiStringType_ != BidiStringType.DEFAULT)
        {
            properties.setBidiStringType(bidiStringType_);
        }
        abt.setBidiConversionProperties(properties);

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination string (before java layout was applied) for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));

        return abt.toJavaLayout(String.copyValueOf(dest));
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        int type = properties.getBidiStringType();
        char[] src = null;
        if (type == BidiStringType.NONE)
        {
            src = source.toCharArray();
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Converting string to byte array (no layout applied) for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        }
        else
        {
            AS400BidiTransform abt = new AS400BidiTransform(ccsid_);
            if (type == BidiStringType.DEFAULT && bidiStringType_ != BidiStringType.DEFAULT)
            {
                properties.setBidiStringType(bidiStringType_);
            }
            abt.setBidiConversionProperties(properties);
            src = abt.toAS400Layout(source).toCharArray();
            if (Trace.traceOn_)
            {
                Trace.log(Trace.CONVERSION, "Bidi String Type: " + type);
                Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
            }
        }
        byte[] dest = new byte[src.length];
        for (int i = 0; i < src.length; dest[i] = fromUnicode_[src[i++]]);

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
        return dest;
    }
}
