///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import netscape.security.PrivilegeManager;

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

    static void setServicePort(String systemName, int service, int port, boolean useSSL)
    {
        if (useSSL) service += 8;
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

    static int getServicePort(String systemName, int service, boolean useSSL)
    {
        if (useSSL) service += 8;
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

    // Load the specified socket container object.
    // param  containerName  The fully package named class name to load.
    static SocketContainer loadSocketContainer(String containerName) throws IOException
    {
        try
        {
            return (SocketContainer)Class.forName(containerName).newInstance();
        }
        catch (ClassNotFoundException e1)
        {
            Trace.log(Trace.ERROR, "Unexpected ClassNotFoundException:", e1);
        }
        catch (IllegalAccessException e2)
        {
            Trace.log(Trace.ERROR, "Unexpected IllegalAccessException:", e2);
        }
        catch (InstantiationException e3)
        {
            Trace.log(Trace.ERROR, "Unexpected InstantiationException:", e3);
        }
        Trace.log(Trace.DIAGNOSTIC, "Load of socket container: " + containerName + " failed");
        throw new IOException();
    }

    static SocketContainer getServerSocket(String systemName, int service, boolean useSSL) throws IOException
    {
        SocketContainer sc = null;
        String serviceName = AS400.getServerName(service);
        // If we're running on a native vm, we're requesting a service that supports a unix domain socket connection, and the unix domain socket code is accessable.
        if (AS400.isSysLocal(systemName) && service != AS400.DATABASE && service != AS400.FILE)
        {
            try
            {
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Starting a local socket to " + serviceName);
                sc = loadSocketContainer("com.ibm.as400.access.SocketContainerUnix");
                sc.setServiceName(serviceName);
                return sc;
            }
            catch (IOException e)
            {
                Trace.log(Trace.ERROR, "Error attempting to connect with Unix Socket:", e);
                sc = null;
            }
        }

        // If browser security classes can be loaded, enable the connect privileges so that signed applets using our classes can make network connections.
        Trace.log(Trace.DIAGNOSTIC, "Loading browser security classes.");

        Class privilegeManagerClass = null;
        Class permissionIDClass = null;
        Class policyEngineClass = null;

        try
        {
            privilegeManagerClass = Class.forName("netscape.security.PrivilegeManager");
            Trace.log(Trace.DIAGNOSTIC, "Loaded Netscape browser security classes.");
        }
        catch (Throwable e)
        {
            Trace.log(Trace.DIAGNOSTIC, "Netscape browser security classes not loaded.");
        }

        try
        {
            permissionIDClass = Class.forName("com.ms.security.PermissionID");
            policyEngineClass = Class.forName("com.ms.security.PolicyEngine");
            Trace.log(Trace.DIAGNOSTIC, "Loaded IE browser security classes.");
        }
        catch (Throwable e)
        {
            Trace.log(Trace.DIAGNOSTIC, "IE browser security classes not loaded.");
        }

        // If available, invoke the Navigator enablePrivilege method.
        if (privilegeManagerClass != null)
        {
            try
            {
                Trace.log(Trace.DIAGNOSTIC, "Enabling connect privileges for Navigator.");
                PrivilegeManager.enablePrivilege("UniversalConnect");
                Trace.log(Trace.DIAGNOSTIC, "Enabled connect privileges for Navigator.");
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Desired Netscape security method error:", e);
            }
        }

        // If available, invoke the IE assertPermission method.
        if ((permissionIDClass != null) && (policyEngineClass != null))
        {
            try
            {
                Trace.log(Trace.DIAGNOSTIC, "Enabling connect privileges for IE.");
                PolicyEngine.assertPermission(PermissionID.NETIO);
                Trace.log(Trace.DIAGNOSTIC, "Enabled connect privileges for IE.");
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Desired IE security method error:", e);
            }
        }

        int srvPort = PortMapper.getServicePort(systemName, service, useSSL);
        if (srvPort == AS400.USE_PORT_MAPPER)
        {
            // Establish a socket connection to the "port mapper" through port 449...
            Trace.log(Trace.DIAGNOSTIC, "Connecting to port mapper...");
            Socket pmSocket = new Socket(systemName, 449);
            InputStream pmInstream = pmSocket.getInputStream();
            OutputStream pmOutstream = pmSocket.getOutputStream();

            // Now we construct and send a "port map" request to get the port number for the requested service...
            String fullServiceName = useSSL ? serviceName + "-s" : serviceName;
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
                Trace.log(Trace.ERROR, "Failed to map a port for " + fullServiceName, e);
                throw (ServerStartupException)e.fillInStackTrace();
            }

            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding entry to Service Port table: system " + systemName + ", service " + fullServiceName + ", port " + srvPort);
            PortMapper.setServicePort(systemName, service, srvPort, useSSL);
        }

        Trace.log(Trace.DIAGNOSTIC, "Opening socket to server...");
        // We use the port returned in the previous reply to establish a new socket connection to the requested service...
        if (useSSL)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Starting a secure socket to " + serviceName);
            sc = loadSocketContainer("com.ibm.as400.access.SocketContainerSSL");
        }
        else
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Starting an inet socket to " + serviceName);
            sc = loadSocketContainer("com.ibm.as400.access.SocketContainerInet");
        }

        Socket socket = new Socket(systemName, srvPort);

        // Try to set the no delay option, but if that doesn't work, keep going.
        try
        {
            socket.setTcpNoDelay(true);
        }
        catch (SocketException e)
        {
            Trace.log(Trace.WARNING, "Socket exception setting no delay:", e);
        }

        // Try to set the SoLinger option, but if that doesn't work, keep going.
        try
        {
            if (socket.getSoLinger() != -1)
            {
                socket.setSoLinger(true, 60);
            }
        }
        catch (SocketException e)
        {
            Trace.log(Trace.WARNING, "Socket exception setting so linger:", e);
        }

        sc.setSocket(socket);
        sc.setServiceName(serviceName);
        return sc;
    }
}
