///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTPThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2004-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.*;
import java.io.*;
import java.util.*;

class FTPThread implements Runnable
{
  private FTP ftp_;
  
  private int port_;
  private Socket socket_;
  private boolean running_ = false;
  private final Object runLock_ = new Object();

  private InetAddress localAddress_;

  FTPThread(FTP ftp)
  {
    ftp_ = ftp;
  }

  // This must be called before the thread is started.
  public void setLocalAddress(InetAddress localAddress)
  {
    localAddress_ = localAddress;
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "FTP thread's local address set to: "+localAddress_.toString());
  }

  public void waitUntilStarted()
  {
    try
    {
      if (!running_)
      {
        synchronized (runLock_)
        {
          if (!running_) runLock_.wait();
        }
      }
    }
    catch (Exception e)
    {
      Trace.log(Trace.ERROR, "Error while waiting for FTP thread to start.", e);
    }
  }

  public int getLocalPort()
  {
    return port_;
  }

  public Socket getSocket()
  {
    if (socket_ == null)
    {
      try
      {
        synchronized(this)
        {
          if (socket_ == null) wait(60000); // Wait 60 seconds to prevent hanging.
        }
      }
      catch (Exception e)
      {
        Trace.log(Trace.ERROR, "Error while getting socket from FTP thread.", e);
        return null;
      }
    }
    Socket s = socket_;
    socket_ = null;
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "FTP thread returned previously accepted socket.");
      if (s == null) Trace.log(Trace.DIAGNOSTIC, "FTPThread.getSocket() is returning null.");
    }
    return s;
  }

  public void issuePortCommand() throws IOException
  {
      String addr = localAddress_.getHostAddress();
      // Try the extended port command.
      String response = ftp_.issueCommand("EPRT |" + (addr.indexOf(':') == -1 ? "1" : "2") + "|" + addr + "|" + port_ + "|");
      if (ftp_.lastMessage_.startsWith("200")) return;

      // System may not support EPRT, fallback to the port command.
    StringTokenizer st = new StringTokenizer(addr, ".");
    StringBuffer cmd = new StringBuffer("PORT ");
    while (st.hasMoreTokens())
    {
      cmd.append(st.nextToken());
      cmd.append(",");
    }
    cmd.append(port_/256);
    cmd.append(",");
    cmd.append(port_ % 256);
    response = ftp_.issueCommand(cmd.toString());
    // A "successful" response will begin with 200.
    if (!ftp_.lastMessage_.startsWith("200"))
    {
      Trace.log(Trace.ERROR, "Unexpected response to " + cmd + ": " + ftp_.lastMessage_);
    }
  }

  public void run()
  {
    ServerSocket ss = null;
    try
    {
      if (ftp_.isReuseSocket())
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "FTP thread will reuse socket if multiple transfers.");
        ss = new ServerSocket(0, 50, localAddress_);
        port_ = ss.getLocalPort();
        while (true)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "FTP thread waiting for new active mode socket.");
          }
          if (!running_)
          {
            running_ = true;
            synchronized (runLock_)
            {
              runLock_.notifyAll();
            }
          }
          socket_ = ss.accept();
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "FTP thread accepted active mode socket: "+socket_);
          }
          synchronized (this)
          {
            notifyAll();
          }
        }
      }
      else  // don't reuse socket
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "FTP thread create new socket if multiple transfers.");
        while (true)
        {
          if (ss == null)
          {
            ss = new ServerSocket(0, 1, localAddress_);
            port_ = ss.getLocalPort();
          }
          if (!running_)
          {
            running_ = true;
            synchronized (runLock_)
            {
              runLock_.notifyAll();
            }
          }
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "FTP thread waiting for new active mode socket.");
          }
          socket_ = ss.accept();
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "FTP thread accepted active mode socket: "+socket_);
          }
          synchronized (this)
          {
            notifyAll();
          }
          // Get a new server socket each time to avoid bug on AIX.
          // We create the server socket with only 1 connection in the backlog
          // so any other incoming requests will have to wait until we can get them
          // a new server socket.  This bottlenecks a multi-threaded client that is
          // sharing an FTP object across threads, but they shouldn't be doing that anyway.
          ss.close();
          ss = null;
        }
      }
    }
    catch (Exception e)
    {
      Trace.log(Trace.ERROR, "Exception in FTP thread.", e);
    }
    finally
    {
      if (socket_ != null)
      {
        try
        {
          socket_.close();
        }
        catch (Exception e)
        {
        }
      }
      if (ss != null)
      {
        try
        {
          ss.close();
        }
        catch (Exception e)
        {
        }
      }
    }
  }
}

