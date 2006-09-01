///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VMessageList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;



/**
The VMessageList class defines the representation of a
list of messages returned from a system for use
in various models and panes in this package.
You must explicitly call load() to load the information from
the system.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VIFSDirectory objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.AS400Message
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VMessageList
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static String           description_            = ResourceLoader.getText ("MESSAGE_LIST_DESCRIPTION");
    private static Icon             icon16_                 = ResourceLoader.getIcon ("VMessageList16.gif", description_);
    private static Icon             icon32_                 = ResourceLoader.getIcon ("VMessageList32.gif", description_);
    private static String           nameColumnHeader_       = ResourceLoader.getText ("MESSAGE_ID");
    private static String           textColumnHeader_       = ResourceLoader.getText ("MESSAGE_TEXT");
    private static String           typeColumnHeader_       = ResourceLoader.getText ("MESSAGE_TYPE");
    private static String           severityColumnHeader_   = ResourceLoader.getText ("MESSAGE_SEVERITY");



    // Properties.
    private AS400Message[]          messageList_            = new AS400Message[0];
    private VNode                   parent_                 = null;



    // Static data.
    private static TableColumnModel detailsColumnModel_     = null;



    // Private data.
    transient private VObject[]     detailsChildren_;



    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private VObjectEventSupport         objectEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;
    transient private VetoableChangeSupport       vetoableChangeSupport_;
    transient private WorkingEventSupport         workingEventSupport_;



/**
Static initializer.
**/
//
// Implementation note:
//
// * The column widths are completely arbitrary.
//
    static
    {
        detailsColumnModel_ = new DefaultTableColumnModel ();
        int columnIndex = 0;

        // Name column.
        VTableColumn nameColumn = new VTableColumn (columnIndex++, VMessage.NAME_PROPERTY);
        nameColumn.setCellRenderer (new VObjectCellRenderer ());
        nameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        nameColumn.setHeaderValue (nameColumnHeader_);
        nameColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (nameColumn);

        // Text column.
        VTableColumn textColumn = new VTableColumn (columnIndex++, VMessage.TEXT_PROPERTY);
        textColumn.setCellRenderer (new VObjectCellRenderer ());
        textColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        textColumn.setHeaderValue (textColumnHeader_);
        textColumn.setPreferredCharWidth (80);
        detailsColumnModel_.addColumn (textColumn);

        // Severity column.
        VTableColumn severityColumn = new VTableColumn (columnIndex++, VMessage.SEVERITY_PROPERTY);
        severityColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderRenderer (new VObjectHeaderRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderValue (severityColumnHeader_);
        severityColumn.setPreferredCharWidth (8);
        detailsColumnModel_.addColumn (severityColumn);

        // Type column.
        VTableColumn typeColumn = new VTableColumn (columnIndex++, VMessage.TYPE_PROPERTY);
        typeColumn.setCellRenderer (new VObjectCellRenderer ());
        typeColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        typeColumn.setHeaderValue (typeColumnHeader_);
        typeColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (typeColumn);
    }



/**
Constructs a VMessageList object.
**/
    public VMessageList ()
    {
        initializeTransient ();
    }



/**
Constructs a VMessageList object.

@param  messageList     The message list.
**/
    public VMessageList (AS400Message[] messageList)
    {
        if (messageList == null)
            throw new NullPointerException ("messageList");

        messageList_ = messageList;
        initializeTransient ();
    }



/**
Constructs a VMessageList object.

@param  parent          The parent.
@param  messageList     The message list.
**/
    public VMessageList (VNode parent, AS400Message[] messageList)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (messageList == null)
            throw new NullPointerException ("messageList");

        parent_ = parent;
        messageList_ = messageList;
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
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.


@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Returns the children of the node.

@return         The children.
**/
    public Enumeration children ()
    {
        return new VEnumeration (this);
    }



/**
Returns the list of actions that can be performed.

@return Always null.  There are no actions.
**/
    public VAction[] getActions ()
    {
        return null;
    }



/**
Indicates if the node allows children.

@return  Always false.
**/
    public boolean getAllowsChildren ()
    {
        return false;
    }



/**
Returns the child node at the specified index.

@param  index   The index.
@return         Always null.
**/
    public TreeNode getChildAt (int index)
    {
        return null;
    }



/**
Returns the number of children.

@return  Always 0.
**/
    public int getChildCount ()
    {
        return 0;
    }



/**
Returns the default action.

@return Always null. There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }



/**
Returns the child for the details at the specified index.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public synchronized VObject getDetailsChildAt (int index)
    {
        if ((index < 0) || (index >= detailsChildren_.length))
            return null;

        return detailsChildren_[index];
    }



/**
Returns the number of children for the details.

@return  The number of children for the details.
**/
    public synchronized int getDetailsChildCount ()
    {
        return detailsChildren_.length;
    }



/**
Returns the index of the specified child for the details.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public synchronized int getDetailsIndex (VObject detailsChild)
    {
        for (int i = 0; i < detailsChildren_.length; ++i)
            if (detailsChildren_[i] == detailsChild)
                return i;
        return -1;
    }



/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }



/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }



/**
Returns the index of the specified child.

@param  child   The child.
@return         Always -1.
**/
    public synchronized int getIndex (TreeNode child)
    {
        return -1;
    }



/**
Returns the message list.

@return The message list.
**/
    public AS400Message[] getMessageList ()
    {
        return messageList_;
    }



/**
Returns the parent node.

@return The parent node, or null if there is no parent.
**/
    public TreeNode getParent ()
    {
        return parent_;
    }



/**
Returns the properties pane.

@return Always null.  There is no properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return null;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                NAME_PROPERTY or DESCRIPTION_PROPERTY,
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        // By default, return null.
        return null;
    }



/**
Returns the text. It is the description

@return The description text.
**/
    public String getText ()
    {
        return description_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        // Initialize private data.
        detailsChildren_        = new VObject[0];
    }



/**
Indicates if the node is a leaf.

@return  Always true.
**/
    public boolean isLeaf ()
    {
        return true;
    }



/**
Indicates if the details children are sortable.

@return Always true.
**/
    public boolean isSortable ()
    {
        return true;
    }



/**
Loads information about the object from the system.
**/
    public void load ()
    {
        workingEventSupport_.fireStartWorking ();

        synchronized (this) {

            // Stop listening to the previous children.
            for (int i = 0; i < detailsChildren_.length; ++i) {
                detailsChildren_[i].removeErrorListener (errorEventSupport_);
                detailsChildren_[i].removeVObjectListener (objectEventSupport_);
                detailsChildren_[i].removeWorkingListener (workingEventSupport_);
            }

            // Refresh the details children based on the message list.
            detailsChildren_ = new VObject[messageList_.length];
            for (int i = 0; i < messageList_.length; ++i) {
                detailsChildren_[i] = new VMessage (messageList_[i]);

                detailsChildren_[i].addErrorListener (errorEventSupport_);
                detailsChildren_[i].addVObjectListener (objectEventSupport_);
                detailsChildren_[i].addWorkingListener (workingEventSupport_);
            }
        }

        // Done loading.
        workingEventSupport_.fireStopWorking ();
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
Sets the message list.

@param messageList The message list.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setMessageList (AS400Message[] messageList)
        throws PropertyVetoException
    {
        if (messageList == null)
            throw new NullPointerException ("messageList");

        AS400Message[] oldValue = messageList_;
        AS400Message[] newValue = messageList;
        vetoableChangeSupport_.fireVetoableChange ("messageList", oldValue, newValue);

        if (oldValue != newValue)
            messageList_ = messageList;

        propertyChangeSupport_.firePropertyChange ("messageList", oldValue, newValue);
    }



/**
Sorts the children for the details.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            indicates to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier. true for ascending order;
                            false for descending order.
**/
    public synchronized void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
        if (propertyIdentifiers == null)
            throw new NullPointerException ("propertyIdentifiers");
        if (orders == null)
            throw new NullPointerException ("orders");

        VUtilities.sort (detailsChildren_, propertyIdentifiers, orders);
    }



/**
Returns the string representation of the description.

@return The string representation of the description.
**/
    public String toString ()
    {
        return description_;
    }


}
