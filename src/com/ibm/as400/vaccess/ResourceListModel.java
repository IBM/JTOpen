///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceListEvent;
import com.ibm.as400.resource.ResourceListListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;



/**
The ResourceListModel class implements an underlying model for
a list, where all information for the model is gathered from
the contents of a
{@link com.ibm.as400.resource.ResourceList ResourceList}
object.   Every item represents a
{@link com.ibm.as400.resource.Resource Resource } from the
list.  You must explicitly call
<a href="#load()">load()</a> to load the information from the resource
list.

<p>Use this class if you want to customize the graphical
user interface that presents a list.  If you do not need
to customize the interface, then use
<a href="ResourceListPane.html">ResourceListPane</a>
instead.

<p>Most errors are reported as
<a href="ErrorEvent.html">ErrorEvents</a>
rather than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>ResourceListModel objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListDataEvent
    <li>PropertyChangeEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a list model filled with
the contents of a user list from a server.  It then presents
the list in a JList object.

<blockquote><pre>
// Create the resource list.  This example creates
// a list of all users on the system.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUserList userList = new RUserList(system);
<br>
// Create the ResourceListModel.
ResourceListModel listModel = new ResourceListModel(userList);
<br>
// Create a JList using the ResourceListModel.
JList list = new JList(listModel);
<br>
// Add the JList to a JFrame and show it.
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(new JScrollPane(list));
frame.pack();
frame.show();
<br>
// The JList will appear empty until we tell the
// ResourceListModel to load.  This gives us control
// of when the list of users is retrieved from the
// server.
listModel.load();
</pre></blockquote>
**/
public class ResourceListModel
implements ListModel, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean             exceptionOccured_   = false;
    private transient boolean   loaded_             = false;
    private ResourceList        resourceList_       = null;



    // Event support.
    transient private ErrorEventSupport       errorEventSupport_;
    transient         ListDataEventSupport    listDataEventSupport_;            // Private.
    transient private PropertyChangeSupport   propertyChangeSupport_;
    transient private ResourceListListener    resourceListListener_;
    transient private WorkingEventSupport     workingEventSupport_;



/**
Constructs a ResourceListModel object.
**/
    public ResourceListModel()
    {
        initializeTransient();
    }



/**
Constructs a ResourceListModel object.

@param resourceList     The resource list.
**/
    public ResourceListModel(ResourceList resourceList)
    {
        if (resourceList == null)
            throw new NullPointerException("resourceList");

        resourceList_ = resourceList;
        initializeTransient();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }



/**
Adds a listener to be notified when the contents of
the list change.

@param  listener    The listener.
**/
    public void addListDataListener(ListDataListener listener)
    {
        listDataEventSupport_.addListDataListener(listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }



/**
Returns the element at the specified index.

@param  rowIndex   The row index (0-based).
@return The element at the specified index. It will be null if the index
        is not valid or the root has not been set.
**/
    public Object getElementAt(int rowIndex)
    {
        if ((resourceList_ == null)
            || (loaded_ == false)
            || (exceptionOccured_))
            return null;

        try {
            // Validate the index.
            if ((rowIndex < 0) || ((resourceList_.isComplete()) && (rowIndex > resourceList_.getListLength())))
                return null;

            return resourceList_.resourceAt(rowIndex);
        }
        catch(Exception e) {
            errorEventSupport_.fireError(e);
            exceptionOccured_ = true;
            return null;
        }
    }



/**
Returns the resource list from which all information for the model is gathered.

@return     The resource list from which all information for the model is gathered.
            This will be null if none has been set.
**/
    public ResourceList getResourceList()
    {
        return resourceList_;
    }



/**
Returns the number of objects in the list.

@return The number of objects in the list.
**/
    public int getSize ()
    {
        if ((resourceList_ == null)
            || (loaded_ == false)
            || (exceptionOccured_))
            return 0;

        try {
            // If the resource list is not complete, then refresh
            // its status.  This will force the reported length
            // to be periodically updated.
            if (! resourceList_.isComplete())
                resourceList_.refreshStatus();

            long listLength = resourceList_.getListLength();
            return (int)listLength;
        }
        catch(Exception e) {
            errorEventSupport_.fireError(e);
            exceptionOccured_ = true;
            return 0;
        }
    }



/**
Initializes transient data.
**/
    private void initializeTransient()
    {
        errorEventSupport_      = new ErrorEventSupport(this);
        listDataEventSupport_   = new ListDataEventSupport(this);
        propertyChangeSupport_  = new PropertyChangeSupport(this);
        resourceListListener_   = new ResourceListListener_();
        workingEventSupport_    = new WorkingEventSupport(this);

        if (resourceList_ != null) {
            resourceList_.addActiveStatusListener(workingEventSupport_);
            resourceList_.addResourceListListener (resourceListListener_);
        }
    }



/**
Loads the information from the resource list.
**/
    public void load()
    {
        int previousLength = getSize();

        if ((loaded_) && (resourceList_ != null) && (!exceptionOccured_)) {
            try {
                resourceList_.refreshContents();
            }
            catch(Exception e) {
                errorEventSupport_.fireError(e);
                exceptionOccured_ = true;
            }
        }
        else
            loaded_ = true;

        listDataEventSupport_.fireContentsChanged(0, previousLength);
    }




/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient();
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }



/**
Removes a list data listener.

@param  listener    The listener.
**/
    public void removeListDataListener(ListDataListener listener)
    {
        listDataEventSupport_.removeListDataListener(listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener(listener);
    }



/**
Removes a working listener.

@param  listener  The listener.
**/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }



/**
Sets the resource list from which all information for the model is gathered.

@param  resourceList The resource list from which all information for the model is gathered.
**/
    public void setResourceList(ResourceList resourceList)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");

        ResourceList oldValue = resourceList_;

        if (oldValue != resourceList) {

            int previousLength = getSize();

            // Redirect event support.
            if (oldValue != null) {
                oldValue.removeActiveStatusListener(workingEventSupport_);
                oldValue.removeResourceListListener(resourceListListener_);
            }
            resourceList.addActiveStatusListener(workingEventSupport_);
            resourceList.addResourceListListener(resourceListListener_);

            // Set the resource list.
            resourceList_ = resourceList;
            exceptionOccured_ = false;

            // Change the contents of the list.
            listDataEventSupport_.fireContentsChanged(0, previousLength);
        }

        propertyChangeSupport_.firePropertyChange ("resourceList", oldValue, resourceList_);
    }



/**
The ResourceListListener_ class processes ResourceListEvents and
fires the appropriate ListDataEvents.
**/
    private class ResourceListListener_ implements ResourceListListener
    {

        private long length_ = 0;

        public void lengthChanged(ResourceListEvent event)
        {
            long newLength = event.getLength();
            listDataEventSupport_.fireIntervalAdded((int)length_, (int)newLength);
            length_ = newLength;
        }

        public void listClosed(ResourceListEvent event)
        {
            // Do nothing.
        }

        public void listCompleted(ResourceListEvent event)
        {
            // Do nothing.
        }

        public void listInError(ResourceListEvent event)
        {
            // Do nothing.
        }

        public void listOpened(ResourceListEvent event)
        {
           // Do nothing.
        }

        public void resourceAdded(ResourceListEvent event)
        {
           // Do nothing.
        }

    }


}


