///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBOriginalDataFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;



/**
The DBOriginalDataFormat class is an implementation of
DBDataFormat which describes the data format used in
datastreams for V4R3 and previous servers.
**/
class DBOriginalDataFormat
implements DBDataFormat
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
	private static final int	REPEATED_LENGTH_ 	= 54;

    private byte[]              rawBytes_           = null;
    private int                 offset_             = -1;

    private int                 numberOfFields_     = -1;

    private int                 length_             = -1;



/**
Constructs a DBOriginalDataFormat object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
    public DBOriginalDataFormat ()
    { }



/**
Constructs a DBOriginalDataFormat object.  Use this when overlaying
on a request datastream.  This sets the cached data so that
the total length can be calculated before calling overlay().
**/
    public DBOriginalDataFormat (int numberOfFields)
    {
        numberOfFields_ = numberOfFields;
        length_         = 8 + numberOfFields_ * REPEATED_LENGTH_;
    }



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
    public void overlay (byte[] rawBytes, int offset)
    {
	    rawBytes_           = rawBytes;
	    offset_             = offset;

        if (numberOfFields_ == -1) {
            numberOfFields_ = BinaryConverter.byteArrayToShort (rawBytes_, offset + 4);
            length_         = 8 + numberOfFields_ * REPEATED_LENGTH_;
        }
        else {
            setNumberOfFields (numberOfFields_);
        }
    }



	public int getLength ()
	{
	    return length_;
	}



    public int getConsistencyToken ()
    {
        return BinaryConverter.byteArrayToInt (rawBytes_, offset_);
    }



    public int getNumberOfFields ()
    {
        return numberOfFields_;
    }



    public int getRecordSize ()
    {
        return BinaryConverter.byteArrayToShort (rawBytes_, offset_ + 6);
    }



    public int getFieldSQLType (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 10 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldLength (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 12 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldScale (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 14 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldPrecision (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 16 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldCCSID (int fieldIndex)
	{
		return BinaryConverter.byteArrayToUnsignedShort (rawBytes_, //@D0C
		    offset_ + 18 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldParameterType (int fieldIndex)
	{
		return rawBytes_[offset_ + 20 + (fieldIndex * REPEATED_LENGTH_)];
	}


    public int getFieldLOBLocator (int fieldIndex)              // @C1A
    {                                                           // @C1A
        return -1;                                              // @C1A
    }


    public int getFieldLOBMaxSize (int fieldIndex)              // @C1A
    {                                                           // @C1A
        return -1;                                              // @C1A
    }


    public int getFieldNameLength (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 28 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldNameCCSID (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 30 + (fieldIndex * REPEATED_LENGTH_));
	}



    public String getFieldName (int fieldIndex, ConvTable converter) //@P0C
	{
        return converter.byteArrayToString (rawBytes_,
	        offset_ + 32 + (fieldIndex * REPEATED_LENGTH_),
	        getFieldNameLength (fieldIndex));
	}



    public void setConsistencyToken (int consistencyToken)
    {
        BinaryConverter.intToByteArray (consistencyToken, rawBytes_,
            offset_);
    }



    public void setNumberOfFields (int numberOfFields)
    {
        BinaryConverter.shortToByteArray ((short) numberOfFields, rawBytes_,
            offset_ + 4);
    }



    public void setRecordSize (int recordSize)
    {
        BinaryConverter.shortToByteArray ((short) recordSize, rawBytes_,
            offset_ + 6);
    }



    public void setFieldDescriptionLength (int fieldIndex)
    {
		BinaryConverter.shortToByteArray ((short) REPEATED_LENGTH_,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 8);
    }



    public void setFieldSQLType (int fieldIndex, int sqlType)
    {
		BinaryConverter.shortToByteArray ((short) sqlType,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 10);
    }



    public void setFieldLength (int fieldIndex, int length)
    {
		BinaryConverter.shortToByteArray ((short) length,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 12);
    }



    public void setFieldScale (int fieldIndex, int scale)
    {
		BinaryConverter.shortToByteArray ((short) scale,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 14);
    }



    public void setFieldPrecision (int fieldIndex, int precision)
    {
		BinaryConverter.shortToByteArray ((short) precision,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 16);
    }



    public void setFieldCCSID (int fieldIndex, int ccsid)
    {
		BinaryConverter.shortToByteArray ((short) ccsid,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 18);
    }



    public void setFieldParameterType (int fieldIndex, int parameterType)
    {
        rawBytes_[offset_ + fieldIndex * REPEATED_LENGTH_ + 20] = (byte) parameterType;
    }



    public void setFieldNameLength (int fieldIndex, int nameLength)
    {
		BinaryConverter.shortToByteArray ((short) nameLength,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 28);
    }



    public void setFieldNameCCSID (int fieldIndex, int nameCCSID)
    {
		BinaryConverter.shortToByteArray ((short) nameCCSID,
		    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 30);
    }



    public void setFieldName (int fieldIndex, String name, ConvTable converter) //@P0C
        throws DBDataStreamException
    {
    	try {
   	    	converter.stringToByteArray (name, rawBytes_,
   		        offset_ + fieldIndex * REPEATED_LENGTH_ + 32);
       	}
       	catch (CharConversionException e) {
            throw new DBDataStreamException ();
        }

		// Pad the remaining field with zeros.
	  	int length = name.length ();
	  	int padOffset = offset_ + fieldIndex * REPEATED_LENGTH_ + 32;
    	for (int i = length; i < 30; i++)
		    rawBytes_[padOffset + i] = 0;
    }



}
