///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  PortMapper.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1998-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.net.InetSocketAddress;

class PortMapper
{
    private PortMapper()
    {
    }

    private static Hashtable systemList = new Hashtable();

    static void setServicePortsToDefault(String systemName)
    {
        int[] newPortList =
        {
            8473, // 0 File.
            8474, // 1 Print.
            8475, // 2 Command.
            8472, // 3 Data Queue.
            8471, // 4 Database.
            446,  // 5 Record Level Access.
            8470, // 6 Central.
            8476, // 7 Sign-on.
            9473, // 8 Secure File.
            9474, // 9 Secure Print.
            9475, // 10 Secure Command.
            9472, // 11 Secure Data Queue.
            9471, // 12 Secure Database.
            448,  // 13 Secure Record Level Access.
            9470, // 14 Secure Central.
            9476  // 15 Secure Sign-on.
        };
        systemList.put(systemName, newPortList);
    }

    static void setServicePort(String systemName, int service, int port, SSLOptions useSSL)
    {
        if (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER) service += 8;
        int[] portList = (int[])systemList.get(systemName);
        if (portList == null)
        {
            int[] newPortList =
            {
                AS400.USE_PORT_MAPPER, // 0 File.
                AS400.USE_PORT_MAPPER, // 1 Print.
                AS400.USE_PORT_MAPPER, // 2 Command.
                AS400.USE_PORT_MAPPER, // 3 Data Queue.
                AS400.USE_PORT_MAPPER, // 4 Database.
                446,                   // 5 Record Level Access.
                AS400.USE_PORT_MAPPER, // 6 Central.
                AS400.USE_PORT_MAPPER, // 7 Sign-on.
                AS400.USE_PORT_MAPPER, // 8 Secure File.
                AS400.USE_PORT_MAPPER, // 9 Secure Print.
                AS400.USE_PORT_MAPPER, // 10 Secure Command.
                AS400.USE_PORT_MAPPER, // 11 Secure Data Queue.
                AS400.USE_PORT_MAPPER, // 12 Secure Database.
                448,                   // 13 Secure Record Level Access.
                AS400.USE_PORT_MAPPER, // 14 Secure Central.
                AS400.USE_PORT_MAPPER  // 15 Secure Sign-on.
            };
            newPortList[service] = port;
            systemList.put(systemName, newPortList);
        }
        else
        {
            portList[service] = port;
        }
    }

    static int getServicePort(String systemName, int service, SSLOptions useSSL)
    {
        if (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER) service += 8;
        int[] portList = (int[])systemList.get(systemName);
        if (portList == null)
        {
            if (service == AS400.RECORDACCESS)
            {
                return 446;
            }
            if (service == AS400.RECORDACCESS + 8)
            {
                return 448;
            }
            return AS400.USE_PORT_MAPPER;
        }
        return portList[service];
    }

    static boolean unixSocketAvailable = true;

    private static boolean canUseUnixSocket(String systemName, int service, boolean mustUseNetSockets)
    {
        if (AS400.onAS400 && unixSocketAvailable && !mustUseNetSockets && service != AS400.FILE && (systemName.equalsIgnoreCase("localhost") || systemName.equalsIgnoreCase("ipv6-localhost")))
        {
            if (service == AS400.DATABASE && AS400.nativeVRM.vrm_ < 0x00060100) return false;
            return true;
        }
        return false;
    }

    static SocketContainer getServerSocket(String systemName, int service, SSLOptions useSSL, SocketProperties socketProperties, boolean mustUseNetSockets) throws IOException
    {
        SocketContainer sc = null;
        String serviceName = AS400.getServerName(service);
        // If we're running on a native vm, we're requesting a service that supports a unix domain socket connection, and the unix domain socket code is accessable.
        if (canUseUnixSocket(systemName, service, mustUseNetSockets))
        {
            try
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting a local socket to " + serviceName);
                sc = AS400.nativeVRM.vrm_ < 0x00050400 ? (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerUnix") : (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerUnix2");
                if (sc != null)
                {
                    sc.setProperties(null, serviceName, null, 0, null);
                    return sc;
                }
            }
            catch (IOException e)
            {
                Trace.log(Trace.ERROR, "Error attempting to connect with Unix Socket:", e);
                sc = null;
            }
            // Only try for Unix domain connection once.
            unixSocketAvailable = false;
        }

        int srvPort = PortMapper.getServicePort(systemName, service, useSSL);
        if (srvPort == AS400.USE_PORT_MAPPER)
        {
            // Establish a socket connection to the "port mapper" through port 449...
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connecting to port mapper...");
            
            //Code to make use of new method java.net.Socket.connect(host, timeout) in jdk 1.4
            //only really needed on first socket connect so we do not hang when a system is down.  
            //Socket pmSocket = new Socket(systemName, 449); //@timeout
            Socket pmSocket = getSocketConnection(systemName, 449, socketProperties); //@timeout2
            
            InputStream pmInstream = pmSocket.getInputStream();
            OutputStream pmOutstream = pmSocket.getOutputStream();

            // Now we construct and send a "port map" request to get the port number for the requested service...
            String fullServiceName = (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER) ? serviceName + "-s" : serviceName;
            AS400PortMapDS pmreq = new AS400PortMapDS(fullServiceName);
            if (Trace.traceOn_) pmreq.setConnectionID(pmSocket.hashCode());
            pmreq.write(pmOutstream);

            // Now we get the response and close the socket connection to the port mapper...
            AS400PortMapReplyDS pmresp = new AS400PortMapReplyDS();
            if (Trace.traceOn_) pmresp.setConnectionID(pmSocket.hashCode());
            pmresp.read(pmInstream);
            pmSocket.close();

            try
            {
                srvPort = pmresp.getPort();
            }
            catch (ServerStartupException e)
            {
                Trace.log(Trace.ERROR, "Failed to map a port for " + fullServiceName, e);
                throw e;
            }

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding entry to Service Port table: system " + systemName + ", service " + fullServiceName + ", port " + srvPort);
            PortMapper.setServicePort(systemName, service, srvPort, useSSL);
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Opening socket to system...");
        Socket socket = getSocketConnection(systemName, srvPort, socketProperties);  //@timeout2
        PortMapper.setSocketProperties(socket, socketProperties);

        // We use the port returned in the previous reply to establish a new socket connection to the requested service...
        if (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER)
        {
         // Refactor code but keep the same logic, try JSSE first, fall back to SSL Light again.
        	if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting a secure socket to " + serviceName);
            if (useSSL.useSslight_)
            { // SSLight is no longer exists since v5r4, instead, using JSSE.
                sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerSSL");
                sc.setProperties(socket, null, null, 0, useSSL); 
            }
            else
            { // JSSE is supported since v5r4.
            	sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerJSSE");
                sc.setProperties(socket, null, systemName, srvPort, null);
            }
        }
        else
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting an inet socket to " + serviceName);
            sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerInet");
            sc.setProperties(socket, null, null, 0, null);
        }
        return sc;
    }

    /*  Helper method to get connection using the timeout available in jmv 1.4+ 
     *  If running in JVM 1.3 then it defaults to the old connection without a timeout
     */
    static Socket getSocketConnection(String systemName, int port, SocketProperties socketProperties) throws IOException
    {
      //Code to make use of new method java.net.Socket.connect(host, timeout) in jdk 1.4
      //only really needed on first socket connect so we do not hang when a system is down.  
      //Socket pmSocket = new Socket(systemName, port);
      Socket pmSocket = null;
      try
      {
        /*  Due to various jvm and compile issues, there are many possible types of exceptions that could
         be thrown, depending on jvm version and implementation etc.  Class.forName() seems to be the best
         solution to finding the jvm version that does not degrade performance. */
        Class.forName("java.net.InetSocketAddress"); //throws ClassNotFoundException (common to all jvm implementations)

        pmSocket = new Socket();

        int loginTimeout = 0;
        if(socketProperties.isLoginTimeoutSet())
        {
          loginTimeout = socketProperties.getLoginTimeout();
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connect to port mapper: system '"+systemName+"', port " +port+ ", login timeout " + loginTimeout + " ms.");

        InetSocketAddress hostAddr = systemName != null ? new InetSocketAddress(systemName, port) :
          new InetSocketAddress(InetAddress.getByName(null), port);

        //pmSocket.connect(hostAddr, timeout); //fyi, PortMapper will not load and gets NoClassDefFoundError in jvm1.3 due to SocketAddress parameter type, must use reflection below
        boolean done = false;
        // Retry 3 times if get a bindException
        int bindExceptionRetries = 3;
        long bindExceptionRetrySleepTime=100; 
        while (!done)  // up to two tries
        {
          try
          {
            Class thisClass = pmSocket.getClass();
            Method method = thisClass.getMethod("connect", new Class[]{ SocketAddress.class, java.lang.Integer.TYPE});
            //method.setAccessible(true);                   //@CRS (applet gets exception when calling setAccessible())
            Object args[] = new Object[2];
            args[0] = hostAddr;
            args[1] = new Integer(loginTimeout);

            method.invoke(pmSocket, args);
            done = true;  // if no exception thrown, then no need to try again
          }
          catch (InvocationTargetException e) {
            Trace.log(Trace.ERROR, e);
            Throwable e2 = e.getTargetException();
            if (e2 != null) Trace.log(Trace.ERROR, e2);
            // If we get a java.net.BindException then too many of the sockets are still reserved.
            // I've only see this when testing.  Retry three times if this occurs. 
            // 
            if (e2 instanceof java.net.BindException) {
              if (bindExceptionRetries > 0) {
                // try again 
                bindExceptionRetries--; 
                try {
                  Thread.sleep(bindExceptionRetrySleepTime); 
                  bindExceptionRetrySleepTime = bindExceptionRetrySleepTime * 2; 
                } catch (Exception sleepException) { 
                  
                }
                done = false; 
              } else {
                 throw (java.net.BindException) e2; 
              }
            } else if(e2 instanceof IOException)
            {
              //Here is the actual timeout or network exceptions that we throw back to caller
              throw (IOException) e2;
            }
            else
            {
              //Else this is some sort of issue related to reflection not being supported.  Just throw ClassNotFoundException and catch it below.
              throw new ClassNotFoundException();
            }
          }
          catch (IllegalAccessException e) {
            //Else this is some sort of issue related to reflection not being supported.  Just throw ClassNotFoundException and catch it below.
            Trace.log(Trace.ERROR, e);
            throw new ClassNotFoundException();
          }
          catch (NoSuchMethodException e) {
            //Else this is some sort of issue related to reflection not being supported.  Just throw ClassNotFoundException and catch it below.
            Trace.log(Trace.ERROR, e);
            throw new ClassNotFoundException();
          }
          catch (Exception e) { // compiler won't let us catch "IOException"
            if (e instanceof NoRouteToHostException)
            {
              // If we previously specified "localhost", retry with "ipv6-localhost".
              if ("localhost".equalsIgnoreCase(hostAddr.getHostName()))
              {
                if (Trace.traceOn_) {
                  Trace.log(Trace.DIAGNOSTIC, e.getMessage());
                  Trace.log(Trace.DIAGNOSTIC, "Retrying with hostname 'ipv6-localhost' instead of 'localhost'.");
                }
                hostAddr = new InetSocketAddress("ipv6-localhost", port);
                done = false;  // iterate the loop
              }
              else throw (NoRouteToHostException)e; // don't retry
            }
            else if (e instanceof IOException) throw (IOException)e;
            else throw new RuntimeException(e);  // should never happen
          }
        } // while
      } // outer try
      catch(ClassNotFoundException e){
        //Here we catch any exception related to running in jdk 1.3 or reflection exceptions
        //Just create socket the way we did before without a timeout.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connect to port mapper: system '"+systemName+"', port " +port+ ", no login timeout (JVM 1.3 or lower).");
        pmSocket = new Socket(systemName, port); //for pre jdk1.4
      }
      return pmSocket;
    }

      static void setSocketProperties(Socket socket, SocketProperties socketProperties) throws SocketException
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting socket options...");
        if (socketProperties.keepAliveSet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting keep alive:", socketProperties.keepAlive_);
          socket.setKeepAlive(socketProperties.keepAlive_);
        }

        if (socketProperties.receiveBufferSizeSet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting receive buffer size:", socketProperties.receiveBufferSize_);
          socket.setReceiveBufferSize(socketProperties.receiveBufferSize_);
        }

        if (socketProperties.sendBufferSizeSet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting send buffer size:", socketProperties.sendBufferSize_);
          socket.setSendBufferSize(socketProperties.sendBufferSize_);
        }

        if (socketProperties.soLingerSet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting so linger:", socketProperties.soLinger_);
          socket.setSoLinger(true, socketProperties.soLinger_);
        }

        if (socketProperties.soTimeoutSet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting so timeout:", socketProperties.soTimeout_);
          socket.setSoTimeout(socketProperties.soTimeout_);
        }

        if (socketProperties.tcpNoDelaySet_)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting TCP no delay:", socketProperties.tcpNoDelay_);
          socket.setTcpNoDelay(socketProperties.tcpNoDelay_);
        }

        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Socket properties:");
          try { Trace.log(Trace.DIAGNOSTIC, "    Remote address: " + socket.getInetAddress()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Remote port:", socket.getPort()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Local address: " + socket.getLocalAddress()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Local port:", socket.getLocalPort()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Keep alive:", socket.getKeepAlive()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Receive buffer size:", socket.getReceiveBufferSize()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    Send buffer size:", socket.getSendBufferSize()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    So linger:", socket.getSoLinger()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    So timeout:", socket.getSoTimeout()); } catch (Throwable t) {}
          try { Trace.log(Trace.DIAGNOSTIC, "    TCP no delay:", socket.getTcpNoDelay()); } catch (Throwable t) {}
        }
      }
    }
