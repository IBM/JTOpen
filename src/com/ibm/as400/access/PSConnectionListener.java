///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSConnectionListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PSConnectionListener class dispatches
ConnectionEvents fired within the ProxyServer to the
client.
**/
class PSConnectionListener
extends PSEventDispatcher
implements ConnectionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final String listenerInterfaceName_ = "com.ibm.as400.access.ConnectionListener";


/**
Constructs a PSConnectionListener object.

@param connection   The proxy server connection.
@param proxyTable   The proxy table.
@param proxyId      The proxy id.
**/
    public PSConnectionListener (PSConnection connection, PxTable proxyTable, long proxyId)
    { 
        super (connection, proxyTable, proxyId);
        String x = Copyright.copyright; 
    }



/**
Invoked when the connection is closed.

@param event    The event.
**/
    public void connected (ConnectionEvent event)
    {
        fireEvent (listenerInterfaceName_, "connected", event);
    }



/**
Invoked when the connection is opened.

@param event    The event.
**/
    public void disconnected (ConnectionEvent event)
    {
        fireEvent (listenerInterfaceName_, "disconnected", event);
    }



}
