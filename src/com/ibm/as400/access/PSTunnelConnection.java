///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSTunnelConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


/**
The PSTunnelConnection class represents a tunnel connection
to a client of the proxy server.
**/
class PSTunnelConnection
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



     // Private data.
     private boolean                             closed_                 = false;
     private long                                connectionId_;
     private PrintWriter                         trace_;


     /**
     Constructs a PSConnection object for tunneling.

     @param connectionId     The connection id.
     **/
     public PSTunnelConnection (long connectionId)
     {
          connectionId_           = connectionId;

	  if (Trace.isTraceProxyOn ()) {
               trace_ = Trace.getPrintWriter();
               Trace.log (Trace.PROXY, "Px server tunnel connection " + this + " opened.");
          }
     }



     public void close ()
     {
          closed_ = true;

          if (Trace.isTraceProxyOn ())
               Trace.log (Trace.PROXY, "Px server tunnel connection " + this + " closed.");

          Verbose.println (ResourceBundleLoader.getText ("PROXY_CONNECTION_CLOSED", Long.toString (connectionId_)));
     }



     protected void finalize ()
     throws Throwable
     {
          if (! closed_)
               close ();
          super.finalize ();
     }



     /**
     Handles IOExceptions that are thrown while replying
     to requests.
     **/
     private void handleIOException (IOException e)
     {
          // This exception is thrown when the client application ends
          // normally.  It is also thrown on certain error conditions.
          // We need to trace the error conditions, but weed out the
          // normal ending.
          if (Trace.isTraceErrorOn ())
               if (((!(e instanceof SocketException))        // Normal Windows end.
                     && (!(e instanceof EOFException)))                // Normal AIX end.
                    || ((e.getMessage () != null)
                         && (e.getMessage ().indexOf ("JVM_recv") < 0)))
                    Trace.log (Trace.ERROR, "IOException in PSConnection", e);

          if (! closed_)
               close ();
     }


     /**
     Processes a single request and sends the reply, if any, using tunneling.
     **/
     public void runRequest (PxReqSV request, OutputStream outputStream)
     {
          PxRepSV reply = request.process ();
          if (reply != null)
          {
               reply.setCorrelationId(request.getCorrelationId());
               reply.setClientId(request.getClientId());
               sendTunneling (reply, outputStream);
          }

     }

     /**
     Sends a reply.
     **/
     public void sendTunneling (PxRepSV reply, OutputStream outputStream)
     {
          try
          {
               if (reply != null)
               {
                    synchronized (outputStream) {
                         if (Trace.isTraceProxyOn ())
                         {
                              reply.dump(trace_);
                         }
                         reply.writeTo(outputStream);
                         outputStream.flush();
                    }
               }
          }
          catch (IOException e)
          {
               handleIOException (e);
          }
     }
}
