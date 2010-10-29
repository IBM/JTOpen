///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataAreaBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;


/**
The DataAreaBeanInfo class provides
bean information for the DataArea class.
**/
public class DataAreaBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = DataArea.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

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
                         "propertyChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        String[] daListeners = {"cleared",
                                "created",
                                "deleted",
                                "read",
                                "written"};
        EventSetDescriptor da = new EventSetDescriptor(beanClass,
                         "dataArea",
                         com.ibm.as400.access.DataAreaListener.class,
                         daListeners,
                         "addDataAreaListener",
                         "removeDataAreaListener");
        da.setDisplayName(loader_.getText("EVT_NAME_DA_EVENT"));
        da.setShortDescription(loader_.getText("EVT_DESC_DA_EVENT"));

        EventSetDescriptor[] events = {da, changed, veto};

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor name = new PropertyDescriptor("name", beanClass,
                                        "getName", null);
        name.setBound(false);
        name.setConstrained(false);
        name.setDisplayName(loader_.getText("PROP_NAME_NAME"));
        name.setShortDescription(loader_.getText("PROP_DESC_NAME"));

        PropertyDescriptor system = new PropertyDescriptor("system", beanClass);
        system.setBound(true);
        system.setConstrained(true);
        system.setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
        system.setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));

        properties_ = new PropertyDescriptor[] {name, system};
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
    Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}

