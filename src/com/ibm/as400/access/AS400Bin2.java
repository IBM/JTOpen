///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Bin2.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400Bin2 class provides a converter between a Short object and a signed two-byte binary number.
 **/
public class AS400Bin2 implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private static final int SIZE = 2;
    private static final short defaultValue = 0;

    /**
     * Constructs an AS400Bin2 object.
     **/
    public AS400Bin2()
    {
    }

    /**
     * Creates a new AS400Bin2 that is identical to the current instance.
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
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int getByteLength()
    {
     return SIZE;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The Short object with a value of zero.
     **/
    public Object getDefaultValue()
    {
     return new Short(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_BIN2 TYPE_BIN2}.
     * @return <tt>AS400DataType.TYPE_BIN2</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_BIN2;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>Short.class</tt>.
     **/
    public Class getJavaType()
    {
      return Short.class;
    }

    /**
     * Converts the specified Java object to IBM i format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Short.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.shortToByteArray(((Short)javaValue).shortValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return as400Value;
    }

    /**
     * Converts the specified short to IBM i format.
     * @param shortValue The value to be converted to IBM i format.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(short shortValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.shortToByteArray(shortValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Short.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.shortToByteArray(((Short)javaValue).shortValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified short into IBM i format in the specified byte array.
     * @param shortValue The value to be converted to IBM i format.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(short shortValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.shortToByteArray(shortValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Short.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.shortToByteArray(((Short)javaValue).shortValue(), as400Value, offset);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified short into IBM i format in the specified byte array.
     * @param shortValue The value to be converted to IBM i format.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(short shortValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.shortToByteArray(shortValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The Short object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Short(BinaryConverter.byteArrayToShort(as400Value, 0));
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return The Short object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Short(BinaryConverter.byteArrayToShort(as400Value, offset));
    }

    /**
     * Converts the specified IBM i data type to a short.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The short corresponding to the data type.
     **/
    public short toShort(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToShort(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to a short.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The short corresponding to the data type.
     **/
    public short toShort(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToShort(as400Value, offset);
    }
}
