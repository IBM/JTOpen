//////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400UnsignedBin8.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigInteger;
import java.util.Arrays;

/**
 Provides a converter between a {@link java.math.BigInteger BigInteger} object and an unsigned eight-byte binary number.
 The range of values representable by this data type is 0 through the maximum integer representable as 8 unsigned bytes (0xFFFFFFFFFFFFFFFF).
 **/
public class AS400UnsignedBin8 implements AS400DataType
{
  static final long serialVersionUID = 4L;

  // Design note: A Java 'long' is an 8-byte signed integer, and a 'double' is an 8-byte signed floating-point.  But we need an unsigned interpretation of an 8-byte field.  Therefore we must use a BigInteger instead of a Double or Long.

  private static final byte[] MAX_VALUE_BYTES = { (byte)0, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
  // Specify a leading 0, to prevent interpretation as a (two's-complement) negative number.

  private static final int SIZE = 8;
  private static final BigInteger MIN_VALUE = BigInteger.ZERO;
  private static final BigInteger MAX_VALUE = new BigInteger(MAX_VALUE_BYTES);
  private static final BigInteger DEFAULT_VALUE = BigInteger.ZERO;


  /**
   * Constructs an AS400UnsignedBin8 object.
   **/
  public AS400UnsignedBin8()
  {
  }

  // Implements method of interface AS400DataType.
  /**
   * Creates a new AS400UnsignedBin8 object that is identical to the current instance.
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

  // Implements method of interface AS400DataType.
  /**
   * Returns the byte length of the data type.
   * @return Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int getByteLength()
  {
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns a Java object representing the default value of the data type.
   * @return {@link java.math.BigInteger#ZERO BigInteger.ZERO}
   **/
  public Object getDefaultValue()
  {
    return DEFAULT_VALUE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns {@link AS400DataType#TYPE_UBIN8 TYPE_UBIN8}.
   * @return <tt>AS400DataType.TYPE_UBIN8</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_UBIN8;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns the Java class that corresponds with this data type.
   * @return <tt>BigInteger.class</tt>.
   **/
  public Class getJavaType()
  {
    return BigInteger.class;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object to IBM i format.
   * @param javaValue The object corresponding to the data type. It must be an instance of BigInteger, and the BigInteger must be greater than or equal to zero and representable in eight bytes (unsigned).
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(Object javaValue)
  {
    byte[] as400Value = new byte[SIZE];  // initialized to zeros
    toBytes(javaValue, as400Value, 0);
    return as400Value;
  }

  /**
   Converts the specified long to IBM i format.
   @param  longValue  The value to be converted to IBM i format.  It must be greater than or equal to zero.
   @return  The IBM i representation of the data type (an 8-byte array).
   **/
  public byte[] toBytes(long longValue)
  {
    byte[] as400Value = new byte[SIZE];
    toBytes(longValue, as400Value, 0);
    return as400Value;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of BigInteger, and the BigInteger must be greater than or equal to zero and representable in eight bytes (unsigned).
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value)
  {
    return toBytes(javaValue, as400Value, 0);
  }

  /**
   Converts the specified long into IBM i format in the specified byte array.
   @param  longValue  The value to be converted to IBM i format.  It must be greater than or equal to zero.
   @param  as400Value  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @return  Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(long longValue, byte[] as400Value)
  {
    return toBytes(longValue, as400Value, 0);
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of BigInteger, and the BigInteger must be greater than or equal to zero and representable in eight bytes (unsigned).
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value, int offset)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    BigInteger bigIntValue = (BigInteger)javaValue;
    if (bigIntValue.compareTo(MIN_VALUE) < 0 ||
        bigIntValue.compareTo(MAX_VALUE) > 0)
    {
      throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    // Allow this line to throw ArrayIndexOutOfBoundsException.
    Arrays.fill(as400Value, offset, offset+SIZE, (byte)0);  // initialize 8 bytes to zeros
    byte[] val = bigIntValue.toByteArray();
    int startPos = (offset+SIZE) - val.length;
    // Right-justify in 8 bytes starting at specified offset.
    System.arraycopy(val, 0, as400Value, startPos, val.length);
    return SIZE;
  }

  /**
   Converts the specified long into IBM i format in the specified byte array.
   @param  longValue  The value to be converted to IBM i format.  It must be greater than or equal to zero.
   @param  as400Value  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @param  offset  The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   @return  Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(long longValue, byte[] as400Value, int offset)
  {
    if (longValue < 0) {
      throw new ExtendedIllegalArgumentException("longValue (" + longValue + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    // BinaryConverter will throw the ArrayIndexException's
    BinaryConverter.longToByteArray(longValue, as400Value, offset);
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return The BigInteger object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value)
  {
    return toBigInteger(as400Value, 0);
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   * @return The BigInteger object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    return toBigInteger(as400Value, offset);
  }

  /**
   Converts the specified IBM i data type to a BigInteger.
   @param  as400Value  The array containing the data type in IBM i format.  The entire data type must be represented.
   @return  The BigInteger corresponding to the data type.
   **/
  public BigInteger toBigInteger(byte[] as400Value)
  {
    return toBigInteger(as400Value, 0);
  }

  /**
   Converts the specified IBM i data type to a BigInteger.
   @param  as400Value  The array containing the data type in IBM i format.  The entire data type must be represented.
   @param  offset  The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   @return  The BigInteger corresponding to the data type.
   **/
  public BigInteger toBigInteger(byte[] as400Value, int offset)
  {
    // Prevent the leading bit being interpreted as a sign bit.
    byte nineBytes[] = new byte[SIZE+1];
    System.arraycopy(as400Value, offset, nineBytes, 1, SIZE);
    return new BigInteger(nineBytes);
  }
}
