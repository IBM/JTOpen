///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LineLayoutFormPanelBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;


import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;

/**
The LineLayoutFormPanelBeanInfo class provides
bean information for the LineLayoutFormPanel class.
**/
public class LineLayoutFormPanelBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = LineLayoutFormPanel.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;

    private static PropertyDescriptor[] properties_;


    static
    {
      
      try
      {
        // ***** PROPERTIES
        PropertyDescriptor size = new PropertyDescriptor("size", beanClass, "getSize", null);

        size.setBound(false);
        size.setConstrained(false);
        size.setDisplayName(loader_.getText("PROP_NAME_SIZE"));
        size.setShortDescription(loader_.getText("PROP_DESC_PANELSIZE"));
        
        properties_ = new PropertyDescriptor[] { size };
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
    Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }


    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    
    /**
      Returns an image for the icon.

      @param icon    The icon size and color.
      @return        The image.
    **/
    public Image getIcon (int icon)
    {   
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("LineLayoutFormPanel16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("LineLayoutFormPanel32.gif");
                break;
        }
        return image;
    }

    /**
    Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}


