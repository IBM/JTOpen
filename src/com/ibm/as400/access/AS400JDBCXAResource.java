///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCXAResource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.DriverManager;
import java.util.Vector;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;



/**
The AS400JDBCXAResource class represents a resource manager
for use in XA transaction management.

<p>This support is only available when connecting to servers running OS/400 V5R1 or later.

<p>The following example creates an AS400JDBCXAResource object
that can be used to work with the XA resource manager.

<pre><blockquote>
// Create an XA data source for making the XA connection.
AS400JDBCXADataSource xaDataSource = new AS400JDBCXADataSource("myAS400");
xaDataSource.setUser("myUser");
xaDataSource.setPassword("myPasswd");

// Get an XAConnection and get the associated XAResource.
// This provides access to the resource manager.
XAConnection xaConnection = xaDataSource.getXAConnection();
XAResource xaResource = xaConnection.getXAResource();

// ... work with the XA resource.

// Close the XA connection when done.  This implicitly
// closes the XA resource.
xaConnection.close();
</blockquote></pre>

@see AS400JDBCXAConnection
@see AS400JDBCXADataSource
**/
//
// Implementation note:
//
// 1.  Information from Fred Kulack:  Closing the XAConnection does not
//     affect the XAResource directly.  Only some of the methods in XAResource
//     affect the associated physical XA connection (start, end). The other calls,
//     (prepare/commit/rollback/recover, etc) can always be used on any XAResource
//     object, regardless of the state of the XAConnection from which it was created.
//     i.e. XAResources are NOT associated with transaction branches. Instead,
//     connections are. The XAResource is just the interface that you use to
//     control the transactions. Conceptually, a couple of the methods have sort of
//     an implicit parameter of the XAConnection from which it was created.
//
// 2.  Information from Randy Johnson:  A single connection cannot be 'multi-plexed'
//     between active transaction branches.  In other words, the following scenario
//     does not work:
//
//     xa_start XID 1 over connection A
//     xa_end XID 1 over connection A
//     xa_start XID 2 over connection A <-- this will fail because the connection
//                                          is still associated with the XID 1 transaction
//                                          branch.
//
//     That XID 1 transaction branch must be committed (xa_commit) or rolled back
//     (xa_rollback) before the connection can be used to start a new transaction branch.
//
// 3.  Information from Randy Johnson:  Multiple connections cannot be used to work
//     on the same transaction branch.  In other words, the following scenario does not
//     work:
//
//     xa_start XID 1 over connection A
//     xa_end XID 1 over connection A
//     xa_start XID 1 with TMJOIN over connection B <-- this will fail because you are
//                                                      trying to use 2 different connections
//                                                      to work on a single transaction branch.
//
//     JTA is currently living with these restrictions, but believe they will be unacceptable
//     sometime in the future (how far into the future is undetermined).  We are hoping to
//     solve this as part of Solomon, but there is no near term solution for either of
//     these.  The MTS support on the client will work around this by making sure all the
//     work for a transaction branch, including the XA API invocations, are sent over a
//     single Client Access-Host Server connection, regardless of which thread the application
//     issues its requests from, and over which logical SQL connection on the client the
//     requests are issued.  This is part of the function that they are porting from Toronto.
//
// 4.  Information from Randy Johnson:  I see that we are planning to reject ANY xa_start
//     request that specifies the TMRESUME or TMJOIN flags.  Likewise, we will reject ANY
//     xa_end request that specifies the TMSUSPEND flag.  In other words, a transaction
//     branch must consist only of:
//
//     xa_start
//     xa_end
//     xa_commit or xa_rollback
//
//     This means that the restrictions I outlined in my previous note are actually a
//     little more severe than I indicated before.  For example, both the following
//     scenarios will not work:
//
//     xa_start for XID 1 over connection A
//     xa_end with TMSUSPEND for XID 1 over connection A  <--rejected
//     xa_start with TMRESUME for XID 1 over connection A
//     xa_end for XID 1 over connection A
//     xa_commit or xa_rollback for XID 1
//
//     xa_start for XID 1 over connection A
//     xa_end for XID 1 over connection A
//     xa_start with TMJOIN for XID 1 over connection A <--rejected
//     xa_end for XID 1 over connection A
//     xa_commit or xa_rollback for XID 1
//
//     We could probably support these last 2 scenarios if it is deemed critical,
//     but we wouldn't be able to support the scenarios I laid out in the previous note.
//
public class AS400JDBCXAResource
implements XAResource
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
  private static int              COUNT_                          = 64;
  private static byte[]           DEFAULT_XA_INFORMATION_         = new byte[256];

  // Start the resource manager IDs at 0xC0001.  The server does not like 0.
  // Microsoft starts at 1.  CA ODBC will start with something else.  This will
  // enable us to quickly identify ours.
  private static int              nextResourceManagerID_          = 0xC001;
  private static Object           nextResourceManagerIDLock_      = new Object();

  private AS400JDBCConnection     connection_;
  // @A1D private boolean                 closed_                         = false;
  private int                     resourceManagerID_              = -1;
  private Xid                     started_                        = null;
  private JDTransactionManager    transactionManager_;
  private int                     transactionTimeout_             = 0;      //@K1A
  private int                     lockWait_                       = -1;      //@K1A


/**
Constructs an AS400JDBCXAResource object.

@param connection       The connection.

@exception XAException If an error occurs.
**/
  AS400JDBCXAResource(AS400JDBCConnection connection)
  throws XAException
  {
    connection_ = connection;
    transactionManager_ = connection_.getTransactionManager();

    synchronized(nextResourceManagerIDLock_)
    {
      resourceManagerID_ = nextResourceManagerID_++;
    }

    // @A1D open();
  }



/**
Closes the resource manager.

@exception XAException If an error occurs.
**/
  /* @A1D - The host server team says we should never need to call this.
  void close()
  throws XAException
  {
      try {
          if (JDTrace.isTraceOn()) {
              JDTrace.logInformation(this, "xa_close");
              JDTrace.logClose(this);
          }

          DBXARequestDS request = new DBXARequestDS(DBXARequestDS.REQUESTID_XA_CLOSE, 0,
                                                       DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
          request.setResourceManagerID(resourceManagerID_);
          request.setXAInformation(DEFAULT_XA_INFORMATION_);
          request.setFlags(TMNOFLAGS);

             DBReplyRequestedDS reply = connection_.sendAndReceive (request);
          processXAReturnCode(reply);

          closed_ = true;
       }
        catch (Exception e) {
          throwXAException(e);
       }
  }
  */






/**
Commits a global transaction.

@param xid          The global transaction identifier.
@param onePhase     true if the resource manager should use a one-phase
                    commit protocol to commit the work; false otherwise.

@exception XAException If an error occurs.
**/
  public void commit(Xid xid, boolean onePhase)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);

      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_commit");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_COMMIT, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(onePhase ? TMONEPHASE : TMNOFLAGS);

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }

      // Mark the transaction state.
      transactionManager_.markGlobalTransactionBoundary();
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
    }
  }



/**
Ends the work performed on behalf of a transaction branch.  The resource
manager disassociates the XA resource from the transaction branch
specified and lets the transaction be completed.

@param xid          The global transaction identifier.  This must correspond
                    to the global transaction identifier previously passed
                    to <a href="#start(javax.transaction.xa.Xid, int)">start()</a>.
@param flags        The flags.  Possible values are:
                    <ul>
                    <li>TMSUCCESS - The portion of work has completed
                        successfully.
                    <li>TMFAIL - The portion of work has failed.  The resource
                        manager may mark the transaction as rollback-only.
                    <li>TMSUSPEND - The transaction branch is temporarily
                        suspended in incomplete state.  The transaction
                        context is in suspend state and must be resumed
                        via <a href="#start(javax.transaction.xa.Xid, int)">start()</a> with TMRESUME.
                        (This is not currently supported for V5R2 and earlier versions.)
                    </ul>

@exception XAException If an error occurs.
**/
  public void end(Xid xid, int flags)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);
      if (started_ == null)
        throw new XAException(XAException.XAER_PROTO);
      if (!started_.equals(xid))
        throw new XAException(XAException.XAER_NOTA);
      if(connection_.getServerFunctionalLevel() < 11)
      {
          if ((flags != TMSUCCESS) && (flags != TMFAIL))
              throw new XAException(XAException.XAER_INVAL);
      }

      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_end");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_END, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(flags);

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }

      // Mark the transaction state.
      transactionManager_.setLocalTransaction(true);
      started_ = null;
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
    }
  }



/**
Closes the resource manager if not explicitly closed by the caller.

@exception   Throwable      If an error occurs.
**/
  /* @A1D
  protected void finalize()
      throws Throwable
  {
      if (!closed_)
          close();
      super.finalize();
  }
  */



/**
Tells the resource manager to forget about a heuristically completed
transaction branch.

@param xid          The global transaction identifier.

@exception XAException If an error occurs.
**/
//
// Implementation note:
//
// When a global transaction has been prepared, it stays around forever.
// Forget is what gets rid of it.
//
  public void forget(Xid xid)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);

      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_forget");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_FORGET, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(TMNOFLAGS);

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
    }
  }



/**
Returns the current transaction timeout value.

@return The current transaction timeout value.  

@exception XAException If an error occurs.
**/
  public int getTransactionTimeout()
  throws XAException
  {
    //@K1D return 0;
      return transactionTimeout_;
  }



/**
Indicates if the resource manager represented by this XA resource
is the same resource manager represented by the specified XA resource.

@param xaResource   The XA resource.
@return             true if both XA resources represent the same
                    resource manager, false otherwise.

@exception XAException If an error occurs.
**/
  public boolean isSameRM(XAResource xaResource)
  throws XAException
  {
    if (xaResource == null)
      return false;
    if (! (xaResource instanceof AS400JDBCXAResource))
      return false;
    return(((AS400JDBCXAResource)xaResource).resourceManagerID_ == resourceManagerID_);
  }



/**
Opens the resource manager.

@exception XAException If an error occurs.
**/
  /* @A1D - The host server team says we never need to call this.
    private void open()
    throws XAException
    {
        try {
            if (JDTrace.isTraceOn()) {
                JDTrace.logOpen(this);
                JDTrace.logInformation(this, "xa_open");
            }

            DBXARequestDS request = new DBXARequestDS(DBXARequestDS.REQUESTID_XA_OPEN, 0,
                                                         DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
            request.setResourceManagerID(resourceManagerID_);
            request.setXAInformation(DEFAULT_XA_INFORMATION_);
            request.setFlags(TMNOFLAGS);

               DBReplyRequestedDS reply = connection_.sendAndReceive (request);
            processXAReturnCode(reply);
         }
          catch (Exception e) {
            throwXAException(e);
         }
    }
    */



/**
Prepares for a transaction commit.

@param xid          The global transaction identifier.
@return             One of the following values:
                    <ul>
                    <li>XA_OK - The transaction work has been prepared
                        normally.
                    <li>XA_RDONLY - The transaction branch has been read-only
                        and has been committed.
                    </ul>

@exception XAException If an error occurs.
**/
//
// Implementation note:
//
// Once a global transaction has been prepared, it is around until it is committed,
// rolled back, or forgotten.
//
  public int prepare(Xid xid)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);

      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_prepare");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_PREPARE, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(TMNOFLAGS);

        reply = connection_.sendAndReceive (request);
        return processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
      return -1;
    }
  }



/**
Processes the XA return code.

@param reply        The reply data stream.
@return             The XA return code if error class is 0.

@exception          XAException If the error class is not 0.
**/
  private int processXAReturnCode(DBReplyRequestedDS reply)
  throws XAException
  {
    int errorClass = reply.getErrorClass();
    int returnValue = reply.getReturnCode();

    if (JDTrace.isTraceOn())
      JDTrace.logInformation(this, "xa error class = " + errorClass + ", return code = " + returnValue);

    if (returnValue < 0)
    {
      if (errorClass == 9)
        throw new XAException(returnValue);
      else if (errorClass != 0)
        throw new XAException(XAException.XAER_RMFAIL);
      else
        return returnValue;
    }
    else
      return returnValue;
  }



/**
Recovers a list of prepared transaction branches from the
resource manager.

@param flags        The flags.  Possible values are:
                    <ul>
                    <li>TMSTARTRSCAN - Start a recovery scan.
                    <li>TMENDRSCAN - End a recovery scan.
                    <li>TMNOFLAGS - No flags are set.
                    </ul>
@return             The global transaction identifiers for the
                    transaction branches that are currently in
                    a prepared or heuristically completed state.

@exception XAException If an error occurs.
**/
//
// This gives a list of all prepared global transactions.
//
  public Xid[] recover(int flags)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_recover");

      // Send the request.

      // We will return at most COUNT Xids.  It is up to the
      // caller to call us again if they want more.  Typically,
      // the first time they call us, they will pass TMSTARTRSCAN.
      // Subsequent calls, they will pass TMNOFLAGS.  The last time
      // they will pass TMENDRSCAN.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_RECOVER, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setCount(COUNT_);
        request.setFlags(flags);

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);

        DBReplyXids xids = reply.getXids();
        return xids.getXidArray();
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
      return null;
    }
  }



/**
Rolls back a transaction branch.

@param xid          The global transaction identifier.

@exception XAException If an error occurs.
**/
  public void rollback(Xid xid)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);

      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_rollback");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_ROLLBACK, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(TMNOFLAGS);

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }

      // Mark the transaction state.
      transactionManager_.markGlobalTransactionBoundary();
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
    }
  }



/**
Sets the current transaction timeout value.  This is not supported.

@param transactionTimeout   The current transaction timeout value in seconds,
                            or 0 to reset the timeout value to the default. The transaction timeout
                            will be set the next time start() is called.
@return                     true if the timeout value can be set successfully,
                            false if the resource manager does not support
                            the transaction timeout value to be set.  
@exception XAException If an error occurs.
**/
  public boolean setTransactionTimeout(int transactionTimeout)
  throws XAException
  {
      try
      {
          if(connection_.getVRM() < JDUtilities.vrm530)                          //@K1A
              return false;
      }
      catch(Exception e)
      {
          return false;
      }

      try
      {
          if(transactionTimeout_ < 0)                                           //@K1A
            JDError.throwSQLException(JDError.EXC_ATTRIBUTE_VALUE_INVALID);   //@K1A
      }
      catch(Exception e)
      {
      }
       
      transactionTimeout_ = transactionTimeout;     //@K1A
      return true;                                  //@K1A
  }



/**
Starts the work on behalf of a transaction branch.
The resource manager associates the XA resource from the transaction branch
specified.

@param xid          The global transaction identifier.
@param flags        The flags.  Possible values are:
                    <ul>
                    <li>TMJOIN - Joins a transaction previously seen by
                        the resource manager. 
                    <li>TMRESUME - Resumes a suspended transaction.
                        (This is not currently supported.)
                    <li>TMNOFLAGS - No flags are set.
                    </ul>

@exception XAException If an error occurs.
**/
  public void start(Xid xid, int flags)
  throws XAException
  {
    try
    {
      // Parameter validation.
      if (xid == null)
        throw new XAException(XAException.XAER_INVAL);
      if (started_ != null)
        throw new XAException(XAException.XAER_PROTO);
      if (flags != TMNOFLAGS && flags != TMJOIN)                //@K1C  added TMJOIN check
        throw new XAException(XAException.XAER_INVAL);


      if (JDTrace.isTraceOn())
        JDTrace.logInformation(this, "xa_start");

      // Send the request.
      //@P0C
      DBXARequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBXARequestDS(DBXARequestDS.REQUESTID_XA_START, 0,
                                            DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);
        request.setResourceManagerID(resourceManagerID_);
        request.setXid(AS400JDBCXid.xidToBytes(xid));
        request.setFlags(flags);

        if(connection_.getServerFunctionalLevel() >= 11)    //@KBA  server level must be version 11 or higher
        {
            request.setCtlTimeout(transactionTimeout_);
            if(lockWait_ != -1)
                request.setLockWait(lockWait_);
        }

        reply = connection_.sendAndReceive (request);
        processXAReturnCode(reply);
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }

      // Mark the transaction state.
      transactionManager_.setLocalTransaction(false);
      started_ = xid;
    }
    catch (XAException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throwXAException(e);
    }
  }

  //@K1A
  /**
  Specifies the number of seconds that the system will wait on any lock request during this transaction.
  
  @param lockWait The time in seconds to wait.
  **/
  public void setLockWait(int lockWait)
  throws SQLException
  {
      if(connection_.getVRM() < JDUtilities.vrm530)
          return;

      if(lockWait < 0)
          JDError.throwSQLException(JDError.EXC_ATTRIBUTE_VALUE_INVALID);

      lockWait_ = lockWait;
  }

  private void throwXAException(Exception e)
  throws XAException
  {
    if (JDTrace.isTraceOn())
    {
      synchronized(DriverManager.class)
      {
        e.printStackTrace(DriverManager.getLogStream ());
      }
    }
    throw new XAException(XAException.XAER_RMFAIL);
  }



/**
Returns the string representation of the XA resource.

@return The string representation.
**/
  public String toString()
  {
    return connection_.toString() + "-XA:RMID#" + resourceManagerID_;
  }
}
