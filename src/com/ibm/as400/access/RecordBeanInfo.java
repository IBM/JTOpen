///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: RecordBeanInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
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
 *The RecordBeanInfo class provides BeanInfo for the Record class.
**/
public class RecordBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  // Bean class
  private final static Class beanClass_ = Record.class;
  // BeanDescriptor for this class
  private static BeanDescriptor beanDescriptor_ = new BeanDescriptor(Record.class);
  // Set of event descriptors for this class:
  // PropertyChange, VetoableChange, RecordDescriptionEvent.FIELD_MODIFIED
  private static EventSetDescriptor[] eventSet_ = new EventSetDescriptor[3];
  // Set of property descriptors for this class
  // getFields, getKeyFields, recordFormat, recordName, recordNumber
  private static PropertyDescriptor[] propertySet_ = new PropertyDescriptor[5];
  // Handles loading the appropriate resource bundle
  private static ResourceBundleLoader loader_;

/**
   Constructs a RecordBeanInfo object.
**/
  public RecordBeanInfo()
  {
    try
    {
      // Populate the event descriptor set
      // RecordDescriptionEvent.FIELD_MODIFIED
      eventSet_[0] = new EventSetDescriptor(beanClass_, "recordDescription",
                                            RecordDescriptionListener.class,
                                            "fieldModified");
      eventSet_[0].setDisplayName(loader_.getText("EVT_NAME_FIELD_MODIFIED"));
      eventSet_[0].setShortDescription(loader_.getText("EVT_DESC_FIELD_MODIFIED"));


      // PropertyChangeEvent
      eventSet_[1] = new EventSetDescriptor(beanClass_, "propertyChange",
                                            java.beans.PropertyChangeListener.class,
                                            "propertyChange");
      eventSet_[1].setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
      eventSet_[1].setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));


      // VetoablePropertyChange
      eventSet_[2] = new EventSetDescriptor(beanClass_, "propertyVeto",
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
      // getFields
      propertySet_[0] = new PropertyDescriptor("fields", beanClass_,
                                               "getFields", null);
      propertySet_[0].setBound(false);
      propertySet_[0].setConstrained(false);
      propertySet_[0].setDisplayName(loader_.getText("PROP_NAME_FIELDS"));
      propertySet_[0].setShortDescription(loader_.getText("PROP_DESC_FIELDS"));


      // getKeyFields
      propertySet_[1] = new PropertyDescriptor("keyFields", beanClass_,
                                               "getKeyFields", null);
      propertySet_[1].setBound(false);
      propertySet_[1].setConstrained(false);
      propertySet_[1].setDisplayName(loader_.getText("PROP_NAME_KEY_FIELDS"));
      propertySet_[1].setShortDescription(loader_.getText("PROP_DESC_KEY_FIELDS"));


      // get/setRecordFormat
      propertySet_[2] = new PropertyDescriptor("recordFormat", beanClass_,
                                               "getRecordFormat",
                                               "setRecordFormat");
      propertySet_[2].setBound(true);
      propertySet_[2].setConstrained(true);
      propertySet_[2].setDisplayName(loader_.getText("PROP_NAME_RECORD_FORMAT"));
      propertySet_[2].setShortDescription(loader_.getText("PROP_DESC_RECORD_FORMAT"));


      // get/setRecordName
      propertySet_[3] = new PropertyDescriptor("recordName", beanClass_,
                                               "getRecordName",
                                               "setRecordName");
      propertySet_[3].setBound(true);
      propertySet_[3].setConstrained(true);
      propertySet_[3].setDisplayName(loader_.getText("PROP_NAME_RECORD_NAME"));
      propertySet_[3].setShortDescription(loader_.getText("PROP_DESC_RECORD_NAME"));


      // get/setRecordNumber
      propertySet_[4] = new PropertyDescriptor("recordNumber", beanClass_,
                                               "getRecordNumber",
                                               "setRecordNumber");
      propertySet_[4].setBound(true);
      propertySet_[4].setConstrained(true);
      propertySet_[4].setDisplayName(loader_.getText("PROP_NAME_RECORD_NUMBER"));
      propertySet_[4].setShortDescription(loader_.getText("PROP_DESC_RECORD_NUMBER"));


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
   *Returns the icon image for a Record bean.
   *@return The icon image.
  **/
  public Image getIcon(int icon)
  {
    Image image = null;

    switch(icon)
    {
    case BeanInfo.ICON_MONO_16x16:
    case BeanInfo.ICON_COLOR_16x16:
      image = loadImage("Record16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("Record32.gif");
      break;
    }
    return image;
  }
}
