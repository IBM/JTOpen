///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ErrorEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.Enumeration;
import java.util.Vector;



/**
The ErrorEventSupport class represents a list of
ErrorListeners.  It manages the list as well as
provides notification services for ErrorEvents.
This is also an ErrorListener and will dispatch
all error events.
**/
//
// Implementation note:
//
// @A1A
// THIS APPLIES TO ALL "EVENT SUPPORT" CLASSES IN THIS PACKAGE: 
//
// Class A is defined to fire ErrorEvents.  As a result, it
// has to maintain a list of the listeners.  Since many classes
// in the package have to do this, we provide an "event support"
// class to maintain this list.  This is patterened after
// the java.beans.PropertyChangeSupport class provided in the JDK.
//
// public class A
// {
//      private ErrorEventSupport eventSupport_ = new ErrorEventSupport (this);
//
//      public void addErrorListener (ErrorListener listener)
//      {
//          eventSupport_.addErrorListener (listener);
//      }
//
//      public void removeErrorListener (ErrorListener listener)
//      {
//          eventSupport_.removeErrorListener (listener);
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
// of class B.  B fires ErrorEvents, and A needs to listen
// to B and refire all ErrorEvents.  In addition, it needs
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
//      private ErrorEventSupport eventSupport_ = new ErrorEventSupport (this);
//
//      public A ()
//      {
//          b_ = new B ();
//          b_.addErrorListener (eventSupport_);
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
// Now, when one of the VIFSFile's catches an exception, it 
// will fire an ErrorEvent up the chain, so that the top level
// frame can process it and, for example, put up an error
// dialog.
//
class ErrorEventSupport
implements ErrorListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Source of the events.
private Object  source_ = null;

// Vector of listeners, used to add and remove listeners.
transient private Vector errorListenersV_
    = new Vector();
// Array used to send the events.  Is a copy of errorListenersV_,
// used for speed.
transient private ErrorListener[] errorListeners_
    = new ErrorListener[0];



/**
Constructs a ErrorEventSupport object.

@param  source      The source of the events.
**/
public ErrorEventSupport (Object source)
{
    source_ = source;
}



/**
Adds a listener.

@param  listener    The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    if (listener == null)
    {
        throw new NullPointerException("listener");
    }

    // Add new listener.
    errorListenersV_.addElement(listener);
    // copyInto is synchronized, so we don't have to synchronize V.
    synchronized(errorListeners_)
    {
        errorListeners_ = new ErrorListener[errorListenersV_.size()];
        errorListenersV_.copyInto(errorListeners_);
    }
}


/**
Processes a error event.

@param  event   The event.
**/
public void errorOccurred (ErrorEvent event)
{
    fireError ( event.getException() );
}



/**
Fires an ErrorEvent to listeners.

@param  e    The exception.
**/
void fireError(Exception e)
{
    fireError (new ErrorEvent(source_, e));
}


/**
Fires an ErrorEvent to listeners.

@param  event   The event.
**/
void fireError(ErrorEvent event)
{
    synchronized(errorListeners_)
    {
        for (int i=0; i<errorListeners_.length; ++i)
        {
            errorListeners_[i].errorOccurred(event);
        }
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
public void removeErrorListener (ErrorListener listener)
{
    if (listener == null)
    {
        throw new NullPointerException("listener");
    }

    // Copy all the action listeners into the array used for throwing.
    if (errorListenersV_.removeElement(listener))
    {
        // copyInto is synchronized, so we don't have to synchronize V
        synchronized(errorListeners_)
        {
            errorListeners_ = new ErrorListener[errorListenersV_.size()];
            errorListenersV_.copyInto(errorListeners_);
        }
    }
}


}


