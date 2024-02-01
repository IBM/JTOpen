///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLDocumentBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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
*  The HTMLDocumentBeanInfo class provides bean information for the HTMLDocument class.
**/
public class HTMLDocumentBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = HTMLDocument.class;

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
        PropertyDescriptor height = new PropertyDescriptor("height", beanClass,
                                        "getPageHeight", "setPageHeight");
        height.setBound(true);
        height.setConstrained(false);
        height.setDisplayName(loader_.getText("PROP_NAME_HD_HEIGHT"));
        height.setShortDescription(loader_.getText("PROP_HD_DESC_HEIGHT"));

        PropertyDescriptor width = new PropertyDescriptor("width", beanClass,
                                        "getPageWidth", "setPageWidth");
        width.setBound(true);
        width.setConstrained(false);
        width.setDisplayName(loader_.getText("PROP_NAME_HD_WIDTH"));
        width.setShortDescription(loader_.getText("PROP_HD_DESC_WIDTH"));

        PropertyDescriptor marginTop = new PropertyDescriptor("marginTop", beanClass,
                                        "getMarginTop", "setMarginTop");
        marginTop.setBound(true);
        marginTop.setConstrained(false);
        marginTop.setDisplayName(loader_.getText("PROP_NAME_MARGIN_TOP"));
        marginTop.setShortDescription(loader_.getText("PROP_DESC_MARGIN_TOP"));

        PropertyDescriptor marginBottom = new PropertyDescriptor("marginBottom", beanClass,
                                        "getMarginBottom", "setMarginBottom");
        marginBottom.setBound(true);
        marginBottom.setConstrained(false);
        marginBottom.setDisplayName(loader_.getText("PROP_NAME_MARGIN_BOTTOM"));
        marginBottom.setShortDescription(loader_.getText("PROP_DESC_MARGIN_BOTTOM"));
        
        PropertyDescriptor marginLeft = new PropertyDescriptor("marginLeft", beanClass,
                                    "getMarginLeft", "setMarginLeft");
        marginLeft.setBound(true);
        marginLeft.setConstrained(false);
        marginLeft.setDisplayName(loader_.getText("PROP_NAME_MARGIN_LEFT"));
        marginLeft.setShortDescription(loader_.getText("PROP_DESC_MARGIN_LEFT"));

        PropertyDescriptor marginRight = new PropertyDescriptor("marginRight", beanClass,
                                        "getMarginRight", "setMarginRight");
        marginRight.setBound(true);
        marginRight.setConstrained(false);
        marginRight.setDisplayName(loader_.getText("PROP_NAME_MARGIN_RIGHT"));
        marginRight.setShortDescription(loader_.getText("PROP_DESC_MARGIN_RIGHT"));

        PropertyDescriptor useFO = new PropertyDescriptor("useFO", beanClass, "isUseFO", "setUseFO");           
        useFO.setBound(true);                                                                                   
        useFO.setConstrained(false);                                                                            
        useFO.setDisplayName(loader_.getText("PROP_NAME_FORMATTING_OBJECT"));                                   
        useFO.setShortDescription(loader_.getText("PROP_DESC_FORMATTING_OBJECT"));                              

        properties_ = new PropertyDescriptor[] { height, width, marginTop, marginBottom, marginLeft, marginRight, useFO};      
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLDocument is a subclass of HTMLTagAttributes, this method
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
                image = loadImage ("HTMLDocument16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLDocument32.gif");
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
