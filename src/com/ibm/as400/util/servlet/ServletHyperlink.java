///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServletHyperlink.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.util.html.HTMLHyperlink;
import com.ibm.as400.util.html.URLEncoder;
import com.ibm.as400.access.Trace;

import java.util.Enumeration;
import java.util.Properties;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.servlet.http.*;

/**
*  The ServletHyperlink class represents an HTML hyperlink tag.
*  
*  <P>This example creates an ServletHyperlink and displays the HTML tag.
*  <BLOCKQUOTE><PRE>
*  ServletHyperlink link = new ServletHyperlink("http://www.myCompany.com", "myCompany Home Page");
*  link.setHttpServletResponse(resp);
*  link.setPathInfo("/myServletDirectory/servlet");
*  System.out.println(link.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the ServletHyperlink:
*  <BLOCKQUOTE><PRE>
*  &lt;a href=&quot;http://www.myCompany.com/myServletDirectory/servlet&SomeSessionID=942349280740&quot;&gt;myCompany Home Page&lt;/a&gt;
*  </PRE></BLOCKQUOTE>
*
*  <P>This example creates an ServletHyperlink and sets two properties.
*  <BLOCKQUOTE><PRE>
*  ServletHyperlink link = new ServletHyperlink("http://www.myCompany.com", "myCompany Home Page");
*  Properties properties = new Properties();
*  properties.put("userID", "fred");
*  properties.put("employeeID", "01234567");
*  link.setProperties(properties);
*  link.setHttpServletResponse(resp);
*  link.setPathInfo("/myServletDirectory/servlet");
*  System.out.println(link.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the ServletHyperlink:
*  <BLOCKQUOTE><PRE>
*  &lt;a href=&quot;http://www.myCompany.com/myServletDirectory/servlet?userid=fred&amp;employeeID=01234567&SomeSessionID=942349280740&quot;&gt;myCompany Home Page&lt;/a&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>ServletHyperlink objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  </ul>
**/
public class ServletHyperlink extends HTMLHyperlink 
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private HttpServletResponse response_;             // An http servlet response **this needs to be
                                                       // transient or else you can't do serialization.

    private String pathInfo_;                          // The extra path info to the servlet location.  
                                                       //   ex. - http://myServlet/myPathInfo


    transient PropertyChangeSupport changes_; //@CRS


    /**
    *  Creates a default ServletHyperlink object.
    **/
    public ServletHyperlink()
    {
    }

    /**
    *  Creates a ServletHyperlink object with the specified resource <i>link</i>.
    *  @param link The Uniform Resource Identifier (URI).
    **/
    public ServletHyperlink(String link)
    {
        super(link);      
    }

    /**
    *  Creates a ServletHyperlink object with the specified resource <i>link</i>
    *  represented by the specified <i>text</i>.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param text The text representation for the resource.
    **/
    public ServletHyperlink(String link, String text)
    {
        super(link, text);
    }

    /**
    *  Creates a ServletHyperlink object with the specified resource <i>link</i>
    *  and <i>target</i> frame represented by the specified <i>text</i>.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param text The text representation for the resource.
    *  @param target The target frame.
    **/
    public ServletHyperlink(String link, String text, String target)
    {
        super(link, text, target);
    }


    /**
    *  Creates a ServletHyperlink object with the specified resource <i>link</i>, link <i>text</i>, 
    *  <i>target</i> frame, resource link <i>path</i>, and HTTPServlet <i>response</i>.
    *  @param link The Uniform Resource Identifier (URI).
    *  @param text The text representation for the resource.
    *  @param target The target frame.
    *  @param path  The resource link path information.
    *  @param response The Http servlet response.
    **/
    public ServletHyperlink(String link, String text, String target, String path, HttpServletResponse response)
    {
        super(link, text, target);

        setPathInfo(path);
        setHttpServletResponse(response);
    }


    /**
    Adds a PropertyChangeListener.  The specified 
    PropertyChangeListener's <b>propertyChange</b> 
    method is called each time the value of any
    bound property is changed.
      @see #removePropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS
        changes_.addPropertyChangeListener(listener);

        //must call the parents change listener since it is
        //in a different package.
        super.addPropertyChangeListener(listener);
    }


    /**
     *  Returns a copy of the ServletHyperlink.
     *
     *  @return An ServletHyperlink.  
     **/
    public Object clone()                           //$A2A
    {
        ServletHyperlink l = new ServletHyperlink();

        try
        {
            if (getHttpServletResponse() != null)
                l.setHttpServletResponse(getHttpServletResponse());

            if (getProperties() != null)
                l.setProperties(getProperties());     // @A5C

            if (getLocation() != null)                  //$A3A
                l.setLocation(getLocation());           //$A3A

            if (getPathInfo() != null)                  // @A4A
                l.setPathInfo(getPathInfo());           // @A4A

            if (getAttributes() != null)                // @A4A
                l.setAttributes(getAttributes());       // @A4A

            if (getLink() != null)
                l.setLink(getLink());

            if (getTarget() != null)
                l.setTarget(getTarget());

            if (getText() != null)
                l.setText(getText());

            if (getTitle() != null)
                l.setTitle(getTitle());

            if (getDirection() != null)
                l.setDirection(getDirection());

            if (getLanguage() != null)
                l.setLanguage(getLanguage());

            if (getName() != null)
                l.setName(getName());
        }
        catch (PropertyVetoException e)
        { /* Ignore */
        }

        return l;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
     **/
    String getDirectionTag()                                                 
    {

        if ((getDirection() != null) && (getDirection().length() > 0))
            return " dir=\"" + getDirection() + "\"";
        else
            return "";
    }


    /**
    *  Returns the Http servlet response.
    *  @return The response.   
    **/
    public HttpServletResponse getHttpServletResponse()
    {
        return response_;
    }


    /**
    *  Returns the language attribute tag.                                            
    *  @return The language tag.                                                      
    **/                                                                               
    String getLanguageTag()                                                  
    {

        if ((getLanguage() != null) && (getLanguage().length() > 0))
            return " lang=\"" + getLanguage() + "\"";
        else
            return "";
    }


    /**
    *  Returns the path information.
    *  @return The path.   
    **/
    public String getPathInfo()
    {
        return pathInfo_;
    }


    /**
    *  Returns the HTML tag that represents the resource link.
    *  @return The HTML tag.
    **/
    public String getTag()
    {
        return getTag(getText(), getProperties());
    }

    /**
    *  Returns the HTML tag that represents the resource link 
    *  with the specified <i>text</i> and <i>properties</i>.  The original ServletHyperlink object <i>text</i> 
    *  and <i>properties</i> are not changed/updated.
    *  @param text The text.
    *  @param properties The Properties.
    *  @return The HTML tag.
    **/
    public String getTag(String text, Properties properties)
    {

        // Verify that the link has been set.
        if (getLink() == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting the link.");
            throw new ExtendedIllegalStateException("link", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Validate the text parameter.
        if (text == null)
            throw new NullPointerException("text");


        // create the tag.
        StringBuffer link = new StringBuffer(getLink());

        //path info for servlet ex.- http://myServer/myPathInfo
        if (pathInfo_ != null)
        {
            // if the link ends with a "/", the path does not need a leading "/"
            if (getLink().endsWith("/"))
            {
                if (pathInfo_.startsWith("/"))
                    pathInfo_ = pathInfo_.substring(1);
                else
                    pathInfo_ = pathInfo_;
            }
            else   //link does not end with a "/", so the path needs to start with "/"
            {
                if (pathInfo_.startsWith("/"))
                    pathInfo_ = pathInfo_;
                else
                    pathInfo_ = "/" + pathInfo_;
            }

            // place holder for real implementation...
            link.append(URLEncoder.encode(pathInfo_, false));
        }

        if (properties != null)
        {
            String name;
            String parmStart = "?";
            Enumeration propertyList = properties.propertyNames();
            while (propertyList.hasMoreElements())
            {
                name = (String)propertyList.nextElement();
                link.append(parmStart);
                link.append(URLEncoder.encode(name));
                link.append("=");
                link.append(URLEncoder.encode(properties.getProperty(name)));
                parmStart = "&";
            }
        }

        StringBuffer url = new StringBuffer();;

        if (response_ != null)
            url.append(response_.encodeUrl(link.toString()));
        else
            url.append(link.toString());

        // create the tag.
        StringBuffer buffer = new StringBuffer();

        buffer.append("<a href=\"");
        buffer.append(url.toString());

        String location = getLocation();                //$A3A
        if (location != null)                           //$A3A
        {
            buffer.append("#");                          //$A3A
            buffer.append(location);                     //$A3A
        }

        buffer.append("\"");

        String name = getName();
        if (name != null)
        {
            buffer.append(" name=\"");
            buffer.append(name);
            buffer.append("\"");
        }

        String title = getTitle();
        if (title != null)
        {
            buffer.append(" title=\"");
            buffer.append(title);
            buffer.append("\"");
        }

        String target = getTarget();
        if (target != null)
        {
            buffer.append(" target=\"");
            buffer.append(target);
            buffer.append("\"");
        }

        buffer.append(getLanguageTag());                                          
        buffer.append(getDirectionTag());                                         
        buffer.append(getAttributeString());              // @Z1A

        buffer.append(">");
        buffer.append(text);
        buffer.append("</a>");

        return buffer.toString();
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

    /**
    Removes the PropertyChangeListener from the internal list.
    If the PropertyChangeListener is not on the list, nothing is done.
      @see #addPropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS

        //must call the parents change listener since it is
        //in a different package.
        super.removePropertyChangeListener(listener);
    }


    /**
    *  Sets the Http servlet <i>response</i> for the resource link.
    *
    *  @param response The Http servlet response.
    **/
    public void setHttpServletResponse(HttpServletResponse response)
    {
        if (response == null)
            throw new NullPointerException("response");

        HttpServletResponse old = response_;

        response_ = response;

        if (changes_ != null) changes_.firePropertyChange("response", old, response); //@CRS
    }


    /**
    *  Sets the <i>path</i> information for the resource link.
    *
    *  @param path The path information.
    **/
    public void setPathInfo(String path) 
    {

        if (path == null)
            throw new NullPointerException("path");

        String old = pathInfo_;

        pathInfo_ = path;

        if (changes_ != null) changes_.firePropertyChange("path", old, path); //@CRS
    }

}
