///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.lang.*;

/**
*  The HTMLDocument class represents an HTML or an XSL-FO document.  The document
*  contains all information needed to display an HTML or an XSL-FO page.
*
*  
*  <BLOCKQUOTE><PRE>
*  The following java program creates an HTMLDocument:
*
*
*  package com.ibm.as400.util.html;
*  import java.*;
*  import java.io.*;
*  import java.lang.*;
*  import java.beans.PropertyVetoException;
*
*  public class FoFile
*  {
*       public static void main (String[] args)
*       {
*           //Create the HTMLDocument that holds necessary document properties
*           HTMLDocument fo = new HTMLDocument();
*        
*           //Set page and margin properties.  Numbers are in inches.
*           fo.setPageWidth(8.5);
*           fo.setPageHeight(11);
*           fo.setMarginTop(1);
*           fo.setMarginBottom(1);
*           fo.setMarginLeft(1);
*           fo.setMarginRight(1);
*        
*           //Create a header for the page.
*           HTMLHead head = new HTMLHead();
*           //Set the title for the header
*           head.setTitle("This is the page header.");
*        
*           //Create several headings
*           HTMLHeading h1 = new HTMLHeading(1, "Heading 1");
*           HTMLHeading h2 = new HTMLHeading(2, "Heading 2");
*           HTMLHeading h3 = new HTMLHeading(3, "Heading 3");
*           HTMLHeading h4 = new HTMLHeading(4, "Heading 4");
*           HTMLHeading h5 = new HTMLHeading(5, "Heading 5");
*           HTMLHeading h6 = new HTMLHeading(6, "Heading 6");
*        
*           //Create some text that is printed from right to left.
*           //Create BidiOrdering object and set the direction
*           BidiOrdering bdo = new BidiOrdering();
*           bdo.setDirection(HTMLConstants.RTL);
*
*           //Create some text
*           HTMLText text = New HTMLText("This is Arabic text.");
*           //Add the text to the bidi-ordering object and get XSL-FO tag
*           bdo.addItem(text);
*        
*           //Add the HTMLHead
*           fo.setHTMLHead(head);
*
*           //Add the items to the document
*           fo.addElement(h1);
*           fo.addElement(h2);
*           fo.addElement(h3);
*           fo.addElement(h4);
*           fo.addElement(h5);
*           fo.addElement(h6);
*           fo.addElement(bdo);
*        
*           //Print the Formatting Object tag.
*           System.out.println(fo.getFOTag());
*
*           //Print the HTML Object tag.
*           System.out.println(fo.getTag());
*       }   
*  }
*
*
*  Here is the output generated by the above program:
*
*
*   &lt;fo:root xmlns:fo = 'http://www.w3.org/1999/XSL/Format'&gt;
*   &lt;fo:layout-master-set&gt;
*   &lt;fo:simple-page-master master-name='body-page' writing-mode='lr-tb' page-width='8.5in' page-height='11.0in' margin-top='1.0in' margin-bottom='1.0in' margin-left='1.0in' margin-right='1.0in'&gt;
*   &lt;fo:region-body region-name='xsl-region-body'/&gt;
*   &lt;fo:region-before region-name='xsl-region-before' precedence='true' extent='1.0in'/&gt;
*   &lt;fo:region-after region-name='xsl-region-after' precedence='true' extent='1.0in'/&gt;
*   &lt;fo:region-start region-name='xsl-region-start' extent='1.0in'/&gt;
*   &lt;fo:region-end region-name='xsl-region-end' extent='1.0in'/&gt;
*   &lt;/fo:simple-page-master&gt;
*   &lt;/fo:layout-master-set&gt;
*   &lt;fo:page-sequence master-name='body-page'&gt;
*   &lt;fo:flow flow-name='xsl-region-body'&gt;
*   
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='25pt'&gt;Heading 1&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;                              
*
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='20pt'&gt;Heading 2&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='15pt'&gt;Heading 3&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*   
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='13pt'&gt;Heading 4&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='11pt'&gt;Heading 5&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*
*   &lt;fo:block-container writing-mode='lr'&gt;
*   &lt;fo:block font-size='9pt'&gt;Heading 6&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*
*   &lt;fo:block-container writing-mode='rl'&gt;
*   &lt;fo:block&gt;This is Arabic text.&lt;/fo:block&gt;
*   &lt;/fo:block-container&gt;
*
*   &lt;/fo:flow&gt;
*   &lt;fo:static-content flow-name='xsl-region-before'&gt;
*   &lt;fo:block-container&gt;
*   This is the page header.&lt;/fo:block-container&gt;
*   &lt;/fo:static-content&gt;
*   &lt;/fo:page-sequence&gt;
*   &lt;/fo:root&gt;
*
*
*   &lt;html&gt;
*   &lt;head&gt;
*   &lt;title&gt;This is the page header.&lt;/title&gt;
*   &lt;/head&gt;
*   &lt;body&gt;
*   &lt;h1&gt;Heading 1&lt;/h1&gt;
*   &lt;h2&gt;Heading 2&lt;/h2&gt;
*   &lt;h3&gt;Heading 3&lt;/h3&gt;
*   &lt;h4&gt;Heading 4&lt;/h4&gt;
*   &lt;h5&gt;Heading 5&lt;/h5&gt;
*   &lt;h6&gt;Heading 6&lt;/h6&gt;
*
*   &lt;bdo dir="rtl"&gt;
*   This is Arabic text.
*   &lt;/bdo&gt;
*
*   &lt;/body&gt;
*   &lt;/html&gt;
*  </PRE></BLOCKQUOTE>
**/

public class HTMLDocument extends HTMLTagAttributes implements java.io.Serializable     
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  static final long serialVersionUID = 1662839037165473585L;
    
    private Vector tag_ = new Vector();               //Vector to hold any tags added to the main body of the document container
    private HTMLHead head_=null;                      //HTMLHead element to hold the page header.
    private double height_ = 11.0;                    //The height of the fo page.
    private double width_ = 8.5;                      //The width of the fo page.
    private double margin_top_=0.5;                   //The top margin for the page.
    private double margin_bottom_=0.5;                //The bottom margin for the page.
    private double margin_left_=0.5;                  //The left margin for the page.
    private double margin_right_=0.5;                 //The right margin for the page.
    private boolean useFO_ = false;                   //Specifies if formatting object tags are used.

    transient private Vector tagListeners_;         // The list of tag listeners. @CRS
                                               
    /**
    *  Constructs a default HTMLDocument object.
    **/
    public HTMLDocument()
    {
        super();
    }

    /**
    *  Constructs an HTMLDocument object with the specified HTMLHead.
    *  @param head An HTMLHead object.
    **/
    public HTMLDocument(HTMLHead head)
    {
        super();
        setHTMLHead(head);
    }

    /**
    *  Adds a tag to the main body of the document.
    *  @param tag An HTMLTagElement object.
    **/
    public void addElement(HTMLTagElement tag)          
    {
        if (tag == null)
            throw new NullPointerException("tag");

        tag_.addElement(tag);
        fireAdded();            // Fire the element added event.
    }

    /**
    *  Adds the &lt;head&gt; HTML tag or the page header for an XSL-FO page.
    *  @param head An HTMLHead object.
    **/
    public void setHTMLHead(HTMLHead head)
    {
        if(head == null)
            throw new NullPointerException("head");

        head_ = head;
        fireAdded();
    }

    /**
    *  Returns an HTMLHead object for the page.
    *  @return The HTMLHead object.
    **/
    public HTMLHead getHTMLHead()
    {
        return head_;
    }

    /**
    *  Adds an array of tags to the document.
    *  @param tag An HTMLTagElement array.
    **/
    public void addElement(HTMLTagElement[] tag)          
    {
        if (tag == null)
            throw new NullPointerException("tag");
        else
        {
            for(int i=0; i<tag.length; i++)
            {
                tag_.addElement(tag[i]);
                fireAdded();            //Fire the element added event.
            }
        }
    }

    /**
    *  Returns the tag for the XSL-FO document.
    *  @return The tag.
    **/
    public String getFOTag()
    {
        //Save current state of useFO_
        boolean useFO = useFO_;

        //Indicate XSL-FO tags are outputted.
        setUseFO(true);

        StringBuffer s = new StringBuffer(getStartRootTag());
        s.append("\n");

        // Add any tags.
        HTMLTagElement tag;
        int size = tag_.size();
        for (int i=0; i< size; i++)
        {
            tag = (HTMLTagElement)tag_.elementAt(i);
            s.append(tag.getFOTag());
            s.append("\n");
        }

        s.append("\n");
        s.append("</fo:flow>\n");

        //Add the page header
        if (head_ != null)
        {
            s.append(getHTMLHead().getFOTag());
        }

        s.append(getEndRootTag());

        //Set useFO_ to previous state.
        setUseFO(useFO);

        return new String(s);
    }
    
    /**
    *  Returns the tag for the HTML document.
    *  @return The tag.
    **/
    public String getTag()
    {
        if(useFO_)
            return getFOTag();
       
        //Indicate that HTML tags are outputted.
        setUseFO(false);

        StringBuffer s = new StringBuffer("<html>\n");
        
        //Add the <head> tag
        if (head_ != null)
        {
          s.append(getHTMLHead().getTag());
        }

        StringBuffer body = new StringBuffer("<body>\n");
        
        // Add any tags.
        HTMLTagElement tag;
        int size = tag_.size();
        for (int i=0; i< size; i++)
        {
            tag = (HTMLTagElement)tag_.elementAt(i);
            body.append(tag.getTag().toString());
            body.append("\n");
        }

        //Add the <body> tag and its contents
        s.append(body.toString());
        s.append("</body>\n");
        s.append("</html>\n");

        return new String(s);
    }
    
    /**
     *  Returns if Formatting Object tags are outputted.
     *  The default value is false.
     *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
     **/
    public boolean isUseFO()
    {
        return useFO_;
    }

    /**
    *  Returns the beginning tags for the XSL-FO document.
    *  @return The tag.
    **/
    private String getStartRootTag()
    {

        StringBuffer tag = new StringBuffer("<fo:root xmlns:fo = 'http://www.w3.org/1999/XSL/Format'>\n");
        tag.append("<fo:layout-master-set>\n");
        tag.append("<fo:simple-page-master master-name='body-page' writing-mode='lr-tb'");

        //Get page width and height, default value is width=8.5in, height=11.0in
        tag.append(" page-width='" + width_ + "in'");
        tag.append(" page-height='" + height_ +"in'");
        
        //Get the page margins, default value is .5in
        tag.append(" margin-top='" + margin_top_ + "in'");
        tag.append(" margin-bottom='" + margin_bottom_ +"in'");
        tag.append(" margin-left='" + margin_left_ +"in'");
        tag.append(" margin-right='" + margin_right_ +"in'>\n");
        
        tag.append("<fo:region-body region-name='xsl-region-body'/>\n");
        tag.append("<fo:region-before region-name='xsl-region-before' precedence='true' extent='");
        tag.append(margin_top_ +"in'/>\n");
        
        tag.append("<fo:region-after region-name='xsl-region-after' precedence='true' extent='");
        tag.append(margin_bottom_ +"in'/>\n");
        
        tag.append("<fo:region-start region-name='xsl-region-start' extent='");
        tag.append(margin_left_ +"in'/>\n");
        
        tag.append("<fo:region-end region-name='xsl-region-end' extent='");
        tag.append(margin_right_ +"in'/>\n");
        
        tag.append("</fo:simple-page-master>\n");
        tag.append("</fo:layout-master-set>\n");
        tag.append("<fo:page-sequence master-name='body-page'>\n");
        tag.append("<fo:flow flow-name='xsl-region-body'>\n");

        return tag.toString();
    }

    /**
    *  Returns the ending tags for the XSL-FO document.
    *  @return The tag.
    **/
    private String getEndRootTag()
    {

        StringBuffer tag = new StringBuffer("</fo:page-sequence>\n");
        tag.append("</fo:root>");

        return tag.toString();
    }
    
    /**
    *  Returns a String representation for the Document tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }

    /**
    *  Fires a ELEMENT_ADDED event.
    **/
    private void fireAdded()
    {
      if (tagListeners_ == null) return;
        Vector targets = (Vector) tagListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_ADDED);
        for (int i=0; i<targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementAdded(event);
        }
    }

    /**
    *  Fires a ELEMENT_REMOVED event.
    **/
    private void fireRemoved()
    {
      if (tagListeners_ == null) return;
        Vector targets = (Vector) tagListeners_.clone();
        ElementEvent event = new ElementEvent(this, ElementEvent.ELEMENT_REMOVED);
        for (int i=0; i< targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            target.elementRemoved(event);
        }
    }

    /**
    *  Removes this tags ElementListener from the internal list.
    *  If the ElementListener is not on the list, nothing is done.
    *  @see #addListener
    *  @param listener The ElementListener.
    **/
    public void removeListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (tagListeners_ != null) tagListeners_.removeElement(listener);
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        //@CRS tagListeners_ = new Vector();
    }
    
    /**
    *  Adds an ElementListener for the tag
    *  The ElementListener object is added to an internal list of tag Listeners;
    *  it can be removed with removeListener.
    *    @see #removeListener
    *    @param listener The ElementListener.
    **/
    public void addListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (tagListeners_ == null) tagListeners_ = new Vector(); //@CRS
        tagListeners_.addElement(listener);
    }

    /**
    *  Sets the <i>page-height</i> of the XSL-FO page in inches.
    *  The default value is 11 inches.
    *  @param height The height.
    **/
    public void setPageHeight( double height )
    {
        if (height < 0 )
            throw new ExtendedIllegalArgumentException("height", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = height_;

        height_ = height;

        if (changes_ != null) changes_.firePropertyChange("height", Double.valueOf(old), Double.valueOf(height) );
    }

    /**
    *  Sets the <i>page-width</i> of the XSL-FO page in inches.
    *  The default value is 8.5 inches.
    *  @param width The width
    **/
    public void setPageWidth( double width )
    {
        if (width < 0 )
            throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = width_;

        width_ = width;

        if (changes_ != null) changes_.firePropertyChange("width", Double.valueOf(old), Double.valueOf(width) );
    }

    /**
    *  Sets the <i>top-margin</i> of the XSL-FO page in inches.
    *  The default value is 0.5 inches.
    *  @param top The width of the top margin
    **/
    public void setMarginTop( double top )
    {
        if (top < 0 )
            throw new ExtendedIllegalArgumentException("top", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = margin_top_;

        margin_top_ = top;

        if (changes_ != null) changes_.firePropertyChange("top", Double.valueOf(old), Double.valueOf(top) );
    }

    /**
    *  Sets the <i>bottom-margin</i> of the XSL-FO page in inches.
    *  The default value is 0.5 inches.
    *  @param bottom The width of the bottom margin
    **/
    public void setMarginBottom( double bottom )
    {
        if (bottom < 0 )
            throw new ExtendedIllegalArgumentException("bottom", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = margin_bottom_;

        margin_bottom_ = bottom;

        if (changes_ != null) changes_.firePropertyChange("bottom", Double.valueOf(old), Double.valueOf(bottom) );
    }

    /**
    *  Sets the <i>right-margin</i> of the XSL-FO page in inches.
    *  The default value is 0.5 inches.
    *  @param right The width of the right margin
    **/
    public void setMarginRight( double right )
    {
        if (right < 0 )
            throw new ExtendedIllegalArgumentException("right", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = margin_right_;

        margin_right_ = right;

        if (changes_ != null) changes_.firePropertyChange("right", Double.valueOf(old), Double.valueOf(right) );
    }

    /**
    *  Sets the <i>left-margin</i> of the XSL-FO page in inches.
    *  The default value is 0.5 inches.
    *  @param left The width of the left margin
    **/
    public void setMarginLeft( double left )
    {
        if (left < 0 )
            throw new ExtendedIllegalArgumentException("left", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        double old = margin_left_;

        margin_left_ = left;

        if (changes_ != null) changes_.firePropertyChange("left", Double.valueOf(old), Double.valueOf(left) );
    }

    /**
    *  Removes an HTMLTagElement from the document.
    *  @param tag The HTMLTagElement.
    **/
    public void removeElement(HTMLTagElement tag)
    {
        if (tag == null)
            throw new NullPointerException("tag");

        //verify the document is not empty
        // Verify the table is not empty.
        if (tag_.size() == 0)
        {
            Trace.log(Trace.ERROR, "Attempting to remove an element when the document is empty.");
            throw new ExtendedIllegalStateException("tag", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (tag_.removeElement(tag))
            fireRemoved();
    }

    /**
    *  Returns the height of the page in inches. 
    *  @return The height.
    **/
    public double getPageHeight()
    {
        return height_;
    }

    /**
    *  Returns the width of the page in inches.
    *  @return The width.
    **/
    public double getPageWidth()
    {
        return width_;
    }

    /**
    *  Returns the top margin of the page in inches.
    *  @return The top margin.
    **/
    public double getMarginTop()
    {
        return margin_top_;
    }

    /**
    *  Returns the bottom margin of the page in inches.
    *  @return The bottom margin.
    **/
    public double getMarginBottom()
    {
        return margin_bottom_;
    }

    /**
    *  Returns the left margin of the page in inches.
    *  @return The left margin.
    **/
    public double getMarginLeft()
    {
        return margin_left_;
    }

    /**
    *  Returns the right margin of the page in inches.
    *  @return The right margin.
    **/
    public double getMarginRight()
    {
        return margin_right_;
    }

    /** 
    * Sets if Formatting Object tags should be used.  
    * The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML. 
    **/    
    public void setUseFO(boolean useFO)
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }
}  
