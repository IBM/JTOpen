///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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



    static final long serialVersionUID = 4L;


    private Converter table;
    
    static boolean faultTolerantConversion_ = false;                  // @B1A

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
      @param  length  the number of bytes of data to read from the array.
      @return  the resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset, int length)
    {
	return table.byteArrayToString(source, offset, length);
    }

    /**
      Converts the specified bytes into a String.
      @param  source  the bytes to convert.
      @param  offset  the offset into the source array for the start of the data.
      @param  length  the number of bytes of data to read from the array.
      @param  type The bidi string type, as defined by the CDRA (Character
                   Data Representataion Architecture). See <a href="BidiStringType.html">
                   BidiStringType</a> for more information and valid values.
      @return  the resultant String.
      @see com.ibm.as400.access.BidiStringType
     **/
    public String byteArrayToString(byte[] source, int offset, int length, int type)    //$E0A
    {
       return table.byteArrayToString(source, offset, length, type);
    }

    // @C0A
    /**
      Converts the specified bytes into a String.
      @param  system  the 400 to go to for table
      @param  source  the bytes to convert.
      @return  the resultant String.
     **/
    public static String byteArrayToString(AS400 system, byte[] source)
    {
        try {
            Converter table = new Converter(system.getCcsid(), system);
            return table.byteArrayToString(source);
        }
        catch(UnsupportedEncodingException e) {
            // This exception should never happen, since we are getting the CCSID
            // from the system itself, and it only gets thrown if we pass a bad CCSID.
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, e);
            return "";
        }
    }

    // @C0A
    /**
      Converts the specified bytes into a String.
      @param  ccsid   the CCSID of the AS/400 text.
      @param  system  the 400 to go to for table
      @param  source  the bytes to convert.
      @return  the resultant String.
      @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static String byteArrayToString(int ccsid, AS400 system, byte[] source) throws UnsupportedEncodingException
    {
        Converter table = new Converter(ccsid, system);
        return table.byteArrayToString(source);
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


    // @B1A
    /**
    Indicates if conversion is fault tolerant.
    
    @return true if conversion is fault tolerant, false otherwise.
    **/
    public static boolean isFaultTolerantConversion()
    {
        return faultTolerantConversion_;
    }


    // @B1A
    /**
    Enables fault tolerant conversion.  Fault tolerant conversion allows incomplete
    EBCDIC character data to be converted without throwing an exception.
    This is a static setting and affects all subsequent character conversion.
    Fault tolerant conversion may adversly affect performance and memory usage
    during character conversion.  The default is false.
    
    @param faultTolerantConversion    true to enable fault tolerant conversion, false
                                otherwise.
    **/
    public static void setFaultTolerantConversion(boolean faultTolerantConversion)
    {
        faultTolerantConversion_ = faultTolerantConversion;

        if (Trace.isTraceOn()) 
            Trace.log(Trace.INFORMATION, "Setting fault tolerant conversion to " + faultTolerantConversion); 
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
      @param  type    the output string type as defined by the CDRA (Character Data Respresentation Architecture).
                      One of the following constants defined in BidiStringType: ST5 (LTR), ST6 (RTL), ST10 (Contextual LTR),
                      or ST11 (Contextual RTL).
      @return  the resultant byte array.
      @see com.ibm.as400.access.BidiStringType
     **/
    public byte[] stringToByteArray(String source, int type)         //$E0A
    {
       return table.stringToByteArray(source, type);
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
      @param  length  the number of bytes of data to write into the array.
      @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
	table.stringToByteArray(source, destination, offset, length);
    }

    /**
      Converts the specified String into bytes.
      @param  source  the String to convert.
      @param  destination  the destination byte array.
      @param  offset  the offset into the destination array for the start of the data.
      @param  length  the number of bytes of data to write into the array.
      @param  type The bidi string type, as defined by the CDRA (Character
                   Data Representataion Architecture). See <a href="BidiStringType.html">
                   BidiStringType</a> for more information and valid values.
      @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
      @see com.ibm.as400.access.BidiStringType
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length, int type)    //$E0A
      throws CharConversionException 
    {
       table.stringToByteArray(source, destination, offset, length, type);
    }

    // @C0A
    /**
      Converts the specified String into bytes.
      @param  system  the 400 to go to for table
      @param  source  the String to convert.
      @return         the destination byte array.
     **/
    public static byte[] stringToByteArray(AS400 system, String source)
    {
        try {
            Converter table = new Converter(system.getCcsid(), system);
            return table.stringToByteArray(source);
        }
        catch(UnsupportedEncodingException e) {
            // This exception should never happen, since we are getting the CCSID
            // from the system itself, and it only gets thrown if we pass a bad CCSID.
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, e);
            return new byte[0];
        }
    }

    // @C0A
    /**
      Converts the specified String into bytes.
      @param  ccsid   the CCSID of the AS/400 text.
      @param  system  the 400 to go to for table
      @param  source  the String to convert.
      @return         the destination byte array.
      @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static byte[] stringToByteArray(int ccsid, AS400 system, String source) throws UnsupportedEncodingException
    {
        Converter table = new Converter(ccsid, system);
        return table.stringToByteArray(source);
    }
}
