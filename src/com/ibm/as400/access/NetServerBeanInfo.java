///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceBeanInfo;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ResourceBundle;



/**
The NetServerBeanInfo class represents the bean information
for the NetServer class.
**/
public class NetServerBeanInfo 
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  // Private data.
  private static final Class                  beanClass_              = NetServer.class;

  private static BeanInfo[]                   additionalBeanInfo_;
  private static BeanDescriptor               beanDescriptor_;
  private static Image                        icon16_;
  private static Image                        icon32_;
  private static PropertyDescriptor[]         propertyDescriptors_;
  private static ResourceBundle               resourceBundle_;



  /**
   Static initializer.
   **/
  static
  {
    try
    {
      // Set up the resource bundle.
      resourceBundle_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI2");

      // Set up the additional bean info.
      additionalBeanInfo_ = new BeanInfo[] { new ResourceBeanInfo() };

      // Set up the bean descriptor.
      beanDescriptor_ = new BeanDescriptor(beanClass_);

      // Note: The NetServer class has no unique properties.
      propertyDescriptors_ = new PropertyDescriptor[0];

    }
    catch (Exception e)
    {
      if (Trace.isTraceOn())
        Trace.log(Trace.ERROR, "Error while loading bean info", e);
      throw new Error(e.toString());
    }
  }



  /**
   Returns the additional bean information.

   @return The additional bean information.
   **/
  public BeanInfo[] getAdditionalBeanInfo()
  {
    return additionalBeanInfo_;
  }


  /**
   Returns the bean descriptor.

   @return The bean descriptor.
   **/
  public BeanDescriptor getBeanDescriptor()
  {
    return beanDescriptor_;
  }


  /**
   Returns the index of the default event.      
   @return The index to the default event.
   **/    
  public int getDefaultEventIndex()    
  {        
    return 0;    
  }    



  /**
   Returns the index of the default property.      
   @return The index to the default property.    
   **/
  public int getDefaultPropertyIndex()    
  {        
    return 0;    
  }



  /**
   Returns the property descriptors.

   @return The property descriptors.
   **/
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    return propertyDescriptors_;
  }



/// TBD - No icons yet.  Revisit this in the next release.
////**
///Returns the icon.
///
///@param iconKind The icon kind.  Possible values are:
///                <ul>
///                <li>BeanInfo.ICON_MONO_16x16
///                <li>BeanInfo.ICON_MONO_32x32
///                <li>BeanInfo.ICON_COLOR_16x16
///                <li>BeanInfo.ICON_COLOR_32x32
///                </ul>
///@return         The icon.                
///**/
///    public Image getIcon(int icon)
///    {
///        switch(icon)
///        {
///            case BeanInfo.ICON_MONO_16x16:
///            case BeanInfo.ICON_COLOR_16x16:
///                if (icon16_ == null)
///                    icon16_ = loadImage("NetServer16.gif");
///                return icon16_;
///
///            case BeanInfo.ICON_MONO_32x32:
///            case BeanInfo.ICON_COLOR_32x32:
///                if (icon32_ == null)
///                    icon32_ = loadImage("NetServer32.gif");
///                return icon32_;
///
///            default:
///            throw new ExtendedIllegalArgumentException("icon", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
///        }
///    }

}

