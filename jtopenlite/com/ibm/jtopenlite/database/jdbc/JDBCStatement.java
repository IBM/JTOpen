///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCStatement.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.Conv;
import com.ibm.jtopenlite.database.*;

import java.io.*;
import java.sql.*;

public class JDBCStatement implements Statement, DatabaseSQLCommunicationsAreaCallback//, DatabaseDescribeCallback
{

	public final static int TYPE_UNKNOWN = 0;
	public final static int TYPE_INSERT_UPDATE_DELETE = 1;
	public final static int TYPE_SELECT = 2;
    public final static int TYPE_CALL   = 3;
    public final static int TYPE_COMMIT = 4;
    public final static int TYPE_ROLLBACK = 5;
    public final static int TYPE_CONNECT  = 6;
    public final static int TYPE_BLOCKED_INSERT = 7;

	  public static int getStatementType(String sql) throws SQLException
	  {
	      // Check for null string
	      if (sql == null) JDBCError.throwSQLException(JDBCError.EXC_SYNTAX_ERROR );

	      String st = sql.toUpperCase().trim();


	      while (st.length() > 0 && st.charAt(0) == '(') st = st.substring(1).trim();
	      int sqlStatementType = st.startsWith("SELECT") || st.startsWith("VALUES") ? TYPE_SELECT :
	                 st.startsWith("INSERT") || st.startsWith("UPDATE") || st.startsWith("DELETE") ? TYPE_INSERT_UPDATE_DELETE :
	                 st.startsWith("CALL") ? TYPE_CALL :
	                 st.startsWith("COMMIT") ? TYPE_COMMIT :
	                 st.startsWith("ROLLBACK") ? TYPE_ROLLBACK :
	                 st.startsWith("CONNECT") || st.startsWith("SET") || st.startsWith("RELEASE") || st.startsWith("DISCONNECT") ? TYPE_CONNECT :
	                 st.startsWith("BLOCKED INSERT") ? TYPE_BLOCKED_INSERT : TYPE_UNKNOWN;


	      // Check for statement too long
	      if (st.length() > (2097152 /* maximum bytes */ / 2)) {
	    	  JDBCError.throwSQLException(JDBCError.EXC_SQL_STATEMENT_TOO_LONG);
	      }
	      return sqlStatementType;
	  }






  JDBCConnection conn_;
  DatabaseRequestAttributes statementAttributes_;    // Used to hold statement type
  DatabaseRequestAttributes attribs_;
  int rpbID_;
  int fetchSize_;

  String cursorName_ = null;
  JDBCResultSet currentResultSet_;
  boolean closed_;

  String generatedKey_;
  int updateCount_ = -1;
  int lastUpdateCount_ = -1;
  int resultSetsCount_ = -1;
  int lastSQLCode_;
  String lastSQLState_;

  String statementName_ = null;
  String catalog_ = null;
  boolean poolable_  = false;  /* Default is false */

  public JDBCStatement(JDBCConnection conn, String statementName,
			String cursorName, int rpbID) throws SQLException {
	  cursorName_ = cursorName;
	  statementName_ = statementName;
		if (rpbID != 0) {
			DatabaseRequestAttributes rpb = new DatabaseRequestAttributes();
			rpb.setCursorName(cursorName);
			rpb.setPrepareStatementName(statementName);
			conn.createRequestParameterBlock(rpb, rpbID);
			attribs_ = rpb;
			statementAttributes_ = rpb.copy();

		}
		conn_ = conn;
		rpbID_ = rpbID;
	}

  public void newSQLCommunicationsAreaData(int sqlCode, String sqlState, String generatedKey, int updateCount, int resultSetsCount)
  {
    if (sqlCode == 0)
    {
      generatedKey_ = generatedKey;
    }
    else
    {
      generatedKey_ = null;
    }
    lastUpdateCount_ = updateCount;
    lastSQLCode_ = sqlCode;
    lastSQLState_ = sqlState;
    resultSetsCount_ = resultSetsCount;
  }

  int getLastSQLCode()
  {
    return lastSQLCode_;
  }

  String getLastSQLState()
  {
    return lastSQLState_;
  }

  DatabaseConnection getDatabaseConnection()
  {
    return conn_.getDatabaseConnection();
  }

  DatabaseRequestAttributes getRequestAttributes()
  {
    attribs_.clear();
    return attribs_;
  }

  /**
   * Not implemented.
  **/
  public void addBatch(String sql) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void cancel() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void clearBatch() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Warning are not supported.  This is a noop.
  **/
  public void clearWarnings() throws SQLException
  {

  }

  public void close() throws SQLException
  {
    if (closed_) return;
    try
    {
      generatedKey_ = null;
      if (currentResultSet_ != null)
      {
        currentResultSet_.close();
        currentResultSet_ = null;
      }
      attribs_.clear();
      conn_.getDatabaseConnection().deleteRequestParameterBlock(attribs_, rpbID_);
      conn_.freeRPBID(rpbID_);

      if (statementName_ != null) {
        conn_.freeStatementAndCursorNames(statementName_, cursorName_);
      }
      statementName_ = null;
      cursorName_ = null;
    }
    catch (IOException io)
    {
      throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
    }
    finally
    {
      closed_ = true;
    }
  }

  public boolean execute(String sql) throws SQLException
  {
    return execute(sql, Statement.NO_GENERATED_KEYS);
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
  {
    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (currentResultSet_ != null)
    {
      currentResultSet_.close();
      currentResultSet_ = null;
    }

    int statementType = getStatementType(sql);
    if (statementType == TYPE_SELECT) {
    	currentResultSet_ = (JDBCResultSet) executeQuery(sql);
    	return true;
    }


    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
    DatabaseExecuteImmediateAttributes deia = getRequestAttributes();//conn_.getRequestAttributes();
    deia.setSQLStatementText(sql);

    if (statementType == TYPE_CALL) {
      deia.setSQLStatementType(TYPE_CALL);
      deia.setOpenAttributes(0x80);  /* READ */
      deia.setPrepareOption(0);      /* normal prepare */
    }
/*    switch (autoGeneratedKeys)
    {
      case Statement.NO_GENERATED_KEYS:
        conn.setSQLCommunicationsAreaCallback(null);
        break;
      case Statement.RETURN_GENERATED_KEYS:
        conn.setSQLCommunicationsAreaCallback(this);
        break;
      default:
        throw new SQLException("Bad value for autoGeneratedKeys parameter");
    }
*/
    boolean resultSetAvailable = false;
    try
    {
      conn.setCurrentRequestParameterBlockID(rpbID_);
      generatedKey_ = null;
      conn.executeImmediate(deia);
      updateCount_ = lastUpdateCount_;
      //
      // Todo:  Need to check for result sets
      //
      if ( resultSetsCount_ > 0) {
    	  resultSetAvailable = true;
  	    DatabaseOpenAndDescribeAttributes oada = getRequestAttributes();


	    oada.setOpenAttributes(0x80);
	    oada.setScrollableCursorFlag(0);
	    oada.setVariableFieldCompression(0xe8);
	      if (catalog_ == null) {
	    	  catalog_ = conn_.getCatalog();
	      }

	    JDBCResultSetMetaData md = new JDBCResultSetMetaData(conn.getInfo().getServerCCSID(), conn_.getCalendar(), catalog_);

	    try  {
	      conn.setCurrentRequestParameterBlockID(rpbID_);
	      if (currentResultSet_ != null)     {
	        currentResultSet_.close();
	        currentResultSet_ = null;
	      }
	      conn.openAndDescribe(oada, md);
	    } catch (IOException io)  {
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
    return resultSetAvailable;
  }

  /**
   * Not implemented.
  **/
  public boolean execute(String sql, int[] columnIndices) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean execute(String sql, String[] columnNames) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public int[] executeBatch() throws SQLException
  {
    throw new NotImplementedException();
  }

  public ResultSet executeQuery(String sql) throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    if (currentResultSet_ != null)
    {
      currentResultSet_.close();
      currentResultSet_ = null;
    }
    if (catalog_ == null) {
  	  catalog_ = conn_.getCatalog();
    }

    DatabaseConnection conn = conn_.getDatabaseConnection();
    conn.setSQLCommunicationsAreaCallback(this);
//      DatabasePrepareAttributes pea = getRequestAttributes(); //conn_.getRequestAttributes();
    DatabasePrepareAndDescribeAttributes pea = getRequestAttributes();
    pea.setExtendedSQLStatementText(sql);
    //      pea.setPrepareOption(0x01); // Enhanced.


    // Verify that the statement could return a result set

    int statementType = getStatementType(sql);
    switch (statementType) {
    	case TYPE_SELECT:
    		// Only request extended column descriptor for select statements (not call )
    	    pea.setOpenAttributes(0x80);
            pea.setExtendedColumnDescriptorOption(0xF1);
    	case TYPE_CALL:
    		break;
    	default:
    		// Not a query -- throw an exception
    		throw new SQLException("Not a query");
    }
    pea.setSQLStatementType(statementType); // SELECT. This has to be set in order to get extended column metadata back.
    try
    {
      conn.setCurrentRequestParameterBlockID(rpbID_);
      generatedKey_ = null;

//        conn.prepare(pea);
      JDBCResultSetMetaData md = new JDBCResultSetMetaData(conn.getInfo().getServerCCSID(), conn_.getCalendar(), catalog_);
      conn.prepareAndDescribe(pea, md, null); // Just a plain prepare doesn't give us extended column metadata back.


      if (statementType == TYPE_SELECT) {


	        DatabaseOpenAndDescribeAttributes oada = (DatabaseOpenAndDescribeAttributes)pea;
	        if (fetchSize_ > 0) oada.setBlockingFactor(fetchSize_);
	        oada.setDescribeOption(0xD5);
	        oada.setScrollableCursorFlag(0);
	        oada.setVariableFieldCompression(0xe8);
	        conn.openAndDescribe(oada, null);
	        currentResultSet_ = new JDBCResultSet(this, md, statementName_, cursorName_, fetchSize_);
	        updateCount_ = -1;
	        return currentResultSet_;
      } else {


    	    DatabaseExecuteAttributes dea = getRequestAttributes();

    	    // Flags set by normal toolbox
    	    ((DatabaseOpenAndDescribeAttributes)dea).setScrollableCursorFlag(0);
    	    ((DatabaseOpenAndDescribeAttributes)dea).setResultSetHoldabilityOption(0xe8); /* Y */
    	    ((DatabaseOpenAndDescribeAttributes)dea).setVariableFieldCompression(0xe8);
    	    if (fetchSize_ > 0) ((DatabaseOpenAndDescribeAttributes)dea).setBlockingFactor(fetchSize_);


    	    dea.setSQLStatementType(JDBCStatement.TYPE_CALL);

    	    conn.execute(dea);


    	    // TODO:  Determine if result set is available from the call.  If so, then call openDescribe using the existing cursor name if it exists
    	    if (resultSetsCount_ > 0) {

    	    	    DatabaseOpenAndDescribeAttributes oada = getRequestAttributes();


    	    	    oada.setOpenAttributes(0x80);
    	    	    oada.setScrollableCursorFlag(0);
    	    	    oada.setVariableFieldCompression(0xe8);
    	    	      if (catalog_ == null) {
    	    	    	  catalog_ = conn_.getCatalog();
    	    	      }

    	    	    md = new JDBCResultSetMetaData(conn.getInfo().getServerCCSID(), conn_.getCalendar(), catalog_);

		    conn.setCurrentRequestParameterBlockID(rpbID_);
		    if (currentResultSet_ != null)
		    {
			currentResultSet_.close();
			currentResultSet_ = null;
		    }
		    conn.openAndDescribe(oada, md);

    	    	    currentResultSet_ = new JDBCResultSet(this, md, statementName_, cursorName_, fetchSize_);
    	          updateCount_ = -1;

    	    	    return currentResultSet_;
    	    } else {
    	    	// Did not return result set
    	    	JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    	    	return null;
    	    }



      }
      }
      catch (IOException io)
      {
	  throw JDBCConnection.convertException(io, lastSQLCode_, lastSQLState_);
      }
  }

  public int executeUpdate(String sql) throws SQLException
  {
    return executeUpdate(sql, Statement.NO_GENERATED_KEYS);
  }

  public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
		if (currentResultSet_ != null) {
			currentResultSet_.close();
			currentResultSet_ = null;
		}
		// Check statement
		getStatementType(sql);

		DatabaseConnection conn = conn_.getDatabaseConnection();
		conn.setSQLCommunicationsAreaCallback(this);
		DatabasePrepareAndExecuteAttributes pea = getRequestAttributes();
		pea.setExtendedSQLStatementText(sql);
		pea.setOpenAttributes(0x80);
		pea.setDescribeOption(0xD5);
		pea.setScrollableCursorFlag(0);

		/*
		 * switch (autoGeneratedKeys) { case Statement.NO_GENERATED_KEYS:
		 * conn.setSQLCommunicationsAreaCallback(null); break; case
		 * Statement.RETURN_GENERATED_KEYS:
		 * conn.setSQLCommunicationsAreaCallback(this); break; default: throw
		 * new SQLException("Bad value for autoGeneratedKeys parameter"); }
		 */
		try {
			conn.setCurrentRequestParameterBlockID(rpbID_);
			generatedKey_ = null;
			updateCount_ = 0;
			conn.setSQLCommunicationsAreaCallback(this);
			try {
				conn.prepareAndExecute(pea, null);
				updateCount_ = lastUpdateCount_;
			} finally {
				// conn.setSQLCommunicationsAreaCallback(null);
			}
		} catch (IOException io) {
			throw JDBCConnection.convertException(io, lastSQLCode_,
					lastSQLState_);
		} finally {
			// conn.setSQLCommunicationsAreaCallback(null);
		}
		return updateCount_;
	}

  /**
   * Not implemented.
  **/
  public int executeUpdate(String sql, int[] columnIndices) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public int executeUpdate(String sql, String[] columnNames) throws SQLException
  {
    throw new NotImplementedException();
  }

  public Connection getConnection() throws SQLException
  {
      // Return connection -- even if closed
      // if (closed_) throw new SQLException("Statement closed");
      //
    return conn_;
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

  public ResultSet getGeneratedKeys() throws SQLException
  {
    if (generatedKey_ != null)
    {
      // The SQLCA contains a 30-digit packed decimal, but I'm always going to treat
      // this as a long, until I see a case where a generated key is bigger than a long.
        if (catalog_ == null) {
      	  catalog_ = conn_.getCatalog();
        }

      JDBCResultSetMetaData md = new JDBCResultSetMetaData(37, conn_.getCalendar(), catalog_);
      md.resultSetDescription(1, 0, 0, 0, 0, 8);
      md.fieldDescription(0, 492, 8, 0, 0, 0, 0, 0, 0); // BIGINT
      md.fieldName(0, "GENERATED_KEY");

      JDBCResultSet rs = new JDBCResultSet(this, md, null, null, 0);
      rs.newResultData(1, 1, 8);
      rs.newRowData(0, Conv.longToByteArray(new Long(generatedKey_).longValue()));

      return rs;
    }
    return null;
  }

  /**
   * Retrieves the maximum number of bytes that can be returned for character and binary column values in a ResultSet object produced by this Statement object.
   *
  **/
  public int getMaxFieldSize() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return 0;
  }

  /**
   * Retrieves the maximum number of rows that a ResultSet object produced by this Statement object can contain.
   * @returns 0 -- there is no limit
  **/
  public int getMaxRows() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return 0;
  }

  /**
   * Not implemented.
  **/
  public boolean getMoreResults() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public boolean getMoreResults(int current) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Retrieves the number of seconds the driver will wait for a Statement object to execute
   * @returns 0:  This driver does not support query timeout. .
  **/
  public int getQueryTimeout() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return 0;
  }

  public ResultSet getResultSet() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return currentResultSet_;
  }

  /**
   * Retrieves the result set concurrency for ResultSet objects generated by this Statement object.
   * @returns  ResultSet.CONCUR_READ_ONLY: This driver only supports READ_ONLY cursors.
  **/
  public int getResultSetConcurrency() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return ResultSet.CONCUR_READ_ONLY;
  }

  /**
   * Retrieves the result set holdability for ResultSet objects generated by this Statement object.
   * returns ResultSet.HOLD_CURSORS_OVER_COMMIT
  **/
  public int getResultSetHoldability() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  public int getResultSetType() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return ResultSet.TYPE_FORWARD_ONLY;
  }

  public int getUpdateCount() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
    return updateCount_;
  }

  /**
   * Not implemented, but we return null to avoid problems with existing applications
  **/
  public SQLWarning getWarnings() throws SQLException
  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	  return null;

  }

  /**
   * Not implemented.
  **/
  public void setCursorName(String name) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setEscapeProcessing(boolean enable) throws SQLException
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
  public void setMaxFieldSize(int max) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setMaxRows(int max) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void setQueryTimeout(int seconds) throws SQLException
  {
    throw new NotImplementedException();
  }

  public boolean isClosed() {
      return closed_;
  }

  protected void setCursorNameInternal(String cursorName) {
	  cursorName_ = cursorName;
  }

  public boolean isPoolable() throws SQLException  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
	  return poolable_;
  }

  public void setPoolable(boolean poolable) throws SQLException  {
	    if (closed_) JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_SEQUENCE);
      poolable_ = poolable;
  }

  public void setCatalog(String catalog) {
    catalog_ = catalog;
  }






}
