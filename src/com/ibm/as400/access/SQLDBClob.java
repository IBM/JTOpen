///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDBClob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLDBClob implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private static final String     default_ = ""; // @B3A

  private int                     length_;                    // Length of string, in characters.     @E3C
  private int                     maxLength_;                 // Max length of field, in bytes.       @E3C
  private SQLConversionSettings   settings_;
  private int                     truncated_;
  private String                  value_;

  // Note: maxLength is in bytes not counting 2 for LL.
  //
  SQLDBClob(int maxLength, SQLConversionSettings settings)
  {
    length_         = 0;
    maxLength_      = maxLength;
    settings_       = settings;
    truncated_      = 0;
    value_          = default_;
  }


  public Object clone()
  {
    return new SQLDBClob(maxLength_, settings_);
  }


  //---------------------------------------------------------//
  //                                                         //
  // CONVERSION TO AND FROM RAW BYTES                        //
  //                                                         //
  //---------------------------------------------------------//

  public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    length_ = BinaryConverter.byteArrayToInt(rawBytes, offset);

    int bidiStringType = settings_.getBidiStringType();
    
    // if bidiStringType is not set by user, use ccsid to get value
    if (bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

    // If the field is DBCLOB, length_ contains the number
    // of characters in the string, while the converter is expecting
    // the number of bytes. Thus, we need to multiply length_ by 2.
    value_ = ccsidConverter.byteArrayToString(rawBytes, offset + 4, length_*2, bidiStringType);
  }

  public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    BinaryConverter.intToByteArray(length_, rawBytes, offset);
    try
    {
      int bidiStringType = settings_.getBidiStringType();
      if (bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

      ccsidConverter.stringToByteArray(value_, rawBytes, offset + 4, maxLength_, bidiStringType);
    }
    catch (Exception e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
    }
  }

  //---------------------------------------------------------//
  //                                                         //
  // SET METHODS                                             //
  //                                                         //
  //---------------------------------------------------------//

  public void set(Object object, Calendar calendar, int scale) throws SQLException
  {
    if (object instanceof String)
    {
      value_ = (String) object;
    }
    else if (object instanceof Clob)
    {
      Clob clob = (Clob) object;
      value_ = clob.getSubString(1, (int) clob.length());
    }
    else
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }

    // Truncate if necessary.
    int valueLength = value_.length();
    if (valueLength > maxLength_)
    {
      value_ = value_.substring(0, maxLength_);
      truncated_ = valueLength - maxLength_;
    }
    else
    {
      truncated_ = 0;
    }

    length_ = value_.length();
  }

  //---------------------------------------------------------//
  //                                                         //
  // DESCRIPTION OF SQL TYPE                                 //
  //                                                         //
  //---------------------------------------------------------//

  public String getCreateParameters()
  {
    return AS400JDBCDriver.getResource("MAXLENGTH"); 
  }

  public int getDisplaySize()
  {
    return(maxLength_ / 2);
  }


  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCClob";
  }

  public String getLiteralPrefix()
  {
    return null;
  }

  public String getLiteralSuffix()
  {
    return null;
  }

  public String getLocalName()
  {
    return "DBCLOB"; 
  }

  public int getMaximumPrecision()
  {
    return 15728640;
  }

  public int getMaximumScale()
  {
    return 0;
  }

  public int getMinimumScale()
  {
    return 0;
  }

  public int getNativeType()
  {
    return 412;
  }

  public int getPrecision()
  {
    return maxLength_;
  }

  public int getRadix()
  {
    return 0;
  }

  public int getScale()
  {
    return 0;
  }

  public int getType()
  {
    return java.sql.Types.CLOB;
  }

  public String getTypeName()
  {
    return "DBCLOB";
  }

  public boolean isSigned()
  {
    return false;
  }

  public boolean isText()
  {
    return true;
  }

  //---------------------------------------------------------//
  //                                                         //
  // CONVERSIONS TO JAVA TYPES                               //
  //                                                         //
  //---------------------------------------------------------//

  public int getActualSize()
  {
    return value_.length();
  }

  public int getTruncated()
  {
    return truncated_;
  }

  public InputStream toAsciiStream()
  throws SQLException
  {
    try
    {
      return new ByteArrayInputStream(value_.getBytes("ISO8859_1"));
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
      return null;
    }
  }

  public BigDecimal toBigDecimal(int scale)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public InputStream toBinaryStream()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Blob toBlob()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public boolean toBoolean()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return false;
  }

  public byte toByte()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public byte[] toBytes()
  throws SQLException
  {
    return value_.getBytes();
  }

  public Reader toCharacterStream()
  throws SQLException
  {
    return new StringReader(value_);
  }

  public Clob toClob()
  throws SQLException
  {
    return new AS400JDBCClob(value_, maxLength_);
  }

  public Date toDate(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public double toDouble()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public float toFloat()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public int toInt()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public long toLong()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public Object toObject()
  {
    return new AS400JDBCClob(value_, maxLength_);
  }

  public short toShort()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public String toString()
  {
    return value_;     
  }

  public Time toTime(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Timestamp toTimestamp(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public InputStream toUnicodeStream()
  throws SQLException
  {
    try
    {
      return new ByteArrayInputStream(value_.getBytes("UnicodeBigUnmarked")); // @B1C @F2C
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);                    // @E2C
      return null;
    }
  }
}

