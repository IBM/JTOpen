///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageFileBeanInfo.java
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
   The MessageFileBeanInfo class provides bean information for
   the MessageFile class.
**/

public class MessageFileBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private final static Class beanClass = MessageFile.class;
  private static EventSetDescriptor[] events_;
  private static PropertyDescriptor[] properties_;
  private static ResourceBundleLoader rbl_;

  static
  {
    try
    {
      // Define the property descriptors.
      PropertyDescriptor property1 =
        new PropertyDescriptor("path", beanClass, "getPath", "setPath");
      property1.setBound(true);
      property1.setConstrained(true);
      property1.setDisplayName(rbl_.getText("PROP_NAME_PATH"));
      property1.setShortDescription(rbl_.getText("PROP_DESC_PATH"));

      PropertyDescriptor property2 =
        new PropertyDescriptor("system", beanClass, "getSystem", "setSystem");
      property2.setBound(true);
      property2.setConstrained(true);
      property2.setDisplayName(rbl_.getText("PROP_NAME_SYSTEM"));
      property2.setShortDescription(rbl_.getText("PROP_DESC_SYSTEM"));

      // @D1A
      PropertyDescriptor property3 =
        new PropertyDescriptor("helpTextFormatting", beanClass, "getHelpTextFormatting", "setHelpTextFormatting");
      property3.setBound(true);
      property3.setConstrained(true);
      property3.setDisplayName(rbl_.getText("PROP_NAME_MF_HELP_TEXT_FORMATTING"));
      property3.setShortDescription(rbl_.getText("PROP_DESC_MF_HELP_TEXT_FORMATTING"));



      PropertyDescriptor[] properties =
      {
        property1, property2, property3                                //@D1C
      };

      properties_ = properties;


      // Define the event descriptors.
      EventSetDescriptor event1 =
        new EventSetDescriptor(beanClass, "propertyChange",
                               PropertyChangeListener.class,
                               "propertyChange");
      event1.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_CHANGE"));
      event1.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_CHANGE"));

      EventSetDescriptor event2 =
        new EventSetDescriptor(beanClass, "vetoableChange",
                               VetoableChangeListener.class,
                               "vetoableChange");
      event2.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_VETO"));
      event2.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_VETO"));

      EventSetDescriptor[] events = { event1, event2 };

      events_ = events;
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
    return 1;
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
    return events_;
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
      image = loadImage("MessageFile16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("MessageFile32.gif");
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


