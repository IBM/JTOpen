///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemValueGroupBeanInfo.java
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
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
 * The SystemValueGroupBeanInfo class provides bean information for the 
 * SystemValueGroup class. 
**/
public class SystemValueGroupBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     * The SystemValueGroup raw class.
     */
    private final static Class beanClass_ = SystemValueGroup.class;

    // Handles loading the appropriate resource bundle
//@A2D    private static ResourceBundleLoader loader_;

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
        changed.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE")); //@A2C
        changed.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE")); //@A2C

        // Vetoable change events
        EventSetDescriptor veto = new EventSetDescriptor(beanClass_,
                           "vetoableChange",
                           java.beans.VetoableChangeListener.class,
                           "vetoableChange");
        veto.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO")); //@A2C
        veto.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO")); //@A2C

        EventSetDescriptor[] events = {changed, veto};
        events_ = events;


        // PROPERTIES

        // System property
        PropertyDescriptor system = new PropertyDescriptor("system", beanClass_);
        system.setBound(true);
        system.setConstrained(true);
        system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM")); //@A2C
        system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM")); //@A2C

        PropertyDescriptor name = new PropertyDescriptor("groupName", beanClass_);
        system.setBound(true);
        system.setConstrained(true);
        system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_NAME")); //@A2C
        system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_NAME")); //@A2C

        PropertyDescriptor description = new PropertyDescriptor("groupDescription", beanClass_);
        system.setBound(true);
        system.setConstrained(true);
        system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_DESCRIPTION")); //@A2C
        system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_DESCRIPTION")); //@A2C

        properties_ = new PropertyDescriptor[] {system, name, description};
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
      return new BeanDescriptor(beanClass_);
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
     * @param  icon The desired icon size and color. 
     * @return The Image for the icon.
     */
    public Image getIcon(int icon) 
    {
      Image image = null;
      switch(icon)
      {
        case BeanInfo.ICON_MONO_16x16:
        case BeanInfo.ICON_COLOR_16x16:
          image = loadImage("SystemValueGroup16.gif");
          break;
        case BeanInfo.ICON_MONO_32x32:
        case BeanInfo.ICON_COLOR_32x32:
          image = loadImage("SystemValueGroup32.gif");
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
