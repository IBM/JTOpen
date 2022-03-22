///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;                            // Java2
import java.util.Vector;                              // Java2

/**
*  Represents a pool of JDBC connections
*  that are available for use by a Java program.
*  <p>
*  Note: AS400JDBCConnectionPool objects are threadsafe.
*
*  <p>The following example creates a connection pool with 10 connections.
*  <blockquote><pre>
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
*  </pre></blockquote>
**/
public class AS400JDBCConnectionPool extends ConnectionPool implements Serializable
{
  static final long serialVersionUID = 4L;



  private boolean closed_;                                       // If the pool is closed.
  private AS400JDBCConnectionPoolDataSource dataSource_;         // The dataSource used for the connection.
  transient long lastSingleThreadRun_;                   // Last time maintenance was run (single-thread mode).
  transient Vector activePool_;                          // Active connections.
  transient Vector availablePool_;                       // Available connections.
  transient Vector deadPool_;                            // Connections staged for removal and disconnection.
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
    boolean trace = JDTrace.isTraceOn();                                              // @B5C
    if (trace)
    {
      JDTrace.logInformation (this, "ConnectionPool cleanup...");
      JDTrace.logInformation (this, "   MaxLifeTime: " + getMaxLifetime());         // @B5C
      JDTrace.logInformation (this, "   MaxUseTime: " + getMaxUseTime());           // @B5C
      JDTrace.logInformation (this, "   MaxInactivity: " + getMaxInactivity());     // @B5C
      JDTrace.logInformation (this, "   PretestConnections: " + isPretestConnections());

      JDTrace.logInformation (this, "Idle Connections: " + availablePool_.size());  // @B5C
      JDTrace.logInformation (this, "Active Connections: " + activePool_.size());   // @B5C
      JDTrace.logInformation (this, "Dead Connections: " + deadPool_.size());
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
            AS400JDBCPooledConnection poolConnection = (AS400JDBCPooledConnection)connections[i].next();

            if (trace)
              JDTrace.logInformation (this, poolConnection.toString());     // @B5C

            if ((!poolConnection.isInUse() && getMaxLifetime() !=-1 && poolConnection.getLifeSpan() > getMaxLifetime()) ||   //@B1C       // inactive connections only.
                (!poolConnection.isInUse() && getMaxInactivity() !=-1 && poolConnection.getInactivityTime() > getMaxInactivity()))  //@B1C //@B3C
            {
              if (trace)
                JDTrace.logInformation (this, "Removing expired connection from the pool.");  // @B5C

              connections[i].remove();

              // Stage the connection for closing.
              synchronized (deadPool_)
              {
                deadPool_.addElement(poolConnection);
              }

              // Notify listeners that the connection expired
              if (poolListeners_ != null)
              {
                ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
                poolListeners_.fireConnectionExpiredEvent(poolEvent);
              }
            }
            else if (getMaxUseTime() > 0 &&
                     poolConnection.getInUseTime() > getMaxUseTime())       // only valid with active connections.
            {
              if (trace)
                JDTrace.logInformation (this, "Returning active connection to the pool.");  // @B5C

              poolConnection.returned(); // invalidate the connection handle
              availablePool_.add(poolConnection);
              connections[i].remove();                  

              // Notify listeners that the connection expired
              if (poolListeners_ != null)
              {
                ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
                poolListeners_.fireConnectionExpiredEvent(poolEvent);
              }
            }
          }   // 'while' loop
        }  // 'for' loop
      } // synchronized (activePool_)
    } // synchronized (availablePool_)

    // Close the removed connections.
    synchronized (deadPool_)
    {
      Iterator connections = deadPool_.iterator();
      while (connections.hasNext())
      {
        AS400JDBCPooledConnection poolConnection = (AS400JDBCPooledConnection)connections.next();

        if (trace)
          JDTrace.logInformation (this, poolConnection.toString());

        if (trace)
          JDTrace.logInformation (this, "Removing dead connection from the pool.");

        closePooledConnection(poolConnection);
        connections.remove();
      }
    }

    // Notify listeners that the maintenance thread was run.
    if (poolListeners_ != null)
    {
      ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(this, ConnectionPoolEvent.MAINTENANCE_THREAD_RUN);
      poolListeners_.fireMaintenanceThreadRun(poolEvent);
    }

    // Check if maintenance should keep running.
    synchronized (availablePool_)
    {
      synchronized (activePool_)
      {
        if (activePool_.isEmpty() && availablePool_.isEmpty())
        {
          if (maintenance_ != null) maintenance_.setRunning(false);
          setInUse(false);          // data source CAN be changed.
        }
      }
    }

    if (!isThreadUsed())
      lastSingleThreadRun_ = System.currentTimeMillis();

    if (trace)
    {
      JDTrace.logInformation (this, "ConnectionPool cleanup finished.");  // @B5C
      JDTrace.logInformation (this, "   Idle Connections: " + availablePool_.size());  // @B5C
      JDTrace.logInformation (this, "   Active Connections: " + activePool_.size());  // @B5C
      JDTrace.logInformation (this, "   Dead Connections: " + deadPool_.size());
    }
  }

  /**
  *  Closes all the database connections in the pool.
  **/
  public void close()
  {
    if (JDTrace.isTraceOn())                                                   // @B5C
    {
      JDTrace.logInformation (this, "Closing the JDBC connection pool.");    // @B5C
      JDTrace.logInformation (this, "Available: " + availablePool_.size());  // @B5C
      JDTrace.logInformation (this, "Active: " + activePool_.size());        // @B5C
    }

    // Close all connections in the pool.
    synchronized (availablePool_)
    {
      synchronized (activePool_)
      {
        synchronized (deadPool_)
        {
          Iterator[] connections = { availablePool_.iterator(), activePool_.iterator(), deadPool_.iterator()};

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
    }

    // Terminate the maintenance thread, if it's still alive.
    if (maintenance_ != null && maintenance_.isAlive()) {
      maintenance_.shutdown();  // tell the thread to terminate
    }

    synchronized( this) { // @A7A  
      if (isInUse())
        setInUse(false);                     // data source CAN be changed.
    }
    // Notify the listeners.
    if (poolListeners_ != null)
    {
      ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_POOL_CLOSED);  
      poolListeners_.fireClosedEvent(event);
    }

    closed_ = true;
  }

  /**
  *  Closes an AS400JDBCPooledConnection.
  *  @param pooledConnection The pooled connection.
  **/
  void closePooledConnection(AS400JDBCPooledConnection pooledConnection)
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
  *  @exception ConnectionPoolException If a database error occurs creating a connection for the pool, or the maximum number of connections has been reached for the pool.
  *  @exception ExtendedIllegalArgumentException if the number of connections to fill the pool with is less than one.
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
          throw new ConnectionPoolException(ConnectionPoolException.MAX_CONNECTIONS_REACHED); //@KBA    fix for JTOpen Bug 3655
        //@KBD throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
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
          if (poolListeners_ != null)
          {
            ConnectionPoolEvent event = new ConnectionPoolEvent(poolConnection, ConnectionPoolEvent.CONNECTION_CREATED);  //@A5M @A5C
            poolListeners_.fireConnectionCreatedEvent(event); //@A5M
          }
        } //@A5A
      }
    }
    catch (SQLException e)
    {
      if (isRunMaintenance() && maintenance_ != null)
        cleanupConnections();                 // re-check old connections.
      throw new ConnectionPoolException(e);
    }

    synchronized(this) { 
    if (!isInUse())
      {
        setInUse(true);                   // Data source now can NOT be changed.

        if (isClosed())
          closed_ = false;                          // Set the state to OPEN if previously closed.
      }
    }

    if (isRunMaintenance() && isThreadUsed())
    {
      if (maintenance_ == null)
      {
        synchronized(this) //@CRS
        {
          if (maintenance_ == null) //@CRS
          {
            maintenance_ = new PoolMaintenance(this);
            maintenance_.start();                     // Start the first time.
            // Give thread a chance to start.                                      
//            if (!maintenance_.isRunning())                                         //@A2C
//            {
//              try
//              {                                                                //@A2A
//                Thread.sleep(10);                                              //@A2A
//              }                                                                  //@A2A
//              catch (InterruptedException e)                                     //@A2A
//              {
                //Ignore  	        					   //@A2A
//              }                                                                  //@A2A
//            }                                                                      //@A2A
            // If thread has still not started, keep giving it chances for 5 minutes.
//            for (int i = 1; !maintenance_.isRunning() && i<6000; i++)              //@A2A
//            {
//              try
//              {                  //@A2A
//                Thread.sleep(50);                                              //@A2A
//              }                  //@A2A
//              catch (InterruptedException ie)            //@A2A
//              {
                //Ignore							   //@A2A
//              }                  //@A2A
//            }                    //@A2A
//            if (!maintenance_.isRunning())             //@A2A
//              JDTrace.logInformation (this, "maintenance thread failed to start");    //@A2A
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
    AS400JDBCPooledConnection pooledConnection = getPooledConnection();

    Connection connection = null;
    try
    {
      connection = pooledConnection.getConnection();
    }
    catch (SQLException sql)
    {
      throw new ConnectionPoolException(sql);
    }

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

    final int maxTries = Math.max(getMaxConnections(), 1+availablePool_.size());
    int numTries = 0;
    while (pooledConnection == null &&
           ++numTries <= maxTries)  // eliminate any possibility of an infinite loop
    {
      synchronized (availablePool_)
      {
        //@B2M Moved following two lines into the synchronization block.
        if (availablePool_.isEmpty())                                    //@B2M
          fill(1);                         // Add a new connection.    //@B2M

        //@CRS pooledConnection = (AS400JDBCPooledConnection)availablePool_.firstElement();
        pooledConnection = (AS400JDBCPooledConnection)availablePool_.lastElement(); //@CRS -- Most recently used.

        // Remove the pooled connection from the available list.
        availablePool_.removeElement(pooledConnection);    
      }

      // Pre-test the connection, if the property is set.
      try
      {
        if (isPretestConnections() && !pooledConnection.isConnectionAlive())
        {
          if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "Connection failed a pretest.");

          synchronized (deadPool_)
          {
            deadPool_.addElement(pooledConnection);      
          }
          pooledConnection = null;
        }
      }
      catch (SQLException sql)
      {
        throw new ConnectionPoolException(sql);
      }

      if (pooledConnection != null)
      {
        synchronized (activePool_)
        {
          activePool_.addElement(pooledConnection);      
        }
      }
    }

    if (pooledConnection == null)
    {
      JDTrace.logInformation (this, "Exceeded maximum attempts to get a valid connection: " + numTries);
      throw new ConnectionPoolException(ConnectionPoolException.UNKNOWN_ERROR);
    }

    // Notify the listeners that a connection was released.
    if (poolListeners_ != null)
    {
      ConnectionPoolEvent event = new ConnectionPoolEvent(pooledConnection, ConnectionPoolEvent.CONNECTION_RELEASED);  //@A5C
      poolListeners_.fireConnectionReleasedEvent(event);
    }
    return pooledConnection;
  }


  /**
  *  Initializes the transient data.
  **/
  private void initializeTransient()
  {
    eventListener_ = new PoolConnectionEventListener(this);

    activePool_ = new Vector();
    availablePool_ = new Vector();  
    deadPool_ = new Vector();  
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
            synchronized (deadPool_)
            {
              // Stage the connection for closing.
              deadPool_.addElement(poolConnection);
            }
          }
        }
      }
    }

    // Close the removed connections.
    synchronized (deadPool_)
    {
      Iterator deadConnections = deadPool_.iterator();
      while (deadConnections.hasNext())
      {
        AS400JDBCPooledConnection poolConnection = (AS400JDBCPooledConnection)deadConnections.next();
        closePooledConnection(poolConnection);
        deadConnections.remove();
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
    if(changes_ != null)                                       //@K1A
        changes_.firePropertyChange(property, old, dataSource);
  }
}
