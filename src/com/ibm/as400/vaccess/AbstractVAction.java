///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AbstractVAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



/**
The AbstractVAction class is an abstract implementation
for an action.
**/
abstract class AbstractVAction
implements VAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private   boolean         enabled_    = true;
    private   VObject         object_     = null;



    // Event support.
    private   ErrorEventSupport           errorEventSupport_    = new ErrorEventSupport (this);
    private   VObjectEventSupport         objectEventSupport_   = new VObjectEventSupport (this);
    private   WorkingEventSupport         workingEventSupport_  = new WorkingEventSupport (this);



/**
Constructs an AbstractVAction object.
**/
    public AbstractVAction ()
    {
        object_ = null;
    }



/**
Constructs an AbstractVAction object.

@param  object  The object.
**/
    public AbstractVAction (VObject object)
    {
        object_ = object;
    }



/**
Adds an error listener.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a VObjectListener.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a working listener.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Fires an error event.

@param e    The exeception.
**/
    protected void fireError (Exception e)
    {
        errorEventSupport_.fireError (e);
    }



/**
Fires an object changed event.
**/
    protected void fireObjectChanged ()
    {
        objectEventSupport_.fireObjectChanged (object_);
    }



/**
Fires an object changed event.

@param object   The object.
**/
    protected void fireObjectChanged (VObject object)
    {
        objectEventSupport_.fireObjectChanged (object);
    }



/**
Fires an object created event.
**/
    protected void fireObjectCreated ()
    {
        objectEventSupport_.fireObjectCreated (object_);
    }



/**
Fires an object created event.

@param object   The object.
**/
    protected void fireObjectCreated (VObject object, VObject child)
    {
        objectEventSupport_.fireObjectCreated (object);
    }



/**
Fires a object deleted event.
**/
    protected void fireObjectDeleted ()
    {
        objectEventSupport_.fireObjectDeleted (object_);
    }



/**
Fires a object deleted event.

@param object   The object.
**/
    protected void fireObjectDeleted (VObject object)
    {
        objectEventSupport_.fireObjectDeleted (object);
    }



/**
Fires a start working event.
**/
    protected void fireStartWorking ()
    {
        workingEventSupport_.fireStartWorking ();
    }



/**
Fires a stop working event.
**/
    protected void fireStopWorking ()
    {
        workingEventSupport_.fireStopWorking ();
    }



/**
Returns the affected AS400 object.

@return The server object.
**/
    public VObject getObject ()
    {
        return object_;
    }



/**
Returns the text for the action.

@return The text.
**/
    public abstract String getText ();



/**
Indicates if the action is enabled.

@return true if the action is enabled, false otherwise.
**/
    public boolean isEnabled ()
    {
        return enabled_;
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled, false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        enabled_ = enabled;
    }



/**
Returns the text for the action.

@return The text.
**/
    public String toString ()
    {
        return getText ();
    }



}
