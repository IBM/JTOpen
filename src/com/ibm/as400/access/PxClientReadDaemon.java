///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxClientReadDaemon.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

//
// Tunneling -- HTTP is stateless which means there is no persistent
// connection between the client and server.  With tunneling, a connection
// is made, data sent and received, then disconnected.  This class now
// has two personalities:
//   1) normal proxy -- start a background thread that sits on
//      on the connection waiting for data.  When
//      data arrives, handle it (possible asnychronous processing).
//   2) tunnel proxy -- no additional thread is started.  When it is time
//      to read data a connection is passed to this class.
//

import java.util.Hashtable;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
// @B1D import java.net.SocketException;                                                    // @A1A



/**
The PxClientReadDaemon class represents a read daemon for reading
replies from the proxy server.
**/
class PxClientReadDaemon
extends StoppableThread
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private PxDSFactory                 factory_                            = new PxDSFactory();
    private InputStream                 input_;
    private InvocationTargetException   invocationTargetException_          = null;
    private IOException                 ioException_                        = null;
    private Hashtable                   replies_                            = new Hashtable();
    private boolean                     running_                            = false;
    private boolean                     started_                            = false;


    // @D1a new c'tor
    // Use this c'tor when Tunneling
    public PxClientReadDaemon ()
    {
        super("Proxy client read daemon-" + newId());

        // Mark this as a daemon thread so that its running does
        // not prevent the JVM from going away.
        setDaemon(true);
    }


    public PxClientReadDaemon (InputStream input)
    {
        super("Proxy client read daemon-" + newId());

        // Mark this as a daemon thread so that its running does
        // not prevent the JVM from going away.
        setDaemon(true);

        input_ = input;
    }


    public PxDSFactory getFactory()
    {
        return factory_;
    }


    // Traditional proxy uses this method.
    public PxRepCV getReply(long correlationId)
        throws InvocationTargetException, IOException
    {
        Long key = new Long(correlationId);

        // Loop and poll, until the correct reply has been read by the
        // read daemon thread.
        while(true) {

            // If any relevant exceptions were caught by the read daemon
            // thread, then throw them now.
            if (ioException_ != null)
                throw ioException_;
            if (invocationTargetException_ != null)
                throw invocationTargetException_;

            // Look in the hashtable to see if the correct reply has been
            // read.
            synchronized(this) {
                if (replies_.containsKey(key)) {
                    PxRepCV reply = (PxRepCV)replies_.get(key);
                    replies_.remove(key);
                    return reply;
                }

                // If not found, but the read daemon is still running,
                // then wait patiently and efficiently.
                if (running_ || !started_) {
                    try {
                        wait();
                    }
                    catch(InterruptedException e) {
                        // Ignore.
                    }
                }

                // If not found, but the read daemon has stopped, then
                // give up hope, something strange has happened.
                else  {
                    if (Trace.isTraceErrorOn())
                        Trace.log(Trace.ERROR, "Proxy read daemon stopped, but replies are expected.");
                    throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
                }
            }
        }
    }



    public void register(PxDSRV datastream)
    {
        factory_.register(datastream);
    }


    // A thread is created only when using traditional proxy.
    public void run()
    {
        started_ = true;
        running_ = true;
        // @B1D int exceptionCounter = 0;                                                   // @A1A

        PxRepCV reply;
        try {
            while (canContinue()) {
                // @B1D try {                                                               // @A1A
                    reply = (PxRepCV)factory_.getNextDS(input_);
                    if (Trace.isTraceProxyOn())
                        reply.dump (Trace.getPrintWriter ());

                    // @B1D // We had a successful read, reset the exception counter.       // @A1A
                    // @B1D exceptionCounter = 0;                                           // @A1A

                    // If the correlation id is set, just store the reply
                    // in the hashtable.  This means that somebody is
                    // waiting for it and they will ask for it when the
                    // time is right.
                    long correlationId = reply.getCorrelationId();
                    if (correlationId >= 0) {
                        synchronized(this) {
                            replies_.put(new Long(correlationId), reply);
                            notifyAll();
                        }
                    }

                    // Otherwise, process it and forget about it!
                    else
                        reply.process();
                // @B1D }                                                                   // @A1A
                // @B1D catch(SocketException e) {                                          // @A1A
                // @B1D     // Ignore this.  Netscape is throwing this in certain           // @A1A
                // @B1D     // situations.  Try again and it will go away!                  // @A1A
                // @B1D     // If we get it a few times in a row, then rethrow it.          // @A1A
                // @B1D     if (++exceptionCounter >= 3)                                    // @A1A
                // @B1D         throw e;                                                    // @A1A
                // @B1D }                                                                   // @A1A
            }
        }
        catch(InvocationTargetException e) {
            invocationTargetException_ = e;
        }
        catch(IOException e) {

            // If an exception is thrown AND the thread was stopped safely,
            // then ignore the exception, i.e., assume the exception just
            // resulted in the socket being closed.
            if ((! wasStoppedSafely()) && (!(e instanceof EOFException))) {
                ioException_ = e;
                if (Trace.isTraceErrorOn ())
                    Trace.log(Trace.ERROR, "Ending read daemon", e);
            }

            // No need to throw exception (there is nobody to catch it!)
            // Simply, end the thread.
            synchronized(this) {
                notifyAll();
            }
        }

        running_ = false;
    }

    // @D1a new method.
    // This method is used when tunneling.  The CID and stream to read are
    // passed each time we retrieve data from the server.
    public PxRepCV getReply(long CID, InputStream input_)
        throws InvocationTargetException, IOException
    {
    //  try
    //  {
           Long key = new Long(CID);

           if (replies_.containsKey(key))
           {
              PxRepCV reply = (PxRepCV)replies_.get(key);
              replies_.remove(key);
              return reply;
           }

           while(true)
           {
              PxRepCV reply;
              reply = (PxRepCV)factory_.getNextDS(input_);

              if (Trace.isTraceProxyOn())
                 reply.dump (Trace.getPrintWriter ());


                    // If the correlation id is set, just store the reply
                    // in the hashtable.  This means that somebody is
                    // waiting for it and they will ask for it when the
                    // time is right.
              long correlationId = reply.getCorrelationId();
              if (correlationId != CID)
                 replies_.put(new Long(correlationId), reply);
              else
              {
                 reply.process();
                 return reply;
              }
           }
    //  }
    //  catch(InvocationTargetException e)
    //  {
    //     invocationTargetException_ = e;
    //  }
    //  catch(IOException e)
    //  {
    //     if (Trace.isTraceErrorOn ())
    //     Trace.log(Trace.ERROR, "Ending read daemon", e);
    //
    //  }
    //  running_ = false;
    //  return null;
    }


}

