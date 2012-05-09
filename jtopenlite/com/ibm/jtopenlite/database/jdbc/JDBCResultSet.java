///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCResultSet.java
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
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * Result sets created by this JDBC driver are forward-only and read-only.
**/
public class JDBCResultSet implements ResultSet, DatabaseFetchCallback
{
  private JDBCStatement statement_;
  private JDBCResultSetMetaData md_;
  private String stName_;
  private String cursorName_;

  private int fetchSize_;
  private boolean closed_;

  private final DataCache dataCache_ = new DataCache();

  private int currentRow_ = 0;
  private boolean lastNull_ = false;

  private byte[] tempDataBuffer_;

  protected boolean isMetadataResultSet_ = false;

  public JDBCResultSet(JDBCStatement statement, JDBCResultSetMetaData md, String statementName, String cursorName, int fetchSize)
  {
    statement_ = statement;
    md_ = md;
    stName_ = statementName;
    cursorName_ = cursorName;
    fetchSize_ = fetchSize;
  }

  public byte[] getTempDataBuffer(int rowSize)
  {
    if (tempDataBuffer_ == null || tempDataBuffer_.length < rowSize)
    {
      tempDataBuffer_ = new byte[rowSize];
    }
    return tempDataBuffer_;
  }

  public void newResultData(int rowCount, int columnCount, int rowSize)
  {
    dataCache_.init(rowCount, columnCount, rowSize);
    if (tempDataBuffer_ == null || tempDataBuffer_.length < rowSize)
    {
      tempDataBuffer_ = new byte[rowSize];
    }
  }

  public void newIndicator(int row, int column, byte[] tempIndicatorData)
  {
    int i = Conv.byteArrayToShort(tempIndicatorData, 0);
    boolean isNull = i == -1 ;
    dataCache_.setNull(row, column, isNull);
  }

  public void newRowData(int row, byte[] tempData)
  {
    dataCache_.setRow(row, tempData);
  }

  ////////////////////////////
  //
  // ResultSet methods
  //
  ////////////////////////////


  /**
   * Not implemented.
  **/
  public boolean absolute(int row) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void afterLast() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Positiong cursor not implemented.
  **/
  public void beforeFirst() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void cancelRowUpdates() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void clearWarnings() throws SQLException
  {
      // No errors from unimplemented clear warnings
  }

  public void close() throws SQLException
  {
    try
    {
      if (cursorName_ == null) return;
      if (!closed_)
      {
        DatabaseConnection conn = statement_.getDatabaseConnection();
        DatabaseCloseCursorAttributes cca = statement_.getRequestAttributes();
        cca.setCursorName(cursorName_);
        try
        {
          conn.setCurrentRequestParameterBlockID(statement_.rpbID_);
          conn.closeCursor(cca);
          //
          // 04/11/2012 -- Not sure why this is reset.  We want to keep the default RPM with
          //            -- the cursor name and statement name
          //
          // DatabaseCreateRequestParameterBlockAttributes rpba = statement_.getRequestAttributes();
          // conn.resetRequestParameterBlock(rpba, statement_.rpbID_);
          //
        }
        catch (IOException io)
        {
          throw JDBCConnection.convertException(io, statement_.getLastSQLCode(), statement_.getLastSQLState());
        }
        closed_ = true;              /* Mark as closed before calling statement close */
        if (isMetadataResultSet_) {
        	statement_.close();
        }
      }
    }
    finally
    {
      closed_ = true;
      statement_ = null;
      md_ = null;
    }
  }

  /**
   * Not implemented.
  **/
  public void deleteRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  public int findColumn(String columnName) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    int columnIndex = md_.getColumnIndex(columnName)+1;
    if (columnIndex <= 0 ) {
	     throw new SQLException("Column not found", "42703", -206);
    }
    return columnIndex;
  }

  /**
   * Not implemented.
  **/
  public boolean first() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   *
  **/
  public int getConcurrency() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    // We currently only support READONLY cursors
    return ResultSet.CONCUR_READ_ONLY;
  }

  public String getCursorName() throws SQLException
  {

    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return cursorName_;
  }

  public int getFetchDirection() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return ResultSet.FETCH_FORWARD;
  }

  public int getFetchSize() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return fetchSize_;
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return md_;
  }

  public int getRow() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return currentRow_;
  }

  public Statement getStatement() throws SQLException
  {
    if (isMetadataResultSet_) {
      // Do not expose statement objects for result set metadata
      return null;
    }
    return statement_;
  }

  public int getType() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return ResultSet.TYPE_FORWARD_ONLY;
  }

  /**
   * For the jtopenlite driver, no warnings will ever be reported
  **/
  public SQLWarning getWarnings() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return null;
  }

  /**
   * Not implemented.
  **/
  public void insertRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean isAfterLast() throws SQLException
  {
    throw new NotImplementedException();
  }

  public boolean isBeforeFirst() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return currentRow_ <= 0;
  }

  public boolean isFirst() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return currentRow_ == 1;
  }

  /**
   * Not implemented.
  **/
  public boolean isLast() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean last() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void moveToCurrentRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void moveToInsertRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  public boolean next() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);


    // If our data cache is empty, or if our cache pointer is at the end.
    if (dataCache_.nextRow() >= dataCache_.getNumRows())
    {
      if (!fetch() || dataCache_.nextRow() >= dataCache_.getNumRows())
      {
        return false;
      }
    }

    ++currentRow_;
    return true;
  }

  private boolean fetch() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (cursorName_ == null) return false;

    Message lastWarning = ((JDBCConnection)statement_.getConnection()).getLastWarningMessage();
    if (lastWarning != null)
    {
      if (lastWarning.getID().equals("SQL0100"))
      {
        // Row not found.
        return false;
      }
    }

    DatabaseConnection conn = statement_.getDatabaseConnection();
      DatabaseFetchAttributes fa = statement_.getRequestAttributes();
      fa.setCursorName(cursorName_);
      fa.setFetchScrollOption(0,0); // Next.
      // TODO:  Get variable field compression working
      fa.setVariableFieldCompression(0xE8);
      if (fetchSize_ > 0)
      {
        fa.setBlockingFactor(fetchSize_);
      }
      else
      {
        fa.setFetchBufferSize(256*1024);
      }
      try
      {
        conn.setCurrentRequestParameterBlockID(statement_.rpbID_);
        conn.fetch(fa, this);
        return true;
      }
      catch (IOException io)
      {
        // io.printStackTrace();
        throw JDBCConnection.convertException(io, statement_.getLastSQLCode(), statement_.getLastSQLState());
      }
  }

  /**
   * Not implemented.
  **/
  public boolean previous() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void refreshRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  public boolean relative(int rows) throws SQLException
  {
    if (rows < 0) throw new SQLException("Result set is forward only.");
    for (int i=0; i<rows; ++i)
    {
      if (!next())
      {
        return false;
  }
    }
    return true;
  }

  /**
   * Not implemented.
  **/
  public boolean rowDeleted() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean rowInserted() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean rowUpdated() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setFetchDirection(int direction) throws SQLException
  {
    throw new NotImplementedException();
  }

  public void setFetchSize(int rows) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    if (rows < 0) throw new SQLException("Bad value for fetch size: "+rows);
    fetchSize_ = rows;
  }

  /**
   * Not implemented.
  **/
  public void updateRow() throws SQLException
  {
    throw new NotImplementedException();
  }

  public boolean wasNull() throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return lastNull_;
  }


  ////////////////////////////
  //
  // ResultSet data getter methods
  //
  ////////////////////////////

  /**
   * Not implemented.
  **/
  public Array getArray(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public Array getArray(String colName) throws SQLException
  {
    return getArray(findColumn(colName));
  }

  /**
   * getAsciiStream is implemented as simple wrapper around getString
  **/
  public InputStream getAsciiStream(int i) throws SQLException
  {
      try
      {
	  String s = getString (i);
	  if (s == null) return null;
	  return new ByteArrayInputStream (s.getBytes ("ISO8859_1"));
      }
      catch (UnsupportedEncodingException e)
      {
        SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        sqlex.initCause(e);
        throw sqlex;

      }

    }

  /**
   * getAsciiStream is implemented as simple wrapper around getString
  **/
  public InputStream getAsciiStream(String colName) throws SQLException
  {
    return getAsciiStream(findColumn(colName));

  }

  /**
   * Implemented as simple wrapper around getString
  **/
  public BigDecimal getBigDecimal(int i) throws SQLException
  {
      try
      {
	  String s = getString(i);
	  if (s == null) return null;
         return new BigDecimal (s.trim ());
        }
      catch (NumberFormatException e)
      {
          SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          sqlex.initCause(e);
          throw sqlex;
      }
  }

  /**
   * Implemented as simple wrapper around getString
  **/
  public BigDecimal getBigDecimal(String colName) throws SQLException
  {
   return getBigDecimal(findColumn(colName));

  }

  /**
   * Implemented as simple wrapper around getString
   * @deprecated
  **/
  public BigDecimal getBigDecimal(int i, int scale) throws SQLException
  {
      try
      {
        if (scale < 0) {
          SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
          throw sqlex;

        }
	  String s = getString(i);
	  if (s == null) return null;

         BigDecimal value = new BigDecimal (s.trim ());
	 return value.setScale(scale, BigDecimal.ROUND_DOWN);
        }
      catch (NumberFormatException e)
      {
        SQLException sqlex = JDBCError.getSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
        sqlex.initCause(e);
        throw sqlex;
      }

  }

  /**
   * Implemented as simple wrapper around getString
   * @deprecated
  **/
  public BigDecimal getBigDecimal(String colName, int scale) throws SQLException
  {
    return getBigDecimal(findColumn(colName), scale);

  }

  /**
   * Not implemented.
  **/
  public InputStream getBinaryStream(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public InputStream getBinaryStream(String colName) throws SQLException
  {
    return getBinaryStream(findColumn(colName));
  }

  public Blob getBlob(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToBlob(dataCache_.getData(), dataCache_.getRowOffset(), (JDBCConnection)statement_.getConnection());
  }

  public Blob getBlob(String colName) throws SQLException
  {
    return getBlob(findColumn(colName));
  }

  public boolean getBoolean(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return false;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToBoolean(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public boolean getBoolean(String colName) throws SQLException
  {
    return getBoolean(findColumn(colName));
  }

  public byte getByte(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToByte(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public byte getByte(String colName) throws SQLException
  {
    return getByte(findColumn(colName));
  }

  public byte[] getBytes(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToOutputBytes(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public byte[] getBytes(String colName) throws SQLException
  {
    return getBytes(findColumn(colName));
  }

  /**
   * getCharacterStream is a simple wrapper around getString
  **/
  public Reader getCharacterStream(int i) throws SQLException
  {
	  String s = getString(i);
	  if (s == null) return null;

    return new java.io.StringReader(s);
  }

  /**
   * getCharacterStream is a simple wrapper around getString
  **/
  public Reader getCharacterStream(String colName) throws SQLException
  {
    return getCharacterStream(findColumn(colName));
  }

  /**
   * Not implemented.
  **/
  public Clob getClob(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public Clob getClob(String colName) throws SQLException
  {
    throw new NotImplementedException();
  }

  public Date getDate(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return getDate(i, ((JDBCConnection)statement_.getConnection()).getCalendar());
  }

  public Date getDate(String colName) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return getDate(colName, ((JDBCConnection)statement_.getConnection()).getCalendar());
  }

  public Date getDate(int i, Calendar cal) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (cal == null) JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);

    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToDate(dataCache_.getData(), dataCache_.getRowOffset(),
                             cal);
  }

  public Date getDate(String colName, Calendar cal) throws SQLException
  {
    return getDate(findColumn(colName), cal);
  }


  public double getDouble(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToDouble(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public double getDouble(String colName) throws SQLException
  {
    return getDouble(findColumn(colName));
  }

  public float getFloat(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToFloat(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public float getFloat(String colName) throws SQLException
  {
    return getFloat(findColumn(colName));
  }

  public int getInt(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToInt(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public int getInt(String colName) throws SQLException
  {
    return getInt(findColumn(colName));
  }

  public long getLong(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToLong(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public long getLong(String colName) throws SQLException
  {
    return getLong(findColumn(colName));
  }

  public Object getObject(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToObject(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public Object getObject(String colName) throws SQLException
  {
    return getObject(findColumn(colName));
  }

//  public Object getObject(int i, Map<String,Class<?>> map) throws SQLException
  /**
   * Not implemented.
  **/
  public Object getObject(int i, Map map) throws SQLException
  {
    throw new NotImplementedException();
  }


  /**
   * Not implemented.
  **/
  public Ref getRef(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public Ref getRef(String colName) throws SQLException
  {
    throw new NotImplementedException();
  }

  public short getShort(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return 0;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToShort(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public short getShort(String colName) throws SQLException
  {
    return getShort(findColumn(colName));
  }

  public String getString(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
    }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToString(dataCache_.getData(), dataCache_.getRowOffset());
  }

  public String getString(String colName) throws SQLException
  {
    return getString(findColumn(colName));
  }

  public Time getTime(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return getTime(i, ((JDBCConnection)statement_.getConnection()).getCalendar());
  }

  public Time getTime(String colName) throws SQLException
  {
    return getTime(findColumn(colName));
  }

  public Time getTime(int i, Calendar cal) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (cal == null) JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToTime(dataCache_.getData(), dataCache_.getRowOffset(),
                             cal);
  }

  public Time getTime(String colName, Calendar cal) throws SQLException
  {
    return getTime(findColumn(colName), cal);
  }

  public Timestamp getTimestamp(int i) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return getTimestamp(i, ((JDBCConnection)statement_.getConnection()).getCalendar());
  }

  public Timestamp getTimestamp(String colName) throws SQLException
  {
    return getTimestamp(findColumn(colName));
  }

  public Timestamp getTimestamp(int i, Calendar cal) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (cal == null) JDBCError.throwSQLException(JDBCError.EXC_DATA_TYPE_MISMATCH);
    if (dataCache_.isNull(i-1))
    {
      lastNull_ = true;
      return null;
  }
    lastNull_ = false;
    Column col = md_.getColumn(i-1);
    return col.convertToTimestamp(dataCache_.getData(), dataCache_.getRowOffset(),
                                  cal);
  }

  public Timestamp getTimestamp(String colName, Calendar cal) throws SQLException
  {
    return getTimestamp(findColumn(colName), cal);
  }

  /**
   * @deprecated
  **/
  public InputStream getUnicodeStream(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * @deprecated
  **/
  public InputStream getUnicodeStream(String colName) throws SQLException
  {
    throw new NotImplementedException();
  }

  public URL getURL(int i) throws SQLException
  {
    String s = getString(i);
    if (s == null) return null;
    try
    {
      return new URL(s);
    }
    catch (MalformedURLException e)
    {
      SQLException sql = new SQLException("Data conversion error");
      sql.initCause(e);
      throw sql;
    }
  }

  public URL getURL(String colName) throws SQLException
  {
    return getURL(findColumn(colName));
  }


  /**
   * Not implemented.
  **/
  public void updateArray(int i, Array x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateArray(String colName, Array x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateAsciiStream(int i, InputStream x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateAsciiStream(String colName, InputStream x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBigDecimal(int i, BigDecimal x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBigDecimal(String colName, BigDecimal x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBinaryStream(int i, InputStream x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBinaryStream(String colName, InputStream x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBlob(int i, Blob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBlob(String colName, Blob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBoolean(int i, boolean x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBoolean(String colName, boolean x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateByte(int i, byte x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateByte(String colName, byte x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBytes(int i, byte[] x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateBytes(String colName, byte[] x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateCharacterStream(int i, Reader x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateCharacterStream(String colName, Reader x, int length) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateClob(int i, Clob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateClob(String colName, Clob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateDate(int i, Date x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateDate(String colName, Date x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateDouble(int i, double x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateDouble(String colName, double x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateFloat(int i, float x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateFloat(String colName, float x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateInt(int i, int x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateInt(String colName, int x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateLong(int i, long x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateLong(String colName, long x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateNull(int i) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateNull(String colName) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateObject(int i, Object x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateObject(String colName, Object x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateObject(int i, Object x, int scale) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateObject(String colName, Object x, int scale) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateRef(int i, Ref x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateRef(String colName, Ref x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateShort(int i, short x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateShort(String colName, short x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateString(int i, String x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateString(String colName, String x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateTime(int i, Time x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateTime(String colName, Time x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateTimestamp(int i, Timestamp x) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void updateTimestamp(String colName, Timestamp x) throws SQLException
  {
    throw new NotImplementedException();
  }


  public int getHoldability() throws SQLException {
    // Same as the statement, exception for stored procedure result sets
    return statement_.getResultSetHoldability();
  }


  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    return getCharacterStream(columnIndex);
  }


  public Reader getNCharacterStream(String columnName) throws SQLException {
    return getNCharacterStream(findColumn(columnName));
  }




  public String getNString(int columnIndex) throws SQLException {
    return getString(columnIndex);
  }

  public String getNString(String columnName ) throws SQLException {
    return getNString(findColumn(columnName));
  }






  public boolean isClosed() throws SQLException {
    return closed_;
  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
    throw new NotImplementedException();
  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateAsciiStream(String arg0, InputStream arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateAsciiStream(int arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateAsciiStream(String arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateBinaryStream(int arg0, InputStream arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateBinaryStream(String arg0, InputStream arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateBinaryStream(int arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateBinaryStream(String arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateBlob(int arg0, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateBlob(String arg0, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateBlob(int arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateBlob(String arg0, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateCharacterStream(String arg0, Reader arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateCharacterStream(int arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateCharacterStream(String arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateClob(int arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateClob(String arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateClob(String arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNCharacterStream(String arg0, Reader arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateNCharacterStream(int arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateNCharacterStream(String arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }




  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNClob(int arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNClob(String arg0, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
   * @param arg2
  **/
  public void updateNClob(String arg0, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNString(int arg0, String arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * Not implemented.
   * @param arg0
   * @param arg1
  **/
  public void updateNString(String arg0, String arg1) throws SQLException {
    throw new NotImplementedException();

  }





  /**
   * Not implemented.
   * @param arg0
  **/
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    throw new NotImplementedException();
  }


  /**
   * Not implemented.
   * @param arg0
  **/
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    throw new NotImplementedException();
  }






  public Object getObject(String columnLabel, Map map)
      throws SQLException {
    return getObject(findColumn(columnLabel), map);
  }
}

