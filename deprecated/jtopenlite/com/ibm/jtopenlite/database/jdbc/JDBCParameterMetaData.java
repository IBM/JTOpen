///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCParameterMetaData.java
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

import java.sql.*;
import java.util.Calendar;

public class JDBCParameterMetaData implements ParameterMetaData, DatabaseParameterMarkerCallback
{
  private Column[] columns_;
  private int rowSize_;
  private JDBCStatement statement_;

  private final Calendar calendar_;

  public JDBCParameterMetaData(Calendar calendarUsedForConversions)
  {
    calendar_ = calendarUsedForConversions;
    statement_ = null;
  }

  void setStatement(JDBCStatement statement) {
      statement_ = statement;
  }


  public void parameterMarkerDescription(int numFields, int recordSize)
  {
    columns_ = new Column[numFields];
    for (int i=0; i<numFields; ++i)
    {
      columns_[i] = new Column(calendar_, i+1, true);
    }
    rowSize_ = recordSize;
  }

  public void parameterMarkerFieldDescription(int fieldIndex, int fieldType, int length, int scale, int precision, int ccsid, int parameterType, int joinRefPosition, int lobLocator, int lobMaxSize)
  {
    columns_[fieldIndex].setType(fieldType);
    columns_[fieldIndex].setLength(length);
    columns_[fieldIndex].setScale(scale);
    columns_[fieldIndex].setPrecision(precision);
    columns_[fieldIndex].setCCSID(ccsid);
  }

  public void parameterMarkerFieldName(int fieldIndex, String name)
  {
    columns_[fieldIndex].setName(name);
  }

  public void parameterMarkerUDTName(int fieldIndex, String name)
  {
  }

  int getRowSize()
  {
    return rowSize_;
  }

  Column getColumn(int fieldIndex) throws SQLException
  {
    if (columns_ == null || fieldIndex >= columns_.length || fieldIndex < 0 ) throw new SQLException("Descriptor index not valid.");
    return columns_[fieldIndex];
  }

  byte[] getExtendedSQLParameterMarkerDataFormat()
  {
    if (columns_ == null) return null;
    final int numFields = columns_.length;
    if (numFields == 0) return null;
    final int size = 16+(numFields*64);
    final byte[] data = new byte[size];
    Conv.intToByteArray(1, data, 0); // Consistency token.
    Conv.intToByteArray(numFields, data, 4);
    Conv.intToByteArray(rowSize_, data, 12);
    int offset = 16;
    for (int i=0; i<numFields; ++i)
    {
      final int recordSize = 64;
      final int type = columns_[i].getType();
      final int length = columns_[i].getLength();
      final int scale = columns_[i].getScale();
      final int precision = columns_[i].getPrecision();
      final int ccsid = columns_[i].getCCSID();
      Conv.shortToByteArray(recordSize, data, offset);
      Conv.shortToByteArray(type, data, offset+2);
      Conv.intToByteArray(length, data, offset+4);
      Conv.shortToByteArray(scale, data, offset+8);
      Conv.shortToByteArray(precision, data, offset+10);
      Conv.shortToByteArray(ccsid, data, offset+12);
      offset += 64;
    }
    return data;
  }

  /**
   * Not implemented.
  **/
  public String getParameterClassName(int param) throws SQLException
  {
    throw new NotImplementedException();
  }

  public int getParameterCount() throws SQLException
  {
      checkRequest();
    return columns_ == null ? 0 : columns_.length;
  }

  public int getParameterMode(int param) throws SQLException
  {
      checkRequest();
    return ParameterMetaData.parameterModeUnknown;
  }

  /**
   *
  **/
  public int getParameterType(int param) throws SQLException
  {
      checkRequest(param);
    return columns_[param-1].getSQLType();
  }

  public String getParameterTypeName(int param) throws SQLException
  {
      checkRequest(param);
    return columns_[param-1].getSQLTypeName();
  }

  public int getPrecision(int param) throws SQLException
  {
      checkRequest(param);

      return JDBCColumnMetaData.getPrecision(columns_[param-1]);


  }

  public int getScale(int param) throws SQLException
  {
    checkRequest(param);
    return JDBCColumnMetaData.getScale(columns_[param-1]);
  }

  /**
   * Not implemented.
  **/
  public int isNullable(int param) throws SQLException
  {
      checkRequest(param);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean isSigned(int param) throws SQLException
  {
      checkRequest(param);
    throw new NotImplementedException();
  }

  private void checkRequest(int param) throws SQLException {
      checkRequest();

      if (columns_ == null || (param < 1) || (param > columns_.length))
            throw JDBCError.getSQLException(JDBCError.EXC_DESCRIPTOR_INDEX_INVALID);

  }
  private void checkRequest() throws SQLException {
      if (statement_.isClosed()) {
	  throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
      }
  }
}
