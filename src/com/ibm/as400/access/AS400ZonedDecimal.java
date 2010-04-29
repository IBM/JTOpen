///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400ZonedDecimal.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *  The AS400ZonedDecimal class provides a converter between a BigDecimal object and a zoned decimal format floating point number.
 **/
public class AS400ZonedDecimal implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private int digits;
    private int scale;
    private static final long defaultValue = 0;
    private static final boolean HIGH_NIBBLE = AS400PackedDecimal.HIGH_NIBBLE;
    private static final boolean LOW_NIBBLE  = AS400PackedDecimal.LOW_NIBBLE;

    private boolean useDouble_ = false;

    /**
     * Constructs an AS400ZonedDecimal object.
     * @param numDigits The number of digits in the zoned decimal number. It must be greater than or equal to one and less than or equal to thirty-one.
     * @param numDecimalPositions The number of decimal positions in the zoned decimal number. It must be greater than or equal to zero and less than or equal to <i>numDigits</i>.
     */
    public AS400ZonedDecimal(int numDigits, int numDecimalPositions)
    {
     // check for valid input
     if (numDigits < 1 || numDigits > 63) // @M0C - changed the upper limit here from 31 for JDBC support
     {
         throw new ExtendedIllegalArgumentException("numDigits (" + String.valueOf(numDigits) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     if (numDecimalPositions < 0 || numDecimalPositions > numDigits)
     {
         throw new ExtendedIllegalArgumentException("numDecimalPositions (" + String.valueOf(numDecimalPositions) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }

     // set instance variables
     this.digits = numDigits;
     this.scale = numDecimalPositions;
    }

    /**
     * Creates a new AS400ZonedDecimal object that is identical to the current instance.
     * @return The new object.
     **/
    public Object clone()
    {
     try
     {
         return super.clone();  // Object.clone does not throw exception
     }
     catch (CloneNotSupportedException e)
     {
         Trace.log(Trace.ERROR, "Unexpected cloning error", e);
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
    }

    /**
     * Returns the byte length of the data type.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int getByteLength()
    {
     return this.digits;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The BigDecimal object with a value of zero.
     **/
    public Object getDefaultValue()
    {
     return BigDecimal.valueOf(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_ZONED TYPE_ZONED}.
     * @return <tt>AS400DataType.TYPE_ZONED</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_ZONED;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>BigDecimal.class</tt>.
     **/
    public Class getJavaType()
    {
      return BigDecimal.class;
    }

    /**
     * Returns the total number of digits in the zoned decimal number.
     * @return The number of digits.
     **/
    public int getNumberOfDigits()
    {
     return this.digits;
    }

    /**
     * Returns the number of decimal positions in the zoned decimal number.
     * @return The number of decimal positions.
     **/
    public int getNumberOfDecimalPositions()
    {
     return this.scale;
    }

    /**
     * Indicates if a {@link java.lang.Double Double} object or a
     * {@link java.math.BigDecimal BigDecimal} object will be returned
     * on a call to {@link #toObject toObject()}.
     * @return true if a Double will be returned, false if a BigDecimal
     * will be returned.  The default is false.
    **/
    public boolean isUseDouble()
    {
      return useDouble_;
    }

    /**
     * Sets whether to return a {@link java.lang.Double Double} object or a
     * {@link java.math.BigDecimal BigDecimal} object on a call to
     * {@link #toObject toObject()}.
     * @see com.ibm.as400.access.AS400PackedDecimal#setUseDouble
    **/
    public void setUseDouble(boolean b)
    {
      useDouble_ = b;
    }

    /**
     * Converts the specified Java object to IBM i format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[this.digits];
     this.toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     int outDigits = this.digits;
     int outDecimalPlaces = this.scale;

     // verify input
     BigDecimal inValue = (BigDecimal)javaValue; // Let this line throw ClassCastException
     if (inValue.scale() > outDecimalPlaces)  // Let this line throw NullPointerException
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }

     // read the sign
     int sign = inValue.signum();

     // get just the digits from BigDecimal, "normalize" away sign, decimal place etc.
     char[] inChars = inValue.abs().movePointRight(outDecimalPlaces).toBigInteger().toString().toCharArray();

     // Check overall length
     int inLength = inChars.length;
     if (inLength > outDigits)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }

     int inPosition = 0; // position in char[]

     // write correct number of leading zero's
     for (int i=0; i<outDigits-inLength; ++i)
     {
         as400Value[offset++] = (byte)0xF0;
     }

     // place all the digits except the last one
     while (inPosition < inChars.length-1)
     {
         as400Value[offset++] = (byte)((inChars[inPosition++] & 0x000F) | 0x00F0);
     }

     // place the sign and last digit
     if (sign != -1)
     {
         as400Value[offset] = (byte)((inChars[inPosition] & 0x000F) | 0x00F0);
     }
     else
     {
         as400Value[offset] = (byte)((inChars[inPosition] & 0x000F) | 0x00D0);
     }
     return outDigits;
    }

    // @E0A
    /**
     * Converts the specified Java object to IBM i format.
     *
     * @param doubleValue   The value to be converted to IBM i format.  If the decimal part
     *                      of this value needs to be truncated, it will be rounded towards
     *                      zero.  If the integral part of this value needs to be truncated,
     *                      an exception will be thrown.
     * @return              The IBM i representation of the data type.
     **/
    public byte[] toBytes(double doubleValue)
    {
        byte[] as400Value = new byte[digits];
        toBytes(doubleValue, as400Value, 0);
        return as400Value;
    }

    // @E0A
    /**
     * Converts the specified Java object into IBM i format in 
     * the specified byte array.
     *
     * @param doubleValue   The value to be converted to IBM i format.  If the decimal part
     *                      of this value needs to be truncated, it will be rounded towards
     *                      zero.  If the integral part of this value needs to be truncated,
     *                      an exception will be thrown.
     * @param as400Value    The array to receive the data type in IBM i format.  There must 
     *                      be enough space to hold the IBM i value.
     * @return              The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(double doubleValue, byte[] as400Value)
    {
        return toBytes(doubleValue, as400Value, 0);
    }

    // @E0A
    /**
     * Converts the specified Java object into IBM i format in 
     * the specified byte array.
     *
     * @param doubleValue   The value to be converted to IBM i format.  If the decimal part
     *                      of this value needs to be truncated, it will be rounded towards
     *                      zero.  If the integral part of this value needs to be truncated,
     *                      an exception will be thrown.
     * @param as400Value    The array to receive the data type in IBM i format.  
     *                      There must be enough space to hold the IBM i value.
     * @param offset        The offset into the byte array for the start of the IBM i value. 
     *                      It must be greater than or equal to zero.
     * @return              The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(double doubleValue, byte[] as400Value, int offset)
    {
        // GOAL:  For performance reasons, we need to do this conversion
        //        without creating any Java objects (e.g., BigDecimals,
        //        Strings).

        // If the number is too big, we can't do anything with it.
        double absValue = Math.abs(doubleValue);
        if (absValue > Long.MAX_VALUE)
            throw new ExtendedIllegalArgumentException("doubleValue", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

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
        int rightmostOffset = offset + digits - 1;
        int padOffset = rightmostOffset - (scale - effectiveScale);
        for (int i = rightmostOffset; i > padOffset; --i)
            as400Value[i] = (byte)0x00F0;       

        // Compute the bytes for the right side of the decimal point. 
        int decimalOffset = rightmostOffset - scale;
        int nextDigit;
        for (int i = padOffset; i > decimalOffset; --i) {
            nextDigit = (int)(rightSide % 10);
            as400Value[i] = (byte)(0x00F0 | nextDigit);
            rightSide /= 10;
        }

        // Compute the bytes for the left side of the decimal point.
        for (int i = decimalOffset; i >= offset; --i) {
            nextDigit = (int)(leftSide % 10);
            as400Value[i] = (byte)(0x00F0 | nextDigit);
            leftSide /= 10;
        }

        // Fix the sign, if negative.
        if (doubleValue < 0)
            as400Value[rightmostOffset] = (byte)(as400Value[rightmostOffset] & 0x00DF);

        // If left side still has digits, then the value was too big
        // to fit.
        if (leftSide > 0)
            throw new ExtendedIllegalArgumentException("doubleValue", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        return digits;
    }

    // @E0A
    /**
     * Converts the specified IBM i data type to a Java double value.  If the
     * decimal part of the value needs to be truncated to be represented by a
     * Java double value, then it is rounded towards zero.  If the integral
     * part of the value needs to be truncated to be represented by a Java
     * double value, then it converted to either Double.POSITIVE_INFINITY
     * or Double.NEGATIVE_INFINITY.
     * 
     * @param as400Value The array containing the data type in IBM i format.  
     *                   The entire data type must be represented.
     * @return           The Java double value corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value)
    {
        return toDouble(as400Value, 0);
    }

    // @E0A
    /**
     * Converts the specified IBM i data type to a Java double value.  If the
     * decimal part of the value needs to be truncated to be represented by a
     * Java double value, then it is rounded towards zero.  If the integral
     * part of the value needs to be truncated to be represented by a Java
     * double value, then it converted to either Double.POSITIVE_INFINITY
     * or Double.NEGATIVE_INFINITY.
     * 
     * @param as400Value The array containing the data type in IBM i format.  
     *                   The entire data type must be represented.
     * @param offset     The offset into the byte array for the start of the IBM i value.  
     *                   It must be greater than or equal to zero.
     * @return           The Java double value corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value, int offset)
    {
        // Check the offset to prevent bogus NumberFormatException message.
        if (offset < 0)
            throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));

        // Compute the value.
        double doubleValue = 0;
        double multiplier = Math.pow(10, digits - scale - 1);
        int rightMostOffset = offset + digits - 1;
        for(int i = offset; i <= rightMostOffset; ++i) {
            doubleValue += ((byte)(as400Value[i] & 0x000F)) * multiplier;
            multiplier /= 10;
        }
                        
        // Determine the sign.
        switch(as400Value[rightMostOffset] & 0x00F0) {
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
                throwNumberFormatException(HIGH_NIBBLE, rightMostOffset,
                                           as400Value[rightMostOffset] & 0x00FF,
                                           as400Value);
        }

        return doubleValue;
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
      if (useDouble_) return new Double(toDouble(as400Value, offset));

     // Check offset to prevent bogus NumberFormatException message
     if (offset < 0)
     {
         throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));
     }

     int size = this.digits;

     int outputPosition = 0; // position in char[]
     int digitsPlaced = 0; // number of digits moved from input to output

     char[] outputData = null;
     // read the sign bit, allow ArrayIndexException to be thrown
     int nibble = (as400Value[offset+size-1] & 0xFF) >>> 4;
     switch (nibble)
     {
         case 0x000B: // valid negative sign bits
         case 0x000D:
          outputData = new char[size+1];
          outputData[outputPosition++] = '-';
          break;
         case 0x000A: // valid positive sign bits
         case 0x000C:
         case 0x000E:
         case 0x000F:
          outputData = new char[size];
          break;
         default: // others invalid
          throwNumberFormatException(HIGH_NIBBLE, offset+size-1,
                                     as400Value[offset+size-1] & 0xFF,
                                     as400Value);
     }

     // place the digits
     while (outputPosition < outputData.length)
     {
         nibble = as400Value[offset++] & 0x000F;
         if (nibble > 0x0009)
           throwNumberFormatException(LOW_NIBBLE, offset-1,
                                      as400Value[offset-1] & 0x00FF,
                                      as400Value);
         outputData[outputPosition++] = (char)(nibble | 0x0030);
     }

     // construct New BigDecimal object
     return new BigDecimal(new BigInteger(new String(outputData)), this.scale);
    }

    private static final void throwNumberFormatException(boolean highNibble, int byteOffset, int byteValue, byte[] fieldBytes) throws NumberFormatException
    {
      AS400PackedDecimal.throwNumberFormatException(highNibble, byteOffset, byteValue, fieldBytes);
    }

}
