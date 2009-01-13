///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSConnection.java
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
The PSConnection class represents a connection
to a client of the proxy server.
**/
class PSConnection
extends StoppableThread
{
    // Private data.
    private boolean                             closed_                 = false;
    private long                                connectionId_;
    private PxDSFactory                         factory_;
    private InputStream                         input_;
    private PSLoad                              load_;
    private OutputStream                        output_;
    private Socket                              socket_;
    private PrintWriter                         trace_;
    private PxTable                             proxyTable_;



/**
Constructs a PSConnection object.

@param connectionId     The connection id.
@param socket           The socket.
@param input            The input stream.                                                      
@param output           The output stream.                                                      
@param load             The load.
**/
    public PSConnection (long connectionId,
                                  Socket socket,
                                  InputStream input,
                                  OutputStream output,
                                  PSLoad load)
    {
        super("PSConnection-" + connectionId);

        connectionId_           = connectionId;
        socket_                 = socket;
        input_                  = input;
        load_                   = load;
        output_                 = output;
        proxyTable_             = new PxTable ();
                
        factory_ = new PxDSFactory ();        
        
        factory_.register (new PxByteParm ());
        factory_.register (new PxShortParm ());
        factory_.register (new PxIntParm ());
        factory_.register (new PxLongParm ());
        factory_.register (new PxFloatParm ());
        factory_.register (new PxDoubleParm ());
        factory_.register (new PxBooleanParm ());
        factory_.register (new PxCharParm ());
        factory_.register (new PxStringParm ());
        factory_.register (new PxSerializedObjectParm ());
        factory_.register (new PxPxObjectParm (proxyTable_));
        factory_.register (new PxToolboxObjectParm ());
        factory_.register (new PxNullParm ());
        factory_.register (new PxClassParm ());
        
        factory_.register (new PxConstructorReqSV (proxyTable_));
        factory_.register (new PxMethodReqSV (proxyTable_)); //@B2D, this));
        factory_.register (new PxFinalizeReqSV (proxyTable_));
        factory_.register (new PxListenerReqSV (this, proxyTable_));

        if (Trace.isTraceProxyOn ()) {
            trace_ = Trace.getPrintWriter();
            Trace.log (Trace.PROXY, "Px server connection " + this + " opened.");
        }
    }


                                 
    public void close ()
    {
        super.stopSafely();
        closed_ = true;

        if (Trace.isTraceProxyOn ())
            Trace.log (Trace.PROXY, "Px server connection " + this + " closed.");

        // I am using separate try-catch blocks to make sure 
        // that everything gets closed.
        // NOTE:  When using SSL, trying to close the output stream, input stream, and    //$B1A
        //        socket are more prone to throw an exception.  This is most likely due
        //        to the SSL design, which we cannot control.                           
        try {
            if (output_ != null)
               output_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Exception closing proxy output stream.", e);     //$B1C
        }

        try {
            if (input_ != null)
               input_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Exception closing proxy input stream.", e);      //$B1C
        }

        try {
            if (socket_ != null)
               socket_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Exception closing proxy socket.", e);            //$B1C
        }

        // Clean up for the connection.  Make sure and do this after closing
        // the input stream.  Otherwise I was seeing requests come across for
        // objects no longer in the proxy table.  That resulted in a NullPointerException.
        if (proxyTable_ != null) {
            proxyTable_.removeAll ();
            proxyTable_ = null;
        }
        factory_ = null;
        load_.connectionClosed ();

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
            if (((!(e instanceof SocketException))      // Normal Windows end.
                 && (!(e instanceof EOFException)))     // Normal AIX end.
                || ((e.getMessage () != null) 
                    && (e.getMessage ().indexOf ("JVM_recv") < 0)))
                Trace.log (Trace.ERROR, "IOException in PSConnection", e);

        if (! closed_)
            close ();
    }



/**
Processes a request and sends the reply, if any.
**/
    private void processReq (PxReqSV request)
    {     
        PxRepSV reply = request.process ();
        if (reply != null) {
            reply.setCorrelationId(request.getCorrelationId());
            send (reply);
        }
    }


/**
Processes requests until the connection is closed.
**/
    public void run()
    {
        load_.connectionOpened ();

        // Loop, continuously replying to requests.
        try {
            while (canContinue ()) {            

                // Get the next request.
                PxReqSV request = (PxReqSV) factory_.getNextDS (input_);
                if (Trace.isTraceProxyOn ()) {
                    trace_.print(getName() + " - ");
                    request.dump(trace_);
                }

                // If the request is asychronous, then process it in another thread.
                // Otherwise, run in in this thread.  Synchronous requests save the
                // overhead of creating a thread, but asynchronous requests are needed
                // in some cases to preserve semantics.  This will be most common
                // for potentially long-running method calls.
                if (request.isAsynchronous ()) {
                    final PxReqSV request2 = request;
                    (new Thread () { public void run () { processReq (request2); }}).start ();
                }
                else {
                    processReq (request);
                }
            }
        }
        catch (IOException e) {
            handleIOException (e);
        }        
    }



/**
Sends a reply.
**/
    public void send (PxRepSV reply)
    {        
        try {
            if (reply != null) {
                synchronized (output_) {
                    if (Trace.isTraceProxyOn ()) {
                        trace_.print(getName() + " - ");
                        reply.dump(trace_);
                    }
                    reply.writeTo(output_);
                    output_.flush();                       
                }
            }
        }
        catch (IOException e) {
            handleIOException (e);
        }
    }



/**
Stops the thread safely.
**/
    public void stopSafely ()
    {
        close ();
    }



}
