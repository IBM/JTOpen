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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

class AS400NoThreadServer extends AS400Server
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private AS400ImplRemote system_;
    private int service_;
    private String jobString_;

    private SocketContainer socket_;
    private InputStream inStream_;
    private OutputStream outStream_;

    private Hashtable replyStreams_;
    private Hashtable instanceReplyStreams_ = new Hashtable();

    private DataStream exchangeAttrReply_ = null;
    private Vector replyList_ = new Vector(5);
    private Vector discardList_ = new Vector();
    private int lastCorrelationId_ = 0;
    private Object correlationIdLock_ = new Object();

    private boolean closed_ = false;

    AS400NoThreadServer(AS400ImplRemote system, int service, SocketContainer socket, String jobString) throws IOException
    {
        system_ = system;
        service_ = service;
        jobString_ = jobString;

        socket_ = socket;
        inStream_  = socket.getInputStream();
        outStream_ = socket.getOutputStream();

        replyStreams_ = AS400Server.replyStreamsHashTables[service];
    }

    int getService()
    {
        return service_;
    }

    String getJobString()
    {
        return jobString_;
    }

    boolean isConnected()
    {
        return closed_ == false;
    }

    DataStream getExchangeAttrReply()
    {
        return exchangeAttrReply_;
    }

    synchronized DataStream sendExchangeAttrRequest(DataStream req) throws IOException
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

    DataStream sendAndReceive(DataStream requestStream) throws IOException
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
        int correlationID = newCorrelationId();
        requestStream.setCorrelation(correlationID);
        requestStream.write(outStream_);
        return correlationID;
    }

    int newCorrelationId()
    {
        synchronized (correlationIdLock_)
        {
          if (++lastCorrelationId_ == 0) lastCorrelationId_ = 1; //@P0C
          return lastCorrelationId_; //@P0C
        }
    }

    void send(DataStream requestStream, int correlationId) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "send(): send request...");
        requestStream.setCorrelation(correlationId);
        requestStream.write(outStream_);
    }

    synchronized DataStream receive(int correlationId) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "AS400Server receive");
        DataStream reply = null;
        do
        {
            synchronized (replyList_)
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
            }

            if (reply == null)
            {
                Trace.log(Trace.DIAGNOSTIC, "run(): wait for reply...");

                DataStream ds = null;
                if (service_ != AS400.RECORDACCESS)
                {
                    ds = ClientAccessDataStream.construct(inStream_, instanceReplyStreams_, replyStreams_, system_);
                }
                else
                {
                    ds = DDMDataStream.construct(inStream_, replyStreams_, system_);
                }

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
            else
            {
                Trace.log(Trace.DIAGNOSTIC, "received(): valid reply received...", correlationId);
            }
        }
        while (reply == null);
        return reply;
    }

    void forceDisconnect()
    {
        closed_ = true;
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

        try
        {
            socket_.close();
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Socket close failed.", e);
        }
    }
}
