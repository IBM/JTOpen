///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CharConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
  A character set converter between Java String objects and AS/400 native code pages.
  <P>Note that in the past few releases, several constructors were deprecated because
  they did not accept an AS400 system object as an argument. Due to recent changes in 
  the behavior of the character conversion routines, this system object is no longer
  necessary.
  @see com.ibm.as400.access.AS400Text
 **/
public class CharConverter implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



  static final long serialVersionUID = 4L;

  private Converter table;

  static boolean faultTolerantConversion_ = false;                  // @B1A

  /**
    Constructs a CharConverter object using a "best guess" based on the default Locale.
   */
  public CharConverter()
  {
    table = new Converter();
  }

  /**
    Constructs a CharConverter object using the specified character encoding.
    @param  encoding  the name of a character encoding.
    @exception  UnsupportedEncodingException  If the <i>encoding</i> is not supported.
   */
  public CharConverter(String encoding) throws UnsupportedEncodingException
  {
    table = new Converter(encoding);
  }

  /**
    Constructs a CharConverter object using the specified ccsid.
    @param  ccsid  the CCSID of the AS/400 text.
    @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
   */
  public CharConverter(int ccsid) throws UnsupportedEncodingException
  {
    table = new Converter(ccsid);
  }

  /**
    Constructs a CharConverter object using the specified ccsid and system.
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
    try
    {
      Converter table = new Converter(system.getCcsid(), system);
      return table.byteArrayToString(source);
    }
    catch (UnsupportedEncodingException e)
    {
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


  //@F0A
  /**
    Converts a QSYS pathname String obtained from the IFS classes into a
    String suitable for use with other Toolbox services such as CommandCall
    and DataQueues.
    <P>
    This method is meant to handle QSYS pathnames and other string data that was retrieved
    using the IFS classes. Object names in QSYS are stored in EBCDIC. The file server always
    returns names to the Toolbox IFS classes in Unicode, so the server must convert the name
    from EBCDIC to Unicode before returning it to the client. The server does this conversion
    using CCSID 37, not the file server job CCSID; however, the name may contain
    variant (but legal) codepoints. Specifically, the three legal variant EBCDIC
    codepoints for QSYS object names are 0x5B, 0x7B, and 0x7C. If the name retrieved using the
    Toolbox IFS classes is given to another Toolbox component such as CommandCall, the name
    will be converted to EBCDIC using the job CCSID for that particular component. If variant
    characters exist in the name, the resulting name used by the host server job may not be
    the same as the original name.
    <P>
    Here is a typical scenario in which this method will be needed.
    The user profile name CASH$FLOW exists on the server. In EBCDIC CCSID 37, it is comprised
    of the codepoints:
    <PRE>
        0xC3 0xC1 0xE2 0xC8  <B>0x5B</B>  0xC6 0xD3 0xD6 0xE6
    </PRE>
    Note that the dollar sign '$' is codepoint 0x5B so it is one of
    the legal codepoints for a QSYS object pathname.
    Now, if this pathname is used in a CommandCall, such as "DLTUSRPRF CASH$FLOW", that 
    command string will get converted to the CCSID of the host server job. If the host server
    job isn't running under CCSID 37, the resulting command string may not contain the 
    dollar sign. For example, in CCSID 285 (United Kingdom) the codepoint 0x5B
    is actually an English pound sterling ('\u00A3' or Unicode 0x00A3). The dollar sign '$' is found at
    codepoint 0x4A instead.
    Hence, the "CASH$FLOW" in the command string will get converted to the following
    EBCDIC CCSID 285 codepoints:
    <PRE>
        0xC3 0xC1 0xE2 0xC8  <B>0x4A</B>  0xC6 0xD3 0xD6 0xE6
    </PRE>
    That is not how the user profile name is stored in QSYS. The 0x4A codepoint should really
    be a 0x5B codepoint. So in this case, the command server will return an error message
    indicating the user profile was not found.
    <P>
    The solution is to use this method to replace the variant codepoints with codepoints that
    will correctly convert given the host server job CCSID.
    When given the string "CASH$FLOW" and the CCSID 285, this method will return the string "CASH\u00A3FLOW".
    If the CommandCall is issued with the string "DLTUSRPRF CASH\u00A3FLOW" and the job CCSID of the
    remote command host server is 285, it will correctly convert the pound sterling '\u00A3' into
    codepoint 0x5B, which is how the user profile name "CASH$FLOW" is actually stored in QSYS.
    <P>
    For more information, please see <A HREF="http://publib.boulder.ibm.com/pubs/html/as400/v4r5/ic2924/info/RBAM6NAMEINCOM.HTM">iSeries Information Center: CL and APIs: Control Language (CL): Naming within commands</A>.
    @see #convertJobPathnameToIFSQSYSPathname
    @param qsysData the String in which to substitute variant QSYS characters.
    @param jobCCSID the CCSID of the job in which to convert the variant characters.
    @return the Unicode String with correctly substituted variant characters for use with host servers that convert based upon job CCSID.
    @exception  UnsupportedEncodingException  If the specified CCSID is not supported.
  **/
  public static String convertIFSQSYSPathnameToJobPathname(String qsysData, int jobCCSID) throws UnsupportedEncodingException
  {
    Converter hostTable = new Converter(jobCCSID, null);
    Converter qsysTable = new Converter(37, null);
    byte[] ebcdic37 = qsysTable.stringToByteArray(qsysData);
    String replaced = hostTable.byteArrayToString(ebcdic37);
    return replaced;
  }

  //@F0A
  /**
    Converts a pathname String obtained from a Toolbox host server (such as CommandCall
    or DataQueue) to a QSYS pathname suitable for use with the IFS classes.
    <P>
    See the javadoc for {@link #convertIFSQSYSPathnameToJobPathname convertIFSQSYSPathnameToJobPathname}
    for more information. This method essentially does the opposite of what convertIFSQSYSPathnameToJobPathname
    does. The specified <I>jobData</I> string has its variant characters substituted so that it can be used
    with the IFS classes. If given the String returned by this method, the file server will correctly convert
    the codepoints into the real QSYS object pathname using CCSID 37.
    @see #convertIFSQSYSPathnameToJobPathname
    @param jobData the String in which to substitute variant QSYS characters.
    @param jobCCSID the CCSID of the job in which to convert the variant characters.
    @return the Unicode String with correctly substituted variant characters for use with the IFS server that converts based upon CCSID 37.
    @exception  UnsupportedEncodingException  If the specified CCSID is not supported.
  **/  
  public static String convertJobPathnameToIFSQSYSPathname(String jobData, int jobCCSID) throws UnsupportedEncodingException
  {
    Converter hostTable = new Converter(jobCCSID, null);
    Converter qsysTable = new Converter(37, null);
    byte[] ebcdicHost = hostTable.stringToByteArray(jobData);
    String replaced = qsysTable.byteArrayToString(ebcdicHost);
    return replaced;
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
    try
    {
      Converter table = new Converter(system.getCcsid(), system);
      return table.stringToByteArray(source);
    }
    catch (UnsupportedEncodingException e)
    {
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

  /**
    Converts the specified String into bytes.
    If the Toolbox does not possess a table for the given CCSID, nor does the JVM,
    an UnsupportedEncodingException is thrown.

    @param  ccsid   the CCSID of the AS/400 text.
    @param  source  the String to convert.
    @return         the destination byte array.
    @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
   **/
  public static byte[] stringToByteArray(int ccsid, String source) throws UnsupportedEncodingException
  {
    Converter table = new Converter(ccsid);
    return table.stringToByteArray(source);
  }
}
