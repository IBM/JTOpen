///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SocketContainerUnix.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SocketContainerUnix extends SocketContainer
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    private UnixSocket usocket;

    void setServiceName(String serviceName) throws IOException
    {
        super.setServiceName(serviceName);

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

        usocket = new UnixSocket(serverNumber);
    }

    void close() throws IOException
    {
        usocket.close();
    }

    InputStream getInputStream() throws IOException
    {
        return usocket.getInputStream();
    }

    OutputStream getOutputStream() throws IOException
    {
        return usocket.getOutputStream();
    }

    byte[] getUser() throws IOException
    {
        UnixSocketUser user = new UnixSocketUser();
        return user.getUserId();
    }

    byte[] getSubstPassword(byte[] clientSeed, byte[] serverSeed) throws IOException
    {
        UnixSocketUser user = new UnixSocketUser();
        return user.getSubstitutePassword(clientSeed, serverSeed);
    }
}
