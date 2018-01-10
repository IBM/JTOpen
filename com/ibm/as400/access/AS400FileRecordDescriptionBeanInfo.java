///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileRecordDescriptionBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @A1 12/12/2007 Correct EventSet for VetoablePropertyChange
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;

/**
  * The AS400FileRecordDescriptionBeanInfo class provides
  * bean information for the AS400FileRecordDescription class.
**/
public class AS400FileRecordDescriptionBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  // Bean class
  private final static Class beanClass_ = AS400FileRecordDescription.class;


  // BeanDescriptor for this class
  private static BeanDescriptor beanDescriptor_ = new BeanDescriptor(AS400FileRecordDescription.class);


  // Set of event descriptors for this class:
  // PropertyChange, VetoableChange, RecordDescriptionEvent.FIELD_MODIFIED
  private static EventSetDescriptor[] eventSet_ = new EventSetDescriptor[3];


  // Set of property descriptors for this class
  // memberName, fileName, recordFormat, path, system
  private static PropertyDescriptor[] propertySet_ = new PropertyDescriptor[4];


  // Handles loading the appropriate resource bundle
  private static ResourceBundleLoader loader_;

  static
  {
    try
    {
      // Populate the event descriptor set
      // FileEvent
      String[] listenerMethods = {"recordFormatRetrieved", "recordFormatSourceCreated"};
      eventSet_[0] = new EventSetDescriptor(beanClass_, "AS400FileRecordDescription",
                                            AS400FileRecordDescriptionListener.class,
                                            listenerMethods, "addAS400FileRecordDescriptionListener",
                                            "removeAS400FileRecordDescriptionListener");
      eventSet_[0].setDisplayName(loader_.getText("EVT_NAME_AS400FILE_RECORD_DESCRIPTION"));
      eventSet_[0].setShortDescription(loader_.getText("EVT_DESC_AS400FILE_RECORD_DESCRIPTION"));


      // PropertyChangeEvent
      eventSet_[1] = new EventSetDescriptor(beanClass_, "propertyChange",
                                            java.beans.PropertyChangeListener.class,
                                            "propertyChange");
      eventSet_[1].setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
      eventSet_[1].setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));


      // VetoablePropertyChange
      eventSet_[2] = new EventSetDescriptor(beanClass_, "propertyChange", //@A1C
                                            java.beans.VetoableChangeListener.class,
                                            "vetoableChange");
      eventSet_[2].setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
      eventSet_[2].setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

    }
    catch(IntrospectionException e)
    {
      throw new Error(e.toString());
    }

    try
    {
      // Populate the property descriptor set
      // getFileName
      propertySet_[0] = new PropertyDescriptor("fileName", beanClass_,
                                               "getFileName", null);
      propertySet_[0].setBound(true);
      propertySet_[0].setConstrained(false);
      propertySet_[0].setDisplayName(loader_.getText("PROP_NAME_FILE_NAME"));
      propertySet_[0].setShortDescription(loader_.getText("PROP_DESC_FILE_NAME"));


      // getKeyFields
      propertySet_[1] = new PropertyDescriptor("memberName", beanClass_,
                                               "getMemberName", null);
      propertySet_[1].setBound(true);
      propertySet_[1].setConstrained(false);
      propertySet_[1].setDisplayName(loader_.getText("PROP_NAME_MEMBER"));
      propertySet_[1].setShortDescription(loader_.getText("PROP_DESC_MEMBER"));


      // get/setPath
      propertySet_[2] = new PropertyDescriptor("path", beanClass_,
                                               "getPath",
                                               "setPath");
      propertySet_[2].setBound(true);
      propertySet_[2].setConstrained(true);
      propertySet_[2].setDisplayName(loader_.getText("PROP_NAME_PATH"));
      propertySet_[2].setShortDescription(loader_.getText("PROP_DESC_PATH"));


      // get/setSystem
      propertySet_[3] = new PropertyDescriptor("system", beanClass_,
                                               "getSystem",
                                               "setSystem");
      propertySet_[3].setBound(true);
      propertySet_[3].setConstrained(true);
      propertySet_[3].setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
      propertySet_[3].setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));
    }
    catch(IntrospectionException e)
    {
      throw new Error(e.toString());
    }
  }

  /**
   *Returns the bean descriptor.
   *@return The bean descriptor.
  **/
  public BeanDescriptor getBeanDescriptor()
  {
    return beanDescriptor_;
  }

  /**
   *Returns the index of the default event.
   *@return The index to the default event.
  **/
  public int getDefaultEventIndex()
  {
    // the index for the property change event
    return 0;
  }


  /**
   *Returns the index of the default property.
   *@return The index to the default property.
  **/
  public int getDefaultPropertyIndex()
  {
    // the index for the default property
    return 0;
  }


  /**
   *Returns the set of event descriptors.
   *@return The event descriptor set.
  **/
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    return eventSet_;
  }

  /**
   *Returns the property descriptors.
   *@return The property descriptors.
  **/
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    return propertySet_;
  }

  /**
   *Returns the icon image for a AS400FileRecordDescription bean.
   *@return The icon image.
  **/
  public Image getIcon(int icon)
  {
    Image image = null;

    switch(icon)
    {
    case BeanInfo.ICON_MONO_16x16:
    case BeanInfo.ICON_COLOR_16x16:
      image = loadImage("AS400FileRecordDescription16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("AS400FileRecordDescription32.gif");
      break;
    }
    return image;
  }
}
