///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTableCell.java
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

/**
*  The HTMLTableCell class represents data in an HTML table cell.
*
*  <P>This example creates an HTML text HTMLTableCell object.
*
*  <P><BLOCKQUOTE><PRE>
*  // Create an HTMLText object.
*  HTMLText ibmText = new HTMLText("IBM");
*  ibmText.setBold(true);
*  ibmText.setItalic(true);
*  HTMLTableCell textCell = new HTMLTableCell(ibmText);
*  textCell.setHorizontalAlignment(HTMLConstants.CENTER);
*  System.out.println(textCell.getTag());
*  </PRE></BLOCKQUOTE>
*
*  Here is the output of the tag:
*  <P><BLOCKQUOTE><PRE>
*  <BR>&lt;td align="center"&gt;&lt;b&gt;&lt;i&gt;IBM&lt;/i&gt;&lt;/b&gt;&lt;/td&gt;
*  <P></PRE></BLOCKQUOTE>  
*
*  <P>This example creates an HTMLTableCell object with the element as an HTMLForm
*  object containing a submit button.
*
*  <P><BLOCKQUOTE><PRE>
*  HTMLTableCell formCell = new HTMLTableCell();
*  // create an HTMLForm object.
*  SubmitFormInput submitButton = new SubmitFormInput("Submit", "Send");
*  HTMLForm form = new HTMLForm("http://myCompany.com/myServlet");
*  form.addElement(submitButton);
*  // add the form to the table cell.
*  formCell.setElement(form);
*  System.out.println(formCell.getTag());
*  </PRE></BLOCKQUOTE>
*
*  Here is the output of the tag:
*  <P><BLOCKQUOTE><PRE>
*  &lt;td&gt;&lt;form action=&quot;http://myCompany.com/myServlet&quot; method=&quot;get&quot;&gt;
*  &lt;input type=&quot;submit&quot; value=&quot;Send&quot; /&gt;
*  &lt;/form&gt;&lt;/td&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLTableCell objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.HTMLTable
*  @see com.ibm.as400.util.html.HTMLTableRow
**/
public class HTMLTableCell extends HTMLTagAttributes implements HTMLConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private HTMLTagElement element_;       // The cell data.
                                           
   private String align_;                 // The cell horizontal alignment.
   private int colSpan_ = 1;              // The number of cell columns in the table the cell should span.
   private int height_;                   // The cell height in percent or pixels.
   private int rowSpan_ = 1;              // The number of cell rows in the table the cell should span.
   private String vAlign_;                // The cell vertical alignment.
   private int width_;                    // The cell width in percent or pixels.
   private boolean wrap_ = true;          // Indicates if normal HTML linebreaking conventions are used.

   private String lang_;        // The primary language used to display the tags contents.  //$B1A
   private String dir_;         // The direction of the text interpretation.                //$B1A

   private boolean heightPercent_ = false; // Indicates if the height is in percent.
   private boolean widthPercent_ = false;  // Indicates if the width is in percent.

   transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

   /**
   *  Constructs a default HTMLTableCell object.
   **/
   public HTMLTableCell()
   {

   }
   
   /**
   *  Constructs an HTMLTableCell.
   *  @param element The table cell element.
   **/
   public HTMLTableCell(HTMLTagElement element)
   {
      if (element == null)
         throw new NullPointerException("element");
      
      element_ = element;
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
   *  Returns the column span.  The default value is one.
   *  @return The column span.
   **/
   public int getColumnSpan()
   {
      return colSpan_;
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
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving direction attribute tag.");

       if ((dir_ != null) && (dir_.length() > 0))
          return " dir=\"" + dir_ + "\"";
       else
          return "";
    }


   /**
   *  Returns the table cell element.
   *  @return The cell element.
   **/
   public HTMLTagElement getElement()
   {
      return element_;
   }

   /**
   *  Returns the table cell end tag.
   *  @return The end tag.
   **/
   String getEndTag()
   {
      return "</td>\n";
   }

   /**
   *  Returns the height relative to the table in pixels or percent.
   *  @return The height.
   **/
   public int getHeight()
   {
      return height_;
   }

   /**
   *  Returns the horizontal alignment.  The default value is LEFT.
   *  @return The horizontal alignment.  One of the following constants
   *  defined in HTMLConstants:  CENTER, LEFT, or RIGHT.
   *  @see com.ibm.as400.util.html.HTMLConstants
   **/
   public String getHorizontalAlignment()
   {
      return align_;
   }

   /**
    *  Returns the <i>language</i> of the table cell.
    *  @return The language of the table cell.
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
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving language attribute tag.");

       if ((lang_ != null) && (lang_.length() > 0))
          return " lang=\"" + lang_ + "\"";
       else
          return "";
    }

   /**
   *  Returns the row span.  The default value is one.
   *  @return The row span.
   **/
   public int getRowSpan()
   {
      return rowSpan_;
   }

   /**
   *  Returns the table cell start tag.
   *  @return The start tag.
   **/
   String getStartTag()
   { 
      return "<td";
   }

   /**
   *  Returns the table cell attribute tag.
   *  @return The cell attribute tag.
   **/
   String getAttributeTag()
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   Retrieving cell attribute tag...");

      StringBuffer tag = new StringBuffer();
      
      // Add the alignment attributes.
      if (align_ != null)
      {
         tag.append(" align=\"");
         tag.append(align_);
         tag.append("\"");
      }
      if (vAlign_ != null)
      {
         tag.append(" valign=\"");
         tag.append(vAlign_ );
         tag.append("\"");
      }
      
      // Add the span attributes.
      if (rowSpan_ > 1)
      {
         tag.append(" rowspan=\"");
         tag.append(rowSpan_);
         tag.append("\"");
      }
      if (colSpan_ > 1)
      {
         tag.append(" colspan=\"");
         tag.append(colSpan_);
         tag.append("\"");
      }
      
      // Add the size attributes.
      if (height_ > 0)
      {
         tag.append(" height=\"");
         tag.append(height_);

         if (heightPercent_)
            tag.append("%");
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

      // Add the wrap attribute.
      if (!wrap_) 
         tag.append(" nowrap=\"nowrap\"");

      tag.append(getLanguageAttributeTag());    //$B1A
      tag.append(getDirectionAttributeTag());   //$B1A
      tag.append(getAttributeString());         // @Z1A
      
      tag.append(">");

      return new String(tag);
   }

   /**
   *  Returns the table cell tag.
   *  @return The cell tag.
   **/
   public String getTag()
   {
      return getTag(element_);
   }

   /**
   *  Returns the table cell tag with the specified <i>element</i>.
   *  It does not change the cell object's element attribute.
   *  @param element The table cell element.
   *  @return The cell tag.
   **/
   public String getTag(HTMLTagElement element)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLTableCell tag...");

      // Verify that the element is set.
      if (element == null)
         throw new NullPointerException("element");

      StringBuffer tag = new StringBuffer(getStartTag());
      tag.append(getAttributeTag());
      tag.append(element.getTag());
      tag.append(getEndTag());
      
      return new String(tag);
   }
   
   /**
   *  Returns the vertical alignment.
   *  @return The vertical alignment.  One of the following constants
   *  defined in HTMLConstants:  BASELINE, BOTTOM, MIDDLE, or TOP.
   *  @see com.ibm.as400.util.html.HTMLConstants
   **/
   public String getVerticalAlignment()
   {
      return vAlign_;
   }

   /**
   *  Returns the width relative to the table in pixels or percent.
   *  @return The width.
   **/
   public int getWidth()
   {
      return width_;
   }
    
   

   /**
   *  Indicates if the height is in percent or pixels.
   *  The default value is false.
   *  @return true if percent; pixels otherwise.
   **/
   public boolean isHeightInPercent()
   {
      return heightPercent_;
   }

   /**
   *  Indicates if the width is in percent or pixels.
   *  The default value is false.
   *  @return true if percent; pixels otherwise.
   **/
   public boolean isWidthInPercent()
   {
      return widthPercent_;
   }

   /**
   *  Indicates if the cell data will use normal HTML linebreaking conventions.
   *  The default value is true.
   *  @return true if normal HTML linebreaking is used; false otherwise.
   **/
   public boolean isWrap()
   {
      return wrap_;
   }

   /**
   *  Deserializes and initializes transient data.
   **/
   private void readObject(java.io.ObjectInputStream in)         
       throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
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
   *  Sets the column span.  The default value is one.
   *  @param span The column span.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setColumnSpan(int span) throws PropertyVetoException
   {
      if (span <= 0)
         throw new ExtendedIllegalArgumentException("span", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldSpan = new Integer(colSpan_);
      Integer newSpan = new Integer(span);

      vetos_.fireVetoableChange("span", oldSpan, newSpan);
      
      colSpan_ = span;

      changes_.firePropertyChange("span", oldSpan, newSpan);
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
        vetos_.fireVetoableChange("dir", old, dir );

        dir_ = dir;

        changes_.firePropertyChange("dir", old, dir );
    }
   
   /**
   *  Sets the table cell element.
   *  @param element The cell element.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setElement(String element) throws PropertyVetoException
   {
      setElement(new HTMLText(element));
   }

   /**
   *  Sets the table cell element.
   *  @param element The cell element.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setElement(HTMLTagElement element) throws PropertyVetoException
   {
      if (element == null)
         throw new NullPointerException("element");

      HTMLTagElement old = element_;
      vetos_.fireVetoableChange("element", old, element );

      element_ = element; 

      changes_.firePropertyChange("element", old, element );
   }

   /**
   *  Sets the height relative to the table.  The default unit is pixels.
   *  A table row can only have one height.
   *  If multiple cell heights are defined for different cells in the row, the outcome is browser
   *  dependent.
   *  @param height The height.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see #setHeightInPercent
   **/
   public void setHeight(int height) throws PropertyVetoException
   {
      if (height <= 0)
         throw new ExtendedIllegalArgumentException("height", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldHeight = new Integer(height_);
      Integer newHeight = new Integer(height);

      vetos_.fireVetoableChange("height", oldHeight, newHeight);
 
      height_ = height;

      changes_.firePropertyChange("height", oldHeight, newHeight);
   }

   /**
   *  Sets the height relative to the table in pixels or percent.
   *  A table row can only have one height.
   *  If multiple cell heights are defined for different cells in the row, the outcome is browser dependent.
   *  @param height The height.
   *  @param heightInPercent true if unit is percent; false if pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setHeight(int height, boolean heightInPercent) throws PropertyVetoException
   {
      int oldHeight = height_;

      setHeight(height);

      try
      {
         setHeightInPercent(heightInPercent);
      }
      catch (PropertyVetoException e)
      {
         // Restore the original height.
         height_ = oldHeight;
         throw new PropertyVetoException("heightInPercent", e.getPropertyChangeEvent());
      }
   }

   /**
   *  Sets the height unit in percent or pixels.  The default is false.
   *  @param heightInPercent true if unit is percent; false if pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see #setHeight
   **/
   public void setHeightInPercent(boolean heightInPercent) throws PropertyVetoException
   {
      Boolean oldHeight = new Boolean(heightPercent_);
      Boolean newHeight = new Boolean(heightInPercent);

      vetos_.fireVetoableChange("heightInPercent", oldHeight, newHeight);
 
      heightPercent_ = heightInPercent;

      changes_.firePropertyChange("heightInPercent", oldHeight, newHeight);
   }

   /**
   *  Sets the horizontal alignment.  The default value is LEFT.
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
         String old = align_;
         vetos_.fireVetoableChange("alignment", old, alignment );
         
         align_ = alignment;

         changes_.firePropertyChange("alignment", old, alignment );
      }
      else
      {
         throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
   }

   /**
    *  Sets the <i>language</i> of the table cell.
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
        vetos_.fireVetoableChange("lang", old, lang );

        lang_ = lang;

        changes_.firePropertyChange("lang", old, lang );
    }

   /**
   *  Sets the row span.  The default value is one.
   *  @param span The row span.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setRowSpan(int span) throws PropertyVetoException
   {
      if (span <= 0)
         throw new ExtendedIllegalArgumentException("span", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldSpan = new Integer(rowSpan_);
      Integer newSpan = new Integer(span);

      vetos_.fireVetoableChange("span", oldSpan, newSpan);

      rowSpan_ = span;

      changes_.firePropertyChange("span", oldSpan, newSpan);
   }

   /**
   *  Sets the vertical alignment.
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
   *  Sets the width relative to the table.  The default width unit is pixels.
   *  A table column can only have one width and the width used is usually the widest.
   *  If multiple cell widths are defined for different cells in the column, the outcome is browser dependent.
   *  @param width The width.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see #setWidthInPercent
   **/
   public void setWidth(int width) throws PropertyVetoException
   {
      if (width <= 0)
         throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldWidth = new Integer(width_);
      Integer newWidth = new Integer(width);

      vetos_.fireVetoableChange("width", oldWidth, newWidth);

      width_ = width;

      changes_.firePropertyChange("width", oldWidth, newWidth);
   }

   /**
   *  Sets the width relative to the table in percent or pixels.
   *  A table column can only have one width and the width used is usually the widest.
   *  If multiple cell widths are defined for different cells in the column, the outcome
   *  is browser dependent.
   *  @param width The width.
   *  @param widthInPercent true if unit is percent; false if pixels.
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
         width_ = oldWidth;
         throw new PropertyVetoException("widthInPercent", e.getPropertyChangeEvent());
      }    
   }

   /**
   *  Sets the width unit in percent or pixels.  The default is false.
   *  @param widthInPercent true if unit is percent; false if pixels.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see #setWidth
   **/
   public void setWidthInPercent(boolean widthInPercent) throws PropertyVetoException
   {
      Boolean oldWidth = new Boolean(widthPercent_);
      Boolean newWidth = new Boolean(widthInPercent);

      vetos_.fireVetoableChange("widthInPercent", oldWidth, newWidth);

      widthPercent_ = widthInPercent;

      changes_.firePropertyChange("widthInPercent", oldWidth, newWidth);
   }
  
   /**
   *  Sets if the cell data will use normal HTML linebreaking conventions.
   *  The default value is true.
   *  @param wrap true if normal HTML linebreaking is used; false otherwise.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setWrap(boolean wrap) throws PropertyVetoException
   {
      Boolean oldWrap = new Boolean(wrap_);
      Boolean newWrap = new Boolean(wrap);

      vetos_.fireVetoableChange("wrap", oldWrap, newWrap);
      
      wrap_ = wrap;
      
      changes_.firePropertyChange("wrap", oldWrap, newWrap);
   }

   /**
   *  Returns the HTML table cell tag.
   *  @return The cell tag.
   **/
   public String toString()
   {
      return getTag();
   }
}

