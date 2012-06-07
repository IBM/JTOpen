///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: DBSuperExtendedDataFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;

/*
Super Extended Data Format

consistency token - 4 bytes                 0
# of fields - 4 bytes                       4
RESERVED - 4 bytes                          8
	In the release(s) after V5R4, the above reserved bytes are replaced with (unless this is a x3813 codepoint)
	Date Format - 1 byte
	Time Format - 1 byte
	Date Separator - 1 byte
	Time Separator - 1 byte
record size - 4 bytes                       12

//following is repeated for each field
(fixed length info)
field description LL - 2 bytes              16
field (SQL) type - 2 bytes                  18
field length - 4 bytes                      20
field scale - 2 bytes                       24              // scale for numeric or character count for GRAPHIC
field precision - 2 bytes                   26
field CCSID - 2 bytes                       28
RESERVED - 1 byte                           30              (field paramter type for parameter marker format)
field Join Ref Position - 2 bytes           31
RESERVED - 9 bytes                          33              (field lob locator for parameter marker fromat)
field lob locator - 4 bytes                 33              (field lob locator for parameter marker fromat)
field flags - 1 byte                        37              (field flags in x3813 for parameter marker fromat) (bits 3-5 xml)  //@xml3 (bit 2 is array bit) //@array
field max cardinality of array - 4 bytes    38              (field max array size for parameter marker fromat)                      //@array
field Lob max size - 4 bytes                42
Reserved - 2 bytes (for alignment)          46
Offset to variable length info - 4 bytes    48              // offset is based on the start of the fixed length info
Length of variable info - 4 bytes           52
Reserved - 8 bytes                          56

//following describes the variable length info
LL - 4 bytes                                64
CP - 2 bytes                                68              // '3840'x for field name, '38xx'x for future variable length column options
Field name CCSID - 2 bytes                  70
Field Name - Char(*)                        72
*/


/**
The DBSuperExtendedDataFormat class is an implementation of
DBDataFormat which describes the data format used in
datastreams for V5R4 and later systems.
**/
class DBSuperExtendedDataFormat
implements DBDataFormat
{
  // Private data.
  private static final int    FIXED_LENGTH_           = 16;
  private static final int  REPEATED_FIXED_LENGTH_    = 48;
  private byte[]              rawBytes_           = null;
  private int                 offset_             = -1;
  private int                 numberOfFields_     = -1;
  private boolean			  csRsData_			  = false;	// @550A indicates whether or not the data associated with this format is from a stored procedure result set

/**
Constructs a DBSuperExtendedDataFormat object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
  public DBSuperExtendedDataFormat ()
  {
  }

/**
Constructs a DBSuperExtendedDataFormat object.  Use this when overlaying
on a request datastream.  This sets the cached data so that
the total length can be calculated before calling overlay().
**/
  public DBSuperExtendedDataFormat (int numberOfFields)
  {
    numberOfFields_ = numberOfFields;
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
    }
    else
    {
      setNumberOfFields (numberOfFields_);
    }
  }

  public int getConsistencyToken ()
  {
    return BinaryConverter.byteArrayToInt (rawBytes_, offset_);
  }

  public int getNumberOfFields ()
  {
    return numberOfFields_;
  }

  // @550
  public int getDateFormat()
  {
	  return (new Byte(rawBytes_[offset_+ 8])).intValue();
  }

  // @550
  public int getTimeFormat()
  {
	  return (new Byte(rawBytes_[offset_+ 9])).intValue();
  }

  // @550
  public int getDateSeparator()
  {
	  return (new Byte(rawBytes_[offset_+ 10])).intValue();
  }

  // @550
  public int getTimeSeparator()
  {
	  return (new Byte(rawBytes_[offset_+ 11])).intValue();
  }

  public int getRecordSize ()
  {
    return BinaryConverter.byteArrayToInt (rawBytes_, offset_ + 12);
  }

  public int getFieldSQLType (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 18 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  public int getFieldLength (int fieldIndex)
  {
    return BinaryConverter.byteArrayToInt (rawBytes_,
                                           offset_ + 20 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  /* for now, this is in same position */
  public int getArrayFieldLength (int fieldIndex)
  {
    return BinaryConverter.byteArrayToInt (rawBytes_,
                                           offset_ + 20 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }



  public int getFieldScale (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 24 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  public int getFieldPrecision (int fieldIndex)
  {
    return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 26 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  public int getFieldCCSID (int fieldIndex)
  {
    // CCSID of the data that goes in the field/column
    return BinaryConverter.byteArrayToUnsignedShort (rawBytes_,
                                                     offset_ + 28 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  public int getFieldParameterType (int fieldIndex)
  {
    return rawBytes_[offset_ + 30 + (fieldIndex * REPEATED_FIXED_LENGTH_)];
  }

  public int getFieldLOBLocator (int fieldIndex)
  {
    return BinaryConverter.byteArrayToInt (rawBytes_,
                                           offset_ + 33 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  //@xml3 return 0 if single, 1 if is doublebyte
  //Note: if 65535, then this is not applicable
  public int getXMLCharType(int fieldIndex)
  {

      int flag = BinaryConverter.byteArrayToInt (rawBytes_,
              offset_ + 37 + (fieldIndex * REPEATED_FIXED_LENGTH_));
      //flag is actually only 1 byte long
      //array bit is bit #5
      int isDBChar = (flag >> 27) & 0x00000001;
      return isDBChar;
  }

  //@array return 1 if is array, 0 if not
  public int getArrayType(int fieldIndex)
  {
      int flag = BinaryConverter.byteArrayToInt (rawBytes_,
              offset_ + 37 + (fieldIndex * REPEATED_FIXED_LENGTH_));
      //flag is actually only 1 byte long
      //array bit is bit #2
      int isArray = (flag >> 30) & 0x00000001;
      return isArray;

  }

  public int getFieldLOBMaxSize (int fieldIndex)
  {
    return BinaryConverter.byteArrayToInt (rawBytes_,
                                           offset_ + 42 + (fieldIndex * REPEATED_FIXED_LENGTH_));
  }

  public int getFieldNameLength (int fieldIndex)
  {
      // Variable Length info LL - 8
      // LL is the length of the variable length info which includes 4 bytes for the LL, 2 bytes for the CP, 2 bytes for the field name ccsid, and ? bytes for the field name
      int offsetToVariableFieldInformation = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 48 + (fieldIndex * REPEATED_FIXED_LENGTH_));
      int lengthOfVariableFieldInformation  = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 16 + (fieldIndex * REPEATED_FIXED_LENGTH_) + offsetToVariableFieldInformation);  // Length of the variable information for a specific codepoint
      int fieldLength = lengthOfVariableFieldInformation - 8;
      return fieldLength;
  }

  public int getFieldNameCCSID (int fieldIndex)
  {
      // CCSID of the field/column name.  Usually the same as the job ccsid.
      int length = findCodePoint(fieldIndex, 0x3840);
      if(length >=0)
      {
          return BinaryConverter.byteArrayToShort (rawBytes_,
                                             offset_ + 16 + length + 6 + (fieldIndex * REPEATED_FIXED_LENGTH_));
      }
      else
      {
          JDTrace.logInformation("Did not find the code point for the field name");
          return getFieldCCSID(fieldIndex);
      }
  }

  public String getFieldName (int fieldIndex, ConvTable converter)
  {
      int length = findCodePoint(fieldIndex, 0x3840);

      if(length >= 0)
      {
          return converter.byteArrayToString (rawBytes_,
                                        offset_ + 16 + length + 8 + (fieldIndex * REPEATED_FIXED_LENGTH_),
                                        getFieldNameLength (fieldIndex));
      }
      else
      {
          JDTrace.logInformation("Did not find the code point for the field name.");
          return "";
      }
  }

  // Finds a specified codepoint in the variable length information and returns a length that can be used when calculating offsets
  // for the variable length information
  private int findCodePoint(int fieldIndex, int cp){

      int lengthOfVariableInformation = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 52 + (fieldIndex * REPEATED_FIXED_LENGTH_));    // length of the variable information for this field

      int length = 0;    // Used to keep track of the length of all of the variable information for this field

      // retrieve the length of the variable field information for the first codepoint
      int offsetToVariableFieldInformation = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 48 + fieldIndex * REPEATED_FIXED_LENGTH_);
      int lengthOfVariableFieldInformation  = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 16 + (fieldIndex * REPEATED_FIXED_LENGTH_) + offsetToVariableFieldInformation);  // Length of the variable information for a specific codepoint
      // retrieve the first codepoint in the variable length information
      int codePoint = BinaryConverter.byteArrayToShort(rawBytes_, offset_ + 16 + (fieldIndex * REPEATED_FIXED_LENGTH_) + offsetToVariableFieldInformation + 4);;

      // Search until you find the codepoint for the information you want, or until the end of of the variable length info
      // move to the next LL CP in the variable inforamtion
      while((codePoint != cp) && (length < lengthOfVariableInformation))
      {
          length += lengthOfVariableFieldInformation;
          lengthOfVariableFieldInformation = BinaryConverter.byteArrayToInt(rawBytes_, offset_ + 16 + length + (fieldIndex * REPEATED_FIXED_LENGTH_) + offsetToVariableFieldInformation);
          codePoint = BinaryConverter.byteArrayToShort(rawBytes_, offset_ + 16 + length + offsetToVariableFieldInformation + (fieldIndex * REPEATED_FIXED_LENGTH_));
      }

      // Check to see if we found the codepoint, otherwise we looped through all of the information and didn't find the codepoint we wanted
      if(codePoint != cp){
          return -1;
      }

      return length + offsetToVariableFieldInformation;
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
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setRecordSize()");
  }

  public void setFieldDescriptionLength (int fieldIndex)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldDescriptionLength()");
  }

  public void setFieldSQLType (int fieldIndex, int sqlType)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldSQLType()");
  }

  public void setFieldLength (int fieldIndex, int length)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldLength()");
  }

  public void setFieldScale (int fieldIndex, int scale)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldScale()");
  }

  public void setFieldPrecision (int fieldIndex, int precision)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldPrecision()");
  }

  public void setFieldCCSID (int fieldIndex, int ccsid)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldCCSID()");
  }

  public void setFieldParameterType (int fieldIndex, int parameterType)
  {
    rawBytes_[offset_ + fieldIndex * REPEATED_FIXED_LENGTH_ + 30] = (byte) parameterType;
  }

  // Note:  No spot for field name length, could be part of LL - length of variable field info
  public void setFieldNameLength (int fieldIndex, int nameLength)
  {
      // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
      // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldNameLength()");
  }

  public void setFieldNameCCSID (int fieldIndex, int nameCCSID)
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldNameCCSID()");
  }

  public void setFieldName (int fieldIndex, String name, ConvTable converter)
  throws DBDataStreamException
  {
    // not applicable - only called by AS400JDBCPreparedStatement.changeDescriptor()
    // At this time we will continue to use the Extended Data Format.
      Trace.log(Trace.DIAGNOSTIC, "called DBSuperExtendedDataFormat.setFieldName()");
  }

  // Don't know actual length without actually parsing all of the data
  public int getLength ()
  {
    return 0;
  }

  //@550A - returns whether or not this data is associated with a stored procedure result set
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
