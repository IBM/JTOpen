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
    private int[] sd_; //@leak [0] is normal descriptor, [1] is needed for close()
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
        else if (serviceName.equalsIgnoreCase("as-database"))
        {
            serverNumber = 6;
        }
        else if (serviceName.equalsIgnoreCase("as-ddm"))
        {
            serverNumber = 8;
        }
        try
        {
            /*  A little background:  From jt400Native.jar (NativeMethod.java) 
            we first load qyjspase32/64 followed load of QYJSPART.SRVPGM.
            qyjspase contains functions with same signature as qyjspart.  
            So these methods are used in a jni call since they were loaded first.
            We had a leak since socketPair creates two descriptors, 
            but we were only closing one of them.
            Since we need to close both socketPair descriptors, we ended 
            up having to create a new method with new signature to return
            both socket descriptors.  And a corresponding socketPaseClose 
            method was added.  We cannot just change the existing function
            signatures to handle two descriptors since if calling from old 
            jt400Native.jar they will fail, but other methods will still 
            be accessable, but will not work with ILE sockets.  So the 
            solution is to use all or none of the pase socket related methods.
            (create, close, read, write).  (ie.  We cannot allow just 
            socketCreate() to fail and thus use old method (in qyjspntv.C), followed
            by calls to pase socketRead/Write since they fail).
            Also note that we want to be able to run with the old jt400Native 
            jar with the new qyjspase lib.  To do this, we have to add a try/catch
            in java com.ibm.as400.access.SocketContainerUnix2 to first try
            the new socketPaseCreate() function and if it fails, then catch the exception
            and try the old function signature (ie socketCreate()).  
            If socketPaseCreate() fails, we cannot just move on down to the socketCreate()
            in qyjspart since then socketRead/Write would fail (no mixing of ile with pase).  
            So this is why we have left the socketCreate/Close functions
            here in this file and just added socketPaseCreate/Close with the needed updates.
            */
                        
            if(NativeMethods.paseLibLoaded)                         //@leak
            {
                try{
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseCreate()");//@leak
                    
                    sd_ = NativeMethods.socketPaseCreate(serverNumber);      //@leak //socketPaseCreate in qyjspase
                }catch(NativeException ne){                                  //@leak
                    //got here because of actual exception creating socket on host.
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseCreate()");//@leak
                    throw ne;                                                //@leak
                }catch(Throwable e){                                         //@leak
                    //Here we actually get java.lang.UnsatisfiedLinkError which is subclass of Throwable
                    //got here is using new jt400Native.jar with old qyjspase32.so lib
                    //so we just call the old method in qyjspase32.so
                    if (Trace.traceOn_)
                    {
                        Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseCreate()");//@leak
                        Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketCreate()");//@leak
                    }
                    sd_ = new int[1];                                    //@leak
                    sd_[0] = NativeMethods.socketCreate(serverNumber);   //@leak //socketCreate in qyjspase
                }
            }
            else
            {                                                        //@leak
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling non-pase NativeMethods.socketPaseCreate()");//@leak
              
                sd_ = new int[1];                                    //@leak
                sd_[0] = NativeMethods.socketCreate(serverNumber);   //@leak //socketCreate in qyjspart
            }                                                        //@leak
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
                if(NativeMethods.paseLibLoaded)               //@leak
                {                                             //@leak
                    try{                                      //@leak
                        if(sd_.length < 2)                    //@leak
                            throw new Throwable();            //@leak
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseClose()");//@leak
                        
                        NativeMethods.socketPaseClose(sd_[0], sd_[1]); //@leak
                    }catch(NativeException ne){                        //@leak
                        //got here because of actual exception calling close on host.
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseClose()");//@leak
                        throw ne;                                      //@leak
                    }catch(Throwable e){                               //@leak
                        //Here we actually get java.lang.UnsatisfiedLinkError which is subclass of Throwable
                        //got here is using new jt400Native.jar with old qyjspase32.so lib
                        //so we just call the old method in qyjspase32.s
                        if (Trace.traceOn_)
                        {
                            Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseClose()");//@leak
                            Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketClose()");//@leak
                        }
                        NativeMethods.socketClose(sd_[0]);    //@leak
                    }                                         //@leak 
                }                                             //@leak
                else                                          //@leak
                {                                             //@leak
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling non-pase NativeMethods.socketClose()");//@leak
                    NativeMethods.socketClose(sd_[0]);        //@leak
                }                                             //@leak
              
                closed_ = true;
            }
            catch (NativeException e)
            {
                throw createSocketException(e);
            }
            finally //@socket2
            { 
                sd_ = null; //@socket2
                closed_ = true; //@socket2 //add this so if close fails, we don't keep trying to close a broken socket
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
            int jobCCSID; //@socket2
            if(NativeMethods.paseLibLoaded)
                jobCCSID = 367; //pase is ascii
            else
                jobCCSID = JobCCSIDNative.retrieveCcsid();

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
                int n = NativeMethods.socketRead(sd_[0], b, off, length);  //@leak
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
                    return NativeMethods.socketAvailable(sd_[0]); //@leak
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
                NativeMethods.socketWrite(sd_[0], b, off, len); //@leak
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
