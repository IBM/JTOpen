///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLFormConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.util.html.HTMLConstants;
import com.ibm.as400.util.html.HTMLHyperlink;
import com.ibm.as400.util.html.HTMLTable;
import com.ibm.as400.util.html.HTMLTableCaption;
import com.ibm.as400.util.html.HTMLTableCell;
import com.ibm.as400.util.html.HTMLTableHeader;
import com.ibm.as400.util.html.HTMLTableRow;
import com.ibm.as400.util.html.HTMLTagElement;
import com.ibm.as400.util.html.HTMLText;
import com.ibm.as400.util.html.LineLayoutFormPanel;                // @D4A

import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.ActionCompletedListener;
import com.ibm.as400.access.Copyright;
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
*  The HTMLFormConverter class can be used to convert data from a RowData object
*  to an array of HTML strings or forms.
*  <P>
*  Each row is converted to a String representation of a one-row HTML table tag that
*  can then be used by a servlet to display the formatted row back to a browser.  The
*  one-row table contains the column headers and the data for the individual row.
*
*  <P>HTMLFormConverter objects generate the following events:
*  <UL>
*    <LI>ActionCompletedEvent</LI>
*    <LI><A href="SectionCompletedEvent.html">SectionCompletedEvent</A></LI>
*    <LI>PropertyChangeEvent</LI>
*    <LI>VetoableChangeEvent</LI>
*  </UL>
*
*  <P>The following example creates an HTMLFormConverter object and converts the row data
*  to an array of forms (html strings).
*  <BLOCKQUOTE><PRE>
*  <P>         // Create an HTMLFormConverter object.
*  HTMLFormConverter converter = new HTMLFormConverter();
*  <P>         // Convert the row data.
*  <P>         // Assume the RowData object was created and initialized in a previous step.
*  String[] html = converter.convert(rowdata);
*  </PRE></BLOCKQUOTE>
*
*  <P>The following examples creates an HTMLFormConverter object and converts the row data
*  to an array of forms (one-row HTMLTable objects).
*  <BLOCKQUOTE><PRE>
*  <P>         // Creates an HTMLFormConverter object.
*  HTMLFormConverter converter = new HTMLFormConverter();
*  <P>         // Convert the row data.  Assume the RowData object was created and initialized
*  in a previous step.
*  HTMLTable[] forms = converter.convertToForms(rowdata);
*  </PRE></BLOCKQUOTE>
*
*  <P>The following example creates an HTMLFormConverter object and sets the column header
*  hyperlinks before doing the conversion.
*  <BLOCKQUOTE><PRE>
*  <P>         // Create an HTMLFormConverter object with a border.
*  HTMLFormConverter converter = new HTMLFormConverter();
*  converter.setBorderWidth(1);
*  <P>         // Create the rowdata.
*  int numberOfColumns = 3;
*  ListMetaData metadata = new ListMetaData(numberOfColumns);
*  metadata.setColumnLabel(0, "Animal ID");
*  metadata.setColumnLabel(1, "Animal Name");
*  metadata.setColumnLabel(2, "Date of Birth");
*  ListRowData rowdata = new ListRowData(metadata);
*  <P>         // Add a row.
*  Object[] data = { new Integer(123456), "Timberwolf", (new Date()).toString() };
*  rowdata.addRow(data);
*  <P>         // Create the header hyperlinks.
*  HTMLHyperlink[] links = new HTMLHyperlink[numberOfColumns];
*  links[0] = new HTMLHyperlink("http://www.myZoo.com/IDList.html", "MyZoo Animal Identification List");
*  links[1] = new HTMLHyperlink("http://www.myZoo.com/animals.html", "MyZoo Animal List");
*  converter.setHeaderHyperlinks(links);
*  <P>         // Convert the rowdata.
*  String[] html = converter.convert(rowdata);
*  System.out.println(html[0]);
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the html output:
*  <BLOCKQUOTE><PRE>
*  &lt;table border="1"&gt;
*  &lt;tr&gt;
*  &lt;th&gt;&lt;a href=&quot;http://www.myZoo.com/IDList.html&quot;&gt;Animal ID&lt;/a&gt;&lt;/th&gt;
*  &lt;td&gt;123456&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;tr&gt;
*  &lt;th&gt;&lt;a href=&quot;http://www.myZoo.com/animals.html&quot;&gt;Animal Name&lt;/a&gt;&lt;/th&gt;
*  &lt;td&gt;Timberwolf&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;tr&gt;
*  &lt;th&gt;Date of Birth&lt;/th&gt;
*  &lt;td&gt;Sun Mar 14 16:00:00 CDT 1999&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;/table&gt;
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is what the form will look like in the browser:
*  <table border="1">
*  <tr>
*  <th><a href="http://www.myZoo.com/IDList.html">Animal ID</a></th>
*  <td>123456</td>
*  </tr>
*  <tr>
*  <th><a href="http://www.myZoo.com/animals.html">Animal Name</a></th>
*  <td>Timberwolf</td>
*  </tr>
*  <tr>
*  <th>Date of Birth</th>
*  <td>Sun Mar 14 16:00:00 CDT 1999</td>
*  </tr>
*  </table>
**/

public class HTMLFormConverter extends StringConverter implements Serializable, HTMLConstants
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private HTMLTable htmlTable_;                    // HTMLTable used to represent form.
   private HTMLHyperlink[] links_;              // The column header's hyperlink list.

   transient private Vector completedListeners_; //@CRS
   transient private SectionCompletedSupport sectionCompletedSupport_; //@CRS
   transient private PropertyChangeSupport changes_; //@CRS
   transient private VetoableChangeSupport vetos_; //@CRS

   /**
   *  Constructs a default HTMLFormConverter object.
   **/
   public HTMLFormConverter()
   {
      super();
      htmlTable_ = new HTMLTable();
   }

   /**
   *  Adds an ActionCompletedListener.
   *  The specified ActionCompletedListener's <b>actionCompleted</b> method is called
   *  each time the form conversion is complete.
   *  The ActionCompletedListener object is added to an internal list of ActionCompletedListeners;
   *  it can be removed with removeActionCompletedListener.
   *
   *  @param listener The ActionCompletedListener.
   *  @see #removeActionCompletedListener
   **/
   public void addActionCompletedListener(ActionCompletedListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");

      if (completedListeners_ == null) completedListeners_ = new Vector(); //@CRS
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

      if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS

      changes_.addPropertyChangeListener(listener);

      // Add a listener for the table attributes.
      htmlTable_.addPropertyChangeListener(listener);
   }

   /**
   *  Adds a SectionCompletedListener.
   *  The specified SectionCompletedListener's <b>sectionCompleted</b> method is called
   *  each time the conversion of a single row to a form is complete.
   *  The SectionCompletedListener object is added to an internal list of SectionCompletedListeners;
   *  it can be removed with removeSectionCompletedListener.
   *
   *  @param listener The SectionCompletedListener.
   *  @see #removeSectionCompletedListener
   **/
   public void addSectionCompletedListener(SectionCompletedListener listener)
   {
     if (sectionCompletedSupport_ == null) sectionCompletedSupport_ = new SectionCompletedSupport(this); //@CRS

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

      if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS

      vetos_.addVetoableChangeListener(listener);

      // Add a listener for the table attributes.
      htmlTable_.addVetoableChangeListener(listener);
   }

   /**
   *  Converts the row data specified by <i>rowdata</i> into an array of HTMLTable objects.
   *  @param rowdata The RowData object that contains the row data.
   *  @param metadata The RowMetaData object that contains the metadata.
   *  @return A vector containing the tables.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/
   private Vector convertRowData(RowData rowdata, RowMetaData metadata) throws PropertyVetoException, RowDataException       // @A1
   {
      if (metadata == null)
      {
         Trace.log(Trace.ERROR, "The rowdata's metadata attribute is invalid.");
         throw new ExtendedIllegalStateException("rowdata metadata", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      // Vector of HTML tables.
      Vector formList = new Vector();

      // Get the number of columns in the row data.
      int numColumns = metadata.getColumnCount();

      HTMLTableHeader[] header = createFormHeader(metadata);      // @B2

      // Process the row data.
      if (rowdata.length() == 0)                                  // @B2
      {
         HTMLTable table = createDefaultTable(header);

         // End the form.
         formList.addElement(table);

         // Notify the listeners that a form is completed.
         if (sectionCompletedSupport_ != null) sectionCompletedSupport_.fireSectionCompleted(table.getTag()); //@CRS
      }
      else
      {
         rowdata.beforeFirst();
      }

      while (rowdata.next())
      {
         HTMLTable table = createDefaultTable(header);

         // Process the row.
         for (int column=0; column< numColumns; column++)
         {
            HTMLTableRow row = table.getRow(column);         // @B2

            // Process the meta data type and add the object.
            HTMLTableCell cell = new HTMLTableCell();
            HTMLTagElement element;

            Vector properties = rowdata.getObjectProperties(column);
            if (properties != null)
            {
               int propSize = properties.size();
               for (int index=0; index< propSize; index++)
               {
                  // Use local cell tag if available.
                  if (properties.elementAt(index) instanceof HTMLTableCell)
                        cell = (HTMLTableCell)properties.elementAt(index);
               }
            }

            // Set the column data.
            Object columnObject = rowdata.getObject(column);

            // If the column data is null, place a <br /> into the cell otherwise       // @D4A
            // a NullPointerException will be thrown for an empty cell elment.          // @D4A
            if (columnObject == null)                                                   // @D4A
               columnObject = new LineLayoutFormPanel();                                // @D4A

            try
            {
               cell.setElement((HTMLTagElement)columnObject);
            }
            catch (ClassCastException e)
            {
               cell.setElement(new HTMLText(columnObject.toString()));
            }

            if (metadata.getColumnAlignment(column) != null)                                       //@D5A
                cell.setHorizontalAlignment(metadata.getColumnAlignment(column));        //@D5A
            
            if (metadata.getColumnDirection(column) != null)                                      //@D5A
                cell.setDirection(metadata.getColumnDirection(column));                       //@D5A

            // Add the column cell to the row.
            row.addColumn(cell);
            // Add the row to the table.
            table.setRow(row, column);       // @B2
         }

         // End the form.
         formList.addElement(table);

         // Notify the listeners that a form is completed.
         if (sectionCompletedSupport_ != null) sectionCompletedSupport_.fireSectionCompleted(table.getTag()); //@CRS
      }

      // Notify the listeners that all the forms are converted.
      fireCompleted();

      return formList;
   }

   /**
   *  Converts the specified <i>rowdata</i> to an array of forms (one-row HTML tables).
   *  Each form is a one-row HTML table with the column headers and the data of the individual row.
   *
   *  @param rowdata The row data.
   *  @return An array of HTML tables.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/
   public HTMLTable[] convertToForms(RowData rowdata) throws PropertyVetoException, RowDataException          // @A1
   {
      if (rowdata == null)
         throw new NullPointerException("rowdata");

      // Convert to a vector of forms (2-column tables).
      Vector tableVector = convertRowData(rowdata, rowdata.getMetaData());

      // Return the list of HTML tables.
      HTMLTable[] tables = new HTMLTable[tableVector.size()];
      tableVector.copyInto(tables);
      return tables;
   }

   /**
   *  Creates a default HTMLTable.
   *  @param header The form headers.
   *  @return An HTMLTable object.
   **/
   private HTMLTable createDefaultTable(HTMLTableHeader[] header)       // @B2 - added parameter.
   {
      HTMLTable table = new HTMLTable();

      try
      {
         if (htmlTable_.getCaption() != null)
            table.setCaption(htmlTable_.getCaption());                // caption
         if (htmlTable_.getAlignment() != null)
            table.setAlignment(htmlTable_.getAlignment());            // alignment
         table.setBorderWidth(htmlTable_.getBorderWidth());           // border width
         table.setCellPadding(htmlTable_.getCellPadding());           // cell padding
         table.setCellSpacing(htmlTable_.getCellSpacing());           // cell spacing

         table.setWidth(htmlTable_.getWidth(), htmlTable_.isWidthInPercent());  // width

         table.setHeaderInUse(false);                            // header usage

         if (htmlTable_.getLanguage() != null)                    // language       //$B1A
            table.setLanguage(htmlTable_.getLanguage());                            //$B1A
         if (htmlTable_.getDirection() != null)                   // direction      //$B1A
            table.setDirection(htmlTable_.getDirection());                          //$B1A

         // Add a column header to each row in the table.
         for (int column=0; column< header.length; column++)      // @B2
         {
            HTMLTableRow row = new HTMLTableRow();                // @B2
            row.addColumn(header[column]);                        // @B2
            table.addRow(row);                                    // @B2
         }
      }
      catch (PropertyVetoException veto) { /* will never occur. */ }
      return table;
   }


   /**
   *  Creates the form header to be used in the default HTMLTable.
   *  @param metadata The RowMetaData object containing the column information.
   *  @return An array of HTMLTableHeader objects.
   *  @exception PropertyVetoException If changed is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/                                                                    // @B2 - new method.
   private HTMLTableHeader[] createFormHeader(RowMetaData metadata) throws PropertyVetoException, RowDataException
   {
      int numColumns = metadata.getColumnCount();
      HTMLTableHeader[] header = new HTMLTableHeader[numColumns];
      for (int column=0; column< numColumns; column++)
      {
         header[column] = new HTMLTableHeader();
         // Write out the headings
         String columnName = "";
         try
         {
            columnName = metadata.getColumnLabel(column);
         }
         catch (NullPointerException e)
         {
            columnName = metadata.getColumnName(column);
         }

         if (links_ != null && links_[column] != null)
         {
            HTMLHyperlink link = links_[column];
            link.setText(columnName);
            header[column].setElement(link);
         }
         else
         {
            HTMLText text = new HTMLText(columnName);
            header[column].setElement(text);
         }
      }
      return header;
   }

   /**
   *  Converts the specified <i>rowdata</i> to an array of HTML strings.
   *
   *  @param rowdata The row data.
   *  @param metadata The meta data.
   *  @return An array of HTML Strings.
   *  @exception PropertyVetoException If a change is vetoed.
   *  @exception RowDataException If a row data error occurs.
   **/
   String[] doConvert(RowData rowdata, RowMetaData metadata)
      throws PropertyVetoException, RowDataException
   {
      // Validate the metadata parameter.
      if (metadata == null)
         throw new NullPointerException("metadata");

      // do the conversion.
      Vector forms = convertRowData(rowdata, metadata);

      // Return the list of form as String array.
      String[] data = new String[forms.size()];

      for (int i=0; i< data.length; i++)
         data[i] = ((HTMLTable)forms.elementAt(i)).getTag();
      return data;
   }

   /**
   *  Fires a section completed event.
   *  @param obj The source object from which the event originated.
   **/
   private void fireCompleted()
   {
     if (completedListeners_ == null) return; //@CRS
     Vector targets = (Vector) completedListeners_.clone();
     ActionCompletedEvent event = new ActionCompletedEvent(this);
     for (int i=0; i< targets.size(); i++)
     {
       ActionCompletedListener target = (ActionCompletedListener)targets.elementAt(i);
       target.actionCompleted(event);
     }
   }

   /**
   *  Returns the form alignment.
   *  @return The form alignment.
   **/
   public String getAlignment()
   {
      return htmlTable_.getAlignment();
   }

   /**
   *  Returns the form border width.
   *  @return The width in pixels.
   **/
   public int getBorderWidth()
   {
      return htmlTable_.getBorderWidth();
   }

   /**
   *  Returns the form caption.
   *  @return An HTMLTableCaption object that contains the form caption.
   **/
   public HTMLTableCaption getCaption()
   {
      return htmlTable_.getCaption();
   }

   /**
   *  Returns the form cell padding in pixels.
   *  @return The cell padding.
   **/
   public int getCellPadding()
   {
      return htmlTable_.getCellPadding();
   }

   /**
   *  Returns the form cell spacing in pixels.
   *  @return The cell spacing.
   **/
   public int getCellSpacing()
   {
      return htmlTable_.getCellSpacing();
   }

   /**
   *  Returns the form text interpretation direction.
   *  @return The direction.
   **/                                                     //$B1A
   public String getDirection()                            //$B1A
   {                                                       //$B1A
      return htmlTable_.getDirection();                    //$B1A
   }

   /**
   *  Returns the form header's hyperlinks.
   *  @return The hyperlinks.
   **/
   public HTMLHyperlink[] getHeaderHyperlinks()
   {
      return links_;
   }

   /**
   *  Returns the language of the form.
   *  @return The language.
   **/
   public String getLanguage()                        //$B1A
   {                                                  //$B1A
      return htmlTable_.getLanguage();                //$B1A
   }                                                  //$B1A

   /**
   *  Returns the object's hyperlink at the specified <i>column</i> within the current row.
   *  @param rowdata The RowData object that contains the data.
   *  @param column The column number (0-based).
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
   *  @return The hyperlink.
   **/
   public HTMLHyperlink getObjectHyperlink(RowData rowdata, int row, int column)
   {
      // Validate the rowdata parameter.
      if (rowdata == null)
         throw new NullPointerException("rowdata");

      HTMLHyperlink link = null;

      if (!rowdata.absolute(row))
         throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Vector properties = rowdata.getObjectProperties(column);

      if (properties != null)
      {
         int propSize = properties.size();
         for (int index=0; index < propSize; index++) {
            if (properties.elementAt(index) instanceof HTMLHyperlink)
            {
               link = (HTMLHyperlink)properties.elementAt(index);
               break;
            }
         }
      }
      return link;
   }

   /**
   *  Returns the form width in pixels or percent.
   *  @return The form width.
   *  @see #isWidthInPercent
   **/
   public int getWidth()
   {
      return htmlTable_.getWidth();
   }

   /**
   *  Indicates if the form width is in percent or pixels.
   *  @return True if percent, false if pixels.
   *  @see #getWidth
   **/
   public boolean isWidthInPercent()
   {
      return htmlTable_.isWidthInPercent();
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
      //@CRS completedListeners_ = new Vector();
      //@CRS sectionCompletedSupport_ = new SectionCompletedSupport(this);
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

      if (completedListeners_ != null) completedListeners_.removeElement(listener); //@CRS
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

      if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS

      // Remove the listener for the table attributes.
      htmlTable_.removePropertyChangeListener(listener);
   }

   /**
   *  Removes this SectionCompletedListener from the internal list.
   *  If the SectionCompletedListener is not on the list, nothing is done.
   *  @param listener The SectionCompltedListener.
   *  @see #addSectionCompletedListener
   **/
   public void removeSectionCompletedListener(SectionCompletedListener listener)
   {
       if(listener == null)                                 //@KCA
           throw new NullPointerException("listener");      //@KCA
      if (sectionCompletedSupport_ != null) sectionCompletedSupport_.removeSectionCompletedListener(listener); //@CRS
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

      if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS

      // Remove the listener for the table attributes.
      htmlTable_.removeVetoableChangeListener(listener);
   }

   /**
   *  Sets the form alignment.  The default value is LEFT.
   *  @param alignment The form alignment.  One of the following constants
   *  defined in HTMLConstants:  CENTER, LEFT, or RIGHT.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see com.ibm.as400.util.html.HTMLConstants
   **/
   public void setAlignment(String alignment) throws PropertyVetoException
   {
      htmlTable_.setAlignment(alignment);
   }

   /**
   *  Sets the form border width in pixels.  A value of zero indicates no border.
   *  The default value is zero.
   *  @param borderWidth The border width in pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setBorderWidth(int borderWidth) throws PropertyVetoException
   {
      htmlTable_.setBorderWidth(borderWidth);
   }

   /**
   *  Sets the form caption.
   *  @param caption The caption text.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setCaption(HTMLTableCaption caption) throws PropertyVetoException
   {
      htmlTable_.setCaption(caption);
   }

   /**
   *  Sets the form cell padding in pixels.  The cell padding is the spacing between
   *  data in the form and the border of the form cell.
   *  The default value is zero (browser default used).
   *  @param cellPadding The cell padding in pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setCellPadding(int cellPadding) throws PropertyVetoException
   {
      htmlTable_.setCellPadding(cellPadding);
   }

   /**
   *  Sets the form cell spacing in pixels.  The cell spacing is the spacing between
   *  the form cells.  The default value is zero (browser default used).
   *  @param cellSpacing The cell spacing in pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setCellSpacing(int cellSpacing) throws PropertyVetoException
   {
      htmlTable_.setCellSpacing(cellSpacing);
   }

   /**
   *  Sets the form text interpretation direction.
   *  @param dir The direction of text interpretation.  One of the following constants
   *             defined in HTMLConstants:  LTR or RTL
   *
   *  @see com.ibm.as400.util.html.HTMLConstants
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setDirection(String dir) throws PropertyVetoException      //$B1A
   {                                                                      //$B1A
      // If direction is not one of the valid HTMLConstants, throw an exception.                                          //$B1A
      if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )                                        //$B1A
      {                                                                                                                   //$B1A
           throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID); //$B1A
      }                                                                                                                   //$B1A
      htmlTable_.setDirection(dir);                                       //$B1A
   }                                                                      //$B1A

   /**
   *  Sets the form header's hyperlinks.
   *  @param links The hyperlinks.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setHeaderHyperlinks(HTMLHyperlink[] links) throws PropertyVetoException
   {
      if (links == null)
         throw new NullPointerException("links");

      HTMLHyperlink[] old = links_;
      if (vetos_ != null) vetos_.fireVetoableChange("links", old, links); //@CRS

      links_ = links;

      if (changes_ != null) changes_.firePropertyChange("links", old, links); //@CRS
   }

   /**
   *  Sets the language of the form.
   *  @param lang The language.  Example language tags include:
   *  en and en-US.
   *
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setLanguage(String lang) throws PropertyVetoException        //$B1A
   {                                                                        //$B1A
      htmlTable_.setLanguage(lang);                                         //$B1A
   }                                                                        //$B1A

   /**
   *  Sets the object's hyperlink at the specified <i>column</i> within the current row.
   *  @param rowdata The RowData object that contains the data.
   *  @param link The hyperlink.
   *  @param column The column number (0-based).
   *  @exception RowDataException If a row data error occurs.
   **/
   public void setObjectHyperlink(RowData rowdata, HTMLHyperlink link, int column)throws RowDataException
   {
      if (rowdata == null)
         throw new NullPointerException("rowdata");

      setObjectHyperlink(rowdata, link, rowdata.getCurrentPosition(), column);
   }

   /**
   *  Sets the object's hyperlink at the specified <i>row</i> and <i>column</i>.
   *  @param rowdata The RowData object that contains the data.
   *  @param link The hyperlink.
   *  @param row The row number (0-based).
   *  @param column The column number (0-based).
   *  @exception RowDataException If a row data error occurs.
   **/
   public void setObjectHyperlink(RowData rowdata, HTMLHyperlink link, int row, int column) throws RowDataException
   {
      // Validate the parameters.
      if (rowdata == null)
         throw new NullPointerException("rowdata");
      if (link == null)
         throw new NullPointerException("link");

      // Position to the row.
      if (!rowdata.absolute(row))
         throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      // Get the object's properties.
      Vector properties = rowdata.getObjectProperties(column);

      if (properties == null)
      {
         // Create a properties list and add the hyperlink.
         properties = new Vector();
         properties.addElement(link);
      }
      else
      {
         // Has properties.
         HTMLHyperlink oldTag = null;        // The existing hyperlink object.
         int linkIndex = -1;                 // The property index of the existing hyperlink.

         // Check for existing hyperlink.
         int propSize = properties.size();
         for (int index=0; index < propSize; index++)
         {
            if (properties.elementAt(index) instanceof HTMLHyperlink)
            {
               // Get the existing hyperlink.
               oldTag = (HTMLHyperlink)properties.elementAt(index);
               linkIndex = index;
               break;
            }
         }

         if (oldTag == null)
            properties.addElement(link);
         else
            properties.setElementAt(link, linkIndex);
      }
      // Set the row object's new properties list.
      rowdata.setObjectProperties(properties, column);
   }

   /**
   *  Sets the form width in pixels or percent.
   *  @param width The form width.
   *  @param widthInPercent true if the width is specified as a percent; false if the width is specified in pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setWidth(int width, boolean widthInPercent) throws PropertyVetoException
   {
      htmlTable_.setWidth(width, widthInPercent);
   }
}
