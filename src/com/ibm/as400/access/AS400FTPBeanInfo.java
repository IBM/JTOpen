///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FTPBeanInfo.java
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
import java.beans.SimpleBeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.VetoableChangeListener;


/**
   The AS400FTPBeanInfo class provides bean information for
   the FTP class.
**/

public class AS400FTPBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private final static Class beanClass = AS400FTP.class;
  private static PropertyDescriptor[] properties_;
  private static ResourceBundleLoader loader_;

  static
  {
    try
    {
      // Define the property descriptors.

      PropertyDescriptor system = new PropertyDescriptor("system", beanClass);
      system.setBound(true);
      system.setConstrained(true);
      system.setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
      system.setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));

      PropertyDescriptor publicAut = new PropertyDescriptor("saveFilePublicAuthority", beanClass);
      publicAut.setBound(true);
      publicAut.setConstrained(true);
      publicAut.setDisplayName(loader_.getText("PROP_NAME_SAVE_FILE_PUBLIC_AUTHORITY"));
      publicAut.setShortDescription(loader_.getText("PROP_DESC_SAVE_FILE_PUBLIC_AUTHORITY"));

      PropertyDescriptor[] properties =
      {
         system, publicAut
      };

      properties_ = properties;

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
     return new BeanInfo[] { new FTPBeanInfo() };
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
     return 0;
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
      image = loadImage("AS400FTP16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("AS400FTP32.gif");
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

