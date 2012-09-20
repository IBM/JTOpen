///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Column.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.database.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import sun.util.BuddhistCalendar;

final class Column
{
  private static final char[] NUMS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private String name_;
  private String udtName_;
  private int type_;
  private int length_;    /* Length of data type in the buffer sent to / from host server */
  private int declaredLength_ = 0;  /* This is the declared length of the type, calculated when needed  */
  private int scale_;
  private int precision_;
  private int ccsid_;
  private boolean isForBitData_;
  private int lobMaxSize_;

  private int offset_;

  private int index_;  /* parameter / column number 1 based */
  private boolean parameter_;  /* is this a parameter */

  private char[] buffer_;

  private int dateFormat_;
  private int timeFormat_;
  private int dateSeparator_;
  private int timeSeparator_;

  private String table_;
  private String label_;
  private String schema_;
  private boolean autoIncrement_;
  private boolean definitelyWritable_;
  private boolean readOnly_ = true;
  private boolean searchable_ = true;
  private boolean writable_;

  private boolean useDateCache_ = false;
  private HashMap dateCache_;
  private Date dateZero_;

  private boolean useTimeCache_ = false;
  private HashMap timeCache_;
  private Time timeZero_;

  private boolean useStringCache_ = false;
  private boolean cacheLastOnly_ = false;
  private HashMap cache_ = null;

  private boolean null_ = false;
  private String stringValue_;
  private int intValue_;
  private long longValue_;
  private short shortValue_;
  private float floatValue_;
  private double doubleValue_;
  private byte byteValue_;
  private boolean booleanValue_;
  private java.util.Date dateValue_; // Used for date, time, and timestamp.
  private byte[] byteArrayValue_;
  private BigDecimal bigDecimalValue_;
  private Object objectValue_;
  private URL urlValue_;
  private InputStream inputStreamValue_; // Used for ascii, binary, and unicode streams.
  private int inputStreamLength_; // Used for ascii, binary, character, and unicode streams.
  private Reader readerValue_;
  private int valueType_ = 0;
  private static final int TYPE_STRING = 1;
  private static final int TYPE_INT = 2;
  private static final int TYPE_LONG = 3;
  private static final int TYPE_SHORT = 4;
  private static final int TYPE_FLOAT = 5;
  private static final int TYPE_DOUBLE = 6;
  private static final int TYPE_BYTE = 7;
  private static final int TYPE_BOOLEAN = 8;
  private static final int TYPE_DATE = 9;
  private static final int TYPE_TIME = 10;
  private static final int TYPE_TIMESTAMP = 11;
  private static final int TYPE_BYTE_ARRAY = 12;
  private static final int TYPE_BIG_DECIMAL = 13;
  private static final int TYPE_OBJECT = 14;
  private static final int TYPE_URL = 15;
  private static final int TYPE_ASCII_STREAM = 16;
  private static final int TYPE_BINARY_STREAM = 17;
  private static final int TYPE_UNICODE_STREAM = 18;
  private static final int TYPE_CHARACTER_STREAM = 19;





  private Calendar calendar_;

  Column(Calendar cal, int index, boolean parameter)
  {
    calendar_ = cal;

    index_ = index;
    parameter_ = parameter;
  }

  void clearValue()
  {
    null_ = false;
    stringValue_ = null;
    intValue_ = 0;
    longValue_ = 0;
    shortValue_ = 0;
    floatValue_ = 0;
    doubleValue_ = 0;
    byteValue_ = 0;
    booleanValue_ = false;
    dateValue_ = null;
    byteArrayValue_ = null;
    bigDecimalValue_ = null;
    objectValue_ = null;
    urlValue_ = null;
    inputStreamValue_ = null;
    inputStreamLength_ = 0;
    readerValue_ = null;
    valueType_ = 0;
  }

  void setValue(String s)
  {
    stringValue_ = s;
    null_ = s == null;
    valueType_ = TYPE_STRING;

  }

  void setValue(int i)
  {
    intValue_ = i;
    null_ = false;
    valueType_ = TYPE_INT;
  }

  void setValue(long l)
  {
    longValue_ = l;
    null_ = false;
    valueType_ = TYPE_LONG;
  }

  void setValue(short s)
  {
    shortValue_ = s;
    null_ = false;
    valueType_ = TYPE_SHORT;
  }

  void setValue(float f)
  {
    floatValue_ = f;
    null_ = false;
    valueType_ = TYPE_FLOAT;
  }

  void setValue(double d)
  {
    doubleValue_ = d;
    null_ = false;
    valueType_ = TYPE_DOUBLE;
  }

  void setValue(byte b)
  {
    byteValue_ = b;
    null_ = false;
    valueType_ = TYPE_BYTE;
  }

  void setValue(boolean b)
  {
    booleanValue_ = b;
    null_ = false;
    valueType_ = TYPE_BOOLEAN;
  }

  void setValue(Date d)
  {
    dateValue_ = d;
    null_ = d == null;
    valueType_ = TYPE_DATE;
  }

  void setValue(Date d, Calendar cal)
  {
    dateValue_ = d;
    null_ = d == null;
    valueType_ = TYPE_DATE;
    calendar_ = cal;
  }

  void setValue(Time t)
  {
    dateValue_ = t;
    null_ = t == null;
    valueType_ = TYPE_TIME;
  }

  void setValue(Time t, Calendar cal)
  {
    dateValue_ = t;
    null_ = t == null;
    valueType_ = TYPE_TIME;
    calendar_=cal;
  }

  void setValue(Timestamp t)
  {
    dateValue_ = t;
    null_ = t == null;
    valueType_ = TYPE_TIMESTAMP;
  }

  void setValue(Timestamp t, Calendar cal)
  {
    dateValue_ = t;
    null_ = t == null;
    valueType_ = TYPE_TIMESTAMP;
    calendar_ = cal;
  }

  void setValue(byte[] b)
  {
    byteArrayValue_ = b;
    null_ = b == null;
    valueType_ = TYPE_BYTE_ARRAY;
  }

  void setValue(BigDecimal bd)
  {
    bigDecimalValue_ = bd;
    null_ = bd == null;
    valueType_ = TYPE_BIG_DECIMAL;
  }

  void setValue(Object obj)
  {
	// Use the right setValue for supported types
	if (obj instanceof String) setValue((String)obj);
	else if (obj instanceof java.sql.Date)   setValue((java.sql.Date)obj);
	else if (obj instanceof java.sql.Time)   setValue((java.sql.Time)obj);
	else if (obj instanceof java.sql.Timestamp)   setValue((java.sql.Timestamp)obj);
	else if (obj instanceof byte[])   setValue((byte[])obj);
	else if (obj instanceof BigDecimal)   setValue((BigDecimal)obj);
	else if (obj instanceof URL)   setValue((URL)obj);
	else {
		objectValue_ = obj;
		null_ = obj == null;
		valueType_ = TYPE_OBJECT;
	}
  }

  void setValue(URL url)
  {
    urlValue_ = url;
    null_ = url == null;
    valueType_ = TYPE_URL;
  }

  void setAsciiStreamValue(InputStream in, int length)
  {
    inputStreamValue_ = in;
    inputStreamLength_ = length;
    null_ = in == null;
    valueType_ = TYPE_ASCII_STREAM;
  }

  void setBinaryStreamValue(InputStream in, int length)
  {
    inputStreamValue_ = in;
    inputStreamLength_ = length;
    null_ = in == null;
    valueType_ = TYPE_BINARY_STREAM;
  }

  void setUnicodeStreamValue(InputStream in, int length)
  {
    inputStreamValue_ = in;
    inputStreamLength_ = length;
    null_ = in == null;
    valueType_ = TYPE_UNICODE_STREAM;
  }

  void setCharacterStreamValue(Reader r, int length)
  {
    readerValue_ = r;
    inputStreamLength_ = length;
    null_ = r == null;
    valueType_ = TYPE_CHARACTER_STREAM;
  }

  private String getNonexponentValueString() throws SQLException {
	  String s;
switch (valueType_)
	      {
	        case TYPE_FLOAT:
	            s = String.valueOf(floatValue_);
	            if (s.indexOf("E")>0) {
	            	s = new BigDecimal(floatValue_).toPlainString();
	            }
	            break;
	          case TYPE_DOUBLE:
	            s = String.valueOf(doubleValue_);
	            if (s.indexOf("E")>0) {
	            	s = new BigDecimal(doubleValue_).toPlainString();
	            }
	            break;
	          case TYPE_BIG_DECIMAL:
	        	  s = bigDecimalValue_.toPlainString();
	        	  break;
	          case TYPE_BOOLEAN:
	              s = booleanValue_ ? "1" : "0";
	              break;
	          default:
	            	return getValueString();
	      }
	    return s;
  }
  private String getValueString() throws SQLException
  {
    String s;

    try
    {
      switch (valueType_)
      {
        case TYPE_STRING :
        	s = stringValue_;
        	break;
        case TYPE_INT:
          s = String.valueOf(intValue_);
          break;
        case TYPE_SHORT:
          s = String.valueOf(shortValue_);
          break;
        case TYPE_LONG:
          s = String.valueOf(longValue_);
          break;
        case TYPE_FLOAT:
          s = String.valueOf(floatValue_);
          break;
        case TYPE_DOUBLE:
          s = String.valueOf(doubleValue_);
          break;
        case TYPE_BYTE:
          s = String.valueOf(byteValue_);
          break;
        case TYPE_BOOLEAN:
          s = String.valueOf(booleanValue_);
          break;
        case TYPE_DATE:
        case TYPE_TIME:
        case TYPE_TIMESTAMP:
          s = dateValue_.toString();
          break;
        case TYPE_BYTE_ARRAY:
          //s = Conv.unicodeByteArrayToString(byteArrayValue_, 0, byteArrayValue_.length);
          s = Conv.bytesToHexString(byteArrayValue_, 0, byteArrayValue_.length);
          break;
        case TYPE_BIG_DECIMAL:
          s = bigDecimalValue_.toString();
          break;
        case TYPE_OBJECT:
          s = objectValue_.toString();
          break;
        case TYPE_URL:
          s = urlValue_.toString();
          break;
        case TYPE_ASCII_STREAM:
        case TYPE_BINARY_STREAM:
          StringBuffer buf = new StringBuffer();
          int count = 0;
          int i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
          break;
        case TYPE_UNICODE_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              byte hi = (byte)i;
              i = inputStreamValue_.read();
              if (i >= 0)
              {
                byte lo = (byte)i;
                // Make sure that the low byte is masked
                char c = (char)((hi << 8) | ( lo & 0xff));
                buf.append(c);
              }
            }
            ++count;
          }
          s = buf.toString();
          break;
        case TYPE_CHARACTER_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = readerValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
          break;

          default:
        	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


      }
    }
    catch (IOException io)
    {
      SQLException sql = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Error reading from parameter stream: "+io.toString());
      sql.initCause(io);
      throw sql;
    }
    return s;
  }

  private String getValueTimeAsString() throws SQLException
  {

    String s = "UNSET";
    try
    {
      switch (valueType_)
      {
	        case 0:
	      if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);

	        case TYPE_INT:
	        case TYPE_SHORT:
	        case TYPE_LONG:
	        case TYPE_FLOAT:
	        case TYPE_DOUBLE:
	        case TYPE_BYTE:
	        case TYPE_BOOLEAN:
	        case TYPE_BYTE_ARRAY:
	        case TYPE_BIG_DECIMAL:
	        case TYPE_URL:
	        case TYPE_DATE:
	        case TYPE_TIMESTAMP:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);

        case TYPE_TIME:
        {
          Time t = new Time(dateValue_.getTime());
          s = t.toString();
          break;
        }
        case TYPE_OBJECT:
          s = objectValue_.toString();
      {
    	  Time t = Time.valueOf(s);
    	  s = t.toString();
      }

          break;
        case TYPE_ASCII_STREAM:
        case TYPE_BINARY_STREAM:
          StringBuffer buf = new StringBuffer();
          int count = 0;
          int i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
    {
        Time t = Time.valueOf(s);
        s = t.toString();
    }

          break;
        case TYPE_UNICODE_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              byte hi = (byte)i;
              i = inputStreamValue_.read();
              if (i >= 0)
              {
                byte lo = (byte)i;
                // Make sure that the low byte is masked
                char c = (char)((hi << 8) | ( lo & 0xff));
                buf.append(c);
              }
            }
            ++count;
          }
          s = buf.toString();
    {
        Time t = Time.valueOf(s);
        s = t.toString();
    }

          break;
        case TYPE_CHARACTER_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = readerValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
    {
        Time ts = Time.valueOf(s);
        s = ts.toString();
    }

          break;
        case  TYPE_STRING :
        {
        	s = stringValue_;
            Time t = Time.valueOf(s);
            s = t.toString();
        }

      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);




      }
      //
      // At this point, the time should have come from
      // Time.toString() so it will be in the right format
      //
    }
    catch (IOException io)
    {
      SQLException sql = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Error reading from parameter stream: "+io.toString());
      sql.initCause(io);
      throw sql;
    } catch (IllegalArgumentException e) {
    SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, s);
    sqlex.initCause(e);
    throw sqlex;
      }

    return s;
  }


  private String getValueTimestampAsString() throws SQLException
  {

    String s ="NOT SET";
    try
    {
      switch (valueType_)
      {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);

        case TYPE_INT:
        case TYPE_SHORT:
        case TYPE_LONG:
        case TYPE_FLOAT:
        case TYPE_DOUBLE:
        case TYPE_BYTE:
        case TYPE_BOOLEAN:
        case TYPE_TIME:
        case TYPE_BYTE_ARRAY:
        case TYPE_BIG_DECIMAL:
        case TYPE_URL:
		    throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of type "+valueType_+" to timestamp not supported.");
          
        case TYPE_DATE:
	  s = dateValue_.toString()+" 00:00:00";
	    {
		Timestamp ts = Timestamp.valueOf(s);
		s = ts.toString();
	    }
	  break;
        case TYPE_TIMESTAMP:
          s = dateValue_.toString();
          break;
        case TYPE_OBJECT:
          s = objectValue_.toString();
	    {
		Timestamp ts = Timestamp.valueOf(s);
		s = ts.toString();
	    }

          break;
        case TYPE_ASCII_STREAM:
        case TYPE_BINARY_STREAM:
          StringBuffer buf = new StringBuffer();
          int count = 0;
          int i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
	  {
	      Timestamp ts = Timestamp.valueOf(s);
	      s = ts.toString();
	  }

          break;
        case TYPE_UNICODE_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = inputStreamValue_.read();
            if (i >= 0)
            {
              byte hi = (byte)i;
              i = inputStreamValue_.read();
              if (i >= 0)
              {
                byte lo = (byte)i;
                // Make sure that the low byte is masked
                char c = (char)((hi << 8) | ( lo & 0xff));
                buf.append(c);
              }
            }
            ++count;
          }
          s = buf.toString();
	  {
	      Timestamp ts = Timestamp.valueOf(s);
	      s = ts.toString();
	  }

          break;
        case TYPE_CHARACTER_STREAM:
          buf = new StringBuffer();
          count = 0;
          i = 0;
          while (i >= 0 && count < inputStreamLength_)
          {
            i = readerValue_.read();
            if (i >= 0)
            {
              buf.append((char)i);
            }
            ++count;
          }
          s = buf.toString();
	  {
	      Timestamp ts = Timestamp.valueOf(s);
	      s = ts.toString();
	  }

          break;
        case  TYPE_STRING :
  	  {
          s = isoTimestamp(stringValue_);
  	      Timestamp ts = Timestamp.valueOf(s);
  	      s = ts.toString();
  	  }

      default:
    	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);
      }
      while (s.length() < 26) {
	  s = s + "0";
      }
      // Convert to IBM I format.
      s = s.substring(0,10)+"-"+s.substring(11,13)+"."+s.substring(14,16)+"."+s.substring(17);
    }
    catch (IOException io)
    {
      SQLException sql = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Error reading from parameter stream: "+io.toString());
      sql.initCause(io);
      throw sql;
    } catch (IllegalArgumentException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, s);
	  sqlex.initCause(e);
	  throw sqlex;
      }

    return s;
  }

  // Make sure the string is in ISO format.
  private String isoTimestamp(String s) throws SQLException {
    //               1         2
    //     01234567890123456789012345
    // IBM 1999-10-10-12.12.12.123456
    // ISO 1999-10-10 12:12:12.123456
    if (s.length() < 18) {
      JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
    }
    if (s.charAt(10) == '-' ||
        s.charAt(13) == '.' ||
        s.charAt(16) == '.' ) {
      char[] chars = s.toCharArray();
      int l = chars.length;
      if (l >= 11 ) chars[10] = ' ';
      if (l >= 14) chars[13] = ':';
      if (l >= 17) chars[16] = ':';
      return new String(chars);
    }
    return s;
  }

  private float getValueFloat() throws SQLException
  {
      String stringValue = null;
      try {
    float f = floatValue_;
    switch (valueType_)
    {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);

      case TYPE_INT:
        f = intValue_;
        break;
      case TYPE_SHORT:
        f = shortValue_;
        break;
      case TYPE_LONG:
          f = longValue_;
          break;
      case TYPE_STRING:
	  stringValue = stringValue_;
        f = Float.parseFloat(stringValue);
        break;
      case TYPE_FLOAT:
        f = floatValue_;
        break;
      case TYPE_DOUBLE:
        f = (float)doubleValue_;
        break;
      case TYPE_BYTE:
        f = (float)byteValue_;
        break;
      case TYPE_BOOLEAN:
        f = booleanValue_ ? (float) 1.0 :  (float) 0.0;
        break;
      case TYPE_DATE:
      case TYPE_TIME:
      case TYPE_TIMESTAMP:
      	throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of date/time/timestamp to float not supported.");
      case TYPE_BYTE_ARRAY:
    	throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of byte array to float not supported.");
      case TYPE_BIG_DECIMAL:
        f = bigDecimalValue_.floatValue();
        break;
      case TYPE_OBJECT:
	  stringValue = objectValue_.toString();
        f = Float.parseFloat(stringValue);
        break;
      case TYPE_URL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to float not supported.");
      case TYPE_ASCII_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to float not supported.");
      case TYPE_BINARY_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to float not supported.");
      case TYPE_UNICODE_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to float not supported.");
      case TYPE_CHARACTER_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to float not supported.");
      default:
    	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


    }
    return f;
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }


  private double getValueDouble() throws SQLException
  {
      String stringValue = null;
      try {
    double d = doubleValue_;
    switch (valueType_)
    {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);

      case TYPE_INT:
        d = intValue_;
        break;
      case TYPE_SHORT:
        d = shortValue_;
        break;
      case TYPE_LONG:
          d = longValue_;
          break;

      case TYPE_STRING:
	  stringValue = stringValue_;
        d = Double.parseDouble(stringValue_);
        break;
      case TYPE_FLOAT:
        d = floatValue_;
        break;
      case TYPE_DOUBLE:
        d = doubleValue_;
        break;
      case TYPE_BYTE:
        d = (double)byteValue_;
        break;
      case TYPE_BOOLEAN:
        d = booleanValue_ ? (double) 1.0 :  (double) 0.0;
        break;
      case TYPE_DATE:
      case TYPE_TIME:
      case TYPE_TIMESTAMP:
        	throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of date/time/timestamp to double not supported.");
      case TYPE_BYTE_ARRAY:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of byte array to double not supported.");
      case TYPE_BIG_DECIMAL:
        d = bigDecimalValue_.doubleValue();
        break;
      case TYPE_OBJECT:
	  stringValue = objectValue_.toString();
        d = Double.parseDouble(stringValue);
        break;
      case TYPE_URL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to double not supported.");
      case TYPE_ASCII_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to double not supported.");
      case TYPE_BINARY_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to double not supported.");
      case TYPE_UNICODE_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to double not supported.");
      case TYPE_CHARACTER_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to double not supported.");
      default:
    	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


    }
    return d;
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue );
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }



  private long getValueLong() throws SQLException
  {

      String stringValue = null;
      try {
    long l ;
    switch (valueType_)
    {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);
        case  TYPE_LONG :
        	l = longValue_;
        	break;
      case TYPE_INT:
        l = intValue_;
        break;
      case TYPE_SHORT:
        l = shortValue_;
        break;
      case TYPE_OBJECT:
          // fall through
      case TYPE_STRING:
      {
    	if (valueType_ == TYPE_OBJECT) {
            stringValue = objectValue_.toString();
    	} else {
            stringValue = stringValue_;
    	}
        double doubleValue = Double.parseDouble(stringValue);
        if (doubleValue > Long.MAX_VALUE || doubleValue < Long.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        // If the doublevalue is in the range that can be accurately convert to
        // a long, just use the double conversion.  A double value has an implied 1.x
        // followed by 52 bits, so the precision is 53 bits.
        // Otherwise, we must rely on
        // parseLong, which will not handle strings of the form 134.53 or 12.3E20
        // 2^53=9,007,199,254,740,992
        if ((doubleValue < 9007199254740992L ) && (doubleValue > -9007199254740992L)) {
        	l = (long) doubleValue;
        } else {
        	try {
        		l = Long.parseLong(stringValue);
        	} catch (NumberFormatException nfe) {
        		int dotIndex = stringValue.indexOf(".");
        		if (dotIndex > 0) {
        			stringValue = stringValue.substring(0,dotIndex);
            		l = Long.parseLong(stringValue);
        		} else {
        			throw nfe;
        		}
        	}
        }

      }
        break;
      case TYPE_FLOAT:
        if (floatValue_ > Long.MAX_VALUE || floatValue_ < Long.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }

        l = (long)floatValue_;
        break;
      case TYPE_DOUBLE:
        if (doubleValue_ > Long.MAX_VALUE || doubleValue_ < Long.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        l = (long)doubleValue_;
        break;
      case TYPE_BYTE:
        l = (long)byteValue_;
        break;
      case TYPE_BOOLEAN:
        l = booleanValue_ ? 1L : 0L;
        break;
      case TYPE_DATE:
      case TYPE_TIME:
      case TYPE_TIMESTAMP:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      case TYPE_BYTE_ARRAY:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of byte array to long not supported.");
      case TYPE_BIG_DECIMAL:
      {
        double doubleValue = bigDecimalValue_.doubleValue();
        if (doubleValue > Long.MAX_VALUE || doubleValue < Long.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }

        l = bigDecimalValue_.longValue();
      }
        break;
      case TYPE_URL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to long not supported.");
      case TYPE_ASCII_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to long not supported.");
      case TYPE_BINARY_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to long not supported.");
      case TYPE_UNICODE_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to long not supported.");
      case TYPE_CHARACTER_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to long not supported.");
        default:
      	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


    }
    return l;
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  private int getValueInt() throws SQLException
  {
      String stringValue = null;
      try {
	  int i ;
	  switch (valueType_) {
      case 0:
        if (parameter_)
          throw JDBCError
              .getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);
      case  TYPE_INT :
    	  i = intValue_;
    	  break;
      case TYPE_LONG:
        if (longValue_ > Integer.MAX_VALUE || longValue_ < Integer.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        i = (int) longValue_;
		  break;
	      case TYPE_SHORT:
		  i = shortValue_;
		  break;
	      case TYPE_STRING: {
        double doubleValue = Double.parseDouble(stringValue_);
        if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }

        i = (int) doubleValue;
      }
        break;
	      case TYPE_FLOAT:
	        if (floatValue_ > Integer.MAX_VALUE || floatValue_ < Integer.MIN_VALUE) {
	          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	        }
	        i = (int)floatValue_;
		  break;
	      case TYPE_DOUBLE:
	        if (doubleValue_ > Integer.MAX_VALUE || doubleValue_ < Integer.MIN_VALUE) {
	          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	        }
		  i = (int)doubleValue_;
		  break;
	      case TYPE_BYTE:
		  i = (int)byteValue_;
		  break;
	      case TYPE_BOOLEAN:
		  i = booleanValue_ ? 1 : 0;
		  break;
	      case TYPE_DATE:
	      case TYPE_TIME:
	      case TYPE_TIMESTAMP:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	      case TYPE_BYTE_ARRAY:
	          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, "Conversion from byte array to integer not supported");
	      case TYPE_BIG_DECIMAL:
	      {
	        double doubleValue = bigDecimalValue_.doubleValue();
	        if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
	          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	        }

	        i = (int) doubleValue;
	      }

		  break;
	      case TYPE_OBJECT:
	      {
	        double doubleValue = Double.parseDouble(objectValue_.toString());
	        if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
	          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	        }

	        i = (int) doubleValue;
	      }
		  break;
	      case TYPE_URL:
		  throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to int not supported.");
	      case TYPE_ASCII_STREAM:
		  throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to int not supported.");
	      case TYPE_BINARY_STREAM:
		  throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to int not supported.");
	      case TYPE_UNICODE_STREAM:
		  throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to int not supported.");
	      case TYPE_CHARACTER_STREAM:
		  throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to int not supported.");


          default:
          	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


	  }
	  return i;
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }
  }

  private short getValueShort() throws SQLException
  {
      String stringValue = null;
      try {
    short sh ;
    switch (valueType_)
    {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);
        case  TYPE_SHORT :
        	sh = shortValue_;
        	break;
      case TYPE_LONG:
        if (longValue_ > Short.MAX_VALUE || longValue_ < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short)longValue_;
        break;
      case TYPE_INT:
        if (intValue_ > Short.MAX_VALUE || intValue_ < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short)intValue_;
        break;
      case TYPE_STRING: {
        double doubleValue = Double.valueOf(stringValue_);
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short) doubleValue;
      }
        break;
      case TYPE_FLOAT:
        if (floatValue_ > Short.MAX_VALUE || floatValue_ < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short)floatValue_;
        break;
      case TYPE_DOUBLE:
        if (doubleValue_ > Short.MAX_VALUE || doubleValue_ < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short)doubleValue_;
        break;
      case TYPE_BYTE:
        sh = (short)byteValue_;
        break;
      case TYPE_BOOLEAN:
        sh = (short)(booleanValue_ ? 1 : 0);
        break;
      case TYPE_DATE:
      case TYPE_TIME:
      case TYPE_TIMESTAMP:
          // This conversion not possible
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      case TYPE_BYTE_ARRAY:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of byte array to short not supported.");
      case TYPE_BIG_DECIMAL:
      {
        double doubleValue = bigDecimalValue_.doubleValue();
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short) doubleValue;
      }
        break;
      case TYPE_OBJECT:
      {
        double doubleValue = Double.valueOf(objectValue_.toString());
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        sh = (short) doubleValue;
      }
        break;
      case TYPE_URL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to short not supported.");
      case TYPE_ASCII_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to short not supported.");
      case TYPE_BINARY_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to short not supported.");
      case TYPE_UNICODE_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to short not supported.");
      case TYPE_CHARACTER_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to short not supported.");



      default:
      	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);

    }
    return sh;
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }


  private byte[]  getValueByteArray() throws SQLException
  {
      byte[] ba = byteArrayValue_;
    switch (valueType_)
    {
        case 0:
	    if (parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);
      case TYPE_BYTE_ARRAY:
        ba = byteArrayValue_;
        break;

      case TYPE_STRING:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of string to byte array not supported.");
      case TYPE_LONG:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of long to byte array not supported.");
      case TYPE_INT:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of int to byte array not supported.");
      case TYPE_SHORT:
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of short to byte array not supported.");
      case TYPE_FLOAT:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of float to byte array not supported.");
      case TYPE_DOUBLE:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of doubleto byte array not supported.");
      case TYPE_BYTE:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of byte to byte array not supported.");
      case TYPE_BOOLEAN:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of boolean to byte array not supported.");
      case TYPE_DATE:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of date to byte array not supported.");
      case TYPE_TIME:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of time to byte array not supported.");
      case TYPE_TIMESTAMP:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of timestamp to byte array not supported.");
      case TYPE_BIG_DECIMAL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of BIGDECIMAL  to byte array not supported.");
      case TYPE_OBJECT:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of OBJECT to byte array not supported.");
      case TYPE_URL:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of URL to byte array not supported.");
      case TYPE_ASCII_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of ASCII stream to byte array not supported.");
      case TYPE_BINARY_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of binary stream to byte array not supported.");
      case TYPE_UNICODE_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of Unicode stream to byte array not supported.");
      case TYPE_CHARACTER_STREAM:
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Conversion of character stream to byte array not supported.");

      default:
      	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unrecognized valueType "+valueType_);


    }
    return ba;

  }




  boolean isNull()
  {
    return null_;
  }

  void setNull(boolean b)
  {
    null_ = b;
  }

  int getOffset()
  {
    return offset_;
  }

  void setOffset(int offset)
  {
    offset_ = offset;
  }

  String getName()
  {
    return name_;
  }

  void setName(String name)
  {
    name_ = name;
  }

  String getUdtName()
  {
    return udtName_;
  }

  public void setUdtName(String name) {
	udtName_ = name;
  }

  String getTable()
  {
    return table_;
  }

  void setTable(String name)
  {
    table_ = name;
  }

  String getSchema()
  {
    return schema_;
  }

  void setSchema(String name)
  {
    schema_ = name;
  }

  String getLabel()
  {
    return label_;
  }

  void setLabel(String name)
  {
    label_ = name;
  }

  boolean isAutoIncrement()
  {
    return autoIncrement_;
  }

  void setAutoIncrement(boolean b)
  {
    autoIncrement_ = b;
  }

  boolean isDefinitelyWritable()
  {
    return definitelyWritable_;
  }

  void setDefinitelyWritable(boolean b)
  {
    definitelyWritable_ = b;
  }

  boolean isReadOnly()
  {
    return readOnly_;
  }

  void setReadOnly(boolean b)
  {
    readOnly_ = b;
  }

  boolean isSearchable()
  {
    return searchable_;
  }

  void setSearchable(boolean b)
  {
    searchable_ = b;
  }

  boolean isWritable()
  {
    return writable_;
  }

  void setWritable(boolean b)
  {
    writable_ = b;
  }

  int getType()
  {
    return type_;
  }

  void setType(int type)
  {
    type_ = type;
  }

  int getSQLType() throws SQLException
  {
    switch (type_ &0xFFFE)
    {
      case DB2Type.DATE: // DATE
        return java.sql.Types.DATE;
      case DB2Type.TIME: // TIME
        return java.sql.Types.TIME;
      case DB2Type.TIMESTAMP: // TIMESTAMP
        return java.sql.Types.TIMESTAMP;
      case DB2Type.DATALINK:
        return java.sql.Types.DATALINK;
      case DB2Type.BLOB: // BLOB
        return java.sql.Types.BLOB;
      case DB2Type.CLOB: // CLOB
      case DB2Type.DBCLOB: // DBCLOB
        return java.sql.Types.CLOB;
      case DB2Type.VARCHAR: // VARCHAR
        return java.sql.Types.VARCHAR;
      case DB2Type.CHAR: // CHAR
        return java.sql.Types.CHAR;
      case DB2Type.LONGVARCHAR: // LONG VARCHAR
        return java.sql.Types.LONGVARCHAR;
      case DB2Type.VARGRAPHIC: // VARGRAPHIC
        return java.sql.Types.VARCHAR;
      case DB2Type.GRAPHIC: // GRAPHIC
        return java.sql.Types.CHAR;
      case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
        return java.sql.Types.LONGVARCHAR;
      case DB2Type.FLOATINGPOINT: // floating point
      {
        if (length_ == 4) {
          return java.sql.Types.REAL;
        } else {
    	  return java.sql.Types.DOUBLE;
        }

      }
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return java.sql.Types.DECIMAL;
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return java.sql.Types.NUMERIC;
      case DB2Type.BIGINT: // BIGINT
        return java.sql.Types.BIGINT;
      case DB2Type.INTEGER: // INTEGER
        return java.sql.Types.INTEGER;
      case DB2Type.SMALLINT: // SMALLINT
        return java.sql.Types.SMALLINT;
      case DB2Type.ROWID: // ROWID
	  {
	      // ROWID was added in JDK 1.6
	      // Since this is a simple driver, we'll call it varbinary
	      return java.sql.Types.VARBINARY;
	  }
      case DB2Type.VARBINARY: // VARBINARY
        return java.sql.Types.VARBINARY;
      case DB2Type.BINARY: // BINARY
        return java.sql.Types.BINARY;
      case DB2Type.BLOB_LOCATOR: // BLOB locator
        return java.sql.Types.BLOB;
      case DB2Type.CLOB_LOCATOR: // CLOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
        return java.sql.Types.CLOB;
      case DB2Type.XML:  // XML
      case DB2Type.XML_LOCATOR:  // XML
	  {
	      // SQLXML was added in JDK 1.6.
	      // For now, call it a clob
	      return java.sql.Types.CLOB;
	  }
      case DB2Type.DECFLOAT:  // DECFLOAT
        return java.sql.Types.OTHER;
      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database column type: "+type_);
    }
  }

  String getSQLTypeName() throws SQLException
  {
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DATE: // DATE
        return "DATE";
      case DB2Type.TIME: // TIME
        return "TIME";
      case DB2Type.TIMESTAMP: // TIMESTAMP
        return "TIMESTAMP";
      case DB2Type.DATALINK: // DATALINK
	  return "DATALINK";
      case DB2Type.BLOB: // BLOB
	  return "BLOB";
      case DB2Type.CLOB: // CLOB
        return "CLOB";
      case DB2Type.DBCLOB: // DBCLOB
    	  return "DBCLOB";

      case DB2Type.VARCHAR: // VARCHAR
        if (isForBitData_) {
          return "VARCHAR() FOR BIT DATA";
        } else {
          return "VARCHAR";
        }
      case DB2Type.CHAR: // CHAR
        if (isForBitData_) {
          return "CHAR() FOR BIT DATA";
        } else {
          return "CHAR";
        }
      case DB2Type.LONGVARCHAR: // LONG VARCHAR
        return "LONG VARCHAR";
      case DB2Type.VARGRAPHIC: // VARGRAPHIC
        return "VARGRAPHIC";
      case DB2Type.GRAPHIC: // GRAPHIC
        return "GRAPHIC";
      case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
        return "LONG VARGRAPHIC";
      case DB2Type.FLOATINGPOINT:
      {
        if (length_ == 4) {
          return "REAL";
        } else {
          return "DOUBLE";
        }
      }
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return "DECIMAL";
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return "NUMERIC";
      case DB2Type.BIGINT: // BIGINT
        return "BIGINT";
      case DB2Type.INTEGER: // INTEGER
        return "INTEGER";
      case DB2Type.SMALLINT: // SMALLINT
        return "SMALLINT";
      case DB2Type.ROWID: // ROWID
        return "ROWID";
      case DB2Type.VARBINARY: // VARBINARY
        if (isForBitData_) {
          return "VARCHAR() FOR BIT DATA";
        } else {
          return "VARBINARY";
        }
      case DB2Type.BINARY: // BINARY
        if (isForBitData_) {
          return "CHAR() FOR BIT DATA";
        } else {
        return "BINARY";
        }
      case DB2Type.BLOB_LOCATOR: // BLOB LOCATOR
        return "BLOB LOCATOR";
      case DB2Type.CLOB_LOCATOR: // CLOB locator
        return "CLOB LOCATOR";
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
        return "DBCLOB";
      case DB2Type.XML:  // SQLXML
	  return "SQLXML";
      case DB2Type.XML_LOCATOR:  // SQLXML
    	  return "XML";         // Matches other JDBC drivers
      case DB2Type.DECFLOAT:  // DECFLOAT
	return "DECFLOAT";

      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database column type: "+type_);
    }
  }

  int getLength()
  {
    return length_;
  }

  /*
   * Returns the length of the declared type.
   */
  int getDeclaredLength() throws SQLException  {
	  if (declaredLength_ == 0) {
		    switch (type_ & 0xFFFE) {
	    	case DB2Type.VARBINARY:
	    	case DB2Type.VARCHAR:
	    	case DB2Type.DATALINK:
	    	case DB2Type.LONGVARCHAR:
	    	case DB2Type.ROWID:
	    		declaredLength_ = length_ - 2;
	    		break;
	    	case DB2Type.VARGRAPHIC:
	    	case DB2Type.LONGVARGRAPHIC:
	    		declaredLength_ = (length_ - 2)/2;
	    		break;
	    	case DB2Type.BLOB:
	    	case DB2Type.CLOB:
	    		declaredLength_ = length_ - 4;
	    		break;
	    	case DB2Type.DBCLOB:
	    		declaredLength_ = (length_ - 4)/2;
	    		break;
	    	case DB2Type.GRAPHIC:
	    		declaredLength_ = length_ / 2;
	    		break;
	    	case DB2Type.DECIMAL:
	    	case DB2Type.NUMERIC:
	    		declaredLength_ = precision_;
	    		break;
	    	case DB2Type.BLOB_LOCATOR:
	    	case DB2Type.CLOB_LOCATOR:
	    	case DB2Type.DBCLOB_LOCATOR:
	    	case DB2Type.XML_LOCATOR:
	    		declaredLength_ = lobMaxSize_;
	    		break;
	    	case DB2Type.DECFLOAT:
				if (length_ == 8) {
					declaredLength_ = 16;
				} else if (length_ == 16) {
					declaredLength_ = 34;
				} else {
					throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,
							"Unknown DECFLOAT length= " + length_);
				}
				break;
	    	case DB2Type.TIMESTAMP:
	    		declaredLength_=26;
	    		break;
	    	case DB2Type.TIME:
	    		declaredLength_=8;
	    		break;
	    	case DB2Type.DATE:
	    		declaredLength_=10;
	    		break;

	    			// TBD
	    	default:
	    		declaredLength_ = length_;


	  }
	  }
	  return declaredLength_;
  }
  void setLength(int len)
  {
    length_ = len;
  }

  int getScale()
  {
    return scale_;
  }

  void setScale(int scale)
  {
    scale_ = scale;
  }

  int getPrecision()
  {
    return precision_;
  }

  void setPrecision(int prec)
  {
    precision_ = prec;
  }

  public void setLobMaxSize(int lobMaxSize) {
	  lobMaxSize_ = lobMaxSize;
  }

  public int getLobMaxSize() {
	  return lobMaxSize_;
  }

  int getCCSID()
  {
    return ccsid_;
  }

  void setCCSID(int ccsid)
  {
    ccsid_ = ccsid;
    // If the ccsid is 65535 switch the type if chartype
    if (ccsid_ == 65535) {
       switch (type_) {
         case DB2Type.CHAR: type_ = DB2Type.BINARY; isForBitData_ = true; break;
         case DB2Type.CHAR+1: type_ = DB2Type.BINARY+1; isForBitData_ = true;  break;
         case DB2Type.VARCHAR: type_ = DB2Type.VARBINARY; isForBitData_ = true;  break;
         case DB2Type.VARCHAR+1: type_ = DB2Type.VARBINARY+1; isForBitData_ = true;  break;
         case DB2Type.LONGVARCHAR: type_ = DB2Type.VARBINARY; isForBitData_ = true;  break;
         case DB2Type.LONGVARCHAR+1: type_ = DB2Type.VARBINARY+1; isForBitData_ = true;  break;
       }

    }
  }

  void setDateFormat(int i)
  {
    dateFormat_ = i;
  }

  void setTimeFormat(int i)
  {
    timeFormat_ = i;
  }

  void setDateSeparator(int i)
  {
    dateSeparator_ = i;
  }

  void setTimeSeparator(int i)
  {
    timeSeparator_ = i;
  }

  void setUseDateCache(boolean b)
  {
    useDateCache_ = b;
    dateCache_ = b ? new HashMap() : null;
  }

  void setUseTimeCache(boolean b)
  {
    useTimeCache_ = b;
    timeCache_ = b ? new HashMap() : null;
  }

  void setUseStringCache(boolean b)
  {
    useStringCache_ = b;
    cache_ = b ? new HashMap() : null;
  }

  void setCacheLastOnly(boolean b)
  {
    cacheLastOnly_ = b;
  }

  private final ByteArrayKey key_ = new ByteArrayKey();

  private String lookupString(final byte[] data, final int offset, final int length)
  {
    key_.setHashData(data, offset, length);
    return (String)cache_.get(key_);
  }

  private Date lookupDate(final byte[] data, final int offset, final int length)
  {
    key_.setHashData(data, offset, length);
    return (Date)dateCache_.get(key_);
  }

  private Time lookupTime(final byte[] data, final int offset, final int length)
  {
    key_.setHashData(data, offset, length);
    return (Time)timeCache_.get(key_);
  }

  private void cache(final byte[] data, final int offset, final int length, final String value)
  {
    byte[] key = new byte[length];
    System.arraycopy(data, offset, key, 0, length);
    if (cacheLastOnly_) cache_.clear();
    cache_.put(new ByteArrayKey(key), value);
  }

  private void cache(final byte[] data, final int offset, final int length, final Date value)
  {
    byte[] key = new byte[length];
    System.arraycopy(data, offset, key, 0, length);
    dateCache_.clear();
    dateCache_.put(new ByteArrayKey(key), value);
  }

  private void cache(final byte[] data, final int offset, final int length, final Time value)
  {
    byte[] key = new byte[length];
    System.arraycopy(data, offset, key, 0, length);
    timeCache_.clear();
    timeCache_.put(new ByteArrayKey(key), value);
  }

  // Convert a string to bytes in an output buffer.
  // Throws truncation exception after
  // converting as much as possible.
  // returns length in bytes
  private int convertString(final String s, final byte[] data, final int offset) throws SQLException
  {
      int outLength = 0;
      boolean truncated = false;
    int length =s.length();
    int declaredLength = getDeclaredLength();
    if (length > declaredLength) {
      	truncated = true;
	      length = declaredLength;
    }
    switch (ccsid_)
    {
      case 13488:
      case 1200:
        outLength = Conv.stringToUnicodeByteArray(s, length, data, offset);
        break;
      case 65535:
        outLength = Conv.stringToEBCDICByteArray37(s, length, data, offset);
        break;
      case 1208:
    	outLength = Conv.stringToUtf8ByteArray(s, length, data, offset);
      default:
        try {
				  outLength = Conv.stringToEBCDICByteArray(s, length, data, offset, ccsid_);
			  } catch (UnsupportedEncodingException e) {
				  SQLException sqlex = JDBCError.getSQLException("22524");
				  sqlex.initCause(e);
				  throw sqlex;
			  }
    }

    if (truncated) {
	throw new DataTruncation(index_,  /* index */
              parameter_, /* was parameter truncated */
              false, /* read */
              s.length(), /* dataSize */
              length); /* transferSize */
    }
    return outLength;
  }

  //
  // Convert column to bytes.  Throws truncation exception after converting as much as possible.
  //
  void convertToBytes(final byte[] data, final int offset) throws SQLException
  {
    boolean truncated = false;
    int length = 0;
    int dataSize = 0;
    try {
      if ((valueType_ == 0) && parameter_) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_COUNT_MISMATCH);


    if (!null_)
    {
      int len = 0;
      String s = stringValue_;
      float f = floatValue_;
      double d = doubleValue_ ;

      long l = longValue_;
      int i = intValue_;
      short sh = shortValue_;
      byte[] ba = byteArrayValue_;
      switch (type_ & 0xFFFE)
      {

        case DB2Type.DATE: // DATE
          switch (dateFormat_)
          {
            case 1: // MDY
            default:
              s = getValueString();
              convertString(s, data, offset);
              break;
          }
          break;
        case DB2Type.TIME:  // TIME
          s = getValueTimeAsString();
          convertString(s, data, offset);
          break;

        case DB2Type.TIMESTAMP: // TIMESTAMP
	  s = getValueTimestampAsString();
	  convertString(s, data, offset);
	  break;
          //TODO
        case DB2Type.DATALINK: // Datalink
          s = getValueString();
          len = convertString(s, data, offset+2);
          Conv.shortToByteArray(len, data, offset);
          break;

      case DB2Type.BLOB: // BLOB
      case DB2Type.CLOB: // CLOB
      case DB2Type.DBCLOB: // DBCLOB
	    throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Lob  not implemented yet");

        case DB2Type.VARCHAR: // VARCHAR
        case DB2Type.LONGVARCHAR: // LONG VARCHAR
          s = getValueString();
          len = convertString(s, data, offset+2);
          Conv.shortToByteArray(len, data, offset);
          break;

        case DB2Type.CHAR: // CHAR
          s = getValueString();
          while (s.length() < length_) s = s + " ";//TODO
          convertString(s, data, offset);
          break;
        case DB2Type.VARGRAPHIC: // VARGRAPHIC
        case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
          s = getValueString();
	  // length is in bytes
          len = Conv.stringToUnicodeByteArray(s, data, offset+2);
          Conv.shortToByteArray(len / 2, data, offset);
          break;
        case DB2Type.GRAPHIC: // GRAPHIC
          s = getValueString();
          Conv.stringToUnicodeByteArray(s, data, offset, length_);
          break;
        case DB2Type.FLOATINGPOINT: // Float
	    if (length_ == 4) {
		f = getValueFloat();
		Conv.floatToByteArray(f, data, offset);
	    } else if (length_ == 8) {
		d = getValueDouble();
		Conv.doubleToByteArray(d, data, offset);
	    } else {
		throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_+" length= "+length_);
	    }
	    break;
        case DB2Type.DECIMAL: // DECIMAL (packed decimal)
          s = getNonexponentValueString();
	      s = formatDecimal(s, precision_, scale_);
	      Conv.stringToPackedDecimal(s, precision_, data, offset);
	      break;

        case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
          s = getNonexponentValueString();
      	  s = formatDecimal(s, precision_, scale_);
          Conv.stringToZonedDecimal(s, precision_, data, offset);
          break;
        case DB2Type.BIGINT: // BIGINT
          l = getValueLong();
          Conv.longToByteArray(l, data, offset);
          break;
        case DB2Type.INTEGER: // INTEGER
          i = getValueInt();
          Conv.intToByteArray(i, data, offset);
          break;
        case DB2Type.SMALLINT: // SMALLINT
          sh = getValueShort();
          Conv.shortToByteArray(sh, data, offset);
          break;
      case DB2Type.ROWID: // ROWID
      case DB2Type.VARBINARY: // VARBINARY
	  ba = getValueByteArray();
	  length = ba.length;
	  if (length + 2 > length_) {
	    truncated = true;
	    dataSize = length;
	    length=length_-2;
	  }
	  for (int z = 0; z < length; z++) {
	      data[offset+2+z] = ba[z];
	  }
          Conv.shortToByteArray(ba.length, data, offset);

          break;
        case DB2Type.BINARY: // BINARY
	  ba = getValueByteArray();
    length = ba.length;
    if (length > length_) {
      dataSize = length;
      truncated = true;
      length=length_;
    }

	  for (int z = 0; z < length; z++) {
	      data[offset+z] = ba[z];
	  }

          break;
      case DB2Type.BLOB_LOCATOR: // BLOB locator
      case DB2Type.CLOB_LOCATOR: // CLOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
      case DB2Type.XML_LOCATOR:
	    throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"LOB locator not implemented yet");

      case DB2Type.XML:  // XML
	    throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"XML not implemented yet");
      case DB2Type.DECFLOAT:  // DECFLOAT
	    throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"DECFLOAT not implemented yet");

        default:
          throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_);
      }
    }
    } catch (NumberFormatException nfe) {
    	SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
    	sqlex.initCause(nfe);
    	throw sqlex;
    }

    if (truncated) {
      throw new DataTruncation(index_,  /* index */
                  parameter_, /* was parameter truncated */
                  false, /* read */
                  dataSize, /* dataSize */
                  length); /* transferSize */
        }

  }

  byte convertToByte(final byte[] data, final int rowOffset)
      throws SQLException {
    String stringValue = null;
    try {
      final int offset = rowOffset + offset_;
      switch (type_ & 0xFFFE) {
      case DB2Type.BIGINT: // BIGINT
      {
        long longValue = convertToLong(data, rowOffset);
        if (longValue > Byte.MAX_VALUE || longValue < Byte.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (byte) longValue;
        }
      }
      case DB2Type.INTEGER: // INTEGER
      {
        int intValue = convertToInt(data, rowOffset);
        if (intValue > Byte.MAX_VALUE || intValue < Byte.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (byte) intValue;
        }
      }
      case DB2Type.SMALLINT: // SMALLINT
      {
        int shortValue = convertToShort(data, rowOffset);
        if (shortValue > Byte.MAX_VALUE || shortValue < Byte.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (byte) shortValue;
        }
      }
      case DB2Type.FLOATINGPOINT: // floating point:
        if (length_ == 4) {
          float floatValue =  Conv.byteArrayToFloat(data, offset);
          if (floatValue > Byte.MAX_VALUE || floatValue < Byte.MIN_VALUE) {
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          } else {
            return (byte) floatValue;
          }
        } else if (length_ == 8) {
          double doubleValue = Conv.byteArrayToDouble(data, offset);
          if (doubleValue > Byte.MAX_VALUE || doubleValue < Byte.MIN_VALUE) {
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          } else {
            return (byte) doubleValue;
          }
        }
      case DB2Type.VARBINARY: // VARBINARY
        int varblen = Conv.byteArrayToShort(data, offset);
        if (varblen == 1) {
          return  data[offset + 2] ;
        } else {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      case DB2Type.BINARY: // BINARY
        if (length_ == 1) {
          return  data[offset] ;
        } else {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      default:
        stringValue = convertToString(data, rowOffset);
        Double doubleValue = new Double(stringValue);
        double d = doubleValue.doubleValue();


        if (d > Byte.MAX_VALUE || d < Byte.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return doubleValue.byteValue();
        }

      }
    } catch (NumberFormatException e) {
      SQLException sqlex = JDBCError.getSQLException(
          JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
      sqlex.initCause(e);
      throw sqlex;
    }

  }

  double convertToDouble(final byte[] data, final int rowOffset) throws SQLException
  {
      String stringValue = null;
      try {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return Conv.packedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return Conv.zonedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.BIGINT: // BIGINT
        return (double)Conv.byteArrayToLong(data, offset);
      case DB2Type.INTEGER: // INTEGER
        return (double)Conv.byteArrayToInt(data, offset);
      case DB2Type.SMALLINT: // SMALLINT
        return (double)Conv.byteArrayToShort(data, offset);
      case DB2Type.FLOATINGPOINT: // floating point:
        if (length_ == 4) {
          return Conv.byteArrayToFloat(data, offset);
        } else if (length_ == 8) {
          return Conv.byteArrayToDouble(data, offset);
        }
      case DB2Type.VARBINARY: // VARBINARY
      case DB2Type.BINARY: // BINARY
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      default:
	  stringValue = convertToString(data, rowOffset);
        return Double.parseDouble(stringValue);
    }
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  float convertToFloat(final byte[] data, final int rowOffset) throws SQLException
  {
      String stringValue = null;
      try {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return (float)Conv.packedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return (float)Conv.zonedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.BIGINT: // BIGINT
        return (float)Conv.byteArrayToLong(data, offset);
      case DB2Type.INTEGER: // INTEGER
        return (float)Conv.byteArrayToInt(data, offset);
      case DB2Type.SMALLINT: // SMALLINT
        return (float)Conv.byteArrayToShort(data, offset);
      case DB2Type.VARBINARY: // VARBINARY
      case DB2Type.BINARY: // BINARY
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      case DB2Type.FLOATINGPOINT: // floating point:
        if (length_ == 4) {
          return Conv.byteArrayToFloat(data, offset);
        } else if (length_ == 8) {
          double d = Conv.byteArrayToDouble(data, offset);
          // Allow these special values to be returned.
          if (d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY || d == Double.NaN) {
            return (float) d;
          }
          if (d > Float.MAX_VALUE || d < - Float.MAX_VALUE) {
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
          } else {
            return (float) d;
          }
        }

      default:
        stringValue = convertToString(data, rowOffset).trim();
      Double doubleValue = new Double(stringValue);
      double d = doubleValue.doubleValue();

      if (d == Double.NEGATIVE_INFINITY || d==Double.POSITIVE_INFINITY || d == Double.NaN) {
         return (float) d;
      }

      if (d > Float.MAX_VALUE || d < - Float.MAX_VALUE) {
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
      } else {
        return doubleValue.floatValue();
      }

    }
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  short convertToShort(final byte[] data, final int rowOffset) throws SQLException
  {
      String stringValue = null;
      try {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
    case DB2Type.FLOATINGPOINT: // floating point:
      if (length_ == 4) {
        float floatValue =  Conv.byteArrayToFloat(data, offset);
        if (floatValue > Short.MAX_VALUE || floatValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (short) floatValue;
        }
      } else if (length_ == 8) {
        double doubleValue = Conv.byteArrayToDouble(data, offset);
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (short) doubleValue;
        }
      }
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
      {
        double doubleValue = Conv.packedDecimalToDouble(data, offset, precision_, scale_);
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (short) doubleValue;
        }

      }
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
      {
        double doubleValue = Conv.zonedDecimalToDouble(data, offset, precision_, scale_);
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (short) doubleValue;
        }
      }
      case DB2Type.BIGINT: // BIGINT
      {
        long longValue = Conv.byteArrayToLong(data, offset);
        if (longValue > Short.MAX_VALUE || longValue < Short.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (short) longValue;
        }
      }

      case DB2Type.INTEGER: // INTEGER
        {
          int intValue = Conv.byteArrayToInt(data, offset);
          if (intValue > Short.MAX_VALUE || intValue < Short.MIN_VALUE) {
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          } else {
            return (short) intValue;
          }
        }
      case DB2Type.SMALLINT: // SMALLINT
        return (short)Conv.byteArrayToShort(data, offset);
      case DB2Type.VARBINARY: // VARBINARY
        int varblen = Conv.byteArrayToShort(data, offset);
        if (varblen > 2) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        switch (varblen)
        {
          case 0: return 0;
          case 1: return (short)(data[offset+2] & 0x00FF);
          case 2: return (short)Conv.byteArrayToShort(data, offset+2);
          default:
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      case DB2Type.BINARY: // BINARY
        int len = length_;
        if (len > 2) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        switch (len)
        {
          case 0: return 0;
          case 1: return (short)(data[offset] & 0x00FF);
          case 2: return (short)Conv.byteArrayToShort(data, offset);
          default:
            throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      default:
	      stringValue = convertToString(data, rowOffset);
        Double doubleValue = new Double (stringValue.trim ());
        double d = doubleValue.doubleValue();
        if( d > Short.MAX_VALUE || d < Short.MIN_VALUE) {
          SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
          throw sqlex;
        }
        return doubleValue.shortValue ();
    }
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  int convertToInt(final byte[] data, final int rowOffset) throws SQLException
  {
      String stringValue = null;
      try {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
      {
        double doubleValue = Conv.packedDecimalToDouble(data, offset, precision_, scale_);
        if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (int) doubleValue;
        }
      }
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
      {
        double doubleValue = Conv.zonedDecimalToDouble(data, offset, precision_, scale_);
        if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        } else {
          return (int) doubleValue;
        }
      }

      case DB2Type.BIGINT: // BIGINT
        {
           long longValue = Conv.byteArrayToLong(data, offset);
           if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
             throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
           } else {
             return (int) longValue;
           }
         }


      case DB2Type.INTEGER: // INTEGER
        return Conv.byteArrayToInt(data, offset);
      case DB2Type.SMALLINT: // SMALLINT
        return Conv.byteArrayToShort(data, offset);
      case DB2Type.VARBINARY: // VARBINARY
        int varblen = Conv.byteArrayToShort(data, offset);
        switch (varblen)
        {
          case 0: return 0;
          case 1: return data[offset+2] & 0x00FF;
          case 2: return Conv.byteArrayToShort(data, offset+2);
          case 3: return ((data[offset+2] << 16) | Conv.byteArrayToShort(data, offset+3)) & 0x00FFFFFF;
          case 4: return Conv.byteArrayToInt(data, offset+2);
          default: throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      case DB2Type.BINARY: // BINARY
        int len = length_;
        switch (len)
        {
          case 0: return 0;
          case 1: return data[offset] & 0x00FF;
          case 2: return Conv.byteArrayToShort(data, offset);
          case 3: return ((data[offset] << 16) | Conv.byteArrayToShort(data, offset+1)) & 0x00FFFFFF;
          case 4: return Conv.byteArrayToInt(data, offset+2);
          default: throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
      default:
	  stringValue = convertToString(data, rowOffset);
      Double doubleValue = new Double (stringValue.trim ());
      double d = doubleValue.doubleValue();
      if( d > Integer.MAX_VALUE || d < Integer.MIN_VALUE) {
        SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
        throw sqlex;
      }
      return doubleValue.intValue ();
    }
      } catch (NumberFormatException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  long convertToLong(final byte[] data, final int rowOffset) throws SQLException
  {
      String stringValue = null;
      try {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DATE: // DATE
        /*
         * This conversion is not supported by any other JDBC drivers.
        int year = (data[offset] & 0x0F)*1000 +
                   (data[offset+1] & 0x0F)*100 +
                   (data[offset+2] & 0x0F)*10 +
                   (data[offset+3] & 0x0F);
        int month = (data[offset+5] & 0x0F)*10 +
                    (data[offset+6] & 0x0F);
        int day = (data[offset+8] & 0x0F)*10 +
                    (data[offset+9] & 0x0F);
        calendar_.clear();
        calendar_.set(Calendar.YEAR, year);
        calendar_.set(Calendar.MONTH, month-1);
        calendar_.set(Calendar.DATE, day);
        return calendar_.getTimeInMillis();
         */
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      case DB2Type.TIME: // TIME
        /*
         * This conversion is not supported by any other JDBC drivers.
        int hours = (data[offset] & 0x0F)*10 +
                    (data[offset+1] & 0x0F);
        int minutes = (data[offset+3] & 0x0F)*10 +
                      (data[offset+4] & 0x0F);
        int seconds = (data[offset+6] & 0x0F)*10 +
                      (data[offset+7] & 0x0F);
        long millis = (hours*60*60*1000L) + (minutes*60*1000L) + (seconds*1000L);
        return millis;
        */
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      case DB2Type.TIMESTAMP: // TIMESTAMP
        /*
         * This conversion is not supported by any other JDBC drivers.
        year = (data[offset] & 0x0F)*1000 +
               (data[offset+1] & 0x0F)*100 +
               (data[offset+2] & 0x0F)*10 +
               (data[offset+3] & 0x0F);
        month = (data[offset+5] & 0x0F)*10 +
                (data[offset+6] & 0x0F);
        day = (data[offset+8] & 0x0F)*10 +
                (data[offset+9] & 0x0F);
        calendar_.clear();
        calendar_.set(Calendar.YEAR, year);
        calendar_.set(Calendar.MONTH, month-1);
        calendar_.set(Calendar.DATE, day);
        hours = (data[offset+11] & 0x0F)*10 +
                (data[offset+12] & 0x0F);
        minutes = (data[offset+14] & 0x0F)*10 +
                  (data[offset+15] & 0x0F);
        seconds = (data[offset+17] & 0x0F)*10 +
                  (data[offset+18] & 0x0F);
        calendar_.set(Calendar.HOUR_OF_DAY, hours);
        calendar_.set(Calendar.MINUTE, minutes);
        calendar_.set(Calendar.SECOND, seconds);
        int ms = (data[offset+20] & 0x0F)*100 +
                 (data[offset+21] & 0x0F)*10 +
                 (data[offset+22] & 0x0F);
        int half = (data[offset+23] & 0x0F);
        if (half >= 5) ++ms; // Round.
        calendar_.set(Calendar.MILLISECOND, ms);
        return calendar_.getTimeInMillis();
        */
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);

      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return (long)Conv.packedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return (long)Conv.zonedDecimalToDouble(data, offset, precision_, scale_);
      case DB2Type.BIGINT: // BIGINT
        return Conv.byteArrayToLong(data, offset);
      case DB2Type.INTEGER: // INTEGER
        return Conv.byteArrayToInt(data, offset);
      case DB2Type.SMALLINT: // SMALLINT
        return Conv.byteArrayToShort(data, offset);
      case DB2Type.VARBINARY: // VARBINARY
        int varblen = Conv.byteArrayToShort(data, offset);
        if (varblen > 8) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        switch (varblen)
        {
          case 0: return 0;
          case 1: return data[offset+2] & 0x00FF;
          case 2: return Conv.byteArrayToShort(data, offset+2);
          case 3: return ((data[offset+2] << 16) | Conv.byteArrayToShort(data, offset+3)) & 0x00FFFFFF;
          case 4: return Conv.byteArrayToInt(data, offset+2);
          case 5: return ((data[offset+2] << 32) | Conv.byteArrayToInt(data, offset+3)) & 0x00FFFFFFFFFFL;
          case 6: return ((data[offset+2] << 40) + (data[offset+3] << 32) + Conv.byteArrayToInt(data, offset+4)) & 0x00FFFFFFFFFFFFL;
          case 7: return ((data[offset+2] << 48) + (data[offset+3] << 40) + (data[offset+4] << 32) + Conv.byteArrayToInt(data, offset+5)) & 0x00FFFFFFFFFFFFFFL;
          case 8: return Conv.byteArrayToLong(data, offset+2);
        }
        return 0;
      case DB2Type.BINARY: // BINARY
        int len = length_;
        if (len > 8) {
          throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        }
        switch (len)
        {
          case 0: return 0;
          case 1: return data[offset] & 0x00FF;
          case 2: return Conv.byteArrayToShort(data, offset);
          case 3: return ((data[offset] << 16) | Conv.byteArrayToShort(data, offset+1)) & 0x00FFFFFF;
          case 4: return Conv.byteArrayToInt(data, offset+2);
          case 5: return ((data[offset] << 32) | Conv.byteArrayToInt(data, offset+1)) & 0x00FFFFFFFFFFL;
          case 6: return ((data[offset] << 40) + (data[offset+1] << 32) + Conv.byteArrayToInt(data, offset+2)) & 0x00FFFFFFFFFFFFL;
          case 7: return ((data[offset] << 48) + (data[offset+1] << 40) + (data[offset+2] << 32) + Conv.byteArrayToInt(data, offset+3)) & 0x00FFFFFFFFFFFFFFL;
          case 8: return Conv.byteArrayToLong(data, offset+2);
        }
        return 0;
      default:
	  stringValue = convertToString(data, rowOffset);
        return Long.parseLong(stringValue);
    }
      } catch (NumberFormatException e) {
        try {
          Double doubleValue = new Double (stringValue.trim ());
          double d = doubleValue.doubleValue(); //@trunc

          if( d > Long.MAX_VALUE || d < Long.MIN_VALUE) {
            SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
            sqlex.initCause(e);
            throw sqlex;
          }
          return doubleValue.longValue ();
        } catch (NumberFormatException e2) {
          SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
          sqlex.initCause(e2);
          throw sqlex;
        }
      }
  }

  Date convertToDate(final byte[] data, final int rowOffset, Calendar cal) throws SQLException
  {
      String stringValue = null;
      try {
    if (buffer_ == null)
    {
      int size = length_+2;
      if (precision_+2 > size) size = precision_+2;
      buffer_ = new char[size];
    }
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.DATE: // DATE
      case DB2Type.TIMESTAMP: // TIMESTAMP
        if (useDateCache_)
        {
          Date value = lookupDate(data, offset, length_);
          if (value != null) return value;
        }
        // We told the server to send us dates in ISO, and EBCDIC makes it easy to mask off.
        int year = (data[offset] & 0x0F)*1000 +
                   (data[offset+1] & 0x0F)*100 +
                   (data[offset+2] & 0x0F)*10 +
                   (data[offset+3] & 0x0F);
        int month = (data[offset+5] & 0x0F)*10 +
                    (data[offset+6] & 0x0F);
        int day = (data[offset+8] & 0x0F)*10 +
                    (data[offset+9] & 0x0F);
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DATE, day);
        Date val = new Date(cal.getTimeInMillis()); // This is way faster than doing java.sql.Date.valueOf().
        if (useDateCache_)
        {
          cache(data, offset, length_, val);
        }
        return val;
      // It does not make send to return a date from any one of these values.
      case DB2Type.TIME: // TIME
      case DB2Type.BIGINT: // BIGINT
      case DB2Type.INTEGER: // INTEGER
      case DB2Type.SMALLINT: // SMALLINT
      case DB2Type.VARBINARY: // VARBINARY
      case DB2Type.BINARY: // BINARY
        throw JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
         /*
        if (useDateCache_)
        {
          Date value = lookupDate(data, offset, length_);
          if (value != null) return value;
        }
        Date value = new Date(convertToLong(data, rowOffset));
        if (useDateCache_)
        {
          cache(data, offset, length_, value);
        }
        return value;
	*/
      default:
	  stringValue = convertToString(data, rowOffset).trim();
        return Date.valueOf(stringValue);
    }
      } catch (IllegalArgumentException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  Time convertToTime(final byte[] data, final int rowOffset, Calendar cal) throws SQLException
  {
      String stringValue = null;
      try {
    if (buffer_ == null)
    {
      int size = length_+2;
      if (precision_+2 > size) size = precision_+2;
      buffer_ = new char[size];
    }
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)  {
      case DB2Type.TIME: // TIME
        switch (timeFormat_)
        {
          case 0: // HMS
          default:
            if (useTimeCache_)
            {
              Time value = lookupTime(data, offset, length_);
              if (value != null) return value;
            }

            int hours = (data[offset] & 0x0F)*10 +
                        (data[offset+1] & 0x0F);
            int minutes = (data[offset+3] & 0x0F)*10 +
                          (data[offset+4] & 0x0F);
            int seconds = (data[offset+6] & 0x0F)*10 +
                          (data[offset+7] & 0x0F);
            cal.clear();
            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, seconds);
            long millis = cal.getTimeInMillis();
            Time value = new Time(millis);
            if (useTimeCache_)
            {
              cache(data, offset, length_, value);
            }
            return value;
        }
      case DB2Type.TIMESTAMP: // TIMESTAMP
        if (useTimeCache_)
        {
          Time value = lookupTime(data, offset, length_);
          if (value != null) return value;
        }
        int hours = (data[offset+11] & 0x0F)*10 +
                    (data[offset+12] & 0x0F);
        int minutes = (data[offset+14] & 0x0F)*10 +
                      (data[offset+15] & 0x0F);
        int seconds = (data[offset+17] & 0x0F)*10 +
                      (data[offset+18] & 0x0F);
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        long millis = cal.getTimeInMillis();


        Time val = new Time(millis);
        if (useTimeCache_)
        {
          cache(data, offset, length_, val);
        }
        return val;
	// It is not valid to create a time from these types
      case DB2Type.DATE: // DATE
      case DB2Type.BIGINT: // BIGINT
      case DB2Type.INTEGER: // INTEGER
      case DB2Type.SMALLINT: // SMALLINT
      case DB2Type.VARBINARY: // VARBINARY
      case DB2Type.BINARY: // BINARY
	  JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	  return null;
      default:
	  stringValue = convertToString(data, rowOffset).trim();
        return Time.valueOf(stringValue);
    } /* switch */
      } catch (IllegalArgumentException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  Timestamp convertToTimestamp(final byte[] data, final int rowOffset, Calendar cal) throws SQLException
  {
       String stringValue = null;
     try {
    if (buffer_ == null)
    {
      int size = length_+2;
      if (precision_+2 > size) size = precision_+2;
      buffer_ = new char[size];
    }
    final int offset = rowOffset+offset_;
    int year = 0;
    int month = 0;
    int day = 0;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    switch (type_ & 0xFFFE )
    {
      case DB2Type.DATE: // DATE
        year = (data[offset] & 0x0F)*1000 +
               (data[offset+1] & 0x0F)*100 +
               (data[offset+2] & 0x0F)*10 +
               (data[offset+3] & 0x0F);
        month = (data[offset+5] & 0x0F)*10 +
                (data[offset+6] & 0x0F);
        day = (data[offset+8] & 0x0F)*10 +
                (data[offset+9] & 0x0F);
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DATE, day);
        return new Timestamp(cal.getTimeInMillis());

      case DB2Type.TIMESTAMP: // TIMESTAMP
        year = (data[offset] & 0x0F)*1000 +
               (data[offset+1] & 0x0F)*100 +
               (data[offset+2] & 0x0F)*10 +
               (data[offset+3] & 0x0F);
        month = (data[offset+5] & 0x0F)*10 +
                (data[offset+6] & 0x0F);
        day = (data[offset+8] & 0x0F)*10 +
                (data[offset+9] & 0x0F);
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DATE, day);
        hours = (data[offset+11] & 0x0F)*10 +
                (data[offset+12] & 0x0F);
        minutes = (data[offset+14] & 0x0F)*10 +
                  (data[offset+15] & 0x0F);
        seconds = (data[offset+17] & 0x0F)*10 +
                  (data[offset+18] & 0x0F);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        Timestamp stamp = new Timestamp(cal.getTimeInMillis());
        int micros = (data[offset+20] & 0x0F)*100000 +
                     (data[offset+21] & 0x0F)*10000 +
                     (data[offset+22] & 0x0F)*1000 +
                     (data[offset+23] & 0x0F)*100 +
                     (data[offset+24] & 0x0F)*10 +
                     (data[offset+25] & 0x0F);
        int nanos = micros*1000;
        stamp.setNanos(nanos);
        return stamp;

	// It is not valid to get a timestamp from any of these types
      case DB2Type.TIME: // TIME
      case DB2Type.BIGINT: // BIGINT
      case DB2Type.INTEGER: // INTEGER
      case DB2Type.SMALLINT: // SMALLINT
      case DB2Type.VARBINARY: // VARBINARY
      case DB2Type.BINARY: // BINARY
	   JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
	  return null;
      default:
	  stringValue = convertToString(data, rowOffset).trim();
        return Timestamp.valueOf(stringValue);
    }
      } catch (IllegalArgumentException e) {
	  SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH, stringValue);
	  sqlex.initCause(e);
	  throw sqlex;
      }

  }

  byte[] convertToOutputBytes(final byte[] data, final int rowOffset) throws SQLException
  {
    int offset = rowOffset+offset_;
    int length = length_;
    switch (type_ & 0xFFFE )
    {
      case DB2Type.DATE: // DATE
        break;
      case DB2Type.TIME: // TIME
        break;
      case DB2Type.TIMESTAMP: // TIMESTAMP
        length = 26;
        break;
      case DB2Type.CLOB: // LOB
        length = Conv.byteArrayToInt(data, offset);
        offset += 4;
        break;
      case DB2Type.VARCHAR: // VARCHAR
      case DB2Type.LONGVARCHAR: // LONG VARCHAR
      case DB2Type.DATALINK:
        length = Conv.byteArrayToShort(data, offset);
        offset += 2;
        break;
      case DB2Type.CHAR: // CHAR
        break;
      case DB2Type.VARGRAPHIC: // VARGRAPHIC
      case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
        length = Conv.byteArrayToShort(data, offset)*2;
        offset += 2;
        break;
      case DB2Type.GRAPHIC: // GRAPHIC
        /* length = length * 2 */ ; /* length already in bytes */
        break;
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
      case DB2Type.BIGINT: // BIGINT
      case DB2Type.INTEGER: // INTEGER
      case DB2Type.SMALLINT: // SMALLINT
      case DB2Type.FLOATINGPOINT: // floating point:
        break;
      case DB2Type.VARBINARY: // VARBINARY
        length = Conv.byteArrayToShort(data, offset);
	  offset += 2;
        break;

      case DB2Type.BINARY: // BINARY
        break;
      case DB2Type.CLOB_LOCATOR: //TODO - LOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locate
//        System.out.println("CCSID! "+ccsid_);
//        System.out.println(length_+", "+offset+", "+buffer_.length+", "+scale_+", "+precision_);
        int locatorHandle = Conv.byteArrayToInt(data, offset);
//        System.out.println("Handle: "+Integer.toHexString(locatorHandle));
      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_);
    }
    final byte[] b = new byte[length];
    System.arraycopy(data, offset, b, 0, length);
    return b;
  }

  String convertToString(final byte[] data, final int rowOffset) throws SQLException
  {
//    System.out.println("Convert to string "+name_+", "+ccsid_);
    if (buffer_ == null)
    {
      int size = length_+2;
      if (precision_+2 > size) size = precision_+2;
      buffer_ = new char[size];
    }
    final int offset = rowOffset+offset_;
    try
    {
      switch (type_ & 0xFFFE )
      {
        case DB2Type.DATE: // DATE
          switch (dateFormat_)
          {
            case 1: // MDY
            default:
              if (useStringCache_)
              {
                String value = lookupString(data, offset, length_);
                if (value != null) return value;
                value = Conv.ebcdicByteArrayToString(data, offset, length_, buffer_);
                cache(data, offset, length_, value);
                return value;
              }
              return Conv.ebcdicByteArrayToString(data, offset, length_, buffer_);
          }
        case DB2Type.TIME: // TIME
          switch (timeFormat_)
          {
            case 0: // HMS
            default:
              if (useStringCache_)
              {
                String value = lookupString(data, offset, length_);
                if (value != null) return value;
                value = Conv.ebcdicByteArrayToString(data, offset, length_, buffer_);
                cache(data, offset, length_, value);
                return value;
              }
              return Conv.ebcdicByteArrayToString(data, offset, length_, buffer_);
          }
        case DB2Type.TIMESTAMP: // TIMESTAMP
          if (useStringCache_)
          {
            String value = lookupString(data, offset, length_);
            if (value != null) return value;
          }
          buffer_[0] = NUMS[data[offset] & 0x0F];
          buffer_[1] = NUMS[data[offset+1] & 0x0F];
          buffer_[2] = NUMS[data[offset+2] & 0x0F];
          buffer_[3] = NUMS[data[offset+3] & 0x0F];
          buffer_[4] = '-';
          buffer_[5] = NUMS[data[offset+5] & 0x0F];
          buffer_[6] = NUMS[data[offset+6] & 0x0F];
          buffer_[7] = '-';
          buffer_[8] = NUMS[data[offset+8] & 0x0F];
          buffer_[9] = NUMS[data[offset+9] & 0x0F];
          buffer_[10] = ' '; // Was using 'T' but now try to match java.sql.Timestamp.valueOf() format.
          buffer_[11] = NUMS[data[offset+11] & 0x0F];
          buffer_[12] = NUMS[data[offset+12] & 0x0F];
          buffer_[13] = ':';
          buffer_[14] = NUMS[data[offset+14] & 0x0F];
          buffer_[15] = NUMS[data[offset+15] & 0x0F];
          buffer_[16] = ':';
          buffer_[17] = NUMS[data[offset+17] & 0x0F];
          buffer_[18] = NUMS[data[offset+18] & 0x0F];
          buffer_[19] = '.';
          buffer_[20] = NUMS[data[offset+20] & 0x0F];
          buffer_[21] = NUMS[data[offset+21] & 0x0F];
          buffer_[22] = NUMS[data[offset+22] & 0x0F];
          buffer_[23] = NUMS[data[offset+23] & 0x0F];
          buffer_[24] = NUMS[data[offset+24] & 0x0F];
          buffer_[25] = NUMS[data[offset+25] & 0x0F];
          String val = new String(buffer_, 0, 26);
          if (useStringCache_)
          {
            cache(data, offset, length_, val);
          }
          return val;
        case DB2Type.CLOB: // LOB
          int totalLength = Conv.byteArrayToInt(data, offset);
          switch (ccsid_)
          {
            case 13488:
            case 1200:
              return Conv.unicodeByteArrayToString(data, offset+4, totalLength, buffer_);
            default:
              return Conv.ebcdicByteArrayToString(data, offset+4, totalLength, buffer_, ccsid_);
          }
        case DB2Type.VARCHAR: // VARCHAR
        case DB2Type.LONGVARCHAR: // LONG VARCHAR
        case DB2Type.DATALINK:
          int varlen = Conv.byteArrayToShort(data, offset);
          switch (ccsid_)
          {
            case 13488:
            case 1200:
              if (useStringCache_)
              {
                String value = lookupString(data, offset+2, varlen);
                if (value != null) return value;
                value = Conv.unicodeByteArrayToString(data, offset+2, varlen, buffer_);
                cache(data, offset+2, varlen, value);
                return value;
              }
              return Conv.unicodeByteArrayToString(data, offset+2, varlen, buffer_);
            default:
              if (useStringCache_)
              {
                String value = lookupString(data, offset+2, varlen);
                if (value != null) return value;
                value = Conv.ebcdicByteArrayToString(data, offset+2, varlen, buffer_, ccsid_);
                cache(data, offset+2, varlen, value);
                return value;
              }
              return Conv.ebcdicByteArrayToString(data, offset+2, varlen, buffer_, ccsid_);
          }
        case DB2Type.CHAR: // CHAR
          switch (ccsid_)
          {
            case 13488:
            case 1200:
              if (useStringCache_)
              {
                String value = lookupString(data, offset, length_);
                if (value != null) return value;
                value = Conv.unicodeByteArrayToString(data, offset, length_, buffer_);
                cache(data, offset, length_, value);
                return value;
              }
              return Conv.unicodeByteArrayToString(data, offset, length_, buffer_);
            default:
              if (useStringCache_)
              {
                String value = lookupString(data, offset, length_);
                if (value != null) return value;
                value = Conv.ebcdicByteArrayToString(data, offset, length_, buffer_);
                cache(data, offset, length_, value);
                return value;
              }
              return Conv.ebcdicByteArrayToString(data, offset, length_, buffer_, ccsid_);
          }
        case DB2Type.VARGRAPHIC: // VARGRAPHIC
        case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
        	if (ccsid_ == 13488 || ccsid_ == 1200 ) {
          int varglen = Conv.byteArrayToShort(data, offset);
          if (useStringCache_)
          {
            String value = lookupString(data, offset+2, varglen*2);
            if (value != null) return value;
            value = Conv.unicodeByteArrayToString(data, offset+2, varglen*2, buffer_);
            cache(data, offset+2, varglen*2, value);
            return value;
          }
          return Conv.unicodeByteArrayToString(data, offset+2, varglen*2, buffer_);
        	} else {
        		JDBCError.throwSQLException(JDBCError.EXC_CHAR_CONVERSION_INVALID);
        	}
        case DB2Type.GRAPHIC: // GRAPHIC
        	if (ccsid_ == 13488 || ccsid_ == 1200 ) {
        		return Conv.unicodeByteArrayToString(data, offset, length_, buffer_);
        	} else {
        		JDBCError.throwSQLException(JDBCError.EXC_CHAR_CONVERSION_INVALID);
        	}
	case DB2Type.FLOATINGPOINT: // floating point:
	    if (length_ == 4) {
		return ""+Conv.byteArrayToFloat(data, offset);
	    } else if (length_ == 8) {
		return ""+Conv.byteArrayToDouble(data, offset);
	    }
	    throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_+" length= "+length_);
        case DB2Type.DECIMAL: // DECIMAL (packed decimal)
          return Conv.packedDecimalToString(data, offset, precision_, scale_, buffer_);
        case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
          return Conv.zonedDecimalToString(data, offset, precision_, scale_, buffer_);
        case DB2Type.BIGINT: // BIGINT
          if (useStringCache_)
          {
            String value = lookupString(data, offset, 8);
            if (value != null) return value;
            value = String.valueOf(Conv.byteArrayToLong(data, offset));
            cache(data, offset, 8, value);
            return value;
          }
          return String.valueOf(Conv.byteArrayToLong(data, offset));
        case DB2Type.INTEGER: // INTEGER
          if (useStringCache_)
          {
            String value = lookupString(data, offset, 4);
            if (value != null) return value;
            value = String.valueOf(Conv.byteArrayToInt(data, offset));
            cache(data, offset, 4, value);
            return value;
          }
          return String.valueOf(Conv.byteArrayToInt(data, offset));
        case DB2Type.SMALLINT: // SMALLINT
          if (useStringCache_)
          {
            String value = lookupString(data, offset, 2);
            if (value != null) return value;
            value = String.valueOf(Conv.byteArrayToShort(data, offset));
            cache(data, offset, 2, value);
            return value;
          }
          return String.valueOf(Conv.byteArrayToShort(data, offset));
        case DB2Type.VARBINARY: // VARBINARY
          int varblen = Conv.byteArrayToShort(data, offset);
          if (useStringCache_)
          {
            String value = lookupString(data, offset+2, varblen);
            if (value != null) return value;
            value = Conv.bytesToHexString(data, offset+2, varblen);
            cache(data, offset+2, varblen, value);
            return value;
          }
          return Conv.bytesToHexString(data, offset+2, varblen);
        case DB2Type.BINARY: // BINARY
          if (useStringCache_)
          {
            String value = lookupString(data, offset, length_);
            if (value != null) return value;
            value = Conv.bytesToHexString(data, offset, length_);
            cache(data, offset, length_, value);
            return value;
          }
          return Conv.bytesToHexString(data, offset, length_);
        case DB2Type.DBCLOB:
        	if (ccsid_ == 13488 || ccsid_ == 1200 ) {
          int varglen = Conv.byteArrayToInt(data, offset);
          if (useStringCache_)
          {
            String value = lookupString(data, offset+4, varglen*2);
            if (value != null) return value;
            value = Conv.unicodeByteArrayToString(data, offset+4, varglen*2, buffer_);
            cache(data, offset+2, varglen*2, value);
            return value;
          }
          return Conv.unicodeByteArrayToString(data, offset+4, varglen*2, buffer_);
        	} else {
        		JDBCError.throwSQLException(JDBCError.EXC_CHAR_CONVERSION_INVALID);
        		return null;
        	}

        case DB2Type.BLOB_LOCATOR: //TODO BLOB locator
        case DB2Type.CLOB_LOCATOR: //TODO - CLOB locator
        case DB2Type.DBCLOB_LOCATOR: // - DBCLOB locator
        case DB2Type.XML_LOCATOR:
//          System.out.println("CCSID! "+ccsid_);
//          System.out.println(length_+", "+offset+", "+buffer_.length+", "+scale_+", "+precision_);
          int locatorHandle = Conv.byteArrayToInt(data, offset);
//          System.out.println("Handle: 0x"+Integer.toHexString(locatorHandle));
          throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unsupported database type: "+type_+" length= "+length_);
        case DB2Type.DECFLOAT:
          if (length_ == 8) {
            return Conv.decfloat16ByteArrayToString(data, offset);
          } else if (length_ == 16) {
            return Conv.decfloat34ByteArrayToString(data, offset);
          } else {
            throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown DECFLOAT length= "+length_);
          }
        default:
          throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_+" length= "+length_);
      }
    }
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH,"Data conversion error");
      sql.initCause(uee);
      throw sql;
    }
  }

  boolean convertToBoolean(final byte[] data, final int rowOffset) throws SQLException
  {
    final int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.VARCHAR: // VARCHAR
      case DB2Type.LONGVARCHAR: // LONG VARCHAR
      case DB2Type.CHAR: // CHAR
      case DB2Type.VARGRAPHIC: // VARGRAPHIC
      case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
      case DB2Type.GRAPHIC: // GRAPHIC
        String s = convertToString(data, rowOffset);
        s = s.trim();
        return !s.equals("0") && !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("n"); //TODO
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return Conv.packedDecimalToDouble(data, offset, precision_, scale_) != 0.0d;
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return Conv.zonedDecimalToDouble(data, offset, precision_, scale_) != 0.0d;
      case DB2Type.BIGINT: // BIGINT
        return convertToLong(data, rowOffset) != 0L;
      case DB2Type.INTEGER: // INTEGER
        return convertToInt(data, rowOffset) != 0;
      case DB2Type.SMALLINT: // SMALLINT
        return convertToShort(data, rowOffset) != 0;
      case DB2Type.FLOATINGPOINT:
        return convertToDouble(data, rowOffset) != 0;
      case DB2Type.VARBINARY: // VARBINARY
        int varblen = Conv.byteArrayToShort(data, offset);
        for (int i=offset+2; i<offset+2+varblen; ++i)
        {
          if (data[i] != 0) return true;
        }
        return false;
      case DB2Type.BINARY: // BINARY
        for (int i=offset; i<offset+length_; ++i)
        {
          if (data[i] != 0) return true;
        }
        return false;
      case DB2Type.DECFLOAT:
      { String stringValue;
        if (length_ == 8) {
          stringValue =  Conv.decfloat16ByteArrayToString(data, offset);
        } else if (length_ == 16) {
          stringValue =  Conv.decfloat34ByteArrayToString(data, offset);
        } else {
          throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown DECFLOAT length= "+length_);
        }
        Double d = Double.parseDouble(stringValue);
        if (d.doubleValue() != 0.0) return true;
        return false;
      }
      case DB2Type.DATE: // DATE
      case DB2Type.TIME: // TIME
      case DB2Type.TIMESTAMP: // TIMESTAMP
      case DB2Type.CLOB: // LOB
      case DB2Type.BLOB_LOCATOR: //TODO BLOB locator
      case DB2Type.CLOB_LOCATOR: //TODO - CLOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
      case DB2Type.XML_LOCATOR:
        JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        return false;
      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_);
    }
  }

  // See http://download.oracle.com/javase/1.4.2/docs/guide/jdbc/getstart/mapping.html#1004791
  Object convertToObject(final byte[] data, final int rowOffset) throws SQLException
  {
    try {
    switch (type_ & 0xFFFE )
    {
      case DB2Type.DATE: // DATE
        return convertToDate(data, rowOffset, calendar_);
      case DB2Type.TIME: // TIME
        return convertToTime(data, rowOffset, calendar_);
      case DB2Type.TIMESTAMP: // TIMESTAMP
        return convertToTimestamp(data, rowOffset, calendar_);
//TODO      case SQLType.CLOB: // LOB
      case DB2Type.VARCHAR: // VARCHAR
      case DB2Type.LONGVARCHAR: // LONG VARCHAR
      case DB2Type.CHAR: // CHAR
      case DB2Type.VARGRAPHIC: // VARGRAPHIC
      case DB2Type.LONGVARGRAPHIC: // LONG VARGRAPHIC
      case DB2Type.GRAPHIC: // GRAPHIC
        return convertToString(data, rowOffset);
      case DB2Type.DECIMAL: // DECIMAL (packed decimal)
        return new BigDecimal(convertToString(data, rowOffset));
      case DB2Type.NUMERIC: // NUMERIC (zoned decimal)
        return new BigDecimal(convertToString(data, rowOffset));
      case DB2Type.BIGINT: // BIGINT
        return new Long(convertToLong(data, rowOffset));
      case DB2Type.INTEGER: // INTEGER
        return new Integer(convertToInt(data, rowOffset));
      case DB2Type.SMALLINT: // SMALLINT
        return new Integer(convertToShort(data, rowOffset));
      case DB2Type.VARBINARY: // VARBINARY
      {
        int offset = rowOffset+offset_;
        int varblen = Conv.byteArrayToShort(data, offset);
        byte[] vb = new byte[varblen];
        System.arraycopy(data, offset+2, vb, 0, varblen);
        return vb;
      }
      case DB2Type.BINARY: // BINARY
        byte[] b = new byte[length_];
        System.arraycopy(data, rowOffset+offset_, b, 0, length_);
        return b;
      case DB2Type.FLOATINGPOINT:
      {
        int offset = rowOffset+offset_;
        if (length_ == 4) {
          float floatValue =  Conv.byteArrayToFloat(data, offset);
          return new Float( floatValue) ;
        } else if (length_ == 8) {
          double doubleValue = Conv.byteArrayToDouble(data, offset);
          return new Double(doubleValue);
        }
      }
        break;
      case DB2Type.BLOB_LOCATOR: //TODO BLOB locator
      case DB2Type.CLOB_LOCATOR: //TODO - CLOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
      case DB2Type.XML_LOCATOR:
    	  throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Type "+type_+" not fully supported");
      case DB2Type.DECFLOAT:

        if (length_ == 8) {
          return new BigDecimal(Conv.decfloat16ByteArrayToString(data, rowOffset));
        } else if (length_ == 16) {
          return new BigDecimal(Conv.decfloat34ByteArrayToString(data, rowOffset));
        } else {
          throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown DECFLOAT length= "+length_);
        }
      case DB2Type.DATALINK:
      {
        String dlValue = convertToString(data, rowOffset);
        try {
          return new java.net.URL(dlValue);
        } catch (MalformedURLException e) {
          SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          sqlex.initCause(e);
          throw sqlex;
        }

      }
      default:
        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_);
    }
    } catch (NumberFormatException nfe) {
      SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
      sqlex.initCause(nfe);
      throw sqlex;
    }
    return null;
  }

  Blob convertToBlob(final byte[] data, final int rowOffset, JDBCConnection conn) throws SQLException
  {
    int offset = rowOffset+offset_;
    switch (type_ & 0xFFFE)
    {
      case DB2Type.CLOB: // LOB
        int len = Conv.byteArrayToInt(data, offset);
        return new JDBCBlob(data, offset+4, len);
      case DB2Type.BLOB_LOCATOR: //TODO BLOB locator
      case DB2Type.CLOB_LOCATOR: //TODO - CLOB locator
      case DB2Type.DBCLOB_LOCATOR: // DBCLOB locator
      case DB2Type.XML_LOCATOR:

        int locatorHandle = Conv.byteArrayToInt(data, offset);
        DatabaseRetrieveLOBDataAttributes a = new DatabaseRequestAttributes();
        a.setLOBLocatorHandle(locatorHandle);
        JDBCBlobLocator locator = new JDBCBlobLocator(conn.getDatabaseConnection(), a);
        return locator;
      default:
        return new JDBCBlob(data, offset, length_);
//      default:
//        throw JDBCError.getSQLException(JDBCError.EXC_INTERNAL,"Unknown database type: "+type_);
    }
  }

  //
  // Make sure that the decimal number has the correct scale.
  //
  String formatDecimal(String input, int precision, int scale) throws SQLException {
	  if (scale == 0) {
		  int dotIndex = input.indexOf('.');
		  if (dotIndex > 0) {
			  input = input .substring(0,dotIndex);
		  }
		  int inputLength = input.length();
		  if (input.charAt(0) == '-') {
			  inputLength--;
		  }
		  if (inputLength > precision) {
				throw new DataTruncation(index_,  /* index */
			              parameter_, /* was parameter truncated */
			              false, /* read */
			              input.length(), /* dataSize */
			              precision); /* transferSize */
		  }
	  } else {
		  int dotIndex = input.indexOf('.');
		  if (dotIndex > 0) {
			  int scaleDigits = input.length() - dotIndex - 1;
			  if (scaleDigits < scale) {
				  StringBuffer sb = new StringBuffer(input);
				  while (scaleDigits < scale) {
					  sb.append('0');
					  scaleDigits++;
				  }
				  input = sb.toString();
			  } else if (scaleDigits > scale) {
				  // Truncation warning should go here
				  input = input.substring(0, dotIndex+1+scale);
			  }
		  } else {
			 // add the scale
			 StringBuffer sb = new StringBuffer(input);
			 sb.append('.');
			 for (int i = 0; i < scale; i++) {
				 sb.append('0');
			 }
			 input = sb.toString();
		  }
		  // check for truncation
		  int inputLength = input.length();
		  if (input.charAt(0) == '-') {
			  inputLength--;
		  }
		  if (inputLength > precision + 1) {
				throw new DataTruncation(index_,  /* index */
			              parameter_, /* was parameter truncated */
			              false, /* read */
			              input.length()-1, /* dataSize */
			              precision); /* transferSize */
		  }
	  }
	  return input;
  }

public int isNullable() {
	if ((type_ & 0x1) != 0) {
		return ResultSetMetaData.columnNullable;
	} else {
		return ResultSetMetaData.columnNoNulls;
	}
}

/*
 * Returns an instance of a Gregorian calendar to be used to set
 * Date values.   This is needed because the server uses the Gregorian calendar.
 * For some locales, the calendar returned by Calendar.getInstance is not usable.
 * For example, in the THAI local, a java.util.BuddhistCalendar is returned.
 */
public static Calendar getGregorianInstance() {
  Calendar returnCalendar = Calendar.getInstance();
  boolean isGregorian = (returnCalendar  instanceof GregorianCalendar);
  boolean isBuddhist = false;
  try {
      isBuddhist  = (returnCalendar  instanceof BuddhistCalendar);
  } catch (Throwable ncdfe) {
    // Just ignore if any exception occurs.
    // Possible exceptions (from Javadoc) are:
    // java.lang.NoClassDefFoundError
    // java.security.AccessControlException (if sun.util classes cannot be used)
  }

  if (isGregorian && (! isBuddhist)) {
     // Calendar is gregorian, but not buddhist
     return returnCalendar;
  } else {
    // Create a new gregorianCalendar for the current timezone and locale
    Calendar gregorianCalendar = new GregorianCalendar();
    return gregorianCalendar;
  }
}

  Clob convertToClob(final byte[] data, final int rowOffset, JDBCConnection conn) throws SQLException
  {
    int offset = rowOffset+offset_;
    switch (type_)
    {
      case 404: // BLOB
      case 405:
      case 408: // LOB
      case 409:
        int len = Conv.byteArrayToInt(data, offset);
        return new JDBCClob(data, offset+4, len, ccsid_);
      case 960: //TODO BLOB locator
      case 961:
      case 964: //TODO - CLOB locator
      case 965:
        int locatorHandle = Conv.byteArrayToInt(data, offset);
        DatabaseRetrieveLOBDataAttributes a = new DatabaseRequestAttributes();
        a.setLOBLocatorHandle(locatorHandle);
//TODO        JDBCClobLocator locator = new JDBCClobLocator(conn.getDatabaseConnection(), a);
//        return locator;
        return null;
      default:
        return new JDBCClob(data, offset, length_, ccsid_);
//      default:
//        throw new SQLException("Unknown database type: "+type_);
    }
  }

}





