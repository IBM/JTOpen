///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTableRow.java
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
import java.io.Serializable;
import java.util.Vector;

/**
*  The HTMLTableRow class represents an HTML row tag.
*
*  <P>This example creates an HTMLTableRow object and sets the attributes.
*  <BLOCKQUOTE><PRE>
*  HTMLTableRow row = new HTMLTableRow();
*  row.setHorizontalAlignment(HTMLTableRow.CENTER);
*  row.setVerticalAlignment(HTMLTableRow.MIDDLE);
*  // Add the columns to the row (Assume that the HTMLTableCell objects are already created).
*  row.addColumn(column1);
*  row.addColumn(column2);
*  row.addColumn(column3);
*  row.addColumn(column4);
*  System.out.println(row.getTag());
*  </PRE></BLOCKQUOTE>
*  Here is the output of the tag:
*  <BLOCKQUOTE><PRE>
*  &lt;tr align="center" valign="middle"&gt;
*  &lt;td&gt;data1&lt;/td&gt;
*  &lt;td&gt;data2&lt;/td&gt;
*  &lt;td&gt;data3&lt;/td&gt;
*  &lt;td&gt;data4&lt;/td&gt;
*  &lt;/tr&gt;
*  </PRE></BLOCKQUOTE>
*  <p>
*  Using XSL Formatting Objects, the output generated is:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:table-row text-align="center"&gt;
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px'&gt;&lt;fo:block-container&gt;
*  &lt;fo:block&gt;data1&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:table-cell&gt;
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px'&gt;&lt;fo:block-container&gt;
*  &lt;fo:block&gt;data2&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:table-cell&gt;
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px'&gt;&lt;fo:block-container&gt;
*  &lt;fo:block&gt;data3&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:table-cell&gt;
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px'&gt;&lt;fo:block-container&gt;
*  &lt;fo:block&gt;data4&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:table-cell&gt;
*  &lt;/fo:table-row&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLTableRow objects generate the following events:
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
*  @see com.ibm.as400.util.html.HTMLTable
*  @see com.ibm.as400.util.html.HTMLTableCell
**/
public class HTMLTableRow extends HTMLTagAttributes implements HTMLConstants, Serializable   // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private Vector row_;      // The columns in the row.
    private String hAlign_;   // The global horizontal alignment for each cells in the row.
    private String vAlign_;   // The global vertical alignment for each cell in the row.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    private boolean useFO_ = false;    // Sets use fo tag.                                     //@D1A
    private int borderWidth_ = 1;      // The border width                                     //@D1A
    private int cellPadding_ = 1;      // The cell padding                                     //@D1A

    transient private Vector columnListeners_;      // The list of column listeners. @CRS
    transient private VetoableChangeSupport vetos_; //@CRS

    /**
    *  Constructs a default HTMLTableRow object.
    **/
    public HTMLTableRow()
    {
        row_ = new Vector();
    }

    /**
    *  Constructs an HTMLTableRow object with the specified <i>cells</i>.
    *  @param cells The HTMLTableCell array.
    **/
    public HTMLTableRow(HTMLTableCell[] cells)
    {
        this();

        if (cells == null)
            throw new NullPointerException("cells");

        for (int i=0; i< cells.length; i++)
            row_.addElement(cells[i]);
    }

    /**
    *  Adds the column to the row.
    *  @param cell The HTMLTableCell containing the column data.
    **/
    public void addColumn(HTMLTableCell cell)
    {
        //@C1D

        if (cell == null)
            throw new NullPointerException("cell");

        row_.addElement(cell);

        // Notify the listeners.
        fireAdded();
    }

    /**
    *  Adds an ElementListener for the columns.
    *  The ElementListener object is added to an internal list of ColumnListeners;
    *  it can be removed with removeColumnListener.
    *    @see #removeColumnListener
    *    @param listener The ElementListener.
    **/
    public void addColumnListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (columnListeners_ == null) columnListeners_ = new Vector(); //@CRS
        columnListeners_.addElement(listener);
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
      if (columnListeners_ == null) return; //@CRS
        Vector targets = (Vector) columnListeners_.clone();
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
      if (columnListeners_ == null) return; //@CRS
        Vector targets = (Vector) columnListeners_.clone();
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
      if (columnListeners_ == null) return; //@CRS
        Vector targets = (Vector) columnListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_REMOVED);
        for (int i=0; i<targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementRemoved(event);
        }
    }

    /**
    *  Returns the column at the specified <i>columnIndex</i>.
    *  @param columnIndex - The column index.
    *  @return An HTMLTableCell object with the column data.
    **/
    public HTMLTableCell getColumn(int columnIndex)
    {
        if (columnIndex < 0 || columnIndex >= row_.size())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return(HTMLTableCell)row_.elementAt(columnIndex);
    }

    /**
    *  Returns the number of columns in the row.
    *  @return The number of columns.
    **/
    public int getColumnCount()
    {
        return row_.size();
    }

    /**
    *  Returns the column index of the specified <i>cell</i>.
    *  @param cell An HTMLTableCell object that contains the cell data.
    *  @return The column index of the cell.  Returns -1 if the column is not found.
    **/
    public int getColumnIndex(HTMLTableCell cell)
    {
        if (cell == null)
            throw new NullPointerException("cell");

        return row_.indexOf(cell);
    }

    /**
    *  Returns the column index of the specified <i>cell</i>.
    *  @param cell An HTMLTableCell object that contains the cell data.
    *  @param index The column index to start searching from.
    *  @return The column index of the cell.  Returns -1 if the column is not found.
    **/
    public int getColumnIndex(HTMLTableCell cell, int index )
    {
        if (cell == null)
            throw new NullPointerException("cell");

        if (index >= row_.size() || index < 0)
            throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return row_.indexOf(cell, index);
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
        //@C1D

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
    *  Returns the global horizontal alignment for the row.
    *  @return The horizontal alignment.  One of the following constants
    *  defined in HTMLConstants:  CENTER, LEFT, or RIGHT.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getHorizontalAlignment()
    {
        return hAlign_;
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
        //@C1D

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
    *  Returns the table row tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        if(useFO_)                      //@D1A
            return getFOTag();          //@D1A

        if (row_.size() == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before adding a column to the row.");
            throw new ExtendedIllegalStateException("column", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        StringBuffer tag = new StringBuffer("<tr");
        if (hAlign_ != null)
        {
            tag.append(" align=\"");
            tag.append(hAlign_);
            tag.append("\"");
        }
        if (vAlign_ != null)
        {
            tag.append(" valign=\"");
            tag.append(vAlign_);
            tag.append("\"");
        }

        tag.append(getLanguageAttributeTag());      //$B1A
        tag.append(getDirectionAttributeTag());     //$B1A
        tag.append(getAttributeString());           // @Z1A
        tag.append(">\n");

        for (int i=0; i< row_.size(); i++)
        {
            HTMLTableCell cell = (HTMLTableCell)row_.elementAt(i);
            tag.append(cell.getTag());
        }
        tag.append("</tr>\n");

        
        return tag.toString();                          //@D1C
        
    }

    /** 
    *  Returns the XSL-FO table row tag.
    *  The valign, halign, and language attributes are not supported in XSL-FO.
    *  @return The xsl-fo tag.
    **/
    public String getFOTag()                            //@D1A
    {
        //Store the current state of useFO_
        boolean useFO = useFO_;

        setUseFO(true);
        
        int size = row_.size();
        if (size == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before adding a column to the row.");
            throw new ExtendedIllegalStateException("column", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        StringBuffer tag = new StringBuffer("");
        
        tag.append("<fo:table-row");
        if (hAlign_ != null)
        {
            tag.append(" text-align=\"");
            if(hAlign_.equalsIgnoreCase("center"))
                tag.append("center");
            else if(hAlign_.equalsIgnoreCase("left"))
                tag.append("start");
            else if(hAlign_.equalsIgnoreCase("right"))
                tag.append("end");
            tag.append("\"");
        } 
        tag.append(">\n");

        for (int i=0; i< size; i++)
        {
            //Must specify the cell border width and padding for each cell
            HTMLTableCell cell = (HTMLTableCell)row_.elementAt(i);
            cell.setBorderWidth(borderWidth_);
            if(cellPadding_ > -1)
                cell.setCellPadding(cellPadding_);
            if ((dir_ != null) && (dir_.length() > 0))
            {
                try
                {
                    cell.setDirection(dir_);
                }
                catch(PropertyVetoException p)
                {
                }
            }
            tag.append(cell.getFOTag());
        }
        tag.append("</fo:table-row>\n");
        
        //Set useFO_ to previous state
        setUseFO(useFO);

        return tag.toString();                                                          //@D1C
    }

    /**
    *  Returns the global vertical alignment for the row.
    *  @return The vertical alignment.  One of the following constants
    *  defined in HTMLConstants:  BASELINE, BOTTOM, MIDDLE, or TOP.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getVerticalAlignment()
    {
        return vAlign_;
    }



    /**
    *  Returns if Formatting Object tags are outputted.
    *  The default value is false.
    *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
    **/
    public boolean isUseFO()                                      //@D1A
    {
        return useFO_;
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
        //@CRS columnListeners_ = new Vector();
    }

    /**
    *  Removes all the columns from the row.
    **/
    public void removeAllColumns()
    {
        //@C1D

        row_.removeAllElements();
        fireRemoved();
    }

    /**
    *  Removes the column element from the row.
    *  @param cell The HTMLTableCell object to be removed.
    **/
    public void removeColumn(HTMLTableCell cell)
    {
        //@C1D

        if (cell == null)
            throw new NullPointerException("cell");

        if (row_.removeElement(cell))
            fireRemoved();             // Fire the column removed event.
    }

    /**
    *  Removes the column at the specified <i>columnIndex</i>.
    *  @param columnIndex The column index.
    **/
    public void removeColumn(int columnIndex)
    {
        //@C1D

        if (columnIndex < 0 || columnIndex >= row_.size())
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        row_.removeElement((HTMLTableCell)row_.elementAt(columnIndex));
        fireRemoved();
    }

    /**
    *  Removes this column ElementListener from the internal list.
    *  If the ElementListener is not on the list, nothing is done.
    *  @see #addColumnListener
    *  @param listener The ElementListener.
    **/
    public void removeColumnListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (columnListeners_ != null) columnListeners_.removeElement(listener); //@CRS
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
    *  Sets the column element at the specified <i>column</i>.
    *  @param cell The HTMLTableCell object to be added.
    *  @param columnIndex The column index.
    **/
    public void setColumn(HTMLTableCell cell, int columnIndex)
    {
        //@C1D

        // Validate the cell parameter.
        if (cell == null)
            throw new NullPointerException("cell");
        // Validate the column parameter.
        if (columnIndex > row_.size() || columnIndex < 0)
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        // Set the column.
        if (columnIndex == row_.size())
            addColumn(cell);
        else
        {
            row_.setElementAt(cell, columnIndex);
            fireChanged();             // Notify the listeners.
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
    *  Sets the global horizontal alignment for the row.
    *  @param alignment The horizontal alignment.  One of the following constants
    *  defined in HTMLConstants:  CENTER, LEFT, or RIGHT.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setHorizontalAlignment(String alignment) throws PropertyVetoException
    {
        if (alignment == null)
        {
            throw new NullPointerException("alignment");
        }
        else if (alignment.equalsIgnoreCase(LEFT) ||
                 alignment.equalsIgnoreCase(CENTER) ||
                 alignment.equalsIgnoreCase(RIGHT))
        {
            String old = hAlign_;
            if (vetos_ != null) vetos_.fireVetoableChange("alignment", old, alignment ); //@CRS

            hAlign_ = alignment;

            if (changes_ != null) changes_.firePropertyChange("alignment", old, alignment ); //@CRS
        }
        else
        {
            throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
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
    *  Sets the border width in pixels.  A value of zero indicates no border.
    *  The default value is one.
    *  @param borderWidth The border width.
    **/
    public void setBorderWidth(int borderWidth)             //@D1A
    {
        Integer oldWidth = new Integer(borderWidth_);
        Integer newWidth = new Integer(borderWidth);

        borderWidth_ = borderWidth;

        if (changes_ != null) changes_.firePropertyChange("borderWidth", oldWidth, newWidth);
    }

    /**
    *  Sets the global table cell padding.  The cell padding is the spacing between
    *  data in a table cell and the border of the cell.
    *  The default value is 1.                     
    *  @param cellPadding The cell padding.
    **/
    public void setCellPadding(int cellPadding)                   //@D1A
    {
        Integer oldPadding = new Integer(cellPadding_);
        Integer newPadding = new Integer(cellPadding);

        cellPadding_ = cellPadding;

        if (changes_ != null) changes_.firePropertyChange("cellPadding", oldPadding, newPadding);
    }

    /** 
    * Sets if Formatting Object tags should be used.
    *  The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML. 
    **/
    public void setUseFO(boolean useFO)        //@D1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
    *  Sets the global vertical alignment for the row.
    *  @param alignment The vertical alignment.  One of the following constants
    *  defined in HTMLConstants:  BASELINE, BOTTOM, MIDDLE, or TOP.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setVerticalAlignment(String alignment) throws PropertyVetoException
    {
        if (alignment == null)
        {
            throw new NullPointerException("alignment");
        }
        else if (alignment.equalsIgnoreCase(TOP) ||
                 alignment.equalsIgnoreCase(MIDDLE) ||
                 alignment.equalsIgnoreCase(BOTTOM) ||
                 alignment.equalsIgnoreCase(BASELINE))
        {
            String old = vAlign_;
            if (vetos_ != null) vetos_.fireVetoableChange("alignment", old, alignment ); //@CRS

            vAlign_ = alignment;

            if (changes_ != null) changes_.firePropertyChange("alignment", old, alignment ); //@CRS
        }
        else
        {
            throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

    /**
    *  Returns the HTML table row tag.
    *  @return The row tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
