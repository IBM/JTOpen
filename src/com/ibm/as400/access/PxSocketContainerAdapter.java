///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxSocketContainerAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;



/**
The PxSocketContainerAdapter class represents a wrapper around
a socket.  The reason that we need to use a wrapper is to avoid
directly referring to a class that may or may not exist in the
user's environment (e.g. SSLSocket).  This will avoid class loader
errors when such classes are not found and they are not needed.
**/
abstract class PxSocketContainerAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Socket  socket_;



/**
Constructs a PxSocketContainerAdapter object.

@param socket   The socket.
**/
    protected PxSocketContainerAdapter (Socket socket)
    {        
        socket_ = socket;

	     // Try to set the no delay option, but if that doesn't work, 
        // keep going.  OS/2 currently throws an exception.
	     try {
		      socket_.setTcpNoDelay (true);
        }
	     catch (SocketException e) {
            if (Trace.isTraceErrorOn ())
		          Trace.log(Trace.ERROR, "Socket exception setting no delay", e);
        }

	     // Try to set the SoLinger option, but if that doesn't work, 
        // keep going.
	     try {
		      if (socket.getSoLinger() != -1)
		          socket.setSoLinger(true, 60);
	     }
	     catch (SocketException e) {
		      Trace.log(Trace.ERROR, "Socket exception setting so linger", e);
        }
    }



/**
Closes the socket.

@exception IOException If an error occurs.
**/
    public void close ()
        throws IOException
    {
        socket_.close ();
        
    }



/**
Returns an input stream for reading from the socket.

@exception IOException If an error occurs.
**/
    public InputStream getInputStream ()
        throws IOException
    {
        return socket_.getInputStream ();
    }



/**
Returns an output stream for writing to the socket.

@exception IOException If an error occurs.
**/
    public OutputStream getOutputStream ()
        throws IOException
    {
        return socket_.getOutputStream ();
    }

}
