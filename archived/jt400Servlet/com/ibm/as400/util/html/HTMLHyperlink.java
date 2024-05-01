///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLHyperlink.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.util.servlet.ServletHyperlink;

import java.util.Enumeration;
import java.util.Properties;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;

/**
*  The HTMLHyperlink class represents an HTML hyperlink tag.
*  
*  <P>This example creates an HTMLHyperlink and displays the HTML tag.
*  <BLOCKQUOTE><PRE>
*  HTMLHyperlink link = new HTMLHyperlink("http://www.myCompany.com", "myCompany Home Page");
*  System.out.println(link.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the HTMLHyperlink:
*  <BLOCKQUOTE><PRE>
*  &lt;a href=&quot;http://www.myCompany.com&quot;&gt;myCompany Home Page&lt;/a&gt;
*  </PRE></BLOCKQUOTE>
*
*  <P>This example creates an HTMLHyperlink and sets two properties.
*  <BLOCKQUOTE><PRE>
*  HTMLHyperlink link = new HTMLHyperlink("http://www.myCompany.com", "myCompany Home Page");
*  Properties properties = new Properties();
*  properties.put("userID", "fred");
*  properties.put("employeeID", "01234567");
*  link.setProperties(properties);
*  System.out.println(link.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the HTMLHyperlink:
*  <BLOCKQUOTE><PRE>
*  &lt;a href=&quot;http://www.myCompany.com?userid=fred&amp;employeeID=01234567&quot;&gt;myCompany Home Page&lt;/a&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLHyperlink objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class HTMLHyperlink extends HTMLTagAttributes implements HTMLConstants, java.io.Serializable  // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 83601853360783566L;

    private String bookmarkName_;          // The bookmark name.
    private String link_;                  // The network address for the link resource.
    private Properties properties_;        // Properties associated with the hyperlink. (ie. parameters for URL rewriting)
    private String target_;                // The target frame for the link resource.
    private String text_;                  // The text to be used to represent the link.
    private String title_;                 // The title for the link resource.
    private String location_;              // The bookmark location (ie - #location) for the link resource.  // $B4A

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    transient VetoableChangeSupport vetos_; //@CRS

    /**
    *  Creates a default HTMLHyperlink object.
    **/
    public HTMLHyperlink()
    {
    }

    /**
    *  Creates an HTMLHyperlink object with the specified resource <i>link</i>.
    *  @param link The Uniform Resource Identifier (URI).
    **/
    public HTMLHyperlink(String link)
    {
        if (link == null)
            throw new NullPointerException("link");
        link_ = link;      
    }

    /**
    *  Creates an HTMLHyperlink object with the specified resource <i>link</i>
    *  represented by the specified <i>text</i>.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param text The text representation for the resource.
    **/
    public HTMLHyperlink(String link, String text)
    {
        this(link);

        if (text == null)
            throw new NullPointerException("text");
        text_ = text;
    }

    /**
    *  Creates an HTMLHyperlink object with the specified resource <i>link</i>
    *  and <i>target</i> frame represented by the specified <i>text</i>.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param text The text representation for the resource.
    *  @param target The target frame.
    **/
    public HTMLHyperlink(String link, String text, String target)
    {
        this(link,text);

        if (target == null)
            throw new NullPointerException("target");
        target_ = target;
    }


    /**
    Adds the VetoableChangeListener.  The specified
    VetoableChangeListener's <b>vetoableChange</b> 
    method is called each time the value of any
    constrained property is changed.
      @see #removeVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
        vetos_.addVetoableChangeListener(listener);
    }

    /**
     *  Returns a copy of the HTMLHyperlink.
     *
     *  @return An HTMLHyperlink.  
     **/
    public Object clone()                          //$B3A
    {
        HTMLHyperlink l = new HTMLHyperlink();
        try
        {
            if (location_ != null)                   //$B4A
                l.setLocation(location_);            //$B4A

            if (properties_ != null)                 // @B5A
                l.setProperties(properties_);        // @B5A

            if (getAttributes() != null)             // @B5A
                l.setAttributes(getAttributes());    // @B5A

            if (link_ != null)
                l.setLink(link_);

            if (target_ != null)
                l.setTarget(target_);

            if (text_ != null)
                l.setText(text_);

            if (title_ != null)
                l.setTitle(title_);

            if (dir_ != null)
                l.setDirection(dir_);

            if (lang_ != null)
                l.setLanguage(lang_);

            if (bookmarkName_ != null)
                l.setName(bookmarkName_);
        }
        catch (PropertyVetoException e)
        { /* Ignore */
        }

        return l;
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
        //@C1D

        if ((lang_ != null) && (lang_.length() > 0))
            return " lang=\"" + lang_ + "\"";
        else
            return "";
    }


    /**
    *  Returns the Uniform Resource Identifier (URI) for the resource link.
    *  @return The Uniform Resource Identifier.
    **/
    public String getLink()
    {
        return link_;
    }


    /**
     *  Returns the bookmark <i>locatoin</i> of the resource link.
     *  @return The location.
     **/
    public String getLocation()
    {
        return location_;
    }


    /**
    *  Returns the bookmark name.
    *  @return The name.   
    **/
    public String getName()
    {
        return bookmarkName_;
    }

    /**
    *  Returns the properties associated with the link resource.
    *  @return The properties.
    **/
    public Properties getProperties()
    {
        return properties_;
    }

    /**
    *  Returns the target frame for the resource link.
    *  @return The target frame.  a user-defined frame or one
    *  of the following constants defined in HTMLConstants:
    *  TARGET_BLANK, TARGET_PARENT, TARGET_SELF, or TARGET_TOP.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getTarget()
    {
        return target_;
    }

    /**
    *  Returns the text that represents the resource link.
    *  This the text that is shown in the HTML document.
    *  @return The text.
    **/
    public String getText()
    {
        return text_;
    }

    /**
    *  Returns the title for the resource link.
    *  @return The title.
    **/
    public String getTitle()
    {
        return title_;
    }  

    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@D1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- An HTMLHyperlink was here -->";
    }

    /**
    *  Returns the HTML tag that represents the resource link.
    *  @return The HTML tag.
    **/
    public String getTag()
    {
        return getTag(text_, properties_);
    }

    /**
    *  Returns the HTML tag that represents the resource link 
    *  with the specified <i>text</i> and <i>properties</i>.  The original HTMLHyperlink object <i>text</i> 
    *  and <i>properties</i> are not changed/updated.
    *  @param text The text.
    *  @param properties The Properties.
    *  @return The HTML tag.
    **/
    public String getTag(String text, Properties properties)
    {
        return getTag(text, bookmarkName_, link_, properties);                                                  //$B2C
    }


    /**
    *  Returns the HTML tag that represents the resource link 
    *  with the specified <i>text</i>, bookmark <i>name</i>, resource <i>link</i>, and <i>properties</i>.  
    *  The original HTMLHyperlink object <i>text</i>, bookmark <i>name</i>, resource <i>link</i>, and <i>properties</i> 
    *  are not changed/updated.
    *  @param text The text.
    *  @param name The bookmark name.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param properties The Properties.
    *  @return The HTML tag.
    **/
    public String getTag(String text, String name, String link, Properties properties)                         //$B2A
    {
        //@C1D

        // Verify that the link has been set.
        if (link == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting the link.");
            throw new ExtendedIllegalStateException("link", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Validate the text parameter.
        if (text == null)
            throw new NullPointerException("text");


        // create the tag.
        StringBuffer buffer = new StringBuffer();

        buffer.append("<a href=\"");
        buffer.append(link);     

        if (properties != null)
        {
            String propName;
            String parmStart = "?";
            Enumeration propertyList = properties.propertyNames();
            while (propertyList.hasMoreElements())
            {
                propName = (String)propertyList.nextElement();
                buffer.append(parmStart);
                buffer.append(URLEncoder.encode(propName));
                buffer.append("=");
                buffer.append(URLEncoder.encode(properties.getProperty(propName)));
                parmStart = "&";
            }
        }

        if (location_ != null)                           //$B4A
        {
            buffer.append("#");                           //$B4A
            buffer.append(location_);                     //$B4A
        }

        buffer.append("\"");

        if (name != null)
        {
            buffer.append(" name=\"");
            buffer.append(name);
            buffer.append("\"");
        }

        if (title_ != null)
        {
            buffer.append(" title=\"");
            buffer.append(title_);
            buffer.append("\"");
        }

        if (target_ != null)
        {
            buffer.append(" target=\"");
            buffer.append(target_);
            buffer.append("\"");
        }

        buffer.append(getLanguageAttributeTag());                                          //$B1A
        buffer.append(getDirectionAttributeTag());                                         //$B1A
        buffer.append(getAttributeString());                                               // @Z1A

        buffer.append(">");
        buffer.append(text);
        buffer.append("</a>");
        return new String(buffer);           
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
    Removes the VetoableChangeListener from the internal list.
    If the VetoableChangeListener is not on the list, nothing is done.
      @see #addVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS
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
    *  Sets the <i>language</i> of the input tag.
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
     *  Sets the bookmark location of the resource link within a document.  The location
     *  is denoted with the # symbol at the end of the 
     *  link followed by a <i>location</i>. (ie - http://myPage.html#myBookmarkLocation)
     *  @param location The location.
     **/
    public void setLocation(String location)                                  //$B4A
    {
        if (location == null)
            throw new NullPointerException("location");

        String old = location_;

        location_ = location;

        if (changes_ != null) changes_.firePropertyChange("location", old, location); //@CRS
    }


    /**
    *  Sets the Uniform Resource Identifier (URI) for the resource <i>link</i>.
    *  @param link The Uniform Resource Identifier.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setLink(String link) throws PropertyVetoException
    {
        if (link == null)
            throw new NullPointerException("link");

        String old = link_;
        if (vetos_ != null) vetos_.fireVetoableChange("link", old, link ); //@CRS

        link_ = link;

        if (changes_ != null) changes_.firePropertyChange("link", old, link ); //@CRS
    }

    /**
    *  Sets the bookmark <i>name</i>.
    *  @param name The bookmark name.   
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setName(String name) throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");

        String old = bookmarkName_;
        if (vetos_ != null) vetos_.fireVetoableChange("name", old, name); //@CRS

        bookmarkName_ = name;      

        if (changes_ != null) changes_.firePropertyChange("name", old, name); //@CRS
    }

    /**
    *  Sets the <i>properties</i> associated with the resource link.
    *  The properties are the attributes associated with the
    *  Uniform Resource Identifier.
    *  @param properties The properties.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setProperties(Properties properties) throws PropertyVetoException
    {
        if (properties == null)
            throw new NullPointerException("properties");

        Properties old = properties_;
        if (vetos_ != null) vetos_.fireVetoableChange("properties", old, properties); //@CRS

        properties_ = properties;

        if (changes_ != null) changes_.firePropertyChange("properties", old, properties); //@CRS
    }

    /**
    *  Sets the <i>target</i> frame for the resource link.
    *  @param target The target frame.  A user-defined frame or
    *  one of the following constants defined in HTMLConstants:
    *  TARGET_BLANK, TARGET_PARENT, TARGET_SELF, or TARGET_TOP.
    *  @exception PropertyVetoException If the change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setTarget(String target) throws PropertyVetoException
    {
        if (target == null)
            throw new NullPointerException("target");

        String old = target_;
        if (vetos_ != null) vetos_.fireVetoableChange("target", old, target); //@CRS

        target_ = target;

        if (changes_ != null) changes_.firePropertyChange("target", old, target); //@CRS
    }

    /**
    *  Sets the <i>text</i> representation for the resource link.
    *  This is the text that is shown in the HTML document.
    *  @param text The text.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setText(String text) throws PropertyVetoException
    {
        if (text == null)
            throw new NullPointerException("text");

        String old = text_;
        if (vetos_ != null) vetos_.fireVetoableChange("text", old, text); //@CRS

        text_ = text;

        if (changes_ != null) changes_.firePropertyChange("text", old, text); //@CRS
    }

    /**
    *  Sets the <i>title</i> for the resource link.
    *  @param title The title.
    *  @exception PropertyVetoException If the change is vetoed.
    **/
    public void setTitle(String title) throws PropertyVetoException
    {
        if (title == null)
            throw new NullPointerException("title");

        String old = title_;
        if (vetos_ != null) vetos_.fireVetoableChange("title", old, title); //@CRS

        title_ = title;

        if (changes_ != null) changes_.firePropertyChange("title", old, title); //@CRS
    }

    /**
    *  Returns the HTML tag that represents the resource link.
    *  @return The HTML tag.
    **/
    public String toString()
    {
        return getTag();
    }
}

