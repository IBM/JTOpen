///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxEventRepSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventObject;



/**
The PxEventRepSV class represents the
server view of an event reply.
**/
class PxEventRepSV
extends PxRepSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    public PxEventRepSV (PxTable proxyTable, 
                         long proxyId, 
                         String listenerInterfaceName,
                         String listenerMethodName, 
                         EventObject event)
    {
        super (ProxyConstants.DS_EVENT_REP);
        addParm (new PxPxObjectParm (proxyId));
        addParm (new PxStringParm (listenerInterfaceName));
        addParm (new PxStringParm (listenerMethodName));
        addParm (new PxSerializedObjectParm (event));

        String x = Copyright.copyright;
    }



}
