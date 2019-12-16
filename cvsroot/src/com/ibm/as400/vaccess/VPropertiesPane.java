///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.ChangeListener;
import java.awt.Component;


/**
The VPropertiesPane interface defines the representation of a
properties pane.  A properties pane is a graphical user interface
that allows the user to view and optionally edit properties of
a system resource.

<p>When the user makes a change using the editor, the properties
pane fires a change event.  This signals to the dialog that a
change was made so that it can enable the Apply button.  The change
to the underlying system resource is not made at this time.  This
way the user can still choose to cancel the changes.

<p>When the user clicks the OK or Apply button, then applyChanges()
is called.  The change to the underlying system resource is made at
this time.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VPropertiesPane objects generate the following events:
<ul>
    <li>ChangeEvent
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see VObject#getPropertiesPane
@see VPropertiesAction
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public interface VPropertiesPane
{



/**
Adds a listener to be notified when the user makes a change.

@param  listener    The listener.
**/
    abstract public void addChangeListener (ChangeListener listener);



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
Applies the changes made by the user.
It will change the resources on the system.

@exception Exception   If an error occurs.
**/
    abstract public void applyChanges ()
        throws Exception;



/**
Returns the graphical user interface component.  This
does not include the dialog or the OK, Cancel, and Apply
buttons.

@return                 The graphical user interface component.
**/
    abstract public Component getComponent ();



/**
Removes a change listener.

@param  listener    The listener.
**/
    abstract public void removeChangeListener (ChangeListener listener);



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



}
