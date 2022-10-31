///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SocketContainerUnix.java
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

class SocketContainerUnix extends SocketContainer
{
    private UnixSocket usocket_;


    void setProperties(Socket socket, String serviceName, String systemName, int port, SSLOptions options) throws IOException
    {
        int serverNumber = 0;
        if (serviceName.equalsIgnoreCase("as-central"))
        {
            serverNumber = 0;
        }
        else if (serviceName.equalsIgnoreCase("as-dtaq"))
        {
            serverNumber = 1;
        }
        else if (serviceName.equalsIgnoreCase("as-netprt"))
        {
            serverNumber = 2;
        }
        else if (serviceName.equalsIgnoreCase("as-rmtcmd"))
        {
            serverNumber = 3;
        }
        else if (serviceName.equalsIgnoreCase("as-signon"))
        {
            serverNumber = 4;
        }
        else if (serviceName.equalsIgnoreCase("as-ddm"))
        {
            serverNumber = 8;
        }

        usocket_ = new UnixSocket(serverNumber);
    }

    void close() throws IOException
    {
        usocket_.close();
    }

    InputStream getInputStream() throws IOException
    {
        return usocket_.getInputStream();
    }

    OutputStream getOutputStream() throws IOException
    {
        return usocket_.getOutputStream();
    
    }
    
    
    int getSoTimeout() throws SocketException {
      return usocket_.getSoTimeout(); 
    }

    void setSoTimeout(int timeout) throws SocketException {
      usocket_.setSoTimeout(timeout); 
    }

}
