///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
*  Represents a pool of connections to the system.
*
*  <P>ConnectionPool objects generate the following events:
*  <ul>
*    <li>{@link ConnectionPoolEvent ConnectionPoolEvent} - The events fired are:
*      <ul>
*       <li>{@link ConnectionPoolEvent#CONNECTION_CREATED CONNECTION_CREATED}</li>
*       <li>{@link ConnectionPoolEvent#CONNECTION_EXPIRED CONNECTION_EXPIRED}</li>
*       <li>{@link ConnectionPoolEvent#CONNECTION_POOL_CLOSED CONNECTION_POOL_CLOSED}</li>
*       <li>{@link ConnectionPoolEvent#CONNECTION_RELEASED CONNECTION_RELEASED}</li>
*       <li>{@link ConnectionPoolEvent#CONNECTION_RETURNED CONNECTION_RETURNED}</li>
*       <li>{@link ConnectionPoolEvent#MAINTENANCE_THREAD_RUN MAINTENANCE_THREAD_RUN}</li>
*       </ul>
*    </li>
*    <li>PropertyChangeEvent</li>
*  </ul>
**/
public abstract class ConnectionPool implements Serializable
{
  static final long serialVersionUID = 4L;

  /**
   Indicates that the CCSID used for new connections is the same as the system default CCSID.
   **/
  static final int CCSID_DEFAULT = ConnectionPoolProperties.CCSID_DEFAULT;

  ConnectionPoolProperties properties_ = new ConnectionPoolProperties();
  private boolean inUse_ = false;
  private boolean isRunMaintenance_ = true;
  //@A4D private boolean useThreads_ = true;

  transient PoolMaintenance maintenance_;                // Maintenance thread.
  transient PropertyChangeSupport changes_;
  transient ConnectionPoolEventSupport poolListeners_;   // Manage the ConnectionPoolEvent listeners.

  public ConnectionPool()
  {
//@CRS    properties_ = new ConnectionPoolProperties();
//@CRS    initializeTransient();
  }

  /**
  *  Adds a ConnectionPoolListener.
  *  @param listener The ConnectionPoolListener.
  *  @see #removeConnectionPoolListener
  **/
  public void addConnectionPoolListener(ConnectionPoolListener listener)
  {
    synchronized(this)
    {
      if (poolListeners_ == null) poolListeners_ = new ConnectionPoolEventSupport();
    }
    poolListeners_.addConnectionPoolListener(listener);
  }

  /**
  *  Adds a PropertyChangeListener.
  *  @param listener The PropertyChangeListener.
  *  @see #removePropertyChangeListener
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    synchronized(this)
    {
      if (changes_ == null) changes_ = new PropertyChangeSupport(this);
      changes_.addPropertyChangeListener(listener);
      properties_.addPropertyChangeListener(listener);
    }
  }

  /**
  *  Removes any connections that have exceeded the maximum inactivity time, replaces any
  *  connections that have aged past maximum usage or maximum lifetime, and removes any
  *  connections that have been in use too long.
  **/
  abstract void cleanupConnections();

  /**
  *  Closes the connection pool.
  *  @exception ConnectionPoolException If a pool error occurs.
  **/
  public abstract void close() throws ConnectionPoolException;

  /**
  *  Closes the connection pool if not explicitly closed by the caller.
  *  @exception Throwable If an error occurs.
  **/
  protected void finalize() throws Throwable
  {
    // Terminate the maintenance thread, if it's still alive.
    if (maintenance_ != null && maintenance_.isAlive()) {
      maintenance_.shutdown();
    }
    super.finalize();
  }

  /**
   *  Returns the CCSID that is used when creating connections.
   *  The default value is the system default CCSID as determined by the AS400 class.
   *  @return The CCSID, or {@link #CCSID_DEFAULT CCSID_DEFAULT} if the system default CCSID is used.
   **/
  int getCCSID()
  {
    return properties_.getCCSID();
  }

  /**
  *  Returns the time interval for how often the maintenance daemon is run.
  *  The default value is 300000 milliseconds (5 minutes).
  *  @return     Number of milliseconds.
  **/
  public long getCleanupInterval()
  {
    return properties_.getCleanupInterval();
  }

  /**
    *  Returns the maximum number of connections.
  *  The default value is -1 indicating no limit to the number of connections.
    *  @return Maximum number of connections.
    **/
  public int getMaxConnections()
  {
    return properties_.getMaxConnections();
  }

  /**
  *  Returns the maximum amount of inactive time before an available connection is closed.
*  The maintenance daemon closes the connection if the threshold is reached.
*  The default value is 60 minutes.  A value of -1 indicates that there is no limit.
  *  @return     Number of milliseconds.
  **/
  public long getMaxInactivity()
  {
    return properties_.getMaxInactivity();
  }

  /**
  *  Returns the maximum life for an available connection.
*  The maintenance daemon closes the available connection if the threshold is reached.
*  The default value is a 24 hour limit.  A value of -1 indicates that there is no limit.
*  @return Number of milliseconds.
  **/
  public long getMaxLifetime()
  {
    return properties_.getMaxLifetime();
  }

  /**
    *  Returns the maximum number of times a connection can be used before it is replaced in the pool.
    *  The default value is -1.  A value of -1 indicates that there is no limit.
    *  @return Maximum usage count.
    **/
  public int getMaxUseCount()
  {
    return properties_.getMaxUseCount();
  }

  /**
    *  Returns the maximum amount of time a connection can be in use before it is closed and returned to the pool.
    *  The default value is -1 indicating that there is no limit.
    *  @return Number of milliseconds.
    **/
  public long getMaxUseTime()
  {
    return properties_.getMaxUseTime();
  }

  /**
  *  Returns the connection pool properties used by the maintenance daemon.
  *  @return The ConnectionPoolProperties object.
  **/
//@CRS  ConnectionPoolProperties getProperties()
//@CRS  {
//@CRS    return properties_;
//@CRS  }

  /**
  *  Initializes the transient data.
  **/
  private void initializeTransient()
  {
//@CRS    changes_ = new PropertyChangeSupport(this);
//@CRS    poolListeners_ = new ConnectionPoolEventSupport();

//    addPropertyChangeListener(new PropertyChangeListener()      //@A2A
//                              {
//                                public void propertyChange(PropertyChangeEvent event)
//                                {
//                                  String property = event.getPropertyName();

//                                  if (property.equals("cleanupInterval"))
//                                  {
//                                    runMaintenance(false);
//                                  }
//                                  else if (property.equals("maxConnections"))
//                                  {
//                                    boolean reduced = false;
//                                    Integer newValue = (Integer)event.getNewValue();
//                                    Integer oldValue = (Integer)event.getOldValue();
//                                    if (oldValue.intValue() == -1 || oldValue.intValue() > newValue.intValue())  //@A2C
//                                      reduced = true;

//                                    runMaintenance(reduced);
//                                  }
//                                  else if (property.equals("runMaintenance"))
//                                  {
//                                    Boolean newValue = (Boolean)event.getNewValue();
//                                    if (!newValue.booleanValue() && maintenance_ != null && maintenance_.isRunning())
//                                      maintenance_.setRunning(false);
//                                    if (newValue.booleanValue() && maintenance_ != null && !maintenance_.isRunning()) //@A3A
//                                      maintenance_.setRunning(true); //@A3A
//                                  }
//                                }
//                              });
  }

  /**
  *  Indicates whether the pool is in use.
  *  Used for checking state conditions.  The default is false.
  *  @return true if the pool is in use; false otherwise.
  **/
  synchronized boolean isInUse()
  {
    return inUse_;
  }

  /**
  *  Indicates whether the maintenance thread is used to cleanup expired connections.
  *  The default is true.
  *  @return true if expired connection are cleaned up by the maintenance thread; false otherwise.
  **/
  public boolean isRunMaintenance()
  {
    return isRunMaintenance_;
  }

  /**
  *  Indicates whether threads are used in communication with the host servers and for running
  *  maintenance.  This property affects AS400ConnectionPool and not AS400JDBCConnectionPool
  *  which is written to the JDBC specification.
  *  The default value is true.
  *  @return true if threads are used; false otherwise.
  **/
  public boolean isThreadUsed()
  {
    //@A4D return useThreads_;
    return properties_.isThreadUsed();  //@A4A
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
  *  Removes a PropertyChangeListener.
  *  @param listener The PropertyChangeListener.
  *  @see #addPropertyChangeListener
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    if (changes_ != null) changes_.removePropertyChangeListener(listener);
    properties_.removePropertyChangeListener(listener);
  }

  /**
  *  Removes a ConnectionPoolListener.
  *  @param listener The ConnectionPoolListener.
  *  @see #addConnectionPoolListener
  **/
  public void removeConnectionPoolListener(ConnectionPoolListener listener)
  {
    if (poolListeners_ != null) poolListeners_.removeConnectionPoolListener(listener);
  }

  //@A2A
  /**
   *  Run cleanupConnections().
   *  @param reduced true if need to check current num connections; false otherwise.
   **/
  abstract void runMaintenance(boolean reduced);

  /**
  *  Sets the CCSID to use when creating connections.
  *  The default value is the system default CCSID as determined by the AS400 class.
  *  @param ccsid The CCSID to use for connections in the pool, or {@link #CCSID_DEFAULT CCSID_DEFAULT} to indicate that the system default CCSID should be used.
  **/
  void setCCSID(int ccsid)
  {
    properties_.setCCSID(ccsid);
  }

  /**
  *  Sets the time interval for how often the maintenance daemon is run.
  *  The default value is 300000 milliseconds or 5 minutes.
  *  @param cleanupInterval The number of milliseconds.
  **/
  public void setCleanupInterval(long cleanupInterval)
  {
    properties_.setCleanupInterval(cleanupInterval);
    runMaintenance(false);

//@CRS    if (getCleanupInterval() == 0)
    if (cleanupInterval == 0) setRunMaintenance(false);
  }

  /**
  *  Sets whether the pool is in use.
  *  Used for setting state conditions.
  *  @param inUse true if the pool is in use; false otherwise.
  **/
  synchronized void setInUse(boolean inUse)
  {
    inUse_ = inUse;
  }

  /**
    *  Sets the maximum number of connections.
    *  The default value is -1 indicating no limit to the number of connections.
    *  @param maxConnections Maximum number of connections.
    **/
  public void setMaxConnections(int maxConnections)
  {
    int oldValue = getMaxConnections();
    properties_.setMaxConnections(maxConnections);
    boolean reduced = (oldValue == -1 || oldValue > maxConnections);
    runMaintenance(reduced);
  }

  /**
  *  Sets the maximum amount of inactive time before an available connection is closed.
*  The maintenance daemon closes the connection if the threshold is reached.
*  The default value is 60 minutes.  A value of -1 indicates that there is no limit.
  *  @param   maxInactivity  Number of milliseconds.
  **/
  public void setMaxInactivity(long maxInactivity)
  {
    properties_.setMaxInactivity(maxInactivity);
  }

  /**
  *  Sets the maximum life for an available connection.
*  The maintenance daemon closes the available connection if the threshold is reached.
*  The default value is a 24 hour limit.  A value of -1 indicates that there is no limit.
  *  @param maxLifetime Number of milliseconds.
  **/
  public void setMaxLifetime(long maxLifetime)
  {
    properties_.setMaxLifetime(maxLifetime);
  }

  /**
  *  Sets the maximum number of times a connection can be used before it is replaced in the pool.
  *  The default value is -1.  A value of -1 indicates that there is no limit.
  *  @param   maxUseCount  Maximum usage count.
  **/
  public void setMaxUseCount(int maxUseCount)
  {
    properties_.setMaxUseCount(maxUseCount);
  }

  /**
  *  Sets the maximum amount of time a connection can be in use before it is closed and returned to the pool.
  *  The default value is -1 indicating that there is no limit.
  *  @param maxUseTime Number of milliseconds.
  **/
  public void setMaxUseTime(long maxUseTime)
  {
    properties_.setMaxUseTime(maxUseTime);
  }

  /**
   *  Sets whether the Toolbox does periodic maintenance on the connection pool to clean up
   *  expired connections.  If setThreadUsed is true, the Toolbox starts an extra thread to
   *  perform maintenance.  If setThreadUsed is false, the Toolbox will perform maintenance
   *  on the user's thread when it uses the connection pool.
   *  This method and {@link #setThreadUsed setThreadUsed} can be set
   *  in interchangeable order.
   *  The default value is true.
   *  @param cleanup If expired connections are cleaned up by the maintenance daemon.
   **/
  public void setRunMaintenance(boolean cleanup)
  {
    String property = "runMaintenance";

//    Boolean oldValue = new Boolean(isRunMaintenance_);
//    Boolean newValue = new Boolean(cleanup);
    boolean oldValue = isRunMaintenance_;

    isRunMaintenance_ = cleanup;
    if (changes_ != null) changes_.firePropertyChange(property, new Boolean(oldValue), new Boolean(cleanup));
  
    if (maintenance_ != null)
    {
      //@CRS: We don't care if maintenance is currently running or not,
      // we just want to tell it to either run or not, from now on.
      maintenance_.setRunning(cleanup);
    }
  }

  /**
  *  Sets whether the IBM Toolbox for Java uses additional threads.
  *  The Toolbox creates additional threads for various purposes, including:
  *  <ul>
  *  <li>for communication with the host servers; and</li>
  *  <li>for running pool maintenance.</li>
  *  </ul>
  *  Letting the IBM Toolbox for Java use additional threads will be beneficial
  *  to performance, but turning threads off may be necessary if your application needs to be compliant
  *  with the Enterprise Java Beans specification. The threadUsed property cannot be changed once
  *  the pool is in use.  This property affects AS400ConnectionPool and not AS400JDBCConnectionPool,
  *  which is written to the JDBC specification.  This method and
  *  {@link #setRunMaintenance setRunMaintenance} can be called
  *  in interchangeable order.
  *  The default value is true.
  *  @param useThreads true to use additional threads; false otherwise.
  **/
  public void setThreadUsed(boolean useThreads)
  {
    //@A4D Moved code to ConnectionPoolProperties
    //@A4D String property = "threadUsed";
    //@A4D if (isInUse())
    //@A4D    throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

    //@A4D Boolean oldValue = new Boolean(isThreadUsed());
    //@A4D Boolean newValue = new Boolean(useThreads);

    //@A4D useThreads_ = useThreads;
    //@A4D changes_.firePropertyChange(property, oldValue, newValue);
    properties_.setThreadUsed(useThreads, isInUse());           //@A4A
  }
}
