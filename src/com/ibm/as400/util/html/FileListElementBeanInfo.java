///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileListElementBeanInfo.java
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
The FileListElementBeanInfo class provides
bean information for the FileListElement class.
**/
public class FileListElementBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = FileListElement.class;

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

        EventSetDescriptor[] events = { changed };

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor renderer = new PropertyDescriptor("renderer", beanClass,           // @A2C
                                                            "getRenderer", "setRenderer");    // @A2C
        renderer.setBound(true);                                                              // @A2C 
        renderer.setConstrained(false);                                                       // @A2C
        renderer.setDisplayName(loader_.getText("PROP_NAME_RENDERER"));                       // @A2C
        renderer.setShortDescription(loader_.getText("PROP_DESC_RENDERER"));                  // @A2C

        PropertyDescriptor request = new PropertyDescriptor("httpServletRequest", beanClass,
                                                            "getHttpServletRequest", "setHttpServletRequest");
        request.setBound(true);
        request.setConstrained(false);
        request.setDisplayName(loader_.getText("PROP_NAME_REQUEST"));
        request.setShortDescription(loader_.getText("PROP_DESC_REQUEST"));

        PropertyDescriptor system = new PropertyDescriptor("system", beanClass,
                                                           "getSystem", "setSystem");
        system.setBound(true);
        system.setConstrained(false);
        system.setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
        system.setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));

        PropertyDescriptor table = new PropertyDescriptor("table", beanClass,            //$A1A
                                                          "getTable", "setTable");       //$A1A
        table.setBound(true);                                                            //$A1A
        table.setConstrained(false);                                                     //$A1A
        table.setDisplayName(loader_.getText("PROP_NAME_TABLE"));                        //$A1A
        table.setShortDescription(loader_.getText("PROP_DESC_TABLE"));                   //$A1A

        properties_ = new PropertyDescriptor[] { renderer, request, system, table };      //$A1C  // @A2C
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
                image = loadImage ("FileListElement16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("FileListElement32.gif");
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

