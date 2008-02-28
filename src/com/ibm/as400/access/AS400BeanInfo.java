///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400BeanInfo.java
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
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
 The AS400BeanInfo class provides bean information for the AS400 class.
 **/
public class AS400BeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = AS400.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor connection = new EventSetDescriptor(BEAN_CLASS, "connection", ConnectionListener.class, new String[] { "connected", "disconnected" }, "addConnectionListener", "removeConnectionListener");
            connection.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_CONNECTION_EVENT"));
            connection.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_CONNECTION_EVENT"));

            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { connection, propertyChange, vetoableChange };

            PropertyDescriptor systemName = new PropertyDescriptor("systemName", BEAN_CLASS);
            systemName.setBound(true);
            systemName.setConstrained(true);
            systemName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_SYSTEM"));
            systemName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_SYSTEM"));

            PropertyDescriptor userId = new PropertyDescriptor("userId", BEAN_CLASS);
            userId.setBound(true);
            userId.setConstrained(true);
            userId.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_USERID"));
            userId.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_USERID"));

            PropertyDescriptor password = new PropertyDescriptor("password", BEAN_CLASS, null, "setPassword");
            password.setBound(false);
            password.setConstrained(false);
            password.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_PASSWORD"));
            password.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_PASSWORD"));

            PropertyDescriptor profileToken = new PropertyDescriptor("profileToken", BEAN_CLASS, null, "setProfileToken");
            profileToken.setBound(false);
            profileToken.setConstrained(false);
            profileToken.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_PROFILETOKEN"));
            profileToken.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_PROFILETOKEN"));

            PropertyDescriptor proxyServer = new PropertyDescriptor("proxyServer", BEAN_CLASS);
            proxyServer.setBound(true);
            proxyServer.setConstrained(true);
            proxyServer.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_PROXYSERVER"));
            proxyServer.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_PROXYSERVER"));

            PropertyDescriptor guiAvailable = new PropertyDescriptor("guiAvailable", BEAN_CLASS);
            guiAvailable.setBound(true);
            guiAvailable.setConstrained(true);
            guiAvailable.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_GUI"));
            guiAvailable.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_GUI"));

            PropertyDescriptor mustUseSockets = new PropertyDescriptor("mustUseSockets", BEAN_CLASS);
            mustUseSockets.setBound(false);
            mustUseSockets.setConstrained(false);
            mustUseSockets.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_MUSTUSESOCKETS"));
            mustUseSockets.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_MUSTUSESOCKETS"));

            PropertyDescriptor showCheckboxes = new PropertyDescriptor("showCheckboxes", BEAN_CLASS);
            showCheckboxes.setBound(false);
            showCheckboxes.setConstrained(false);
            showCheckboxes.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_SHOWCHECKBOXES"));
            showCheckboxes.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_SHOWCHECKBOXES"));

            PropertyDescriptor threadUsed = new PropertyDescriptor("threadUsed", BEAN_CLASS);
            threadUsed.setBound(true);
            threadUsed.setConstrained(true);
            threadUsed.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_THREADUSED"));
            threadUsed.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_THREADUSED"));

            PropertyDescriptor useDefaultUser = new PropertyDescriptor("useDefaultUser", BEAN_CLASS);
            useDefaultUser.setBound(true);
            useDefaultUser.setConstrained(true);
            useDefaultUser.setExpert(true);
            useDefaultUser.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_DEFUSER"));
            useDefaultUser.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_DEFUSER"));

            PropertyDescriptor usePasswordCache = new PropertyDescriptor("usePasswordCache", BEAN_CLASS);
            usePasswordCache.setBound(true);
            usePasswordCache.setConstrained(true);
            usePasswordCache.setExpert(true);
            usePasswordCache.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_PWCACHE"));
            usePasswordCache.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_PWCACHE"));

            propertyDescriptors = new PropertyDescriptor[] { systemName, userId, password, profileToken, proxyServer, guiAvailable, mustUseSockets, showCheckboxes, threadUsed, useDefaultUser, usePasswordCache };
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
        // The index for the "connection" event.
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "systemName" property.
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
     @param  icon  The desired icon size and color.
     @return  The Image for the icon.
     **/
    public Image getIcon(int icon)
    {
        switch (icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("AS40016.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("AS40032.gif");
        }
        return null;
    }
}
