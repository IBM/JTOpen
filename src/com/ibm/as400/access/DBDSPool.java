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
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private DBDSPool() {}

  // Request streams.
  private static DBSQLRPBDS[] dbsqlrpbdsPool_ = new DBSQLRPBDS[4];
  private static DBSQLDescriptorDS[] dbsqldescriptordsPool_ = new DBSQLDescriptorDS[4];
  private static DBSQLResultSetDS[] dbsqlresultsetdsPool_ = new DBSQLResultSetDS[4];
  private static DBSQLRequestDS[] dbsqlrequestdsPool_ = new DBSQLRequestDS[4];
  private static DBNativeDatabaseRequestDS[] dbnativedatabaserequestdsPool_ = new DBNativeDatabaseRequestDS[4];
  private static DBReturnObjectInformationRequestDS[] dbreturnobjectinformationrequestdsPool_ = new DBReturnObjectInformationRequestDS[4];
  private static DBSQLAttributesDS[] dbsqlattributesdsPool_ = new DBSQLAttributesDS[4];
  private static DBXARequestDS[] dbxarequestdsPool_ = new DBXARequestDS[4];

  // Reply streams.
  private static DBReplyRequestedDS[] dbreplyrequesteddsPool_ = new DBReplyRequestedDS[4];

  static final DBStoragePool storagePool_ = new DBStoragePool();
  
  // IMPORTANT: These methods only retrieve free streams from their respective pools.
  // It is up to the code using these pools to free up the streams by setting
  // their inUse_ flags to false.

  static final DBReplyRequestedDS getDBReplyRequestedDS()
  {
    DBReplyRequestedDS[] pool = dbreplyrequesteddsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBReplyRequestedDS();
          pool[i].inUse_ = true;
          return pool[i];
        }
        else if (!pool[i].inUse_)
        {
          pool[i].initialize();
          pool[i].inUse_ = true;
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBReplyRequestedDS[] temp = new DBReplyRequestedDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBReplyRequestedDS();
      temp[max].inUse_ = true;
      dbreplyrequesteddsPool_ = temp;
      return temp[max];
    }
  }

  
  static final DBXARequestDS getDBXARequestDS(int a, int b, int c, int d)
  {
    DBXARequestDS[] pool = dbxarequestdsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBXARequestDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBXARequestDS[] temp = new DBXARequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBXARequestDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbxarequestdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBSQLAttributesDS getDBSQLAttributesDS(int a, int b, int c, int d)
  {
    DBSQLAttributesDS[] pool = dbsqlattributesdsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBSQLAttributesDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBSQLAttributesDS[] temp = new DBSQLAttributesDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBSQLAttributesDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbsqlattributesdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBNativeDatabaseRequestDS getDBNativeDatabaseRequestDS(int a, int b, int c, int d)
  {
    DBNativeDatabaseRequestDS[] pool = dbnativedatabaserequestdsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBNativeDatabaseRequestDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBNativeDatabaseRequestDS[] temp = new DBNativeDatabaseRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBNativeDatabaseRequestDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbnativedatabaserequestdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBReturnObjectInformationRequestDS getDBReturnObjectInformationRequestDS(int a, int b, int c, int d)
  {
    DBReturnObjectInformationRequestDS[] pool = dbreturnobjectinformationrequestdsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<pool.length; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBReturnObjectInformationRequestDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBReturnObjectInformationRequestDS[] temp = new DBReturnObjectInformationRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBReturnObjectInformationRequestDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbreturnobjectinformationrequestdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBSQLDescriptorDS getDBSQLDescriptorDS(int a, int b, int c, int d)
  {
    DBSQLDescriptorDS[] pool = dbsqldescriptordsPool_;
    synchronized(pool)
    {
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBSQLDescriptorDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBSQLDescriptorDS[] temp = new DBSQLDescriptorDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBSQLDescriptorDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbsqldescriptordsPool_ = temp;
      return temp[max];
    }
  }

  static final DBSQLRequestDS getDBSQLRequestDS(int a, int b, int c, int d)
  {
    DBSQLRequestDS[] pool = dbsqlrequestdsPool_;
    synchronized(dbsqlrequestdsPool_)
    {
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBSQLRequestDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBSQLRequestDS[] temp = new DBSQLRequestDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBSQLRequestDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbsqlrequestdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBSQLResultSetDS getDBSQLResultSetDS(int a, int b, int c, int d)
  {
    DBSQLResultSetDS[] pool = dbsqlresultsetdsPool_;
    synchronized(dbsqlresultsetdsPool_)
    {
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBSQLResultSetDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBSQLResultSetDS[] temp = new DBSQLResultSetDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBSQLResultSetDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbsqlresultsetdsPool_ = temp;
      return temp[max];
    }
  }

  static final DBSQLRPBDS getDBSQLRPBDS(int a, int b, int c, int d)
  {
    DBSQLRPBDS[] pool = dbsqlrpbdsPool_;
    synchronized(dbsqlrpbdsPool_)
    {
      int max = pool.length;
      for (int i=0; i<max; ++i)
      {
        if (pool[i] == null)
        {
          pool[i] = new DBSQLRPBDS(a,b,c,d);
          pool[i].inUse_ = true;
          return pool[i];
        }
        if (!pool[i].inUse_)
        {
          pool[i].inUse_ = true;
          pool[i].initialize(a,b,c,d);
          return pool[i];
        }
      }
      // All are in use, so expand the pool.
      DBSQLRPBDS[] temp = new DBSQLRPBDS[max*2];
      System.arraycopy(pool, 0, temp, 0, max);
      temp[max] = new DBSQLRPBDS(a,b,c,d);
      temp[max].inUse_ = true;
      dbsqlrpbdsPool_ = temp;
      return temp[max];
    }
  }

}

