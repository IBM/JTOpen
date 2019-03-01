///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTableHeader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;

/**
*  The HTMLTableHeader represents an HTML table header tag.
*
*  <P>This example creates an HTMLTableHeader and displays the tag output.
*  <P><BLOCKQUOTE><PRE>
*  HTMLTableHeader header = new HTMLTableHeader();
*  header.setHorizontalAlignment(HTMLTableHeader.CENTER);
*  HTMLText headerText = new HTMLText("Customer Name");
*  header.setElement(headerText);
*  System.out.println(header.getTag());
*  </PRE></BLOCKQUOTE>
*  Here is the output of the tag:
*  <BLOCKQUOTE><PRE>
*  &lt;th align="center"&gt;Customer Name&lt;/th&gt;
*  </PRE></BLOCKQUOTE>
*  <P>Calling getFOTag() produces the following tag with the default cell border and padding properties:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:table-cell border-style='solid' border-width='1px' padding='1px'&gt;
*  &lt;fo:block font-weight='bold'&gt;&lt;fo:block&gt;Customer Name&lt;/fo:block&gt;
*  &lt;/fo:block&gt;
*  &lt;/fo:table-cell&gt;
*  </PRE></BLOCKQUOTE>
*
**/
public class HTMLTableHeader extends HTMLTableCell
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 6954665365850223957L;



    private boolean useFO_ = false;        // Set use FO tag.      //@C1A
    private int borderWidth_ = 1;          // The table border.    //@C1A
    private int cellPadding_ = 1;          //The cell padding      //@C1A
    
    /**
    *  Constructs a default HTMLTableHeader object.
    **/
    public HTMLTableHeader()
    {
        super();
    }

    /**
    *  Constructs an HTMLTableHeader object with the specified data <i>element</i>.
    *  @param element An HTMLTagElement object containing the data.
    **/
    public HTMLTableHeader(HTMLTagElement element)
    {
        super(element);
    }

    /**
    *  Returns the table header tag.
    *  @return The HTML tag.
    **/
    public String getTag()
    {
        return getTag(getElement());
    }

    /**
    *  Returns the XSL-FO table header tag.
    *  @return the XSL-FO tag
    **/
    public String getFOTag()              //@C1A
    {
        return getFOTag(getElement());
    }

    /**
    *  Returns the table header tag with the specified data <i>element</i>.
    *  @return The XSL-FO tag.
    **/
    public String getFOTag(HTMLTagElement element)                              //@C1A
    {
        //Save the current state of useFO_
        boolean useFO = useFO_;

        setUseFO(true);

        if (element == null)
            throw new NullPointerException("element");

        StringBuffer tag = new StringBuffer("<fo:table-cell border-style='solid' border-width='");   
        tag.append(borderWidth_);                                         
        tag.append("px'");                                                
        tag.append(" padding='");                                         
        tag.append(cellPadding_);                                         
        tag.append("px'");
        int height = getHeight();
        if(height > 1)
        {
            tag.append(" height='");
            tag.append(height);
            if(isHeightInPercent())
                tag.append("%");
            tag.append("'");
        }
        int width = getWidth();
        if(width > 1)
        {
            tag.append(" width='");
            tag.append(width);
            if(isWidthInPercent())
                tag.append("%");
            tag.append("'");
        }
        int columnSpan = getColumnSpan();
        if (columnSpan > 1)  
        {
            tag.append(" number-columns-spanned='");
            tag.append(columnSpan);
            tag.append("'");
        }
        int rowSpan  = getRowSpan();
        if (rowSpan > 1)   
        {
            tag.append(" number-rows-spanned='");
            tag.append(rowSpan);
            tag.append("'");
        }
        tag.append(">\n");                                             
        tag.append("<fo:block font-weight='bold'");
        String dir = getDirection();
        if((dir != null) && (dir.length()>0))                                      
        {                                                                          
                if(dir.equals(HTMLConstants.RTL))                                  
                    tag.append(" writing-mode='rl'");                              
                else                                                                    
                    tag.append(" writing-mode='lr'");                                   
        } 
        String align = getHorizontalAlignment();
        if(align != null)
        {
            tag.append(" text-align='");               
            if(align.equals(HTMLConstants.CENTER))       
                tag.append("center");               
            else if(align.equals(HTMLConstants.LEFT))
                tag.append("start");                
            else if(align.equals(HTMLConstants.RIGHT))
                tag.append("end");                   
            tag.append("'");
        }
        tag.append(">");                      
        tag.append(element.getFOTag());                                   
        tag.append("</fo:block>\n");                                      
        tag.append("</fo:table-cell>\n");                                 
            
        //Set useFO_ to previous state
        setUseFO(useFO);                                                    
                                                                      
        return tag.toString();                                                  
    }

    /**
    *  Returns the table header tag with the specified data <i>element</i>.
    *  @return The HTML tag.
    **/
    public String getTag(HTMLTagElement element)
    {
        //@B1D

        if(useFO_)                      //@C1A
            return getFOTag(element);          //@C1A

        if (element == null)
            throw new NullPointerException("element");

        StringBuffer tag = new StringBuffer("<th");                         
        tag.append(getAttributeTag());             
        tag.append(element.getTag());              
        tag.append("</th>\n");                     

        return tag.toString();                                                  //@C1C        
    }

    /**
    *  Returns if Formatting Object tags are outputted.
    *  The default value is false.
    *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
    **/
    public boolean isUseFO()        //@C1A
    {
        return useFO_;
    }

    /**
    *  Sets the border width in pixels.  A value of zero indicates no border.
    *  The default value is one.
    *  @param borderWidth The border width.
    **/
    public void setBorderWidth(int borderWidth)                           //@C1A
    {
        Integer oldWidth = new Integer(borderWidth_);
        Integer newWidth = new Integer(borderWidth);

        borderWidth_ = borderWidth;

        if (changes_ != null) changes_.firePropertyChange("borderWidth", oldWidth, newWidth);
    }

    /**
    *  Sets the global table cell padding.  The cell padding is the spacing between
    *  data in a table cell and the border of the cell.
    *  The default value is 1                     
    *  @param cellPadding The cell padding.
    **/
    public void setCellPadding(int cellPadding)                     //@C1A
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
    public void setUseFO(boolean useFO)                    //@C1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
    *  Returns the HTML table header tag.
    *  @return The header tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
