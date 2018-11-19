///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterBeanInfo.java
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
BeanInfo for Printer class.
**/
public class PrinterBeanInfo extends PrintObjectBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Additional properties defined for Printer
    private static PropertyDescriptor[] prtDProperties_;

    // Class this bean info represents.
    private final static Class beanClass = Printer.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader rbl_;

    static
    {
        try
        {
            PropertyDescriptor name =
              new PropertyDescriptor("name", beanClass);
            name.setBound(true);
            name.setConstrained(true);
            name.setDisplayName(rbl_.getText("PROP_NAME_PRTD_NAME"));
            name.setShortDescription(rbl_.getText("PROP_DESC_PRTD_NAME"));

            PropertyDescriptor[] properties = {name};
            prtDProperties_ = properties;
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

    // Printer does not define any additional events so we
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

        combinedSize = printObjectProperties.length + prtDProperties_.length;
        combinedProperties = new PropertyDescriptor[combinedSize];

        // copy PrintObject properties
        System.arraycopy( printObjectProperties,          // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          0,                              // --offset
                          printObjectProperties.length ); // length

        // copy Printer properties
        System.arraycopy( prtDProperties_,                // source
                          0,                              // --offset
                          combinedProperties,             // destination
                          printObjectProperties.length,   // --offset
                          prtDProperties_.length );       // length

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
                image = loadImage("Printer16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("Printer32.gif");
                break;
        }

        return image;
    }

}

