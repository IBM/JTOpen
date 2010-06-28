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
  /* private  */ static DBReplyRequestedDS[] dbreplyrequesteddsPool_ = new DBReplyRequestedDS[4];
  private static final Object dbreplyrequesteddsPoolLock_ = new Object(); //@P1A

  static final DBStoragePool storagePool_ = new DBStoragePool();
  
  // IMPORTANT: These methods only retrieve free streams from their respective pools.
  // It is up to the code using these pools to free up the streams by setting
  // their inUse_ flags to false.
  static String changeTime="2010/06/14 08:09"; 
  static boolean noDBReplyPooling = false;
  // static boolean monitor = false; 
  static { 
	  String noPoolingProperty = System.getProperty("com.ibm.as400.access.noDBReplyPooling"); 
	  if (noPoolingProperty != null) { 
		  noDBReplyPooling = true; 
	  }
  //	  String monitorProperty = System.getProperty("com.ibm.as400.access.DBDSPool.monitor");
  //	  if (monitorProperty != null) { 
  //		  monitor = true; 
  //	  }
  }
  
  static final DBReplyRequestedDS getDBReplyRequestedDS()
  {
	if (noDBReplyPooling) { 
		DBReplyRequestedDS newDS = new DBReplyRequestedDS();
		newDS.canUse(); 
		return newDS; 
	} else { 
    synchronized(dbreplyrequesteddsPoolLock_) //@P1C
    {
      DBReplyRequestedDS[] pool = dbreplyrequesteddsPool_; //@P1M
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBReplyRequestedDS();
          pool[i].canUse();
   		  // pool[i].setPoolIndex(i); 
          return pool[i];
        } else { 
        	   if (( pool[i].inUse_ == false) && (pool[i].canUse())) {
        		pool[i].initialize();
        		// pool[i].setPoolIndex(i); 
        		
        		return pool[i];
        	  }
        }
      }
      // All are in use, so expand the pool but keep the pool less than 16384
      if (max * 2 < 16384) { 
    	// if (monitor) {
    	//	System.out.println("Expanding DBDSPool to size "+( max * 2)); 
    	//	for (int i = 0 ; i < max; i++) { 
    	//		System.out.println("Entry "+i+" : "+pool[i].getAllocatedLocation());
    	//	}
    	//}
        DBReplyRequestedDS[] temp = new DBReplyRequestedDS[max*2];
        System.arraycopy(pool, 0, temp, 0, max);
        temp[max] = new DBReplyRequestedDS();
        temp[max].canUse();
		// temp[max].setPoolIndex(max); 

        dbreplyrequesteddsPool_ = temp;
        return temp[max];
      } else {
  		DBReplyRequestedDS newDS = new DBReplyRequestedDS();
		newDS.canUse(); 
		return newDS; 
      }
    }
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
          pool[i] = new DBXARequestDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBXARequestDS(a,b,c,d);
      temp[max].canUse();
      dbxarequestdsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBSQLAttributesDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBSQLAttributesDS(a,b,c,d);
      temp[max].canUse();
      dbsqlattributesdsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBNativeDatabaseRequestDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBNativeDatabaseRequestDS(a,b,c,d);
      temp[max].canUse();
      dbnativedatabaserequestdsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBReturnObjectInformationRequestDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBReturnObjectInformationRequestDS(a,b,c,d);
      temp[max].canUse();
      dbreturnobjectinformationrequestdsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBSQLDescriptorDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBSQLDescriptorDS(a,b,c,d);
      temp[max].canUse();
      dbsqldescriptordsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBSQLRequestDS(a,b,c,d);
          pool[i].canUse();
          if (i > dbsqlrequestdsPoolHighMark_) dbsqlrequestdsPoolHighMark_ = i;   // @A8A
          return pool[i];
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
      temp[max] = new DBSQLRequestDS(a,b,c,d);
      temp[max].canUse();
      dbsqlrequestdsPool_ = temp;
      if (max > dbsqlrequestdsPoolHighMark_) dbsqlrequestdsPoolHighMark_ = max;   // @A8A
      return temp[max];
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
          pool[i] = new DBSQLResultSetDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBSQLResultSetDS(a,b,c,d);
      temp[max].canUse();
      dbsqlresultsetdsPool_ = temp;
      return temp[max];
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
          pool[i] = new DBSQLRPBDS(a,b,c,d);
          pool[i].canUse();
          return pool[i];
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
      temp[max] = new DBSQLRPBDS(a,b,c,d);
      temp[max].canUse();
      dbsqlrpbdsPool_ = temp;
      return temp[max];
    }
  }

}

