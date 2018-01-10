///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCPreparedStatement.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class JDBCPreparedStatement extends JDBCStatement implements PreparedStatement
{
  private JDBCParameterMetaData pmd_;
  private int descriptorHandle_;
  private boolean returnGeneratedKeys_;
  private int sqlStatementType_;
  private JDBCResultSetMetaData rsmd_;
  
  public JDBCPreparedStatement(JDBCConnection conn, String sql, Calendar calendar, String statementName, String cursorName, int rpbID) throws SQLException
  {
    super(conn, statementName, cursorName, rpbID);
    poolable_ = true;
    if (sql == null) {
	   	  JDBCError.throwSQLException(JDBCError.EXC_SYNTAX_ERROR);
	  	  return;
	}
    rsmd_ = null; 
    // Check for null statement

    DatabaseRequestAttributes dpa = new DatabaseRequestAttributes();
    //    dpa.setDescribeOption(0xD5); // Name/alias.
    //
    // Only set the statement name and cursor name in the RPB
    //
    sqlStatementType_ = JDBCStatement.getStatementType(sql);

    statementAttributes_.setSQLStatementType(sqlStatementType_);

      dpa.setSQLStatementType(sqlStatementType_);
      dpa.setPrepareOption(0); // Normal prepare.
      if (sqlStatementType_ == JDBCStatement.TYPE_SELECT ) {   // Only set for select statement
        dpa.setOpenAttributes(0x80); // Read only. Otherwise blocking doesn't work.
      }

    JDBCParameterMetaData pmd = new JDBCParameterMetaData(calendar);
    String catalog = conn_.getCatalog();
    // Getting the catalog may change the current rpb for the connection.  
    // Reset it after getting back.  Otherwise the call to 
    // prepareAndDescribe may fail with a PWS0001
    
    DatabaseConnection databaseConn = conn_.getDatabaseConnection();
    databaseConn.setCurrentRequestParameterBlockID(rpbID_);
    rsmd_ = new JDBCResultSetMetaData(conn.getDatabaseInfo().getServerCCSID(), calendar, catalog);

    dpa.setExtendedSQLStatementText(sql);
    conn.prepareAndDescribe(dpa, rsmd_, pmd);

    int handle = -1;
  	  // Only change the descriptor if there are parameters available
    DatabaseChangeDescriptorAttributes cda = (DatabaseChangeDescriptorAttributes)dpa;
      byte[] b = pmd.getExtendedSQLParameterMarkerDataFormat();
      cda.setExtendedSQLParameterMarkerDataFormat(b);
      handle = b == null ? -1 : conn.getNextDescriptorHandle();
      if (handle >= 0)
      {
        conn.changeDescriptor(cda, handle);
      }


    pmd_ = pmd;
    pmd_.setStatement(this);
    descriptorHandle_ = handle;
  }

  void setReturnGeneratedKeys(boolean b)
  {
    returnGeneratedKeys_ = b;
  }

  /**
   * Not implemented.
  **/
  public void addBatch() throws SQLException
  {
    throw new NotImplementedException();
  }

  public void clearParameters() throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    for (int i=0; i<pmd_.getParameterCount(); ++i)
    {
      Column col = pmd_.getColumn(i);
      col.clearValue();
    }
  }

  public boolean execute() throws SQLException
  {
	boolean callStatement = false;
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);


    //
    // If this is a select statement, use the executeQuery path
    //
    if (sqlStatementType_ == JDBCStatement.TYPE_SELECT ) {   // Only set for select statement
    	executeQuery();
    	return true;
    }

    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
    DatabaseExecuteAttributes dea = getRequestAttributes();
    // Not necessary -- part of RPB
    // dea.setPrepareStatementName(statementName_);

    // Flags set by normal toolbox
    ((DatabaseOpenAndDescribeAttributes)dea).setScrollableCursorFlag(0);
    ((DatabaseOpenAndDescribeAttributes)dea).setResultSetHoldabilityOption(0xe8); /* Y */
    ((DatabaseOpenAndDescribeAttributes)dea).setVariableFieldCompression(0xe8); /* Y */


    if (statementAttributes_.getSQLStatementType() == JDBCStatement.TYPE_CALL) {   /* if call */
      	dea.setSQLStatementType(JDBCStatement.TYPE_CALL);
      	callStatement = true;
      }



    if (pmd_.getParameterCount() > 0) {
       byte[] pmData = getExtendedParameterMarkerData();
       dea.setSQLExtendedParameterMarkerData(pmData);
      }


    try
    {

      conn.setCurrentRequestParameterBlockID(rpbID_);
      if (currentResultSet_ != null)
      {
        currentResultSet_.close();
        currentResultSet_ = null;
      }
//      conn.setSQLCommunicationsAreaCallback(returnGeneratedKeys_ ? this : null);
      updateCount_ = 0;
      if (descriptorHandle_ < 0)
      {
        conn.execute(dea);
      }
      else
      {
        conn.execute(dea, descriptorHandle_);
      }
      updateCount_ = lastUpdateCount_;

      // TODO:  Determine if result set is available.  If so, then call openDescribe
      if (callStatement && resultSetsCount_ > 0) {
    	    DatabaseOpenAndDescribeAttributes oada = getRequestAttributes();

    	    oada.setOpenAttributes(0x80);
    	    oada.setScrollableCursorFlag(0);
    	    oada.setVariableFieldCompression(0xe8);
    	    JDBCResultSetMetaData md = new JDBCResultSetMetaData(conn.getInfo().getServerCCSID(), conn_.getCalendar(), conn_.getCatalog());

    	    try
    	    {
    	      conn.setCurrentRequestParameterBlockID(rpbID_);
    	      if (currentResultSet_ != null)
    	      {
    	        currentResultSet_.close();
    	        currentResultSet_ = null;
    	      }
    	      if (descriptorHandle_ < 0)
    	      {
    	        conn.openAndDescribe(oada, md);
    	      }
    	      else
    	      {
    	        conn.openAndDescribe(oada, descriptorHandle_, md);
    	      }
    	    }
    	    catch (IOException io)
    	    {
    	      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    	    }

    	    currentResultSet_ = new JDBCResultSet(this, md, statementName_, cursorName_, fetchSize_);
    	    updateCount_ = -1;


      }


    }
    catch (IOException io)
    {
      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    }
    finally
    {
//      conn.setSQLCommunicationsAreaCallback(null);
    }
    return true;
  }

  public ResultSet executeQuery() throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    switch (sqlStatementType_) {
    	case JDBCStatement.TYPE_SELECT:
    		// Valid
    		break;
    	case JDBCStatement.TYPE_CALL:
    	{
    		boolean result = execute();
    		if (result) {
    			ResultSet rs = getResultSet();
    			if (rs == null) {
            		throw JDBCError.getSQLException(JDBCError.EXC_CURSOR_STATE_INVALID);
    			} else {
    			  return rs;
    			}
    		} else {
        		throw JDBCError.getSQLException(JDBCError.EXC_CURSOR_STATE_INVALID);
    		}
    	}
    	default:
    		throw JDBCError.getSQLException(JDBCError.EXC_CURSOR_STATE_INVALID);
    }

    if (currentResultSet_ != null)
    {
      currentResultSet_.close();
      currentResultSet_ = null;
    }


    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
    DatabaseOpenAndDescribeAttributes dea = getRequestAttributes(); //conn_.getRequestAttributes();
    dea.setPrepareStatementName(statementName_);
    if (cursorName_ == null) {
    	cursorName_ = conn_.getNextCursorName();
    }
    dea.setCursorName(cursorName_);
    if (fetchSize_ > 0) dea.setBlockingFactor(fetchSize_);
    dea.setDescribeOption(0xD5);
    dea.setScrollableCursorFlag(0);
    dea.setVariableFieldCompression(0xe8);

    if (descriptorHandle_ >= 0) {
      byte[] pmData = getExtendedParameterMarkerData();
      dea.setSQLExtendedParameterMarkerData(pmData);
    }
    JDBCResultSetMetaData md = new JDBCResultSetMetaData(conn.getInfo().getServerCCSID(), conn_.getCalendar(), conn_.getCatalog());

    try
    {
      conn.setCurrentRequestParameterBlockID(rpbID_);
      if (descriptorHandle_ < 0)
      {
        conn.openAndDescribe(dea, md);
      }
      else
      {
        conn.openAndDescribe(dea, descriptorHandle_, md);
      }
    }
    catch (IOException io)
    {
      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    }

    currentResultSet_ = new JDBCResultSet(this, md, statementName_, cursorName_, fetchSize_);
    return currentResultSet_;
  }

  private byte[] getExtendedParameterMarkerData() throws SQLException
  {
    final int indicatorSize = 2;
    final int numCols = pmd_.getParameterCount();
    final int size = 20+(numCols*indicatorSize)+pmd_.getRowSize();
    final byte[] data = new byte[size];
    Conv.intToByteArray(1, data, 0); // Consistency token.
    Conv.intToByteArray(1, data, 4); // Row count.
    Conv.shortToByteArray(numCols, data, 8); // Column count.
    Conv.shortToByteArray(indicatorSize, data, 10); // Indicator size.
    Conv.intToByteArray(pmd_.getRowSize(), data, 16); // Row size.

    // Indicators and data.
    int indicatorOffset = 20;
    int dataOffset = 20+(numCols*indicatorSize);
    for (int i=0; i<numCols; ++i)
    {
      Column col = pmd_.getColumn(i);
      if (col.isNull())
      {
        data[indicatorOffset] = (byte)0xFF;
        data[indicatorOffset+1] = (byte)0xFF;
      }
      else
      {
        col.convertToBytes(data, dataOffset);
      }
      indicatorOffset += 2;
      dataOffset += col.getLength();
    }
    return data;
  }


  public void close() throws SQLException
  {
    if (closed_) return;
    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
    try
    {
      if (descriptorHandle_ >= 0)
      {
        conn.deleteDescriptor(null, descriptorHandle_);
      }
    }
    catch (IOException io)
    {
      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    }
    super.close();
  }


  public int executeUpdate() throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
    DatabaseExecuteAttributes dea = getRequestAttributes();
    dea.setPrepareStatementName(statementName_);

    if (pmd_.getParameterCount() > 0) {
      byte[] pmData = getExtendedParameterMarkerData();
      dea.setSQLExtendedParameterMarkerData(pmData);
    }

    if (statementAttributes_.getSQLStatementType() == 3) {   /* if call */
    	dea.setSQLStatementType(3);
    }
    try
    {
      conn.setCurrentRequestParameterBlockID(rpbID_);
      if (currentResultSet_ != null)
      {
        currentResultSet_.close();
        currentResultSet_ = null;
      }
//      conn.setSQLCommunicationsAreaCallback(this);
      try
      {
        updateCount_ = 0;
        if (descriptorHandle_ < 0)
        {
          conn.execute(dea);
        }
        else
        {
          conn.execute(dea, descriptorHandle_);
        }
        updateCount_ = lastUpdateCount_;
      }
      finally
      {
//        conn.setSQLCommunicationsAreaCallback(null);
      }

      return updateCount_;
    }
    catch (IOException io)
    {
      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    }
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
	    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	    return rsmd_;
  }

  public ParameterMetaData getParameterMetaData() throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    return pmd_;
  }

  /**
   * Not implemented.
  **/
  public void setArray(int parameterIndex, Array x) throws SQLException
  {
    throw new NotImplementedException();
  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setAsciiStreamValue(x, length);
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setBinaryStreamValue(x, length);
  }

  /**
   * Not implemented.
  **/
  public void setBlob(int parameterIndex, Blob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setByte(int parameterIndex, byte x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setCharacterStream(int parameterIndex, Reader x, int length) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setCharacterStreamValue(x, length);
  }

  /**
   * Not implemented.
  **/
  public void setClob(int parameterIndex, Clob x) throws SQLException
  {
    throw new NotImplementedException();
  }

  public void setDate(int parameterIndex, Date x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
  {
	    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	    if (cal == null) throw JDBCError.getSQLException(JDBCError.EXC_PARAMETER_TYPE_INVALID, "cal is null");

	    Column col = pmd_.getColumn(parameterIndex-1);
	    col.setValue(x, cal );

  }

  public void setDouble(int parameterIndex, double x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setFloat(int parameterIndex, float x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setInt(int parameterIndex, int x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setLong(int parameterIndex, long x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setNull(int parameterIndex, int sqlType) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setNull(true);
  }

  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setNull(true);
  }

  public void setObject(int parameterIndex, Object x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  /**
   * Not implemented.
  **/
  public void setObject(int parameterIndex, Object x, int targetSQLType) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setObject(int parameterIndex, Object x, int targetSQLType, int scale) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setRef(int parameterIndex, Ref x) throws SQLException
  {
    throw new NotImplementedException();
  }

  public void setShort(int parameterIndex, short x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);

    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setString(int parameterIndex, String x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setTime(int parameterIndex, Time x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
  {
	    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	    if (cal == null) throw JDBCError.getSQLException(JDBCError.EXC_ATTRIBUTE_VALUE_INVALID, "cal is null");
	    Column col = pmd_.getColumn(parameterIndex-1);
	    col.setValue(x, cal );
  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
  {
	    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	    if (cal == null) throw JDBCError.getSQLException(JDBCError.EXC_ATTRIBUTE_VALUE_INVALID, "cal is null");
	    Column col = pmd_.getColumn(parameterIndex-1);
	    col.setValue(x, cal);
  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    Column col = pmd_.getColumn(parameterIndex-1);
    col.setUnicodeStreamValue(x, length);
  }

  public void setURL(int parameterIndex, URL x) throws SQLException
  {
    if (closed_) throw JDBCError.getSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    Column col = pmd_.getColumn(parameterIndex-1);
    col.setValue(x);
  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setAsciiStream(int parameterIndex, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setAsciiStream(int parameterIndex, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setBinaryStream(int parameterIndex, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setBinaryStream(int parameterIndex, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setBlob(int parameterIndex, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setBlob(int parameterIndex, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setCharacterStream(int parameterIndex, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setCharacterStream(int parameterIndex, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setClob(int parameterIndex, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setClob(int parameterIndex, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setNCharacterStream(int parameterIndex, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setNCharacterStream(int parameterIndex, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }




  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setNClob(int parameterIndex, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   * @param arg2
   */
  public void setNClob(int parameterIndex, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parameterIndex
   * @param arg1
   */
  public void setNString(int parameterIndex, String arg1) throws SQLException {
    throw new NotImplementedException();

  }


}
