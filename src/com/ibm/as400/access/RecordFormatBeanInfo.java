///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordFormatBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
 *The RecordFormatBeanInfo class provides BeanInfo for the RecordFormat class.
**/
public class RecordFormatBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Bean class
  private final static Class beanClass_ = RecordFormat.class;

  // BeanDescriptor for this class
  private static BeanDescriptor beanDescriptor_ = new BeanDescriptor(RecordFormat.class);

  // Set of event descriptors for this class:
  // PropertyChange, VetoableChange, RecordDescriptionEvent
  private static EventSetDescriptor[] eventSet_ = new EventSetDescriptor[3];

  // Set of property descriptors for this class
  // getFieldDescriptions, getKeyFieldDescriptions, getFieldNames, getKeyFieldNames,
  // getName
  private static PropertyDescriptor[] propertySet_ = new PropertyDescriptor[5];

  // Handles loading the appropriate resource bundle
  private static ResourceBundleLoader loader_;

/**
   Constructs a RecordFormatBeanInfo object.
**/
  public RecordFormatBeanInfo()
  {
    try
    {
      // Populate the event descriptor set
      // RecordDescriptionEvent.FIELD_DESCRIPTION_ADDED and
      // KEY_FIELD_DESCRIPTION_ADDED
      String[] listenerMethods = {"fieldDescriptionAdded", "keyFieldDescriptionAdded"};
      eventSet_[0] = new EventSetDescriptor(beanClass_, "recordDescription",
                                            RecordDescriptionListener.class,
                                            listenerMethods, "addRecordDescriptionListener", "removeRecordDescriptionListener");
      eventSet_[0].setDisplayName(loader_.getText("EVT_NAME_RECORD_DESCRIPTION_EVENT"));
      eventSet_[0].setShortDescription(loader_.getText("EVT_DESC_RECORD_DESCRIPTION_EVENT"));


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
      // getFieldDescriptions
      propertySet_[0] = new PropertyDescriptor("fieldDescriptions", beanClass_,
                                               "getFieldDescriptions", null);
      propertySet_[0].setBound(false);
      propertySet_[0].setConstrained(false);
      propertySet_[0].setDisplayName(loader_.getText("PROP_NAME_FIELD_DESCRIPTIONS"));
      propertySet_[0].setShortDescription(loader_.getText("PROP_DESC_FIELD_DESCRIPTIONS"));

      // getKeyFieldDescriptions
      propertySet_[1] = new PropertyDescriptor("keyFieldDescriptions", beanClass_,
                                               "getKeyFieldDescriptions", null);
      propertySet_[1].setBound(false);
      propertySet_[1].setConstrained(false);
      propertySet_[1].setDisplayName(loader_.getText("PROP_NAME_KEY_FIELD_DESCRIPTIONS"));
      propertySet_[1].setShortDescription(loader_.getText("PROP_DESC_KEY_FIELD_DESCRIPTIONS"));

      // getFieldNames
      propertySet_[2] = new PropertyDescriptor("fieldNames", beanClass_,
                                               "getFieldNames", null);
      propertySet_[2].setBound(false);
      propertySet_[2].setConstrained(false);
      propertySet_[2].setDisplayName(loader_.getText("PROP_NAME_FIELD_NAMES"));
      propertySet_[2].setShortDescription(loader_.getText("PROP_DESC_FIELD_NAMES"));

      // getKeyFieldNames
      propertySet_[3] = new PropertyDescriptor("keyFieldNames", beanClass_,
                                               "getKeyFieldNames", null);
      propertySet_[3].setBound(false);
      propertySet_[3].setConstrained(false);
      propertySet_[3].setDisplayName(loader_.getText("PROP_NAME_KEY_FIELD_NAMES"));
      propertySet_[3].setShortDescription(loader_.getText("PROP_DESC_KEY_FIELD_NAMES"));

      // get/setName
      propertySet_[4] = new PropertyDescriptor("name", beanClass_,
                                               "getName",
                                               "setName");
      propertySet_[4].setBound(true);
      propertySet_[4].setConstrained(true);
      propertySet_[4].setDisplayName(loader_.getText("PROP_NAME_RECORD_FORMAT_NAME"));
      propertySet_[4].setShortDescription(loader_.getText("PROP_DESC_RECORD_FORMAT_NAME"));

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
   Returns the copyright.
   @return The copyright String.
   **/
  private static String getCopyright()
  {
    return Copyright.copyright;
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
   *Returns the icon image for a RecordFormat bean.
   *@return The icon image.
  **/
  public Image getIcon(int icon)
  {
    Image image = null;

    switch(icon)
    {
    case BeanInfo.ICON_MONO_16x16:
    case BeanInfo.ICON_COLOR_16x16:
      image = loadImage("RecordFormat16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("RecordFormat32.gif");
      break;
    }
    return image;
  }
}
