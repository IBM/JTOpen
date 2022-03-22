///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserListBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
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
 The UserListBeanInfo class represents the bean information for the UserList class.
 **/
public class UserListBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = UserList.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { propertyChange, vetoableChange };

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor groupInfo = new PropertyDescriptor("groupInfo", BEAN_CLASS);
            groupInfo.setBound(true);
            groupInfo.setConstrained(true);
            groupInfo.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_GROUPINFO"));
            groupInfo.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_GROUPINFO"));

            PropertyDescriptor userInfo = new PropertyDescriptor("userInfo", BEAN_CLASS);
            userInfo.setBound(true);
            userInfo.setConstrained(true);
            userInfo.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_USERINFO"));
            userInfo.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_USERINFO"));

            PropertyDescriptor userProfile = new PropertyDescriptor("userProfile", BEAN_CLASS);
            userProfile.setBound(false);
            userProfile.setConstrained(false);
            userProfile.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_USERPROFILE"));
            userProfile.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_USERPROFILE"));
            propertyDescriptors = new PropertyDescriptor[] { system, groupInfo, userInfo, userProfile };
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
                return loadImage("UserList16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("UserList32.gif");
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
