///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: KeyedDataQueueBeanInfo.java
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

/**
 The KeyedDataQueueBeanInfo class provides bean information for the KeyedDataQueue class.
 **/
public class KeyedDataQueueBeanInfo extends BaseDataQueueBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class BEAN_CLASS = KeyedDataQueue.class;

    /**
     Returns the bean descriptor.
     @return  The bean descriptor.
     **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(BEAN_CLASS);
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
                return loadImage("KeyedDataQueue16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("KeyedDataQueue32.gif");
        }
        return null;
    }
}
