///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400Bin4.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400Bin4 class provides a converter between an Integer object and a signed four-byte binary number.
 **/
public class AS400Bin4 implements AS400DataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    private static final int SIZE = 4;
    private static final int defaultValue = 0;

    /**
     * Constructs an AS400Bin4 object.
     **/
    public AS400Bin4()
    {
    }

    /**
     * Creates a new AS400Bin4 object that is identical to the current instance.
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
     * @return Four (4), the number of bytes in the AS/400 representation of the data type.
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
     * Converts the specified Java object to AS/400 format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Integer.
     * @return The AS/400 representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.intToByteArray(((Integer)javaValue).intValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return as400Value;
    }

    /**
     * Converts the specified int to AS/400 format.
     * @param intValue The value to be converted to AS/400 format.
     * @return The AS/400 representation of the data type.
     **/
    public byte[] toBytes(int intValue)
    {
     byte[] as400Value = new byte[SIZE];
     BinaryConverter.intToByteArray(intValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be an instance of Integer.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return Four (4), the number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.intToByteArray(((Integer)javaValue).intValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified int into AS/400 format in the specified byte array.
     * @param intValue The value to be converted to AS/400 format.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return Four (4), the number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(int intValue, byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.intToByteArray(intValue, as400Value, 0);
     return SIZE;
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of Integer.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return Four (4), the number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.intToByteArray(((Integer)javaValue).intValue(), as400Value, offset);  // Allow this line to throw ClassCastException and NullPointerException
     return SIZE;
    }

    /**
     * Converts the specified int into AS/400 format in the specified byte array.
     * @param intValue The value to be converted to AS/400 format.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return Four (4), the number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(int intValue, byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     BinaryConverter.intToByteArray(intValue, as400Value, offset);
     return SIZE;
    }

    /**
     * Converts the specified AS/400 data type to an int.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The int corresponding to the data type.
     **/
    public int toInt(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToInt(as400Value, 0);
    }

    /**
     * Converts the specified AS/400 data type to an int.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The int corresponding to the data type.
     **/
    public int toInt(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return BinaryConverter.byteArrayToInt(as400Value, offset);
    }

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The Integer object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Integer(BinaryConverter.byteArrayToInt(as400Value, 0));
    }

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The Integer object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     // BinaryConverter will throw the ArrayIndexException's
     return new Integer(BinaryConverter.byteArrayToInt(as400Value, offset));
    }
}
