///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;                            // Java2
import java.util.Vector;                              // Java2
import javax.sql.ConnectionEvent;                     // JDBC std-ext
import javax.sql.ConnectionEventListener;             // JDBC std-ext

/**
*  The AS400JDBCConnectionPool class represents a pool of AS/400 or iSeries JDBC connections
*  that are available for use by a Java program.
*  <p>
*  Note: AS400JDBCConnectionPool objects are threadsafe.
*
*  <p>The following example creates a connection pool with 10 connections.
*  <pre><blockquote>
*  // Obtain an AS400JDBCConnectionPoolDataSource object from JNDI.
*  Context context = new InitialContext(environment);
*  AS400JDBCConnectionPoolDataSource datasource = (AS400JDBCConnectionPoolDataSource)context.lookup("jdbc/myDatabase");
*
*  // Create an AS400JDBCConnectionPool object.
*  AS400JDBCConnectionPool pool = new AS400JDBCConnectionPool(datasource);
*
*  // Adds 10 connections to the pool that can be used by the application (creates the physical database connections based on the data source).
*  pool.fill(10);
*
*  // Get a handle to a database connection from the pool.
*  Connection connection = pool.getConnection();
*
*  ... Perform miscellenous queries/updates on the database.
*
*  // Close the connection handle to return it to the pool.
*  connection.close();  
*
*  ... Application works with some more connections from the pool.
*
*  // Close the pool to release all resources.
*  pool.close();
*  </blockquote></pre>
**/
public class AS400JDBCConnectionPool extends ConnectionPool implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



  static final long serialVersionUID = 4L;



  private boolean closed_;                                       // If the pool is closed.
  private AS400JDBCConnectionPoolDataSource dataSource_;         // The dataSource used for the connection.
  transient private long lastSingleThreadRun_;                   // Last time maintenance was run (single-thread mode).
  transient private Vector activePool_;                          // Active connections.
  transient private Vector availablePool_;                       // Available connections.
  transient private PoolConnectionEventListener eventListener_;  // Listener for events on pooled connections.

  /**
  *  Constructs a default AS400JDBCConnectionPool object.
  **/
  public AS400JDBCConnectionPool()
  {
    super();
    initializeTransient();
  }

  /**
  *  Constructs an AS400JDBCConnectionPool object with the specified <i>dataSource</i>.
  *  @param dataSource The AS400JDBCConnectionPoolDataSource object.
  **/
  public AS400JDBCConnectionPool(AS400JDBCConnectionPoolDataSource dataSource)
  {
    this();

    try
    {
      setDataSource(dataSource);      
    }
    catch (PropertyVetoException p)
    { /* will never occur. */
    }
  }


  /**
  *  Removes any connections that have exceeded maximum inactivity time, replaces any connections that have aged past maximum
  *  usage or maximum lifetime, and removes any connections that have been in use too long.
  *  @exception SQLException If a database error occurs closing a connection.
  **/
  void cleanupConnections()
  {
    AS400JDBCPooledConnection poolConnection;

        boolean trace = JDTrace.isTraceOn();                                              // @B5C
    if (trace)
    {
            JDTrace.logInformation (this, "ConnectionPool cleanup...");
            JDTrace.logInformation (this, "   MaxLifeTime: " + getMaxLifetime());         // @B5C
            JDTrace.logInformation (this, "   MaxUseTime: " + getMaxUseTime());           // @B5C
            JDTrace.logInformation (this, "   MaxInactivity: " + getMaxInactivity());     // @B5C

            JDTrace.logInformation (this, "Idle Connections: " + availablePool_.size());  // @B5C
            JDTrace.logInformation (this, "Active Connections: " + activePool_.size());   // @B5C
        }

    synchronized (availablePool_)
    {
      synchronized (activePool_)
      {
        Iterator[] connections = { availablePool_.iterator(), activePool_.iterator()};
        for (int i=0; i< connections.length; i++)
        {
          while (connections[i].hasNext())
          {
            poolConnection = (AS400JDBCPooledConnection)connections[i].next();

            if (trace)
                            JDTrace.logInformation (this, poolConnection.toString());     // @B5C

            if ((!poolConnection.isInUse() && getMaxLifetime() !=-1 && poolConnection.getLifeSpan() > getMaxLifetime()) ||   //@B1C       // inactive connections only.
                            (!poolConnection.isInUse() && getMaxInactivity() !=-1 && poolConnection.getInactivityTime() > getMaxInactivity()))  //@B1C //@B3C
                            {
              if (trace)
                                JDTrace.logInformation (this, "Removing expired connection from the pool.");  // @B5C

              closePooledConnection(poolConnection);
              connections[i].remove();

              // Notify listeners that the connection expired
              ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
              poolListeners_.fireConnectionExpiredEvent(poolEvent);
            }
            else if (getMaxUseTime() > 0 &&
                     poolConnection.getInUseTime() > getMaxUseTime())       // only valid with active connections.
            {
              if (trace)
                                JDTrace.logInformation (this, "Returning active connection to the pool.");  // @B5C

              poolConnection.returned();
              availablePool_.add(poolConnection);
              connections[i].remove();                  

              // Notify listeners that the connection expired
              ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
              poolListeners_.fireConnectionExpiredEvent(poolEvent);
            }
          }
        }
      }

      // Notify listeners that the maintenance thread was run.
      ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(this, ConnectionPoolEvent.MAINTENANCE_THREAD_RUN);
      poolListeners_.fireMaintenanceThreadRun(poolEvent);

      // Check if maintenance should keep running.
      if (activePool_.isEmpty() && availablePool_.isEmpty())
      {
        maintenance_.setRunning(false);
        setInUse(false);          // data source CAN be changed.
      }
    }

    if (!isThreadUsed())
      lastSingleThreadRun_ = System.currentTimeMillis();

    if (trace)
    {
            JDTrace.logInformation (this, "ConnectionPool cleanup finished.");  // @B5C
            JDTrace.logInformation (this, "   Idle Connections: " + availablePool_.size());  // @B5C
            JDTrace.logInformation (this, "   Active Connections: " + activePool_.size());  // @B5C
        }
    }

  /**
  *  Closes all the unused database connections in the pool.
  **/
  public void close()
  {
        if (JDTrace.isTraceOn())                                                   // @B5C
        {
            JDTrace.logInformation (this, "Closing the JDBC connection pool.");    // @B5C
            JDTrace.logInformation (this, "Available: " + availablePool_.size());  // @B5C
            JDTrace.logInformation (this, "Active: " + activePool_.size());        // @B5C
        }

    synchronized (availablePool_)
    {
      synchronized (activePool_)
      {
        Iterator[] connections = { availablePool_.iterator(), activePool_.iterator()};

        for (int i=0; i< connections.length; i++)
        {
          while (connections[i].hasNext())
          {
            AS400JDBCPooledConnection pooledConnection = (AS400JDBCPooledConnection)connections[i].next();
            closePooledConnection(pooledConnection);
            connections[i].remove();
          }
        }
      }
    }

    // Stop the maintenance thread.
    if (isRunMaintenance() && maintenance_ != null)
      maintenance_.setRunning(false);

    if (isInUse())
      setInUse(false);                     // data source CAN be changed.

    // Notify the listeners.
    ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_POOL_CLOSED);  
    poolListeners_.fireClosedEvent(event);

    closed_ = true;
  }

  /**
  *  Closes an AS400JDBCPooledConnection.
  *  @param pooledConnection The pooled connection.
  **/
  private void closePooledConnection(AS400JDBCPooledConnection pooledConnection)
  {
    try
    {
      pooledConnection.close();
    }
    catch (SQLException e)
    { 
      /* ignore connection is being removed anyway. */
            JDTrace.logInformation (this, e.getMessage());  // @B5C
        }
    }

  /**
  *  Creates a pooledConnection for the pool.
  *  @return An AS400JDBCPooledConnection object.
  *  @exception SQLException If a database error occurs.
  **/
  private AS400JDBCPooledConnection createPooledConnection() throws SQLException
  {
    if (dataSource_ == null)
      throw new ExtendedIllegalStateException("dataSource", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    AS400JDBCPooledConnection pooledConnection = new AS400JDBCPooledConnection(dataSource_.getConnection());  //@A3C
    pooledConnection.addConnectionEventListener(eventListener_);
    dataSource_.log("PooledConnection created");     //@A3A

    return pooledConnection;
  }

  /**
  *  Fills the connection pool with the specified number of database connections.
  *  @param numberOfConnections The number of connections to add to the pool.
  *  @exception ConnectionPoolException If a database error occurs creating a connection for the pool.
  **/
  public void fill(int numberOfConnections) throws ConnectionPoolException
  {
        if (JDTrace.isTraceOn())        // @B5C
            JDTrace.logInformation (this, "Filling the pool with " + numberOfConnections + " connections."); //@B5C

    // Validate the numberOfConnections parameter.
    if (numberOfConnections < 1)
      throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    int maxConnections = getMaxConnections();
    if (maxConnections != -1)
    {
      if (numberOfConnections + getActiveConnectionCount() + getAvailableConnectionCount() > maxConnections)
        throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    // Add connections to the pool.
    try
    {
      synchronized (availablePool_)
      {
        for (int i=0; i< numberOfConnections; i++)
        { //@A5A
          AS400JDBCPooledConnection poolConnection = createPooledConnection(); //@A5A
          availablePool_.addElement(poolConnection); //@A5C

          // Notify the listeners.
          ConnectionPoolEvent event = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_CREATED);  //@A5M @A5C
          poolListeners_.fireConnectionCreatedEvent(event); //@A5M
        } //@A5A
      }
    }
    catch (SQLException e)
    {
      if (isRunMaintenance() && maintenance_ != null)
        cleanupConnections();                 // re-check old connections.
      throw new ConnectionPoolException(e);
    }

    if (!isInUse())
    {
      setInUse(true);                   // Data source now can NOT be changed.

      if (isClosed())
        closed_ = false;                          // Set the state to OPEN if previously closed.
    }

    if (isRunMaintenance() && isThreadUsed())
    {
      if (maintenance_ == null)
      {
        synchronized(this) //@CRS
        {
          if (maintenance_ == null) //@CRS
          {
            maintenance_ = new PoolMaintenance();
            maintenance_.start();                     // Start the first time.
            // Give thread a chance to start.                                      
            if (!maintenance_.isRunning())                                         //@A2C
            {
              try
              {                                                                //@A2A
                Thread.sleep(10);                                              //@A2A
              }                                                                  //@A2A
              catch (InterruptedException e)                                     //@A2A
              {
                //Ignore  	        					   //@A2A
              }                                                                  //@A2A
            }                                                                      //@A2A
            // If thread has still not started, keep giving it chances for 5 minutes.
            for (int i = 1; !maintenance_.isRunning() && i<6000; i++)              //@A2A
            {
              try
              {                  //@A2A
                Thread.sleep(50);                                              //@A2A
              }                  //@A2A
              catch (InterruptedException ie)            //@A2A
              {
                //Ignore							   //@A2A
              }                  //@A2A
            }                    //@A2A
            if (!maintenance_.isRunning())             //@A2A
              JDTrace.logInformation (this, "maintenance thread failed to start");    //@A2A
          }                     //@A2A
        }
      }
//@CRS      else if (!maintenance_.isRunning())
      if (!maintenance_.isRunning()) maintenance_.setRunning(true);            // Restart. @CRS
    }
    else if (isRunMaintenance() && !isThreadUsed())
      lastSingleThreadRun_ = System.currentTimeMillis();
  }

  /**
  *  Closes the connection pool if not explicitly closed by the caller.
  *  @exception Throwable If an error occurs.
  **/
  protected void finalize() throws Throwable
  {
    if (!isClosed())
      close();
    super.finalize();
  }

  /**
  *  Returns the number of active connections the pool has created.
  *  @return The number of active connections.
  **/
  public int getActiveConnectionCount()
  {
    return activePool_.size();
  }

  /**
  *  Returns the number of available PooledConnections in the pool.
  *  @return The number of available PooledConnections.
  **/
  public int getAvailableConnectionCount()
  {
    return availablePool_.size();      
  }

  /**
  *  Returns a connection from the pool.
  *  Updates the pool cache.
  *  @return The connection.
  *  @exception ConnectionPoolException If a database error occurs getting the connection.
  **/
  public Connection getConnection() throws ConnectionPoolException
  {
    AS400JDBCPooledConnection pooledConnection = null;

    synchronized (availablePool_)
    {
            //@B2M Moved following two lines into the synchronization block.
            if (availablePool_.isEmpty())                                    //@B2M
                fill(1);                         // Add a new connection.    //@B2M

      pooledConnection = (AS400JDBCPooledConnection)availablePool_.firstElement();

      // Remove the pooled connection from the available list.
      availablePool_.removeElement(pooledConnection);    
    }
    synchronized (activePool_)
    {
      activePool_.addElement(pooledConnection);      
    }

    Connection connection = null;
    try
    {
      connection = pooledConnection.getConnection();
    }
    catch (SQLException sql)
    {
      throw new ConnectionPoolException(sql);
    }
    // Notify the listeners that a connection was released.
    ConnectionPoolEvent event = new ConnectionPoolEvent(pooledConnection, ConnectionPoolEvent.CONNECTION_RELEASED);  //@A5C
    poolListeners_.fireConnectionReleasedEvent(event);

    return connection;
  }

  /**
  *  Returns the data source used to make connections.
  *  @return The AS400JDBCConnectionPoolDataSource object.
  **/
  public AS400JDBCConnectionPoolDataSource getDataSource()
  {
    return dataSource_;
  }


  //@A3A
  /**
  *  Returns a connection from the pool.
  *  Updates the pool cache.
  *  @return The connection.
  *  @exception ConnectionPoolException If a database error occurs getting the connection.
  **/
  AS400JDBCPooledConnection getPooledConnection() throws ConnectionPoolException
  {
    AS400JDBCPooledConnection pooledConnection = null;

    synchronized (availablePool_)
    {
            //@B2M Moved following two lines into the synchronization block.
            if (availablePool_.isEmpty())                                    //@B2M
                fill(1);                         // Add a new connection.    //@B2M

      pooledConnection = (AS400JDBCPooledConnection)availablePool_.firstElement();

      // Remove the pooled connection from the available list.
      availablePool_.removeElement(pooledConnection);    
    }
    synchronized (activePool_)
    {
      activePool_.addElement(pooledConnection);      
    }

    // Notify the listeners that a connection was released.
    ConnectionPoolEvent event = new ConnectionPoolEvent(pooledConnection, ConnectionPoolEvent.CONNECTION_RELEASED);  //@A5C
    poolListeners_.fireConnectionReleasedEvent(event);
    return pooledConnection;
  }


  /**
  *  Initializes the transient data.
  **/
  private void initializeTransient()
  {
    eventListener_ = new PoolConnectionEventListener();

    activePool_ = new Vector();
    availablePool_ = new Vector();  
    closed_ = true;

    //@A1D Moved property change listener to parent; moved runMaintenance method below.
  }

  /**
  *  Indicates whether the connection pool is closed.
  *  @return true if closed; false otherwise.
  **/
  public boolean isClosed()
  {
    return closed_;
  }

  /**
  *  Deserializes and initializes transient data.
  *  @exception IOException If a file I/O error occurs.
  *  @exception ClassNotFoundException If a file error occurs.
  **/
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initializeTransient();      
  }

  /**
  *  Reduces the number of available connections down to the maximum number if necessary and possible.
  *  Note: A possibility still exists where the number of active connections is greater than the number of max connections.
  *  We will do this check in the PooledConnectionEventListener.
  **/
  private void reduceConnectionCount()
  {
    synchronized (availablePool_)
    {
      synchronized (activePool_)
      {
        int current = availablePool_.size() + activePool_.size();
        int required = getMaxConnections();

        if (current > required)
        {
                    if (JDTrace.isTraceOn())  // @B5C
                        JDTrace.logInformation (this, "Reducing number of connections... Current: " + current + "(" + availablePool_.size() + ") " + " Max: " + required);  // @B5C

          int removed = 0;
          int reduceBy = current - required;
          while (removed < reduceBy && availablePool_.size() != 0)
          {
            AS400JDBCPooledConnection poolConnection = (AS400JDBCPooledConnection)availablePool_.remove(0);
            removed++;
            closePooledConnection(poolConnection);
          }
        }
      }
    }
  }

  //@A1A
  /**
   *  Run cleanupConnections().
   *  @param reduced true if need to check current num connections; false otherwise.
   **/
  void runMaintenance(boolean reduced)
  {
    if (maintenance_ != null && maintenance_.isRunning())
    {
      synchronized(maintenance_)
      {
        if (reduced)
          reduceConnectionCount();     // Check to see if number of available connections needs adjusting.
        maintenance_.notify();
      }
    }
  }

  /**
  *  Sets the data source used to make connections.
  *  @param dataSource The AS400JDBCConnectionPoolDataSource object.
  *  @exception PropertyVetoException If a change is vetoed.
  **/
  public void setDataSource(AS400JDBCConnectionPoolDataSource dataSource) throws PropertyVetoException
  {
    String property = "dataSource";
    if (dataSource == null)
      throw new NullPointerException(property);

    if (isInUse())
    {
            JDTrace.logInformation (this, "Connection pool data source is already in use.");  // @B5C
      throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    AS400JDBCConnectionPoolDataSource old = dataSource_;

    dataSource_ = dataSource;
    changes_.firePropertyChange(property, old, dataSource);
  }


  /**
  *  PoolConnectionEventListener to manage the closing of pooled connections to return 
  *  them to the pool for reuse.
  **/
  private class PoolConnectionEventListener implements ConnectionEventListener
  {
    /**
    *  Constructs a default PoolConnectionEventListener.
    **/
    public PoolConnectionEventListener()
    {
    }                                                                 

    /**
    *  Removes the pooled connection from the pool in the event that a connection error occurs
    *  making the connection unusable. 
    *  @param event The ConnectionEvent object.
    **/
    public void connectionErrorOccurred(ConnectionEvent event)
    {
            JDTrace.logInformation (this, "PooledConnection is in error...");  // @B5C
      closePooledConnection( (AS400JDBCPooledConnection)event.getSource() );
    }

    /**
    *  Returns the pooled connection to the available pool for reuse.
    *  Note: called from the ConnectionHandle.close().
    *  @param event The ConnectionEvent object.
    **/
    public void connectionClosed(ConnectionEvent event)
    {
      AS400JDBCPooledConnection connection = (AS400JDBCPooledConnection)event.getSource();

      synchronized (activePool_)
      {
        activePool_.removeElement(connection);       // Update the pools.
      }

      // Determine if connection has expired.
      if ((getMaxLifetime() != -1 && connection.getLifeSpan() > getMaxLifetime()) ||         // Max lifetime exceeded.
          (getMaxUseCount() != -1 && connection.getUseCount() == getMaxUseCount()) ||        // Max Use Count.
          (getMaxConnections() != -1 &&activePool_.size() > getMaxConnections()))           // MaxConnections reduced.
      {
                JDTrace.logInformation (this, "Connection has expired.  Removed from the pool.");  // @B5C
        closePooledConnection(connection);

        // Notify listeners that the connection expired
        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(connection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
        poolListeners_.fireConnectionExpiredEvent(poolEvent);
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
        synchronized (availablePool_)
        {
          availablePool_.addElement(connection);                // connection still good, reuse.
        }

        // Notify listeners that a connection was returned.
        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(connection, ConnectionPoolEvent.CONNECTION_RETURNED); //@A5C
        poolListeners_.fireConnectionReturnedEvent(poolEvent);
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
      if (!isThreadUsed() && isRunMaintenance() && System.currentTimeMillis() - lastSingleThreadRun_ > getCleanupInterval())
        cleanupConnections();
    }
  }
}
