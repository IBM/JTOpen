
////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenCredentialBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.security.auth;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.IntrospectionException;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
/**
 * Bean information for the ProfileTokenCredential class.
 *
 */
public class ProfileTokenCredentialBeanInfo extends SimpleBeanInfo
{
   // Class this bean info represents.
   private final static Class beanClass = ProfileTokenCredential.class;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader_a loader_;

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
         changed.setDisplayName(loader_.getAccessText("EVT_NAME_PROPERTY_CHANGE"));
         changed.setShortDescription(loader_.getAccessText("EVT_DESC_PROPERTY_CHANGE"));

         EventSetDescriptor veto = new EventSetDescriptor(beanClass,
                                                          "vetoableChange",
                                                          java.beans.VetoableChangeListener.class,
                                                          "vetoableChange");
         veto.setDisplayName(loader_.getAccessText("EVT_NAME_PROPERTY_VETO"));
         veto.setShortDescription(loader_.getAccessText("EVT_DESC_PROPERTY_VETO"));

         EventSetDescriptor[] events = {changed, veto};

         events_ = events;

         // ***** PROPERTIES
         PropertyDescriptor token = new PropertyDescriptor("token", beanClass,
                                                           "getToken", "setToken");
         token.setBound(true);
         token.setConstrained(true);
         token.setDisplayName(loader_.getText("PROP_NAME_CR_PT_TOKEN"));
         token.setShortDescription(loader_.getText("PROP_DESC_CR_PT_TOKEN"));

         PropertyDescriptor type = new PropertyDescriptor("tokenType", beanClass,
                                                          "getTokenType", "setTokenType");
         type.setBound(true);
         type.setConstrained(true);
         type.setDisplayName(loader_.getText("PROP_NAME_CR_PT_TYPE"));
         type.setShortDescription(loader_.getText("PROP_DESC_CR_PT_TYPE"));

         PropertyDescriptor timeoutInterval = new PropertyDescriptor("timeoutInterval", beanClass,
                                                                     "getTimeoutInterval", "setTimeoutInterval");
         timeoutInterval.setBound(true);
         timeoutInterval.setConstrained(true);
         timeoutInterval.setDisplayName(loader_.getText("PROP_NAME_CR_PT_TIMEOUTINTERVAL"));
         timeoutInterval.setShortDescription(loader_.getText("PROP_DESC_CR_PT_TIMEOUTINTERVAL"));

         properties_ = new PropertyDescriptor[] { token, type, timeoutInterval};
      }
      catch ( Exception e )
      {
         AuthenticationSystem.handleUnexpectedException(e);
      }

   }


   /**
    * Returns additional bean information.
    *
    * @return
    *		The bean information.
    *
    */
   public BeanInfo[] getAdditionalBeanInfo()
   {
      return new BeanInfo[] { new AS400CredentialBeanInfo()};
   }


   /**
    * Returns the bean descriptor.
    *
    * @return The bean descriptor.
    *
    */
   public BeanDescriptor getBeanDescriptor()
   {
      return new BeanDescriptor(beanClass);
   }


   /**
    * Returns the descriptors for all events.
    *
    * @return The descriptors for all events.
    *
    */
   public EventSetDescriptor[] getEventSetDescriptors()
   {
      return events_;
   }


   /**
    * Returns an Image for this bean's icon.
    *
    * @param icon The desired icon size and color.
    *
    * @return The Image for the icon.
    *
    */
   public Image getIcon(int icon)
   {
      Image image = null;
      switch ( icon )
      {
         case BeanInfo.ICON_MONO_16x16:
         case BeanInfo.ICON_COLOR_16x16:
            image = loadImage("ProfileTokenCredential16.gif");
            break;
         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
            image = loadImage("ProfileTokenCredential32.gif");
            break;
      }
      return image;
   }


   /**
    * Returns the descriptors for all properties.
    *
    * @return The descriptors for all properties.
    *
    */
   public PropertyDescriptor[] getPropertyDescriptors()
   {
      return properties_;
   }

}
