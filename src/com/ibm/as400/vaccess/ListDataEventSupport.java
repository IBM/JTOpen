///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ListDataEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Vector;



/**
The ListDataEvent class represents a list of ListDataListeners.
This is also a ListDataListener and will dispatch all list data
events.
**/
class ListDataEventSupport
implements ListDataListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   ListDataListener[]  listeners_      = new ListDataListener[0]; // For speed.
    private transient   Vector              listenersV_     = new Vector ();
    private             Object              source_;



/**
Constructs a ListDataEventSupport object.

@param  source      The source of the events.
**/
    public ListDataEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addListDataListener (ListDataListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new ListDataListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Processes an contents changed event.

@param  event   The event.
**/
    public void contentsChanged (ListDataEvent event)
    {
        fireContentsChanged (event.getIndex0 (), event.getIndex1 ());
    }



/**
Fires a contents changed event.

@param  index0          The index 0.
@param  index1          The index 1.
**/
    public void fireContentsChanged (int index0, int index1)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].contentsChanged (new ListDataEvent (source_,
                    ListDataEvent.CONTENTS_CHANGED, index0, index1));
        }
    }



/**
Fires an interval added event.

@param  type            The type.
@param  index0          The index 0.
@param  index1          The index 1.
**/
    public void fireIntervalAdded (int index0, int index1)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].intervalAdded (new ListDataEvent (source_,
                    ListDataEvent.INTERVAL_ADDED, index0, index1));
        }
    }



/**
Fires an interval removed event.

@param  type            The type.
@param  index0          The index 0.
@param  index1          The index 1.
**/
    public void fireIntervalRemoved (int index0, int index1)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].intervalRemoved (new ListDataEvent (source_,
                    ListDataEvent.INTERVAL_REMOVED, index0, index1));
        }
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Processes an interval added event.

@param  event   The event.
**/
    public void intervalAdded (ListDataEvent event)
    {
        fireIntervalAdded (event.getIndex0 (), event.getIndex1 ());
    }



/**
Processes an interval removed event.

@param  event   The event.
**/
    public void intervalRemoved (ListDataEvent event)
    {
        fireIntervalRemoved (event.getIndex0 (), event.getIndex1 ());
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeListDataListener (ListDataListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new ListDataListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


}


