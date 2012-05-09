///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMField.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

import com.ibm.jtopenlite.*;
import java.io.*;
import java.util.*;
import java.math.*;

/**
 * Represents an individual field of a record format.
**/
public final class DDMField
{
//  public static final char TYPE_LOB = '1';
//  public static final char TYPE_DBCLOB = '3';
//  public static final char TYPE_DATALINK = '4';

  public static final char TYPE_CHARACTER = 'A';
  public static final char TYPE_DBCS_EITHER = 'E';
  public static final char TYPE_DBCS_GRAPHIC = 'G';
  public static final char TYPE_DBCS_ONLY = 'J';
  public static final char TYPE_DBCS_OPEN = 'O';
  public static final char TYPE_BINARY = 'B';
  public static final char TYPE_FLOAT = 'F';
  public static final char TYPE_DECIMAL_FLOAT = '6';
  public static final char TYPE_HEXADECIMAL = 'H';
  public static final char TYPE_BINARY_CHARACTER = '5';
  public static final char TYPE_DATE = 'L';
  public static final char TYPE_PACKED_DECIMAL = 'P';
  public static final char TYPE_ZONED_DECIMAL = 'S';
  public static final char TYPE_TIME = 'T';
  public static final char TYPE_TIMESTAMP = 'Z';

  private final int offset_; // The offset into the record.
  private final String name_;
  private final int length_;
  private final int numDigits_;
  private final int decimalPositions_;
  private final String text_;
  private final char type_;
  private final String defaultValue_;
  private final int ccsid_;
  private final String variableLengthField_;
  private final int allocatedLength_;
  private final String allowNulls_;
  private final String dateTimeFormat_;
  private final String dateTimeSeparator_;
  private final char[] buffer_;

  private HashMap cache_;

  DDMField(final int offset,
           final String name, final int length, final int digits, final int decpos,
           final String text, final char type, final String defaultValue, final int ccsid,
           final String varlen, final int alloc, final String allowNull,
           final String dateFormat, final String dateSeparator)
  {
    offset_ = offset;
    name_ = name;
    length_ = length;
    numDigits_ = digits;
    decimalPositions_ = decpos;
    text_ = text;
    type_ = type;
    defaultValue_ = defaultValue;
    ccsid_ = ccsid;
    variableLengthField_ = varlen;
    allocatedLength_ = alloc;
    allowNulls_ = allowNull;
    dateTimeFormat_ = dateFormat;
    dateTimeSeparator_ = dateSeparator;
    buffer_ = new char[length_*2+2]; //TODO - Is this cool?
  }

  /**
   * Returns a new copy of this field, which is useful if multiple threads
   * need to operate on the same field without contention, as this class
   * is not threadsafe.
  **/
  public DDMField newCopy()
  {
    DDMField f = new DDMField(offset_, name_, length_, numDigits_, decimalPositions_, text_,
                              type_, defaultValue_, ccsid_, variableLengthField_,
                              allocatedLength_, allowNulls_, dateTimeFormat_,
                              dateTimeSeparator_);
    f.setCacheStrings(isCacheStrings());
    return f;
  }

  /**
   * Returns the name (WHFLDE) of this field.
  **/
  public String getName()
  {
    return name_;
  }

  /**
   * Returns the type (WHFLDT) of this field.
  **/
  public char getType()
  {
    return type_;
  }

  /**
   * Returns the length (WHFLDB) of this field.
  **/
  public int getLength()
  {
    return length_;
  }

  /**
   * Returns the offset in the record data where this field begins.
  **/
  public int getOffset()
  {
    return offset_;
  }

  /**
   * Returns the text description (WHFTXT) of this field.
  **/
  public String getText()
  {
    return text_;
  }

  /**
   * Returns the CCSID (WHCCSID) of this field.
  **/
  public int getCCSID()
  {
    return ccsid_;
  }

  /**
   * Returns the default value (WHDFT) of this field.
  **/
  public String getDefaultValue()
  {
    return defaultValue_;
  }

  /**
   * Indicates if this field is variable length (WHVARL).
  **/
  public boolean isVariableLength()
  {
    return variableLengthField_.equals("Y");
  }

  /**
   * Indicates if this field allows null values (WHNULL).
  **/
  public boolean isNullAllowed()
  {
    return allowNulls_.equals("Y");
  }

  /**
   * Returns the database allocated length (WHALLC) of this field.
  **/
  public int getAllocatedLength()
  {
    return allocatedLength_;
  }

  /**
   * Returns the total number of digits (WHFLDO) of this field.
  **/
  public int getNumberOfDigits()
  {
    return numDigits_;
  }

  /**
   * Returns the number of decimal positions (WHFLDP) of this field.
  **/
  public int getDecimalPositions()
  {
    return decimalPositions_;
  }

  /**
   * Returns the date/time format (WHFMT) of this field.
  **/
  public String getDateTimeFormat()
  {
    return dateTimeFormat_;
  }

  /**
   * Returns the date/time separator (WHSEP) of this field.
  **/
  public String getDateTimeSeparator()
  {
    return dateTimeSeparator_;
  }

  /**
   * Indicates if {@link #getString getString()} will cache previously created String values
   * for memory conservation. This can be extremely helpful for fields that contain a finite
   * number of distinct values across all the records in the file.
   * @see #setCacheStrings
  **/
  public boolean isCacheStrings()
  {
    return cache_ != null;
  }

  /**
   * Enables or disables string caching for this field. If enabled, {@link #getString getString()}
   * will cache previously created String values for the purposes of conserving memory. This can
   * be extremely helpful for fields that contain a finite number of distinct values across
   * all the records in the file.
  **/
  public void setCacheStrings(final boolean useCache)
  {
    cache_ = useCache ? new HashMap() : null;
  }

  /**
   * Converts the specified record data at this field's offset into a long value, if possible.
  **/
  public long getLong(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return(long)Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return(long)Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).longValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).longValue();
        }
      default:
        return Long.parseLong(getString(recordData));
    }
  }

  /**
   * Converts the specified record data at this field's offset into a short value, if possible.
  **/
  public short getShort(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return(short)Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return(short)Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return(short)Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return(short)Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return(short)Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return(short)new BigDecimal(value).intValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return(short)new BigDecimal(value).intValue();
        }
      default:
        return Short.parseShort(getString(recordData));
    }
  }

  /**
   * Converts the specified record data at this field's offset into an int value, if possible.
  **/
  public int getInt(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return(int)Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return(int)Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return(int)Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).intValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).intValue();
        }
      default:
        return Integer.parseInt(getString(recordData));
    }
  }

  /**
   * Converts the specified record data at this field's offset into a byte value, if possible.
  **/
  public byte getByte(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return(byte)Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return(byte)Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return(byte)Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return(byte)Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return(byte)Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return(byte)new BigDecimal(value).intValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return(byte)new BigDecimal(value).intValue();
        }
      default:
        return Byte.parseByte(getString(recordData));
    }
  }

  /**
   * Returns an array of bytes that is only this field's data, which is a subset of the specified record data.
  **/
  public byte[] getBytes(final byte[] recordData) throws IOException
  {
    final boolean varlen = isVariableLength();
    final int length = varlen ? length_-2 : length_;
    final int offset = varlen ? offset_+2 : offset_;
    byte[] b = new byte[length];
    System.arraycopy(recordData, offset, b, 0, length);
    return b;
  }

  /**
   * Converts the specified byte array value into record data at this field's offset, if possible.
  **/
  public void setBytes(final byte[] value, final byte[] recordData) throws IOException
  {
    final boolean varlen = isVariableLength();
    final int length = varlen ? length_-2 : length_;
    final int offset = varlen ? offset_+2 : offset_;

    int len = value.length;
    int limit = len < length ? len : length;
    System.arraycopy(value, 0, recordData, offset, limit);
    if (varlen)
    {
      Conv.shortToByteArray(limit, recordData, offset_);
    }
    else if (len < length)
    {
      int left = length-len;
      int count = offset+limit;
      for (int i=0; i<left; ++i)
      {
        recordData[count++] = 0;
      }
    }
  }

  private final ByteArrayKey key_ = new ByteArrayKey();

  private String lookup(final byte[] recordData)
  {
    key_.setHashData(recordData, offset_, length_);
    return(String)cache_.get(key_);
  }

  private void cache(final byte[] recordData, final String value)
  {
    final byte[] key = new byte[length_];
    System.arraycopy(recordData, offset_, key, 0, length_);
    cache_.put(new ByteArrayKey(key), value);
  }

  /**
   * Converts the specified record data at this field's offset into a float value, if possible.
  **/
  public float getFloat(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return(float)Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return(float)Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return(float)Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return(float)Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).floatValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).floatValue();
        }
      default:
        return Float.parseFloat(getString(recordData));
    }
  }

  /**
   * Converts the specified record data at this field's offset into a double value, if possible.
  **/
  public double getDouble(final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          return(double)Conv.byteArrayToShort(recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          return(double)Conv.byteArrayToInt(recordData, offset_);
        }
        else
        {
          return(double)Conv.byteArrayToLong(recordData, offset_);
        }
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          return(double)Conv.byteArrayToFloat(recordData, offset_);
        }
        else
        {
          return Conv.byteArrayToDouble(recordData, offset_);
        }
      case TYPE_DECIMAL_FLOAT: // Decimal float
        if (length_ == 8)
        {
          // DECFLOAT 16
          String value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).doubleValue();
        }
        else
        {
          // length must be 16
          // DECFLOAT 34
          String value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          return new BigDecimal(value).doubleValue();
        }
      default:
        return Double.parseDouble(getString(recordData));
    }
  }

  /**
   * Converts the specified record data at this field's offset directly into a String value, if possible.
  **/
  public String getString(final byte[] recordData) throws IOException
  {
    final boolean useCache = cache_ != null;
    String value = useCache ? lookup(recordData) : null;
    if (value == null)
    {
      switch (type_)
      {
        case '1': // BLOB or CLOB data - this should never occur, since DDM doesn't allow us to open a file containing LOB data.
        case '3': // DBCLOB data
          throw new IOException("LOB data not allowed");
        case '4': // DATALINK data - this should never occur.
          throw new IOException("DATALINK data not allowed");
        case TYPE_CHARACTER: // Character field
        case TYPE_DBCS_EITHER: // DBCS-Either field
        case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
        case TYPE_DBCS_ONLY: // DBCS-Only field
        case TYPE_DBCS_OPEN: // DBCS-Open field
          final boolean varlen = isVariableLength();
          final int length = varlen ? (Conv.byteArrayToShort(recordData, offset_) * (type_ == TYPE_DBCS_GRAPHIC ? 2 : 1)) : length_;
          final int offset = varlen ? offset_+2 : offset_;
          value = Conv.ebcdicByteArrayToString(recordData, offset, length, buffer_, ccsid_);
          break;
//        int defaultLen = WHDFTL;
//        String defaultVal = WHDFT; // Could be *NULL or have apostrophes ' in it
        case TYPE_BINARY: // Binary field
          //TODO Binary fields can have decimal positions, actually -- I think the decimal must get inserted after the number is converted to base-10.
          if (numDigits_ < 5)
          {
            value = String.valueOf(Conv.byteArrayToShort(recordData, offset_));
          }
          else if (numDigits_ < 10)
          {
            value = String.valueOf(Conv.byteArrayToInt(recordData, offset_));
          }
          else
          {
            value = String.valueOf(Conv.byteArrayToLong(recordData, offset_));
          }
          break;
        case TYPE_FLOAT: // Float field
          if (length_ == 4)
          {
            value = String.valueOf(Conv.byteArrayToFloat(recordData, offset_));
          }
          else
          {
            value = String.valueOf(Conv.byteArrayToDouble(recordData, offset_));
          }
          break;
        case TYPE_DECIMAL_FLOAT: // Decimal float
          if (length_ == 8)
          {
            // DECFLOAT 16
            value = Conv.decfloat16ByteArrayToString(recordData, offset_);
          }
          else
          {
            // length must be 16
            // DECFLOAT 34
            value = Conv.decfloat34ByteArrayToString(recordData, offset_);
          }
          break;
        case TYPE_HEXADECIMAL: // Hex field
        case TYPE_BINARY_CHARACTER: // Binary character
          final boolean varLen = isVariableLength();
          final int fLength = varLen ? Conv.byteArrayToShort(recordData, offset_) : length_;
          final int fOffset = varLen ? offset_+2 : offset_;
          value = Conv.bytesToHexString(recordData, fOffset, fLength, buffer_);
          break;
        case TYPE_DATE: // Date field
          value = Conv.ebcdicByteArrayToString(recordData, offset_, length_, buffer_); //TODO - format?
          break;
        case TYPE_PACKED_DECIMAL: // Packed decimal field
          value = Conv.packedDecimalToString(recordData, offset_, numDigits_, decimalPositions_, buffer_);
          break;
        case TYPE_ZONED_DECIMAL: // Zoned decimal field
          value = Conv.zonedDecimalToString(recordData, offset_, numDigits_, decimalPositions_, buffer_);
          break;
        case TYPE_TIME: // Time field
          value = Conv.ebcdicByteArrayToString(recordData, offset_, length_, buffer_); //TODO - format?
          break;
        case TYPE_TIMESTAMP: // Timestamp field
          value = Conv.ebcdicByteArrayToString(recordData, offset_, length_, buffer_); //TODO - format?
          break;
        default:
          throw new IOException("Unhandled field type: '"+type_+"'");

      }
      if (useCache)
      {
        cache(recordData, value);
      }
    }
    return value;
  }

  /**
   * Converts the specified record data at this field's offset into a date, time, or timestamp, if possible,
   * and sets the appropriate fields in the provided Calendar object.
   * @return true if the field data was converted and set into the Calendar object; false otherwise.
  **/
  public boolean getDate(final byte[] recordData, final Calendar cal) throws IOException
  {
    switch (type_)
    {
      case TYPE_DATE:
        if (dateTimeFormat_ != null && dateTimeFormat_.equals("*ISO"))
        {
          String value = getString(recordData);
          int year = Integer.parseInt(value.substring(0,4));
          int month = Integer.parseInt(value.substring(5,7));
          int day = Integer.parseInt(value.substring(8,10));
          cal.set(Calendar.YEAR, year);
          cal.set(Calendar.MONTH, month-1);
          cal.set(Calendar.DAY_OF_MONTH, day);
          return true;
        }
        break;
      case TYPE_TIME:
        if (dateTimeFormat_ != null && dateTimeFormat_.equals("*ISO"))
        {
          String value = getString(recordData);
          int hour = Integer.parseInt(value.substring(0,2));
          int minute = Integer.parseInt(value.substring(3,5));
          int second = Integer.parseInt(value.substring(6,8));
          cal.set(Calendar.HOUR_OF_DAY, hour);
          cal.set(Calendar.MINUTE, minute);
          cal.set(Calendar.SECOND, second);
          return true;
        }
        break;
      case TYPE_TIMESTAMP:
        String value = getString(recordData);
        int year = Integer.parseInt(value.substring(0,4));
        int month = Integer.parseInt(value.substring(5,7));
        int day = Integer.parseInt(value.substring(8,10));
        int hour = Integer.parseInt(value.substring(11,13));
        int minute = Integer.parseInt(value.substring(14,16));
        int second = Integer.parseInt(value.substring(17,19));
        int milli = Integer.parseInt(value.substring(20,23));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, milli);
        return true;
      default:
        break;
    }
    return false;
  }

  /**
   * Converts the specified Calendar value into record data for this field's type at this field's offset, if possible.
  **/
  public void setDate(final Calendar cal, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_DATE:
        if (dateTimeFormat_ != null && dateTimeFormat_.equals("*ISO"))
        {
          int year = cal.get(Calendar.YEAR);
          int month = cal.get(Calendar.MONTH)+1;
          int day = cal.get(Calendar.DAY_OF_MONTH);
          String value = year+"-"+get2(month)+"-"+get2(day);
          Conv.stringToEBCDICByteArray37(value, recordData, offset_);
        }
        else
        {
          throw new IOException("Unhandled date/time format: '"+dateTimeFormat_+"'");
        }
        break;
      case TYPE_TIME:
        if (dateTimeFormat_ != null && dateTimeFormat_.equals("*ISO"))
        {
          int hour = cal.get(Calendar.HOUR_OF_DAY);
          int minute = cal.get(Calendar.MINUTE);
          int second = cal.get(Calendar.SECOND);
          String value = get2(hour)+"."+get2(minute)+"."+get2(second);
          Conv.stringToEBCDICByteArray37(value, recordData, offset_);
        }
        else
        {
          throw new IOException("Unhandled date/time format: '"+dateTimeFormat_+"'");
        }
        break;
      case TYPE_TIMESTAMP:
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int milli = cal.get(Calendar.MILLISECOND);
        String value = year+"-"+get2(month)+"-"+get2(day)+"."+get2(hour)+"."+get2(minute)+"."+get2(second)+".";
        if (milli < 100) value += "0";
        if (milli < 10) value += "0";
        value += milli;
        value += "000"; // Microseconds.
        Conv.stringToEBCDICByteArray37(value, recordData, offset_);
        break;
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  private final char[] buf2 = new char[2];

  private String get2(int val)
  {
    buf2[0] = (char)((val > 10) ? ((val / 10) + '0') : '0');
    buf2[1] = (char)((val % 10) + '0');
    return new String(buf2);
  }


  /**
   * Converts the specified String value into record data for this field's type at this field's offset, if possible.
  **/
  public void setString(String value, final byte[] recordData) throws IOException
  {
    final boolean varlen = isVariableLength();
    final int offset = varlen ? offset_+2 : offset_;
    switch (type_)
    {
      case '1': // BLOB or CLOB data - this should never occur, since DDM doesn't allow us to open a file containing LOB data.
      case '3': // DBCLOB data
        throw new IOException("LOB data not allowed");
      case '4': // DATALINK data - this should never occur.
        throw new IOException("DATALINK data not allowed");
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        if (varlen)
        {
          int num = Conv.stringToEBCDICByteArray(value, recordData, offset, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(value, recordData, offset, len, ccsid_);
        }
        break;
//        int defaultLen = WHDFTL;
//        String defaultVal = WHDFT; // Could be *NULL or have apostrophes ' in it
      case TYPE_BINARY: // Binary field
        //TODO Binary fields can have decimal positions, actually -- I think the decimal must get inserted after the number is converted to base-10.
        if (numDigits_ < 5)
        {
          short val = Short.parseShort(value);
          Conv.shortToByteArray(val, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          int val = Integer.parseInt(value);
          Conv.intToByteArray(val, recordData, offset_);
        }
        else
        {
          long val = Long.parseLong(value);
          Conv.longToByteArray(val, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          float val = Float.parseFloat(value);
          Conv.floatToByteArray(val, recordData, offset_);
        }
        else
        {
          double val = Double.parseDouble(value);
          Conv.doubleToByteArray(val, recordData, offset_);
        }
        break;
//TODO        case TYPE_DECIMAL_FLOAT: // Decimal float
      case TYPE_HEXADECIMAL: // Hex field
      case TYPE_BINARY_CHARACTER: // Binary character
        int numBytes = Conv.hexStringToBytes(value, recordData, offset);
        if (varlen)
        {
          Conv.shortToByteArray(numBytes, recordData, offset_);
        }
        break;
      case TYPE_DATE: // Date field
      case TYPE_TIME: // Time field
      case TYPE_TIMESTAMP: // Timestamp field
        Conv.stringToEBCDICByteArray37(value, recordData, offset_);
        break;
      case TYPE_PACKED_DECIMAL: // Packed decimal field
        Conv.stringToPackedDecimal(value, numDigits_, recordData, offset_);
        break;
      case TYPE_ZONED_DECIMAL: // Zoned decimal field
        Conv.stringToZonedDecimal(value, numDigits_, recordData, offset_);
        break;
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified double value into record data for this field's type at this field's offset, if possible.
  **/
  public void setDouble(final double value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray((short)value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray((int)value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray((long)value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray((float)value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray(value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified float value into record data for this field's type at this field's offset, if possible.
  **/
  public void setFloat(final float value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray((short)value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray((int)value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray((long)value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray(value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray((double)value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified byte value into record data for this field's type at this field's offset, if possible.
  **/
  public void setByte(final byte value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray((short)value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray((int)value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray((long)value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray((float)value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray((double)value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified short value into record data for this field's type at this field's offset, if possible.
  **/
  public void setShort(final short value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray(value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray((int)value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray((long)value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray((float)value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray((double)value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified int value into record data for this field's type at this field's offset, if possible.
  **/
  public void setInt(final int value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray((short)value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray(value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray((long)value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray((float)value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray((double)value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }

  /**
   * Converts the specified long value into record data for this field's type at this field's offset, if possible.
  **/
  public void setLong(final long value, final byte[] recordData) throws IOException
  {
    switch (type_)
    {
      case TYPE_CHARACTER: // Character field
      case TYPE_DBCS_EITHER: // DBCS-Either field
      case TYPE_DBCS_GRAPHIC: // DBCS-Graphic field
      case TYPE_DBCS_ONLY: // DBCS-Only field
      case TYPE_DBCS_OPEN: // DBCS-Open field
        final String s = String.valueOf(value);
        if (isVariableLength())
        {
          int num = Conv.stringToEBCDICByteArray(s, recordData, offset_+2, ccsid_);
          if (type_ == TYPE_DBCS_GRAPHIC) num = num >> 1;
          Conv.shortToByteArray(num, recordData, offset_);
        }
        else
        {
          final int len = type_ == TYPE_DBCS_GRAPHIC ? length_*2 : length_;
          Conv.stringToBlankPadEBCDICByteArray(s, recordData, offset_, len, ccsid_);
        }
        break;
      case TYPE_BINARY:
        if (numDigits_ < 5)
        {
          Conv.shortToByteArray((short)value, recordData, offset_);
        }
        else if (numDigits_ < 10)
        {
          Conv.intToByteArray((int)value, recordData, offset_);
        }
        else
        {
          Conv.longToByteArray(value, recordData, offset_);
        }
        break;
      case TYPE_FLOAT: // Float field
        if (length_ == 4)
        {
          Conv.floatToByteArray((float)value, recordData, offset_);
        }
        else
        {
          Conv.doubleToByteArray((double)value, recordData, offset_);
        }
        break;
//TODO      case TYPE_DECIMAL_FLOAT: // Decimal float
      default:
        throw new IOException("Unhandled field type: '"+type_+"'");
    }
  }
}
