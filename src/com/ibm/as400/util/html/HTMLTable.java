///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTable.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.util.Vector;
import java.io.Serializable;

/**
*  The HTMLTable class represents an HTML table tag.
*
*  <P>This example creates an HTMLTable object and sets its attributes.
*
*  <BLOCKQUOTE><PRE>
*  HTMLTable table = new HTMLTable();
*  table.setAlignment(HTMLTable.CENTER);
*  table.setHeaderInUse(false);
*  table.setBorderWidth(1);
*  table.setCellSpacing(2);
*  table.setCellPadding(2);
*  // Add the rows to the table (Assume that the HTMLTableRow objects are already created).
*  table.addRow(row1);
*  table.addRow(row2);
*  table.addRow(row3);
*  System.out.println(table.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the table tag:
*  <BLOCKQUOTE><PRE>
*  &lt;table border="1" align="center" cellspacing="2" cellpadding="2"&gt;
*  &lt;tr&gt;
*  &lt;td&gt;row1data1&lt;/td&gt;
*  &lt;td&gt;row1data2&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;tr&gt;
*  &lt;td&gt;row2data1&lt;/td&gt;
*  &lt;td&gt;row2data2&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;tr&gt;
*  &lt;td&gt;row3data1&lt;/td&gt;
*  &lt;td&gt;row3data2&lt;/td&gt;
*  &lt;/tr&gt;
*  &lt;/table&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLTable objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementChanged
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.HTMLTableRow
*  @see com.ibm.as400.util.html.HTMLTableCell
*  @see com.ibm.as400.util.html.HTMLTableHeader
*  @see com.ibm.as400.util.html.HTMLTableCaption
**/
public class HTMLTable extends HTMLTagAttributes implements HTMLConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private Vector rows_;                           // The table rows.
    private HTMLTableCaption caption_;              // The table caption.
    private Vector headerTag_;                      // The table header.

    private String alignment_;                      // The table horizontal alignment.
    private int borderWidth_ = 0;                   // The table border.
    private int cellPadding_ = -1;                  // The global table cell padding.   // @C1C
    private int cellSpacing_ = -1;                  // The global table cell spacing.   // @C1C
    private int width_ = 0;                         // The table width.

    private boolean headerInUse_ = true;            // Indicates if the column headers are used.
    private boolean widthPercent_ = false;          // Indicates if the table width is in percent.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    transient private Vector rowListeners_;         // The list of row listeners. @CRS
    transient private VetoableChangeSupport vetos_; //@CRS

    /**
    *  Constructs a default HTMLTable object.
    **/
    public HTMLTable()
    {
        rows_ = new Vector();

    }

    /**
    *  Constructs an HTMLTable object with the specified <i>rows</i>.
    *  @param rows An array of HTMLTableRow objects.
    **/
    public HTMLTable(HTMLTableRow[] rows)
    {
        this();

        if (rows == null)
            throw new NullPointerException("rows");

        // Add the rows.
        for (int i=0; i < rows.length; i++)
            addRow(rows[i]);
    }

    /**
    *  Adds a column to the end of the table.
    *  @param column An array of HTMLTableCell objects containing the data.
    **/
    public void addColumn(HTMLTableCell[] column)
    {
        // Validate the column parameter.
        if (column == null)
            throw new NullPointerException("column");

        HTMLTableRow row;
        int size = rows_.size();

        synchronized (rows_)
        {
            // Add new rows to an empty table.
            if (size == 0)
            {
                for (int i=0; i< column.length; i++)
                {
                    row = new HTMLTableRow();
                    row.addColumn(column[i]);
                    rows_.addElement(row);
                }
            }
            // Validate the column length.
            else if (column.length != size)
            {
                throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
            }
            // Add the columns.
            else
            {
                for (int i=0; i< size; i++)
                {
                    row = (HTMLTableRow)rows_.elementAt(i);
                    row.addColumn(column[i]);
                }
            }
        }
    }

    /**
    *  Adds a column header to the end of the table header.
    *  @param header The column header.
    **/
    public void addColumnHeader(String header)
    {
        addColumnHeader(new HTMLTableHeader(new HTMLText(header)));
    }

    /**
    *  Adds a column header to the end of the table header.
    *  @param header The column header.
    **/
    public void addColumnHeader(HTMLTableHeader header)
    {
        if (header == null)
            throw new NullPointerException("header");

        // Verify that the header's HTMLTagElement is set.
        if (header.getElement() == null)
        {
            Trace.log(Trace.ERROR, "The HTMLTableHeader's element attribute is invalid.");
            throw new ExtendedIllegalArgumentException("header", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (headerTag_ == null)
            headerTag_ = new Vector();

        headerTag_.addElement(header);
    }

    /**
    *  Adds a row to the end of the table.
    *  @param row An HTMLTableRow object containing the row data.
    **/
    public void addRow(HTMLTableRow row)          // @B2C
    {
        //@C2D

        if (row == null)
            throw new NullPointerException("row");

        rows_.addElement(row);
        fireAdded();            // Fire the row added event.
    }



    /**
    *  Adds an ElementListener for the rows.
    *  The ElementListener object is added to an internal list of RowListeners;
    *  it can be removed with removeRowListener.
    *    @see #removeRowListener
    *    @param listener The ElementListener.
    **/
    public void addRowListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (rowListeners_ == null) rowListeners_ = new Vector(); //@CRS
        rowListeners_.addElement(listener);
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
    *  Fires a ELEMENT_ADDED event.
    **/
    private void fireAdded()
    {
      if (rowListeners_ == null) return; //@CRS
        Vector targets = (Vector) rowListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_ADDED);
        for (int i=0; i<targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementAdded(event);
        }
    }

    /**
    *  Fires a ELEMENT_CHANGED event.
    **/
    private void fireChanged()
    {
      if (rowListeners_ == null) return; //@CRS
        Vector targets = (Vector) rowListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_CHANGED);
        for (int i=0; i<targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementChanged(event);
        }
    }

    /**
    *  Fires a ELEMENT_REMOVED event.
    **/
    private void fireRemoved()
    {
      if (rowListeners_ == null) return; //@CRS
        Vector targets = (Vector) rowListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_REMOVED);
        for (int i=0; i< targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementRemoved(event);
        }
    }

    /**
    *  Returns the table horizontal alignment.
    *  @return The table alignment.
    **/
    public String getAlignment()
    {
        return alignment_;
    }

    /**
    *  Returns the border width.  A value of zero indicates no border.
    *  @return The border width.
    **/
    public int getBorderWidth()
    {
        return borderWidth_;
    }

    /**
    *  Returns the table caption.
    *  @return An HTMLTableCaption object containing the table caption.
    **/
    public HTMLTableCaption getCaption()
    {
        return caption_;
    }

    /**
    *  Returns the global table cell padding.  The cell padding is the spacing
    *  between data in a table cell and the border of the cell.
    *  @return The cell padding.
    **/
    public int getCellPadding()
    {
        return cellPadding_;
    }

    /**
    *  Returns the global table cell spacing.
    *  The cell spacing is the spacing between the cells.
    *  @return The cell spacing.
    **/
    public int getCellSpacing()
    {
        return cellSpacing_;
    }

    /**
    *  Returns a column in the table as an array of HTMLTableCell objects.
    *  @param columnIndex The index of the table column (0-based).
    *  @return An array of HTMLTableCell objects.
    **/
    public HTMLTableCell[] getColumn(int columnIndex)
    {
        // Validate the table is valid.
        if (rows_.size() == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to get a column before adding a row to the table.");
            throw new ExtendedIllegalStateException("rows", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Get the cells.
        HTMLTableCell[] list = new HTMLTableCell[rows_.size()];
        HTMLTableRow row;
        int size = rows_.size();
        for (int i=0; i< size; i++)
        {
            row = (HTMLTableRow)rows_.elementAt(i);
            list[i] = row.getColumn(columnIndex);               // columnIndex parameter validation done here.
        }
        return list;
    }

    /**
    *  Returns the table header tag for the specified <i>columnIndex</i>.
    *  @param columnIndex The index of the column header (0-based).
    *  @return The table header tag.
    **/
    public HTMLTableHeader getColumnHeader(int columnIndex)
    {
        if (columnIndex < 0 || columnIndex >= headerTag_.size())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return(HTMLTableHeader)headerTag_.elementAt(columnIndex);
    }

    /**
     *  Returns the <i>direction</i> of the text interpretation.
     *  @return The direction of the text.
     **/
    public String getDirection()                               //$B1A
    {
        return dir_;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 //$B1A
    {
        //@C2D

        if ((dir_ != null) && (dir_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" dir=\"");
            buffer.append(dir_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }

    /**
    *  Returns the table end tag.
    *  @return The tag.
    **/
    private String getEndTableTag()
    {
        return "</table>\n";
    }

    /**
    *  Returns the table column header tags.
    *  @return The header tags or null if the header is not set.
    **/
    public HTMLTableHeader[] getHeader()
    {
        if (headerTag_ == null)
            return null;
        else
        {
            HTMLTableHeader[] list = new HTMLTableHeader[headerTag_.size()];
            headerTag_.copyInto(list);
            return list;
        }
    }

    /**
    *  Returns the HTML tag for the table column headers.
    *  @return The HTML table header tag or an empty String
    *  if the header is not set.
    **/
    public String getHeaderTag()
    {
        if (headerTag_ == null)
            return "";
        else
        {
            StringBuffer tag = new StringBuffer();
            tag.append("<tr>\n");

            HTMLTableHeader colHeader;
            int size = headerTag_.size();

            for (int i=0; i< size; i++)
            {
                colHeader = (HTMLTableHeader)headerTag_.elementAt(i);
                tag.append(colHeader.getTag());
            }
            tag.append("</tr>\n");
            return new String(tag);
        }
    }

    /**
     *  Returns the <i>language</i> of the caption.
     *  @return The language of the caption.
     **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.
    *  @return The language tag.
    **/
    String getLanguageAttributeTag()                                                  //$B1A
    {
        //@C2D

        if ((lang_ != null) && (lang_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" lang=\"");
            buffer.append(lang_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }

    /**
    *  Returns the number of rows in the table.
    *  @return The number of rows.
    **/
    public int getRowCount()
    {
        return rows_.size();
    }

    /**
    *  Returns the HTMLTableRow object for the specified <i>rowIndex</i>.
    *  @param rowIndex The index of the table row (0-based).
    *  @return The table row object.
    **/
    public HTMLTableRow getRow(int rowIndex)
    {
        if (rowIndex < 0 || rowIndex >= rows_.size())
            throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return(HTMLTableRow)rows_.elementAt(rowIndex);
    }

    /**
    *  Returns the start table tag.
    *  @return The tag.
    **/
    private String getStartTableTag()
    {
        //@C2D

        StringBuffer tag = new StringBuffer("<table");

        if (alignment_ != null)
        {
            tag.append(" align=\"");
            tag.append(alignment_);
            tag.append("\"");
        }
        if (borderWidth_ > 0)
        {
            tag.append(" border=\"");
            tag.append(borderWidth_);
            tag.append("\"");
        }
        if (cellPadding_ >= 0)                // @C1C
        {
            tag.append(" cellpadding=\"");
            tag.append(cellPadding_);
            tag.append("\"");
        }
        if (cellSpacing_ >= 0)                // @C1C
        {
            tag.append(" cellspacing=\"");      
            tag.append(cellSpacing_);
            tag.append("\"");
        }
        if (width_ > 0)
        {
            tag.append(" width=\"");
            tag.append(width_);

            if (widthPercent_)
                tag.append("%");
            tag.append("\"");
        }

        tag.append(getLanguageAttributeTag());        //$B1A
        tag.append(getDirectionAttributeTag());       //$B1A
        tag.append(getAttributeString());             // @Z1A

        tag.append(">\n");

        if (caption_ != null)
            tag.append(caption_.getTag());

        return new String(tag);
    }

    /**
    *  Returns the HTML table tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C2D

        StringBuffer tag = new StringBuffer(getStartTableTag());

        // Add the column headers.
        if (headerInUse_)
        {
            if (rows_.size() > 0)
            {
                // Verify that the header is set.
                if (headerTag_ == null)
                {
                    Trace.log(Trace.ERROR, "Attempting to get the table tag before setting the table header.");
                    throw new ExtendedIllegalStateException("header", ExtendedIllegalStateException.PROPERTY_NOT_SET);
                }

                int hdrSize = headerTag_.size();

                for (int i=0; i<rows_.size(); ++i)                                                               // @B2A
                {
                    // @B2A
                    // Verify that the table header size greater or equal to the number of columns in a row.      // @B2C
                    if (hdrSize < ((HTMLTableRow)rows_.elementAt(i)).getColumnCount() )                           // @B2C
                    {
                        Trace.log(Trace.ERROR, "Attempting to get the table tag when the length of the table header is invalid.");
                        throw new ExtendedIllegalArgumentException("header or row " + i, ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
                    }
                }
            }
            tag.append(getHeaderTag());
        }

        // Add the rows.
        HTMLTableRow row;
        int size = rows_.size();
        for (int i=0; i< size; i++)
        {
            row = (HTMLTableRow)rows_.elementAt(i);
            tag.append(row.getTag());
        }
        tag.append(getEndTableTag());

        return new String(tag);
    }

    /**
    *  Returns the table width in pixels or percent.
    *  @return The table width.
    *  @see #isWidthInPercent
    **/
    public int getWidth()
    {
        return width_;
    }



    /**
    *  Indicates if the table column header should be used.
    *  @return true if column header should be used; false otherwise.
    **/
    public boolean isHeaderInUse()
    {
        return headerInUse_;
    }

    /**
    *  Indicates if the table width is in percent or pixels.
    *  @return true if percent, false if pixels.
    *  @see #getWidth
    **/
    public boolean isWidthInPercent()
    {
        return widthPercent_;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        //@CRS rowListeners_ = new Vector();
        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
    }

    /**
    *  Removes all the rows from the table.
    **/
    public void removeAllRows()
    {
        //@C2D

        rows_.removeAllElements();
        fireRemoved();
    }

    /**
    *  Removes a column from the table at the specified <i>columnIndex</i>.
    *  If the column header exists it is also removed.
    *  @param columnIndex The index of the column to be removed (0-based).
    **/
    public void removeColumn(int columnIndex)
    {
        // Validate the columnIndex parameter.
        if (rows_.size() == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to remove a column before adding a row to the table.");
            throw new ExtendedIllegalStateException("rows", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        HTMLTableRow row;
        synchronized (rows_)
        {
            int size = rows_.size();
            for (int i=0; i< size; i++)
            {
                row = (HTMLTableRow)rows_.elementAt(i);
                row.removeColumn( (HTMLTableCell)row.getColumn(columnIndex) );
            }
            // Remove the column header.
            if (headerTag_ != null && columnIndex < headerTag_.size())
                removeColumnHeader(columnIndex);
        }
    }

    /**
    *  Removes the column header at the specified <i>columnIndex</i>.
    *  @param columnIndex The index of the column header to be removed (0-based).
    **/
    public void removeColumnHeader(int columnIndex)
    {
        // Verify that the column header list exists.
        if (headerTag_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to remove a column header before adding the header list to the table.");
            throw new ExtendedIllegalStateException("header", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (columnIndex < 0 || columnIndex >= headerTag_.size())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        headerTag_.removeElementAt(columnIndex);
    }

    /**
    *  Removes a column header from the table header.
    *  @param header The column header.
    **/
    public void removeColumnHeader(HTMLTableHeader header)
    {
        // Verify that the column header list exists.
        if (headerTag_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to remove a column header before adding the header list to the table.");
            throw new ExtendedIllegalStateException("header", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (header == null)
            throw new NullPointerException("header");

        if (!headerTag_.removeElement(header))
            throw new ExtendedIllegalArgumentException("header", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    /**
    *  Removes the row from the table.
    *  @param row An HTMLTableRow object containing the row data.
    **/
    public void removeRow(HTMLTableRow row)
    {
        //@C2D

        // Validate the row parameter.
        if (row == null)
            throw new NullPointerException("row");

        // Verify the table is not empty.
        if (rows_.size() == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to remove a row when the table is empty.");
            throw new ExtendedIllegalStateException("rows", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Remove the row and notify the listeners.
        if (rows_.removeElement(row))
            fireRemoved();
    }

    /**
    *  Removes the row at the specified <i>rowIndex</i>.
    *  @param rowIndex The index of the row to be removed (0-based).
    **/
    public void removeRow(int rowIndex)
    {
        //@C2D

        // Validate the rowIndex parameter.
        if (rowIndex < 0 || rowIndex >= rows_.size())
            throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        // Remove the row and notify the listeners.
        rows_.removeElementAt(rowIndex);
        fireRemoved();
    }



    /**
    *  Removes this row ElementListener from the internal list.
    *  If the ElementListener is not on the list, nothing is done.
    *  @see #addRowListener
    *  @param listener The ElementListener.
    **/
    public void removeRowListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (rowListeners_ != null) rowListeners_.removeElement(listener); //@CRS
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
    *  Sets the table horizontal alignment.  The default value is LEFT.
    *  @param alignment The table alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, CENTER, or RIGHT.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setAlignment(String alignment) throws PropertyVetoException
    {
        if (alignment == null)
        {
            throw new NullPointerException("alignment");
        }
        else if (alignment.equalsIgnoreCase(LEFT) ||
                 alignment.equalsIgnoreCase(CENTER) ||
                 alignment.equalsIgnoreCase(RIGHT))
        {
            String old = alignment_;
            if (vetos_ != null) vetos_.fireVetoableChange("alignment", old, alignment ); //@CRS

            alignment_ = alignment;

            if (changes_ != null) changes_.firePropertyChange("alignment", old, alignment ); //@CRS
        }
        else
        {
            throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

    /**
    *  Sets the border width in pixels.  A value of zero indicates no border.
    *  The default value is zero.
    *  @param borderWidth The border width.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setBorderWidth(int borderWidth) throws PropertyVetoException
    {
        if (borderWidth < 0)
            throw new ExtendedIllegalArgumentException("borderWidth", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@CRS Integer oldWidth = new Integer(borderWidth_);
        //@CRS Integer newWidth = new Integer(borderWidth);
        int oldWidth = borderWidth_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("borderWidth", new Integer(oldWidth), new Integer(borderWidth)); //@CRS

        borderWidth_ = borderWidth;

        if (changes_ != null) changes_.firePropertyChange("borderWidth", new Integer(oldWidth), new Integer(borderWidth)); //@CRS
    }

    /**
    *  Sets the table caption.
    *  @param caption The table caption.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setCaption(String caption) throws PropertyVetoException
    {
        setCaption(new HTMLTableCaption(new HTMLText(caption)));
    }

    /**
    *  Sets the table caption.
    *  @param caption An HTMLTableCaption object containing the table caption.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setCaption(HTMLTableCaption caption) throws PropertyVetoException
    {
        if (caption == null)
            throw new NullPointerException("caption");

        HTMLTableCaption old = caption_;
        if (vetos_ != null) vetos_.fireVetoableChange("caption", old, caption ); //@CRS

        caption_ = caption;

        if (changes_ != null) changes_.firePropertyChange("caption", old, caption ); //@CRS
    }

    /**
    *  Sets the global table cell padding.  The cell padding is the spacing between
    *  data in a table cell and the border of the cell.
    *  The default value is -1 (browser default used).                     
    *  @param cellPadding The cell padding.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setCellPadding(int cellPadding) throws PropertyVetoException
    {
        if (cellPadding < -1)                                               // @C1C
            throw new ExtendedIllegalArgumentException("cellPadding", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@CRS Integer oldPadding = new Integer(cellPadding_);
        //@CRS Integer newPadding = new Integer(cellPadding);
        int oldPadding = cellPadding_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("cellPadding", new Integer(oldPadding), new Integer(cellPadding)); //@CRS

        cellPadding_ = cellPadding;

        if (changes_ != null) changes_.firePropertyChange("cellPadding", new Integer(oldPadding), new Integer(cellPadding)); //@CRS
    }

    /**
    *  Sets the global table cell spacing.
    *  The cell spacing is the spacing between the cells.
    *  The default value is -1 (browser default used).                      
    *  @param cellSpacing The cell spacing.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setCellSpacing(int cellSpacing) throws PropertyVetoException
    {
        if (cellSpacing < -1)                                                // @C1C
            throw new ExtendedIllegalArgumentException("cellSpacing", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@CRS Integer oldSpacing = new Integer(cellSpacing_);
        //@CRS Integer newSpacing = new Integer(cellSpacing);
        int oldSpacing = cellSpacing_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("cellSpacing", new Integer(oldSpacing), new Integer(cellSpacing)); //@CRS

        cellSpacing_ = cellSpacing;

        if (changes_ != null) changes_.firePropertyChange("cellSpacing", new Integer(oldSpacing), new Integer(cellSpacing)); //@CRS
    }

    /**
    *  Sets a column in the table at the specified <i>columnIndex</i>.
    *  @param column An array of HTMLTableCell objects containing the column data.
    *  @param columnIndex The index of the column (0-based).
    **/
    public void setColumn(HTMLTableCell[] column, int columnIndex)
    {
        // Validate the column parameter.
        if (column == null)
            throw new NullPointerException("column");

        int size = rows_.size();

        // Add the rows if table is empty.
        if (size == 0)
        {
            addColumn(column);
            return;
        }
        else if (column.length != size)
        {
            throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        // Validate the columnIndex parameter.
        if (columnIndex < 0 || columnIndex > ((HTMLTableRow)rows_.elementAt(0)).getColumnCount())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        // Update the table rows.
        HTMLTableRow row;
        synchronized (rows_)
        {
            for (int i=0; i< size; i++)
            {
                row = (HTMLTableRow)rows_.elementAt(i);
                row.setColumn(column[i], columnIndex);
            }
        }
    }

    /**
    *  Sets the table column header tag.
    *  @param header The table column header.
    *  @param columnIndex The index of the column to be changed (0-based).
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setColumnHeader(String header, int columnIndex) throws PropertyVetoException
    {
        setColumnHeader(new HTMLTableHeader(new HTMLText(header)), columnIndex);
    }

    /**
    *  Sets the table column header tag at the specified <i>columnIndex</i>.
    *  @param header The table column header.
    *  @param columnIndex The index of the column to be changed (0-based).
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setColumnHeader(HTMLTableHeader header, int columnIndex) throws PropertyVetoException
    {
        // Validate the header parameter.
        if (header == null)
            throw new NullPointerException("header");

        // Validate that the header tag exists.
        if (headerTag_ == null)
        {
            if (columnIndex == 0)
            {
                addColumnHeader(header);
                return;
            }
            else
            {
                Trace.log(Trace.ERROR, "Attempting to change a column header before adding a column header to the table.");
                throw new ExtendedIllegalStateException("header", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
        }

        // Verify that the header's HTMLTagElement is set.
        if (header.getElement() == null)
        {
            Trace.log(Trace.ERROR, "The HTMLTableHeader's element attribute is invalid.");
            throw new ExtendedIllegalArgumentException("header", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Validate the columnIndex parameter.
        if (columnIndex < 0 || columnIndex > headerTag_.size())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        else if (columnIndex == headerTag_.size())
        {
            addColumnHeader(header);
            return;
        }
        else
        {
            Vector old = headerTag_;
            if (vetos_ != null) vetos_.fireVetoableChange("header", old, headerTag_); //@CRS

            headerTag_.setElementAt(header, columnIndex);

            if (changes_ != null) changes_.firePropertyChange("header", old, headerTag_); //@CRS
        }
    }

    /**
     *  Sets the <i>direction</i> of the text interpretation.
     *  @param dir The direction.  One of the following constants
     *  defined in HTMLConstants:  LTR or RTL.
     *
     *  @see com.ibm.as400.util.html.HTMLConstants
     *
     *  @exception PropertyVetoException If a change is vetoed.
     **/
    public void setDirection(String dir)                                     //$B1A
    throws PropertyVetoException
    {
        if (dir == null)
            throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
        {
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = dir_;
        if (vetos_ != null) vetos_.fireVetoableChange("dir", old, dir ); //@CRS

        dir_ = dir;

        if (changes_ != null) changes_.firePropertyChange("dir", old, dir ); //@CRS
    }

    /**
    *  Sets the table column headers.
    *  @param header The column headers.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see #setHeaderInUse
    **/
    public void setHeader(HTMLTableHeader[] header) throws PropertyVetoException
    {
        if (header == null)
            throw new NullPointerException("header");

        // Verify that the header size matches the number of columns in a row.
        if ((!rows_.isEmpty()) && header.length != ((HTMLTableRow)rows_.elementAt(0)).getColumnCount())
            throw new ExtendedIllegalArgumentException("header", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        Vector old = headerTag_;
        if (vetos_ != null) vetos_.fireVetoableChange("header", old, header ); //@CRS

        headerTag_ = new Vector();
        for (int i=0; i< header.length; i++)
            headerTag_.addElement(header[i]);

        if (changes_ != null) changes_.firePropertyChange("header", old, header ); //@CRS
    }

    /**
    *  Sets the table column headers.
    *  @param header The column headers.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setHeader(String[] header) throws PropertyVetoException
    {
        if (header == null)
            throw new NullPointerException("header");

        // Create an array of HTMLTableHeader objects.
        HTMLTableHeader[] tableHeader = new HTMLTableHeader[header.length];
        for (int column=0; column < header.length; column++)
            tableHeader[column] = new HTMLTableHeader(new HTMLText(header[column]));

        setHeader(tableHeader);
    }

    /**
    *  Sets if table column headers should be used.  The default value is true.
    *  @param headerInUse true if the column headers should be used; false otherwise.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setHeaderInUse(boolean headerInUse) throws PropertyVetoException
    {
        //@CRS Boolean oldUse = new Boolean(headerInUse_);
        //@CRS Boolean newUse = new Boolean(headerInUse);
        boolean oldUse = headerInUse_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("useHeader", new Boolean(oldUse), new Boolean(headerInUse)); //@CRS

        headerInUse_ = headerInUse;

        if (changes_ != null) changes_.firePropertyChange("useHeader", new Boolean(oldUse), new Boolean(headerInUse)); //@CRS
    }

    /**
     *  Sets the <i>language</i> of the caption.
     *  @param lang The language.  Example language tags include:
     *  en and en-US.
     *
     *  @exception PropertyVetoException If a change is vetoed.
     **/
    public void setLanguage(String lang)                                      //$B1A
    throws PropertyVetoException
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;
        if (vetos_ != null) vetos_.fireVetoableChange("lang", old, lang ); //@CRS

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
    }

    /**
    *  Sets the table row at the specified <i>rowIndex</i>.
    *  @param row An HTMLTableRow object with the row data.
    *  @param rowIndex The index of the row (0-based).
    **/
    public void setRow(HTMLTableRow row, int rowIndex)
    {
        //@C2D

        // Validate the row parameter.
        if (row == null)
            throw new NullPointerException("row");

        // Validate the rowIndex parameter.
        if (rowIndex < 0 || rowIndex > rows_.size())
            throw new ExtendedIllegalArgumentException("rowIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        // Set the row.
        if (rowIndex == rows_.size())
            addRow(row);
        else
        {
            // Validate the number of columns in the row.
            if (row.getColumnCount() != ((HTMLTableRow)rows_.elementAt(0)).getColumnCount())
                throw new ExtendedIllegalArgumentException("row", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

            rows_.setElementAt(row, rowIndex);
            // Notify the listeners.
            fireChanged();
        }
    }

    /**
    *  Sets the table width.  The default width unit is pixels.
    *  @param width The table width.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see #setWidthInPercent
    **/
    public void setWidth(int width) throws PropertyVetoException
    {
        if (width < 0)
            throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@CRS Integer oldWidth = new Integer(width_);
        //@CRS Integer newWidth = new Integer(width);
        int oldWidth = width_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("width", new Integer(oldWidth), new Integer(width)); //@CRS

        width_ = width;

        if (changes_ != null) changes_.firePropertyChange("width", new Integer(oldWidth), new Integer(width)); //@CRS
    }

    /**
    *  Sets the table width in percent or pixels.
    *  @param width The table width.
    *  @param widthInPercent true if width is specified as a percent; false if width is specified in pixels.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setWidth(int width, boolean widthInPercent) throws PropertyVetoException
    {
        int oldWidth = width_;

        setWidth(width);

        try
        {
            setWidthInPercent(widthInPercent);
        }
        catch (PropertyVetoException e)
        {
            // restore the original width.
            width_ = oldWidth;
            throw new PropertyVetoException("widthInPercent", e.getPropertyChangeEvent());
        }
    }

    /**
    *  Sets the table width unit in percent or pixels.  The default is false.
    *  @param widthInPercent true if width is specified as a percent; false if width is specified in pixels.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see #setWidth
    **/
    public void setWidthInPercent(boolean widthInPercent) throws PropertyVetoException
    {
        //@CRS Boolean oldValue = new Boolean(widthPercent_);
        //@CRS Boolean newValue = new Boolean(widthInPercent);
        boolean oldValue = widthPercent_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("widthInPercent", new Boolean(oldValue), new Boolean(widthInPercent)); //@CRS

        widthPercent_ = widthInPercent;

        if (changes_ != null) changes_.firePropertyChange("widthInPercent", new Boolean(oldValue), new Boolean(widthInPercent)); //@CRS
    }

    /**
    *  Returns the HTML table tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}





