///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBDSPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.ref.SoftReference;


// This handles all of the datastream pooling for JDBC.
final class DBDSPool
{
  static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";

  private DBDSPool() {}

  // Request streams.
  private static DBSQLRPBDS[] dbsqlrpbdsPool_ = new DBSQLRPBDS[4];
  private static final Object dbsqlrpbdsPoolLock_ = new Object(); //@P1A
  
  private static DBSQLDescriptorDS[] dbsqldescriptordsPool_ = new DBSQLDescriptorDS[4];
  private static final Object dbsqldescriptordsPoolLock_ = new Object(); //@P1A
  
  private static DBSQLResultSetDS[] dbsqlresultsetdsPool_ = new DBSQLResultSetDS[4];
  private static final Object dbsqlresultsetdsPoolLock_ = new Object(); //@P1A
  
  private static DBSQLRequestDS[] dbsqlrequestdsPool_ = new DBSQLRequestDS[4];
  private static final Object dbsqlrequestdsPoolLock_ = new Object(); //@P1A
  private static int          dbsqlrequestdsPoolHighMark_ = 0;        // @A8A
  private static int          dbsqlrequestdsPoolAllocations_ = 0; 
  private static int          dbsqlrequestdsPoolReclaimThreshold = 1000;  
   
  private static DBNativeDatabaseRequestDS[] dbnativedatabaserequestdsPool_ = new DBNativeDatabaseRequestDS[4];
  private static final Object dbnativedatabaserequestdsPoolLock_ = new Object(); //@P1A
  
  private static DBReturnObjectInformationRequestDS[] dbreturnobjectinformationrequestdsPool_ = new DBReturnObjectInformationRequestDS[4];
  private static final Object dbreturnobjectinformationrequestdsPoolLock_ = new Object(); //@P1A
  
  private static DBSQLAttributesDS[] dbsqlattributesdsPool_ = new DBSQLAttributesDS[4];
  private static final Object dbsqlattributesdsPoolLock_ = new Object(); //@P1A
  
  private static DBXARequestDS[] dbxarequestdsPool_ = new DBXARequestDS[4];
  private static final Object dbxarequestdsPoolLock_ = new Object(); //@P1A

  // Reply streams.
  /* private  */ 
  static SoftReference[] dbreplyrequesteddsPool_ = new SoftReference[4];
  private static final Object dbreplyrequesteddsPoolLock_ = new Object(); //@P1A
  private static int dbReplyRequestedPoolSearchStart_=0;                                         //@B5A
  private static final int DB_REPLY_REQUEST_POOL_SEARCH_RESET = 4096;         // @B5A
  private static final int DB_REPLY_REQUEST_LOCALITY = 256; 
  private static int       dbReplyRequestedPoolSearchCount_ = 0;                                    // @B5A
  private static boolean   dbReplyRequestedPoolFull_ = false; 

  static final DBStoragePool storagePool_ = new DBStoragePool();
  
  
  // IMPORTANT: These methods only retrieve free streams from their respective pools.
  // It is up to the code using these pools to free up the streams by setting
  // their inUse_ flags to false.
  static String changeTime="2010/06/14 08:09"; 
  static boolean noDBReplyPooling = false;
  static boolean monitor = false;                                                                                 //@B5A

  static { 
    // NOTE:  We must use SystemProperties.getProperty to read the property.  Otherwise
    //        an exception may be thrown if the application is not allowed to read properties.  @D8C 
	  String noPoolingProperty = SystemProperties.getProperty("com.ibm.as400.access.noDBReplyPooling");  /*@D8C*/ 
	  if (noPoolingProperty != null) { 
		  noDBReplyPooling = true; 
	  }
    String monitorProperty = SystemProperties.getProperty("com.ibm.as400.access.DBDSPool.monitor");    //@B5A@D8C
  	  if (monitorProperty != null) { 
  		  monitor = true; 
  	  }
  }
  
  static void returnToDBReplyRequestedPool(int poolIndex) {                                        //@B5A
	  if (poolIndex >= 0) {
	  if (dbReplyRequestedPoolFull_) {
		  dbReplyRequestedPoolFull_ = false;
	      dbReplyRequestedPoolSearchStart_ = poolIndex; 
	  } else { 
	    if (poolIndex < dbReplyRequestedPoolSearchStart_ &&
		  poolIndex >= (dbReplyRequestedPoolSearchStart_ - DB_REPLY_REQUEST_LOCALITY )) {
	      dbReplyRequestedPoolSearchStart_ = poolIndex; 
	    }
	  }
	  }
  }
  
  public static int getDBReplyRequestedDSPoolSize() {
	  return dbreplyrequesteddsPool_.length; 
  }
  
  static final DBReplyRequestedDS getDBReplyRequestedDS() {
    if (noDBReplyPooling) {
      DBReplyRequestedDS unpooledDS = new DBReplyRequestedDS(monitor, -1); // @B5C
      unpooledDS.canUse();
      unpooledDS.setInPool(false); // @B5A
      return unpooledDS;
    } else {
      synchronized (dbreplyrequesteddsPoolLock_) // @P1C
      {
        // This pools is maintained as weak references to
        // all the garbage collector to free up as needed.
        // DBReplyRequestedDS[] pool = dbreplyrequesteddsPool_; //@P1M
        SoftReference[] pool = dbreplyrequesteddsPool_; // @P1M
        int max = pool.length;

        // Periodically begin the search for free items at the beginning of the
        // pool @B5A
        int searchStart = dbReplyRequestedPoolSearchStart_;
        if (dbReplyRequestedPoolFull_) {
          searchStart = pool.length;
        }
        if (dbReplyRequestedPoolSearchCount_ > DB_REPLY_REQUEST_POOL_SEARCH_RESET) {
          dbReplyRequestedPoolFull_ = false;
          searchStart = 0;
          dbReplyRequestedPoolSearchCount_ = 0;
        }
        dbReplyRequestedPoolSearchCount_++;

        for (int i = searchStart; i < pool.length; ++i) {
          if (pool[i] == null) {
            DBReplyRequestedDS pooledDs = new DBReplyRequestedDS(monitor, i);
            pooledDs.canUse();
            pooledDs.setInPool(true);
            pool[i] = new SoftReference(pooledDs);
            dbReplyRequestedPoolSearchStart_ = i + 1;
            return pooledDs;

          } else {
            DBReplyRequestedDS ds = (DBReplyRequestedDS) pool[i].get();
            // Sometimes the pool gets unpooled entries.  If this is the case
            // just remove it from the pool. 
            // Sometimes ds.storage_ can be null because of a
            // GC bug.  If so, do not use it. 
            if (ds == null || ds.storage_ == null   ) {
              ds = new DBReplyRequestedDS(monitor, i);
              ds.canUse();
              ds.setInPool(true);
              pool[i] = new SoftReference(ds);
              dbReplyRequestedPoolSearchStart_ = i + 1;
              // pool[i].setPoolIndex(i);
              return ds;

            } else {
              if ((ds.inUse_ == false) && (ds.canUse())) {
            	  
                // More code for the GC bug.  If the finalize
            	  // happened to run after we checked then storage could have been null  
                if (ds.storage_ == null) {
                  // If the storage_ happens to be null, don't use it.
                  // Keep looking for another one.  This will be cleaned 
                  // up on the next search (see above). 
                } else {
                  ds.initialize();
                  // pool[i].setPoolIndex(i);
                  dbReplyRequestedPoolSearchStart_ = i + 1;
                  return ds;
                }
              }
            }
          }
        }
        // All are in use, so expand the pool but keep the pool less than 16384
        if (max * 2 < 16385) {
          // Have a monitor so we can see the usage of these items @B5A
          if (monitor) {
            System.out.println("Expanding DBDSPool to size " + (max * 2));
            for (int i = 0; i < max; i++) {
              DBReplyRequestedDS ds = (DBReplyRequestedDS) pool[i].get();
              if (ds == null) {
                System.out.println("Entry " + i + " : null");
              } else {
                System.out.println("Entry " + i + " : "
                    + ds.getAllocatedLocation());
              }
            }
          }
          SoftReference[] temp = new SoftReference[max * 2];
          System.arraycopy(pool, 0, temp, 0, max);
          DBReplyRequestedDS pooledDs = new DBReplyRequestedDS(monitor, max);
          pooledDs.canUse(); 
          pooledDs.setInPool(true); // @B5A
          temp[max] = new SoftReference(pooledDs);
          // temp[max].setPoolIndex(max);

          dbreplyrequesteddsPool_ = temp;
          // Start the search at zero after expanding
          dbReplyRequestedPoolSearchStart_ = 0; // @B5A
          return pooledDs;
        } else {
          DBReplyRequestedDS nonpooledDS = new DBReplyRequestedDS(monitor, -1);
          nonpooledDS.canUse();
          nonpooledDS.setInPool(false);
          if (!dbReplyRequestedPoolFull_) {
            dbReplyRequestedPoolFull_ = true;
          }
          return nonpooledDS;
        }
      } /* synchronized */ 
    }
  }
  
  static final DBXARequestDS getDBXARequestDS(int a, int b, int c, int d)
  {
    synchronized(dbxarequestdsPoolLock_) //@P1C
    {
      DBXARequestDS[] pool = dbxarequestdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          DBXARequestDS request = new DBXARequestDS(a,b,c,d);
          request.canUse();
          pool[i] = request; 
          return request;
        }
         if (( pool[i].inUse_ == false) && pool[i].canUse())
           {
            pool[i].initialize(a,b,c,d);
            return pool[i];
           }
      }
      // All are in use, so expand the pool.
      DBXARequestDS[] temp = new DBXARequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBXARequestDS request = new DBXARequestDS(a,b,c,d);
      request.canUse();
      temp[max] = request; 
      dbxarequestdsPool_ = temp;
      return request;
    }
  }

  static final DBSQLAttributesDS getDBSQLAttributesDS(int a, int b, int c, int d)
  {
    synchronized(dbsqlattributesdsPoolLock_) //@P1C
    {
      DBSQLAttributesDS[] pool = dbsqlattributesdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          DBSQLAttributesDS request = new DBSQLAttributesDS(a,b,c,d);
          request.canUse(); 
          pool[i] = request; 
          return request;
        }

          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
          }
      }
      // All are in use, so expand the pool.
      DBSQLAttributesDS[] temp = new DBSQLAttributesDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBSQLAttributesDS ds = new DBSQLAttributesDS(a,b,c,d);
      ds.canUse();
      temp[max]= ds;
      dbsqlattributesdsPool_ = temp;
      return ds;
    }
  }

  static final DBNativeDatabaseRequestDS getDBNativeDatabaseRequestDS(int a, int b, int c, int d)
  {
    synchronized(dbnativedatabaserequestdsPoolLock_) //@P1C
    {
      DBNativeDatabaseRequestDS[] pool = dbnativedatabaserequestdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          DBNativeDatabaseRequestDS request =  new DBNativeDatabaseRequestDS(a,b,c,d);
          // Mark as used before placing in the pool
          request.canUse(); 
          pool[i] =request; 
          return request;
        }
          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
          }
      }
      // All are in use, so expand the pool.
      DBNativeDatabaseRequestDS[] temp = new DBNativeDatabaseRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBNativeDatabaseRequestDS newRequest = new DBNativeDatabaseRequestDS(a,b,c,d);
      // Mark as inuse before adding to the pool
      newRequest.canUse(); 
      temp[max] = newRequest; 
  
      dbnativedatabaserequestdsPool_ = temp;
      return newRequest;
    }
  }

  static final DBReturnObjectInformationRequestDS getDBReturnObjectInformationRequestDS(int a, int b, int c, int d)
  {
    synchronized(dbreturnobjectinformationrequestdsPoolLock_) //@P1C
    {
      DBReturnObjectInformationRequestDS[] pool = dbreturnobjectinformationrequestdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          DBReturnObjectInformationRequestDS request = new DBReturnObjectInformationRequestDS(a,b,c,d);
          request.canUse(); 
          pool[i] = request; 
          
          return request;
        }
    	synchronized(pool[i]) {   // @A7A  

          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
         }
        }
      }
      // All are in use, so expand the pool.
      DBReturnObjectInformationRequestDS[] temp = new DBReturnObjectInformationRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBReturnObjectInformationRequestDS newRequest = new DBReturnObjectInformationRequestDS(a,b,c,d);
      newRequest.canUse(); 
      temp[max] = newRequest; 
      dbreturnobjectinformationrequestdsPool_ = temp;
      return newRequest;
    }
  }

  static final DBSQLDescriptorDS getDBSQLDescriptorDS(int a, int b, int c, int d)
  {
    synchronized(dbsqldescriptordsPoolLock_) //@P1C
    {
      DBSQLDescriptorDS[] pool = dbsqldescriptordsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          DBSQLDescriptorDS ds = new DBSQLDescriptorDS(a,b,c,d);
          ds.canUse();
          pool[i] = ds;
          return ds;
        }
    	synchronized(pool[i]) {   // @A7A  

          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
          }
    	}
      }
      // All are in use, so expand the pool.
      DBSQLDescriptorDS[] temp = new DBSQLDescriptorDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBSQLDescriptorDS ds = new DBSQLDescriptorDS(a,b,c,d);
      ds.canUse();
      temp[max] = ds; 
      dbsqldescriptordsPool_ = temp;
      return ds;
    }
  }

  static final DBSQLRequestDS getDBSQLRequestDS(int a, int b, int c, int d)
  {
    synchronized(dbsqlrequestdsPoolLock_) //@P1C
    {
      DBSQLRequestDS[] pool = dbsqlrequestdsPool_; //@P1M

      int max = pool.length;

      //  @A8A  
      dbsqlrequestdsPoolAllocations_++; 
      if (dbsqlrequestdsPoolAllocations_ > dbsqlrequestdsPoolReclaimThreshold) {
    	  // System.out.println("Running cleanup in getDBSQLRequestDS"); 
    	  dbsqlrequestdsPoolAllocations_ = 0; 
    	  for (int i = dbsqlrequestdsPoolHighMark_+1; i < max; i++) {
   	        if (pool[i] != null) {
   	        	if (( pool[i].inUse_ == false) && pool[i].canUse()) {
     	          pool[i].reclaim(); 
     	          pool[i].returnToPool();
   	            }
   	        }
    	  }
    	  dbsqlrequestdsPoolHighMark_ = 0; 
    	  // System.out.println("Finished cleanup in getDBSQLRequestDS"); 
      }
      
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          DBSQLRequestDS ds = new DBSQLRequestDS(a,b,c,d);
          ds.canUse();
          pool[i] = ds; 
          if (i > dbsqlrequestdsPoolHighMark_) dbsqlrequestdsPoolHighMark_ = i;   // @A8A
          return ds;
        }
          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            if (i > dbsqlrequestdsPoolHighMark_) dbsqlrequestdsPoolHighMark_ = i; 
            return pool[i];
          }
      }
      // All are in use, so expand the pool.
      DBSQLRequestDS[] temp = new DBSQLRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBSQLRequestDS ds = new DBSQLRequestDS(a,b,c,d);
      ds.canUse();
      temp[max]=ds; 
      dbsqlrequestdsPool_ = temp;
      if (max > dbsqlrequestdsPoolHighMark_) dbsqlrequestdsPoolHighMark_ = max;   // @A8A
      return ds;
    }
  }

  static final DBSQLResultSetDS getDBSQLResultSetDS(int a, int b, int c, int d)
  {
    synchronized(dbsqlresultsetdsPoolLock_) //@P1C
    {
      DBSQLResultSetDS[] pool = dbsqlresultsetdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          DBSQLResultSetDS ds = new DBSQLResultSetDS(a,b,c,d);
          ds.canUse();
          pool[i] = ds; 
          return ds;
        }
          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
          }
      }
      // All are in use, so expand the pool.
      DBSQLResultSetDS[] temp = new DBSQLResultSetDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBSQLResultSetDS ds = new DBSQLResultSetDS(a,b,c,d);
      ds.canUse();
      temp[max] = ds; 
      dbsqlresultsetdsPool_ = temp;
      return ds;
    }
  }

  static final DBSQLRPBDS getDBSQLRPBDS(int a, int b, int c, int d)
  {
    synchronized(dbsqlrpbdsPoolLock_) //@P1C
    {
      DBSQLRPBDS[] pool = dbsqlrpbdsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          DBSQLRPBDS ds = new DBSQLRPBDS(a,b,c,d);
          ds.canUse();
          pool[i] = ds; 
          return ds;
        }
          if (( pool[i].inUse_ == false) && pool[i].canUse())
          {
            pool[i].initialize(a,b,c,d);
            return pool[i];
          }
      }
      // All are in use, so expand the pool.
      DBSQLRPBDS[] temp = new DBSQLRPBDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      DBSQLRPBDS ds = new DBSQLRPBDS(a,b,c,d);
      ds.canUse();
      temp[max] = ds; 
      dbsqlrpbdsPool_ = temp;
      return ds;
    }
  }

}

