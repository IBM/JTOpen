///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListTablePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import com.ibm.as400.access.AS400;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Enumeration;



/**
The RecordListTablePane class represents a table that contains
the records and fields of a file using record-level access.

<p>The data in the table is retrieved from the system when
<i>load()</i> is called.  If <i>load()</i> is not called,
the table will be empty.

<p>Users must call <i>close()</i> to ensure that the system
resources are properly freed when this table is no longer needed.

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>The data in this table is not editable, the individual
cell values cannot be changed.

<p>RecordListTablePane objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>ListSelectionEvent
  <li>PropertyChangeEvent
</ul>

<pre>
 // Set up table for file contents.
AS400 system = new AS400("MySystem", "Userid", "Password");
String file = "/QSYS.LIB/QGPL.LIB/MyFile.FILE";
final RecordListTablePane pane = new RecordListTablePane(system, file);

 // Set up window to hold table
JFrame frame = new JFrame ("My Window");
WindowListener l = new WindowAdapter()
{
     // Close the model when window is closed.
    public void windowClosing(WindowEvent e)
    {
        pane.close();
    }
};
frame.addWindowListener(l);

// Set up the error dialog adapter.
pane.addErrorListener (new ErrorDialogAdapter (frame));

// Add the component and get data from system.
frame.getContentPane().add(pane);
pane.load();

 // Display the window
frame.setVisible(true)
</pre>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class RecordListTablePane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

// The variables and methods which have private commented out
// had to be made package scope since some JVMs (IE and AS400)
// does not allow inner class to access private items in their
// containing class.

/**
Constant indicating search type of equal.
**/
static public final int KEY_EQ = RecordListData.KEY_EQ;
/**
Constant indicating search type of greater than.
**/
static public final int KEY_GT = RecordListData.KEY_GT;
/**
Constant indicating search type of greater than or equal.
**/
static public final int KEY_GE = RecordListData.KEY_GE;
/**
Constant indicating search type of less than.
**/
static public final int KEY_LT = RecordListData.KEY_LT;
/**
Constant indicating search type of less than or equal.
**/
static public final int KEY_LE = RecordListData.KEY_LE;


// The table contained in this panel.
/*private*/ transient JTable table_; //@B0C - made transient
/*private*/ transient JScrollPane tablePane_; //@B0C - made transient
// The data model for the table.
/*private*/ RecordListTableModel model_;

//@B0 - need to save the table's state since it's transient now.
private Color tableColor_ = null; //@B0A
private boolean tableShowHorizontalLines_ = true; //@B0A
private boolean tableShowVerticalLines_ = true; //@B0A


// Event support.
transient private PropertyChangeSupport changeListeners_
    = new PropertyChangeSupport(this);
transient private VetoableChangeSupport vetoListeners_
    = new VetoableChangeSupport(this);
transient private ErrorEventSupport errorListeners_
     = new ErrorEventSupport(this);
transient private ListSelectionEventSupport selectionListeners_
     = new ListSelectionEventSupport(this);

// Adapter for listening for working events and enabling working cursor.
transient private WorkingCursorAdapter worker_
    = new WorkingCursorAdapter(this);

// Renderers for the different types of data, columns use these.
/*private*/ DBCellRenderer rightCell_ = new DBCellRenderer(SwingConstants.RIGHT);
/*private*/ DBCellRenderer leftCell_ = new DBCellRenderer(SwingConstants.LEFT);
// General types of data in columns.  Used to determine column renderer.
private static final int TYPE_CHAR = 1;
private static final int TYPE_HEX = 2;
private static final int TYPE_NUMBER = 3;


/**
Constructs a RecordListTablePane object.
This constructor sets the <i>keyed</i> property to false.
**/
public RecordListTablePane ()
{
    super();

    // Create table and model to hold data.
    model_ = new RecordListTableModel();

/* @B0M - moved code to initializeTransient()
    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setModel(model_);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Listen for events, pass them on to our listeners.
    model_.addPropertyChangeListener(changeListeners_);
    model_.addVetoableChangeListener(vetoListeners_);
    model_.addErrorListener(errorListeners_);
    model_.addWorkingListener(worker_);
    table_.getSelectionModel().addListSelectionListener(selectionListeners_);

    // Build GUI
    setLayout(new BorderLayout());
    tablePane_ = new JScrollPane (table_); // @A1C
    add("Center",tablePane_);
*/
    initializeTransient(); //@B0A
}



/**
Constructs a RecordListTablePane object.
This constructor sets the <i>keyed</i> property to false.

@param       system          The system where the file is located.
@param       fileName        The file name.
 The name is specified as a fully qualified path name in the library file system.
**/
public RecordListTablePane (AS400 system,
                             String fileName)
{
    super();

    // Create table and model to hold data.
    // note: model validates parms
    model_ = new RecordListTableModel(system, fileName);

/* @B0M - moved code to initializeTransient()
    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setModel(model_);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Listen for events, pass them on to our listeners.
    model_.addPropertyChangeListener(changeListeners_);
    model_.addVetoableChangeListener(vetoListeners_);
    model_.addErrorListener(errorListeners_);
    model_.addWorkingListener(worker_);
    table_.getSelectionModel().addListSelectionListener(selectionListeners_);

    // Build GUI
    setLayout(new BorderLayout());
    tablePane_ = new JScrollPane (table_); // @A1C
    add("Center",tablePane_);
*/
    initializeTransient(); //@B0A
}



/**
Constructs a RecordListTablePane object.
This constructor sets the <i>keyed</i> property to true.

@param       system          The system where the file is located.
@param       fileName        The keyed file name.
 The name is specified as a fully qualified path name in the library file system.
@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
@param      searchType      Constant indicating the type of match required.
**/
public RecordListTablePane (AS400 system,
                       String fileName,
                       Object[] key,
                       int searchType)
{
    super();

    // Create table and model to hold data.
    // note: model validates parms
    model_ = new RecordListTableModel(system, fileName, key, searchType);

/* @B0M - moved code to initializeTransient()
    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setModel(model_);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Listen for events, pass them on to our listeners.
    model_.addPropertyChangeListener(changeListeners_);
    model_.addVetoableChangeListener(vetoListeners_);
    model_.addErrorListener(errorListeners_);
    model_.addWorkingListener(worker_);
    table_.getSelectionModel().addListSelectionListener(selectionListeners_);

    // Build GUI
    setLayout(new BorderLayout());
    tablePane_ = new JScrollPane (table_); // @A1C
    add("Center",tablePane_);
*/
    initializeTransient(); //@B0A
}



/**
Adds a listener to be notified when an error occurs.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    errorListeners_.addErrorListener(listener);
}


/**
Adds a listener to be notified when the selection changes.

@param  listener  The listener.
**/
public void addListSelectionListener (ListSelectionListener listener)
{
    selectionListeners_.addListSelectionListener(listener);
}



/**
Adds a listener to be notified when the value of any bound
property is changed.

@param  listener  The listener.
**/
public void addPropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.addPropertyChangeListener(listener);
    super.addPropertyChangeListener(listener);
}


/**
Adds a listener to be notified when the value of any constrained
property is changed.

@param  listener  The listener.
**/
public void addVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.addVetoableChangeListener(listener);
    super.addVetoableChangeListener(listener);
}


/**
Closes the file this table represents.
**/
public void close()
{
    model_.close();
}



/**
Returns the column model for this table.

@return  The model for this table's columns.
**/
public TableColumnModel getColumnModel()
{
    return table_.getColumnModel();
}



/**
Returns the title of a column.  This is used for the table column heading.
If an error occurs, null is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return  The title of the column.
**/
public String getColumnTitle(int columnIndex)
{
    try
    {
        return (String)(table_.getColumnModel().getColumn(columnIndex).getHeaderValue());
    }
    catch (Exception e)
    {
        Trace.log(Trace.WARNING, "getColumnTitle() error:" + e);
        return null;
    }
}



/**
Returns the width of a column.
If an error occurs, 0 is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return  The width of the column.
**/
public int getColumnWidth(int columnIndex)
{
    try
    {
        return table_.getColumnModel().getColumn(columnIndex).getPreferredWidth(); //@B1C
    }
    catch (Exception e)
    {
        Trace.log(Trace.WARNING, "getColumnWidth() error:" + e);
        return 0;
    }
}



/**
Returns the data model for the table.

@return  The data model for the table.
**/
public RecordListTableModel getDataModel()
{
    return model_;
}


/**
Returns the file name.
The name is formatted as a fully qualified path name in the library file system.

@return The file name.
**/
public String getFileName ()
{
    return model_.getFileName();
}



/**
Returns the color used to draw grid lines.

@return The color used to draw grid lines.
**/
public Color getGridColor()
{
//@B0D    return table_.getGridColor();
  return tableColor_; //@B0A
}



/**
Returns the key.
The key is only used if the <i>keyed</i> property is true.

@return The key.
**/
public Object[] getKey ()
{
    return model_.getKey();
}


/**
Returns whether the file will be accessed in key or sequential order.

@return  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
**/
public boolean getKeyed ()
{
    return model_.getKeyed();
}



/**
Returns the search type.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.

@return The search type.
**/
public int getSearchType ()
{
    return model_.getSearchType();
}



/**
Returns the ListSelectionModel that is used to maintain row selection state.

@return  The model that provides row selection state.
**/
public ListSelectionModel getSelectionModel()
{
    return table_.getSelectionModel();
}



/**
Returns whether horizontal lines are drawn between rows.
@return true if horizontal lines are to be drawn; false otherwise.
**/
public boolean getShowHorizontalLines()
{
//@B0D    return table_.getShowHorizontalLines();
  return tableShowHorizontalLines_; //@B0A
}



/**
Returns whether vertical lines are drawn between columns.
@return true if vertical lines are to be drawn; false otherwise.
**/
public boolean getShowVerticalLines()
{
//@B0D    return table_.getShowVerticalLines();
  return tableShowVerticalLines_; //@B0A
}



/**
Returns the system where the file is located.

@return The system where the file is located.
**/
public AS400 getSystem ()
{
    return model_.getSystem();
}



/**
Returns the string value at the specifed row and column.
Indices start at 0.
If an error occurs, null is returned.

@param  rowIndex            The row index.
@param  columnIndex         The column index.

@return The value at the specified row and column as a string.
**/
// Note that this method is dependent on the cell renderer of a column
// being a JLabel.
public String getStringValueAt (int rowIndex,
                          int columnIndex)
{
    // Try to catch row index out of range.
    if (rowIndex >= model_.getRowCount() )
    {
        Trace.log(Trace.WARNING, "getStringValueAt() column out of range");
        return null;
    }

    try
    {
        TableColumnModel cmodel = getColumnModel();
        Component cellComp = cmodel.getColumn(columnIndex)
            .getCellRenderer()
            .getTableCellRendererComponent(table_,
                getValueAt(rowIndex,columnIndex),
                false,
                false,
                rowIndex,
                columnIndex);
        if (cellComp instanceof JLabel)
            return ((JLabel)cellComp).getText();
        else
            return null;
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "getStringValueAt() error:" + e);
        return null;
    }
}



/**
Returns the value at the specifed row and column.
Indices start at 0.
If an error occurs, null is returned.

@param  rowIndex            The row index.
@param  columnIndex         The column index.

@return The value at the specified row and column.
**/
public Object getValueAt (int rowIndex,
                          int columnIndex)
{
    try
    {
        // must change the table column index to the
        // model index
        return model_.getValueAt(rowIndex,
           getColumnModel().getColumn(columnIndex).getModelIndex());
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "getStringValueAt() error:" + e);
        return null;
    }
}


//@B0A
/**
Initializes the transient data.
**/
private void initializeTransient()
{

    //@B0M - moved this code out of constructors

    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setModel(model_);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    if (tableColor_ == null) tableColor_ = table_.getGridColor(); //@B1A
    
    // Listen for events, pass them on to our listeners.
    model_.addPropertyChangeListener(changeListeners_);
    model_.addVetoableChangeListener(vetoListeners_);
    model_.addErrorListener(errorListeners_);
    model_.addWorkingListener(worker_);
    table_.getSelectionModel().addListSelectionListener(selectionListeners_);

    setLayout(new BorderLayout());
    tablePane_ = new JScrollPane (table_); // @A1C
    add("Center",tablePane_);
    
    //@B0A
    // We add a fake FocusListener whose real purpose is to uninstall
    // the UI early so the JTable that is part of our UI does not try
    // to get serialized.
    // See also: source code for javax.swing.JComponent in JDK 1.2.
    addFocusListener(new SerializationListener(this)); //@B0A
    addFocusListener(new SerializationListener(model_, table_)); //@B0A

}


/**
Loads the table based on the state of the system.
The <i>fileName</i> and <i>system</i> properties
must be set before this method is called.
The table heading is reconstructed to ensure it matches
the data, so any column customization will be lost.
**/
public void load()
{
    // refresh the result set data
    // note: model handles error conditions
    model_.load();
    refreshHeadings();
}



/**
Moves the column and heading at columnIndex to newIndex.
The old column at columnIndex will now be found at newIndex,
the column that used to be at newIndex is shifted left or right
to make room.
Indices start at 0.

@param columnIndex The index of column to be moved.
@param newIndex  The new index to move the column to.
**/
public void moveColumn(int columnIndex,
                       int newIndex)
{
    // Catch errors if index being out of range.
    try
    {
        table_.getColumnModel().moveColumn(columnIndex,newIndex);
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "moveColumn() error:" + e);
    }
}


/**
Restore the state of this object from an object input stream.
It is used when deserializing an object.
@param in The input stream of the object being deserialized.
@throws IOException
@throws ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
{
    // Restore the non-static and non-transient fields.
    in.defaultReadObject();

    // Initialize the transient fields.
    changeListeners_ = new PropertyChangeSupport(this);
    model_.addPropertyChangeListener(changeListeners_);
    vetoListeners_ = new VetoableChangeSupport(this);
    model_.addVetoableChangeListener(vetoListeners_);
    errorListeners_ = new ErrorEventSupport(this);
    model_.addErrorListener(errorListeners_);
    worker_ = new WorkingCursorAdapter(this);
    model_.addWorkingListener(worker_);
    selectionListeners_ = new ListSelectionEventSupport(this);
    
    initializeTransient(); //@B0A
    //@B0D table_.getSelectionModel().addListSelectionListener(selectionListeners_);
    

    //@B0 - table_ is now transient, so we need to reset its attributes.
    table_.setGridColor(tableColor_);    //@B0A
    table_.setShowHorizontalLines(tableShowHorizontalLines_); //@B0A
    table_.setShowVerticalLines(tableShowVerticalLines_); //@B0A

}



/**
Updates the table header to match the data.
Any column customization will be lost.
**/
public void refreshHeadings()
{
  Runnable refreshHeading = new Runnable()
  {
    public void run()
    {


    // Remove all columns.
    // First copy enumereration, then delete each column.
    TableColumnModel model = table_.getColumnModel();
    int oldColumnCount = model.getColumnCount();
    TableColumn oldColumns[] = new TableColumn[oldColumnCount];
    Enumeration e = model.getColumns();
    for (int i=0; e.hasMoreElements() ; ++i)
    {
        oldColumns[i] = (TableColumn)e.nextElement();
    }
    for (int i=0; i<oldColumnCount; ++i)
    {
         model.removeColumn(oldColumns[i]);
    }

    // set up columns to match data
    int numColumns = model_.getColumnCount();

    // Type of data in column.
    int type;
    Class rlaType;
    Class byteClass = (new byte[0]).getClass();

    // Get size of font.  Note if this method is called too early,
    // table_.getFont()==null, and column widths won't be adjusted.
    int size = 0;
    if (table_.getFont() != null)
        // Note: 'M' is just used to get a reasonable width of a
        // large character in this font.
        size = table_.getFontMetrics(table_.getFont()).charWidth('M');
    int colSize, colDataSize, colTitleSize;

    // set up columns
    for (int i=0; i<numColumns; ++i)
    {
        TableColumn col = new TableColumn(i);
        col.setIdentifier(model_.getColumnID(i));

        rlaType = model_.getColumnClass(i);
        if (rlaType == Integer.class ||
            rlaType == Short.class ||
            rlaType == Float.class ||
            rlaType == BigDecimal.class ||
            rlaType == Double.class)
            type = TYPE_NUMBER;
        else if (rlaType == byteClass)
            type = TYPE_HEX;
        else
            // text, DBCSxxx, time/date/timestamp
            type = TYPE_CHAR;

        String title = model_.getColumnName(i);
        if (type == TYPE_NUMBER)
        {
            col.setCellRenderer(rightCell_);
            col.setHeaderRenderer(new VObjectHeaderRenderer(
                title,SwingConstants.RIGHT));
        }
        else    // character or hex value
        {
            col.setCellRenderer(leftCell_);
            col.setHeaderRenderer(new VObjectHeaderRenderer(
                title,SwingConstants.LEFT));
        }

        // Adjust width if font size is available.
        if (size != 0)
        {
            colDataSize = model_.getColumnWidth(i);
            if (type == TYPE_HEX)
                // displayed in hex, need more room
                colDataSize = colDataSize*2;
            colTitleSize = title.length();
            colSize = colDataSize>colTitleSize?colDataSize:colTitleSize;
            // add 10 to account for the empty border in the cells
            col.setPreferredWidth(colSize*size+10); //@B1C
        }

        table_.addColumn(col);
    }

    // Redo the panel.  This is needed in the case where there was
    // no data previously, since no header would have been created
    // for the table.  Only done if no previous columns, and now
    // there are columns.
    if (oldColumnCount == 0 && numColumns > 0)
    {
        if (tablePane_ != null)
            remove(tablePane_);
        tablePane_ = new JScrollPane (table_); // @A1C
        add("Center",tablePane_);
    }

    // Refresh the pane.
    validate();


    }
  };

  // Try and refresh the heading in the event dispatcher thread.
  // This is done because doing it inline seems to cause hangs,
  // and Swing documentation seems to suggest doing all GUI
  // work in the event dispatching thread.
  try
  {
    SwingUtilities.invokeAndWait(refreshHeading);
  }
  catch(Error e)
  {
    // Error received.  Assume that the error was because we are
    // already in the event dispatching thread.  Do work in the
    // current thread.
    Trace.log(Trace.DIAGNOSTIC, "invokeAndWait error:" + e);
    refreshHeading.run();
  }
  catch(Exception e)
  {
    Trace.log(Trace.ERROR, "invokeAndWait exception:" + e);
  }
}



/**
Removes a column from the table.

@param columnIndex The index of column.  Indices start at 0.
**/
public void removeColumn(int columnIndex)
{
    // Catch errors if the index being out of range.
    try
    {
        table_.getColumnModel().removeColumn
          (table_.getColumnModel().getColumn(columnIndex));
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "removeColumn() error:" + e);
    }
}



/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    errorListeners_.removeErrorListener(listener);
}



/**
Removes a listener from being notified when the selection changes.

@param  listener  The listener.
**/
public void removeListSelectionListener (ListSelectionListener listener)
{
    selectionListeners_.removeListSelectionListener(listener);
}



/**
Removes a listener from being notified when the value of any bound
property is changed.

@param  listener  The listener.
**/
public void removePropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.removePropertyChangeListener(listener);
    super.removePropertyChangeListener(listener);
}



/**
Removes a listener from being notified when the value of any constrained
property is changed.

@param  listener  The listener.
**/
public void removeVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.removeVetoableChangeListener(listener);
    super.removeVetoableChangeListener(listener);
}



/**
Sets the title of a column.  This is used for the table column heading.

@param columnIndex The index of column.  Indices start at 0.
@param title       The title of the column.
**/
public void setColumnTitle(int columnIndex,
                           String title)
{
    // Catch errors if the index being out of range.
    try
    {
        table_.getColumnModel().getColumn(columnIndex).setHeaderValue(title);
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "setColumnTitle() error:" + e);
    }
    // Swing doesn't repaint without a little prodding.
    validate();
}



/**
Sets the width of a column.

@param columnIndex The index of column.  Indices start at 0.
@param width       The column width.
**/
public void setColumnWidth(int columnIndex,
                           int width)
{
    // Catch errors if the index being out of range.
    try
    {
        table_.getColumnModel().getColumn(columnIndex).setPreferredWidth(width); //@B1C
    }
    catch(Exception e)
    {
        Trace.log(Trace.WARNING, "setColumnWidth() error:" + e);
    }
}



/**
Sets the name of the file.
This property is bound and constrained.
Note that the data in the table will not change until a
<i>load()</i> is done.

@param       fileName                The file name.
 The name is specified as a fully qualified path name in the library file system.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setFileName (String fileName)
    throws PropertyVetoException
{
    // Note: the model handles the binding and constraining.
    // note: model validates parms
    model_.setFileName(fileName);
}



/**
Sets the color used to draw grid lines.

@param color The color used to draw the grid lines.
**/
public void setGridColor(Color color)
{
    table_.setGridColor(color);
    tableColor_ = color; //@B0A
}



/**
Sets the key.
This property is bound and constrained.
Note that the data in the table will not change until a
<i>load()</i> is done.
The key is only used if the <i>keyed</i> property is true.

@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setKey (Object[] key)
    throws PropertyVetoException
{
    // Note: the model handles the binding and constraining.
    model_.setKey(key);
}


/**
Sets whether the file will be accessed in key or sequential order.
This property is bound and constrained.
Note that the data in the table will not change
until a <i>load()</i> is done.

@param keyed  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setKeyed (boolean keyed)
    throws PropertyVetoException
{
    // Note: the model handles the binding and constraining.
    model_.setKeyed(keyed);
}



/**
Sets the search type.
This property is bound and constrained.
Note that the data in the table will not change until a
<i>load()</i> is done.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.

@param      searchType      Constant indicating the type of match required.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setSearchType (int searchType)
    throws PropertyVetoException
{
    // Note: the model handles the binding and constraining.
    model_.setSearchType(searchType);
}



/**
Sets the ListSelectionModel that is used to maintain row selection state.

@param  model The model that provides the row selection state.
**/
public void setSelectionModel(ListSelectionModel model)
{
    // cleanup old listener
    table_.getSelectionModel().removeListSelectionListener(selectionListeners_);
    // make change
    table_.setSelectionModel(model);
    // listen to new model
    table_.getSelectionModel().addListSelectionListener(selectionListeners_);
}



/**
Sets whether horizontal lines are drawn between rows.
@param show true if horizontal lines are to be drawn; false otherwise.
**/
public void setShowHorizontalLines(boolean show)
{
    table_.setShowHorizontalLines(show);
    tableShowHorizontalLines_ = show; //@B0A
}



/**
Sets whether vertical lines are drawn between columns.
@param show true if vertical lines are to be drawn; false otherwise.
**/
public void setShowVerticalLines(boolean show)
{
    table_.setShowVerticalLines(show);
    tableShowVerticalLines_ = show; //@B0A
}



/**
Sets the system where the file is located.
This property is bound and constrained.
Note that the data in the table will not change until a
<i>load()</i> is done.

@param       system                  The system where the file is located.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setSystem (AS400 system)
    throws PropertyVetoException
{
    // Note: the model handles the binding and constraining.
    // note: model validates parms
    model_.setSystem(system);
}



}
