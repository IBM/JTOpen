///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemPoolBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.IndexedPropertyDescriptor;
import java.lang.Class;
import java.lang.reflect.Method;

/**
 * The VSystemPoolBeanInfo class provides bean 
 * information for the VSystemPool class.
**/
public class VSystemPoolBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class beanClass_ = VSystemPool.class;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    // Static initializer.
    static
    {
        try 
        {
            // Events.
            String[] propertyChangeMethods = {"propertyChange"};
            EventSetDescriptor propertyChange = new EventSetDescriptor(
                        beanClass_,
                        "propertyChange",
                        java.beans.PropertyChangeListener.class,
                        propertyChangeMethods,
                        "addPropertyChangeListener",
                        "removePropertyChangeListener");

            propertyChange.setDisplayName(ResourceLoader.getText
                                                ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText
                                                ("EVT_DESC_PROPERTY_CHANGE"));

            events_ = new EventSetDescriptor[] { propertyChange };

            // Properties.
            PropertyDescriptor system = new PropertyDescriptor (
                                                "system",
                                                beanClass_,
                                                "getSystem",
                                                null);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText(
                                                          "PROP_DESC_SYSTEM"));

            properties_ = new PropertyDescriptor[] { system };
        }
        catch (IntrospectionException e) 
        {
            throw new Error(e.toString());
        } 
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
     * @return The default event index.
     **/
    public int getDefaultEventIndex() 
    {
        return 1; 
    }

    /** 
     * Returns the default property index.
     * @return The default property index.
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
     * Returns an image for this bean's icon.     
     * @param icon The desired icon size.
     * @return The image for the icon.
     */
    public Image getIcon(int icon) 
    {
        if(icon== BeanInfo.ICON_MONO_16x16) 
        {
            java.awt.Image img = loadImage("VSystemPool16.gif");
            return img;
        }
        else if(icon== BeanInfo.ICON_COLOR_16x16) 
        {
            java.awt.Image img = loadImage("VSystemPool16.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_MONO_32x32) 
        {
            java.awt.Image img = loadImage("VSystemPool32.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_COLOR_32x32) 
        {
            java.awt.Image img = loadImage("VSystemPool32.gif");
            return img;
        }        
        return null;
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

