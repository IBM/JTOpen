///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;
import java.io.*;


/**
 *  The JdbcMeConnection class provides a JDBC connection
 *  to a specific DB2 for OS/400 database.  Use
 *  DriverManager.getConnection() to create new JdbcMeConnection objects.
 *
 *  <p>There are many optional properties that can be specified
 *  when the connection is created.  Properties can be specified either
 *  as part of the URL or in a java.util.Properties object.  See
 *  <a href="../../../../JDBCProperties.html"> JDBC properties</a>
 *   for a complete list of properties supported by the JdbcMeDriver.
 *
 *  <p><b>Note:</b> Since Java 2 Micro-Edition does not include java.sql,
 *  JdbcMeConnection implements the java.sql package that is also part 
 *  of this driver.
 **/
public class JdbcMeConnection implements java.sql.Connection
{
    // The system object used to athenticate and communicate using this JdbcMeConnection.
    AS400 system_;

    int         connectionId_ = -1;  // The connection
    boolean     closed_       = true;
    boolean     autoCommit_   = true;
    int         isolation_    = TRANSACTION_READ_UNCOMMITTED;


    /**
     *  Default JdbcMeConnection constructor.
     **/
    private JdbcMeConnection()
    {
    }


    /**
     *  Construct a new JdbcMeConnection using the URL, and system specified.
     *  This causes an implicit network connection to the database
     *  server if one does not already exist.
     *
     *  @param url The URL for the database.
     *  @param  system  The iSeries to connect.
     *
     *  @exception JdbcMeException If the driver is unable to make the connection.
     **/
    JdbcMeConnection(String url, AS400 system) throws JdbcMeException 
    {
        /**
         * Line flows out:
         *    Function ID
         *    JDBC URL (of the form 'url;user=<user>;password=<password>'
         * Line flows in:
         *    Connection Handle ID or -1 and exception data
         **/
        system_ = system;

        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_NEW);
            // No object ID written in this case.
            system_.toServer_.writeUTF(url);
            system_.toServer_.flush();

            connectionId_ = system_.fromServer_.readInt();

            if (connectionId_ == -1)
                JdbcMeDriver.processException(this);

            closed_ = false;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Releases the connection's resources immediately instead of waiting
     *  for them to be automatically released.  This rolls back any active
     *  transactions, closes all statements that are running in the context
     *  of the connection, and disconnects from the server.
     *
     *  @exception JdbcMeException  If an error occurs.
     **/
    public void close() throws JdbcMeException 
    {
        /**
         *  Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    None
         **/
        if (!closed_)
        {
            try
            {
                system_.toServer_.writeInt(MEConstants.CONN_CLOSE);
                system_.toServer_.writeInt(connectionId_);
                system_.toServer_.flush();
            }
            catch (IOException e)
            {
                throw new JdbcMeException(e.toString(), null);
            }
            finally
            {
                disconnected();
            }
        }
        return;
    }

    /**
     * This JdbcMeConnection has been disconnected
     * due to a communication failure or close()
     * request.
     */
    void disconnected()
    {
        closed_ = true;
        connectionId_ = -1;
        // TODO: We want to manage multiple JdbcMeConnections
        // TODO: over a single network connection.
        // TODO: In order to do that, we'll have to synchronize
        // TODO: on the network connection when we're
        // TODO: doing line flows on behalf of a particular
        // TODO: connection, and then NOT close these network
        // TODO: objects here.
        // TODO: See JdbcMeDriver for details.
        try
        {
            system_.disconnect();
        }
        catch (Exception e)
        {
        }
    }

    /**
     *  Creates a Statement object for executing SQL statements without
     *  parameters.  If the same SQL statement is executed many times, it
     *  is more efficient to use prepareStatement().
     *  
     *  @return     The statement object.
     *  
     *  @exception  JdbcMeException    If the connection is not open,
     *                                                  the maximum number of statements
     *                                                  for this connection has been reached, or an
     *                                                  error occurs.
     **/
    public Statement createStatement() throws JdbcMeException 
    {
        /**
         *  Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    Statement handle or -1 and exception info
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_CREATE_STATEMENT);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.flush();

            int   statementId = system_.fromServer_.readInt();

            if (statementId == -1)
                JdbcMeDriver.processException(this);

            return new JdbcMeStatement(this, statementId);
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Create a Statement object that can be used to directly execute SQL. 
     *
     *  @param resultSetType The result set type.  Valid values are:
     *                           <ul>
     *                             <li>ResultSet.TYPE_FORWARD_ONLY
     *                             <li>ResultSet.TYPE_SCROLL_INSENSITIVE
     *                             <li>ResultSet.TYPE_SCROLL_SENSITIVE
     *                           </ul>
     *  @param resultSetConcurrency The result set concurrency.  Valid values are:
     *                           <ul>
     *                             <li>ResultSet.CONCUR_READ_ONLY
     *                             <li>ResultSet.CONCUR_UPDATABLE
     *                           </ul>
     *
     *  @return The statement object.
     *
     *  @exception JdbcMeException If the connection is not open, the maximum number of statements for
     *  this connection has been reached, the result type or currency is not supported, or an error occurs.
     **/
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws JdbcMeException 
    {
        /**
         * NOTE:
         * Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    Statement handle or -1 and exception info
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_CREATE_STATEMENT2);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.writeInt(resultSetType);
            system_.toServer_.writeInt(resultSetConcurrency);
            system_.toServer_.flush();

            int   statementId = system_.fromServer_.readInt();

            if (statementId == -1)
                JdbcMeDriver.processException(this);

            JdbcMeStatement stmt = new JdbcMeStatement(this, statementId);
            stmt.concurrency_ = resultSetConcurrency;
            stmt.type_ = resultSetType;

            return stmt;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /** 
     * Indicates if the connection is closed.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public boolean isClosed() throws JdbcMeException 
    {
        return closed_;
    }

    /**
     *  Sets the auto-commit mode. If the connection is in auto-commit mode, then all of its SQL statements are
     *  executed and committed as individual transactions. Otherwise, its SQL statements are grouped into
     *  transactions that are terminated by either a commit or rollback. 
     * 
     *  By default, the connection is in auto-commit mode. The commit occurs when the statement execution
     *  completes or the next statement execute occurs, whichever comes first. In the case of statements returning
     *  a result set, the statement execution completes when the last row of the result set has been retrieved or the
     *  result set has been closed. In advanced cases, a single statement may return multiple results as well as
     *  output parameter values. Here the commit occurs when all results and output parameter values have been
     *  retrieved. 
     *
     *  @param autoCommit true to turn on auto-commit mode, false to turn it off.
     *
     *  @exception JdbcMeException  If the connection is not open or an error occurs.
     **/ 
    public void setAutoCommit(boolean autoCommit) throws JdbcMeException 
    {
        /**
         * Line flows out:
         *   Function ID
         *   Connection Handle ID
         *   Boolean value
         * Line flows in:
         *   1  - Success
         *   -1 - failure
         *        Exception information
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_SET_AUTOCOMMIT);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.writeBoolean(autoCommit);
            system_.toServer_.flush();

            int   rc = system_.fromServer_.readInt();

            if (rc == -1)
                JdbcMeDriver.processException(this);

            autoCommit_ = autoCommit;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Returns the auto-commit state.
     *
     *  @return true if the connection is in auto-commit mode; false otherwise.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public boolean getAutoCommit() throws JdbcMeException 
    {
        return autoCommit_;
    }

    /**
     *  Commits all changes made since the previous commit or rollback and releases any database locks
     *  currently held by the connection. This has no effect when the connection is in auto-commit mode.
     *
     * exception JdbcMeException If the connection is not open or an error occurs.
     **/
    public void commit() throws JdbcMeException 
    {
        /**
         * Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    1  - Success
         *    -1 - Failure
         *         Exception information
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_COMMIT);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.flush();

            int   rc = system_.fromServer_.readInt();

            if (rc == -1)
                JdbcMeDriver.processException(this);
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Drops all changes made since the previous commit or rollback and releases any database locks currently
     *  held by the connection. This has no effect when the connection is in auto-commit mode. 
     *  
     *  @exception JdbcMeException If the connection is not open or an error occurs.
     **/
    public void rollback() throws JdbcMeException 
    {
        /**
         * Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    1  - Success
         *    -1 - Failure
         *         Exception information
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_ROLLBACK);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.flush();

            int   rc = system_.fromServer_.readInt();

            if (rc == -1)
                JdbcMeDriver.processException(this);
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     * Returns the transaction isolation level.
     *
     *  @return The transaction isolation level. Possible values are: 
     *     <ul>
     *       <li>   TRANSACTION_NONE 
     *       <li>   TRANSACTION_READ_UNCOMMITTED 
     *       <li>   TRANSACTION_READ_COMMITTED 
     *       <li>   TRANSACTION_REPEATABLE_READ 
     *     </ul>
     *  @exception JdbcMeException If an error occurs.
     **/
    public int getTransactionIsolation() throws JdbcMeException 
    {
        return isolation_;
    }

    /**
     * Sets the transaction isolation level. The transaction isolation level cannot be changed while in the middle of a transaction. 
     *
     * <p>JDBC and DB2/400 use different terminology for transaction
     *  isolation levels.  The following table provides a terminology mapping:
     *
     *  <p><table border>
     *  <tr><th>AS/400 isolation level</th><th>JDBC transaction isolation level</th></tr>
     *  <tr><td>*CHG</td> <td>TRANSACTION_READ_UNCOMMITTED</td></tr>
     *  <tr><td>*CS</td>  <td>TRANSACTION_READ_COMMITTED</td></tr>
     *  <tr><td>*ALL</td> <td>TRANSACTION_READ_REPEATABLE_READ</td></tr>
     *  <tr><td>*RR</td>  <td>TRANSACTION_SERIALIZABLE</td></tr>
     *  </table>
     *  
     *  @param      level   The transaction isolation level.  Possible values are:
     *  <ul>
     *  <li>TRANSACTION_READ_UNCOMMITTED
     *  <li>TRANSACTION_READ_COMMITTED
     *  <li>TRANSACTION_REPEATABLE_READ
     *  <li>TRANSACTION_SERIALIZABLE
     *  </ul>
     *
     *  @exception JdbcMeException If the connection is not open, the input level is not valid or
     *  unsupported, or a transaction is active.
     **/
    public void setTransactionIsolation(int isolation) throws JdbcMeException 
    {
        /**
         * Line flows out:
         *    Function ID
         *    Connection Handle ID
         * Line flows in:
         *    1  - Success
         *    -1 - Failure
         *         Exception information
         **/
        try
        {
            system_.toServer_.writeInt(MEConstants.CONN_SET_TRANSACTION_ISOLATION);
            system_.toServer_.writeInt(connectionId_);
            system_.toServer_.writeInt(isolation_);
            system_.toServer_.flush();

            int   rc = system_.fromServer_.readInt();

            if (rc == -1)
                JdbcMeDriver.processException(this);

            isolation_ = isolation;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }
}
