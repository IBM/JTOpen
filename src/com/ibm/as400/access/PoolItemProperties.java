///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private long creationTime_;                  // Creation time.
   private long lastUseTime_ = 0;               // Active connection time. 
   private long timeIdleInPool_;                // Idle time waiting in the pool.
   private int timesUsedCount_ = 0;             // Number of times connection has been used.

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
   *  Clears the timers.
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
   *  @return The time elapsed.
   **/
   private long getElapsedTime(long startTime)
	{
      if (startTime == 0) 
         return 0;
      else
		   return System.currentTimeMillis() - startTime;		
	}

   /**
   *  Returns the elapsed time the connection has been idle waiting in the pool.
   *  @return The idle time.
   **/
   public long getInactivityTime()
   {
      return getElapsedTime(timeIdleInPool_);
   }
   
   /**
   *  Returns the elapsed time the connection has been in use.
   *  @return The elapsed time.
   **/
	public long getInUseTime()
	{            
      if (isInUse()) 
         return getElapsedTime(lastUseTime_);
      else
         return lastUseTime_;

	}
	
   /**
   *  Returns the elapsed time the pooled connection has been alive.
   *  @return The elapsed time.
   **/
	public long getLifeSpan()
	{
		return getElapsedTime(creationTime_);
	}
	
   /**
   *  Returns the number of times the pooled connection has been used.
   *  @return The number of times used.
   **/
   public int getUseCount()
   {
      return timesUsedCount_;
   }

   /**
   *  Indicates if the pooled connection is in use.
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
