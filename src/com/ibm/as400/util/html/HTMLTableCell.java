///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTableCell.java
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
*  <P>Calling getFOTag() produces the following:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px' text-align='center'&gt;&lt;fo:block-container&gt;
*  &lt;fo:block font-weight='bold' font-style='italic'&gt;IBM&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:table-cell&gt;
*  </PRE></BLOCKQUOTE>
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
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

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

    private boolean useFO_ = false;            // Indicates if XSL-FO tags are used            //@D1A
    private int borderWidth_ = 1;              // The width of the cell border                 //@D1A
    private int cellPadding_ = 1;              // The padding for the cell                     //@D1A
    transient private VetoableChangeSupport vetos_; //@CRS

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
        if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
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
        if(useFO_ )                                                                      //@D1A
        {                                                                                //@D1A
            if((dir_ != null) && (dir_.length()>0))                                      //@D1A
            {                                                                            //@D1A
                if(dir_.equals(HTMLConstants.RTL))                                       //@D1A
                    return " writing-mode='rl'";                                         //@D1A
                else                                                                     //@D1A
                    return " writing-mode='lr'";                                         //@D1A
            }                                                                            //@D1A
            else                                                                         //@D1A
                return "";                                                               //@D1A
        }                                                                                //@D1A
        else                                                                             //@D1A
        {                                                                                //@D1A
            //@C1D

            if ((dir_ != null) && (dir_.length() > 0))
                return " dir=\"" + dir_ + "\"";
            else
                return "";
        }                                                                                //@D1A
        
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
        if(!useFO_)        //@D1A
            return "</td>\n";
        else
            return "</fo:table-cell>\n"; // @D1A
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
        //@C1D

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
        if(!useFO_)        //@D1A
            return "<td";
        else                       //must indicate border and padding for each cell     //@D1A
            return "<fo:table-cell border-style='solid' border-width='" + borderWidth_ + "px' padding='" + cellPadding_ +"px'";    //@D1A
    }

    /**
    *  Returns the table cell attribute tag.
    *  @return The cell attribute tag.
    **/
    String getAttributeTag()
    {
        //@C1D

        StringBuffer tag = new StringBuffer();

        // Add the alignment attributes.
        if (align_ != null)
        {
            if(!useFO_ )                //@D1A
            {
                tag.append(" align=\"");
                tag.append(align_);
                tag.append("\"");
            }
            else                                            //@D1A
            {                                               //@D1A
                tag.append(" text-align='");                //@D1A
                if(align_.equalsIgnoreCase("center"))       //@D1A
                    tag.append("center");                   //@D1A
                else if(align_.equalsIgnoreCase("left"))    //@D1A
                    tag.append("start");                    //@D1A
                else if(align_.equalsIgnoreCase("right"))   //@D1A
                    tag.append("end");                      //@D1A
                tag.append("'");                            //@D1A
            }                                               //@D1A
        }
        if(!useFO_ )            //@D1A
        {
            
            if (vAlign_ != null)    //@D1A
            {
                tag.append(" valign=\"");
                tag.append(vAlign_ );
                tag.append("\"");
            }

            // Add the span attributes.
            if (rowSpan_ > 1)   //@D1A
            {
                tag.append(" rowspan=\"");
                tag.append(rowSpan_);
                tag.append("\"");
            }
            if (colSpan_ > 1)  //@D1A
            {
                tag.append(" colspan=\"");
                tag.append(colSpan_);
                tag.append("\"");
            }

            // Add the size attributes.
            if (height_ > 0)    //@D1A
            {
                tag.append(" height=\"");
                tag.append(height_);

                if (heightPercent_)
                    tag.append("%");
                tag.append("\"");
            }
            if (width_ > 0)     //@D1A
            {
                tag.append(" width=\"");
                tag.append(width_);

                if (widthPercent_)
                    tag.append("%");
                tag.append("\"");
            }

            // Add the wrap attribute.
            if (!wrap_)         //@D1A
                tag.append(" nowrap=\"nowrap\"");

            tag.append(getLanguageAttributeTag());    //$B1A
            tag.append(getDirectionAttributeTag());   //$B1A
            tag.append(getAttributeString());         // @Z1A
        }
        else                 //@D1A
        {
            // Add the span attributes.
            if (rowSpan_ > 1)   
            {
                tag.append(" number-rows-spanned='");
                tag.append(rowSpan_);
                tag.append("'");
            }
            if (colSpan_ > 1)  
            {
                tag.append(" number-columns-spanned='");
                tag.append(colSpan_);
                tag.append("'");
            }
            // Add the size attributes.
            if (height_ > 0)    //@D1A
            {
                tag.append(" height='");
                tag.append(height_);

                if (heightPercent_)
                    tag.append("%");
                tag.append("'");
            }
            if (width_ > 0)     
            {
                tag.append(" width='");
                tag.append(width_);

                if (widthPercent_)
                    tag.append("%");
                tag.append("'");
            }
        }

        tag.append(">");

        return tag.toString();              //@D1C
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
    *  Returns the XSL-FO table cell tag.
    *  @return The cell tag.
    **/
    public String getFOTag()        //@D1A
    {
        return getFOTag(element_);
    }

    /**
    *  Returns the XSL-FO table cell tag with the specified <i>element</i>.
    *  It does not change the cell object's element attribute.
    *  The valign, wrap and laguage attributes are not supported in XSL-FO.
    *  @param element The table cell element.
    *  @return The XSL-FO cell tag.
    **/
    public String getFOTag(HTMLTagElement element)                      //@D1A
    {
        //Save current state of useFO_
        boolean useFO = useFO_;                                         

        setUseFO(true);
        
        // Verify that the element is set.
        if (element == null)
            throw new NullPointerException("element");

        StringBuffer tag = new StringBuffer(getStartTag());
        tag.append(getAttributeTag());
        tag.append("<fo:block-container");                         
        tag.append(getDirectionAttributeTag());                    
        tag.append(">\n");                                        
        tag.append(element.getFOTag());                            
        tag.append("</fo:block-container>\n");                      
        tag.append(getEndTag());

        //Set useFO_ to previous state                                                 
        setUseFO(useFO);                                           

        return tag.toString();                                          
    }

    /**
    *  Returns the table cell tag with the specified <i>element</i>.
    *  It does not change the cell object's element attribute.
    *  @param element The table cell element.
    *  @return The cell tag.
    **/
    public String getTag(HTMLTagElement element)
    {
        //@C1D

        if(useFO_)                      //@D1A
            return getFOTag(element);          //@D1A

        // Verify that the element is set.
        if (element == null)
            throw new NullPointerException("element");

        StringBuffer tag = new StringBuffer(getStartTag());
        tag.append(getAttributeTag());
        tag.append(element.getTag());
        tag.append(getEndTag());

        return tag.toString();                                          //@D1C
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
    *  Returns if Formatting Object tags are outputted.
    *  The default value is false.
    *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
    **/
    public boolean isUseFO()
    {
        //@D1A
        return useFO_;
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
        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
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
    *  Sets the column span.  The default value is one.
    *  @param span The column span.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setColumnSpan(int span) throws PropertyVetoException
    {
        if (span <= 0)
            throw new ExtendedIllegalArgumentException("span", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@CRS Integer oldSpan = new Integer(colSpan_);
        //@CRS Integer newSpan = new Integer(span);
        int oldSpan = colSpan_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("span", new Integer(oldSpan), new Integer(span)); //@CRS

        colSpan_ = span;

        if (changes_ != null) changes_.firePropertyChange("span", new Integer(oldSpan), new Integer(span)); //@CRS
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
        if (vetos_ != null) vetos_.fireVetoableChange("element", old, element ); //@CRS

        element_ = element; 

        if (changes_ != null) changes_.firePropertyChange("element", old, element ); //@CRS
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

        //@CRS Integer oldHeight = new Integer(height_);
        //@CRS Integer newHeight = new Integer(height);
        int oldHeight = height_;

        if (vetos_ != null) vetos_.fireVetoableChange("height", new Integer(oldHeight), new Integer(height)); //@CRS

        height_ = height;

        if (changes_ != null) changes_.firePropertyChange("height", new Integer(oldHeight), new Integer(height)); //@CRS
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
        //@CRS Boolean oldHeight = new Boolean(heightPercent_);
        //@CRS Boolean newHeight = new Boolean(heightInPercent);
      boolean oldHeight = heightPercent_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("heightInPercent", new Boolean(oldHeight), new Boolean(heightInPercent)); //@CRS

        heightPercent_ = heightInPercent;

        if (changes_ != null) changes_.firePropertyChange("heightInPercent", new Boolean(oldHeight), new Boolean(heightInPercent)); //@CRS
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
            if (vetos_ != null) vetos_.fireVetoableChange("alignment", old, alignment ); //@CRS

            align_ = alignment;

            if (changes_ != null) changes_.firePropertyChange("alignment", old, alignment ); //@CRS
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
        if (vetos_ != null) vetos_.fireVetoableChange("lang", old, lang ); //@CRS

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
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

        //@CRS Integer oldSpan = new Integer(rowSpan_);
        //@CRS Integer newSpan = new Integer(span);
        int oldSpan = rowSpan_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("span", new Integer(oldSpan), new Integer(span)); //@CRS

        rowSpan_ = span;

        if (changes_ != null) changes_.firePropertyChange("span", new Integer(oldSpan), new Integer(span)); //@CRS
    }

    /** 
    * Sets if Formatting Object tags should be used.
    *  The default value is false.
    * @param useFO - true if output generated is a XSL formatting object, false if the output generated is HTML. 
    **/
    public void setUseFO(boolean useFO)                // @D1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
    *  Sets the border width in pixels.  A value of zero indicates no border.
    *  The default value is one.
    *  @param borderWidth The border width.
    **/
    public void setBorderWidth(int borderWidth)                // @D1A
    {
        Integer oldWidth = new Integer(borderWidth_);
        Integer newWidth = new Integer(borderWidth);

        borderWidth_ = borderWidth;

        if (changes_ != null) changes_.firePropertyChange("borderWidth", oldWidth, newWidth);
    }

    /**
    *  Sets the global table cell padding.  The cell padding is the spacing between
    *  data in a table cell and the border of the cell.
    *  @param cellPadding The cell padding.
    **/
    public void setCellPadding(int cellPadding)         // @D1A
    {
        Integer oldPadding = new Integer(cellPadding_);
        Integer newPadding = new Integer(cellPadding);

        cellPadding_ = cellPadding;

        if (changes_ != null) changes_.firePropertyChange("cellPadding", oldPadding, newPadding);
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

        //@CRS Integer oldWidth = new Integer(width_);
        //@CRS Integer newWidth = new Integer(width);
        int oldWidth = width; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("width", new Integer(oldWidth), new Integer(width)); //@CRS

        width_ = width;

        if (changes_ != null) changes_.firePropertyChange("width", new Integer(oldWidth), new Integer(width)); //@CRS
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
        //@CRS Boolean oldWidth = new Boolean(widthPercent_);
        //@CRS Boolean newWidth = new Boolean(widthInPercent);
      boolean oldWidth = widthPercent_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("widthInPercent", new Boolean(oldWidth), new Boolean(widthInPercent)); //@CRS

        widthPercent_ = widthInPercent;

        if (changes_ != null) changes_.firePropertyChange("widthInPercent", new Boolean(oldWidth), new Boolean(widthInPercent)); //@CRS
    }

    /**
    *  Sets if the cell data will use normal HTML linebreaking conventions.
    *  The default value is true.
    *  @param wrap true if normal HTML linebreaking is used; false otherwise.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setWrap(boolean wrap) throws PropertyVetoException
    {
        //@CRS Boolean oldWrap = new Boolean(wrap_);
        //@CRS Boolean newWrap = new Boolean(wrap);
        boolean oldWrap = wrap_; //@CRS

        if (vetos_ != null) vetos_.fireVetoableChange("wrap", new Boolean(oldWrap), new Boolean(wrap)); //@CRS

        wrap_ = wrap;

        if (changes_ != null) changes_.firePropertyChange("wrap", new Boolean(oldWrap), new Boolean(wrap)); //@CRS
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

