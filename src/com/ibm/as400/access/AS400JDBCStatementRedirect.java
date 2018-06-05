package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class AS400JDBCStatementRedirect 
/* ifdef JDBC40  
extends ToolboxWrapper
 endif */
implements Statement {
   Statement stmt_; 
   AS400JDBCConnection connection_; 
   
   AS400JDBCStatementRedirect(Statement stmt) throws SQLException {
     stmt_ = stmt; 
     connection_ = (AS400JDBCConnection) stmt.getConnection(); 
   }
  
  public ResultSet executeQuery(String sql) throws SQLException {
    boolean retry = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT; 
    while (retry) {
      retry = false;
      try {
        return stmt_.executeQuery(sql); 
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
    return stmt_.getResultSet(); 
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




  /* ifdef JDBC40

  public boolean isClosed() throws SQLException {
    return stmt_.isClosed(); 
  }

  public boolean isPoolable() throws SQLException {
    return stmt_.isPoolable(); 
  }
  public void setPoolable(boolean x) throws SQLException {
    stmt_.setPoolable(x); 
    
  }
  endif */ 
}
