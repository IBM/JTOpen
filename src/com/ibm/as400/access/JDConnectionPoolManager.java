///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDConnectionPoolManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2005 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.ConnectionEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 This is the Toolbox's built-in connection pooling manager.
 Each instance of AS400JDBCManagedDataSource creates and uses an instance of this class to manage its pooled connections.

 <p>
 Note that the Toolbox's original (older) data source classes are not automatically managed:
 <ul>
 <li>AS400JDBCDataSource
 <li>AS400JDBCConnectionPoolDataSource
 <li>AS400JDBCXADataSource
 </ul>

 **/
final class JDConnectionPoolManager
implements ConnectionEventListener
{
  private static final boolean DEBUG = false;  // turn on if debugging
  private static final boolean GATHER_STATS = false;  // turn on to gather/print additional statistics (useful when debugging and performance-tuning)
  private static final boolean TESTING_ERROR_EVENTS = false;


  // Terminology for connections:
  //   'active'    == in-use     == allocated
  //   'available' == not-in-use == unallocated
  //   'condemned' == neither of the above; selected for removal from pool

  // Note to maintenance developer: Be _very_ careful about synchronizing methods.  They are likely to cause deadlocks. 

  // All of the following lists (activeConnections_, availableConnections_, and condemnedConnections_) are ultimately just lists of AS400JDBCPooledConnection objects.  They're just organized in different ways.
  // Note: When obtaining multiple simultaneous locks, the order of synchronization must never deviate from: active, available1, available2, condemned


  // Connections that are currently active (allocated).
  // This is a sorted list containing AS400JDBCPooledConnection objects.
  // Connections are added to this list when they are allocated to an application.
  // Connections are removed from this list when they are returned to the pool,
  // or when they are identified as "expired" by the reaper daemon.
  // The connections in this list are sorted by creation date (in ascending order).
  // Since the list is maintained in order of creation time, the reaper daemon can quickly
  // identify any "expired" connections, without having to examine the entire list.
  // The set contains AS400JDBCPooledConnection objects.
  // The Comparator compares connection creation dates; for identical creation dates, hashcodes are also compared.
  private final TreeSet activeConnections_ = new TreeSet(new JDAgeComparator());


  // Companion list for the activeConnections_ list.
  // Active connections that have incurred connection errors, and therefore must not be reused.
  // This is a sorted list containing AS400JDBCPooledConnection objects.
  // Connections are added to this list when they fire a connectionErrorOccurred event.
  // Connections are removed from this list when they are returned to the pool,
  // or when they are identified as "expired" by the reaper daemon.
  // The set contains AS400JDBCPooledConnection objects.
  // The Comparator simply compares connection hashcodes.
  private final HashSet activeConnectionsInError_ = new HashSet();


  // Connections that are available to be handed out.
  // Connections are added to this list when they are created by the pool manager,
  // and when they are returned to the pool after being allocated.
  // Connections are removed from this list when they are allocated to an application,
  // or when they are identified as "stale" by the maintainer daemon.
  // This array contains two HashMaps (plus a placeholder for swapping).
  // Each HashMap maps JDConnectionPoolKeys to Stacks.
  // Each Stack contains AS400JDBCPooledConnection objects.
  // The connectionKey gets you to the Stack that contains connections for that key.
  private final HashMap[] availableConnections_ = {new HashMap(100), new HashMap(100), null};


  // Companion list for the availableConnections_ list.
  // Connections are added to and removed from this list, whenever they are added to / removed from
  // the availableConnections_ list.
  // This array contains two LinkedHashSets (plus a placeholder for swapping).
  // Each LinkedHashSet contains AS400JDBCPooledConnection objects, in the order in which they were (most recently) added to the associated availableConnections_ list.
  // Note:  The availableConnections_ and idledConnectionsSequence_ lists must always be swapped simultaneously.  The availableConnectionsIdledSequence_ list is "metadata" for the availableConnections_ list.
  // Access to an availableConnectionsIdledSequence_ list must only be done within a sync block for the associated availableConnections_ list.
  private final LinkedHashSet[] availableConnectionsIdledSequence_ = {new LinkedHashSet(), new LinkedHashSet(), null};



  // Connections that are no longer in use, which are staged to be removed from the pool.
  // Connections are added to this list when it is determined that they should be physically closed.
  // Connections are removed from this list when they are physically closed.
  // This list contains AS400JDBCPooledConnection objects.
  private final ArrayList condemnedConnections_ = new ArrayList(100);




  // Connection keys that have been invalidated via the invalidate() method.
  // This list contains JDConnectionPoolKey objects.
  private final HashSet invalidatedKeys_ = new HashSet();

  // The datasource being used for logging and tracing.
  private AS400JDBCManagedDataSource logger_;

  // The datasource that creates new connections, and specifies pool properties.
  private AS400JDBCManagedConnectionPoolDataSource cpds_;


  // Standard JDBC connection pool properties.  (See JDBC Tutorial p. 442, table 14.1)

  // For consistency, all time-related properties in this class are in units of milliseconds.
  // Note that all "PoolSize_" variables refer to the total number of connections ('active', 'available', and 'condemned'), not just available connections.

  private int initialPoolSize_;// The # of physical connections the pool should contain
                               // when it is created.
  private int minPoolSize_;    // The minimum # of physical connections in the pool.
  private int maxPoolSize_;    // The maximum # of physical connections the pool should contain.
                               // 0 (zero) indicates no maximum size.
  private long maxIdleTime_;   // The # of milliseconds that a physical connection should
                               // remain unused in the pool before it is closed; that is, when the
                               // connection is considered "stale".
                               // 0 (zero) indicates no time limit.
                               // See JDBC Tutorial, p. 643, 2nd paragraph.
  private long propertyCycle_; // The interval, in milliseconds, that the pool should wait before
                               // enforcing the policy defined by the values currently assigned
                               // to these connection pool properties.
  //          maxStatements_;  // The total # of statements the pool should keep open.
                               // 0 (zero) indicates that the caching of statements is disabled.
                               // Note: The Toolbox JDBC driver doesn't cache prepared statements.


  // Additional connection pool properties.

  private long maxLifetime_;   // The # of milliseconds that a physical connection should
                               // remain in the pool before it is closed; that is, when the
                               // connection is considered "expired".
                               // 0 (zero) indicates no time limit.

  private long maintainerInterval_;  // Max # of milliseconds between maintainer daemon cycles.
  private long reaperInterval_;      // Max # of milliseconds between reaper daemon cycles.
  private long scavengerInterval_;   // Max # of milliseconds between scavenger daemon cycles.

  private int condemnedListLengthThreshold_; // When the 'condemned' list reaches this length, we wake up the scavenger daemon to close all connections on 'condemned' list.
  private long minSwapInterval_;     // Min # of milliseconds between list swaps.


  // The total (cumulative) number of physical connections that have ever been created by this pool manager.
  private int numConnectionsCreated_;

  // The total (cumulative) number of physical connections that have ever been closed (and removed from the pool) by this pool manager.
  private int numConnectionsDestroyed_;

  // Note: At any given moment, the total number of connections in the pool is the difference of the above two values.


  private final Object connectionsCreatedLock_ = new Object();
  private final Object connectionsDestroyedLock_ = new Object();



  private boolean poolSizeLimited_;   // Total pool size (number of open connections) is limited.
  private boolean connectionLifetimeLimited_; // Connections have maximum lifetime; i.e. they can expire.
  private boolean reuseConnections_;        // Re-use connections that have been returned to pool.
  private int minDefaultStackSize_;         // Desired minimum size of 'available' stack for the default key.


  private JDPoolMaintainer poolMaintainer_; // Manages the 'available' lists.
  private Thread maintainerDaemon_;

  private JDPoolReaper poolReaper_;         // Removes expired connections from 'active' list.
  private Thread reaperDaemon_;             // Only started if connectionLifetimeLimited_ == true.

  private JDPoolScavenger poolScavenger_;   // Closes physical connections in 'condemned' list.
  private Thread scavengerDaemon_;

  private boolean keepDaemonsAlive_ = true;  // When this is false, the daemons shut down.
  private final Object maintainerSnoozeLock_ = new Object();
  private final Object maintainerSleepLock_ = new Object();
  private final Object reaperSleepLock_     = new Object();
  private final Object scavengerSleepLock_  = new Object();

  private boolean poolPaused_ = false;
  private final Object poolPauseLock_ = new Object();

  private boolean fillingPool_;  // gets set to 'true' while fillPool() is running
  private boolean poolClosed_;   // gets set to 'true' (and stays true) when closePool() starts
  private boolean poolClosedCompletely_;  // gets set to 'true' when closePool() completes
  private boolean needMoreConnections_;  // informs the maintainerDaemon_ that the foreground 'available' list is running low on default-key connections, and needs to be swapped-out

  // Time at which the foreground and background 'available' lists were last compared (and possibly swapped).
  private long timeLastSwapAttempted_ = 0L;
  private boolean swapInProgress_;
  private final Object swapLock_ = new Object();

  private boolean healthCheckInProgress_;
  private final Object healthCheckLock_ = new Object();

  // 'Sides' of the availableConnections_ list: "foreground" list and "background" list.
  private static final int FOREGROUND  = 0;  // specifies list accessed by the foreground process
  private static final int BACKGROUND  = 1;  // specifies list updated by the daemons
  private static final int HOLD        = 2;  // dummy value; used when swapping the above 2 lists

  private static final int SYNC_NONE       = 10;
  private static final int SYNC_ALL        = 11;

  // Used for debugging only:

  private int maintainerDaemonCycles_ = 0;
  private int reaperDaemonCycles_ = 0;
  private int scavengerDaemonCycles_ = 0;

  private int numGetConnectionCalls_received_ = 0;
  private int numGetConnectionCalls_succeeded_ = 0;
  private int numGetConnectionCalls_returnedNull_ = 0;
  private int numGetConnectionCalls_whileClosing_ = 0;

  private int connectionsReturnedToPool_ = 0;
  private int connectionErrorsOccurred_ = 0;

  private int staleConnectionsIdentified_ = 0;
  private int expiredConnectionsIdentifiedByReaper_ = 0;
  private int expiredConnectionsIdentifiedWhenReturned_ = 0;
  private int surplusPrecreatedConnectionsRemoved_ = 0;
  private int survivingConnectionsRemoved_ = 0;
  private int condemnedConnectionsRemoved_ = 0;

  private int swapsAttempted_ = 0;
  private int swapsSucceeded_ = 0;
  private int swapsFailed_ = 0;
  private int swapsFailed_notWorthIt_ = 0;
  private int swapsSucceeded_foreground_ = 0;
  private int swapsSucceeded_background_ = 0;
  private int swapsFailed_foreground_ = 0;
  private int swapsFailed_background_ = 0;
  private int swapsFailed_premature_ = 0;
  private int swapsFailed_foreground_inProgress_ = 0;
  private int swapsFailed_background_inProgress_ = 0;
  private int swapsFailed_foreground_daemonAwake_ = 0;

  /**
   **/
  JDConnectionPoolManager(AS400JDBCManagedDataSource logger, AS400JDBCManagedConnectionPoolDataSource cpds)
  {
    // We assume that the caller will never give us null args.
    logger_ = logger;
    cpds_ = cpds;

    initialPoolSize_  = cpds.getInitialPoolSize();
    minPoolSize_      = cpds.getMinPoolSize();
    maxPoolSize_      = cpds.getMaxPoolSize();
    reuseConnections_ = cpds.isReuseConnections();
    maxIdleTime_      = cpds.getMaxIdleTime()*1000;    // convert to milliseconds
    maxLifetime_      = cpds.getMaxLifetime()*1000;    // convert to milliseconds
    propertyCycle_    = cpds.getPropertyCycle()*1000;  // convert to milliseconds
    if (DEBUG)
    {
      logInformation("initialPoolSize_:  " + initialPoolSize_);
      logInformation("minPoolSize_:  " + minPoolSize_);
      logInformation("maxPoolSize_:  " + maxPoolSize_);
      logInformation("reuseConnections_:  " + reuseConnections_);
      logInformation("maxIdleTime_:  " + maxIdleTime_ + " msecs");
      logInformation("maxLifetime_:  " + maxLifetime_ + " msecs");
      logInformation("propertyCycle_:  " + propertyCycle_ + " msecs");
      logInformation("server name: |" + cpds_.getServerName() + "|");
      logInformation("default user: |" + cpds_.getUser() + "|");
    }
    if (GATHER_STATS)
    {
      System.out.println("initialPoolSize_:  " + initialPoolSize_);
      System.out.println("minPoolSize_:  " + minPoolSize_);
      System.out.println("maxPoolSize_:  " + maxPoolSize_);
      System.out.println("reuseConnections_:  " + reuseConnections_);
      System.out.println("maxIdleTime_:  " + maxIdleTime_ + " msecs");
      System.out.println("maxLifetime_:  " + maxLifetime_ + " msecs");
      System.out.println("propertyCycle_:  " + propertyCycle_ + " msecs");
      System.out.println("server name: |" + cpds_.getServerName() + "|");
      System.out.println("default user: |" + cpds_.getUser() + "|");
    }

    maintainerInterval_  = propertyCycle_;
    reaperInterval_      = 1800*1000;  // at least once every 30 minutes (reasonable value)
    scavengerInterval_   = maintainerInterval_ * 20;  // at least once every 20 maint cycles

    if (maxLifetime_ == 0) connectionLifetimeLimited_ = false;
    else {
      connectionLifetimeLimited_ = true;
      reaperInterval_ = maxLifetime_ / 3;  // reasonable value
    }

    minSwapInterval_     = 50;  // no more often than once every 50 msecs (reasonable value)

    if (maxPoolSize_ == 0) {
      poolSizeLimited_ = false;
    }
    else
    {
      poolSizeLimited_ = true;
      if (maxPoolSize_ < minPoolSize_)
      {
        if (JDTrace.isTraceOn())
          logWarning("minPoolSize ("+minPoolSize_+") exceeds maxPoolSize ("+maxPoolSize_+")");
        maxPoolSize_ = minPoolSize_ + 5;  // a reasonable value
      }

      // Handle the initPoolSize being out of range.
      if (initialPoolSize_ > maxPoolSize_)
      {
        if (JDTrace.isTraceOn()) {
          logWarning("initialPoolSize ("+initialPoolSize_+") exceeds maxPoolSize ("+maxPoolSize_+")");
        }
        initialPoolSize_ = minPoolSize_;
      }
    }

    // Note: Tolerate initialPoolSize_ < minPoolSize_.

    condemnedListLengthThreshold_ = Math.max(3, minPoolSize_/50);  // # of 'condemned' connections that triggers an immediate cleanup

    minDefaultStackSize_ = (reuseConnections_ ? 4 : 1);

    // Fill the pool with the number of connections specified for initial pool size.
    // We distribute the new connections evenly between the two 'sides'.
    fillPool(Math.max(1,initialPoolSize_/2), FOREGROUND);  // at least 1 connection
    fillPool(initialPoolSize_/2, BACKGROUND);

    // Create and start the maintainer daemon.
    poolMaintainer_ = new JDPoolMaintainer();
    maintainerDaemon_ = new Thread(poolMaintainer_, "PoolMaintainerDaemon");
    maintainerDaemon_.setDaemon(true);
    maintainerDaemon_.start();

    // Create and start the scavenger daemon.
    poolScavenger_ = new JDPoolScavenger();
    scavengerDaemon_ = new Thread(poolScavenger_, "PoolScavengerDaemon");
    scavengerDaemon_.setDaemon(true);
    scavengerDaemon_.start();

    // If connections have a maximum lifetime, create and start the reaper daemon.
    if (connectionLifetimeLimited_)
    {
      poolReaper_ = new JDPoolReaper();
      reaperDaemon_ = new Thread(poolReaper_, "PoolReaperDaemon");
      reaperDaemon_.setDaemon(true);
      reaperDaemon_.start();
    }

  }

  // Verifies that all the daemons are still running.
  private final boolean areDaemonsAlive()
  {
    boolean ok = true;
    if (!maintainerDaemon_.isAlive()) {
      ok = false;
      logError("The maintainerDaemon_ is no longer running");
    }
    if (!scavengerDaemon_.isAlive()) {
      ok = false;
      logError("The scavengerDaemon_ is no longer running");
    }
    if (connectionLifetimeLimited_ && !reaperDaemon_.isAlive()) {
      ok = false;
      logError("The reaperDaemon_ is no longer running");
    }
    return ok;
  }

  // Checks the health of the connection pool: That all the connection count is consistent with the list lengths, and that all the daemons are still running.
  // Future enhancement: Call this method periodically from one of the daemons.
  final boolean checkHealth(boolean logStatistics)
  {
    boolean ok = true;

    // First see if another thread is already running this method; and if so, just return true.
    boolean alreadyChecking = healthCheckInProgress_;
    if (!alreadyChecking) {
      synchronized (healthCheckLock_) {
        if (healthCheckInProgress_) alreadyChecking = true;
        else healthCheckInProgress_ = true;
      }
    }

    if (alreadyChecking) {
      if (DEBUG) logDiagnostic("checkHealth is already in progress");
      return true;
    }

    try
    {
      // First pause everything.
      pausePool();
      try {
        Thread.sleep(1500);  // Let everything reach a pause point.
      }
      catch (InterruptedException ie) {}  // ignore

      if (swapInProgress_)  // swaps ignore pause requests
      {
        try {
          Thread.sleep(20);  // Let the swap finish.
        }
        catch (InterruptedException ie) {}  // ignore
      }
      if (swapInProgress_ && DEBUG) {
        logError("checkHealth(): swapInProgress_ flag is still on after 50 msec wait.");
      }

      // Lock all the lists and counts.
      // Then check that everything looks reasonable and consistent.
      synchronized (activeConnections_)
      {
        synchronized (availableConnections_[FOREGROUND])
        {
          synchronized (availableConnections_[BACKGROUND])
          {
            synchronized (condemnedConnections_)
            {
              synchronized (connectionsCreatedLock_)
              {
                synchronized (connectionsDestroyedLock_)
                {
                  // Count all connections on the 'available' list.
                  int totalAvailConnCount = 0;
                  for (int side = FOREGROUND; side <= BACKGROUND; side++)
                  {
                    int availConnCount = 0;
                    // Get list of all connection keys in the 'available' HashMap.
                    JDConnectionPoolKey[] poolKeys = (JDConnectionPoolKey[])availableConnections_[side].keySet().toArray(new JDConnectionPoolKey[0]);
                    for (int i=0; i<poolKeys.length; i++)
                    {
                      // Count the connections for the specified key.
                      Stack connStack = (Stack)availableConnections_[side].get(poolKeys[i]);
                      if (connStack != null) {
                        availConnCount += connStack.size();
                      }
                    }
                    totalAvailConnCount += availConnCount;

                    // Verify that the associated 'idledSequence' has same number of connections.
                    if (availableConnectionsIdledSequence_[side].size() != availConnCount)
                    {
                      ok = false;
                      logError("Connection count mismatch for side " + side +": #avail==" + availConnCount + " ; #idled==" + availableConnectionsIdledSequence_[side].size());
                    }

                    if (poolClosedCompletely_)
                    {
                      // Verify that all this 'available' connection list is empty.
                      if (availConnCount != 0) {
                        ok = false;
                        logError("Available connections in list " + side + " ==" + availConnCount + " after pool closed");
                      }
                    }

                    if (DEBUG || GATHER_STATS)
                    {
                      // Verify that the connections in the 'idledSequence' list are in order of when they were idled.
                      Iterator idledIter = availableConnectionsIdledSequence_[side].iterator();
                      long timeWhenPriorConnIdled = 0;
                      for (int i=0; idledIter.hasNext(); i++)
                      {
                        AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)idledIter.next();
                        if (conn.timeWhenPoolStatusLastModified_ < timeWhenPriorConnIdled)
                        {
                          ok = false;
                          logError("Idled connection sequence for side " + side + " is not arranged in order of idled-time");
                        }
                        timeWhenPriorConnIdled = conn.timeWhenPoolStatusLastModified_;
                      }
                    }

                  }  // for (side ...)

                  int totalConnCount = totalAvailConnCount + activeConnections_.size() + condemnedConnections_.size();
                  if (totalConnCount != numConnectionsCreated_ - numConnectionsDestroyed_)
                  {
                    // We may have interrupted the pool while a connection was in "limbo".
                    logWarning("totalConnCount==" + totalConnCount + "; numConnectionsCreated_=="+numConnectionsCreated_ + "; numConnectionsDestroyed_==" + numConnectionsDestroyed_ + " (difference: " + (numConnectionsCreated_ - numConnectionsDestroyed_) + ")");
                  }

                  if (poolClosedCompletely_)
                  {
                    // Verify that the 'available' connection lists are empty.
                    if (availableConnections_[FOREGROUND].size() != 0) {
                      ok = false;
                      logError("Available connection list (foreground) is not empty after pool closed");
                    }
                    if (availableConnections_[BACKGROUND].size() != 0) {
                      ok = false;
                      logError("Available connection list (background) is not empty after pool closed");
                    }
                    // Verify that the 'active' connection list is empty.
                    if (activeConnections_.size() != 0) {
                      ok = false;
                      logError("Active connection count is " + activeConnections_.size() + " after pool closed");
                    }
                    // Verify that the 'active(error)' connection list is empty.
                    if (activeConnectionsInError_.size() != 0) {
                      ok = false;
                      logError("Active(error) connection count is " + activeConnectionsInError_.size() + " after pool closed");
                    }
                    // Verify that the 'condemned' connection list is empty.
                    if (condemnedConnections_.size() != 0) {
                      ok = false;
                      logError("Condemned connection count is " + condemnedConnections_.size() + " after pool closed");
                    }
                    // Verify that the total connection count is zero.
                    if (numConnectionsCreated_ != numConnectionsDestroyed_) {
                      ok = false;
                      logError("numConnectionsCreated_ == " + numConnectionsCreated_ + ", numConnectionsDestroyed_ == " + numConnectionsDestroyed_ + " after pool closed");
                    }
                  }

                  if (DEBUG)
                  {
                    logInformation("Total available connections==" + totalAvailConnCount + "\n activeConnections_.size()=="+activeConnections_.size() + "\n condemnedConnections_.size()==" + condemnedConnections_.size());
                  }

                  if (DEBUG || GATHER_STATS)
                  {
                    // Verify that the connections in the 'active' list are in proper sequence.
                    Iterator activeIter = activeConnections_.iterator();
                    long timeWhenPriorConnWasCreated = 0L;
                    for (int i=0; activeIter.hasNext(); i++)
                    {
                      AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)activeIter.next();
                      if (conn.timeWhenCreated_ < timeWhenPriorConnWasCreated)
                      {
                        ok = false;
                        logError("Active connection sequence is not arranged in order of creation: conn.timeWhenCreated_=="+conn.timeWhenCreated_+", timeWhenPriorConnWasCreated=="+timeWhenPriorConnWasCreated);
                      }
                      timeWhenPriorConnWasCreated = conn.timeWhenCreated_;
                    }
                  }

                  if (DEBUG || logStatistics)
                  {
                    String msg =
                      "\nswapsAttempted_=="+swapsAttempted_+"\n"+
                      "swapsSucceeded_=="+swapsSucceeded_+"\n"+
                      "swapsFailed_=="+swapsFailed_+"\n"+
                      "swapsFailed_notWorthIt_=="+swapsFailed_notWorthIt_+"\n"+
                      "swapsSucceeded_foreground_=="+swapsSucceeded_foreground_+"\n"+
                      "swapsSucceeded_background_=="+swapsSucceeded_background_+"\n"+
                      "swapsFailed_foreground_=="+swapsFailed_foreground_+"\n"+
                      "swapsFailed_background_=="+swapsFailed_background_+"\n"+
                      "swapsFailed_premature_=="+swapsFailed_premature_+"\n"+
                      "swapsFailed_foreground_inProgress_=="+swapsFailed_foreground_inProgress_+"\n"+
                      "swapsFailed_background_inProgress_=="+swapsFailed_background_inProgress_+"\n"+
                      "swapsFailed_foreground_daemonAwake_=="+swapsFailed_foreground_daemonAwake_+"\n"+

                      "maintainerDaemonCycles_=="+maintainerDaemonCycles_+"\n"+
                      "reaperDaemonCycles_=="+reaperDaemonCycles_+"\n"+
                      "scavengerDaemonCycles_=="+scavengerDaemonCycles_+"\n"+

                      "numGetConnectionCalls_received_=="+numGetConnectionCalls_received_+"\n"+
                      "numGetConnectionCalls_succeeded_=="+numGetConnectionCalls_succeeded_+"\n"+
                      "numGetConnectionCalls_returnedNull_=="+numGetConnectionCalls_returnedNull_+"\n"+
                      "numGetConnectionCalls_whileClosing_=="+numGetConnectionCalls_whileClosing_+"\n"+

                      "connectionsReturnedToPool_=="+connectionsReturnedToPool_ +"\n"+
                      "connectionErrorsOccurred_=="+connectionErrorsOccurred_ +"\n"+

                      "staleConnectionsIdentified_: " + staleConnectionsIdentified_ +"\n"+
                      "expiredConnectionsIdentifiedByReaper_: " + expiredConnectionsIdentifiedByReaper_ +"\n"+
                      "expiredConnectionsIdentifiedWhenReturned_: " + expiredConnectionsIdentifiedWhenReturned_ +"\n"+
                      "surplusPrecreatedConnectionsRemoved_: " + surplusPrecreatedConnectionsRemoved_ +"\n"+
                      "survivingConnectionsRemoved_: " + survivingConnectionsRemoved_ +"\n"+
                      "condemnedConnectionsRemoved_: " + condemnedConnectionsRemoved_ +"\n"+

                      "numConnectionsCreated_: " + numConnectionsCreated_ +"\n"+
                      "numConnectionsDestroyed_: " + numConnectionsDestroyed_;

                    logInformation(msg);
                  }

                }
              }
            }
          }
        }
      }  // outermost 'synchronized' block

      if (!poolPaused_) {
        if (!fillingPool_) ok = false;
        logError("The poolPaused_ flag is off during checkHealth()");
      }
      if (swapInProgress_) {
        ok = false;
        logError("The swapInProgress_ flag is on during checkHealth()");
      }

      if (!poolClosed_)
      {
        // Verify that all the daemons are running.
        if (!areDaemonsAlive()) ok = false;

        // Verify that all the flags have reasonable values.
        if (!keepDaemonsAlive_) {
          ok = false;
          logError("The keepDaemonsAlive_ flag is off when the poolClosed_ flag is off");
        }
        if (poolClosedCompletely_) {
          ok = false;
          logError("The poolClosedCompletely_ flag is on when the poolClosed_ flag is off");
        }
      }
      else if (poolClosedCompletely_)
      {
        // Verify that all the daemons have stopped, now that pool is closed.
        if (maintainerDaemon_.isAlive()) {
          ok = false;
          logError("The maintainerDaemon_ is still running after closePool()");
        }
        if (scavengerDaemon_.isAlive()) {
          ok = false;
          logError("The scavengerDaemon_ is still running after closePool()");
        }
        if (reaperDaemon_.isAlive()) {
          ok = false;
          logError("The reaperDaemon_ is no still running after closePool()");
        }

        // Verify that all the flags have reasonable values, now that pool is closed.
        if (keepDaemonsAlive_) {
          ok = false;
          logError("The keepDaemonsAlive_ flag is on after pool has been closed");
        }
        if (fillingPool_) {
          ok = false;
          logError("The fillingPool_ flag is on after pool has been closed");
        }
        if (!poolClosed_) {
          ok = false;
          logError("The poolClosed_ flag is off after pool has been closed");
        }
        if (needMoreConnections_) {
          ok = false;
          logError("The needMoreConnections_ flag is on after pool has been closed");
        }
        if (poolMaintainer_.isAwake_) {
          ok = false;
          logError("The pool maintainer daemon's isAwake_ flag is still on after closePool()");
        }
        if (poolMaintainer_.snooze_) {
          ok = false;
          logError("The pool maintainer daemon's snooze_ flag is still on after closePool()");
        }

      } // poolClosedCompletely_
    }
    finally {
      healthCheckInProgress_ = false;
      // Unpause everything.
      unpausePool();
    }

    return ok;
  }


  /**
   Closes the physical connection that underlies the pooled connection.
   @param pooledConnection The pooled connection.
   **/
  private final void closePhysicalConnection(AS400JDBCPooledConnection pooledConnection)
  {
    // Note: Never pause or terminate early from this method.  It's called from within sync blocks.

    try {
      pooledConnection.close();
      // Note: This does _not_ fire a 'connectionClosed' event back at us.
      // Only AS400JDBCConnectionHandle fires the event.
    }
    catch (Exception e) {
      // Ignore - connection is being removed anyway.
      if (JDTrace.isTraceOn()) logException("Exception when closing physical connection", e);
    }
    catch (Throwable e) {
      // Ignore - connection is being removed anyway.
      if (JDTrace.isTraceOn()) logError(e.getMessage());
    }
    finally {
      synchronized (connectionsDestroyedLock_) {
        numConnectionsDestroyed_++;  // increment the 'connections destroyed' counter
      }
    }
  }

  // Note: The DataSource interfaces don't have a close() method.  This method only gets called by the finalize() method.
  /**
   Cleans up the pool. Closes all java.sql.Connection objects. Gracefully stops the
   background PoolMaintainer and PoolReaper daemons.
   **/
  final void closePool()
  {
    if (poolClosed_) return;
    logInformation("Closing connection pool");
    poolClosed_ = true;

    // Note: Never pause or exit early while executing this method.

    if (JDTrace.isTraceOn()) logInformation(ResourceBundleLoader.getText("AS400CP_SHUTDOWN"));

    try
    {
      needMoreConnections_ = false;
      poolPaused_ = false;  // just in case

      // Wake up the daemons so they can shut down.
      keepDaemonsAlive_ = false;
      poolMaintainer_.snooze_ = false;
      synchronized (maintainerSnoozeLock_) {
        poolMaintainer_.snooze_ = false;  // ensure that it stays off
        maintainerSnoozeLock_.notify();
      }
      wakeMaintainerDaemon();
      wakeScavengerDaemon();
      wakeReaperDaemon();
    }
    catch (Throwable e) {} // ignore

     // Give each daemon up to 10 seconds to shut down.
    try {
      maintainerDaemon_.join(10*1000);
    }
    catch (Throwable e) {} // ignore

    try {
      scavengerDaemon_.join(10*1000);
    }
    catch (Throwable e) {} // ignore

    if (poolReaper_ != null) {
      try {
        reaperDaemon_.join(10*1000);
      }
      catch (Throwable e) {} // ignore
    }

    // Unblock everybody who's blocked on a lock, so they'll notice that we're closing the pool and quit whatever they're doing.
    synchronized (activeConnections_) {
      activeConnections_.notifyAll();
    }
    synchronized (availableConnections_[FOREGROUND]) {
      availableConnections_[FOREGROUND].notifyAll();
    }
    synchronized (availableConnections_[BACKGROUND]) {
      availableConnections_[BACKGROUND].notifyAll();
    }
    synchronized (condemnedConnections_) {
      condemnedConnections_.notifyAll();
    }
    synchronized (connectionsCreatedLock_) {
      connectionsCreatedLock_.notifyAll();
    }
    synchronized (connectionsDestroyedLock_) {
      connectionsDestroyedLock_.notifyAll();
    }

    // The pool is being shut down, so we can (and should) go ahead and lock all lists.
    // By now the daemons should have all shut down.
    // We will assume that the caller is not in a hurry.
    synchronized (activeConnections_)
    {
      synchronized (availableConnections_[FOREGROUND])
      {
        synchronized (availableConnections_[BACKGROUND])
        {
          synchronized (condemnedConnections_)
          {
            if (DEBUG)
            {
              // Note: The easiest way to get total # of available connections is to get sizes of 'idled' lists.
              logInformation("Number available: " + availableConnectionsIdledSequence_[FOREGROUND].size()+availableConnectionsIdledSequence_[BACKGROUND].size());
              logInformation("Number active: " + activeConnections_.size());
              logInformation("Number active in error: " + activeConnectionsInError_.size());
              logInformation("Number condemned: " + condemnedConnections_.size());
              if (DEBUG) {
                logInformation("Internal connection counter: " + getConnectionCount(SYNC_ALL));
              }
            }

            Iterator connIter;

            // Close all connections on the 'available' list.
            try
            {
              for (int side = FOREGROUND; side <= BACKGROUND; side++)
              {
                // Get list of all connection keys in the 'available' HashMap.
                JDConnectionPoolKey[] poolKeys = (JDConnectionPoolKey[])availableConnections_[side].keySet().toArray(new JDConnectionPoolKey[0]);
                for (int i=0; i<poolKeys.length; i++)
                {
                  // Get the stack of connections for the specified key.
                  Stack connStack = (Stack)availableConnections_[side].get(poolKeys[i]);
                  if (connStack != null)
                  {
                    connIter = connStack.iterator();
                    while (connIter.hasNext())
                    {
                      AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)connIter.next();
                      closePhysicalConnection(conn);
                      if (GATHER_STATS) survivingConnectionsRemoved_++;
                    }
                    connStack.clear();
                  }
                }
                availableConnections_[side].clear();
              }
            }
            catch (Throwable e) {} // ignore

            // Clear the 'idled key sequence' lists.
            availableConnectionsIdledSequence_[FOREGROUND].clear();
            availableConnectionsIdledSequence_[BACKGROUND].clear();


            // Close all connections on the 'active' list.
            try
            {
              connIter = activeConnections_.iterator();
              while (connIter.hasNext())
              {
                AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)connIter.next();
                closePhysicalConnection(conn);
                if (GATHER_STATS) survivingConnectionsRemoved_++;
              }
              activeConnections_.clear();
              activeConnectionsInError_.clear();

            }
            catch (Throwable e) {} // ignore

            // Close all connections on the 'condemned' list.
            try
            {
              connIter = condemnedConnections_.iterator();
              while (connIter.hasNext())
              {
                AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)connIter.next();
                closePhysicalConnection(conn);
                if (GATHER_STATS) condemnedConnectionsRemoved_++;
              }
              condemnedConnections_.clear();
            }
            catch (Throwable e) {} // ignore
          }
        }
      }
    }  // outermost 'synchronized' block

    logInformation(ResourceBundleLoader.getText("AS400CP_SHUTDOWNCOMP"));
    poolClosedCompletely_ = true;
  }


  // method required by javax.sql.ConnectionEventListener
  /**
   Notifies this ConnectionEventListener that the application has called close() on its representation of a pooled connection.

   @param event An event object describing the source of the event.
   **/
  public void connectionClosed(javax.sql.ConnectionEvent event)
  {
    if (DEBUG || GATHER_STATS) connectionsReturnedToPool_++;
    if (poolClosed_) return;
    pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called

    // The ConnectionHandle object marked itself as closed, so
    // we should not have to worry about users doing things with
    // it anymore (no need to detach the Handle object or anything
    // like that at this point).
    AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection) event.getSource();
    if (JDTrace.isTraceOn()) { logInformation(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_RETCONN"), new String[] {cpds_.getServerName(), conn.getPoolKey().getUser()} ));
    }

    // Remove the connection from the 'active' list.
    boolean removed;
    synchronized (activeConnections_)
    {
      if (poolClosed_) return;
      removed = activeConnections_.remove(conn);
      if (conn.fatalConnectionErrorOccurred_) {
        activeConnectionsInError_.remove(conn);  // also remove from 'error' list
      }
    }

    if (removed) // The connection was found on the 'active' list.
    {
      long timeNow = System.currentTimeMillis();
      if (reuseConnections_ &&                       // we're reusing connections;
          !conn.fatalConnectionErrorOccurred_ &&     // no fatal errors on this connection;
          !isExpired(conn, timeNow - maxLifetime_))  // this connection is not expired
      {  // Prepare the connection for re-use.

        // Tell the pooled connection object to make itself ready for reuse.
        conn.returned();

        // Put the connection back on the 'available' list.
        JDConnectionPoolKey poolKey = conn.getPoolKey();
        {
          synchronized (availableConnections_[FOREGROUND])
          {  // Don't leave the connection in limbo.
            Stack connStack = (Stack)availableConnections_[FOREGROUND].get(poolKey);
            if (connStack == null)
            {
              // This could happen if the connection was allocated from the other 'avail' list and the lists have since been swapped.
              connStack = new Stack();
              availableConnections_[FOREGROUND].put(poolKey, connStack);  // add new stack
            }
            connStack.push(conn);
            availableConnectionsIdledSequence_[FOREGROUND].add(conn);
            conn.timeWhenPoolStatusLastModified_ = timeNow;
          }
        }
      }
      else  // Just discard (and close) the returned connection.
      {
        if (DEBUG || GATHER_STATS) {
          if (isExpired(conn, timeNow - maxLifetime_)) {
            expiredConnectionsIdentifiedWhenReturned_++;
          }
        }
        synchronized (condemnedConnections_)
        {  // Don't leave the connection in limbo.
          condemnedConnections_.add(conn);
        }

        // Let the scavenger daemon close the condemned physical connection.
      }
    }
    else  // Connection not found on 'active' list.
    {
      // This could happen if the reaper daemon decided the connection was "expired", and has already removed it from the 'active' list.
      if (JDTrace.isTraceOn()) logDiagnostic("connectionClosed(): The returned connection was not found on the 'active connections' list: " + conn.toString());
    }

  }


  // Note: Since we preconnect all connections (to the database service), then even if the password is changed underneath us, those connections are still usable.  Only if connection needs to use an additional service (besided the database service) will it encounter an "incorrect password" exception.



  // method required by javax.sql.ConnectionEventListener
  /**
   Notifies this ConnectionEventListener that a fatal error has occurred and the pooled connection can no longer be used.  The driver makes this notification just before it throws the application the SQLException contained in the given ConnectionEvent object.

   @param event An event object describing the source of the event and containing the SQLException that the driver is about to throw.
   **/
  public void connectionErrorOccurred(javax.sql.ConnectionEvent event)
  {
    logException("connectionErrorOccurred", event.getSQLException());
    if (DEBUG || GATHER_STATS) connectionErrorsOccurred_++;
    // Note: (From the JDBC Tutorial, bottom of p. 640:) "... the driver will notify the connection pooling module when the Connection object ... is unusable because of a fatal error".

    // This event does _not_ cause the pool manager to remove the connection from the pool, or to close the physical connection.  The manager will simply make note that this connection is not to be re-used, if/when it gets returned to the pool.

    if (poolClosed_) return;
    pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called

    AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection) event.getSource();

    activeConnectionsInError_.add(conn);  // track this connection until it gets closed
  }


  /**
   Attempts to create one new physical connection with the specified key, and add it to the foreground 'available' list.
   **/
  private final void createNewConnection(JDConnectionPoolKey poolKey, boolean keyIsDefault, String password)
  {
    if (DEBUG) logInformation("createNewConnection("+poolKey.getUser()+")");
    if (poolClosed_) return;
    // Note: Never pause during this method.  It's called from within a sync block.

    // Fail fast if pool is already full.
    if (isPoolFull())
    {
      if (DEBUG) logWarning("Connection pool is full, so no new connection was added.");
      return;
    }

    // Prepare to add a new connection to the pool.  Pre-create a new connection, outside of the sync block.
    if (JDTrace.isTraceOn()) logInformation(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), new String[] { (new Integer(1)).toString(), 
    cpds_.getServerName(), poolKey.getUser()} ));
    AS400JDBCPooledConnection newConn = null;
    boolean addedConnectionToPool = false;
    try
    {
      if (keyIsDefault) {
        newConn = (AS400JDBCPooledConnection)cpds_.getPooledConnection();
      }
      else {
        newConn = (AS400JDBCPooledConnection)cpds_.getPooledConnection(poolKey.getUser(), password);
      }
      synchronized (connectionsCreatedLock_) { // increment the 'connections created' counter
        numConnectionsCreated_++;
      }
      newConn.addConnectionEventListener(this);
      newConn.setPoolKey(poolKey);

      synchronized (availableConnections_[FOREGROUND])
      { // Don't leave the newly-created connection in limbo.
        // Get the stack of connections for the specified key.
        Stack connStack = (Stack)availableConnections_[FOREGROUND].get(poolKey);
        if (connStack == null) { // This key doesn't have a stack yet.
          connStack = new Stack();
          availableConnections_[FOREGROUND].put(poolKey, connStack);  // Add new stack to the list.
        }

        // Now that we've got locks, check the totals again.
        // Note that the newly-created connection has been added to the connection totals.
        if (!isPoolOverFull())
        {
          connStack.push(newConn);
          availableConnectionsIdledSequence_[FOREGROUND].add(newConn);
          addedConnectionToPool = true;
          newConn.timeWhenPoolStatusLastModified_ = System.currentTimeMillis();
        }
        else {
          if (DEBUG) logWarning("Connection pool is full, so new connection not added");
        }
      }
    }
    catch (SQLException e)
    {
      wakeMaintainerDaemon();
      logException(ResourceBundleLoader.getText("AS400CP_FILLEXC"), e);
    }
    finally
    {
      // If the pre-created connection wasn't added to pool, close it.
      if (!addedConnectionToPool)
      {
        if (newConn != null) {
          if (DEBUG) logDiagnostic("Closing pre-created connection");
          closePhysicalConnection(newConn);
          if (GATHER_STATS) surplusPrecreatedConnectionsRemoved_++;
        }
      }
    }
  }


  // Note: This method is only called by the constructor and by the maintainer daemon, therefore it will never be called simultaneously by two threads, and therefore doesn't need to be synchronized.
  /**
   Creates the specified number of connections with the default key, and adds them to the 'available' list.
   @param numConnectionsToAdd The number of new connections being requested.
   @param side The side that is making the request: Either FOREGROUND or BACKGROUND.
   **/
  private final void fillPool(int numConnectionsToAdd, int side)
  {
    // Design note: Never synchronize this method or call it from within a synchronized block or method, or you will likely get deadlocks when checkHealth() runs, since this method calls pauseIfPoolPaused().

    if (poolClosed_) return;

    if (DEBUG) logDiagnostic("fillPool("+numConnectionsToAdd+","+side+")");

    // Fail fast if pool is already full.
    if (isPoolFull())
    {
      if (DEBUG) logDiagnostic("fillPool() is returning because pool is already full");
      return;
    }

    // See if opening the number of connections specified, will create more connections than the maximum pool size.
    // Note: "Maximum pool size" means the total number of connections open (that is, avail + active + condemned + limbo).
    // See JDBC Tutorial p. 442, table 14.1 "Standard Connection Pool Properties".
    if (poolSizeLimited_ && (numConnectionsToAdd + getConnectionCount(SYNC_NONE) > maxPoolSize_))
    {
      if (DEBUG)
        logDiagnostic("The requested number of connections ("+numConnectionsToAdd+") would cause the connection pool to exceed its maximum size");
      numConnectionsToAdd = maxPoolSize_ - getConnectionCount(SYNC_NONE);
    }

    if (numConnectionsToAdd < 1) {
      if (DEBUG) logDiagnostic("fillPool() is returning because pool is already full");
      return;
    }

    // Prepare to add connections to the pool.  Pre-create some new connections, outside of the sync block.

    JDConnectionPoolKey poolKey = cpds_.getConnectionPoolKey();  // default key
    AS400JDBCPooledConnection[] newConnections = null;
    int numberOfConnsCreatedForThisRequest = 0;
    int numConnectionsAddedToPool = 0;

    // Implementation note: The creation of new physical connections can be a slow process.
    // We don't want checkHealth() to run while we're filling the pool, since the connection counts may temporarily appear to be inconsistent.
    pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called
    fillingPool_ = true;
    try
    {
      newConnections = new AS400JDBCPooledConnection[numConnectionsToAdd];
      try
      {
        for (int i=0; i<numConnectionsToAdd; i++)
        {
          AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)cpds_.getPooledConnection();
          numberOfConnsCreatedForThisRequest++;
          conn.addConnectionEventListener(this);
          conn.setPoolKey(poolKey);
          newConnections[i] = conn;
        }
      }
      finally
      {
        if (numberOfConnsCreatedForThisRequest != 0) {
          synchronized (connectionsCreatedLock_) { // increment the 'connections created' counter
            numConnectionsCreated_ += numberOfConnsCreatedForThisRequest;
          }
        }
      }

      if (JDTrace.isTraceOn()) logInformation(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), new String[] { (new Integer(numConnectionsToAdd)).toString(), 
      cpds_.getServerName(), poolKey.getUser()} ));

      synchronized (availableConnections_[side])
      { // Don't leave the newly-created connections in limbo.
        // First get the stack of connections for the specified key.
        Stack connStack = (Stack)availableConnections_[side].get(poolKey);
        if (connStack == null) { // This key doesn't have a stack yet.
          connStack = new Stack();
          availableConnections_[side].put(poolKey, connStack);  // Add new stack to the list.
        }

        // Now that we've got locks, check the totals again.
        // If we've exceeded max pool size, add only some of the newly-created connections to the pool.
        // (Note that the connectionCount now includes the newly-created connections.)
        if (isPoolOverFull())
        {
          // We created too many new connections.  Decrement the number of connections to add, by the amount we've exceeded maxPoolSize_.
          // If we haven't exceeded, then no need to decrement.
          numConnectionsToAdd -= Math.max(0, getConnectionCount(SYNC_ALL) - maxPoolSize_);
        }

        long timeNow = System.currentTimeMillis();
        for (int i=0; i<numConnectionsToAdd; i++)
        {
          connStack.push(newConnections[i]);
          availableConnectionsIdledSequence_[side].add(newConnections[i]);
          numConnectionsAddedToPool++;
          newConnections[i].timeWhenPoolStatusLastModified_ = timeNow;
        }
      }
    }
    catch (SQLException e)
    {
      // If exception occurs, stop creating connections, wake up maintainer daemon,
      // and throw whatever exception was received on creation to user.
      wakeMaintainerDaemon();
      logException(ResourceBundleLoader.getText("AS400CP_FILLEXC"), e);
    }
    finally
    {
      // Close any surplus pre-created connections.
      if (numberOfConnsCreatedForThisRequest > numConnectionsAddedToPool)
      {
        if (DEBUG) {
          logDiagnostic("newConnections.length=="+newConnections.length+"; numConnectionsAddedToPool=="+numConnectionsAddedToPool+"; need to close " + (numberOfConnsCreatedForThisRequest - numConnectionsAddedToPool) + " connections");
        }

        for (int i=numConnectionsAddedToPool; i<numberOfConnsCreatedForThisRequest && i<newConnections.length; i++)
        {
          if (DEBUG) logDiagnostic("Closing surplus pre-created connection["+i+"]");
          closePhysicalConnection(newConnections[i]);  // Note: This increments the 'destroyed' count.
          if (GATHER_STATS) surplusPrecreatedConnectionsRemoved_++;
        }
      }
      fillingPool_ = false;  // give checkHealth() the all-clear to validate connection counts
    }
  }


  /**
   Closes the connection pool if not explicitly closed by the caller.
   @throws Throwable If an error occurs.
   **/
  protected void finalize() throws Throwable
  {
    if (DEBUG) logInformation("finalize");
    closePool();
  }


  // Design Note: WebAccess's DatabaseConnectionPool.getConnection() takes: (driverName,driverURL,userName,password).
  // Native's UDBConnectionPoolManager.getConnection() takes (java.util.Properties), but it's only actually interested in the following properties: (user,password,databaseName)


  // Utility method.
  private final int getConnectionCount(int howToSync)
  {
    // Note: Never pause or terminate early from this method.
    switch (howToSync)
    {
      case SYNC_NONE:
        return numConnectionsCreated_ - numConnectionsDestroyed_;
      case SYNC_ALL:
        synchronized (connectionsCreatedLock_)
          {
            synchronized (connectionsDestroyedLock_)
            {
              return numConnectionsCreated_ - numConnectionsDestroyed_;
            }
          }
      default:
        logError("Internal error: JDConnectionPoolManager.getConnectionCount("+howToSync+")");
        return numConnectionsCreated_ - numConnectionsDestroyed_;
    }
  }


  /**
   Returns (a handle to) a connection with properties matching those specified in the parameters.
   May return null if the pool is at or near capacity (maximum number of connections).

   @param poolKey The connection pool key.  'null' indicates that the default key is to be used.
   @param password The password.  Ignored if poolKey is null.
   @return The connection, or null if the pool is at or near capacity.
   **/
  final AS400JDBCConnectionHandle getConnection(JDConnectionPoolKey poolKey, String password)
  {
    if (DEBUG || GATHER_STATS) numGetConnectionCalls_received_++;

    if (poolClosed_) {
      if (DEBUG || GATHER_STATS) numGetConnectionCalls_whileClosing_++;
      return null;
    }
    pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called

    int numAvailableForKey = 0;  // number of available connections in foreground list for the specified key
    boolean triedToSwap = false;
    boolean triedToCreateNewConnection = false;
    boolean keyIsDefault;

    if (poolKey == null) {
      poolKey = cpds_.getConnectionPoolKey();  // use current default key
      keyIsDefault = true;
    }
    else if (poolKey.equals(cpds_.getConnectionPoolKey())) {
      keyIsDefault = true;
    }
    else keyIsDefault = false;

    AS400JDBCPooledConnection conn = null;
    boolean needMoreDefaultConnections = false;

    // Make at most 3 tries to get a connection.
    // If we're lucky there's a connection available and we'll only need 1 pass to get a connection.
    // If not, the 2nd pass is after swap attempt.
    // If still no luck, then the third pass is after we've attempted to add a new physical connection to the pool.
    synchronized (availableConnections_[FOREGROUND])
    {
      if (poolClosed_) {
        if (DEBUG || GATHER_STATS) numGetConnectionCalls_whileClosing_++;
        return null;
      }
      for (int i=0; conn == null && i<3; i++)
      {
        // See if there's an available connection.
        Stack connStack = (Stack)availableConnections_[FOREGROUND].get(poolKey);
        if (connStack != null && !connStack.empty())
        {
          // Retrieve the most recently used connection.
          conn = (AS400JDBCPooledConnection)connStack.pop();
          // Also remove the connection from the associated 'idled sequence' list.
          // For performance, search the idled sequence from the end (most-recently inserted).
          availableConnectionsIdledSequence_[FOREGROUND].remove(conn);  // connection is no longer idle

          // While we've got the list locked, see if we've taken the last available connection.
          if (keyIsDefault && connStack.empty()) needMoreDefaultConnections = true;
        }

        if (conn == null)
        {
          if (!triedToSwap)  // We haven't tried swapping lists yet.
          {
            // Try swapping the foreground and background 'available' lists.
            // Note that the swap may fail if maintainer daemon is running, or if background list is not longer than foreground list.
            if (DEBUG) logInformation("getConnection() is requesting a swap");
            swapConnectionLists(FOREGROUND, poolKey);
            triedToSwap = true;
          }
          else if (!triedToCreateNewConnection && !isPoolFull()) // The swap attempt didn't help.  Go ahead a try creating a new connection now.
          {
            createNewConnection(poolKey, keyIsDefault, password);  // create new physical connection and add it to pool
            triedToCreateNewConnection = true;
          }
        }
      }  // 'for' loop
    }
    
    AS400JDBCConnectionHandle handle = null;
    if (conn != null)
    {
      try {
        handle = conn.getConnectionHandle();  // returns an instance of AS400JDBCConnectionHandle
      }
      catch (SQLException e)
      {
        logException(ResourceBundleLoader.getText("AS400CP_FILLEXC"), e);
        // This pooled connection is bad.  Add it to the 'condemned' list.
        synchronized (condemnedConnections_)
        {
          condemnedConnections_.add(conn);
        }
        conn = null;
      }
    }

    if (conn != null)
    {
      if (DEBUG || GATHER_STATS) numGetConnectionCalls_succeeded_++;
      // Add the connection to the 'active' list.
      synchronized (activeConnections_)
      {  // Don't pause while connection is in limbo.
        activeConnections_.add(conn);
        conn.timeWhenPoolStatusLastModified_ = System.currentTimeMillis();
      }
    }

    if (conn == null)
    {
      if (keyIsDefault) needMoreDefaultConnections = true;
      if (GATHER_STATS) System.out.print("(("+poolKey.getUser() + "/" +password+"))");
      if (DEBUG || GATHER_STATS) numGetConnectionCalls_returnedNull_++;
      if (DEBUG)
      {
        Stack foregroundStack = (Stack)availableConnections_[FOREGROUND].get(poolKey);
        Stack backgroundStack = (Stack)availableConnections_[BACKGROUND].get(poolKey);
        logWarning("PoolManager returning null for  |"+poolKey+"|. " + getConnectionCount(SYNC_ALL) + "/" + maxPoolSize_ + "/ " +
                           (foregroundStack == null ? "null" : Integer.toString(foregroundStack.size())) + "," +
                           (backgroundStack == null ? "null" : Integer.toString(backgroundStack.size())));
      }
    }

    // See if we need to wake up the maintainer daemon to replenish and/or cleanup the pool.
    if (!needMoreConnections_ &&
        (needMoreDefaultConnections || isPoolFull() || getConnectionCount(SYNC_NONE) < minPoolSize_))
    {
      needMoreConnections_ = true;
      wakeMaintainerDaemon();
    }

    return handle;
  }


  /**
   Invalidates all pooled connections for the specified connection pool key.
   All current available (unallocated) connections for this key are closed.
   As active connections with this key are returned to the pool, they are closed.
   @param poolKey The connection pool key.
   **/
  final void invalidate(JDConnectionPoolKey poolKey)
  {
    if (poolClosed_) return;

    synchronized (invalidatedKeys_) {
      invalidatedKeys_.add(poolKey);
    }
  }

  private final boolean isExpired(AS400JDBCPooledConnection conn, long cutoffTime)
  {
    if (conn.timeWhenCreated_ < cutoffTime) {
      return true;
    }
    else return false;
  }

  private final boolean isStale(AS400JDBCPooledConnection conn, long cutoffTime)
  {
    if (conn.timeWhenPoolStatusLastModified_ < cutoffTime) {
      return true;
    }
    else return false;
  }

  // Returns true if pool contains maxPoolSize_ connections (or more).
  private final boolean isPoolFull()
  {
    if (poolSizeLimited_ && getConnectionCount(SYNC_NONE) >= maxPoolSize_) {
      if (DEBUG || GATHER_STATS) {
        System.out.println("\nPOOL IS FULL: " +
          100*getConnectionCount(SYNC_NONE)/maxPoolSize_ + "% ALLOCATED");
      }
      return true;
    }
    else return false;
  }

  // Returns true if pool contains more than maxPoolSize_ connections.
  private final boolean isPoolOverFull()
  {
    if (poolSizeLimited_ && (getConnectionCount(SYNC_NONE) > maxPoolSize_)) {
      return true;
    }
    else return false;
  }

  // Logs a diagnostic message.
  private final void logDiagnostic(String text)
  {
    logger_.logDiagnostic(text);
  }

  // Logs an error message.
  private final void logError(String text)
  {
    logger_.logError(text);
  }

  // Logs an exception.
  private final void logException(String text, Exception e)
  {
    logger_.logException(text, e);
  }

  // Logs an informational message.
  private final void logInformation(String text)
  {
    logger_.logInformation(text);
  }

  // Logs a warning message.
  private final void logWarning(String text)
  {
    logger_.logWarning(text);
  }


  // Pause if the pool's "pause" flag is up.
  // Note: Never call this method while inside a sync block, otherwise you're likely to hang.
  private final void pauseIfPoolPaused(long maxTimeToPause)
  {
    while (poolPaused_) {
      if (DEBUG) logDiagnostic("Pool is paused");
      try {
        synchronized (poolPauseLock_) {
          if (poolPaused_) poolPauseLock_.wait(maxTimeToPause);
        }
      }
      catch (InterruptedException ie) {}  // ignore
    }
  }


 // Pauses all threads manipulating the connection lists.
 // Caution: If the pool is paused but not subsequently unpaused, all threads using the pool will hang, and the PoolReaper would eventually clean up connections in the pool as their lifetimes expire.
 // <br>Note: Never call this method while inside a sync block, otherwise a hang is likely to result.
  private final void pausePool()
  {
    poolPaused_ = true;
    synchronized (poolPauseLock_) {
      poolPaused_ = true;  // ensure that it ends up 'on'
    }
  }


  // A quick-and-dirty precheck to see if the background 'available' list is longer than the foreground list.
  private final boolean isBackgroundListLongerThanForegroundList(JDConnectionPoolKey poolKey)
  {
    // It only makes sense to swap lists if there are more available connections (for desired key) in background list than in foreground list.

    // Design note: For performance, we do _not_ necessarily synchronize access to lists here.
    // If running unsynchronized, we may occasionally get a false positive or negative (for example if the lists get modified or swapped underneath us), but that won't cause any real damage.

    Stack backgroundStack = (Stack)availableConnections_[BACKGROUND].get(poolKey);
    if (backgroundStack != null && backgroundStack.size() != 0)
    {
      Stack foregroundStack = (Stack)availableConnections_[FOREGROUND].get(poolKey);
      if (foregroundStack == null || (foregroundStack.size() < backgroundStack.size()))
      {
        return true;
      }
    }
    return false;
  }

  // Utility method.  Assumes that the caller has locked the lists.
  private final void swapAvailLists()
  {
    // Swap the foreground and background 'available' lists.
    availableConnections_[HOLD] = availableConnections_[FOREGROUND];
    availableConnections_[FOREGROUND] = availableConnections_[BACKGROUND];
    availableConnections_[BACKGROUND] = availableConnections_[HOLD];
    availableConnections_[HOLD] = null;

    // Swap the foreground and background 'idled sequence' lists.
    availableConnectionsIdledSequence_[HOLD] = availableConnectionsIdledSequence_[FOREGROUND];
    availableConnectionsIdledSequence_[FOREGROUND] = availableConnectionsIdledSequence_[BACKGROUND];
    availableConnectionsIdledSequence_[BACKGROUND] = availableConnectionsIdledSequence_[HOLD];
    availableConnectionsIdledSequence_[HOLD] = null;
  }


  // Utility method.
  // Swaps the foreground and background 'available' connection lists.
  // Returns true if the swap succeeded; false if the swap wasn't performed.
  // The swap may fail, for example, if:
  // (1) a swap is already in progress; or
  // (2) the background list (for specified key) isn't longer than the foreground list; or
  // (3) the request is from the foreground process, and the poolMaintainer daemon currently has the background list locked; or
  // (4) the previous swap happened too recently.
  // If the swap fails, this method exits and returns false.
  // If the foreground process is requesting the swap, we ensure that the swap either succeeds quickly or fails quickly.
  private final boolean swapConnectionLists(int sideRequestingSwap, JDConnectionPoolKey poolKey)
  {
    if (GATHER_STATS) swapsAttempted_++;
    boolean swapped = false;
    boolean alreadySwapping = swapInProgress_;
    try
    {
      if (!alreadySwapping)
      {
        // If we recently performed a swap attempt, reject this request.
        if (System.currentTimeMillis() - timeLastSwapAttempted_ < minSwapInterval_) {
          if (DEBUG || GATHER_STATS) swapsFailed_premature_++;
          if (DEBUG) logDiagnostic("Premature swap request rejected");
          return false;  // Bail out now.
        }

        // Precheck the foreground/background lists, to see if it's worth even trying to swap.
        if (!isBackgroundListLongerThanForegroundList(poolKey))
        {
          if (DEBUG) logDiagnostic("Swap request rejected because background list is not longer than foreground list");
          if (GATHER_STATS) swapsFailed_notWorthIt_++;
          return false;  // Bail out now.
        }

        synchronized (swapLock_) {  // Grab lock, and check again.
          if (swapInProgress_) alreadySwapping = true;
          else swapInProgress_ = true;
        }
      }

      if (alreadySwapping)
      {
        if (DEBUG) logDiagnostic("Swap request rejected because swap already in progress");
        if (GATHER_STATS) {
          if (sideRequestingSwap == FOREGROUND) swapsFailed_foreground_inProgress_++;
          else swapsFailed_background_inProgress_++;
        }
        return false;  // Bail out now.
      }

      // If we got this far, we've determined that a swap isn't already in progress,
      // and we've turned on the swapInProgress_ flag ourselves.
      try
      {
        // Note: Never pause during a list swap.

        switch (sideRequestingSwap)
        {
          case BACKGROUND: // The maintainer daemon is requesting the swap.
            { // Just grab the necessary locks and do the swap.
              synchronized (availableConnections_[FOREGROUND])
              {
                if (poolClosed_) return false;
                synchronized (availableConnections_[BACKGROUND])
                {
                  // If we got this far, proceed with the swap (don't check poolClosed_).
                  timeLastSwapAttempted_ = System.currentTimeMillis();

                  // It only makes sense to swap if there are more available connections (for desired key) in background list than in foreground list.
                  if (isBackgroundListLongerThanForegroundList(poolKey))
                  {
                    swapAvailLists();

                    swapped = true;
                    if (DEBUG || GATHER_STATS) {
                      swapsSucceeded_background_++;
                    }
                  }
                  else {  // Background list isn't longer than foreground list.
                    if (DEBUG || GATHER_STATS) {
                      swapsFailed_background_++;
                    }
                    if (DEBUG) logDiagnostic("Swap requested by daemon rejected because background list is not longer than foreground list");
                  }
                }
              }  // outermost sync block
            }
            break;

          case FOREGROUND: // The foreground process is requesting the swap.
            {
              // Fail-fast if maintainer daemon is currently awake, so we don't wait for locks.
              if (poolMaintainer_.isAwake_)
              {
                if (DEBUG || GATHER_STATS) swapsFailed_foreground_daemonAwake_++;
                if (DEBUG) logDiagnostic("Foreground swap request rejected because daemon is awake");
                return false;
              }

              synchronized (maintainerSnoozeLock_) {
                poolMaintainer_.snooze_ = true;  // Instruct daemon to go dormant if it wakes up.
                // We must prevent the maintainer thread from waking up, locking the background list, then attempting a swap, since that could cause a deadlock.
              }
              try
              {
                if (poolMaintainer_.isAwake_) {
                  if (DEBUG || GATHER_STATS) swapsFailed_foreground_daemonAwake_++;
                  if (DEBUG) logDiagnostic("Foreground swap request rejected because daemon is awake");
                  return false;
                }
                // Daemon is still sleeping, so it's safe to grab locks.
                synchronized (availableConnections_[FOREGROUND])
                {
                  if (poolClosed_) return false;
                  synchronized (availableConnections_[BACKGROUND])
                  {
                    // If we got this far, go ahead and do the swap (don't check poolClosed_).
                    timeLastSwapAttempted_ = System.currentTimeMillis();

                    // It only makes sense to swap if there are more available connections (for desired key) in background list than in foreground list.
                    if (isBackgroundListLongerThanForegroundList(poolKey))
                    {
                      swapAvailLists();

                      swapped = true;
                      if (DEBUG || GATHER_STATS) swapsSucceeded_foreground_++;
                    }
                    else {  // Background list isn't longer than foreground list.
                      if (DEBUG || GATHER_STATS) swapsFailed_foreground_++;
                      if (DEBUG) logDiagnostic("Swap request rejected because background list is not longer than foreground list");
                    }
                  }
                }  // outermost sync block
              }
              finally
              {  // Tell the maintainer daemon to stop snoozing.
                poolMaintainer_.snooze_ = false;
                synchronized (maintainerSnoozeLock_) {
                  poolMaintainer_.snooze_ = false;  // just to be sure
                  maintainerSnoozeLock_.notify();
                }
              }
            }
            break;

          default:  // neither FOREGROUND nor BACKGROUND
            {
              // Internal design error.
              // Rather than crashing, attempt a fail-fast swap.
              logError("Internal error: JDConnectionPoolManager.swapConnectionLists("+sideRequestingSwap+")" + sideRequestingSwap);
              swapConnectionLists(FOREGROUND, poolKey);
            }
            break;

        }  // switch
      }  // inner try
      finally
      {
        if (swapped) {
          needMoreConnections_ = false;
        }
        swapInProgress_ = false;  // we turned the flag on, so now we need to turn it off
      }
    }  // outer try
    finally
    {
      if (GATHER_STATS) {
        if (swapped) swapsSucceeded_++;
        else swapsFailed_++;
      }
    }

    return swapped;
  }



  /**
   Unpauses all threads manipulating the connection lists.
   Cancels the effects of a prior call to pausePool().
   @see #pausePool()
   **/
  private final void unpausePool()
  {
    poolPaused_ = false;
    synchronized (poolPauseLock_) {
      poolPaused_ = false;  // ensure that it stays off
      poolPauseLock_.notifyAll();
    }
  }

  private final void wakeMaintainerDaemon()
  {
    synchronized (maintainerSleepLock_) {
      maintainerSleepLock_.notify();
    }
  }

  private final void wakeReaperDaemon()
  {
    synchronized (reaperSleepLock_) {
      reaperSleepLock_.notify();
    }
  }

  private final void wakeScavengerDaemon()
  {
    synchronized (scavengerSleepLock_) {
      scavengerSleepLock_.notify();
    }
  }




  // JDAgeComparator --------------------------------------------------------------------

  /**
   Helper class.  This comparator compares two AS400JDBCPooledConnection objects based on their
   age (creation dates).
   **/
  private final class JDAgeComparator implements java.util.Comparator
  {
    /**
     Compares two AS400JDBCPooledConnection objects, based on their creation dates.
     @return -1, 0, or +1 if the first argument is less than, equal to, or greater than the second.
     @throws IllegalArgumentException if the arguments aren't instances of AS400JDBCPooledConnection.
     **/
    public int compare(Object o1,
                       Object o2)
    {
      try
      {
        long creationTime1 = ((AS400JDBCPooledConnection)o1).timeWhenCreated_;
        long creationTime2 = ((AS400JDBCPooledConnection)o2).timeWhenCreated_;
        if (creationTime1 < creationTime2) return -1;
        else if (creationTime1 > creationTime2) return 1;
        else // the two objects were created at the same time
        {
          // We must not report "equal" if they are different objects, otherwise the TreeSet will overlay one of them with the other.
          if (o1.hashCode() < o2.hashCode()) return -1;
          else if (o1.hashCode() > o2.hashCode()) return 1;
          else return 0;  // If hashcodes match, and created at same time, assume the objects are identical.
        }
      }
      catch (ClassCastException e) {
        logException("Exception when comparing connections", e);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
    }
  }  // internal class JDAgeComparator



  // JDPoolMaintainer --------------------------------------------------------------------

  /**
   Helper class. This daemon wakes up every maintainerInterval_ msecs and performs maintainenance on the current backup 'available connection' list.  It performs a list swap if needed.
   It also identifies any stale connections in the avail list and moves them the 'condemned' list.
   **/
  private final class JDPoolMaintainer implements Runnable
  {
    private final static String DAEMON_NAME = "Maintainer daemon";
    boolean isAwake_ = true;  // false if this daemon is sleeping, true otherwise
    boolean snooze_ = false;  // tells this daemon to "sleep late" if it wakes up


    /**
     The main job of this daemon is to renew/replenish the 'available' lists as needed.
     **/
    public void run()
    {
      final JDConnectionPoolKey[] dummyKeyArray_ = new JDConnectionPoolKey[0]; // for toArray()
      logInformation(DAEMON_NAME + " started with maintainerInterval_ = "+maintainerInterval_+" msecs and  maxIdleTime_ = "+maxIdleTime_+" msecs");

      ArrayList candidatesForRemoval = new ArrayList(minPoolSize_);  // reasonable initial size
      int numCondemnedConnections = 0;
      long previousRunTime = 0;  // duration of the previous cycle's maintenance activity
      long runStartTime = 0;

      try
      {
        while (keepDaemonsAlive_)
        {
          if (GATHER_STATS) System.out.print("(m)");
          isAwake_ = true;
          try
          {
            long timeToSleep = maintainerInterval_ - previousRunTime;
            if (timeToSleep > 0)
            {
              isAwake_ = false;
              try {
                synchronized (maintainerSleepLock_) {
                  maintainerSleepLock_.wait(timeToSleep);
                  isAwake_ = true;
                }
              }
              catch (InterruptedException ie) {}  // ignore
              finally { isAwake_ = true; }  // just to be sure
            }
            else {
              if (JDTrace.isTraceOn()) logWarning(DAEMON_NAME + " did not sleep");
            }
            while (snooze_)
            {
              isAwake_ = false;
              try {
                synchronized (maintainerSnoozeLock_) {
                  if (snooze_) maintainerSnoozeLock_.wait();
                  isAwake_ = true;
                }
              }
              catch (InterruptedException ie) {}  // ignore
              finally { isAwake_ = true; }  // just to be sure
            }
            if (!keepDaemonsAlive_) break;
            if (DEBUG) logInformation(DAEMON_NAME + " woke up");
            if (DEBUG || GATHER_STATS) maintainerDaemonCycles_++;

            pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called
            runStartTime = System.currentTimeMillis();
            if (GATHER_STATS) System.out.print("(M)");

            // Perform sequence of tasks, in separate try blocks.  That way, if an exception occurs in an early task, the final tasks in the loop don't get skipped.

            // Warn if there are 'active but in error' connections.
            if (isPoolFull() || activeConnectionsInError_.size() > 5) {
              if (activeConnectionsInError_.size() > 1)
              logWarning(activeConnectionsInError_.size() + " connections have experienced fatal connection errors, but are still held by the application.");
            }

            // Identify and remove any stale connections in the 'available' list.
            try
            {
              // Figure out our high water mark. If lastUsed_
              // on a pooled connection is before this value, that
              // means the connection has not been touched in 
              // a longer time than is allowed and we need to close
              // it down and remove it.
              long cutoffTime = System.currentTimeMillis() - maxIdleTime_;
              candidatesForRemoval.clear();

              // Identify any stale connections, and move them from the 'available' list to the candidatesForRemoval list.
              synchronized (availableConnections_[BACKGROUND])
              {
                if (!keepDaemonsAlive_) break;
                // Get list of all connection keys in the 'available' HashMap.
                JDConnectionPoolKey[] poolKeys = (JDConnectionPoolKey[])availableConnections_[BACKGROUND].keySet().toArray(dummyKeyArray_);
                for (int i=0; i<poolKeys.length && keepDaemonsAlive_; i++)
                {
                  JDConnectionPoolKey poolKey = poolKeys[i];
                  // Get stack for this key.
                  Stack connStack = (Stack)availableConnections_[BACKGROUND].get(poolKey);

                  if (!connStack.empty())  // Stack has some available connections in it.
                  {  // Remove invalidated or stale connections.
                    boolean keyIsInvalidated = invalidatedKeys_.contains(poolKey);  // take our chances on not sync-ing
                    Iterator availIter = connStack.iterator();
                    while (availIter.hasNext())
                    {
                      AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)availIter.next();
                      if (keyIsInvalidated || isStale(conn, cutoffTime))
                      {
                        if (DEBUG || GATHER_STATS) staleConnectionsIdentified_++;
                        // Prepare to remove the connection from the pool.
                        availIter.remove();  // remove it from 'available' list
                        candidatesForRemoval.add(conn);  // ... and move it to the 'removal pending' list.
                        // Also remove the connection from the associated 'idled sequence' list.
                        availableConnectionsIdledSequence_[BACKGROUND].remove(conn);
                      }
                      else if (!keyIsInvalidated) {
                        break;
                        // Connections are stored in order of longest-inactivity first.  As we move forward in a list: once we find a non-stale connection, we're done with that list.
                      }
                    }
                  }
                }
              }  // sync block


              if (DEBUG || GATHER_STATS) {
                if (candidatesForRemoval.size() != 0) {
                  System.out.println("\n(CLEANUP)"+DAEMON_NAME+" has added " + candidatesForRemoval.size() + " stale connections to condemned list.");
                }
              }

              // Now that we've unlocked the 'available' list:
              // Move the stale connections from the candidates list to the 'condemned' list.
              Iterator candidatesIter = candidatesForRemoval.iterator();
              synchronized (condemnedConnections_)
              {
                while (candidatesIter.hasNext()) {
                  condemnedConnections_.add(candidatesIter.next());
                  candidatesIter.remove();  // remove connection from candidates list
                }
                numCondemnedConnections = condemnedConnections_.size();
              }
              candidatesIter = null;  // We're done with this iterator.
              // Let the scavenger daemon close the condemned physical connections.

              if (!keepDaemonsAlive_) break;
              pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called

              // See if there are too many connections in the pool.
              if (isPoolOverFull())
              {
                // First close any 'condemned' connections.
                synchronized (condemnedConnections_)
                {
                  if (!keepDaemonsAlive_) break;
                  Iterator condemnedIter = condemnedConnections_.iterator();
                  while (condemnedIter.hasNext())
                  {
                    AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)condemnedIter.next();
                    condemnedIter.remove();
                    closePhysicalConnection(conn);
                    if (DEBUG || GATHER_STATS) condemnedConnectionsRemoved_++;
                  }
                }

                if (!keepDaemonsAlive_) break;
                pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called

                // Check again.  If still too many connections, remove oldest 'available' connections.
                if (isPoolOverFull())
                {
                  synchronized (availableConnections_[BACKGROUND])
                  {
                    if (!keepDaemonsAlive_) break;
                    int numToRemove = getConnectionCount(SYNC_ALL) - maxPoolSize_;
                    Iterator idledIter = availableConnectionsIdledSequence_[BACKGROUND].iterator();
                    for (int i=0; i<numToRemove && idledIter.hasNext(); i++)
                    {
                      AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)idledIter.next();
                      JDConnectionPoolKey poolKey = conn.poolKey_;
                      Stack keyStack = (Stack)availableConnections_[BACKGROUND].get(poolKey);
                      keyStack.remove(conn); // remove it from 'available' list
                      candidatesForRemoval.add(conn); // ... and move it to the 'removal pending' list.
                      // Also remove the connection from 'idled sequence' list.
                      idledIter.remove();
                    }
                  }

                  if (DEBUG || GATHER_STATS) {
                    if (candidatesForRemoval.size() != 0) {
                      System.out.println("\n(CLEANUP)"+DAEMON_NAME+" has additionally added " + candidatesForRemoval.size() + " semi-stale connections to condemned list.");
                    }
                  }

                  candidatesIter = candidatesForRemoval.iterator();
                  synchronized (condemnedConnections_)
                  { // Don't leave connections in limbo.
                    while (candidatesIter.hasNext()) {
                      condemnedConnections_.add(candidatesIter.next());
                      candidatesIter.remove();  // remove connection from candidates list
                    }
                    numCondemnedConnections = condemnedConnections_.size();
                  }
                  candidatesIter = null;  // We're done with this iterator.
                  // Let the scavenger daemon close the condemned physical connections.

                }
              }

              if (!keepDaemonsAlive_) break;
              pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called


              // If total pool size has fallen below minimum threshold, refill it.
              // Note: Unsynchronized access.  We can take our chances on count accuracies.
              int numToAdd = 0;
              int connectionCount = getConnectionCount(SYNC_NONE);
              if (connectionCount < minPoolSize_)
              {
                // Add new connections so that we're at minimum pool size.
                // Note: Don't just fill to maxPoolSize_, since we need to leave room to create non-default connections if any requests for them come in.
                numToAdd = minPoolSize_ - connectionCount;
              }

              // Try to have at least one connection available for the default key.
              if (!isPoolFull() && (numToAdd == 0))
              {
                JDConnectionPoolKey defaultPoolKey = cpds_.getConnectionPoolKey();
                // We can take our chances and not synchronize this access of the 'avail' list.
                Stack defaultStack = (Stack)availableConnections_[BACKGROUND].get(defaultPoolKey);
                if (defaultStack == null || defaultStack.size() < minDefaultStackSize_) {
                  int stackSize = (defaultStack == null ? 0 : defaultStack.size());
                  numToAdd = minDefaultStackSize_ - stackSize;
                }
              }

              if (numToAdd > 0) {
                fillPool(numToAdd, BACKGROUND);
              }

              // See if the foreground process has indicated that we need to do a list swap.
              if (needMoreConnections_)
              {
                // Request a swap.  This may take awhile, but the daemon is not in a hurry.
                // Note that the swap may fail if background list is not longer than foreground list.
                if (DEBUG) logInformation(DAEMON_NAME + " is requesting a swap; needMoreConnections_ =="+needMoreConnections_);
                JDConnectionPoolKey defaultPoolKey = cpds_.getConnectionPoolKey();
                swapConnectionLists(BACKGROUND, defaultPoolKey);
              }
            }
            catch (Exception e)
            {
              if (JDTrace.isTraceOn()) logException("Exception caught by " + DAEMON_NAME, e);
              // Keep running.
            }


            // If needed, wake up the other daemons to close condemned or expired connections.
            if (numCondemnedConnections > condemnedListLengthThreshold_)
            {
              wakeScavengerDaemon();
            }
            else if (isPoolOverFull())
            {
              wakeReaperDaemon();
            }

          }  // inner try
          catch (Exception e)
          {
            if (JDTrace.isTraceOn()) logException ("Exception caught by " + DAEMON_NAME, e);
            // Keep running.
          }
          finally {
            isAwake_ = true;
            previousRunTime = System.currentTimeMillis() - runStartTime;
            minSwapInterval_ = Math.max(50, previousRunTime / 2);
          }
        }  // while keepDaemonsAlive_
      }  // outermost try
      finally
      {
        logInformation(DAEMON_NAME + " has stopped");
        isAwake_ = false;
      }
    }

  }  // internal class JDPoolMaintainer




  // JDPoolReaper --------------------------------------------------------------------

  /**
   Helper class. This daemon wakes up every reaperInterval_ milliseconds
   and scans the list of active connections. If a connection is expired,
   PoolReaper moves it from the 'active' list to the 'condemned' list.
   Note: This daemon is _not_ started if connections have "unlimited lifetime".
   **/
  private final class JDPoolReaper implements Runnable
  {
    private final static String DAEMON_NAME = "Reaper daemon";

    public void run()
    {
      logInformation(DAEMON_NAME + " started with reaperInterval_ = "+reaperInterval_+" msecs and maxLifetime_ = "+maxLifetime_+" msecs");

      ArrayList candidatesForRemoval = new ArrayList(minPoolSize_);  // reasonable initial size
      long previousRunTime = 0;  // duration of the previous cycle's maintenance activity
      long runStartTime = 0;

      try
      {
        while (keepDaemonsAlive_)
        {
          if (GATHER_STATS) System.out.print("(r)");
          try
          {
            long timeToSleep = reaperInterval_ - previousRunTime;
            if (timeToSleep > 0)
            {
              try {
                synchronized (reaperSleepLock_) {
                  reaperSleepLock_.wait(timeToSleep);
                }
              }
              catch (InterruptedException ie) {}  // ignore
              if (DEBUG) logInformation(DAEMON_NAME + " emerged from wait()");
            }
            else {
              if (JDTrace.isTraceOn()) logWarning(DAEMON_NAME + " did not sleep");
            }
            if (!keepDaemonsAlive_) break;
            if (DEBUG) logInformation(DAEMON_NAME + " woke up");
            if (DEBUG || GATHER_STATS) reaperDaemonCycles_++;

            pauseIfPoolPaused(reaperInterval_);  // don't risk pausing forever

            runStartTime = System.currentTimeMillis();
            if (GATHER_STATS) System.out.print("(R)");

            // Identify any expired connections in the 'active' list, and move them to 'condemned' list.

            long cutoffTime = runStartTime - maxLifetime_; // Any connections allocated prior to this moment in time are considered expired, and are subject to forced disconnection.
            candidatesForRemoval.clear();

            synchronized (activeConnections_)
            {
              if (!keepDaemonsAlive_) break;
              Iterator activeIter = activeConnections_.iterator();
              while (activeIter.hasNext())
              {
                AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)activeIter.next();
                if (isExpired(conn, cutoffTime))
                {
                  if (isStale(conn, runStartTime - maxIdleTime_))  // Don't reap connections that were recently allocated.
                  {
                    if (DEBUG || GATHER_STATS) expiredConnectionsIdentifiedByReaper_++;
                    activeIter.remove(); // Remove connection from the 'active' list.
                    if (conn.fatalConnectionErrorOccurred_) {
                      activeConnectionsInError_.remove(conn); // ... and also from 'error' list
                    }
                    candidatesForRemoval.add(conn); // ... and add it to the 'condemned' list.
                  }
                }
                else break;  // This list is sorted in ascending order of connection creation time.  As we move forward in the list: once we find a non-expired connection, we're done with the list.
              }  // while
            }  // sync

            if (DEBUG || GATHER_STATS) {
              if (candidatesForRemoval.size() != 0) {
                System.out.println("\n(CLEANUP)"+DAEMON_NAME+" has added " + candidatesForRemoval.size() + " expired connections to condemned list.");
              }
            }

            // Move any staged expired connections to the 'condemned' list.
            Iterator candidatesIter = candidatesForRemoval.iterator();
            int numCondemnedConnections;
            synchronized (condemnedConnections_)
            {  // Don't leave connections in limbo.
              while (candidatesIter.hasNext()) {
                AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)candidatesIter.next();
                if (JDTrace.isTraceOn()) logDiagnostic(DAEMON_NAME+" is closing an active connection that has exceeded the maximum lifetime: " + conn.toString());
                condemnedConnections_.add(conn);
                candidatesIter.remove();  // remove connection from candidatesIter list
              }
              numCondemnedConnections = condemnedConnections_.size();
            }

            // If needed, wake up the scavenger daemon to close condemned physical connections.
            if (numCondemnedConnections > condemnedListLengthThreshold_)
            {
              wakeScavengerDaemon();
            }

          }  // inner try
          catch (Exception e)
          {
            if (JDTrace.isTraceOn()) logException("Exception caught by " + DAEMON_NAME, e);
          }
          finally {
            previousRunTime = System.currentTimeMillis() - runStartTime;
          }
        }  // while keepDaemonsAlive_
      }  // outermost try
      finally
      {
        logInformation(DAEMON_NAME + " has stopped");
      }
    }

  }  // internal class JDPoolReaper




  // JDPoolScavenger --------------------------------------------------------------------

  /**
   Helper class. This daemon wakes up every 'scavengerInterval_' milliseconds and
   closes all the connections in the 'condemned' list.
   **/
  private final class JDPoolScavenger implements Runnable
  {
    private final static String DAEMON_NAME = "Scavenger daemon";

    public void run()
    {
      logInformation(DAEMON_NAME + " started with scavengerInterval_ = "+scavengerInterval_+" msecs");
      ArrayList candidatesForRemoval = new ArrayList(minPoolSize_);  // reasonable initial size
      long previousRunTime = 0;  // duration of the previous cycle's maintenance activity
      long runStartTime = 0;

      try
      {
        while (keepDaemonsAlive_)
        {
          if (GATHER_STATS) System.out.print("(s)");
          try
          {
            long timeToSleep = scavengerInterval_ - previousRunTime;
            if (timeToSleep > 0)
            {
              try {
                synchronized (scavengerSleepLock_) {
                  scavengerSleepLock_.wait(timeToSleep);
                }
              }
              catch (InterruptedException ie) {}  // ignore
              if (DEBUG) logInformation(DAEMON_NAME + " emerged from wait()");
            }
            else {
              if (JDTrace.isTraceOn()) logWarning(DAEMON_NAME + " did not sleep");
            }
            if (!keepDaemonsAlive_) break;
            if (DEBUG) logInformation(DAEMON_NAME + " woke up");
            if (DEBUG || GATHER_STATS) scavengerDaemonCycles_++;

            pauseIfPoolPaused(0);  // if pool is paused, wait until unpause() is called
            runStartTime = System.currentTimeMillis();
            if (GATHER_STATS) System.out.print("(+S)");

            // Close all connections in the 'condemned' list.

            synchronized (condemnedConnections_)
            {
              if (!keepDaemonsAlive_) break;
              Iterator condemnedIter = condemnedConnections_.iterator();
              while (condemnedIter.hasNext())
              {
                AS400JDBCPooledConnection conn = (AS400JDBCPooledConnection)condemnedIter.next();
                condemnedIter.remove();
                candidatesForRemoval.add(conn);
              }
            }

            if (DEBUG || GATHER_STATS) {
              if (candidatesForRemoval.size() != 0) {
                System.out.println("\n(CLEANUP)"+DAEMON_NAME+" is closing " + candidatesForRemoval.size() + " condemned connections.");
              }
            }

            // Don't leave connections in limbo.
            Iterator candidatesIter = candidatesForRemoval.iterator();
            while (candidatesIter.hasNext()) {
              closePhysicalConnection((AS400JDBCPooledConnection)candidatesIter.next());
              if (DEBUG || GATHER_STATS) condemnedConnectionsRemoved_++;
              candidatesIter.remove();  // remove connection from candidatesForRemoval list
            }

            if (DEBUG && getConnectionCount(SYNC_ALL) < 0) {
              logError(DAEMON_NAME + ": Total connection count is negative"); // This would indicate a design flaw.
            }

          }  // inner try
          catch (Exception e)
          {
            if (JDTrace.isTraceOn()) logException("Exception caught by " + DAEMON_NAME, e);
          }
          finally {
            previousRunTime = System.currentTimeMillis() - runStartTime;

            // If needed, wake up the reaper daemon to close any expired connections.
            if (isPoolOverFull())
            {
              wakeReaperDaemon();
            }
          }
        }  // while keepDaemonsAlive_
      }  // outermost try
      finally
      {
        logInformation(DAEMON_NAME + " has stopped");
      }
    }

  }  // internal class JDPoolScavenger

}  // class JDConnectionPoolManager
