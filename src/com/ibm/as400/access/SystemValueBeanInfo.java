///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SystemValueBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;


/**
 * The SystemValueBeanInfo class provides bean information for the 
 * SystemValue class.
**/
public class SystemValueBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     * The SystemValue raw class.
    **/
    private final static Class beanClass_ = SystemValue.class;
    
    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    static
    {
      try
      {
        // EVENTS

        // Property change events
        EventSetDescriptor changed = new EventSetDescriptor(beanClass_,
                           "propertyChange",
                           java.beans.PropertyChangeListener.class,
                           "propertyChange");
        changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
        changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

        // Vetoable change events
        EventSetDescriptor veto = new EventSetDescriptor(beanClass_,
                           "vetoableChange",
                           java.beans.VetoableChangeListener.class,
                           "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        // SystemValue events
        String[] listeners_ = {"systemValueChanged"};
        EventSetDescriptor sv = new EventSetDescriptor(beanClass_,
                           "systemValueEvent",
                           com.ibm.as400.access.SystemValueListener.class,
                           listeners_,
                           "addSystemValueListener",
                           "removeSystemValueListener");
        sv.setDisplayName(loader_.getText("EVT_NAME_SV_EVENT"));
        sv.setShortDescription(loader_.getText("EVT_DESC_SV_EVENT"));

        EventSetDescriptor[] events = {sv, changed, veto};
        events_ = events;


        // PROPERTIES

        // Name property
        PropertyDescriptor name = new PropertyDescriptor("name", beanClass_);
        name.setBound(false);
        name.setConstrained(false);
        name.setDisplayName(loader_.getText("PROP_NAME_NAME"));
        name.setShortDescription(loader_.getText("PROP_DESC_NAME"));

        // System property
        PropertyDescriptor system = new PropertyDescriptor("system", beanClass_);
        system.setBound(true);
        system.setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
        system.setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));

        properties_ = new PropertyDescriptor[] {name, system};
      }
      catch(Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
     * Returns the bean descriptor.
     * 
     * @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor ()
    {
        return new BeanDescriptor (beanClass_);
    }


    /**
     * Copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }


    /**
     * Returns the default event index.
     * @return The default event index (always 1).
     **/
    public int getDefaultEventIndex() 
    {
        return 1; 
    }


    /**
     * Returns the default property index.
     * @return The default property index (always 0).
     **/
    public int getDefaultPropertyIndex() 
    {
        return 0; 
    }


    /**
     * Returns the descriptors for all events. 
     * @return The descriptors for all events.
     **/ 
    public EventSetDescriptor[] getEventSetDescriptors() 
    {
      return events_;
    }


    /**
     * Returns an Image for this bean's icon.
     * @param icon The desired icon size and color. 
     * @return The Image for the icon.
     */
    public Image getIcon(int icon) 
    {
      Image image = null;
      switch(icon)
      {
        case BeanInfo.ICON_MONO_16x16:
        case BeanInfo.ICON_COLOR_16x16:
          image = loadImage("SystemValue16.gif");
          break;
        case BeanInfo.ICON_MONO_32x32:
        case BeanInfo.ICON_COLOR_32x32:
          image = loadImage("SystemValue32.gif");
          break;
      }
      return image;
    }


    /** 
     * Returns the descriptors for all properties.
     * @return The descriptors for all properties. 
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
      return properties_;
    }

}
