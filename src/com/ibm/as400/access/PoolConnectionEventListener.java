///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolConnectionEventListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.*;
import javax.sql.ConnectionEvent;                     // JDBC std-ext
import javax.sql.ConnectionEventListener;             // JDBC std-ext

/**
*  PoolConnectionEventListener to manage the closing of pooled connections to return 
*  them to the pool for reuse.
**/
class PoolConnectionEventListener implements ConnectionEventListener
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  private transient AS400JDBCConnectionPool pool_;

  /**
  *  Constructs a default PoolConnectionEventListener.
  **/
  public PoolConnectionEventListener(AS400JDBCConnectionPool pool)
  {
    pool_ = pool;
  }                                                                 

  /**
  *  Removes the pooled connection from the pool in the event that a connection error occurs
  *  making the connection unusable. 
  *  @param event The ConnectionEvent object.
  **/
  public void connectionErrorOccurred(ConnectionEvent event)
  {
    JDTrace.logInformation (this, "PooledConnection is in error...");  // @B5C
    pool_.closePooledConnection( (AS400JDBCPooledConnection)event.getSource() );
  }

  /**
  *  Returns the pooled connection to the available pool for reuse.
  *  Note: called from the ConnectionHandle.close().
  *  @param event The ConnectionEvent object.
  **/
  public void connectionClosed(ConnectionEvent event)
  {
    AS400JDBCPooledConnection connection = (AS400JDBCPooledConnection)event.getSource();

    synchronized (pool_.activePool_)
    {
      pool_.activePool_.removeElement(connection);       // Update the pools.
    }

    // Determine if connection has expired.
    if ((pool_.getMaxLifetime() != -1 && connection.getLifeSpan() > pool_.getMaxLifetime()) ||         // Max lifetime exceeded.
        (pool_.getMaxUseCount() != -1 && connection.getUseCount() == pool_.getMaxUseCount()) ||        // Max Use Count.
        (pool_.getMaxConnections() != -1 && pool_.activePool_.size() > pool_.getMaxConnections()))           // MaxConnections reduced.
    {
      JDTrace.logInformation (this, "Connection has expired.  Removed from the pool.");  // @B5C
      pool_.closePooledConnection(connection);

      // Notify listeners that the connection expired
      if (pool_.poolListeners_ != null)
      {
        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(connection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
        pool_.poolListeners_.fireConnectionExpiredEvent(poolEvent);
      }
    }
    else
    {
      if (JDTrace.isTraceOn())  // @B5C
        JDTrace.logInformation (this, "Returning active connection to the pool.");  // @B5C

      // Add a check to see if the connection is still good
      AS400JDBCConnection jdbcConnection = connection.getInternalConnection();  //@B4A
      try
      {                                                                         //@B4A 
        if (!jdbcConnection.isClosed())                                       //@B4A
        {
          synchronized (pool_.availablePool_)
          {
            pool_.availablePool_.addElement(connection);                // connection still good, reuse.
          }

          // Notify listeners that a connection was returned.
          if (pool_.poolListeners_ != null)
          {
            ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(connection, ConnectionPoolEvent.CONNECTION_RETURNED); //@A5C
            pool_.poolListeners_.fireConnectionReturnedEvent(poolEvent);
          }
        }
        else
        {            //@B4A
          if (JDTrace.isTraceOn())         //@B4A @B5C
            JDTrace.logInformation (this, "Removing closed connection from pool.");   //@B4A @B5C
        }
      }
      catch (SQLException sqe)
      {
        // Should not be thrown.  isClosed() reports that it throws SQLExceptions,
        // but it doesn't really.
      }
    }         

    // periodic cleanup for single-threaded mode.
    if (!pool_.isThreadUsed() && pool_.isRunMaintenance() && System.currentTimeMillis() - pool_.lastSingleThreadRun_ > pool_.getCleanupInterval())
      pool_.cleanupConnections();
  }
}

