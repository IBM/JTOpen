///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400ZonedDecimal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private int digits;
    private int scale;
    private static final long defaultValue = 0;

    /**
     * Constructs an AS400ZonedDecimal object.
     * @param numDigits The number of digits in the zoned decimal number. It must be greater than or equal to one and less than or equal to thirty-one.
     * @param numDecimalPositions The number of decimal positions in the zoned decimal number. It must be greater than or equal to zero and less than or equal to <i>numDigits</i>.
     */
    public AS400ZonedDecimal(int numDigits, int numDecimalPositions)
    {
     // check for valid input
     if (numDigits < 1 || numDigits > 31)
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
     * @return The number of bytes in the AS/400 representation of the data type.
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
     * Converts the specified Java object to AS/400 format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @return The AS/400 representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[this.digits];
     this.toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits and a less than or equal to number of decimal places.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value. It must be greater than or equal to zero.
     * @return The number of bytes in the AS/400 representation of the data type.
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

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // Check offset to prevent bogus NumberFormatException message
     if (offset < 0)
     {
         throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));
     }

     int size = this.digits;

     int outputPosition = 0; // position in char[]
     int digitsPlaced = 0; // number of digits moved from input to output

     char[] outputData;
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
          throw new NumberFormatException(String.valueOf(offset+size-1));
     }

     // place the digits
     while (outputPosition < outputData.length)
     {
         nibble = as400Value[offset++] & 0x000F;
         if (nibble > 0x0009) throw new NumberFormatException(String.valueOf(offset-1));
         outputData[outputPosition++] = (char)(nibble | 0x0030);
     }

     // construct New BigDecimal object
     return new BigDecimal(new BigInteger(new String(outputData)), this.scale);
    }
}
