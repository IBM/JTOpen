///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400ThreadedServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

final class AS400ThreadedServer extends AS400Server implements Runnable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private static int threadCount = 0;

  private AS400ImplRemote system_;
  private int service_;
  private String jobString_;

  private SocketContainer socket_;
  private InputStream inStream_;
  private OutputStream outStream_;

  private Hashtable replyStreams_;
  private Hashtable instanceReplyStreams_ = new Hashtable();

  private Thread readDaemon_ = null;
  private IOException readDaemonException_ = null;
  private RuntimeException unlikelyException_ = null; //@P0A

  private DataStream exchangeAttrReply_ = null;
  //@P0D private Vector replyList_ = new Vector(5);
  //@P0D private Vector discardList_ = new Vector();
  
  //@P0A - Vectors are slow, but Object arrays are big, so we 
  // implement our own hashtables to compromise.
  private final ReplyList replyList_ = new ReplyList();

  //@P1A
  private final class DataStreamCollection
  {
    DataStream[] chain_;
    DataStreamCollection(DataStream ds)
    {
      chain_ = new DataStream[] { ds };
    }
  };

  private final class ReplyList
  {
    final DataStreamCollection[] streams_ = new DataStreamCollection[16];

    final void add(DataStream ds)
    {
      int id = ds.getCorrelation();
      if (discardList_.remove(id))
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "ReplyList: Discarded datastream: ", id);
        if (ds instanceof DBReplyRequestedDS)
        {
          ((DBReplyRequestedDS)ds).inUse_ = false;
        }
        return;
      }
      int hash = ds.getCorrelation() % 16;
      
      //@P1C - Use the collection object for synchronization to prevent bottlenecks.
      DataStreamCollection coll = streams_[hash];
      if (coll == null)
      {
        streams_[hash] = new DataStreamCollection(ds);
        return;
      }
      synchronized(coll)
      {
        DataStream[] chain = coll.chain_;
          int max = chain.length;
          for (int i=0; i<max; ++i)
          {
            if (chain[i] == null)
            {
              chain[i] = ds;
              return;
            }
          }
          DataStream[] newChain = new DataStream[max*2];
          System.arraycopy(chain, 0, newChain, 0, max);
          newChain[max] = ds;
        coll.chain_ = newChain;
      }
    }
    
    final DataStream remove(int correlation)
    {
      int hash = correlation % 16;
      DataStreamCollection coll = streams_[hash];
      if (coll == null) return null;
      //@P1C - Use the collection object for synchronization to prevent bottlenecks.
      synchronized(coll)
      {
        DataStream[] chain = coll.chain_;
        for (int i=0; i<chain.length; ++i)
        {
          if (chain[i] != null && chain[i].getCorrelation() == correlation)
          {
            DataStream ds = chain[i];
            //@P1A  Move up the remaining entries because chained replies have the
            //@P1A  same correlation ID and need to remain in chronological order
            if (i+1 < chain.length)
            {
              System.arraycopy(chain, i+1, chain, i, chain.length-i-1);
            }
            //@P1A  Set last element to null.
            chain[chain.length-1] = null;
            return ds;
          }
        }
        return null;
      }
    }
  };

  
  private final DiscardList discardList_ = new DiscardList();

  private final class DiscardList
  {
    int[] ids_ = new int[8];

    final void add(int correlation)
    {
      DataStream ds = replyList_.remove(correlation);
      if (ds != null)
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DiscardList: Discarded datastream: ", correlation);
        if (ds instanceof DBReplyRequestedDS)
        {
          ((DBReplyRequestedDS)ds).inUse_ = false;
        }
        return;
      }
      synchronized(ids_)
      {
        int max = ids_.length;
        for (int i=0; i<max; ++i)
        {
          if (ids_[i] == 0)
          {
            ids_[i] = correlation; // We don't allow 0 as a valid correlation ID
            return;
          }
        }
        int[] newIds = new int[max*2];
        System.arraycopy(ids_, 0, newIds, 0, max);
        newIds[max] = correlation;
        ids_ = newIds;
      }
    }
    
    final boolean remove(int correlation)
    {
      int max = ids_.length;
      for (int i=0; i<ids_.length; ++i)
      {
        if (ids_[i] == correlation)
        {
          ids_[i] = 0;
          return true;
        }
      }
      return false;
    }
  }


  private int lastCorrelationId_ = 0;
  private final Object correlationIdLock_ = new Object(); //@P0C
  private final Object receiveLock_ = new Object(); //@P0C



  AS400ThreadedServer(AS400ImplRemote system, int service, SocketContainer socket, String jobString) throws IOException
  {
    system_ = system;
    service_ = service;
    jobString_ = jobString;

    socket_ = socket;
    inStream_  = socket.getInputStream();
    outStream_ = socket.getOutputStream();

    replyStreams_ = AS400Server.replyStreamsHashTables[service];

    readDaemon_ = new Thread(this, "AS400 Read Daemon-" + (++threadCount));
    readDaemon_.setDaemon(true);
    readDaemon_.start();
  }

  // Print is the only thing that uses this.
  final void addInstanceReplyStream(DataStream replyStream)
  {
    instanceReplyStreams_.put(replyStream, replyStream);
  }

  // Print is the only thing that uses this.
  final void clearInstanceReplyStreams()
  {
    instanceReplyStreams_.clear();
  }

  final void forceDisconnect()
  {
    if (readDaemonException_ == null)
    {
      readDaemonException_ = new ConnectionDroppedException(ConnectionDroppedException.DISCONNECT_RECEIVED);
    }

    if (service_ == AS400.DATABASE || service_ == AS400.COMMAND || service_ == AS400.CENTRAL)
    {
      AS400EndJobDS endjob = new AS400EndJobDS(AS400Server.getServerId(service_));
      try
      {
        endjob.write(outStream_);
      }
      catch (IOException e)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Send end job data stream failed.", e); //@P0C
      }
      catch(RuntimeException e) //@P0A
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Send end job data stream failed.", e); //@P0A
      }
    }

    readDaemon_.interrupt();

    try
    {
      socket_.close();
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Socket close failed.", e); //@P0C
    }
    catch(RuntimeException e) //@P0A
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Socket close failed.", e); //@P0A
    }
  }
  
  final DataStream getExchangeAttrReply()
  {
    return exchangeAttrReply_;
  }

  final String getJobString()
  {
    return jobString_;
  }

  final int getService()
  {
    return service_;
  }

  final boolean isConnected()
  {
    return readDaemonException_ == null && unlikelyException_ == null; //@P0C
  }

  final int newCorrelationId()
  {
    synchronized (correlationIdLock_)
    {
      if (++lastCorrelationId_ == 0) lastCorrelationId_ = 1; //@P0C - don't allow 0 as a valid correlation ID
      return lastCorrelationId_; //@P0C
    }
  }

  final DataStream receive(int correlationId) throws IOException, InterruptedException
  {
    //@P0C - changed this method
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "AS400Server.receive");
    synchronized (receiveLock_)
    {
      while(true)
      {
        DataStream ds = replyList_.remove(correlationId);
        if (ds != null)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "receive(): Valid reply found: " + correlationId);
          return ds;
        }
        else if (readDaemonException_ != null)
        {
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "receive(): Read daemon exception: ", readDaemonException_);
          throw readDaemonException_;
        }
        else if (unlikelyException_ != null)
        {
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "receive(): Read daemon exception: ", unlikelyException_);
          throw unlikelyException_;
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "receive(): Reply not found. Waiting...");
        receiveLock_.wait();
      }
    }
  }

  public void run()
  {
    //@P0C - changed this method
    while (readDaemonException_ == null && unlikelyException_ == null)
    {
      try
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Waiting for reply...");

        DataStream reply = null;

        // If client access server, construct ClientAccessDataStream.
        if (service_ != AS400.RECORDACCESS)
        {
          reply = ClientAccessDataStream.construct(inStream_, instanceReplyStreams_, replyStreams_, system_);
        }
        else  // Construct a DDMDataStream.
        {
          reply = DDMDataStream.construct(inStream_, replyStreams_, system_);
        }
        // Note: the thread is blocked on the above call if the inputStream has nothing to receive.
        
        int correlation = reply.getCorrelation();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Adding reply: ", correlation);
        replyList_.add(reply);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Notifying threads.");
        synchronized (receiveLock_)
        {
          receiveLock_.notifyAll();  // Notify all waiting threads.
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Threads notified.");
      }
      catch (IOException e)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "run(): Caught IOException: ", e);
        // At this point all waiting threads must be notified that the connection has failed...
        if (readDaemonException_ == null)
        {
          readDaemonException_ = e;
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Notifying threads after IOException.");
        synchronized (receiveLock_)
        {
          receiveLock_.notifyAll();  // Notify all waiting threads.
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Threads notified after IOException.");
      }
      catch(RuntimeException e)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "run(): Caught RuntimeException: ", e);
        if (unlikelyException_ == null)
        {
          unlikelyException_ = e;
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Notifying threads after RuntimeException.");
        synchronized(receiveLock_)
        {
          receiveLock_.notifyAll();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): Threads notified after RuntimeException.");
      }
    }
  }

  final int send(DataStream requestStream) throws IOException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send(): send request..."); //@P0C
    if (readDaemonException_ != null)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Read daemon generated exception:", readDaemonException_); //@P0C
      throw readDaemonException_;
    }
    if (unlikelyException_ != null) //@P0A
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Read daemon generated exception:", unlikelyException_); //@P0A
      throw unlikelyException_; //@P0A
    }
    int correlationID = newCorrelationId();
    requestStream.setCorrelation(correlationID);
    requestStream.write(outStream_);
    return correlationID;
  }

  final void send(DataStream requestStream, int correlationId) throws IOException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send(): send request..."); //@P0C
    if (readDaemonException_ != null)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Read daemon generated exception:", readDaemonException_); //@P0C
      throw readDaemonException_;
    }
    if (unlikelyException_ != null) //@P0A
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Read daemon generated exception:", unlikelyException_); //@P0A
      throw unlikelyException_; //@P0A
    }
    requestStream.setCorrelation(correlationId);
    requestStream.write(outStream_);
  }

  final void sendAndDiscardReply(DataStream requestStream) throws IOException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send and discard(): ..."); //@P0C
    int correlationID = send(requestStream);
    discardList_.add(correlationID); //@P0C
  }

  final DataStream sendAndReceive(DataStream requestStream) throws IOException, InterruptedException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send and receive(): ..."); //@P0C
    int correlationID = send(requestStream);
    return receive(correlationID);
  }

  final synchronized DataStream sendExchangeAttrRequest(DataStream req) throws IOException, InterruptedException
  {
    if (exchangeAttrReply_ == null)
    {
      exchangeAttrReply_ = sendAndReceive(req);
    }
    return exchangeAttrReply_;
  }
}
