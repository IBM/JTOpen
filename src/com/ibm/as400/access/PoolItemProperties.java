///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolItemProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class PoolItemProperties
{
   private long creationTime_   = 0;  // Time when this connection was created.
   private long lastUseTime_    = 0;  // Time when this connection became in-use.
   private long timeIdleInPool_ = 0;  // Time when this connection became available.
   private int  timesUsedCount_ = 0;  // Number of times connection has been assigned.

   /**
   *  Constructs a default PoolItemProperties object.
   **/
   public PoolItemProperties()
   {
      // Initialize the timers.
      creationTime_ = System.currentTimeMillis();
      timeIdleInPool_ = creationTime_;
   }

   /**
   *  Clears the timers and counters.  Called by AS400JDBCPooledConnection.close().
   **/
   public void clear()
   {
      creationTime_ = 0;
      timeIdleInPool_ = 0;
      lastUseTime_ = 0;
      timesUsedCount_ = 0;
   }

   /**
   *  Returns the time elapsed.
   *  @param startTime The starting time.
   *  @return The time elapsed (milliseconds).
   *  If a value of 0 is specified for startTime, 0 is returned.
   **/
   private long getElapsedTime(long startTime)
	{
      if (startTime == 0) 
         return 0;
      else
		   return System.currentTimeMillis() - startTime;		
	}

   /**
   *  Returns the elapsed time that the connection has been idle waiting in the pool.
   *  @return The idle time (milliseconds).
   *  If the connection is currently in use, 0 is returned.
   **/
   public long getInactivityTime()
   {
      return getElapsedTime(timeIdleInPool_);
   }
   
   /**
   *  Returns the elapsed time since the connection was assigned.
   *  @return The elapsed time (milliseconds).
   *  If the connection is not currently in use, 0 is returned.
   **/
	public long getInUseTime()
	{            
      if (isInUse()) 
         return getElapsedTime(lastUseTime_);
      else
         return 0;

	}
	
   /**
   *  Returns the elapsed time that the pooled connection has been alive.
   *  @return The elapsed time (milliseconds).
   **/
	public long getLifeSpan()
	{
		return getElapsedTime(creationTime_);
	}
	
   /**
   *  Returns the number of times the pooled connection has been assigned.
   *  @return The number of times used.
   **/
   public int getUseCount()
   {
      return timesUsedCount_;
   }

   /**
   *  Indicates if the pooled connection is currently assigned.
   *  @return true if the pooled connection is in use; false otherwise.
   **/
   public boolean isInUse()
   {
      if (lastUseTime_ == 0) 
         return false;
      else
         return true;
   }


   /**
   *  Sets the connection timer values based on the active usage state of the connection.
   *  @param inUse true if the connection is currently active; false otherwise.
   **/
   void setInUse(boolean inUse)
   {
      if (inUse)
            {
            timeIdleInPool_ = 0;                      //@B1A // reset the idle timer  
            lastUseTime_ = System.currentTimeMillis();         // set the active start time.
            timesUsedCount_++;
            }
        else
            {
         timeIdleInPool_ = System.currentTimeMillis();      // start the idle timer.
            lastUseTime_ = 0;                                  // no longer being used.
            }
    }
    }
