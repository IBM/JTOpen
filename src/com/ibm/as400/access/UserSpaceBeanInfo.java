///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserSpaceBeanInfo.java
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
   The UserSpaceBeanInfo class provides bean information for
   the UserSpace class.
**/

public class UserSpaceBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private final static Class beanClass = UserSpace.class;
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

      // The introspecition process will reveal features that aren't
      // really properties.  We must declare them and mark as hidden.
      PropertyDescriptor property3 =
        new PropertyDescriptor("autoExtendible", beanClass, "isAutoExtendible", "setAutoExtendible");
      property3.setHidden(true);

      PropertyDescriptor property4 =
        new PropertyDescriptor("initialValue", beanClass, "getInitialValue", "setInitialValue");
      property4.setHidden(true);

      PropertyDescriptor property5 =
        new PropertyDescriptor("length", beanClass, "getLength", "setLength");
      property5.setHidden(true);

      PropertyDescriptor property6 =
        new PropertyDescriptor("name",beanClass, "getName", null);
      property6.setHidden(true);

      // @E1 new property
      PropertyDescriptor property7 =
        new PropertyDescriptor("mustUseProgramCall", beanClass, "isMustUseProgramCall", "setMustUseProgramCall");
      property7.setBound(true);
      property7.setConstrained(false);
      property7.setDisplayName("mustUseProgramCall");
      property7.setShortDescription("Use ProgramCall to read and write user space data.");


      // @E1 add new property to list
      PropertyDescriptor[] properties =
      {
        property1, property2, property3, property4, property5, property6, property7
      };
      properties_ = properties;

      // Define the event descriptors.
      EventSetDescriptor event1 =
        new EventSetDescriptor(beanClass, "propertyChange",
                               PropertyChangeListener.class,
                               "propertyChange");
      event1.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_CHANGE"));
      event1.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_CHANGE"));
      String[] listenerMethods = { "created", "deleted", "read", "written"};
      EventSetDescriptor event2 =
        new EventSetDescriptor(beanClass, "userSpace", UserSpaceListener.class,
                               listenerMethods, "addUserSpaceListener",
                               "removeUserSpaceListener");
      event2.setDisplayName(rbl_.getText("EVT_NAME_US_EVENT"));
      event2.setShortDescription(rbl_.getText("EVT_DESC_US_EVENT"));
      EventSetDescriptor event3 =
        new EventSetDescriptor(beanClass, "vetoableChange",
                               VetoableChangeListener.class,
                               "vetoableChange");
      event3.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_VETO"));
      event3.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_VETO"));
      EventSetDescriptor[] events = { event1, event2, event3 };
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
   Returns the copyright.
   @return The copyright String.
   **/
  private static String getCopyright()
  {
    return Copyright.copyright;
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
      image = loadImage("UserSpace16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("UserSpace32.gif");
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
