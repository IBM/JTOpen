///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400JPing.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Date;
import java.net.Socket;


/**
 *  The AS400JPing class is used to determine if OS/400 services are running.
 *  <p>
 *  Here is an example of calling AS400JPing within a Java program to ping the AS400 Remote Command Service:
 *  <br>
 *  <BLOCKQUOTE><PRE>
 *  AS400JPing pingObj = new AS400JPing("myAS400", AS400.COMMAND, false);
 *  if (pingObj.ping())
 *     System.out.println("SUCCESS");
 *  else
 *     System.out.println("FAILED");
 *  </PRE></BLOCKQUOTE>
 *
 **/
public class AS400JPing
{
  private static final String copyright = "Copyright (C) 1999-2003 International Business Machines Corporation and others.";

   /**
    *  Constant for pinging all the OS/400 services.    
    **/
   public final static int ALL_SERVICES = 99;      // The default service is 99, which pings all the servers.

   private String systemName_;
   private int    service_ = ALL_SERVICES;
   private int    length_ = 10;
           
   private boolean    echo_ = false;
   private SSLOptions useSSL_ = null;
   private long       time_ = 20000;            //$A1D   $A2A

   private PrintWriter     writer_;
   private SocketContainer sc_;                   // SocketContainer to all OS/400 Services but the DDM Server.      $A2A
   private Socket          s_;                    // Socket to the DDM Server.                                       $A2A
   private Thread          thread_;               // Thread to handle timeout values.                                $A2A
   private JPingThread     jpingThread;           // Inner class that implements runnable.                           $A2A
   private SocketProperties socketProperties_ = new SocketProperties();

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader loader_;

   
   /**
    *  Constructs an AS400JPing object with the specified <i>systemName</i>.
    *
    *  A JPing object created with this constructor
    *  will ping all of the OS/400 services when ping() is called.
    *
    *  @param systemName The server to ping.  The <i>systemName</i> string can be
    *                    in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), 
    *                    or IP address (eg. "9.1.2.3").
    *
    **/
   public AS400JPing(String systemName)
   {  
      this(systemName, ALL_SERVICES, false);
   }

   /**
    *  Constructs an AS400JPing object with the specified <i>systemName</i> and <i>service</i>.
    *
    *  @param systemName The server to ping.  The <i>systemName</i> string can be
    *                    in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), 
    *                    or IP address (eg. "9.1.2.3").
    *  @param service The AS/40 service to ping.  One of the following constants: AS400.FILE, 
    *                 AS400.DATABASE, AS400.COMMAND, AS400.SIGNON, AS400.CENTRAL, AS400.DATAQUEUE, 
    *                 AS400.RECORDACCESS, AS400.PRINT, or ALL_SERVICES.
    *
    *  @see com.ibm.as400.access.AS400
    *
    **/
   public AS400JPing(String systemName, int service)
   {  
      this(systemName, service, false);
   }
   

   /**
    *  Constructs an AS400JPing object.
    *
    *  @param systemName The server to ping.  The <i>systemName</i> string can be
    *                    in 3 forms:  shortname (eg. "myAS400"), longname (eg. "myAS400.myCompany.com"), 
    *                    or IP address (eg. "9.1.2.3").
    *  @param service  The OS/400 service to ping.  One of the following constants: AS400.FILE, AS400.DATABASE, 
    *                  AS400.COMMAND, AS400.SIGNON, AS400.CENTRAL, AS400.DATAQUEUE, 
    *                  AS400.RECORDACCESS, AS400.PRINT, or ALL_SERVICES.
    *  @param useSSL  true if the pinging the SSL port for the service, false otherwise.  The default is false.
    *
    *  @see com.ibm.as400.access.AS400
    *
    **/
   public AS400JPing(String systemName, int service, boolean useSSL)
   {  
      if (systemName == null)
         throw new NullPointerException("systemName");

      if (service < AS400.FILE || (service > AS400.SIGNON && service != ALL_SERVICES))
         throw new ExtendedIllegalArgumentException("service", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

      systemName_ = systemName;
      service_ = service;
      useSSL_ = useSSL ? new SSLOptions() : null;
   }


   /**
    *  Ping the iSeries server.  
    *
    *  @return true if all of the services can be pinged successfully, and false otherwise.
    *
    **/
   public boolean ping()
   {
      if (service_ == ALL_SERVICES)
      {
         boolean rtn = true;
         for (int i=AS400.FILE; i<=AS400.SIGNON; ++i)
         {
            rtn = rtn && ping(i);
            if (rtn == false)   // if we get a failed ping, we want to return right away,
               return rtn;      // instead of pinging the other servers.
         }

         return rtn;
      }
      else
      {
         return ping(service_);
      }
   }


   /**
    *  Ping a specific OS/400 service.  One of the following constants: AS400.FILE, AS400.DATABASE, 
    *  AS400.COMMAND, AS400.SIGNON, AS400.CENTRAL, AS400.DATAQUEUE, AS400.RECORDACCESS, AS400.PRINT, 
    *  or ALL_SERVICES.  
    *
    * @return true if the service can be pinged successfully, and false otherwise.
    *
    **/
   public synchronized boolean ping(int service)
   {
      try
      {
         if (Trace.isTraceOn())
         { 
            Trace.log(Trace.INFORMATION, "Ping System:   " + systemName_);
            Trace.log(Trace.INFORMATION, "Ping Service:  " + AS400.getServerName(service));
            Trace.log(Trace.INFORMATION, "Packet Length: " + length_);
            Trace.log(Trace.INFORMATION, "Echo Packet:   " + ((echo_) ? "on" : "off"));
            Trace.log(Trace.INFORMATION, "Ping SSL Port: " + ((useSSL_ != null) ? "yes" : "no"));
         }

         service_ = service;                      //$A2A

         // DDM does not use the Client Access Datastreams, so we have to use a different
         // datastream to determine if the DDM server is connected.
         if (service == AS400.RECORDACCESS)
            pingDDM();
         else
         {  
            jpingThread = new JPingThread();                                                //$A2A
            
            synchronized(this)                                                              //$A2A
            {                                                                               //$A2A
               try                                                                          //$A2A
               {                                                                            //$A2A
                  if (Trace.isTraceOn())                                                    //$A2A
                     Trace.log(Trace.INFORMATION, "Ping Timeout:  " + time_ + "(ms)" );     //$A2A
                                                                                            //$A2A
                  wait(time_);                                                              //$A2A
               }                                                                            //$A2A
               catch(InterruptedException ie)                                               //$A2A
               {                                                                            //$A2A
                  if (Trace.isTraceOn())                                                    //$A2A
                     Trace.log(Trace.ERROR, "Unexpected exception.", ie);                   //$A2A
               }                                                                            //$A2A
            }                                                                               //$A2A
            
            // If the Thread has not returned within the timeout period, stop the thread    //$A2A
            // and fail the ping attempt.                                                   //$A2A
            if (sc_ == null)                                                                //$A2A
            {                                                                               //$A2A
               thread_.stop();                                                              //$A2A
               throw new Exception("Ping timeout occurred.");                               //$A2A
            }                                                                               //$A2A
            else  // The Thread has returned within the timeout period.                     //$A2A
            { 
               InputStream inStream = sc_.getInputStream();
               OutputStream outStream = sc_.getOutputStream();
               
               // buffer is the data that is pinged back and forth to the server.
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
               
               // Close the socket and Input/Output Streams.
               try
               {
                  outStream.close();
                  inStream.close();
                  sc_.close();
               }
               catch(Exception e)
               {
                  Trace.log(Trace.ERROR, "Problem closing the streams and socket connections.");
               }
            }
         }
         
         // If we have gotten this far without an exception, then the iSeries system 
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
         
         if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, sse);
            
         return false;
      }
      catch (Exception e)
      {
         if (writer_ != null)
            writer_.println(loader_.substitute(loader_.getText("PROP_NAME_AJP_FAILED"),  new String[] { AS400.getServerName(service), ((useSSL_ != null) ? "-s" : "") } ) );
         
         if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, e);

         return false;
      }
   }

   /**
    *  Ping the DDM Server.
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

      try
      {
         jpingThread = new JPingThread();                                                 //$A2A
         
         synchronized(this)                                                               //$A2A
         {                                                                                //$A2A
            try                                                                           //$A2A
            {                                                                             //$A2A
               if (Trace.isTraceOn())                                                     //$A2A
                     Trace.log(Trace.INFORMATION, "Ping Timeout:  " + time_ + "(ms)");    //$A2A
                                                                                          //$A2A
               wait(time_);                                                               //$A2A
            }                                                                             //$A2A
            catch(InterruptedException ie)                                                //$A2A
            {                                                                             //$A2A
               if (Trace.isTraceOn())                                                     //$A2A
                  Trace.log(Trace.ERROR, "Unexpected exception.", ie);                    //$A2A
            }                                                                             //$A2A
         }                                                                                //$A2A

         // If the Thread has not returned within the timeout period, stop the thread
         // and fail the ping attempt.
         if (s_ == null)                                                                  //$A2A
         {                                                                                //$A2A
            thread_.stop();                                                               //$A2A
            throw new Exception("Ping Timeout occurred.");                                //$A2A
         }                                                                                //$A2A
         else  // The Thread has returned within the timeout period.                      //$A2A
         {
            OutputStream os = s_.getOutputStream();
            os.write(excsatReq);
            os.flush();
            
            InputStream is = s_.getInputStream();
            byte[] excsatRep = new byte[113];
            is.read(excsatRep);
            
            // Close the input/output streams and the socket.
            is.close();
            os.close();
            s_.close();
            
            for (int i=0; i<113; ++i)
            {
               // If the reply matched our expected reply, continue.  Otherwise throw an exception, which will
               // bubble up to the ping(int) method and get handled properly.
               if (excsatRep[i] != expectedRep[i])
               {  
                  Trace.log(Trace.ERROR, "Unexpected ddm server response.");
                  throw new Exception();
               }
            }
         }
      }
      catch(Exception e)
      {  
         if (e.getMessage().equals("Ping Timeout occurred."))
            throw e;
         else
            throw new Exception("Unexpected exception.");
      }
   }
   


   /**
    *  Set the PrintWriter to log ping information to.
    *
    *  @param stream The OutputStream.
    *
    *  @exception  IOException  If an error occurs while accessing the stream. 
    *
    **/
   public void setPrintWriter(OutputStream stream) throws IOException
   {
      if (stream == null)
         throw new NullPointerException("stream");

      writer_ = new PrintWriter(stream, true);
   }

   
   /**
    *  Set the timeout period in milliseconds.  The default timeout period is 20000 (20 sec).
    *
    *  @param time The timeout period.
    **/
   public void setTimeout(long time)                  //$A1D    $A2A
   {
      time_ = time;
   }

   /**
    *  Sets the length of the data in the datastream.
    *
    *  @param length  The length of the data (1 - 1000).  The default is 10.
    *
    **/
   void setLength(int length)
   {
      if (length < 1 || length > 1000)
         throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      length_ = length;
   }

   /**
    *  Sets whether the echo is on.
    *
    *  @param echo  true if the service should echo back the packet received, false otherwise.  The default is false.
    *
    **/
   void setUseEcho(boolean echo)
   {
      echo_ = echo;
   }

   
   
   /**
    *  JPingThread is the inner class that tries to create the Socket connection to the iSeries system service
    *  using a Thread.  An inner class is used so that the user does not see that the AS400JPing
    *  class implements Runnable and does not see the run() method and they don't try to call that
    *  method in their applications.
    *
    **/
   private class JPingThread implements Runnable            //$A2A
   {
      JPingThread()
      {  
         thread_ = new Thread(this);
         thread_.start();
      }
      
      /**
       *  A Thread is started to do the Socket creation.  The socket creation
       *  will hang if the server is not responding.  To aleviate the long
       *  hang times, if this Thread does not return within the timeout period,
       *  it is assumed the server is unreachable.  The hang could also be 
       *  attributed to network speed.  The default timeout period is 20000ms. (20 sec)
       *
       **/
      public void run()                    //$A2A
      {
         try
         {  
            if (service_ == AS400.RECORDACCESS)
            {
               // Create a socket to the DDM Server
               s_ = new Socket(systemName_, 446);
            }
            else
            {
               // Create a socket to the service.
               sc_ = PortMapper.getServerSocket(systemName_, service_, useSSL_, socketProperties_);
            }
         }
         catch (Exception e)
         {
            if (Trace.isTraceOn())
               Trace.log(Trace.ERROR, "Unexpected exception.", e);
         }
         
         synchronized (AS400JPing.this)
         {  
            AS400JPing.this.notifyAll();
         }
      }
   }

}



