///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnixSocketInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.FileInputStream;
import java.io.IOException;

class UnixSocketInputStream extends FileInputStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    private boolean eof;
    private UnixSocketImpl impl;
    private byte temp[] = new byte[1];

    UnixSocketInputStream(UnixSocketImpl impl) throws IOException
    {
        super(impl.getFileDescriptor());
        this.impl = impl;
    }

    private native int socketRead(byte b[], int off, int len) throws IOException;

    public int read(byte b[]) throws IOException
    {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int length) throws IOException
    {
        if (eof)
        {
            return -1;
        }
        int n = socketRead(b, off, length);
        if (n <= 0)
        {
            eof = true;
            return -1;
        }
        return n;
    }

    public int read() throws IOException
    {
        if (eof)
        {
            return -1;
        }

        int n = read(temp, 0, 1);
        if (n <= 0)
        {
            return -1;
        }
        return temp[0] & 0xff;
    }

    public long skip(long numbytes) throws IOException
    {
        if (numbytes <= 0)
        {
            return 0;
        }
        long n = numbytes;
        int buflen = (int) Math.min(1024, n);
        byte data[] = new byte[buflen];
        while (n > 0)
        {
            int r = read(data, 0, (int) Math.min((long) buflen, n));
            if (r < 0)
            {
                break;
            }
            n -= r;
        }
        return numbytes - n;
    }

    public int available() throws IOException
    {
        return impl.available();
    }

    public void close() throws IOException
    {
        impl.close();
    }

    protected void finalize()
    {
    }
}
