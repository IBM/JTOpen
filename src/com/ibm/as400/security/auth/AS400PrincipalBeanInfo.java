///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400PrincipalBeanInfo.java
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
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;

/**
 * Bean information for the AS400Principal class.
 *
 */
public class AS400PrincipalBeanInfo extends SimpleBeanInfo
{
   // Class this bean info represents.
   private final static Class beanClass = AS400Principal.class;

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

         EventSetDescriptor[] events = { changed, veto};

         events_ = events;

         // ***** PROPERTIES
         PropertyDescriptor system = new PropertyDescriptor("system", beanClass, "getSystem", "setSystem");
         system.setBound(true);
         system.setConstrained(true);
         system.setDisplayName(loader_.getAccessText("PROP_NAME_SYSTEM"));
         system.setShortDescription(loader_.getAccessText("PROP_DESC_SYSTEM"));

         PropertyDescriptor name = new PropertyDescriptor("name", beanClass, "getName", null);
         name.setBound(false);
         name.setConstrained(false);
         name.setDisplayName(loader_.getText("PROP_NAME_PR_NAME"));
         name.setShortDescription(loader_.getText("PROP_DESC_PR_NAME"));

         PropertyDescriptor user = new PropertyDescriptor("user", beanClass, "getUser", null);
         user.setBound(false);
         user.setConstrained(false);
         user.setDisplayName(loader_.getText("PROP_NAME_PR_USER"));
         user.setShortDescription(loader_.getText("PROP_DESC_PR_USER"));
         user.setExpert(true);

         properties_ = new PropertyDescriptor[] { system, name, user};
      }
      catch ( Exception e )
      {
         AuthenticationSystem.handleUnexpectedException(e);
      }

   }


   /**
    * Returns the index of the default event.
    *
    * @return The index to the default event.
    *
    */
   public int getDefaultEventIndex()
   {
      return 0;
   }


   /**
    * Returns the index of the default property.
    *
    * @return The index to the default property.
    *
    */

   public int getDefaultPropertyIndex()
   {
      return 0;
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
    * <p> The superclass returns a generic image; subclasses
    * should override with specific images as appropriate.
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
            image = loadImage("Principal16.gif");
            break;
         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
            image = loadImage("Principal32.gif");
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
