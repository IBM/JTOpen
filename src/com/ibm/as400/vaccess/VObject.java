///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObject.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.Icon;



/**
The VObject interface defines the representation of an
server resource for use in various models and panes in this
package.

<p>An object has any number of properties.  These are
attributes of the object itself and are identified by
property identifiers.  Every object has at least name and
desctiption properties, and most will define more.

<p>Many of these methods are not called directly by
programs.  Instead, they are called by the server panes
to respond to the user interface as needed.
                 
<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VObject objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

<p>An implementation of this interface should pass on all
events fired by its actions and properties pane to its
listeners.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
// @A1C - javadoc
public interface VObject
{



/**
Property identifier for the name.
**/
    public static final Object      NAME_PROPERTY               = "Name";

/**
Property identifier for the description.
**/
    public static final Object      DESCRIPTION_PROPERTY        = "Description";



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
Returns the list of actions that can be performed.   This is called
when the user brings up a popup menu on this VObject.  The popup
menu will contain a menu item for each returned VAction.

@return The actions, or null if there are no actions.
**/ 
// @A1C - javadoc
    abstract public VAction[] getActions ();



/**
Returns the default action.  This is called when the user double-clicks
on this VObject.  The returned action will then be performed.

@return The default action, or null if there is no default
        action.
**/ 
// @A1C - javadoc
    abstract public VAction getDefaultAction ();



/**
Returns the icon.  This is called to determine which icon to show
with this VObject.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    true for the open icon; false for the closed
                icon.  If there is only one icon, then this
                parameter has no effect.
@return         The icon, or null if there is none.
**/
// @A1C - javadoc
    abstract public Icon getIcon (int size, boolean open);



/**
Returns the properties pane.   This is called when the user 
selects the "Properties" menu item on this VObject's popup
menu.  The return VPropertiesPane object will be presented
as a properties dialog.

@return The properties pane, or null if there is none.
**/
// @A1C - javadoc
    abstract public VPropertiesPane getPropertiesPane ();



/**
Returns a property value.  This is called in order to 
fill the contents of cells in an AS400DetailsPane or
AS400DetailsModel object.

<p>An implementation of this interface should describe the
properties that it supports.  At minimum, it should support
the following property identifiers:

<table border=1>
<tr>
  <th>Property</th>
  <th>Return value</th>
</tr>
<tr>
  <td>NAME_PROPERTY</td>
  <td>Returns the VObject itself ("this").  This will
      be rendered using an icon and a name.</td>
</tr>
<tr>
  <td>DESCRIPTION_PROPERTY</td>
  <td>Returns a text description of the VObject.</td>
</tr>
</table>

@param      propertyIdentifier  The property identifier.
@return                         The property value, or null
                                if the property identifier
                                is not recognized.
**/
// @A1C - javadoc
    abstract public Object getPropertyValue (Object propertyIdentifier);



/**
Returns the descriptive text associated with this object.
This is called when rendering the VObject in a user interface.

@return The descriptive text associated with this object.
**/
// @A1C - javadoc
    abstract public String getText ();



/**
Loads information about the object from the server.
**/
    abstract public void load ();



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


