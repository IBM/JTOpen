///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListFormPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ActionCompletedListener;
import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.HexFieldDescription;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;



/**
The RecordListFormPane class represents a form that is filled in with the fields
of a file on the server.  The form displays one record at a time
and provides buttons that allow the user to scroll forward,
backward, to the first or last record, or refresh the
view of the file.

<p>The data in the form is retrieved from the system
(and the GUI fields for the data are created)
when <i>load()</i> is called.  If <i>load()</i> is not called,
the form will be empty.

<p>Users must call <i>close()</i> to ensure that the server
resources are properly freed when this form is no longer needed.

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>RecordListFormPane objects generate the following events:
<ul>
  <li>ActionCompletedEvent
  <li>ErrorEvent
  <li>PropertyChangeEvent
</ul>

<pre>
 // Set up table for file contents.
AS400 system = new AS400("MySystem", "Userid", "Password");
String file = "/QSYS.LIB/QGPL.LIB/MyFile.FILE";
final RecordListFormPane pane = new RecordListFormPane(system, file);

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
**/
public class RecordListFormPane
extends JComponent
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



// Label for the record number.
transient private   JLabel     recordLabel_; //@B0C - made transient
// Label for the record number.
transient private   JLabel     recordNumber_; //@B0C - made transient
// Button for moving to the first record.
/*private*/   transient JButton     firstButton_; //@B0C - made transient
// Button for moving to the last record.
/*private*/   transient JButton     lastButton_; //@B0C - made transient
// Button for moving to the next record.
/*private*/   transient JButton     nextButton_; //@B0C - made transient
// Button for moving to the previous record.
/*private*/   transient JButton     previousButton_; //@B0C - made transient
// Button for refreshing the data.
/*private*/   transient JButton     refreshButton_; //@B0C - made transient
// Labels for the description of each field.
transient private JLabel[] labels_ = new JLabel[0];
// Textfields for the value of each field.
transient private JTextField[] values_ = new JTextField[0];
// Formatters for the value of each field.
transient private DBCellRenderer[] formatter_ = new DBCellRenderer[0];
// Status line.  Used for errors.
private JLabel status_;
// Panel used for center data area.
private JPanel dataArea_;

// Renderers for the different types of data, columns use these.
private DBCellRenderer leftCell_ = new DBCellRenderer(SwingConstants.LEFT);
// General types of data in columns.  Used to determine column renderer.
private static final int TYPE_CHAR = 1;
private static final int TYPE_HEX = 2;


// Event support.
transient private ActionCompletedEventSupport actionListeners_
    = new ActionCompletedEventSupport (this);
transient /*private*/ ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);


// Record data
transient int current_ = -1;  // Record being shown.  Range is 0 to numRows-1.
private RecordListData tableData_ = new RecordListData();

// Flag for if an error event was sent.
transient /*private*/ boolean error_;

// Adapter for listening for working events and enabling working cursor.
transient private WorkingCursorAdapter worker_
    = new WorkingCursorAdapter(this);



/**
Constructs a RecordListFormPane object.
This constructor sets the <i>keyed</i> property to false.
**/
public RecordListFormPane ()
{
    super();

    //@B0A
    // We add a fake FocusListener whose real purpose is to uninstall
    // the UI early so the JTable that is part of our UI does not try
    // to get serialized.
    // See also: source code for javax.swing.JComponent in JDK 1.2.
    addFocusListener(new SerializationListener(this)); //@B0A
    
    // Add self as listener for errors and work events
    tableData_.addErrorListener(new ErrorListener_());
    tableData_.addWorkingListener(worker_);

    init();
}



/**
Constructs a RecordListFormPane object.
This constructor sets the <i>keyed</i> property to false.

@param       system          The system where the file is located.
@param       fileName        The file name.
 The name is specified as a fully qualified path name in the library file system.
**/
public RecordListFormPane (AS400 system,
                       String fileName)
{
    super();
    
    if (system == null)
        throw new NullPointerException("system");
    if (fileName == null)
        throw new NullPointerException("fileName");

    //@B0A
    // We add a fake FocusListener whose real purpose is to uninstall
    // the UI early so the JTable that is part of our UI does not try
    // to get serialized.
    // See also: source code for javax.swing.JComponent in JDK 1.2.
    addFocusListener(new SerializationListener(this)); //@B0A
    
    tableData_.setSystem(system);
    tableData_.setFileName(fileName);

    // Add self as listener for errors and work events
    tableData_.addErrorListener(new ErrorListener_());
    tableData_.addWorkingListener(worker_);

    init();
}



/**
Constructs a RecordListFormPane object.
This constructor sets the <i>keyed</i> property to true.

@param       system          The system where the file is located.
@param       fileName        The file name.
 The name is specified as a fully qualified path name in the library file system.
@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
@param      searchType      Constant indicating the type of match required.
**/
public RecordListFormPane (AS400 system,
                       String fileName,
                       Object[] key,
                       int searchType)
{
    super();
    if (system == null)
        throw new NullPointerException("system");
    if (fileName == null)
        throw new NullPointerException("fileName");
    
    //@B0A
    // We add a fake FocusListener whose real purpose is to uninstall
    // the UI early so the JTable that is part of our UI does not try
    // to get serialized.
    // See also: source code for javax.swing.JComponent in JDK 1.2.
    addFocusListener(new SerializationListener(this)); //@B0A
    
    tableData_.setSystem(system);
    tableData_.setFileName(fileName);
    tableData_.setKeyed(true);
    tableData_.setKey(key);
    tableData_.setSearchType(searchType);

    // Add self as listener for errors and work events
    tableData_.addErrorListener(new ErrorListener_());
    tableData_.addWorkingListener(worker_);

    init();
}



/**
Adds a listener to be notified when a new record is displayed.
The listener's <i>actionCompleted()</i> method will be called.

@param  listener  The listener.
**/
public void addActionCompletedListener (ActionCompletedListener listener)
{
    actionListeners_.addActionCompletedListener(listener);
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
Closes the server file this form represents.
**/
public void close()
{
    tableData_.close();
}


/**
Displays the first record of the record list.
**/
public void displayFirst ()
{
    synchronized (this)
    {
        current_ = 0;
        refreshScreen();
    }

    // Send event.
    actionListeners_.fireActionCompleted();
}



/**
Displays the last record of the record list.
**/
public void displayLast ()
{
    synchronized (this)
    {
        error_ = false;
        current_ = tableData_.getNumberOfRows() - 1;
        if (error_)    // error during getNumberOfRows
            current_ = tableData_.getLastRecordProcessed();

        refreshScreen();
    }

    // Send event.
    actionListeners_.fireActionCompleted();
}



/**
Displays the next record of the record list.
If the last record is being displayed, the first record
will be displayed.
**/
public void displayNext ()
{
    synchronized (this)
    {
        // Move the current record index.
        if (tableData_.getAllRecordsProcessed())
        {
            // Do not want to do getNumberOfRows unless all records processed
            // since getNumberOfRows will process all records.
            // getNumberOfRows should never fire an error here.
            if (current_+1 == tableData_.getNumberOfRows())
               current_ = 0;
            else
               ++current_;
        }
        else
        {
            ++current_;
        }

        refreshScreen();
    }

    // Send event.
    actionListeners_.fireActionCompleted();
}



/**
Displays the previous record of the record list.
If the first record is being displayed, the last record
will be displayed.
**/
public void displayPrevious ()
{
    synchronized (this)
    {
        // Move the current record index.
        if (current_ < 1)
        {
            error_ = false;
            current_ = tableData_.getNumberOfRows() - 1;
            if (error_)  //getNumberOfRows() failed
            {
                current_ = tableData_.getLastRecordProcessed();
            }
        }
        else
        {
            --current_;
        }

        refreshScreen();
    }

    // Send event.
    actionListeners_.fireActionCompleted();
}



/**
Returns the index of the record currently being displayed.
Indices start at 0, and increment one for each of the records
in the list.
Note that this is not the same as the record number.

@return The index of the record currently being displayed.
If there is no record being displayed, -1 is returned.
**/
synchronized public int getCurrentRecord()
{
    return current_;
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
Returns the text of the label at the given index.

@param index The index of the label.  Indices start at 0.

@return The text of the label at the given index.
**/
synchronized public String getLabelText(int index)
{
    return labels_[index].getText();
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
Returns the string value of the current record at the given index.

@param index The index of the value.  Indices start at 0.

@return The value at the given index as a string.
**/
synchronized public String getStringValueAt(int index)
{
    return values_[index].getText();
}



/**
Returns the value of the current record at the given index.

@param index Index of the value.  Indices start at 0.

@return The value at the given index.
**/
synchronized public Object getValueAt(int index)
{
    return tableData_.getValueAt(current_, index);
}



/**
Create GUI.
**/
private void init()
{
    //------------------------------------------------------
    // Create constant screen components.  Data fields will
    // be created by reload().
    //------------------------------------------------------
    // Create position labels.
    recordLabel_ = new JLabel(ResourceLoader.getText("DBFORM_LABEL_RECORD_NUMBER"));
    recordNumber_ = new JLabel();

    // Create all the buttons.
    String text = ResourceLoader.getText("DBFORM_TOOLTIP_FIRST");
    firstButton_ = new JButton(ResourceLoader.getIcon("FirstIcon.gif", text));
    firstButton_.setToolTipText(text);
    text = ResourceLoader.getText("DBFORM_TOOLTIP_LAST");
    lastButton_ = new JButton(ResourceLoader.getIcon("LastIcon.gif", text));
    lastButton_.setToolTipText(text);
    text = ResourceLoader.getText("DBFORM_TOOLTIP_NEXT");
    nextButton_ = new JButton(ResourceLoader.getIcon("NextIcon.gif", text));
    nextButton_.setToolTipText(text);
    text = ResourceLoader.getText("DBFORM_TOOLTIP_PREVIOUS");
    previousButton_ = new JButton(ResourceLoader.getIcon("PreviousIcon.gif", text));
    previousButton_.setToolTipText(text);
    text = ResourceLoader.getText("DBFORM_TOOLTIP_REFRESH");
    refreshButton_ = new JButton(ResourceLoader.getIcon("RefreshIcon.gif", text));
    refreshButton_.setToolTipText(text);
    // Add listeners to buttons to call correct functions.
    ButtonListener_ l = new ButtonListener_();
    firstButton_.addActionListener(l);
    lastButton_.addActionListener(l);
    nextButton_.addActionListener(l);
    previousButton_.addActionListener(l);
    refreshButton_.addActionListener(l);

    // build center data area
    dataArea_ = new JPanel();
	 dataArea_.setBorder(new EmptyBorder(5,5,5,5));

    // build status line
    status_ = new JLabel(ResourceLoader.getText("DBFORM_MSG_NO_DATA"));

    //------------------------------------------------------
    // Build the constant screen components.  Data fields will
    // be added by reload().
    //------------------------------------------------------
    // Set main layout to border layout.
    setLayout(new BorderLayout());
    // Add buttons to new panel.
    JPanel panel1 = new JPanel();
    panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
    panel1.add(firstButton_);
    panel1.add(previousButton_);
    panel1.add(nextButton_);
    panel1.add(lastButton_);
    panel1.add(refreshButton_);
    // Add status line to new panel.
    JPanel panel2 = new JPanel();
    panel2.setLayout(new FlowLayout(FlowLayout.CENTER));
    panel2.add(status_);
    // Add buttons and status to bottom.
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add("North", panel1);
    panel.add("South", panel2);
    add("South", panel);
    // Add position labels.
    panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    panel.add(recordLabel_);
    panel.add(recordNumber_);
    add("North", panel);
    // Add center data area
    add("Center", dataArea_);

}



/**
Refreshes the view based on the state of the system.
The first record will be displayed.
The labels are reconstructed, so any label customization will be lost.
The <i>fielName</i> and <i>system</i> properties
must be set before this method is called.
**/
public void load ()
{
    synchronized (this)
    {
        // Remove any previous components from the screen
        dataArea_.removeAll();

        // verify we have enough info to get data
        IllegalStateException exception = null;
        error_ = false;
        if (tableData_.getSystem() == null)
        {
            exception = new IllegalStateException("system");
            error_ = true;
        }
        else if (tableData_.getFileName() == null)
        {
            exception = new IllegalStateException("fileName");
            error_ = true;
        }
        else
        {
            // Do a reload to make sure we have no errors.
            // Other calls should not throw errors once a reload is done
            // successfully.
            tableData_.load();
        }

        if (error_)
        {
            values_ = new JTextField[0];
            labels_ = new JLabel[0];
            current_ = -1;
            refreshScreen();
            if (exception != null)
                throw exception;
            return;
        }

        // Build labels, values, and formatters.
        // Add all fields to the screen.
        int num = tableData_.getNumberOfColumns();
        // label variables
        labels_ = new JLabel[num];
        JLabel label;
        // value variables
        values_ = new JTextField[num];
        JTextField value;
        // formatter variables
        DBCellRenderer leftCell = new DBCellRenderer(SwingConstants.LEFT);
        formatter_ = new DBCellRenderer[num];

        // panel setup
        dataArea_.setLayout(new GridLayout(num,1));
        JPanel panel;

        int type, rlaType;
        Class byteClass = (new byte[0]).getClass();
        // Loop through each field in the record.
        for (int i=0; i<num; ++i)
        {
            // create label
            label = new JLabel(tableData_.getColumnLabel(i));
            labels_[i] = label;

            // determine type of field
            // Try to get the value of a column.
            Object v = null;
            for (int j=0; !tableData_.getAllRecordsProcessed() && v == null; ++j)
            {
                v = tableData_.getValueAt(j, i);
            }
            if (v != null && v.getClass() == byteClass)
                type = TYPE_HEX;
            else
                // text, DBCSxxx, time/date/timestamp, numeric
                type = TYPE_CHAR;

            // create value textfield
            int size = tableData_.getColumnDisplaySize(i);
            // @D1D if (type == TYPE_HEX)
            // @D1D     size = size * 2;  // displayed in hex
            value = new JTextField(size);
            value.setEditable(false);
            values_[i] = value;

            // create formatter
            formatter_[i] = leftCell;

            // add components to screen
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(labels_[i]);
            panel.add(values_[i]);
            dataArea_.add(panel);
        }

        // Update the screen.
        current_ = 0;
        refreshScreen();

    } // end synchronized block

    // Send event.
    actionListeners_.fireActionCompleted();
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
    actionListeners_ = new ActionCompletedEventSupport(this);
    errorListeners_ = new ErrorEventSupport(this);
    worker_ = new WorkingCursorAdapter(this);
    labels_ = new JLabel[0];
    values_ = new JTextField[0];
    formatter_ = new DBCellRenderer[0];
    current_ = -1;
    
    init(); //@B0A - need to initialize the stuff I've made transient
}


/**
Refresh the screen for the current record.
**/
private void refreshScreen ()
{
    if (values_.length == 0)
    {
        // error retrieving data
        current_ = -1;
    }
    else if (tableData_.getAllRecordsProcessed() &&
             tableData_.getNumberOfRows() == 0)
    {
        // no data records
        current_ = -1;
    }

    // No data to show.
    if (current_ == -1)
    {
        status_.setText(ResourceLoader.getText("DBFORM_MSG_NO_DATA"));
        // blank out any value fields
        int num = values_.length;
        for (int i=0; i<num; ++i)
        {
            values_[i].setText("");
        }
        recordNumber_.setText("");
    }
    else
    {
        // Update the text field values.
        int num = values_.length;
        Object data;
        for (int i=0; i<num; ++i)
        {
            // if error during getValueAt, field set to ""
            data = tableData_.getValueAt(current_, i);
            values_[i].setText(formatter_[i].getText(data));
        }
        recordNumber_.setText(String.valueOf(current_ + 1));
        recordNumber_.setSize(recordNumber_.getPreferredSize()); // make sure all text shows
        status_.setText("");
    }
    validate();
}



/**
Removes a listener from being notified when a new record is displayed.

@param  listener  The listener.
**/
public void removeActionCompletedListener(ActionCompletedListener listener)
{
    actionListeners_.removeActionCompletedListener(listener);
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
Sets the name of the file.
This property is bound and constrained.
Note that the data in the form will not change
until a <i>load()</i> is done.

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
    fireVetoableChange("fileName", old, fileName);

    // Make property change.
    tableData_.setFileName(fileName);

    // Fire the property change event.
    firePropertyChange("fileName", old, fileName);
}



/**
Sets the key.
This property is bound and constrained.
Note that the data in the form will not change
until a <i>load()</i> is done.
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
    fireVetoableChange("key", old, key);

    // Make property change.
    tableData_.setKey(key);

    // Fire the property change event.
    firePropertyChange("key", old, key);
}


/**
Sets whether the file will be accessed in key or sequential order.
This property is bound and constrained.
Note that the data in the form will not change
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
    fireVetoableChange("keyed", old, new Boolean(keyed));

    // Make property change.
    tableData_.setKeyed(keyed);

    // Fire the property change event.
    firePropertyChange("keyed", old, new Boolean(keyed));
}



/**
Sets the text of the label at the given index.

@param index The index of the label.  Indices start at 0.
@param text The text of the label.
**/
public void setLabelText(int index,
                         String text)
{
    labels_[index].setText(text);
}



/**
Sets the search type.
This property is bound and constrained.
Note that the data in the form will not change
until a <i>load()</i> is done.
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
    fireVetoableChange("searchType",
        new Integer(old), new Integer(searchType));

    // Make property change.
    tableData_.setSearchType(searchType);

    // Fire the property change event.
    firePropertyChange("searchType",
        new Integer(old), new Integer(searchType));
}



/**
Sets the system where the file is located.
This property is bound and constrained.
Note that the data in the form will not change
until a <i>load()</i> is done.

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
    fireVetoableChange("system", old, system);

    // Make property change.
    tableData_.setSystem(system);

    // Fire the property change event.
    firePropertyChange("system", old, system);
}







/**
Class for listening to action events.  This is used to call the
appropriate methods when buttons are pressed.
**/
private class ButtonListener_
implements ActionListener
{

public void actionPerformed(ActionEvent ev)
{
    // try to perform the appropriate action
    Object source = ev.getSource();
    if (source == firstButton_)
    {
        displayFirst();
    }
    else if (source == lastButton_)
    {
        displayLast();
    }
    else if (source == nextButton_)
    {
        displayNext();
    }
    else if (source == previousButton_)
    {
        displayPrevious();
    }
    else if (source == refreshButton_)
    {
        load();
    }
    // else unknown source - ignore
}

} // end of class ButtonListener_





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
