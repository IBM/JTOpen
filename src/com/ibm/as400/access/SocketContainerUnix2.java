///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SocketContainerUnix.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

class SocketContainerUnix2 extends SocketContainer
{
    private int sd_;
    private boolean closed_ = false;
    private Object lock_ = new Object();

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
        try
        {
            sd_ = NativeMethods.socketCreate(serverNumber);
        }
        catch (NativeException e)
        {
            throw createSocketException(e);
        }
    }

    void close() throws IOException
    {
        if (!closed_)
        {
            try
            {
                NativeMethods.socketClose(sd_);
                closed_ = true;
            }
            catch (NativeException e)
            {
                throw createSocketException(e);
            }
        }
    }

    protected void finalize() throws IOException
    {
        close();
    }

    InputStream getInputStream() throws IOException
    {
        synchronized (lock_)
        {
            return new SCUInputStream();
        }
    }

    OutputStream getOutputStream() throws IOException
    {
        synchronized (lock_)
        {
            return new SCUOutputStream();
        }
    }

    private static SocketException createSocketException(NativeException e)
    {
        try
        {
            Trace.log(Trace.ERROR, "Error with unix domain socket, errno: " + e.errno_, e);
            int jobCCSID = JobCCSIDNative.retrieveCcsid();
            Converter conv = new Converter(jobCCSID);
            return new SocketException(conv.byteArrayToString(e.data));
        }
        catch (Throwable t)
        {
        }
        return new SocketException();
    }

    private final class SCUInputStream extends InputStream
    {
        private boolean eof_;
        private byte[] temp_ = new byte[1];

        public int read(byte b[]) throws IOException
        {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int length) throws IOException
        {
            if (eof_)
            {
                return -1;
            }
            try
            {
                int n = NativeMethods.socketRead(sd_, b, off, length);
                if (n <= 0)
                {
                    eof_ = true;
                    return -1;
                }
                return n;
            }
            catch (NativeException e)
            {
                throw createSocketException(e);
            }
        }

        public int read() throws IOException
        {
            if (eof_)
            {
                return -1;
            }

            int n = read(temp_, 0, 1);
            if (n <= 0)
            {
                return -1;
            }
            return temp_[0] & 0xff;
        }

        public long skip(long numbytes) throws IOException
        {
            if (numbytes <= 0)
            {
                return 0;
            }
            long n = numbytes;
            int buflen = (int)Math.min(1024, n);
            byte data[] = new byte[buflen];
            while (n > 0)
            {
                int r = read(data, 0, (int)Math.min((long)buflen, n));
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
            synchronized (lock_)
            {
                try
                {
                    return NativeMethods.socketAvailable(sd_);
                }
                catch (NativeException e)
                {
                    throw createSocketException(e);
                }
            }
        }

        public void close() throws IOException
        {
            SocketContainerUnix2.this.close();
        }
    }

    private final class SCUOutputStream extends OutputStream
    {
        private byte temp_[] = new byte[1];

        public void write(int b) throws IOException
        {
            temp_[0] = (byte)b;
            write(temp_, 0, 1);
        }

        public void write(byte b[]) throws IOException
        {
            write(b, 0, b.length);
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            try
            {
                NativeMethods.socketWrite(sd_, b, off, len);
            }
            catch (NativeException e)
            {
                throw createSocketException(e);
            }
        }

        public void close() throws IOException
        {
            SocketContainerUnix2.this.close();
        }
    }
}
