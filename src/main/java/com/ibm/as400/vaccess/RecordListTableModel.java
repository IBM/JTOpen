///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListTableModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelEvent;
import com.ibm.as400.access.AS400;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;



/**
The RecordListTableModel class represents a table that contains
the records of a file using record-level access.
This model can be used to create a table of the results.

<p>This class should be used by users who wish to change the default
interface for the table.  When the default look and behavior is
sufficient, RecordListTablePane can be used.

<p>Users must call <i>close()</i> to ensure that the system
resources are properly freed when this model is no longer needed.

<p>The data in the model is retrieved from the system when
<i>load()</i> is called.  If <i>load()</i> is not called,
the model will contain no data.
Not all data is retrieved at once, rather data is retrieved as needed
(in chunks), to improve performance.

<p>The data in this model is not editable.  The individual
cell values cannot be changed.

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>RecordListTableModel objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>PropertyChangeEvent
  <li>TableModelEvent
  <li>WorkingEvent
</ul>

<pre>
 // Set up table for file contents.
AS400 system = new AS400("MySystem", "Userid", "Password");
String file = "/QSYS.LIB/QGPL.LIB/MyFile.FILE";
final RecordListTableModel model = new RecordListTableModel(system, file);

 // Set up window to hold table
JFrame frame = new JFrame ("My Window");
WindowListener l = new WindowAdapter()
{
     // Close the model when window is closed.
    public void windowClosing(WindowEvent e)
    {
        model.close();
    }
};
frame.addWindowListener(l);

// Set up the error dialog adapter.
model.addErrorListener (new ErrorDialogAdapter (frame));

// Add the component and get data from system.
model.load();
JTable table = new JTable(model);
frame.getContentPane().add(new JScrollPane(table));

 // Display the window
frame.setVisible(true)
</pre>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
// Note that this class throws error and working events from within
// synchronized blocks,
// which could cause hangs if the handlers of these events do
// operations from a different thread an attempt to access another
// synchronized piece of code.
// At this time this seems to be an acceptable risk, since the
// events thrown are not likely to need enough processing to
// require another thread, and getting having the events thrown
// from outside a sychronized block would be nearly impossible.
// The other option is to have the firing of the events be done
// from another thread, but the overhead of creating another thread
// not only takes resources, but also delays the delivery of the event.
public class RecordListTableModel
extends AbstractTableModel
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables which have private commented out had to made
// package scope since currently Internet Explorer does not
// allow inner class to access private variables in their
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


//The file from which records are being displayed.
private RecordListData   tableData_ = new RecordListData();

// Column information
transient private int numColumns_ = 0;

// Row information
// Number of records processed (put in local cache) at a time.
private static final int READ_INCREMENT_ = 20;
// Number of rows in the table.
// This value is only accurate if tableData_.getAllRecordsProcessed()==true.
// After load() it is 1.
// Otherwise it is data_.getLastRecordProcessed()+1.
// It is never greater than the number of actual rows, but it can be less.
transient private int numRows_ = 0;


// Event support.
transient private PropertyChangeSupport changeListeners_
    = new PropertyChangeSupport (this);
transient private VetoableChangeSupport vetoListeners_
    = new VetoableChangeSupport (this);
transient /*private*/ ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);
transient private WorkingEventSupport workListeners_
    = new WorkingEventSupport (this);

// Private listener for if there errors occurred in tableData_.
transient private ErrorListener_ errorListener_
    = new ErrorListener_();

// Flag for if an error event was sent.
transient /*private*/ boolean error_;




/**
Constructs a RecordListTableModel object.
This constructor sets the <i>keyed</i> property to false.
**/
public RecordListTableModel ()
{
    super();

    // Add self as listener for errors and work events
    tableData_.addErrorListener(errorListener_);
    tableData_.addWorkingListener (workListeners_);
}



/**
Constructs a RecordListTableModel object.
This constructor sets the <i>keyed</i> property to false.

@param       system          The system where the file is located.
@param       fileName        The file name.
 The name is specified as a fully qualified path name in the library file system.
**/
public RecordListTableModel (AS400 system,
                                 String fileName)
{
    super();
    if (fileName == null)
        throw new NullPointerException("fileName");
    if (system == null)
        throw new NullPointerException("system");

    tableData_.setFileName(fileName);
    tableData_.setSystem(system);

    // Add self as listener for errors and work events
    tableData_.addErrorListener(errorListener_);
    tableData_.addWorkingListener (workListeners_);
}



/**
Constructs a RecordListTableModel object.
This constructor sets the <i>keyed</i> property to true.

@param       system          The system where the file is located.
@param       fileName        The keyed file name.
 The name is specified as a fully qualified path name in the library file system.
@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
@param      searchType      Constant indicating the type of match required.
**/
public RecordListTableModel (AS400 system,
                       String fileName,
                       Object[] key,
                       int searchType)
{
    super();
    if (fileName == null)
        throw new NullPointerException("fileName");
    if (system == null)
        throw new NullPointerException("system");

    tableData_.setFileName(fileName);
    tableData_.setSystem(system);
    tableData_.setKeyed(true);
    tableData_.setKey(key);
    tableData_.setSearchType(searchType);

    // Add self as listener for errors and work events
    tableData_.addErrorListener(errorListener_);
    tableData_.addWorkingListener (workListeners_);
}



/**
Adds a listener to be notified when an error occurs.
The listener's <i>errorOccurred()</i> method will be called.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    errorListeners_.addErrorListener (listener);
}



/**
Adds a listener to be notified when the value of any bound
property is changed.
The listener's <i>propertyChange()</i> method will be called.

@param  listener  The listener.
**/
public void addPropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.addPropertyChangeListener (listener);
}



/**
Adds a listener to be notified when the value of any constrained
property is changed.
The listener's <i>vetoableChange()</i> method will be called.

@param  listener  The listener.
**/
public void addVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.addVetoableChangeListener (listener);
}



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void addWorkingListener (WorkingListener listener)
{
    workListeners_.addWorkingListener (listener);
}


/**
Closes the file this model represents.
**/
public void close ()
{
    tableData_.close();
}


/**
Returns the class of the values in the column.
If an error occurs, null is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return The class of the column values.
**/
/*@D0D synchronized */ public Class getColumnClass(int columnIndex)
{
    if (columnIndex >= numColumns_)
    {
        Trace.log(Trace.WARNING, "getColumnClass() error - index out of bounds.");
        return null;
    }

    // Try to get the value of a column.
    Object value = null;
    for (int i=0; i < getRowCount() && value == null; ++i)
    {
        value = getValueAt(i, columnIndex);
    }

    if (value != null)  // error getting value or all values null
        return value.getClass();
    else
    {
        try
        {
            return Class.forName("java.lang.Object");
        }
        catch(ClassNotFoundException e)
        {
            return null;
        }
    }
}


/**
Returns the number of columns in the table.

@return The number of columns in the table.
**/
/*@D0D synchronized */ public int getColumnCount()
{
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn()) //@D0A
      Trace.log(Trace.DIAGNOSTIC, "getColumnCount:", numColumns_);
    return numColumns_;
}


/**
Returns the identifier of the column.  This is the field name in
the database.
If an error occurs, null is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return The column identifier.
**/
/*@D0D synchronized */ public String getColumnID(int columnIndex)
{
    if (columnIndex >= numColumns_)
    {
        Trace.log(Trace.WARNING, "getColumnID() error - index out of bounds.");
        return null;
    }
    return tableData_.getColumnName(columnIndex);
}


/**
Returns the name of the column for use in a table heading.
If an error occurs, null is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return The column name.
**/
/*@D0D synchronized */ public String getColumnName(int columnIndex)
{
    if (columnIndex >= numColumns_)
    {
        Trace.log(Trace.WARNING, "getColumnName() error - index out of bounds.");
        return null;
    }
    return tableData_.getColumnLabel(columnIndex);
}



/**
Returns the width of a column as a character count.
If an error occurs, 0 is returned.

@param columnIndex The index of the column.  Indices start at 0.

@return The width of the column expressed as a character count.
**/
/*@D0D synchronized */ public int getColumnWidth(int columnIndex)
{
    if (columnIndex >= numColumns_)
    {
        Trace.log(Trace.WARNING, "getColumnWidth() error - index out of bounds.");
        return 0;
    }
    return tableData_.getColumnDisplaySize(columnIndex);
}


/**
Returns the file name.
The name is formatted as a fully qualified path name in the library file system.

@return The file name.
**/
public String getFileName ()
{
    String result = tableData_.getFileName();
    if (result == null)
        return "";
    return result;
}



/**
Returns the key.
The key is only used if the <i>keyed</i> property is true.

@return The key.
**/
public Object[] getKey ()
{
    return tableData_.getKey();
}


/**
Returns whether the file will be accessed in key or sequential order.

@return  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
**/
public boolean getKeyed ()
{
    return tableData_.getKeyed();
}



/**
Returns the number of rows in the table.
Because of incremental data retrieval, this value may
not be accurate.

@return The number of rows in the table.
**/
/*@D0D synchronized */ public int getRowCount()
{
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn()) //@D0A
      Trace.log(Trace.DIAGNOSTIC, "getRowCount:", numRows_);
    return numRows_;
}



/**
Returns the search type.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.

@return The search type.
**/
public int getSearchType ()
{
    return tableData_.getSearchType();
}



/**
Returns the system where the file is located.

@return The system where the file is located.
**/
public AS400 getSystem ()
{
    return tableData_.getSystem();
}


/**
Returns the value at the specifed column and row.
If an error occurs, null is returned.

@param  rowIndex            The row index.  Values start at 0.
@param  columnIndex    The column index.  Values start at 0.

@return The value at the specified column and row.
**/
public Object getValueAt (int rowIndex,
                          int columnIndex)
{
    TableModelEvent event = null;
    error_ = false;  // set to true if we should return null

    synchronized(this)
    {

        Trace.log(Trace.DIAGNOSTIC, "getValueAt[" +rowIndex+ "][" +columnIndex+ "]");
        if (tableData_ == null)
        {
            Trace.log(Trace.WARNING, "getValueAt() error - no load done.");
            return null;
        }
        if (columnIndex >= numColumns_)
        {
            Trace.log(Trace.WARNING, "getValueAt() error - column index out of range.");
            return null;
        }

        // If all records read, verify the row is in range.
        if (tableData_.getAllRecordsProcessed() &&
            rowIndex >= tableData_.getNumberOfRows())
        {
            Trace.log(Trace.WARNING, "getValueAt() error - row out of range.");
            return null;
        }

        // If we haven't yet transferred this row from the
        // result set to the table cache, do so.
        error_ = false;
        if (tableData_.getLastRecordProcessed() < rowIndex)
        {
            if (rowIndex - tableData_.getLastRecordProcessed() > READ_INCREMENT_)
                tableData_.readMoreRecords(rowIndex - tableData_.getLastRecordProcessed());
            else
                tableData_.readMoreRecords(READ_INCREMENT_);

            int oldnum = numRows_;

            if (tableData_.getAllRecordsProcessed())
            {
                numRows_ = tableData_.getLastRecordProcessed()+1;  // numRows is 1-based
                Trace.log(Trace.INFORMATION, "All rows read, number of rows:", numRows_);
                // Verify the row is in range.
                if (rowIndex >= tableData_.getNumberOfRows())
                {
                    Trace.log(Trace.WARNING, "getValueAt() error - row out of range(2).");
                    error_ = true;
                }
            }
            else
                // Add 2 - numRows_ is 1-based, plus there are more records.
                numRows_ = tableData_.getLastRecordProcessed()+2;

            // Notify listeners that we've changed.
            event = new TableModelEvent(this, oldnum, numRows_-1,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        }
    } // end synchronized block

    if (event != null)
    {
        fireTableChanged(event);
        Trace.log(Trace.INFORMATION, "Changed number of rows to:", numRows_);
    }

    // return the value
    if (error_)
        return null;
    return tableData_.getValueAt(rowIndex, columnIndex);
}



/**
Loads the table based on the state of the system.
The <i>system</i> and <i>fileName</i> properties must be set
before this method is called.
**/
public void load ()
{
    Trace.log(Trace.DIAGNOSTIC, "Doing model load.");

    TableModelEvent event = null;
    int oldnum = 0;
    synchronized(this)
    {
        oldnum = numRows_-1;
        if (numRows_ > 0)
        {
            // Set columns and rows to 0 while we rebuild.
            numColumns_ = 0;
            numRows_ = 0;
            event = new TableModelEvent(this, 0, oldnum,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        }
    }

    if (event != null)
    {
        // notify listeners that we've deleted all rows.
        fireTableChanged(event);
        Trace.log(Trace.INFORMATION, "Starting load, changed number of rows to:", numRows_);
    }

    // verify we have enough info to get data
    if (tableData_.getSystem() == null)
    {
        throw new IllegalStateException("system");
    }
    if (tableData_.getFileName() == null)
    {
        throw new IllegalStateException("fileName");
    }

    synchronized(this)
    {
        error_ = false;
        // Do a load to make sure we have no errors.
        // Other calls should not throw errors once a load is done
        // successfully.
        tableData_.load();
        if (error_)
            return;

        // get info about the columns returned
        numColumns_ = tableData_.getNumberOfColumns();

        if (tableData_.getAllRecordsProcessed())
            // covers case when no rows
            numRows_ = tableData_.getNumberOfRows();
        else
            // Set number of rows to default
            numRows_ = 1;
    }

    if (numRows_ > 0)
    {
        // notify listeners that we've changed
        event = new TableModelEvent(this, 0, numRows_-1,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        fireTableChanged(event);
        Trace.log(Trace.INFORMATION, "Did load, changed number of rows to:", numRows_);
    }
}


/**
Restore the state of this object from an object input stream.
It is used when deserializing an object.
@param in The input stream of the object being deserialized.
@exception IOException
@exception ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
{
    // Restore the non-static and non-transient fields.
    in.defaultReadObject();
    // Initialize the transient fields.
    changeListeners_ = new PropertyChangeSupport(this);
    vetoListeners_ = new VetoableChangeSupport(this);
    errorListeners_ = new ErrorEventSupport(this);
    workListeners_ = new WorkingEventSupport(this);
    errorListener_ = new ErrorListener_();
    numRows_ = 0;
    numColumns_ = 0;

    // Add self as listener for errors and work events
    tableData_.addErrorListener(errorListener_);
    tableData_.addWorkingListener (workListeners_);
}


/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    errorListeners_.removeErrorListener (listener);
}



/**
Removes a listener from being notified when the value of any bound
property is changed.

@param  listener  The listener.
**/
public void removePropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.removePropertyChangeListener (listener);
}



/**
Removes a listener from being notified when the value of any constrained
property is changed.

@param  listener  The listener.
**/
public void removeVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.removeVetoableChangeListener (listener);
}



/**
Removes a listener from being notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void removeWorkingListener (WorkingListener listener)
{
    workListeners_.removeWorkingListener (listener);
}



/**
Sets the name of the file.
This property is bound and constrained.
Note that the data will not change until a <i>load()</i> is done.

@param       fileName                The file name.
 The name is specified as a fully qualified path name in the library file system.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setFileName (String fileName)
    throws PropertyVetoException
{
    if (fileName == null)
        throw new NullPointerException("fileName");

    String old = getFileName();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("fileName", old, fileName);

    // Make property change.
    tableData_.setFileName(fileName);

    // Fire the property change event.
    changeListeners_.firePropertyChange("fileName", old, fileName);
}



/**
Sets the key.
This property is bound and constrained.
Note that the data will not change until a <i>load()</i> is done.
The key is only used if the <i>keyed</i> property is true.

@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setKey (Object[] key)
    throws PropertyVetoException
{
    Object[] old = getKey();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("key", old, key);

    // Make property change.
    tableData_.setKey(key);

    // Fire the property change event.
    changeListeners_.firePropertyChange("key", old, key);
}


/**
Sets whether the file will be accessed in key or sequential order.
This property is bound and constrained.
Note that the data in will not change
until a <i>load()</i> is done.

@param keyed  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setKeyed (boolean keyed)
    throws PropertyVetoException
{
    Boolean old = new Boolean(getKeyed());

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("keyed", old, new Boolean(keyed));

    // Make property change.
    tableData_.setKeyed(keyed);

    // Fire the property change event.
    changeListeners_.firePropertyChange("keyed", old, new Boolean(keyed));
}



/**
Sets the search type.
This property is bound and constrained.
Note that the data will not change until a <i>load()</i> is done.
The default is KEY_EQ.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.

@param      searchType      Constant indicating the type of match required.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setSearchType (int searchType)
    throws PropertyVetoException
{
    int old = getSearchType();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("searchType",
        new Integer(old), new Integer(searchType));

    // Make property change.
    tableData_.setSearchType(searchType);

    // Fire the property change event.
    changeListeners_.firePropertyChange("searchType",
        new Integer(old), new Integer(searchType));
}



/**
Sets the system where the file is located.
This property is bound and constrained.
Note that the data will not change until a <i>load()</i> is done.

@param       system                  The system where the file is located.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setSystem (AS400 system)
    throws PropertyVetoException
{
    if (system == null)
        throw new NullPointerException("system");

    AS400 old = getSystem();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("system", old, system);

    // Make property change.
    tableData_.setSystem(system);

    // Fire the property change event.
    changeListeners_.firePropertyChange("system", old, system);
}



/**
Class for listening to error events.  The error_ flag is set,
and the event's source is changed and redispatched to our listeners.
**/
private class ErrorListener_
implements ErrorListener
{

public void errorOccurred(ErrorEvent event)
{
    // set flag that an error occurred
    error_ = true;
    // Change the source in the event and fire
    // to our listeners.
    errorListeners_.fireError(event.getException());
}

} // end of class ErrorListener_


}
