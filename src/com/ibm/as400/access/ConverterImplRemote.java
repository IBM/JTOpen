///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConverterImplRemote.java
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

class ConverterImplRemote implements ConverterImpl
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable table_;

    ConverterImplRemote()
    {
    }

    ConverterImplRemote(ConvTable table)
    {
        table_ = table;
    }

    String byteArrayToString(byte[] source)
    {
        return byteArrayToString(source, 0, source.length, new BidiConversionProperties(getStringType(getCcsid())));
    }

    String byteArrayToString(byte[] source, int offset)
    {
        return byteArrayToString(source, offset, source.length - offset, new BidiConversionProperties(getStringType(getCcsid())));
    }

    public String byteArrayToString(byte[] source, int offset, int length)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(getStringType(getCcsid())));
    }                                                                     

    public String byteArrayToString(byte[] source, int offset, int length, int type)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(type));
    }

    public String byteArrayToString(byte[] source, int offset, int length, BidiConversionProperties properties)
    {
        return table_.byteArrayToString(source, offset, length, properties);
    }

    static ConverterImplRemote getConverter(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
        return new ConverterImplRemote(ConvTable.getTable(ccsid, system));
    }

    public String getEncoding()
    {
        return table_.getEncoding();
    }

    public int getCcsid()
    {
        return table_.getCcsid();
    }

    // Returns the string type of the text based on the ccsid.  If the text is bidi, then the string type will be set accordingly.  If not, the default it LTR (left-to-right) text.
    // @return  The string type.
    int getStringType(int ccsid)
    {
        if (AS400BidiTransform.isBidiCcsid(ccsid))
        {
            return AS400BidiTransform.getStringType(ccsid);
        }
        else
        {
            return BidiStringType.DEFAULT;
        }
    }

    public void setCcsid(int ccsid, AS400Impl system) throws UnsupportedEncodingException
    {
        table_ = ConvTable.getTable(ccsid, null);
    }

    public void setEncoding(String encoding) throws UnsupportedEncodingException
    {
        table_ = ConvTable.getTable(encoding);
    }

    public byte[] stringToByteArray(String source)
    {
        return stringToByteArray(source, new BidiConversionProperties(getStringType(getCcsid())));
    }

    public byte[] stringToByteArray(String source, int type)
    {
        return stringToByteArray(source, new BidiConversionProperties(type));
    }

    public byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        return table_.stringToByteArray(source, properties);
    }

    void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
        byte[] convertedBytes = stringToByteArray(source, new BidiConversionProperties(getStringType(getCcsid())));
        if (convertedBytes.length > destination.length)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, 0, destination.length);
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: " + source.length()+"," + destination.length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, 0, convertedBytes.length);
    }

    void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
        byte[] convertedBytes = stringToByteArray(source, new BidiConversionProperties(getStringType(getCcsid())));
        if (convertedBytes.length > destination.length - offset)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, offset, destination.length - offset);
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: " + source.length() + "," + destination.length + "," + offset);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @param  destination  the destination byte array.
    // @param  offset  the offset into the destination array for the start of the data.
    // @param  length  the length of data to write into the array.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
        stringToByteArray(source, destination, offset, length, new BidiConversionProperties(getStringType(getCcsid())));
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @param  destination  the destination byte array.
    // @param  offset  the offset into the destination array for the start of the data.
    // @param  length  the length of data to write into the array.
    // @param  type  the bidi string type.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length, int type) throws CharConversionException
    {
        stringToByteArray(source, destination, offset, length, new BidiConversionProperties(type));
    }

    void stringToByteArray(String source, byte[] destination, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        byte[] convertedBytes = stringToByteArray(source, properties);
        if (convertedBytes.length > length)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, offset, length);
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: " + source.length() + "," + destination.length + "," + offset + "," + length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }
}
