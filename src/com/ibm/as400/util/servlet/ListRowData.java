///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ListRowData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Vector;
/**
*  The ListRowData class represents a list of data.
*  <P>The list of data is formatted into a series of rows where each row
*  contains a finite number of columns determined by the ListMetaData object.
*  Each column within a row contains an individual data item.
*
*  <P>Here are some examples of what a ListRowData object can represent:
*  <UL>
*    <LI>A directory in the integrated file system.</LI>
*    <LI>A list of jobs.</LI>
*    <LI>A list of messages in a message queue.</LI>
*    <LI>A list of printers.</LI>
*    <LI>A list of spooled files.</LI>
*    <LI>A list of users.</LI>
*  </UL>
*
*  <P>A ListRowData object maintains a position in the list that points to its
*  current row of data.  The initial position in the list is set before the
*  first row.  The <i>next</i> method moves to the next row in the list.
*
*  <P>The <i>getObject</i> method is used to retrieve the column value for
*  the current row indexed by the column number.  Columns are numbered
*  starting from 0.
*
*  <P>The number, types, and properties of the list's columns are provided
*  by the <A href="ListMetaData.html">ListMetaData</A> object
*  returned by the <i>getMetaData</i> method.
*
*  <P>ListRowData objects generate the following events:
*  <UL>
*    <LI><A HREF="RowDataEvent.html">RowDataEvent</A> - The events fired are:
*      <UL>
*      <LI>rowAdded()</LI>
*      <LI>rowChanged()</LI>
*      <LI>rowRemoved()</LI>
*      </UL>
*    </LI>
*  </UL>
*
*  <P>The following example creates a ListRowData object and adds rows to represent a directory in the
*  integrated file system.
*  <P><BLOCKQUOTE><PRE>
*  <P>          // Get the files in a directory.
*  AS400 mySystem = new AS400("mySystem.myCompany.com");
*  IFSFile f = new IFSFile(mySystem, pathName);
*  FileListener listener = new FileListener();
*  f.list(listener);
*  Vector files = listener.getFiles();
*  <P>          // Create a metadata object.
*  ListMetaData metaData = new ListMetaData(4);
*  <P>          // Set first column to be the file name.
*  metaData.setColumnName(0, "Name");
*  metaData.setColumnLabel(0, "Name");
*  metaData.setColumnType(0, RowMetaDataType.STRING_DATA_TYPE);
*  <P>          // Set second column to be the file size.
*  metaData.setColumnName(1, "Size");
*  metaData.setColumnLabel(1, "Size");
*  metaData.setColumnType(1, RowMetaDataType.INTEGER_DATA_TYPE);
*  <P>          // Set third column to the file data/time stamp.
*  metaData.setColumnName(2, "DateTime");
*  metaData.setColumnLabel(2, "Date/Time");
*  metaData.setColumnType(2, RowMetaDataType.STRING_DATA_TYPE);
*  <P>          // Set fourth column to the file type.
*  metaData.setColumnName(3, "Type");
*  metaData.setColumnLabel(3, "Type");
*  metaData.setColumnType(3, RowMetaDataType.STRING_DATA_TYPE);
*  <P>          // Create a ListRowData object.
*  ListRowData rowData = new ListRowData();
*  rowData.setMetaData(metaData);
*  <P>          // Add directory entries to list.
*  for (int i=0; i < files.size(); i++)
*  {
*     Object[] row = new Object[4];
*     IFSFile file = (IFSFile)files.elementAt(i);
*     row[0] = file.getName();
*     row[1] = new Long(file.length());
*     row[2] = new java.util.Date(file.lastModified());
*     if (file.isDirectory())
*     {
*        row[3] = "Directory";
*     }
*     else
*     {
*        row[3] = "File";
*     }
*     rowData.addRow(row);
*  }
*  </PRE></BLOCKQUOTE></P>
*
*  @see com.ibm.as400.util.servlet.ListMetaData
**/
public class ListRowData extends RowData implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private RowMetaData metadata_;                 // The metadata.
  transient private RowDataSupport rowdataSupport_;        // The list of row data listeners.

  /**
    Constructs a default ListRowData object.
  **/
  public ListRowData()
  {
    super();
    //@CRS rowdataSupport_ = new RowDataSupport(this);
  }

  /**
  *  Constructs a ListRowData object with the specified <i>metadata</i>.
  *  @param metadata The metadata.
  *  @exception RowDataException If a row data error occurs.
  **/
  public ListRowData(RowMetaData metadata) throws RowDataException
  {
    this();

    try
    {
      setMetaData(metadata);
    }
    catch (PropertyVetoException e)
    { /* Will never occur. */
    }
  }

  /**
  *  Adds the specified <i>row</i> to the list.
  *  The metadata needs to be set before adding a row to the list.
  *  @param row The row to be added.
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void addRow(Object[] row) throws RowDataException
  {
    if (metadata_ == null)
    {
      Trace.log(Trace.ERROR, "Attempting to add a row before setting the metadata.");
      throw new ExtendedIllegalStateException("metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    addRow(row, new Vector[metadata_.getColumnCount()]);
  }

  /**
  *  Adds the specified <i>row</i> to the list.  Each object in the
  *  row is assigned a list of properties specified by <i>properties</i>.
  *  The metadata needs to be set before adding a row to the list.
  *  @param row The row to be added.
  *  @param properties The properties list.
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void addRow(Object[] row, Vector[] properties) throws RowDataException
  {

    // Validate the row and properties parameters.
    validateRow(row);
    validateProperties(properties);

    // Add the values.
    rows_.addElement(row);
    rowProperties_.addElement(properties);

    // Notify listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireAdded(); //@CRS
  }

  /**
  *  Adds the specified <i>row</i> to the list at <i>rowIndex</i>.
  *  The metadata needs to be set before adding a row to the list.
  *  @param row The row.
  *  @param rowIndex The row index (0-based).
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void addRow(Object[] row, int rowIndex) throws RowDataException
  {
    if (metadata_ == null)
    {
      Trace.log(Trace.ERROR, "Attempting to add a row before setting the metadata.");
      throw new ExtendedIllegalStateException("metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    addRow(row, new Vector[metadata_.getColumnCount()], rowIndex);
  }

  /**
  *  Adds the specified <i>row</i> to the list at <i>rowIndex</i>.
  *  Each object in row is assigned a properties list specified by <i>properties</i>.
  *  The metadata needs to be set before adding a row to the list.
  *  @param row The row.
  *  @param properties The properties list.
  *  @param rowIndex The row index (0-based).
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void addRow(Object[] row, Vector[] properties, int rowIndex) throws RowDataException
  {

    // Validate the row and properties parameters.
    validateRow(row);
    validateProperties(properties);

    // Validate the rowIndex parameter.
    if (rowIndex < 0 || rowIndex > rows_.size())
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    // Add the values.
    rows_.insertElementAt(row, rowIndex);
    rowProperties_.insertElementAt(properties, rowIndex);

    // notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireAdded(); //@CRS
  }


  /**
  *  Adds a RowDataListener.
  *  The RowDataListener object is added to an internal list of RowDataListeners;
  *  it can be removed with removeRowDataListener.
  *
  *  @param listener The RowDataListener.
  **/
  public void addRowDataListener(RowDataListener listener)
  {
    if (rowdataSupport_ == null) rowdataSupport_ = new RowDataSupport(this); //@CRS
    rowdataSupport_.addRowDataListener(listener);
  }

  /**
  *  Returns the metadata.
  *  @return The metadata.
  **/
  public RowMetaData getMetaData()
  {
    return metadata_;
  }

  /**
  *  Returns the data objects for the current row.
  *  @return The row.
  **/
  public Object[] getRow()
  {
    // Validate that the list is not empty.
    validateRowList("Attempting to get the row object");

    // Get the current row.
    validateListPosition("Attempting to get the row object");

    return(Object[])rows_.elementAt(position_);
  }

  /**
  *  Deserializes and initializes transient data.
  **/
  private void readObject(java.io.ObjectInputStream in)
  throws java.io.IOException, ClassNotFoundException, RowDataException
  {
    in.defaultReadObject();
    //@CRS rowdataSupport_ = new RowDataSupport(this);
  }

  /**
  *  Removes the row from the list at the specified <i>rowIndex</i>.
  *  @param rowIndex The row index (0-based).
  **/
  public void removeRow(int rowIndex)
  {

    // Validate that the list is not empty.
    validateRowList("Attempting to remove a row");

    // Validate the rowIndex parameter.
    if ((rowIndex < 0) || (rowIndex >= rows_.size()))
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    rows_.removeElementAt(rowIndex);

    // Remove the parameter list.
    rowProperties_.removeElementAt(rowIndex);
    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireRemoved(); //@CRS
  }

  /**
  *  Removes this RowDataListener from the internal list.
  *  If the RowDataListener is not on the list, nothing is done.
  *  @param listener The RowDataListener.
  **/
  public void removeRowDataListener(RowDataListener listener)
  {
    if(listener == null)
        throw new NullPointerException("listener");
    if (rowdataSupport_ != null) rowdataSupport_.removeRowDataListener(listener); //@CRS
  }

  /**
  *  Sets the metadata.
  *  @param metadata The metadata.
  *  @exception RowDataException If a row data error occurs.
  *  @exception PropertyVetoException If a change is vetoed.
  **/
  public void setMetaData(RowMetaData metadata) throws RowDataException, PropertyVetoException
  {
    // Validate the metadata parameter.
    if (metadata == null)
      throw new NullPointerException("metaData");

    if (metadata.getColumnCount() == 0)
    {
      Trace.log(Trace.ERROR, "The metadata parameter 'columns' is invalid.");
      throw new ExtendedIllegalStateException("metadata columns", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    RowMetaData old = metadata_;
    if (vetos_ != null) vetos_.fireVetoableChange("metadata", old, metadata); //@CRS

    metadata_ = metadata;

    if (changes_ != null) changes_.firePropertyChange("metadata", old, metadata); //@CRS
  }

  /**
  *  Sets the row at the specified <i>rowIndex</i> to be the specified <i>row</i>.
  *  @param row The updated row.
  *  @param rowIndex The row index (0-based).
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void setRow(Object[] row, int rowIndex) throws RowDataException
  {
    if (metadata_ == null)
    {
      Trace.log(Trace.ERROR, "Attempting to add a row before setting the metadata.");
      throw new ExtendedIllegalStateException("metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    setRow(row, new Vector[metadata_.getColumnCount()], rowIndex);
  }

  /**
  *  Sets the row at the specified <i>rowIndex</i> to be the specified <i>row</i>.
  *  Each object in the row is assigned a properties list specified by <i>properties</i>.
  *  @param row The updated row.
  *  @param properties The properties list.
  *  @param rowIndex The row index (0-based).
  *  @exception RowDataException If the row length does not match the number
  *  of columns specified in the metadata.
  **/
  public void setRow(Object[] row, Vector[] properties, int rowIndex) throws RowDataException
  {

    // Validate that the list is not empty.
    validateRowList("Attempting to change a row");

    // Validate the row parameter.
    validateRow(row);

    // Validate the rowIndex parameter.
    if (rowIndex < 0 || rowIndex >= rows_.size())
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    // Validate the properties parameter.
    validateProperties(properties);

    // Set the new values.
    rows_.setElementAt(row, rowIndex);
    rowProperties_.setElementAt(properties, rowIndex);

    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireChanged(); //@CRS
  }

  /**
  *  Validates the row properties.
  *  @param properties The row properties.
  *  @exception RowDataException If a rowdata error occurs.
  **/
  private void validateProperties(Vector[] properties) throws RowDataException
  {
    // Validate the properties parameter.
    if (properties == null)
      throw new NullPointerException("properties");

    // Verify the length matches the number of columns.
    if (properties.length != metadata_.getColumnCount())
      throw new ExtendedIllegalArgumentException("properties", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }

  /**
  *  Validates the specified <i>row</i>.
  *  @param row The row.
  *  @exception RowDataException If a rowdata error occurs.
  **/
  private void validateRow(Object[] row) throws RowDataException
  {
    // Validate the row parameter.
    if (row == null)
      throw new NullPointerException("row");

    // Verify that the metadata is set.  Used in determining the number of columns in a row.
    if (metadata_ == null)
    {
      Trace.log(Trace.ERROR, "Attempting to process a row before setting the metadata.");
      throw new ExtendedIllegalStateException("metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Verify that the length matches the number of columns specified by the metadata.
    if (row.length != metadata_.getColumnCount())
      throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }
}
