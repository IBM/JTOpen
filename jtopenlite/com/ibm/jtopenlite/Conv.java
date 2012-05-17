///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Conv.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.util.*;
import java.math.*;

import com.ibm.jtopenlite.ccsidConversion.CcsidConversion;

/**
 * Utility class for converting data from one format to another.
**/
public final class Conv
{
  private static final char[] NUM = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
  private static final byte[] CHAR_HIGH = new byte[10];
  private static final byte[] CHAR_LOW = new byte[10];
  static
  {
    for (int i=0; i<=9; ++i)
    {
      int val = i;
      CHAR_HIGH[i] = (byte)(val << 4);
      CHAR_LOW[i] = (byte)val;
    }
  }

  // The array offset is the Unicode character value, the array value is the EBCDIC 37 value.
  // e.g. CONV_TO_37['0'] == 0xF0  and  CONV_TO_37[' '] == 0x40
  private static final byte[] CONV_TO_37 = new byte[65536];

  private static final byte[] INIT_TO_37 = new byte[]
  {
    0x00, 0x01, 0x02, 0x03, 0x37, 0x2D, 0x2E, 0x2F, 0x16, 0x05, 0x25, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x3C, 0x3D, 0x32, 0x26, 0x18, 0x19, 0x3F, 0x27, 0x1C, 0x1D, 0x1E, 0x1F,
    0x40, 0x5A, 0x7F, 0x7B, 0x5B, 0x6C, 0x50, 0x7D, 0x4D, 0x5D, 0x5C, 0x4E, 0x6B, 0x60, 0x4B, 0x61,
    (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, (byte)0xF8, (byte)0xF9, 0x7A, 0x5E, 0x4C, 0x7E, 0x6E, 0x6F,
    0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, (byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6,
    (byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0xBA, (byte)0xE0, (byte)0xBB, (byte)0xB0, 0x6D,
    0x79, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87, (byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96,
    (byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, (byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, 0x4F, (byte)0xD0, (byte)0xA1, 0x07,
    0x20, 0x21, 0x22, 0x23, 0x24, 0x15, 0x06, 0x17, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x09, 0x0A, 0x1B,
    0x30, 0x31, 0x1A, 0x33, 0x34, 0x35, 0x36, 0x08, 0x38, 0x39, 0x3A, 0x3B, 0x04, 0x14, 0x3E, (byte)0xFF,
    0x41, (byte)0xAA, 0x4A, (byte)0xB1, (byte)0x9F, (byte)0xB2, 0x6A, (byte)0xB5, (byte)0xBD, (byte)0xB4, (byte)0x9A, (byte)0x8A, 0x5F, (byte)0xCA, (byte)0xAF, (byte)0xBC,
    (byte)0x90, (byte)0x8F, (byte)0xEA, (byte)0xFA, (byte)0xBE, (byte)0xA0, (byte)0xB6, (byte)0xB3, (byte)0x9D, (byte)0xDA, (byte)0x9B, (byte)0x8B, (byte)0xB7, (byte)0xB8, (byte)0xB9, (byte)0xAB,
    0x64, 0x65, 0x62, 0x66, 0x63, 0x67, (byte)0x9E, 0x68, 0x74, 0x71, 0x72, 0x73, 0x78, 0x75, 0x76, 0x77,
    (byte)0xAC, 0x69, (byte)0xED, (byte)0xEE, (byte)0xEB, (byte)0xEF, (byte)0xEC, (byte)0xBF, (byte)0x80, (byte)0xFD, (byte)0xFE, (byte)0xFB, (byte)0xFC, (byte)0xAD, (byte)0xAE, 0x59,
    0x44, 0x45, 0x42, 0x46, 0x43, 0x47, (byte)0x9C, 0x48, 0x54, 0x51, 0x52, 0x53, 0x58, 0x55, 0x56, 0x57,
    (byte)0x8C, 0x49, (byte)0xCD, (byte)0xCE, (byte)0xCB, (byte)0xCF, (byte)0xCC, (byte)0xE1, 0x70, (byte)0xDD, (byte)0xDE, (byte)0xDB, (byte)0xDC, (byte)0x8D, (byte)0x8E, (byte)0xDF
  };

  // The array offset is the EBCDIC 37 character value, the array value is the Unicode value.
  // e.g. CONV_FROM_37[0xF0] == '0'  and  CONV_FROM_37[0x40] == ' '
  private static final char[] CONV_FROM_37 = new char[]
  {
    0x0000, 0x0001, 0x0002, 0x0003, 0x009C, 0x0009, 0x0086, 0x007F, 0x0097, 0x008D, 0x008E, 0x000B, 0x000C, 0x000D, 0x000E, 0x000F,
    0x0010, 0x0011, 0x0012, 0x0013, 0x009D, 0x0085, 0x0008, 0x0087, 0x0018, 0x0019, 0x0092, 0x008F, 0x001C, 0x001D, 0x001E, 0x001F,
    0x0080, 0x0081, 0x0082, 0x0083, 0x0084, 0x000A, 0x0017, 0x001B, 0x0088, 0x0089, 0x008A, 0x008B, 0x008C, 0x0005, 0x0006, 0x0007,
    0x0090, 0x0091, 0x0016, 0x0093, 0x0094, 0x0095, 0x0096, 0x0004, 0x0098, 0x0099, 0x009A, 0x009B, 0x0014, 0x0015, 0x009E, 0x001A,
    0x0020, 0x00A0, 0x00E2, 0x00E4, 0x00E0, 0x00E1, 0x00E3, 0x00E5, 0x00E7, 0x00F1, 0x00A2, 0x002E, 0x003C, 0x0028, 0x002B, 0x007C,
    0x0026, 0x00E9, 0x00EA, 0x00EB, 0x00E8, 0x00ED, 0x00EE, 0x00EF, 0x00EC, 0x00DF, 0x0021, 0x0024, 0x002A, 0x0029, 0x003B, 0x00AC,
    0x002D, 0x002F, 0x00C2, 0x00C4, 0x00C0, 0x00C1, 0x00C3, 0x00C5, 0x00C7, 0x00D1, 0x00A6, 0x002C, 0x0025, 0x005F, 0x003E, 0x003F,
    0x00F8, 0x00C9, 0x00CA, 0x00CB, 0x00C8, 0x00CD, 0x00CE, 0x00CF, 0x00CC, 0x0060, 0x003A, 0x0023, 0x0040, 0x0027, 0x003D, 0x0022,
    0x00D8, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x00AB, 0x00BB, 0x00F0, 0x00FD, 0x00FE, 0x00B1,
    0x00B0, 0x006A, 0x006B, 0x006C, 0x006D, 0x006E, 0x006F, 0x0070, 0x0071, 0x0072, 0x00AA, 0x00BA, 0x00E6, 0x00B8, 0x00C6, 0x00A4,
    0x00B5, 0x007E, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007A, 0x00A1, 0x00BF, 0x00D0, 0x00DD, 0x00DE, 0x00AE,
    0x005E, 0x00A3, 0x00A5, 0x00B7, 0x00A9, 0x00A7, 0x00B6, 0x00BC, 0x00BD, 0x00BE, 0x005B, 0x005D, 0x00AF, 0x00A8, 0x00B4, 0x00D7,
    0x007B, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x00AD, 0x00F4, 0x00F6, 0x00F2, 0x00F3, 0x00F5,
    0x007D, 0x004A, 0x004B, 0x004C, 0x004D, 0x004E, 0x004F, 0x0050, 0x0051, 0x0052, 0x00B9, 0x00FB, 0x00FC, 0x00F9, 0x00FA, 0x00FF,
    0x005C, 0x00F7, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005A, 0x00B2, 0x00D4, 0x00D6, 0x00D2, 0x00D3, 0x00D5,
    0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x00B3, 0x00DB, 0x00DC, 0x00D9, 0x00DA, 0x009F
  };

  private static final String[] CACHE_FROM_37 = new String[256];
  private static final boolean cacheFrom37Init_;

  static
  {
    System.arraycopy(INIT_TO_37, 0, CONV_TO_37, 0, INIT_TO_37.length);
    for (int i=INIT_TO_37.length; i<CONV_TO_37.length; ++i)
    {
      CONV_TO_37[i] = 0x3F;
    }
    char[] buffer = new char[1];
    byte[] data = new byte[1];
    for (int i=0; i<CACHE_FROM_37.length; ++i)
    {
      data[0] = (byte)i;
      CACHE_FROM_37[i] = ebcdicByteArrayToString(data, 0, 1, buffer);
    }
    cacheFrom37Init_ = true;
  }

  private Conv()
  {
  }


  /**
   * Converts the specified bytes into their hexadecimal String representation.
  **/
  public static final String bytesToHexString(final byte[] data, final int offset, final int length)
  {
    return bytesToHexString(data, offset, length, new char[length*2]);
  }

  /**
   * Converts the specified bytes into their hexadecimal String representation.
  **/
  public static final String bytesToHexString(final byte[] data, final int offset, final int length, final char[] buffer)
  {
    final int numChars = length*2;
    int count = numChars;
    for (int i=offset+length-1; i>=offset; --i)
    {
      int low = data[i] & 0x000F;
      int high = (data[i] >> 4) & 0x000F;
      buffer[--count] = NUM[low];
      buffer[--count] = NUM[high];
    }
    return new String(buffer, 0, numChars);
  }

  /**
   * Converts the specified hexadecimal String into its constituent byte values.
  **/
  public static final byte[] hexStringToBytes(final String value)
  {
    int len = value.length();
    /* this comparison works with negative numbers */
    if (len % 2 != 0) ++len;
    final byte[] data = new byte[len>>1];
    hexStringToBytes(value, data, 0);
    return data;
  }

  /**
   * Converts the specified hexadecimal String into its constituent byte values.
  **/
  public static final int hexStringToBytes(final String value, final byte[] data, final int offset)
  {
    final int len = value.length();
    final int odd = len % 2;
    if (odd == 1)
    {
      data[offset] = 0;
    }
    for (int i=0; i<len; ++i)
    {
      final char c = value.charAt(i);
      int val = 0;
      if (c >= '0' && c <= '9')
      {
        val = c - '0';
      }
      else if (c >= 'A' && c <= 'F')
      {
        val = c - 'A' + 10;
      }
      else if (c >= 'a' && c <= 'f')
      {
        val = c - 'a' + 10;
      }
      final int arrOff = offset + ((i+odd)>>1);
      data[arrOff] = i % 2 == odd ? (byte)(val << 4) : (byte)(data[arrOff] | val);
    }
    final int num = len >> 1;
    return odd == 0 ? num : num+1;
  }


  /**
   * Converts the specified String into CCSID 37 bytes.
  **/
  public static final byte[] stringToEBCDICByteArray37(final String s)
  {
    final byte[] b = new byte[s.length()];
    stringToEBCDICByteArray37(s, b, 0);
    return b;
  }

  /**
   * Converts the specified String into CCSID 37 bytes.
  **/
  public static final int stringToEBCDICByteArray37(final String s, final byte[] data, final int offset)
  {
      return stringToEBCDICByteArray37(s, s.length(), data, offset);
  }


  public static final int stringToEBCDICByteArray37(final String s, int length,  final byte[] data, final int offset)
  {
	int sLength = s.length();
	if (sLength < length) {
		length = sLength;
	}
    final int stop = offset+length;
    for (int i=offset; i<stop; ++i)
    {
      data[i] = CONV_TO_37[s.charAt(i-offset)];
    }
    return length;
  }


  /**
   * Converts the specified String into the appropriate byte values for the specified CCSID.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final byte[] stringToEBCDICByteArray(final String s, final int ccsid) throws UnsupportedEncodingException
  {
    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37) return stringToEBCDICByteArray37(s);
    String encoding = encodings_[ccsidToUse];
    if (encoding != null)
    {
    	try {
    		return s.getBytes(encoding);
    	} catch (UnsupportedEncodingException ex) {
    		encodings_[ccsidToUse] = null;
    	}
    }
    return CcsidConversion.stringToEBCDICByteArray(s, ccsidToUse);

  }

  /**
   * Converts the specified String into the appropriate byte values for the specified CCSID.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final int stringToEBCDICByteArray(final String s, final byte[] data, final int offset, final int ccsid) throws UnsupportedEncodingException
  {
    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37) return stringToEBCDICByteArray37(s, data, offset);
    String encoding = encodings_[ccsidToUse];
    if (encoding != null)
    {
    	try {
      // BOOOO!
      byte[] b = s.getBytes(encoding);
      System.arraycopy(b, 0, data, offset, b.length);
      return b.length;
    	} catch (UnsupportedEncodingException ex) {
    		encodings_[ccsidToUse] = null;
    	}
    }
    return CcsidConversion.stringToEBCDICByteArray(s, data, offset, ccsidToUse);

  }

  /**
   * Converts the specified String into the appropriate byte values for the specified CCSID.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final int stringToEBCDICByteArray(final String s, int length, final byte[] data, final int offset, final int ccsid) throws UnsupportedEncodingException
  {
	  int sLength = s.length();
	  if (length > sLength) length = sLength;

    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37) return stringToEBCDICByteArray37(s, length, data, offset);
    String encoding = encodings_[ccsidToUse];
    if (encoding != null)
    {
    	try {
    	      // BOOOO!

      byte[] b = s.substring(0,length).getBytes(encoding);
      System.arraycopy(b, 0, data, offset, b.length);
      return b.length;
    	} catch (UnsupportedEncodingException ex) {
    		encodings_[ccsidToUse] = null;
    	}
    }
    return CcsidConversion.stringToEBCDICByteArray(s, length,  data, offset, ccsidToUse);

  }


  /**
   * Converts the specified String into Unicode bytes.
   * returns the number of bytes.
  **/
  public static final byte[] stringToUnicodeByteArray(final String s)
  {
    final byte[] b = new byte[s.length()*2];
    stringToUnicodeByteArray(s, b, 0);
    return b;
  }

  /**
   * Converts the specified String into Unicode bytes.
  **/
  public static final int stringToUnicodeByteArray(final String s, final byte[] data, final int offset)
  {
      return stringToUnicodeByteArray(s, s.length(), data, offset);
  }


  public static final int stringToUnicodeByteArray(final String s, int length, final byte[] data, final int offset)
  {
    for (int i=0; i<length; ++i)
    {
      char c = s.charAt(i);
      byte high = (byte)(c >> 8);
      byte low = (byte)c;
      data[offset+(i*2)] = high;
      data[offset+(i*2)+1] = low;
    }
    return length*2;
  }

  /* Version that pads with spaces */
  public static void stringToUnicodeByteArray(String s, byte[] data, int offset, int byteLength) {
    int sLength = s.length();

    for (int i=0; i<byteLength / 2; ++i)
    {
      char c;
      if (i < sLength) {
       c = s.charAt(i);
      } else {
        c = ' ';
      }
      byte high = (byte)(c >> 8);
      byte low = (byte)c;
      data[offset+(i*2)] = high;
      data[offset+(i*2)+1] = low;
    }

  }

  public static final int stringToUtf8ByteArray(final String s, int length, final byte[] data, final int offset)
  {
	  int sLength = s.length();
	  if (length > sLength) length = sLength;

    	      // BOOOO!

      byte[] b ;
      try {
      b = s.substring(0,length).getBytes("UTF-8");
      System.arraycopy(b, 0, data, offset, b.length);
      return b.length;
      } catch (UnsupportedEncodingException uee) {
    	  // should never happen
    	  return 0;
      }


  }



  /**
   * Converts the specified String into Unicode bytes, padding the byte array with Unicode
   * spaces (0x0020) up to <i>length</i> bytes.
  **/
  public static final void stringToBlankPadUnicodeByteArray(final String s,
		  final byte[] data, final int offset, final int length)
  {
    int counter = 0;
    if (s != null)
    {
      for (int i=0; i<s.length() && (counter+2)<=length; ++i)
      {
        byte high = (byte)(s.charAt(i) >> 8);
        byte low = (byte)s.charAt(i);
        data[offset+counter] = high;
        ++counter;
        data[offset+counter] = low;
        ++counter;
      }
    }
    while ((counter+2) <= length)
    {
      data[offset+counter] = 0x00;
      ++counter;
      data[offset+counter] = 0x20;
      ++counter;
    }
  }

  /**
   * Converts the specified Unicode bytes into a String.
   * The length is in bytes, and should be twice the length of the returned String.
  **/
  public static final String unicodeByteArrayToString(final byte[] data, final int offset, final int length)
  {
    char[] buf = new char[length];
    return unicodeByteArrayToString(data, offset, length, buf);
  }

  /**
   * Converts the specified Unicode bytes into a String.
   * The length is in bytes, and should be twice the length of the returned String.
  **/
  public static final String unicodeByteArrayToString(final byte[] data, final int offset, final int length, final char[] buffer)
  {
    final int numChars = length/2;
    int count = numChars;
    for (int i=offset+length-1; i>=offset; i-=2)
    {
      int low = data[i] & 0x00FF;
      int high = data[i-1] & 0x00FF;
      char c = (char)((high << 8) | low);
      buffer[--count] = c;
    }
    return new String(buffer, 0, numChars);
  }

  /**
   * Converts the specified String into CCSID 37 bytes, padding the byte array with EBCDIC spaces (0x40) up to <i>length</i> bytes.
  **/
  public static final void stringToBlankPadEBCDICByteArray(final String s, final byte[] data, final int offset, final int length)
  {
    for (int i=0; i<s.length() && i<length; ++i)
    {
      data[offset+i] = CONV_TO_37[s.charAt(i)];
    }
    for (int i=s.length(); i<length; ++i)
    {
      data[offset+i] = 0x40;
    }
  }

  /**
   * Converts the specified String into bytes for the specified CCSID, padding the byte array with spaces up to <i>length</i> bytes.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final void stringToBlankPadEBCDICByteArray(final String s, final byte[] data, final int offset, final int length, final int ccsid) throws UnsupportedEncodingException
  {
    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37)
    {
      stringToBlankPadEBCDICByteArray(s, data, offset, length);
    }
    else
    {
      String encoding = encodings_[ccsidToUse];
      if (encoding != null)
      {
        // BOOOO!
        byte[] b = s.getBytes(encoding);
        int len = b.length;
        int total = len < length ? len : length;
        System.arraycopy(b, 0, data, offset, total);
        int rem = length-len;
        if (rem > 0)
        {
          final byte[] blank = " ".getBytes(encoding);
          while (rem > 0)
          {
            System.arraycopy(blank, 0, data, offset+total, blank.length);
            total += blank.length;
            rem = rem - blank.length;
          }
        }
      }
      else
      {
        throw new UnsupportedEncodingException("CCSID "+ccsidToUse);
      }
    }
  }


  /**
   * Converts the specified CCSID 37 bytes into a String.
   * Note: You might as well just use new String(data,"Cp037") to avoid the extra char array this method needs to create.
   * Note: You cannot use new String(data,"Cp037" because this is not supported on all JVMS
  **/
  public static final String ebcdicByteArrayToString(final byte[] data, final int offset, final int length)
  {
    if (length == 1 && cacheFrom37Init_) return CACHE_FROM_37[data[offset] & 0x00FF];
    return ebcdicByteArrayToString(data, offset, length, new char[length]);
  }

  public static final String ebcdicByteArrayToString(final byte[] data,  final char[] buffer) {
    int offset = 0;
    int length = data.length;
    return ebcdicByteArrayToString(data, offset, length, buffer);
  }

  // @csmith: Perf testing:
  // This uses the same amount of memory as new String(data, "Cp037")
  // but is about 2x faster on IBM 1.5 Windows 32-bit and about 3x faster on Sun 1.4 Windows 32-bit.
  /**
   * Converts the specified CCSID 37 bytes into a String.
  **/
  public static final String ebcdicByteArrayToString(final byte[] data, final int offset, final int length, final char[] buffer)
  {
    if (length == 1 && cacheFrom37Init_) return CACHE_FROM_37[data[offset] & 0x00FF];
    int counter = length;
    for (int i=offset+length-1; i>=offset; --i)
    {
      buffer[--counter] = CONV_FROM_37[data[i] & 0x00FF];
    }
    return new String(buffer, 0, length);
  }

  // Conversion maps copied from Toolbox/JTOpen.
  private static final HashMap encodingCcsid_ = new HashMap();
  private static final HashMap ccsidEncoding_ = new HashMap();

  private static final String[] encodings_ = new String[65536];

  static
  {
    // 137+ possible Java encodings. 13 have unknown CCSIDs.
    // We have 128 known in this table.
    encodingCcsid_.put("ASCII",         "367");  // ANSI X.34 ASCI.
    encodingCcsid_.put("Cp1252",        "1252");
    encodingCcsid_.put("ISO8859_1",     "819");
    encodingCcsid_.put("Unicode",       "13488");
    encodingCcsid_.put("UnicodeBig",    "13488");  // BOM is 0xFEFF.
    // encodingCcsid_.put("UnicodeBigUnmarked", 13488);
    encodingCcsid_.put("UnicodeLittle", "1202"); // BOM is 0xFFFE.
    // encodingCcsid_.put("UnicodeLittleUnmarked", 13488);
    encodingCcsid_.put("UTF8",          "1208");
    encodingCcsid_.put("UTF-8",         "1208");
    encodingCcsid_.put("UTF-16BE",      "1200");

    encodingCcsid_.put("Big5",      "950");
    // encodingCcsid_.put("Big5 HKSCS", ???); // Big5 with Hong Kong extensions.
    encodingCcsid_.put("CNS11643",  "964");
    encodingCcsid_.put("Cp037",     "37");
    encodingCcsid_.put("Cp256",     "256");
    encodingCcsid_.put("Cp273",     "273");
    encodingCcsid_.put("Cp277",     "277");
    encodingCcsid_.put("Cp278",     "278");
    encodingCcsid_.put("Cp280",     "280");
    encodingCcsid_.put("Cp284",     "284");
    encodingCcsid_.put("Cp285",     "285");
    encodingCcsid_.put("Cp290",     "290");
    encodingCcsid_.put("Cp297",     "297");
    encodingCcsid_.put("Cp420",     "420");
    encodingCcsid_.put("Cp423",     "423");
    encodingCcsid_.put("Cp424",     "424");
    encodingCcsid_.put("Cp437",     "437");
    encodingCcsid_.put("Cp500",     "500");
    encodingCcsid_.put("Cp737",     "737");
    encodingCcsid_.put("Cp775",     "775");
    encodingCcsid_.put("Cp833",     "833");
    encodingCcsid_.put("Cp838",     "838");
    encodingCcsid_.put("Cp850",     "850");
    encodingCcsid_.put("Cp852",     "852");
    encodingCcsid_.put("Cp855",     "855");
    encodingCcsid_.put("Cp856",     "856");
    encodingCcsid_.put("Cp857",     "857");
    encodingCcsid_.put("Cp858",     "858");
    encodingCcsid_.put("Cp860",     "860");
    encodingCcsid_.put("Cp861",     "861");
    encodingCcsid_.put("Cp862",     "862");
    encodingCcsid_.put("Cp863",     "863");
    encodingCcsid_.put("Cp864",     "864");
    encodingCcsid_.put("Cp865",     "865");
    encodingCcsid_.put("Cp866",     "866");
    encodingCcsid_.put("Cp868",     "868");
    encodingCcsid_.put("Cp869",     "869");
    encodingCcsid_.put("Cp870",     "870");
    encodingCcsid_.put("Cp871",     "871");
    encodingCcsid_.put("Cp874",     "874");
    encodingCcsid_.put("Cp875",     "875");
    encodingCcsid_.put("Cp880",     "880");
    encodingCcsid_.put("Cp905",     "905");
    encodingCcsid_.put("Cp918",     "918");
    encodingCcsid_.put("Cp921",     "921");
    encodingCcsid_.put("Cp922",     "922");
    encodingCcsid_.put("Cp923",     "923");  // IBM Latin-9.
    encodingCcsid_.put("Cp924",     "924");
    encodingCcsid_.put("Cp930",     "930");
    encodingCcsid_.put("Cp933",     "933");
    encodingCcsid_.put("Cp935",     "935");
    encodingCcsid_.put("Cp937",     "937");
    encodingCcsid_.put("Cp939",     "939");
    encodingCcsid_.put("Cp942",     "942");
    // encodingCcsid_.put("Cp942C",    ???);  // Don't know the CCSID - unclear what the 'C' means.
    encodingCcsid_.put("Cp943",     "943");
    // encodingCcsid_.put("Cp943C",    ???); // Don't know the CCSID - unclear what the 'C' means.
    encodingCcsid_.put("Cp948",     "948");
    encodingCcsid_.put("Cp949",     "949");
    // encodingCcsid_.put("Cp949C",    ???); // Don't know the CCSID - unclear what the 'C' means.
    encodingCcsid_.put("Cp950",     "950");
    encodingCcsid_.put("Cp964",     "964");
    encodingCcsid_.put("Cp970",     "970");
    encodingCcsid_.put("Cp1006",   "1006");
    encodingCcsid_.put("Cp1025",   "1025");
    encodingCcsid_.put("Cp1026",   "1026");
    encodingCcsid_.put("Cp1027",   "1027");
    encodingCcsid_.put("Cp1046",   "1046");
    encodingCcsid_.put("Cp1097",   "1097");
    encodingCcsid_.put("Cp1098",   "1098");
    encodingCcsid_.put("Cp1112",   "1112");
    encodingCcsid_.put("Cp1122",   "1122");
    encodingCcsid_.put("Cp1123",   "1123");
    encodingCcsid_.put("Cp1124",   "1124");
    encodingCcsid_.put("Cp1130",   "1130");
    encodingCcsid_.put("Cp1132",   "1132");
    encodingCcsid_.put("Cp1137",   "1137");
    encodingCcsid_.put("Cp1140",   "1140");
    encodingCcsid_.put("Cp1141",   "1141");
    encodingCcsid_.put("Cp1142",   "1142");
    encodingCcsid_.put("Cp1143",   "1143");
    encodingCcsid_.put("Cp1144",   "1144");
    encodingCcsid_.put("Cp1145",   "1145");
    encodingCcsid_.put("Cp1146",   "1146");
    encodingCcsid_.put("Cp1147",   "1147");
    encodingCcsid_.put("Cp1148",   "1148");
    encodingCcsid_.put("Cp1149",   "1149");
    encodingCcsid_.put("Cp1153",   "1153");
    encodingCcsid_.put("Cp1154",   "1154");
    encodingCcsid_.put("Cp1155",   "1155");
    encodingCcsid_.put("Cp1156",   "1156");
    encodingCcsid_.put("Cp1157",   "1157");
    encodingCcsid_.put("Cp1158",   "1158");
    encodingCcsid_.put("Cp1160",   "1160");
    encodingCcsid_.put("Cp1164",   "1164");
    encodingCcsid_.put("Cp1250",   "1250");
    encodingCcsid_.put("Cp1251",   "1251");
    encodingCcsid_.put("Cp1253",   "1253");
    encodingCcsid_.put("Cp1254",   "1254");
    encodingCcsid_.put("Cp1255",   "1255");
    encodingCcsid_.put("Cp1256",   "1256");
    encodingCcsid_.put("Cp1257",   "1257");
    encodingCcsid_.put("Cp1258",   "1258");
    encodingCcsid_.put("Cp1364",   "1364");
    encodingCcsid_.put("Cp1381",   "1381");
    encodingCcsid_.put("Cp1383",   "1383");
    encodingCcsid_.put("Cp1388",   "1388");
    encodingCcsid_.put("Cp1399",   "1399");
    encodingCcsid_.put("Cp4971",   "4971");
    encodingCcsid_.put("Cp5123",   "5123");
    encodingCcsid_.put("Cp9030",   "9030");
    encodingCcsid_.put("Cp13121", "13121");
    encodingCcsid_.put("Cp13124", "13124");
    encodingCcsid_.put("Cp28709", "28709");
    encodingCcsid_.put("Cp33722", "33722");

    // The Toolbox does not directly support EUC at this time, Java will do the conversion.
    encodingCcsid_.put("EUC_CN", "1383");  // Superset of 5479.
    encodingCcsid_.put("EUC_JP", "33722");
    encodingCcsid_.put("EUC_KR", "970");  // Superset of 5066.
    encodingCcsid_.put("EUC_TW", "964");  // Superset of 5060.

    encodingCcsid_.put("GB2312", "1381");
    encodingCcsid_.put("GB18030", "1392"); //1392 is mixed 4-byte; the individual component CCSIDs are not supported.
    encodingCcsid_.put("GBK",    "1386");

    // encodingCcsid_.put("ISCII91", ???); // Indic scripts.

    // The Toolbox does not directly support ISO2022.
    // encodingCcsid_.put("ISO2022CN",     ???);  // Not sure of the CCSID, possibly 9575?
    // encodingCcsid_.put("ISO2022CN_CNS", "965");  // Java doesn't support this one?
    // encodingCcsid_.put("ISO2022CN_GB",  "9575");  // Java doesn't support this one?

    encodingCcsid_.put("ISO2022JP", "5054"); // Could be 956 also, but the IBM i JVM uses 5054.
    encodingCcsid_.put("ISO2022KR", "25546"); // Could be 17354 also, but the IBM i JVM uses 25546.

    encodingCcsid_.put("ISO8859_2", "912");
    encodingCcsid_.put("ISO8859_3", "913");
    encodingCcsid_.put("ISO8859_4", "914");
    encodingCcsid_.put("ISO8859_5", "915");
    encodingCcsid_.put("ISO8859_6", "1089");
    encodingCcsid_.put("ISO8859_7", "813");
    encodingCcsid_.put("ISO8859_8", "916");
    encodingCcsid_.put("ISO8859_9", "920");
    // encodingCcsid_.put("ISO8859_13", ???);  // Latin alphabet No. 7.
    // encodingCcsid_.put("ISO8859_15_FDIS", ???); // Don't know the CCSID; FYI, this codepage is ISO 28605.

    // The Toolbox does not directly support JIS.
    encodingCcsid_.put("JIS0201",       "897"); // Could be 895, but the IBM i JVM uses 897.
    encodingCcsid_.put("JIS0208",       "952");
    encodingCcsid_.put("JIS0212",       "953");
    // encodingCcsid_.put("JISAutoDetect", ???); // Can't do this one. Would need to look at the bytes to determine the CCSID.

    encodingCcsid_.put("Johab",  "1363");
    encodingCcsid_.put("KOI8_R", "878");
    encodingCcsid_.put("KSC5601", "949");

    encodingCcsid_.put("MS874", "874");
    encodingCcsid_.put("MS932", "943");
    encodingCcsid_.put("MS936", "1386");
    encodingCcsid_.put("MS949", "949");
    encodingCcsid_.put("MS950", "950");

    // encodingCcsid_.put("MacArabic", ???); // Don't know.
    encodingCcsid_.put("MacCentralEurope", "1282");
    encodingCcsid_.put("MacCroatian", "1284");
    encodingCcsid_.put("MacCyrillic", "1283");
    // encodingCcsid_.put("MacDingbat", ???); // Don't know.
    encodingCcsid_.put("MacGreek", "1280");
    // encodingCcsid_.put("MacHebrew", ???); // Don't know.
    encodingCcsid_.put("MacIceland", "1286");
    encodingCcsid_.put("MacRoman", "1275");
    encodingCcsid_.put("MacRomania", "1285");
    // encodingCcsid_.put("MacSymbol", ???); // Don't know.
    // encodingCcsid_.put("MacThai", ???); // Don't know.
    encodingCcsid_.put("MacTurkish", "1281");
    // encodingCcsid_.put("MacUkraine", ???); // Don't know.

    encodingCcsid_.put("SJIS", "932"); // Could be 943, but the IBM i JVM uses 932.
    encodingCcsid_.put("TIS620", "874"); // IBM i JVM uses 874.
  }

  static
  {
    // Build the CCSID to encoding map.
    Iterator it = encodingCcsid_.keySet().iterator();
    while (it.hasNext())
    {
      Object key = it.next();
      ccsidEncoding_.put(encodingCcsid_.get(key), key);
    }

    ccsidEncoding_.put("13488", "UTF-16BE");
    ccsidEncoding_.put("61952", "UTF-16BE");
    ccsidEncoding_.put("17584", "UTF-16BE"); // IBM i doesn't support this, but other people use it.

    it = ccsidEncoding_.keySet().iterator();
    while (it.hasNext())
    {
      String ccsid = (String)it.next();
      String encoding = (String)ccsidEncoding_.get(ccsid);
      int i = new Integer(ccsid).intValue();
      encodings_[i] = encoding;
    }
  }

  /**
   * Converts the specific CCSID bytes into a String.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final String ebcdicByteArrayToString(final byte[] data, final int offset, final int length, final int ccsid) throws UnsupportedEncodingException
  {
    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37) return ebcdicByteArrayToString(data, offset, length);
    String encoding = encodings_[ccsidToUse];
    if (encoding != null)
    {
      return new String(data, offset, length, encoding);
    }
    throw new UnsupportedEncodingException("CCSID "+ccsidToUse);
  }

  /**
   * Converts the specific CCSID bytes into a String.
   * @exception UnsupportedEncodingException Thrown if conversion to or from the specified CCSID is not supported.
  **/
  public static final String ebcdicByteArrayToString(final byte[] data, final int offset, final int length, final char[] buffer, final int ccsid) throws UnsupportedEncodingException
  {
    final int ccsidToUse = ccsid & 0x00FFFF; // So we don't overflow our encodings_ table.
    if (ccsidToUse == 37) return ebcdicByteArrayToString(data, offset, length, buffer);
    String encoding = encodings_[ccsidToUse];
    if (encoding != null)
    {
      try {
         return new String(data, offset, length, encoding);
      } catch (UnsupportedEncodingException ex) {
    	  // Mark as unsupported
    	  encodings_[ccsidToUse] = null;
    	  // Fall through and convert
    	  encoding = null;

      }
    }
   return CcsidConversion.createString(data, offset, length, ccsidToUse);
  }

  /**
   * Returns true if the conversion to or from the specific CCSID is supported by the methods on this class.
  **/
  public static boolean isSupported(final int ccsid)
  {
    if (ccsid < 0 || ccsid > 65535) return false;
    return ccsid == 37 || encodings_[ccsid] != null;
  }

  /**
   * Converts the specified bytes into a long value.
  **/
  public static final long byteArrayToLong(final byte[] data, final int offset)
  {
    int p0 = (0x00FF & data[offset]) << 24;
    int p1 = (0x00FF & data[offset+1]) << 16;
    int p2 = (0x00FF & data[offset+2]) << 8;
    int p3 = 0x00FF & data[offset+3];
    int p4 = (0x00FF & data[offset+4]) << 24;
    int p5 = (0x00FF & data[offset+5]) << 16;
    int p6 = (0x00FF & data[offset+6]) << 8;
    int p7 = (0x00FF & data[offset+7]);
    long l1 = (long)(p0 | p1 | p2 | p3);
    long l2 = (long)(p4 | p5 | p6 | p7);
    return(l1 << 32) | (l2 & 0x00FFFFFFFFL);
  }

  /**
   * Converts the specified bytes into an int value.
  **/
  public static final int byteArrayToInt(final byte[] data, final int offset)
  {
    int p0 = (0x00FF & data[offset]) << 24;
    int p1 = (0x00FF & data[offset+1]) << 16;
    int p2 = (0x00FF & data[offset+2]) << 8;
    int p3 = 0x00FF & data[offset+3];
    return p0 | p1 | p2 | p3;
  }

  /**
   * Converts the specified bytes into a short value.
  **/
  public static final short byteArrayToShort(final byte[] data, final int offset)
  {
    short p0 = (short) (( 0x00FF & data[offset]) << 8);
    short p1 = (short) (0x00FF & data[offset+1]);
    return (short) (p0 | p1);
  }

  /**
   * Converts the specified short value into 2 bytes.
  **/
  public static final void shortToByteArray(final int value, final byte[] data, final int offset)
  {
    data[offset] = (byte)(value >> 8);
    data[offset+1] = (byte)value;
  }

  /**
   * Converts the specified int value into 4 bytes.
  **/
  public static final byte[] intToByteArray(final int value)
  {
    final byte[] val = new byte[4];
    intToByteArray(value, val, 0);
    return val;
  }

  /**
   * Converts the specified int value into 4 bytes.
  **/
  public static final void intToByteArray(final int value, final byte[] data, final int offset)
  {
    data[offset] = (byte)(value >> 24);
    data[offset+1] = (byte)(value >> 16);
    data[offset+2] = (byte)(value >>  8);
    data[offset+3] = (byte)value;
  }

  /**
   * Converts the specified long value into 8 bytes.
  **/
  public static final byte[] longToByteArray(final long longValue)
  {
    final byte[] val = new byte[8];
    longToByteArray(longValue, val, 0);
    return val;
  }

  // @csmith: Perf testing:
  // Confirmed breaking the long into two int's is much faster.
  // Using sign extension >> rather than unsigned shift >>> is faster on IBM JRE but not on Sun (Windows 32-bit).
  /**
   * Converts the specified long value into 8 bytes.
  **/
  public static final void longToByteArray(final long longValue, final byte[] data, final int offset)
  {
    // Do in two parts to avoid long temps.
    final int high = (int)(longValue >> 32);
    final int low = (int)longValue;

    data[offset] = (byte)(high >> 24);
    data[offset+1] = (byte)(high >> 16);
    data[offset+2] = (byte)(high >>  8);
    data[offset+3] = (byte)high;

    data[offset+4] = (byte)(low >> 24);
    data[offset+5] = (byte)(low >> 16);
    data[offset+6] = (byte)(low >>  8);
    data[offset+7] = (byte)low;
  }

  static final void writeStringToUnicodeBytes(final String s, final HostServerConnection.HostOutputStream out) throws IOException
  {
    for (int i=0; i<s.length(); ++i)
    {
      out.writeShort(s.charAt(i));
    }
  }

  static final void writePadEBCDIC(final String s, final int length, final HostServerConnection.HostOutputStream out) throws IOException
  {
    for (int i=0; i<length; ++i)
    {
      if (s == null || s.length() <= i)
      {
        out.write(0x40);
      }
      else
      {
        out.write(CONV_TO_37[s.charAt(i)]);
      }
    }
  }

  static final void writePadEBCDIC10(final String s, final HostServerConnection.HostOutputStream out) throws IOException
  {
    writePadEBCDIC(s, 10, out);
  }

  //TODO - Replace calls to this method with calls to the one above.
  /**
   * Converts the specified String into a total of ten CCSID 37 bytes, blank padding with EBCDIC 0x40 as necessary.
  **/
  public static final byte[] blankPadEBCDIC10(final String s)// throws IOException
  {
    byte[] blank37 = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};
    if (s != null)
    {
      for (int i=0; i<s.length() && i<10; ++i)
      {
        blank37[i] = CONV_TO_37[s.charAt(i)];
      }
    }
    return blank37;
  }

  /**
   * Converts the specified bytes into a float value.
  **/
  public static final float byteArrayToFloat(final byte[] data, final int offset)
  {
    final int i = byteArrayToInt(data, offset);
    return Float.intBitsToFloat(i);
  }

  /**
   * Converts the specified float value into 4 bytes.
  **/
  public static final void floatToByteArray(final float f, final byte[] data, final int offset)
  {
    final int i = Float.floatToIntBits(f);
    intToByteArray(i, data, offset);
  }

  /**
   * Converts the specified float value into 4 bytes.
  **/
  public static final byte[] floatToByteArray(final float f)
  {
    final int i = Float.floatToIntBits(f);
    return intToByteArray(i);
  }

  /**
   * Converts the specified bytes into a double value.
  **/
  public static final double byteArrayToDouble(final byte[] data, final int offset)
  {
    final long l = byteArrayToLong(data, offset);
    return Double.longBitsToDouble(l);
  }

  /**
   * Converts the specified double value into 8 bytes.
  **/
  public static final void doubleToByteArray(final double d, final byte[] data, final int offset)
  {
    final long l = Double.doubleToLongBits(d);
    longToByteArray(l, data, offset);
  }

  /**
   * Converts the specified double value into 8 bytes.
  **/
  public static final byte[] doubleToByteArray(final double d)
  {
    final long l = Double.doubleToLongBits(d);
    return longToByteArray(l);
  }

  // Copied from JTOpen.
  private final static int DEC_FLOAT_16_BIAS = 398;
  private final static long DEC_FLOAT_16_SIGNAL_MASK = 0x0200000000000000L; // 1 bit (7th bit from left) //@snan
  private final static long DEC_FLOAT_16_SIGN_MASK = 0x8000000000000000L; // 1 bits
  private final static long DEC_FLOAT_16_COMBINATION_MASK = 0x7c00000000000000L; // 5 bits
  private final static long DEC_FLOAT_16_EXPONENT_CONTINUATION_MASK = 0x03fc000000000000L; // 8 bits
  private final static long DEC_FLOAT_16_COEFFICIENT_CONTINUATION_MASK = 0x0003ffffffffffffL; // 50 bits

  private final static int DEC_FLOAT_34_BIAS = 6176;
  private final static long DEC_FLOAT_34_SIGNAL_MASK = 0x0200000000000000L; // 1 bit (7th bit from left) //@snan
  private final static long DEC_FLOAT_34_SIGN_MASK = 0x8000000000000000L; // 1 bits
  private final static long DEC_FLOAT_34_COMBINATION_MASK = 0x7c00000000000000L; // 5 bits
  private final static long DEC_FLOAT_34_EXPONENT_CONTINUATION_MASK = 0x03ffc00000000000L; // 12 bits
  private final static long DEC_FLOAT_34_COEFFICIENT_CONTINUATION_MASK = 0x00003fffffffffffL; // 46 bits + 64 bits = 110 bits

  private static final int[][] TEN_RADIX_MAGNITUDE =
  {
    { 0x3b9aca00 }, // 10^9
    { 0x0de0b6b3, 0xa7640000 }, // 10^18
	  { 0x033b2e3c, 0x9fd0803c, 0xe8000000 }, // 10^27
	};

  // Copied from JTOpen. TODO - Needs optimization.
  /**
   * Converts the specified 8 bytes in decfloat16 format into a String.
  **/
  public static final String decfloat16ByteArrayToString(final byte[] data, final int offset)
  {
    long decFloat16Bits = byteArrayToLong(data, offset);
    long combination = (decFloat16Bits & DEC_FLOAT_16_COMBINATION_MASK) >> 58;

    //compute sign here so we can get -+Infinity values
    int sign = ((decFloat16Bits & DEC_FLOAT_16_SIGN_MASK) == DEC_FLOAT_16_SIGN_MASK) ? -1 : 1;

    // deal with special numbers. (not a number and infinity)
    if ((combination == 0x1fL) && (sign == 1))
    {
      long nanSignal = (decFloat16Bits & DEC_FLOAT_16_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
      return nanSignal == 1 ? "SNaN" : "NaN";
    }
    else if ((combination == 0x1fL) && (sign == -1))
    {
      long nanSignal = (decFloat16Bits & DEC_FLOAT_16_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
      return nanSignal == 1 ? "-SNaN" : "-NaN";
    }
    else if ((combination == 0x1eL) && (sign == 1))
    {
      return "Infinity";
    }
    else if ((combination == 0x1eL) && (sign == -1))
    {
      return "-Infinity";
    }

    // compute the exponent MSD and the coefficient MSD.
    int exponentMSD;
    long coefficientMSD;
    if ((combination & 0x18L) == 0x18L)
    {
      // format of 11xxx:
      exponentMSD = (int) ((combination & 0x06L) >> 1);
      coefficientMSD = 8 + (combination & 0x01L);
    }
    else
    {
      // format of xxxxx:
      exponentMSD = (int) ((combination & 0x18L) >> 3);
      coefficientMSD = (combination & 0x07L);
    }

    // compute the exponent.
    int exponent = (int) ((decFloat16Bits & DEC_FLOAT_16_EXPONENT_CONTINUATION_MASK) >> 50);
    exponent |= (exponentMSD << 8);
    exponent -= DEC_FLOAT_16_BIAS;

    // compute the coefficient.
    long coefficientContinuation = decFloat16Bits & DEC_FLOAT_16_COEFFICIENT_CONTINUATION_MASK;
    int coefficientLo = decFloatBitsToDigits((int) (coefficientContinuation & 0x3fffffff)); // low 30 bits (9 digits)
    int coefficientHi = decFloatBitsToDigits((int) ((coefficientContinuation >> 30) & 0xfffff)); // high 20 bits (6 digits)
    coefficientHi += coefficientMSD * 1000000L;

    // compute the int array of coefficient.
    int[] value = computeMagnitude(new int[] { coefficientHi, coefficientLo});

    // convert value to a byte array of coefficient.
    byte[] magnitude = new byte[8];
    magnitude[0] = (byte) (value[0] >>> 24);
    magnitude[1] = (byte) (value[0] >>> 16);
    magnitude[2] = (byte) (value[0] >>> 8);
    magnitude[3] = (byte) (value[0]);
    magnitude[4] = (byte) (value[1] >>> 24);
    magnitude[5] = (byte) (value[1] >>> 16);
    magnitude[6] = (byte) (value[1] >>> 8);
    magnitude[7] = (byte) (value[1]);

    BigInteger bigInt = new BigInteger(sign, magnitude);
    BigDecimal bigDec = new BigDecimal(bigInt, -exponent);
    return bigDec.toString();
  }

  // Copied from JTOpen. TODO - Needs optimization.
  /**
   * Converts the specified 16 bytes in decfloat34 format into a String.
  **/
  public static final String decfloat34ByteArrayToString(final byte[] data, final int offset)
  {
    long decFloat34BitsHi = byteArrayToLong(data, offset);
    long decFloat34BitsLo = byteArrayToLong(data, offset + 8);
    long combination = (decFloat34BitsHi & DEC_FLOAT_34_COMBINATION_MASK) >> 58;

    //compute sign.
    int sign = ((decFloat34BitsHi & DEC_FLOAT_34_SIGN_MASK) == DEC_FLOAT_34_SIGN_MASK) ? -1 : 1;

    // deal with special numbers.
    if ((combination == 0x1fL) && (sign == 1))
    {
      long nanSignal = (decFloat34BitsHi & DEC_FLOAT_34_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
      return nanSignal == 1 ? "SNaN" : "NaN";
    }
    else if ((combination == 0x1fL) && (sign == -1))
    {
      long nanSignal = (decFloat34BitsHi & DEC_FLOAT_34_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
      return nanSignal == 1 ? "-SNaN" : "-NaN";
    }
    else if ((combination == 0x1eL) && (sign == 1))
    {
      return "Infinity";
    }
    else if ((combination == 0x1eL) && (sign == -1))
    {
      return "-Infinity";
    }

    // compute the exponent MSD and the coefficient MSD.
    int exponentMSD;
    long coefficientMSD;
    if ((combination & 0x18L) == 0x18L)
    {
      // format of 11xxx:
      exponentMSD = (int) ((combination & 0x06L) >> 1);
      coefficientMSD = 8 + (combination & 0x01L);
    }
    else
    {
      // format of xxxxx:
      exponentMSD = (int) ((combination & 0x18L) >> 3);
      coefficientMSD = (combination & 0x07L);
    }

    // compute the exponent.
    int exponent = (int) ((decFloat34BitsHi & DEC_FLOAT_34_EXPONENT_CONTINUATION_MASK) >> 46);
    exponent |= (exponentMSD << 12);
    exponent -= DEC_FLOAT_34_BIAS;

    // compute the coefficient.
    int coefficientLo = decFloatBitsToDigits((int) (decFloat34BitsLo & 0x3fffffff)); // last 30 bits (9 digits)
    // another 30 bits (9 digits)
    int coefficientMeLo = decFloatBitsToDigits((int) ((decFloat34BitsLo >> 30) & 0x3fffffff));
    // another 30 bits (9 digits). 26 bits from hi and 4 bits from lo.
    int coefficientMeHi = decFloatBitsToDigits((int) (((decFloat34BitsHi & 0x3ffffff) << 4) | ((decFloat34BitsLo >> 60) & 0xf)));
    int coefficientHi = decFloatBitsToDigits((int) ((decFloat34BitsHi >> 26) & 0xfffff)); // high 20 bits (6 digits)
    coefficientHi += coefficientMSD * 1000000L;

    // compute the int array of coefficient.
    int[] value = computeMagnitude(new int[] { coefficientHi, coefficientMeHi, coefficientMeLo, coefficientLo});

    // convert value to a byte array of coefficient.
    byte[] magnitude = new byte[16];
    magnitude[0] = (byte) (value[0] >>> 24);
    magnitude[1] = (byte) (value[0] >>> 16);
    magnitude[2] = (byte) (value[0] >>> 8);
    magnitude[3] = (byte) (value[0]);
    magnitude[4] = (byte) (value[1] >>> 24);
    magnitude[5] = (byte) (value[1] >>> 16);
    magnitude[6] = (byte) (value[1] >>> 8);
    magnitude[7] = (byte) (value[1]);
    magnitude[8] = (byte) (value[2] >>> 24);
    magnitude[9] = (byte) (value[2] >>> 16);
    magnitude[10] = (byte) (value[2] >>> 8);
    magnitude[11] = (byte) (value[2]);
    magnitude[12] = (byte) (value[3] >>> 24);
    magnitude[13] = (byte) (value[3] >>> 16);
    magnitude[14] = (byte) (value[3] >>> 8);
    magnitude[15] = (byte) (value[3]);

    BigInteger bigInt = new BigInteger(sign, magnitude);
    BigDecimal bigDec = new BigDecimal(bigInt, -exponent);
    return bigDec.toString();
  }

  // Copied from JTOpen. TODO - Needs optimization.
  // Compute the int array of magnitude from input value segments.
  private static final int[] computeMagnitude(final int[] input)
  {
    final int length = input.length;
    final int[] mag = new int[length];

    final int stop = length-1;
    mag[stop] = input[stop];
    for (int i=0; i<stop; ++i)
    {
      int carry = 0;
      int j = TEN_RADIX_MAGNITUDE[i].length-1;
      int k = length-1;
      for (; j >= 0; --j, --k)
      {
        long product = (input[length-2-i] & 0xFFFFFFFFL) * (TEN_RADIX_MAGNITUDE[i][j] & 0xFFFFFFFFL)
                       + (mag[k] & 0xFFFFFFFFL) // add previous value
                       + (carry & 0xFFFFFFFFL); // add carry
        carry = (int) (product >>> 32);
        mag[k] = (int) (product & 0xFFFFFFFFL);
      }
      mag[k] = (int) carry;
    }
    return mag;
  }

  // Copied from JTOpen. TODO - Needs optimization.
  // Convert 30 binary bits coefficient to 9 decimal digits.
  private static final int decFloatBitsToDigits (int bits)
  {
    int decimal = 0;
    for (int i=2; i>=0; --i)
    {
      decimal *= 1000;
      decimal += unpackDenselyPackedDecimal((int)((bits >> (i * 10)) & 0x03ffL));
    }
    return decimal;
  }

  // Copied from JTOpen. TODO - Needs optimization.
  // Internal declet decoding helper method.
  private static int unpackDenselyPackedDecimal (int bits)
  {
    //Declet is the three bit encoding of one decimal digit.  The Decfloat is made up of declets to represent
    //the decfloat 16 or 34 digits
    int combination;
    if ((bits & 14) == 14)
    {
      combination = ((bits & 96) >> 5) | 4;
    }
    else
    {
      combination = ((bits & 8) == 8) ? (((~bits) & 6) >> 1) : 0;
    }
    int decoded = 0;
    switch (combination)
    {
      case 0:  // bit 6 is 0
        decoded = ((bits & 896) << 1) | (bits & 119);
        break;
      case 1:  // bits 6,7,8 are 1-1-0
        decoded = ((bits & 128) << 1) | (bits & 113) | ((bits & 768) >> 7) | 2048;
        break;
      case 2:  // bits 6,7,8 are 1-0-1
        decoded = ((bits & 896) << 1) | (bits & 17) | ((bits & 96) >> 4) | 128;
        break;
      case 3:  // bits 6,7,8 are 1-0-0
        decoded = ((bits & 896) << 1) | (bits & 113) | 8;
        break;
      case 4:  // bits 6,7,8 are 1-1-1, bits 3,4 are 0-0
        decoded = ((bits & 128) << 1) | (bits & 17) | ((bits & 768) >> 7) | 2176;
        break;
      case 5:  // bits 6,7,8 are 1-1-1, bits 3,4 are 0-1
        decoded = ((bits & 128) << 1) | (bits & 17) | ((bits & 768) >> 3) | 2056;
        break;
      case 6:  // bits 6,7,8 are 1-1-1, bits 3,4 are 1-0
        decoded = ((bits & 896) << 1) | (bits & 17) | 136;
        break;
      case 7:  // bits 6,7,8 are 1-1-1, bits 3,4 are 1-1
        // NB: we ignore values of bits 0,1 in this case
        decoded = ((bits & 128) << 1) | (bits & 17) | 2184;
        break;
    }
    return((decoded & 3840) >> 8) * 100 + ((decoded & 240) >> 4) *10 + (decoded & 15);
  }

  /**
   * Converts the specified packed decimal bytes into a String.
  **/
  public static final String packedDecimalToString(final byte[] data, final int offset, final int numDigits, final int scale)
  {
    final int len = numDigits/2+1;
    int sign = data[offset+len-1] & 0x0F;
    boolean isNegative = sign == 0x0B || sign == 0x0D;
    char[] buf = new char[numDigits+(scale > 0 ? 1 : 0)+(isNegative ? 1 : 0)];
    return packedDecimalToString(data, offset, numDigits, scale, buf);
  }

  /**
   * Converts the specified packed decimal bytes into a String.
   * The number of bytes used from <i>data</i> is equal to <i>numDigits</i>/2+1.
  **/
  public static final String packedDecimalToString(final byte[] data, int offset, int numDigits, final int scale, final char[] buffer)
  {

    // even number of digits will have a leading zero
    if (numDigits%2 == 0) ++numDigits;

    final int len = numDigits/2+1;

    int sign = data[offset+len-1] & 0x0F;
    boolean isNegative = sign == 0x0B || sign == 0x0D;
    int count = 0;
    if (isNegative)
    {
      buffer[count++] = '-';
    }
    // boolean doHigh = numDigits % 2 == 1;
    // FindBugs says:  The code uses x % 2 == 1 to check to see if a value is odd, but this won't work for negative numbers (e.g., (-5) % 2 == -1).
    // If this code is intending to check for oddness, consider using x & 1 == 1, or x % 2 != 0.
    boolean doHigh = numDigits % 2 != 0;

    int digitsBeforeDecimal = numDigits-scale;
    boolean foundNonZero = false;
    for (int i=0; i<digitsBeforeDecimal; ++i)
    {
      int nibble = (doHigh ? (data[offset] >> 4) : data[offset]) & 0x0F;
      if (foundNonZero || nibble != 0)
      {
        buffer[count++] = NUM[nibble];
        foundNonZero = true;
      }
      if (!doHigh)
      {
        doHigh = true;
        ++offset;
      }
      else
      {
        doHigh = false;
      }
    }
    if (count == 0 || (isNegative && count == 1))
    {
      buffer[count++] = '0';
    }
    if (scale > 0)
    {
      buffer[count++] = '.';
    }
    for (int i=digitsBeforeDecimal; i<numDigits; ++i)
    {
      int nibble = (doHigh ? (data[offset] >> 4) : data[offset]) & 0x0F;
      buffer[count++] = NUM[nibble];
      if (!doHigh)
      {
        doHigh = true;
        ++offset;
      }
      else
      {
        doHigh = false;
      }
    }
    return new String(buffer, 0, count);
  }

  // Copied from JTOpen AS400PackedDecimal.
  public static final double packedDecimalToDouble(final byte[] data, final int offset, final int numDigits, final int scale)
  {
    // Compute the value.
    double doubleValue = 0;
    double multiplier = Math.pow(10, -scale);
    int rightMostOffset = offset + numDigits/2;
    boolean nibble = true; // true for left nibble, false for right nibble.
    for (int i = rightMostOffset; i >= offset;)
    {
      if (nibble)
      {
        doubleValue += (byte)((data[i] & 0x00F0) >> 4) * multiplier;
        --i;
      }
      else
      {
        doubleValue += ((byte)(data[i] & 0x000F)) * multiplier;
      }

      multiplier *= 10;
      nibble = ! nibble;
    }

    // Determine the sign.
    switch (data[rightMostOffset] & 0x000F)
    {
      case 0x000B:
      case 0x000D:
        // Negative.
        doubleValue *= -1;
        break;
      case 0x000A:
      case 0x000C:
      case 0x000E:
      case 0x000F:
        // Positive.
        break;
      default:
        throw new NumberFormatException("Byte sequence not valid for packed decimal ("+rightMostOffset+": "+(data[rightMostOffset] & 0x000F)+").");
    }

    return doubleValue;
  }

  public static final byte[] doubleToPackedDecimal(final double d, final int numDigits, final int scale)
  {
    byte[] data = new byte[numDigits/2+1];
    doubleToPackedDecimal(d, data, 0, numDigits, scale);
    return data;
  }

  // Copied from JTOpen AS400PackedDecimal.
  public static final void doubleToPackedDecimal(final double d, final byte[] data, final int offset, final int numDigits, final int scale)
  {
    // GOAL:  For performance reasons, we need to do this conversion
    //        without creating any Java objects (e.g., BigDecimals,
    //        Strings).

    // If the number is too big, we can't do anything with it.
    double absValue = Math.abs(d);
    if (absValue > Long.MAX_VALUE)
    {
      throw new NumberFormatException("Double value is too big: "+d);
    }

    // Extract the normalized value.  This is the value represented by
    // two longs (one for each side of the decimal point).  Using longs
    // here improves the quality of the algorithm as well as the
    // performance of arithmetic operations.  We may need to use an
    // "effective" scale due to the lack of precision representable
    // by a long.
    long leftSide = (long)absValue;
    int effectiveScale = (scale > 15) ? 15 : scale;
    long rightSide = (long)Math.round((absValue - (double)leftSide) * Math.pow(10, effectiveScale));

    // Ok, now we are done with any double arithmetic!
    int length = numDigits/2;
    int b = offset + length;
    boolean nibble = true; // true for left nibble, false for right nibble.

    // If the effective scale is different than the actual scale,
    // then pad with zeros.
    int scaleDifference = scale - effectiveScale;
    for (int i = 1; i <= scaleDifference; ++i)
    {
      if (nibble)
      {
        data[b] &= (byte)(0x000F);
        --b;
      }
      else
      {
        data[b] &= (byte)(0x00F0);
      }
      nibble = !nibble;
    }

    // Compute the bytes for the right side of the decimal point.
    int nextDigit;
    for (int i = 1; i <= effectiveScale; ++i)
    {
      nextDigit = (int)(rightSide % 10);
      if (nibble)
      {
        data[b] &= (byte)(0x000F);
        data[b] |= ((byte)nextDigit << 4);
        --b;
      }
      else
      {
        data[b] &= (byte)(0x00F0);
        data[b] |= (byte)nextDigit;
      }
      nibble = !nibble;
      rightSide /= 10;
    }

    // Compute the bytes for the left side of the decimal point.
    int leftSideDigits = numDigits - scale;
    for (int i = 1; i <= leftSideDigits; ++i)
    {
      nextDigit = (int)(leftSide % 10);
      if (nibble)
      {
        data[b] &= (byte)(0x000F);
        data[b] |= ((byte)nextDigit << 4);
        --b;
      }
      else
      {
        data[b] &= (byte)(0x00F0);
        data[b] |= (byte)nextDigit;
      }
      nibble = !nibble;
      leftSide /= 10;
    }

    // Zero out the left part of the value, if needed.
    while (b >= offset)
    {
      if (nibble)
      {
        data[b] &= (byte)(0x000F);
        --b;
      }
      else
      {
        data[b] &= (byte)(0x00F0);
      }
      nibble = !nibble;
    }

    // Fix the sign.
    b = offset + length;
    data[b] &= (byte)(0x00F0);
    data[b] |= (byte)((d >= 0) ? 0x000F : 0x000D);

    // If left side still has digits, then the value was too big
    // to fit.
    if (leftSide > 0)
    {
      throw new NumberFormatException("Double value "+d+" too big for output array.");
    }
  }

  /**
   * Converts the specified String (number) into packed decimal bytes.
  **/
  public static final byte[] stringToPackedDecimal(final String s, final int numDigits)
  {
    byte[] b = new byte[numDigits/2+1];
    stringToPackedDecimal(s, numDigits, b, 0);
    return b;
  }

  /**
   * Converts the specified String (number) into packed decimal bytes.
   * The string must have the correct number of decimal digits for
   * the conversion to be correct.
  **/
  public static final void stringToPackedDecimal(final String s, final int numDigits, final byte[] buffer, final int offset)
  {
    final int len = numDigits/2+1;
    final boolean isNegative = s != null && s.length() > 0 && s.charAt(0) == '-';
    int counter = offset+len-1;
    buffer[counter] = isNegative ? (byte)0x0D : (byte)0x0F;
    final int stop = isNegative ? 1 : 0;
    boolean doHigh = true;
    if ( s != null) {

    for (int i = s.length()-1; i>=stop; --i)
    {
      char c = s.charAt(i);
      if (c != '.')
      {
    	  int index = (int)c-'0';
    	  if (index < 0 || index > 9) {
    		  throw new NumberFormatException("Invalid character "+c);
    	  }
        if (doHigh)
        {
          buffer[counter--] |= CHAR_HIGH[index];
          doHigh = false;
        }
        else
        {
          buffer[counter] = CHAR_LOW[index];
          doHigh = true;
        }
      }
    }
    }
  }

  /**
   * Converts the specified zoned decimal bytes into a String.
  **/
  public static final String zonedDecimalToString(final byte[] data, final int offset, final int numDigits, final int scale)
  {
    int sign = (data[offset+numDigits-1] >> 4) & 0x0F;
    boolean isNegative = sign == 0x0B || sign == 0x0D;
    char[] buf = new char[numDigits+(scale > 0 ? 1 : 0)+(isNegative ? 1 : 0)];
    return zonedDecimalToString(data, offset, numDigits, scale, buf);
  }

  /**
   * Converts the specified zoned decimal bytes into a String.
  **/
  public static final String zonedDecimalToString(final byte[] data, int offset, final int numDigits, final int scale, final char[] buffer)
  {
    int sign = (data[offset+numDigits-1] >> 4) & 0x0F;
    boolean isNegative = sign == 0x0B || sign == 0x0D;
    int count = 0;
    boolean foundNonZero = false;
    if (isNegative)
    {
      buffer[count++] = '-';
    }
    int digitsBeforeDecimal = numDigits-scale;
    for (int i=0; i<digitsBeforeDecimal; ++i)
    {
      int nibble = data[offset++] & 0x0F;
      if (foundNonZero || nibble != 0)
      {
        buffer[count++] = NUM[nibble];
        foundNonZero = true;
      }
    }
    if (count == 0 || (isNegative && count == 1))
    {
      buffer[count++] = '0';
    }
    if (scale > 0)
    {
      buffer[count++] = '.';
    }
    for (int i=digitsBeforeDecimal; i<numDigits; ++i)
    {
      int nibble = data[offset++] & 0x0F;
      buffer[count++] = NUM[nibble];
    }
    return new String(buffer, 0, count);
  }

  // Copied from JTOpen AS400ZonedDecimal.
  public static final double zonedDecimalToDouble(final byte[] data, final int offset, final int numDigits, final int scale)
  {
    /*
     * This old code had a bug in that it can produce
     * inexact answers. For example
     * 10.10105 is turned into -10.101049999999999

    // Compute the value.
    double doubleValue = 0;
    double multiplier = Math.pow(10, numDigits - scale - 1);
    int rightMostOffset = offset + numDigits - 1;
    for (int i = offset; i <= rightMostOffset; ++i)
    {
      doubleValue += ((byte)(data[i] & 0x000F)) * multiplier;
      multiplier /= 10;
    }
    */

    /*
     * Instead we gather the digits using a long, then / by the scale.
     * Note:  Using a multiple by Math.pow(10, -scale) gives a worse answer.
     * Math.pow(10,-scale) is a less accurate number than Math.pow(10,scale)
     *
     * A number which exposes this issue is 10.10105
     */

    long   longValue = 0;
    double doubleValue = 0;
    double divisor = Math.pow(10, scale);
    int rightMostOffset = offset + numDigits - 1;
    for(int i = offset; i <= rightMostOffset; ++i) {
        longValue = longValue * 10 + (byte)(data[i] & 0x000F);
    }
    doubleValue = longValue / divisor;



    // Determine the sign.
    switch (data[rightMostOffset] & 0x00F0)
    {
      case 0x00B0:
      case 0x00D0:
        // Negative.
        doubleValue *= -1;
        break;
      case 0x00A0:
      case 0x00C0:
      case 0x00E0:
      case 0x00F0:
        // Positive.
        break;
      default:
        throw new NumberFormatException("Byte sequence not valid for zoned decimal ("+rightMostOffset+": "+(data[rightMostOffset] & 0x00FF)+").");
    }

    return doubleValue;
  }

  public static final byte[] doubleToZonedDecimal(final double d, final int numDigits, final int scale)
  {
    byte[] data = new byte[numDigits];
    doubleToZonedDecimal(d, data, 0, numDigits, scale);
    return data;
  }

  // Copied from JTOpen AS400ZonedDecimal.
  public static final void doubleToZonedDecimal(final double d, final byte[] data, final int offset, final int numDigits, final int scale)
  {
    // GOAL:  For performance reasons, we need to do this conversion
    //        without creating any Java objects (e.g., BigDecimals,
    //        Strings).

    // If the number is too big, we can't do anything with it.
    double absValue = Math.abs(d);
    if (absValue > Long.MAX_VALUE)
    {
      throw new NumberFormatException("Double value is too big: "+d);
    }

    // Extract the normalized value.  This is the value represented by
    // two longs (one for each side of the decimal point).  Using longs
    // here improves the quality of the algorithm as well as the
    // performance of arithmetic operations.  We may need to use an
    // "effective" scale due to the lack of precision representable
    // by a long.
    long leftSide = (long)absValue;
    int effectiveScale = (scale > 15) ? 15 : scale;
    long rightSide = (long)Math.round((absValue - (double)leftSide) * Math.pow(10, effectiveScale));

    // Ok, now we are done with any double arithmetic!

    // If the effective scale is different than the actual scale,
    // then pad with zeros.
    int rightmostOffset = offset + numDigits - 1;
    int padOffset = rightmostOffset - (scale - effectiveScale);
    for (int i = rightmostOffset; i > padOffset; --i)
      data[i] = (byte)0x00F0;

    // Compute the bytes for the right side of the decimal point.
    int decimalOffset = rightmostOffset - scale;
    int nextDigit;
    for (int i = padOffset; i > decimalOffset; --i)
    {
      nextDigit = (int)(rightSide % 10);
      data[i] = (byte)(0x00F0 | nextDigit);
      rightSide /= 10;
    }

    // Compute the bytes for the left side of the decimal point.
    for (int i = decimalOffset; i >= offset; --i)
    {
      nextDigit = (int)(leftSide % 10);
      data[i] = (byte)(0x00F0 | nextDigit);
      leftSide /= 10;
    }

    // Fix the sign, if negative.
    if (d < 0)
      data[rightmostOffset] = (byte)(data[rightmostOffset] & 0x00DF);

    // If left side still has digits, then the value was too big
    // to fit.
    if (leftSide > 0)
    {
      throw new NumberFormatException("Double value "+d+" too big for output array.");
    }
  }

  /**
   * Converts the specified String (number) into zoned decimal bytes.
  **/
  public static final byte[] stringToZonedDecimal(final String s, final int numDigits)
  {
    byte[] b = new byte[numDigits];
    stringToZonedDecimal(s, numDigits, b, 0);
    return b;
  }

  /**
   * Converts the specified String (number) into zoned decimal bytes.
  **/
  public static final void stringToZonedDecimal(final String s, final int numDigits, final byte[] buffer, final int offset)
  {
    int counter = offset+numDigits-1;
    final boolean isNegative = s != null && s.length() > 0 && s.charAt(0) == '-';
    final int stop = isNegative ? 1 : 0;
    if (s != null) {
    for (int i = s.length()-1; i>=stop; --i)
    {
      char c = s.charAt(i);
      if (c != '.')
      {
    	int index = c - '0';
    	if (index < 0 || index > 9 ) {
    		throw new NumberFormatException("Invalid character "+c);
    	}
        buffer[counter--] = (byte) ( CHAR_LOW[index] | 0xF0) ;
      }
    }
    }
    if (isNegative)
    {
      buffer[offset+numDigits-1] = (byte) ((buffer[offset+numDigits-1] & ((byte) 0x0F)) | (byte) 0xD0);
    }
  }



}

