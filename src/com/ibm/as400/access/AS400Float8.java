///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Float8.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400Float8 class provides a converter between a Double object and a eight-byte floating point number.
 **/
public class AS400Float8 implements AS400DataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    

    static final long serialVersionUID = 4L;


    private static final int SIZE = 8;
    private static final double defaultValue = 0.0;

    /**
     * Constructs an AS400Float8 object.
     */
    public AS400Float8()
    {
    }

    /**
     * Creates a new AS400Float8 object that is identical to the current instance.
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
     * @return Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int getByteLength()
    {
     return SIZE;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The Double object with a value of zero.
     **/
    public Object getDefaultValue()
    {
     return new Double(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_FLOAT8 TYPE_FLOAT8}.
     * @return Returns AS400DataType.TYPE_FLOAT8.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_FLOAT8;
    }

    /**
     * Converts the specified Java object to server format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Double.
     * @return The server representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.doubleToByteArray(((Double)javaValue).doubleValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return as400Value;
    }

    /**
     * Converts the specified double to server format.
     * @param doubleValue The value to be converted to server format.
     * @return The server representation of the data type.
     **/
    public byte[] toBytes(double doubleValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.doubleToByteArray(doubleValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into server format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Double.
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @return Eight (8), the number of bytes in the server representation of the data type.
    **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.doubleToByteArray(((Double)javaValue).doubleValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified double into server format in the specified byte array.
     * @param doubleValue The value to be converted to server format.
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @return Eight (8), the number of bytes in the server representation of the data type.
    **/
    public int toBytes(double doubleValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.doubleToByteArray(doubleValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified Java object into server format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be an instance of Double.
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @param offset The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
     * @return Eight(8), the number of bytes in the server representation of the data type.
    **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.doubleToByteArray(((Double)javaValue).doubleValue(), as400Value, offset);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified double into server format in the specified byte array.
     * @param doubleValue The value to be converted to server format.
     * @param as400Value The array to receive the data type in server format.  There must be enough space to hold the server value.
     * @param offset The offset into the byte array for the start of the server value. It must be greater than or equal to zero.
     * @return Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int toBytes(double doubleValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.doubleToByteArray(doubleValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified server data type to a double.
     * @param as400Value The array containing the data type in server format.  The entire data type must be represented.
     * @return The double corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToDouble(as400Value, 0);
    }

    /**
     * Converts the specified server data type to a double.
     * @param as400Value The array containing the data type in server format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
     * @return The double corresponding to the data type.
     **/
    public double toDouble(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToDouble(as400Value, offset);
    }

    /**
     * Converts the specified server data type to a Java object.
     * @param as400Value The array containing the data type in server format.  The entire data type must be represented.
     * @return The Double object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Double(BinaryConverter.byteArrayToDouble(as400Value, 0));
    }

    /**
     * Converts the specified server data type to a Java object.
     * @param as400Value the array containing the data type in server format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the server value. It must be greater than or equal to zero.
     * @return The Double object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Double(BinaryConverter.byteArrayToDouble(as400Value, offset));
    }
}
