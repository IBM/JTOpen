///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UserProfilePrincipalBeanInfo.java
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
 * Bean information for the UserProfilePrincipal class.
 *
 */
public class UserProfilePrincipalBeanInfo extends SimpleBeanInfo
{
   private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
   // Class this bean info represents.
   private final static Class beanClass = UserProfilePrincipal.class;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader_a loader_;

   private static PropertyDescriptor[] properties_;

   static 
   {
      try
      {
         // ***** PROPERTIES
         PropertyDescriptor userProfileName = new PropertyDescriptor("userProfileName", beanClass);
         userProfileName.setBound(true);
         userProfileName.setConstrained(true);
         userProfileName.setDisplayName(loader_.getText("PROP_NAME_PR_USERPROFILENAME"));
         userProfileName.setShortDescription(loader_.getText("PROP_DESC_PR_USERPROFILENAME"));

         properties_ = new PropertyDescriptor[] { userProfileName};
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
      return new BeanInfo[] { new AS400PrincipalBeanInfo()};
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
            image = loadImage("UserProfilePrincipal16.gif");
            break;
         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
            image = loadImage("UserProfilePrincipal32.gif");
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
