///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBExtendedDataFormat.java
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
The DBExtendedDataFormat class is an implementation of
DBDataFormat which describes the data format used in
datastreams for V4R4 and later systems.
**/
class DBExtendedDataFormat
implements DBDataFormat
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
  private static final int    FIXED_LENGTH_           = 16;
  private static final int  REPEATED_LENGTH_      = 64;

  private byte[]              rawBytes_           = null;
  private int                 offset_             = -1;

  private int                 numberOfFields_     = -1;

  private int                 length_             = -1;
  private boolean			  csRsData_			  = false;  // @550A indicates whether the data associated with this format is from a stored procedure result set



/**
Constructs a DBExtendedDataFormat object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
  public DBExtendedDataFormat ()
  {
  }



/**
Constructs a DBExtendedDataFormat object.  Use this when overlaying
on a request datastream.  This sets the cached data so that
the total length can be calculated before calling overlay().
**/
  public DBExtendedDataFormat (int numberOfFields)
  {
    numberOfFields_ = numberOfFields;
    length_         = FIXED_LENGTH_ + numberOfFields_ * REPEATED_LENGTH_;
  }



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
  public void overlay (byte[] rawBytes, int offset)
  {
    rawBytes_           = rawBytes;
    offset_             = offset;

    if (numberOfFields_ == -1)
    {
      numberOfFields_ = BinaryConverter.byteArrayToInt (rawBytes_, offset + 4);
      length_         = FIXED_LENGTH_ + numberOfFields_ * REPEATED_LENGTH_;
    }
    else
    {
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
    return BinaryConverter.byteArrayToInt (rawBytes_, offset_ + 12);
  }



  public int getFieldSQLType (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 18 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldLength (int fieldIndex)
  {
    return BinaryConverter.byteArrayToInt (rawBytes_,
                                           offset_ + 20 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldScale (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 24 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldPrecision (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 26 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldCCSID (int fieldIndex)
  {
    return BinaryConverter.byteArrayToUnsignedShort (rawBytes_, //@B0C
                                                     offset_ + 28 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldParameterType (int fieldIndex)
  {
    return rawBytes_[offset_ + 30 + (fieldIndex * REPEATED_LENGTH_)];
  }



  public int getFieldLOBLocator (int fieldIndex)                          // @A1A
  {                                                                       // @A1A
    return BinaryConverter.byteArrayToInt (rawBytes_,                   // @A1A
                                           offset_ + 33 + (fieldIndex * REPEATED_LENGTH_));                // @A1A
  }                                                                       // @A1A


  public int getFieldLOBMaxSize (int fieldIndex)                          // @A1A
  {                                                                       // @A1A
    return BinaryConverter.byteArrayToInt (rawBytes_,                   // @A1A
                                           offset_ + 42 + (fieldIndex * REPEATED_LENGTH_));                // @A1A
  }                                                                       // @A1A


  public int getFieldNameLength (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 46 + (fieldIndex * REPEATED_LENGTH_));
  }



  public int getFieldNameCCSID (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 48 + (fieldIndex * REPEATED_LENGTH_));
  }



  public String getFieldName (int fieldIndex, ConvTable converter) //@P0C
  {
    return converter.byteArrayToString (rawBytes_,
                                        offset_ + 50 + (fieldIndex * REPEATED_LENGTH_),
                                        getFieldNameLength (fieldIndex));
  }



  public void setConsistencyToken (int consistencyToken)
  {
    BinaryConverter.intToByteArray (consistencyToken, rawBytes_,
                                    offset_);
  }



  public void setNumberOfFields (int numberOfFields)
  {
    BinaryConverter.intToByteArray (numberOfFields, rawBytes_,
                                    offset_ + 4);
  }



  public void setRecordSize (int recordSize)
  {
    BinaryConverter.intToByteArray (recordSize, rawBytes_,
                                    offset_ + 12);
  }



  public void setFieldDescriptionLength (int fieldIndex)
  {
    BinaryConverter.shortToByteArray ((short) REPEATED_LENGTH_,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 16);
  }



  public void setFieldSQLType (int fieldIndex, int sqlType)
  {
    BinaryConverter.shortToByteArray ((short) sqlType,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 18);
  }



  public void setFieldLength (int fieldIndex, int length)
  {
    BinaryConverter.intToByteArray (length,
                                    rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 20);
  }



  public void setFieldScale (int fieldIndex, int scale)
  {
    BinaryConverter.shortToByteArray ((short) scale,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 24);
  }



  public void setFieldPrecision (int fieldIndex, int precision)
  {
    BinaryConverter.shortToByteArray ((short) precision,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 26);
  }



  public void setFieldCCSID (int fieldIndex, int ccsid)
  {
    BinaryConverter.shortToByteArray ((short) ccsid,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 28);
  }



  public void setFieldParameterType (int fieldIndex, int parameterType)
  {
    rawBytes_[offset_ + fieldIndex * REPEATED_LENGTH_ + 30] = (byte) parameterType;
  }



  public void setFieldNameLength (int fieldIndex, int nameLength)
  {
    BinaryConverter.shortToByteArray ((short) nameLength,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 46);
  }



  public void setFieldNameCCSID (int fieldIndex, int nameCCSID)
  {
    BinaryConverter.shortToByteArray ((short) nameCCSID,
                                      rawBytes_, offset_ + fieldIndex * REPEATED_LENGTH_ + 48);
  }



  public void setFieldName (int fieldIndex, String name, ConvTable converter) //@P0C
  throws DBDataStreamException
  {
    try
    {
      converter.stringToByteArray (name, rawBytes_,
                                   offset_ + fieldIndex * REPEATED_LENGTH_ + 50);
    }
    catch (CharConversionException e)
    {
      throw new DBDataStreamException ();
    }

    // Pad the remaining field with zeros.
    int length = name.length ();
    int padOffset = offset_ + fieldIndex * REPEATED_LENGTH_ + 50;
    for (int i = length; i < 30; i++)
      rawBytes_[padOffset + i] = 0;
  }


  // @550A - This isn't included in the Extended Data Stream Format
  public int getDateFormat() throws DBDataStreamException {
	  return -1;
  }

  // @550A - This isn't included in the Extended Data Stream Format
  public int getTimeFormat() throws DBDataStreamException {
	return -1;
  }

  // @550A - This isn't included in the Extended Data Stream Format
  public int getDateSeparator() throws DBDataStreamException {
	return -1;
  }

  // @550A - This isn't included in the Extended Data Stream Format
  public int getTimeSeparator() throws DBDataStreamException {
	return -1;
  }
  
  // @550A - returns whether or not this data is associated with a stored procedure result set 
  public boolean getCSRSData()
  {
	  return csRsData_;
  }
  
  //@550A - sets whether or not this data is associated with a stored procedure result set 
  public void setCSRSData(boolean csRsData)
  {
	  csRsData_ = csRsData;
  }
}
