///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileBeanInfo.java
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

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.Method; //@B0A

/**
 * The AS400FileBeanInfo class provides bean
 * information for the AS400File class.
**/
public class AS400FileBeanInfo extends SimpleBeanInfo
{
  static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  // Bean class
  private final static Class beanClass_ = AS400File.class;


  // BeanDescriptor for this class
  private static BeanDescriptor beanDescriptor_ = new BeanDescriptor(AS400File.class);


  // Set of event descriptors for this class:
  // PropertyChange, VetoableChange, RecordDescriptionEvent.FIELD_MODIFIED
  private static EventSetDescriptor[] eventSet_ = new EventSetDescriptor[3];


  // Set of property descriptors for this class
  // memberName, fileName, recordFormat, path, system
  private static PropertyDescriptor[] propertySet_ = new PropertyDescriptor[5];


  // Handles loading the appropriate resource bundle
  // private static ResourceBundleLoader loader_;

  public AS400FileBeanInfo()
  {
    try
    {
      // Populate the event descriptor set
      // FileEvent
      String[] listenerMethods = {"fileClosed", "fileCreated", "fileDeleted",
      "fileModified", "fileOpened"};
      eventSet_[0] = new EventSetDescriptor(beanClass_, "file",
                                            FileListener.class,
                                            listenerMethods, "addFileListener",
                                            "removeFileListener");
      eventSet_[0].setDisplayName(ResourceBundleLoader.getText("EVT_NAME_FILE_EVENT"));
      eventSet_[0].setShortDescription(ResourceBundleLoader.getText("EVT_DESC_FILE_EVENT"));


      // PropertyChangeEvent
      eventSet_[1] = new EventSetDescriptor(beanClass_, "propertyChange",
                                            java.beans.PropertyChangeListener.class,
                                            "propertyChange");
      eventSet_[1].setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
      eventSet_[1].setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));


      // VetoablePropertyChange
      eventSet_[2] = new EventSetDescriptor(beanClass_, "propertyChange", //@A1C
                                            java.beans.VetoableChangeListener.class,
                                            "vetoableChange");
      eventSet_[2].setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
      eventSet_[2].setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

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
      propertySet_[0].setDisplayName(ResourceBundleLoader.getText("PROP_NAME_FILE_NAME"));
      propertySet_[0].setShortDescription(ResourceBundleLoader.getText("PROP_DESC_FILE_NAME"));


      // getKeyFields
      propertySet_[1] = new PropertyDescriptor("memberName", beanClass_,
                                               "getMemberName", null);
      propertySet_[1].setBound(true);
      propertySet_[1].setConstrained(false);
      propertySet_[1].setDisplayName(ResourceBundleLoader.getText("PROP_NAME_MEMBER"));
      propertySet_[1].setShortDescription(ResourceBundleLoader.getText("PROP_DESC_MEMBER"));


      // get/setPath
      propertySet_[2] = new PropertyDescriptor("path", beanClass_,
                                               "getPath",
                                               "setPath");
      propertySet_[2].setBound(true);
      propertySet_[2].setConstrained(true);
      propertySet_[2].setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
      propertySet_[2].setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));


      // get/setRecordFormat
//@B0D      propertySet_[3] = new PropertyDescriptor("recordFormat", beanClass_,
//@B0D                                               "getRecordFormat",
//@B0D                                               "setRecordFormat");
      Method getter = beanClass_.getMethod("getRecordFormat", (java.lang.Class []) null); //@B0A //@pdc cast for jdk1.5
      Method setter = beanClass_.getMethod("setRecordFormat", new Class[] { RecordFormat.class }); //@B0A
      propertySet_[3] = new PropertyDescriptor("recordFormat", getter, setter); //@B0A
      propertySet_[3].setBound(true);
      propertySet_[3].setConstrained(true);
      propertySet_[3].setDisplayName(ResourceBundleLoader.getText("PROP_NAME_RECORD_FORMAT"));
      propertySet_[3].setShortDescription(ResourceBundleLoader.getText("PROP_DESC_RECORD_FORMAT"));


      // get/setSystem
      propertySet_[4] = new PropertyDescriptor("system", beanClass_,
                                               "getSystem",
                                               "setSystem");
      propertySet_[4].setBound(true);
      propertySet_[4].setConstrained(true);
      propertySet_[4].setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
      propertySet_[4].setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));
    }
    catch(Exception e) //@B0C
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B0A
      {                                                //@B0A
        Trace.log(Trace.ERROR, "Failed to initialize AS400FileBeanInfo.", e); //@B0A
      }                                                //@B0A
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

}


