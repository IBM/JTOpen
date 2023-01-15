///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VMessageQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.MessageQueue;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.QueuedMessage;
import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;



/**
The VMessageQueue class defines the representation of a
message queue on a system for use in various models and
panes in this package.
You must explicitly call load() to load the information from
the system.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VMessageQueue objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.MessageQueue
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VMessageQueue
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI
    private static String           description_                = ResourceLoader.getText ("MESSAGE_QUEUE_DESCRIPTION");
    private static Icon             icon16_                     = ResourceLoader.getIcon ("VMessageList16.gif", description_);
    private static Icon             icon32_                     = ResourceLoader.getIcon ("VMessageList32.gif", description_);
    private static String           dateColumnHeader_           = ResourceLoader.getText ("MESSAGE_DATE");
    private static String           fromJobColumnHeader_        = ResourceLoader.getText ("MESSAGE_FROM_JOB");
    private static String           fromJobNumberColumnHeader_  = ResourceLoader.getText ("MESSAGE_FROM_JOB_NUMBER");
    private static String           fromProgramColumnHeader_    = ResourceLoader.getText ("MESSAGE_FROM_PROGRAM");
    private static String           fromUserColumnHeader_       = ResourceLoader.getText ("MESSAGE_FROM_USER");
    private static String           messageQueueColumnHeader_   = ResourceLoader.getText ("MESSAGE_QUEUE");
    private static String           nameColumnHeader_           = ResourceLoader.getText ("MESSAGE_ID");
    private static String           textColumnHeader_           = ResourceLoader.getText ("MESSAGE_TEXT");
    private static String           typeColumnHeader_           = ResourceLoader.getText ("MESSAGE_TYPE");
    private static String           severityColumnHeader_       = ResourceLoader.getText ("MESSAGE_SEVERITY");



    // Properties.
    private VNode                   parent_                     = null;
    private MessageQueue            queue_                      = null;



    // Static data.
    private static TableColumnModel detailsColumnModel_         = null;



    // Private data.
    transient private VAction[]         actions_;
    transient         VObject[]         detailsChildren_; // Private.
    transient private Enumeration       enum_;
    transient private int               loaded_;
    transient private VPropertiesPane   propertiesPane_;



    // Event support.
    transient         ErrorEventSupport           errorEventSupport_; // Private.
    transient         VObjectEventSupport         objectEventSupport_; // Private.
    transient         VObjectListener_            objectListener_; // Private.
    transient         PropertyChangeSupport       propertyChangeSupport_; // Private.
    transient         VetoableChangeSupport       vetoableChangeSupport_; // Private.
    transient         WorkingEventSupport         workingEventSupport_; // Private.



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
        VTableColumn nameColumn = new VTableColumn (columnIndex++, VQueuedMessage.NAME_PROPERTY);
        nameColumn.setCellRenderer (new VObjectCellRenderer ());
        nameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        nameColumn.setHeaderValue (nameColumnHeader_);
        nameColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (nameColumn);

        // Text column.
        VTableColumn textColumn = new VTableColumn (columnIndex++, VQueuedMessage.TEXT_PROPERTY);
        textColumn.setCellRenderer (new VObjectCellRenderer ());
        textColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        textColumn.setHeaderValue (textColumnHeader_);
        textColumn.setPreferredCharWidth (40);
        detailsColumnModel_.addColumn (textColumn);

        // Severity column.
        VTableColumn severityColumn = new VTableColumn (columnIndex++, VQueuedMessage.SEVERITY_PROPERTY);
        severityColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderRenderer (new VObjectHeaderRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderValue (severityColumnHeader_);
        severityColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (severityColumn);

        // Type column.
        VTableColumn typeColumn = new VTableColumn (columnIndex++, VQueuedMessage.TYPE_PROPERTY);
        typeColumn.setCellRenderer (new VObjectCellRenderer ());
        typeColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        typeColumn.setHeaderValue (typeColumnHeader_);
        typeColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (typeColumn);

        // Date column.
        VTableColumn dateColumn = new VTableColumn (columnIndex++, VQueuedMessage.DATE_PROPERTY);
        dateColumn.setCellRenderer (new VObjectCellRenderer ());
        dateColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        dateColumn.setHeaderValue (dateColumnHeader_);
        dateColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (dateColumn);

        // From job column.
        VTableColumn fromJobColumn = new VTableColumn (columnIndex++, VQueuedMessage.FROM_JOB_PROPERTY);
        fromJobColumn.setCellRenderer (new VObjectCellRenderer ());
        fromJobColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        fromJobColumn.setHeaderValue (fromJobColumnHeader_);
        fromJobColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (fromJobColumn);

        // From user column.
        VTableColumn fromUserColumn = new VTableColumn (columnIndex++, VQueuedMessage.FROM_USER_PROPERTY);
        fromUserColumn.setCellRenderer (new VObjectCellRenderer ());
        fromUserColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        fromUserColumn.setHeaderValue (fromUserColumnHeader_);
        fromUserColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (fromUserColumn);

        // From job number column.
        VTableColumn fromJobNumberColumn = new VTableColumn (columnIndex++, VQueuedMessage.FROM_JOB_NUMBER_PROPERTY);
        fromJobNumberColumn.setCellRenderer (new VObjectCellRenderer ());
        fromJobNumberColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        fromJobNumberColumn.setHeaderValue (fromJobNumberColumnHeader_);  //@C1C
        fromJobNumberColumn.setPreferredCharWidth (20);                   //@C1C
        detailsColumnModel_.addColumn (fromJobNumberColumn);

        // From program column.
        VTableColumn fromProgramColumn = new VTableColumn (columnIndex++, VQueuedMessage.FROM_PROGRAM_PROPERTY);
        fromProgramColumn.setCellRenderer (new VObjectCellRenderer ());
        fromProgramColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        fromProgramColumn.setHeaderValue (fromProgramColumnHeader_);
        fromProgramColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (fromProgramColumn);

        // Message queue column.
        VTableColumn messageQueueColumn = new VTableColumn (columnIndex++, VQueuedMessage.MESSAGE_QUEUE_PROPERTY);
        messageQueueColumn.setCellRenderer (new VObjectCellRenderer ());
        messageQueueColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        messageQueueColumn.setHeaderValue (messageQueueColumnHeader_);
        messageQueueColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (messageQueueColumn);
    }



/**
Constructs a VMessageQueue object.
**/
    public VMessageQueue ()
    {
        queue_ = new MessageQueue ();
        initializeTransient ();
    }



/**
Constructs a VMessageQueue object.

@param system   The system on which the message queue resides.
**/
    public VMessageQueue (AS400 system)
    {
        if (system == null)
            throw new NullPointerException ("system");

        queue_ = new MessageQueue (system);
        initializeTransient ();
    }



/**
Constructs a VMessageQueue object.

@param  system      The system on which the message queue resides.
@param  path        The fully qualified integrated file system path name of the message queue. The path  must be in the format of /QSYS.LIB/libname.LIB/messageQueue.MSGQ.
**/
    public VMessageQueue (AS400 system, String path)
    {
        if (system == null)
            throw new NullPointerException ("system");
        if (path == null)
            throw new NullPointerException ("path");

        queue_ = new MessageQueue (system, path);
        initializeTransient ();
    }



/**
Constructs a VMessageQueue object.

@param  parent      The parent.
@param  system      The system on which the message queue resides.
@param  path        The fully qualified integrated file system path name of the message queue. The path  must be in the format of /QSYS.LIB/libname.LIB/messageQueue.MSGQ.
**/
    public VMessageQueue (VNode parent, AS400 system, String path)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");
        if (path == null)
            throw new NullPointerException ("path");

        parent_ = parent;
        queue_ = new MessageQueue (system, path);
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
<ul>
    <li>clear
</ul>

@return The actions.
**/
    public VAction[] getActions ()
    {
        return actions_;
    }



/**
Indiciates if the node allows children.

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

@return Always null.  There is no default action.
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
    public VObject getDetailsChildAt (int index)
    {
        if ((index < 0) || (index >= detailsChildren_.length))
            return null;

        loadMore (index);
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
        for (int i = 0; i < loaded_; ++i)
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
    public int getIndex (TreeNode child)
    {
        return -1;
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
Returns the fully qualified integrated file system path name of the message queue.

@return  The fully qualified integrated file system path name of the message queue.

@see com.ibm.as400.access.MessageQueue#getPath
**/
    public String getPath ()
    {
        return queue_.getPath ();
    }



/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                NAME_PROPERTY or DESCRIPTION_PROPERTY.
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
Returns the selection.

@return The selection.

@see com.ibm.as400.access.MessageQueue#getSelection
**/
    public String getSelection ()
    {
        return queue_.getSelection ();
    }



/**
Returns the severity.

@return The severity.

@see com.ibm.as400.access.MessageQueue#getSeverity
**/
    public int getSeverity ()
    {
        return queue_.getSeverity ();
    }



/**
Returns the system on which the message queue exists.

@return The system on which the message queue exists.
**/
    public AS400 getSystem ()
    {
        return queue_.getSystem ();
    }



/**
Returns the name of the message queue.

@return The name of the message queue.
**/
    public String getText ()
    {
        String pathName = queue_.getPath ();
        if (pathName.equals (MessageQueue.CURRENT)) {
            AS400 system = queue_.getSystem ();
            if (system != null)
                return system.getUserId ();
            else
                return "";
        }
        else {
            QSYSObjectPathName path = new QSYSObjectPathName (pathName);
            return path.getObjectName ();
        }
    }




/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        objectListener_         = new VObjectListener_ ();
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        queue_.addPropertyChangeListener (propertyChangeSupport_);
        queue_.addVetoableChangeListener (vetoableChangeSupport_);

        // Initialize the private data.
        detailsChildren_        = new VObject[0];
        enum_                   = null;
        loaded_                 = -1;

        // Initialize the actions.
        actions_ = new VAction[] {
            new MessageQueueClearAction (this, queue_)
        };

        for (int i = 0; i < actions_.length; ++i) {
            actions_[i].addErrorListener (errorEventSupport_);
            actions_[i].addVObjectListener (objectEventSupport_);
            actions_[i].addVObjectListener (objectListener_);
            actions_[i].addWorkingListener (workingEventSupport_);
        }

        // Initialize the properties pane.
        propertiesPane_ = new MessageQueuePropertiesPane (this);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addVObjectListener (objectListener_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
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

@return Always false.
**/
//
// Implementation note: We do not allow sorting because of the fact
// that we load the list incrementally.
//
    public boolean isSortable ()
    {
        return false;
    }



/**
Loads information about the object from the system.
**/
    public void load ()
    {
        if (Trace.isTraceOn ())
            Trace.log (Trace.INFORMATION, "Loading messages from message queue " + this + ".");

        workingEventSupport_.fireStartWorking ();

        Exception error = null;

        try {                                                       // @A1A
            enum_ = queue_.getMessages();                           // @A1A
        }                                                           // @A1A
        catch (Exception e) {                                       // @A1A
            error = e;                                              // @A1A
        }                                                           // @A1A

        synchronized (this) {

            // Stop listening to the previous children.
            for (int i = 0; i < loaded_; ++i) {
                detailsChildren_[i].removeErrorListener (errorEventSupport_);
                detailsChildren_[i].removeVObjectListener (objectEventSupport_);
                detailsChildren_[i].removeVObjectListener (objectListener_);
                detailsChildren_[i].removeWorkingListener (workingEventSupport_);
            }

            // Refresh the children based on the queue.
            loaded_ = 0;
            // @A1D try {
            // @A1D     enum_ = queue_.getMessages ();
            if (error == null)                                      // @A1A
                detailsChildren_ = new VQueuedMessage[queue_.getLength ()];
            else                                                    // @A1A
                detailsChildren_ = new VQueuedMessage[0];           // @A1A

            // @A1D }
            // @A1D catch (Exception e) {
            // @A1D     error = e;
            // @A1D     detailsChildren_ = new VUser[0];
            // @A1D }
        }

        if (error != null)
            errorEventSupport_.fireError (error);

        objectEventSupport_.fireObjectChanged (this, true);           // @A1A

        workingEventSupport_.fireStopWorking ();
    }



/**
Loads more messages from the system.

@param index    The index needed.
**/
    private void loadMore (int index)
    {
        if (index >= loaded_) {

            workingEventSupport_.fireStartWorking ();

            Exception error = null;
            // @A1D synchronized (this) {

                for (int i = loaded_; i <= index; ++i) {
                    QueuedMessage message = (QueuedMessage) enum_.nextElement ();
                    detailsChildren_[i] = new VQueuedMessage (message, queue_);
                    detailsChildren_[i].addErrorListener (errorEventSupport_);
                    detailsChildren_[i].addVObjectListener (objectEventSupport_);
                    detailsChildren_[i].addVObjectListener (objectListener_);
                    detailsChildren_[i].addWorkingListener (workingEventSupport_);
                }

            // @A1D }

            loaded_ = index + 1;

            if (error != null)
                errorEventSupport_.fireError (error);

            workingEventSupport_.fireStopWorking ();
        }
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

Sets the fully qualified integrated file system path name of the message queue. This does not take effect the data until load() is done.

@param path The fully qualified integrated file system path name of the message queue. The path  must be in the format of /QSYS.LIB/libname.LIB/messageQueue.MSGQ.  The library and queue name must each be 10 characters or less.


@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.MessageQueue#setPath
**/
    public void setPath (String path)
        throws PropertyVetoException
    {
        if (path == null)
            throw new NullPointerException ("path");

        queue_.setPath (path);
    }



/**
Sets the selection.

@param selection The selection.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.MessageQueue#setSelection
**/
    public void setSelection (String selection)
        throws PropertyVetoException
    {
        if (selection == null)
            throw new NullPointerException ("selection");

        queue_.setSelection (selection);
    }



/**
Sets the severity.

@param severity The severity.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.MessageQueue#setSeverity
**/
    public void setSeverity (int severity)
        throws PropertyVetoException
    {
        queue_.setSeverity (severity);
    }



/**
Sets the system on which the message queue resides.

@param system The system on which the message queue resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        queue_.setSystem (system);
    }



/**
Sorts the children for the details.  Since sorting is not supported,
this method does nothing.

@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier. true for ascending order;
                            false for descending order.
**/
    public void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
      // No sorting here!
    }



/**
Returns the string representation of the description.

@return The string representation of the description.
**/
    public String toString ()
    {
        return getText ();
    }



/**
Listens for events and adjusts the children accordingly.
**/
    private class VObjectListener_
    implements VObjectListener, Serializable
    {



        public void objectChanged (VObjectEvent event)
        {
            // Nothing here.
        }



        public void objectCreated (VObjectEvent event)
        {
            // Nothing here.
        }



        public void objectDeleted (VObjectEvent event)
        {
            VObject object = event.getObject ();

            // Forward this event to the event support first,
            // so the the listener can handle it before we
            // go and remove the object from our list.
            objectEventSupport_.objectDeleted (event);

            // If the deleted object is contained in the list,
            // then remove it from the list.
            synchronized (VMessageQueue.this) {

                // Remove from the details children.
                int count;
                int index = getDetailsIndex (object);
                if (index >= 0) {
                    VObject[] oldDetailsChildren = detailsChildren_;
                    count = detailsChildren_.length;
                    detailsChildren_ = new VObject[count - 1];
                    System.arraycopy (oldDetailsChildren, 0,
                        detailsChildren_, 0, index);
                    System.arraycopy (oldDetailsChildren, index + 1,
                        detailsChildren_, index, count - index - 1);
                    --loaded_; // @B1A
                }

            }

            // Stop listening to the object.
            object.removeErrorListener (errorEventSupport_);
            object.removeVObjectListener (objectEventSupport_);
            object.removeVObjectListener (objectListener_);
            object.removeWorkingListener (workingEventSupport_);
        }

    }

}
