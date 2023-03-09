///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PanelGroup.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
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


/**
 The PanelGroupBeanInfo class provides bean information for the PanelGroup class.
 **/
public class PanelGroupBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = PanelGroup.class;

    private static EventSetDescriptor[] eventSetDescriptors_;
    private static PropertyDescriptor[] propertyDescriptors_;

    static
    {
        try
        {
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            eventSetDescriptors_ = new EventSetDescriptor[] { propertyChange };

            PropertyDescriptor path = new PropertyDescriptor("path", BEAN_CLASS);
            path.setBound(true);
            path.setConstrained(false);
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(false);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            propertyDescriptors_ = new PropertyDescriptor[] { path, system };
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
     @return  Zero (0), the index to the property change event.
     **/
    public int getDefaultEventIndex()
    {
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the path property.
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
        return eventSetDescriptors_;
    }

    /**
     Returns the descriptors for all properties.
     @return  The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors_;
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
            return loadImage("PanelGroup16.gif");
        case BeanInfo.ICON_MONO_32x32:
        case BeanInfo.ICON_COLOR_32x32:
            return loadImage("PanelGroup32.gif");
        }
        return null;
    }
}

