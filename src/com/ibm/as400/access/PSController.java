///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PSController.java
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
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;



/**
The PSController class represents a connection
to a client of the proxy server.
**/
class PSController
extends StoppableThread 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static long                         nextConnectionId_       = 1000;
    private static Object                       nextConnectionIdLock_   = new Object ();

    private boolean                             closed_                 = false;
    private PSConfig                            config_;
    private Socket                              connectedSocket_;     
    private long                                connectionId_           = -1;
    private PxDSFactory                         factory_;
    private InputStream                         input_;
    private boolean                             ownSocket_              = false;
    private OutputStream                        output_;
    private boolean                             running_                = false;
    private PSServerSocketContainerAdapter      serverSocket_;
    private Vector                              threadGroup_;



/**
Constructs a PSController object.

@param threadGroup      The thread group.                               
@param proxyServer      The proxy server.                                  
@param load             The load.
@param loadBalancer     The load balancer.
@param config           The configuration.
@param serverSocket     The server socket container.
**/
    public PSController (Vector threadGroup,
                                  ProxyServer proxyServer,
                                  PSLoad load,
                                  PSLoadBalancer loadBalancer,
                                  PSConfig config,
                                  PSServerSocketContainerAdapter serverSocket)
    {
        super("PSController-" + serverSocket);

        threadGroup_    = threadGroup;
        config_         = config;
        serverSocket_   = serverSocket;

        factory_ = new PxDSFactory ();                   
        factory_.register (new PxBooleanParm ());
        factory_.register (new PxIntParm ());
        factory_.register (new PxStringParm ());           
        factory_.register (new PxSerializedObjectParm (null));          
        factory_.register (new PxConnectReqSV (threadGroup, this, load, loadBalancer, serverSocket.isSecure ()));
        factory_.register (new PxConfigReqSV (config, this));
        factory_.register (new PxEndReqSV (proxyServer, this));
        factory_.register (new PxLoadReqSV (load));       

        if (Trace.isTraceProxyOn ())
            Trace.log (Trace.PROXY, "Px server controller " + this + " opened.");
    }



    public void closeServerSocket ()
    {
        if (Trace.isTraceProxyOn ())
            Trace.log (Trace.PROXY, "Px server controller " + this + " closed.");

        closeSocket();

        try {
            serverSocket_.close ();
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
        }

        closed_ = true;
    }



    public void closeSocket()
    {
        if (ownSocket_) {
            try {
                if (input_ != null)
                    input_.close ();
            }
            catch (IOException e) {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, e.getMessage (), e);
            }
    
            try {
                if (output_ != null)
                    output_.close ();
            }
            catch (IOException e) {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, e.getMessage (), e);
            }        

            try {
                if (connectedSocket_ != null)
                    connectedSocket_.close ();
            }
            catch (IOException e) {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, e.getMessage (), e);
            }        

            ownSocket_ = false;
        }
    }


    protected void finalize ()
        throws Throwable
    {
        if (closed_ == false)
            closeServerSocket();
        super.finalize ();
    }


/**
Returns the current requesting client's address.

@return The current requesting client's address.
**/
    public InetAddress getClientAddress ()
    {                               
        return connectedSocket_.getInetAddress ();
    }


                           
/**
Returns the current requesting client's socket.

@return The current requesting client's socket.
**/
    public Socket getConnectedSocket ()
    {                         
        // Give up ownership of the socket.  A connection
        // owns it now.
        ownSocket_ = false;

        return connectedSocket_;
    }


                           
/**
Returns the current requesting client's unique connection id.

@return The current requesting client's unique connection id.
**/
    public long getConnectionId ()
    {
        return connectionId_;
    }



/**
Returns the input stream used for receiving requests from
the current client.

@return The input stream used for receiving requests from
the current client.
**/
    public InputStream getInputStream ()
    {
        return input_;
    }



/**
Returns the output stream used for sending replies to
the current client.

@return The output stream used for sending replies to 
the current client.
**/
    public OutputStream getOutputStream ()
    {
        return output_;
    }




/**
Runs the controller.
**/
    public void run ()
    {
        running_ = true;

        // Loop forever, handling each connection that comes in.
        while (canContinue ()) {

            // If anything goes wrong here, stop the controller.
            try {
                connectedSocket_ = serverSocket_.accept();               
                // Test note: We see the phrase "Address in use: bind"
                // on occasion when calling this method.  My best guess
                // is that the JDK is printing it!
            }
            catch(Exception e) {
                Verbose.println (e);
                if (Trace.isTraceErrorOn ())
                    e.printStackTrace (Trace.getPrintWriter ());
                break;
            }

            // From here on out, if anything goes wrong, we just loop and try again!
            try {               
                input_ = new BufferedInputStream (connectedSocket_.getInputStream());
                output_ = new BufferedOutputStream (connectedSocket_.getOutputStream());

                // For now, this class "owns" the socket... at least
                // until a connection gets it for its own use.
                ownSocket_ = true;
                
                synchronized (nextConnectionIdLock_) {
                    connectionId_ = ++nextConnectionId_;
                }

                // Get the next request.
                PxReqSV request = (PxReqSV) factory_.getNextDS (input_);
                if (Trace.isTraceProxyOn ())
                    request.dump (Trace.getPrintWriter ());

                // Process the request and return the reply, if any.
                PxRepSV reply = (PxRepSV) request.process ();
                if (reply != null) {
                    reply.setCorrelationId(request.getCorrelationId());
                    synchronized (output_) {
                        if (Trace.isTraceProxyOn ())
                            reply.dump (Trace.getPrintWriter ());
                        reply.writeTo(output_);
                        output_.flush();
                    }
                }
            }
            catch (Exception e) {
                Verbose.println (e);
                if (Trace.isTraceErrorOn ())
                    e.printStackTrace (Trace.getPrintWriter ());
            }    
            finally {
                closeSocket();
            }
        }

        running_ = false;
    }



/**
Stops the thread safely.
**/
    public void stopSafely ()
    {
        super.stopSafely();

        // Close the sockets, etc.
        closeServerSocket();

        // Wait for controller loop to finish.  This verifies that the
        // socket was finally closed.  (On some platforms (e.g. Windows)
        // it seems like the close does not take full effect until a few
        // seconds after close() is called.
        try {
            while (running_)
                Thread.sleep(500);
        }
        catch(InterruptedException e) {
            // Ignore.
        }

        Verbose.println(ResourceBundleLoader.getText("PROXY_SERVER_ENDED", serverSocket_));
    }



    public String toString()
    {
        return serverSocket_.toString();
    }


}
