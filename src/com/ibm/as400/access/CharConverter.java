///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CharConverter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 A character set converter between Java String objects and i5/OS native code pages.
 <P>Note that in the past few releases, several constructors were deprecated because they did not accept a system object as an argument.  Due to recent changes in the behavior of the character conversion routines, this system object is no longer necessary.
 @see  com.ibm.as400.access.AS400Text
 **/
public class CharConverter implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    private static boolean faultTolerantConversion_ = false;

    private Converter table_;

    /**
     Constructs a CharConverter object using a "best guess" based on the default Locale.
     */
    public CharConverter()
    {
        table_ = new Converter();
    }

    /**
     Constructs a CharConverter object using the specified character encoding.
     @param  encoding  The name of a character encoding.
     @exception  UnsupportedEncodingException  If the <i>encoding</i> is not supported.
     */
    public CharConverter(String encoding) throws UnsupportedEncodingException
    {
        table_ = new Converter(encoding);
    }

    /**
     Constructs a CharConverter object using the specified CCSID.
     @param  ccsid  The CCSID of the i5/OS text.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     */
    public CharConverter(int ccsid) throws UnsupportedEncodingException
    {
        table_ = new Converter(ccsid);
    }

    /**
     Constructs a CharConverter object using the specified CCSID and system.
     @param  ccsid  The CCSID of the i5/OS text.
     @param  system  The system object representing the system with which to connect.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     */
    public CharConverter(int ccsid, AS400 system) throws UnsupportedEncodingException
    {
        table_ = new Converter(ccsid, system);
    }

    /**
     Converts the specified bytes into a String.
     @param  source  The bytes to convert.
     @return  The resultant String.
     **/
    public String byteArrayToString(byte[] source)
    {
        return table_.byteArrayToString(source);
    }

    /**
     Converts the specified bytes into a String.
     @param  source  The bytes to convert.
     @param  offset  The offset into the source array for the start of the data.
     @return  The resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset)
    {
        return table_.byteArrayToString(source, offset);
    }

    /**
     Converts the specified bytes into a String.
     @param  source  The bytes to convert.
     @param  offset  The offset into the source array for the start of the data.
     @param  length  The number of bytes of data to read from the array.
     @return  The resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset, int length)
    {
        return table_.byteArrayToString(source, offset, length);
    }

    /**
     Converts the specified bytes into a String.
     @param  source  The bytes to convert.
     @param  offset  The offset into the source array for the start of the data.
     @param  length  The number of bytes of data to read from the array.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The resultant String.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public String byteArrayToString(byte[] source, int offset, int length, int type)
    {
        return byteArrayToString(source, offset, length, new BidiConversionProperties(type));
    }

    /**
     Converts the specified bytes into a String.
     @param  source  The bytes to convert.
     @param  offset  The offset into the source array for the start of the data.
     @param  length  The number of bytes of data to read from the array.
     @param  properties  The bidi conversion properties.
     @return  The resultant String.
     **/
    public String byteArrayToString(byte[] source, int offset, int length, BidiConversionProperties properties)
    {
        return table_.byteArrayToString(source, offset, length, properties);
    }

    /**
     Converts the specified bytes into a String.
     @param  system  The system object representing the system with which to connect.
     @param  source  The bytes to convert.
     @return  The resultant String.
     **/
    public static String byteArrayToString(AS400 system, byte[] source)
    {
        try
        {
            return new Converter(system.getCcsid(), system).byteArrayToString(source);
        }
        catch (UnsupportedEncodingException e)
        {
            // This exception should never happen, since we are getting the CCSID from the system itself, and it only gets thrown if we pass a bad CCSID.
            Trace.log(Trace.ERROR, "Unexpected UnsupportedEncodingException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Converts the specified bytes into a String.
     @param  ccsid   The CCSID of the i5/OS text.
     @param  system  The system object representing the system with which to connect.
     @param  source  The bytes to convert.
     @return  The resultant String.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static String byteArrayToString(int ccsid, AS400 system, byte[] source) throws UnsupportedEncodingException
    {
        return new Converter(ccsid, system).byteArrayToString(source);
    }

    /**
     Converts the specified bytes into a String.  If the Toolbox does not possess a table for the given CCSID, nor does the JVM, an UnsupportedEncodingException is thrown.
     @param  ccsid   The CCSID of the i5/OS text.
     @param  source  The bytes to convert.
     @return  The resultant String.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static String byteArrayToString(int ccsid, byte[] source) throws UnsupportedEncodingException
    {
        return new Converter(ccsid).byteArrayToString(source);
    }

    /**
     Converts a QSYS pathname String obtained from the IFS classes into a String suitable for use with other Toolbox services such as CommandCall and DataQueues.
     <p>This method is meant to handle QSYS pathnames and other string data that was retrieved using the IFS classes.  Object names in QSYS are stored in EBCDIC.  The file server always returns names to the Toolbox IFS classes in Unicode, so the system must convert the name from EBCDIC to Unicode before returning it to the client.  The system does this conversion using CCSID 37, not the file server job CCSID; however, the name may contain variant (but legal) codepoints.  Specifically, the three legal variant EBCDIC codepoints for QSYS object names are 0x5B, 0x7B, and 0x7C.  If the name retrieved using the Toolbox IFS classes is given to another Toolbox component such as CommandCall, the name will be converted to EBCDIC using the job CCSID for that particular component.  If variant characters exist in the name, the resulting name used by the host server job may not be the same as the original name.
     <p>Here is a typical scenario in which this method will be needed.  The user profile name CASH$FLOW exists on the system.  In EBCDIC CCSID 37, it is comprised of the codepoints:
     <pre>
     0xC3 0xC1 0xE2 0xC8  <B>0x5B</B>  0xC6 0xD3 0xD6 0xE6
     </pre>
     Note that the dollar sign '$' is codepoint 0x5B so it is one of the legal codepoints for a QSYS object pathname.  Now, if this pathname is used in a CommandCall, such as "DLTUSRPRF CASH$FLOW", that command string will get converted to the CCSID of the host server job.  If the host server job isn't running under CCSID 37, the resulting command string may not contain the dollar sign. For example, in CCSID 285 (United Kingdom) the codepoint 0x5B is actually an English pound sterling ('\u00A3' or Unicode 0x00A3).  The dollar sign '$' is found at codepoint 0x4A instead.  Hence, the "CASH$FLOW" in the command string will get converted to the following EBCDIC CCSID 285 codepoints:
     <pre>
     0xC3 0xC1 0xE2 0xC8  <B>0x4A</B>  0xC6 0xD3 0xD6 0xE6
     </pre>
     That is not how the user profile name is stored in QSYS.  The 0x4A codepoint should really be a 0x5B codepoint.  So in this case, the command server will return an error message indicating the user profile was not found.
     <p>The solution is to use this method to replace the variant codepoints with codepoints that will correctly convert given the host server job CCSID.  When given the string "CASH$FLOW" and the CCSID 285, this method will return the string "CASH\u00A3FLOW".  If the CommandCall is issued with the string "DLTUSRPRF CASH\u00A3FLOW" and the job CCSID of the remote command host server is 285, it will correctly convert the pound sterling '\u00A3' into codepoint 0x5B, which is how the user profile name "CASH$FLOW" is actually stored in QSYS.
     <p>For more information, please see <A HREF="http://publib.boulder.ibm.com/pubs/html/as400/v4r5/ic2924/info/RBAM6NAMEINCOM.HTM">i5/OS Information Center: CL and APIs: Control Language (CL): Naming within commands</A>.
     @see #convertJobPathnameToIFSQSYSPathname
     @param  qsysData  The String in which to substitute variant QSYS characters.
     @param  jobCCSID  The CCSID of the job in which to convert the variant characters.
     @return  The Unicode String with correctly substituted variant characters for use with host servers that convert based upon job CCSID.
     @exception  UnsupportedEncodingException  If the specified CCSID is not supported.
     **/
    public static String convertIFSQSYSPathnameToJobPathname(String qsysData, int jobCCSID) throws UnsupportedEncodingException
    {
        return new Converter(jobCCSID, null).byteArrayToString(new Converter(37, null).stringToByteArray(qsysData));
    }

    /**
     Converts a pathname String obtained from a Toolbox host server (such as CommandCall or DataQueue) to a QSYS pathname suitable for use with the IFS classes.
     <p>See the javadoc for {@link #convertIFSQSYSPathnameToJobPathname convertIFSQSYSPathnameToJobPathname} for more information.  This method essentially does the opposite of what convertIFSQSYSPathnameToJobPathname does.  The specified <I>jobData</I> string has its variant characters substituted so that it can be used with the IFS classes.  If given the String returned by this method, the file server will correctly convert the codepoints into the real QSYS object pathname using CCSID 37.
     @see  #convertIFSQSYSPathnameToJobPathname
     @param  jobData  The String in which to substitute variant QSYS characters.
     @param  jobCCSID  The CCSID of the job in which to convert the variant characters.
     @return  The Unicode String with correctly substituted variant characters for use with the IFS server that converts based upon CCSID 37.
     @exception  UnsupportedEncodingException  If the specified CCSID is not supported.
     **/  
    public static String convertJobPathnameToIFSQSYSPathname(String jobData, int jobCCSID) throws UnsupportedEncodingException
    {
        return new Converter(37, null).byteArrayToString(new Converter(jobCCSID, null).stringToByteArray(jobData));
    }

    /**
     Returns the CCSID of this conversion object.
     @return  The CCSID.
     **/
    public int getCcsid()
    {
        return table_.getCcsid();
    }

    /**
     Returns the encoding of this conversion object.
     @return  The encoding.
     **/
    public String getEncoding()
    {
        return table_.getEncoding();
    }

    /**
     Indicates if conversion is fault tolerant.
     @return  true if conversion is fault tolerant, false otherwise.
     **/
    public static boolean isFaultTolerantConversion()
    {
        return faultTolerantConversion_;
    }

    /**
     Enables fault tolerant conversion.  Fault tolerant conversion allows incomplete EBCDIC character data to be converted without throwing an exception.  This is a static setting and affects all subsequent character conversion.  Fault tolerant conversion may adversly affect performance and memory usage during character conversion.  The default is false.
     @param  faultTolerantConversion  true to enable fault tolerant conversion, false otherwise.
     **/
    public static void setFaultTolerantConversion(boolean faultTolerantConversion)
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Setting fault tolerant conversion:", faultTolerantConversion);
        faultTolerantConversion_ = faultTolerantConversion;
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @return  The resultant byte array.
     **/
    public byte[] stringToByteArray(String source)
    {
        return table_.stringToByteArray(source);
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The resultant byte array.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public byte[] stringToByteArray(String source, int type)
    {
        return stringToByteArray(source, new BidiConversionProperties(type));
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  properties  The bidi conversion properties.
     @return  The resultant byte array.
     **/
    public byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        return table_.stringToByteArray(source, properties);
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  destination  The destination byte array.
     @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
        table_.stringToByteArray(source, destination);
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  destination  The destination byte array.
     @param  offset  The offset into the destination array for the start of the data.
     @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
        table_.stringToByteArray(source, destination, offset);
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  destination  The destination byte array.
     @param  offset  The offset into the destination array for the start of the data.
     @param  length  The number of bytes of data to write into the array.
     @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
        table_.stringToByteArray(source, destination, offset, length);
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  destination  The destination byte array.
     @param  offset  The offset into the destination array for the start of the data.
     @param  length  The number of bytes of data to write into the array.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length, int type) throws CharConversionException
    {
        table_.stringToByteArray(source, destination, offset, length, new BidiConversionProperties(type));
    }

    /**
     Converts the specified String into bytes.
     @param  source  The String to convert.
     @param  destination  The destination byte array.
     @param  offset  The offset into the destination array for the start of the data.
     @param  length  The number of bytes of data to write into the array.
     @param  properties  The bidi conversion properties.
     @exception  CharConversionException  If <i>destination</i> is not large enough to hold the converted string.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public void stringToByteArray(String source, byte[] destination, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        table_.stringToByteArray(source, destination, offset, length, properties);
    }

    /**
     Converts the specified String into bytes.
     @param  system  The system object representing the system with which to connect.
     @param  source  The String to convert.
     @return  The destination byte array.
     **/
    public static byte[] stringToByteArray(AS400 system, String source)
    {
        try
        {
            return new Converter(system.getCcsid(), system).stringToByteArray(source);
        }
        catch (UnsupportedEncodingException e)
        {
            // This exception should never happen, since we are getting the CCSID from the system itself, and it only gets thrown if we pass a bad CCSID.
            Trace.log(Trace.ERROR, "Unexpected UnsupportedEncodingException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Converts the specified String into bytes.
     @param  ccsid  The CCSID of the i5/OS text.
     @param  system  The system object representing the system with which to connect.
     @param  source  The String to convert.
     @return  The destination byte array.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static byte[] stringToByteArray(int ccsid, AS400 system, String source) throws UnsupportedEncodingException
    {
        return new Converter(ccsid, system).stringToByteArray(source);
    }

    /**
     Converts the specified String into bytes.  If the Toolbox does not possess a table for the given CCSID, nor does the JVM, an UnsupportedEncodingException is thrown.
     @param  ccsid  The CCSID of the i5/OS text.
     @param  source  The String to convert.
     @return  The destination byte array.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public static byte[] stringToByteArray(int ccsid, String source) throws UnsupportedEncodingException
    {
        return new Converter(ccsid).stringToByteArray(source);
    }
}
