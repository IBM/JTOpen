///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLTableRow.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
public class HTMLTableRow implements HTMLTagElement, HTMLConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private Vector row_;      // The columns in the row.
   private String hAlign_;   // The global horizontal alignment for each cells in the row.
   private String vAlign_;   // The global vertical alignment for each cell in the row.

   transient private Vector columnListeners_;      // The list of column listeners.
   transient private PropertyChangeSupport changes_;
   transient private VetoableChangeSupport vetos_;

   /**
   *  Constructs a default HTMLTableRow object.
   **/   
   public HTMLTableRow()
   {
      row_ = new Vector();
      initializeTransient();
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
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Adding column to the HTMLTableRow.");

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
      columnListeners_.addElement(listener);
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
   *  Fires a ELEMENT_ADDED event.
   **/
   private void fireAdded()
   {
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
   
      return (HTMLTableCell)row_.elementAt(columnIndex);
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
   *  Returns the table row tag.
   *  @return The tag.
   **/
   public String getTag()
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLTableRow tag...");

      if (row_.size() == 0)
      {
         Trace.log(Trace.ERROR, "Attempting to get tag before adding a column to the row.");
         throw new ExtendedIllegalStateException("column", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      StringBuffer tag = new StringBuffer("<tr");
      if (hAlign_ != null)
         tag.append(" align=\"" + hAlign_ + "\"");
      if (vAlign_ != null)
         tag.append(" valign=\"" + vAlign_ + "\"");
      tag.append(">\n");

      for (int i=0; i< row_.size(); i++)
      {
         HTMLTableCell cell = (HTMLTableCell)row_.elementAt(i);
         tag.append(cell.getTag());
      }
      tag.append("</tr>\n");
      
      return new String(tag);
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
   *  Provided to initialize transient data if this object is de-serialized.
   **/
   private void initializeTransient()
   {
      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
      columnListeners_ = new Vector();
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
   *  Removes all the columns from the row.
   **/
   public void removeAllColumns()
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Removing all columns from the HTMLTableRow.");

      row_.removeAllElements();
      fireRemoved();
   }

   /**
   *  Removes the column element from the row.
   *  @param cell The HTMLTableCell object to be removed.
   **/
   public void removeColumn(HTMLTableCell cell)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Removing a column from the HTMLTableRow.");

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
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Removing a column from the HTMLTableRow.");

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
      columnListeners_.removeElement(listener);
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
   *  Sets the column element at the specified <i>column</i>.
   *  @param cell The HTMLTableCell object to be added.
   *  @param columnIndex The column index.
   **/
   public void setColumn(HTMLTableCell cell, int columnIndex)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Changing a column in the HTMLTableRow.");

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
         vetos_.fireVetoableChange("alignment", old, alignment );
         
         hAlign_ = alignment;
         
         changes_.firePropertyChange("alignment", old, alignment );
      }
      else
      {
         throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
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
         vetos_.fireVetoableChange("alignment", old, alignment );
         
         vAlign_ = alignment;

         changes_.firePropertyChange("alignment", old, alignment );
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
