///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ChangeEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;



/**
The ChangeEventSupport class represents a list of ChangeListeners.
**/
class ChangeEventSupport implements ChangeListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   ChangeListener[]    listeners_      = new ChangeListener[0]; // For speed.
    private transient   Vector              listenersV_     = new Vector ();
    private             Object              source_;



/**
Constructs a ChangeEventSupport object.

@param  source      The source of the events.
**/
    public ChangeEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addChangeListener (ChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new ChangeListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires a state changed event.
**/
    public void fireStateChanged ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].stateChanged (new ChangeEvent (source_));
        }
    }



/**
Fires a state changed event.
**/
    public void fireStateChanged (ChangeEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].stateChanged (event);
        }
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeChangeListener (ChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new ChangeListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }

    public void stateChanged(ChangeEvent event)
    {
        fireStateChanged (event);
    }
}


