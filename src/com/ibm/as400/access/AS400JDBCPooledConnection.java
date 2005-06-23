///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCPooledConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/**
*  The AS400JDBCPooledConnection class represents a connection object
*  that provides hooks for connection pool management. This object is returned
*  by the {@link com.ibm.as400.access.AS400JDBCConnectionPoolDataSource#getPooledConnection AS400JDBCConnectionPoolDataSource.getPooledConnection()} method.
*
*  The following example creates an AS400JDBCPooledConnection object that can be used to cache JDBC connections.
*
*  <pre><blockquote>
*  // Create a data source for making the connection.
*  AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource("myAS400");
*  datasource.setUser("Mickey Mouse");
*  datasource.setPassword("IAMNORAT");
*
*  // Get a PooledConnection and get the connection handle to the database.
*  AS400JDBCPooledConnection pooledConnection = datasource.getPooledConnection();
*  Connection connection = pooledConnection.getConnection();
*
*  ... work with the connection handle.
*
*  // Close the connection handle to make available for reuse (physical connection not closed).
*  connection.close();
*
*  // Reuse the connection somewhere else.
*  Connection reusedConnection = pooledConnection.getConnection();
*  ... work with the connection handle.
*  reusedConnection.close();
*
*  // Close the physical connection.
*  pooledConnection.close();  
*  </blockquote></pre>
*
*  <p>
*  AS400JDBCPooledConnection objects generate the following events:
*  <UL>
*  <li>javax.sql.ConnectionEvent - The events fired are:</li>
*    <ul>
*       <li>connectionClosed</li>
*       <li>connectionErrorOccurred</li>
*    </ul>
*  </ul>
**/
public class AS400JDBCPooledConnection implements PooledConnection
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private AS400JDBCConnection connection_;                          // The database connection.
//@CRS - If we maintain a reference to the handle, and the user doesn't call close(), it
// will never get closed because it will never get garbage collected.
// Instead, we are a listener to the handle, so we know when either the user or the
// garbage collector closes it.  At that point, this PooledConnection is no longer
// considered "in use" and can hand out a new handle when someone calls getConnection().
//   private AS400JDBCConnectionHandle handle_;               // The handle to the connection.

  private PoolItemProperties properties_;                  // The usage properties.
  private AS400JDBCConnectionEventSupport eventManager_;


  /**
  *  Constructs an AS400JDBCPooledConnection object.
  *  @param connection The physical connection to be pooled.
  *  @exception SQLException If a database error occurs.
  **/
  AS400JDBCPooledConnection(Connection connection) throws SQLException
  {
    if (connection == null) throw new NullPointerException("connection");
    connection_ = (AS400JDBCConnection)connection;

    properties_ = new PoolItemProperties();
    eventManager_ = new AS400JDBCConnectionEventSupport();

    if (JDTrace.isTraceOn())                                                    //@G2A
    {
      JDTrace.logInformation(this, "A new AS400JDBCPooledConnection was created");    //@G2A
    }
  }


  /**
  *  Adds a ConnectionEventListener.
  *  @param listener The listener.
  **/
  public void addConnectionEventListener(ConnectionEventListener listener)
  {
    eventManager_.addConnectionEventListener(listener);
  }


  /**
  *  Closes the physical connection.
  *  @exception SQLException If an error occurs closing the connection.
  **/
  public synchronized void close() throws SQLException
  {
    if (connection_.isClosed()) return;

    connection_.close();

    // Reset the usage timers.
    properties_.clear();

    if (JDTrace.isTraceOn())
    {                                                             //@G2C
      JDTrace.logInformation(this, "close() was called on this AS400JDBCPooledConnection"); //@G2C
    }
  }


  //@G4A JDBC 3.0
  /**
  *  Closes all the Statement objects that have been opened by this PooledConnection
  *  object.  This method is not supported.
  *  @exception SQLException Always thrown because this method is not supported.
  **/
  public void closeAll() throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_FUNCTION_NOT_SUPPORTED);
  }


  /**
  *  Fire the connection closed event.
  *  @param event The ConnectionEvent.
  **/
  void fireConnectionCloseEvent(ConnectionEvent event)
  {
    // This gets called by AS400JDBCConnectionHandle.close().
    returned();                                     // Reset the pooledConnection.
    eventManager_.fireCloseEvent(event);            // Notify the pool.
  }


  /**
  *  Returns the connection handle to the database. Only one connection handle can be open
  * at a time for any given AS400JDBCPooledConnection object.
  *  @return The connection handle.
  *  @exception SQLException If a database error occurs or if this PooledConnection is already in use. 
  **/
  public synchronized Connection getConnection() throws SQLException
  {
    if (JDTrace.isTraceOn())                                                    //@G2C
    {
      JDTrace.logInformation(this, "AS400JDBCPooledConnection.getConnection() was called");  //@G2C
    }

    if (connection_.isClosed())
    {
      if (JDTrace.isTraceOn())                                             // @G2A
      {
        JDTrace.logInformation(this, "This AS400JDBCPooledConnection is invalid because connection is closed.");  // @G2A
      }
      JDError.throwSQLException(this, JDError.EXC_CONNECTION_NONE);  //@G2A
    }

    if (isInUse())
    {
      if (JDTrace.isTraceOn())
      {
        JDTrace.logInformation(this, "This AS400JDBCPooledConnection is already in use.");
      }
      JDError.throwSQLException(this, JDError.EXC_CONNECTION_UNABLE); // Is this the right thing to throw here?
    }

    // Start the connection tracking timers.
    setInUse(true);

    return new AS400JDBCConnectionHandle(this, connection_);
  }


  AS400JDBCConnection getInternalConnection()        //@G1A
  {                                                  //@G1A
    return connection_;        //@G1A
  }                                                  //@G1A


  /**
  *  Returns the elapsed time the connection has been idle waiting in the pool.
  *  @return The idle time.
  **/
  public long getInactivityTime()
  {
    return properties_.getInactivityTime();
  }


  /**
  *  Returns the elapsed time the connection has been in use.
  *  @return The elapsed time.
  **/
  public long getInUseTime()
  {
    return properties_.getInUseTime();
  }


  /**
  *  Returns the elapsed time the pooled connection has been alive.
  *  @return The elapsed time.
  **/
  public long getLifeSpan()
  {
    return properties_.getLifeSpan();
  }


  /**
  *  Returns the number of times the pooled connection has been used.
  *  @return The number of times used.
  **/
  public int getUseCount()
  {
    return properties_.getUseCount();
  }


  /**
  *  Ping the connection to check the status.
  *  @return true if the connection is active; false otherwise.
  **/
  boolean isConnected() throws SQLException // @A3A
  {
    return connection_.getAS400().isConnected(AS400.DATABASE);
  }


  /**
  *  Indicates if the pooled connection is in use.
  *  @return true if the pooled connection is in use; false otherwise.
  **/
  public boolean isInUse()
  {
    return properties_.isInUse();
  }


  /**
  *  Removes a ConnectionEventListener.
  *  @param listener The listener to be removed.
  **/
  public void removeConnectionEventListener(ConnectionEventListener listener)
  {
    eventManager_.removeConnectionEventListener(listener);
  }


  /**
  *  Returns the connection after usage. 
  *  Update the connection timers and invalidate connection handle.
  **/
  synchronized void returned()
  {
    if (JDTrace.isTraceOn())                                                    //@G2C
    {
      JDTrace.logInformation(this, "This AS400JDBCPooledConnection is being returned."); //@G2C
    }

    // Reset the timers.
    setInUse(false);
  }


  /**
  *  Sets the connection timer values based on the active usage state of the connection.
  *  @param inUse true if the connection is currently active; false otherwise.
  **/
  synchronized void setInUse(boolean inUse)
  {
    properties_.setInUse(inUse);
  }
}
