///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTableDoubleMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * This is the parent class for all ConvTableXXX classes that represent double-byte ccsids.
**/
abstract class ConvTableDoubleMap extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  char[] toUnicode_ = null;
  char[] fromUnicode_ = null;


  /**
   * Constructor.
  **/
  ConvTableDoubleMap(int ccsid, char[] toUnicode, char[] fromUnicode)
  {
    super(ccsid);
    toUnicode_ = decompress(toUnicode);
    fromUnicode_ = decompress(fromUnicode);
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded double-byte map for ccsid: " + ccsid_);
    }
  }


  /**
   * Helper method used to decompress conversion tables when they are initialized.
  **/
  char[] decompress(char[] arr)
  {
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Decompressing double-byte conversion table for ccsid: " + ccsid_, arr.length);
    }
    char[] buf = new char[65536];
    int c = 0;
    for (int i=0; i<arr.length; ++i)
    {
      if (arr[i] == cic_)
      {
        if (arr[i+1] == pad_)
        {
          buf[c++] = arr[i++];
        }
        else
        {
          long max = (0xFFFF & arr[i+1]) + (0xFFFF & c);
          char ch = arr[i+2];
          while (c < max)
          {
            buf[c++] = ch;
          }
          i += 2;
        }
      }
      else if (arr[i] == ric_)
      {
        if (arr[i+1] == pad_)
        {
          buf[c++] = arr[i++];
        }
        else
        {
          int start = (0xFFFF & arr[i+2]);
          int num = (0xFFFF & arr[i+1]);
          for (int j=start; j<(num+start); ++j)
          {
            buf[c++] = (char)j;
          }
          i += 2;
        }
      }
      else if (arr[i] == hbic_)
      {
        if (arr[i+1] == pad_)
        {
          buf[c++] = arr[i++];
        }
        else
        {
          int hbNum = (0x0000FFFF & arr[++i]);
          char firstChar = arr[++i];
          char highByteMask = (char)(0xFF00 & firstChar);
          buf[c++] = firstChar;
          ++i;
          for (int j=0; j<hbNum; ++j)
          {
            char both = arr[i+j];
            buf[c++] = (char)(highByteMask + ((0xFF00 & both) >>> 8));
            buf[c++] = (char)(highByteMask + (0x00FF & both));
          }
          i = i + hbNum - 1;
        }
      }
      else
      { // regular character
        buf[c++] = arr[i];
      }            
    }

    return buf;
  }


  /**
   * Perform an AS/400 CCSID to Unicode conversion.
  **/
  String byteArrayToString(byte[] buf, int offset, int length, int type)    //$E0C
  {
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E2C
    {
      Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
    }
    char[] dest = new char[length/2];
    for(int i=0; i<length/2; ++i)
    {
      dest[i] = toUnicode_[((0x00FF & buf[(i*2)+offset]) << 8) + (0x00FF & buf[(i*2)+1+offset])]; //@E3C
    }
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
    byte[] dest = new byte[src.length*2];
    for(int i=0; i<src.length; ++i)
    {
      dest[i*2] = (byte)(fromUnicode_[src[i]] >>> 8);
      dest[i*2+1] = (byte)(0x00FF & fromUnicode_[src[i]]);
    }
    if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@E3A
    {
      Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest); //@E3A
    }
    return dest;
  }
}


