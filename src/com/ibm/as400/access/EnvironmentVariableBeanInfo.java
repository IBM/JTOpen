///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  EnvironmentVariableBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 The EnvironmentVariableBeanInfo class represents the bean information for the EnvironmentVariable class.
 **/
public class EnvironmentVariableBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = EnvironmentVariable.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            // Set up the event descriptors.
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            eventSetDescriptors = new EventSetDescriptor[] { propertyChange };

            // Set up the property descriptors.
            PropertyDescriptor name = new PropertyDescriptor("name", BEAN_CLASS);
            name.setBound(true);
            name.setConstrained(false);
            name.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_NAME"));
            name.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_NAME"));

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(false);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            propertyDescriptors = new PropertyDescriptor[] { name, system };
        }
        catch (IntrospectionException e)
        {
            Trace.log(Trace.ERROR, "Unexpected IntrospectionException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Returns the bean descriptor.
     @return  The bean descriptor.
     **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(BEAN_CLASS);
    }

    /**
     Returns the index of the default event.
     @return  Zero (0), the index to the default event.
     **/
    public int getDefaultEventIndex()
    {
        // The index for the "propertyChange" event.
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "name" property.
        return 0;
    }

    /**
     Returns the descriptors for all events.
     @return  The descriptors for all events.
     **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return eventSetDescriptors;
    }

    /**
     Returns the descriptors for all properties.
     @return  The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }

    /**
     Returns an Image for this bean's icon.
     @param  iconKind  The desired icon size and color.  Possible values are:
     <ul>
     <li>BeanInfo.ICON_MONO_16x16
     <li>BeanInfo.ICON_MONO_32x32
     <li>BeanInfo.ICON_COLOR_16x16
     <li>BeanInfo.ICON_COLOR_32x32
     </ul>
     @return  The Image for the icon.
     **/
    public Image getIcon(int iconKind)
    {
        switch (iconKind)
        {
            case ICON_MONO_16x16:
            case ICON_COLOR_16x16:
                return loadImage("EnvironmentVariable16.gif");
            case ICON_MONO_32x32:
            case ICON_COLOR_32x32:
                return loadImage("EnvironmentVariable32.gif");
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'iconKind' is not valid:", iconKind);
                throw new ExtendedIllegalArgumentException("iconKind (" + iconKind + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }
}
