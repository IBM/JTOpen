///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ListSelectionEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import java.util.Vector;



/**
The ListSelectionEventSupport class represents a list of
ListSelectionListeners.  This is also a ListSelectionListener
and will dispatch all list selection events.
**/
class ListSelectionEventSupport
implements ListSelectionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   ListSelectionListener[] listeners_      = new ListSelectionListener[0]; // For speed.
    private transient   Vector                  listenersV_     = new Vector ();
    private             Object                  source_;



/**
Constructs a ListSelectionEventSupport object.

@param  source          The source of the events.
**/
    public ListSelectionEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addListSelectionListener (ListSelectionListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new ListSelectionListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires a value changed event.

@param  firstIndex  The first index.
@param  lastIndex   The last index.
@param  isAdjusting true if this is a rapid series of events, false otherwise.
**/
    public void fireValueChanged (int firstIndex,
                                  int lastIndex,
                                  boolean isAdjusting)
    {
        synchronized (listeners_) {
            ListSelectionEvent event = new ListSelectionEvent (source_,
                    firstIndex, lastIndex, isAdjusting);
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].valueChanged (event);
        }
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeListSelectionListener (ListSelectionListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new ListSelectionListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }



/**
Processes a value changed event.

@param  event       The event.
**/
    public void valueChanged (ListSelectionEvent event)
    {
        fireValueChanged (event.getFirstIndex (),
            event.getLastIndex (), event.getValueIsAdjusting ());
    }



}

