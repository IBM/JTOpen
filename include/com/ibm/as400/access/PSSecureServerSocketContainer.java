///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: PSSecureServerSocketContainer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import com.ibm.sslight.SSLContext;
import com.ibm.sslight.SSLException;
import com.ibm.sslight.SSLServerSocket;
import com.ibm.sslight.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



/**
The PSSecureServerSocketContainer class represents a wrapper 
around an SSL server socket.
**/
class PSSecureServerSocketContainer
extends PSServerSocketContainerAdapter
{

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


    // MRI.
    private static final String NAME_               = ResourceBundleLoader.getText ("PROXY_SERVER_SECURE_CONTAINER");




/**
Constructs a PSSecureServerSocketContainer object.

@param port   The port.

@exception IOException  If an error occurs.
**/
    public PSSecureServerSocketContainer (int port, String keyringName, String keyringPwd)      //$B1C
        throws IOException
    {        
        super (createSSLServerSocket (port, keyringName, keyringPwd));                          //$B1C
    }



/**
Accepts a connection to the socket.

@return The socket for the connection.
                       
@exception IOException If an error occurs.
**/
    public Socket accept ()
        throws IOException
    {
       boolean   connected = false;                                                             //$B1A
       SSLSocket socket    =  null;                                                             //$B1A
       
       // Continue to accept connections even if there is an SSLException.  That exception
       // may be due to an invalid Keyring.  The detailed error trace will give more 
       // information as to why the proxy did not accept the SSL connection.
       // If a connection is made, then we return the SSLSocket.                                //$B1A
       while (!connected)                                                                       //$B1A
       {
          try 
          {
             socket = (SSLSocket) ((SSLServerSocket) serverSocket_).accept (null);              //$B1C
             
             if (Trace.isTraceOn ())
                PxSecureSocketContainer.traceSSLSocket (socket);
             
             connected = true;                                                                  //$B1A
          }
          catch (SSLException e) 
          {
             if (Trace.isTraceOn())                                                             //$B1C
                Trace.logSSL(Trace.DIAGNOSTIC, e.getCategory(), e.getError(), e.getInt1());     //$B1C

             connected = false;                                                                 //$B1A
          }
       }
       return socket;                                                                           //$B1C
    }



/**
Creates an SSL server socket.

@param port     The port.
@return         The server socket.

@exception IOException  If an error occurs.
**/
    private static ServerSocket createSSLServerSocket (int port, String keyringName, String keyringPwd)   //$B1C
        throws IOException
    {
        SSLContext context = PxSecureSocketContainer.initializeServerSSLContext (keyringName, keyringPwd);//$B1C
        if (context == null)                                                                              //$B1A
           return null;                                                                                   //$B1A
        else                                                                                              //$B1A
           return new SSLServerSocket (port, context);
    }



/**
Indicates if the socket is secure.
**/
    public boolean isSecure ()
    {
        return true;
    }


/**
Returns the name of this container.

@return The name of this container.
**/
    public String toString ()
    {
        return NAME_;
    }



}
