///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListPane.java
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;



/**
The ResourceListPane class represents a graphical user interface
that presents the contents of a
{@link com.ibm.as400.resource.ResourceList ResourceList}
in a list.  Every item represents a
{@link com.ibm.as400.resource.Resource Resource} from the
list.  You must explicitly call <a href="#load()">load()</a>
to load the information from the resource list.

<p>Pop-up menus are enabled by default.
The pop-up menus will contain a single
"Properties" menu item which, when selected,
presents one of the following Properties dialogs:
<ul>
<li>If a list item is clicked, the dialog presents
    the corresponding Resource object's attribute
    values.  The layout of this Properties dialog
    is defined by the resource properties specified
    for this ResourceListPane object.
    By default, it will contain a "General" tab
    which shows the Resource object's icon and
    full name.
<li>If something other than a list item is clicked,
    the dialog presents the ResourceList object's
    selection and sort values.
</ul>

<p>Most errors are reported as
<a href="ErrorEvent.html">ErrorEvents</a>
rather than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>ResourceListPane objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListSelectionEvent
    <li>PropertyChangeEvent
</ul>

<p>The following example creates a list pane filled with
the list of messages in a message queue.

<blockquote><pre>
// Create the resource list.  This example creates
// a list of all messages in a message queue.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RMessageQueue mq = new RMessageQueue(system, "/QSYS.LIB/MYLIB.LIB/MYMQ.MSGQ");
<br>
// Create the ResourceListPane.
ResourceListPane listPane = new ResourceListPane();
listPane.setResourceList(mq);
<br>
// Add the ResourceListPane to a JFrame and show it.
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(listPane);
frame.pack();
frame.show();
<br>
// The ResourceListPane will appear empty until we
// load it.  This gives us control of when the list
// of messages is retrieved from the server.
listPane.load();
</pre></blockquote>

@see ResourceListModel
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class ResourceListPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String ACTION_PROPERTIES_          = ResourceLoader.getText("ACTION_PROPERTIES");
    private static final String ACTION_LIST_PROPERTIES_     = ResourceLoader.getText("ACTION_LIST_PROPERTIES");



    // Private data.
    private boolean                   allowActions_               = true;
    private transient JList           list_                       = null;
    private ResourceListModel         model_                      = null;
    private ResourceProperties        resourceProperties_         = null;



    // Event support.
    transient private ErrorEventSupport               errorEventSupport_;
    transient private ListSelectionEventSupport       listSelectionEventSupport_;
    transient private ResourceListPopupMenuAdapter    popupMenuAdapter_;
    transient private PropertyChangeSupport           propertyChangeSupport_;



/**
Constructs a ResourceListPane object.
**/
    public ResourceListPane()
    {
        initializeCommon();

        // Set the default resource properties.
        resourceProperties_ = new ResourceProperties();
    }



/**
Constructs a ResourceListPane object.

@param resourceList         The resource list.
@param resourceProperties   The resource properties.
**/
    public ResourceListPane(ResourceList resourceList,
                            ResourceProperties resourceProperties)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");
        if (resourceProperties == null)
            throw new NullPointerException ("resourceProperties");

        initializeCommon();

        resourceProperties_ = resourceProperties;
        model_.setResourceList(resourceList);
        popupMenuAdapter_.setResourceList(resourceList);
        popupMenuAdapter_.setResourceProperties(resourceProperties);
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener  The listener.
**/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }



/**
Adds a listener to be notified when a list selection occurs.

@param  listener  The listener.
**/
    public void addListSelectionListener(ListSelectionListener listener)
    {
        listSelectionEventSupport_.addListSelectionListener(listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
        propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Indicates if pop-up menus are enabled.

@return true if pop-up menus are enabled, false otherwise.
**/
    public boolean getAllowActions ()
    {
        return allowActions_;
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
Returns the resource that corresponds to the row located at the specified
point.  This can be useful for present customized pop-up menus.

@param point        The point.
@return             The resource that corresponds to the row located at
                    the point, or null if no such resource exists.
**/
    public Resource getResourceAtPoint(Point point)
    {
        Resource resource = null;
        int row = list_.locationToIndex(point);
        if (row != -1)
            resource = (Resource)model_.getElementAt(row);
        return resource;
    }



/**
Returns the resource list from which all information for the model is gathered.

@return     The resource list from which all information for the model is gathered.
            This will be null if none has been set.
**/
    public ResourceList getResourceList()
    {
        return model_.getResourceList();
    }



/**
Returns the resource properties.  The resource properties object
describes the arrangement of the Properties dialog that is presented
when the user right clicks on a list item and selects "Properties".
**/
    public ResourceProperties getResourceProperties()
    {
        return resourceProperties_;
    }



/**
Returns the first selected resource.

@return The first selected resource, or null if none are selected.

@see #getSelectionModel
@see #setSelectionModel
**/
    public Resource getSelectedResource()
    {
        Resource selectedResource = null;
        int selectedIndex = list_.getSelectedIndex();
        if (selectedIndex >= 0)
            selectedResource = (Resource)model_.getElementAt(selectedIndex);
        return selectedResource;
    }



/**
Returns the selected resources.

@return  The selected resources.
         The array is empty if no resources are selected.

@see #getSelectionModel
@see #setSelectionModel
**/
    public Resource[] getSelectedResources()
    {
        int[] selectedIndices = list_.getSelectedIndices();
        int selectedCount = selectedIndices.length;
        Resource[] selectedResources = new Resource[selectedCount];
        for (int i = 0; i < selectedCount; ++i)
            selectedResources[i] = (Resource)model_.getElementAt(selectedIndices[i]);
        return selectedResources;
    }



/**
Returns the selection model that is used to maintain
selection state.  This provides the ability to programmatically
select and deselect resources.

@return The selection model.
**/
    public ListSelectionModel getSelectionModel ()
    {
        return list_.getSelectionModel();
    }



/**
Returns the preferred number of visible rows.

@return The preferred number of visible rows.
**/
    public int getVisibleRowCount()
    {
        return list_.getVisibleRowCount();
    }



/**
Initializes the common data.
**/
    private void initializeCommon()
    {
        // Initialize the model.
        model_ = new ResourceListModel();

        initializeTransient();

        // Layout the pane.
        setLayout(new BorderLayout());
        add("Center", new JScrollPane(list_));
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport(this);
        listSelectionEventSupport_  = new ListSelectionEventSupport(this);
        propertyChangeSupport_      = new PropertyChangeSupport(this);

        // Initialize the list.
        list_ = new JList(model_);
        list_.setCellRenderer(new ResourceCellRenderer(null));

        model_.addErrorListener(errorEventSupport_);
        model_.addPropertyChangeListener(propertyChangeSupport_);
        list_.getSelectionModel().addListSelectionListener(listSelectionEventSupport_);

        // Initialize the other adapters.
        popupMenuAdapter_ = new ResourceListPopupMenuAdapter(this, model_.getResourceList(), resourceProperties_, errorEventSupport_);
        model_.addWorkingListener(new WorkingCursorAdapter(list_));
        setAllowActions(allowActions_);
    }



/**
Indicates if the resource is selected.

@param  resource The resource.
@return true if the resource is selected; false otherwise.

@see #getSelectionModel
@see #setSelectionModel
**/
    public boolean isSelected(Resource resource)
    {
        if (resource == null)
            throw new NullPointerException ("resource");

        Object[] selectedResources = list_.getSelectedValues();
        for (int i = 0; i < selectedResources.length; ++i)
            if (selectedResources[i].equals(resource))
                return true;
        return false;
    }



/**
Loads the information from the resource list.
**/
    public void load()
    {
        list_.clearSelection();
        model_.load();
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
Sets whether pop-up menus are enabled.

@param allowActions true to enable pop-up menus, false otherwise.
       The default is true.
**/
    public void setAllowActions(boolean allowActions)
    {
        Boolean oldValue = new Boolean(allowActions_);

        allowActions_ = allowActions;
        if (allowActions_)
            list_.addMouseListener(popupMenuAdapter_);
        else
            list_.removeMouseListener(popupMenuAdapter_);

        propertyChangeSupport_.firePropertyChange("allowActions", oldValue, new Boolean(allowActions));
    }



/**
Sets the resource list from which all information for the model is gathered.

@param  resourceList The resource list from which all information for the model is gathered.
**/
    public void setResourceList(ResourceList resourceList)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");

        list_.clearSelection();
        model_.setResourceList(resourceList);
        popupMenuAdapter_.setResourceList(resourceList);
    }



/**
Sets the resource properties.  The resource properties object
describes the arrangement of the Properties dialog that is presented when
the user right clicks on a list item and selects "Properties".

@param resourceProperties   The resource properties.
**/
    public void setResourceProperties(ResourceProperties resourceProperties)
    {
        if (resourceProperties == null)
            throw new NullPointerException("resourceProperties");

        ResourceProperties oldValue = resourceProperties_;

        resourceProperties_ = resourceProperties;
        popupMenuAdapter_.setResourceProperties(resourceProperties);

        propertyChangeSupport_.firePropertyChange("resourceProperties", oldValue, resourceProperties);
    }



/**
Sets the selection model that is used to maintain selection
state.  This provides the ability to programmatically select
and deselect resources.

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


}
