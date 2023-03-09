///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLInteger.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
/* ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */
import java.sql.SQLException;
/*ifdef JDBC40 
import java.sql.SQLXML;
endif */
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLBoolean extends SQLDataBase {
  static final String copyright = "Copyright (C) 2020-2020 International Business Machines Corporation and others.";

  // Private data.
  private boolean value_;
  private int vrm_;

  SQLBoolean(int vrm, SQLConversionSettings settings) {
    this(0, vrm, settings);
  }

  SQLBoolean(int scale, int vrm, SQLConversionSettings settings) {
    super(settings);

    value_ = false;
    vrm_ = vrm;
  }

  public Object clone() {
    return new SQLBoolean(vrm_, settings_);
  }

  // ---------------------------------------------------------//
  // //
  // CONVERSION TO AND FROM RAW BYTES //
  // //
  // ---------------------------------------------------------//

  public void convertFromRawBytes(byte[] rawBytes, int offset,
      ConvTable ccsidConverter, boolean ignoreConversionErrors)
      throws SQLException {
    byte binaryValue = rawBytes[offset];
    if (binaryValue == (byte) 0xF0) {
      value_ = false;
    } else {
      value_ = true;
    }
  }

  public void convertToRawBytes(byte[] rawBytes, int offset,
      ConvTable ccsidConverter) throws SQLException {
    if (value_) {
      rawBytes[offset] = (byte) 0xF1;
    } else {
      rawBytes[offset] = (byte) 0xF0;
    }
  }

  // ---------------------------------------------------------//
  // //
  // SET METHODS //
  // //
  // ---------------------------------------------------------//
  public static Boolean getBooleanObject(Object caller, String string) throws SQLException { 
    if (string == null) return null; 
    return new Boolean(getBoolean(caller,string)); 
  }
  
  public static boolean getBoolean(Object caller, String string)
      throws SQLException {
    string = string.toUpperCase().trim();
    boolean value = false;
    if ("TRUE".equals(string) || "T".equals(string) || "Y".equals(string)
        || "YES".equals(string) || "ON".equals(string)) {
      value = true;
    } else if ("FALSE".equals(string) || "F".equals(string)
        || "N".equals(string) || "NO".equals(string) || "OFF".equals(string)) {

      value = false;
    } else {
      try {
        double doubleValue = Double.parseDouble(string);
        if (doubleValue == 0.0) {
          if (string.charAt(0) == '0') {
            value = false;
          } else {
            if (JDTrace.isTraceOn()) {
              JDTrace.logInformation(caller,
                  "Unable to assign string (" + string + ")");
            }
            JDError.throwSQLException(caller, JDError.EXC_DATA_TYPE_MISMATCH);
          }
        } else {
          value = true;
        }
      } catch (NumberFormatException nfe) {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(caller,
              "Unable to assign string (" + string + ")");
        }
        JDError.throwSQLException(caller, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
      }
    }
    return value;
  }

  public static Boolean getBooleanObject(Object caller, Number object) throws SQLException { 
    if (object == null) return null; 
    return new Boolean(getBoolean(caller,object)); 
  }

  public static boolean getBoolean(Object caller, Number object) {
     boolean value; 
      double doubleValue = ((Number) object).doubleValue();
      if (doubleValue == 0.0) {
        value = false;
      } else {
        value = true;
      }
      return value; 
  }

  
    public static boolean getBoolean(Object caller, long longValue) {
     boolean value; 
      if (longValue == 0L) {
        value = false;
      } else {
        value = true;
      }
      return value; 
  }

  
  public void set(Object object, Calendar calendar, int scale)
      throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false;

    if (object instanceof String) {
      value_ = getBoolean(this, (String) object);
    }

    else if (object instanceof Number) {
      value_ = getBoolean(this, (Number) object); 
    } else if (object instanceof Boolean)
      value_ = (((Boolean) object).booleanValue());
    else {
      if (JDTrace.isTraceOn()) {
        if (object == null) {
          JDTrace.logInformation(this, "Unable to assign null object");
        } else {
          JDTrace.logInformation(this, "Unable to assign object(" + object
              + ") of class(" + object.getClass().toString() + ")");
        }
      }

      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }

  }


  public void set(int value) {
    value_ = (value == 0);
  }

  // ---------------------------------------------------------//
  // //
  // DESCRIPTION OF SQL TYPE //
  // //
  // ---------------------------------------------------------//

  public int getSQLType() {
    return SQLData.BOOLEAN;
  }

  public String getCreateParameters() {
    return null;
  }

  public int getDisplaySize() {
    return 1;
  }

  // @F1A JDBC 3.0
  public String getJavaClassName() {
    return "java.lang.Boolean";
  }

  public String getLiteralPrefix() {
    return null;
  }

  public String getLiteralSuffix() {
    return null;
  }

  public String getLocalName() {
    return "BOOLEAN";
  }

  public int getMaximumPrecision() {
    return 1;
  }

  public int getMaximumScale() {
    return 0;
  }

  public int getMinimumScale() {
    return 0;
  }

  public int getNativeType() {
    return 2436;
  }

  public int getPrecision() {
    return 1;
  }

  public int getRadix() {
    return 0;
  }

  public int getScale() {
    return 0;
  }

  public int getType() {
    return java.sql.Types.BOOLEAN;
  }

  public String getTypeName() {
    return "BOOLEAN";
  }

  public boolean isSigned() {
    return false;
  }

  public boolean isText() {
    return false;
  }

  public int getActualSize() {
    return 1; // @D0C
  }

  public int getTruncated() {
    return truncated_;
  }

  public boolean getOutOfBounds() {
    return outOfBounds_;
  }

  // ---------------------------------------------------------//
  // //
  // CONVERSIONS TO JAVA TYPES //
  // //
  // ---------------------------------------------------------//

  public BigDecimal getBigDecimal(int scale) throws SQLException {
    BigDecimal bigDecimalValue;

    if (value_) {
      bigDecimalValue = new BigDecimal(1);
    } else {
      bigDecimalValue = new BigDecimal(0);
    }
    if (scale > 0) {
      bigDecimalValue = bigDecimalValue.setScale(scale);
    }
    return bigDecimalValue;
  }

  public InputStream getBinaryStream() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Blob getBlob() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }
  
    public Clob getClob() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }


  public boolean getBoolean() throws SQLException {
    return value_;
  }

  public byte getByte() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  public byte[] getBytes() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Date getDate(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public double getDouble() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  public float getFloat() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  public int getInt() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  public long getLong() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  public Object getObject() throws SQLException {
    return new Boolean(value_);
  }

  public short getShort() throws SQLException {
    if (value_) {
      return 1;
    } else {
      return 0;
    }
  }

  // getString returns 1 or 0 for boolean values.  This matches the behavior of the jcc
  // JDBC driver.  It also consistent with the ResultSet getBoolean() method which returns true for '1'
  // and false of '0'
  public String getString() throws SQLException {
    if (value_) {
      return "1";
    } else {
      return "0";
    }
  }

  public Time getTime(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Timestamp getTimestamp(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  // @pda jdbc40
  public String getNString() throws SQLException {
    return getString();
  }

  /* ifdef JDBC40 
  public RowId getRowId() throws SQLException
  {
      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
      return null;
  }
  
  
    public NClob getNClob() throws SQLException
  {
      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
      return null;
  }

endif */     

  /* ifdef JDBC40 
  public SQLXML getSQLXML() throws SQLException
  {
      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
      return null;
  }
endif */     
  // @array

  // @array

  public void saveValue() {
    savedValue_ = new Boolean(value_);
  }

}
