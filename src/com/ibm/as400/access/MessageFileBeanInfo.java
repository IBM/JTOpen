///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  MessageFileBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
 Provides bean information for the MessageFile class.
 **/
public class MessageFileBeanInfo extends SimpleBeanInfo
{
    // Class this bean info represents.
    private static final Class BEAN_CLASS = MessageFile.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            // Set up the event set descriptors.
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { propertyChange, vetoableChange };

            // Set up the property descriptors.
            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor path = new PropertyDescriptor("path", BEAN_CLASS);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor helpTextFormatting = new PropertyDescriptor("helpTextFormatting", BEAN_CLASS);
            helpTextFormatting.setBound(true);
            helpTextFormatting.setConstrained(true);
            helpTextFormatting.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_MF_HELP_TEXT_FORMATTING"));
            helpTextFormatting.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_MF_HELP_TEXT_FORMATTING"));

            propertyDescriptors = new PropertyDescriptor[] { system, path, helpTextFormatting };
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
     @return  One (1), the index to the default event.
     **/
    public int getDefaultEventIndex()
    {
        // The index for the "vetoableChange" event.
        return 1;
    }

    /**
     Returns the index of the default property.
     @return One (1), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "path" property.
        return 1;
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
     Returns an Image for this bean's icon.
     @param  icon  The desired icon size and color.  Possible values are:
     <ul>
     <li>BeanInfo.ICON_MONO_16x16
     <li>BeanInfo.ICON_MONO_32x32
     <li>BeanInfo.ICON_COLOR_16x16
     <li>BeanInfo.ICON_COLOR_32x32
     </ul>
     @return  The Image for the icon.
     **/
    public Image getIcon(int icon)
    {
        switch (icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("MessageFile16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("MessageFile32.gif");
        }
        Trace.log(Trace.ERROR, "Value of parameter 'icon' is not valid:", icon);
        throw new ExtendedIllegalArgumentException("icon (" + icon + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    /**
     Returns the descriptors for all properties.
     @return  The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }
}
