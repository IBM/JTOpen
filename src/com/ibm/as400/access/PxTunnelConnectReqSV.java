///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxTunnelConnectReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.util.Hashtable;   

// The PxTunnelConnectReqSV class represents the server view of a connect request for tunneling.
class PxTunnelConnectReqSV extends PxReqSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	// MRI.
	private static final String PROXY_CONNECTION_ACCEPTED_ = ResourceBundleLoader.getText("PROXY_CONNECTION_ACCEPTED");
		
	// Private data.
	private Hashtable    clientIds_;  
	private PSTunnelController controller_;

	/**
	Constructs a PxTunnelConnectReqSV object.
	                                                             
	@param controller               The controller.
	@param clientIds                The list of client ids.
	**/
	public PxTunnelConnectReqSV (PSTunnelController controller,
								 Hashtable clientIds) 
	{
		super (ProxyConstants.DS_CONNECT_TUNNEL_REQ);

		controller_             = controller;
		clientIds_              = clientIds;  
	}

	// Processes the request.
	// @return  The corresponding reply, or null if none.
	public PxRepSV process()
	{
	        PxRepSV reply;
		PSTunnelConnection connection = new PSTunnelConnection (controller_.getConnectionId()); 
		long clientId = controller_.getNextClientId();                                          
		clientIds_.put(new Long(clientId), connection);                             
		reply = new PxAcceptRepSV();                                                                   
		reply.setClientId(clientId);    

		//Verbose.println(ResourceBundleLoader.substitute(PROXY_CONNECTION_ACCEPTED_, new Object[] { controller_, controller_.getClientAddress(), Long.toString(controller_.getConnectionId())} ));
		return reply;
	}
}


