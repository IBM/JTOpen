///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConvTableSingleMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * This is the parent class for all ConvTableXXX classes that represent single-byte ccsids.
**/
abstract class ConvTableSingleMap extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  
  char[] toUnicode_ = null;
  byte[] fromUnicode_ = null;
  
  
  /**
   * Constructor.
  **/
  ConvTableSingleMap(int ccsid, char[] toUnicode, char[] fromUnicode)
  {
    super(ccsid);
    ccsid_ = ccsid;
    toUnicode_ = toUnicode;
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Decompressing single-byte conversion table for ccsid: " + ccsid_, fromUnicode.length);
    }
    //@E4M: Moved decompression algorithm to parent.
    fromUnicode_ = decompressSB(fromUnicode, (byte)0x3F); //@E4C
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded single-byte map for ccsid: " + ccsid_);
    }
  }
  
  
  /**
   * Perform an AS/400 CCSID to Unicode conversion.
  **/
  String byteArrayToString(byte[] buf, int offset, int length, int type)   //$E0C
  {
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
    }
    char[] dest = new char[length];
    for (int i=0; i<length; dest[i] = toUnicode_[0x00FF & buf[offset + (i++)]]); // The 0x00FF is so we don't get any negative indices
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E3A
    {
      Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest)); //@E3A
    }
    return String.copyValueOf(dest);
  }
    
  
  /**
   * Perform a Unicode to AS/400 CCSID conversion.
  **/
  byte[] stringToByteArray(String source, int type)    //$E0C
  {
    char[] src = source.toCharArray();
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
    }
    byte[] dest = new byte[src.length];
    for (int i=0; i<src.length; dest[i] = fromUnicode_[src[i++]]);
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E3A
    {
      Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest); //@E3A
    }
    return dest;
  }
}


