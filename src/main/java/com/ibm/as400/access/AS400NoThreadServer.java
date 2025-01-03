///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400NoThreadServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AS400NoThreadServer extends AS400Server
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private AS400ImplRemote system_;

    private Hashtable replyStreams_;
    private Hashtable instanceReplyStreams_ = new Hashtable();

    private DataStream exchangeAttrReply_ = null;
    private Vector replyList_ = new Vector(5);
    private Vector discardList_ = new Vector();
    private AtomicInteger lastCorrelationId_ = new AtomicInteger();

    private final Lock lock_ = new ReentrantLock();

    private boolean closed_ = false;

    AS400NoThreadServer(AS400ImplRemote system, int service, SocketContainer socket, String jobString) throws IOException
    {
        system_ = system;
        service_ = service;
        jobString_ = jobString;

        socket_ = socket;
        connectionID_ = socket_.hashCode();
        inStream_  = socket.getInputStream();
        outStream_ = socket.getOutputStream();

        replyStreams_ = AS400Server.replyStreamsHashTables[service];
    }
    
    AS400NoThreadServer(AS400ImplRemote system, int service) throws IOException
    {
        system_ = system;
        service_ = service;

        replyStreams_ = AS400Server.replyStreamsHashTables[service];
    }
    
    void setSocket(SocketContainer socket) throws IOException
    {
        try 
        {
            lock_.lock();
            
            if (socket_ != null)
            {
                try {
                    socket_.close();
                }
                catch (IOException e) {
                    Trace.log(Trace.ERROR, "Socket close of previous socket failed.", e);
                }
            }
            
            socket_ = socket;
            connectionID_ = socket_.hashCode();
            inStream_ = socket.getInputStream();
            outStream_ = socket.getOutputStream();
            
            closed_ = false;
        }
        finally {
            lock_.unlock();
        }
    }

    @Override
    boolean isConnected() {
        return closed_ == false;
    }

    @Override
    public DataStream getExchangeAttrReply() {
        return exchangeAttrReply_;
    }

    @Override
    public DataStream sendExchangeAttrRequest(DataStream req) throws IOException
    {
        if (exchangeAttrReply_ == null)
            exchangeAttrReply_ = sendAndReceive(req);

        return exchangeAttrReply_;
    }

    @Override
    void addInstanceReplyStream(DataStream replyStream) {
        instanceReplyStreams_.put(replyStream, replyStream);
    }

    @Override
    void clearInstanceReplyStreams() {
        instanceReplyStreams_.clear();
    }

    @Override
    public DataStream sendAndReceive(DataStream requestStream) throws IOException
    {
        try 
        {
            lock_.lock();

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send and receive(): ...");
            int correlationID = send(requestStream);
            return receive(correlationID);
        }
        finally {
            lock_.unlock();
        }
    }

    @Override
    void sendAndDiscardReply(DataStream requestStream) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send and discard(): ..."); //@pdc
        int correlationID = send(requestStream);
        discardList_.addElement(Integer.valueOf(correlationID));
    }

    @Override
    final void sendAndDiscardReply(DataStream requestStream,int correlationID) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "send and discard(): ...");
        send(requestStream,correlationID);
        discardList_.addElement(Integer.valueOf(correlationID));
    }
    
    @Override
    int send(DataStream requestStream) throws IOException
    {
        try 
        {
            lock_.lock();

            if (Trace.traceOn_) {
                Trace.log(Trace.DIAGNOSTIC, "send(): send request to job " + getJobString());
                requestStream.setConnectionID(connectionID_);
            }
            
            int correlationID = newCorrelationId();
            requestStream.setCorrelation(correlationID);
            requestStream.write(outStream_);
            return correlationID;
        }
        finally {
            lock_.unlock();
        }
    }

    @Override
    int newCorrelationId() {
        return lastCorrelationId_.incrementAndGet();
    }

    @Override
    void send(DataStream requestStream, int correlationId) throws IOException
    {
        try 
        {
            lock_.lock();

            if (Trace.traceOn_) {
                Trace.log(Trace.DIAGNOSTIC, "send(): send request to job " + getJobString());
                requestStream.setConnectionID(connectionID_);
            }

            requestStream.setCorrelation(correlationId);
            requestStream.write(outStream_);
        } 
        finally {
            lock_.unlock();
        }
    }

    @Override
    DataStream receive(int correlationId) throws IOException
    {
        try 
        {
            lock_.lock();

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "AS400Server receive from job " + getJobString());
            DataStream reply = null;
            do
            {
                if (!replyList_.isEmpty())
                {
                    for (int i = 0; i < replyList_.size(); i++)
                    {
                        DataStream nextReply = (DataStream)replyList_.elementAt(i);
                        if (nextReply.getCorrelation() == correlationId)
                        {
                            replyList_.removeElementAt(i);
                            reply = nextReply;
                            break;
                        }
                    }
                }

                if (reply == null)
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "run(): wait for reply..."); //@pdc
    
                    DataStream ds = null;
                    if (service_ != AS400.RECORDACCESS)
                        ds = ClientAccessDataStream.construct(inStream_, instanceReplyStreams_, replyStreams_, system_, connectionID_);
                    else
                        ds = DDMDataStream.construct(inStream_, replyStreams_, system_, connectionID_);
    
                    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "run(): reply received..." + ds.toString());
    
                    boolean keepDataStream = true;
                    int correlation = ds.getCorrelation();
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
                        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "adding reply...", correlation);
                        replyList_.addElement(ds); // Save off the reply.
                    }
                }
                else if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "received(): valid reply received...", correlationId);
            }
            while (reply == null);
            
            return reply;
        }
        finally {
            lock_.unlock();
        }
    }

    @Override
    void forceDisconnect()
    {        
        // Currently, referencing counting is only applicable to hostcnn server, since those can be shared across AS400 objects. 
        int count = removeReference();
        if (service_ == AS400.HOSTCNN && count > 0)
        {
            Trace.log(Trace.DIAGNOSTIC, "Force disconnect for hostcnn service ignored, reference count: " + count);
            return;
        }
        
        try
        {
            lock_.lock();

            if (closed_)
                return;
            
            closed_ = true;
            
            if (socket_ == null)
                return;
            
            if (service_ == AS400.DATABASE || service_ == AS400.COMMAND 
                    || service_ == AS400.CENTRAL || service_ == AS400.SIGNON || service_ == AS400.HOSTCNN)
            {
                AS400EndJobDS endjob = new AS400EndJobDS(AS400Server.getServerId(service_));
                try {
                    endjob.write(outStream_);
                }
                catch(Exception e) {
                    Trace.log(Trace.ERROR, "Send end job data stream failed.", e);
                }
            }
    
            try {
                socket_.close();
            }
            catch (IOException e) {
                Trace.log(Trace.ERROR, "Socket close failed.", e);
            }
        }
        finally {
            lock_.unlock();
        }
    }

    @Override
    public void setExchangeAttrReply(DataStream xChgAttrReply) {
        exchangeAttrReply_ = xChgAttrReply;
    }
    
    void lock() {
        lock_.lock();
    }
    
    void unlock() {
        lock_.unlock();
    }

    void markClosed() {
        closed_ = true;
    }
}
