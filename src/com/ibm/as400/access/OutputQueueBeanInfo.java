///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueBeanInfo.java
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
import java.beans.BeanInfo;
import java.awt.Image;

/**
BeanInfo for OutputQueue class.
**/
public class OutputQueueBeanInfo extends PrintObjectBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Additional events defined for OutputQueue
    private static EventSetDescriptor[] outQEvents_;

    // Additional properties defined for OutputQueue
    private static PropertyDescriptor[] outQProperties_;

    // Class this bean info represents.
    private final static Class beanClass = OutputQueue.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader rbl_;

    static
    {
        try
        {
            // "name" is not a property, hide it from introspection.
            PropertyDescriptor name =
              new PropertyDescriptor( "name", beanClass, "getName", null );
            name.setHidden(true);

            PropertyDescriptor path =
              new PropertyDescriptor("path", beanClass);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(rbl_.getText("PROP_NAME_PATH"));
            path.setShortDescription(rbl_.getText("PROP_DESC_PATH"));

            PropertyDescriptor[] properties = {name, path};
            outQProperties_ = properties;

            String[] listenerMethods = { "outputQueueCleared",
                                         "outputQueueHeld",
                                         "outputQueueReleased" };
            EventSetDescriptor outQ =
              new EventSetDescriptor ( beanClass,
                                       "outputQueue",
                                       com.ibm.as400.access.OutputQueueListener.class,
                                       listenerMethods,
                                       "addOutputQueueListener",
                                       "removeOutputQueueListener" );
            outQ.setDisplayName(rbl_.getText("EVT_NAME_OUTQ_EVENT"));
            outQ.setShortDescription(rbl_.getText("EVT_DESC_OUTQ_EVENT"));

            EventSetDescriptor[] events = {outQ};
            outQEvents_ = events;
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
        // the index for the OutputQueue event
        return super.getDefaultEventIndex() + 1;
    }

    // We want "system" as the default property. PrintObject
    // sets this so we don't have to override:
    // public int getDefaultPropertyIndex()

    /**
    Returns the descriptors for all events.
    @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        // Per the JavaBean spec, events are a mechanism for propagating
        // state change notifications between a source object and one or
        // more target listener objects.

        EventSetDescriptor[] printObjectEvents;
        EventSetDescriptor[] combinedEvents;
        int combinedSize;

        // Get the events defined in PrintObjectBeanInfo
        printObjectEvents = super.getEventSetDescriptors();

        combinedSize = printObjectEvents.length + outQEvents_.length;
        combinedEvents = new EventSetDescriptor[combinedSize];

        // copy PrintObject events
        System.arraycopy( printObjectEvents,          // source
                          0,                          // --offset
                          combinedEvents,             // destination
                          0,                          // --offset
                          printObjectEvents.length ); // length

        // copy OutputQueue events
        System.arraycopy( outQEvents_,                // source
                          0,                          // --offset
                          combinedEvents,             // destination
                          printObjectEvents.length,   // --offset
                          outQEvents_.length );       // length

        return combinedEvents;
    }

    /**
    Returns the descriptors for all properties.
    @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        // Per the JavaBean spec, properties are discrete, named attributes
        // of a Java Bean that can affect its appearance or its behavior.

        PropertyDescriptor[] printObjectProperties;
        PropertyDescriptor[] combinedProperties;
        int combinedSize;

        // Get the properties defined in PrintObjectBeanInfo
        printObjectProperties = super.getPropertyDescriptors();

        combinedSize = printObjectProperties.length + outQProperties_.length;
        combinedProperties = new PropertyDescriptor[combinedSize];

        // copy PrintObject properties
        System.arraycopy( printObjectProperties,          // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          0,                              // --offset
                          printObjectProperties.length ); // length

        // copy OutputQueue properties
        System.arraycopy( outQProperties_,                // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          printObjectProperties.length,   // --offset
                          outQProperties_.length );       // length

        return combinedProperties;
    }

    /**
      * Returns an Image for this bean's icon.
      * @param icon The desired icon size and color.
      * @return The Image for the icon.
      **/
    public Image getIcon(int icon)
    {
        Image image = null;

        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage("OutputQueue16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("OutputQueue32.gif");
                break;
        }

        return image;
    }

}

