///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ConvTableAsciiMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * This is the parent class for all ConvTableXXX classes that represent single-byte ASCII ccsids.
**/
abstract class ConvTableAsciiMap extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  char[] toUnicode_ = null;
  byte[] fromUnicode_ = null;


  /**
   * Constructor.
  **/
  ConvTableAsciiMap(int ccsid, char[] toUnicode, char[] fromUnicode)
  {
    super(ccsid);
    ccsid_ = ccsid;
    toUnicode_ = toUnicode;
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Decompressing single-byte ASCII conversion table for ccsid: " + ccsid_, fromUnicode.length);
    }
    fromUnicode_ = decompressSB(fromUnicode, (byte)0x1A);
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded single-byte ASCII map for ccsid: " + ccsid_);
    }
  }


  /**
   * Perform an OS/400 CCSID to Unicode conversion.
  **/
  final String byteArrayToString(byte[] buf, int offset, int length, int type) //@P0C
  {
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
    }
    char[] dest = new char[length];
    for (int i=0; i<length; dest[i] = toUnicode_[0x00FF & buf[offset + (i++)]]); // The 0x00FF is so we don't get any negative indices
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
    }
    return String.copyValueOf(dest);
  }


  /**
   * Perform a Unicode to OS/400 CCSID conversion.
  **/
  final byte[] stringToByteArray(String source, int type) //@P0C
  {
    char[] src = source.toCharArray();
    //@G0M - call char[] method
    return stringToByteArray(src, 0, src.length); //@G0A
  }


  //@G0A
  final byte[] stringToByteArray(char[] src, int offset, int length)
  {
    //@G0M
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, offset, length)); //@G0C
    }
    byte[] dest = new byte[length]; //@G0C
    for (int i=offset; i<length; dest[i] = fromUnicode_[src[i++]]); //@G0C
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
    }
    return dest;
  }
}


