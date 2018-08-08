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

  AS400JDBCCallableStatement cstmt_;

  AS400JDBCCallableStatementRedirect(AS400JDBCCallableStatement stmt)
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
    registerOutParameter(findParameterIndex(parameterName), sqlType);

  }

  public void registerOutParameter(String parameterName, int sqlType, int scale)
      throws SQLException {
    registerOutParameter(findParameterIndex(parameterName), sqlType, scale);

  }

  public void registerOutParameter(String parameterName, int sqlType,
      String typeName) throws SQLException {
    registerOutParameter(findParameterIndex(parameterName), sqlType, typeName);

  }

  public URL getURL(int parameterIndex) throws SQLException {
    return cstmt_.getURL(parameterIndex);
  }

  public void setURL(String parameterName, URL val) throws SQLException {
    setURL(findParameterIndex(parameterName), val);
  }

  public void setNull(String parameterName, int sqlType) throws SQLException {
    setNull(findParameterIndex(parameterName), sqlType);
  }

  public void setBoolean(String parameterName, boolean x) throws SQLException {
    setBoolean(findParameterIndex(parameterName), x);
  }

  public void setByte(String parameterName, byte x) throws SQLException {
    setByte(findParameterIndex(parameterName), x);
  }

  public void setShort(String parameterName, short x) throws SQLException {
    setShort(findParameterIndex(parameterName), x);
  }

  public void setInt(String parameterName, int x) throws SQLException {
    setInt(findParameterIndex(parameterName), x);
  }

  public void setLong(String parameterName, long x) throws SQLException {
    setLong(findParameterIndex(parameterName), x);

  }

  public void setFloat(String parameterName, float x) throws SQLException {
    setFloat(findParameterIndex(parameterName), x);
  }

  public void setDouble(String parameterName, double x) throws SQLException {
    setDouble(findParameterIndex(parameterName), x);
  }

  public void setBigDecimal(String parameterName, BigDecimal x)
      throws SQLException {
    setBigDecimal(findParameterIndex(parameterName), x);
  }

  public void setString(String parameterName, String x) throws SQLException {
    setString(findParameterIndex(parameterName), x);
  }

  public void setBytes(String parameterName, byte[] x) throws SQLException {
    setBytes(findParameterIndex(parameterName), x);
  }

  public void setDate(String parameterName, Date x) throws SQLException {
    setDate(findParameterIndex(parameterName), x);
  }

  public void setTime(String parameterName, Time x) throws SQLException {
    setTime(findParameterIndex(parameterName), x);
  }

  public void setTimestamp(String parameterName, Timestamp x)
      throws SQLException {
    setTimestamp(findParameterIndex(parameterName), x);
  }

  public void setAsciiStream(String parameterName, InputStream x, int length)
      throws SQLException {
    setAsciiStream(findParameterIndex(parameterName), x, length);
  }

  public void setBinaryStream(String parameterName, InputStream x, int length)
      throws SQLException {
    setBinaryStream(findParameterIndex(parameterName), x, length);
  }

  public void setObject(String parameterName, Object x, int targetSqlType,
      int scale) throws SQLException {
    setObject(findParameterIndex(parameterName), x, targetSqlType);
  }

  public void setObject(String parameterName, Object x, int targetSqlType)
      throws SQLException {
   setObject(findParameterIndex(parameterName), x, targetSqlType);
  }

  public void setObject(String parameterName, Object x) throws SQLException {
    setObject(findParameterIndex(parameterName), x);
  }

  public void setCharacterStream(String parameterName, Reader reader, int length)
      throws SQLException {
    setCharacterStream(findParameterIndex(parameterName), reader, length);
  }

  public void setDate(String parameterName, Date x, Calendar cal)
      throws SQLException {
    setDate(findParameterIndex(parameterName), x, cal);
  }

  public void setTime(String parameterName, Time x, Calendar cal)
      throws SQLException {
    setTime(findParameterIndex(parameterName), x, cal);
  }

  public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
      throws SQLException {
    setTimestamp(findParameterIndex(parameterName), x, cal);
  }

  public void setNull(String parameterName, int sqlType, String typeName)
      throws SQLException {
    setNull(findParameterIndex(parameterName), sqlType, typeName);
  }

  public String getString(String parameterName) throws SQLException {
    return getString(findParameterIndex(parameterName));
  }

  public boolean getBoolean(String parameterName) throws SQLException {
    return getBoolean(findParameterIndex(parameterName));
  }

  public byte getByte(String parameterName) throws SQLException {
    return getByte(findParameterIndex(parameterName));
  }

  public short getShort(String parameterName) throws SQLException {
    return getShort(findParameterIndex(parameterName));
  }

  public int getInt(String parameterName) throws SQLException {
    return getInt(findParameterIndex(parameterName));
  }

  public long getLong(String parameterName) throws SQLException {
    return getLong(findParameterIndex(parameterName));
  }

  public float getFloat(String parameterName) throws SQLException {
    return getFloat(findParameterIndex(parameterName));
  }

  public double getDouble(String parameterName) throws SQLException {
    return getDouble(findParameterIndex(parameterName));
  }

  public byte[] getBytes(String parameterName) throws SQLException {
    return getBytes(findParameterIndex(parameterName));
  }

  public Date getDate(String parameterName) throws SQLException {
    return getDate(findParameterIndex(parameterName));
  }

  public Time getTime(String parameterName) throws SQLException {
    return getTime(findParameterIndex(parameterName));
  }

  public Timestamp getTimestamp(String parameterName) throws SQLException {
    return getTimestamp(findParameterIndex(parameterName));
  }

  public Object getObject(String parameterName) throws SQLException {
    return getObject(findParameterIndex(parameterName));
  }

  public BigDecimal getBigDecimal(String parameterName) throws SQLException {
    return getBigDecimal(findParameterIndex(parameterName));
  }

  public Object getObject(String parameterName, Map map) throws SQLException {
    return getObject(findParameterIndex(parameterName), map);
  }

  public Ref getRef(String parameterName) throws SQLException {
    return getRef(findParameterIndex(parameterName));
  }

  public Blob getBlob(String parameterName) throws SQLException {
    return getBlob(findParameterIndex(parameterName));
  }

  public Clob getClob(String parameterName) throws SQLException {
    return getClob(findParameterIndex(parameterName));
  }

  public Array getArray(String parameterName) throws SQLException {
    return getArray(findParameterIndex(parameterName));
  }

  public Date getDate(String parameterName, Calendar cal) throws SQLException {
    return getDate(findParameterIndex(parameterName), cal);
  }

  public Time getTime(String parameterName, Calendar cal) throws SQLException {
    return getTime(findParameterIndex(parameterName), cal);
  }

  public Timestamp getTimestamp(String parameterName, Calendar cal)
      throws SQLException {
    return getTimestamp(findParameterIndex(parameterName), cal);
  }

  public URL getURL(String parameterName) throws SQLException {
    return getURL(findParameterIndex(parameterName));
  }


  
  public Reader getCharacterStream(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getCharacterStream(parameterIndex); 
  }

  
  public Reader getCharacterStream(String parameterName) throws SQLException {
    return getCharacterStream(findParameterIndex(parameterName)); 
  }

  
  public Reader getNCharacterStream(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNCharacterStream(parameterIndex); 
  }

  
  public Reader getNCharacterStream(String parameterName) throws SQLException {
    return getNCharacterStream(findParameterIndex(parameterName)); 
  }

  

  
  public String getNString(int parameterIndex) throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getNString(parameterIndex); 
  }

  
  public String getNString(String parameterName) throws SQLException {
    return getNString(findParameterIndex(parameterName)); 
  }

  

  

  

  
  public void setAsciiStream(String parameterName, InputStream stream) throws SQLException {
    setAsciiStream(findParameterIndex(parameterName), stream); 
  }

  
  public void setAsciiStream(String parameterName, InputStream stream, long length)
      throws SQLException {
    setAsciiStream(findParameterIndex(parameterName), stream, length); 
    
  }

  
  public void setBinaryStream(String parameterName, InputStream stream)
      throws SQLException {
    setBinaryStream(findParameterIndex(parameterName), stream); 
    
  }

  
  public void setBinaryStream(String parameterName, InputStream stream, long length)
      throws SQLException {
    setBinaryStream(findParameterIndex(parameterName), stream, length); 
    
  }

  
  public void setBlob(String parameterName, Blob blob) throws SQLException {
    setBlob(findParameterIndex(parameterName), blob); 
    
  }

  
  public void setBlob(String parameterName, InputStream stream) throws SQLException {
    setBlob(findParameterIndex(parameterName), stream); 
    
  }

  
  public void setBlob(String parameterName, InputStream stream, long length)
      throws SQLException {
    setBlob(findParameterIndex(parameterName), stream, length); 
    
  }

  
  public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
    setCharacterStream(findParameterIndex(parameterName), reader); 
    
  }

  
  public void setCharacterStream(String parameterName, Reader reader, long length)
      throws SQLException {
    setCharacterStream(findParameterIndex(parameterName), reader, length); 
  }

  
  public void setClob(String parameterName, Clob clob) throws SQLException {
    setClob(findParameterIndex(parameterName), clob); 
  }

  
  public void setClob(String parameterName, Reader reader) throws SQLException {
    setClob(findParameterIndex(parameterName), reader); 
  }

  
  public void setClob(String parameterName, Reader reader, long length) throws SQLException {
    setClob(findParameterIndex(parameterName), reader, length); 
  }

  
  public void setNCharacterStream(String parameterName, Reader reader) throws SQLException {
    setNCharacterStream(findParameterIndex(parameterName), reader); 
  }

  
  public void setNCharacterStream(String parameterName, Reader reader, long length)
      throws SQLException {
    setNCharacterStream(findParameterIndex(parameterName), reader, length); 
    
  }

  

  
  public void setNClob(String parameterName, Reader reader) throws SQLException {
    setNClob(findParameterIndex(parameterName), reader); 
    
  }

  
  public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
    setNClob(findParameterIndex(parameterName), reader, length); 
    
  }

  
  public void setNString(String parameterName, String x) throws SQLException {
    setNString(findParameterIndex(parameterName), x); 
    
  }
  
  public Object getObject(String parameterName, Class type)
      throws SQLException {
    return getObject(findParameterIndex(parameterName), type); 
  }

  
  public Object getObject(int parameter, Class type)
      throws SQLException {
    return ((AS400JDBCCallableStatement) cstmt_).getObject(parameter, type); 
  }

/* ifdef JDBC40
      
  public void setNClob(String parameterName, NClob clob) throws SQLException {
    setNClob(findParameterIndex(parameterName), clob); 
    
  }

  public void setRowId(String parameterName, RowId x) throws SQLException {
    setRowId(findParameterIndex(parameterName), x); 
    
  }

  
  public void setSQLXML(String parameterName, SQLXML xml) throws SQLException {
    setSQLXML(findParameterIndex(parameterName), xml); 
    
  }
  
  
  
    public NClob getNClob(int parameterIndex) throws SQLException {
    return cstmt_.getNClob(parameterIndex); 
  }

  
  public NClob getNClob(String parameterName) throws SQLException {
        return getNClob(findParameterIndex(parameterName)); 
  }


  public RowId getRowId(int parameterIndex) throws SQLException {
    return cstmt_.getRowId(parameterIndex); 
  }

  
  public RowId getRowId(String parameterName) throws SQLException {
    return getRowId(findParameterIndex(parameterName)); 
  }

  
  public SQLXML getSQLXML(int parameterIndex) throws SQLException {
    return cstmt_.getSQLXML(parameterIndex); 
  }

  
  public SQLXML getSQLXML(String parameterName) throws SQLException {
    return getSQLXML(findParameterIndex(parameterName)); 
  }



endif */ 
  public void setObject(String parameterName,
                         Object x,
                         /* ifdef JDBC42        
                         SQLType  
                   endif*/ 
                   /* ifndef JDBC42 */
                   Object
                   /* endif */
                         targetSqlType,
                         int scaleOrLength)                    throws SQLException
         {
    setObject(findParameterIndex(parameterName),  x, targetSqlType, scaleOrLength); 
                  }


 public void setObject(String parameterName,
                         Object x,
/* ifdef JDBC42        
                         SQLType  
endif*/ 
/* ifndef JDBC42 */
                   Object
/* endif */
                         targetSqlType)
                  throws SQLException
                  {
   
setObject(findParameterIndex(parameterName),  x, targetSqlType);                  
                  }


  public void registerOutParameter(int parameterIndex,
/* ifdef JDBC42        
      SQLType  
endif*/ 
/* ifndef JDBC42 */
      Object
/* endif */
                                    sqlType)
                             throws SQLException{
    cstmt_.registerOutParameter(parameterIndex, sqlType); 
  }

  
  public void registerOutParameter(int parameterIndex,
      /* ifdef JDBC42        
      SQLType  
endif*/ 
/* ifndef JDBC42 */
      Object
/* endif */
                                    sqlType,
                                    int scale)
                             throws SQLException {
    cstmt_.registerOutParameter(parameterIndex, sqlType, scale); 
  }

  public void registerOutParameter(int parameterIndex,
/* ifdef JDBC42        
      SQLType  
endif*/ 
/* ifndef JDBC42 */
      Object
/* endif */
                                    sqlType,
                                    String typeName)
                             throws SQLException {
    cstmt_.registerOutParameter(parameterIndex, sqlType, typeName); 
  }

 public void registerOutParameter(String parameterName,
     /* ifdef JDBC42        
     SQLType  
endif*/ 
/* ifndef JDBC42 */
     Object
/* endif */
                                    sqlType)
                             throws SQLException {
   registerOutParameter(findParameterIndex(parameterName), sqlType); 
 }

public void registerOutParameter(String parameterName,
    /* ifdef JDBC42        
    SQLType  
endif*/ 
/* ifndef JDBC42 */
    Object
/* endif */
                                    sqlType,
                                    int scale)
                             throws SQLException {
  registerOutParameter(findParameterIndex(parameterName), sqlType, scale); 
}


public void registerOutParameter(String parameterName,
    /* ifdef JDBC42        
    SQLType  
endif*/ 
/* ifndef JDBC42 */
    Object
/* endif */
                                    sqlType,
                                    String typeName)
                             throws SQLException
      {
  registerOutParameter(findParameterIndex(parameterName), sqlType, typeName); 
       }

  
  
  
  
}
