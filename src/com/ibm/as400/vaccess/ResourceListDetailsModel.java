///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListDetailsModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.Presentation;
import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceListEvent;
import com.ibm.as400.resource.ResourceListListener;
import com.ibm.as400.resource.ResourceMetaData;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;



/**
The ResourceListDetailsModel class implements an underlying 
model for a table, where all information for the model is gathered
from the contents of a {@link com.ibm.as400.resource.ResourceList ResourceList} 
object.  Every row in the table represents a
{@link com.ibm.as400.resource.Resource Resource} from the
list.  You must explicitly call <a href="#load()">load()</a> 
to load the information from the resource list.

<p>The table columns are specified as an array of column attribute IDs.
The table will contain a column for each element of the array.  
The following can be specified as column attribute IDs:
<ul>
<li>null - The entire resource is returned as the table
data for the column.
<li>Resource attribute IDs - These are defined by the 
{@link com.ibm.as400.resource.Resource Resource}
objects that make up the rows of the table.  The corresponding
attribute value is returned as the table data for the column.
</ul>

<p>Use this class if you want to customize the graphical
user interface that presents a table.  If you do not need
to customize the graphical user interface, then use 
{@link com.ibm.as400.vaccess.ResourceListDetailsPane ResourceListDetailsPane} 
instead.

<p>Most errors are reported as 
{@link com.ibm.as400.vaccess.ErrorEvent ErrorEvents}
rather than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>ResourceListDetailsModel objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>TableModelEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a details model filled with
details about the jobs running on a server.  It then presents
the table in a JTable object.

<blockquote><pre>
// Create the resource list.  This example creates
// a list of all jobs on the system.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJobList jobList = new RJobList(system);
<br>
// Create the ResourceListDetailsModel.  In this example,
// there are four columns in the table.  The first column
// contains the icons and names for each job.  The remaining
// columns contain the status, type, and subtype, respectively,
// for each job.
Object[] columnAttributeIDs = new Object[] { null, RJob.JOB_STATUS, RJob.JOB_TYPE, RJob.JOB_SUBTYPE };
ResourceListDetailsModel detailsModel = new ResourceListDetailsModel(jobList, columnAttributeIDs);
<br>
// Create a JTable using the ResourceListDetailsModel.
JTable table = new JTable(detailsModel);
<br>
// Add the JTable to a JFrame and show it.
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(new JScrollPane(table));
frame.pack();
frame.show();
<br>
// The JTable will appear empty until we tell the
// ResourceListDetailsModel to load.  This gives us 
// control of when the list of jobs is retrieved 
// from the server.
detailsModel.load();
</pre></blockquote>
**/
public class ResourceListDetailsModel
implements TableModel, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String RESOURCE_COLUMN_NAME_       = ResourceLoader.getText("RESOURCE_COLUMN_NAME");
    


    // Constants.
    private static final int    COLUMN_SOURCE_NOT_KNOWN_                = 0;
    private static final int    COLUMN_SOURCE_RESOURCE_                 = 1;
    private static final int    COLUMN_SOURCE_RESOURCE_ATTRIBUTE_       = 2;
    private static final int    COLUMN_SOURCE_NOT_VALID_                = 99;



    // Private data.
    private boolean             exceptionOccured_   = false;
    private transient boolean   loaded_             = false;
    private ResourceList        resourceList_       = null;
    private Object[]            columnAttributeIDs_ = null;
    private int[]               columnSources_      = null;



    // Event support.
    transient private ErrorEventSupport       errorEventSupport_;
    transient private PropertyChangeSupport   propertyChangeSupport_;
    transient private ResourceListListener    resourceListListener_;
    transient         TableModelEventSupport  tableModelEventSupport_;      // Private.
    transient         WorkingEventSupport     workingEventSupport_;         // Private.



/**
Constructs a ResourceListDetailsModel object. 
**/
    public ResourceListDetailsModel()
    {
        initializeTransient();
    }



/**
Constructs a ResourceListDetailsModel object.

@param resourceList         The resource list from which all information for the model is gathered.
@param columnAttributeIDs   The column attribute IDs.
**/
    public ResourceListDetailsModel(ResourceList resourceList, Object[] columnAttributeIDs)
    {
        if (resourceList == null)
            throw new NullPointerException ("resourceList");
        if (columnAttributeIDs == null)
            throw new NullPointerException ("columnAttributeIDs");

        initializeTransient ();
        setResourceList(resourceList);
        setColumnAttributeIDs(columnAttributeIDs);
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
Adds a listener to be notified when the contents of the
table change.

@param  listener  The listener.
**/
    public void addTableModelListener (TableModelListener listener)
    {
        tableModelEventSupport_.addTableModelListener (listener);
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
Returns the column attribute IDs.

@return The column attribute IDs.
**/
    public Object[] getColumnAttributeIDs()
    {
        return columnAttributeIDs_;
    }



/**
Returns the class for objects that are contained in a column.

@param  columnIndex   The column index (0-based).
@return               The column class. 
**/
    public Class getColumnClass (int columnIndex)
    {
        switch(getColumnSource(columnIndex)) {           

        case COLUMN_SOURCE_RESOURCE_:
            return Resource.class;

        case COLUMN_SOURCE_RESOURCE_ATTRIBUTE_:
            return resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]).getType();
        
        default:
            return null;
        }
    }



/**
Returns the number of columns.

@return The number of columns.
**/
    public int getColumnCount ()
    {
        if ((resourceList_ == null) || (columnAttributeIDs_ == null))
            return 0;

        return columnAttributeIDs_.length;
    }



/**
Returns the name of a column.

@param  columnIndex   The column index (0-based).
@return               The column name.
**/
    public String getColumnName(int columnIndex)
    {
        if ((resourceList_ == null) || (columnAttributeIDs_ == null))
            return null;

        switch(getColumnSource(columnIndex)) {           

        case COLUMN_SOURCE_RESOURCE_:
            return RESOURCE_COLUMN_NAME_;

        case COLUMN_SOURCE_RESOURCE_ATTRIBUTE_:
            return resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]).getPresentation().getName();
        
        default:
            if ((columnIndex < 0) || (columnIndex >= columnAttributeIDs_.length))
                return null;
            else
                return columnAttributeIDs_[columnIndex].toString();
        }
    }



/**
Returns the column source.

@param columnIndex  The column index (0-based).
@return             The column source.
**/
    private int getColumnSource(int columnIndex)
    {
        if (columnAttributeIDs_ == null)
            return COLUMN_SOURCE_NOT_VALID_;
        if ((columnIndex < 0) || (columnIndex >= columnAttributeIDs_.length))
            return COLUMN_SOURCE_NOT_VALID_;

        // The first time a column is retrieved, we determine its
        // source so that future lookups can be faster.
        if (columnSources_[columnIndex] == COLUMN_SOURCE_NOT_KNOWN_) {
            if (columnAttributeIDs_[columnIndex] == null)
                columnSources_[columnIndex] = COLUMN_SOURCE_RESOURCE_;
            else {
                try {
                    ResourceMetaData attributeMetaData = resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]);                
                    columnSources_[columnIndex] = COLUMN_SOURCE_RESOURCE_ATTRIBUTE_;
                }
                catch(IllegalArgumentException e) {
                    columnSources_[columnIndex] = COLUMN_SOURCE_NOT_VALID_;
                }
            }
        }
        return columnSources_[columnIndex];
    }



/**
Returns the preferred column width, in characters.

@param  columnIndex   The column index (0-based).
@return               The preferred column width, in characters.
**/
    public int getColumnWidth(int columnIndex)
    {
        if ((resourceList_ == null) || (columnAttributeIDs_ == null))
            return 20;

        switch(getColumnSource(columnIndex)) {           

        case COLUMN_SOURCE_RESOURCE_:
        default:
            return 20;

        case COLUMN_SOURCE_RESOURCE_ATTRIBUTE_:
            int dataWidth = 20;
            ResourceMetaData attributeMetaData = resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]);
            Class type = attributeMetaData.getType();
            Presentation[] possibleValuePresentations = attributeMetaData.getPossibleValuePresentations();

            // If it is a numeric value, use 10.
            if (Number.class.isAssignableFrom(type))
                dataWidth = 10;

            // If it has possible values, use the width of the longest.
            else if (possibleValuePresentations.length > 0) {
                dataWidth = 1;
                for(int i = 0; i < possibleValuePresentations.length; ++i) {
                    int nameLength = possibleValuePresentations[i].getName().toString().length();
                    if (nameLength > dataWidth)
                        dataWidth = nameLength;
                }
            }

            // Make sure its big enough to show the header.
            int nameLength = attributeMetaData.getPresentation().getName().toString().length();
            if (nameLength > dataWidth)
                dataWidth = nameLength;

            return dataWidth;

        }
    }



/**
Returns the resource at the specified row.

@param  rowIndex            The row index (0-based).
@return                     The object at the specified row.
**/
    public Resource getResourceAt(int rowIndex)
    {
        if ((resourceList_ == null) 
            || (columnAttributeIDs_ == null)
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
**/
    public ResourceList getResourceList()
    {
        return resourceList_;
    }



/**
Returns the number of rows in the table.

@return The number of rows in the table, or 0 if the root
        has not been set.
**/
    public int getRowCount ()
    {
        if ((resourceList_ == null) 
            || (columnAttributeIDs_ == null)
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
Returns the value at the specified row and column.

@param  rowIndex            The row index (0-based).
@param  columnIndex         The column index (0-based).
@return                     The value at the specified
                            row and column. It will be null if the
                            column index is not valid or the root has
                            not been set. 
**/
    public Object getValueAt (int rowIndex, int columnIndex)
    {
        if ((resourceList_ == null) 
            || (columnAttributeIDs_ == null)
            || (loaded_ == false)
            || (exceptionOccured_))
            return null;

        Resource resource = getResourceAt(rowIndex);
        if (resource == null)
            return null;
        
        switch(getColumnSource(columnIndex)) {           

        case COLUMN_SOURCE_RESOURCE_:
            return resource;

        case COLUMN_SOURCE_RESOURCE_ATTRIBUTE_:
            resource.addActiveStatusListener(workingEventSupport_);
            try {
                return resource.getAttributeValue(columnAttributeIDs_[columnIndex]);
            }
            catch(Exception e) {
                errorEventSupport_.fireError(e);
                exceptionOccured_ = true;
                return null;
            }
            finally {
                resource.removeActiveStatusListener(workingEventSupport_);
            }
        
        default:
            return null;
        }
    }



/**
Indicates if the cell is editable. 
This always returns false.  No cells are editable.

@param  rowIndex            The row index (0-based).
@param  columnIndex         The column index (0-based).
@return                     Always false.
**/
    public boolean isCellEditable (int rowIndex, int columnIndex)
    {
        return false;
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        errorEventSupport_      = new ErrorEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        resourceListListener_   = new ResourceListListener_();
        tableModelEventSupport_ = new TableModelEventSupport (this, this);
        workingEventSupport_    = new WorkingEventSupport (this);

        if (resourceList_ != null) {
            resourceList_.addActiveStatusListener(workingEventSupport_);
            resourceList_.addResourceListListener(resourceListListener_);         
        }
    }



/**
Loads the information from the resource list. 
**/
    public void load()
    {
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

        tableModelEventSupport_.fireTableChanged(-1);
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
Removes a table model listener.

@param  listener  The listener.
**/
    public void removeTableModelListener (TableModelListener listener)
    {
        tableModelEventSupport_.removeTableModelListener (listener);
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
Sets the column attribute IDs.

@param columnAttributeIDs The column attribute IDs.
**/
    public void setColumnAttributeIDs(Object[] columnAttributeIDs)
    {
        if (columnAttributeIDs == null)
            throw new NullPointerException ("columnAttributeIDs");

        Object[] oldValue = columnAttributeIDs_;

        if (oldValue != columnAttributeIDs) {

            columnAttributeIDs_ = columnAttributeIDs;

            columnSources_ = new int[columnAttributeIDs_.length];

            // Change the contents of the list.
            tableModelEventSupport_.fireTableChanged (-1);
        }

        propertyChangeSupport_.firePropertyChange ("columnAttributeIDs", oldValue, columnAttributeIDs_);
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
            if (columnAttributeIDs_ != null)
                columnSources_ = new int[columnAttributeIDs_.length];

            // Change the contents of the list.
            tableModelEventSupport_.fireTableChanged (-1);
        }

        propertyChangeSupport_.firePropertyChange ("resourceList", oldValue, resourceList_);
    }



/**
Sets the value at the specified row and column. 
This has no effect, the cells are not editable.

@param value                The value.
@param  rowIndex            The row index (0-based).
@param  columnIndex         The column index (0-based).
**/
    public void setValueAt (Object value,
                            int rowIndex,
                            int columnIndex)
    {
        // Nothing.
    }



/**
The ResourceListListener_ class processes ResourceListEvents and 
fires the appropriate ListDataEvents and WorkingEvents.
**/
    private class ResourceListListener_ implements ResourceListListener
    {

        private long length_ = 0;

        public void lengthChanged(ResourceListEvent event)
        {
            long newLength = event.getLength();
            tableModelEventSupport_.fireTableChanged((int)length_, (int)newLength, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
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

