///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServiceProgramCallBeanInfo.java
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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 The ServiceProgramCallBeanInfo class provides bean information for the ServiceProgramCall class.  ServiceProgramCall is a subclass of ProgramCall so bean information from ProgramCallBeanInfo also applies to ServiceProgramCall.
 **/
public class ServiceProgramCallBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = ServiceProgramCall.class;

    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            PropertyDescriptor procedureName = new PropertyDescriptor("procedureName", BEAN_CLASS);
            procedureName.setBound(true);
            procedureName.setConstrained(true);
            procedureName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMPROCEDURE"));
            procedureName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMPROCEDURE"));

            PropertyDescriptor returnValueFormat = new PropertyDescriptor("returnValueFormat", BEAN_CLASS);
            returnValueFormat.setBound(true);
            returnValueFormat.setConstrained(true);
            returnValueFormat.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PARMRETURNFORMAT"));
            returnValueFormat.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PARMRETURNFORMAT"));

            propertyDescriptors = new PropertyDescriptor[] { procedureName, returnValueFormat };
        }
        catch (IntrospectionException e)
        {
            Trace.log(Trace.ERROR, "Unexpected IntrospectionException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Returns the BeanInfo for the superclass of this bean.  Since ServiceProgramCall is a subclass of ProgramCall, this method will return a ProgramCallBeanInfo object.
     @return  an array of BeanInfo objects containing this bean's superclass BeanInfo.
     **/
    public BeanInfo[] getAdditionalBeanInfo()
    {
        return new BeanInfo[] { new ProgramCallBeanInfo() };
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
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "procedureName" property.
        return 0;
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
                return loadImage("ServiceProgramCall16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("ServiceProgramCall32.gif");
        }
        return null;
    }
}
