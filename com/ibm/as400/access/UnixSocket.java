///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnixSocket.java
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

class UnixSocket
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    UnixSocketImpl impl;

    public UnixSocket(int serverNumber) throws IOException
    {
        impl = new UnixSocketImpl();
        impl.create(serverNumber);
    }

    public InputStream getInputStream() throws IOException
    {
        return impl.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException
    {
        return impl.getOutputStream();
    }

    public synchronized void close() throws IOException
    {
        impl.close();
    }

    public int getSoTimeout() {
      return impl.getSoTimeout(); 
    }

    public void setSoTimeout(int timeout) {
      impl.setSoTimeout(timeout); 
    }
}
