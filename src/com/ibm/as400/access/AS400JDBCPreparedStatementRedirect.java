package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class AS400JDBCPreparedStatementRedirect extends
    AS400JDBCStatementRedirect implements PreparedStatement {

  PreparedStatement pstmt_ = null;

  AS400JDBCPreparedStatementRedirect(PreparedStatement stmt)
      throws SQLException {
    super(stmt);
    pstmt_ = stmt;
  }

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
       ((AS400JDBCPreparedStatement)pstmt_).setNClob(parameterIndex, reader); 
     }

    public void setAsciiStream(int parameterIndex, InputStream stream) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setAsciiStream(parameterIndex, stream); 
    }

    public void setAsciiStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setAsciiStream(parameterIndex,  stream, length); 
      
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setBinaryStream(parameterIndex,  stream); 
    }

    
    public void setBinaryStream(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setBinaryStream(parameterIndex,  stream, length); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setBlob(parameterIndex,  stream); 
    }

    
    public void setBlob(int parameterIndex, InputStream stream, long length)
        throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setBlob(parameterIndex,  stream, length); 
      
      
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setCharacterStream(parameterIndex, reader); 
    }

    
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
    ((AS400JDBCPreparedStatement)pstmt_).setCharacterStream(parameterIndex,  reader, length);   
    }

    
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setClob(parameterIndex, reader); 
      
    }

    
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setClob(parameterIndex, reader, length); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setNCharacterStream(parameterIndex,  reader); 
    }

    
    public void setNCharacterStream(int parameterIndex, Reader reader, long length)
        throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setNCharacterStream(parameterIndex,  reader, length); 
    }

    

    
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setNClob(parameterIndex,  reader, length); 
    }

    
    public void setNString(int parameterIndex, String x) throws SQLException {
      ((AS400JDBCPreparedStatement)pstmt_).setNString(parameterIndex, x); 
      
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
