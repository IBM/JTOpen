///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxEndReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;



/**
The PxEndReqSV class represents the
server view of a end request.
**/
class PxEndReqSV
extends PxReqSV 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String PROXY_SERVER_END_           = ResourceBundleLoader.getText ("PROXY_SERVER_END");
    private static final String PROXY_SERVER_END_REJECTED_  = ResourceBundleLoader.getText ("PROXY_SERVER_END_REJECTED");



    // Private data.
    private PSController   controller_;
    private ProxyServer             proxyServer_;



/**
Constructs a PxEndReqSV object.

@param proxyServer      The proxy server.
@param controller       The proxy server controller.
**/
    public PxEndReqSV (ProxyServer proxyServer, PSController controller)
    {
        super (ProxyConstants.DS_END_REQ);

        proxyServer_    = proxyServer;
        controller_     = controller;

        String x = Copyright.copyright;
    }



/**
Processes the request.

@return The corresponding reply, or null if none.
**/
    public PxRepSV process ()
    {
        InetAddress clientAddress = controller_.getClientAddress ();
        try {
            // Honor this request only if the client is running on the
            // same system as the server.
            if (InetAddress.getLocalHost ().equals (clientAddress)) {

                // Get the information from the datastream parameters.
                boolean endJVM = ((PxBooleanParm) getParm (0)).getBooleanValue ();            
 
                Verbose.println (ResourceBundleLoader.substitute (PROXY_SERVER_END_, clientAddress));
        
                if (endJVM)
                    System.exit (0);
                else 
                    proxyServer_.stop ();
            }
            else
                Verbose.println (ResourceBundleLoader.substitute (PROXY_SERVER_END_REJECTED_, clientAddress));
        }
        catch (UnknownHostException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error while ending proxy server", e);
            Verbose.println (ResourceBundleLoader.substitute (PROXY_SERVER_END_REJECTED_, clientAddress));
        }

        return null;
    }


}

