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
*  <p>HTMLHeading objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLHeading extends HTMLTagAttributes implements java.io.Serializable           // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private int level_;
    private String text_;
    private String align_;                                      // @B3C

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A





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
        //@B4D

        if ((dir_ != null) && (dir_.length() > 0))
            return " dir=\"" + dir_ + "\"";
        else
            return "";
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
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        changes_ = new PropertyChangeSupport(this);
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

        changes_.firePropertyChange("align", old, align );

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

        changes_.firePropertyChange("dir", old, dir );
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

        changes_.firePropertyChange("lang", old, lang );
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

        changes_.firePropertyChange("level", new Integer(old), new Integer(level) );
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

        changes_.firePropertyChange("text", old, text );
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
