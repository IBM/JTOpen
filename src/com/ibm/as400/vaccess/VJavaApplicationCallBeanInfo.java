///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VJavaApplicationCallBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.beans.PropertyChangeListener;

/**
 * The VJavaApplicationCallBeanInfo class provides bean information for the
 * VJavaApplicationCall class.
**/
public class VJavaApplicationCallBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class beanClass_ = VJavaApplicationCall.class;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    // Static initializer.
    static
    {
        try
        {
            // Events.
            EventSetDescriptor propertyChange = new EventSetDescriptor (
                        beanClass_,
                        "propertyChange",
                        PropertyChangeListener.class,
                        "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            events_ = new EventSetDescriptor[] { propertyChange };

            // Properties.
            PropertyDescriptor javaAppCall =
                new PropertyDescriptor("javaApplicationCallObject", beanClass_,
                                       "getJavaApplicationCall",
                                       "setJavaApplicationCall");
            javaAppCall.setBound(true);
            javaAppCall.setConstrained(true);
            javaAppCall.setDisplayName(ResourceLoader.getText("PROP_NAME_JAC"));
            javaAppCall.setShortDescription(ResourceLoader.getText("PROP_DESC_JAC"));

            properties_ =  new PropertyDescriptor[]{
                                           javaAppCall
                                           };
        }
        catch(IntrospectionException e)
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
        return Copyright_v.copyright;
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

        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage("VJavaApplicationCall16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("VJavaApplicationCall32.gif");
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

