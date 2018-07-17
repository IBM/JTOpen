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

  AS400JDBCPreparedStatement stmt_ = null;
  AS400JDBCConnection connection_; 

  AS400JDBCPreparedStatementRedirect(AS400JDBCPreparedStatement stmt)
      throws SQLException {

    stmt_ = stmt;
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
        AS400JDBCResultSet rs = (AS400JDBCResultSet) stmt_.executeQuery(sql);
        if (rs != null) { 
           rs.setStatement(this);
        }
        return rs; 
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
        return stmt_.executeUpdate(sql); 
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
         stmt_.close(); 
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
    return stmt_.getMaxFieldSize();
  }

  public void setMaxFieldSize(int max) throws SQLException {
    stmt_.setMaxFieldSize(max); 
  }

  public int getMaxRows() throws SQLException {
    return stmt_.getMaxRows(); 
  }

  public void setMaxRows(int max) throws SQLException {
    stmt_.setMaxRows(max); 
  }

  public void setEscapeProcessing(boolean enable) throws SQLException {
    stmt_.setEscapeProcessing(enable);
  }

  public int getQueryTimeout() throws SQLException {
    return stmt_.getQueryTimeout(); 
  }

  public void setQueryTimeout(int seconds) throws SQLException {
    stmt_.setQueryTimeout(seconds); 
  }

  public void cancel() throws SQLException {
    stmt_.cancel(); 
  }

  public SQLWarning getWarnings() throws SQLException {
    return stmt_.getWarnings(); 
  }

  public void clearWarnings() throws SQLException {
    stmt_.clearWarnings(); 
  }

  public void setCursorName(String name) throws SQLException {
    stmt_.setCursorName(name); 
  }

  public boolean execute(String sql) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return stmt_.execute(sql);  
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
    AS400JDBCResultSet rs = (AS400JDBCResultSet) stmt_.getResultSet();
    if (rs != null) { 
       rs.setStatement(this);
    }
    return  rs; 
  }

  public int getUpdateCount() throws SQLException {
    return stmt_.getUpdateCount(); 
  }

  public boolean getMoreResults() throws SQLException {
    return stmt_.getMoreResults(); 
  }

  public void setFetchDirection(int direction) throws SQLException {
    stmt_.setFetchDirection(direction); 
  }

  public int getFetchDirection() throws SQLException {
    return stmt_.getFetchDirection(); 
  }

  public void setFetchSize(int rows) throws SQLException {
    stmt_.setFetchSize(rows);

  }

  public int getFetchSize() throws SQLException {
    return stmt_.getFetchSize(); 
  }

  public int getResultSetConcurrency() throws SQLException {
    return stmt_.getResultSetConcurrency(); 
  }

  public int getResultSetType() throws SQLException {
    return stmt_.getResultSetType(); 
  }

  public void addBatch(String sql) throws SQLException {
    stmt_.addBatch(sql); 
  }

  public void clearBatch() throws SQLException {
    stmt_.clearBatch(); 
  }

  public int[] executeBatch() throws SQLException {
    return stmt_.executeBatch(); 
  }

  public Connection getConnection() throws SQLException {
    return stmt_.getConnection(); 
  }

  public boolean getMoreResults(int current) throws SQLException {
    return stmt_.getMoreResults(current); 
  }

  public ResultSet getGeneratedKeys() throws SQLException {
    return stmt_.getGeneratedKeys(); 
  }

  public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
         return stmt_.executeUpdate(sql,autoGeneratedKeys); 
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
         return stmt_.executeUpdate(sql, columnIndexes); 
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
         return stmt_.executeUpdate(sql, columnNames); 
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
         return stmt_.execute(sql, autoGeneratedKeys); 
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
         return stmt_.execute(sql, columnIndexes); 
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
         return stmt_.execute(sql, columnNames); 
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
    return stmt_.getResultSetHoldability(); 
  }

  
  public boolean isCloseOnCompletion() throws SQLException {
    return ((AS400JDBCStatement)stmt_).isCloseOnCompletion(); 
  }

  public void closeOnCompletion() throws SQLException {
    ((AS400JDBCStatement)stmt_).closeOnCompletion(); 
  }


  
  
      public   long getLargeUpdateCount() throws SQLException {
        return stmt_.getLargeUpdateCount();  
      }
      
      public void setLargeMaxRows(long max) throws SQLException {
        stmt_.setLargeMaxRows(max); 
      }
      public long getLargeMaxRows() throws SQLException {
        return stmt_.getLargeMaxRows(); 
      }
    
    public long[] executeLargeBatch()    throws SQLException  {
      return stmt_.executeLargeBatch(); 
    }

   public long executeLargeUpdate(String sql)   throws SQLException {
     boolean retry = true;
     int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
     while (retry) {
       retry = false;
       try {
         return stmt_.executeLargeUpdate(sql); 
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
          return stmt_.executeLargeUpdate(sql, autoGeneratedKeys); 
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
          return stmt_.executeLargeUpdate(sql, columnIndexes); 
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
          return stmt_.executeLargeUpdate(sql, columnNames); 
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


    public int getPositionOfSyntaxError() throws SQLException {
      return stmt_.getPositionOfSyntaxError(); 
    }

    public boolean isClosed() throws SQLException {
      return stmt_.isClosed(); 
    }

    public String toString() {
      return stmt_.toString(); 
    }

    public void setPoolable(boolean poolable) throws SQLException {
      stmt_.setPoolable(poolable); 
    }

    public boolean isPoolable() throws SQLException {
      return stmt_.isPoolable(); 
    }
               
    void checkOpen() throws SQLException {
        stmt_.checkOpen(); 
      }

      void notifyClose () throws SQLException   {
         stmt_.notifyClose(); 
      }
      
      JDCursor getCursor() {
        return stmt_.getCursor(); 
      }
      
      AS400JDBCStatementLock getInternalLock() {
        return stmt_.getInternalLock(); 
      }

      
      int getInternalResultSetHoldability() {
        return stmt_.getInternalResultSetHoldability(); 
      }

      boolean isQueryRunning() {
        return stmt_.isQueryRunning();  
      }

    int getInternalQueryTimeout() {

      return stmt_.getInternalQueryTimeout();

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
        AS400JDBCResultSet rs = (AS400JDBCResultSet) stmt_.executeQuery();
        if (rs != null) { 
           rs.setStatement(this);
        }
        return rs; 
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
        return stmt_.executeUpdate();
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
    stmt_.setNull(parameterIndex, sqlType);
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    stmt_.setBoolean(parameterIndex, x);

  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    stmt_.setByte(parameterIndex, x);

  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    stmt_.setShort(parameterIndex, x);

  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    stmt_.setInt(parameterIndex, x);

  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    stmt_.setLong(parameterIndex, x);

  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    stmt_.setFloat(parameterIndex, x);

  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    stmt_.setDouble(parameterIndex, x);

  }

  public void setBigDecimal(int parameterIndex, BigDecimal x)
      throws SQLException {
    stmt_.setBigDecimal(parameterIndex, x);

  }

  public void setString(int parameterIndex, String x) throws SQLException {
    stmt_.setString(parameterIndex, x);

  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    stmt_.setBytes(parameterIndex, x);

  }

  public void setDate(int parameterIndex, Date x) throws SQLException {
    stmt_.setDate(parameterIndex, x);

  }

  public void setTime(int parameterIndex, Time x) throws SQLException {
    stmt_.setTime(parameterIndex, x);

  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    stmt_.setTimestamp(parameterIndex, x);

  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    stmt_.setAsciiStream(parameterIndex, x, length);

  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    stmt_.setUnicodeStream(parameterIndex, x, length);

  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length)
      throws SQLException {
    stmt_.setBinaryStream(parameterIndex, x, length);

  }

  public void clearParameters() throws SQLException {
    stmt_.clearParameters();

  }

  public void setObject(int parameterIndex, Object x, int targetSqlType,
      int scale) throws SQLException {
    stmt_.setObject(parameterIndex, x);

  }

  public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException {
    stmt_.setObject(parameterIndex, x, targetSqlType);

  }

  public void setObject(int parameterIndex, Object x) throws SQLException {
    stmt_.setObject(parameterIndex, x);

  }

  public boolean execute() throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retry) {
      retry = false;
      try {
        return stmt_.execute();
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
    stmt_.addBatch();

  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException {
    stmt_.setCharacterStream(parameterIndex, reader, length);

  }

  public void setRef(int parameterIndex, Ref x) throws SQLException {
    stmt_.setRef(parameterIndex, x);

  }

  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    stmt_.setBlob(parameterIndex, x);

  }

  public void setClob(int parameterIndex, Clob x) throws SQLException {
    stmt_.setClob(parameterIndex, x);

  }

  public void setArray(int parameterIndex, Array x) throws SQLException {
    stmt_.setArray(parameterIndex, x);

  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return stmt_.getMetaData(); 
  }

  public void setDate(int parameterIndex, Date x, Calendar cal)
      throws SQLException {
    stmt_.setDate(parameterIndex, x, cal);

  }

  public void setTime(int parameterIndex, Time x, Calendar cal)
      throws SQLException {
    stmt_.setTime(parameterIndex, x, cal);

  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
      throws SQLException {
    stmt_.setTimestamp(parameterIndex, x, cal);

  }

  public void setNull(int parameterIndex, int sqlType, String typeName)
      throws SQLException {
    stmt_.setNull(parameterIndex, sqlType, typeName);

  }

  public void setURL(int parameterIndex, URL x) throws SQLException {
    stmt_.setURL(parameterIndex, x);

  }

  public ParameterMetaData getParameterMetaData() throws SQLException {
    return stmt_.getParameterMetaData();
  }

     
     public void setNClob(int parameterIndex, Reader reader) throws SQLException 
     {
       stmt_.setNClob(parameterIndex, reader); 
     }

    public void setAsciiStream(int parameterIndex, InputStream stream) throws SQLException {
      stmt_.setAsciiStream(parameterIndex, stream); 
    }

    public void setAsciiStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      stmt_.setAsciiStream(parameterIndex,  stream, length); 
      
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream) throws SQLException {
      stmt_.setBinaryStream(parameterIndex,  stream); 
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      stmt_.setBinaryStream(parameterIndex,  stream, length); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream) throws SQLException {
      stmt_.setBlob(parameterIndex,  stream); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      stmt_.setBlob(parameterIndex,  stream, length); 
      
      
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      stmt_.setCharacterStream(parameterIndex, reader); 
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
    stmt_.setCharacterStream(parameterIndex,  reader, length);   
    }

    
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
      stmt_.setClob(parameterIndex, reader); 
      
    }

    
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      stmt_.setClob(parameterIndex, reader, length); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      stmt_.setNCharacterStream(parameterIndex,  reader); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
stmt_.setNCharacterStream(parameterIndex,  reader, length); 
    }

    

    
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      stmt_.setNClob(parameterIndex,  reader, length); 
    }

    
    public void setNString(int parameterIndex, String x) throws SQLException {
      stmt_.setNString(parameterIndex, x); 
    }

    public void setDB2Default(int parameterIndex) throws SQLException {
      stmt_.setDB2Default(parameterIndex); 
    }

    public void setDBDefault(int parameterIndex) throws SQLException {
      stmt_.setDBDefault(parameterIndex); 
      
    }

    public void setDB2Unassigned(int parameterIndex) throws SQLException {
      stmt_.setDB2Unassigned(parameterIndex);
      
      
    }

    public void setDBUnassigned(int parameterIndex) throws SQLException {
      stmt_.setDBUnassigned(parameterIndex);
      
    }

    public String getDB2ParameterName(int parm) throws SQLException {
      return stmt_.getDB2ParameterName(parm);
      
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
      stmt_.setObject(parameterIndex,x,targetSqlType,scaleOrLength); 
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
      stmt_.setObject(parameterIndex,  x, targetSqlType); 
    }
  
    
    

    public long executeLargeUpdate() throws SQLException {
      return stmt_.executeLargeUpdate(); 
    }

    int findParameterIndex(String s) throws SQLException {
      return stmt_.findParameterIndex(s); 
      
    }

    int getParameterCcsid(int p) throws SQLException {
      return stmt_.getParameterCcsid(p); 
      
    }

    String getParameterClassName(int p) throws SQLException {
      return stmt_.getParameterClassName(p); 
      
    }

    int getParameterCount() throws SQLException {
      return stmt_.getParameterCount();
    }

    int getParameterMode(int param) throws SQLException {
      return stmt_.getParameterMode(param); 
    }

    int getParameterType(int param) throws SQLException {
      return stmt_.getParameterType(param); 
    }

    String getParameterTypeName(int param) throws SQLException {
      return stmt_.getParameterTypeName(param); 
    }

    int getPrecision(int param) throws SQLException {
      return stmt_.getPrecision(param); 
    }

    JDServerRow getResultRow() {
      return stmt_.getResultRow(); 
    }

    int getScale(int param) throws SQLException {
      return stmt_.getScale(param); 
    }

    int isNullable(int param) throws SQLException {
      return stmt_.isNullable(param); 
    }

    boolean isSigned(int param) throws SQLException {
      return stmt_.isSigned(param); 
    }

    void setSaveParameterValues(boolean saveParameterValues) {
      stmt_.setSaveParameterValues(saveParameterValues); 
      
    }

    

    
/* ifdef JDBC40


    public void setNClob(int parameterIndex, NClob clob) throws SQLException {
      stmt_.setNClob(parameterIndex,  clob);
    }

    
    
    public void setSQLXML(int parameterIndex, SQLXML xml) throws SQLException {
      stmt_.setSQLXML(parameterIndex, xml); 
    }

    
    
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
      stmt_.setRowId(parameterIndex, x); 
     } 

 
endif */

}
