///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SocketContainerJSSE.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// SocketContainerJSSE contains a socket capable of SSL communications with JSSE.
class SocketContainerJSSE extends SocketContainer
{
    private SSLSocket sslSocket_;

    void setProperties(Socket socket, String serviceName, String systemName, int port, SSLOptions options) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerJSSE: create SSLSocket");
        SSLSocketFactory sslFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        sslSocket_ = (SSLSocket)sslFactory.createSocket(socket, systemName, port, true);
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

    
    int getSoTimeout() throws SocketException {
      return sslSocket_.getSoTimeout(); 
    }

    void setSoTimeout(int timeout) throws SocketException {
      sslSocket_.setSoTimeout(timeout); 
    }

}
