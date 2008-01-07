///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  QSYSObjectPathNameBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2008 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
// @A1 12/12/2007 Correct EventSet for VetoablePropertyChange
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
 The QSYSObjectPathNameBeanInfo class provides BeanInfo for QSYSObjectPathName class.
 **/
public class QSYSObjectPathNameBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class BEAN_CLASS = QSYSObjectPathName.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", //@A1C
                                                                       VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { propertyChange, vetoableChange };

            PropertyDescriptor path = new PropertyDescriptor("path", BEAN_CLASS);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor libraryName = new PropertyDescriptor("libraryName", BEAN_CLASS);
            libraryName.setBound(true);
            libraryName.setConstrained(true);
            libraryName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_LIBRARY"));
            libraryName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_LIBRARY"));

            PropertyDescriptor objectName = new PropertyDescriptor("objectName", BEAN_CLASS);
            objectName.setBound(true);
            objectName.setConstrained(true);
            objectName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_OBJECT"));
            objectName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_OBJECT"));

            PropertyDescriptor memberName = new PropertyDescriptor("memberName", BEAN_CLASS);
            memberName.setBound(true);
            memberName.setConstrained(true);
            memberName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_MEMBER"));
            memberName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_MEMBER"));

            PropertyDescriptor objectType = new PropertyDescriptor("objectType", BEAN_CLASS);
            objectType.setBound(true);
            objectType.setConstrained(true);
            objectType.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_TYPE"));
            objectType.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_TYPE"));

            propertyDescriptors = new PropertyDescriptor[] { path, libraryName, objectName, memberName, objectType };
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
        // The index for the "path" property.
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
                return loadImage("QSYSObjectPathName16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("QSYSObjectPathName32.gif");
        }
        return null;
    }
}
