///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxPeerConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;



/**
The PxPeerConnection class represents the connection 
to a proxy server.  This acts as the interface between
multiple peer proxy servers.
**/
class PxPeerConnection
extends PxClientConnectionAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxPeerConnection object.

@param proxyServer  The proxy server.
**/
    public PxPeerConnection (String proxyServer)
    {               
        super (proxyServer, false);
    }

          

/**
Configures the proxy server.

@param config    The configuration.
**/
    public void configure (PSConfig config)
    {
        PxConfigReqCV request = new PxConfigReqCV (config);
        send (request);
    }



/**
Returns the load on the proxy server. 

@return                 The load.
**/
    public PSLoad load ()
    {
        PxLoadReqCV request = new PxLoadReqCV ();
        try {
            return (PSLoad) sendAndReceive (request);
        }
        catch (InvocationTargetException e) {            
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        }
    }



    public void open(String proxyServer, boolean secure)
    {
        super.open(proxyServer, secure);
       
        // We need to reinitialize the factory everytime the
        // connection is opened.
        PxDSFactory factory = getFactory();
        factory.register (new PxIntParm ());
        factory.register (new PxLoadRepCV ());        
    }


}

