///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PSServerSocketContainerAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



/**
The PSServerSocketContainerAdapter class represents a wrapper 
around a server socket.  The reason that we need to use a wrapper 
is to avoid directly referring to a class that may or may not exist 
in the user's environment (e.g. SSLServerSocket).  This will avoid 
class loader errors when such classes are not found and they are not 
needed.
**/
abstract class PSServerSocketContainerAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private ServerSocket  serverSocket_;



/**
Constructs a PSServerSocketContainerAdapter object.

@param serverSocket   The server socket.
**/
    protected PSServerSocketContainerAdapter (ServerSocket serverSocket)
    {        
        serverSocket_ = serverSocket;

        String x = Copyright.copyright;
    }



/**
Accepts a connection to the socket.

@return The socket for the connection.
                       
@exception IOException If an error occurs.
**/
    public Socket accept ()
        throws IOException
    {
        return serverSocket_.accept ();
    }



/**
Closes the socket.

@exception IOException If an error occurs.
**/
    public void close ()
        throws IOException
    {
        serverSocket_.close ();
    }



/**
Returns the port on which the socket is listening.

@return The port on which the socket is listening.
**/
    public int getLocalPort ()
    {
        return serverSocket_.getLocalPort ();
    }


/**
Indicates if the socket is secure.
**/
    public boolean isSecure ()
    {
        return false;
    }


}
