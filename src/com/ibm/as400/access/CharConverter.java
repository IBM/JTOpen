///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: CharConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
  A character set converter between Java String objects and AS/400 native code pages.
 **/
public class CharConverter implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private Converter table;

    /**
      Gets a CharConverter object from the pool using a "best guess" based on the default Locale.

      @deprecated Replaced by CharConverter(int, AS400).
      Any CharConverter object that is created without
      specifying an AS400 system object on its constructor may
      not behave as expected in certain environments.
      
     */
    public CharConverter()
    {
	table = new Converter();
    }

    /**
      Gets a CharConverter object from the pool using the specified character encoding.
      @param  encoding  the name of a character encoding.
      @exception  UnsupportedEncodingException  If the <i>encoding</i> is not supported.

      @deprecated Replaced by CharConverter(int, AS400).
      Any CharConverter object that is created without
      specifying an AS400 system object on its constructor may
      not behave as expected in certain environments.
      
     */
    public CharConverter(String encoding) throws UnsupportedEncodingException
    {
	table = new Converter(encoding);
    }

    /**
      Gets a CharConverter object from the pool using the specified ccsid.
      @param  ccsid  the CCSID of the AS/400 text.
      @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.

      @deprecated Replaced by CharConverter(int, AS400).
      Any CharConverter object that is created without
      specifying an AS400 system object on its constructor may
      not behave as expected in certain environments.
      
     */
    public CharConverter(int ccsid) throws UnsupportedEncodingException
    {
	table = new Converter(ccsid);
    }

    /**
      Gets a CharConverter object from the pool using the specified ccsid and system.
      @param  ccsid  the CCSID of the AS/400 text.
      @param  system  the 400 to go to for table
      @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     */
    public CharConverter(int ccsid, AS400 system) throws UnsupportedEncodingException
    {
	table = new Converter(ccsid, system);
    }

    /**
      Converts the specified bytes into a String.
      @param  source  the bytes to convert.
      @return  the resultant String.
     **/
    public String byteArrayToString(byte[] source)
    {
	return table.byteArrayToString(source);
    }

    /**
      Converts the specified bytes into a String.
      @param  source  the bytes to convert.
      @param  offset  the offset into the source array for the start of the data.
      @return  the resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset)
    {
	return table.byteArrayToString(source, offset);
    }

    /**
      Converts the specified bytes into a String.
      @param  source  the bytes to convert.
      @param  offset  the offset into the source array for the start of the data.
      @param  length  the length of data to read from the array.
      @return  the resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset, int length)
    {
	return table.byteArrayToString(source, offset, length);
    }

    /**
      Returns the ccsid of this conversion object.
      @return  the ccsid.
     **/
    public int getCcsid()
    {
	return table.getCcsid();
    }

    /**
      Returns the encoding of this conversion object.
      @return  the encoding.
     **/
    public String getEncoding()
    {
	return table.getEncoding();
    }

    /**
      Converts the specified String into bytes.
      @param  source  the String to convert.
      @return  the resultant byte array.
     **/
    public byte[] stringToByteArray(String source)
    {
	return table.stringToByteArray(source);
    }

    /**
      Converts the specified String into bytes.
      @param  source  the String to convert.
      @param  destination  the destination byte array.
      @exception CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
	table.stringToByteArray(source, destination);
    }

    /**
      Converts the specified String into bytes.
      @param  source  the String to convert.
      @param  destination  the destination byte array.
      @param  offset  the offset into the destination array for the start of the data.
      @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
	table.stringToByteArray(source, destination, offset);
    }

    /**
      Converts the specified String into bytes.
      @param  source  the String to convert.
      @param  destination  the destination byte array.
      @param  offset  the offset into the destination array for the start of the data.
      @param  length  the length of data to write into the array.
      @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
	table.stringToByteArray(source, destination, offset, length);
    }
}
