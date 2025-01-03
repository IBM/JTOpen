///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SocketContainerInet.java
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

class SocketContainerInet extends SocketContainer
{
    Socket socket_;

    @Override
    void setProperties(Socket socket, String serviceName, String systemName, int port, SSLOptions options) throws IOException
    {
        socket_ = socket;
    }

    @Override
    void close() throws IOException
    {
        socket_.close();
    }

    @Override
    InputStream getInputStream() throws IOException
    {
        return socket_.getInputStream();
    }

    @Override
    OutputStream getOutputStream() throws IOException
    {
        return socket_.getOutputStream();
    }
    
    @Override
    int getSoTimeout() throws SocketException {
      return socket_.getSoTimeout(); 
    }

    @Override
    void setSoTimeout(int timeout) throws SocketException {
      socket_.setSoTimeout(timeout); 
    }

    @Override
    String getLocalAddress() {
        return socket_.getLocalAddress().getHostAddress();
    }
}
