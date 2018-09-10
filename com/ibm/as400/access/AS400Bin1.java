///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Bin1.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 Provides a converter between a {@link java.lang.Byte Byte} object and a signed one-byte binary number.
 The range of values representable by this data type is -128 through 127 (<tt>Byte.MIN_VALUE</tt> through <tt>Byte.MAX_VALUE</tt>).
 <p>Note: According to the DDS specification, BINARY() fields occupy a minimum of 2 bytes of storage in IBM i records.  Therefore the behavior of this class is not consistent with the semantics of the IBM i BINARY(1) data type.  For that reason, this converter is not yet exploited in PCML, RFML, or XPCML.
 @see AS400UnsignedBin1
 **/
public class AS400Bin1 implements AS400DataType
{
  static final long serialVersionUID = 4L;

  private static final int  SIZE = 1;
  private static final byte MIN_VALUE = (byte)-128; // 0x80 (two's complement)
  private static final byte MAX_VALUE = (byte)127;  // 0x7F
  private static final byte DEFAULT_VALUE = (byte)0;

  /**
   * Constructs an AS400Bin1 object.
   **/
  public AS400Bin1()
  {
  }

  // Implements method of interface AS400DataType.
  /**
   * Creates a new AS400Bin1 object that is identical to the current instance.
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
   * @return The Byte object with a value of zero.
   **/
  public Object getDefaultValue()
  {
    return new Byte(DEFAULT_VALUE);
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_BIN1 TYPE_BIN1}.
   * @return <tt>AS400DataType.TYPE_BIN1</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_BIN1;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns the Java class that corresponds with this data type.
   * @return <tt>Byte.class</tt>.
   **/
  public Class getJavaType()
  {
    return Byte.class;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object to IBM i format.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Byte.
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(Object javaValue)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    byte[] as400Value = new byte[SIZE];
    as400Value[0] = ((Byte)javaValue).byteValue();

    return as400Value;
  }

  /**
   * Converts the specified byte to IBM i format.
   * @param byteValue The value to be converted to IBM i format.
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(byte byteValue)
  {
    byte[] as400Value = new byte[SIZE];
    as400Value[0] = byteValue;
    return as400Value;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Byte.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    as400Value[0] = ((Byte)javaValue).byteValue();
    return SIZE;
  }

  /**
   * Converts the specified byte into IBM i format in the specified byte array.
   * @param byteValue The value to be converted to IBM i format.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(byte byteValue, byte[] as400Value)
  {
    as400Value[0] = byteValue;
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of Byte.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value, int offset)
  {
    // Allow this line to throw ClassCastException and NullPointerException.
    as400Value[offset] = ((Byte)javaValue).byteValue();
    return SIZE;
  }

  /**
   * Converts the specified byte into IBM i format in the specified byte array.
   * @param byteValue The value to be converted to IBM i format.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(byte byteValue, byte[] as400Value, int offset)
  {
    as400Value[offset] = byteValue;
    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return A Byte object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value)
  {
    return new Byte(as400Value[0]);
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   * @return A Byte object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    return new Byte(as400Value[offset]);
  }

  /**
   * Converts the specified IBM i data type to a byte.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return The byte corresponding to the data type.
   **/
  public byte toByte(byte[] as400Value)
  {
    return as400Value[0];
  }

  /**
   * Converts the specified IBM i data type to a byte.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The byte corresponding to the data type.
   **/
  public byte toByte(byte[] as400Value, int offset)
  {
    return as400Value[offset];
  }

}
