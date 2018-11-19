///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400UnsignedBin2.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400UnsignedBin2 class provides a converter between an Integer object and an unsigned two-byte binary number.
 **/
public class AS400UnsignedBin2 implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private static final int SIZE = 2;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 0xFFFF;
    private static final int defaultValue = 0;

    /**
     * Constructs an AS400UnsignedBin2 object.
     **/
    public AS400UnsignedBin2()
    {
    }

    /**
     * Creates a new AS400UnsignedBin2 object that is identical to the current instance.
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
     * @return The Integer object with a value of zero.
     **/
    public Object getDefaultValue()
    {
     return new Integer(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_UBIN2 TYPE_UBIN2}.
     * @return <tt>AS400DataType.TYPE_UBIN2</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_UBIN2;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>Integer.class</tt>.
     **/
    public Class getJavaType()
    {
      return Integer.class;
    }

    /**
     * Converts the specified Java object to IBM i format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Integer, and the integer must be greater than or equal to zero and representable in two bytes.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     int intValue = ((Integer)javaValue).intValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified int to IBM i format.
     * @param intValue The value to be converted to IBM i format.  The integer must be greater than or equal to zero and representable in two bytes.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(int intValue)
    {
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("intValue (" + String.valueOf(intValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Integer, and the integer must be greater than or equal to zero and representable in two bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     int intValue = ((Integer)javaValue).intValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified int into IBM i format in the specified byte array.
     * @param intValue The value to be converted to IBM i format.  The integer must be greater than or equal to zero and representable in two bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(int intValue, byte[] as400Value)
    {
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("intValue (" + String.valueOf(intValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Integer, and the integer must be greater than or equal to zero and representable in two bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     int intValue = ((Integer)javaValue).intValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified int into IBM i format in the specified byte array.
     * @param intValue The value to be converted to IBM i format.  The integer must be greater than or equal to zero and representable in two bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return Two (2), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(int intValue, byte[] as400Value, int offset)
    {
     if (intValue < MIN_VALUE || intValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("intValue (" + String.valueOf(intValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedShortToByteArray(intValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified IBM i data type to an int.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The int corresponding to the data type.
     **/
    public int toInt(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToUnsignedShort(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to an int.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The int corresponding to the data type.
     **/
    public int toInt(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToUnsignedShort(as400Value, offset);
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The Integer object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Integer(BinaryConverter.byteArrayToUnsignedShort(as400Value, 0));
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The Integer object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Integer(BinaryConverter.byteArrayToUnsignedShort(as400Value, offset));
    }
}
