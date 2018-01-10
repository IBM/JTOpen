///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WorkingEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.ActiveStatusEvent;            // @C1A
import com.ibm.as400.resource.ActiveStatusListener;         // @C1A
import java.util.Vector;



/**
The WorkingEventSupport class represents a list of WorkingListeners.
This implements the WorkingListener interface and will dispatch all
working events that it receives.
**/       
//
// Implementation note:
//
// @A1A
// THIS APPLIES TO ALL "EVENT SUPPORT" CLASSES IN THIS PACKAGE: 
//
// Class A is defined to fire WorkingEvents.  As a result, it
// has to maintain a list of the listeners.  Since many classes
// in the package have to do this, we provide an "event support"
// class to maintain this list.  This is patterened after
// the java.beans.PropertyChangeSupport class provided in the JDK.
//
// public class A
// {
//      private WorkingEventSupport eventSupport_ = new WorkingEventSupport (this);
//
//      public void addWorkingListener (WorkingListener listener)
//      {
//          eventSupport_.addWorkingListener (listener);
//      }
//
//      public void removeWorkingListener (WorkingListener listener)
//      {
//          eventSupport_.removeWorkingListener (listener);
//      }
//
//      public void someMethod ()
//      {
//          ... some code here ...
//          eventSupport_.fireObjectChanged (anObject);
//          ... some more code here ...
//      }
// }
//
// There is another case where class A contains a private instance
// of class B.  B fires WorkingEvents, and A needs to listen
// to B and refire all WorkingEvents.  In addition, it needs
// to change the source of the event so that it looks like
// A is the source, even though B is the real source.  The
// reason for this is that listeners have public access to the
// source of the event, and we don't want them to have access to B.
//
// This is all easy to accompilsh by simply adding the
// event support object as a listener to B.  The event
// support object takes care of the rest.
//
// public class A
// {
//      private B b_;
//      private WorkingEventSupport eventSupport_ = new WorkingEventSupport (this);
//
//      public A ()
//      {
//          b_ = new B ();
//          b_.addWorkingListener (eventSupport_);
//      }
// }
//
// This type of event dispatching happens all over in the
// vaccess package.  This is because of the hierarchy of
// components.  Consider an explorer pane that presents
// a view of a directory structure.  Here is the hierarchy:
//
// AS400ExplorerPane
// |
// +-- AS400TreePane
// |   |
// |   +-- AS400TreeModel
// |       |
// |       +-- VIFSDirectory (root directory) <-----------+
// |           |                                          |
// |           +-- VIFSDirectory (subdirectory)           |
// |           |   |                                      |
// |           |   +-- VIFSFile (file in subdirectory)    |
// |           |                                          |
// |           +-- VIFSFile (file in root directory)      |
// |                                                      |
// +-- AS400DetailsPane                                   |
//     |                                                  |
//     +-- AS400DetailsModel                              |
//         |                                              |
//         +----------------------------------------------+
//
// If any object fires certain events, then all objects
// above them in the hierarchy need to hear about it.  Rather
// than making all objects specifically listen to all objects
// below them, we just make them listen to the objects
// directly below them.  Then they "bubble" (dispatch) them
// up the chain to the parent.  All nodes in between can process
// them on the way up.
//
// Now, when one of the VIFSFile's goes to the system, it will
// fire a WorkingEvent up the chain, so that the top level
// frame can process it and, for example, change its cursor
// to reflect the working state.
//
class WorkingEventSupport
implements WorkingListener, ActiveStatusListener            // @C1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   WorkingListener[]   listeners_      = new WorkingListener[0]; // For speed.
    private transient   Vector              listenersV_     = new Vector ();
    private             Object              source_;



/**
Constructs a WorkingEventSupport object.

@param  source      The source of the events.
**/
    public WorkingEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new WorkingListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



    public void busy(ActiveStatusEvent event)               // @C1A
    {                                                       // @C1A
        fireStartWorking();                                 // @C1A
    }                                                       // @C1A

                    

/**
Fires a start working event.
**/
    public void fireStartWorking ()
    {
        // @C2D synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].startWorking (new WorkingEvent (source_));
        // @C2D }
    }



/**
Fires a stop working event.
**/
    public void fireStopWorking ()
    {
        // @C2D synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].stopWorking (new WorkingEvent (source_));
        // @C2D }
    }




    public void idle(ActiveStatusEvent event)               // @C1A
    {                                                       // @C1A
        fireStopWorking();                                  // @C1A
    }                                                       // @C1A

                    

/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new WorkingListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }



/**
Processes a start working event.

@param  event   The event.
**/
    public void startWorking (WorkingEvent event)
    {
        fireStartWorking ();
    }



/**
Processes a stop working event.

@param  event   The event.
**/
    public void stopWorking (WorkingEvent event)
    {
        fireStopWorking ();
    }



}


