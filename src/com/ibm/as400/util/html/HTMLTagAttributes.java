///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTagAttributes.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Properties;
import java.util.Enumeration;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
*  The HTMLTagAttributes class represents any additional HTML tag attributes
*  not implemented in the HTML classes.
*    
*  <p>HTMLTagAttributes objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>  
**/
public abstract class HTMLTagAttributes implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private Properties attributes_;      // The additional html tag attributes.
    
    transient PropertyChangeSupport changes_; //@CRS
    

    /**
    *  Constructs a default HTMLTagAttributes.
    **/
    public HTMLTagAttributes()
    {
       super();
    }


    /**
     * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
     * <b>propertyChange</b> method will be called each time the value of any
     * bound property is changed.
     * 
     * @see #removePropertyChangeListener
     *
     * @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
      if (listener == null)
           throw new NullPointerException ("listener");
      if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS
      changes_.addPropertyChangeListener(listener);
    }

    
    /**
     *  Returns the attribute properties object.
     *
     *  @return The attributes.
     **/
    public Properties getAttributes()
    {
       return attributes_;
    }


    /**
     *  Returns the attribute string.
     *
     *  @return The attributes.
     **/
    public String getAttributeString()
    {
       if (attributes_ == null)
          return "";

       StringBuffer buffer = new StringBuffer("");

       Enumeration e = attributes_.propertyNames();
       
       while(e.hasMoreElements())
       {
          String s = (String)e.nextElement();
          buffer.append(" ");
          buffer.append(s);
          buffer.append("=");
          buffer.append("\"");
          buffer.append(attributes_.getProperty(s));
          buffer.append("\"");
       }

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
     *  Removes the PropertyChangeListener from the internal list.
     *  If the PropertyChangeListener is not on the list, nothing is done.
     *
     *  @see #addPropertyChangeListener
     *
     *  @param listener The PropertyChangeListener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS
    }


    /**
     *  Set the additional HTML tag attributes.
     *
     *  @param attributes The attributes.
     **/
    public void setAttributes(Properties attributes)
    {
       if (attributes == null)
          throw new NullPointerException("attributes");

       Properties old = attributes_;
       
       attributes_ = attributes;

       if (changes_ != null) changes_.firePropertyChange("attributes", old, attributes_ ); //@CRS
    }
}

