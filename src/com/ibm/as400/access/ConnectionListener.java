///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventListener;

/**
 The ConnectionListener interface provides a listener interface for receiving Connection events.
 **/
public interface ConnectionListener extends EventListener
{
    /**
     Invoked when a service has been connected.
     @param  event  The connection event.
     **/
    public void connected(ConnectionEvent event);

    /**
     Invoked when a service has been disconnected.
     @param  event  The connection event.
     **/
    public void disconnected(ConnectionEvent event);
}
