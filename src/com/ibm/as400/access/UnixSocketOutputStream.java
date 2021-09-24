///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnixSocketOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.FileOutputStream;
import java.io.IOException;

class UnixSocketOutputStream extends FileOutputStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    private UnixSocketImpl impl;
    private byte temp[] = new byte[1];

    UnixSocketOutputStream(UnixSocketImpl impl) throws IOException
    {
        super(impl.getFileDescriptor());
        this.impl = impl;
    }

    private native void socketWrite(byte b[], int off, int len) throws IOException;

    public void write(int b) throws IOException
    {
        temp[0] = (byte)b;
        this.socketWrite(temp, 0, 1);
    }

    public void write(byte b[]) throws IOException
    {
        this.socketWrite(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException
    {
        this.socketWrite(b, off, len);
    }

    public void close() throws IOException
    {
        impl.close();
    }

    protected void finalize()
    {
    }
}
