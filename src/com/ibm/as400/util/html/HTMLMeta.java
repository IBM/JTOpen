///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLMeta.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
*  The HTMLMeta class represents meta-information used within an HTMLHead tag.
*  This meta information can be used in identifying, indexing, and defining
*  information within the HTML document.
*  <P>
*  This example creates an HTMLMeta tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLMeta.
*  HTMLMeta meta = new HTMLMeta("Expires", "Mon, 01 Jun 2000 12:00:00 CST");
*  System.out.println(header);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLMeta tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;meta http-equiv=&quot;Expires&quot; content=&quot;Mon, 01 Jun 2000 12:00:00 CST&quot; /&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLMeta objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLMeta extends HTMLTagAttributes implements java.io.Serializable      // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String content_;
    private String name_;
    private String HttpEquiv_;
    private String url_;

    private String lang_;        // The primary language used to display the tags contents.  
    private String dir_;         // The direction of the text interpretation.                



    /**
    *  Constructs a default HTMLMeta object.
    **/
    public HTMLMeta()
    {
       super();
    }


    /**
    *  Constructs an HTMLMeta object with the specified meta <i>HTTP-EQUIV</i> and <i>content</i>.
    *
    *  @param HttpEquiv The HTTP-EQUIV meta information.
    *  @param content  The value of a named property.
    **/
    public HTMLMeta(String HttpEquiv, String content)
    {
       super();
       
       setHttpEquiv(HttpEquiv);
       setContent(content);
    }


    /**
    *  Constructs an HTMLMeta object with the specified meta <i>HTTP-EQUIV</i>, <i>content</i>, and <i>URL</i>.
    *
    *  @param HttpEquiv The HTTP-EQUIV meta information.
    *  @param content  The value of a named property.
    *  @param url The URL to reload after the time specified in the content attribute.
    **/
    public HTMLMeta(String HttpEquiv, String content, String url)
    {
       super();
       
       setHttpEquiv(HttpEquiv);
       setContent(content);
       setUrl(url);
    }


    
    /**
    *  Returns the <i>content</i> of the HTMLMeta tag.
    *  @return The value of a named property.
    **/
    public String getContent()                               
    {
        return content_;
    }

    
    /**
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()                               
    {
        return dir_;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 
    {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving direction attribute tag.");

       if ((dir_ != null) && (dir_.length() > 0))
          return " dir=\"" + dir_ + "\"";
       else
          return "";
    }


    /**
    *  Returns the <i>HTTP-EQUIV</i> of the meta tag.
    *  @return The HTTP-EQUIV meta information.
    **/
    public String getHttpEquiv()                               
    {
        return HttpEquiv_;
    }


    /**
    *  Returns the <i>language</i> of the meta tag.
    *  @return The language of the meta tag.
    **/
    public String getLanguage()                                
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.                                            
    *  @return The language tag.                                                      
    **/                                                                               
    String getLanguageAttributeTag()                                                  
    {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving language attribute tag.");

       if ((lang_ != null) && (lang_.length() > 0))
          return " lang=\"" + lang_ + "\"";
       else
          return "";
    }


    /**
    *  Returns the <i>name</i> of the meta tag.
    *  @return The name of a property.
    **/
    public String getName()                                
    {
        return name_;
    }


    /**
    *  Returns the <i>URL</i> of the meta tag.
    *  @return The URL to reload.
    **/
    public String getUrl()                                
    {
        return url_;
    }

    
    /**
    *  Returns the tag for the HTML heading.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLMeta tag...");

        if(content_ == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting meta content.");
           throw new ExtendedIllegalStateException(
               "content", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if (name_ == null && HttpEquiv_ == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting name or http-equiv attributes.");
           throw new ExtendedIllegalStateException("name/HttpEquiv", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        
        
        StringBuffer s = new StringBuffer("<meta");
        
        if (HttpEquiv_ != null)
        {
           s.append(" http-equiv=\"");
           s.append(HttpEquiv_);
           s.append("\"");
        }
        else
        {
           s.append(" name=\"");
           s.append(name_);
           s.append("\"");
        }

        if (url_ != null)
        {   
           s.append(" content=\"");
           s.append(content_);
           s.append("; URL=");
           s.append(url_);
           s.append("\"");
        }
        else
        {
           s.append(" content=\"");
           s.append(content_);
           s.append("\"");
        }

        s.append(getLanguageAttributeTag());                                          
        s.append(getDirectionAttributeTag()); 
        s.append(getAttributeString());            // @Z1A

        s.append(" />\n");
        
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


    
    /**
    *  Sets the <i>content</i> of the meta information.
    *  @param content The value for a named property.
    **/
    public void setContent(String content)                                     
    {   
        if (content == null)
           throw new NullPointerException("content");

        String old = content_;

        content_ = content;

        changes_.firePropertyChange("content", old, content );
    }

    
    /**
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setDirection(String dir)                                     
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
    *  Sets the <i>HTTP-EQUIV</i> of the meta tag.
    *
    *  @param HttpEquiv The HTTP-EQUIV meta information.
    **/
    public void setHttpEquiv(String HttpEquiv)                                     
    {   
        if (HttpEquiv == null)
           throw new NullPointerException("HttpEquiv");

        String old = HttpEquiv_;

        HttpEquiv_ = HttpEquiv;

        changes_.firePropertyChange("HttpEquiv", old, HttpEquiv );
    }


    /**
    *  Sets the <i>language</i> of the meta tag.
    *
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    **/
    public void setLanguage(String lang)                                      
    {   
        if (lang == null)
           throw new NullPointerException("lang");

        String old = lang_;

        lang_ = lang;

        changes_.firePropertyChange("lang", old, lang );
    }


    /**
    *  Sets the <i>name</i> of the meta tag.  If the name is
    *  not set, it is assumed to be the same as the value of
    *  the <i>HTTP-EQUIV</i>.
    *
    *  @param name The name of a property.
    **/
    public void setName(String name)                                      
    {   
        if (name == null)
           throw new NullPointerException("name");

        String old = name_;

        name_ = name;

        changes_.firePropertyChange("name", old, name );
    }


    /**
    *  Sets the <i>URL</i> to reload after the time specified 
    *  in the <i>content</i> attribute.
    *
    *  @param url The URL to reload.
    **/
    public void setUrl(String url)                                      
    {   
        if (url == null)
           throw new NullPointerException("url");

        String old = url_;

        url_ = url;

        changes_.firePropertyChange("url", old, url );
    }

    
    /**
    *  Returns a String representation for the HTMLMeta tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}

