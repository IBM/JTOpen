///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PSEventDispatcher.java
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
The PxMethodEventDispatcher class dispatches
events fired within the ProxyServer to the
client.
**/
class PSEventDispatcher
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private PSConnection    connection_;
    private PxTable         proxyTable_;
    private long            proxyId_;



/**
Constructs a PxMethodEventDispatcher object.

@param connection   The proxy server connection.
@param proxyId      The proxy id.
**/
    public PSEventDispatcher (PSConnection connection, PxTable proxyTable, long proxyId)
    { 
        connection_ = connection;
        proxyTable_ = proxyTable;
        proxyId_ = proxyId;
        String x = Copyright.copyright; 
    }



    public void fireEvent (String listenerInterfaceName,
                           String listenerMethodName, 
                           EventObject event)
    {        
        PxEventRepSV reply = new PxEventRepSV (proxyTable_, proxyId_, listenerInterfaceName, listenerMethodName, event);
        connection_.send (reply);
    }



}
