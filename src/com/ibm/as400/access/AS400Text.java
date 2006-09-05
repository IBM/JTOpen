///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Text.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

/**
 The AS400Text class provides character set conversion between Java String objects and i5/OS code pages.
 <P>Note that in the past few releases, several constructors were deprecated because they did not accept a system object as an argument.  Due to recent changes in the behavior of the character conversion routines, this system object is no longer necessary, except when the AS400Text object is to be passed as a parameter on a Toolbox Proxy connection.  Since this case is extremely rare, it is more beneficial not to have the constructors issue deprecation warnings.
 @see  com.ibm.as400.access.CharConverter
 **/
public class AS400Text implements AS400DataType
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    private int length_;
    private int ccsid_ = 65535;
    transient private String encoding_ = null;
    private AS400 system_;
    transient ConverterImpl tableImpl_;
    private static final String defaultValue = "";
    private byte[] padding_ = null;

    /**
     Constructs an AS400Text object.
     It uses the most likely CCSID based on the default locale.
     @param  length  The byte length of the i5/OS text.  It must be greater than or equal to zero.
     **/
    public AS400Text(int length)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        length_ = length;
    }

    /**
     Constructs an AS400Text object.
     @param  length  The byte length of the i5/OS text.  It must be greater than or equal to zero.
     @param  ccsid  The CCSID of the i5/OS text.  It must refer to a valid and available CCSID.  The value 65535 will cause the data type to use the most likely CCSID based on the default locale.
     **/
    public AS400Text(int length, int ccsid)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (ccsid < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        length_ = length;
        ccsid_ = ccsid;
    }

    /**
     Constructs AS400Text object.
     @param  length  The byte length of the i5/OS text.  It must be greater than or equal to zero.
     @param  encoding  The name of a character encoding.  It must be a valid and available encoding.
     **/
    public AS400Text(int length, String encoding)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (encoding == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'encoding' is null.");
            throw new NullPointerException("encoding");
        }
        length_ = length;
        encoding_ = encoding;
    }

    /**
     Constructs an AS400Text object.  The CCSID used for conversion will be the CCSID of the <i>system</i> object.
     @param  length  The byte length of the i5/OS text.  It must be greater than or equal to zero.
     @param  system  The system with which to determine the CCSID.
     */
    public AS400Text(int length, AS400 system)
    {
        // Passing a 65535 will cause setTable() to do a system.getCcsid() at conversion time.
        this(length, 65535, system);
    }

    /**
     Constructs an AS400Text object.
     @param  length  The byte length of the i5/OS text.  It must be greater than or equal to zero.
     @param  ccsid  The CCSID of the i5/OS text.  It must refer to a valid and available CCSID.  The value 65535 will cause the data type to use the most likely CCSID based on the default locale.
     @param  system  The system from which the conversion table may be downloaded.
     */
    public AS400Text(int length, int ccsid, AS400 system)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (ccsid < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        length_ = length;
        ccsid_ = ccsid;
        system_ = system;
    }

    // Package scope constructor for use on the proxy server.  Note that this constructor is only used in AS400FileRecordDescriptionImplRemote.  It is expected that the client code (AS400FileRecordDescription) will call fillInConverter() on each AS400Text object returned.
    AS400Text(int length, int ccsid, AS400Impl system)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (ccsid < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        length_ = length;
        ccsid_ = ccsid;
        // Notice that we have not filled in the Converter object.  We can't do that because we don't know if this object will in the end be used on the public side (Converter) or on the i5/OS side (ConverterImpl).
        // We also can't do that yet since the Converter ctor will connect to the system.
    }

    /**
     Creates a new AS400Text object that is identical to the current instance.
     @return  The new object.
     **/
    public Object clone()
    {
        try
        {
            return super.clone();  // Object.clone does not throw exception.
        }
        catch (CloneNotSupportedException e)
        {
            Trace.log(Trace.ERROR, "Unexpected CloneNotSupportedException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Returns the byte length of the data type.
     @return  The number of bytes in the i5/OS representation of the data type.
     **/
    public int getByteLength()
    {
        return length_;
    }

    /**
     Returns the CCSID of the data type.
     @return  The CCSID.
     **/
    public int getCcsid()
    {
        if (ccsid_ == 65535) setTable();
        return ccsid_;
    }

    // Returns the ConverterImpl object so other classes don't need to create a new Converter if they already have an AS400Text object.
    ConverterImpl getConverter()
    {
        setTable();
        return tableImpl_;
    }

    /**
     Returns a Java object representing the default value of the data type.
     @return  The String object representing an empty string ("").
     **/
    public Object getDefaultValue()
    {
        return new String(defaultValue);
    }

    /**
     Returns the encoding of the data type.
     @return  The encoding of the data type.
     **/
    public String getEncoding()
    {
        if (encoding_ == null) setTable();
        return encoding_;
    }

    /**
     Returns {@link com.ibm.as400.access.AS400DataType#TYPE_TEXT TYPE_TEXT}.
     @return  The value AS400DataType.TYPE_TEXT.
     **/
    public int getInstanceType()
    {
        return AS400DataType.TYPE_TEXT;
    }

    // This method is used in conjunction with the constructor that takes an AS400Impl.  It is used to fully instantiate the member data of this AS400Text object once it has been serialized and received on the client from the proxy server.  We do it this way because we can't create a normal AS400Text object on the proxy server and expect it to be valid on the proxy client because its internal Converter object would not be proxified correctly.
    // When an AS400Text object is serialized from the proxy server over to the client, the client code must set the converter using this method.
    void setConverter(AS400 system)
    {
        system_ = system;
        setTable();
    }

    // When an AS400Text object is serialized from the client over to the proxy server, the server code must set the converter using this method.  Note that we cannot refer directly to the ConverterImplRemote class here, so it is left up to the server code to create that and pass it in to this method.
    void setConverter(ConverterImpl converter)
    {
        tableImpl_ = converter;
        // Just in case this object ever goes back to the client.
        ccsid_ = tableImpl_.getCcsid();
    }

    // Private method to initialize the Converter table and its impl.
    private void setTable()
    {
        if (tableImpl_ == null)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Text object initializing, encoding: " + encoding_ + ", CCSID: " + ccsid_ + ", system: " + system_);
            if (encoding_ != null)
            {
                try
                {
                    Converter table = new Converter(encoding_);
                    ccsid_ = table.getCcsid();
                    tableImpl_ = table.impl;
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new ExtendedIllegalArgumentException("encoding (" + encoding_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
            else
            {
                try
                {
                    if (system_ == null)
                    {
                        Converter table;
                        if (ccsid_ == 65535)
                        {
                            table = new Converter();
                            ccsid_ = table.getCcsid();
                        }
                        else
                        {
                            table = new Converter(ccsid_);
                        }
                        tableImpl_ = table.impl;
                    }
                    else
                    {
                        if (ccsid_ == 65535)
                        {
                            ccsid_ = system_.getCcsid();
                        }
                        Converter table = new Converter(ccsid_, system_);
                        tableImpl_ = table.impl;
                    }
                    encoding_ = tableImpl_.getEncoding();
                }
                catch (UnsupportedEncodingException e)
                {
                    Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid_);
                    throw new ExtendedIllegalArgumentException("ccsid (" + ccsid_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Text object initialized, encoding: " + encoding_ + ", CCSID: " + ccsid_ + ", system: " + system_ + ", table: " + tableImpl_);
        }
    }

    /**
     Converts the specified Java object to i5/OS format.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @return  The i5/OS representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
        byte[] serverValue = new byte[length_];
        toBytes(javaValue, serverValue, 0);
        return serverValue;
    }

    /**
     Converts the specified Java object into i5/OS format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     @return  The number of bytes in the i5/OS representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue)
    {
        return toBytes(javaValue, serverValue, 0);
    }

    /**
     Converts the specified Java object into i5/OS format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     @param  offset  The offset into the byte array for the start of the i5/OS value.  It must be greater than or equal to zero.
     @return  The number of bytes in the i5/OS representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset)
    {
        if (AS400BidiTransform.isBidiCcsid(getCcsid()))
        {
            return toBytes(javaValue, serverValue, offset, new BidiConversionProperties(AS400BidiTransform.getStringType(ccsid_)));
        }
        else
        {
            return toBytes(javaValue, serverValue, offset, new BidiConversionProperties(BidiStringType.DEFAULT));
        }
    }

    /**
     Converts the specified Java object into i5/OS format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     @param  offset  The offset into the byte array for the start of the i5/OS value.  It must be greater than or equal to zero.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The number of bytes in the i5/OS representation of the data type.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset, int type)
    {
        return toBytes(javaValue, serverValue, offset, new BidiConversionProperties(type));
    }

    /**
     Converts the specified Java object into i5/OS format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     @param  offset  The offset into the byte array for the start of the i5/OS value.  It must be greater than or equal to zero.
     @param  properties  The bidi conversion properties.
     @return  The number of bytes in the i5/OS representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset, BidiConversionProperties properties)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (javaValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'javaValue' is null.");
            throw new NullPointerException("javaValue");
        }

        // Make sure the table is set.
        setTable();

        // We need to pad the String before the conversion in the case of a Bidi CCSID, because the Bidi transform needs to affect the entire String so it knows where the padding spaces need to go.
        String toConvert = (String)javaValue;

        // We can't pad the String after the transform if we're bidi.
        if (AS400BidiTransform.isBidiCcsid(ccsid_)) // We can use ccsid_ since we already called setTable().
        {
            int realLength = toConvert.length();
            // Cases where we are Bidi, but we are DBCS.
            if (ccsid_ == 13488 || ccsid_ == 61952)
            {
                realLength = realLength * 2;
            }
            int numPadBytes = length_ - realLength;
            // Cases where we are Bidi, but we are DBCS.
            if (ccsid_ == 13488 || ccsid_ == 61952)
            {
                numPadBytes = numPadBytes / 2;
            }
            if (numPadBytes > 0)
            {
                char[] cbuf = toConvert.toCharArray();
                // Since all of our Bidi maps are SBCS, we can add one char for each extra byte we need.
                char[] paddedBuf = new char[cbuf.length + numPadBytes];
                System.arraycopy(cbuf, 0, paddedBuf, 0, cbuf.length);
                for (int i = cbuf.length; i < paddedBuf.length; ++i)
                {
                    paddedBuf[i] = (char)0x0020; // SBCS space
                }
                toConvert = new String(paddedBuf);
                if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Pre-padded Bidi String with " + numPadBytes + " spaces from '" + javaValue + "' to '" + toConvert + "'");
            }
        }
        byte[] eValue = tableImpl_.stringToByteArray(toConvert, properties);

        // Check that converted data fits within data type.
        if (eValue.length > length_)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'javaValue' is not valid: '" + javaValue + "'");
            throw new ExtendedIllegalArgumentException("javaValue (" + toConvert.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // Let this line throw ArrayIndexException.
        System.arraycopy(eValue, 0, serverValue, offset, eValue.length);

        // Pad with spaces.
        // Note that this may sort of kludge the byte array in cases where the allocated size isn't an even number for double-byte CCSID's.  e.g. new AS400Text(11, 13488) and wrote "ABCDE" which would take up 5*2=10 bytes, so we would pad the 11th byte with a double-byte space (0x00 0x20), so only the 0x00 would get written.  Not much we can do about it though.

        // Build padding string.
        int index = offset + eValue.length;
        if (index < serverValue.length && index < offset+length_)
        {
            if (padding_ == null)
            {
                // Convert padding string using appropriate CCSID.
                padding_ = tableImpl_.stringToByteArray("\u0020"); // The single-byte space.
                // Either 0020 or 3000 must translate to a valid space character, no matter the codepage.
                switch (padding_.length)
                {
                    case 0:  // Char wasn't in table.
                        padding_ = tableImpl_.stringToByteArray("\u3000");
                        break;
                    case 1: // Char may be a single-byte substitution character.
                        if (padding_[0] == 0x3F || padding_[0] == 0x7F || padding_[0] == 0x1A)
                        {
                            padding_ = tableImpl_.stringToByteArray("\u3000");
                        }
                        break;
                    case 2: // Char may be a double-byte substitution character.
                        int s = (0xFFFF & BinaryConverter.byteArrayToShort(padding_, 0));
                        if (s == 0xFEFE || s == 0xFFFD || s == 0x003F || s == 0x007F || s == 0x001A)
                        {
                            padding_ = tableImpl_.stringToByteArray("\u3000");
                        }
                        break;
                    default:
                        if (Trace.traceOn_)
                        {
                            Trace.log(Trace.WARNING, "AS400Text.toBytes(): Padding character not found for 0x0020 or 0x3000 under CCSID " + tableImpl_.getCcsid(), padding_, 0, padding_.length);
                            Trace.log(Trace.WARNING, "Using 0x40 as default padding character.");
                        }
                        padding_ = new byte[] { 0x40};
                }
            }
            // Copy padding bytes into destination as many times as necessary.  Could've used a StringBuffer and a System.arraycopy, but this is faster...
            int max = (offset+length_) < serverValue.length ? (offset+length_) : serverValue.length;
            for (int i = 0; i < max - index; ++i)
            {
                serverValue[i+index] = padding_[i % padding_.length];
            }
        }

        // Copy padding bytes into destination as many times as necessary.  Could've used a StringBuffer and a System.arraycopy, but this is faster...
        int max = (offset+length_) < serverValue.length ? (offset+length_) : serverValue.length;
        for (int i = 0; i < max - index; ++i)
        {
            serverValue[i + index] = padding_[i % padding_.length];
        }
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Text.toBytes(): Converted javaValue (" + toConvert + ") to:", serverValue, offset, length_);

        return length_;
    }

    /**
     Converts the specified i5/OS data type to a Java object.
     @param  serverValue  The array containing the data type in i5/OS format.  The entire data type must be represented.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        setTable(); // Make sure the table is set.
        return tableImpl_.byteArrayToString(serverValue, 0, length_);
    }

    /**
     Converts the specified i5/OS data type to a Java object.
     @param  serverValue  The array containing the data type in i5/OS format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the i5/OS value. It must be greater than or equal to zero.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue, int offset)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        setTable(); // Make sure the table is set.
        return tableImpl_.byteArrayToString(serverValue, offset, length_);
    }

    /**
     Converts the specified i5/OS data type to a Java object.
     @param  serverValue  The array containing the data type in i5/OS format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the i5/OS value. It must be greater than or equal to zero.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The String object corresponding to the data type.
     @see com.ibm.as400.access.BidiStringType
     **/
    public Object toObject(byte[] serverValue, int offset, int type)
    {
        return toObject(serverValue, offset, new BidiConversionProperties(type));
    }

    /**
     Converts the specified i5/OS data type to a Java object.
     @param  serverValue  The array containing the data type in i5/OS format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the i5/OS value. It must be greater than or equal to zero.
     @param  properties  The bidi conversion properties.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue, int offset, BidiConversionProperties properties)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        setTable(); // Make sure the table is set
        return tableImpl_.byteArrayToString(serverValue, offset, length_, properties);
    }
}
