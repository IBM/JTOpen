///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.About;
import com.ibm.jtopenlite.Message;
import com.ibm.jtopenlite.MessageException;
import com.ibm.jtopenlite.database.*;

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
<p>The JDBCConnection class provides a JDBC connection
to a specific DB2 for IBM i database.  Use
DriverManager.getConnection() with a jdbc:jtopenlite://SYSTENAME URL to create AS400JDBCConnection
objects.
**/
public class JDBCConnection implements java.sql.Connection, DatabaseWarningCallback
{
  private DatabaseConnection conn_;

  private int statementCounter_ = 0;
  private int cursorCounter_ = 0;
  private int descriptorCounter_ = 0;
  private final char[] statementName_ = new char[] { 'S', 'T', 'M', 'T', '0', '0', '0', '0', '0', '0', '0', '0' };
  private final char[] cursorName_ = new char[] { 'C', 'R', 'S', 'R', '0', '0', '0', '0', '0', '0', '0', '0' };
  private static final char[] CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  private int lastWarningClass_;
  private int lastWarningReturnCode_;
  private String lastWarningMessageID_;
  private String lastWarningMessageText_;
  private String lastWarningSecondLevelText_;
  private String catalog_ = null;
  private int serverVersion_;
  private String serverJobIdentifier_;

  private boolean autoCommit_ = true;         /* JDBC view of autocommit */
  private int     transactionIsolation_ = Connection.TRANSACTION_NONE;  /* JDBC view of transaction isolation level */
                                          /* None means that transaction isolation has not been set */
  private final boolean[] usedRPBs_ = new boolean[32768];

  private final Calendar calendar_ = Calendar.getInstance(); // Master calendar used for conversions.

  private String userName_;

  private Vector freeStatements_ = new Vector();
  private Vector freeCursors_ = new Vector();


  private JDBCConnection(DatabaseConnection conn)
  {
    conn_ = conn;
  }

  Calendar getCalendar()
  {
    return calendar_;
  }

  synchronized String getNextStatementName()
  {
	  int freeStatementCount = freeStatements_.size();
	  if (freeStatementCount > 0) {
		  return (String) freeStatements_.remove(freeStatementCount - 1);
	  }
      statementCounter_ = statementCounter_ == 0x7FFFFFFF ? 1 : statementCounter_+1;
      int counter = statementCounter_;
      for (int i=11; i>=4; --i)
      {
        statementName_[i] = CHARS[counter & 0x0F];
        counter = counter >> 4;
      }
      return new String(statementName_);
  }

  synchronized void freeStatementAndCursorNames(String statementName, String cursorName) throws SQLException
  {
	  if (statementName != null ) {
		  freeStatements_.addElement(statementName);
	  }
	  if (cursorName != null ) {
		  freeCursors_.addElement(cursorName);
	  }
  }



  synchronized String getNextCursorName()
  {
	  int freeCursorCount = freeCursors_.size();
	  if (freeCursorCount > 0) {
		  return (String) freeCursors_.remove(freeCursorCount - 1);
	  }

	  cursorCounter_ = cursorCounter_ == 0x7FFFFFFF ? 1 : cursorCounter_+1;
      int counter = cursorCounter_;
      for (int i=11; i>=4; --i)
      {
        cursorName_[i] = CHARS[counter & 0x0F];
        counter = counter >> 4;
      }
      return new String(cursorName_);
  }

  int getNextDescriptorHandle()
  {
      descriptorCounter_ = descriptorCounter_ == 0x7FFF ? 1 : descriptorCounter_+1;
      return descriptorCounter_;
    }

  synchronized int getNextRPBID()
  {
      // 0 is the default RPB on the server.
      for (int i=1; i<usedRPBs_.length; ++i)
      {
        if (!usedRPBs_[i])
        {
          usedRPBs_[i] = true;
          return i;
        }
      }
    return -1;
  }

  synchronized void freeRPBID(int id)
  {
      usedRPBs_[id] = false;
  }

  public void newWarning(int rcClass, int rcClassReturnCode)
  {
    lastWarningClass_ = rcClass;
    lastWarningReturnCode_ = rcClassReturnCode;
  }

  public void noWarnings()
  {
    lastWarningClass_ = 0;
    lastWarningReturnCode_ = 0;
    lastWarningMessageID_ = null;
    lastWarningMessageText_ = null;
    lastWarningSecondLevelText_ = null;
  }

  public void newMessageID(String id)
  {
    lastWarningMessageID_ = id;
  }

  public void newMessageText(String text)
  {
    lastWarningMessageText_ = text;
  }

  public void newSecondLevelText(String text)
  {
    lastWarningSecondLevelText_ = text;
  }

  int getLastWarningClass()
  {
    return lastWarningClass_;
  }

  int getLastWarningReturnCode()
  {
    return lastWarningReturnCode_;
  }

  Message getLastWarningMessage()
  {
    if (lastWarningMessageID_ != null)
    {
      return new Message(lastWarningMessageID_, lastWarningMessageText_);
    }
    return null;
  }

  public static JDBCConnection getConnection(String system, String user, String password, boolean debug) throws SQLException
  {
    try
    {
      DatabaseConnection conn = DatabaseConnection.getConnection(system, user, password);
      conn.setDebug(debug);
      DatabaseServerAttributes dsa = new DatabaseServerAttributes();
      dsa.setNamingConventionParserOption(0);
      dsa.setUseExtendedFormats(0xF2);
      dsa.setDefaultClientCCSID(13488);
      dsa.setDateFormatParserOption(5); // ISO.
      dsa.setLOBFieldThreshold(1024*1024); // Use a locator for any LOB data fields longer than 1 MB.
      dsa.setClientSupportInformation(0x40000000);  // Client supports True autocommit
      dsa.setInterfaceType("JDBC");
      dsa.setInterfaceName(About.INTERFACE_NAME);
      dsa.setInterfaceLevel(About.INTERFACE_LEVEL);

      //
      // Do not set any commitment control levels.
      // The default behavior is true auto commit = off
      // and a transaction isolation of *NONE.
      // This means that connection behaves like autocommit = on.
      //

      conn.setServerAttributes(dsa);

      //
      // Remember the serverJobIdentifier
      //


//      DatabaseRequestAttributes rpb = new DatabaseRequestAttributes();
//      conn.createRequestParameterBlock(rpb);
//      JDBCConnection j = new JDBCConnection(conn, rpb);
      JDBCConnection j = new JDBCConnection(conn);
      conn.setWarningCallback(j);
      j.serverJobIdentifier_ = dsa.getServerJobName()+dsa.getServerJobUser() + dsa.getServerJobNumber();
      return j;
    }
    catch (IOException io)
    {
      throw convertException(io);
    }
  }

  DatabaseConnection getDatabaseConnection()
  {
    return conn_;
  }

  static SQLException convertException(IOException io)
  {
    return convertException(io, -99999, "");
  }

  static SQLException convertException(IOException io, int sqlCode, String sqlState)
  {
    SQLException sql = null;
    if (io instanceof MessageException)
    {
      MessageException me = (MessageException)io;
      Message[] messages = me.getMessages();
      String reason = messages[0].toString();
      sql = new SQLException(reason, sqlState, sqlCode);
      sql.initCause(io);

    }
    else
    {
      String reason = io.toString();
      sql = new SQLException(reason, sqlState, sqlCode);
      sql.initCause(io);
    }

    return sql;
  }

  public void clearWarnings() throws SQLException
  {
    noWarnings();
  }

  public void close() throws SQLException
  {
    if (isClosed()) return;
    try
    {
      conn_.close();
    }
    catch (IOException io)
    {
      throw convertException(io);
    }

  }

  private void  checkOpen() throws SQLException {
      if (isClosed()) {
	  throw JDBCError.getSQLException(JDBCError.EXC_CONNECTION_NONE);
      }
  }

  /**
   * Commit the current transaction.
  **/
  public void commit() throws SQLException
  {
    try
    {
      conn_.commit();
    }
    catch (IOException io)
    {
      throw convertException(io);
    }
  }

  public Statement createStatement() throws SQLException
  {
      String stName = getNextStatementName();
      int rpbId = getNextRPBID();
      String cursorName = getNextCursorName();
      return new JDBCStatement(this,  stName, cursorName, rpbId);
  }


  /**
   * Only valid for ResultSet.TYPE_FORWARD_ONLY and ResultSet.CONCUR_READ_ONLY.
  **/
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
  {
	  // Just create the statement if the setting match the default settings
      if (resultSetType == ResultSet.TYPE_FORWARD_ONLY &&
	  resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)    {

	  return createStatement();
      }

    throw new NotImplementedException();
  }

  /**
   * Only valid for ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, and ResultSet.HOLD_CURSORS_OVER_COMMIT.
  **/
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
	/* if default values are used, then just call the one with default values */
	if ((resultSetType == ResultSet.TYPE_FORWARD_ONLY) &&
			(resultSetConcurrency == ResultSet.CONCUR_READ_ONLY) &&
			(resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT )) {
		return createStatement();
	}
    throw new NotImplementedException();
  }

  /**
   * Return the autocommit setting.
  **/
  public boolean getAutoCommit() throws SQLException
  {
      checkOpen();
    return autoCommit_;
  }

  /**
   * Not implemented.
  **/
  public String getCatalog() throws SQLException
  {
      if (catalog_ == null) {
	  JDBCStatement stmt = (JDBCStatement) createStatement();
	  stmt.setCatalog("LOCAL");
	  ResultSet rs = stmt.executeQuery("select CATALOG_NAME from qsys2.syscatalogs where RDBTYPE='LOCAL'");
	  rs.next();
	  catalog_ = rs.getString(1);
	  rs.close();
	  stmt.close();
      }
      return catalog_;
  }

  /**
   * The holdability is always ResultSet.HOLD_CURSORS_OVER_COMMIT.
  **/
  public int getHoldability() throws SQLException
  {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  /**
   * Returns the metadata for this connection.
  **/
  public DatabaseMetaData getMetaData() throws SQLException
  {
      return new JDBCDatabaseMetaData(this);

 }

  /**
   * Not implemented.
  **/
  public int getTransactionIsolation() throws SQLException
  {
      // NONE is not really a valid JDBC option
      if (transactionIsolation_ == Connection.TRANSACTION_NONE) {
	  return Connection.TRANSACTION_READ_UNCOMMITTED;
      }
      return transactionIsolation_;
  }

//  public Map<String,Class<?>> getTypeMap() throws SQLException
  /**
   * Not implemented.
  **/
  public Map getTypeMap() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Returns null because get warnings is not implemented on this driver.
   *
  **/
  public SQLWarning getWarnings() throws SQLException
  {
    return null;
  }

  public boolean isClosed() throws SQLException
  {
    return conn_.isClosed();
  }

  /**
   * Not implemented.
  **/
  public boolean isReadOnly() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public String nativeSQL(String sql) throws SQLException
  {
    throw new NotImplementedException();
  }

  public CallableStatement prepareCall(String sql) throws SQLException {

		String stName = getNextStatementName();
		int rpbId = getNextRPBID();
		String cursorName = getNextCursorName();

		JDBCCallableStatement cs = new JDBCCallableStatement(this, sql,
				calendar_, stName, cursorName, rpbId);
		cs.setCursorNameInternal(cursorName);
		return cs;

	}

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
	if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY) {
		return prepareCall(sql);
	} else {
       throw new NotImplementedException();
	}
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
		if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY) {
			return prepareCall(sql);
		} else {
	       throw new NotImplementedException();
		}
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    return prepareStatement(sql, Statement.NO_GENERATED_KEYS);
  }



  public synchronized PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
  {
      String stName = getNextStatementName();
      int rpbId = getNextRPBID();
      String cursorName = getNextCursorName();

        JDBCPreparedStatement ps = new JDBCPreparedStatement(this, sql, calendar_, stName, cursorName, rpbId);
        ps.setCursorNameInternal(cursorName);
        switch (autoGeneratedKeys)
        {
          case Statement.NO_GENERATED_KEYS:
            ps.setReturnGeneratedKeys(false);
            break;
          case Statement.RETURN_GENERATED_KEYS:
            ps.setReturnGeneratedKeys(true);
            break;
          default:
            throw new SQLException("Bad value for autoGeneratedKeys parameter");
        }
        return ps;
  }

  /**
   * Not implemented.
  **/
  public PreparedStatement prepareStatement(String sql, int[] columnIndices) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Only implemented for ResultSet.TYPE_FORWARD_ONLY and ResultSet.CONCUR_READ_ONLY.
  **/
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
	/* if default values are used, then just call the one with default values */
	if ((resultSetType == ResultSet.TYPE_FORWARD_ONLY) &&
	    (resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)) {
	    return prepareStatement(sql);
	} else {
	    throw new NotImplementedException();
	}
  }

  /**
   * Only implemented for ResultSet.TYPE_FORWARD_ONLY and ResultSet.CONCUR_READ_ONLY and ResultSet.HOLD_CURSORS_OVER_COMMIT.
  **/
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
	if ((resultSetType == ResultSet.TYPE_FORWARD_ONLY) &&
			(resultSetConcurrency == ResultSet.CONCUR_READ_ONLY) &&
			(resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT )) {
	    return prepareStatement(sql);
	} else {
	    throw new NotImplementedException();
	}
  }

  /**
   * Not implemented.
  **/
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void releaseSavepoint(Savepoint savepoint) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Rollback the current transaction.
  **/
  public void rollback() throws SQLException
  {
    try
    {
      conn_.rollback();
    }
    catch (IOException io)
    {
      throw convertException(io);
    }
  }

  /**
   * Not implemented.
  **/
  public void rollback(Savepoint savepoint) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Set the autocommit level.
  **/
  public void setAutoCommit(boolean autoCommit) throws SQLException
  {
      checkOpen();
      if (autoCommit) {
	  // Turn on auto commit
	  if (autoCommit_) {
	      // autoCommit already set .. ignore
	  } else {
	      // Changing from autocommit off to autocommit on
	      // Call commit to commit existing work
	      commit();
	      // set the transaction isolation level to none.
	      transactionIsolation_ = Connection.TRANSACTION_NONE;
	      updateServerTransactionAttributes();
	      // For now, we don't worry about supporting true autocommit
	      autoCommit_ = true;
	  }
      } else {
	  // Turn off autocommit
	  if (!autoCommit_) {
	      // autocommit if already off ignore
	  } else {
	      // If the transaction isolation level is NONE, bump it to
	      // *CHG
	      if (transactionIsolation_ == Connection.TRANSACTION_NONE) {
 		     transactionIsolation_ = Connection.TRANSACTION_READ_UNCOMMITTED;
	      }
	      updateServerTransactionAttributes();
	      autoCommit_ = false;
	  }
      }
  }

  private void updateServerTransactionAttributes() throws SQLException {
      try {
	  DatabaseServerAttributes dsa = new DatabaseServerAttributes();
	  int lipiTransactionOption  = getLipiTransactionOption();
	  dsa.setCommitmentControlLevelParserOption(lipiTransactionOption);
	  if (lipiTransactionOption == DatabaseServerAttributes.CC_NONE) {
	      // Make sure true auto commit is off
	      dsa.setTrueAutoCommitIndicator(DatabaseServerAttributes.AUTOCOMMIT_OFF);
	  } else {
	      if (autoCommit_) {
 		    dsa.setTrueAutoCommitIndicator(DatabaseServerAttributes.AUTOCOMMIT_ON);
	      } else {
		    dsa.setTrueAutoCommitIndicator(DatabaseServerAttributes.AUTOCOMMIT_OFF);
	      }
	  }
	  conn_.setServerAttributes(dsa);
      }
      catch (IOException io)
      {
	  throw convertException(io);
      }
  }

  private int getLipiTransactionOption() throws SQLException {
      switch(transactionIsolation_) {
	  case Connection.TRANSACTION_NONE:
	      return DatabaseServerAttributes.CC_NONE;
	  case Connection.TRANSACTION_READ_UNCOMMITTED:
	      return DatabaseServerAttributes.CC_CHG;
	  case Connection.TRANSACTION_READ_COMMITTED:
	      return DatabaseServerAttributes.CC_CS;
	  case Connection.TRANSACTION_REPEATABLE_READ:
	      return DatabaseServerAttributes.CC_RR;
	  case Connection.TRANSACTION_SERIALIZABLE:
	      return DatabaseServerAttributes.CC_ALL;
      }
      throw JDBCError.getSQLException(JDBCError.EXC_ATTRIBUTE_VALUE_INVALID);
  }



  /**
   * Not implemented.
  **/
  public void setCatalog(String catalog) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * The only allowed holdability is to hold cursors over commit.
  **/
  public void setHoldability(int holdability) throws SQLException
  {
    if (holdability != ResultSet.HOLD_CURSORS_OVER_COMMIT)
    {
	  throw new NotImplementedException();
      }
  }

  /**
   * Not implemented.
  **/
  public void setReadOnly(boolean readOnly) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public Savepoint setSavepoint() throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public Savepoint setSavepoint(String name) throws SQLException
  {
    throw new NotImplementedException();
  }

  /**
   * sets the isolation level used by the connection.
  **/
  public void setTransactionIsolation(int level) throws SQLException
  {
      switch (level) {
	  case Connection.TRANSACTION_NONE:
	  case Connection.TRANSACTION_READ_UNCOMMITTED:
	  case Connection.TRANSACTION_READ_COMMITTED:
	  case Connection.TRANSACTION_REPEATABLE_READ:
	  case Connection.TRANSACTION_SERIALIZABLE:
	      transactionIsolation_ = level;
	      updateServerTransactionAttributes();
	      return;
      }

      throw JDBCError.getSQLException(JDBCError.EXC_ATTRIBUTE_VALUE_INVALID);
  }

  /**
   * Not implemented.
  **/
  public void setTypeMap(Map map) throws SQLException
  {
    throw new NotImplementedException();
  }
  /**
   * Return the version level.  See SystemInfo.VERSION_VxRx constants for possible values.
   * @return
   */
  protected int getServerVersion() {
	 if (serverVersion_ == 0) {
		 serverVersion_ = conn_.getInfo().getServerVersion();
	 }
	 return serverVersion_;
  }

public String getUserName() throws SQLException  {
	checkOpen();
	if (userName_ == null) {
		userName_ = conn_.getUser();
	}
	return userName_;
}

public String getURL() {
	return JDBCDriver.URL_PREFIX_ + conn_.getInfo().getSystem();
}

public String getServerJobIdentifier() {
    return serverJobIdentifier_;
}

void createRequestParameterBlock(DatabaseRequestAttributes rpb,
		int rpbId) throws SQLException  {
	try {
	conn_.createRequestParameterBlock(rpb, rpbId);
	} catch (IOException io) {
      throw convertException(io);
    }

}

void prepareAndDescribe(DatabaseRequestAttributes attribs, DatabaseDescribeCallback listener,
		JDBCParameterMetaData pmd) throws SQLException {
	try {
	conn_.prepareAndDescribe(attribs, listener, pmd);
	} catch (IOException io) {
	      throw convertException(io);
	    }

}

void changeDescriptor(DatabaseChangeDescriptorAttributes cda, int handle) throws SQLException {
	try {
		conn_.changeDescriptor(cda, handle);
	} catch (IOException io) {
	      throw convertException(io);
	}
}


public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
  throw new NotImplementedException();
}


public Blob createBlob() throws SQLException {
  throw new NotImplementedException();
}


public Clob createClob() throws SQLException {
  throw new NotImplementedException();
}




/**
 * @param arg0
 * @param arg1
 */
public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
  throw new NotImplementedException();
}


public Properties getClientInfo() throws SQLException {
  throw new NotImplementedException();
}


/**
 * @param arg0
 */
public String getClientInfo(String arg0) throws SQLException {
  throw new NotImplementedException();
}

/**
 * @param arg0
 */
public boolean isValid(int arg0) throws SQLException {
  throw new NotImplementedException();
}






}

