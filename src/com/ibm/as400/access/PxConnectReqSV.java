///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxConnectReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;

// The PxConnectReqSV class represents the server view of a connect request.
class PxConnectReqSV extends PxReqSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // MRI.
    private static final String PROXY_CONNECTION_ACCEPTED_ = ResourceBundleLoader.getText("PROXY_CONNECTION_ACCEPTED");
    private static final String PROXY_CONNECTION_REDIRECTED_ = ResourceBundleLoader.getText("PROXY_CONNECTION_REDIRECTED");
    private static final String PROXY_CONNECTION_REJECTED_ = ResourceBundleLoader.getText("PROXY_CONNECTION_REJECTED");

    // Private data.
    private PSController controller_;
    private PSLoad load_;
    private PSLoadBalancer loadBalancer_;
    private Vector threadGroup_;

    // Constructs a PxConnectReqSV object.
    // @param  threadGroup  The thread group.
    // @param  controller  The controller.
    // @param  load  The load.
    // @param  loadBalancer  The load balancer.
    // @param  secure  true if this is for a secure connection, false otherwise.
    public PxConnectReqSV(Vector threadGroup, PSController controller, PSLoad load, PSLoadBalancer loadBalancer)
    {
        super(ProxyConstants.DS_CONNECT_REQ);

        controller_ = controller;
        load_ = load;
        loadBalancer_ = loadBalancer;
        threadGroup_ = threadGroup;
    }

    // Processes the request.
    // @return  The corresponding reply, or null if none.
    public PxRepSV process()
    {
        // Read information from request.
        String modification = ((PxStringParm)getParm(0)).getStringValue();
        int rejectionCount = ((PxIntParm)getParm(1)).getIntValue();
        String clientLocale = ((PxStringParm)getParm(2)).getStringValue();

        // Decide whether to accept or reject.
        PxRepSV reply;
        String peer = loadBalancer_.accept(rejectionCount);

        // If accepted, then start up a thread for the connection.
        if (peer == null)
        {
            reply = new PxAcceptRepSV();

            PSConnection connection = new PSConnection(controller_.getConnectionId(), controller_.getConnectedSocket(), controller_.getInputStream(), controller_.getOutputStream(), load_);
            connection.start();
            threadGroup_.addElement(connection);

            Verbose.println(ResourceBundleLoader.substitute(PROXY_CONNECTION_ACCEPTED_, new Object[] { controller_, controller_.getClientAddress(), Long.toString(controller_.getConnectionId()) } ));
        }

        // If rejected, then do nothing.
        else
        {
            reply = new PxRejectRepSV(peer);
            if (peer.length() == 0)
            {
                Verbose.println(ResourceBundleLoader.substitute(PROXY_CONNECTION_REJECTED_, controller_, controller_.getClientAddress()));
            }
            else
            {
                Verbose.println(ResourceBundleLoader.substitute(PROXY_CONNECTION_REDIRECTED_, new Object[] { controller_, controller_.getClientAddress(), peer }));
            }
        }

        return reply;
    }
}
