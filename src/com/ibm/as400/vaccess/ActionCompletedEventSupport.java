///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ActionCompletedEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.ActionCompletedListener;
import java.util.Enumeration;
import java.util.Vector;



/**
The ActionCompletedEventSupport class represents a list of
ActionCompletedListeners.  It manages the list as well as
provides notification services for ActionCompletedEvents.
**/
class ActionCompletedEventSupport
implements ActionCompletedListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Source of the events.
private Object  source_ = null;

// Vector of listeners, used to add and remove listeners.
transient private Vector actionListenersV_
    = new Vector();
// Array used to send the events.  Is a copy of actionListenersV_,
// used for speed.
transient private ActionCompletedListener[] actionListeners_
    = new ActionCompletedListener[0];



/**
Constructs a ActionCompletedEventSupport object.

@param  source      The source of the events.
**/
public ActionCompletedEventSupport (Object source)
{
    source_ = source;
}



/**
Processes an action completed event.

@param event The event.
**/
public void actionCompleted (ActionCompletedEvent event)
{
    fireActionCompleted ();
}



/**
Adds a listener.

@param  listener    The listener.
**/
public void addActionCompletedListener (ActionCompletedListener listener)
{
    if (listener == null)
    {
        throw new NullPointerException("listener");
    }

    // Add new listener.
    actionListenersV_.addElement(listener);
    // copyInto is synchronized, so we don't have to synchronize V.
    synchronized(actionListeners_)
    {
        actionListeners_ = new ActionCompletedListener[actionListenersV_.size()];
        actionListenersV_.copyInto(actionListeners_);
    }
}


/**
Fires an ActionCompletedEvent to listeners.
**/
void fireActionCompleted()
{
    ActionCompletedEvent event = new ActionCompletedEvent(source_);
    synchronized(actionListeners_)
    {
        for (int i=0; i<actionListeners_.length; ++i)
        {
            actionListeners_[i].actionCompleted(event);
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
public void removeActionCompletedListener (ActionCompletedListener listener)
{
    if (listener == null)
    {
        throw new NullPointerException("listener");
    }

    // Copy all the action listeners into the array used for throwing.
    if (actionListenersV_.removeElement(listener))
    {
        // copyInto is synchronized, so we don't have to synchronize V
        synchronized(actionListeners_)
        {
            actionListeners_ = new ActionCompletedListener[actionListenersV_.size()];
            actionListenersV_.copyInto(actionListeners_);
        }
    }
}


}


