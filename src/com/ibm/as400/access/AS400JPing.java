///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400JPing.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

/**
 Determines if services are running on the IBM i system.
 <p>Here is an example of calling AS400JPing within a Java program to ping the Remote Command Service:
 <pre>
 * AS400JPing pingObj = new AS400JPing("myAS400", AS400.COMMAND, false);
 * if (pingObj.ping())
 *     System.out.println("SUCCESS");
 * else
 *     System.out.println("FAILED");
 </pre>
 *
 * @see utilities.JPing
 **/
public class AS400JPing
{
    /**
     Constant for pinging all the services.
     **/
    public final static int ALL_SERVICES = 99;  // The default service is 99, which pings all the services.

    private String systemName_;
    private int service_ = ALL_SERVICES;
    private int length_ = 10;

    private boolean echo_ = false;
    private SSLOptions useSSL_ = null;
    private long time_ = 20000;

    private PrintWriter writer_;
    // SocketContainer to all Services but the DDM Server.
    private SocketContainer socketContainer_;
    // Socket to the DDM Server.
    private Socket ddmSocket_;
    // Thread to handle timeout values.
    private Thread jpingDaemon_;
    // Inner class that implements runnable.
    private JPingThread     jpingThread_;
    private SocketProperties socketProperties_ = new SocketProperties();

    // Handles loading the appropriate resource bundle.
    private static ResourceBundleLoader loader_;

    /**
     Constructs an AS400JPing object with the specified <i>systemName</i>.  A JPing object created with this constructor will ping all of the services when ping() is called.
     @param  systemName  The system to ping.  The <i>systemName</i> string can be in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), or IP address (eg. "9.1.2.3").
     @see AS400#getSystemName()
     **/
    public AS400JPing(String systemName)
    {
        this(systemName, ALL_SERVICES, false);
    }

    /**
     Constructs an AS400JPing object with the specified <i>systemName</i> and <i>service</i>.
     @param  systemName  The system to ping.  The <i>systemName</i> string can be in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), or IP address (eg. "9.1.2.3").
     @param  service  The service to ping.  Valid services are:
     <ul>
     <li>{@link AS400#FILE AS400.FILE} - the IFS file service
     <li>{@link AS400#PRINT AS400.PRINT} - the print service
     <li>{@link AS400#COMMAND AS400.COMMAND} - the command and program call service
     <li>{@link AS400#DATAQUEUE AS400.DATAQUEUE} - the data queue service
     <li>{@link AS400#DATABASE AS400.DATABASE} - the JDBC service
     <li>{@link AS400#RECORDACCESS AS400.RECORDACCESS} - the record level access service
     <li>{@link AS400#CENTRAL AS400.CENTRAL} - the license management service
     <li>{@link AS400#SIGNON AS400.SIGNON} - the sign-on service
     <li>{@link #ALL_SERVICES ALL_SERVICES} - all services
     </ul>
     @see AS400#getSystemName()
     **/
    public AS400JPing(String systemName, int service)
    {
        this(systemName, service, false);
    }

    /**
     Constructs an AS400JPing object.
     @param  systemName  The system to ping.  The <i>systemName</i> string can be in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), or IP address (eg. "9.1.2.3").
     @param  service  The service to ping.  Valid services are:
     <ul>
     <li>{@link AS400#FILE AS400.FILE} - the IFS file service
     <li>{@link AS400#PRINT AS400.PRINT} - the print service
     <li>{@link AS400#COMMAND AS400.COMMAND} - the command and program call service
     <li>{@link AS400#DATAQUEUE AS400.DATAQUEUE} - the data queue service
     <li>{@link AS400#DATABASE AS400.DATABASE} - the JDBC service
     <li>{@link AS400#RECORDACCESS AS400.RECORDACCESS} - the record level access service
     <li>{@link AS400#CENTRAL AS400.CENTRAL} - the license management service
     <li>{@link AS400#SIGNON AS400.SIGNON} - the sign-on service
     <li>{@link #ALL_SERVICES ALL_SERVICES} - all services
     </ul>
     @param  useSSL  true if the pinging the SSL port for the service, false otherwise.  The default is false.
     @see AS400#getSystemName()
     **/
    public AS400JPing(String systemName, int service, boolean useSSL)
    {
        if (systemName == null)
            throw new NullPointerException("systemName");

        if (service < AS400.FILE || (service > AS400.SIGNON && service != ALL_SERVICES))
            throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        systemName_ = systemName;
        service_ = service;
        useSSL_ = useSSL ? new SSLOptions() : null;
    }

    /**
     Ping the system.
     @return  true if all of the services (or, if a service was specified on the constructor, the specified service) can be pinged successfully; false otherwise.
     @see #pingAllServices
     **/
    public boolean ping()
    {
        if (service_ == ALL_SERVICES)
        {
            boolean rtn = true;
            for (int i = AS400.FILE; i <= AS400.SIGNON; ++i)
            {
                rtn = rtn && ping(i);
                if (rtn == false)   // if we get a failed ping, we want to return right away,
                    return rtn;      // instead of pinging the other services.
            }

            return rtn;
        }
        else
        {
            return ping(service_);
        }
    }

    /**
     Ping a specific service.
     @param  service  The service to ping.  Valid services are:
     <ul>
     <li>{@link AS400#FILE AS400.FILE} - the IFS file service
     <li>{@link AS400#PRINT AS400.PRINT} - the print service
     <li>{@link AS400#COMMAND AS400.COMMAND} - the command and program call service
     <li>{@link AS400#DATAQUEUE AS400.DATAQUEUE} - the data queue service
     <li>{@link AS400#DATABASE AS400.DATABASE} - the JDBC service
     <li>{@link AS400#RECORDACCESS AS400.RECORDACCESS} - the record level access service
     <li>{@link AS400#CENTRAL AS400.CENTRAL} - the license management service
     <li>{@link AS400#SIGNON AS400.SIGNON} - the sign-on service
     </ul>
     @return  true if the service can be pinged successfully, and false otherwise.
     **/
    public synchronized boolean ping(int service)
    {
        if (service < AS400.FILE || service > AS400.SIGNON)
          throw new ExtendedIllegalArgumentException("service (" + service + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        InputStream inStream = null;
        OutputStream outStream = null;
        try
        {
            if (Trace.traceOn_)
            {
                Trace.log(Trace.INFORMATION, "Ping System:   " + systemName_);
                Trace.log(Trace.INFORMATION, "Ping Service:  " + AS400.getServerName(service));
                Trace.log(Trace.INFORMATION, "Packet Length: " + length_);
                Trace.log(Trace.INFORMATION, "Echo Packet:   " + ((echo_) ? "on" : "off"));
                Trace.log(Trace.INFORMATION, "Ping SSL Port: " + ((useSSL_ != null) ? "yes" : "no"));
            }

            service_ = service;

            // DDM does not use the Client Access Datastreams, so we have to use a different
            // datastream to determine if the DDM server is connected.
            if (service == AS400.RECORDACCESS)
                pingDDM();
            else
            {
                jpingThread_ = new JPingThread();
                jpingDaemon_ = new Thread(jpingThread_, "AS400JPingDaemon");
                jpingDaemon_.setDaemon(true);
                jpingDaemon_.start();

                synchronized (this)
                {
                    try
                    {
                        if (Trace.traceOn_)
                            Trace.log(Trace.INFORMATION, "Ping Timeout:  " + time_ + "(ms)" );

                        wait(time_);
                    }
                    catch (InterruptedException ie)
                    {
                        if (Trace.traceOn_)
                            Trace.log(Trace.ERROR, "Unexpected exception.", ie);
                    }
                }

                // If the Thread has not returned within the timeout period,
                // interrupt the thread and fail the ping attempt.
                if (socketContainer_ == null)
                {
                    jpingDaemon_.interrupt();
                    throw new Exception("Ping timeout occurred.");
                }
                else  // The Thread has returned within the timeout period.
                {
                    inStream = socketContainer_.getInputStream();
                    outStream = socketContainer_.getOutputStream();

                    // buffer is the data that is pinged back and forth to the system.
                    byte[] buffer = new byte[length_];

                    for (int i = 0; i < length_; ++i)
                    {
                        buffer[i] = (byte)0xDD;  //some byte data.
                    }

                    if (echo_)
                    {
                        JPingEchoDS req = new JPingEchoDS(service, buffer);
                        req.write(outStream);
                        outStream.flush();

                        JPingEchoReplyDS reply = new JPingEchoReplyDS(buffer.length);
                        if (reply.read(inStream) == length_)
                        { /* bytes echoed back match the number of bytes sent, do nothing. */  }
                        else
                        {
                            Trace.log(Trace.ERROR, "Bytes echoed did not match then number of bytes sent.");
                            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
                        }
                    }
                    else
                    {
                        JPingDS req = new JPingDS(service, buffer);
                        req.write(outStream);
                        outStream.flush();
                    }
                }
            }

            // If we have gotten this far without an exception, then the
            // service has successfully been pinged.
            if (writer_ != null)
                writer_.println(loader_.substitute(loader_.getText("PROP_NAME_AJP_SUCCESS"),
                                                   new String[] { AS400.getServerName(service), ((useSSL_ != null) ? "-s" : "") } ) );

            return true;
        }
        catch (ServerStartupException sse)
        {
            if (writer_ != null)
                writer_.println(loader_.substitute(loader_.getText("PROP_NAME_AJP_FAILED"),
                                                   new String[] { AS400.getServerName(service), ((useSSL_ != null) ? "-s" : "") } ) );

            if (Trace.traceOn_)
                Trace.log(Trace.ERROR, sse);

            return false;
        }
        catch (Exception e)
        {
            if (writer_ != null)
                writer_.println(loader_.substitute(loader_.getText("PROP_NAME_AJP_FAILED"),  new String[] { AS400.getServerName(service), ((useSSL_ != null) ? "-s" : "") } ) );

            if (Trace.traceOn_)
                Trace.log(Trace.ERROR, e);

            return false;
        }
        finally
        {
          // Close the input/output streams and the socket.
          if (outStream != null) {
            try { outStream.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
          if (inStream != null) {
            try { inStream.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
          if (socketContainer_ != null) {
            try { socketContainer_.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
        }
    }

    /**
     Ping all services.
     This method differs from {@link #ping ping()} in that it doesn't immediately return when a failed ping is encountered, but rather continues until all services have been pinged.
     @return  true if all of the services can be pinged successfully; false if at least one service cannot be pinged.
     **/
    public boolean pingAllServices()
    {
      boolean rtn = true;
      for (int i = AS400.FILE; i <= AS400.SIGNON; ++i)
      {
        rtn = ping(i) && rtn;
      }

      return rtn;
    }

    /**
     Ping the DDM server.
     **/
    private void pingDDM() throws Exception
    {
        // DDM request datastream.
        byte[] excsatReq = new byte[]
        {
            0x00, 0x75, (byte)0xD0, 0x01, 0x00, 0x00, 0x00, 0x6F, 0x10, 0x41, 0x00, 0x0B, 0x11, 0x47, 0x00, 0x07,
            0x00, 0x09, (byte)0xD8, (byte)0xC1, (byte)0xE2, 0x00, 0x60, 0x14, 0x04, 0x14, 0x03, 0x00, 0x03, 0x14, 0x23, 0x00,
            0x03, 0x14, 0x05, 0x00, 0x03, 0x14, 0x06, 0x00, 0x03, 0x14, 0x07, 0x00, 0x03, 0x14, 0x74, 0x00,
            0x05, 0x14, 0x58, 0x00, 0x01, 0x14, 0x57, 0x00, 0x03, 0x14, 0x0C, 0x00, 0x03, 0x14, 0x19, 0x00,
            0x03, 0x14, 0x1E, 0x00, 0x03, 0x14, 0x22, 0x00, 0x03, 0x24, 0x0F, 0x00, 0x03, 0x14, 0x32, 0x00,
            0x03, 0x14, 0x33, 0x00, 0x03, 0x14, 0x40, 0x00, 0x01, 0x14, 0x3B, 0x00, 0x03, 0x24, 0x07, 0x00,
            0x03, 0x14, 0x63, 0x00, 0x03, 0x14, 0x65, 0x00, 0x03, 0x14, 0x3C, 0x00, 0x03, 0x14, 0x7F, 0x00,
            0x04, 0x14, (byte)0xA0, 0x00, 0x04
        };

        // DDM expected reply datastream.
        byte[] expectedRep = new byte[]
        {
            0x00, 0x71, (byte)0xD0, 0x03, 0x00, 0x00, 0x00, 0x6B, 0x14, 0x43, 0x00, 0x07, 0x11, 0x47, (byte)0xD8, (byte)0xC1,
            (byte)0xE2, 0x00, 0x60, 0x14, 0x04, 0x14, 0x03, 0x00, 0x03, 0x14, 0x23, 0x00, 0x03, 0x14, 0x05, 0x00,
            0x03, 0x14, 0x06, 0x00, 0x03, 0x14, 0x07, 0x00, 0x03, 0x14, 0x74, 0x00, 0x05, 0x14, 0x58, 0x00,
            0x01, 0x14, 0x57, 0x00, 0x03, 0x14, 0x0C, 0x00, 0x03, 0x14, 0x19, 0x00, 0x03, 0x14, 0x1E, 0x00,
            0x03, 0x14, 0x22, 0x00, 0x03, 0x24, 0x0F, 0x00, 0x03, 0x14, (byte)0xA0, 0x00, 0x04, 0x14, 0x32, 0x00,
            0x03, 0x14, 0x33, 0x00, 0x03, 0x14, 0x40, 0x00, 0x01, 0x14, 0x3B, 0x00, 0x03, 0x24, 0x07, 0x00,
            0x03, 0x14, 0x63, 0x00, 0x03, 0x14, 0x65, 0x00, 0x03, 0x14, 0x3C, 0x00, 0x03, 0x14, 0x7F, 0x00,
            0x04
        };

        OutputStream os = null;
        InputStream is = null;
        try
        {
            jpingThread_ = new JPingThread();
            jpingDaemon_ = new Thread(jpingThread_, "AS400JPingDaemon");
            jpingDaemon_.setDaemon(true);
            jpingDaemon_.start();

            synchronized (this)
            {
                try
                {
                    if (Trace.traceOn_)
                        Trace.log(Trace.INFORMATION, "Ping Timeout:  " + time_ + "(ms)");

                    wait(time_);
                }
                catch (InterruptedException ie)
                {
                    if (Trace.traceOn_)
                        Trace.log(Trace.ERROR, "Unexpected exception.", ie);
                }
            }

            // If the Thread has not returned within the timeout period,
            // interrupt the thread and fail the ping attempt.
            if (ddmSocket_ == null)
            {
                jpingDaemon_.interrupt();
                throw new Exception("Ping Timeout occurred.");
            }
            else  // The Thread has returned within the timeout period.
            {
                // Copied from PortMapper.setSocketProperties():
                if (Trace.traceOn_)
                {
                  Trace.log(Trace.DIAGNOSTIC, "Socket properties:");
                  try { Trace.log(Trace.DIAGNOSTIC, "    Remote address: " + ddmSocket_.getInetAddress()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Remote port:", ddmSocket_.getPort()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Local address: " + ddmSocket_.getLocalAddress()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Local port:", ddmSocket_.getLocalPort()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Keep alive:", ddmSocket_.getKeepAlive()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Receive buffer size:", ddmSocket_.getReceiveBufferSize()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    Send buffer size:", ddmSocket_.getSendBufferSize()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    So linger:", ddmSocket_.getSoLinger()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    So timeout:", ddmSocket_.getSoTimeout()); } catch (Throwable t) {}
                  try { Trace.log(Trace.DIAGNOSTIC, "    TCP no delay:", ddmSocket_.getTcpNoDelay()); } catch (Throwable t) {}
                }

                os = ddmSocket_.getOutputStream();
                os.write(excsatReq);
                os.flush();

                is = ddmSocket_.getInputStream();
                byte[] excsatRep = new byte[113];
                int numBytesRead = is.read(excsatRep);
                if (numBytesRead < excsatRep.length)
                {
                  Trace.log(Trace.ERROR, "Unexpected DDM server response.", excsatRep);
                  throw new Exception("Unexpected DDM server response.");
                }

                for (int i=0; i<113; ++i)
                {
                    // If the reply matched our expected reply, continue.  Otherwise throw an exception, which will bubble up to the ping(int) method and get handled properly.
                    if (excsatRep[i] != expectedRep[i])
                    {
                        Trace.log(Trace.ERROR, "Unexpected DDM server response.", excsatRep);
                        throw new Exception("Unexpected DDM server response.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (e.getMessage().equals("Ping Timeout occurred."))
                throw e;
            else
                throw new Exception("Unexpected exception.");
        }
        finally
        {
          // Close the input/output streams and the socket.
          if (is != null) {
            try { is.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
          if (os != null) {
            try { os.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
          if (ddmSocket_ != null) {
            try { ddmSocket_.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
        }
    }

    /**
     Set the PrintWriter to log ping information to.
     @param  stream  The OutputStream.
     *
     @exception  IOException  If an error occurs while accessing the stream.
     **/
    public void setPrintWriter(OutputStream stream) throws IOException
    {
        if (stream == null)
            throw new NullPointerException("stream");

        writer_ = new PrintWriter(stream, true);
    }

    /**
     Set the timeout period in milliseconds.  The default timeout period is 20000 (20 seconds).
     @param  time  The timeout period.
     **/
    public void setTimeout(long time)
    {
        time_ = time;
    }

    // Not used.
    // /**
    // Sets the length of the data in the datastream.
    // @param  length  The length of the data (1 - 1000).  The default is 10.
    // **/
    //void setLength(int length)
    //{
    //    if (length < 1 || length > 1000)
    //        throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    //    length_ = length;
    //}

    // Not used.
    // /**
    // Sets whether the echo is on.
    // @param  echo  true if the service should echo back the packet received, false otherwise.  The default is false.
    // **/
    //void setUseEcho(boolean echo)
    //{
    //    echo_ = echo;
    //}

    /**
     JPingThread is the inner class that tries to create the Socket connection to the system using a Thread.  An inner class is used so that the user does not see that the AS400JPing class implements Runnable and does not see the run() method and they don't try to call that method in their applications.
     **/
    private class JPingThread implements Runnable
    {
        /**
         A Thread is started to do the Socket creation.  The socket creation will hang if the system is not responding.  To alleviate the long hang times, if this Thread does not return within the timeout period, it is assumed the system is unreachable.  The hang could also be attributed to network speed.  The default timeout period is 20000ms (20 sec).
         **/
        public void run()
        {
            try
            {
                if (service_ == AS400.RECORDACCESS)
                {
                    // Create a socket to the DDM Server.
                    ddmSocket_ = new Socket(systemName_, 446); // DRDA is on port 446
                }
                else
                {
                    // Create a socket to the service (let PortMapper figure out which port).
                    socketContainer_ = PortMapper.getServerSocket(systemName_, service_, useSSL_, socketProperties_, false);
                }
            }
            catch (Exception e)
            {
                if (Trace.traceOn_)
                    Trace.log(Trace.ERROR, "Unexpected exception.", e);
                // Note: Calling interrupt() on this JPingThread object, won't cause the JPingThread object to incur an InterruptedException.
            }
            finally
            {
              synchronized (AS400JPing.this)
              {
                AS400JPing.this.notifyAll();
              }
            }
        }
    }
}
