///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnixSocketImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class UnixSocketImpl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    static
    {
        System.load("/QSYS.LIB/QYJSPSCK.SRVPGM");
    }

    FileDescriptor fd;
    int timeout_ = 0; 
    
    UnixSocketImpl()
    {
    }

    synchronized void create(int serverNumber) throws IOException
    {
        fd = new FileDescriptor();
        try
        {
            this.socketCreate(serverNumber);
        }
        catch (IOException e)
        {
            this.close();
            throw e;
        }
    }

    FileDescriptor getFileDescriptor()
    {
        return fd;
    }

    synchronized InputStream getInputStream() throws IOException
    {
        return new UnixSocketInputStream(this);
    }

    synchronized OutputStream getOutputStream() throws IOException
    {
        return new UnixSocketOutputStream(this);
    }

    synchronized int available() throws IOException
    {
        return this.socketAvailable();
    }

    void close() throws IOException
    {
        if (fd != null)
        {
            this.socketClose();
            fd = null;
        }
    }

    protected void finalize() throws IOException
    {
        this.close();
    }

    private native int socketAvailable() throws IOException;
    private native void socketCreate(int serverNumber) throws IOException;
    private native void socketClose() throws IOException;

    public int getSoTimeout() {
      return timeout_; 
    }
    
    public void setSoTimeout(int timeout) {
      timeout_ = timeout;
      // TODO JDBC41.  Make sure timeout is reflected on streams 
    }
}
