///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ClusteredHashTableBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;

/**
   The ClusteredHashTableBeanInfo class provides bean information for
   the ClusteredHashTable class.
**/

public class ClusteredHashTableBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private final static Class beanClass = ClusteredHashTable.class;
  private static PropertyDescriptor[] properties_;

  static
  {
    try
    {
      // Define the property descriptors.
      PropertyDescriptor property1 =
        new PropertyDescriptor("name", beanClass, "getName", "setName");
      property1.setBound(true);
      property1.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_NAME"));
      property1.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_NAME"));

      PropertyDescriptor property2 =
        new PropertyDescriptor("system", beanClass, "getSystem", "setSystem");
      property2.setBound(true);
      property2.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
      property2.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));
      PropertyDescriptor[] properties =
      {
        property1, property2
      };
      properties_ = properties;

    }
    catch(Exception e)
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
   Returns the default property index.
   @return The default property index.
   **/
  public int getDefaultPropertyIndex()
  {
    return 1;
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
      image = loadImage("ClusteredHashTable16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("ClusteredHashTable32.gif");
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
