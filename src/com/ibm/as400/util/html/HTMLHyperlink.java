///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLHyperlink.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
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
public class HTMLHyperlink implements HTMLTagElement, HTMLConstants, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String bookmarkName_;          // The bookmark name.
   private String link_;                  // The network address for the link resource.
   private Properties properties_;        // Properties associated with the hyperlink. (ie. parameters for URL rewriting)
   private String target_;                // The target frame for the link resource.
   private String text_;                  // The text to be used to represent the link.
   private String title_;                 // The title for the link resource.   
   
   transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

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
      changes_.addPropertyChangeListener(listener);
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
      vetos_.addVetoableChangeListener(listener);
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
      if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "Generating HTMLHyperlink tag...");

      // Verify that the link has been set.
      if (link_ == null)
      {
         Trace.log(Trace.ERROR, "Attempting to get tag before setting the link.");
         throw new ExtendedIllegalStateException("link", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      // Validate the text parameter.
      if (text == null)
         throw new NullPointerException("text");
   

      // create the tag.
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("<a href=\"" + link_);     
      
      if (properties != null) 
      {
         String name;
         String parmStart = "?";
         Enumeration propertyList = properties.propertyNames();
         while (propertyList.hasMoreElements()) 
         {
            name = (String)propertyList.nextElement();
            buffer.append(parmStart);
            buffer.append(URLEncoder.encode(name));
            buffer.append("=" + URLEncoder.encode(properties.getProperty(name)));
            parmStart = "&";
         }
      }
      buffer.append("\"");

      if (bookmarkName_ != null) 
         buffer.append(" name=\"" + bookmarkName_ + "\"");
      
      if (title_ != null) 
         buffer.append(" title=\"" + title_ + "\"");
      
      if (target_ != null) 
         buffer.append(" target=\"" + target_ + "\"");
      
      buffer.append(">" + text + "</a>");
      return new String(buffer);           
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
   Removes the PropertyChangeListener from the internal list.
   If the PropertyChangeListener is not on the list, nothing is done.
     @see #addPropertyChangeListener
     @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      changes_.removePropertyChangeListener(listener);
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
      vetos_.removeVetoableChangeListener(listener);
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
      vetos_.fireVetoableChange("link", old, link );
      
      link_ = link;

      changes_.firePropertyChange("link", old, link );
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
      vetos_.fireVetoableChange("name", old, name);

      bookmarkName_ = name;      

      changes_.firePropertyChange("name", old, name); 
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
      vetos_.fireVetoableChange("properties", old, properties);

      properties_ = properties;

      changes_.firePropertyChange("properties", old, properties);
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
      vetos_.fireVetoableChange("target", old, target);

      target_ = target;

      changes_.firePropertyChange("target", old, target);
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
      vetos_.fireVetoableChange("text", old, text);

      text_ = text;

      changes_.firePropertyChange("text", old, text);
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
      vetos_.fireVetoableChange("title", old, title);
      
      title_ = title;

      changes_.firePropertyChange("title", old, title);
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

