///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListRowData.java
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
import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.ResourceMetaData;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeSupport;
import java.util.Vector;


/**
 * The ResourceListRowData class represents a resource list of data.
 * <P>The list of data is formatted into a series of rows where each row
 * contains a finite number of columns determined by the number of column
 * attribute ID's.  Each column within a row contains an individual data item.
 *
 * <P>A ResourceListRowData object can represent any implementation of the
 * {@link com.ibm.as400.resource.ResourceList ResourceList} interface.  The row data
 * columns are specified as an array of column attribute IDs. The row data will contain a column
 * for each element of the array. The following can be specified as column attribute IDs:
 * <P>
 * <UL>
 *   <LI>null - The name from each Resource's Presentation object are presented in the column.
 *   <P>
 *   <LI>Resource attribute IDs - These are defined by the Resource objects that make up the rows data.
 *   The corresponding attribute value is presented in the column.
 * </UL>
 * <P>A ResourceListRowData object maintains a position in the resource list
 * that points to its current row of data.  The initial position in the list
 * is set before the first row.  The <i>next</i> method moves to the next row in the list.
 *
 * <P>The <i>getObject</i> method is used to retrieve the column value for
 * the current row indexed by the column number.  Columns are numbered
 * starting from 0.
 *
 * <P>The following example creates a ResourceListRowData object using an RUserList:
 * <P><BLOCKQUOTE><PRE>
 * <P>          // Create an object to represent the server system.
 * AS400 mySystem = new AS400("mySystem.myCompany.com");
 * <P>          // Create a resource user list.
 * RUserList userList = new RUserList(sys);
 * <P>          // Set the selection so that all user profiles
 *              // are included in the list.
 * userList.setSelectionValue(RUserList.SELECTION_CRITERIA, RUserList.ALL);
 * <P>          // Create an HTMLTableConverter object.
 * HTMLTableConverter converter = new HTMLTableConverter();
 *              // Set up the table tag with a maximum of 20 rows per table.
 * converter.setMaximumTableSize(20);
 * <P>          // Create an HTMLTable and use the meta data for the table headers.
 * HTMLTable table = new HTMLTable();
 * table.setCellSpacing(6);
 * table.setBorderWidth(8);
 * converter.setTable(table);
 * converter.setUseMetaData(true);
 * <P>          // Create a ResourceListRowData.
 * ResourceListRowData rowdata = new ResourceListRowData(userList, new Object[] { null, RUser.TEXT_DESCRIPTION } );
 * <P>          // Convert the ResourceListRowData into an HTMLTable.
 * String[] html = converter.convert(rowdata);
 * <P>          // Print out the first table of 20 users from the html array.
 * System.out.println(html[0]);
 *
 * </PRE></BLOCKQUOTE></P>
 */
public class ResourceListRowData extends RowData implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  static final long serialVersionUID = 1081925300369525536L;

   private ResourceListMetaData  metadata_;               // The metadata.
   private ResourceList          resourceList_;           // The resource list.
   private Object[]              columnAttributeIDs_;     // The column attributes.
   private Exception             lastException_;          // Keeps track of exceptions.
   private boolean               refreshed_;


   /**
    *  Constructs a default ResourceListRowData object.
    **/
   public ResourceListRowData()
   {
      super();
      setColumnAttributeIDs(new Object[] {});
   }

   /**
    *  Constructs a ResourceListRowData object with the specified <i>resourceList</i> and
    *  <i>columnAttributeIDs</i>.
    *
    *  @param resourceList The resource list.
    *  @param columnAttributeIDs The array of column attributes.
    *  @see com.ibm.as400.resource.ResourceList
    **/
   public ResourceListRowData(ResourceList resourceList, Object[] columnAttributeIDs)
   {
      this();

      setMetaData(resourceList, columnAttributeIDs);
      setResourceList(resourceList);
      setColumnAttributeIDs(columnAttributeIDs);
   }


   /**
    *  Returns the array of column attribute IDs.
    *  @return The column attribute IDs.
    **/
   public Object[] getColumnAttributeIDs()
   {
      return columnAttributeIDs_;
   }


   /**
    *  Return the resource list length.
    *  @return The list length.
    **/
   int getListLength()
   {
      try
      {
         if (resourceList_ == null)
            return 0;

         if (!refreshed_)
         {
            resourceList_.refreshStatus();
            refreshed_ = true;
         }
         return (int)resourceList_.getListLength();
      }
      catch(ResourceException e)
      {
         Trace.log(Trace.ERROR, "Error getting resource length.", e);
         lastException_ = e;

         return 0;
      }
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
    *  Returns the current row's column data specified at <i>columnIndex</i>.
    *
    *  @param columnIndex The column index (0-based).
    *  @return The column object.
    *  @exception RowDataException If a row data error occurs.
    **/
   public Object getObject(int columnIndex) throws RowDataException
   {
      try
      {
         if (lastException_ != null)
            throw new RowDataException(lastException_);

         // Validate that there are column attribute IDs.
         if (columnAttributeIDs_ == null)
            throw new NullPointerException("columnAttributeIDs");
         if (columnAttributeIDs_.length == 0)
         {
            Trace.log(Trace.ERROR, "Attempting to get the column object before setting the column attribute IDs.");
            throw new ExtendedIllegalStateException("columnAttributeIDs", ExtendedIllegalStateException.PROPERTY_NOT_SET);
         }

         // Validate that the list is not empty.
         validateRowList("Attempting to get the column object");

         // Get the current row.
         validateListPosition("Attempting to get the current object");

         // Validate the column parameter.
         if ( columnIndex < 0 || columnIndex >= columnAttributeIDs_.length )
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

         resourceList_.open();

         if (!resourceList_.isResourceAvailable(columnIndex))    //$A2C
             resourceList_.waitForResource(columnIndex);         //$A2C

         // If the user specifies a null value in the columnAttributeIDs array, display the resource name.
         if ( columnAttributeIDs_[columnIndex] == null)
            return resourceList_.resourceAt(position_).getPresentation().getName();
         else
         {
            ResourceMetaData meta = resourceList_.getAttributeMetaData(columnAttributeIDs_[columnIndex]);                     //$A1A
            Object attributeValue = resourceList_.resourceAt(position_).getAttributeValue(columnAttributeIDs_[columnIndex]);  //$A1A

            // This will allow us to use the MRI attribute values.                                                            //$A1A
            if (meta.getPossibleValuePresentation(attributeValue) != null)                                                    //$A1A
               return meta.getPossibleValuePresentation(attributeValue).getName();                                            //$A1A
            else                                                                                                              //$A1A
               return attributeValue;                                                                                         //$A1C
         }
      }
      catch(ResourceException e)
      {
         throw new RowDataException(e);
      }
   }


   /**
    *  Returns the data object's property list at the specified <i>columnIndex</i>.
    *
    *  @param columnIndex The column index (0-based).
    *  @return The property list for the column data object.
    *  @see com.ibm.as400.util.servlet.RowData#setObjectProperties
    **/
   public Vector getObjectProperties(int columnIndex)
   {
      // Validate that the list is not empty.
      validateRowList("Attempting to get the column object's properties");

      // Get the current row.
      validateListPosition("Attempting to get the current object's properties");

      // Validate the columnIndex parameter.
      if ( columnIndex < 0 || columnIndex >= columnAttributeIDs_.length )
         throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      return new Vector();
   }


   /**
    *  Return the resource list.
    *  @return The resource list.
    **/
   public ResourceList getResourceList()
   {
      return resourceList_;
   }


   /**
    *  Returns the number of rows in the resource list.
    *
    *  @return The number of rows.
    **/
   public int length()
   {
      return getListLength();
   }


   /**
    *  Sets the resource list column attribute IDs.
    *
    *  @param columnAttributeIDs The column attribute IDs.
    **/
   public void setColumnAttributeIDs(Object[] columnAttributeIDs)
   {

      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Setting the column attribute IDs.");

      if (columnAttributeIDs == null)
         throw new NullPointerException("columnAttributeIDs");

      Object[] old = columnAttributeIDs_;

      columnAttributeIDs_ = columnAttributeIDs;

      if (changes_ != null) changes_.firePropertyChange("columnAttributeIDs", old, columnAttributeIDs); //@CRS

      if (metadata_ == null)
         metadata_ = new ResourceListMetaData();

      metadata_.setColumnAttributeIDs(columnAttributeIDs);

   }

   /**
    *  Sets the metadata.
    *  @param resourceList The resource list.
    *  @param columnAttributeIDs The column attributes.
    **/
   void setMetaData(ResourceList resourceList, Object[] columnAttributeIDs)
   {
      // Validate the metadata parameter.
      if (resourceList == null)
         throw new NullPointerException("resourceList");
      if (columnAttributeIDs == null)
         throw new NullPointerException("columnAttributeIDs");
      if (columnAttributeIDs.length == 0)
         throw new ExtendedIllegalArgumentException("columnAttributeIDs", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      ResourceListMetaData old = metadata_;

      metadata_ = new ResourceListMetaData(resourceList, columnAttributeIDs);

      if (changes_ != null) changes_.firePropertyChange("metadata", old, metadata_); //@CRS
   }


   /**
    *  Sets the resource list.
    *
    *  @param resourceList The resource list.
    *  @see com.ibm.as400.resource.ResourceList
    **/
   public void setResourceList(ResourceList resourceList)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Setting the resource list.");

      if (resourceList == null)
         throw new NullPointerException("resourceList");

      ResourceList old = resourceList_;

      resourceList_ = resourceList;

      if (changes_ != null) changes_.firePropertyChange("resourceList", old, resourceList); //@CRS

      if (metadata_ == null)
         metadata_ = new ResourceListMetaData();

      metadata_.setResourceList(resourceList);
   }
}
