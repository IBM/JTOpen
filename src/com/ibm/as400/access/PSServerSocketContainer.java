///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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
**/
    public PSServerSocketContainer (int port)
        throws IOException
    {        
        super (createServerSocket (port));
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
    private static ServerSocket createServerSocket(int port)
        throws IOException
    {
        ServerSocket serverSocket = null;
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

        return serverSocket;
    }



/**
Returns the name of this socket container.

@return The name of this socket container.
**/
    public String toString ()
    {
        String x = Copyright.copyright;
        return NAME_;
    }



}
