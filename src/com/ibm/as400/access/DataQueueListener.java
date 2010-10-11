///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DataQueueListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventListener;

/**
 Provides an interface for receiving DataQueue events.
 <p>Note: Only actions performed <i>via the Java object that implements this interface</i> will fire events to listeners.
 Data queue accesses performed by other means (such as by calling system APIs or CL commands) do not fire events to listeners.
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
