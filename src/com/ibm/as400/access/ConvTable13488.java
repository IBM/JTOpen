///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable13488.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class ConvTable13488 extends ConvTable // instead of ConvTableDoubleMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  
  ConvTable13488()
  {
    super(13488);
  }
  
  
  /**
   * Perform an AS/400 CCSID to Unicode conversion.
  **/
  String byteArrayToString(byte[] buf, int offset, int length, int type)    //$E0C
  {
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E1A
    {
      Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length); //@E1A
    }
    char[] dest = new char[length/2];
    for (int i=0; i<length/2; ++i)
    {
      dest[i] = (char)(((0x00FF & buf[(i*2)+offset]) << 8) + (0x00FF & buf[(i*2)+1+offset])); //@E1C
    }
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E1A
    {
      Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest)); //@E1A
    }
    return String.copyValueOf(dest);
  }
    
  
  /**
   * Perform a Unicode to AS/400 CCSID conversion.
  **/
  byte[] stringToByteArray(String source, int type)       //$E0C
  {
    char[] src = source.toCharArray();
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E1A
    {
      Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src)); //@E1A
    }
    byte[] dest = new byte[src.length*2];
    for (int i=0; i<src.length; ++i)
    {
      dest[i*2] = (byte)(src[i] >>> 8);
      dest[i*2+1] = (byte)(0x00FF & src[i]);
    }
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E1A
    {
      Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest); //@E1A
    }
    return dest;
  }
}
