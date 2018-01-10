///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;



/**
The VAction interface defines an action to be performed
on a system resource.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VAction objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see VObject#getActions
@see VObject#getDefaultAction
@see VActionAdapter
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
//
// Implementation note:
//
// This action does not relate directly to the Swing defined
// Action.  The reason is that our actions need to have some state
// passed at perform-time that is not available at construction
// time, hence VActionContext.
//
// VActionAdapter is what ties this action to the Swing action.
//
public interface VAction
{



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    abstract public void addErrorListener (ErrorListener listener);



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    abstract public void addVObjectListener (VObjectListener listener);



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    abstract public void addWorkingListener (WorkingListener listener);



/**
Returns the text for the action.

@return The text.
**/
    abstract public String getText ();



/**
Indicates if the action is enabled.

@return true if the action is enabled; false otherwise.
**/
    abstract public boolean isEnabled ();



/**
Performs the action.

@param  context   The action context.
**/
    abstract public void perform (VActionContext context);



/**
Removes an error listener.

@param  listener    The listener.
**/
    abstract public void removeErrorListener (ErrorListener listener);



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    abstract public void removeVObjectListener (VObjectListener listener);



/**
Removes a working listener.

@param  listener    The listener.
**/
    abstract public void removeWorkingListener (WorkingListener listener);



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled; false otherwise.
**/
    abstract public void setEnabled (boolean enabled);



}

