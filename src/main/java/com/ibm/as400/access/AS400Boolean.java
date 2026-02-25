///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Boolean.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2025 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 Provides a converter between a {@link java.lang.Boolean Boolean} object and a signed one-byte boolean value. 
 <p>Note: According to the DDS specification, BOOLEAN() fields occupy 1 byte of storage in IBM i records.  
 **/
public class AS400Boolean implements AS400DataType
{
  static final long serialVersionUID = 4L;

  private static final int  SIZE = 1;
  private static final boolean DEFAULT_VALUE = false;
  private static final byte    SYSTEM_FALSE = (byte) 0xF0; 
  private static final byte    SYSTEM_TRUE = (byte) 0xF1; 

  /**
   * Constructs an AS400Booleanobject.
   **/
  public AS400Boolean()
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
      throw new InternalErrorException(InternalErrorException.UNKNOWN, e);
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
   * @return The Boolean object with a value of false; 
   **/
  public Object getDefaultValue()
  {
    return Boolean.valueOf(DEFAULT_VALUE);
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_BOOLEAN TYPE_BOOLEAN}.
   * @return <tt>AS400DataType.TYPE_BOOLEAN</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_BOOLEAN;
  }

  // Implements method of interface AS400DataType.
  /**
   * Returns the Java class that corresponds with this data type.
   * @return <tt>Boolean.class</tt>.
   **/
  public Class getJavaType()
  {
    return Boolean.class;
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
    if (((Boolean)javaValue).booleanValue()) {
      as400Value[0] = SYSTEM_TRUE;
    } else {
      as400Value[0] = SYSTEM_FALSE; 
    }

    return as400Value;
  }

  /**
   * Converts the specified boolean to IBM i format.
   * @param booleanValue The value to be converted to IBM i format.
   * @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(boolean booleanValue)
  {
    byte[] as400Value = new byte[SIZE];
    if (booleanValue) { 
      as400Value[0] = SYSTEM_TRUE;
    } else {
      as400Value[0] = SYSTEM_FALSE; 
    }
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
    if (((Boolean)javaValue).booleanValue()) {
      as400Value[0] = SYSTEM_TRUE;
    } else {
      as400Value[0] = SYSTEM_FALSE;
    }
    return SIZE;
  }

  /**
   * Converts the specified byte into IBM i format in the specified byte array.
   * @param booleanValue The value to be converted to IBM i format.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(boolean booleanValue, byte[] as400Value)
  {
    if (booleanValue) {
      as400Value[0] = SYSTEM_TRUE;
    } else {
      as400Value[0] = SYSTEM_FALSE; 
    }
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
    if (((Boolean)javaValue).booleanValue()) {
      as400Value[offset] = SYSTEM_TRUE;
    } else {
      as400Value[offset] = SYSTEM_FALSE; /* EBCDIC F */ 
    }

    return SIZE;
  }

  /**
   * Converts the specified boolean into IBM i format in the specified byte array.
   * @param booleanValue The value to be converted to IBM i format.
   * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(boolean booleanValue, byte[] as400Value, int offset)
  {
    if (booleanValue) {
      as400Value[offset] = SYSTEM_TRUE;
    } else {
      as400Value[offset] = SYSTEM_FALSE; /* EBCDIC F */ 
    }

    return SIZE;
  }

  // Implements method of interface AS400DataType.
  /**
   * Converts the specified IBM i data type to a Java object.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return A Boolean object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value)
  {
    return toObject(as400Value,0); 
    
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
    if (as400Value[offset] == SYSTEM_TRUE) 
      return Boolean.TRUE;
      else if (as400Value[offset] == SYSTEM_FALSE)
        return Boolean.FALSE; 
      else
        throw new ArithmeticException("Unable to create boolean from 0x"+Integer.toHexString(0xff & as400Value[offset]));

  }

  /**
   * Converts the specified IBM i data type to a boolean
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @return The byte corresponding to the data type.
   **/
  public boolean toBoolean(byte[] as400Value)
  {
    return toBoolean( as400Value, 0); 
  }

  /**
   * Converts the specified IBM i data type to a byte.
   * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   * @return The byte corresponding to the data type.
   **/
  public boolean toBoolean(byte[] as400Value, int offset)
  {
    if (as400Value[offset] == SYSTEM_TRUE)   
      return true ;
      else if (as400Value[offset] == SYSTEM_FALSE)
        return false; 
      else
        throw new ArithmeticException("Unable to create boolean from 0x"+Integer.toHexString(0xff & as400Value[0]));

  }

}
