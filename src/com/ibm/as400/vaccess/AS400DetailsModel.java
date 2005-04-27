///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400DetailsModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;



/**
The AS400DetailsModel class implements an underlying model for
a table, where all information for the model is gathered
from the contents of a server resource, known as the root.
You must explicitly call load() to load the information from
the server.

<p>Use this class if you want to customize the graphical
user interface that presents a table.  If you do not need
to customize the interface, then use AS400DetailsPane instead.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400DetailsModel objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>TableModelEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a details model filled with
details about the jobs running on a server.  It then presents
the table in a JTable object.

<pre>
<br>
// Set up the details model and JTable.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VJobList jobList = new VJobList (system);
AS400DetailsModel detailsModel = new AS400DetailsModel (jobList);
detailsModel.load ();
JTable table = new JTable (detailsModel);
<br>
// Add the JTable to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(table));

</pre>

@see AS400DetailsPane
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400DetailsModel
implements TableModel, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    VNode root_                   = null; // Private.



    // Event support.
    transient private ErrorEventSupport       errorEventSupport_;
    transient private VObjectListener         objectListener_;
    transient private PropertyChangeSupport   propertyChangeSupport_;
    transient         TableModelEventSupport  tableModelEventSupport_; // Private.
    transient private VetoableChangeSupport   vetoableChangeSupport_;
    transient private WorkingEventSupport     workingEventSupport_;



/**
Constructs an AS400DetailsModel object. 
**/
    public AS400DetailsModel ()
    {
        initializeTransient ();
    }



/**
Constructs an AS400DetailsModel object.

@param  root    The root, or the server resource, from which all information for the model is gathered.

**/
    public AS400DetailsModel (VNode root)
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
Returns the class for objects that are contained in
a column.

@param  columnIndex   The column index.
@return               The column class. It will be null if the
                      column index is not valid or the root has
                      not been set.
                      
**/
    public Class getColumnClass (int columnIndex)
    {
        // Validate the root.
        if (root_ == null)
            return null;

        // Validate the column index.
        TableColumnModel columnModel = root_.getDetailsColumnModel ();
        if (columnModel == null)
            return null;
        if ((columnIndex < 0) || (columnIndex >= columnModel.getColumnCount ()))
            return null;

        return Object.class;
    }



/**
Returns the number of columns.

@return The number of columns.  This will be 0 if the
        root has not been set.
**/
    public int getColumnCount ()
    {
        // Validate the root.
        if (root_ == null)
            return 0;

        // Validate the column model.
        TableColumnModel columnModel = root_.getDetailsColumnModel ();
        if (columnModel == null)
            return 0;

        return root_.getDetailsColumnModel ().getColumnCount ();
    }



/**
Returns the name of a column.

@param  columnIndex   The column index.
@return               The column name. It will be null if the
                      column index is not valid or the root has
                      not been set.
**/
    public String getColumnName (int columnIndex)
    {
        // Validate the root.
        if (root_ == null)
            return null;

        // Validate the column index.
        TableColumnModel columnModel = root_.getDetailsColumnModel ();
        if (columnModel == null)
            return null;
        if ((columnIndex < 0) || (columnIndex >= columnModel.getColumnCount ()))
            return null;

        return columnModel.getColumn (columnIndex).getHeaderValue ().toString ();
    }



/**
Returns the object at the specifed row.

@param  rowIndex            The row index.
@return                     The object at the specified
                            row. It will be null if the index is
                            not valid or the root
                            has not been set.
**/
    public VObject getObjectAt (int rowIndex)
    {
        // Make sure the root has been set.
        if (root_ == null)
            return null;

        // Validate the row index.
        if ((rowIndex < 0) || (rowIndex >= root_.getDetailsChildCount ()))
            return null;

        // Return the object.
        return root_.getDetailsChildAt (rowIndex);
    }



/**
Returns the root, or the server resource, from which all information for the model is gathered.

@return     The root, or the server resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public VNode getRoot ()
    {
        return root_;
    }



/**
Returns the number of rows in the table.

@return The number of rows in the table, or 0 if the root
        has not been set.
**/
    public int getRowCount ()
    {
        // Make sure the root has been set.
        if (root_ == null)
            return 0;

        return root_.getDetailsChildCount ();
    }



/**
Returns the value at the specifed row and column.

@param  rowIndex            The row index.
@param  columnIndex         The column index.
@return                     The value at the specified
                            row and column. It will be null if the
                            column index is not valid or the root has
                            not been set. 
**/
    public Object getValueAt (int rowIndex, int columnIndex)
    {
        // Make sure the root has been set.
        if (root_ == null)
            return null;

        // Validate the row index.
        if ((rowIndex < 0) || (rowIndex >= getRowCount ()))
            return null;

        // Validate the column index.
        TableColumnModel columnModel = root_.getDetailsColumnModel ();
        if (columnModel == null)
            return null;
        if ((columnIndex < 0) || (columnIndex >= columnModel.getColumnCount ()))
            return null;

        // Get the object represented by the row.
        VObject object = root_.getDetailsChildAt (rowIndex);
        if (object == null)
            return null;

        // Return the value.
        Object value = object.getPropertyValue (columnModel.getColumn (columnIndex).getIdentifier ());
        return value;
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        // Initialize event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectListener_         = new VObjectListener_ ();
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        tableModelEventSupport_ = new TableModelEventSupport (this, this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        if (root_ != null) {
            root_.addErrorListener (errorEventSupport_);
            root_.addVObjectListener (objectListener_);
            root_.addWorkingListener (workingEventSupport_);
        }
    }



/**
Indicates if the cell is editable.

@param  rowIndex            The row index.
@param  columnIndex         The column index.
@return                     true if the cell is editable;
                            false if the cell is not editable,
                            the index is not valid, or the
                            root has not been set.
**/
    public boolean isCellEditable (int rowIndex, int columnIndex)
    {
        // Validate the root.
        if (root_ == null)
            return false;

        // Validate the row index.
        if ((rowIndex < 0) || (rowIndex >= getRowCount ()))
            return false;

        // Validate the column index.
        TableColumnModel columnModel = root_.getDetailsColumnModel ();
        if (columnModel == null)
            return false;
        if ((columnIndex < 0) || (columnIndex >= columnModel.getColumnCount ()))
            return false;

        return (columnModel.getColumn (columnIndex).getCellEditor() != null);
    }



/**
Loads the information from the server. 
**/
    public void load ()
    {
        if (root_ != null)
            root_.load ();

        tableModelEventSupport_.fireTableChanged (-1);
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
Sets the root, or the server resource, from which all information for the model is gathered. It will not take effect until load() is done.

@param  root   The root, or the server resource, from which all information for the model is gathered.

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

            // Redirect error event support.
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

            // Change the contents of the list.
            tableModelEventSupport_.fireTableChanged (-1);
        }

        propertyChangeSupport_.firePropertyChange ("root", oldValue, newValue);
    }



/**
Sets the value at the specifed row and column. This method has no effect, the value will not change.

@param value                The value.
@param  rowIndex            The row index.
@param  columnIndex         The column index.
**/
    public void setValueAt (Object value,
                            int rowIndex,
                            int columnIndex)
    {
        // Nothing.
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
                tableModelEventSupport_.fireTableChanged (-1);
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
                tableModelEventSupport_.fireTableChanged();             // @A1A @E1C
            }                                                           // @A1A

            // If the changed object is contained in the table,
            // then fix up its row.
            else {
                int index = root_.getDetailsIndex (object);
                if (index >= 0)
                    tableModelEventSupport_.fireTableChanged (index);
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
                    tableModelEventSupport_.fireTableChanged (index - 1,
                        index - 1, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT);
            }
        }



        public void objectDeleted (VObjectEvent event)
        {
            VObject object = event.getObject ();

            // If the deleted object is contained in the list,
            // then remove it from the list.
            int index = root_.getDetailsIndex (object);
            if (index >= 0)
                tableModelEventSupport_.fireTableChanged (index, index,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        }

    }

}

