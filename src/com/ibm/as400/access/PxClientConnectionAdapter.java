///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

//
// Tunneling is another mechanism to get to the server.  This class now
// has two ways to get to the server:
//    Traditional -- a socket connection is made to the server and a
//    deamon thread is started to handly data coming back on the connection.
//    Tunnel -- A URL connection is made for each data flow.  The connection
//    is made to our tunnel servelet running in the http server.
//


/**
The PxClientConnectionAdapter class represents the connection
to a proxy server.
**/
abstract class PxClientConnectionAdapter
{
  private static final String copyright = "Copyrixght (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean                     closed_             = false;
    private InputStream                 input_              = null;
    private OutputStream                output_             = null;
    private PxClientReadDaemon          readDaemon_         = null;
    private PxSocketContainerAdapter    socket_             = null;
    private SSLOptions                  sslOptions_         = null;

                                                                    // tunnel_ is used by the
                                                                    // subclass ProxyClientConnection
                                                                    // so it cannot be private
            boolean                     tunnel_             = false;           // @D1a
    private long                        clientId_           = -1;              // @D1a @D2C
    private URL                         tunnelURL_          = null;            // @D1a


    protected PxClientConnectionAdapter (String proxyServer, SSLOptions secure)
    {
        // @D2D clientId_ = new byte[8];
        sslOptions_ = secure;
        open (proxyServer);
    }



/**
Closes the connection to the proxy server.
      // @D1a question -- should we do something in the tunneling case
      //      to clean up the server?
**/
    public void close ()
    {
        if (!tunnel_)                                                          // @D1a
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

           // $$ Question for Jim, his new class skipped the above
           //    two funtions -- input_.close() and output_.close().
           //    If a good idea then remove from here as well.
           try {
               socket_.close ();
           }
           catch (IOException e) {
               if (Trace.isTraceErrorOn ())
                   Trace.log (Trace.ERROR, e.getMessage (), e);
               throw new ProxyException (ProxyException.CONNECTION_DROPPED);
           }
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


    public void open (String proxyServer)
    {
        boolean secure = (sslOptions_ != null && sslOptions_.proxyEncryptionMode_ != SecureAS400.PROXY_SERVER_TO_SERVER);

        if (Trace.isTraceOn()) Trace.log(Trace.PROXY, "Opening a connection to proxy server "
                                                     + proxyServer
                                                     + " (secure=" + secure + ").");


        // Parse the proxy server name, port number (and protocol if tunneling)
        String localName    = proxyServer;
        String protocolName = null;

        int port = -1;

        // determine if we are going with traditional or tunnel proxy.
        // Assume any string with a :// wants to use the tunnel.  This would
        // be http://, https://, etc.
        if (proxyServer.indexOf("://") > 0)                                    // @D1a
        {
           tunnel_ = true;
           // the name of the server is everything beyond the ://
           localName    = proxyServer.substring(proxyServer.indexOf(":") + 3);
           protocolName = proxyServer.substring(0, proxyServer.indexOf(":"));
        }

        // now strip the port of the end of the server name (if one exists)
        int colon = localName.indexOf(':');
        if (colon >= 0)
        {
            if (colon < localName.length() - 1)
                port = Integer.parseInt(localName.substring (colon + 1));
            localName = localName.substring(0, colon);
        }


        if (! tunnel_)                                                         // @D1a
        {
            if (port < 0)
               port =  secure ? ProxyConstants.SECURE_PORT_NUMBER : ProxyConstants.PORT_NUMBER;       //$B1C
            openTraditional(localName, port, secure);
        }
        else
        {
           // when openTunnel comes back move creating tunnelURL_ to the try/catch.
           openTunnel(protocolName, localName, port);                                                         // @D1a
        }
    }

    // this method used to be part of open.  It was split out when
    // tunneling was added.
    void openTraditional(String name, int port, boolean secure)                                // @D1a
    {
        // Open the socket and streams.
        try {
            if (secure) {
            	// Call view reflection to remove dependency on sslight.zip
            	Class classPxSecureSocketContainer = Class.forName("com.ibm.as400.access.PxSecureSocketContainer");
            	Class[] parameterTypes = new Class[3]; 
            	parameterTypes[0] = "".getClass(); 
            	parameterTypes[1] = Integer.TYPE; 
            	parameterTypes[2] = Class.forName("com.ibm.as400.access.SSLOptions"); 
            	Constructor constructor = classPxSecureSocketContainer.getConstructor(parameterTypes);
            	Object[] initargs = new Object[3]; 
            	initargs[0] = name; 
            	initargs[1] = new Integer(port); 
            	initargs[2] = sslOptions_; 
                socket_ = (PxSocketContainerAdapter) constructor.newInstance(initargs);
            } else
                socket_ = new PxSocketContainer (name, port);
            output_     = new BufferedOutputStream (socket_.getOutputStream());
            input_      = new BufferedInputStream(new RetryInputStream(socket_.getInputStream())); // @A2C

            readDaemon_ = new PxClientReadDaemon(input_);
            readDaemon_.start();

            if (Trace.isTraceProxyOn ())
                Trace.log (Trace.PROXY, "Connection established.");
        }
        catch (ClassNotFoundException e) { 
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (ClassNotFound", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
        }
        catch (NoSuchMethodException e) { 
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (NoSuchMethodException", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
        }
        catch (IllegalAccessException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (IllegalAccessException", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
        }
        catch (IOException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (openio", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
        } catch (IllegalArgumentException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (IllegalArgumentException", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
		} catch (InstantiationException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (InstantiationException", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
		} catch (InvocationTargetException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server (InvocationTargetException", e);
            throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
		}
    }


    // @D1a New method
    void openTunnel(String protocol, String name, int port)
    {
       try
       {
          readDaemon_ = new PxClientReadDaemon();
          readDaemon_.register(new PxAcceptRepCV());

          if (port < 0)
             tunnelURL_ = new URL(protocol, name, "/servlet/com.ibm.as400.access.TunnelProxyServer");
          else
             tunnelURL_ = new URL(protocol, name, port, "/servlet/com.ibm.as400.access.TunnelProxyServer");
       }
       catch (IOException e)
       {
          if (Trace.isTraceErrorOn ())
              Trace.log (Trace.ERROR, "Error when opening connection to proxy server", e);
          throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
       }
    }


    // @D1a New method
    private URLConnection tunnelSend(PxReqCV request)
    {
       try
       {
          URLConnection connection_;

          // @D2D if (clientId_ == null)
          // @D2D    request.setClientId(new byte[8]);
          // @D2D else
             request.setClientId(clientId_);

          // connection_ = (HttpURLConnection) tunnelURL_.openConnection();
          connection_ = tunnelURL_.openConnection();

          connection_.setUseCaches(false);
          connection_.setDoOutput(true);
          connection_.setDoInput(true);

          connection_.setRequestProperty("Content-type", "application/octet-stream");
          connection_.setRequestProperty("Connection",   "Keep-Alive");
          // connection_.setRequestMethod("POST");
          // connection_.setFollowRedirects(false);
          // connection.setRequestProperty("Content-length", " " + bytes.length)

          // connection_.connect();
          OutputStream connectionOut = connection_.getOutputStream();

          if (Trace.isTraceProxyOn())
              request.dump (Trace.getPrintWriter ());

          request.writeTo(connectionOut);
          connectionOut.flush();
          // connectionOut.close();

          return connection_;
       }
       catch (Exception e)
       {
          if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error when opening connection to proxy server", e);
             throw new ProxyException (ProxyException.CONNECTION_NOT_ESTABLISHED);
       }
    }



   Object tunnelReceive(long correlationId, URLConnection connection_)
        throws InvocationTargetException
   {
      try
      {
          InputStream connectionIn = connection_.getInputStream();

          PxRepCV reply;
          Object returnValue;
          try
          {
             reply = (PxRepCV) readDaemon_.getReply(correlationId, connectionIn);
             returnValue = reply.process ();
             clientId_ = reply.getClientId();
             return returnValue;
          }
          catch (IOException e) {
             if (Trace.isTraceErrorOn ())
                 Trace.log (Trace.ERROR, "Error when receiving reply from proxy server", e);
             throw new ProxyException (ProxyException.CONNECTION_DROPPED);
          }
      }
      catch (InvocationTargetException ite)
      {
         throw ite;
      }
      catch (Exception e)
      {
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
        Object returnValue = null;

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

        if (! tunnel_)                                                     // @D1a
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
        else
        {
           URLConnection connection_ = tunnelSend(request);                // @D1a
           // $$1 do something with the connection
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
        if (! tunnel_)                                                     // @D1a
        {
           send (request);
           return receive (request.getCorrelationId());
        }
        else                                                               // @D1a
        {
           // cannot use HttpURLConnection in IE or Netscape so use URLConnection instead

           URLConnection connection_ = tunnelSend(request);                // @D1a
           Object o = tunnelReceive(request.getCorrelationId(), connection_);  // @D1a
           // return tunnelReceive(request.getCorrelationId(), connection_);  // @D1a
           return o;
        }                                                                  // @D1a
    }


}

