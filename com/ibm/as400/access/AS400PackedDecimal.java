///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400PackedDecimal.java
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
 * Provides a converter between a BigDecimal object and a packed decimal format floating point number.
 **/
public class AS400PackedDecimal implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private int digits_;
    private int scale_;
    private static final long defaultValue = 0;
    static final boolean HIGH_NIBBLE = true;
    static final boolean LOW_NIBBLE  = false;

    private boolean useDouble_ = false;

    /**
     * Constructs an AS400PackedDecimal object.
     * @param numDigits The number of digits in the packed decimal number.  It must be greater than or equal to one and less than or equal to thirty-one.
     * @param numDecimalPositions The number of decimal positions in the packed decimal number.  It must be greater than or equal to zero and less than or equal to <i>numDigits</i>.
     **/
    public AS400PackedDecimal(int numDigits, int numDecimalPositions)
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
     this.digits_ = numDigits;
     this.scale_ = numDecimalPositions;
    }

    /**
     * Creates a new AS400PackedDecimal object that is identical to the current instance.
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
     return this.digits_/2+1;
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
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_PACKED TYPE_PACKED}.
     * @return <tt>AS400DataType.TYPE_PACKED</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_PACKED;
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
     * Returns the total number of digits in the packed decimal number.
     * @return The number of digits.
     **/
    public int getNumberOfDigits()
    {
     return this.digits_;
    }

    /**
     * Returns the number of decimal positions in the packed decimal number.
     * @return The number of decimal positions.
     **/
    public int getNumberOfDecimalPositions()
    {
     return this.scale_;
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
     * @param b 
     * @see com.ibm.as400.access.AS400ZonedDecimal#setUseDouble
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
     byte[] as400Value = new byte[this.digits_/2+1];
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
     * @param javaValue An object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     int outDigits = this.digits_;
     int outDecimalPlaces = this.scale_;
     int outLength = outDigits/2+1;
     
     // verify input
     BigDecimal inValue = null;
     try {
       inValue = (BigDecimal)javaValue; // Let this line throw ClassCastException
     }
     catch (ClassCastException e) {
       Trace.log(Trace.ERROR, "ClassCastException when attempting to cast a " + javaValue.getClass().getName() + " to a BigDecimal", e);
       throw e;
     }
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

     // calculate number of leading zero's
     int leadingZeros = (outDigits % 2 == 0) ? (outDigits - inLength + 1) : (outDigits - inLength);

     // write correct number of leading zero's, allow ArrayIndexException to be thrown below
     for (int i=0; i<leadingZeros-1; i+=2)
     {
         as400Value[offset++] = 0;
     }

     // if odd number of leading zero's, write leading zero and first digit
     if (leadingZeros > 0)
     {
       if (leadingZeros % 2 != 0)
       {
         as400Value[offset++] = (byte)(inChars[inPosition++] & 0x000F);
       }
     }
     else if (Trace.traceOn_)
     {
       Trace.log(Trace.DIAGNOSTIC, "The calculated number of leading zeros is negative.", leadingZeros);
     }

     int firstNibble;
     int secondNibble;
     // place all the digits except last one
     while (inPosition < inChars.length-1)
     {
         firstNibble = (inChars[inPosition++] & 0x000F) << 4;
         secondNibble = inChars[inPosition++] & 0x000F;
         as400Value[offset++] = (byte)(firstNibble + secondNibble);
     }

     // place last digit and sign nibble
     firstNibble = (inChars[inPosition++] & 0x000F) << 4;
     if (sign != -1)
     {
         as400Value[offset++] = (byte)(firstNibble + 0x000F);
     }
     else
     {
         as400Value[offset++] = (byte)(firstNibble + 0x000D);
     }
     return outLength;
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
        byte[] as400Value = new byte[digits_/2+1];
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
        int effectiveScale = (scale_ > 15) ? 15 : scale_;       
        long rightSide = (long)Math.round((absValue - (double)leftSide) * Math.pow(10, effectiveScale));

        // Ok, now we are done with any double arithmetic!
        int length = digits_/2;
        int b = offset + length;
        boolean nibble = true; // true for left nibble, false for right nibble.

        // If the effective scale is different than the actual scale,
        // then pad with zeros.
        int scaleDifference = scale_ - effectiveScale;
        for (int i = 1; i <= scaleDifference; ++i) {
            if (nibble) {
                as400Value[b] &= (byte)(0x000F);
                --b;
            }
            else {
                as400Value[b] &= (byte)(0x00F0);
            }
            nibble = !nibble;
        }

        // Compute the bytes for the right side of the decimal point. 
        int nextDigit;
        for (int i = 1; i <= effectiveScale; ++i) {
            nextDigit = (int)(rightSide % 10);
            if (nibble) {
                as400Value[b] &= (byte)(0x000F);
                as400Value[b] |= ((byte)nextDigit << 4);
                --b;
            }
            else {
                as400Value[b] &= (byte)(0x00F0);
                as400Value[b] |= (byte)nextDigit;
            }
            nibble = !nibble;
            rightSide /= 10;
        }

        // Compute the bytes for the left side of the decimal point.
        int leftSideDigits = digits_ - scale_;
        for (int i = 1; i <= leftSideDigits; ++i) {
            nextDigit = (int)(leftSide % 10);
            if (nibble) {
                as400Value[b] &= (byte)(0x000F);
                as400Value[b] |= ((byte)nextDigit << 4);
                --b;
            }
            else {
                as400Value[b] &= (byte)(0x00F0);
                as400Value[b] |= (byte)nextDigit;
            }
            nibble = !nibble;
            leftSide /= 10;
        }

        // Zero out the left part of the value, if needed.
        while (b >= offset) {
            if (nibble) {
                as400Value[b] &= (byte)(0x000F);
                --b;
            }
            else {
                as400Value[b] &= (byte)(0x00F0);
            }
            nibble = !nibble;
        }

        // Fix the sign.
        b = offset + length;
        as400Value[b] &= (byte)(0x00F0);
        as400Value[b] |= (byte)((doubleValue >= 0) ? 0x000F : 0x000D);

        // If left side still has digits, then the value was too big
        // to fit.
        if (leftSide > 0)
            throw new ExtendedIllegalArgumentException("doubleValue", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        return length+1;
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
        double multiplier = Math.pow(10, -scale_);
        int rightMostOffset = offset + digits_/2;
        boolean nibble = true; // true for left nibble, false for right nibble.
        for(int i = rightMostOffset; i >= offset;) {
            if (nibble) {
                doubleValue += (byte)((as400Value[i] & 0x00F0) >> 4) * multiplier;
                --i;
            }
            else {
                doubleValue += ((byte)(as400Value[i] & 0x000F)) * multiplier;
            }

            multiplier *= 10;
            nibble = ! nibble;
        }
                        
        // Determine the sign.
        switch(as400Value[rightMostOffset] & 0x000F) {
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
              throwNumberFormatException(LOW_NIBBLE, rightMostOffset,
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
    public Object toObject(byte[] as400Value)     {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented and the data type must have valid packed decimal format.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)   {
      return toObject(as400Value, offset, false); 
    }
    public Object toObject(byte[] as400Value, int offset, boolean ignoreErrors)     {  /*@Q2C*/
      int startOffset = offset;
      if (useDouble_) return new Double(toDouble(as400Value, offset));

     // Check offset to prevent bogus NumberFormatException message
     if (offset < 0)
     {
         if (ignoreErrors) {             /*@Q2A*/
           return null; 
         } else { 
           throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));
         }
     }

     int numDigits = this.digits_;
     int inputSize = numDigits/2+1;

     // even number of digits will have a leading zero
     if (numDigits%2 == 0) ++numDigits;

     char[] outputData = null;
     int outputPosition = 0; // position in char[]

     // read the sign nibble, allow ArrayIndexException to be thrown
     int nibble = (as400Value[offset+inputSize-1] & 0x0F);
     switch (nibble)
     {
         case 0x0B: // valid negative sign bits
         case 0x0D:
          outputData = new char[numDigits+1];
          outputData[outputPosition++] = '-';
          break;
         case 0x0A: // valid positive sign bits
         case 0x0C:
         case 0x0E:
         case 0x0F:
          outputData = new char[numDigits];
          break;
         default: // others invalid
           if (ignoreErrors) {  /*@Q2A*/
             return null; 
           } else { 
             throwNumberFormatException(LOW_NIBBLE, offset+inputSize-1,
                                     as400Value[offset+inputSize-1] & 0xFF,
                                     as400Value);
           }
          return null; 
     }

     // read all the digits except last one
     while (outputPosition < (outputData.length-1))
     {
         nibble = (as400Value[offset] & 0xFF) >>> 4;
         if (nibble > 0x09) {
           if (ignoreErrors) {   /*@Q2A*/
             return null; 
           } else {
           throwNumberFormatException(HIGH_NIBBLE, offset,
                                      as400Value[offset] & 0xFF,
                                      as400Value);
           }
         }
         outputData[outputPosition] = (char)(nibble | 0x0030);
         outputPosition++;
         
         nibble = (as400Value[offset] & 0x0F);
         if (nibble > 0x09) {
           if (Trace.traceOn_) Trace.log(Trace.ERROR, 
                 " outputPosition="+outputPosition+
                 " outputData.length="+outputData.length +
                 " numDigits = "+numDigits +
                 " this.digits = "+this.digits_ +
                 " offset = "+offset+
                 " startOffset = "+startOffset);
           
           if (ignoreErrors) {    /*@Q2A*/
             return null; 
           } else { 
           throwNumberFormatException(LOW_NIBBLE, offset,
                                      as400Value[offset] & 0xFF,
                                      as400Value);
           }
         }
         offset++; 
         outputData[outputPosition] = (char)(nibble | 0x0030);
         outputPosition++; 
     }

     // read last digit
     nibble = (as400Value[offset] & 0xFF) >>> 4;
     if (nibble > 0x09) {
       if (ignoreErrors) {   /*@Q2A*/
         return null; 
       } else { 
         throwNumberFormatException(HIGH_NIBBLE, offset,
                                  as400Value[offset] & 0xFF,
                                  as400Value);
       }
     }
     outputData[outputPosition] = (char)(nibble | 0x0030);

     // construct New BigDecimal object
     return new BigDecimal(new BigInteger(new String(outputData)), this.scale_);
    }

    static final void throwNumberFormatException(boolean highNibble, int byteOffset, int byteValue, byte[] fieldBytes) throws NumberFormatException
    {
      String text;
      if (highNibble) {
        text = ResourceBundleLoader.getText("EXC_HIGH_NIBBLE_NOT_VALID", Integer.toString(byteOffset), byteToString(byteValue));
      }
      else {
        text = ResourceBundleLoader.getText("EXC_LOW_NIBBLE_NOT_VALID", Integer.toString(byteOffset), byteToString(byteValue));
      }
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Byte sequence is not valid for a field of type 'packed decimal':", fieldBytes);
      NumberFormatException nfe = new NumberFormatException(text);
      if (Trace.traceOn_) Trace.log(Trace.ERROR, nfe); 
      throw nfe; 
    }

    private static final String byteToString(int byteVal)
    {
      int leftDigitValue = (byteVal >>> 4) & 0x0F;
      int rightDigitValue = byteVal & 0x0F;
      char[] digitChars = new char[2];
      // 0x30 = '0', 0x41 = 'A'
      digitChars[0] = leftDigitValue < 0x0A ? (char)(0x30 + leftDigitValue) : (char)(leftDigitValue - 0x0A + 0x41);
      digitChars[1] = rightDigitValue < 0x0A ? (char)(0x30 + rightDigitValue) : (char)(rightDigitValue - 0x0A + 0x41);
      return new String(digitChars);
    }

}
