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
    private int[] sd_; // [0] is normal descriptor, [1] is additionally needed for close()
    private boolean closed_ = false;
    private Object lock_ = new Object();
    private int timeout_ = 0; /* timeout in milliseconds */ 
    
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
        else
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unrecognized serviceName: " + serviceName + ". Defaulting to as-central");
        }
        try
        {
            /*  A little background:  From jt400Native.jar (NativeMethod.java)
            we first load qyjspase32/64 followed by load of QYJSPART.SRVPGM.
            qyjspase contains functions with same signature as qyjspart.
            So these methods are used in a JNI call since they were loaded first.
            (However, load order can't be relied upon when predicting which of
            same-named methods will be found and used at run-time.)
            We had a leak since socketPair creates two descriptors,
            but we were only closing one of them.
            Since we need to close both socketPair descriptors, we ended
            up having to create a new method with new signature to return
            both socket descriptors.  And a corresponding socketPaseClose
            method was added.  We cannot just change the existing function
            signatures to handle two descriptors, since if calling from old
            jt400Native.jar they will fail, but other methods will still
            be accessable, but will not work with ILE sockets.  So the
            solution is to use all or none of the PASE socket related methods.
            (create, close, read, write).  (ie.  We cannot allow just
            socketCreate() to fail and thus use old method (in qyjspntv.C), followed
            by calls to PASE socketRead/Write since they fail.)
            Also note that we want to be able to run with the old jt400Native
            jar with the new qyjspaseXX lib.  To do this, we have to add a try/catch
            in java com.ibm.as400.access.SocketContainerUnix2 to first try
            the new socketPaseCreate() function and if it fails, then catch the exception
            and try the old function signature (ie socketCreate()).
            If socketPaseCreate() fails, we cannot just move on down to the socketCreate()
            in qyjspart since then socketRead/Write would fail (no mixing of ILE with PASE).
            So this is why we have left the socketCreate/Close functions
            here in this file and just added socketPaseCreate/Close with the needed updates.
            */

            boolean paseCallSucceeded = false;
            if(NativeMethods.paseLibLoaded)
            {
                try{
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseCreate()");

                    sd_ = NativeMethods.socketPaseCreate(serverNumber); //socketPaseCreate in qyjspase
                    paseCallSucceeded = true;

                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Socket descriptor:", Integer.toString(sd_[0]));
                }
                catch(NativeException ne){
                    // Got here because of actual exception creating socket on host.
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseCreate()");
                    throw ne;
                }
                catch(UnsatisfiedLinkError e){
                    // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                    // so we just call the generic-named method in qyjspaseXX.so
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UnsatisfiedLinkError while calling NativeMethods.socketPaseCreate()");
                }
                catch(Throwable e){
                    // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                    // so we just call the generic-named method in qyjspaseXX.so
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseCreate()", e);
                }
            }

            if (!paseCallSucceeded)  // try calling the generic-named method
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketCreate()");

                sd_ = new int[1];
                sd_[0] = NativeMethods.socketCreate(serverNumber); //socketCreate in qyjspart

                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Socket descriptor:", Integer.toString(sd_[0]));
            }
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
                boolean paseCallSucceeded = false;
                if(NativeMethods.paseLibLoaded)
                {
                    try{
                        if (sd_.length < 2) {
                          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Descriptor is not paired:", (sd_.length == 0 ? "null" : Integer.toString(sd_[0])));
                          throw new Throwable();
                        }
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseClose("+Integer.toString(sd_[0])+")");

                        NativeMethods.socketPaseClose(sd_[0], sd_[1]);
                        paseCallSucceeded = true;
                    }
                    catch(NativeException ne){
                        // Got here because of actual exception calling 'close' on host.
                      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseClose("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                      throw ne;
                    }
                    catch(UnsatisfiedLinkError e){
                        // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                        // so we just call the generic-named method in qyjspaseXX.so
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UnsatisfiedLinkError while calling NativeMethods.socketPaseClose("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                    }
                    catch(Throwable e){
                        // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                        // so we just call the generic-named method in qyjspaseXX.so
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseClose("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")", e);
                    }
                }

                if (!paseCallSucceeded)  // try calling the generic-named method
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketClose("+Integer.toString(sd_[0])+")");

                    NativeMethods.socketClose(sd_[0]);
                }
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
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error with unix domain socket, errno: " + e.errno_, e);
            int jobCCSID; //@socket2
            if(NativeMethods.paseLibLoaded)
                jobCCSID = 367; // PASE is ascii
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
              int n = 0;
              boolean paseCallSucceeded = false;
              if(NativeMethods.paseLibLoaded)
              {
                try
                {
                  if (sd_.length < 2) {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Descriptor is not paired:", (sd_.length == 0 ? "null" : Integer.toString(sd_[0])));
                    throw new Throwable();
                  }
                  //if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseRead("+Integer.toString(sd_[0])+")");

                  n = NativeMethods.socketPaseRead(sd_[0], sd_[1], b, off, length);
                  paseCallSucceeded = true;
                }
                catch(NativeException ne) {
                  // Got here because of actual exception calling 'read' on host.
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseRead("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                  throw ne;
                }
                catch(UnsatisfiedLinkError e) {
                  // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                  // so we just call the generic-named method in qyjspaseXX.so
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UnsatisfiedLinkError while calling NativeMethods.socketPaseRead("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                }
                catch(Throwable e) {
                  // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                  // so we just call the generic-named method in qyjspaseXX.so
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseRead("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")", e);
                }
              }

              if (!paseCallSucceeded)  // try calling the generic-named method
              {
                if (Trace.traceOn_ && NativeMethods.paseLibLoaded) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketRead("+Integer.toString(sd_[0])+")");

                n = NativeMethods.socketRead(sd_[0], b, off, length);
              }

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
                  int result = 0;
                  boolean paseCallSucceeded = false;
                  if(NativeMethods.paseLibLoaded)
                  {
                    try
                    {
                      if (sd_.length < 2) {
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Descriptor is not paired:", (sd_.length == 0 ? "null" : Integer.toString(sd_[0])));
                        throw new Throwable();
                      }
                      //if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseAvailable("+Integer.toString(sd_[0])+")");

                      result = NativeMethods.socketPaseAvailable(sd_[0], sd_[1]);
                      paseCallSucceeded = true;
                    }
                    catch(NativeException ne){
                      // Got here because of actual exception calling 'available' on host.
                      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseAvailable("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                      throw ne;
                    }
                    catch(UnsatisfiedLinkError e){
                      // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                      // so we just call the generic-named method in qyjspaseXX.so
                      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UnsatisfiedLinkError while calling NativeMethods.socketPaseAvailable("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                    }
                    catch(Throwable e){
                      // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                      // so we just call the generic-named method in qyjspaseXX.so
                      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseAvailable("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")", e);
                    }
                  }

                  if (!paseCallSucceeded)  // try calling the generic-named method
                  {
                    if (Trace.traceOn_ && NativeMethods.paseLibLoaded) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketAvailable("+Integer.toString(sd_[0])+")");

                    result = NativeMethods.socketAvailable(sd_[0]);
                  }

                  return result;
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

        public void write(byte b[], int off, int length) throws IOException
        {
            try
            {
              boolean paseCallSucceeded = false;
              if(NativeMethods.paseLibLoaded)
              {
                try
                {
                  if (sd_.length < 2) {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Descriptor is not paired:", (sd_.length == 0 ? "null" : Integer.toString(sd_[0])));
                    throw new Throwable();
                  }
                  //if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketPaseWrite("+Integer.toString(sd_[0])+")");

                  NativeMethods.socketPaseWrite(sd_[0], sd_[1], b, off, length);
                  paseCallSucceeded = true;
                }
                catch(NativeException ne){
                  // Got here because of actual exception calling 'write' on host.
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "NativeException while calling NativeMethods.socketPaseWrite("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                  throw ne;
                }
                catch(UnsatisfiedLinkError e){
                  // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                  // so we just call the generic-named method in qyjspaseXX.so
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UnsatisfiedLinkError while calling NativeMethods.socketPaseWrite("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")");
                }
                catch(Throwable e){
                  // Probably got here because using new jt400Native.jar with old qyjspaseXX.so,
                  // so we just call the generic-named method in qyjspaseXX.so
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Throwable while calling NativeMethods.socketPaseWrite("+Integer.toString(sd_[0])+","+Integer.toString(sd_[1])+")", e);
                }
              }

              if (!paseCallSucceeded)  // try calling the generic-named method
              {
                if (Trace.traceOn_ && NativeMethods.paseLibLoaded) Trace.log(Trace.DIAGNOSTIC, "Calling NativeMethods.socketWrite("+Integer.toString(sd_[0])+")");

                NativeMethods.socketWrite(sd_[0], b, off, length);
              }
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

    int getSoTimeout() throws SocketException {
      return timeout_;
    }

    void setSoTimeout(int timeout) throws SocketException {
      timeout_ = timeout; 
     
    }
}
