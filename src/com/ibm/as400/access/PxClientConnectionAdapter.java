///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxClientConnectionAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;



/**
The PxClientConnectionAdapter class represents the connection 
to a proxy server.  
**/
abstract class PxClientConnectionAdapter 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean                     closed_             = false;
    private InputStream                 input_              = null;
    private OutputStream                output_             = null;
    private PxClientReadDaemon          readDaemon_         = null;
    private PxSocketContainerAdapter    socket_             = null;
    
    
    
    protected PxClientConnectionAdapter (String proxyServer, boolean secure)
    {
        open (proxyServer, secure);
    }

          

/**
Closes the connection to the proxy server.
**/
    public void close ()
    {
        if (Trace.isTraceProxyOn ())
            Trace.log (Trace.PROXY, "Closing a connection to proxy server.");
            
        readDaemon_.stopSafely();

        // I am using separate try-catch blocks to make sure 
        // that everything gets closed.
        try {
            input_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new ProxyException (ProxyException.CONNECTION_DROPPED);
        }

        try {
            output_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new ProxyException (ProxyException.CONNECTION_DROPPED);
        }

        try {
            socket_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new ProxyException (ProxyException.CONNECTION_DROPPED);
        }

        closed_ = true;
    }



    protected void finalize ()
        throws Throwable
    {
        if (closed_ == false)
            close ();
        super.finalize ();
    }



    public PxDSFactory getFactory()
    {
        return readDaemon_.getFactory();
    }


    public void open (String proxyServer, boolean secure)
    {                
        if (Trace.isTraceProxyOn ())
            Trace.log (Trace.PROXY, "Opening a connection to proxy server " 
                       + proxyServer + " (secure=" + secure + ").");
            
        // Parse the proxy server name and port number.
        String name;
        int port = -1;
        int colon = proxyServer.indexOf(':');
        if (colon < 0)
            name = proxyServer;        
        else {
            name = proxyServer.substring(0, colon);
            if (colon < proxyServer.length() - 1)
                port = Integer.parseInt(proxyServer.substring (colon + 1));
        }

        if (port < 0)
            port = /* secure ? ProxyConstants.SECURE_PORT_NUMBER :*/ ProxyConstants.PORT_NUMBER;

        // Open the socket and streams.
        try {
            if (secure)
                socket_ = new PxSecureSocketContainer (name, port);
            else
                socket_ = new PxSocketContainer (name, port);
            output_     = new BufferedOutputStream (socket_.getOutputStream());
            input_      = new BufferedInputStream(new RetryInputStream(socket_.getInputStream())); // @A2C

            readDaemon_ = new PxClientReadDaemon(input_);
            readDaemon_.start();

            if (Trace.isTraceProxyOn ())
                Trace.log (Trace.PROXY, "Connection established.");
        }
        catch (IOException e) {  
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
        } 
    }

          

    // Note: This method should NOT be synchronized.  If a thread is waiting to         // @A1A
    // receive a reply, it should not block other threads from sending requests.        // @A1A
    // (This is the point of the read daemon thread.)                                   // @A1A
    private Object receive (long correlationId)                                         // @A1C
        throws InvocationTargetException
    {
        Object returnValue;
        PxRepCV reply;
        try {
            reply = (PxRepCV) readDaemon_.getReply(correlationId);
            returnValue = reply.process ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when receiving reply from proxy server", e);
            throw new ProxyException (ProxyException.CONNECTION_DROPPED);
        }

        return returnValue;
    }



/**
Sends a request to the proxy server.  No reply is expected.

@param request  The request.
**/
    protected synchronized void send (PxReqCV request)
    {
        if (Trace.isTraceProxyOn())
            request.dump (Trace.getPrintWriter ());

        try {
            request.writeTo (output_);
            output_.flush ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new ProxyException (ProxyException.CONNECTION_DROPPED);
        }
    }



/**
Sends a request to the proxy server and receives and processes a reply.

@param request  The request.
@return         The returned object, or null if none.
**/
    // Note: This method should NOT be synchronized.  If a thread is waiting to         // @A1A
    // receive a reply, it should not block other threads from sending requests.        // @A1A
    // (This is the point of the read daemon thread.)                                   // @A1A
    protected Object sendAndReceive (PxReqCV request)                                   // @A1C
        throws InvocationTargetException
    {        
        send (request);
        return receive (request.getCorrelationId());
    }



}

