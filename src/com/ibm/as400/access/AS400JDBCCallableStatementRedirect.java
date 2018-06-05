package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*; 
import java.util.Calendar;
import java.util.Map;

public class AS400JDBCCallableStatementRedirect extends
    AS400JDBCPreparedStatementRedirect implements CallableStatement {

  CallableStatement cstmt_;

  AS400JDBCCallableStatementRedirect(CallableStatement stmt)
      throws SQLException {
    super(stmt);
    cstmt_ = stmt;
  }

  public void registerOutParameter(int parameterIndex, int sqlType)
      throws SQLException {
    cstmt_.registerOutParameter(parameterIndex, sqlType);

  }

  public void registerOutParameter(int parameterIndex, int sqlType, int scale)
      throws SQLException {
    cstmt_.registerOutParameter(parameterIndex, sqlType, scale);

  }

  public boolean wasNull() throws SQLException {
    return cstmt_.wasNull();
  }

  public String getString(int parameterIndex) throws SQLException {
    return cstmt_.getString(parameterIndex);
  }

  public boolean getBoolean(int parameterIndex) throws SQLException {
    return cstmt_.getBoolean(parameterIndex);
  }

  public byte getByte(int parameterIndex) throws SQLException {
    return cstmt_.getByte(parameterIndex);
  }

  public short getShort(int parameterIndex) throws SQLException {
    return cstmt_.getShort(parameterIndex);
  }

  public int getInt(int parameterIndex) throws SQLException {
    return cstmt_.getInt(parameterIndex);
  }

  public long getLong(int parameterIndex) throws SQLException {
    return cstmt_.getLong(parameterIndex);
  }

  public float getFloat(int parameterIndex) throws SQLException {
    return cstmt_.getFloat(parameterIndex);
  }

  public double getDouble(int parameterIndex) throws SQLException {
    return cstmt_.getDouble(parameterIndex);
  }

  public BigDecimal getBigDecimal(int parameterIndex, int scale)
      throws SQLException {
    return cstmt_.getBigDecimal(parameterIndex, scale);
  }

  public byte[] getBytes(int parameterIndex) throws SQLException {
    return cstmt_.getBytes(parameterIndex);
  }

  public Date getDate(int parameterIndex) throws SQLException {
    return cstmt_.getDate(parameterIndex);
  }

  public Time getTime(int parameterIndex) throws SQLException {
    return cstmt_.getTime(parameterIndex);
  }

  public Timestamp getTimestamp(int parameterIndex) throws SQLException {
    return cstmt_.getTimestamp(parameterIndex);
  }

  public Object getObject(int parameterIndex) throws SQLException {
    return cstmt_.getObject(parameterIndex);
  }

  public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
    return cstmt_.getBigDecimal(parameterIndex);
  }

  public Object getObject(int parameterIndex, Map map) throws SQLException {
    return cstmt_.getObject(parameterIndex, map);
  }

  public Ref getRef(int parameterIndex) throws SQLException {
    return cstmt_.getRef(parameterIndex);
  }

  public Blob getBlob(int parameterIndex) throws SQLException {
    return cstmt_.getBlob(parameterIndex);
  }

  public Clob getClob(int parameterIndex) throws SQLException {
    return cstmt_.getClob(parameterIndex);
  }

  public Array getArray(int parameterIndex) throws SQLException {
    return cstmt_.getArray(parameterIndex);
  }

  public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
    return cstmt_.getDate(parameterIndex, cal);
  }

  public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
    return cstmt_.getTime(parameterIndex, cal);
  }

  public Timestamp getTimestamp(int parameterIndex, Calendar cal)
      throws SQLException {
    return cstmt_.getTimestamp(parameterIndex, cal);
  }

  public void registerOutParameter(int paramIndex, int sqlType, String typeName)
      throws SQLException {
    cstmt_.registerOutParameter(paramIndex, sqlType, typeName);

  }

  public void registerOutParameter(String parameterName, int sqlType)
      throws SQLException {
    cstmt_.registerOutParameter(parameterName, sqlType);

  }

  public void registerOutParameter(String parameterName, int sqlType, int scale)
      throws SQLException {
    cstmt_.registerOutParameter(parameterName, sqlType, scale);

  }

  public void registerOutParameter(String parameterName, int sqlType,
      String typeName) throws SQLException {
    cstmt_.registerOutParameter(parameterName, sqlType, typeName);

  }

  public URL getURL(int parameterIndex) throws SQLException {
    return cstmt_.getURL(parameterIndex);
  }

  public void setURL(String parameterName, URL val) throws SQLException {
    cstmt_.setURL(parameterName, val);

  }

  public void setNull(String parameterName, int sqlType) throws SQLException {
    cstmt_.setNull(parameterName, sqlType);

  }

  public void setBoolean(String parameterName, boolean x) throws SQLException {
    cstmt_.setBoolean(parameterName, x);

  }

  public void setByte(String parameterName, byte x) throws SQLException {
    cstmt_.setByte(parameterName, x);

  }

  public void setShort(String parameterName, short x) throws SQLException {
    cstmt_.setShort(parameterName, x);

  }

  public void setInt(String parameterName, int x) throws SQLException {
    cstmt_.setInt(parameterName, x);

  }

  public void setLong(String parameterName, long x) throws SQLException {
    cstmt_.setLong(parameterName, x);

  }

  public void setFloat(String parameterName, float x) throws SQLException {
    cstmt_.setFloat(parameterName, x);

  }

  public void setDouble(String parameterName, double x) throws SQLException {
    cstmt_.setDouble(parameterName, x);

  }

  public void setBigDecimal(String parameterName, BigDecimal x)
      throws SQLException {
    cstmt_.setBigDecimal(parameterName, x);

  }

  public void setString(String parameterName, String x) throws SQLException {
    cstmt_.setString(parameterName, x);

  }

  public void setBytes(String parameterName, byte[] x) throws SQLException {
    cstmt_.setBytes(parameterName, x);

  }

  public void setDate(String parameterName, Date x) throws SQLException {
    cstmt_.setDate(parameterName, x);

  }

  public void setTime(String parameterName, Time x) throws SQLException {
    cstmt_.setTime(parameterName, x);

  }

  public void setTimestamp(String parameterName, Timestamp x)
      throws SQLException {
    cstmt_.setTimestamp(parameterName, x);

  }

  public void setAsciiStream(String parameterName, InputStream x, int length)
      throws SQLException {
    cstmt_.setAsciiStream(parameterName, x, length);

  }

  public void setBinaryStream(String parameterName, InputStream x, int length)
      throws SQLException {
    cstmt_.setBinaryStream(parameterName, x, length);

  }

  public void setObject(String parameterName, Object x, int targetSqlType,
      int scale) throws SQLException {
    cstmt_.setObject(parameterName, x, targetSqlType);

  }

  public void setObject(String parameterName, Object x, int targetSqlType)
      throws SQLException {
    cstmt_.setObject(parameterName, x, targetSqlType);

  }

  public void setObject(String parameterName, Object x) throws SQLException {
    cstmt_.setObject(parameterName, x);

  }

  public void setCharacterStream(String parameterName, Reader reader, int length)
      throws SQLException {
    cstmt_.setCharacterStream(parameterName, reader, length);

  }

  public void setDate(String parameterName, Date x, Calendar cal)
      throws SQLException {
    cstmt_.setDate(parameterName, x, cal);

  }

  public void setTime(String parameterName, Time x, Calendar cal)
      throws SQLException {
    cstmt_.setTime(parameterName, x, cal);

  }

  public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
      throws SQLException {
    cstmt_.setTimestamp(parameterName, x, cal);

  }

  public void setNull(String parameterName, int sqlType, String typeName)
      throws SQLException {
    cstmt_.setNull(parameterName, sqlType, typeName);

  }

  public String getString(String parameterName) throws SQLException {
    return cstmt_.getString(parameterName);
  }

  public boolean getBoolean(String parameterName) throws SQLException {
    return cstmt_.getBoolean(parameterName);
  }

  public byte getByte(String parameterName) throws SQLException {
    return cstmt_.getByte(parameterName);
  }

  public short getShort(String parameterName) throws SQLException {
    return cstmt_.getShort(parameterName);
  }

  public int getInt(String parameterName) throws SQLException {
    return cstmt_.getInt(parameterName);
  }

  public long getLong(String parameterName) throws SQLException {
    return cstmt_.getLong(parameterName);
  }

  public float getFloat(String parameterName) throws SQLException {
    return cstmt_.getFloat(parameterName);
  }

  public double getDouble(String parameterName) throws SQLException {
    return cstmt_.getDouble(parameterName);
  }

  public byte[] getBytes(String parameterName) throws SQLException {
    return cstmt_.getBytes(parameterName);
  }

  public Date getDate(String parameterName) throws SQLException {
    return cstmt_.getDate(parameterName);
  }

  public Time getTime(String parameterName) throws SQLException {
    return cstmt_.getTime(parameterName);
  }

  public Timestamp getTimestamp(String parameterName) throws SQLException {
    return cstmt_.getTimestamp(parameterName);
  }

  public Object getObject(String parameterName) throws SQLException {
    return cstmt_.getObject(parameterName);
  }

  public BigDecimal getBigDecimal(String parameterName) throws SQLException {
    return cstmt_.getBigDecimal(parameterName);
  }

  public Object getObject(String parameterName, Map map) throws SQLException {
    return cstmt_.getObject(parameterName, map);
  }

  public Ref getRef(String parameterName) throws SQLException {
    return cstmt_.getRef(parameterName);
  }

  public Blob getBlob(String parameterName) throws SQLException {
    return cstmt_.getBlob(parameterName);
  }

  public Clob getClob(String parameterName) throws SQLException {
    return cstmt_.getClob(parameterName);
  }

  public Array getArray(String parameterName) throws SQLException {
    return cstmt_.getArray(parameterName);
  }

  public Date getDate(String parameterName, Calendar cal) throws SQLException {
    return cstmt_.getDate(parameterName, cal);
  }

  public Time getTime(String parameterName, Calendar cal) throws SQLException {
    return cstmt_.getTime(parameterName, cal);
  }

  public Timestamp getTimestamp(String parameterName, Calendar cal)
      throws SQLException {
    return cstmt_.getTimestamp(parameterName, cal);
  }

  public URL getURL(String parameterName) throws SQLException {
    return cstmt_.getURL(parameterName);
  }


  
  public Reader getCharacterStream(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getCharacterStream(parameterIndex); 
  }

  
  public Reader getCharacterStream(String parameterName) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getCharacterStream(parameterName); 
  }

  
  public Reader getNCharacterStream(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNCharacterStream(parameterIndex); 
  }

  
  public Reader getNCharacterStream(String parameterName) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNCharacterStream(parameterName); 
  }

  

  
  public String getNString(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNString(parameterIndex); 
  }

  
  public String getNString(String parameterName) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNString(parameterName); 
  }

  

  

  

  
  public void setAsciiStream(String parameterName, InputStream stream) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setAsciiStream(parameterName, stream); 
    
  }

  
  public void setAsciiStream(String parameterName, InputStream stream, long length)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setAsciiStream(parameterName, stream, length); 
    
  }

  
  public void setBinaryStream(String parameterName, InputStream stream)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setBinaryStream(parameterName, stream); 
    
  }

  
  public void setBinaryStream(String parameterName, InputStream stream, long length)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setBinaryStream(parameterName, stream, length); 
    
  }

  
  public void setBlob(String parameterName, Blob blob) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setBlob(parameterName, blob); 
    
  }

  
  public void setBlob(String parameterName, InputStream stream) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setBlob(parameterName, stream); 
    
  }

  
  public void setBlob(String parameterName, InputStream stream, long length)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setBlob(parameterName, stream, length); 
    
  }

  
  public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setCharacterStream(parameterName, reader); 
    
  }

  
  public void setCharacterStream(String parameterName, Reader reader, long length)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setCharacterStream(parameterName, reader, length); 
    
  }

  
  public void setClob(String parameterName, Clob clob) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setClob(parameterName, clob); 
    
  }

  
  public void setClob(String parameterName, Reader reader) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setClob(parameterName, reader); 
    
  }

  
  public void setClob(String parameterName, Reader reader, long length) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setClob(parameterName, reader, length); 
    
  }

  
  public void setNCharacterStream(String parameterName, Reader reader) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setNCharacterStream(parameterName, reader); 
    
  }

  
  public void setNCharacterStream(String parameterName, Reader reader, long length)
      throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setNCharacterStream(parameterName, reader, length); 
    
  }

  

  
  public void setNClob(String parameterName, Reader reader) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setNClob(parameterName, reader); 
    
  }

  
  public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setNClob(parameterName, reader, length); 
    
  }

  
  public void setNString(String parameterName, String x) throws SQLException {
    ((AS400JDBCCallableStatement) cstmt_).setNString(parameterName, x); 
    
  }

/* ifdef JDBC40
      
  public void setNClob(String parameterName, NClob clob) throws SQLException {
    cstmt_.setNClob(parameterName, clob); 
    
  }

  public void setRowId(String parameterName, RowId x) throws SQLException {
    cstmt_.setRowId(parameterName, x); 
    
  }

  
  public void setSQLXML(String parameterName, SQLXML xml) throws SQLException {
    cstmt_.setSQLXML(parameterName, xml); 
    
  }
  
  
  
    public NClob getNClob(int parameterIndex) throws SQLException {
    return cstmt_.getNClob(parameterIndex); 
  }

  
  public NClob getNClob(String parameterName) throws SQLException {
        return cstmt_.getNClob(parameterName); 
  }


  public RowId getRowId(int parameterIndex) throws SQLException {
    return cstmt_.getRowId(parameterIndex); 
  }

  
  public RowId getRowId(String parameterName) throws SQLException {
    return cstmt_.getRowId(parameterName); 
  }

  
  public SQLXML getSQLXML(int parameterIndex) throws SQLException {
    return cstmt_.getSQLXML(parameterIndex); 
  }

  
  public SQLXML getSQLXML(String parameterName) throws SQLException {
    return cstmt_.getSQLXML(parameterName); 
  }



endif */ 
  
  
  
}
