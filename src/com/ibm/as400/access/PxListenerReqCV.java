///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxListenerReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxListenerReqCV class represents the
client view of a listener request.
**/
class PxListenerReqCV
extends PxReqCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxListenerReqCV object.

@param proxyId          The proxy id.
@param operation        The operation.
@param eventName        The event name.
**/
    public PxListenerReqCV (long proxyId, 
                            int operation, 
                            String eventName)
    {
        super (ProxyConstants.DS_LISTENER_REQ);

        addParm (new PxPxObjectParm (proxyId));
        addParm (new PxIntParm (operation));
        addParm (new PxStringParm (eventName));

        
    }



}
