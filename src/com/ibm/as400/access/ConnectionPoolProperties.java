///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConnectionPoolProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
*  The ConnectionPoolProperties class contains properties for maintaining a ConnectionPool.  
*  It is used to control the behavior of the connection pool.
*
*  A maintenance daemon is used to validate the connection status and determine if the connection should
*  be closed based on the timeout limits.  The daemon can be set to run at any defined time interval.
**/
class ConnectionPoolProperties implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private long cleanupInterval_ = 300000;				   // 5 minutes maintenance intervals
   private int maxConnections_ = -1;				         // maximum number of active connections for the datasource.
   private long maxInactivity_ = 3600000;	               // 60 minutes
   private long maxLifetime_ = 86400000;	               // 24 hours
   private int maxUseCount_ = -1;                        // maximum number of times connection can be used.
   private long maxUseTime_ = -1;					         // maximum usage time, release after this period, -1 for never
   private boolean useThreads_ = true;                 

   transient private PropertyChangeSupport changes_;

	/**
	*  Constructs a default ConnectionPoolProperties object.  
   **/
	public ConnectionPoolProperties()
	{
      initializeTransient();
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

      changes_.addPropertyChangeListener(listener);
   }

   /**
   *  Indicates whether threads are used in communication with the host servers.
   *  The default value is true.
   *  @return true if threads are used; false otherwise.
   **/
   public boolean isThreadUsed()
   {
   	return useThreads_;
   }


	/**
	*  Returns the time interval for how often the maintenance daemon is run.
	*  The default value is 300000 milliseconds.
	*  @return     Number of milliseconds.
	**/
	public long getCleanupInterval()
	{
		return cleanupInterval_;
	}
	
   /**
	*  Returns the maximum number of connections.
   *  The default value is -1 indicating no limit to the number of connections.
	*  @return Maximum number of connections.
	**/
	public int getMaxConnections()
	{
		return maxConnections_;
	}

	/**
	*  Returns the maximum amount of inactive time before the connection is closed.
   *  The maintenance daemon closes the connection if the threshold is reached.
   *  The default value is 60 minutes.  A value of -1 indicates that there is no limit.
	*  @return     Number of milliseconds.
	**/
	public long getMaxInactivity()
	{
		return maxInactivity_;
	}

	/**
	*  Returns the maximum life for an available connection.
   *  The maintenance daemon closes the available connection if the threshold is reached.
   *  The default value is a 24 hour limit.  A value of -1 indicates that there is no limit.
   *  @return Number of milliseconds.
	**/
	public long getMaxLifetime()
	{
		return maxLifetime_;
	}
	
   /**
	*  Returns the maximum number of times a connection can be used before it is replaced in the pool.
	*  The default value is -1.  A value of -1 indicates that there is no limit.
	*  @return Maximum usage count.
	**/
	public int getMaxUseCount()
	{
		return maxUseCount_;
	}
	
   /**
	*  Returns the maximum amount of time a connection can be in use before it is closed and returned to the pool.
	*  The default value is -1 indicating that there is no limit.
	*  @return Number of milliseconds.
	**/
	public long getMaxUseTime()
	{
		return maxUseTime_;
	}

   /**
   *  Initializes the transient data.
   **/
   private void initializeTransient()
   {
      changes_ = new PropertyChangeSupport(this);
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

      changes_.removePropertyChangeListener(listener);
   }

   /**
	*  Sets the time interval for how often the maintenance daemon is run.
	*  The default value is 300000 milliseconds or 5 minutes.
	*  @param cleanupInterval The number of milliseconds.
	**/
	public void setCleanupInterval(long cleanupInterval)
	{
      String property = "cleanupInterval";

      if (cleanupInterval < 0) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Long oldValue = new Long(getCleanupInterval());
      Long newValue = new Long(cleanupInterval);

		cleanupInterval_ = cleanupInterval;
      changes_.firePropertyChange(property, oldValue, newValue);
	}
	
   /**
	*  Sets the maximum number of connections.
	*  The default value is -1 indicating no limit to the number of connections.
	*  @param maxConnections Maximum number of connections.
	**/
	public void setMaxConnections(int maxConnections)
	{
      String property = "maxConnections";
      if (maxConnections < -1) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldValue = new Integer(getMaxConnections());
      Integer newValue = new Integer(maxConnections);

		maxConnections_ = maxConnections;
      changes_.firePropertyChange(property, oldValue, newValue);
	}

 	/**
	*  Sets the maximum amount of inactive time before the connection is closed.
   *  The maintenance daemon closes the connection if the threshold is reached.
   *  The default value is 60 minutes.  A value of -1 indicates that there is no limit.
	*  @param   maxInactivity  Number of milliseconds.
	**/
	public void setMaxInactivity(long maxInactivity)
	{
      String property = "maxInactivity";
      if (maxInactivity < -1) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Long oldValue = new Long(getMaxInactivity());
      Long newValue = new Long(maxInactivity);

		maxInactivity_ = maxInactivity;
      changes_.firePropertyChange(property, oldValue, newValue);
	}

	/**
	*  Sets the maximum life for an available connection.
   *  The maintenance daemon closes the available connection if the threshold is reached.
   *  The default value is a 24 hour limit.  A value of -1 indicates that there is no limit.
	*  @param maxLifetime Number of milliseconds.
	**/
	public void setMaxLifetime(long maxLifetime)
	{
      String property = "maxLifetime";
      if (maxLifetime < -1) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Long oldValue = new Long(getMaxLifetime());
      Long newValue = new Long(maxLifetime);

		maxLifetime_ = maxLifetime;
      changes_.firePropertyChange(property, oldValue, newValue);
	}


  /**
   *  Sets whether the AS/400 Toolbox for Java uses threads in communication with the host servers
   *  and whether the pool uses threads for running maintenance.   
   *  The default value is true.   
   *  @param useThreads true to use threads; false otherwise.
   **/
   public void setThreadUsed(boolean useThreads, boolean isInUse)
   {
      String property = "threadUsed";
      if (isInUse) 
         throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

      Boolean oldValue = new Boolean(isThreadUsed());
      Boolean newValue = new Boolean(useThreads);

      useThreads_ = useThreads;
      changes_.firePropertyChange(property, oldValue, newValue);

   }

	/**
	*  Sets the maximum number of times a connection can be used before it is replaced in the pool.
	*  The default value is -1.  A value of -1 indicates that there is no limit.
	*  @param   maxUseCount  Maximum usage count.
	**/
	public void setMaxUseCount(int maxUseCount)
	{
      String property = "maxUseCount";
      if (maxUseCount < -1) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldValue = new Integer(getMaxUseCount());
      Integer newValue = new Integer(maxUseCount);

		maxUseCount_ = maxUseCount;
      changes_.firePropertyChange(property, oldValue, newValue);
   }

	/**
	*  Sets the maximum amount of time a connection can be in use before it is closed and returned to the pool.
	*  The default value is -1 indicating that there is no limit.
	*  @param maxUseTime Number of milliseconds.
	**/
	public void setMaxUseTime(long maxUseTime)
	{
      String property = "maxUseTime";
      if (maxUseTime < -1) 
         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Long oldValue = new Long(getMaxUseTime());
      Long newValue = new Long(maxUseTime);

		maxUseTime_ = maxUseTime;
      changes_.firePropertyChange(property, oldValue, newValue);
	}
}
