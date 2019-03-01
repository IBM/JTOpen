///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpaceBeanInfo.java
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
 The UserSpaceBeanInfo class provides bean information for the UserSpace class.
 **/
public class UserSpaceBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = UserSpace.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            // Define the event descriptors.
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor userSpace = new EventSetDescriptor(BEAN_CLASS, "userSpace", UserSpaceListener.class, new String[] { "created", "deleted", "read", "written" }, "addUserSpaceListener", "removeUserSpaceListener");
            userSpace.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_US_EVENT"));
            userSpace.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_US_EVENT"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));
            eventSetDescriptors = new EventSetDescriptor[] { propertyChange, userSpace, vetoableChange };

            // Define the property descriptors.
            PropertyDescriptor path = new PropertyDescriptor("path", BEAN_CLASS);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            // The introspecition process will reveal features that aren't really properties.  We must declare them and mark as hidden.
            PropertyDescriptor autoExtendible = new PropertyDescriptor("autoExtendible", BEAN_CLASS);
            autoExtendible.setHidden(true);

            PropertyDescriptor initialValue = new PropertyDescriptor("initialValue", BEAN_CLASS);
            initialValue.setHidden(true);

            PropertyDescriptor length = new PropertyDescriptor("length", BEAN_CLASS);
            length.setHidden(true);

            PropertyDescriptor name = new PropertyDescriptor("name",BEAN_CLASS, "getName", null);
            name.setHidden(true);

            PropertyDescriptor mustUseProgramCall = new PropertyDescriptor("mustUseProgramCall", BEAN_CLASS);
            mustUseProgramCall.setBound(true);
            mustUseProgramCall.setConstrained(false);
            mustUseProgramCall.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_US_MUSTUSEPGMCALL"));
            mustUseProgramCall.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_US_MUSTUSEPGMCALL"));

            propertyDescriptors = new PropertyDescriptor[] { path, system, autoExtendible, initialValue, length, name, mustUseProgramCall };
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
        // The index for the "userSpace" event.
        return 1;
    }

    /**
     Returns the index of the default property.
     @return  One (1), the index to the default property.
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
                return loadImage("UserSpace16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("UserSpace32.gif");
        }
        return null;
    }
}
