///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLResultSetMetaData.java
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
*  An SQLResultSetMetaData object can be used to find out information about the columns of
*  an <A href="SQLResultSetRowData.html">SQLResultSetRowData</A> object.
*
*  <P>
*  Serializing the object results in the metadata being cached with the object.
*  After deserialization the cached data is used until the metadata is reset using
*  the setMetaData method.
*
*  <P>SQLResultSetMetaData objects generate the following events:
*  <UL>
*  <LI>PropertyChangeEvent</LI>
*  <LI>VetoableChangeEvent</LI>
*  </UL>
**/
public class SQLResultSetMetaData implements RowMetaData, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  static final long serialVersionUID = 6743260415904796964L;

   transient private ResultSetMetaData metadata_;     // The result set metadata.
   private String[] columnLabels_;                       // The column label list.
   private boolean isCached_ = false;                      // Indicates if the metadata is cached or
                                                      // if the ResultSetMetaData object is used.

   // metadata information cache
   private int columnCount_ = -1;                        // The number of columns.
   private int[] columnDisplaySize_;                       // The column display size.
   private String[] columnName_;                            // The column name.
   private int[] columnType_;                               // The column type.
   private String[] columnTypeName_;                       // The column type name.
   private int[] columnPrecision_;                       // The column precision.
   private int[] columnScale_;                                // The column scale.

   transient private PropertyChangeSupport changes_; //@CRS
   transient private VetoableChangeSupport vetos_; //@CRS

    private String[] columnAlignment_;  // The array of column alignments.  @D5A
    private String[] columnDirection_;   // The array of column alignments.  @D5A
   /**
     Constructs a default SQLResultSetMetaData object.
   **/
   public SQLResultSetMetaData()
   {
   }

   /**
     Constructs an SQLResultSetMetaData object with the specified <i>metadata</i>.
     @param metadata The metadata.
     @exception RowDataException If a row data error occurs.
   **/
   public SQLResultSetMetaData(ResultSetMetaData metadata) throws RowDataException
   {
      // Set the metadata parameter.
      try
      {
         setMetaData(metadata);
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
     *  Returns the alignment of the column specified by <i>columnIndex</i>.
     *  @param columnIndex The column index (0-based).
     *  @return The horizontal column alignment.  One of the following constants
     *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
     *  @see com.ibm.as400.util.html.HTMLConstants
     *  @exception RowDataException If a row data error occurs.
     **/
    public String getColumnAlignment(int columnIndex) throws RowDataException       //@D5A
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
    *  @exception RowDataException If a row data error occurs.
    **/
    public String getColumnDirection(int columnIndex) throws RowDataException       //@D5A
    {
        // Validate the column parameter.
        validateColumnIndex(columnIndex);

        return columnDirection_[columnIndex];
    }

   /**
     Returns the number of columns in the result set.
     @return The number of columns.
     @exception RowDataException If a row data error occurs.
   **/
   public int getColumnCount() throws RowDataException
   {
      try
      {
         if (isCached_)
            return columnCount_;
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the column count");
            return metadata_.getColumnCount();
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the display size in characters of the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column display size in characters.
     @exception RowDataException If a row data error occurs.
   **/
   public int getColumnDisplaySize(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnDisplaySize_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the column display size");
            return metadata_.getColumnDisplaySize(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the label of the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column label.
     @exception RowDataException If a row data error occurs.
   **/
   public String getColumnLabel(int columnIndex) throws RowDataException
   {
      // Validate the metadata.
      if (!isCached_)
         validateMetaData("Attempting to get the column label");

      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      return columnLabels_[columnIndex];
   }

   /**
     Returns the name of the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column name.
     @exception RowDataException If a row data error occurs.
   **/
   public String getColumnName(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnName_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the column name");
            return metadata_.getColumnName(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the SQL data type of the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column SQL type (see java.sql.Types).
     @exception RowDataException If a row data error occurs.
   **/
   public int getColumnType(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnType_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the column type");
            return metadata_.getColumnType(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the data type name of the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column data type name.
     @exception RowDataException If a row data error occurs.
   **/
   public String getColumnTypeName(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnTypeName_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the column type name");
            return metadata_.getColumnTypeName(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the result set metadata.
     @return The metadata.
   **/
   public ResultSetMetaData getMetaData()
   {
      return metadata_;
   }

   /**
     Returns the number of decimal digits for the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column precision (number of decimal digits).
     @exception RowDataException If a row data error occurs.
   **/
   public int getPrecision(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnPrecision_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the precision");
            return metadata_.getPrecision(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
     Returns the number of digits to the right of the decimal point for the column
     specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @return The column scale (number of decimal digits to the right of the decimal point).
     @exception RowDataException If a row data error occurs.
   **/
   public int getScale(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      try
      {
         if (isCached_)
            return columnScale_[columnIndex];
         else
      {
            // Validate the metadata.
            validateMetaData("Attempting to get the scale");
            return metadata_.getScale(columnIndex + 1);
      }
      }
      catch (SQLException e)
      {
         Trace.log(Trace.INFORMATION, "Rethrowing SQLException.");
         throw new RowDataException(e);
      }
   }

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains numeric data.
   *  @param columnIndex The column index (0-based).
   *  @return true if numeric data; false otherwise.
   *  @exception RowDataException If a row data error occurs.
   **/
   public boolean isNumericData(int columnIndex) throws RowDataException   // @A1
   {
      // validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      switch (getColumnType(columnIndex))
      {
      case Types.BIGINT:
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.INTEGER:
      case Types.NUMERIC:
      case Types.REAL:
      case Types.SMALLINT:
      case Types.TINYINT:
          return true;
      case Types.OTHER:     //determine if we are a DECFLOAT
          if(getColumnTypeName(columnIndex).equals("DECFLOAT"))
              return true;
          else
              return false;
      default:
          return false;
      }
   }

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains text data.
   *  @param columnIndex The column index (0-based).
   *  @return true if text data; false otherwise.
   *  @exception RowDataException If a row data error occurs.
   **/
   public boolean isTextData(int columnIndex) throws RowDataException
   {
      // Validate the columnIndex parameter.
      validateColumnIndex(columnIndex);

      switch(getColumnType(columnIndex))
      {
      case Types.CHAR:
      case Types.DATE:
      case Types.LONGVARCHAR:
      case Types.TIME:
      case Types.TIMESTAMP:
      case Types.VARCHAR:
         return true;
      default:
         return false;
      }
   }

   /**
   *  Deserializes the object and initializes the transient data.
   **/
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      metadata_ = null;            // use cached data until reset.

      // Setup the listeners.
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
   *  Sets the result set metadata.
   *  @param metadata The metadata.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/
   public void setMetaData(ResultSetMetaData metadata) throws PropertyVetoException, RowDataException
   {
      // Validate the metadata parameter.
      if (metadata == null)
         throw new NullPointerException("metadata");

      ResultSetMetaData old = metadata_;
      if (vetos_ != null) vetos_.fireVetoableChange("metadata", old, metadata); //@CRS

      try
      {
         metadata_ = metadata;
            int count = getColumnCount();                                          //@D5A
            columnLabels_ = new String[count];                                 //@D5C
         for (int i=0; i< columnLabels_.length; i++)
            columnLabels_[i] = metadata_.getColumnLabel(i+1);

            columnAlignment_ = new String[count];                             //@D5A
            columnDirection_ = new String[count];                              //@D5A
      }
      catch (SQLException e)
      {
         throw new RowDataException(e);
      }

      if (changes_ != null) changes_.firePropertyChange("metadata", old, metadata); //@CRS

      // clear the cached data.
      if (isCached_)
      {
         columnCount_ = -1;
         columnDisplaySize_ = null;
         columnName_ = null;
         columnType_ = null;
         columnTypeName_ = null;
         columnPrecision_ = null;
         columnScale_ = null;
      isCached_ = false;      // Use metadata information instead of cache.
      }
   }


    /**
     *  Sets the specified horizontal <i>alignment</i> for the column data specified by <i>columnIndex</i>.
     *  @param columnIndex The column index (0-based).
     *  @param alignment The horizontal column alignment.  One of the following constants
     *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
     *  @see com.ibm.as400.util.html.HTMLConstants
     *  @exception RowDataException If a row data error occurs.
     **/
    public void setColumnAlignment(int columnIndex, String alignment) throws RowDataException        //@D5A
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
    *  @exception RowDataException If a row data error occurs.
    **/
    public void setColumnDirection(int columnIndex, String dir) throws RowDataException         //@D5A
    {
        validateColumnIndex(columnIndex);

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        columnDirection_[columnIndex] = dir;
    }

   /**
     Sets the specified <i>label</i> at the column specified by <i>columnIndex</i>.
     @param columnIndex The column index (0-based).
     @param label The label.
     @exception RowDataException If a row data error occurs.
   **/
   public void setColumnLabel(int columnIndex, String label) throws RowDataException
   {
      // Validate the label parameter.
      if (label == null)
         throw new NullPointerException("label");

      // Validate the column parameter.
      validateColumnIndex(columnIndex);

      columnLabels_[columnIndex] = label;
   }

   /**
   *  Validates the column index.
   *  @param columnIndex The column index.
   **/
   private void validateColumnIndex(int columnIndex) throws RowDataException
   {
      if ( (columnIndex < 0) || (columnIndex >= getColumnCount()) )
         throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
   }

   /**
   *  Validates the metadata.
   *  @param action The attempted action on the metadata.
   **/
   private void validateMetaData(String action)
   {
      if (metadata_ == null)
      {
         Trace.log(Trace.ERROR, action + " before setting the metadata.");
         throw new ExtendedIllegalStateException("metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
   }

   /**
   *  Serializes the metadata information.
   *  @param out The output stream.
   *  @exception IOException If a file I/O error occurs.
   *  @exception RowDataException If a rowdata error occurs.
   **/
   private void writeObject(ObjectOutputStream out) throws IOException, RowDataException
   {
      // cache the metadata information.
      if (metadata_ != null)
      {
         columnCount_ = getColumnCount();

         columnDisplaySize_ = new int[columnCount_];
         columnName_ = new String[columnCount_];
         columnType_ = new int[columnCount_];
         columnTypeName_ = new String[columnCount_];
         columnPrecision_ = new int[columnCount_];
         columnScale_ = new int[columnCount_];
            columnAlignment_ = new String[columnCount_];        //@D5A
            columnDirection_ = new String[columnCount_];         //@D5A

         for (int columnIndex = 0; columnIndex < columnCount_; columnIndex++)
         {
            columnDisplaySize_[columnIndex] = getColumnDisplaySize(columnIndex);
            columnName_[columnIndex] = getColumnName(columnIndex);
            columnType_[columnIndex] = getColumnType(columnIndex);
            columnTypeName_[columnIndex] = getColumnTypeName(columnIndex);
            columnPrecision_[columnIndex] = getPrecision(columnIndex);
            columnScale_[columnIndex] = getScale(columnIndex);
                columnAlignment_[columnIndex] = getColumnAlignment(columnIndex);        //@D5A
                columnDirection_[columnIndex] = getColumnDirection(columnIndex);          //@D5A
         }
      isCached_ = true;            // data has now been cached.
      }

      // Serialize the object.
      out.defaultWriteObject();
   }
}
