///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CharacterDataAreaBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.awt.Image;


/**
The CharacterDataAreaBeanInfo class provides
bean information for the CharacterDataArea class.
**/
public class CharacterDataAreaBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class dbeanClass = CharacterDataArea.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

    private static PropertyDescriptor[] properties_;             //@D1A

    static
    {
      try
      {
        PropertyDescriptor path = new PropertyDescriptor("path", dbeanClass);
        path.setBound(true);
        path.setConstrained(true);
        path.setDisplayName(loader_.getText("PROP_NAME_PATH"));
        path.setShortDescription(loader_.getText("PROP_DESC_PATH"));
        properties_ = new PropertyDescriptor[] {path};           //@D1A
      }
      catch(Exception e)
      {
        throw new Error(e.toString());
      }
    }


    /**
    Returns additional bean information.
    **/
    public BeanInfo[] getAdditionalBeanInfo()
    {
     return new BeanInfo[] { new DataAreaBeanInfo() };
    }


    /**
    Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(dbeanClass);
    }


    /**
    Returns an Image for this bean's icon.
      @param icon The desired icon size and color.
      @return The Image for the icon.
    **/
    public Image getIcon(int icon)
    {
        Image image = null;

        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage("CharacterDataArea16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("CharacterDataArea32.gif");
                break;
        }

        return image;
    }

    /**                                                            //D1A
    Returns the index of the default property.                     //D1A
      @return The index to the default property.                   //D1A
    **/                                                            //D1A
    public int getDefaultPropertyIndex()                           //D1A
    {                                                              //D1A
        // the index for the "path" property                       //D1A
        return 0;                                                  //D1A
    }                                                              //D1A
                                                                   //D1A

    /**
    Returns the descriptors for all properties.                    //D1A
      @return The descriptors for all properties.                  //D1A
    **/                                                            //D1A
    public PropertyDescriptor[] getPropertyDescriptors()           //D1A
    {                                                              //D1A
        return properties_;                                        //D1A
    }                                                              //D1A

}

