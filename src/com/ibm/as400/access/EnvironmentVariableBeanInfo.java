///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: EnvironmentVariableBeanInfo.java
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
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ResourceBundle;



/**
The EnvironmentVariableBeanInfo class represents the bean information
for the EnvironmentVariable class.
**/
public class EnvironmentVariableBeanInfo 
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final Class                  beanClass_              = EnvironmentVariable.class;

    private static BeanDescriptor               beanDescriptor_;
    private static EventSetDescriptor[]         eventSetDescriptors_;
    private static Image                        icon16_;
    private static Image                        icon32_;
    private static PropertyDescriptor[]         propertyDescriptors_;
    private static ResourceBundle               resourceBundle_;



/**
Static initializer.
**/
    static
    {
        try
        {
            // Set up the bean descriptor.
            beanDescriptor_ = new BeanDescriptor(beanClass_);

            // Set up the event descriptors.            
            EventSetDescriptor propertyChange = new EventSetDescriptor(beanClass_,
                                                                       "propertyChange",
                                                                       PropertyChangeListener.class,
                                                                       "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            eventSetDescriptors_ = new EventSetDescriptor[] { propertyChange };

            // Set up the property descriptors.
            PropertyDescriptor name = new PropertyDescriptor("name", beanClass_);
            name.setBound(true);
            name.setConstrained(true);
            name.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_NAME"));
            name.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_NAME"));

            PropertyDescriptor system = new PropertyDescriptor("system", beanClass_);
            system.setBound(true);
            system.setConstrained(false);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            propertyDescriptors_ = new PropertyDescriptor[] { name, system };

        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error while loading bean info", e);
            throw new Error(e.toString());
        }
    }
    


/**
Returns the bean descriptor.

@return The bean descriptor.
**/
    public BeanDescriptor getBeanDescriptor()
    {
        return beanDescriptor_;
    }


/**
Returns the default event index.

@return The default event index.
**/
    public int getDefaultEventIndex()
    {
        return 0;
    }


    
/**
Returns the default property index.

@return The default property index.
**/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }



/**
Returns the event set descriptors.

@return The event set descriptors.
**/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
       return eventSetDescriptors_;
    }



/**
Returns the property descriptors.

@return The property descriptors.
**/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
       return propertyDescriptors_;
    }



/**
Returns the icon.

@param iconKind The icon kind.  Possible values are:
                <ul>
                <li>BeanInfo.ICON_MONO_16x16
                <li>BeanInfo.ICON_MONO_32x32
                <li>BeanInfo.ICON_COLOR_16x16
                <li>BeanInfo.ICON_COLOR_32x32
                </ul>
@return         The icon.                
**/
    public Image getIcon(int icon)
    {
        switch(icon)
        {
            case ICON_MONO_16x16:
            case ICON_COLOR_16x16:
                if (icon16_ == null)
                    icon16_ = loadImage("EnvironmentVariable16.gif");
                return icon16_;

            case ICON_MONO_32x32:
            case ICON_COLOR_32x32:
                if (icon32_ == null)
                    icon32_ = loadImage("EnvironmentVariable32.gif");
                return icon32_;

            default:
                throw new ExtendedIllegalArgumentException("icon", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

}

