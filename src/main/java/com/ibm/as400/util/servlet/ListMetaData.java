///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ListMetaData.java
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
import com.ibm.as400.util.html.HTMLConstants;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Serializable;

/**
*  A ListMetaData object can be used to find out information about the columns of a
*  <A href="ListRowData.html">ListRowData</A> object.
*
*  <P>ListMetaData objects generate the following events:
*  <UL>
*  <LI>PropertyChangeEvent</LI>
*  <LI>VetoableChangeEvent</LI>
*  </UL>
**/
public class ListMetaData implements RowMetaData, Serializable
{
  static final long serialVersionUID = -230291530342866669L;

   // metadata values.
   private int columnCount_ = 0;       // The number of columns.

   private String[] columnName_;       // The array of column names.
   private String[] columnLabel_;      // The array of column labels.
   private int[] columnType_;          // The array of column types.
   private int[] columnSize_;          // The array of column sizes.

    private String[] columnAlignment_;  // The array of column alignments.  @D5A
    private String[] columnDirection_;   // The array of column alignments.  @D5A

   transient private PropertyChangeSupport changes_; //@CRS
   transient private VetoableChangeSupport vetos_; //@CRS

   /**
   *  Constructs a default ListMetaData object.
   **/
   public ListMetaData()
   {
   }

   /**
   *  Constructs a ListMetaData object with the specified number of <i>columns</i>.
   *  @param columns The number of columns.
   **/
   public ListMetaData(int columns)
   {
      this();
      try
      {
         setColumns(columns);
      }
      catch (PropertyVetoException e) { /* This will never happen. */ }
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
      if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS
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
      if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
      vetos_.addVetoableChangeListener(listener);
   }

   /**
   *  Returns the number of columns.
   *  @return The column count.
   **/
   public int getColumnCount()
   {
      return columnCount_;
   }

   /**
   *  Returns the display size in characters of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column display size in characters.
   **/
   public int getColumnDisplaySize(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return columnSize_[columnIndex];
   }

   /**
   *  Returns the label of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column label.
   **/
   public String getColumnLabel(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return columnLabel_[columnIndex];
   }

   /**
   *  Returns the name of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column name.
   **/
   public String getColumnName(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return columnName_[columnIndex];
   }


   /**
   *  Returns the data type of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column RowMetaDataType.
   *  @see com.ibm.as400.util.servlet.RowMetaDataType
   **/
   public int getColumnType(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return columnType_[columnIndex];
   }


   /**
    *  Returns the alignment of the column specified by <i>columnIndex</i>.
    *  @param columnIndex The column index (0-based).
    *  @return The horizontal column alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getColumnAlignment(int columnIndex)        //@D5A
    {
        // Validate the column parameter.
        validateColumnIndex(columnIndex);

        return columnAlignment_[columnIndex];
    }


    /**
    *  Returns the direction of the column specified by <i>columnIndex</i>.
    *  @param columnIndex The column index (0-based).
    *  @return The column direction.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getColumnDirection(int columnIndex)        //@D5A
    {
        // Validate the column parameter.
        validateColumnIndex(columnIndex);

        return columnDirection_[columnIndex];
    }


    /**
   *  Returns the data type name of the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @return The column data type name.
   **/
   public String getColumnTypeName(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      if (columnType_[columnIndex] != 0)
         return RowMetaDataType.getDataTypeName(columnType_[columnIndex]);
      else
         return null;
   }

   /**
   *  Returns the precision of the column specified by <i>columnIndex</i>.
   *  This value is not used.  It will always return zero.
   *  @param columnIndex The column index (0-based).
   *  @return The column precision (number of decimal digits).
   **/
   public int getPrecision(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return 0;
   }

   /**
   *  Returns the scale of the column specified by <i>columnIndex</i>.
   *  This value is not used.  It will always return zero.
   *  @param columnIndex The column index (0-based).
   *  @return The column scale (number of digits to the right of the decimal point).
   **/
   public int getScale(int columnIndex)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      return 0;
   }

   /**
   *  Indicates if the data type is numeric data.
   *  @param columnIndex The column index (0-based).
   *  @return true if numeric data; false otherwise.
   **/
   public boolean isNumericData(int columnIndex)       // @A1
   {
      return RowMetaDataType.isNumericData(getColumnType(columnIndex));
   }

   /**
   *  Indicates if the data type is text data.
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
       throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      //@CRS changes_ = new PropertyChangeSupport(this);
      //@CRS vetos_ = new VetoableChangeSupport(this);
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

      if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS
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

      if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS
   }

    /**
    *  Sets the specified horizontal <i>alignment</i> for the column data specified by <i>columnIndex</i>.
    *  @param columnIndex The column index (0-based).
    *  @param alignment The horizontal column alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setColumnAlignment(int columnIndex, String alignment)        //@D5A
    {
        validateColumnIndex(columnIndex);

        // If align is not one of the valid HTMLConstants, throw an exception.
        if (alignment == null)
            throw new NullPointerException("alignment");
        else if ( !(alignment.equals(HTMLConstants.LEFT))  && !(alignment.equals(HTMLConstants.RIGHT)) && !(alignment.equals(HTMLConstants.CENTER)) && !(alignment.equals(HTMLConstants.JUSTIFY)) )
            throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        columnAlignment_[columnIndex] = alignment;
    }

    /**
    *  Sets the specified <i>direction</i> for the column data specified by <i>columnIndex</i>.
    *  @param columnIndex The column index (0-based).
    *  @param dir The column direction.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setColumnDirection(int columnIndex, String dir)         //@D5A
    {
        validateColumnIndex(columnIndex);

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        columnDirection_[columnIndex] = dir;
    }

   /**
   *  Sets the specified <i>displaySize</i> for the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @param displaySize The column display size in characters.
   **/
   public void setColumnDisplaySize(int columnIndex, int displaySize)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      // Validate the displaySize parameter.
      if (displaySize < 1)
         throw new ExtendedIllegalArgumentException("displaySize", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      columnSize_[columnIndex] = displaySize;
   }

   /**
   *  Sets the specified <i>label</i> for the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @param label The column label.
   **/
   public void setColumnLabel(int columnIndex, String label)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      // Validate the label parameter.
      if (label == null)
         throw new NullPointerException("label");

      columnLabel_[columnIndex] = label;
   }

   /**
   *  Sets the specified <i>name</i> for the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @param name The column name.
   **/
   public void setColumnName(int columnIndex, String name)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      // Validate the name parameter.
      if (name == null)
         throw new NullPointerException("name");

      columnName_[columnIndex] = name;

      // Set the column label as well.
      if (columnLabel_[columnIndex] == null)
      {
         columnLabel_[columnIndex] = name;
      }
   }

   /**
   *  Sets the number of columns.  Any previous column information is cleared.
   *  @param columns The number of columns.
   *  @exception PropertyVetoException If a change is vetoed.
   **/
   public void setColumns(int columns) throws PropertyVetoException
   {
      if (columns <= 0)
         throw new ExtendedIllegalArgumentException("columns", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      //@CRS Integer oldInt = new Integer(columnCount_);
      //@CRS Integer newInt = new Integer(columns);
      int oldInt = columnCount_; //@CRS

      if (vetos_ != null) vetos_.fireVetoableChange("columns", new Integer(oldInt), new Integer(columns)); //@CRS

      columnCount_ = columns;

      columnName_ = new String[columnCount_];
      columnLabel_ = new String[columnCount_];
      columnType_ = new int[columnCount_];
      columnSize_ = new int[columnCount_];

      columnAlignment_ = new String[columnCount_];        //@D5A
      columnDirection_ = new String[columnCount_];         //@D4A

      if (changes_ != null) changes_.firePropertyChange("columns", new Integer(oldInt), new Integer(columns)); //@CRS
   }

   /**
   *  Sets the specified data <i>type</i> for the column specified by <i>columnIndex</i>.
   *  @param columnIndex The column index (0-based).
   *  @param type The RowMetaDataType type.
   *  @see com.ibm.as400.util.servlet.RowMetaDataType
   **/
   public void setColumnType(int columnIndex, int type)
   {
      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      // Validate the type parameter.
      if (RowMetaDataType.isDataTypeValid(type))
         columnType_[columnIndex] = type;
      else
         throw new ExtendedIllegalArgumentException("type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
   }

   /**
   *  Validates the column index.
   *  @param columnIndex The column index (0-based).
   **/
   private void validateColumnIndex(int columnIndex)
   {
      if ( columnIndex < 0 || columnIndex >= columnCount_ )
         throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
   }
}
