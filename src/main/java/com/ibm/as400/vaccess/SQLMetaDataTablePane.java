///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLMetaDataTablePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;



/**
The SQLMetaDataTablePane class represents a table that contains the
field information for a set of tables.
**/
class SQLMetaDataTablePane
extends JPanel
implements java.io.Serializable //@B0A - for consistency
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



//The table contained in this panel.
transient JTable table_; //@B0C - made transient
// The data model for the table.
SQLMetaDataTableModel model_;


/**
Constructs a SQLMetaDataTablePane object.

@param       model  The data model for table data.
**/
public SQLMetaDataTablePane (SQLMetaDataTableModel model)
{
    super();
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization
    model_ = model;
    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); //@B0C
    table_.setModel(model_);
    table_.setShowGrid(false);
    // set up table columns
    TableColumn column = new TableColumn(SQLMetaDataTableModel.FIELD_NAME_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_NAME"));
    column.setPreferredWidth(150); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_TYPE_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_TYPE"));
    column.setPreferredWidth(70); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_LENGTH_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_LENGTH"));
    column.setPreferredWidth(60); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_DECIMALS_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_DECIMALS"));
    column.setPreferredWidth(65); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_NULLS_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_NULL"));
    column.setPreferredWidth(80); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_DESC_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_DESCRIPTION"));
    column.setPreferredWidth(180); //@B0C
    table_.addColumn(column);
    // build panel with table and headings
    setupPane();
}



/**
Loads the table data from the system.
**/
public void load()
{
    // refresh the result set data
    model_.load();
}


//@B0A
/**
Restores the state of this object from an object input stream.
It is used when deserializing an object.
@param in The input stream of the object being deserialized.
@exception IOException
@exception ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws java.io.IOException, ClassNotFoundException
{
    in.defaultReadObject();
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization next time

    //@B0A: The following code is copied from the constructor since
    // table_ is now transient.
    table_ = new JTable();
    table_.setAutoCreateColumnsFromModel(false);
    table_.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); //@B0C
    table_.setModel(model_);
    table_.setShowGrid(false);
    // set up table columns
    TableColumn column = new TableColumn(SQLMetaDataTableModel.FIELD_NAME_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_NAME"));
    column.setPreferredWidth(150); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_TYPE_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_TYPE"));
    column.setPreferredWidth(70); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_LENGTH_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_LENGTH"));
    column.setPreferredWidth(60); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_DECIMALS_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_DECIMALS"));
    column.setPreferredWidth(65); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_NULLS_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_NULL"));
    column.setPreferredWidth(80); //@B0C
    table_.addColumn(column);
    column = new TableColumn(SQLMetaDataTableModel.FIELD_DESC_);
    column.setHeaderValue(ResourceLoader.getQueryText("DBQUERY_COLUMN_DESCRIPTION"));
    column.setPreferredWidth(180); //@B0C
    table_.addColumn(column);
    // build panel with table and headings
    setupPane();
}




/**
Changes the data for the table.

@param model The data model for the table.
**/
public void setDataModel(SQLMetaDataTableModel model)
{
    model_ = model;
    table_.setModel(model);
}



/**
Enables or disables this pane
@param enabled true if the pane should be enabled, false otherwise.
**/
public void setEnabled(boolean enabled)
{
    table_.setEnabled(enabled);
}


/**
Puts the table and heading into the panel.
**/
private void setupPane()
{
	setLayout(new BorderLayout());

    // Add table to panel.
    JScrollPane pane = new JScrollPane (table_); // @A1C
    BevelBorder border = new BevelBorder(BevelBorder.LOWERED);
    pane.setBorder(border);
    add("Center", pane);
}

}

