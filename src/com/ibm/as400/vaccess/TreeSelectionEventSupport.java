///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: TreeSelectionEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.util.Vector;



/**
The TreeSelectionEventSupport class represents a list of
TreeSelectionListeners.  This is also a TreeSelectionListener
and will dispatch all tree selection events that it receives.
**/
class TreeSelectionEventSupport
implements TreeSelectionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   TreeSelectionListener[] listeners_      = new TreeSelectionListener[0]; // For speed.
    private transient   Vector                  listenersV_     = new Vector ();
    private             Object                  source_;



/**
Constructs a TreeSelectionEventSupport object.

@param  source      The source of the events.
**/
    public TreeSelectionEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addTreeSelectionListener (TreeSelectionListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new TreeSelectionListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires a value changed event.

@param  event   The event.
**/
    public void fireValueChanged (TreeSelectionEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].valueChanged ((TreeSelectionEvent) event.cloneWithSource (source_));
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
Removes a listener.

@param  listener    The listener.
**/
    public void removeTreeSelectionListener (TreeSelectionListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new TreeSelectionListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }



/**
Processes a value changed event.

@param  event       The event.
**/
    public void valueChanged (TreeSelectionEvent event)
    {
        fireValueChanged (event);
    }



}


