///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileOutputStreamBeanInfo.java
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
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.VetoableChangeListener;


/**
The IFSFileOutputStreamBeanInfo class provides BeanInfo for the IFSFileOutputStream class.
**/
public class IFSFileOutputStreamBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private final static Class beanClass = IFSFileOutputStream.class;
  private static EventSetDescriptor[] events_;
  private static PropertyDescriptor[] ifsFileOutputStreamProperties_;
  protected static PropertyDescriptor[] ifsTextFileOutputStreamProperties_;
  private static ResourceBundleLoader rbl_;

  static
  {
    try
    {
      // Define the property descriptors.
      PropertyDescriptor property1 =
        new PropertyDescriptor("append", beanClass, null, "setAppend");
      property1.setBound(true);
      property1.setConstrained(true);
      property1.setDisplayName(rbl_.getText("PROP_NAME_APPEND_PROP"));
      property1.setShortDescription(rbl_.getText("PROP_DESC_APPEND"));
      PropertyDescriptor property2 =
        new PropertyDescriptor("FD", beanClass, "getFD", "setFD");
      property2.setBound(true);
      property2.setConstrained(true);
      property2.setDisplayName(rbl_.getText("PROP_NAME_FILE_DESCRIPTOR"));
      property2.setShortDescription(rbl_.getText("PROP_DESC_FILE_DESCRIPTOR"));
      PropertyDescriptor property3 =
        new PropertyDescriptor("path", beanClass, "getPath", "setPath");
      property3.setBound(true);
      property3.setConstrained(true);
      property3.setDisplayName(rbl_.getText("PROP_NAME_PATH"));
      property3.setShortDescription(rbl_.getText("PROP_DESC_PATH"));
      PropertyDescriptor property4 =
        new PropertyDescriptor("shareOption", beanClass, "getShareOption",
                               "setShareOption");
      property4.setBound(true);
      property4.setConstrained(true);
      property4.setPropertyEditorClass(IFSShareOptionEditor.class);
      property4.setDisplayName(rbl_.getText("PROP_NAME_SHARE_OPTION"));
      property4.setShortDescription(rbl_.getText("PROP_DESC_SHARE_OPTION"));
      PropertyDescriptor property5 =
        new PropertyDescriptor("system", beanClass, "getSystem", "setSystem");
      property5.setBound(true);
      property5.setConstrained(true);
      property5.setDisplayName(rbl_.getText("PROP_NAME_SYSTEM"));
      property5.setShortDescription(rbl_.getText("PROP_DESC_SYSTEM"));

      PropertyDescriptor property6 =
        new PropertyDescriptor("CCSID",
                               IFSTextFileOutputStream.class,
                               "getCCSID", "setCCSID");
      property6.setBound(true);
      property6.setConstrained(true);
      property6.setShortDescription(rbl_.getText("PROP_DESC_AS400_CCSID"));

      PropertyDescriptor[] ifsFileOutputStreamProperties =
      {
        property1, property2, property3, property4, property5
      };
      ifsFileOutputStreamProperties_ = ifsFileOutputStreamProperties;
      PropertyDescriptor[] ifsTextFileOutputStreamProperties =
      {
        property1, property2, property3, property4, property5, property6
      };
      ifsTextFileOutputStreamProperties_ = ifsTextFileOutputStreamProperties;

      // Define the event descriptors.
      EventSetDescriptor event1 =
        new EventSetDescriptor(beanClass, "propertyChange",
                               PropertyChangeListener.class,
                               "propertyChange");
      event1.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_CHANGE"));
      event1.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_CHANGE"));
      String[] listenerMethods = { "fileClosed", "fileModified",
                                   "fileOpened" };
      EventSetDescriptor event2 =
        new EventSetDescriptor(beanClass, "file", FileListener.class,
                               listenerMethods, "addFileListener",
                               "removeFileListener");
      event2.setDisplayName(rbl_.getText("EVT_NAME_FILE_EVENT"));
      event2.setShortDescription(rbl_.getText("EVT_DESC_FILE_EVENT"));
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
   Returns the default event index.
   @return The default event index (always 1).
   **/
  public int getDefaultEventIndex()
  {
    return 1;
  }

  /**
   Returns the default property index.
   @return The default property index (always 1).
   **/
  public int getDefaultPropertyIndex()
  {
    return 2;
  }

  /**
   Returns the descriptors for all events.
   @return The descriptors for all events.
   **/
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    return events_;
  }

  // @A1A
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
              image = loadImage("IFSFileOutputStream16.gif");
              break;
          case BeanInfo.ICON_MONO_32x32:
          case BeanInfo.ICON_COLOR_32x32:
              image = loadImage("IFSFileOutputStream32.gif");
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
    return ifsFileOutputStreamProperties_;
  }

}








