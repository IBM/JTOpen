///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordFormatMetaData.java
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

import com.ibm.as400.access.AS400Array;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Bin2;             // Java Short
import com.ibm.as400.access.AS400Bin4;             // Java Integer
import com.ibm.as400.access.AS400ByteArray;        // Java Byte[]
import com.ibm.as400.access.AS400Float4;           // Java Float
import com.ibm.as400.access.AS400Float8;           // Java Double
import com.ibm.as400.access.AS400PackedDecimal;    // Java BigDecimal
import com.ibm.as400.access.AS400Structure;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.AS400UnsignedBin2;     // Java Integer
import com.ibm.as400.access.AS400UnsignedBin4;     // Java Long
import com.ibm.as400.access.AS400ZonedDecimal;     // Java BigDecimal
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.RecordFormat;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Serializable;

/**
*  A RecordFormatMetaData object can be used to find out information about the columns of a
*  <A href="RecordListRowData.html">RecordListRowData</A> object.
*
*  <P>RecordFormatMetaData objects generate the following events:
*  <UL>
*  <LI>PropertyChangeEvent</LI>
*  <LI>VetoableChangeEvent</LI>
*  </UL>
**/
public class RecordFormatMetaData implements RowMetaData, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private RecordFormat recordFormat_;          // The record format.
   transient private String[] columnLabel_;     // The column label list.

   transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

   /**
   *  Constructs a default RecordFormatMetaData object.
   **/
   public RecordFormatMetaData()
   {

   }

   /**
   *  Constructs a RecordFormatMetaData object with the specified <i>recordFormat</i>.
   *  @param recordFormat The record format.
   **/
   public RecordFormatMetaData(RecordFormat recordFormat)
   {
      try
      {
         setRecordFormat(recordFormat);
      }
      catch (PropertyVetoException e) { /* Will never occur. */ }
   }

   /**
   *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b>
   *  method is called each time the value of any bound property is changed.
   *  @see #removePropertyChangeListener
   *  @param listener The PropertyChangeListener.
   **/
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");

      changes_.addPropertyChangeListener(listener);
   }

   /**
   *  Adds the VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b>
   *  method is called each time the value of any constrained property is changed.
   *  @see #removeVetoableChangeListener
   *  @param listener The VetoableChangeListener.
   **/
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");

      vetos_.addVetoableChangeListener(listener);
   }

   /**
   *  Returns the number of columns.
   *  @return The column count.
   **/
   public int getColumnCount()
   {
      // Verify that the record format has been set.
      if (recordFormat_ == null)
      {
         Trace.log(Trace.ERROR, "Attempting to get the column count before setting the record format.");
         throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      return recordFormat_.getNumberOfFields();
   }


   /**
   *  Returns the display size in characters of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column display size in characters.
   **/
   public int getColumnDisplaySize(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the column display size");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Get the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      return fd.getLength();
   }

   /**
   *  Returns the label of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column label.
   **/
   public String getColumnLabel(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the column label");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      String label = columnLabel_[columnIndex];

      // Use column name if null.
      if (label == null)
         label = getColumnName(columnIndex);
      columnLabel_[columnIndex] = label;

      return label;
   }

   /**
   *  Returns the name of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column name.
   **/
   public String getColumnName(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the column name");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Pull the column name from the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      return fd.getFieldName();
   }

   /**
   *  Returns the data type of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column RowMetaDataType.
   *  @see com.ibm.as400.util.servlet.RowMetaDataType
   **/
   public int getColumnType(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the column type");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Get the data type from the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      AS400DataType datatype = fd.getDataType();

      if (datatype instanceof AS400Bin2)
         return RowMetaDataType.SHORT_DATA_TYPE;
      else if (datatype instanceof AS400Bin4)
         return RowMetaDataType.INTEGER_DATA_TYPE;
      else if (datatype instanceof AS400ByteArray)
         return RowMetaDataType.BYTE_ARRAY_DATA_TYPE;
      else if (datatype instanceof AS400Float4)
         return RowMetaDataType.FLOAT_DATA_TYPE;
      else if (datatype instanceof AS400Float8)
         return RowMetaDataType.DOUBLE_DATA_TYPE;
      else if (datatype instanceof AS400PackedDecimal)
         return RowMetaDataType.BIG_DECIMAL_DATA_TYPE;
      else if (datatype instanceof AS400Structure)
         return RowMetaDataType.BYTE_ARRAY_DATA_TYPE;
      else if (datatype instanceof AS400Array)
         return RowMetaDataType.BYTE_ARRAY_DATA_TYPE;
      else if (datatype instanceof AS400Text)
         return RowMetaDataType.STRING_DATA_TYPE;
      else if (datatype instanceof AS400UnsignedBin2)
         return RowMetaDataType.INTEGER_DATA_TYPE;
      else if (datatype instanceof AS400UnsignedBin4)
         return RowMetaDataType.LONG_DATA_TYPE;
      else if (datatype instanceof AS400ZonedDecimal)
         return RowMetaDataType.BIG_DECIMAL_DATA_TYPE;
      else
         return RowMetaDataType.BYTE_ARRAY_DATA_TYPE;
   }

   /**
   *  Returns the data type name of column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column data type name.
   **/
   public String getColumnTypeName(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the column type name");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Get the data type from the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      AS400DataType datatype = fd.getDataType();

      if (datatype instanceof AS400Bin2)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.SHORT_DATA_TYPE);
      else if (datatype instanceof AS400Bin4)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.INTEGER_DATA_TYPE);
      else if (datatype instanceof AS400ByteArray)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BYTE_ARRAY_DATA_TYPE);
      else if (datatype instanceof AS400Float4)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.FLOAT_DATA_TYPE);
      else if (datatype instanceof AS400Float8)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.DOUBLE_DATA_TYPE);
      else if (datatype instanceof AS400PackedDecimal)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BIG_DECIMAL_DATA_TYPE);
      else if (datatype instanceof AS400Structure)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BYTE_ARRAY_DATA_TYPE);
      else if (datatype instanceof AS400Array)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BYTE_ARRAY_DATA_TYPE);
      else if (datatype instanceof AS400Text)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.STRING_DATA_TYPE);
      else if (datatype instanceof AS400UnsignedBin2)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.INTEGER_DATA_TYPE);
      else if (datatype instanceof AS400UnsignedBin4)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.LONG_DATA_TYPE);
      else if (datatype instanceof AS400ZonedDecimal)
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BIG_DECIMAL_DATA_TYPE);
      else
         return RowMetaDataType.getDataTypeName(RowMetaDataType.BYTE_ARRAY_DATA_TYPE);
   }

   /**
   *  Returns the precision of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column precision (number of decimal digits).
   **/
   public int getPrecision(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the precision");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Get the data type from the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      AS400DataType datatype = fd.getDataType();

      if (datatype instanceof AS400ZonedDecimal)
         return ((AS400ZonedDecimal)datatype).getNumberOfDigits();
      else if (datatype instanceof AS400PackedDecimal)
         return ((AS400PackedDecimal)datatype).getNumberOfDigits();
      else
         return 0;
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
   *  Returns the scale of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column scale (number of digits to the right of the decimal point).
   **/
   public int getScale(int columnIndex)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to get the scale");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      // Get the data type from the field description.
      FieldDescription fd = recordFormat_.getFieldDescription(columnIndex);
      AS400DataType datatype = fd.getDataType();

      if (datatype instanceof AS400ZonedDecimal)
         return ((AS400ZonedDecimal)datatype).getNumberOfDecimalPositions();
      else if (datatype instanceof AS400PackedDecimal)
         return ((AS400PackedDecimal)datatype).getNumberOfDecimalPositions();
      else
         return 0;
   }

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains numeric data.
   *  @param columnIndex The column index (0-based).
   *  @return true if numeric data; false otherwise.
   **/
   public boolean isNumericData(int columnIndex)       // @A1
   {
      return RowMetaDataType.isNumericData(getColumnType(columnIndex));
   }

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains text data.
   *  @param columnIndex The column index (0-based).
   *  @return true if text data; false otherwise.
   **/
   public boolean isTextData(int columnIndex)
   {
      return RowMetaDataType.isTextData(getColumnType(columnIndex));
   }

   /**
   *  Deserializes and initializes transient data.
   **/
   private void readObject(java.io.ObjectInputStream in)
       throws java.io.IOException, ClassNotFoundException, RowDataException
   {
      in.defaultReadObject();

      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);

      if (recordFormat_ != null)
      {
         try
         {
            setRecordFormat(recordFormat_);
         }
         catch (PropertyVetoException e) { /* do nothing. */ }
      }
   }

   /**
   *  Removes the PropertyChangeListener from the internal list.
   *  If the PropertyChangeListener is not on the list, nothing is done.
   *  @see #addPropertyChangeListener
   *  @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");

      changes_.removePropertyChangeListener(listener);
   }

   /**
   *  Removes the VetoableChangeListener from the internal list.
   *  If the VetoableChangeListener is not on the list, nothing is done.
   *  @see #addVetoableChangeListener
   *  @param listener The VetoableChangeListener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");

      vetos_.removeVetoableChangeListener(listener);
   }

   /**
   *  Sets the specified <i>label</i> at the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @param label The label.
   **/
   public void setColumnLabel(int columnIndex, String label)
   {
      // Validate that the record format is set.
      validateRecordFormat("Attempting to set the column label");

      // Validate the label parameter.
      if (label == null)
         throw new NullPointerException("label");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      columnLabel_[columnIndex] = label;
   }

   /**
   *  Sets the record format.
   *  @param recordFormat The record format.
   *  @exception PropertyVetoException If a change is vetoed.
   **/
   public void setRecordFormat(RecordFormat recordFormat) throws PropertyVetoException
   {
      // Validate the format parameter.
      if (recordFormat == null)
         throw new NullPointerException("recordFormat");

      RecordFormat old = recordFormat_;
      vetos_.fireVetoableChange("recordFormat", old, recordFormat);

      recordFormat_ = recordFormat;

      changes_.firePropertyChange("recordFormat", old, recordFormat);

      // Initialize the label array.
      columnLabel_ = new String[getColumnCount()];
   }

   /**
   *  Validates the column index.
   *  @param columnIndex The column index.
   **/
   private void validateColumnIndex(int columnIndex)
   {
      if ( (columnIndex < 0) || (columnIndex >= recordFormat_.getNumberOfFields()) )
         throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
   }

   /**
   *  Validates the record format.
   *  @param action The attempted action.
   **/
   private void validateRecordFormat(String action)
   {
      if (recordFormat_ == null)
      {
         Trace.log(Trace.ERROR, action + " before setting the record format.");
         throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
   }
}
