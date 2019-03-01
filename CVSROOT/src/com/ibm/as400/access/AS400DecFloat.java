///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400DecFloat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
//@DFA new class
/**
 *  The AS400DecFloat class provides a converter between a BigDecimal object and a DecimalFloat type.
 **/
public class AS400DecFloat implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private int digits; //Precision 16 or 34
    
    
    private static final long defaultValue = 0;
    static final boolean HIGH_NIBBLE = true;
    static final boolean LOW_NIBBLE  = false;

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

    private static final int[][] tenRadixMagnitude = { { 0x3b9aca00 }, // 10^9
        { 0x0de0b6b3, 0xa7640000 }, // 10^18
        { 0x033b2e3c, 0x9fd0803c, 0xe8000000 }, // 10^27
      };

    /**
     * Constructs an AS400DecFloat object.
     * @param numDigits The number of digits (16 or 34).
     **/
    public AS400DecFloat(int numDigits)
    {
        // check for valid input
        if (numDigits != 16 && numDigits != 34) //34 is max for DecFloat(34)
        {
            throw new ExtendedIllegalArgumentException("numDigits (" + String.valueOf(numDigits) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        // set instance variables
        this.digits = numDigits;

    }

    /**
     * Creates a new AS400DecFloat object that is identical to the current instance.
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
     * @return The number of bytes in the server representation of the data type.
     **/
    public int getByteLength()
    {
        return digits == 16 ? 8 : 16;  //either 8 or 16 bytes on server
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
     * Returns instance type
     * @return <tt>AS400DataType.TYPE_DECFLOAT</tt>.
     **/
    public int getInstanceType()
    {
        return AS400DataType.TYPE_DECFLOAT;
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
     * Returns the total number of digits in the decfloat number.
     * @return The number of digits.
     **/
    public int getNumberOfDigits()
    {
        return this.digits;
    }

    /**
     * Converts the specified Java object to server format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits.
     * @return The server representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
        byte[] as400Value = new byte[this.getByteLength()];
        this.toBytes(javaValue, as400Value, 0);
        return as400Value;
    }

    /**
     * Converts the specified Java object into server format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of BigDecimal and the BigDecimal must have a less than or equal to number of digits.
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @return The number of bytes in the server representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
        return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into server format in the specified byte array.
     * @param javaValue An object corresponding to the data type.  It must be an instance of BigDecimal or String (if value is "NaN", "Infinity", or "-Infinity").
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @param offset The offset into the byte array for the start of the server value. It must be greater than or equal to zero.
     * @return The number of bytes in the server representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
        //verify input
        long specialCombination = 0L; //ieee algorithm says: Combination G (11111-> NaN, 11110-> (-1)^sign Infinity)
        int signalingNaN = -1;  //@sig1 for now, only support non-signaling until decfloat/double etc support it
        if(javaValue instanceof String)
        {
            //special value "NaN", "Infinity", or "-Infinity"
            //use dummy BigDecimal("1" or "-1"), and overlay ieee (Combination G) at end of method
            if ( javaValue.equals("NaN") )
            {
                javaValue = new BigDecimal("1");
                specialCombination = 0x1fL;
                signalingNaN = 0;  //@sig1 non signaling
            }
            else if ( javaValue.equals("-NaN") )
            {
                javaValue = new BigDecimal("-1");
                specialCombination = 0x1fL;
                signalingNaN = 0;  //@sig1 non signaling
            }
            else if ( javaValue.equals("SNaN") )   //@snan
            {
                javaValue = new BigDecimal("1");
                specialCombination = 0x1fL;
                signalingNaN = 1;  //@sig1 signaling
            }
            else if ( javaValue.equals("-SNaN") )  //@snan
            {
                javaValue = new BigDecimal("-1");
                specialCombination = 0x1fL;
                signalingNaN = 1;  //@sig1 signaling
            }
            else if ( javaValue.equals("Infinity") )
            {
                javaValue = new BigDecimal("1"); 
                specialCombination = 0x1eL;
            }
            else if ( javaValue.equals("-Infinity") )
            {
                javaValue = new BigDecimal("-1"); //negative dummy so that sign gets set to negative Infinity
                specialCombination = 0x1eL;
            }
          
        }
        BigDecimal inValue = (BigDecimal) javaValue; // Let this line throw ClassCastException
        
        //get the sign of the BigDecimal.
        int sign = inValue.signum ();
                    
        //get the exponent.
        long exponent = inValue.scale () * (-1);
        
        //get the unscaled value as string.
        String bdUnscaledStr = inValue.abs().unscaledValue().toString ();
        
        if (this.digits == 16) //DECFLOAT16
        {
            //get precision of the BigDecimal.
            int bdPrecision = SQLDataFactory.getPrecisionForTruncation(inValue, 16)[0]; //bdUnscaledStr.length (); //@rnd1

            //bug in jdk1.5 (need to pad with 0z if exp is greater than (maxexp - (maxprecision - precision)) (ie. 9.99E380 since 380>384-(16-3))   //@max
            //so if among the top 16 exponent values, then need to have the precision digits padded with zeros
            int zeros = 0;                                                         //@max
            if((exponent > 368 ) && bdUnscaledStr.length() < 16){ //maxexp-16      //@max
                //pad 0s                                                           //@max
                zeros = 16 - bdUnscaledStr.length();                               //@max
                bdUnscaledStr += "0000000000000000";                               //@max
                bdUnscaledStr = bdUnscaledStr.substring(0, 16);                    //@max
                bdPrecision += zeros;                                              //@max
                exponent -= zeros;                                                 //@max
            }                                                                      //@max
            
            if(bdUnscaledStr.length() > bdPrecision)
                exponent = bdUnscaledStr.length() - bdPrecision; //get exponent in terms of precisionForTruncation
            
            // check for error condition.
            if ((exponent + (bdPrecision - 1)) > 384)
                throw new ExtendedIllegalArgumentException("numDecimalPositions (" + String.valueOf((exponent + (bdPrecision - 1))) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
            else if ((exponent + (bdPrecision - 1)) < -383)
                throw new ExtendedIllegalArgumentException("numDecimalPositions (" + String.valueOf((exponent + (bdPrecision - 1))) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

             
            // compute coefficient digits.
            int[] coefficientDigits = new int[16];
            int zeroBase = '0'; 
            for (int indx = 0; indx < bdPrecision; indx++)
            {
                coefficientDigits[(16 - bdPrecision) + indx] = bdUnscaledStr.charAt(indx) - zeroBase;
            }

            // the result decFloat16 in bits.
            long decFloat16Bits = 0L;

            // mask the coefficient continuation.
            for (int indx = 1; indx < 16; indx += 3)
            {
                decFloat16Bits <<= 10; // declet.  (3 digits are accommondated in 10 bits).
                int decDigits = packDenselyPackedDecimal(coefficientDigits, indx);
                decFloat16Bits |= decDigits;
            }

            // mask the exponent continuation.
            exponent += DEC_FLOAT_16_BIAS;
            decFloat16Bits |= ((exponent & 0xff) << 50);

            // mask the combination.
            long combination;
            if (specialCombination != 0L)
            {
                combination = specialCombination;  //set for Combination G (11111-> NaN, 11110-> (-1)^sign Infinity)
            }
            else if (coefficientDigits[0] >= 8)
            {
                combination = 0x18;
                combination |= ((exponent & 0x300) >> 7);
                combination |= (coefficientDigits[0] & 0x1);
            } else
            {
                combination = 0x0;
                combination |= ((exponent & 0x300) >> 5);
                combination |= coefficientDigits[0];
            }

            decFloat16Bits |= (combination << 58);
 
            // mask the sign bit.
            if (sign == -1)
            {
                decFloat16Bits |= DEC_FLOAT_16_SIGN_MASK;
            }

            as400Value[offset] = (byte) ((decFloat16Bits >> 56) & 0xFF);
            as400Value[offset + 1] = (byte) ((decFloat16Bits >> 48) & 0xFF);
            as400Value[offset + 2] = (byte) ((decFloat16Bits >> 40) & 0xFF);
            as400Value[offset + 3] = (byte) ((decFloat16Bits >> 32) & 0xFF);
            as400Value[offset + 4] = (byte) ((decFloat16Bits >> 24) & 0xFF);
            as400Value[offset + 5] = (byte) ((decFloat16Bits >> 16) & 0xFF);
            as400Value[offset + 6] = (byte) ((decFloat16Bits >> 8) & 0xFF);
            as400Value[offset + 7] = (byte) (decFloat16Bits & 0xFF);
            
            if(signalingNaN == 0)                                                            //@sig1
                as400Value[offset] &= 0xFD;  //non signaling (switch off 7th bit)            //@sig1
            else if (signalingNaN == 1)                                                      //@sig1
                as400Value[offset] |= 0x02;  //signaling (switch on 7th bit)                 //@sig1
            
            return 8;  //always 8 bytes  for DECFLOAT16
        }
        else  //DECFLOAT34
        {
           
            //get precision of the BigDecimal.
            int bdPrecision = SQLDataFactory.getPrecisionForTruncation(inValue, 34)[0]; //bdUnscaledStr.length (); //@rnd1
            
            //bug in jdk1.5                                                        //@max
            int zeros = 0;                                                         //@max
            if((exponent > 6110 ) && bdUnscaledStr.length() < 34){ //maxexp-34     //@max
                //pad 0s                                                           //@max
                zeros = 34 - bdUnscaledStr.length();                               //@max
                bdUnscaledStr += "00000000000000000000000000000000";               //@max
                bdUnscaledStr = bdUnscaledStr.substring(0, 34);                    //@max
                bdPrecision += zeros;                                              //@max
                exponent -= zeros;                                                 //@max
            }                                                                      //@max
            
            if(bdUnscaledStr.length() > bdPrecision)
                exponent = bdUnscaledStr.length() - bdPrecision; //get exponent in terms of precisionForTruncation
            
            // check for error condition.
            if ((exponent + (bdPrecision - 1)) > 6144)
                throw new ExtendedIllegalArgumentException("numDecimalPositions (" + String.valueOf((exponent + (bdPrecision - 1))) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
            else if ((exponent + (bdPrecision - 1)) < -6143)
                throw new ExtendedIllegalArgumentException("numDecimalPositions (" + String.valueOf((exponent + (bdPrecision - 1))) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

             
            // compute coefficient digits.
            int[] coefficientDigits = new int[34];
            int zeroBase = '0';
            for (int indx = 0; indx < bdPrecision; indx++)
            {
                coefficientDigits[(34 - bdPrecision) + indx] = bdUnscaledStr.charAt(indx) - zeroBase;
            }

            // the result decFloat34 in bits.
            long decFloat34BitsHi = 0L; // for high 8 bytes.
            long decFloat34BitsLo = 0L; // for low 8 bytes.

            // mask the coefficient continuation.
            int indx = 1;
            int decDigits;
            // handle the first 12 digits in high 8 bytes.
            for (; indx < 13; indx += 3) {
                decFloat34BitsHi <<= 10; // 3 digits are accommondated in 10 bits.
                decDigits = packDenselyPackedDecimal (coefficientDigits, indx);
                decFloat34BitsHi |= decDigits;
            }
            // handle the 3 digits on the boundary of high and low 8 bytes.
            decDigits = packDenselyPackedDecimal (coefficientDigits, indx);
            decFloat34BitsHi <<= 6;
            decFloat34BitsHi |= ((decDigits & 0x3f0) >> 4); // get high 6 bits of decDigits.
            decFloat34BitsLo |= (decDigits & 0xf); // get low 4 bits of decDigits.
            indx += 3;
            // handle the rest 18 digits in high 8 bytes.
            for (; indx < 34; indx += 3) 
            {
                decFloat34BitsLo <<= 10; // 3 digits are accommondated in 10 bits.
                decDigits = packDenselyPackedDecimal (coefficientDigits, indx);
                decFloat34BitsLo |= decDigits;
            }

            // mask the exponent continuation.
            exponent += DEC_FLOAT_34_BIAS;
            decFloat34BitsHi |= ((exponent & 0xfff) << 46);

            // mask the combination.
            long combination;
            if (specialCombination != 0L)
            {
                combination = specialCombination;  //set for Combination G (11111-> NaN, 11110-> (-1)^sign Infinity)
            }
            else if (coefficientDigits[0] >= 8) 
            {
                combination = 0x18;
                combination |= ((exponent & 0x3000) >> 11);
                combination |= (coefficientDigits[0] & 0x1);
            }
            else 
            {
                combination = 0x0;
                combination |= ((exponent & 0x3000) >> 9);
                combination |= coefficientDigits[0];
            }
            decFloat34BitsHi |= (combination << 58);

            // mask the sign bit.
            if (sign == -1) 
            {
                decFloat34BitsHi |= DEC_FLOAT_34_SIGN_MASK;
            }

            as400Value[offset] = (byte) ((decFloat34BitsHi >> 56) & 0xFF);
            as400Value[offset + 1] = (byte) ((decFloat34BitsHi >> 48) & 0xFF);
            as400Value[offset + 2] = (byte) ((decFloat34BitsHi >> 40) & 0xFF);
            as400Value[offset + 3] = (byte) ((decFloat34BitsHi >> 32) & 0xFF);
            as400Value[offset + 4] = (byte) ((decFloat34BitsHi >> 24) & 0xFF);
            as400Value[offset + 5] = (byte) ((decFloat34BitsHi >> 16) & 0xFF);
            as400Value[offset + 6] = (byte) ((decFloat34BitsHi >> 8) & 0xFF);
            as400Value[offset + 7] = (byte) (decFloat34BitsHi & 0xFF);
            as400Value[offset + 8] = (byte) ((decFloat34BitsLo >> 56) & 0xFF);
            as400Value[offset + 9] = (byte) ((decFloat34BitsLo >> 48) & 0xFF);
            as400Value[offset + 10] = (byte) ((decFloat34BitsLo >> 40) & 0xFF);
            as400Value[offset + 11] = (byte) ((decFloat34BitsLo >> 32) & 0xFF);
            as400Value[offset + 12] = (byte) ((decFloat34BitsLo >> 24) & 0xFF);
            as400Value[offset + 13] = (byte) ((decFloat34BitsLo >> 16) & 0xFF);
            as400Value[offset + 14] = (byte) ((decFloat34BitsLo >> 8) & 0xFF);
            as400Value[offset + 15] = (byte) (decFloat34BitsLo & 0xFF);
            
            if(signalingNaN == 0)                                                            //@sig1
                as400Value[offset] &= 0xFD;  //non signaling (switch off 7th bit)            //@sig1
            else if (signalingNaN == 1)                                                      //@sig1
                as400Value[offset] |= 0x02;  //signaling (switch on 7th bit)                 //@sig1
            
            return 16;  //always 16 bytes for DECFLOAT34

        }
    }

    
    /**
	 * Converts the specified Java object to server format.
	 * 
	 * @param doubleValue
	 *            The value to be converted to server format. If the decimal part
     *            of this value needs to be truncated, it will be rounded based on 
     *            decfloat rounding mode property.
	 * @return The server representation of the data type.
	 */
    public byte[] toBytes(double doubleValue)
    {
        byte[] as400Value = new byte[digits == 16 ? 64 : 128];
        toBytes(doubleValue, as400Value, 0);
        return as400Value;
    }

  
    /**
     * Converts the specified Java object into server format in 
     * the specified byte array.
     *
     * @param doubleValue   The value to be converted to server format.  If the decimal part
     *                      of this value needs to be truncated, it will be rounded based on 
     *                      decfloat rounding mode property.
     * @param as400Value    The array to receive the data type in server format.  There must 
     *                      be enough space to hold the server value.
     * @return              The number of bytes in the server representation of the data type.
     **/
    public int toBytes(double doubleValue, byte[] as400Value)
    {
        return toBytes(doubleValue, as400Value, 0);
    }

    
    /**
     * Converts the specified Java object into server format in the specified byte array.
     *
     * @param doubleValue   The value to be converted to server format.  If the decimal part
     *                      of this value needs to be truncated, it will be rounded based on 
     *                      decfloat rounding mode property.
     * @param as400Value    The array to receive the data type in server format.  
     *                      There must be enough space to hold the server value.
     * @param offset        The offset into the byte array for the start of the server value. 
     *                      It must be greater than or equal to zero.
     * @return              The number of bytes in the server representation of the data type.
     **/
    public int toBytes(double doubleValue, byte[] as400Value, int offset)
    {
        BigDecimal bd = new BigDecimal(doubleValue);
        return toBytes(bd, as400Value, offset);
    }


    /**
     * Converts the specified server data type to a Java double value.  
     * @param as400Value The array containing the data type in server format.  
     *                   The entire data type must be represented.
     * @return           The Java double value corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value)
    {
        return toDouble(as400Value, 0);
    }

     
    /**
     * Converts the specified server data type to a Java double value.  
     * 
     * @param as400Value The array containing the data type in server format.  
     *                   The entire data type must be represented.
     * @param offset     The offset into the byte array for the start of the server value.  
     *                   It must be greater than or equal to zero.
     * @return           The Java double value corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value, int offset)
    {
        // Check the offset to prevent bogus NumberFormatException message.
        if (offset < 0)
            throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));

        // Compute the value.
        BigDecimal bd = (BigDecimal) this.toObject(as400Value, offset);
        return bd.doubleValue();
    }

    /**
     * Converts the specified server data type to a Java object.
     * @param as400Value The array containing the data type in server format.  The entire data type must be represented.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified server data type to a Java object (BigDecimal).
     * @param as400Value The array containing the data type in server format.  The entire data type must be represented and the data type must have valid packed decimal format.
     * @param offset The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
     * @return The BigDecimal object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
        
        // Check offset to prevent bogus NumberFormatException message
        if (offset < 0)
        {
            throw new ArrayIndexOutOfBoundsException(String.valueOf(offset));
        }
                
        if(this.digits == 16)
        {
            long decFloat16Bits = BinaryConverter.byteArrayToLong(as400Value, offset);
            long combination = (decFloat16Bits & DEC_FLOAT_16_COMBINATION_MASK) >> 58;
                
            //compute sign here so we can get -+Infinity values
            int sign = ((decFloat16Bits & DEC_FLOAT_16_SIGN_MASK) == DEC_FLOAT_16_SIGN_MASK) ? -1 : 1;
            
            // deal with special numbers. (not a number and infinity)
            if ((combination == 0x1fL) && ( sign == 1))
            {
                long  nanSignal = (decFloat16Bits & DEC_FLOAT_16_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
                if (nanSignal == 1)
                    throw new ExtendedIllegalArgumentException("SNaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                else
                    throw new ExtendedIllegalArgumentException("NaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                    
            }
            else if ((combination == 0x1fL) && ( sign == -1))
            {
                long  nanSignal = (decFloat16Bits & DEC_FLOAT_16_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
                if (nanSignal == 1)
                    throw new ExtendedIllegalArgumentException("-SNaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                else
                    throw new ExtendedIllegalArgumentException("-NaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            else if ((combination == 0x1eL) && ( sign == 1))
            {
                throw new ExtendedIllegalArgumentException("Infinity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            else if ((combination == 0x1eL) && ( sign == -1))
            {
                throw new ExtendedIllegalArgumentException("-Infinity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }

            // compute the exponent MSD and the coefficient MSD.
            int exponentMSD;
            long coefficientMSD;
            if ((combination & 0x18L) == 0x18L) {
              // format of 11xxx:
              exponentMSD = (int) ((combination & 0x06L) >> 1);
              coefficientMSD = 8 + (combination & 0x01L);
            }
            else {
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
            int coefficientLo = decFloatBitsToDigits ((int) (coefficientContinuation & 0x3fffffff)); // low 30 bits (9 digits)
            int coefficientHi = decFloatBitsToDigits ((int) ((coefficientContinuation >> 30) & 0xfffff)); // high 20 bits (6
            // digits)
            coefficientHi += coefficientMSD * 1000000L;

            // compute the int array of coefficient.
            int[] value = computeMagnitude (new int[] { coefficientHi, coefficientLo });

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


            BigInteger bigInt = new java.math.BigInteger (sign, magnitude);
            return getNewBigDecimal(bigInt, -exponent);
                
        }else
        {
            //decfloat34
            long decFloat34BitsHi = BinaryConverter.byteArrayToLong (as400Value, offset);
            long decFloat34BitsLo = BinaryConverter.byteArrayToLong (as400Value, offset + 8);
            long combination = (decFloat34BitsHi & DEC_FLOAT_34_COMBINATION_MASK) >> 58;

            //compute sign.
            int sign = ((decFloat34BitsHi & DEC_FLOAT_34_SIGN_MASK) == DEC_FLOAT_34_SIGN_MASK) ? -1 : 1;
            
            // deal with special numbers.
            if ((combination == 0x1fL) && ( sign == 1))
            {
                long  nanSignal = (decFloat34BitsHi & DEC_FLOAT_34_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
                if (nanSignal == 1)
                    throw new ExtendedIllegalArgumentException("SNaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                else
                    throw new ExtendedIllegalArgumentException("NaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            else if ((combination == 0x1fL) && ( sign == -1))
            {
                long  nanSignal = (decFloat34BitsHi & DEC_FLOAT_34_SIGNAL_MASK) >> 57; //shift first 7 bits to get signal bit out  //@snan
                if (nanSignal == 1)
                    throw new ExtendedIllegalArgumentException("-SNaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                else
                    throw new ExtendedIllegalArgumentException("-NaN", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            else if ((combination == 0x1eL) && ( sign == 1))
            {
                throw new ExtendedIllegalArgumentException("Infinity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            else if ((combination == 0x1eL) && ( sign == -1))
            {
                throw new ExtendedIllegalArgumentException("-Infinity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            
            // compute the exponent MSD and the coefficient MSD.
            int exponentMSD;
            long coefficientMSD;
            if ((combination & 0x18L) == 0x18L) {
              // format of 11xxx:
              exponentMSD = (int) ((combination & 0x06L) >> 1);
              coefficientMSD = 8 + (combination & 0x01L);
            }
            else {
              // format of xxxxx:
              exponentMSD = (int) ((combination & 0x18L) >> 3);
              coefficientMSD = (combination & 0x07L);
            }

            // compute the exponent.
            int exponent = (int) ((decFloat34BitsHi & DEC_FLOAT_34_EXPONENT_CONTINUATION_MASK) >> 46);
            exponent |= (exponentMSD << 12);
            exponent -= DEC_FLOAT_34_BIAS;

            // compute the coefficient.
            int coefficientLo = decFloatBitsToDigits ((int) (decFloat34BitsLo & 0x3fffffff)); // last 30 bits (9 digits)
            // another 30 bits (9 digits)
            int coefficientMeLo = decFloatBitsToDigits ((int) ((decFloat34BitsLo >> 30) & 0x3fffffff));
            // another 30 bits (9 digits). 26 bits from hi and 4 bits from lo.
            int coefficientMeHi = decFloatBitsToDigits ((int) (((decFloat34BitsHi & 0x3ffffff) << 4) | ((decFloat34BitsLo >> 60) & 0xf)));
            int coefficientHi = decFloatBitsToDigits ((int) ((decFloat34BitsHi >> 26) & 0xfffff)); // high 20 bits (6 digits)
            coefficientHi += coefficientMSD * 1000000L;

            // compute the int array of coefficient.
            int[] value = computeMagnitude (new int[] { coefficientHi, coefficientMeHi, coefficientMeLo, coefficientLo });

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


            java.math.BigInteger bigInt = new java.math.BigInteger (sign, magnitude);
            return getNewBigDecimal(bigInt, -exponent);
        }
    }

//    /**
//     *  helper method to throw exception during conversion 
//     */
//    static final void throwNumberFormatException(boolean highNibble, int byteOffset, int byteValue, byte[] fieldBytes) throws NumberFormatException
//    {
//      AS400PackedDecimal.throwNumberFormatException(highNibble, byteOffset, byteValue, fieldBytes);
//    }

    /** 
     * Converts byte to string */
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


    /**
     * Internal declet encoding helper method. 
     * 
     **/
    private static int packDenselyPackedDecimal (int[] digits, int indx)
    {
        //Declet is the three bit encoding of one decimal digit.  The Decfloat is made up of declets to represent
        //the decfloat 16 or 34 digits
        int result = 0;
        int combination = ((digits[indx+0] & 8) >> 1) | ((digits[indx+1] & 8) >> 2) | ((digits[indx+2] & 8) >> 3);
        switch (combination) {
        case 0: // no, no, no
            result = (digits[indx+0] << 7) | (digits[indx+1] << 4) | digits[indx+2];
            break;
        case 1: // no, no, yes
            result = (digits[indx+0] << 7) | (digits[indx+1] << 4) | (digits[indx+2] & 1) | 8;
            break;
        case 2: // no, yes, no
            result = (digits[indx+0] << 7) | ((digits[indx+2] & 6) << 4) | ((digits[indx+1] & 1) << 4) | (digits[indx+2] & 1) | 10;
            break;
        case 3: // no, yes, yes
            result = (digits[indx+0] << 7) | ((digits[indx+1] & 1) << 4) | (digits[indx+2] & 1) | 78;
            break;
        case 4: // yes, no, no
            result = ((digits[indx+2] & 6) << 7) | ((digits[indx+0] & 1) << 7) | (digits[indx+1] << 4) | (digits[indx+2] & 1) | 12;
            break;
        case 5: // yes, no, yes
            result = ((digits[indx+1] & 6) << 7) | ((digits[indx+0] & 1) << 7) | ((digits[indx+1] & 1) << 4) | (digits[indx+2] & 1) | 46;
            break;
        case 6: // yes, yes, no
            result = ((digits[indx+2] & 6) << 7) | ((digits[indx+0] & 1) << 7) | ((digits[indx+1] & 1) << 4) | (digits[indx+2] & 1) | 14;
            break;
        case 7: // yes, yes, yes
            result = ((digits[indx+0] & 1) << 7) | ((digits[indx+1] & 1) << 4) | (digits[indx+2] & 1) | 110;
            break;
        }
        return result;
    }
    

    /**
     * Internal declet decoding helper method. 
     **/
    private static int unpackDenselyPackedDecimal (int bits)
    {
        //Declet is the three bit encoding of one decimal digit.  The Decfloat is made up of declets to represent
        //the decfloat 16 or 34 digits
        int combination;
        if ((bits & 14) == 14) combination = ((bits & 96) >> 5) | 4;
        else combination = ((bits & 8) == 8) ? (((~bits) & 6) >> 1) : 0;
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
        return ((decoded & 3840) >> 8) * 100 + ((decoded & 240) >> 4) *10 + (decoded & 15);
    }

    /**
     * Compute the int array of magnitude from input value segments.
     */
    private static final int[] computeMagnitude (int[] input)
    {
        int length = input.length;
        int[] mag = new int[length];

        mag[length - 1] = input[length - 1];
        for (int i = 0; i < length - 1; i++) {
            int carry = 0;
            int j = tenRadixMagnitude[i].length - 1;
            int k = length - 1;
            for (; j >= 0; j--, k--) {
                long product = (input[length - 2 - i] & 0xFFFFFFFFL) * (tenRadixMagnitude[i][j] & 0xFFFFFFFFL)
                + (mag[k] & 0xFFFFFFFFL) // add previous value
                + (carry & 0xFFFFFFFFL); // add carry
                carry = (int) (product >>> 32);
                mag[k] = (int) (product & 0xFFFFFFFFL);
            }
            mag[k] = (int) carry;
        }
        return mag;
    }

    /**
     * Convert 30 binary bits coefficient to 9 decimal digits. Note that for performance purpose, 
     * it does not do array-out-of-bound checking.
     */
    private static final int decFloatBitsToDigits (int bits)
    {

        int decimal = 0;
        for (int i = 2; i >= 0; i--) {
            decimal *= 1000;
            decimal += unpackDenselyPackedDecimal ((int)((bits >> (i * 10)) & 0x03ffL));
        }
        return decimal;
    }

    /**
     * This method rounds the number (unscaled integer value and exponent).
     * mcPrecision and mcRoundingMode are what is in jdk 5.0 MathContext.
     * What is returned from this method should be the same as what jre 5.0 would
     * have rounded to using MathContext and BigDecimal.
     */
    private static BigDecimal roundByModePreJDK5(BigInteger intVal, int scale, int mcPrecision, String mcRoundingMode)
    {
 
        BigInteger roundingMax = null;
        if (mcPrecision == 16)
            roundingMax = new BigInteger("10000000000000000"); // 16 0s
        else
            roundingMax = new BigInteger("10000000000000000000000000000000000"); // 34 0s

        BigInteger roundingMin = roundingMax.negate();

        if (roundingMax != null && intVal.compareTo(roundingMax) < 0
                && intVal.compareTo(roundingMin) > 0)
            return getNewBigDecimal(intVal, scale); //rounding not needed
        //get precision from intVal without 0's on right side
        int[] values = SQLDataFactory.getPrecisionForTruncation(getNewBigDecimal(intVal, scale), mcPrecision); //=precisionStr.length() - trimCount; //@rnd1
        int precisionNormalized = values[0]; //@rnd1
        int droppedZeros = values[1];  //@rnd1 decrease scale by number of zeros removed from precision                                       //@rnd1
        if(droppedZeros != 0)                                                 //@rnd1
        {                                                                     //@rnd1
            //adjust intVal number of zeros removed off end                   //@rnd1
            intVal = intVal.divide( new BigInteger("10").pow(droppedZeros));  //@rnd1
        }                                                                     //@rnd1
        
        //get number of digits to round off
        int drop = precisionNormalized - mcPrecision;
        //@rnd1 if (drop <= 0)
          //@rnd1  return getNewBigDecimal(intVal, scale);
        BigDecimal rounded = roundOffDigits(intVal, scale, mcRoundingMode, drop);
        
        if(droppedZeros != 0)                                  //@rnd1
        {                                                      //@rnd1
            //adjust rounded bigdecimal by dropped zero count  //@rnd1
            rounded = rounded.movePointRight(droppedZeros);    //@rnd1
        }                                                      //@rnd1
        
        return rounded;                                        //@rnd1
    }

    /**
     * Helper method to round off digits
     */
    private static BigDecimal roundOffDigits(BigInteger intVal, int scale,
            String mcRoundingMode, int dropCount)
    {

        BigDecimal divisor = new BigDecimal((new BigInteger("10")).pow(dropCount), 0);
        BigDecimal preRoundedBD = getNewBigDecimal(intVal, scale);
        int roundingMode = 0;
        try
        {
            //get int value for RoundingMode from BigDecimal
            roundingMode = ((Integer) Class.forName("java.math.BigDecimal").getDeclaredField(mcRoundingMode).get(null)).intValue(); 
        } catch (Exception e)
        {
            throw new InternalErrorException(InternalErrorException.UNKNOWN); //should never happen
        }
        BigDecimal rounded = preRoundedBD.divide(divisor, scale, roundingMode); // do actual rounding here
        
        BigInteger bigIntPart = rounded.unscaledValue();
        
        rounded = getNewBigDecimal(bigIntPart, scale - dropCount);
    
        return rounded;
    }
    
    /**
    Creates and returns a new BigDecimal based on parameters. 
    This is a temporary hack due to pre-jre 1.5 not being able to handle negative scales (positive exp)
    After we no longer support pre-java 1.5, this method can be replaced with new BigDecimal(bigInt, scale).
    @param  bigInt   BigInteger part.
    @param  scale    scale part.
    **/
    private static BigDecimal getNewBigDecimal(BigInteger bigInt, int scale)
    {
        BigDecimal bigDecimal = null; 
        try{
            bigDecimal = new BigDecimal(bigInt, scale);
        }catch(NumberFormatException e)
        {
            //note that creating BigDecimal with negative scale is ok in 5, but not in 1.4
            //deal with negative scale in pre jdk 5.0 here
            if (scale > 0)
                throw e;
            bigDecimal = new BigDecimal(bigInt);
            bigDecimal = bigDecimal.movePointRight(-scale);
        }
        
        return bigDecimal;
    }
    
    //Decimal float.  //@DFA
    /**
      Rounds the precision of a BigDecimal by removing least significant digits from
      the right side of the precision. (least significant digits could be left or right of implicit decimal point)
      @param  bd BigDecimal to truncate.
      @param  precision to truncate bd to. (16 or 34)
      @param  roundingMode to use when truncating
    **/
    public static BigDecimal roundByMode(BigDecimal bd, int precision, String roundingMode)
    {        
        BigDecimal roundedBD = null;
        
        //MathContext is in jdk1.5.  So use reflection so code will build under pre-1.5
        //later, use this when we move to jdk1.5
        //All we are doing below is:  bdAbs = inValue.abs(new MathContext(16, roundingMode));
        boolean isGEJVM50 = true;
        try
        {
            //in this try block, we do rounding via BigDecimal and MathContext
            Class cls = Class.forName("java.math.MathContext"); //thorw ClassNotFoundException if pre 1.5 jvm
            Constructor ct = cls.getConstructor(new Class[] { Integer.TYPE, Class.forName("java.math.RoundingMode") }); 
            Object arglist[] = new Object[2]; 
            arglist[0] = new Integer(precision); //MathContext.DECIMAL64 (16 or 34 char decfloat precision)
            arglist[1] = Class.forName("java.math.RoundingMode").getDeclaredField(roundingMode.substring(6)).get(null); //@pdc remove "ROUND_"
         
            Object mathContextRounded = ct.newInstance(arglist);  //ie. new MathContext(16or34, RoundingMode.x);
            Object[] arglist2 = new Object[]{mathContextRounded};
            Class[] c = new Class[] { Class.forName("java.math.MathContext") };
            java.lang.reflect.Method method = java.math.BigDecimal.class.getDeclaredMethod("round", c);
                  
            roundedBD = (java.math.BigDecimal) method.invoke(bd, arglist2);
        }  
        //Unfortunately, we cannot just catch Exception since we do not want to miss any real exceptions
        //from rounding etc.  And can't re-throw Exception it since method is not declared with "throws"
        catch (ClassNotFoundException  e)
        { 
            //got exception due to pre-java 5.0.
            isGEJVM50 = false;
        }
        catch (NoSuchMethodException  e)
        { isGEJVM50 = false; }
        catch (NoSuchFieldException  e)
        { isGEJVM50 = false; }
        catch (IllegalAccessException  e)
        { isGEJVM50 = false; }
        catch (InvocationTargetException  e)
        { isGEJVM50 = false; }
        catch (InstantiationException  e)
        { isGEJVM50 = false; }
        
        if(isGEJVM50 == false)
        {
            //use our rounding code to round in pre java 5.0
            roundedBD = roundByModePreJDK5(bd.unscaledValue(), bd.scale(), precision, roundingMode);
        }
        
        return roundedBD;
    }
    
  
}
