///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ThreadedServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

class AS400ThreadedServer extends AS400Server implements Runnable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static int threadCount = 0;

    private AS400ImplRemote system_;
    private int service_;

    private SocketContainer socket_;
    private InputStream inStream_;
    private OutputStream outStream_;

    private Hashtable replyStreams_;
    private Hashtable instanceReplyStreams_ = new Hashtable();

    private Thread readDaemon_ = null;
    private IOException readDaemonException_ = null;

    private DataStream exchangeAttrReply_ = null;
    private Vector replyList_ = new Vector(5);
    private Vector discardList_ = new Vector();
    private int lastCorrelationId_ = 0;
    private Object correlationIdLock_ = new Object();
    private Object receiveLock_ = new Object();

    AS400ThreadedServer(AS400ImplRemote system, int service, SocketContainer socket) throws IOException
    {
        system_ = system;
        service_ = service;

        socket_ = socket;
        inStream_  = socket.getInputStream();
        outStream_ = socket.getOutputStream();

        replyStreams_ = AS400Server.replyStreamsHashTables[service];

        readDaemon_ = new Thread(this, "AS400 Read Daemon-" + (++threadCount));
        readDaemon_.setDaemon(true);
        readDaemon_.start();
    }

    int getService()
    {
        return service_;
    }

    boolean isConnected()
    {
        return readDaemonException_ == null;
    }

    DataStream getExchangeAttrReply()
    {
        return exchangeAttrReply_;
    }

    synchronized DataStream sendExchangeAttrRequest(DataStream req) throws IOException, InterruptedException
    {
        if (exchangeAttrReply_ == null)
        {
            exchangeAttrReply_ = sendAndReceive(req);
        }
        return exchangeAttrReply_;
    }

    void addInstanceReplyStream(DataStream replyStream)
    {
        instanceReplyStreams_.put(replyStream, replyStream);
    }

    void clearInstanceReplyStreams()
    {
        instanceReplyStreams_.clear();
    }

    DataStream sendAndReceive(DataStream requestStream) throws IOException, InterruptedException
    {
        Trace.log(Trace.DIAGNOSTIC, "send and receive(): ...");
        int correlationID = send(requestStream);
        return receive(correlationID);
    }

    void sendAndDiscardReply(DataStream requestStream) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "send and discard(): ...");
        int correlationID = send(requestStream);
        discardList_.addElement(new Integer(correlationID));
    }

    int send(DataStream requestStream) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "send(): send request...");
        if (readDaemonException_ != null)
        {
            Trace.log(Trace.ERROR, "Read daemon generated exception:", readDaemonException_);
            throw readDaemonException_;
        }
        int correlationID = newCorrelationId();
        requestStream.setCorrelation(correlationID);
        requestStream.write(outStream_);
        return correlationID;
    }

    int newCorrelationId()
    {
        synchronized (correlationIdLock_)
        {
            return ++lastCorrelationId_;
        }
    }

    void send(DataStream requestStream, int correlationId) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "send(): send request...");
        if (readDaemonException_ != null)
        {
            Trace.log(Trace.ERROR, "Read daemon generated exception:", readDaemonException_);
            throw readDaemonException_;
        }
        requestStream.setCorrelation(correlationId);
        requestStream.write(outStream_);
    }

    DataStream receive(int correlationId) throws IOException, InterruptedException
    {
        Trace.log(Trace.DIAGNOSTIC, "AS400Server.receive");
        synchronized (receiveLock_)
        {
            do
            {
                synchronized (replyList_)
                {
                    for (int i = 0; i < replyList_.size(); ++i)
                    {
                        DataStream nextReply = (DataStream)replyList_.elementAt(i);
                        if (nextReply.getCorrelation() == correlationId)
                        {
                            replyList_.removeElementAt(i);
                            Trace.log(Trace.DIAGNOSTIC, "received(): valid reply received...", correlationId);
                            return nextReply;
                        }
                    }
                }

                if (readDaemonException_ != null)
                {
                    Trace.log(Trace.ERROR, "Read daemon exception:", readDaemonException_);
                    throw readDaemonException_;
                }

                receiveLock_.wait();
            }
            while (true);
        }
    }

    public void run()
    {
        while (readDaemonException_ == null)
        {
            try
            {
                // First clean up any datastreams that are to be discarded.
                if (!discardList_.isEmpty())
                {
                    synchronized (replyList_)
                    {
                        DataStream ds;
                        for (int i = replyList_.size() - 1; i >= 0; i--)
                        {
                            ds = (DataStream)replyList_.elementAt(i);
                            int correlation = ds.getCorrelation();

                            for (int j = 0; j < discardList_.size(); j++)
                            {
                                if (((Integer)discardList_.elementAt(j)).intValue() == correlation)
                                {
                                    replyList_.removeElementAt(i);
                                    discardList_.removeElementAt(j);
                                    break;
                                }
                            }
                        }
                    }
                }

                Trace.log(Trace.DIAGNOSTIC, "run(): wait for reply...");

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
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "run(): reply received..." + reply.toString());

                // Should we discard this datastream  If yes, then don't put it into the reply list.
                boolean keepDataStream = true;
                int correlation = reply.getCorrelation();

                for (int i = 0; i < discardList_.size(); i++)
                {
                    if (((Integer)discardList_.elementAt(i)).intValue() == correlation)
                    {
                        discardList_.removeElementAt(i);
                        keepDataStream = false;
                        break;
                    }
                }

                if (keepDataStream)
                {
                    synchronized (replyList_)
                    {
                        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "adding reply...", reply.getCorrelation());
                        replyList_.addElement(reply);
                    }
                    Trace.log(Trace.DIAGNOSTIC, "run(): notifying threads");
                    synchronized (receiveLock_)
                    {
                        receiveLock_.notifyAll();  // Notify all waiting threads.
                    }
                    Trace.log(Trace.DIAGNOSTIC, "run(): threads notified");
                }
            }
            catch(IOException e)
            {
                Trace.log(Trace.ERROR, "run(): IOException", e);
                // At this point all waiting threads must be notified that the connection has failed...
                if (readDaemonException_ == null)
                {
                    readDaemonException_ = e;
                }
                synchronized (receiveLock_)
                {
                    receiveLock_.notifyAll();  // Notify all waiting threads.
                }
            }
        }
    }

    void forceDisconnect()
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
            catch(IOException e)
            {
                Trace.log(Trace.ERROR, "Send end job data stream failed.", e);
            }
        }

        readDaemon_.interrupt();

        try
        {
            socket_.close();
        }
        catch(IOException e)
        {
            Trace.log(Trace.ERROR, "Socket close failed.", e);
        }
    }
}
