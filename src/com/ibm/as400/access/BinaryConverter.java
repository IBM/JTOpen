///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BinaryConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  A binary types converter between Java byte arrays and Java simple types.
 **/
public class BinaryConverter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    // Don't allow instances of this class.
    private BinaryConverter()
    {
    }

    /**
      Convert the specified short into server format in the specified byte array.
      @param  shortValue  The value to be converted to server format.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
     **/
    public static void shortToByteArray(short shortValue, byte[] serverValue, int offset)
    {
	serverValue[offset]   = (byte)(shortValue >>> 8);
	serverValue[offset+1] = (byte) shortValue;
    }

    /**
      Convert the specified short into server format in the specified byte array.
      @param  shortValue  The value to be converted to server format.
      @return  The array with the data type in server format.
     **/
    public static byte[] shortToByteArray(short shortValue)
    {
        byte[] serverValue = new byte[2];
        shortToByteArray(shortValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to a short.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
      @return  a short corresponding to the data type.
     **/
    public static short byteArrayToShort(byte[] serverValue, int offset)
    {
	return (short)(((serverValue[offset]   & 0xFF) << 8) +
		        (serverValue[offset+1] & 0xFF));
    }

    /**
      Convert the specified int into server format in the specified byte array.
      @param  intValue  The value to be converted to server format.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
     **/
    public static void intToByteArray(int intValue, byte[] serverValue, int offset)
    {
	serverValue[offset]   = (byte)(intValue >>> 24);
	serverValue[offset+1] = (byte)(intValue >>> 16);
	serverValue[offset+2] = (byte)(intValue >>>  8);
	serverValue[offset+3] = (byte) intValue;
    }

    /**
      Convert the specified int into server format in the specified byte array.
      @param  intValue  The value to be converted to server format.
      @return  The array with the data type in server format.
     **/
    public static byte[] intToByteArray(int intValue)
    {
        byte[] serverValue = new byte[4];
        intToByteArray(intValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to an int.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
      @return  an int corresponding to the data type.
     **/
    public static int byteArrayToInt(byte[] serverValue, int offset)
    {
	return ((serverValue[offset]   & 0xFF) << 24) +
	       ((serverValue[offset+1] & 0xFF) << 16) +
	       ((serverValue[offset+2] & 0xFF) <<  8) +
	        (serverValue[offset+3] & 0xFF);
    }

    /**
      Convert the specified float into server format in the specified byte array.
      @param  floatValue  The value to be converted to server format.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified float into server format in the specified byte array.
      @param  floatValue  The value to be converted to server format.
      @return  The array with the data type in server format.
     **/
    public static byte[] floatToByteArray(int floatValue)
    {
        byte[] serverValue = new byte[4];
        floatToByteArray(floatValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to a float.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified double into server format in the specified byte array.
      @param  doubleValue  The value to be converted to server format.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified double into server format in the specified byte array.
      @param  doubleValue  The value to be converted to server format.
      @return  The array with the data type in server format.
     **/
    public static byte[] doubleToByteArray(double doubleValue)
    {
        byte[] serverValue = new byte[8];
        doubleToByteArray(doubleValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to a double.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified int into server format in the specified byte array.
      @param  intValue  The value to be converted to server format.  The integer should be greater than or equal to zero and representable in two bytes.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
     **/
    public static void unsignedShortToByteArray(int intValue, byte[] serverValue, int offset)
    {
	serverValue[offset]   = (byte)(intValue >>> 8);
	serverValue[offset+1] = (byte) intValue;
    }

    /**
      Convert the specified int into server format in the specified byte array.
      @param  intValue  The value to be converted to server format.  The integer should be greater than or equal to zero and representable in two bytes.
      @return  The array with the data type in server format.
     **/
    public static byte[] unsignedShortToByteArray(int intValue)
    {
        byte[] serverValue = new byte[2];
        unsignedShortToByteArray(intValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to an int.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
      @return  an int corresponding to the data type.
     **/
    public static int byteArrayToUnsignedShort(byte[] serverValue, int offset)
    {
	return ((serverValue[offset]   & 0xFF) <<  8) +
	        (serverValue[offset+1] & 0xFF);
    }

    /**
      Convert the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.  The long should be greater than or equal to zero and representable in four bytes.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.  The long should be greater than or equal to zero and representable in four bytes.
      @return  The array with the data type in server format.
     **/
    public static byte[] unsignedIntToByteArray(long longValue)
    {
        byte[] serverValue = new byte[4];
        unsignedIntToByteArray(longValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to a long.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
      @return  a long corresponding to the data type.
     **/
    public static long byteArrayToUnsignedInt(byte[] serverValue, int offset)
    {
	return (((serverValue[offset]   & 0xFF) << 24) +
		((serverValue[offset+1] & 0xFF) << 16) +
		((serverValue[offset+2] & 0xFF) <<  8) +
		 (serverValue[offset+3] & 0xFF))         & 0xFFFFFFFFL;
    }

    /**
      Convert the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.
      @param  serverValue  The array to receive the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
      Convert the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.
      @return  The array with the data type in server format.
     **/
    public static byte[] longToByteArray(long longValue)
    {
        byte[] serverValue = new byte[8];
        longToByteArray(longValue, serverValue, 0);
        return serverValue;
    }

    /**
      Convert the specified server data type to a long.
      @param  serverValue  The array containing the data type in server format.
      @param  offset  The offset into the byte array for the start of the server value.
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
	return ((long)firstPart << 32) + (secondPart & 0xFFFFFFFFL);
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
}
