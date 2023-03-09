///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400StructureBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.BeanDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.BeanInfo;
import java.awt.Image;

/**
 *  The AS400Structure class provides bean information for the AS400Structure class.
 **/
public class AS400StructureBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = AS400Structure.class;

    private static PropertyDescriptor[] properties_;

    static
    {
        try
        {
            IndexedPropertyDescriptor members = new IndexedPropertyDescriptor("members", beanClass);
            members.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400STRUCTURE_MEMBERS"));
            members.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400STRUCTURE_MEMBERS"));

            PropertyDescriptor properties[] = {members};

            properties_ = properties;
        }
        catch (Exception e)
        {
            throw new Error(e.toString());
        }
    }


    /**
     * Returns the bean descriptor.
     * @return The bean descriptor.
     **/
    public BeanDescriptor getBeanDescriptor()
    {
     return new BeanDescriptor(beanClass);
    }

    /**
     * Returns the descriptors for all properties.
     * @return The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
         return properties_;
    }

    /**
      * Returns an Image for this bean's icon.
      * @param icon The desired icon size and color.
      * @return The Image for the icon.
      **/
    public Image getIcon(int icon)
    {
        Image image = null;

        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage("AS400Structure16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("AS400Structure32.gif");
                break;
        }

        return image;
    }
}

