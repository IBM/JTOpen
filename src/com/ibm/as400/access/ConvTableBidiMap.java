///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConvTableBidiMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * This is the parent class for all ConvTableXXX classes that represent bidi ccsids.
**/
abstract class ConvTableBidiMap extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  char[] toUnicode_ = null;
  byte[] fromUnicode_ = null;

  // The Bidi Transformation object used to set bidi text characteristics
  private AS400BidiTransform abt;
  
  
  /**
   * Constructor.
  **/
  ConvTableBidiMap(int ccsid, char[] toUnicode, char[] fromUnicode)
  {
    super(ccsid);
    ccsid_ = ccsid;
    toUnicode_ = toUnicode;
    //@E4M: Moved decompressiong algorithm to parent class.
    if (Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Decompressing bidi single-byte conversion table for ccsid: " + ccsid_, fromUnicode.length);
    }
    fromUnicode_ = decompressSB(fromUnicode, (byte)0x3F); //@E4C

    abt = new AS400BidiTransform(ccsid);

    bidiStringType_ = AS400BidiTransform.getStringType(ccsid); //@P0A

    if (Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded bidi single-byte map for ccsid: " + ccsid_);
    }
  }
  
  
  /**
   * Perform an AS/400 CCSID to Unicode conversion.
  **/
  final String byteArrayToString(byte[] buf, int offset, int length, int type)    //@E0C @P0C
  {
    if (Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Bidi String Type: " + type);
      Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
    }

    char[] dest = new char[length];
    for (int i=0; i<length; dest[i] = toUnicode_[0x00FF & buf[offset + (i++)]]); // The 0x00FF is so we don't get any negative indices
    if (type != 0)                            //@E0A
      abt.setJavaStringType(type);           //@E0A
    
    if (Trace.traceOn_) //@E3A @P0C
    {
      Trace.log(Trace.CONVERSION, "Destination string (before java layout was applied) for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest)); //@E3A
    }
    
    return abt.toJavaLayout(String.copyValueOf(dest));
  }
    
  
  /**
   * Perform a Unicode to AS/400 CCSID conversion.
  **/
  final byte[] stringToByteArray(String source, int type)   //@E0C @P0C
  {
    if (type != 0)                            //@E0A
      abt.setJavaStringType(type);           //@E0A
    char[] src = abt.toAS400Layout(source).toCharArray();
    if (Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Bidi String Type: " + type);
      Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
    }
    byte[] dest = new byte[src.length];
    for (int i=0; i<src.length; dest[i] = fromUnicode_[src[i++]]);
     
    if (Trace.traceOn_) //@E3A @P0C
    {
      Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest); //@E3A
    }
    return dest;
  }
}


