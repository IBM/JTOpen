///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: Converter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

// A character set converter between Java String objects and AS/400 native code pages.
class Converter implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    transient ConverterImpl impl;

    private String encoding = null;
    private int ccsid = -1;
    AS400 system = null; //@B6C - made package scope

    /**
      Gets a Converter object from the pool using a "best guess" based on the default Locale.
      @deprecated Replaced by Converter(int, AS400).
    **/
    Converter()
    {
        ccsid = ExecutionEnvironment.getBestGuessAS400Ccsid();
        try
        {
            chooseImpl();
        }
        catch (UnsupportedEncodingException e)
        {
            Trace.log(Trace.ERROR, "Unexpected CCSID returned from getBestGuessAS400Ccsid: " + ccsid, e);
            throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }
    }

    /**
      Constructs a character set conversion object using the specified character encoding.
      @param  encoding  the name of a character encoding.
      @exception  UnsupportedEncodingException  If the encoding is not supported.
      @deprecated Replaced by Converter(int, AS400).
    **/
    Converter(String encoding) throws UnsupportedEncodingException
    {
        this.encoding = encoding;
        chooseImpl();
    }

    /**
      Constructs a character set conversion object using the specified ccsid.
      @param  ccsid  the CCSID of the AS/400 text.
      @param  system  the 400 to go to for table
      @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
      @deprecated Replaced by Converter(int, AS400).
    **/
    Converter(int ccsid) throws UnsupportedEncodingException
    {
        this.ccsid = ccsid;
        chooseImpl();
    }

    // Constructs a character set conversion object using the specified ccsid.
    // @param  ccsid  the CCSID of the AS/400 text.
    // @param  system  the 400 to go to for table
    // @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
    Converter(int ccsid, AS400 system) throws UnsupportedEncodingException
    {
        this.ccsid = ccsid;
        this.system = system;
        chooseImpl();
    }

    // Deserializes and initializes transient data.
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        chooseImpl();
    }

    // Get the proper implementation object.
    private void chooseImpl() throws UnsupportedEncodingException
    {
        if (system == null)
        {
            // Only alternative is to try to load remote impl
            impl = (ConverterImpl)AS400.loadImpl("com.ibm.as400.access.ConverterImplRemote");
            if (impl == null) throw new UnsupportedEncodingException();
            if (encoding != null)
            {
                impl.setEncoding(encoding);
            }
            else
            {
                impl.setCcsid(ccsid, null);
            }
        }
        else
        {
            // load proxy or remote impl
            impl = (ConverterImpl)system.loadImpl2("com.ibm.as400.access.ConverterImplRemote", "com.ibm.as400.access.ConverterImplProxy");
            AS400Impl systemImpl = system.getImpl();
	    try
	    {
		impl.setCcsid(ccsid, systemImpl);
	    }
	    catch (UnsupportedEncodingException e)
	    {
		try
		{
		    system.connectService(AS400.CENTRAL);
		    impl.setCcsid(ccsid, systemImpl);
		}
		catch (Exception ee)
		{
		    throw (UnsupportedEncodingException)e.fillInStackTrace();
		}
	    }
        }
    }

    // Converts the specified bytes into a String.
    // @param  source  the bytes to convert.
    // @return  the resultant String.
    String byteArrayToString(byte[] source)
    {
        return impl.byteArrayToString(source, 0, source.length);
    }

    // Converts the specified bytes into a String.
    // @param  source  the bytes to convert.
    // @param  offset  the offset into the source array for the start of the data.
    // @return  the resultant String.
    String byteArrayToString(byte[] source, int offset)
    {
        return impl.byteArrayToString(source, offset, source.length-offset);
    }

    // Converts the specified bytes into a String.
    // @param  source  the bytes to convert.
    // @param  offset  the offset into the source array for the start of the data.
    // @param  length  the length of data to read from the array.
    // @return  the resultant String.
    String byteArrayToString(byte[] source, int offset, int length)
    {
        return impl.byteArrayToString(source, offset, length);
    }

    // Returns the ccsid of this conversion object.
    // @return  the ccsid.
    int getCcsid()
    {
        return impl.getCcsid();
    }

    // Returns the encoding of this conversion object.
    // @return  the encoding.
    String getEncoding()
    {
        return impl.getEncoding();
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @return  the resultant byte array.
    byte[] stringToByteArray(String source)
    {
        return impl.stringToByteArray(source);
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @param  destination  the destination byte array.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > destination.length)
        {
            // Copy as much as will fit
            System.arraycopy(convertedBytes, 0, destination, 0, destination.length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, 0, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @param  destination  the destination byte array.
    // @param  offset  the offset into the destination array for the start of the data.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > destination.length - offset)
        {
            // Copy as much as will fit
            System.arraycopy(convertedBytes, 0, destination, offset, destination.length - offset);
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
        byte[] convertedBytes = impl.stringToByteArray(source);
        if (convertedBytes.length > length)
        {
            // Copy as much as will fit
            System.arraycopy(convertedBytes, 0, destination, offset, length);
            throw new CharConversionException();
        }
        System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }
}
