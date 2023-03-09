///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ListModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;



/**
The AS400ListModel class implements an underlying model for
a list, where all information for the model is gathered
from the contents of a system resource, known as the root.
You must explicitly call load() to load the information from
the system.

<p>Use this class if you want to customize the graphical
user interface that presents a list.  If you do not need
to customize the interface, then use AS400ListPane instead.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400ListModel objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListDataEvent
    <li>PropertyChangeEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a list model filled with
the contents of a directory in the integrated file system
of a system.  It then presents the list in a JList object.

<pre>
// Set up the list model and JList.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
AS400ListModel listModel = new AS400ListModel (directory);
listModel.load ();
JList list = new JList (listModel);
<br>
// Add the JList to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(list));

</pre>

@see AS400ListPane
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400ListModel
implements ListModel, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    VNode     root_          = null; // Private.



    // Event support.
    transient private ErrorEventSupport       errorEventSupport_;
    transient         ListDataEventSupport    listDataEventSupport_; // Private.
    transient private VObjectListener         objectListener_;
    transient private PropertyChangeSupport   propertyChangeSupport_;
    transient private VetoableChangeSupport   vetoableChangeSupport_;
    transient private WorkingEventSupport     workingEventSupport_;



/**
Constructs an AS400ListModel object.
**/
    public AS400ListModel ()
    {
        initializeTransient ();
    }



/**
Constructs an AS400ListModel object.

@param  root    The root, or the system resource, from which all information for the model is gathered.
**/
    public AS400ListModel (VNode root)
    {
        if (root == null)
            throw new NullPointerException ("root");

        root_ = root;
        initializeTransient ();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the contents of
the list change.

@param  listener    The listener.
**/
    public void addListDataListener (ListDataListener listener)
    {
        listDataEventSupport_.addListDataListener (listener);
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
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Returns the element at the specifed index.

@param  index   The index.
@return The element at the specified index. It will be null if the index
        is not valid or the root has not been set.
**/
    public Object getElementAt (int index)
    {
        // Make sure the root has been set.
        if (root_ == null)
            return null;

        // Validate the index.
        if ((index < 0) || (index >= root_.getDetailsChildCount ()))
            return null;

        // Return the element.
        return root_.getDetailsChildAt (index);
    }



/**
Returns the root, or the system resource, from which all information for the model is gathered.

@return     The root, or the system resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public VNode getRoot ()
    {
        return root_;
    }



/**
Returns the number of objects in the list.

@return The number of objects in the list.

**/
    public int getSize ()
    {
        // Make sure the root has been set.
        if (root_ == null)
            return 0;

        return root_.getDetailsChildCount ();
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        // Initialize event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        listDataEventSupport_   = new ListDataEventSupport (this);
        objectListener_         = new VObjectListener_ ();
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        if (root_ != null) {
            root_.addErrorListener (errorEventSupport_);
            root_.addVObjectListener (objectListener_);
            root_.addWorkingListener (workingEventSupport_);
        }
    }



/**
Loads the information from the system.
**/
    public void load ()
    {
        int previousCount = getSize ();

        if (root_ != null) {
            root_.load ();

            // Load each of the children so that there attributes
            // are reflected.
            int rowCount = root_.getDetailsChildCount ();
            for (int i = 0; i < rowCount; ++i) {
                VObject detailsChild = root_.getDetailsChildAt (i);
                if (detailsChild != null)
                    root_.getDetailsChildAt (i);
            }
        }

        listDataEventSupport_.fireIntervalRemoved (0, previousCount);
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

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a list data listener.

@param  listener    The listener.
**/
    public void removeListDataListener (ListDataListener listener)
    {
        listDataEventSupport_.removeListDataListener (listener);
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
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a working listener.

@param  listener  The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
  Sets the root, or the system resource, from which all information for the model is gathered. It will not take effect until load() is done.


@param  root    The root, or the system resource, from which all information for the model is gathered.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setRoot (VNode root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        VNode oldValue = root_;
        VNode newValue = root;
        vetoableChangeSupport_.fireVetoableChange ("root", oldValue, newValue);

        if (oldValue != newValue) {

            // Get the size before.
            int oldSize;
            if (root_ == null)
                oldSize = 0;
            else
                oldSize = root_.getDetailsChildCount ();

            // Redirect event support.
            if (oldValue != null) {
                oldValue.removeErrorListener (errorEventSupport_);
                oldValue.removeVObjectListener (objectListener_);
                oldValue.removeWorkingListener (workingEventSupport_);
            }
            newValue.addErrorListener (errorEventSupport_);
            newValue.addVObjectListener (objectListener_);
            newValue.addWorkingListener (workingEventSupport_);

            // Set the root.
            root_ = newValue;

            // Clear the contents of the list.
            listDataEventSupport_.fireContentsChanged (0, oldSize);
        }

        propertyChangeSupport_.firePropertyChange ("root", oldValue, newValue);
    }



/**
Sorts the contents. The propertyIdentifer[0], orders[0] combination  is used to do the sort. If the values are equal, propertyIdentifier[1], orders[1] is used to break the tie, and so forth.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            indicates to sort using the string
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

        if (root_ != null) {
            if (root_.isSortable ()) {
                root_.sortDetailsChildren (propertyIdentifiers, orders);
                listDataEventSupport_.fireContentsChanged (0, getSize ());
            }
        }
    }



/**
Listens for explorer events and adjusts the model accordingly.
**/
    private class VObjectListener_
    implements VObjectListener, Serializable
    {



        public void objectChanged (VObjectEvent event)
        {
            VObject object = event.getObject ();

            // If the changed object is the root,
            // then reload the whole model.
            if (object == root_) {                                      // @A1C
                if (! event.isDuringLoad ())                            // @A1A
                    load ();
                listDataEventSupport_.fireContentsChanged (0,           // @A1A
                                        root_.getDetailsChildCount ()); // @A1A
            }                                                           // @A1A

            // If the changed object is contained in the list,
            // then fix up its row.
            else {
                int index = root_.getDetailsIndex (object);
                if (index >= 0)
                    listDataEventSupport_.fireContentsChanged (index, index);
            }
        }



        public void objectCreated (VObjectEvent event)
        {
            VObject object = event.getObject ();
            VNode parent = event.getParent ();

            // If the created object is contained in the list,
            // then insert it into the list.
            if (parent == root_) {
                int index = root_.getDetailsIndex (object);
                if (index >= 0)
                    listDataEventSupport_.fireIntervalAdded (index, index);
            }
        }



        public void objectDeleted (VObjectEvent event)
        {
            VObject object = event.getObject ();

            // If the deleted object is contained in the list,
            // then remove it from the list.
            int index = root_.getDetailsIndex (object);
            if (index >= 0)
                listDataEventSupport_.fireIntervalRemoved (index, index);
        }

    }
}


