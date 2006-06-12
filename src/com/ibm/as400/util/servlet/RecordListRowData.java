///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListRowData.java
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
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
*  The RecordListRowData class represents a list of records.
*
*  <P>A Record represents the data described by a RecordFormat.  It can represent:
*  <UL>
*    <LI>An entry in a data queue.</LI>
*    <LI>The parameter data provided to or returned by a program call.</LI>
*    <LI>A record to be written to or read from a file.</LI>
*    <LI>Any data returned from the i5/OS system that needs to be converted between i5/OS
*    format and Java format.</LI>
*  </UL>
*
*  <P>A RecordListRowData object maintains a position in the list that points to its
*  current row of data.  The initial position in the list is set before the
*  first row.  The <i>next</i> method moves to the next row in the list.
*
*  <P>The <i>getObject</i> method is used to retrieve the column value for
*  the current row indexed by the column number.  Columns are numbered
*  starting from 0.
*
*  <P>The number, types, and properties of the list's columns are provided
*  by the <A href="RecordFormatMetaData.html">RecordFormatMetaData</A>
*  object returned by the <i>getMetaData</i> method.
*
*  <P>RecordListRowData objects generate the following events:
*  <UL>
*    <LI><A href="RowDataEvent.html">RowDataEvent</A> - The events fired are:
*      <UL>
*      <LI>rowAdded()</LI>
*      <LI>rowChanged()</LI>
*      <LI>rowRemoved()</LI>
*      </UL>
*    </LI>
*  </UL>
*
*  <P>The following example creates a RecordListRowData object and adds the records from a
*  sequential file.
*  <BLOCKQUOTE><PRE>
*  <P>       // Create an object to represent the server system.
*  AS400 mySystem = new AS400("mySystem.myCompany.com");
*  <P>       // Create a file object that represents the file.
*  SequentialFile sf = new SequentialFile(mySystem, ifspath);
*  <P>       // Set the record format of the file.
*  sf.setRecordFormat();
*  <P>       // Get the records in the file.
*  Record[] records = sf.readAll();
*  <P>       // Create a RecordListRowData object and add the records.
*  RecordListRowData rowData = new RecordListRowData();
*  for (int i=0; i < records.length; i++)
*  {
*  <P>   rowData.addRow(records[i]);
*  }
*  </PRE></BLOCKQUOTE>
**/
public class RecordListRowData extends RowData implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  static final long serialVersionUID = 7785461189425222072L;

  private RecordFormat recordFormat_;            // The record format.
  private RecordFormatMetaData metadata_;        // The record metadata.

  transient private RowDataSupport rowdataSupport_;   // The list of row data listeners.
  transient private Record currentRecord_;       // The current record.
  transient private int currentRecordIndex_ = -1;     // The row position of the current record.

  /**
    Constructs a default RecordListRowData object.
  **/
  public RecordListRowData()
  {
    super();
    //@CRS rowdataSupport_ = new RowDataSupport(this);
  }

  /**
  *  Constructs a RecordListRowData object with the specified <i>recordFormat</i>.
  *  @param recordFormat The record format.
  **/
  public RecordListRowData(RecordFormat recordFormat)
  {
    this();

    try
    {
      setRecordFormat(recordFormat);
    }
    catch (PropertyVetoException e)
    { /* Will never happen. */
    }
  }

  /**
  *  Adds the specified <i>record</i> to the end of the record list.
  *  @param record The record to be added.
  *  @exception RowDataException If a row data error occurs.
  **/
  public void addRow(Record record) throws RowDataException
  {
    // Validate the record parameter.
    if (record == null)
      throw new NullPointerException("record");

    // Add the row.
    addRow(record, new Vector[record.getRecordFormat().getNumberOfFields()]);
  }

  /**
  *  Adds the specified <i>record</i> to the end of the record list.
  *  Each field in the record is assigned the appropriate properties list
  *  specified by <i>properties</i>.
  *  @param record The record to be added.
  *  @param properties The properties list.
  **/
  public void addRow(Record record, Vector[] properties)
  {

    // Validate the record parameter.
    validateRecord(record);

    // Validate the properties parameter.
    validateProperties(properties);

    // Add the row and properties to the list.
    rows_.addElement(record);
    rowProperties_.addElement(properties);

    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireAdded(); //@CRS
  }

  /**
  *  Adds the specified <i>record</i> to the record list at the specified <i>rowIndex</i>.
  *  @param record The record to be added.
  *  @param rowIndex The rowIndex (0-based).
  *  @exception RowDataException If a row data error occurs.
  **/
  public void addRow(Record record, int rowIndex) throws RowDataException
  {
    // Validate the record parameter.
    if (record == null)
      throw new NullPointerException("record");

    addRow(record, rowIndex, new Vector[record.getRecordFormat().getNumberOfFields()]);
  }

  /**
  *  Adds the specified <i>record</i> to the record list at the specified <i>rowIndex</i>.
  *  Each field in the record is assigned the appropriate properties list specified by
  *  <i>properties</i>.
  *  @param record The record to be added.
  *  @param rowIndex The row index (0-based).
  *  @param properties The properties list.
  **/
  public void addRow(Record record, int rowIndex, Vector[] properties)
  {

    // Validate the record parameter.
    validateRecord(record);

    // Validate the rowIndex parameter.
    validateRowIndex(rowIndex);

    // Validate the properties parameter.
    validateProperties(properties);

    // Add the row and properties to the list.
    rows_.insertElementAt(record, rowIndex);
    rowProperties_.insertElementAt(properties, rowIndex);

    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireAdded(); //@CRS
  }

  /**
  *  Adds a RowDataListener.
  *  The RowDataListener object is added to an internal list of row data listeners;
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
  *  Returns the current record's field at the specified <i>columnIndex</i>.
  *  @param columnIndex The column index (0-based).
  *  @return The field object.
  *  @exception RowDataException If a row data error occurs.
  **/
  public Object getObject(int columnIndex) throws RowDataException
  {
    String action = "Attempting to get the column object";

    // Verify that a row has been added.
    validateRowList(action);

    // Validate the columnIndex parameter.
    if ((columnIndex < 0) || (columnIndex >= metadata_.getColumnCount()))
      throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    // Check the current row.
    validateListPosition(action);

    // Get the current record.
    if (currentRecordIndex_ != position_)
    {
      currentRecord_ = (Record)rows_.elementAt(position_);
      currentRecordIndex_ = position_;
    }

    try
    {
      return currentRecord_.getField(columnIndex);
    }
    catch (UnsupportedEncodingException e)
    {
      Trace.log(Trace.ERROR, "Rethrowing UnsupportedEncodingException");
      throw new RowDataException(e);
    }
  }

  /**
  *  Returns the record format.
  *  @return The record format.
  **/
  public RecordFormat getRecordFormat()
  {
    return recordFormat_;
  }

  /**
  *  Returns the Record object for the current row.
  *  @return The row.
  **/
  public Record getRow()
  {
    String action = "Attempting to get the row object";

    // Validate the list is not empty.
    validateRowList(action);

    // Validate the list position.
    validateListPosition(action);

    return(Record)rows_.elementAt(position_);
  }

  /**
  *  Deserializes and initializes transient data.
  **/
  private void readObject(java.io.ObjectInputStream in)
  throws java.io.IOException, ClassNotFoundException, RowDataException, PropertyVetoException
  {
    in.defaultReadObject();

    //@CRS rowdataSupport_ = new RowDataSupport(this);
    currentRecordIndex_ = -1;
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
  *  Removes a record from the record list at the specified <i>rowIndex</i>.
  *  @param rowIndex The row index (0-based).
  **/
  public void removeRow(int rowIndex)
  {

    // Verify the list is not empty.
    validateRowList("Attempting to remove a row");

    // Validate the rowIndex parameter.
    if (rowIndex < 0 || rowIndex >= rows_.size())
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    // Remove the row and row properties from the list.
    rows_.removeElementAt(rowIndex);
    rowProperties_.removeElementAt(rowIndex);

    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireRemoved(); //@CRS
  }

  /**
  *  Sets the record at the specified <i>rowIndex</i> to be the specified <i>record</i>.
  *  @param record The record.
  *  @param rowIndex The row index (0-based).
  **/
  public void setRow(Record record, int rowIndex)
  {
    // Validate the record parameter.
    if (record == null)
      throw new NullPointerException("record");

    setRow(record, rowIndex, new Vector[record.getRecordFormat().getNumberOfFields()]);
  }

  /**
  *  Sets the record at the specified <i>rowIndex</i> to be the specified <i>record</i>.
  *  Each object in the row is assigned the appropriate properties list specified by
  *  <i>properties</i>.
  *  @param record The record.
  *  @param rowIndex The row index (0-based).
  *  @param properties The row properties.
  **/
  public void setRow(Record record, int rowIndex, Vector[] properties)
  {

    // Validate that the list is not empty.
    validateRowList("Attempting to change the row");

    // Validate the record parameter.
    validateRecord(record);

    // Validate the row parameter.
    if (rowIndex < 0 || rowIndex >= rows_.size())
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    // Validate the properites parameter.
    validateProperties(properties);

    // Set the row and properties to the new values.
    rows_.setElementAt(record, rowIndex);
    rowProperties_.setElementAt(properties, rowIndex);

    // Notify the listeners.
    if (rowdataSupport_ != null) rowdataSupport_.fireChanged(); //@CRS
  }

  /**
  *  Sets the record format for the record list.
  *  The metadata is set using the specified <i>recordFormat</i>.
  *  If a record format already exists, then setting a new record format
  *  will remove all rows from the list.
  *  @param recordFormat The record format.
  *  @exception PropertyVetoException If a change is vetoed.
  *  @see #getRecordFormat
  *  @see #getMetaData
  **/
  public void setRecordFormat(RecordFormat recordFormat) throws PropertyVetoException
  {
    // Validate the recordFormat parameter.
    if (recordFormat == null)
      throw new NullPointerException("recordFormat");

    RecordFormat old = recordFormat_;
    if (vetos_ != null) vetos_.fireVetoableChange("recordFormat", old, recordFormat); //@CRS

    recordFormat_ = recordFormat;

    // Remove all existing rows and row properties.
    if (!rows_.isEmpty())
    {
      rows_ = new Vector();
      rowProperties_ = new Vector();
    }

    if (changes_ != null) changes_.firePropertyChange("recordFormat", old, recordFormat); //@CRS

    // Set the metadata.
    if (metadata_ == null)
      metadata_ = new RecordFormatMetaData(recordFormat_);
    else
      metadata_.setRecordFormat(recordFormat_);
  }

  /**
  *  Validates the row properties.
  *  @param properties The properties for the row.
  **/
  private void validateProperties(Vector[] properties)
  {
    if (properties == null)
      throw new NullPointerException("properties");

    // Verify the length match the number of columns.
    if (properties.length != metadata_.getColumnCount())
      throw new ExtendedIllegalArgumentException("properties", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }

  /**
  *  Validates the record.
  *  @param record The record.
  **/
  private void validateRecord(Record record)
  {
    if (record == null)
      throw new NullPointerException("record");

    try
    {
      // Check the record format.
      if (recordFormat_ == null)
        setRecordFormat(record.getRecordFormat());
      else
      {
        if (record.getRecordFormat() != recordFormat_)
        {
          Trace.log(Trace.ERROR, "Parameter 'record' does not contain the correct record format.");
          throw new ExtendedIllegalArgumentException("record", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
      }
    }
    catch (PropertyVetoException e)
    { /* Will never occur. */
    }
  }

  /**
  *  Validates the row index parameter.
  *  @param rowIndex The row index.
  **/
  private void validateRowIndex(int rowIndex)
  {
    if (rowIndex < 0 || rowIndex > rows_.size())
      throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
  }
}
