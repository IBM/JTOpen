///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SocketContainerInet.java
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

class SocketContainerInet extends SocketContainer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    void close() throws IOException
    {
        socket.close();
    }

    InputStream getInputStream() throws IOException
    {
        return socket.getInputStream();
    }

    OutputStream getOutputStream() throws IOException
    {
        return socket.getOutputStream();
    }

    byte[] getUser() throws IOException
    {
        return null;
    }

    byte[] getSubstPassword(byte[] clientSeed, byte[] serverSeed) throws IOException
    {
        return null;
    }
}
