///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileHandleCredentialBeanInfo.java
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
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
/**
 * Bean information for the ProfileHandleCredential class.
 *
 */
public class ProfileHandleCredentialBeanInfo extends SimpleBeanInfo
{
   private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
   // Class this bean info represents.
   private final static Class beanClass = ProfileHandleCredential.class;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader_a loader_;

   private static PropertyDescriptor[] properties_;

   static 
   {
      try
      {
         // ***** PROPERTIES
         PropertyDescriptor handle = new PropertyDescriptor("handle", beanClass);
         handle.setBound(true);
         handle.setConstrained(true);
         handle.setDisplayName(loader_.getText("PROP_NAME_CR_PH_HANDLE"));
         handle.setShortDescription(loader_.getText("PROP_DESC_CR_PH_HANDLE"));

         properties_ = new PropertyDescriptor[] { handle };
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
            image = loadImage("ProfileHandleCredential16.gif");
            break;
         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
            image = loadImage("ProfileHandleCredential32.gif");
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
