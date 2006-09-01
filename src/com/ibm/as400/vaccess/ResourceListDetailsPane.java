///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListDetailsPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceMetaData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;



/**
The ResourceListDetailsPane class represents a graphical user interface
that presents the contents of a
{@link com.ibm.as400.resource.ResourceList  ResourceList }
in a table.  Every row in the table represents a
{@link com.ibm.as400.resource.Resource  Resource } from the
list.  You must explicitly call <a href="#load()">load()</a>
to load the information from the resource list.

<p>The table columns are specified as an array of column attribute IDs.
The table will contain a column for each element of the array.
The following can be specified as column attribute IDs:
<ul>
<li>null - The name and icon from each Resource's
{@link com.ibm.as400.resource.Presentation Presentation }
object are presented in the column.
<li>Resource attribute IDs - These are defined by the
Resource objects that make up the rows of the table.
The corresponding attribute value is presented in the column.
</ul>

<p>Pop-up menus are enabled by default.
The pop-up menus will contain a single
"Properties" menu item which, when selected,
presents one of the following Properties dialogs:
<ul>
<li>If a list item is clicked, the dialog presents
    the corresponding Resource object's attribute
    values.  The layout of this Properties dialog
    is defined by the resource properties specified
    for this ResourceListDetailsPane object.
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

<p>ResourceListDetailsPane objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListSelectionEvent
    <li>PropertyChangeEvent
</ul>

<p>The following example creates a details pane which presents
a list of users.

<blockquote><pre>
// Create the resource list.  This example creates
// a list of all users on the system.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUserList userList = new RUserList(system);
<br>
// Create the ResourceListDetailsPane.  In this example,
// there are two columns in the table.  The first column
// contains the icons and names for each user.  The
// second column contains the text description for each
// user.
Object[] columnAttributeIDs = new Object[] { null, RUser.TEXT_DESCRIPTION };
ResourceListDetailsPane detailsPane = new ResourceListDetailsPane();
detailsPane.setResourceList(userList);
detailsPane.setColumnAttributeIDs(columnAttributeIDs);
<br>
// Add the ResourceListDetailsPane to a JFrame and show it.
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(detailsPane);
frame.pack();
frame.show();
<br>
// The ResourceListDetailsPane will appear empty until
// we load it.  This gives us control of when the list
// of users is retrieved from the system.
detailsPane.load();
</pre></blockquote>

@see ResourceListDetailsModel
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class ResourceListDetailsPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String ACTION_PROPERTIES_        = ResourceLoader.getText("ACTION_PROPERTIES");
    private static final String ACTION_LIST_PROPERTIES_   = ResourceLoader.getText("ACTION_LIST_PROPERTIES");



    // Private data.
    private boolean                   allowActions_               = true;
    private ResourceListDetailsModel  model_                      = null;
    private ResourceProperties        resourceProperties_         = null;
    private transient JScrollPane     scrollPane_                 = null;
    private transient JTable          table_                      = null;



    // Event support.
    transient private ErrorEventSupport               errorEventSupport_ ;
    transient private ListSelectionEventSupport       listSelectionEventSupport_;
    transient private ResourceListPopupMenuAdapter    popupMenuAdapter_;
    transient private PropertyChangeSupport           propertyChangeSupport_;



/**
Constructs a ResourceListDetailsPane object.
**/
    public ResourceListDetailsPane()
    {
        initializeCommon();

        // Set the default resource properties.
        resourceProperties_ = new ResourceProperties();
    }



/**
Constructs a ResourceListDetailsPane object.

@param resourceList         The resource list from which all information for the model is gathered.
@param columnAttributeIDs   The column attribute IDs.
@param resourceProperties   The resource properties.
**/
    public ResourceListDetailsPane(ResourceList resourceList,
                                   Object[] columnAttributeIDs,
                                   ResourceProperties resourceProperties)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");
        if (columnAttributeIDs == null)
            throw new NullPointerException ("columnAttributeIDs");
        if (resourceProperties == null)
            throw new NullPointerException ("resourceProperties");

        initializeCommon();
        resourceProperties_ = resourceProperties;
        model_.setResourceList(resourceList);
        model_.setColumnAttributeIDs(columnAttributeIDs);
        refreshColumns();
        popupMenuAdapter_.setResourceList(resourceList);
        popupMenuAdapter_.setResourceProperties(resourceProperties);
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
Indicates if pop-up menus are enabled.

@return true if pop-up menus are enabled, false otherwise.
**/
    public boolean getAllowActions ()
    {
        return allowActions_;
    }




/**
Returns the column attribute IDs.

@return The column attribute IDs.
**/
    public Object[] getColumnAttributeIDs()
    {
        return model_.getColumnAttributeIDs();
    }



/**
Returns the column model that is used to maintain the columns.
This provides the ability to programmatically add, remove,
reorder, and resize columns in the table.

@return The column model.
**/
    public TableColumnModel getColumnModel ()
    {
        return table_.getColumnModel ();
    }



/**
Returns the model that contains data for the table.

@return The model that contains data for the table.
**/
    public TableModel getModel ()
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
        int row = table_.rowAtPoint(point);
        if (row != -1)
            resource = model_.getResourceAt(row);
        return resource;
    }



/**
Returns the resource list from which all information for the model is gathered.

@return     The resource list from which all information for the model is gathered.
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
        int[] selectedRows = table_.getSelectedRows();
        if (selectedRows.length > 0)
            selectedResource = model_.getResourceAt(selectedRows[0]);
        return selectedResource;
    }



/**
Returns the resources which are represented by the selected rows.

@return  The resources which are represented by the selected rows.
         The array is empty if no resources are selected.

@see #getSelectionModel
@see #setSelectionModel
**/
    public Resource[] getSelectedResources()
    {
        int[] selectedRows = table_.getSelectedRows();
        Resource[] selectedResource = new Resource[selectedRows.length];
        for (int i = 0; i < selectedRows.length; ++i)
            selectedResource[i] = model_.getResourceAt(selectedRows[i]);
        return selectedResource;
    }



/**
Returns the selection model that is used to maintain row
selection state.  This provides the ability to programmatically
select and deselect resources.

@return The selection model.
**/
    public ListSelectionModel getSelectionModel ()
    {
        return table_.getSelectionModel ();
    }



/**
Initializes the common data.
**/
    private void initializeCommon()
    {
        // Initialize the model.
        model_ = new ResourceListDetailsModel();

        initializeTransient();
        setAllowActions(true);
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Workaround for Swing JTable serialization bug.
        addFocusListener(new SerializationListener(this));

        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport (this);
        listSelectionEventSupport_  = new ListSelectionEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);

        // Initialize the table.
        table_ = new JTable(model_);
        table_.setAutoCreateColumnsFromModel(false);
        table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table_.setColumnSelectionAllowed(false);
        table_.setRowSelectionAllowed(true);
        table_.setShowGrid(false);

        model_.addErrorListener(errorEventSupport_);
        model_.addPropertyChangeListener(propertyChangeSupport_);
        table_.getSelectionModel().addListSelectionListener(listSelectionEventSupport_);

        // Layout the pane.
        setLayout(new BorderLayout());
        scrollPane_ = new JScrollPane(table_);
        add("Center", scrollPane_);
        
        // Scrolling performance improvement.                                              
        table_.setDoubleBuffered(true);                                                    
        scrollPane_.getViewport().setBackingStoreEnabled(true);                             
        scrollPane_.setDoubleBuffered(true);                                                
        scrollPane_.getViewport().setDoubleBuffered(true);                                  

        // This option taken from Swing performance hints in JViewport javadoc             
        // and at http://java.sun.com/products/jfc/tsc/articles/performance/               
        scrollPane_.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);      

        // Initialize the other adapters.
        popupMenuAdapter_ = new ResourceListPopupMenuAdapter(this, model_.getResourceList(), resourceProperties_, errorEventSupport_);
        model_.addWorkingListener(new WorkingCursorAdapter(table_));
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

        int[] selectedRows = table_.getSelectedRows ();
        for (int i = 0; i < selectedRows.length; ++i) {
            Resource selectedResource = model_.getResourceAt(selectedRows[i]);
            if (selectedResource != null)
                if (selectedResource.equals(resource))
                    return true;
        }
        return false;
    }



/**
Loads the information from the resource list.
**/
    public void load()
    {
        table_.clearSelection();
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
Refreshes the columns.
**/
    private void refreshColumns()
    {
        // Remove the existing columns.  Note that we
        // must make our own list because the enumeration
        // gets wrecked when we remove an element.
        TableColumnModel tableColumnModel = table_.getColumnModel();
        Vector oldColumns = new Vector();
        for (Enumeration e = tableColumnModel.getColumns(); e.hasMoreElements(); )
            oldColumns.addElement(e.nextElement());

        for (Enumeration e = oldColumns.elements(); e.hasMoreElements(); ) {
            TableColumn column = (TableColumn)e.nextElement();
            tableColumnModel.removeColumn(column);
        }

        // Add the new columns.
        Object[] columnAttributeIDs = getColumnAttributeIDs();
        if (columnAttributeIDs != null) {
            for(int i = 0; i < columnAttributeIDs.length; ++i) {
                ResourceMetaData metaData = null;
                if (columnAttributeIDs[i] != null) {
                    try {
                        metaData = getResourceList().getAttributeMetaData(columnAttributeIDs[i]);
                    }
                    catch(Exception e) {
                        if (Trace.isTraceErrorOn())
                            Trace.log(Trace.ERROR, "Column attribute ID not valid:" + columnAttributeIDs[i], e);
                        // Ignore.  This just means an invalid column attribute ID was passed.
                    }
                }

                VTableColumn tableColumn = new VTableColumn(i, columnAttributeIDs[i]);
                tableColumn.setCellRenderer(new ResourceCellRenderer(metaData));
                tableColumn.setHeaderValue(model_.getColumnName(i));
                tableColumn.setHeaderRenderer(new ResourceHeaderRenderer(metaData));
                tableColumn.setPreferredCharWidth(15);
                tableColumnModel.addColumn(tableColumn);
            }
        }

        // Size the columns.  Get the font size.  Use M as a good sample character.
        int fontSize = 0;
        Font font = table_.getFont();
        if (font != null)
            fontSize = table_.getFontMetrics(font).charWidth('M');

        if (fontSize > 0) {
            TableColumnModel columnModel = table_.getColumnModel();
            int columnCount = columnModel.getColumnCount();
            for (int i = 0; i < columnCount; ++i) {
                TableColumn column = columnModel.getColumn(i);
                column.setPreferredWidth(model_.getColumnWidth(i) * fontSize + 10); //@B0C
            }
        }
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
        if (allowActions_) {
            scrollPane_.getViewport().addMouseListener(popupMenuAdapter_);
            table_.addMouseListener(popupMenuAdapter_);
        }
        else {
            scrollPane_.getViewport().removeMouseListener(popupMenuAdapter_);
            table_.removeMouseListener(popupMenuAdapter_);
        }

        propertyChangeSupport_.firePropertyChange("allowActions", oldValue, new Boolean(allowActions));
    }



/**
Sets the column attribute IDs.

@param columnAttributeIDs The column attribute IDs.
**/
    public void setColumnAttributeIDs(Object[] columnAttributeIDs)
    {
        if (columnAttributeIDs == null)
            throw new NullPointerException ("columnAttributeIDs");

        table_.clearSelection();
        model_.setColumnAttributeIDs(columnAttributeIDs);
        refreshColumns();
    }



/**
Sets the resource list from which all information for the model is gathered.

@param  resourceList The resource list from which all information for the model is gathered.
**/
    public void setResourceList(ResourceList resourceList)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");

        // Refresh the table and model.
        table_.clearSelection();
        model_.setResourceList(resourceList);
        refreshColumns();
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
           throw new NullPointerException("selectionModel");

        // Do not dispatch events from the old selection model any more.
        ListSelectionModel oldSelectionModel = table_.getSelectionModel ();
        if (oldSelectionModel != null)
            oldSelectionModel.removeListSelectionListener (listSelectionEventSupport_);

        table_.setSelectionModel (selectionModel);

        // Dispatch events from the new selection model.
        if (selectionModel != null)
            selectionModel.addListSelectionListener (listSelectionEventSupport_);
    }



}
