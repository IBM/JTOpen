///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PortMapper.java
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
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

class PortMapper
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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

    static SocketContainer getServerSocket(String systemName, int service, SSLOptions useSSL, SocketProperties socketProperties) throws IOException
    {
        SocketContainer sc = null;
        String serviceName = AS400.getServerName(service);
        // If we're running on a native vm, we're requesting a service that supports a unix domain socket connection, and the unix domain socket code is accessable.
        if (AS400.onAS400 && systemName.equalsIgnoreCase("localhost") && service != AS400.DATABASE && service != AS400.FILE)
        {
            try
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting a local socket to " + serviceName);
                sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerUnix");
                if (sc != null)
                {
                sc.setServiceName(serviceName);
                return sc;
            }
            }
            catch (IOException e)
            {
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error attempting to connect with Unix Socket:", e); //@P0C
                sc = null;
            }
        }

        int srvPort = PortMapper.getServicePort(systemName, service, useSSL);
        if (srvPort == AS400.USE_PORT_MAPPER)
        {
            // Establish a socket connection to the "port mapper" through port 449...
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connecting to port mapper...");
            Socket pmSocket = new Socket(systemName, 449);
            InputStream pmInstream = pmSocket.getInputStream();
            OutputStream pmOutstream = pmSocket.getOutputStream();

            // Now we construct and send a "port map" request to get the port number for the requested service...
            String fullServiceName = (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER) ? serviceName + "-s" : serviceName;
            AS400PortMapDS pmreq = new AS400PortMapDS(fullServiceName);
            pmreq.write(pmOutstream);

            // Now we get the response and close the socket connection to the port mapper...
            AS400PortMapReplyDS pmresp = new AS400PortMapReplyDS();
            pmresp.read(pmInstream);
            pmSocket.close();

            try
            {
                srvPort = pmresp.getPort();
            }
            catch (ServerStartupException e)
            {
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to map a port for " + fullServiceName, e); //@P0C
                throw e;
            }

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding entry to Service Port table: system " + systemName + ", service " + fullServiceName + ", port " + srvPort);
            PortMapper.setServicePort(systemName, service, srvPort, useSSL);
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Opening socket to server...");
        Socket socket = new Socket(systemName, srvPort);

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
            try { Trace.log(Trace.DIAGNOSTIC, "    Remote address: " + socket.getInetAddress()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Remote port:", socket.getPort()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Local address: " + socket.getLocalAddress()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Local port:", socket.getLocalPort()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Keep alive:", socket.getKeepAlive()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Receive buffer size:", socket.getReceiveBufferSize()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    Send buffer size:", socket.getSendBufferSize()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    So linger:", socket.getSoLinger()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    So timeout:", socket.getSoTimeout()); } catch(Throwable t) {}
            try { Trace.log(Trace.DIAGNOSTIC, "    TCP no delay:", socket.getTcpNoDelay()); } catch(Throwable t) {}
        }

        // We use the port returned in the previous reply to establish a new socket connection to the requested service...
        if (useSSL != null && useSSL.proxyEncryptionMode_ != SecureAS400.CLIENT_TO_PROXY_SERVER)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting a secure socket to " + serviceName);
            try
            {
                if (useSSL.useSslight_) throw new Exception();
                sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerJSSE");
                ((SocketContainerJSSE)sc).setSystemNameAndPort(systemName, srvPort);
                sc.setSocket(socket);
                sc.setServiceName(serviceName);
            }
            catch (Throwable e)
            {
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Exception using JSSE falling back to sslight:", e);
                sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerSSL");
                ((SocketContainerSSL)sc).setOptions(useSSL);
                sc.setSocket(socket);
                sc.setServiceName(serviceName);
            }
        }
        else
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Starting an inet socket to " + serviceName);
            sc = (SocketContainer)AS400.loadImpl("com.ibm.as400.access.SocketContainerInet");
        sc.setSocket(socket);
        sc.setServiceName(serviceName);
        }
        return sc;
    }
}
