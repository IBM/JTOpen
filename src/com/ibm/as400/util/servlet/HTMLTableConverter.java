///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLTableConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.util.html.HTMLHyperlink;
import com.ibm.as400.util.html.HTMLTable;
import com.ibm.as400.util.html.HTMLTableCell;
import com.ibm.as400.util.html.HTMLTableHeader;
import com.ibm.as400.util.html.HTMLTableRow;
import com.ibm.as400.util.html.HTMLTagElement;
import com.ibm.as400.util.html.HTMLText;

import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.ActionCompletedListener;
import com.ibm.as400.access.ExtendedIllegalArgumentException;   
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.Vector;

/**
*  The HTMLTableConverter class can be used to convert the data from a RowData
*  object to an array of strings or HTML tables.  Each resulting HTML table then can be used
*  by a servlet to display the rowdata to a browser.
*
*  <P>HTMLTableConverter objects generate the following events:
*  <UL>
*    <LI>ActionCompletedEvent</LI>
*    <LI><A href="SectionCompletedEvent.html">SectionCompletedEvent</A></LI>
*    <LI>PropertyChangeEvent</LI>
*    <LI>VetoableChangeEvent</LI>
*  </UL>
*
*  <P>The following example creates an HTMLTableConverter object and does the conversion.
*  <BLOCKQUOTE><PRE>
*  <P>         // Create an HTMLTableConverter object.
*  HTMLTableConverter converter = new HTMLTableConverter(); 
*  <P>         // Setup the table tag with a maximum of 25 rows/table.
*  HTMLTable table = new HTMLTable();
*  table.setMaximumTableSize(25);            
*  converter.setTable(table);
*  <P>         // Convert the row data.
*  <P>         // Assume the RowData object was created and initialized in a previous step.        
*  String[] html = converter.convert(rowdata);
*  </PRE></BLOCKQUOTE>
**/
public class HTMLTableConverter extends StringConverter implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private HTMLTable htmlTable_;		                     // The html table.
   private HTMLHyperlink[] links_;		                  // The table column header hyperlinks.
   private int maxTableSize_ = 0;		                  // The maximum number of rows in a table.
   private boolean useMetaData_ = false;	               // Whether the metadata is used to create the table header.  Otherwise, the existing table header is used.

   transient private Vector completedListeners_;			// The conversion completed listeners.
   transient private PropertyChangeSupport changes_;	   // The property change listeners.
   transient private VetoableChangeSupport vetos_;			// The vetoable change listeners.
   transient private SectionCompletedSupport sectionCompletedSupport_;	// The section completed listeners.

   /**
   *  Constructs a default HTMLTableConverter object.
   **/
   public HTMLTableConverter()
   {
      super();
      // Initialize the transient data (listeners).
      initializeTransient();
   }

   /**
   *  Adds an ActionCompletedListener.
   *  The specified ActionCompletedListener's <b>actionCompleted</b> method is called
   *  each time the table conversion is complete and all the row data is converted.
   *  The ActionCompletedListener object is added to a list of ActionCompletedListeners
   *  managed by this class; it can be removed with removeActionCompletedListener.
   *
   *  @param listener The ActionCompletedListener.
   *  @see #removeActionCompletedListener
   **/
   public void addActionCompletedListener(ActionCompletedListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      completedListeners_.addElement(listener);
   }

   /**
   *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> 
   *  method is called each time the value of any bound property is changed.
   *  @param listener The PropertyChangeListener.
   *  @see #removePropertyChangeListener
   **/
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      changes_.addPropertyChangeListener(listener);
   }

   /**
   *  Adds a SectionCompletedListener.
   *  The specified SectionCompletedListener's <b>sectionCompleted</b> method is called
   *  each time the conversion for a single table is complete.  
   *  The SectionCompletedListener object is added to an internal list of SectionCompletedListeners;
   *  it can be removed with removeSectionCompletedListener.
   *
   *  @param listener The SectionCompletedListener.
   *  @see #removeSectionCompletedListener
   **/
   public void addSectionCompletedListener(SectionCompletedListener listener)
   {
      sectionCompletedSupport_.addSectionCompletedListener(listener);
   }

   /**
   *  Adds the VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b> 
   *  method is called each time the value of any constrained property is changed.
   *  @param listener The VetoableChangeListener.
   *  @see #removeVetoableChangeListener
   **/
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      vetos_.addVetoableChangeListener(listener);
   }

   /**
   *  Calculates the number of tables in the array.
   *  @param numberRows The number of row in the list.
   *  @return The array of HTMLTables objects.
   **/
   private HTMLTable[] calculateNumberOfTables(int numberRows)
   {
      int numTables = 1;
      if (maxTableSize_ > 0 && numberRows > 0)	    // @C1
      {
         numTables = numberRows / maxTableSize_;
         if (numberRows % maxTableSize_ != 0)
            numTables++;
      }
      return new HTMLTable[numTables];
   }

   /**
   *  Converts the row data specified by <i>rowdata</i> into an array of HTMLTable objects.
   *  If the default table has not been set, it is automatically created with the
   *  column header information being obtained from the metadata.
   *  @param rowdata The RowData object that contains the row data.
   *  @param metadata The RowMetaData object that contains the metadata.
   *  @return A vector containing the tables.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   *  @see #setTable
   **/
   private Vector convertRowData(RowData rowdata, RowMetaData metadata) throws PropertyVetoException, RowDataException			// @A1
   {
      if (metadata == null) 
      {
         Trace.log(Trace.ERROR, "The rowdata's metadata attribute is invalid.");
         throw new ExtendedIllegalStateException("rowdata metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      // Vector to hold the HTML table strings.
      Vector vector = new Vector();             

      // Create the table to be used.
      if (htmlTable_ == null)
      {
  	      // Use the metadata for the column headers.
         setUseMetaData(true);
         try
	      {
            htmlTable_ = new HTMLTable();
            htmlTable_.setHeaderInUse(false);
	      }
	      catch (PropertyVetoException veto) { /* will never occur. */ }       
      }
      // Set the table header based on the metadata.
      if (isUseMetaData()) 
         setTableHeader(metadata);

      // Create the Table array and initialize the first table.
      HTMLTable[] tables = calculateNumberOfTables(rowdata.length());
      int currentTable = 0;
      tables[currentTable] = createDefaultTable();

      // Row processing variables.
      long numRowsInTable = 1;
      int numColumns = metadata.getColumnCount();

      // Process the row data. 
      if (rowdata.length() != 0)          // @C2
         rowdata.beforeFirst();

      while (rowdata.next())
      {
         // Determine if the table is at the maximum size.
         if (maxTableSize_ > 0)
         {
            if (numRowsInTable > 1 && (numRowsInTable % maxTableSize_ == 1) )
            {
               // Add the table to the list.
               vector.addElement(tables[currentTable]);

               // Notify the listeners that a table is finished.
               sectionCompletedSupport_.fireSectionCompleted(tables[currentTable].getTag());

               // Create the next table.
               tables[++currentTable] = createDefaultTable();
            }    
         }

         // Start the row (default row from table).
         HTMLTableRow row = new HTMLTableRow();

         Vector properties;
         for (int column=0; column< numColumns; column++)
         {
            // Create a default cell.
            HTMLTableCell cell = new HTMLTableCell();
	    if (metadata.isNumericData(column) == true)			// @C1
               cell.setHorizontalAlignment(HTMLTableCell.RIGHT);
            HTMLTagElement element;

            // Check object properties for a specific table cell to use.
            properties = rowdata.getObjectProperties(column);   
            if (properties != null) 
            {
               int propSize = properties.size();
               for (int index=0; index< propSize; index++) 
               {
                  // Use the local cell tag.
                  if (properties.elementAt(index) instanceof HTMLTableCell) 
                     cell = (HTMLTableCell)properties.elementAt(index);
               }
            }

            // Set the column data.
            Object columnObject = rowdata.getObject(column);
            try
            {
               cell.setElement((HTMLTagElement)columnObject);
            }
            catch (ClassCastException e)
            {
               cell.setElement(new HTMLText(columnObject.toString()));
            }
               
            // Add the column cell to the row.
            row.addColumn(cell);
         }
         // The the row of data to the table.
         tables[currentTable].addRow(row);
         numRowsInTable++;
      }

      // Add the last table to the list.
      vector.addElement(tables[currentTable]);
      // Notify section completed listeners that the last table is converted.
      sectionCompletedSupport_.fireSectionCompleted(tables[currentTable].getTag());

      // Notify listeners that the tables have been converted.
      fireCompleted();

      return vector;
   }

   /**
   *  Converts the row data specified by <i>rowdata</i> into an array of HTMLTable objects.
   *  @param rowdata The RowData object that contains the row data.
   *  @return An array of HTMLTable objects.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/
   public HTMLTable[] convertToTables(RowData rowdata) throws PropertyVetoException, RowDataException		// @A1
   {
      if (rowdata == null)
         throw new NullPointerException("rowdata");
         
      Vector vector = convertRowData(rowdata, rowdata.getMetaData());

      // Return the list of HTML tables.
      HTMLTable[] tables = new HTMLTable[vector.size()];
      vector.copyInto(tables);
      return tables;
   }

   /**
   *  Creates a default HTMLTable.
   *  @return An HTMLTable object.
   **/
   private HTMLTable createDefaultTable()
   {
      HTMLTable table = new HTMLTable();

      try
      {
         if (htmlTable_.getHeader() != null)
            table.setHeader(htmlTable_.getHeader());				   // header
         if (htmlTable_.getCaption() != null)
            table.setCaption(htmlTable_.getCaption());				// caption
         if (htmlTable_.getAlignment() != null)
            table.setAlignment(htmlTable_.getAlignment());			// alignment
         table.setBorderWidth(htmlTable_.getBorderWidth());			// border width
         table.setCellPadding(htmlTable_.getCellPadding());			// cell padding
         table.setCellSpacing(htmlTable_.getCellSpacing());			// cell spacing
         table.setWidth(htmlTable_.getWidth(), htmlTable_.isWidthInPercent());	// width
         table.setHeaderInUse(htmlTable_.isHeaderInUse());			// header usage
      }
      catch (PropertyVetoException veto) { /* will never occur. */ }     
      return table;
   }

   /**
   *  Converts the row data to a String array of HTML tables.
   *  If the default table has not been set, it is automatically created with the
   *  column header information being obtained from the metadata.
   *  @param rowdata The RowData object that contains the row data.
   *  @param metadata The RowMetaData object that contains the metadata.
   *
   *  @return An array of HTML strings.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   *  @see #setTable
   **/
   String[] doConvert(RowData rowdata, RowMetaData metadata) throws PropertyVetoException, RowDataException
   {
      Vector vector = convertRowData(rowdata, metadata);

      // Return the list of tables as String array.
      String[] data = new String[vector.size()];

      for (int i=0; i< data.length; i++)
         data[i] = ((HTMLTable)vector.elementAt(i)).getTag();
      return data;
   }

   /**
   *  Fires a completed event to notify that all the tables have been converted.
   **/
   private void fireCompleted()
   {
      Vector targets = (Vector) completedListeners_.clone();
      ActionCompletedEvent event = new ActionCompletedEvent(this);
      for (int i=0; i< targets.size(); i++)
      {
        ActionCompletedListener target = (ActionCompletedListener)targets.elementAt(i);
        target.actionCompleted(event);
      }
   }

   /**
   *  Returns the table header's hyperlinks.
   *  @return The hyperlinks.
   **/
   public HTMLHyperlink[] getHeaderHyperlinks() 
   {
      return links_;
   }

   /**
   *  Returns the maximum number of rows in a table.
   *  The default maximum size is 0 (no maximum).
   *  @return The maximum size.
   **/
   public int getMaximumTableSize()
   {
      return maxTableSize_;
   }

   /**
   *  Returns the object hyperlink for the current row's specified <i>column</i>.
   *  @param rowdata The RowData object that contains the data.
   *  @param column The column number (0-based).
   *
   *  @return The hyperlink.
   **/
   public HTMLHyperlink getObjectHyperlink(RowData rowdata, int column)
   {
      // Validate the rowdata parameter.
      if (rowdata == null) 
         throw new NullPointerException("rowdata");
      
      return getObjectHyperlink(rowdata, rowdata.getCurrentPosition(), column);
   }

   /**
   *  Returns the object's hyperlink at the specified <i>row</i> and <i>column</i>.
   *  @param rowdata The RowData object that contains the data.
   *  @param row The row number (0-based).
   *  @param column The column number (0-based).
   *
   *  @return The hyperlink.
   **/
   public HTMLHyperlink getObjectHyperlink(RowData rowdata, int row, int column)
   {
      // Validate the rowdata parameter.
      if (rowdata == null) 
         throw new NullPointerException("rowdata");

      // Position to the row.
      if (!rowdata.absolute(row))				// Validates the row parameter.
         throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      // Get the object's properties.               
      Vector properties = rowdata.getObjectProperties(column);	// Validates the column parameter.

      HTMLHyperlink link = null;
      if (properties != null)
      {
         // Get the hyperlink associated with the object.
         int size = properties.size();
         for (int index=0; index< size; index++) 
         {
            if (properties.elementAt(index) instanceof HTMLHyperlink) 
            {                                                                                                                                                                                                                 link = (HTMLHyperlink)properties.elementAt(index);
                break;
            }
	 }
      }      
      return link;
   }

   /**
   *  Returns the default HTML table.
   *  @return The table.
   **/
   public HTMLTable getTable()
   {
      return htmlTable_;
   }

   /**
   *  Initializes the transient data.
   **/
   private void initializeTransient()
   {
      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
      sectionCompletedSupport_ = new SectionCompletedSupport(this);
      completedListeners_ = new Vector();
   }

   /**
   *  Indicates whether the table header is created using the
   *  metadata.  Default value is false (use existing table header).
   *  @return true if the metadata is used; false otherwise.
   **/
   public boolean isUseMetaData()
   {
      return useMetaData_;
   }

   /**
   *  Deserializes and initializes transient data.
   **/
   private void readObject(java.io.ObjectInputStream in)         
       throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      initializeTransient();
   }

   /**
   *  Removes this ActionCompletedListener from the internal list.
   *  If the ActionCompletedListener is not on the list, nothing is done.
   *  @param listener The ActionCompletedListener.
   *  @see #addActionCompletedListener
   **/
   public void removeActionCompletedListener(ActionCompletedListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      completedListeners_.removeElement(listener);
   }

   /**
   *  Removes the PropertyChangeListener from the internal list.
   *  If the PropertyChangeListener is not on the list, nothing is done.
   *  @param listener The PropertyChangeListener.
   *  @see #addPropertyChangeListener
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      changes_.removePropertyChangeListener(listener);
   }

   /**
   *  Removes this SectionCompletedListener from the internal list.
   *  If the SectionCompletedListener is not on the list, nothing is done.
   *  @param listener The SectionCompletedListener.
   *  @see #addSectionCompletedListener
   **/
   public void removeSectionCompletedListener(SectionCompletedListener listener)
   {
      sectionCompletedSupport_.removeSectionCompletedListener(listener);
   }
   
   /**
   *  Removes the VetoableChangeListener from the internal list.
   *  If the VetoableChangeListener is not on the list, nothing is done.
   *  @param listener The VetoableChangeListener.
   *  @see #addVetoableChangeListener
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");   
      vetos_.removeVetoableChangeListener(listener);
   }

   /**
   *  Sets the table header's hyperlinks.
   *  @param links The hyperlinks.
   *  @exception PropertyVetoException If a change is vetoed.
   **/
   public void setHeaderHyperlinks(HTMLHyperlink[] links) throws PropertyVetoException
   {
      if (links == null) 
         throw new NullPointerException("links");

      HTMLHyperlink[] old = links_;
      vetos_.fireVetoableChange("links", old, links);

      links_ = links;   

      changes_.firePropertyChange("links", old, links);
   }

   /**
   *  Sets the maximum number of rows in a table.  The default value is 0 (no maximum).
   *  @param size The maximum size.
   *  @exception PropertyVetoException If a change is vetoed.
   **/
   public void setMaximumTableSize(int size) throws PropertyVetoException
   {
      if (size < 0) 
         throw new ExtendedIllegalArgumentException("size", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldSize = new Integer(maxTableSize_);
      Integer newSize = new Integer(size);

      vetos_.fireVetoableChange("size", oldSize, newSize);

      maxTableSize_ = size;

      changes_.firePropertyChange("size", oldSize, newSize);
   }

   /**
   *  Sets the object's hyperlink at the specified <i>column</i> within the current row.
   *  The hyperlink is a property of the data object that can be used to link the data object
   *  to an Uniform Resource Identifier (URI).
   *  @param rowdata The RowData object that contains the rowdata.
   *  @param link The hyperlink tag.
   *  @param column The column number (0-based).
   *
   *  @exception RowDataException If a row data exception occurs.
   **/
   public void setObjectHyperlink(RowData rowdata, HTMLHyperlink link, int column) throws RowDataException
   {
      // Validate the rowdata parameter.
      if (rowdata == null) 
         throw new NullPointerException("rowdata");
      setObjectHyperlink(rowdata, link, rowdata.getCurrentPosition(), column);
   }

   /**
   *  Sets the row object's hyperlink specified by <i>row</i> and <i>column</i>.
   *  The hyperlink is a property of the data object that can be used to link the data
   *  object to an Uniform Resource Identifier (URI).
   *  @param rowdata The RowData object that contains the row data.
   *  @param link The hyperlink tag.
   *  @param row The row number (0-based).
   *  @param column The column number (0-based).
   *  @exception RowDataException If a row data exception occurs.
   **/
   public void setObjectHyperlink(RowData rowdata, HTMLHyperlink link, int row, int column) throws RowDataException
   {
      // Validate the rowdata parameter.
      if (rowdata == null) 
         throw new NullPointerException("rowdata");

      // Validate the link parameter.
      if (link == null) 
         throw new NullPointerException("link");

      // Validate the row parameter.
      if (!rowdata.absolute(row))
         throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      // Get the object's properties.
      Vector properties = rowdata.getObjectProperties(column);

      // Add the hyperlink to the object properties list.
      if (properties == null) 
      {
         // Create the properties list and add link.
         properties = new Vector();
         properties.addElement(link);     
      }
      else 
      {     
         // Has properties.
         HTMLHyperlink old = null;       // The existing hyperlink object.
         int linkIndex = -1;             // The property index of the the existing hyperlink.

         // Check for existing hyperlink.
         int size = properties.size();           
         for (int index=0; index< size; index++) 
         {
            if (properties.elementAt(index) instanceof HTMLHyperlink)
            {
               // Get the existing hyperlink.
               old = (HTMLHyperlink)properties.elementAt(index);
               linkIndex = index;
               break;
            }
         }
         if (old == null) 
            properties.addElement(link);        
         else
            properties.setElementAt(link, linkIndex);      
      }
      // Set the row object's properties with the new hyperlink.
      rowdata.setObjectProperties(properties, column);
   }

   /**
   *  Sets the default HTML table to be used during conversion.
   *  The default table's column headers must be set.  The <i>setUseMetaData</i>
   *  method can also be used to set the column headers based on the metadata.
   *  @param table The HTML table.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see #setUseMetaData
   **/
   public void setTable(HTMLTable table) throws PropertyVetoException
   {
      if (table == null) 
         throw new NullPointerException("table");

      HTMLTable old = htmlTable_;
      vetos_.fireVetoableChange("table", old, table);

      htmlTable_ = table;

      changes_.firePropertyChange("table", old, table);
   }

   /**
   *  Sets the table column header.  The metadata column labels are
   *  used in creating the table header.  If a column label does not
   *  exist the column name is used.  
   *
   *  @param metadata The meta data.
   *  @exception RowDataException If a row data error occurs.
   *  @exception PropertyVetoException If a property change is vetoed.
   **/
   private void setTableHeader(RowMetaData metadata)
      throws RowDataException, PropertyVetoException 
   {  
      // Create the header list.
      int numColumns = metadata.getColumnCount();
      HTMLTableHeader[] headerList = new HTMLTableHeader[numColumns];
       
      // Get the header names from the metadata.
      String colName = "";
      HTMLTagElement element;
      for (int column=0; column< numColumns; column++)
      {                
         // Use the column label if it exists; otherwise use the name.
         try                                                         
         {                                                           
            colName = metadata.getColumnLabel(column); 
         }                                                          
         catch (NullPointerException e)                                        
         {                                                          
            colName = metadata.getColumnName(column);                   
         }              
         // Check for hyperlinks.
         if (links_ != null && links_[column] != null) 
         {
            HTMLHyperlink link = links_[column];
            link.setText(colName);
            element = link;
         }
         else
            element = new HTMLText(colName);

         headerList[column] = new HTMLTableHeader(element);
      }

      // Set the table's header.
      htmlTable_.setHeader(headerList);
      if (!htmlTable_.isHeaderInUse())
         htmlTable_.setHeaderInUse(true);
   }

   /**
   *  Sets whether the table header is created using the
   *  metadata.  Default value is false (use existing table header).
   *  @param useMetaData true if the metadata is used; false otherwise.
   **/
   public void setUseMetaData(boolean useMetaData)
   {
      useMetaData_ = useMetaData;
   }

}
