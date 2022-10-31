///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400JDBCStatementListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.*;

/**
 * Represents a listener that will get notified when certain events occur during the processing of an SQL statement.
 * Note that {@link #modifySQL modifySQL()} is called before {@link #commentsStripped commentsStripped()} is called.
 * The methods on this listener are invoked when the Toolbox JDBC driver constructs an SQL statement internally,
 * before it is executed on the server.
**/
public interface AS400JDBCStatementListener
{
  /**
   * Gives the listener an opportunity to modify the SQL statement before it is executed.
   * @param connection The connection to the server that the statement will execute under.
   * @param sql The original SQL string.
   * @return The SQL string this listener wants to execute instead of the original. If this
   * listener does not want to modify the SQL string, then it should either return the <i>sql</i> parameter
   * as it was passed in, or null. Returning null will cause the Toolbox JDBC driver to use the original SQL string.
   * @throws SQLException Thrown by the listener if necessary.
  **/
  public String modifySQL(Connection connection, String sql) throws SQLException;

  /**
   * Notifies the listener that comments have been stripped off of the SQL statement to be executed.
   * @param connection The connection to the server that the statement will execute under.
   * @param oldSQL The original SQL string.
   * @param newSQL The SQL string with comments stripped off of it.
   * @throws SQLException Thrown by the listener if necessary.
  **/
  public void commentsStripped(Connection connection, String oldSQL, String newSQL) throws SQLException;

}
