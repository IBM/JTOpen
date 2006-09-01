///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ListPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The AS400ListPane class represents a graphical user interface
that presents a list of the contents of a system resource,
known as the root.  You must explicitly call load() to load the
information from the system.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400ListPane objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListSelectionEvent
    <li>PropertyChangeEvent
</ul>

<p>The following example creates a list pane filled with
the list of printers on a system.

<pre>
// Set up the list pane.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VPrinters printers = new VPrinters (system);
AS400ListPane listPane = new AS400ListPane (printers);
listPane.load ();
<br>
// Add the list pane to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add (listPane);
</pre>

@see AS400ListModel
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400ListPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    boolean             allowActions_           = false; // Private.
    boolean             confirm_                = true; // Private.
    JList               list_                   = null; // Private.
    AS400ListModel      model_                  = null; // Private.



    // Private data.
    transient private VActionContext                  actionContext_;
    transient private DoubleClickAdapter              doubleClickAdapter_;
    transient private PopupMenuAdapter                popupMenuAdapter_;



    // Event support.
    transient private ErrorEventSupport               errorEventSupport_;
    transient private ListSelectionEventSupport       listSelectionEventSupport_;
    transient private PropertyChangeSupport           propertyChangeSupport_;
    transient private VetoableChangeSupport           vetoableChangeSupport_;



/**
Constructs an AS400ListPane object.
**/
    public AS400ListPane ()
    {
        // Initialize the model.
        model_ = new AS400ListModel ();

        // Initialize the list.
        list_ = new JList (model_);
        list_.setCellRenderer (new VObjectCellRenderer ());

        // Layout the pane.
        setLayout (new BorderLayout ());
        add ("Center", new JScrollPane (list_));

        initializeTransient ();
    }



/**
Constructs an AS400ListPane object.

@param  root    The root, or the system resource, from which all information for the model is gathered.

**/
    public AS400ListPane (VNode root)
    {
        this ();

        if (root == null)
            throw new NullPointerException ("root");

        try {
            model_.setRoot (root);
        }
        catch (PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener  The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when a list selection occurs.

@param  listener  The listener.
**/
    public void addListSelectionListener (ListSelectionListener listener)
    {
        listSelectionEventSupport_.addListSelectionListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener (listener);
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        super.addVetoableChangeListener (listener);
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Returns the context in which actions will be performed.

@return The action context.
**/
    public VActionContext getActionContext ()
    {
        return actionContext_;
    }



/**
Indicates if actions can be invoked on objects.

@return true if actions can be invoked; false otherwise.
**/
    public boolean getAllowActions ()
    {
        return allowActions_;
    }



/**
Indicates if certain actions are confirmed with the user.

@return true if certain actions are confirmed with the user;
        false otherwise.
**/
    public boolean getConfirm ()
    {
        return confirm_;
    }



/**
Returns the list model.

@return The list model.
**/
    public ListModel getModel ()
    {
        return model_;
    }



/**
Returns the root, or the system resource, from which all information for the model is gathered.

@return     The root, or the system resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public VNode getRoot ()
    {
        return model_.getRoot ();
    }



/**
Returns the first selected object.

@return The first selected object, or null if none are
        selected.
**/
    public VObject getSelectedObject ()
    {
        VObject selectedObject = null;
        int selectedIndex = list_.getSelectedIndex ();
        if (selectedIndex >= 0)
            selectedObject = (VObject) model_.getElementAt (selectedIndex);
        return selectedObject;
    }



/**
Returns the selected objects.

@return  The selected objects.
**/
    public VObject[] getSelectedObjects ()
    {
        int[] selectedIndices = list_.getSelectedIndices ();
        int selectedCount = selectedIndices.length;
        VObject[] selectedObjects = new VObject[selectedCount];
        for (int i = 0; i < selectedCount; ++i)
            selectedObjects[i] = (VObject) model_.getElementAt (selectedIndices[i]);
        return selectedObjects;
    }



/**
Returns the selection model that is used to maintain
selection state.  This provides the ability to programmatically
select and deselect objects.

@return The selection model.
**/
    public ListSelectionModel getSelectionModel ()
    {
        return list_.getSelectionModel ();
    }



/**
Returns the preferred number of visible rows.

@return The preferred number of visible rows.
**/
    public int getVisibleRowCount ()
    {
        return list_.getVisibleRowCount ();
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport (this);
        listSelectionEventSupport_  = new ListSelectionEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);
        vetoableChangeSupport_      = new VetoableChangeSupport (this);


        model_.addErrorListener (errorEventSupport_);
        model_.addPropertyChangeListener (propertyChangeSupport_);
        model_.addVetoableChangeListener (vetoableChangeSupport_);
        list_.getSelectionModel ().addListSelectionListener (listSelectionEventSupport_);

        // Initialize the action context.
        actionContext_ = new VActionContext_ ();

        // Initialize the other adapters.
        model_.addWorkingListener (new WorkingCursorAdapter (list_));

        VPane_ pane = new VPane_ ();
        doubleClickAdapter_   = new DoubleClickAdapter (pane, actionContext_);
        popupMenuAdapter_     = new PopupMenuAdapter (pane, actionContext_);
    }



/**
Indicates if the object is selected.

@param  object The object.
@return true if the object is selected; false otherwise.
**/
    public boolean isSelected (VObject object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        Object[] selectedObjects = list_.getSelectedValues ();
        for (int i = 0; i < selectedObjects.length; ++i)
            if ((VObject) selectedObjects[i] == object)
                return true;
        return false;
    }



/**
Loads the information from the system.
**/
    public void load ()
    {
        list_.clearSelection ();
        model_.load ();
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
Removes an error listener.

@param  listener  The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a list selection listener.

@param  listener  The listener.
**/
    public void removeListSelectionListener (ListSelectionListener listener)
    {
        listSelectionEventSupport_.removeListSelectionListener (listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        super.removePropertyChangeListener (listener);
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener (VetoableChangeListener listener)
    {
        super.removeVetoableChangeListener (listener);
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Sets whether actions are allowed.  The following are enabled only when
actions are allowed:

<ul>
  <li>popup menu on selected object
  <li>double clicking on a object performs the default action.
</ul>

<p>The default is false.

@param allowActions true if actions are allowed; false otherwise.
**/
    public void setAllowActions (boolean allowActions)
    {
        if (allowActions_ != allowActions) {

            allowActions_ = allowActions;

            if (allowActions_) {
                list_.addMouseListener (popupMenuAdapter_);
                list_.addMouseListener (doubleClickAdapter_);
            }
            else {
                list_.removeMouseListener (popupMenuAdapter_);
                list_.removeMouseListener (doubleClickAdapter_);
            }
        }
    }



/**
Sets whether certain actions are confirmed with the user.  The default
is true.

@param confirm    true if certain actions are confirmed with the
                           user; false otherwise.
**/
    public void setConfirm (boolean confirm)
    {
        confirm_ = confirm;
    }



/**
Sets the root, or the system resource, from which all information for the model is gathered. It will not take effect until load() is done.

@param  root   The root, or the system resource, from which all information for the model is gathered.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setRoot (VNode root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        list_.clearSelection ();
        model_.setRoot (root);
    }



/**
Sets the selection model that is used to maintain selection
state.  This provides the ability to programmatically select
and deselect objects.

@param  selectionModel  The selection model.
**/
    public void setSelectionModel (ListSelectionModel selectionModel)
    {
        if (selectionModel == null)
            throw new NullPointerException ("selectionModel");

        // Do not dispatch events from the old selection model any more.
        ListSelectionModel oldSelectionModel = list_.getSelectionModel ();
        oldSelectionModel.removeListSelectionListener (listSelectionEventSupport_);

        list_.setSelectionModel (selectionModel);

        // Dispatch events from the new selection model.
        selectionModel.addListSelectionListener (listSelectionEventSupport_);
    }



/**
Sets the preferred number of visible rows.

@param  visibleRowCount The preferred number of visible rows.
**/
    public void setVisibleRowCount (int visibleRowCount)
    {
        list_.setVisibleRowCount (visibleRowCount);
    }



/**
Sorts the contents. The propertyIdentifer[0], orders[0] combination  is used to do the sort. If the values are equal, propertyIdentifier[1], orders[1] is used to break the tie, and so forth.


@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier; true for ascending order,
                            false for descending order.
**/
    public void sort (Object[] propertyIdentifiers, boolean[] orders)
    {
        if (propertyIdentifiers == null)
            throw new NullPointerException ("propertyIdentifiers");
        if (orders == null)
            throw new NullPointerException ("orders");

        model_.sort (propertyIdentifiers, orders);
    }



/**
Implements the VActionContext interface.
**/
    private class VActionContext_
    implements VActionContext, Serializable
    {
        public boolean getConfirm ()
        {
            return confirm_;
        }

        public Frame getFrame ()
        {
            return VUtilities.getFrame (AS400ListPane.this);
        }

        public CellEditor startEditing (VObject object, Object propertyIdentifier)
        {
            if (object == null)
                throw new NullPointerException ("object");
            if (propertyIdentifier == null)
                throw new NullPointerException ("propertyIdentifier");

            // Edits are not allowed because Swing does not
            // provide a ListCellEditor.
            return null;
        }
    };



/**
Implements the VPane interface.
**/
    private class VPane_
    implements VPane, Serializable
    {

        public VObject getObjectAt (Point point)
        {
            VObject object = null;
            int row = list_.locationToIndex (point);
            if (row != -1)
                object = (VObject) model_.getElementAt (row);
            return object;
        }

        public VNode getRoot ()
        {
            return AS400ListPane.this.getRoot ();
        }

        public void setRoot (VNode root)
           throws PropertyVetoException
        {
            AS400ListPane.this.setRoot (root);
        }

    };



}
