///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SocketContainerJSSE.java
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
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// SocketContainerJSSE contains a socket capable of SSL communications with JSSE.
class SocketContainerJSSE extends SocketContainer
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private SSLSocket sslSocket_;
    private String systemName_;
    private int port_;

    void setSystemNameAndPort(String systemName, int port)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: setSystemNameAndPort");
        systemName_ = systemName;
        port_ = port;
    }

    // Store the socket.
    void setSocket(Socket socket)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: setSocket");
        this.socket = socket;
    }

    void setServiceName(String serviceName) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: setServiceName");
        super.setServiceName(serviceName);

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: create SSLSocket");
        SSLSocketFactory sslFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        sslSocket_ = (SSLSocket)sslFactory.createSocket(socket, systemName_, port_, true);
    }

    void close() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: close");
        sslSocket_.close();
    }

    InputStream getInputStream() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: getInputStream");
        return sslSocket_.getInputStream();
    }

    OutputStream getOutputStream() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: getOutputStream");
        return sslSocket_.getOutputStream();
    }

    byte[] getUser() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: getUser");
        return null;
    }

    byte[] getSubstPassword(byte[] clientSeed, byte[] serverSeed) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: getSubstPassword");
        return null;
    }
}
