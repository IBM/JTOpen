///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTPBeanInfo.java
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
   The FTPBeanInfo class provides bean information for
   the FTPClient class.
**/

public class FTPBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private final static Class beanClass = FTP.class;
  private static EventSetDescriptor[] events_;
  private static PropertyDescriptor[] properties_;

  static
  {
    try
    {
      // Define the property descriptors.
      PropertyDescriptor property1 =
        new PropertyDescriptor("port", beanClass, "getPort", "setPort");
      property1.setBound(true);
      property1.setConstrained(true);
      property1.setDisplayName("port");
      property1.setShortDescription("The port to use when connecting to the server.");


      PropertyDescriptor property2 =
        new PropertyDescriptor("server", beanClass, "getServer", "setServer");
      property2.setBound(true);
      property2.setConstrained(true);
      property2.setDisplayName("server");
      property2.setShortDescription("The server to connect to.");


      PropertyDescriptor property3 =
        new PropertyDescriptor("user", beanClass, "getUser", "setUser");
      property3.setBound(true);
      property3.setConstrained(true);
      property3.setDisplayName("user");
      property3.setShortDescription("User identifer.");


      // PropertyDescriptor property4 =
      //   new PropertyDescriptor("reconnect", beanClass, "isReconnect", "setReconnect");
      // property4.setBound(true);
      // property4.setConstrained(true);
      // property4.setDisplayName("reconnect");
      // property4.setShortDescription("Automatically reconnect to the server when the communications link goes down.");


      PropertyDescriptor property5 =
        new PropertyDescriptor("password", beanClass, null, "setPassword");
      property5.setBound(true);
      property5.setConstrained(true);
      property5.setDisplayName("password");
      property5.setShortDescription("Password.");


      PropertyDescriptor property5a =
        new PropertyDescriptor("bufferSize", beanClass, "getBufferSize", "setBufferSize");
      property5a.setBound(true);
      property5a.setConstrained(true);
      property5a.setDisplayName("bufferSize");
      property5a.setShortDescription("Buffer size.");


      PropertyDescriptor property6 =
        new PropertyDescriptor("lastMessage", beanClass, "getLastMessage", null);

      property6.setHidden(true);

      PropertyDescriptor[] properties =
      {
        property1, property2, property3, property5, property5a, property6
        // property4
      };

      properties_ = properties;




      // Define the event descriptors.
      EventSetDescriptor event1 =
        new EventSetDescriptor(beanClass, "propertyChange",
                               PropertyChangeListener.class,
                               "propertyChange");
      event1.setDisplayName("propertyChange");
      event1.setShortDescription("A bound property has changed.");

      String[] listenerMethods = { "connected", "disconnected", "retrieved", "put", "listed"};


      EventSetDescriptor event2 =
        new EventSetDescriptor(beanClass, "FTP", FTPListener.class,
                               listenerMethods, "addFTPListener",
                               "removeFTPListener");
      event2.setDisplayName("FTPEvent");
      event2.setShortDescription("An ftp event has occurred.");


      EventSetDescriptor event3 =
        new EventSetDescriptor(beanClass, "vetoableChange",
                               VetoableChangeListener.class,
                               "vetoableChange");
      event3.setDisplayName("vetoableChange");
      event3.setShortDescription("A constrained property has changed");
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
      image = loadImage("FTP16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("FTP32.gif");
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

