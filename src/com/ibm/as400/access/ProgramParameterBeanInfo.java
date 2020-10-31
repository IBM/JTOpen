///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramParameterBeanInfo.java
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
 The ProgramParameterBeanInfo class provides bean information for the ProgramParameter class.
 **/
public class ProgramParameterBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = ProgramParameter.class;

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

            PropertyDescriptor inputData = new PropertyDescriptor("inputData", BEAN_CLASS);
            inputData.setBound(true);
            inputData.setConstrained(true);
            inputData.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMINPUT"));
            inputData.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMINPUT"));

            PropertyDescriptor outputDataLength = new PropertyDescriptor("outputDataLength", BEAN_CLASS);
            outputDataLength.setBound(true);
            outputDataLength.setConstrained(true);
            outputDataLength.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMOUTPUTLEN"));
            outputDataLength.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMOUTPUTLEN"));

            PropertyDescriptor outputData = new PropertyDescriptor("outputData", BEAN_CLASS, "getOutputData", null);
            outputData.setBound(false);
            outputData.setConstrained(false);
            outputData.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMOUTPUT"));
            outputData.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMOUTPUT"));

            PropertyDescriptor parameterType = new PropertyDescriptor("parameterType", BEAN_CLASS);
            parameterType.setBound(true);
            parameterType.setConstrained(true);
            parameterType.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMTYPE"));
            parameterType.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMTYPE"));

            propertyDescriptors = new PropertyDescriptor[] { inputData, outputDataLength, outputData, parameterType };
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
        // The index for the "propertyChange" event
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "inputData" property.
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
                return loadImage("ProgramParameter16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("ProgramParameter32.gif");
        }
        return null;
    }
}
