//////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400UnsignedBin4.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400UnsignedBin4 class provides a converter between a Long object and a unsigned four-byte binary number.
 **/
public class AS400UnsignedBin4 implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private static final int SIZE = 4;
    private static final long MIN_VALUE = 0;
    private static final long MAX_VALUE = 0xFFFFFFFFL;
    private static final long defaultValue = 0;

    /**
     * Constructs an AS400UnsignedBin4 object.
     **/
    public AS400UnsignedBin4()
    {
    }

    /**
     * Creates a new unsigned, binary-four data type that is identical to the current instance.
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
     * @return Four (4), the number of bytes in the IBM i representation of the data type.
     **/
    public int getByteLength()
    {
     return SIZE;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The Long object with a value of zero.
     **/
    public Object getDefaultValue()
    {
     return new Long(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_UBIN4 TYPE_UBIN4}.
     * @return <tt>AS400DataType.TYPE_UBIN4</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_UBIN4;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>Long.class</tt>.
     **/
    public Class getJavaType()
    {
      return Long.class;
    }

    /**
     * Converts the specified Java object to IBM i format.
     * @param javaValue The object corresponding to the data type. It must be an instance of Long, and the long must be greater than or equal to zero and representable in four bytes.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     long longValue = ((Long)javaValue).longValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified long to IBM i format.
     * @param longValue The value to be converted to IBM i format.  The long must be greater than or equal to zero and representable in four bytes.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(long longValue)
    {
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("longValue (" + String.valueOf(longValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Long, and the long must be greater than or equal to zero and representable in four bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Four (4), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     long longValue = ((Long)javaValue).longValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified long into IBM i format in the specified byte array.
     * @param longValue The value to be converted to IBM i format.  The long must be greater than or equal to zero and representable in four bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return Four (4), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(long longValue, byte[] as400Value)
    {
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("longValue (" + String.valueOf(longValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Long, and the long must be greater than or equal to zero and representable in four bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return Four (4), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     long longValue = ((Long)javaValue).longValue();  // Allow this line to throw ClassCastException and NullPointerException
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified long into IBM i format in the specified byte array.
     * @param longValue The value to be converted to IBM i format.  The long must be greater than or equal to zero and representable in four bytes.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return Four (4), the number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(long longValue, byte[] as400Value, int offset)
    {
     if (longValue < MIN_VALUE || longValue > MAX_VALUE)
     {
         throw new ExtendedIllegalArgumentException("longValue (" + String.valueOf(longValue) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.unsignedIntToByteArray(longValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified IBM i data type to a long.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The long corresponding to the data type.
     **/
    public long toLong(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToUnsignedInt(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to a long.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return The long corresponding to the data type.
     **/
    public long toLong(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToUnsignedInt(as400Value, offset);
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The Long object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Long(BinaryConverter.byteArrayToUnsignedInt(as400Value, 0));
    }

    /**
     * Converts the specified IBM i data type to a Java object.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     * @return The Long object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Long(BinaryConverter.byteArrayToUnsignedInt(as400Value, offset));
    }
}
