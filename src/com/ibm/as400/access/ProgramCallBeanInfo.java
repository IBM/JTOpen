///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramCallBeanInfo.java
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
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
 The ProgramCallBeanInfo class provides bean information for the ProgramCall class.
 **/
public class ProgramCallBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = ProgramCall.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor actionCompleted = new EventSetDescriptor(BEAN_CLASS, "actionCompleted", ActionCompletedListener.class, "actionCompleted");
            actionCompleted.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_ACTION_COMPLETED"));
            actionCompleted.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_ACTION_COMPLETED"));

            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { actionCompleted, propertyChange, vetoableChange };

            PropertyDescriptor program = new PropertyDescriptor("program", BEAN_CLASS);
            program.setBound(true);
            program.setConstrained(true);
            program.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PROGRAM"));
            program.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PROGRAM"));

            PropertyDescriptor parameterList = new PropertyDescriptor("parameterList", BEAN_CLASS);
            parameterList.setBound(true);
            parameterList.setConstrained(true);
            parameterList.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMLIST"));
            parameterList.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMLIST"));

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor threadSafe = new PropertyDescriptor("threadSafe", BEAN_CLASS);
            threadSafe.setBound(true);
            threadSafe.setConstrained(false);
            // Note:  We share some names/descriptions with CommandCall.
            threadSafe.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CMD_THREADSAFE"));
            threadSafe.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CMD_THREADSAFE"));

            propertyDescriptors = new PropertyDescriptor[] { program, parameterList, system, threadSafe };
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
        // The index for the "actionCompleted" event.
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "program" property.
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
                return loadImage("ProgramCall16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("ProgramCall32.gif");
        }
        return null;
    }
}
