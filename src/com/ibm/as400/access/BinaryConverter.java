///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

    
    // Don't allow instances of this class
    private BinaryConverter()
    {
    }

    /**
      Convert the specified short into AS/400 format in the specified byte array.
      @param  shortValue  the value to be converted to AS/400 format.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void shortToByteArray(short shortValue, byte[] as400Value, int offset)
    {
	as400Value[offset]   = (byte)(shortValue >>> 8);
	as400Value[offset+1] = (byte) shortValue;
    }

    /**
      Convert the specified AS/400 data type to a short.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  a short corresponding to the data type.
     **/
    public static short byteArrayToShort(byte[] as400Value, int offset)
    {
	return (short)(((as400Value[offset]   & 0xFF) << 8) +
		        (as400Value[offset+1] & 0xFF));
    }

    /**
      Convert the specified int into AS/400 format in the specified byte array.
      @param  intValue  the value to be converted to AS/400 format.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void intToByteArray(int intValue, byte[] as400Value, int offset)
    {
	as400Value[offset]   = (byte)(intValue >>> 24);
	as400Value[offset+1] = (byte)(intValue >>> 16);
	as400Value[offset+2] = (byte)(intValue >>>  8);
	as400Value[offset+3] = (byte) intValue;
    }

    /**
      Convert the specified AS/400 data type to an int.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  an int corresponding to the data type.
     **/
    public static int byteArrayToInt(byte[] as400Value, int offset)
    {
	return ((as400Value[offset]   & 0xFF) << 24) +
	       ((as400Value[offset+1] & 0xFF) << 16) +
	       ((as400Value[offset+2] & 0xFF) <<  8) +
	        (as400Value[offset+3] & 0xFF);
    }

    /**
      Convert the specified float into AS/400 format in the specified byte array.
      @param  floatValue  the value to be converted to AS/400 format.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void floatToByteArray(float floatValue, byte[] as400Value, int offset)
    {
	int bits = Float.floatToIntBits(floatValue);
	as400Value[offset]   = (byte)(bits >>> 24);
	as400Value[offset+1] = (byte)(bits >>> 16);
	as400Value[offset+2] = (byte)(bits >>>  8);
	as400Value[offset+3] = (byte) bits;
    }

    /**
      Convert the specified AS/400 data type to a float.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  a float corresponding to the data type.
     **/
    public static float byteArrayToFloat(byte[] as400Value, int offset)
    {
	int bits = ((as400Value[offset]   & 0xFF) << 24) +
	           ((as400Value[offset+1] & 0xFF) << 16) +
	           ((as400Value[offset+2] & 0xFF) <<  8) +
	            (as400Value[offset+3] & 0xFF);
	return Float.intBitsToFloat(bits);
    }

    /**
      Convert the specified double into AS/400 format in the specified byte array.
      @param  doubleValue  the value to be converted to AS/400 format.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void doubleToByteArray(double doubleValue, byte[] as400Value, int offset)
    {
	long bits = Double.doubleToLongBits(doubleValue);
	// Do in two parts to avoid long temps
	int high = (int)(bits >>> 32);
	int low = (int)bits;

	as400Value[offset]   = (byte)(high >>> 24);
	as400Value[offset+1] = (byte)(high >>> 16);
	as400Value[offset+2] = (byte)(high >>>  8);
	as400Value[offset+3] = (byte) high;

	as400Value[offset+4] = (byte)(low >>> 24);
	as400Value[offset+5] = (byte)(low >>> 16);
	as400Value[offset+6] = (byte)(low >>>  8);
	as400Value[offset+7] = (byte) low;
    }

    /**
      Convert the specified AS/400 data type to a double.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  a double corresponding to the data type.
     **/
    public static double byteArrayToDouble(byte[] as400Value, int offset)
    {
	// Do in two parts to avoid long temps
	int firstPart =  ((as400Value[offset]   & 0xFF) << 24) +
	                 ((as400Value[offset+1] & 0xFF) << 16) +
	                 ((as400Value[offset+2] & 0xFF) <<  8) +
	                  (as400Value[offset+3] & 0xFF);
	int secondPart = ((as400Value[offset+4] & 0xFF) << 24) +
	                 ((as400Value[offset+5] & 0xFF) << 16) +
	                 ((as400Value[offset+6] & 0xFF) <<  8) +
	                  (as400Value[offset+7] & 0xFF);
	long bits = ((long)firstPart << 32) + (secondPart & 0xFFFFFFFFL);
	return Double.longBitsToDouble(bits);
    }

    /**
      Convert the specified int into AS/400 format in the specified byte array.
      @param  intValue  the value to be converted to AS/400 format.  The integer should be greater than or equal to zero and representable in two bytes.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void unsignedShortToByteArray(int intValue, byte[] as400Value, int offset)
    {
	as400Value[offset]   = (byte)(intValue >>> 8);
	as400Value[offset+1] = (byte) intValue;
    }

    /**
      Convert the specified AS/400 data type to an int.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  an int corresponding to the data type.
     **/
    public static int byteArrayToUnsignedShort(byte[] as400Value, int offset)
    {
	return ((as400Value[offset]   & 0xFF) <<  8) +
	        (as400Value[offset+1] & 0xFF);
    }

    /**
      Convert the specified long into AS/400 format in the specified byte array.
      @param  longValue  the value to be converted to AS/400 format.  The long should be greater than or equal to zero and representable in four bytes.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void unsignedIntToByteArray(long longValue, byte[] as400Value, int offset)
    {
	// Grab bits into int to avoid long temps
	int bits = (int)longValue;
	as400Value[offset]   = (byte)(bits >>> 24);
	as400Value[offset+1] = (byte)(bits >>> 16);
	as400Value[offset+2] = (byte)(bits >>>  8);
	as400Value[offset+3] = (byte) bits;
    }

    /**
      Convert the specified AS/400 data type to a long.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  a long corresponding to the data type.
     **/
    public static long byteArrayToUnsignedInt(byte[] as400Value, int offset)
    {
	return (((as400Value[offset]   & 0xFF) << 24) +
		((as400Value[offset+1] & 0xFF) << 16) +
		((as400Value[offset+2] & 0xFF) <<  8) +
		 (as400Value[offset+3] & 0xFF))         & 0xFFFFFFFFL;
    }

    /**
      Convert the specified long into AS/400 format in the specified byte array.
      @param  longValue  the value to be converted to AS/400 format.
      @param  as400Value  the array to receive the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
     **/
    public static void longToByteArray(long longValue, byte[] as400Value, int offset)
    {
	// Do in two parts to avoid long temps
	int high = (int)(longValue >>> 32);
	int low = (int)longValue;

	as400Value[offset]   = (byte)(high >>> 24);
	as400Value[offset+1] = (byte)(high >>> 16);
	as400Value[offset+2] = (byte)(high >>>  8);
	as400Value[offset+3] = (byte) high;

	as400Value[offset+4] = (byte)(low >>> 24);
	as400Value[offset+5] = (byte)(low >>> 16);
	as400Value[offset+6] = (byte)(low >>>  8);
	as400Value[offset+7] = (byte) low;
    }

    /**
      Convert the specified AS/400 data type to a long.
      @param  as400Value  the array containing the data type in AS/400 format.
      @param  offset  the offset into the byte array for the start of the AS/400 value.
      @return  a long corresponding to the data type.
     **/
    public static long byteArrayToLong(byte[] as400Value, int offset)
    {
	// Do in two parts to avoid long temps
	int firstPart =  ((as400Value[offset]   & 0xFF) << 24) +
	                 ((as400Value[offset+1] & 0xFF) << 16) +
	                 ((as400Value[offset+2] & 0xFF) <<  8) +
	                  (as400Value[offset+3] & 0xFF);
	int secondPart = ((as400Value[offset+4] & 0xFF) << 24) +
	                 ((as400Value[offset+5] & 0xFF) << 16) +
	                 ((as400Value[offset+6] & 0xFF) <<  8) +
	                  (as400Value[offset+7] & 0xFF);
	return ((long)firstPart << 32) + (secondPart & 0xFFFFFFFFL);
    }
}
