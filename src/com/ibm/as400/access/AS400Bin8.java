///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Bin8.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  The AS400Bin8 class provides a converter between a Long object and a signed eight-byte binary number.
 **/
public class AS400Bin8 implements AS400DataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    static final long serialVersionUID = 4L;



    private static final int SIZE = 8;
    private static final long defaultValue = 0L;

    /**
      Constructs an AS400Bin8 object.
     **/
    public AS400Bin8()
    {
    }

    /**
      Creates a new AS400Bin8 object that is identical to the current instance.
      @return  The new object.
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
      Returns the byte length of the data type.
      @return  Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int getByteLength()
    {
	return SIZE;
    }

    /**
      Returns a Java object representing the default value of the data type.
      @return  The Long object with a value of zero.
     **/
    public Object getDefaultValue()
    {
	return new Long(defaultValue);
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_BIN8 TYPE_BIN8}.
     * @return Returns AS400DataType.TYPE_BIN8.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_BIN8;
    }

    /**
      Converts the specified Java object to server format.
      @param  javaValue  The object corresponding to the data type.  It must be an instance of Long.
      @return  The server representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
	byte[] as400Value = new byte[SIZE];
	BinaryConverter.longToByteArray(((Long)javaValue).longValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
	return as400Value;
    }

    /**
      Converts the specified long to server format.
      @param  longValue  The value to be converted to server format.
      @return  The server representation of the data type.
     **/
    public byte[] toBytes(long longValue)
    {
	byte[] as400Value = new byte[SIZE];
	BinaryConverter.longToByteArray(longValue, as400Value, 0);
	return as400Value;
    }

    /**
      Converts the specified Java object into server format in the specified byte array.
      @param  javaValue  The object corresponding to the data type. It must be an instance of Long.
      @param  as400Value  The array to receive the data type in server format.  There must be enough space to hold the server value.
      @return Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
        // BinaryConverter will throw the ArrayIndexException's
	BinaryConverter.longToByteArray(((Long)javaValue).longValue(), as400Value, 0);  // Allow this line to throw ClassCastException and NullPointerException
	return SIZE;
    }

    /**
      Converts the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.
      @param  as400Value  The array to receive the data type in server format.  There must be enough space to hold the server value.
      @return  Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int toBytes(long longValue, byte[] as400Value)
    {
        // BinaryConverter will throw the ArrayIndexException's
	BinaryConverter.longToByteArray(longValue, as400Value, 0);
	return SIZE;
    }

    /**
      Converts the specified Java object into server format in the specified byte array.
      @param  javaValue  The object corresponding to the data type.  It must be an instance of Long.
      @param  as400Value  The array to receive the data type in server format.  There must be enough space to hold the server value.
      @param  offset  The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
      @return  Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
        // BinaryConverter will throw the ArrayIndexException's
	BinaryConverter.longToByteArray(((Long)javaValue).longValue(), as400Value, offset);  // Allow this line to throw ClassCastException and NullPointerException
	return SIZE;
    }

    /**
      Converts the specified long into server format in the specified byte array.
      @param  longValue  The value to be converted to server format.
      @param  as400Value  The array to receive the data type in server format.  There must be enough space to hold the server value.
      @param  offset  The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
      @return  Eight (8), the number of bytes in the server representation of the data type.
     **/
    public int toBytes(long longValue, byte[] as400Value, int offset)
    {
        // BinaryConverter will throw the ArrayIndexException's
	BinaryConverter.longToByteArray(longValue, as400Value, offset);
	return SIZE;
    }

    /**
      Converts the specified server data type to a long.
      @param  as400Value  The array containing the data type in server format.  The entire data type must be represented.
      @return  The long corresponding to the data type.
     **/
    public long toLong(byte[] as400Value)
    {
        // BinaryConverter will throw the ArrayIndexException's
	return BinaryConverter.byteArrayToLong(as400Value, 0);
    }

    /**
      Converts the specified server data type to a long.
      @param  as400Value  The array containing the data type in server format.  The entire data type must be represented.
      @param  offset  The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
      @return  The long corresponding to the data type.
     **/
    public long toLong(byte[] as400Value, int offset)
    {
        // BinaryConverter will throw the ArrayIndexException's
	return BinaryConverter.byteArrayToLong(as400Value, offset);
    }

    /**
      Converts the specified server data type to a Java object.
      @param  as400Value  The array containing the data type in server format.  The entire data type must be represented.
      @return  The Long object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value)
    {
        // BinaryConverter will throw the ArrayIndexException's
	return new Long(BinaryConverter.byteArrayToLong(as400Value, 0));
    }

    /**
      Converts the specified server data type to a Java object.
      @param  as400Value  The array containing the data type in server format.  The entire data type must be represented.
      @param  offset  The offset into the byte array for the start of the server value.  It must be greater than or equal to zero.
      @return  The Long object corresponding to the data type.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
        // BinaryConverter will throw the ArrayIndexException's
	return new Long(BinaryConverter.byteArrayToLong(as400Value, offset));
    }
}
