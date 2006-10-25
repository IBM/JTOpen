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

//@pda (bug reported) A AS400JDBCPooledConnection can get expired and moved back to available queue.  So this
// handle class needs a way for AS400JDBCPooledConnection to notify it of this change in state. 
// This way, when finalize() is called by GC we will not try to double-close the connection.
// Without this method, it is possible for two handles to have references to the same pooledConnection.
// After we are done with handle_, we must set it to null, so AS400JDBCConnectionHandle can be GCed.
// In order for the handle to be GCed, a leaked connection has to be Expired and have handle_ set to null
// upon returning to available queue.
  private AS400JDBCConnectionHandle handle_;               // The handle to the connection. //@pdc make use of reference to handle

  private PoolItemProperties properties_;                  // The usage properties.
  private AS400JDBCConnectionEventSupport eventManager_;

  private int hashCode_;    //@CPMa

  // The following fields are reserved for use by JDConnectionPoolManager.         //@CPMa
  JDConnectionPoolKey poolKey_;          // connection-pool key for this connection
  long timeWhenCreated_;                 // time when this connection was created.
  long timeWhenPoolStatusLastModified_;  // time when this connection's pooling status last changed.
  boolean fatalConnectionErrorOccurred_; // this clues the pool manager not to reuse connection


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
    hashCode_ = connection_.hashCode();
    timeWhenCreated_ = System.currentTimeMillis();

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
    // Note: AS400JDBCConnectionHandle.close() calls fireConnectionCloseEvent().

    connection_.close();  // close the physical connection

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


  // JDConnectionPoolManager needs this when identifying returned connections.
  public boolean equals(Object obj)
  {
    try
    {
      AS400JDBCPooledConnection pc = (AS400JDBCPooledConnection)obj;
      return (connection_.equals(pc.connection_));
    }
    catch (Throwable e) {
      return false;
    }
  }

  // Needed for good hashing.
  public int hashCode()
  {
    return hashCode_;
  }


  // Note: The following method is called by AS400JDBCConnectionHandle.close().
  /**
  *  Fire the connection closed event.
  *  @param event The ConnectionEvent.
  **/
  void fireConnectionCloseEvent(ConnectionEvent event)
  {
    returned();                                     // Reset the pooledConnection.
    eventManager_.fireCloseEvent(event);            // Notify the pool.
  }


  // Note: The following method is called by AS400JDBCConnectionHandle.close().
  /**
  *  Fire the connection error event.
  *  @param event The ConnectionEvent.
  **/
  void fireConnectionErrorEvent(ConnectionEvent event)
  {
    // Don't bother cleaning up the connection, it won't get re-used.
    eventManager_.fireErrorEvent(event);            // Notify the pool.
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
    return getConnectionHandle();
  }


  // Used by JDConnectionPoolManager.         //@CPMa
  /**
  *  Returns the connection handle to the database. Only one connection handle can be open
  * at a time for any given AS400JDBCPooledConnection object.
  *  @return The connection handle.
  *  @exception SQLException If a database error occurs or if this PooledConnection is already in use.
  **/
  synchronized final AS400JDBCConnectionHandle getConnectionHandle() throws SQLException
  {
    if (connection_.isClosed())
    {
      if (JDTrace.isTraceOn())                                             // @G2A
      {
        JDTrace.logInformation(this, "This AS400JDBCPooledConnection is invalid because connection is closed.");  // @G2A
      }
      JDError.throwSQLException(this, JDError.EXC_CONNECTION_NONE);  //@G2A
    }

    // Note: The JDBC Tutorial says that if PooledConnection.getConnection() is called while already in use, this should close the existing connection.  "The purpose of allowing the server to invoke the method getConnection a 2nd time is to give the application server a way to take a connection away from an application and give it to someone else.  This will probably rarely happen, but the capability is there."
    // However, we haven't had a request for this behavior, so we just throw an exception instead.
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

    handle_ = new AS400JDBCConnectionHandle(this, connection_); //@pdc handle
    return handle_; //@pda handle

  }


  // For exclusive use by JDConnectionPoolManager.    //@CPMa
  final JDConnectionPoolKey getPoolKey()
  {
    return poolKey_;
  }


  AS400JDBCConnection getInternalConnection()        //@G1A
  {                                                  //@G1A
    return connection_;        //@G1A
  }                                                  //@G1A


  /**
  *  Returns the elapsed time the connection has been idle waiting in the pool.
  *  @return The idle time (milliseconds).
  **/
  public long getInactivityTime()
  {
    return properties_.getInactivityTime();
  }


  /**
  *  Returns the elapsed time the connection has been in use.
  *  @return The elapsed time (milliseconds).
  **/
  public long getInUseTime()
  {
    return properties_.getInUseTime();
  }


  /**
  *  Returns the elapsed time the pooled connection has been alive.
  *  @return The elapsed time (milliseconds).
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

    try {                                                                      //@CPMa
      connection_.clearWarnings();  // This seems safe and reasonable enough.
      connection_.setHoldability(AS400JDBCResultSet.HOLDABILITY_NOT_SPECIFIED);  // This is the default for all new instances of AS400JDBCConnection.
      boolean readOnly = connection_.isReadOnlyAccordingToProperties();  // Get default from props.
      connection_.setReadOnly(readOnly);  // In case the user forgot to reset.
      connection_.setAutoCommit(true);    // Ditto.
      if (handle_ != null) //@pda handle
      {
          handle_.invalidate();      //@pda Invalidate the handle.  so if this pooledConnection gets expired then also need to invalidate (remove reference from handle to pooledConnection) handle.
                                     //if the handle gets GCed (due to connection leak), then handle.finalize() will not try to close this pooledConnection, which could have been already assigned to a new handle.
                                     //(ie prevent two handles from pointing to one pooledConnection)
          handle_ = null;          //remove reference also, so handle is free for GC.
      }
    }
    catch (SQLException e) {
      JDTrace.logException(this, "Exception while resetting properties of returned connection.", e);
    }

    // Note: We can assume that if the connection has been used and then returned to the pool, it has been sufficiently cleaned-up/reset by AS400JDBCConnectionHandle.close(), which calls AS400JDBCConnection.pseudoClose(), which does any needed rollbacks and/or statement closing.              //@CPMa
  }


  /**
  *  Sets the connection timer values based on the active usage state of the connection.
  *  @param inUse true if the connection is currently active; false otherwise.
  **/
  synchronized void setInUse(boolean inUse)
  {
    properties_.setInUse(inUse);
  }


  // For exclusive use by JDConnectionPoolManager.    //@CPMa
  final void setPoolKey(JDConnectionPoolKey key)
  {
    poolKey_ = key;
  }

}
