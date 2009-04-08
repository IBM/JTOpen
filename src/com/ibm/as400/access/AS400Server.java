///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400Server.java
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

// A class representing an IBM i server job.
abstract class AS400Server
{
    protected int connectionID_;

    // Returns the connection ID for this AS400Server object.
    // @return  The connection ID.
    final int getConnectionID()
    {
      return connectionID_;
    }

    // Returns the service ID for a given service name.
    // @param  serviceName  The service name of the associated service job.
    // @return  The server ID for the given service name.
    static final int getServerId(String serviceName)
    {
        if ("as-central".equals(serviceName)) return 0xE000;
        if ("as-file".equals(serviceName)) return 0xE002;
        if ("as-netprt".equals(serviceName)) return 0xE003;
        // Note: the "as-database" service has 3 server Ids:
        //       0xE004 == SQL
        //       0xE005 == NDB
        //       0xE006 == ROI
        if ("as-database".equals(serviceName)) return 0xE004;
        if ("as-dtaq".equals(serviceName)) return 0xE007;
        if ("as-rmtcmd".equals(serviceName)) return 0xE008;
        if ("as-signon".equals(serviceName)) return 0xE009;

        Trace.log(Trace.ERROR, "Invalid service name: " + serviceName);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    // Returns the service ID for a given service constant.
    // param  service  The service constant of the associated service job.
    // return  The server ID of for the given service name.
    static final int getServerId(int service)
    {
        switch (service)
        {
            case AS400.CENTRAL:   return 0xE000;
            case AS400.FILE:      return 0xE002;
            case AS400.PRINT:     return 0xE003;
                // Note: the "as-database" service has 3 server Ids:
                //       0xE004 == SQL
                //       0xE005 == NDB
                //       0xE006 == ROI
            case AS400.DATABASE:  return 0xE004;
            case AS400.DATAQUEUE: return 0xE007;
            case AS400.COMMAND:   return 0xE008;
            case AS400.SIGNON:    return 0xE009;
        }
        Trace.log(Trace.ERROR, "Invalid service:", service);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    // Converts a service name into service ID.
    static int getServiceId(String serviceName)
    {
        if (serviceName.equals("as-file")) return AS400.FILE;
        if (serviceName.equals("as-netprt")) return AS400.PRINT;
        if (serviceName.equals("as-rmtcmd")) return AS400.COMMAND;
        if (serviceName.equals("as-dtaq")) return AS400.DATAQUEUE;
        if (serviceName.equals("as-ddm")) return AS400.RECORDACCESS;
        if (serviceName.equals("as-database")) return AS400.DATABASE;
        if (serviceName.equals("as-central")) return AS400.CENTRAL;
        if (serviceName.equals("as-signon")) return AS400.SIGNON;

        Trace.log(Trace.ERROR, "Invalid service: " + serviceName);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    // The following static array holds the reply streams hash tables for all server daemons.  These Hashtables are populated by the access classes using the addReplyStream(...) method.
    static Hashtable[] replyStreamsHashTables =  { new Hashtable(), new Hashtable(), new Hashtable(), new Hashtable(), new Hashtable(), new Hashtable(), new Hashtable(), new Hashtable() };

    // Add a prototype reply data stream to the collection of reply prototypes.  There must be a prototype reply for every type of reply that must be constructed automatically on receipt.  This method detects an attempt to add the same prototype reply more than once and ignores redundant attempts.
    // @param  replyStream  The prototype reply data stream to be added.
    // @param  serviceName  The service name of the server job that is the source of the reply streams.
    static void addReplyStream(DataStream replyStream, String serviceName)
    {
        addReplyStream(replyStream, AS400Server.getServiceId(serviceName));
    }
    static void addReplyStream(DataStream replyStream, int service)
    {
        replyStreamsHashTables[service].put(replyStream, replyStream);
    }


    abstract int getService();
    abstract String getJobString();
    abstract boolean isConnected();
    abstract DataStream getExchangeAttrReply();
    abstract DataStream sendExchangeAttrRequest(DataStream req) throws IOException, InterruptedException;
    abstract void addInstanceReplyStream(DataStream replyStream);
    abstract void clearInstanceReplyStreams();
    abstract DataStream sendAndReceive(DataStream requestStream) throws IOException, InterruptedException;
    abstract void sendAndDiscardReply(DataStream requestStream) throws IOException;
    abstract int send(DataStream requestStream) throws IOException;
    abstract int newCorrelationId();
    abstract void send(DataStream requestStream, int correlationId) throws IOException;
    abstract DataStream receive(int correlationId) throws IOException, InterruptedException;
    abstract void forceDisconnect();
}
