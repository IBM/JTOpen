///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UndoableEditEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.util.Vector;



/**
The UndoableEditEventSupport class represents a list of
UndoableEditListeners.  This is also a UndoableEditListener
and will dispatch all undoable edit events.
**/
class UndoableEditEventSupport
implements UndoableEditListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   UndoableEditListener[]  listeners_      = new UndoableEditListener[0]; // For speed.
    private transient   Vector                  listenersV_     = new Vector ();
    private             Object                  source_;



/**
Constructs a UndoableEditEventSupport object.

@param  source          The source of the events.
**/
    public UndoableEditEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addUndoableEditListener (UndoableEditListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new UndoableEditListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires an undoable edit happened event.

@param  event   The event.
**/
    public void fireUndoableEditHappened (UndoableEditEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].undoableEditHappened (event);
        }
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeUndoableEditListener (UndoableEditListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new UndoableEditListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


/**
Processes an undoable edit happened event.

@param  event       The event.
**/
    public void undoableEditHappened (UndoableEditEvent event)
    {
        fireUndoableEditHappened (event);
    }



}



