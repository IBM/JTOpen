///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServletHyperlinkBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;


import com.ibm.as400.util.html.HTMLHyperlink;
import com.ibm.as400.util.html.HTMLHyperlinkBeanInfo;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;

/**
The ServletHyperlinkBeanInfo class provides
bean information for the ServletHyperlink class.
**/
public class ServletHyperlinkBeanInfo extends SimpleBeanInfo 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = ServletHyperlink.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_s loader_;

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
        PropertyDescriptor path = new PropertyDescriptor("response", beanClass, 
                                                         "getHttpServletResponse", "setHttpServletResponse");
        path.setBound(true);
        path.setConstrained(false);
        path.setDisplayName(loader_.getText("PROP_NAME_RESPONSE"));
        path.setShortDescription(loader_.getText("PROP_DESC_RESPONSE"));

        PropertyDescriptor response = new PropertyDescriptor("path", beanClass, "getPathInfo", "setPathInfo");
        response.setBound(true);
        response.setConstrained(false);
        response.setDisplayName(loader_.getText("PROP_NAME_PATH"));
        response.setShortDescription(loader_.getText("PROP_DESC_PATH"));
        
        properties_ = new PropertyDescriptor[] { path, response };
      }
      catch (Exception e)
      { 
        throw new Error(e.toString());
      }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * ButtonFormInput is a subclass of FormInput, this method
     * will return a FormInputBeanInfo object.
     *
     * @return BeanInfo[] containing this bean's superclass BeanInfo
     **/
    public BeanInfo[] getAdditionalBeanInfo()
    {
     return new BeanInfo[] { new HTMLHyperlinkBeanInfo() };
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
        // the index for the propertyChange event
        return 0;
    }

    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        // the index for the "path" property
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
                image = loadImage ("ServletHyperlink16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("ServletHyperlink32.gif");
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


