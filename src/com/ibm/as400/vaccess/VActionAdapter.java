///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VActionAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.Action;
import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;



/**
The VActionAdapter class represents an object that listens for
action events and subsequently performs a VAction.

<p>A VActionAdapter object is useful when an action listener is
needed to perform a VAction.  For example, this can be used to
as an action listener for button clicks or menu item selections.

<p>This class essentially adapts the VAction
class hierarchy to the javax.swing.Action class hierarchy.

<p>The following example creates an action adapter which is
used to put up a properties pane for a directory in the integrated
file system of an AS/400.  The action adapter is then attached
to a menu item, so that the action is performed when the menu
item is selected.

<pre>
// Set up the directory object.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
<br>
// Set up a pane and add it to a frame.
AS400ExplorerPane pane = new AS400ExplorerPane (directory);

JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(pane));


<br>
// Set up the properties action adapter.
VActionAdapter adapter = new VActionAdapter (directory,
    new VPropertiesAction (), pane.getActionContext ());
<br>
// Set up the menu item.
JMenuItem menuItem = new JMenuItem ("Properties");
menuItem.setEnabled (true);
menuItem.addActionListener (adapter);
</pre>

@see VAction
**/
public class VActionAdapter
implements Action, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    private VAction             action_         = null;
    private VActionContext      actionContext_  = null;
    private boolean             enabled_        = false;



    // Private data.
    transient private Hashtable propertyDictionary_;



    // Event support.
    transient private PropertyChangeSupport propertyChangeSupport_;



/**
Constructs a VActionAdapter object.
**/
    public VActionAdapter ()
    {
        initializeTransient ();
    }



/**
Constructs a VActionAdapter object.

@param  action          The action.
@param  actionContext   The action context.
**/
    public VActionAdapter (VAction action,
                           VActionContext actionContext)
    {
        if (action == null)
            throw new NullPointerException ("action");
        if (actionContext == null)
            throw new NullPointerException ("actionContext");

        action_         = action;
        actionContext_  = actionContext;

        initializeTransient ();
    }



/**
Invoked when an action is performed.  This will cause the
specified event to also be performed.

@param  event   The event.
**/
    public void actionPerformed (ActionEvent event)
    {
        if ((action_ != null)
            && (actionContext_ != null)
            && (isEnabled () == true)) {

            action_.perform (actionContext_);

        }
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the action with which the action adapter
is associated.

@return The action, or null if none has been set.
**/
    public VAction getAction ()
    {
        return action_;
    }



/**
Returns the context in which the associated action will be
performed.

@return The action context, or null if none has been set.
**/
    public VActionContext getActionContext ()
    {
        return actionContext_;
    }



/**
Returns the value of the property associated with the given key.

@param key  The key.
@return     The value.
**/
    public Object getValue (String key)
    {
        if (key == null)
            throw new NullPointerException ("key");

        if (propertyDictionary_.containsKey (key))
            return propertyDictionary_.get (key);
        else
            return null;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Private data.
        propertyDictionary_ = new Hashtable ();

        // Event support.
        propertyChangeSupport_ = new PropertyChangeSupport (this);
    }



/**
Indicates if the action is enabled.

@return true if the action is enabled; false otherwise.
**/
    public boolean isEnabled ()
    {
        if (action_ != null)
            return action_.isEnabled ();
        else
            return enabled_;
    }



/**
Sets the value of the property associated with the given key.

@param key      The key.
@param value    The value of the property.
**/
    public void putValue (String key, Object value)
    {
        if (key == null)
            throw new NullPointerException ("key");
        if (value == null)
            throw new NullPointerException ("value");

        propertyDictionary_.put (key, value);
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Sets the action with which the action adapter
is associated.

@param action The action.
**/
    public void setAction (VAction action)
    {
        if (action == null)
            throw new NullPointerException ("action");

        VAction oldValue = action_;
        VAction newValue = action;

        action_  = action;

        propertyChangeSupport_.firePropertyChange ("action", oldValue, newValue);
    }



/**
Sets the context in which the associated action will be
performed.

@param actionContext The action context.
**/
    public void setActionContext (VActionContext actionContext)
    {
        if (actionContext == null)
            throw new NullPointerException ("actionContext");

        VActionContext oldValue = actionContext_;
        VActionContext newValue = actionContext;

        actionContext_ = actionContext;

        propertyChangeSupport_.firePropertyChange ("actionContext", oldValue, newValue);
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled; false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        Boolean oldValue = new Boolean (enabled_);
        Boolean newValue = new Boolean (enabled);

        enabled_ = enabled;
        if (action_ != null)
            action_.setEnabled (enabled);

        propertyChangeSupport_.firePropertyChange ("enabled", oldValue, newValue);
    }



}
