///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBSQLDADataFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBSQLDADataFormat class is an implementation of
DBDataFormat which describes the data format used in
the SQLDA, specifically for the package cache.
**/
class DBSQLDADataFormat
implements DBDataFormat
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
	private static final int	REPEATED_LENGTH_ 	= 80;

    private byte[]              rawBytes_           = null;
    private int                 offset_             = -1;

    private int                 numberOfFields_     = -1;

    private int                 jobCCSID_;                              // @D1A
    private int                 length_             = -1;



/**
Constructs a DBSQLDADataFormat object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
    public DBSQLDADataFormat(int jobCCSID)                              // @D1C
    { 
        jobCCSID_ = jobCCSID;                                           // @D1A
    }



// @D1D /**
// @D1D Constructs a DBSQLDADataFormat object.  Use this when overlaying
// @D1D on a request datastream.  This sets the cached data so that
// @D1D the total length can be calculated before calling overlay().
// @D1D **/
// @D1D     public DBSQLDADataFormat (int numberOfFields)
// @D1D     {
// @D1D         numberOfFields_ = numberOfFields;
// @D1D     }



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
    public void overlay (byte[] rawBytes, int offset)
    {
	    rawBytes_           = rawBytes;
	    offset_             = offset;

        if (numberOfFields_ == -1)
            numberOfFields_ = BinaryConverter.byteArrayToShort (rawBytes_, offset_ + 14);

        length_             = 16 + numberOfFields_ * REPEATED_LENGTH_;
    }



	public int getLength ()
	{
	    return length_;
	}



    public int getConsistencyToken ()
    {
        // Not applicable.
        return -1;
    }



    public int getNumberOfFields ()
	{
		return numberOfFields_;
    }



    public int getRecordSize()
	{
	    int recordSize = 0;
	    int numberOfFields = getNumberOfFields ();
	    for (int i = 0; i <numberOfFields; ++i)
	        recordSize += getFieldLength (i);
		return recordSize;
	}



	public int getFieldSQLType (int fieldIndex)
	{
		return BinaryConverter.byteArrayToShort (rawBytes_,
		    offset_ + 16 + (fieldIndex * REPEATED_LENGTH_));
	}



	public int getFieldLength (int fieldIndex)
	{
	    // For VARCHARs and VARBINARYs, the length as stored
	    // in the SQLDA does not include the 2 bytes for the
	    // actual length.  Therefore, we need to add 2 for these
	    // types.
	    int length = BinaryConverter.byteArrayToShort (rawBytes_, offset_ + 18 + (fieldIndex * REPEATED_LENGTH_));

	    // @A0A
	    // For type 484 (DECIMAL) and 488 (NUMERIC), the 1st byte in the Field Length
	    // represents the precision and the 2nd byte represents the scale.
	    // Thus, the real field length has to be calculated.
	    int precision = (short) rawBytes_[offset_ + 18 + (fieldIndex * REPEATED_LENGTH_)];  //@A0A
	    int fieldType = getFieldSQLType (fieldIndex) & 0xFFFE;                             //@A0A

	    if (fieldType == 484) {                                                         //@A0A
		// Type 484 is a DECIMAL (Packed Decimal).
		length = (precision / 2) + 1;                                               //@A0A
	    }                                                                               //@A0A
	    else if (fieldType == 488) {                                                    //@A0A
		// Type 488 is a NUMERIC (Zoned Decimal)
		length = precision;                                                         //@A0A
	    }                                                                               //@A0A
            else if ((fieldType == 464) || (fieldType == 472) || (fieldType == 468))        //@F1A
            {                                                                               //@F1A
                //@F1A The graphic datatypes have their length listed in characters, not bytes like the rest.
                length = length*2;                                                          //@F1A
            }                                                                               //@F1A

	    if (isVarType (fieldIndex))
	        length += 2;
	    return length;
	}



	public int getFieldScale (int fieldIndex)
	{
	    // @A1A
	    // Changed code to return 0 when the field is a Smallint/Integer type.
	    // This change is made to support binary fields with scales.
	    int fieldType = getFieldSQLType (fieldIndex) & 0xFFFE;                             //@A1A
	    if (fieldType == 496 || fieldType == 500)                                       //@A1A
		    return 0;                                                               // @A1A

		return rawBytes_[offset_ + 19 + (fieldIndex * REPEATED_LENGTH_)];
	}



	public int getFieldPrecision (int fieldIndex)
  	{
	    // For VARCHARs and VARBINARYs, the length as stored
	    // in the SQLDA does not include the 2 bytes for the
	    // actual length.  Therefore, we need to add 2 for these
	    // types.
	    int length = rawBytes_[offset_ + 18 + (fieldIndex * REPEATED_LENGTH_)];
	    if (isVarType (fieldIndex))
	        length += 2;
	    return length;
	}



	public int getFieldCCSID (int fieldIndex)
	{
		return BinaryConverter.byteArrayToUnsignedShort (rawBytes_, //@E0C
		    offset_ + 34 + (fieldIndex * REPEATED_LENGTH_));
	}



    public int getFieldParameterType (int fieldIndex)
        throws DBDataStreamException
	{
		switch (rawBytes_[offset_ + 48 + (REPEATED_LENGTH_ * fieldIndex)]) {
		    case (byte) 0xC9: // 'I'
		    default:
		        return (byte) 0xF0;
		    case (byte) 0xD6: // 'O'
		        return (byte) 0xF1;
		    case (byte) 0xC2: // 'B'
		        return (byte) 0xF2;
		}

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
		    offset_ + 64 + (fieldIndex * REPEATED_LENGTH_));
	}



	public int getFieldNameCCSID (int fieldIndex)
	{
		return jobCCSID_; // @D1C
	}



	public String getFieldName (int fieldIndex, ConvTable converter)	  throws DBDataStreamException //@P0C
	{
	    return converter.byteArrayToString (rawBytes_,
	        offset_ + 66 + (fieldIndex * REPEATED_LENGTH_),
	        getFieldNameLength(fieldIndex));
	}



    private boolean isVarType (int fieldIndex)
    {
	    int type = getFieldSQLType (fieldIndex) & 0xFFFE;
	    return ((type == 448)       // Varchar.
    	        || (type == 456)    // Varchar long.
	            || (type == 464)    // Graphic (pure DBCS).
	            || (type == 472));   // Graphic long (pure DBCS).
    }



    public void setConsistencyToken (int consistencyToken)
    {
        // Not applicable.
    }



    public void setNumberOfFields (int numberOfFields)
    {
        numberOfFields_ = numberOfFields;
        // Otherwise, not applicable.
    }



    public void setRecordSize (int recordSize)
    {
        // Not applicable.
    }



    public void setFieldDescriptionLength (int fieldIndex)
    {
        // Not applicable.
    }



    public void setFieldSQLType (int fieldIndex, int sqlType)
    {
        // Not applicable.
    }



    public void setFieldLength (int fieldIndex, int length)
    {
        // Not applicable.
    }



    public void setFieldScale (int fieldIndex, int scale)
    {
        // Not applicable.
    }



    public void setFieldPrecision (int fieldIndex, int precision)
    {
        // Not applicable.
    }



    public void setFieldCCSID (int fieldIndex, int ccsid)
    {
        // Not applicable.
    }



    public void setFieldParameterType (int fieldIndex, int parameterType)
    {
        // Not applicable.
    }



    public void setFieldNameLength (int fieldIndex, int nameLength)
    {
        // Not applicable.
    }



    public void setFieldNameCCSID (int fieldIndex, int nameCCSID)
    {
        // Not applicable.
    }



    public void setFieldName (int fieldIndex, String name, ConvTable converter) //@P0C
        throws DBDataStreamException
    {
        // Not applicable.
    }



}


