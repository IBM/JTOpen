///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400DataType.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 Provides an interface for conversions between Java objects and byte arrays representing IBM i data types.
 **/
public interface AS400DataType extends Cloneable, Serializable
{
  static final long serialVersionUID = 4L;


  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Array AS400Array} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_ARRAY = 0;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Bin2 AS400Bin2} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_BIN2 = 1;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Bin4 AS400Bin4} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_BIN4 = 2;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Bin8 AS400Bin8} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_BIN8 = 3;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400ByteArray AS400ByteArray} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_BYTE_ARRAY = 4;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Float4 AS400Float4} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_FLOAT4 = 5;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Float8 AS400Float8} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_FLOAT8 = 6;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400PackedDecimal AS400PackedDecimal} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_PACKED = 7;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Structure AS400Structure} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_STRUCTURE = 8;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Text AS400Text} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_TEXT = 9;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400UnsignedBin2 AS400UnsignedBin2} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_UBIN2 = 10;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400UnsignedBin4 AS400UnsignedBin4} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_UBIN4 = 11;
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400ZonedDecimal AS400ZonedDecimal} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_ZONED = 12;
  //@DFA
  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400DecFloat AS400DecFloat} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_DECFLOAT = 13;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Bin1 AS400Bin1} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_BIN1 = 14;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400UnsignedBin1 AS400UnsignedBin1} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_UBIN1 = 15;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400UnsignedBin8 AS400UnsignedBin8} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_UBIN8 = 16;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Date AS400Date} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_DATE = 17;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Time AS400Time} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_TIME = 18;

  /**
   * @deprecated Use {@link #TYPE_TIME TYPE_TIME} instead.
   **/
  public static final int TYPE_TIME_OF_DAY = TYPE_TIME;

  /**
   * Constant representing the instance of this class is an {@link com.ibm.as400.access.AS400Timestamp AS400Timestamp} object.
   * @see #getInstanceType
   **/
  public static final int TYPE_TIMESTAMP = 19;



  /**
   * Creates a new AS400DataType object that is identical to the current instance.
   * @return The new object.
   **/
  public abstract Object clone();  // Implementers must provide a clone() method that is public and does not throw CloneNotSupported Exception

  /**
   * Returns the byte length of the data type.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public abstract int getByteLength();

  /**
   * Returns a Java object representing the default value of the data type.
   * @return The Object of the corresponding data type.
   **/
  public abstract Object getDefaultValue();


  /**
   * Returns an integer constant representing the type of class that implements
   * this interface. This is typically faster than using the instanceof operator, and may prove useful
   * where code needs a primitive type for ease of calculation.
   * Possible values for standard com.ibm.as400.access classes that implement this
   * interface are provided as constants in this class. Note that any implementing class provided
   * by a third party is not guaranteed to correctly return one of the pre-defined constants.
   * @return The type of object implementing this interface.
   **/
  public abstract int getInstanceType();

  /**
   * Returns the Java class that corresponds with this data type.
   * @return The corresponding Java class for this data type.
   **/
  public abstract Class getJavaType();


  /**
   * Converts the specified Java object to IBM i format.
   * @param javaValue The object corresponding to the data type.  It must be an instance of the correct type.
   * @return The IBM i representation of the data type.
   **/
  public abstract byte[] toBytes(Object javaValue);

  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type.  It must be an instance of the correct type.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public abstract int toBytes(Object javaValue, byte[] as400Value);

  /**
   * Converts the specified Java object into IBM i format in the specified byte array.
   * @param javaValue The object corresponding to the data type. It must be an instance of the correct type.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public abstract int toBytes(Object javaValue, byte[] as400Value, int offset);

  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return The object corresponding to the data type.
   **/
  public abstract Object toObject(byte[] as400Value);

  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The object corresponding to the data type.
   **/
  public abstract Object toObject(byte[] as400Value, int offset);
}
