///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.Vector;



/**
The VObjectEventSupport class represents a list of VObjectListeners.
This is also a VObjectListener and will dispatch all VObjectEvents
that it receives.
**/
//
// Implementation note:
//
// THIS APPLIES TO ALL "EVENT SUPPORT" CLASSES IN THIS PACKAGE:
//
// Class A is defined to fire VObjectEvents.  As a result, it
// has to maintain a list of the listeners.  Since many classes
// in the package have to do this, we provide an "event support"
// class to maintain this list.  This is patterened after
// the java.beans.PropertyChangeSupport class provided in the JDK.
//
// public class A
// {
//      private VObjectEventSupport eventSupport_ = new VObjectEventSupport (this);
//
//      public void addVObjectListener (VObjectListener listener)
//      {
//          eventSupport_.addVObjectListener (listener);
//      }
//
//      public void removeVObjectListener (VObjectListener listener)
//      {
//          eventSupport_.removeVObjectListener (listener);
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
// of class B.  B fires VObjectEvents, and A needs to listen
// to B and refire all VObjectEvents.  In addition, it needs
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
//      private VObjectEventSupport eventSupport_ = new VObjectEventSupport (this);
//
//      public A ()
//      {
//          b_ = new B ();
//          b_.addVObjectListener (eventSupport_);
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
// Now, when one of the VIFSFile's is deleted, it will
// fire a VObjectEvent up the chain, so that:
//
// 1. The VIFSDirectory can modify its list of children.
// 2. The AS400TreeModel can remove the file from the model.
// 3. The AS400TreePane can update its view.
// 4. The AS400DetailsModel can remove the file from the model.
// 5. The AS400DetailsPane can update its view.
// 6. The AS400ExplorerPane can refire the event to its listeners.
// 7. The caller (application) can process it.
//
class VObjectEventSupport
implements VObjectListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   VObjectListener[]     listeners_      = new VObjectListener[0]; // For speed.
    private transient   Vector                listenersV_     = new Vector ();
    private             Object                source_;



/**
Constructs a VObjectEventSupport object.

@param  source      The source of the events.
**/
    public VObjectEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new VObjectListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires an object changed event.

@param object The object.
**/
    public void fireObjectChanged (VObject object)
    {
        fireObjectChanged (new VObjectEvent (source_, object));
    }



// @A1A
    public void fireObjectChanged (VObject object, boolean duringLoad)
    {
        fireObjectChanged (new VObjectEvent (source_, object, duringLoad));
    }



/**
Fires an object changed event.

@param event    The event.
**/
    public void fireObjectChanged (VObjectEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].objectChanged (event);
        }
    }



/**
Fires an object created event.

@param object The object.
**/
    public void fireObjectCreated (VObject object)
    {
        fireObjectCreated (new VObjectEvent (source_, object));
    }



/**
Fires an object created event.

@param object The object.
@param parent The parent.
**/
    public void fireObjectCreated (VObject object, VNode parent)
    {
        fireObjectCreated (new VObjectEvent (source_, object, parent));
    }



/**
Fires an object created event.

@param event The event.
**/
    public void fireObjectCreated (VObjectEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].objectCreated (event);
        }
    }



/**
Fires an object deleted event.

@param object The object.
**/
    public void fireObjectDeleted (VObject object)
    {
        fireObjectDeleted (new VObjectEvent (source_, object));
    }



/**
Fires an object deleted event.

@param event The event.
**/
    public void fireObjectDeleted (VObjectEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].objectDeleted (event);
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
Processes object created events.

@param event The event.
**/
    public void objectCreated (VObjectEvent event)
    {
        fireObjectCreated (event);
    }



/**
Processes object changed events.

@param event The event.
**/
    public void objectChanged (VObjectEvent event)
    {
        fireObjectChanged (event);
    }



/**
Processes object deleted events.

@param event The event.
**/
    public void objectDeleted (VObjectEvent event)
    {
        fireObjectDeleted (event);
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new VObjectListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


}

