///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxConfigReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;



/**
The PxConfigReqSV class represents the server
view of a configure request.
**/
class PxConfigReqSV
extends PxReqSV 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private PSConfig        config_;
    private PSController    controller_;



/**
Constructs a PxConfigReqSV object.

@param config           The configuration.
@param controller       The proxy server controller.
**/
    public PxConfigReqSV (PSConfig config, PSController controller)
    {
        super (ProxyConstants.DS_CONFIGURE_REQ);
        config_         = config;
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

                // Get the configuration properties from the parameter and apply
                // it to the current configuration.
                Properties configProperties = (Properties) ((PxParm) getParm (0)).getObjectValue ();
                config_.apply (configProperties);
            }
        }
        catch (UnknownHostException e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error while chaning configuration", e);
        }

        return null;
    }


}

