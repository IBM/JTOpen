///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryFieldsPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.sql.Types;
import java.util.Vector;



/**
The SQLQueryFieldsPane class represents a panel which
contains a fields table,
used for a page of the SQLQueryBuilderPane notebook.
**/
abstract class SQLQueryFieldsPane
extends JPanel
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// This class is not meant to be serialized, it should be transient.
// This class has items marked transient even though it is not
// serializable because otherwise errors were received when
// serializing objects that contained this class (even though they
// were transient instances.  readObject() was added to be safe.

// GUI components
protected SQLMetaDataTablePane fields_;
protected SQLMetaDataTableModel fieldModel_;
protected SQLQueryBuilderPane parent_;

// Indicates if fieldModel_ has changed.
protected boolean fieldsChanged_ = false;

// Listen to changes in the fields table.
transient protected FieldListener_ fieldListener_ = null;


/**
Constructs a SQLQuerySelectPane object.
<i>init</i> must be called to build the GUI contents.
@param parent The parent of this panel.
**/
public SQLQueryFieldsPane (SQLQueryBuilderPane parent)
{
    super();
    parent_ = parent;
    fieldModel_ = parent_.fields_;
    // Add listener to changes to table fields.
    fieldListener_ = new FieldListener_();
    parent_.addFieldListener(fieldListener_);
}


/**
Returns the name of the field at the given row.
@param index The row of field.
@return The name of the field at the given row.
**/
protected String fieldName(int index)
{
    return (String)
        (fieldModel_.getValueAt(index, SQLMetaDataTableModel.FIELD_NAME_));
}


/**
Returns the names of the fields which are characters.
@return The names of the character fields.
**/
protected String[] getCharacterFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type = fieldModel_.getSQLType(i);
        if (type == Types.CHAR || type == Types.VARCHAR ||
            type == Types.LONGVARCHAR)
        {
            // character field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Returns the names of the fields which are a date or timestamp.
@return The names of the date and timestamp fields.
**/
protected String[] getDateFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type = fieldModel_.getSQLType(i);
        if (type == Types.DATE ||
            type == Types.TIMESTAMP)
        {
            // numeric field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Returns the names of the fields which are a date, time, or timestamp.
@return The names of the date, time, and timestamp fields.
**/
protected String[] getDateTimeFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type = fieldModel_.getSQLType(i);
        if (type == Types.DATE ||
            type == Types.TIME ||
            type == Types.TIMESTAMP)
        {
            // numeric field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Returns the names of the fields in the fields table.
@return The names of the fields.
**/
protected String[] getFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    String[] results = new String[numRows];
    for (int i=0; i<numRows; ++i)
    {
        results[i] = (String)
                (fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
    }
    return results;
}


/**
Returns the names of the fields in the fields table that are a specific
type.
@param type The SQL type of fields to return.
@return The names of the fields.
**/
protected String[] getFieldNamesOfType(int type)
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type2 = fieldModel_.getSQLType(i);
        if (type2 == type)
        {
            // numeric field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Returns the names of the numeric fields in the fields table.
@return The names of the numeric fields.
**/
protected String[] getNumericFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type = fieldModel_.getSQLType(i);
        if (type == Types.BIGINT ||
            type == Types.DECIMAL ||
            type == Types.DOUBLE ||
            type == Types.FLOAT ||
            type == Types.INTEGER ||
            type == Types.NUMERIC ||
            type == Types.REAL ||
            type == Types.SMALLINT ||
            type == Types.TINYINT)
        {
            // numeric field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Returns the names of the fields which are a time or timestamp.
@return The names of the time and timestamp fields.
**/
protected String[] getTimeFieldNames()
{
    int numRows = fieldModel_.getRowCount();
    Vector results = new Vector();
    for (int i=0; i<numRows; ++i)
    {
        int type = fieldModel_.getSQLType(i);
        if (type == Types.TIME ||
            type == Types.TIMESTAMP)
        {
            // numeric field
            results.addElement(
                fieldModel_.getValueAt(i,SQLMetaDataTableModel.FIELD_NAME_));
        }
    }
    String[] results2 = new String[results.size()];
    results.copyInto(results2);
    return results2;
}


/**
Build the panel GUI.
**/
public void init()
{
    setupPane();
    update();
}


/**
Restores the state of this object from an object input stream.
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
    fieldListener_ = null;
}


/**
Called when a row in the table is double-clicked on.
This implementatin does nothing, subclasses should override
if double-clicking a field should result in action.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    // Build table pane.  Every instance has their own table,
    // even though the model may be shared.
    fields_ = new SQLMetaDataTablePane(fieldModel_);
    fields_.setBorder(new EmptyBorder(5, 5, 5, 5));

    // Listen for double clicks on table.
    final JTable table = fields_.table_;  // need final for anonymous class
    table.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked (MouseEvent event)
            {
                if (event.getClickCount () > 1) // if double click
                {
                    // Get the object that was double clicked, if any.
                    int row = table.rowAtPoint(event.getPoint());
                    if (row != -1)  // -1 means no object under mouse
                    {
                            rowPicked(row);
                    }
                }
            }
        }
    );
}



/**
Update the fieldModel if needed.
**/
public void update()
{
    if (fieldsChanged_)
    {
        // Build new model.
        fieldModel_ = parent_.fields_;

        // Set table to use new model.
        fields_.setDataModel(fieldModel_);

        fieldsChanged_ = false;
    }
}



/**
Class to listen for property changes on the fields contained in the
tables associated with the query.
**/
/* private */ class FieldListener_ //@B0C - made package scope
implements PropertyChangeListener, java.io.Serializable //@B0C - made serializable
{
    public void propertyChange(PropertyChangeEvent event)
    {
        fieldsChanged_ = true;
    }
}

}
