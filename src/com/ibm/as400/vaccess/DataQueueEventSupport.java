///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataQueueEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.DataQueueEvent;
import com.ibm.as400.access.DataQueueListener;
import java.util.Vector;



/**
The DataQueueEventSupport class represents a list of DataQueueListeners.
This is also a DataQueueListener and will dispatch all DataQueueEvents
that it receives.
**/
class DataQueueEventSupport
implements DataQueueListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   DataQueueListener[]   listeners_      = new DataQueueListener[0]; // For speed.
    private transient   Vector                listenersV_     = new Vector ();
    private             Object                source_;



/**
Constructs a DataQueueEventSupport object.

@param  source      The source of the events.
**/
    public DataQueueEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addDataQueueListener (DataQueueListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new DataQueueListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires a data queue cleared event.

@param event    The event.
**/
    public void fireCleared (DataQueueEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].cleared (event);
        }
    }



/**
Fires a data queue peeked event.

@param event    The event.
**/
    public void firePeeked (DataQueueEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].peeked (event);
        }
    }



/**
Fires a data queue read event.

@param event    The event.
**/
    public void fireRead (DataQueueEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].read (event);
        }
    }



/**
Fires a data queue written event.

@param event    The event.
**/
    public void fireWritten (DataQueueEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].written (event);
        }
    }



/**
Processes data queue cleared events.

@param event The event.
**/
    public void cleared (DataQueueEvent event)
    {
        fireCleared (event);
    }



/**
Processes data queue peeked events.

@param event The event.
**/
    public void peeked (DataQueueEvent event)
    {
        firePeeked (event);
    }



/**
Processes data queue read events.

@param event The event.
**/
    public void read (DataQueueEvent event)
    {
        fireRead (event);
    }



/**
Processes data queue written events.

@param event The event.
**/
    public void written (DataQueueEvent event)
    {
        fireWritten (event);
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeDataQueueListener (DataQueueListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new DataQueueListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


}

