///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataQueueListener.java
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
 The DataQueueListener interface provides an interface for receiving DataQueue events.
 **/
public interface DataQueueListener extends EventListener
{
    /**
     Invoked when a clear has been performed.
     @param  event  The DataQueue event.
     **/
    public void cleared(DataQueueEvent event);

    /**
     Invoked when a peek has been performed.
     @param  event  The DataQueue event.
     **/
    public void peeked(DataQueueEvent event);

    /**
     Invoked when a read has been performed.
     @param  event  The DataQueue event.
     **/
    public void read(DataQueueEvent event);

    /**
     Invoked when a write has been performed.
     @param  event  The DataQueue event.
     **/
    public void written(DataQueueEvent event);
}
