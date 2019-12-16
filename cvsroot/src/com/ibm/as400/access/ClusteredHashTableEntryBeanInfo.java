///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ClusteredHashTableEntryBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;

/**
   The ClusteredHashTableEntryBeanInfo class provides bean information for
   the ClusteredHashTableEntry class.
**/

public class ClusteredHashTableEntryBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private final static Class beanClass = ClusteredHashTableEntry.class;
  private static PropertyDescriptor[] properties_;

  static
  {
    try
    {
      // Define the property descriptors.
      PropertyDescriptor property1 =
        new PropertyDescriptor("entryAuthority", beanClass, "getEntryAuthority", "setEntryAuthority");
      property1.setBound(true);
      property1.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_ENTRY_AUTHORITY"));
      property1.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_ENTRY_AUTHORITY"));

      PropertyDescriptor property2 =
        new PropertyDescriptor("key", beanClass, "getKey", "setKey");
      property2.setBound(true);
      property2.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_KEY"));
      property2.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_KEY"));

      PropertyDescriptor property3 =
        new PropertyDescriptor("timeToLive", beanClass, "getTimeToLive", "setTimeToLive");
      property3.setBound(true);
      property3.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_TIME_TO_LIVE"));
      property3.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_TIME_TO_LIVE"));

      PropertyDescriptor property4 =
        new PropertyDescriptor("updateOption", beanClass, "getUpdateOption", "setUpdateOption");
      property4.setBound(true);
      property4.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_UPDATE_OPTION"));
      property4.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_UPDATE_OPTION"));

      PropertyDescriptor property5 =
        new PropertyDescriptor("userData", beanClass, "getUserData", "setUserData");
      property5.setBound(true);
      property5.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_USER_DATA"));
      property5.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_USER_DATA"));

      PropertyDescriptor[] properties =
      {
        property1, property2, property3, property4, property5
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
   Returns the default event index.
   @return The default event index.
   **/
  public int getDefaultEventIndex()
  {
    return 0;
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
   Returns the descriptors for all events.
   @return The descriptors for all events.
   **/
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    return null;
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
      image = loadImage("ClusteredHashTableEntry16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("ClusteredHashTableEntry32.gif");
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
