///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceMetaData;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.Presentation;


class ResourceListMetaData implements RowMetaData, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private ResourceList  resourceList_;           // The resource list.
   private Object[]      columnAttributeIDs_;     // The column attributes.
   private String[]      columnLabel_;            // The array of column labels.


   /**
    *  Constructs a default ResourceListMetaData object.
   **/
   public ResourceListMetaData()
   {
      super();
   }


   /**
   *  Constructs a ResourceListMetaData object with the specified <i>resourceList</i> and <i>columnAttributeIDs</i>.
   *  @param resourceList The resource list.
   *  @param columnAttributeIDs The array of column attribute IDs.
   *  @see com.ibm.as400.resource.ResourceList
   **/    
   public ResourceListMetaData(ResourceList resourceList, Object[] columnAttributeIDs)
   {
      this();

      setResourceList(resourceList);
      setColumnAttributeIDs(columnAttributeIDs);
   }


   /**
    *  Returns the number of columns.
    *  @return The column count.
    **/
   public int getColumnCount()
   {
      return columnAttributeIDs_.length;
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

      if (columnAttributeIDs_[columnIndex] == null)
         return 20;
      else 
      {
         int dataWidth = 20;
         ResourceMetaData attributeMetaData = resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]);
         Class type = attributeMetaData.getType();
         Presentation[] possibleValuePresentations = attributeMetaData.getPossibleValuePresentations();
         
         // If it is a numeric value, use 10.
         if (Number.class.isAssignableFrom(type))
            dataWidth = 10;
         
         // If it has possible values, use the width of the longest.
         else if (possibleValuePresentations.length > 0) 
         {
            dataWidth = 1;
            for(int i = 0; i < possibleValuePresentations.length; ++i) 
            {
               int nameLength = possibleValuePresentations[i].getName().toString().length();
               if (nameLength > dataWidth)
                  dataWidth = nameLength;
            }
         }
         
         // Make sure its big enough to show the header.
         int nameLength = attributeMetaData.getPresentation().getName().toString().length();
         if (nameLength > dataWidth)
            dataWidth = nameLength;
         
         return dataWidth;
      }
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
      
      if (columnLabel_ != null && columnLabel_[columnIndex] != null)
      {  
         return columnLabel_[columnIndex];
      }
      else
      {
         if (columnAttributeIDs_[columnIndex] == null)
         {
            ResourceBundleLoader_s loader = new ResourceBundleLoader_s();
            return loader.getText("PROP_NAME_RL_NAME");
         }
         else
            return resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]).getPresentation().getFullName();
      }
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
      
      if (columnAttributeIDs_[columnIndex] != null)
         return resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]).getPresentation().getName();
      else
      {
         ResourceBundleLoader_s loader = new ResourceBundleLoader_s();
         return loader.getText("PROP_NAME_RL_NAME");
      }
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
      
      if (columnAttributeIDs_[columnIndex] == null)
         return RowMetaDataType.STRING_DATA_TYPE;

      Class type = resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]).getType();

      if (type == byte[].class)
         return RowMetaDataType.BYTE_ARRAY_DATA_TYPE;
      else if (type == java.math.BigDecimal.class)
         return RowMetaDataType.BIG_DECIMAL_DATA_TYPE;
      else if ((type == Double.class) || (type == Double.TYPE))
         return RowMetaDataType.DOUBLE_DATA_TYPE;
      else if ((type == Float.class) || (type == Float.TYPE))
         return RowMetaDataType.FLOAT_DATA_TYPE;
      else if ((type == Integer.class) || (type == Integer.TYPE))
         return RowMetaDataType.INTEGER_DATA_TYPE;
      else if ((type == Long.class) || (type == Long.TYPE))
         return RowMetaDataType.LONG_DATA_TYPE;
      else if ((type == Short.class) || (type == Short.TYPE))
         return RowMetaDataType.SHORT_DATA_TYPE;
      else
         return RowMetaDataType.STRING_DATA_TYPE;
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
      
      return RowMetaDataType.getDataTypeName(getColumnType(columnIndex));
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
   public boolean isNumericData(int columnIndex)	
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
    *  Sets the column attribute IDs.
    *  @param columnAttributeIDs The array of column attribute IDs.
    **/
   public void setColumnAttributeIDs(Object[] columnAttributeIDs)
   {
      if (Trace.isTraceOn()) 
         Trace.log(Trace.INFORMATION, "Setting the column attribute IDs.");

      columnAttributeIDs_ = columnAttributeIDs;
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
      
      if (columnLabel_ == null)
         columnLabel_ = new String[getColumnCount()];

      columnLabel_[columnIndex] = label;
   }


   /**
    *  Sets the resource list.
    *  @param resourceList The resource list.
    *  @see com.ibm.as400.resource.ResourceList
    **/
   public void setResourceList(ResourceList resourceList)
   {
      if (Trace.isTraceOn()) 
         Trace.log(Trace.INFORMATION, "Setting the meta resource list.");

      resourceList_ = resourceList;
   }


   /**
    *  Validates the column index.
    *  @param columnIndex The column index (0-based).
    **/
   private void validateColumnIndex(int columnIndex)
   {
      if ( columnIndex < 0 || columnIndex >= columnAttributeIDs_.length ) 
         throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);   
   }

}
