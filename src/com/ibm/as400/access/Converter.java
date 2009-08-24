///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Converter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

// A character set converter between Java String objects and native code pages.
class Converter implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    transient ConverterImpl impl;
    private String encoding_ = null;
    private int ccsid_ = -1;
    private AS400 system_ = null;

    // Gets a Converter object from the pool using a "best guess" based on the default Locale.
    Converter()
    {
        ccsid_ = ExecutionEnvironment.getBestGuessAS400Ccsid();
        try
        {
            chooseImpl();
        }
        catch (UnsupportedEncodingException e)
        {
            Trace.log(Trace.ERROR, "Unexpected CCSID returned from getBestGuessAS400Ccsid: " + ccsid_, e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
        }
    }

    // Constructs a character set conversion object using the specified character encoding.
    // @param  encoding  The name of a character encoding.
    // @exception  UnsupportedEncodingException  If the encoding is not supported.
    Converter(String encoding) throws UnsupportedEncodingException
    {
        encoding_ = encoding;
        chooseImpl();
    }

    // Constructs a character set conversion object using the specified ccsid.
    // @param  ccsid  The CCSID of the server text.
    // @param  system  The system object representing the server with which to connect.
    // @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
    Converter(int ccsid) throws UnsupportedEncodingException
    {
        ccsid_ = ccsid;
        chooseImpl();
    }

    // Constructs a character set conversion object using the specified ccsid.
    // @param  ccsid  The CCSID of the server text.
    // @param  system  The system object representing the server with which to connect.
    // @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
    Converter(int ccsid, AS400 system) throws UnsupportedEncodingException
    {
        ccsid_ = ccsid;
        system_ = system;
        chooseImpl();
    }

    // Deserializes and initializes transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        in.defaultReadObject();
        chooseImpl();
    }

    // Get the proper implementation object.
    private void chooseImpl() throws UnsupportedEncodingException
    {
        if (system_ == null)
        {
            // Only alternative is to try to load remote impl.
            impl = (ConverterImpl)AS400.loadImpl("com.ibm.as400.access.ConverterImplRemote");
            if (impl == null) throw new UnsupportedEncodingException();
            if (encoding_ != null)
            {
                impl.setEncoding(encoding_);
            }
            else
            {
                impl.setCcsid(ccsid_, null);
            }
        }
        else
        {
            // Load proxy or remote impl.
            impl = (ConverterImpl)system_.loadImpl2("com.ibm.as400.access.ConverterImplRemote", "com.ibm.as400.access.ConverterImplProxy");
            AS400Impl systemImpl = system_.getImpl();
            try
            {
                impl.setCcsid(ccsid_, systemImpl);
            }
            catch (UnsupportedEncodingException e)
            {
                try
                {
                    system_.connectService(AS400.CENTRAL);
                    impl.setCcsid(ccsid_, systemImpl);
                }
                catch (Exception ee)
                {
                    throw e;
                }
            }
        }
    }

    // Converts the specified bytes into a String.
    // @param  source  The bytes to convert.
    // @return  The resultant String.
    String byteArrayToString(byte[] source)
    {
        return impl.byteArrayToString(source, 0, source.length);
    }

    // Converts the specified bytes into a String.
    // @param  source  The bytes to convert.
    // @param  offset  The offset into the source array for the start of the data.
    // @return  The resultant String.
    String byteArrayToString(byte[] source, int offset)
    {
        return impl.byteArrayToString(source, offset, source.length - offset);
    }

    // Converts the specified bytes into a String.
    // @param  source  The bytes to convert.
    // @param  offset  The offset into the source array for the start of the data.
    // @param  length  The number of bytes of data to read from the array.
    // @return  The resultant String.
    String byteArrayToString(byte[] source, int offset, int length)
    {
        return impl.byteArrayToString(source, offset, length);
    }

    // Converts the specified bytes into a String.
    // @param  source  The bytes to convert.
    // @param  offset  The offset into the source array for the start of the data.
    // @param  length  The number of bytes of data to read from the array.
    // @param  type  The output string type as defined by the CDRA (Character Data Respresentation Architecture).  One of the following constants defined in BidiStringType: ST5 (LTR), ST6 (RTL), ST10 (Contextual LTR), or ST11 (Contextual RTL).
    // @return  The resultant String.
    String byteArrayToString(byte[] source, int offset, int length, int type)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(type));
    }

    // Converts the specified bytes into a String.
    // @param  source  The bytes to convert.
    // @param  offset  The offset into the source array for the start of the data.
    // @param  length  The number of bytes of data to read from the array.
    // @param  properties  The bidi conversion properties.
    // @return  The resultant String.
    String byteArrayToString(byte[] source, int offset, int length, BidiConversionProperties properties)
    {
        return impl.byteArrayToString(source, offset, length, properties);
    }

    // Returns the ccsid of this conversion object.
    // @return  The ccsid.
    int getCcsid()
    {
        return impl.getCcsid();
    }

    // Returns the encoding of this conversion object.
    // @return  The encoding.
    String getEncoding()
    {
        return impl.getEncoding();
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @return  The resultant byte array.
    byte[] stringToByteArray(String source)
    {
        return impl.stringToByteArray(source);
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  type  The output string type as defined by the CDRA (Character Data Respresentation Architecture).  One of the following constants defined in BidiStringType: ST5 (LTR), ST6 (RTL), ST10 (Contextual LTR), or ST11 (Contextual RTL).
    // @return  The resultant byte array.
    byte[] stringToByteArray(String source, int type)
    {
        return stringToByteArray(source, new BidiConversionProperties(type));
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  properties  The bidi conversion properties.
    // @return  The resultant byte array.
    byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        return impl.stringToByteArray(source, properties);
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  destination  The destination byte array.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > destination.length)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, 0, destination.length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, 0, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  destination  The destination byte array.
    // @param  offset  The offset into the destination array for the start of the data.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > destination.length - offset)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, offset, destination.length - offset);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  destination  The destination byte array.
    // @param  offset  The offset into the destination array for the start of the data.
    // @param  length  The number of bytes of data to write into the array.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > length)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, offset, length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  destination  The destination byte array.
    // @param  offset  The offset into the destination array for the start of the data.
    // @param  length  The number of bytes of data to write into the array.
    // @param  type  The output string type as defined by the CDRA (Character Data Respresentation Architecture).  One of the following constants defined in BidiStringType: ST5 (LTR), ST6 (RTL), ST10 (Contextual LTR), or ST11 (Contextual RTL).
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length, int type) throws CharConversionException
    {
        stringToByteArray(source, destination, offset, length, new BidiConversionProperties(type));
    }

    // Converts the specified String into bytes.
    // @param  source  The String to convert.
    // @param  destination  The destination byte array.
    // @param  offset  The offset into the destination array for the start of the data.
    // @param  length  The number of bytes of data to write into the array.
    // @param  properties  The bidi conversion properties.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source, properties);
        if (convertedBytes.length > length)
        {
            // Copy as much as will fit.
            System.arraycopy(convertedBytes, 0, destination, offset, length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }
}
