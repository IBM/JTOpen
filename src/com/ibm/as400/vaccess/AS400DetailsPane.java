///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400DetailsPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.CellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;



/**
The AS400DetailsPane class represents a graphical user interface
that displays the details regarding the contents of an AS/400
resource, known as the root.  You must explicitly call load() to
load the information from the AS/400.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400DetailsPane objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListSelectionEvent
    <li>PropertyChangeEvent
</ul>

<p>The following example creates a details pane.

<pre>
// Set up the details pane.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VPrinterOutput printerOutput = new VPrinterOutput (system);
AS400DetailsPane detailsPane = new AS400DetailsPane (printerOutput);
detailsPane.load ();
<br>
// Add the details pane to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add (detailsPane);
</pre>

@see AS400DetailsModel
**/
//
// Implementation notes:
//
// 1.  I want to provide different views of the model (e.g.
//     large icons, small icons, list, table).  The view could
//     be a property of this object, and could be implemented
//     using an AS400DetailsView (e.g.) interface and many
//     implementations.
//
//     The list and table views would be easy.  However the icon
//     views would be too hard, since I would have to implement
//     selection mechanisms for those.
//
// 2.  Up to and including Swing 0.7, I was able to add function so
//     that clicking on the column headings would sort the rows
//     by that column.  Clicking again would sort in the reverse
//     order.  This was all triggered by a column selection.
//
//     Swing 1.0 changed the column selection mechanism so that
//     column selections happen all the time (except when clicking
//     on the column heading).
//
//     The solution would be to listen to the buttons in the column
//     headings to trigger this, but there was not enough time
//     to do this rework.  Given that there are still public sort()
//     methods all over the place, the caller could possibly do
//     this.
//
public class AS400DetailsPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    boolean             allowActions_               = true; // Private.
    boolean             confirm_                    = true; // Private.
    transient JTable    table_                      = null; // Private. @B0C
    AS400DetailsModel   model_                      = null; // Private.



    // Private data.
    transient private VActionContext        actionContext_;
    transient private DoubleClickAdapter    doubleClickAdapter_;
    transient private PopupMenuAdapter      popupMenuAdapter_;



    // Event support.
    transient private ErrorEventSupport               errorEventSupport_ ;
    transient private ListSelectionEventSupport       listSelectionEventSupport_;
    transient private PropertyChangeSupport           propertyChangeSupport_;
    transient private VetoableChangeSupport           vetoableChangeSupport_;



/**
Constructs an AS400DetailsPane object.
**/
    public AS400DetailsPane ()
    {
        // Initialize the model.
        model_ = new AS400DetailsModel ();

        initializeTransient (); //@B0M - do this first to initialize table_
        
        // Layout the pane.
        setLayout (new BorderLayout ());
        JScrollPane scrollPane = new JScrollPane (table_); // @A1C
        add ("Center", scrollPane);

    }



/**
Constructs an AS400DetailsPane object.

@param  root  The root, or the AS/400 resource, from which all information for the model is gathered.
**/
    public AS400DetailsPane (VNode root)
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

@return true if a actions can be invoked; false otherwise.
**/
    public boolean getAllowActions ()
    {
        return allowActions_;
    }



/**
Returns the column model that is used to maintain the columns.
This provides the ability to programmatically add and remove
columns.

@return The column model.
**/
    public TableColumnModel getColumnModel ()
    {
        return table_.getColumnModel ();
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
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
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
Returns the root, or the AS/400 resource, from which all information for the model is gathered.

@return     The parent root, or null if none.
**/
    public VNode getRoot ()
    {
        return model_.getRoot ();
    }


/**
Returns the indicator for whether the rows can be selected.

@return true if rows can be selected; false otherwise.
**/
    public boolean getRowSelectionAllowed ()       // @A2
    {
        return table_.getRowSelectionAllowed();
    }


/**
Returns the first selected object.

@return The first selected object, or null if none are
        selected.
**/
    public VObject getSelectedObject ()
    {
        VObject selectedObject = null;
        int[] selectedRows = table_.getSelectedRows ();
        if (selectedRows.length > 0)
            selectedObject = model_.getObjectAt (selectedRows[0]);
        return selectedObject;
    }



/**
Returns the objects which are represented by the selected rows.

@return  The objects which are represented by the selected rows.
**/
    public VObject[] getSelectedObjects ()
    {
        int[] selectedRows = table_.getSelectedRows ();
        VObject[] selectedObjects = new VObject[selectedRows.length];
        for (int i = 0; i < selectedRows.length; ++i)
            selectedObjects[i] = model_.getObjectAt (selectedRows[i]);
        return selectedObjects;
    }



/**
Returns the selection model that is used to maintain row
selection state.  This provides the ability to programmatically
select and deselect objects.

@return The selection model.
**/
    public ListSelectionModel getSelectionModel ()
    {
        return table_.getSelectionModel ();
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        addFocusListener(new SerializationListener(this)); //@B0A
        
        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport (this);
        listSelectionEventSupport_  = new ListSelectionEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);
        vetoableChangeSupport_      = new VetoableChangeSupport (this);

        // Initialize the table. @B0M - moved table initialization out of ctor.
        table_ = new JTable (model_);
        table_.setAutoCreateColumnsFromModel (false);
        table_.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
        table_.setColumnSelectionAllowed (false);
        table_.setRowSelectionAllowed (true);
        table_.setShowGrid (false);

        model_.addErrorListener (errorEventSupport_);
        model_.addPropertyChangeListener (propertyChangeSupport_);
        model_.addVetoableChangeListener (vetoableChangeSupport_);
        table_.getSelectionModel ().addListSelectionListener (listSelectionEventSupport_);

        // Initialize the action context.
        actionContext_ = new VActionContext_ ();

        // Initialize the other adapters.
        model_.addPropertyChangeListener (new ColumnAdapter_ ());
        model_.addWorkingListener (new WorkingCursorAdapter (table_));

        VPane_ pane = new VPane_ ();
        doubleClickAdapter_ = new DoubleClickAdapter (pane, actionContext_);
        popupMenuAdapter_   = new PopupMenuAdapter (pane, actionContext_);

        if (allowActions_) {
            table_.addMouseListener (popupMenuAdapter_);
            table_.addMouseListener (doubleClickAdapter_);
        }
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

        int[] selectedRows = table_.getSelectedRows ();
        for (int i = 0; i < selectedRows.length; ++i)
            if (model_.getObjectAt (selectedRows[i]).equals (object))
                return true;
        return false;
    }



/**
Loads the information from the AS/400.
**/
    public void load ()
    {
        table_.clearSelection ();
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
  <li>double clicking on a object sets the root object
</ul>

<p>The default is true.

@param allowActions true if actions are allowed; false otherwise.
**/
    public void setAllowActions (boolean allowActions)
    {
        if (allowActions_ != allowActions) {

            allowActions_ = allowActions;

            // Set up adapters only when actions are allowed.
            if (allowActions_) {
                table_.addMouseListener (popupMenuAdapter_);
                table_.addMouseListener (doubleClickAdapter_);
            }
            else {
                table_.removeMouseListener (popupMenuAdapter_);
                table_.removeMouseListener (doubleClickAdapter_);
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
Sets the root, or the AS/400 resource, from which all information for the model is gathered.

@param  root   The root, or the AS/400 resource, from which all information for the model is gathered.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setRoot (VNode root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        table_.clearSelection ();
        model_.setRoot (root);
    }


/**
Sets the indicator for whether the rows can be selected.

@param  selectable  The value indicating if the rows can be selected.
**/
    public void setRowSelectionAllowed (boolean selectable)   // @A2
    {
       table_.setRowSelectionAllowed(selectable);
       
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

/**
Sets the column widths.
**/
    void sizeColumns () // Private.
    {
        // Get the font size.  Use M as a good sample character.
        int fontSize = 0;
        if (table_.getFont () != null)
            fontSize = table_.getFontMetrics (table_.getFont ()).charWidth ('M');

        if (fontSize > 0) {

            // Iterate through the columns.
            TableColumnModel columnModel = table_.getColumnModel ();
            int columnCount = columnModel.getColumnCount ();
            for (int i = 0; i < columnCount; ++i) {
                TableColumn column = columnModel.getColumn (i);
                if (column instanceof VTableColumn) {
                    VTableColumn vcolumn = (VTableColumn) column;
                    //@B0 - In Swing 1.1, should use setPreferredWidth instead of setWidth. (according to Swing javadoc)
                    column.setPreferredWidth (vcolumn.getPreferredCharWidth () * fontSize + 10); //@B0C
                }
            }
        }
    }



/**
Sorts the contents.The propertyIdentifer[0], orders[0] combination  is used to do the sort. If the values are equal, propertyIdentifier[1], orders[1]  is used to break the tie, and so forth.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
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
The ColumnAdapter_ class listens for change in the
root object of a details model.  When such a change occurs, this
class updates the columns of the table as needed.
**/
    private class ColumnAdapter_
    implements PropertyChangeListener
    {
        public void propertyChange (PropertyChangeEvent event)
        {
            // If the root object changed in the model,
            // then we need to recreate the table.
            if (event.getPropertyName () == "root") {

                // Remove the existing columns.  Note that we
                // must make our own list because the enumeration
                // gets wrecked when we remove an element.
                TableColumnModel columnModel = table_.getColumnModel ();
                Vector oldColumns = new Vector ();
                for (Enumeration e = columnModel.getColumns (); e.hasMoreElements (); )
                    oldColumns.addElement (e.nextElement ());

                for (Enumeration e = oldColumns.elements (); e.hasMoreElements (); ) {
                    TableColumn column = (TableColumn) e.nextElement ();
                    columnModel.removeColumn (column);
                }

                // Add the new table columns.
                TableColumnModel columnModel2 = model_.getRoot ().getDetailsColumnModel ();
                if (columnModel2 != null) {
                    for (Enumeration e = columnModel2.getColumns (); e.hasMoreElements (); ) {
                        TableColumn column = (TableColumn) e.nextElement ();
                        columnModel.addColumn (column);
                    }
                }

                sizeColumns ();
            }
        }

        private String getCopyright ()             { return Copyright_v.copyright; }
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
            return VUtilities.getFrame (AS400DetailsPane.this);
        }

        public CellEditor startEditing (VObject object, Object propertyIdentifier)
        {
            // Validate the parameters.
            if (object == null)
                throw new NullPointerException ("object");
            if (propertyIdentifier == null)
                throw new NullPointerException ("propertyIdentifier");

            if (allowActions_ == false)
                return null;

            int rowIndex = model_.getRoot ().getDetailsIndex (object);
            if (rowIndex < 0)
                return null;

            TableColumn column = null;
            try {
                column = table_.getColumn (propertyIdentifier);
            }
            catch (IllegalArgumentException e) {
                // The property is not valid.
                return null;
            }

            // Edit.
            int columnIndex = column.getModelIndex ();
            table_.editCellAt (rowIndex, columnIndex);
            return column.getCellEditor();
        }
    };



/**
Implements the VPane interface.
**/
    private class VPane_
    implements VPane, Serializable
    {
        private String getCopyright ()
        {
            return Copyright_v.copyright;
        }

        public VNode getRoot ()
        {
            return AS400DetailsPane.this.getRoot ();
        }

        public VObject getObjectAt (Point point)
        {
            VObject object = null;
            int row = table_.rowAtPoint (point);
            if (row != -1)
                object = model_.getObjectAt (row);
            return object;
        }

        public void setRoot (VNode root)
            throws PropertyVetoException
        {
            AS400DetailsPane.this.setRoot (root);
        }
    };



}
