///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectBeanInfo.java
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
Base class for the various BeanInfo classes
describing print objects.
**/
public class PrintObjectBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Events defined for PrintObject
    private static EventSetDescriptor[] events_;

    // Properties defined for PrintObject
    private static PropertyDescriptor[] properties_;

    // Class this bean info represents.
    private final static Class beanClass = PrintObject.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader rbl_;

    static
    {
        try
        {
            PropertyDescriptor system =
              new PropertyDescriptor("system", beanClass);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(rbl_.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(rbl_.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor properties[] = {system};
            properties_ = properties;

            EventSetDescriptor changed =
              new EventSetDescriptor( beanClass,
                                      "propertyChange",
                                      java.beans.PropertyChangeListener.class,
                                      "propertyChange" );
            changed.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor veto =
              new EventSetDescriptor( beanClass,
                                      "propertyChange",
                                      java.beans.VetoableChangeListener.class,
                                      "vetoableChange" );
            veto.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_VETO"));

            EventSetDescriptor[] events = {changed, veto};
            events_ = events;
        }

        catch (IntrospectionException e)
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
        // the index for the PropertyChange event
        return 0;
    }

    /**
    Returns the index of the default property.
    @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        // the index for the "system" property
        return 0;
    }

    /**
    Returns the descriptors for all events.
    @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        // Per the JavaBean spec, events are a mechanism for propagating
        // state change notifications between a source object and one or
        // more target listener objects.

        return events_;
    }

    /**
    Returns the descriptors for all properties.
    @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        // Per the JavaBean spec, properties are discrete, named attributes
        // of a Java Bean that can affect its appearance or its behavior.

        return properties_;
    }

}

