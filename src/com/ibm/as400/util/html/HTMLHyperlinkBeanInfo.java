///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLHyperlinkBeanInfo.java
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
import java.beans.IntrospectionException;
import java.beans.BeanInfo;

/**
The HTMLHyperlinkBeanInfo class provides
bean information for the HTMLHyperlink class.
**/
public class HTMLHyperlinkBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = HTMLHyperlink.class;

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

        EventSetDescriptor veto = new EventSetDescriptor(beanClass,
                         "vetoableChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        EventSetDescriptor[] events = {changed, veto};

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor link = new PropertyDescriptor("link", beanClass,
                                        "getLink", "setLink");
        link.setBound(true);
        link.setConstrained(true);
        link.setDisplayName(loader_.getText("PROP_NAME_LINK"));
        link.setShortDescription(loader_.getText("PROP_DESC_LINK"));

        PropertyDescriptor name = new PropertyDescriptor("name", beanClass,
                                        "getName", "setName");
        name.setBound(true);
        name.setConstrained(true);
        name.setDisplayName(loader_.getText("PROP_NAME_NAME"));
        name.setShortDescription(loader_.getText("PROP_HHLNK_DESC_NAME"));

        PropertyDescriptor properties = new PropertyDescriptor("properties", beanClass,
                                        "getProperties", "setProperties");
        properties.setBound(true);
        properties.setConstrained(true);
        properties.setDisplayName(loader_.getText("PROP_NAME_PROPERTIES"));
        properties.setShortDescription(loader_.getText("PROP_DESC_PROPERTIES"));

        PropertyDescriptor target = new PropertyDescriptor("target", beanClass,
                                        "getTarget", "setTarget");
        target.setBound(true);
        target.setConstrained(true);
        target.setDisplayName(loader_.getText("PROP_NAME_TARGET"));
        target.setShortDescription(loader_.getText("PROP_HHLNK_DESC_TARGET"));

        PropertyDescriptor text = new PropertyDescriptor("text", beanClass,
                                        "getText", "setText");
        text.setBound(true);
        text.setConstrained(true);
        text.setDisplayName(loader_.getText("PROP_NAME_TEXT"));
        text.setShortDescription(loader_.getText("PROP_HHLNK_DESC_TEXT"));

        PropertyDescriptor title = new PropertyDescriptor("title", beanClass,
                                        "getTitle", "setTitle");
        title.setBound(true);
        title.setConstrained(true);
        title.setDisplayName(loader_.getText("PROP_NAME_TITLE"));
        title.setShortDescription(loader_.getText("PROP_DESC_TITLE"));

        properties_ = new PropertyDescriptor[] { link, name, properties, target, text, title };
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
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
                image = loadImage ("HTMLHyperlink16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLHyperlink32.gif");
                break;
        }
        return image;
    }
    
    /**
    Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
