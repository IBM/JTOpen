///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RowMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

/**
*  A RowMetaData object can be used to find out information about the columns
*  of a <A href="RowData.html">RowData</A> object.
**/
public interface RowMetaData
{

   /**
    *  Returns the horizontal alignment of the column data  specified by <i>columnIndex</i>.
    *  For a list of values, see {@link com.ibm.as400.util.html.HTMLConstants HTMLConstants}.
    *  @param columnIndex The column index (0-based).
    *  @return The horizontal column alignment.    One of the following constants
    *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
    *  @exception RowDataException If a row data error occurred.
    **/
    public abstract String getColumnAlignment(int columnIndex) throws RowDataException;           //@D5A

    /**
   *  Returns the number of columns.
   *
   *  @return The number of columns.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract int getColumnCount() throws RowDataException;

   /**
    *  Returns the direction of the column data  specified by <i>columnIndex</i>.
    *  For a list of values, see {@link com.ibm.as400.util.html.HTMLConstants HTMLConstants}.
    *  @param columnIndex The column index (0-based).
    *  @return The direction.
    *  @exception RowDataException If a row data error occurred.
    **/
    public abstract String getColumnDirection(int columnIndex) throws RowDataException;            //@D5A

    /**
   *  Returns the display size in characters of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index (0-based).
   *  @return The display size.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract int getColumnDisplaySize(int columnIndex) throws RowDataException;

   /**
   *  Returns the label of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index(0-based).
   *  @return The label.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract String getColumnLabel(int columnIndex) throws RowDataException;

   /**
   *  Returns the name of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index(0-based).
   *  @return The name.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract String getColumnName(int columnIndex) throws RowDataException;

   /**
   *  Returns the data type of the column specified by <i>columnIndex</i>.
   *  For a list of values, see <a href="RowMetaDataType.html">RowMetaDataType</a>
   *  @param columnIndex The column index (0-based).
   *  @return The data type.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract int getColumnType(int columnIndex) throws RowDataException;

   /**
   *  Returns the data type name of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index (0-based).
   *  @return The data type name.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract String getColumnTypeName(int columnIndex) throws RowDataException;

   /**
   *  Returns the precision of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index (0-based).
   *  @return The precision (number of decimal digits).
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract int getPrecision(int columnIndex) throws RowDataException;

   /**
   *  Returns the scale of the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index (0-based).
   *  @return The scale (number of digits to the right of the decimal point).
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract int getScale(int columnIndex) throws RowDataException;

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains numeric data.
   *  @param columnIndex The column index (0-based).
   *  @return true if numeric data; false otherwise.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract boolean isNumericData(int columnIndex) throws RowDataException;    // @A1

   /**
   *  Indicates if the column specified by <i>columnIndex</i> contains text data.
   *  @param columnIndex The column index (0-based).
   *  @return true if text data; false otherwise.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract boolean isTextData(int columnIndex) throws RowDataException;

   /**
   *  Sets the specified <i>label</i> for the column specified by <i>columnIndex</i>.
   *
   *  @param columnIndex The column index (0-based).
   *  @param label The label.
   *  @exception RowDataException If a row data error occurred.
   **/
   public abstract void setColumnLabel(int columnIndex, String label) throws RowDataException;
}
