///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLFormBeanInfo.java
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
The HTMLFormBeanInfo class provides
bean information for the HTMLForm class.
**/
public class HTMLFormBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = HTMLForm.class;

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
        PropertyDescriptor hidden = new PropertyDescriptor("hiddenParameterList", beanClass,
                                        "getHiddenParameterList", "setHiddenParameterList");
        hidden.setBound(true);
        hidden.setConstrained(true);
        hidden.setDisplayName(loader_.getText("PROP_NAME_HIDDENPARAMETERLIST"));
        hidden.setShortDescription(loader_.getText("PROP_DESC_HIDDENPARAMETERLIST"));

        PropertyDescriptor method = new PropertyDescriptor("method", beanClass, "getMethod", "setMethod");
        method.setBound(true);
        method.setConstrained(true);
        method.setDisplayName(loader_.getText("PROP_NAME_METHOD"));
        method.setShortDescription(loader_.getText("PROP_DESC_METHOD"));

        PropertyDescriptor target = new PropertyDescriptor("target", beanClass, "getTarget", "setTarget");
        target.setBound(true);
        target.setConstrained(true);
        target.setDisplayName(loader_.getText("PROP_NAME_TARGET"));
        target.setShortDescription(loader_.getText("PROP_HF_DESC_TARGET"));

        PropertyDescriptor url = new PropertyDescriptor("url", beanClass, "getURL", "setURL");
        url.setBound(true);
        url.setConstrained(true);
        url.setDisplayName(loader_.getText("PROP_NAME_URL"));
        url.setShortDescription(loader_.getText("PROP_DESC_URL"));

        PropertyDescriptor lang = new PropertyDescriptor("lang", beanClass, "getLanguage", "setLanguage");   //$B3A
        lang.setBound(true);                                                                                 //$B3A
        lang.setConstrained(true);                                                                           //$B3A
        lang.setDisplayName(loader_.getText("PROP_NAME_LANGUAGE"));                                          //$B3A
        lang.setShortDescription(loader_.getText("PROP_DESC_LANGUAGE"));                                     //$B3A

        PropertyDescriptor dir = new PropertyDescriptor("dir", beanClass, "getDirection", "setDirection");   //$B3A
        dir.setBound(true);                                                                                  //$B3A
        dir.setConstrained(true);                                                                            //$B3A
        dir.setDisplayName(loader_.getText("PROP_NAME_DIRECTION"));                                          //$B3A
        dir.setShortDescription(loader_.getText("PROP_DESC_DIRECTION"));                                     //$B3A
        
        properties_ = new PropertyDescriptor[] { hidden, method, target, url, lang, dir };                   //$B3C
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }

    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLForm is a subclass of HTMLTagAttributes, this method
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
                image = loadImage ("HTMLForm16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLForm32.gif");
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
