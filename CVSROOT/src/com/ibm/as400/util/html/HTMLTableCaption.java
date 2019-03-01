///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTableCaption.java
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
*  The HTMLTableCaption class represents an HTML Caption tag.
*
*  <P>This example creates an HTMLTableCaption object with an HTMLText object for the caption element.
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLText object for the caption text.
*  HTMLText captionText = new HTMLText("MY TABLE");
*  // Create the HTMLTableCaption object with the new HTMLText object.
*  HTMLTableCaption caption = new HTMLTableCaption(captionText);
*  // Display the tag.
*  System.out.println(caption.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the tag:
*  <P><BLOCKQUOTE><PRE>
*  &lt;caption&gt;MY TABLE&lt;/caption&gt;
*  </PRE></BLOCKQUOTE>
*  <P>
*  The equivalent tag using XSL Formatting Objects is:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:block&gt;&lt;fo:block&gt;MY TABLE&lt;/fo:block&gt;
*  &lt;/fo:block&gt;
*  </PRE></BLOCKQUOTE>
*
*  <P>This example creates an HTMLTableCaption object with an HTMLHyperlink object for the caption element.
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLHyperlink object for the caption text.
*  HTMLHyperlink companyLink = new HTMLHyperlink("http://www.myCompany.com", "My Company");
*  // Create the HTMLTableCaption object with the new HTMLHyperlink object.
*  HTMLTableCaption caption = new HTMLTableCaption(companyLink);
*  caption.setAlignment(HTMLConstants.BOTTOM);
*  // Display the tag.
*  System.out.println(caption.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the tag:
*  <P><BLOCKQUOTE><PRE>
*  &lt;caption align="bottom"&gt;&lt;a href="http://www.myCompany.com&gt;My Company"&lt;/a&gt;&lt;/caption&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLTableCaption objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class HTMLTableCaption extends HTMLTagAttributes implements HTMLConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 8692666542126042315L;

    private HTMLTagElement element_;   // The caption element.
    private String align_;             // The caption alignment.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A
    private boolean useFO_ = false;    // Indicates if XSL-FO tags are outputted.            //@D1A

    transient private VetoableChangeSupport vetos_; //@CRS

    /**
    *  Constructs a default HTMLTableCaption object.
    **/
    public HTMLTableCaption()
    {

    }

    /**
    *  Constructs an HTMLTableCaption object with the specified <i>element</i>.
    *  @param element An HTMLTagElement object.
    **/
    public HTMLTableCaption(HTMLTagElement element)
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
    *  Returns the caption alignment relative to the table.
    *  @return The caption alignment.  One of the following constants
    *  defined in HTMLConstants:  BOTTOM, LEFT, RIGHT, or TOP.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getAlignment()
    {
        return align_;
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
        
        if(useFO_)                                                                       //@D1A
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
            {
                StringBuffer buffer = new StringBuffer(" dir=\"");
                buffer.append(dir_);
                buffer.append("\"");

                return buffer.toString();
            }
            else
                return "";
        }                                                                                //@D1A
        
    }


    /**
    *  Returns the element for the caption.
    *  @return An HTMLTagElement.
    **/
    public HTMLTagElement getElement()
    {
        return element_;
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
    *  Returns the HTML caption tag.
    *  @return The caption tag.
    **/
    public String getTag()
    {
        //@C1D

        if(useFO_)                      //@D1A
            return getFOTag();          //@D1A

        if (element_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting the 'element' parameter.");
            throw new ExtendedIllegalStateException("element", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        StringBuffer tag = new StringBuffer("<caption");
        if (align_ != null)
        {
            tag.append(" align=\"");
            tag.append(align_);
            tag.append("\"");
        }
        tag.append(getLanguageAttributeTag());              //$B1A
        tag.append(getDirectionAttributeTag());             //$B1A
        tag.append(getAttributeString());                   // @Z1A
        tag.append(">");
        tag.append(element_.getTag());      
        tag.append("</caption>\n");

        
        return tag.toString();                              //@D1C

    }

    /**
    *  Returns the XSL-FO caption tag.
    *  The language attribute is not supported in XSL-FO.  The table caption will appear at the
    *  left of the page if align=left, right of the page if align=right, or at the center
    *  of the page for the rest of the alignments.
    *  @return The caption tag.
    **/
    public String getFOTag()                    //@D1A
    {
        //Save current state of useFO_
        boolean useFO = useFO_;

        //Indicate Formatting Object tags are used.
        setUseFO(true);

        if (element_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting the 'element' parameter.");
            throw new ExtendedIllegalStateException("element", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        
        StringBuffer tag = new StringBuffer("");

        tag.append("<fo:block");
        if(align_!=null)
        {
            tag.append(" text-align=\"");
            if(align_.equals(HTMLConstants.CENTER))
                tag.append("center\"");
            else if(align_.equals(HTMLConstants.RIGHT))
                tag.append("end\"");
            else if(align_.equals(HTMLConstants.LEFT))
                tag.append("start\"");
            else
                tag.append("center\"");
        }
        tag.append(getDirectionAttributeTag());             
        tag.append(">");
        tag.append(element_.getFOTag());
        tag.append("</fo:block>\n");

        //Set useFO_ to previous state
        setUseFO(useFO);
        
        return tag.toString();

    }

    /**
    *  Returns if Formatting Object tags are outputted.
    *  The default value is false.
    *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
    **/
    public boolean isUseFO()                        //@D1A
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
    *  Sets the caption alignment relative to the table.
    *  @param alignment The caption alignment.  One of the following constants
    *  defined in HTMLConstants:  BOTTOM, LEFT, RIGHT, or TOP.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setAlignment(String alignment) throws PropertyVetoException
    {
        if (alignment == null)
        {
            throw new NullPointerException("alignment");
        }
        else if (alignment.equalsIgnoreCase(BOTTOM) ||
                 alignment.equalsIgnoreCase(TOP) ||
                 alignment.equalsIgnoreCase(LEFT) ||
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
    *  Sets the specified <i>text</i> for the caption.
    *  @param text The caption text.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setElement(String text) throws PropertyVetoException
    {
        setElement(new HTMLText(text));
    }

    /**
    *  Sets the element for the caption.
    *  @param element An HTMLTagElement.
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
    * Sets if Formatting Object tags should be used. 
    *  The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML. 
    **/
    public void setUseFO(boolean useFO)                        //@D1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
    *  Returns the HTML caption tag.
    *  @return The caption tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
