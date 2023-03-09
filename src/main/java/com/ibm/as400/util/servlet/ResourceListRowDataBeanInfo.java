///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListRowDataBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;

/**
*  The ResourceListRowDataBeanInfo class provides bean information 
*  for the ResourceListRowData class.
**/
public class ResourceListRowDataBeanInfo extends SimpleBeanInfo
{   
    // Class this bean info represents.
    private final static Class beanClass = ResourceListRowData.class;

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

        EventSetDescriptor[] events = { changed };

        events_ = events;

        PropertyDescriptor currentPosition = new PropertyDescriptor("currentPosition", beanClass,                                                                     "getCurrentPosition", null);
        currentPosition.setBound(false);
        currentPosition.setConstrained(false);
        currentPosition.setDisplayName(loader_.getText("PROP_NAME_CURRENTPOSITION"));
        currentPosition.setShortDescription(loader_.getText("PROP_DESC_RL_CURRENTPOSITION"));

        PropertyDescriptor length = new PropertyDescriptor("length", beanClass, "length", null);
        length.setBound(false);
        length.setConstrained(false);
        length.setDisplayName(loader_.getText("PROP_NAME_LENGTH"));
        length.setShortDescription(loader_.getText("PROP_DESC_RL_LENGTH"));

        PropertyDescriptor resourceList = new PropertyDescriptor("resourceList", beanClass,
                                                             "getResourceList", "setResourceList");
        resourceList.setBound(true);
        resourceList.setConstrained(false);
        resourceList.setDisplayName(loader_.getText("PROP_NAME_RESOURCELIST"));
        resourceList.setShortDescription(loader_.getText("PROP_DESC_RESOURCELIST"));

        PropertyDescriptor columnAttribute = new PropertyDescriptor("columnAttributeIDs", beanClass,
                                                             "getColumnAttributeIDs", "setColumnAttributeIDs");
        columnAttribute.setBound(true);
        columnAttribute.setConstrained(false);
        columnAttribute.setDisplayName(loader_.getText("PROP_NAME_RL_COLUMNATTRIBUTE"));
        columnAttribute.setShortDescription(loader_.getText("PROP_DESC_RL_COLUMNATTRIBUTE"));

        properties_ = new PropertyDescriptor[] { currentPosition, length, resourceList, columnAttribute };
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
     *  Returns the bean descriptor.
     *  @return The bean descriptor.
     **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }


    /** 
     *  Returns the index of the default event.
     *  @return The index to the default event.
     **/
    public int getDefaultEventIndex()
    {
       return 0;
    }

    /**
     *  Returns the index of the default property.
     *  @return The index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
       return 0;
    }

    /**
     *  Returns the descriptors for all events.
     *  @return The descriptors for all events.
     **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }

    /**
     *  Returns an image for the icon.
     *  
     *  @param icon    The icon size and color.
     *  @return        The image.
     **/
    public Image getIcon (int icon)
    {
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("ResourceListRowData16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("ResourceListRowData32.gif");
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
