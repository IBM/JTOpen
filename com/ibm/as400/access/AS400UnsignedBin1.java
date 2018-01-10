///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400UnsignedBin1.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 Provides a converter between a {@link java.lang.Short Short} object and an unsigned one-byte binary number.
 The range of values representable by this data type is 0 through 255 (0x00 through 0xFF).
 <p>Note: According to the DDS specification, BINARY() fields occupy a minimum of 2 bytes of storage in IBM i records.  Therefore the behavior of this class is not consistent with the semantics of the IBM i BINARY(1) data type.  For that reason, this converter is not yet exploited in PCML, RFML, or XPCML.
 @see AS400Bin1
 **/
public class AS400UnsignedBin1 implements AS400DataType
{
  static final long serialVersionUID = 4L;

  private static final int   SIZE = 1;
  private static final short MIN_VALUE = 0;
  private static final short MAX_VALUE = 255;  // 0xFF
  private static final short DEFAULT_VALUE = (byte)0;

  /**
   * Constructs an AS400UnsignedBin1 object.
   **/
  public AS400UnsignedBin1()
  {
  }

  // Implements method of interface AS400DataType.
  /**
   * Creates a new AS400UnsignedBin1 object that is identical to the current instance.
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
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int getByteLength()
  {
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns a Java object representing the default value of the data type.
   * @return The Short object with a value of zero.
   **/
  public Object getDefaultValue()
  {
    return new Short(DEFAULT_VALUE);
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns {@link AS400DataType#TYPE_UBIN1 TYPE_UBIN1}.
   * @return <tt>AS400DataType.TYPE_UBIN1</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_UBIN1;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns the Java class that corresponds with this data type.
   * @return <tt>Short.class</tt>.
   **/
  public Class getJavaType()
  {
    return Short.class;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object to IBM i format.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Short, and the short must be in the range 0 through 255.
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(Object javaValue)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    short shortValue = ((Short)javaValue).shortValue();
    return toBytes(shortValue);
  }

  /**
   * Converts the specified short to IBM i format.
   * @param shortValue The value to be converted to IBM i format.  The short must be greater than or equal to zero and representable in one unsigned byte.
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(short shortValue)
  {
    byte[] as400Value = new byte[SIZE];
    toBytes(shortValue, as400Value, 0);
    return as400Value;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Short, and the short must be in the range 0 through 255.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value)
  {
    return toBytes(javaValue, as400Value, 0);
  }

  /**
   * Converts the specified short shorto IBM i format in the specified byte array.
   * @param shortValue The value to be converted to IBM i format.  The short must be greater than or equal to zero and representable in one unsigned byte.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(short shortValue, byte[] as400Value)
  {
    return toBytes(shortValue, as400Value, 0);
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Short, and the short must be in the range 0 through 255.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value, int offset)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    short shortValue = ((Short)javaValue).shortValue();
    return toBytes(shortValue, as400Value, offset);
  }

  /**
   * Converts the specified short into IBM i format in the specified byte array.
   * @param shortValue The value to be converted to IBM i format.  The short must be greater than or equal to zero and representable in one unsigned byte.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(short shortValue, byte[] as400Value, int offset)
  {
    if (shortValue < MIN_VALUE || shortValue > MAX_VALUE)
    {
      throw new ExtendedIllegalArgumentException("shortValue (" + String.valueOf(shortValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    as400Value[offset] = (byte)(shortValue & 0x00FF);
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return A Short object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value)
  {
    // Allow this line to throw NullPointerException and ArrayIndexOutOfBoundsException.
    return new Short(toShort(as400Value, 0));
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return A Short object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    // Allow this line to throw NullPointerException and ArrayIndexOutOfBoundsException.
    return new Short(toShort(as400Value, offset));
  }

  /**
   * Converts the specified IBM i data type to a short.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return The short corresponding to the data type.
   **/
  public short toShort(byte[] as400Value)
  {
    return toShort(as400Value, 0);
  }

  /**
   * Converts the specified IBM i data type to a short.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The short corresponding to the data type.
   **/
  public short toShort(byte[] as400Value, int offset)
  {
    // Allow this line to throw NullPointerException and ArrayIndexOutOfBoundsException.
    return (short)(0x00FF & as400Value[offset]);  // prevent sign-extension
  }

}
