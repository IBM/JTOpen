///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConnectionPoolBeanInfo.java
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
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
*  The ConnectionPoolBeanInfo class provides bean information
*  for the ConnectionPool class.
**/
public class ConnectionPoolBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   // Class this bean info represents.
   private final static Class beanClass = ConnectionPool.class;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader loader_;
   private static EventSetDescriptor[] events_;
   private static PropertyDescriptor[] properties_;

   static
   {
     try
     {
       EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                                                           "propertyChange",
                                                           java.beans.PropertyChangeListener.class,
                                                           "propertyChange");
       changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
       changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

       EventSetDescriptor[] events = { changed };

       events_ = events;

       // ***** PROPERTIES
       PropertyDescriptor runMaintenance = new PropertyDescriptor("runMaintenance", beanClass,
                                           "isRunMaintenance", "setRunMaintenance");
       runMaintenance.setBound(true);
       runMaintenance.setConstrained(false);
       runMaintenance.setDisplayName(loader_.getText("PROP_NAME_CP_RUN_MAINTENANCE"));
       runMaintenance.setShortDescription(loader_.getText("PROP_DESC_CP_RUN_MAINTENANCE"));

       PropertyDescriptor threadUsed = new PropertyDescriptor("threadUsed", beanClass,
                                           "isThreadUsed", "setThreadUsed");
       threadUsed.setBound(true);
       threadUsed.setConstrained(false);
       threadUsed.setDisplayName(loader_.getText("PROP_NAME_CP_THREAD_USED"));
       threadUsed.setShortDescription(loader_.getText("PROP_DESC_CP_THREAD_USED"));

       properties_ = new PropertyDescriptor[] { runMaintenance, threadUsed };
     }
     catch (Exception e)
     {
       throw new Error(e.toString());
     }
   }

   //@A1D public BeanInfo[] getAdditionalBeanInfo()
   //@A1D {
   //@A1D   return new BeanInfo[] { new ConnectionPoolPropertiesBeanInfo() };
   //@A1D}

   /**
     Returns the bean descriptor.
     @return The bean descriptor.
   **/
   public BeanDescriptor getBeanDescriptor()
   {
       return new BeanDescriptor(beanClass);
   }


   /**
     Returns the index of the default event.
     @return The index to the default event.
   **/
   public int getDefaultEventIndex()
   {
       // the index for the propertyChange event
       return 0;
   }

   /**
     Returns the index of the default property.
     @return The index to the default property.
   **/
   public int getDefaultPropertyIndex()
   {
       // the index for the "path" property
       return 0;
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
     Returns the descriptors for all properties.
     @return The descriptors for all properties.
   **/
   public PropertyDescriptor[] getPropertyDescriptors()
   {
       return properties_;
   }
}
