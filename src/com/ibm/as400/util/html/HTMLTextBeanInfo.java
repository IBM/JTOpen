///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTextBeanInfo.java
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
The HTMLTextBeanInfo class provides
bean information for the HTMLText class.
**/
public class HTMLTextBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = HTMLText.class;

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
        PropertyDescriptor alignment = new PropertyDescriptor("alignment", beanClass,
                                           "getAlignment", "setAlignment");
        alignment.setBound(true);
        alignment.setConstrained(true);
        alignment.setDisplayName(loader_.getText("PROP_NAME_ALIGNMENT"));
        alignment.setShortDescription(loader_.getText("PROP_HTXT_DESC_ALIGNMENT"));

        PropertyDescriptor bold = new PropertyDescriptor("bold", beanClass,
                                        "isBold", "setBold");
        bold.setBound(true);
        bold.setConstrained(true);
        bold.setDisplayName(loader_.getText("PROP_NAME_BOLD"));
        bold.setShortDescription(loader_.getText("PROP_DESC_BOLD"));

        PropertyDescriptor color = new PropertyDescriptor("color", beanClass,
                                        "getColor", "setColor");
        color.setBound(true);
        color.setConstrained(true);
        color.setDisplayName(loader_.getText("PROP_NAME_COLOR"));
        color.setShortDescription(loader_.getText("PROP_DESC_COLOR"));

        PropertyDescriptor fixed = new PropertyDescriptor("fixed", beanClass,
                                        "isFixed", "setFixed");
        fixed.setBound(true);
        fixed.setConstrained(true);
        fixed.setDisplayName(loader_.getText("PROP_NAME_FIXED"));
        fixed.setShortDescription(loader_.getText("PROP_DESC_FIXED"));

        PropertyDescriptor italic = new PropertyDescriptor("italic", beanClass,
                                        "isItalic", "setItalic");
        italic.setBound(true);
        italic.setConstrained(true);
        italic.setDisplayName(loader_.getText("PROP_NAME_ITALIC"));
        italic.setShortDescription(loader_.getText("PROP_DESC_ITALIC"));

        PropertyDescriptor size = new PropertyDescriptor("size", beanClass,
                                        "getSize", "setSize");
        size.setBound(true);
        size.setConstrained(true);
        size.setDisplayName(loader_.getText("PROP_NAME_SIZE"));
        size.setShortDescription(loader_.getText("PROP_HTXT_DESC_SIZE"));

        PropertyDescriptor text = new PropertyDescriptor("text", beanClass,
                                        "getText", "setText");
        text.setBound(true);
        text.setConstrained(true);
        text.setDisplayName(loader_.getText("PROP_NAME_TEXT"));
        text.setShortDescription(loader_.getText("PROP_HTXT_DESC_TEXT"));

        PropertyDescriptor underscore = new PropertyDescriptor("underscore", beanClass,
                                        "isUnderscore", "setUnderscore");
        underscore.setBound(true);
        underscore.setConstrained(true);
        underscore.setDisplayName(loader_.getText("PROP_NAME_UNDERSCORE"));
        underscore.setShortDescription(loader_.getText("PROP_DESC_UNDERSCORE"));

        PropertyDescriptor lang = new PropertyDescriptor("lang", beanClass,                                  //$B3A
                                                         "getLanguage", "setLanguage");                      //$B3A
        lang.setBound(true);                                                                                 //$B3A
        lang.setConstrained(true);                                                                           //$B3A
        lang.setDisplayName(loader_.getText("PROP_NAME_LANGUAGE"));                                          //$B3A
        lang.setShortDescription(loader_.getText("PROP_DESC_LANGUAGE"));                                     //$B3A

        PropertyDescriptor dir = new PropertyDescriptor("dir", beanClass,                                    //$B3A
                                                         "getDirection", "setDirection");                    //$B3A
        dir.setBound(true);                                                                                  //$B3A
        dir.setConstrained(true);                                                                            //$B3A
        dir.setDisplayName(loader_.getText("PROP_NAME_DIRECTION"));                                          //$B3A
        dir.setShortDescription(loader_.getText("PROP_DESC_DIRECTION"));                                     //$B3A

        PropertyDescriptor useFO = new PropertyDescriptor("useFO", beanClass, "isUseFO", "setUseFO");           //@C1A
        useFO.setBound(true);                                                                                   //@C1A
        useFO.setConstrained(false);                                                                            //@C1A
        useFO.setDisplayName(loader_.getText("PROP_NAME_FORMATTING_OBJECT"));                                   //@C1A
        useFO.setShortDescription(loader_.getText("PROP_DESC_FORMATTING_OBJECT"));                              //@C1A


        properties_ = new PropertyDescriptor[] { alignment, bold, color, fixed, italic,                      //$B3C
                                                 size, text, underscore, lang, dir, useFO };                        //$B3C      //@C1C
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLText is a subclass of HTMLTagAttributes, this method
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
                image = loadImage ("HTMLText16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLText32.gif");
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
