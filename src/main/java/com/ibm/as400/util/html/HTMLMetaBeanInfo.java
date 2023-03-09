///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLMetaBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;


import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;


/**
*  The HTMLMetaBeanInfo class provides bean information for the HTMLMeta class.
**/
public class HTMLMetaBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = HTMLMeta.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;

    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;


    static
    {
      
      try
      {
        EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                         "propertyChange",
                         java.beans.PropertyChangeListener.class,
                         "propertyChange");
        changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
        changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

        EventSetDescriptor[] events = {changed};

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor name = new PropertyDescriptor("name", beanClass,
                                        "getName", "setName");
        name.setBound(true);
        name.setConstrained(false);
        name.setDisplayName(loader_.getText("PROP_NAME_NAME"));
        name.setShortDescription(loader_.getText("PROP_HM_DESC_NAME"));

        PropertyDescriptor HttpEquiv = new PropertyDescriptor("HTTP-EQUIV", beanClass,
                                        "getHttpEquiv", "setHttpEquiv");
        HttpEquiv.setBound(true);
        HttpEquiv.setConstrained(false);
        HttpEquiv.setDisplayName(loader_.getText("PROP_NAME_HTTPEQUIV"));
        HttpEquiv.setShortDescription(loader_.getText("PROP_HM_DESC_HTTPEQUIV"));

        PropertyDescriptor content = new PropertyDescriptor("content", beanClass,
                                        "getContent", "setContent");
        content.setBound(true);
        content.setConstrained(false);
        content.setDisplayName(loader_.getText("PROP_NAME_CONTENT"));
        content.setShortDescription(loader_.getText("PROP_HM_DESC_CONTENT"));

        PropertyDescriptor url = new PropertyDescriptor("url", beanClass,
                                        "getUrl", "setUrl");
        url.setBound(true);
        url.setConstrained(false);
        url.setDisplayName(loader_.getText("PROP_NAME_URL"));
        url.setShortDescription(loader_.getText("PROP_HM_DESC_URL"));

        PropertyDescriptor lang = new PropertyDescriptor("lang", beanClass, "getLanguage", "setLanguage");   
        lang.setBound(true);                                                                                 
        lang.setConstrained(false);                                                                           
        lang.setDisplayName(loader_.getText("PROP_NAME_LANGUAGE"));                                          
        lang.setShortDescription(loader_.getText("PROP_DESC_LANGUAGE"));                                     

        PropertyDescriptor dir = new PropertyDescriptor("dir", beanClass, "getDirection", "setDirection");   
        dir.setBound(true);                                                                                  
        dir.setConstrained(false);                                                                            
        dir.setDisplayName(loader_.getText("PROP_NAME_DIRECTION"));                                          
        dir.setShortDescription(loader_.getText("PROP_DESC_DIRECTION"));                                     

                                                                                                             
        properties_ = new PropertyDescriptor[] {name, HttpEquiv, content, url, lang, dir};
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLMeta is a subclass of HTMLTagAttributes, this method
     * will return a HTMLTagAttributesBeanInfo object.
     *
     * @return BeanInfo[] containing this bean's superclass BeanInfo
     **/
    public BeanInfo[] getAdditionalBeanInfo()                            // @Z1A
    {
       return new BeanInfo[] { new HTMLTagAttributesBeanInfo() };        
    }


    /**
    Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }

    
    /**
    Returns the index of the default event.
      @return The index to the default event.
    **/
    public int getDefaultEventIndex()
    {
        return 0;
    }

    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    /**
      Returns the descriptors for all events.
      @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }

    /**
      Returns an image for the icon.

      @param icon    The icon size and color.
      @return        The image.
    **/
    public Image getIcon (int icon)
    {
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("HTMLMeta16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLMeta32.gif");
                break;
        }
        return image;
    }

 
    /**
    *  Returns the descriptors for all properties.
    *  @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
