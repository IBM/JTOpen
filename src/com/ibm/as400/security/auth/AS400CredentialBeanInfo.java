///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400CredentialBeanInfo.java
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
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

/**
 * Bean information for the AS400Credential class.
 *
 */
public class AS400CredentialBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
   // Class this bean info represents.
   private final static Class beanClass = AS400Credential.class;

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

         String[] crListeners = {"created", "destroyed", "refreshed", "swapped"};
         EventSetDescriptor cr = new EventSetDescriptor(beanClass, 
                                                        "as400Credential", 
                                                        AS400CredentialListener.class, 
                                                        crListeners, 
                                                        "addCredentialListener", 
                                                        "removeCredentialListener");
         cr.setDisplayName(loader_.getText("EVT_NAME_CR_EVENT"));
         cr.setShortDescription(loader_.getText("EVT_DESC_CR_EVENT"));

         events_ = new EventSetDescriptor[] {cr, changed, veto};
        
         // ***** PROPERTIES
         PropertyDescriptor current = new PropertyDescriptor("current", beanClass, "isCurrent", null);
         current.setBound(false);
         current.setConstrained(false);
         current.setDisplayName(loader_.getText("PROP_NAME_CR_CURRENT"));
         current.setShortDescription(loader_.getText("PROP_DESC_CR_CURRENT"));

         PropertyDescriptor destroyed = new PropertyDescriptor("destroyed", beanClass, "isDestroyed", null);
         destroyed.setBound(false);
         destroyed.setConstrained(false);
         destroyed.setDisplayName(loader_.getText("PROP_NAME_CR_DESTROYED"));
         destroyed.setShortDescription(loader_.getText("PROP_DESC_CR_DESTROYED"));

         PropertyDescriptor renewable = new PropertyDescriptor("renewable", beanClass, "isRenewable", null);
         renewable.setBound(false);
         renewable.setConstrained(false);
         renewable.setDisplayName(loader_.getText("PROP_NAME_CR_RENEWABLE"));
         renewable.setShortDescription(loader_.getText("PROP_DESC_CR_RENEWABLE"));
         renewable.setExpert(true);

         PropertyDescriptor timed = new PropertyDescriptor("timed", beanClass, "isTimed", null);
         timed.setBound(false);
         timed.setConstrained(false);
         timed.setDisplayName(loader_.getText("PROP_NAME_CR_TIMED"));
         timed.setShortDescription(loader_.getText("PROP_DESC_CR_TIMED"));
         timed.setExpert(true);

         PropertyDescriptor timeToExpiration = new PropertyDescriptor("timeToExpiration", beanClass, "getTimeToExpiration", null);
         timeToExpiration.setBound(false);
         timeToExpiration.setConstrained(false);
         timeToExpiration.setDisplayName(loader_.getText("PROP_NAME_CR_TIMETOEXPIRATION"));
         timeToExpiration.setShortDescription(loader_.getText("PROP_DESC_CR_TIMETOEXPIRATION"));

         PropertyDescriptor system = new PropertyDescriptor("system", beanClass, "getSystem", "setSystem");
         system.setBound(true);
         system.setConstrained(true);
         system.setDisplayName(loader_.getAccessText("PROP_NAME_SYSTEM"));
         system.setShortDescription(loader_.getAccessText("PROP_DESC_SYSTEM"));

         PropertyDescriptor principal = new PropertyDescriptor("principal", beanClass, "getPrincipal", "setPrincipal");
         principal.setBound(true);
         principal.setConstrained(true);
         principal.setDisplayName(loader_.getText("PROP_NAME_CR_PRINCIPAL"));
         principal.setShortDescription(loader_.getText("PROP_DESC_CR_PRINCIPAL"));
         principal.setExpert(true);

         properties_ = new PropertyDescriptor[] { system, principal, current, destroyed, renewable, timed, timeToExpiration };
      }
      catch ( Exception e )
      {
         AuthenticationSystem.handleUnexpectedException(e);
      }

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
            image = loadImage("Credential16.gif");
            break;
         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
            image = loadImage("Credential32.gif");
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
