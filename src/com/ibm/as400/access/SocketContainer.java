///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SocketContainer.java
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

abstract class SocketContainer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Socket socket;
    void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    String serviceName;
    void setServiceName(String serviceName) throws IOException
    {
        this.serviceName = serviceName;
    }

    abstract void close() throws IOException;
    abstract InputStream getInputStream() throws IOException;
    abstract OutputStream getOutputStream() throws IOException;
    abstract byte[] getUser() throws IOException;
    abstract byte[] getSubstPassword(byte[] clientSeed, byte[] serverSeed) throws IOException;
}
