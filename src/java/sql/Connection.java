///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Connection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package java.sql;

/**
 *  A connection (session) with a specific database. Within the context of a Connection, SQL statements are
 *  executed and results are returned. 
 *  <p>
 *  A Connection's database is able to provide information describing its tables, its supported SQL grammar, its
 *  stored procedures, the capabilities of this connection, and so on. This information is obtained with the
 *  getMetaData method. 
 *  <p>
 *  <b>Note:</b> By default the Connection automatically commits changes after executing each statement. If auto commit
 *  has been disabled, the method commit must be called explicitly; otherwise, database changes will not be saved.
 *  <p>
 *  This class contains the smallest useful set of methods and data from java.sql.Connection for a wireless device.
 **/
public interface Connection
{
    /**
     *  Indicates that transactions are not supported.
     **/
    int TRANSACTION_NONE        = 0;

    /**
     *  Dirty reads, non-repeatable reads and phantom reads can occur. This level allows a row changed by one
     *  transaction to be read by another transaction before any changes in that row have been committed (a "dirty
     *  read"). If any of the changes are rolled back, the second transaction will have retrieved an invalid row.
     **/
    int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     *  Dirty reads are prevented; non-repeatable reads and phantom reads can occur. This level only prohibits a
     *  transaction from reading a row with uncommitted changes in it.
     **/
    int TRANSACTION_READ_COMMITTED   = 2;
    
    /**
     *  Dirty reads and non-repeatable reads are prevented; phantom reads can occur. This level prohibits a
     *  transaction from reading a row with uncommitted changes in it, and it also prohibits the situation where one
     *  transaction reads a row, a second transaction alters the row, and the first transaction rereads the row,
     *  getting different values the second time (a "non-repeatable read").
     **/
    int TRANSACTION_REPEATABLE_READ  = 4;

    /**
     *  Dirty reads, non-repeatable reads and phantom reads are prevented. This level includes the prohibitions in
     *  TRANSACTION_REPEATABLE_READ and further prohibits the situation where one transaction reads
     *  all rows that satisfy a WHERE condition, a second transaction inserts a row that satisfies that WHERE
     *  condition, and the first transaction rereads for the same condition, retrieving the additional "phantom" row
     *  in the second read.
     **/
    int TRANSACTION_SERIALIZABLE     = 8;

    /**
     *  Creates a Statement object for sending SQL statements to the database. SQL statements without
     *  parameters are normally executed using Statement objects. If the same SQL statement is executed many
     *  times, it is more efficient to use a PreparedStatement object. 
     *
     *  Result sets created using the returned Statement object will by default have forward-only type and
     *  read-only concurrency.
     *
     *  @return a new Statement object.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    Statement createStatement() throws SQLException;

    /**
     *  Creates a Statement object that will generate ResultSet objects with the given type and concurrency. This
     *  method is the same as the createStatement method above, but it allows the default result set type and result
     *  set concurrency type to be overridden.
     *
     *  @param resultSetType a result set type; see ResultSet.TYPE_XXX.
     *  @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX.
     *
     *  @return a new Statement object.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException;

    /**
     *  Sets this connection's auto-commit mode. If a connection is in auto-commit mode, then all its SQL
     *  statements will be executed and committed as individual transactions. Otherwise, its SQL statements are
     *  grouped into transactions that are terminated by a call to either the method commit or the method
     *  rollback. By default, new connections are in auto-commit mode. The commit occurs when the statement
     *  completes or the next execute occurs, whichever comes first. In the case of statements returning a
     *  ResultSet, the statement completes when the last row of the ResultSet has been retrieved or the ResultSet
     *  has been closed. In advanced cases, a single statement may return multiple results as well as output
     *  parameter values. In these cases the commit occurs when all results and output parameter values have
     *  been retrieved.
     *
     *  @param autoCommit  true enables auto-commit; false disables auto-commit..
     *
     *  @exception SQLException if a database access error occurs.
     **/
    void setAutoCommit(boolean autoCommit) throws SQLException;

    /**
     *  Gets the current auto-commit state.
     *
     *  @return the current state of auto-commit mode.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    boolean getAutoCommit() throws SQLException;

    /**
     *  Makes all changes made since the previous commit/rollback permanent and releases any database locks
     *  currently held by the Connection. This method should be used only when auto-commit mode has been
     *  disabled.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    void commit() throws SQLException;

    /**
     *  Drops all changes made since the previous commit/rollback and releases any database locks currently held
     *  by this Connection. This method should be used only when auto- commit has been disabled.
     *  
     *  @exception SQLException if a database access error occurs.
     **/
    void rollback() throws SQLException;

    /**
     *  Releases a Connection's database and JDBC resources immediately instead of waiting for them to be
     *  automatically released. 
     *
     *  <b>Note:</b> A Connection is automatically closed when it is garbage collected. Certain fatal errors also result in
     *  a closed Connection.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    void close() throws SQLException;

    /**
     *  Tests to see if a Connection is closed.
     *
     *  @return true if the connection is closed; false if it's still open.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    boolean isClosed() throws SQLException;

    /**
     *  Attempts to change the transaction isolation level to the one given. The constants defined in the interface
     *  Connection are the possible transaction isolation levels. 
     *
     *  <b>Note:</b> This method cannot be called while in the middle of a transaction.
     *
     *  @param level one of the TRANSACTION_* isolation values with the exception of
     *                      TRANSACTION_NONE; some databases may not support other values
     *
     *  @exception SQLException if a database access error occurs.
     **/
    void setTransactionIsolation(int level) throws SQLException;

    /**
     *  Gets this Connection's current transaction isolation level.
     *
     *  @return the current TRANSACTION_* mode value.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    int getTransactionIsolation() throws SQLException;
}







