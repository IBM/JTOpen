///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BinaryConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  A binary types converter between Java byte arrays and Java simple types.
 **/
public class BinaryConverter
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  // Don't allow instances of this class.
  private BinaryConverter()
  {
  }

  /**
    Convert the specified short into i5/OS format in the specified byte array.
    @param  shortValue  The value to be converted to i5/OS format.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void shortToByteArray(short shortValue, byte[] serverValue, int offset)
  {
    serverValue[offset]   = (byte)(shortValue >>> 8);
    serverValue[offset+1] = (byte) shortValue;
  }

  /**
    Convert the specified short into i5/OS format in the specified byte array.
    @param  shortValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] shortToByteArray(short shortValue)
  {
    byte[] serverValue = new byte[2];
    shortToByteArray(shortValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to a short.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  a short corresponding to the data type.
   **/
  public static short byteArrayToShort(byte[] serverValue, int offset)
  {
    return(short)(((serverValue[offset]   & 0xFF) << 8) +
                  (serverValue[offset+1] & 0xFF));
  }

  /**
    Convert the specified int into i5/OS format in the specified byte array.
    @param  intValue  The value to be converted to i5/OS format.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void intToByteArray(int intValue, byte[] serverValue, int offset)
  {
    serverValue[offset]   = (byte)(intValue >>> 24);
    serverValue[offset+1] = (byte)(intValue >>> 16);
    serverValue[offset+2] = (byte)(intValue >>>  8);
    serverValue[offset+3] = (byte) intValue;
  }

  /**
    Convert the specified int into i5/OS format in the specified byte array.
    @param  intValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] intToByteArray(int intValue)
  {
    byte[] serverValue = new byte[4];
    intToByteArray(intValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to an int.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  an int corresponding to the data type.
   **/
  public static int byteArrayToInt(byte[] serverValue, int offset)
  {
    return((serverValue[offset]   & 0xFF) << 24) +
    ((serverValue[offset+1] & 0xFF) << 16) +
    ((serverValue[offset+2] & 0xFF) <<  8) +
    (serverValue[offset+3] & 0xFF);
  }

  /**
    Convert the specified float into i5/OS format in the specified byte array.
    @param  floatValue  The value to be converted to i5/OS format.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void floatToByteArray(float floatValue, byte[] serverValue, int offset)
  {
    int bits = Float.floatToIntBits(floatValue);
    serverValue[offset]   = (byte)(bits >>> 24);
    serverValue[offset+1] = (byte)(bits >>> 16);
    serverValue[offset+2] = (byte)(bits >>>  8);
    serverValue[offset+3] = (byte) bits;
  }

  /**
    Convert the specified float into i5/OS format in the specified byte array.
    @param  floatValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
    @deprecated  Use floatToByteArray(float) instead.
   **/
  public static byte[] floatToByteArray(int floatValue)
  {
    byte[] serverValue = new byte[4];
    floatToByteArray(floatValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified float into i5/OS format in the specified byte array.
    @param  floatValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] floatToByteArray(float floatValue)
  {
    byte[] serverValue = new byte[4];
    floatToByteArray(floatValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to a float.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  a float corresponding to the data type.
   **/
  public static float byteArrayToFloat(byte[] serverValue, int offset)
  {
    int bits = ((serverValue[offset]   & 0xFF) << 24) +
               ((serverValue[offset+1] & 0xFF) << 16) +
               ((serverValue[offset+2] & 0xFF) <<  8) +
               (serverValue[offset+3] & 0xFF);
    return Float.intBitsToFloat(bits);
  }

  /**
    Convert the specified double into i5/OS format in the specified byte array.
    @param  doubleValue  The value to be converted to i5/OS format.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void doubleToByteArray(double doubleValue, byte[] serverValue, int offset)
  {
    long bits = Double.doubleToLongBits(doubleValue);
    // Do in two parts to avoid long temps.
    int high = (int)(bits >>> 32);
    int low = (int)bits;

    serverValue[offset]   = (byte)(high >>> 24);
    serverValue[offset+1] = (byte)(high >>> 16);
    serverValue[offset+2] = (byte)(high >>>  8);
    serverValue[offset+3] = (byte) high;

    serverValue[offset+4] = (byte)(low >>> 24);
    serverValue[offset+5] = (byte)(low >>> 16);
    serverValue[offset+6] = (byte)(low >>>  8);
    serverValue[offset+7] = (byte) low;
  }

  /**
    Convert the specified double into i5/OS format in the specified byte array.
    @param  doubleValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] doubleToByteArray(double doubleValue)
  {
    byte[] serverValue = new byte[8];
    doubleToByteArray(doubleValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to a double.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  a double corresponding to the data type.
   **/
  public static double byteArrayToDouble(byte[] serverValue, int offset)
  {
    // Do in two parts to avoid long temps.
    int firstPart =  ((serverValue[offset]   & 0xFF) << 24) +
                     ((serverValue[offset+1] & 0xFF) << 16) +
                     ((serverValue[offset+2] & 0xFF) <<  8) +
                     (serverValue[offset+3] & 0xFF);
    int secondPart = ((serverValue[offset+4] & 0xFF) << 24) +
                     ((serverValue[offset+5] & 0xFF) << 16) +
                     ((serverValue[offset+6] & 0xFF) <<  8) +
                     (serverValue[offset+7] & 0xFF);
    long bits = ((long)firstPart << 32) + (secondPart & 0xFFFFFFFFL);
    return Double.longBitsToDouble(bits);
  }

  /**
    Convert the specified int into i5/OS format in the specified byte array.
    @param  intValue  The value to be converted to i5/OS format.  The integer should be greater than or equal to zero and representable in two bytes.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void unsignedShortToByteArray(int intValue, byte[] serverValue, int offset)
  {
    serverValue[offset]   = (byte)(intValue >>> 8);
    serverValue[offset+1] = (byte) intValue;
  }

  /**
    Convert the specified int into i5/OS format in the specified byte array.
    @param  intValue  The value to be converted to i5/OS format.  The integer should be greater than or equal to zero and representable in two bytes.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] unsignedShortToByteArray(int intValue)
  {
    byte[] serverValue = new byte[2];
    unsignedShortToByteArray(intValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to an int.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  an int corresponding to the data type.
   **/
  public static int byteArrayToUnsignedShort(byte[] serverValue, int offset)
  {
    return((serverValue[offset]   & 0xFF) <<  8) +
    (serverValue[offset+1] & 0xFF);
  }

  /**
    Convert the specified long into i5/OS format in the specified byte array.
    @param  longValue  The value to be converted to i5/OS format.  The long should be greater than or equal to zero and representable in four bytes.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void unsignedIntToByteArray(long longValue, byte[] serverValue, int offset)
  {
    // Grab bits into int to avoid long temps.
    int bits = (int)longValue;
    serverValue[offset]   = (byte)(bits >>> 24);
    serverValue[offset+1] = (byte)(bits >>> 16);
    serverValue[offset+2] = (byte)(bits >>>  8);
    serverValue[offset+3] = (byte) bits;
  }

  /**
    Convert the specified long into i5/OS format in the specified byte array.
    @param  longValue  The value to be converted to i5/OS format.  The long should be greater than or equal to zero and representable in four bytes.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] unsignedIntToByteArray(long longValue)
  {
    byte[] serverValue = new byte[4];
    unsignedIntToByteArray(longValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to a long.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  a long corresponding to the data type.
   **/
  public static long byteArrayToUnsignedInt(byte[] serverValue, int offset)
  {
    return(((serverValue[offset]   & 0xFF) << 24) +
           ((serverValue[offset+1] & 0xFF) << 16) +
           ((serverValue[offset+2] & 0xFF) <<  8) +
           (serverValue[offset+3] & 0xFF))         & 0xFFFFFFFFL;
  }

  /**
    Convert the specified long into i5/OS format in the specified byte array.
    @param  longValue  The value to be converted to i5/OS format.
    @param  serverValue  The array to receive the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
   **/
  public static void longToByteArray(long longValue, byte[] serverValue, int offset)
  {
    // Do in two parts to avoid long temps.
    int high = (int)(longValue >>> 32);
    int low = (int)longValue;

    serverValue[offset]   = (byte)(high >>> 24);
    serverValue[offset+1] = (byte)(high >>> 16);
    serverValue[offset+2] = (byte)(high >>>  8);
    serverValue[offset+3] = (byte) high;

    serverValue[offset+4] = (byte)(low >>> 24);
    serverValue[offset+5] = (byte)(low >>> 16);
    serverValue[offset+6] = (byte)(low >>>  8);
    serverValue[offset+7] = (byte) low;
  }

  /**
    Convert the specified long into i5/OS format in the specified byte array.
    @param  longValue  The value to be converted to i5/OS format.
    @return  The array with the data type in i5/OS format.
   **/
  public static byte[] longToByteArray(long longValue)
  {
    byte[] serverValue = new byte[8];
    longToByteArray(longValue, serverValue, 0);
    return serverValue;
  }

  /**
    Convert the specified i5/OS data type to a long.
    @param  serverValue  The array containing the data type in i5/OS format.
    @param  offset  The offset into the byte array for the start of the i5/OS value.
    @return  a long corresponding to the data type.
   **/
  public static long byteArrayToLong(byte[] serverValue, int offset)
  {
    // Do in two parts to avoid long temps.
    int firstPart =  ((serverValue[offset]   & 0xFF) << 24) +
                     ((serverValue[offset+1] & 0xFF) << 16) +
                     ((serverValue[offset+2] & 0xFF) <<  8) +
                     (serverValue[offset+3] & 0xFF);
    int secondPart = ((serverValue[offset+4] & 0xFF) << 24) +
                     ((serverValue[offset+5] & 0xFF) << 16) +
                     ((serverValue[offset+6] & 0xFF) <<  8) +
                     (serverValue[offset+7] & 0xFF);
    return((long)firstPart << 32) + (secondPart & 0xFFFFFFFFL);
  }

  static byte[] charArrayToByteArray(char[] charValue)
  {
    if (charValue == null) return null;
    byte[] byteValue = new byte[charValue.length * 2];
    int inPos = 0;
    int outPos = 0;
    while (inPos < charValue.length)
    {
      byteValue[outPos++] = (byte)(charValue[inPos] >> 8);
      byteValue[outPos++] = (byte)charValue[inPos++];
    }
    return byteValue;
  }

  static char[] byteArrayToCharArray(byte[] byteValue)
  {
    if (byteValue == null) return null;
    char[] charValue = new char[byteValue.length / 2];
    int inPos = 0;
    int outPos = 0;
    while (inPos < byteValue.length)
    {
      charValue[outPos++] = (char)(((byteValue[inPos++] & 0xFF) << 8) + (byteValue[inPos++] & 0xFF));
    }
    return charValue;
  }

// Constant used in bytesToString()
  private static final char[] c_ = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  static final char hiNibbleToChar(byte b)
  {
    return c_[(b >>> 4) & 0x0F];
  }

  static final char loNibbleToChar(byte b)
  {
    return c_[b & 0x0F];
  }

  /**
    Convert the specified byte array to its hexadecimal String representation.
    @param  b  The array containing the data.
    @return  A String containing the hex characters that represent the byte data.
   **/
  public static final String bytesToString(final byte[] b)
  {
    return bytesToString(b, 0, b.length);
  }

  /**
    Convert the specified byte array to its hexadecimal String representation.
    @param  b  The array containing the data.
    @param offset The offset into the array at which to begin reading bytes.
    @param length The number of bytes to read out of the array.
    @return  A String containing the hex characters that represent the byte data.
   **/
  public static final String bytesToString(final byte[] b, int offset, int length)
  {
    char[] c = new char[length*2];
    int num = bytesToString(b, offset, length, c, 0);
    return new String(c, 0, num);
  }

  // Helper method to convert a byte array into its hex string representation.
  // This is faster than calling Integer.toHexString(...)
  static final int bytesToString(final byte[] b, int offset, int length, final char[] c, int coffset)
  {
    for (int i=0; i<length; ++i)
    {
      final int j = i*2;
      final byte hi = (byte)((b[i+offset]>>>4) & 0x0F);
      final byte lo = (byte)((b[i+offset] & 0x0F));
      c[j+coffset] = c_[hi];
      c[j+coffset+1] = c_[lo];
    }
    return length*2;
  }

  // Constant used in stringToBytes()
  // Note that 0x11 is "undefined".
  private static final byte[] b_ = 
  {
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11
  };

  static final byte charsToByte(char hi, char lo)
  {
    int c1 = 0x00FFFF & hi;
    int c2 = 0x00FFFF & lo;
    //if(c1 > 255 || c2 > 255) return 0;
    if (c1 > 255 || c2 > 255) throw new NumberFormatException();
    byte b1 = b_[c1];
    byte b2 = b_[c2];
    if (b1 == 0x11 || b2 == 0x11) return 0;
    return(byte)(((byte)(b1 << 4)) + b2);
  }

  /**
    Convert the specified hexadecimal String into a byte array containing the byte values for the 
    hexadecimal characters in the String. If the String contains characters other than those allowed
    for a hexadecimal String (0-9 and A-F), an exception will be thrown.
    @param  s  The String containing the hexadecimal representation of the data.
    @return  A byte array containing the byte values of the hex characters.
  **/
  public static final byte[] stringToBytes(String s)
  {
      int stringLength = s.length();                                                        //@KBA  check for empty string
      if(stringLength > 0)                                                                  //@KBA
      {                                                                                     //@KBA
          if(s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))              //@KBA
              s = s.substring(2);                                                           //@KBA
          else if((s.charAt(0) == 'x' || s.charAt(0) == 'X') && s.charAt(1) == '\'')        //@KBA
          {                                                                                 //@KBA
              if(s.charAt(stringLength - 1) == '\'')                                        //@KBA
                  s = s.substring(2, stringLength-1);                                       //@KBA
              else                                                                          //@KBA
                  throw new NumberFormatException();                                        //@KBA
          }                                                                                 //@KBA
      }                                                                                     //@KBA

    //check if an odd number of characters                                                  //@KBA
    if(s.length()%2 == 1)                                                                   //@KBA
        s = "0" + s;                                                                        //@KBA

    char[] c = s.toCharArray();
    return stringToBytes(c, 0, c.length);
  }

  static final byte[] stringToBytes(char[] hex, int offset, int length)
  {
    if (hex.length == 0) return new byte[0];
    byte[] buf = new byte[(length+1)/2];
    int num = stringToBytes(hex, offset, length, buf, 0);
    if (num < buf.length)
    {
      byte[] temp = buf;
      buf = new byte[num];
      System.arraycopy(temp, 0, buf, 0, num);
    }
    return buf;
  }

  // Helper method to convert a String in hex into its corresponding byte array.
  static final int stringToBytes(char[] hex, int offset, int length, final byte[] b, int boff)
  {
    if (hex.length == 0) return 0;

    // account for char[] of odd length
    //@KBD if (hex.length % 2 == 1)             //@KBD put checks in stringToBytes(String s)
    //@KBD {
    //@KBD   char[] temp = hex;
    //@KBD   hex = new char[temp.length+1];
    //@KBD   System.arraycopy(temp, 0, hex, 0, temp.length-1);
    //@KBD   hex[hex.length-2] = '0';
    //@KBD   hex[hex.length-1] = temp[temp.length-1];
    //@KBD }
    //@KBD if (hex[offset] == '0' && (hex.length > offset+1 && (hex[offset+1] == 'X' || hex[offset+1] == 'x')))
    //@KBD {
    //@KBD   offset += 2;
    //@KBD   length -= 2;
    //@KBD }
    for (int i=0; i<b.length; ++i)
    {
      final int j = i*2;
      final int c1 = 0x00FFFF & hex[j+offset];
      final int c2 = 0x00FFFF & hex[j+offset+1];
      if (c1 > 255 || c2 > 255) // out of range
      {
        //b[i+boff] = 0x00;
        throw new NumberFormatException();
      }
      else
      {
        final byte b1 = b_[c1];
        final byte b2 = b_[c2];
        if (b1 == 0x11 || b2 == 0x11) // out of range
        {
          //b[i+boff] = 0x00;
          throw new NumberFormatException();
        }
        else
        {
          final byte hi = (byte)(b1<<4);
          b[i+boff] = (byte)(hi + b2);
        }
      }
    }
    return b.length;
  }
}
