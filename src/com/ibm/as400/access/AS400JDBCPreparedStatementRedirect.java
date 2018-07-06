package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

// 
// This extends AS400JDBCPrepareStatement for compatibility reasons only
// All methods have been overridden
// 
public class AS400JDBCPreparedStatementRedirect  extends AS400JDBCPreparedStatement {

  AS400JDBCPreparedStatement pstmt_ = null;
  AS400JDBCConnection connection_; 

  AS400JDBCPreparedStatementRedirect(AS400JDBCPreparedStatement stmt)
      throws SQLException {

    pstmt_ = stmt;
    connection_ = (AS400JDBCConnection) stmt.getConnection(); 
  }

  // Methods from AS400JDBCStatementRedirect.  
  // Needed because we can't have double inheritance
  
  
  public ResultSet executeQuery(String sql) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
        return pstmt_.executeQuery(sql); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return null;
  }

  public int executeUpdate(String sql) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
        return pstmt_.executeUpdate(sql); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return 0;
  }

  public void close() throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         pstmt_.close(); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */

  }

  public int getMaxFieldSize() throws SQLException {
    return pstmt_.getMaxFieldSize();
  }

  public void setMaxFieldSize(int max) throws SQLException {
    pstmt_.setMaxFieldSize(max); 
  }

  public int getMaxRows() throws SQLException {
    return pstmt_.getMaxRows(); 
  }

  public void setMaxRows(int max) throws SQLException {
    pstmt_.setMaxRows(max); 
  }

  public void setEscapeProcessing(boolean enable) throws SQLException {
    pstmt_.setEscapeProcessing(enable);
  }

  public int getQueryTimeout() throws SQLException {
    return pstmt_.getQueryTimeout(); 
  }

  public void setQueryTimeout(int seconds) throws SQLException {
    pstmt_.setQueryTimeout(seconds); 
  }

  public void cancel() throws SQLException {
    pstmt_.cancel(); 
  }

  public SQLWarning getWarnings() throws SQLException {
    return pstmt_.getWarnings(); 
  }

  public void clearWarnings() throws SQLException {
    pstmt_.clearWarnings(); 
  }

  public void setCursorName(String name) throws SQLException {
    pstmt_.setCursorName(name); 
  }

  public boolean execute(String sql) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.execute(sql);  
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return false; 
  }

  public ResultSet getResultSet() throws SQLException {
    return pstmt_.getResultSet(); 
  }

  public int getUpdateCount() throws SQLException {
    return pstmt_.getUpdateCount(); 
  }

  public boolean getMoreResults() throws SQLException {
    return pstmt_.getMoreResults(); 
  }

  public void setFetchDirection(int direction) throws SQLException {
    pstmt_.setFetchDirection(direction); 
  }

  public int getFetchDirection() throws SQLException {
    return pstmt_.getFetchDirection(); 
  }

  public void setFetchSize(int rows) throws SQLException {
    pstmt_.setFetchSize(rows);

  }

  public int getFetchSize() throws SQLException {
    return pstmt_.getFetchSize(); 
  }

  public int getResultSetConcurrency() throws SQLException {
    return pstmt_.getResultSetConcurrency(); 
  }

  public int getResultSetType() throws SQLException {
    return pstmt_.getResultSetType(); 
  }

  public void addBatch(String sql) throws SQLException {
    pstmt_.addBatch(sql); 
  }

  public void clearBatch() throws SQLException {
    pstmt_.clearBatch(); 
  }

  public int[] executeBatch() throws SQLException {
    return pstmt_.executeBatch(); 
  }

  public Connection getConnection() throws SQLException {
    return pstmt_.getConnection(); 
  }

  public boolean getMoreResults(int current) throws SQLException {
    return pstmt_.getMoreResults(current); 
  }

  public ResultSet getGeneratedKeys() throws SQLException {
    return pstmt_.getGeneratedKeys(); 
  }

  public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.executeUpdate(sql,autoGeneratedKeys); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return 0;
  }

  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.executeUpdate(sql, columnIndexes); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return 0;
  }

  public int executeUpdate(String sql, String[] columnNames)
      throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.executeUpdate(sql, columnNames); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return 0;
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.execute(sql, autoGeneratedKeys); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return false;
  }

  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.execute(sql, columnIndexes); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return false;
  }

  public boolean execute(String sql, String[] columnNames) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return pstmt_.execute(sql, columnNames); 
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--; 
          if (retryCount >= 0 ) {
            retry = true;
          } else {
            throw e; 
          }
        } else {
          throw e; 
        }
      }
    } /* retry */
    return false;
  }

  public int getResultSetHoldability() throws SQLException {
    return pstmt_.getResultSetHoldability(); 
  }

  
  public boolean isCloseOnCompletion() throws SQLException {
    return ((AS400JDBCStatement)pstmt_).isCloseOnCompletion(); 
  }

  public void closeOnCompletion() throws SQLException {
    ((AS400JDBCStatement)pstmt_).closeOnCompletion(); 
  }


  
  
      public   long getLargeUpdateCount() throws SQLException {
        return pstmt_.getLargeUpdateCount();  
      }
      
      public void setLargeMaxRows(long max) throws SQLException {
        pstmt_.setLargeMaxRows(max); 
      }
      public long getLargeMaxRows() throws SQLException {
        return pstmt_.getLargeMaxRows(); 
      }
    
    public long[] executeLargeBatch()    throws SQLException  {
      return pstmt_.executeLargeBatch(); 
    }

   public long executeLargeUpdate(String sql)   throws SQLException {
     boolean retry = true;
     int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
     while (retry) {
       retry = false;
       try {
         return pstmt_.executeLargeUpdate(sql); 
       } catch (AS400JDBCTransientException e) {
         if (connection_.canSeamlessFailover()) {
           retryCount--; 
           if (retryCount >= 0 ) {
             retry = true;
           } else {
             throw e; 
           }
         } else {
           throw e; 
         }
       }
     } /* retry */
     return 0;

   }


    public long executeLargeUpdate(String sql,
                                    int autoGeneratedKeys)
                             throws SQLException {
      boolean retry = true;
      int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
      while (retry) {
        retry = false;
        try {
          return pstmt_.executeLargeUpdate(sql, autoGeneratedKeys); 
        } catch (AS400JDBCTransientException e) {
          if (connection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retry = true;
            } else {
              throw e; 
            }
          } else {
            throw e; 
          }
        }
      } /* retry */
      return 0;
    }


    public long executeLargeUpdate(String sql,
                                    int[] columnIndexes) 
                             throws SQLException {

      boolean retry = true;
      int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
      while (retry) {
        retry = false;
        try {
          return pstmt_.executeLargeUpdate(sql, columnIndexes); 
        } catch (AS400JDBCTransientException e) {
          if (connection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retry = true;
            } else {
              throw e; 
            }
          } else {
            throw e; 
          }
        }
      } /* retry */
      return 0;

    
    }

    public long executeLargeUpdate(String sql,
                                    String[] columnNames)  throws SQLException {
      boolean retry = true;
      int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
      while (retry) {
        retry = false;
        try {
          return pstmt_.executeLargeUpdate(sql, columnNames); 
        } catch (AS400JDBCTransientException e) {
          if (connection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retry = true;
            } else {
              throw e; 
            }
          } else {
            throw e; 
          }
        }
      } /* retry */
      return 0;
      
    }

    void checkOpen() throws SQLException {
      pstmt_.checkOpen(); 
    }

    public int getPositionOfSyntaxError() throws SQLException {
      return pstmt_.getPositionOfSyntaxError(); 
    }

    public boolean isClosed() throws SQLException {
      return pstmt_.isClosed(); 
    }

    public String toString() {
      return pstmt_.toString(); 
    }

    public void setPoolable(boolean poolable) throws SQLException {
      pstmt_.setPoolable(poolable); 
    }

    public boolean isPoolable() throws SQLException {
      return pstmt_.isPoolable(); 
    }
               

  
  
  
  //
  // Start of prepared statement only methods 
  // 
  
  public ResultSet executeQuery() throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retry) {
      retry = false;
      try {
        return pstmt_.executeQuery();
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--;
          if (retryCount >= 0) {
            retry = true;
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    } /* retry */
    return null;

  }

  public int executeUpdate() throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retry) {
      retry = false;
      try {
        return pstmt_.executeUpdate();
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--;
          if (retryCount >= 0) {
            retry = true;
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    } /* retry */
    return 0;
  }

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    pstmt_.setNull(parameterIndex, sqlType);
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    pstmt_.setBoolean(parameterIndex, x);

  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    pstmt_.setByte(parameterIndex, x);

  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    pstmt_.setShort(parameterIndex, x);

  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    pstmt_.setInt(parameterIndex, x);

  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    pstmt_.setLong(parameterIndex, x);

  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    pstmt_.setFloat(parameterIndex, x);

  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    pstmt_.setDouble(parameterIndex, x);

  }

  public void setBigDecimal(int parameterIndex, BigDecimal x)
      throws SQLException {
    pstmt_.setBigDecimal(parameterIndex, x);

  }

  public void setString(int parameterIndex, String x) throws SQLException {
    pstmt_.setString(parameterIndex, x);

  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    pstmt_.setBytes(parameterIndex, x);

  }

  public void setDate(int parameterIndex, Date x) throws SQLException {
    pstmt_.setDate(parameterIndex, x);

  }

  public void setTime(int parameterIndex, Time x) throws SQLException {
    pstmt_.setTime(parameterIndex, x);

  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    pstmt_.setTimestamp(parameterIndex, x);

  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    pstmt_.setAsciiStream(parameterIndex, x, length);

  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    pstmt_.setUnicodeStream(parameterIndex, x, length);

  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    pstmt_.setBinaryStream(parameterIndex, x, length);

  }

  public void clearParameters() throws SQLException {
    pstmt_.clearParameters();

  }

  public void setObject(int parameterIndex, Object x, int targetSqlType,
      int scale) throws SQLException {
    pstmt_.setObject(parameterIndex, x);

  }

  public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException {
    pstmt_.setObject(parameterIndex, x, targetSqlType);

  }

  public void setObject(int parameterIndex, Object x) throws SQLException {
    pstmt_.setObject(parameterIndex, x);

  }

  public boolean execute() throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retry) {
      retry = false;
      try {
        return pstmt_.execute();
      } catch (AS400JDBCTransientException e) {
        if (connection_.canSeamlessFailover()) {
          retryCount--;
          if (retryCount >= 0) {
            retry = true;
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    } /* retry */
    return false;
  }

  public void addBatch() throws SQLException {
    pstmt_.addBatch();

  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException {
    pstmt_.setCharacterStream(parameterIndex, reader, length);

  }

  public void setRef(int parameterIndex, Ref x) throws SQLException {
    pstmt_.setRef(parameterIndex, x);

  }

  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    pstmt_.setBlob(parameterIndex, x);

  }

  public void setClob(int parameterIndex, Clob x) throws SQLException {
    pstmt_.setClob(parameterIndex, x);

  }

  public void setArray(int parameterIndex, Array x) throws SQLException {
    pstmt_.setArray(parameterIndex, x);

  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return pstmt_.getMetaData(); 
  }

  public void setDate(int parameterIndex, Date x, Calendar cal)
      throws SQLException {
    pstmt_.setDate(parameterIndex, x, cal);

  }

  public void setTime(int parameterIndex, Time x, Calendar cal)
      throws SQLException {
    pstmt_.setTime(parameterIndex, x, cal);

  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
      throws SQLException {
    pstmt_.setTimestamp(parameterIndex, x, cal);

  }

  public void setNull(int parameterIndex, int sqlType, String typeName)
      throws SQLException {
    pstmt_.setNull(parameterIndex, sqlType, typeName);

  }

  public void setURL(int parameterIndex, URL x) throws SQLException {
    pstmt_.setURL(parameterIndex, x);

  }

  public ParameterMetaData getParameterMetaData() throws SQLException {
    return pstmt_.getParameterMetaData();
  }

     
     public void setNClob(int parameterIndex, Reader reader) throws SQLException 
     {
       pstmt_.setNClob(parameterIndex, reader); 
     }

    public void setAsciiStream(int parameterIndex, InputStream stream) throws SQLException {
      pstmt_.setAsciiStream(parameterIndex, stream); 
    }

    public void setAsciiStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      pstmt_.setAsciiStream(parameterIndex,  stream, length); 
      
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream) throws SQLException {
      pstmt_.setBinaryStream(parameterIndex,  stream); 
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      pstmt_.setBinaryStream(parameterIndex,  stream, length); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream) throws SQLException {
      pstmt_.setBlob(parameterIndex,  stream); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      pstmt_.setBlob(parameterIndex,  stream, length); 
      
      
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      pstmt_.setCharacterStream(parameterIndex, reader); 
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
    pstmt_.setCharacterStream(parameterIndex,  reader, length);   
    }

    
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
      pstmt_.setClob(parameterIndex, reader); 
      
    }

    
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      pstmt_.setClob(parameterIndex, reader, length); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      pstmt_.setNCharacterStream(parameterIndex,  reader); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
pstmt_.setNCharacterStream(parameterIndex,  reader, length); 
    }

    

    
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      pstmt_.setNClob(parameterIndex,  reader, length); 
    }

    
    public void setNString(int parameterIndex, String x) throws SQLException {
      pstmt_.setNString(parameterIndex, x); 
    }

    public void setDB2Default(int parameterIndex) throws SQLException {
      pstmt_.setDB2Default(parameterIndex); 
    }

    public void setDBDefault(int parameterIndex) throws SQLException {
      pstmt_.setDBDefault(parameterIndex); 
      
    }

    public void setDB2Unassigned(int parameterIndex) throws SQLException {
      pstmt_.setDB2Unassigned(parameterIndex);
      
      
    }

    public void setDBUnassigned(int parameterIndex) throws SQLException {
      pstmt_.setDBUnassigned(parameterIndex);
      
    }

    public String getDB2ParameterName(int parm) throws SQLException {
      return pstmt_.getDB2ParameterName(parm);
      
    }


  
    
    public  void setObject(int parameterIndex,
        Object x,
/* ifdef JDBC42        
        SQLType  
endif*/ 
/* ifndef JDBC42 */
Object
/* endif */
        targetSqlType,
        int scaleOrLength)
 throws SQLException  {
      pstmt_.setObject(parameterIndex,x,targetSqlType,scaleOrLength); 
    }
  

    public void setObject(int parameterIndex,
        Object x,
        /* ifdef JDBC42        
        SQLType  
  endif*/ 
  /* ifndef JDBC42 */
  Object
  /* endif */
        targetSqlType)
 throws SQLException {
      pstmt_.setObject(parameterIndex,  x, targetSqlType); 
    }
  
    
    

    public long executeLargeUpdate() throws SQLException {
      return pstmt_.executeLargeUpdate(); 
    }

    int findParameterIndex(String s) throws SQLException {
      return pstmt_.findParameterIndex(s); 
      
    }

    int getParameterCcsid(int p) throws SQLException {
      return pstmt_.getParameterCcsid(p); 
      
    }

    String getParameterClassName(int p) throws SQLException {
      return pstmt_.getParameterClassName(p); 
      
    }

    int getParameterCount() throws SQLException {
      return pstmt_.getParameterCount();
    }

    int getParameterMode(int param) throws SQLException {
      return pstmt_.getParameterMode(param); 
    }

    int getParameterType(int param) throws SQLException {
      return pstmt_.getParameterType(param); 
    }

    String getParameterTypeName(int param) throws SQLException {
      return pstmt_.getParameterTypeName(param); 
    }

    int getPrecision(int param) throws SQLException {
      return pstmt_.getPrecision(param); 
    }

    JDServerRow getResultRow() {
      return pstmt_.getResultRow(); 
    }

    int getScale(int param) throws SQLException {
      return pstmt_.getScale(param); 
    }

    int isNullable(int param) throws SQLException {
      return pstmt_.isNullable(param); 
    }

    boolean isSigned(int param) throws SQLException {
      return pstmt_.isSigned(param); 
    }

    

    
/* ifdef JDBC40


    public void setNClob(int parameterIndex, NClob clob) throws SQLException {
      pstmt_.setNClob(parameterIndex,  clob);
    }

    
    
    public void setSQLXML(int parameterIndex, SQLXML xml) throws SQLException {
      pstmt_.setSQLXML(parameterIndex, xml); 
    }

    
    
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
      pstmt_.setRowId(parameterIndex, x); 
     } 

 
endif */

}
