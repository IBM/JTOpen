///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ByteArray.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400ByteArray class provides a converter between a byte array and fixed-length byte array representing AS/400 data that is not convertible.
 **/
public class AS400ByteArray implements AS400DataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    static final long serialVersionUID = 4L;



    private int length;
    private static final int defaultValue = 0;

    /**
     * Constructs an AS400ByteArray object.
     * @param length The byte length of the AS/400 byte array.  It must be greater than or equal to zero.
     */
    public AS400ByteArray(int length)
    {
     if (length < 0)
     {
         throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }
     this.length = length;
    }

    /**
     * Creates a new AS400ByteArray object that is identical to the current instance.
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
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int getByteLength()
    {
     return this.length;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The zero length byte array.
     **/
    public Object getDefaultValue()
    {
     return new byte[defaultValue];
    }

    /**
     * Converts the specified Java object to AS/400 format.
     * @param javaValue The object corresponding to the data type. It must be a byte array.  If the provided byte array is not long enough to fill the return array, the remaining bytes will be zero filled.  Any extra bytes in the provided array will not be used.
     * @return The AS/400 representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[this.length];
     this.toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be a byte array.  If the provided byte array is not long enough to fill the return array, the remaining bytes will be zero filled.  Any extra bytes in the provided array will not be used.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be a byte array.  If the provided byte array is not long enough to fill the return array, the remaining bytes will be zero filled.  Any extra bytes in the provided array will not be used.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value. It must be greater than or equal to zero.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     int size = this.length;
     byte[] byteValue = (byte[])javaValue;  // allow this line to throw ClassCastException

     // transfer data from array to array
     int dataSize = (size < byteValue.length) ? size : byteValue.length;
     System.arraycopy(byteValue, 0, as400Value, offset, dataSize);

     // zero fill if necessary
     for (int i = byteValue.length; i < size; ++i)
     {
         as400Value[offset+i] = 0;  // allow this line to throw ArrayIndexException
     }
     return size;
    }

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The byte array corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The byte array corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     int size = this.length;
     byte[] javaValue = new byte[size];
     System.arraycopy(as400Value, offset, javaValue, 0, size);
     return javaValue;
    }
}
