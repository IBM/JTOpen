///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLHeading.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


/**
*  The HTMLHeading class represents a section heading in an HTML page.
*  <P>
*  This example creates a HTMLHeading tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLHeading.
*  HTMLHeading header = new HTMLHeading(1, "My Heading", HTMLConstants.CENTER);
*  System.out.println(header);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLHeading tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;h1 align=&quot;center&quot;&gt;My Heading&lt;/h1&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>
*  Calling getFOTag() would produce the following XSL Formatting Object tag:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:block-container&gt;
*  &lt;fo:block font-size='25pt' text-align='center'&gt;My Heading&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLHeading objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLHeading extends HTMLTagAttributes implements java.io.Serializable           // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 4715727576328707163L;


    private int level_;
    private String text_;
    private String align_;                                      // @B3C

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A
    private boolean useFO_ = false; //Indicates if XSL-FO tags are outputted.                //@C1A





    /**
    *  Constructs a default HTMLHeading object.
    **/
    public HTMLHeading()
    {
        super();

        setLevel(1);
    }


    /**
    *  Constructs an HTMLHeading object with the specified heading <i>level</i>.
    *
    *  @param level The heading level.
    **/
    public HTMLHeading(int level)
    {
        super();

        setLevel(level);
    }


    /**
    *  Constructs an HTMLHeading object with the specified heading <i>level</i> and <i>text</i>.
    *
    *  @param level The heading level.
    *  @param text  The heading text.
    **/
    public HTMLHeading(int level, String text)
    {
        super();

        setLevel(level);
        setText(text);
    }


    /**
    *  Constructs an HTMLHeading object with the specified heading <i>level</i>, <i>text</i>, and <i>align</i>.
    *
    *  @param level The heading level.
    *  @param text  The heading text.
    *  @param align The heading alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
    **/
    public HTMLHeading(int level, String text, String align)
    {
        super();

        setLevel(level);
        setText(text);
        setAlign(align);
    }


    /**
     *  Returns the alignment of the header.
     *  @return The alignment.
     **/
    public String getAlign()
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
        if(useFO_ )                                                                      //@C1A
        {                                                                                //@C1A
            if((dir_ != null) && (dir_.length()>0))                                      //@C1A
            {                                                                            //@C1A
                if(dir_.equals(HTMLConstants.RTL))                                       //@C1A
                    return " writing-mode='rl'";                                         //@C1A
                else                                                                     //@C1A
                    return " writing-mode='lr'";                                         //@C1A
            }                                                                            //@C1A
            else                                                                         //@C1A
                return "";                                                               //@C1A
        }                                                                                //@C1A
        else                                                                             //@C1A
        {                                                                                //@C1A
            //@B4D

            if ((dir_ != null) && (dir_.length() > 0))
                return " dir=\"" + dir_ + "\"";
            else
                return "";
        }                                                                                //@C1A

    }


    /**
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
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
        //@B4D

        if ((lang_ != null) && (lang_.length() > 0))
            return " lang=\"" + lang_ + "\"";
        else
            return "";
    }


    /**
     *  Returns the level of the header.
     *  @return The level.
     **/
    public int getLevel()
    {
        return level_;
    }


    /**
     *  Returns the text of the header.
     *  @return The text.
     **/
    public String getText()
    {
        return text_;
    }


    /**
    *  Returns the tag for the HTML heading.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@B4D

        if(useFO_)                      //@C1A
            return getFOTag();          //@C1A

        if (text_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting heading text.");
            throw new ExtendedIllegalStateException(
                                                   "text", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<h" + Integer.toString(level_));

        if (align_ != null)
        {
            if (align_.equals(HTMLConstants.LEFT))
                s.append(" align=\"left\"");
            else if (align_.equals(HTMLConstants.RIGHT))
                s.append(" align=\"right\"");
            else if (align_.equals(HTMLConstants.CENTER))
                s.append(" align=\"center\"");
        }

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A

        s.append(getAttributeString());                                               // @Z1A

        s.append(">" + text_ + "</h");

        if (level_ > 0)
            s.append(Integer.toString(level_));

        s.append(">");

        return s.toString();
    }


    /**
    *  Returns the tag for the XSL-FO heading.
    *  The language attribute is not supported by XSL-FO.
    *  @return The tag.
    **/
    public String getFOTag()                                    //@C1A
    {
        //Save current state of useFO_
        boolean useFO = useFO_;

        //Indicate Formatting Object tags are outputted.
        setUseFO(true);

        if (text_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting heading text.");
            throw new ExtendedIllegalStateException(
                                               "text", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }


        StringBuffer s = new StringBuffer("<fo:block-container");
        s.append(getDirectionAttributeTag());
        s.append(">\n");
        s.append("<fo:block");

        switch(level_)
        {
            case 1: s.append(" font-size='25pt'");
                break;
            case 2:  s.append(" font-size='20pt'");
                break;
            case 3:  s.append(" font-size='15pt'");
                break;
            case 4:  s.append(" font-size='13pt'");
                break;
            case 5: s.append(" font-size='11pt'");
                break;
            case 6: s.append(" font-size='9pt'");
                break;
        }

        if (align_ != null)
        {
            if (align_.equals(HTMLConstants.LEFT))
                s.append(" text-align='start'");
            else if (align_.equals(HTMLConstants.RIGHT))
                s.append(" text-align='end'");
            else if (align_.equals(HTMLConstants.CENTER))
                s.append(" text-align='center'");
        }

        s.append(">");
        s.append(text_);
        s.append("</fo:block>\n");
        s.append("</fo:block-container>\n");
        
        //Set useFO_ to previous state.
        setUseFO(useFO);

        return s.toString();
    }

    /**
     *  Returns if Formatting Object tags are outputted.
     *  The default value is false.
     *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
     **/
    public boolean isUseFO()                                //@C1A
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
    }


    // @B3C
    /**
    *  Sets the horizontal alignment for the header.
    *  @param align The alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/             
    public void setAlign(String align)
    {
        if (align == null)
            throw new NullPointerException("align");

        // If align is not one of the valid HTMLConstants, throw an exception.
        if ( !(align.equals(HTMLConstants.LEFT))  && !(align.equals(HTMLConstants.RIGHT)) && !(align.equals(HTMLConstants.CENTER)) )
        {
            throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        //@B4D

        String old = align_;

        align_ = align;

        if (changes_ != null) changes_.firePropertyChange("align", old, align ); //@CRS

    }


    /**
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setDirection(String dir)                                     //$B1A
    {
        if (dir == null)
            throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
        {
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = dir_;

        dir_ = dir;

        if (changes_ != null) changes_.firePropertyChange("dir", old, dir ); //@CRS
    }


    /**
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    **/
    public void setLanguage(String lang)                                      //$B1A
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
    }


    /**
    *  Sets the level of the header.  Heading 1(H1) is rendered 
    *  as the largest and most important section heading while 
    *  Heading 6(H6) is rendered as the smallest (lowest importance) 
    *  heading.
    *
    *  @param level The heading level (1 - 6).
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setLevel(int level)
    {
        if (level < 1 || level > 6)
            throw new ExtendedIllegalArgumentException("level", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        //@B4D

        int old = level_;

        level_ = level;

        if (changes_ != null) changes_.firePropertyChange("level", new Integer(old), new Integer(level) ); //@CRS
    }


    /**
     *  Set the visible text to display in the header.
     *
     *  @param text The text.
     *
     **/
    public void setText(String text)
    {
        if (text == null)
            throw new NullPointerException("text");

        if (text.length() == 0)
            throw new ExtendedIllegalArgumentException("text", 
                                                       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        //@B4D

        String old = text_;

        text_ = text;

        if (changes_ != null) changes_.firePropertyChange("text", old, text ); //@CRS
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
    *  Returns a String representation for the HTMLHeading tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
