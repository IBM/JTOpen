///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SocketContainer.java
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

abstract class SocketContainer
{
    abstract void setProperties(Socket socket, String serviceName, String systemName, int port, SSLOptions options) throws IOException;
    abstract void close() throws IOException;
    abstract InputStream getInputStream() throws IOException;
    abstract OutputStream getOutputStream() throws IOException;
    abstract void setSoTimeout(int timeout) throws SocketException; 
    abstract int  getSoTimeout() throws SocketException; 
}
