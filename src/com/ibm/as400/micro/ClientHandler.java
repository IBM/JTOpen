///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ClientHandler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import java.net.*;
import java.util.*;

import com.ibm.as400.access.Trace;

/**
 *  The ClientHandler class handles all incoming JdbcMe requests.
 **/
class ClientHandler extends Thread
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    /*
    NOTE:  There is one serious issue to changing this
           to use a Hasttable as would seem a good choice.
           That is that the ResultSet object is not directly
           maintained here.  The ResultSet is a 'part' of the
           statement.  Therefore when you get an object id in
           and that object is a ResultSet, you are going to
           make it much harder to recognize that object.
 
           Given the fact that 90% of operations are to be done
           on the ResultSet, I am not so sure that we want to
           make that tradeoff.
 
           Perhaps we want three Hashtables so that we have
           direct access to the ResultSet handles here (and we
           would need that as we map handles to the objects),
           but then it becomes more cumbersome throughout to
           track when the ResultSet closes and such.  The current
           schema allows the JDBC driver to deal with many of
           those details.
    */
    private Socket socket_;
    private MicroDataInputStream in_;
    private MicroDataOutputStream out_;
    private int functionId_;
    
    String serviceName_ = "com.ibm.as400.micro.JdbcMeService";
    
    private Service service_;

    /**
    Constuctor - handles instantiating the data input and output
    streams, creating the object storage mechanism and starting the
    thread.
    <P>
    If an exception is thrown by the constructor, the caller is
    responsible for closing the input socket.  If no exception is
    thrown, this object takes over ownership of the socket and is
    responsible for closing it.
    **/
    public ClientHandler(Socket socket, MicroDataInputStream in, MicroDataOutputStream out, int function) throws IOException 
    {
        socket_ = socket;
        functionId_ = function;
        in_ = in;
        out_ = out;
        
        try
        {
            service_ = (Service) Class.forName(serviceName_).newInstance();
            service_.setDataStreams(in, out);
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Server could not instantiate service " + serviceName_, e);
        }
    }    


    public void run()
    {
        try
        {
            do
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.PROXY, "Trying service " + serviceName_ + " for Function id " + Integer.toHexString(functionId_));
                
                if (service_.acceptsRequest(functionId_))
                    break;
                
                if (Trace.isTraceOn())
                    Trace.log(Trace.PROXY, "Servicing function id " + Integer.toHexString(functionId_));

                // Request the function execution.
                if (service_ != null)
                {
                    service_.handleRequest(functionId_);
                    
                    if (Trace.isTraceOn())
                        Trace.log(Trace.PROXY, "Service complete");
                }
                else
                {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.PROXY, "Didn't complete last service...");
                }

                // Always flush the buffer at the end of the request.
                out_.flush();
            } while ( (functionId_ = in_.readInt()) != MEConstants.DISCONNECT);

            // TODO:  A resource cleanup call for each service is needed here...

        }
        catch (EOFException eof)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "EOF: Client disconnected", eof);
        }
        catch (IOException ioe)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "IOException detected - finally block handles cleanup.", ioe);
        }
        finally
        {
            try
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.PROXY, "Client handler shutting down.");

                socket_.close();
            }
            catch (IOException e)
            {
                // do nothing.
            }
        }
    }
}

