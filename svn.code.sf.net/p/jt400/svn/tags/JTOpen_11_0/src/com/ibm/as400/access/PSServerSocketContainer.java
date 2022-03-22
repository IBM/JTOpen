///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSServerSocketContainer.java
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
import java.net.SocketException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;



/**
The PSServerSocketContainerAdapter class represents a wrapper 
around a server socket.
**/
class PSServerSocketContainer
extends PSServerSocketContainerAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // MRI.
    private static final String NAME_               = ResourceBundleLoader.getText ("PROXY_SERVER_CONTAINER");



/**
Constructs a PSServerSocketContainer object.

@param port   The port.
 * @throws  IOException  If an error occurs while communicating with the system.
**/
    public PSServerSocketContainer (int port)
        throws IOException
    {        
        super (createServerSocket (port, false));
    }


    /**
Constructs a PSServerSocketContainer object.

@param port   The port.
 * @throws  IOException  If an error occurs while communicating with the system.
**/
    public PSServerSocketContainer (int port, boolean secure)
        throws IOException
    {        
        super (createServerSocket (port, secure));
    }



    

/**
Creates the server socket. 
**/    
//
//  Implementation note: This method is a workaround for an apparant 
//  Win32 bug (documented in Sun's bug parade).  It happens when 
//  creating a ServerSocket on a port previously used, even when
//  it was cleaned up properly.
//
    private static ServerSocket createServerSocket(int port, boolean secure)
        throws IOException
    {
        ServerSocket serverSocket = null;
        if (!secure) { 
        try {
            serverSocket = new ServerSocket(port);
        }
        catch(SocketException e) {
            // Didn't work?  Take a break and try again.
            try {
                Thread.sleep(500);
            }
            catch(InterruptedException e1) {
                // Ignore.
            }
            serverSocket = new ServerSocket(port);
        }
        } else {
          // If this has a problem, the exception will be thrown
         ServerSocketFactory sslServerSocketFactory = SSLServerSocketFactory.getDefault();
         serverSocket = sslServerSocketFactory.createServerSocket(port);

        }
        return serverSocket;
    }



/**
Returns the name of this socket container.

@return The name of this socket container.
**/
    public String toString ()
    {
        return NAME_;
    }



}
