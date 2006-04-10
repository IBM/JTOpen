///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolMaintenance.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
*  The AS400JDBCConnectionPoolMaintenance class cleans up pooled connections
*  that have expired.
**/
class PoolMaintenance extends Thread
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  private boolean run_ = false;        // Whether the maintenance is running.
  private boolean stayAlive_ = true;   // Whether thread should stay alive.
  private transient long lastRun_;     // Last time maintenance was run.
  private transient ConnectionPool pool_;

  /**
  *  Constructs a AS400JDBCConnectionPoolMaintenance object.
  *  @param pool The AS400JDBCConnectionPool object.
  **/
  public PoolMaintenance(ConnectionPool pool)
  {
    super("AS400ConnectionPoolMaintenanceThread");
    setDaemon(true);
    pool_ = pool;
    lastRun_ = System.currentTimeMillis();     // Set start time.
  }

  /**
  *  Returns the last time the maintenance was run.
  *  @return The time in milliseconds.
  **/
  public long getLastTime()
  {
    return lastRun_;
  }

  /**
  *  Indicates whether the maintenance thread is running.
  *  @return true if running; false otherwise.
  **/
  public boolean isRunning()
  {
    return run_;
  }

  /**
  *  Runs the pool maintenance cleanup thread.
  **/
  public synchronized void run()
  {
    if (Trace.traceOn_)
    {
      Trace.log(Trace.INFORMATION, "Connection pool maintenance daemon is started...");
    }
    run_ = true;
    while (stayAlive_)
    {
      if (run_)
      {
        try
        {
          // sleep for cleanup interval.
          wait(pool_.getCleanupInterval());
        }
        catch (InterruptedException ie)
        {
          Trace.log(Trace.ERROR, "Connection pool maintenance daemon failed.");
        }
        pool_.cleanupConnections();

        lastRun_ = System.currentTimeMillis();       // set the time of last run.
      }
      else
      {
        try
        {
          wait();  // wait for someone to notify() me to continue
        }
        catch (InterruptedException e)
        {
          Trace.log(Trace.ERROR, "Connection pool maintenance daemon failed.");
        }
      }
    }
  }

  /**
  *  Sets whether the maintenance thread is running.
  *  Note: Calling this method with 'false' does not terminate the maintenance thread.
  *  To terminate the thread, call shutdown().
  *  @param running true if running; false otherwise.
  **/
  public synchronized void setRunning(boolean running)
  {
    if (run_ != running)
    {
      run_ = running;
      notify();
    }
  }

  /**
  *  Informs the maintenance thread that it should terminate.
  **/
  public void shutdown()
  {
    run_ = false;
    stayAlive_ = false;
    notify();
  }

}
  
