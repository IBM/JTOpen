///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AFPResourceBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


import java.beans.PropertyDescriptor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.awt.Image;

/**
The AFPResourceBeanInfo class provides bean information for the AFPResource class.
**/
public class AFPResourceBeanInfo extends PrintObjectBeanInfo
{
    static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Additional properties defined for AFPResources
    private static PropertyDescriptor[] AFPRProperties_;

    // Class this bean info represents.
    private final static Class beanClass = AFPResource.class;

    // Handles loading the appropriate resource bundle
    // private static ResourceBundleLoader rbl_;

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
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor[] properties = {name, path};
            AFPRProperties_ = properties;
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

    // AFPResource does not define any additional events so we
    // let the PrintObject provide:
    // public int getDefaultEventIndex()
    // public EventSetDescriptor[] getEventSetDescriptors()

    // We want "system" as the default property. PrintObject
    // sets this so we don't have to override:
    // public int getDefaultPropertyIndex()

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

        combinedSize = printObjectProperties.length + AFPRProperties_.length;
        combinedProperties = new PropertyDescriptor[combinedSize];

        // copy PrintObject properties
        System.arraycopy( printObjectProperties,          // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          0,                              // --offset
                          printObjectProperties.length ); // length

        // copy AFPResource properties
        System.arraycopy( AFPRProperties_,                // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          printObjectProperties.length,   // --offset
                          AFPRProperties_.length );       // length

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
                image = loadImage("AFPResource16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("AFPResource32.gif");
                break;
        }

        return image;
    }

}
