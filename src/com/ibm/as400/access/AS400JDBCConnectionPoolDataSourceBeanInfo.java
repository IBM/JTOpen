///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionPoolDataSourceBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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
 * The AS400JDBCConnectionPoolDataSourceBeanInfo class provides bean information
 * for the AS400JDBCConnectionPoolDataSource class.
**/
public class AS400JDBCConnectionPoolDataSourceBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Class this bean info represents.
  private static final Class beanClass_ = AS400JDBCConnectionPoolDataSource.class;

  private static PropertyDescriptor[] propertyDescriptors_; //@B0A
  private static final BeanDescriptor beanDescriptor_; //@B0A
  private static final BeanInfo[] additionalBeanInfo_; //@B0A


  //@B0A
  static
  {
    try
    {
      beanDescriptor_ = new BeanDescriptor(beanClass_);
      additionalBeanInfo_ = new BeanInfo[] { new AS400JDBCDataSourceBeanInfo()};

      PropertyDescriptor initialPoolSize = new PropertyDescriptor("initialPoolSize", beanClass_);
      initialPoolSize.setBound(true);
      initialPoolSize.setConstrained(false);
      initialPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_INIT_POOL_SIZE"));
      initialPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_INIT_POOL_SIZE"));

      PropertyDescriptor maxIdleTime = new PropertyDescriptor("maxIdleTime", beanClass_);
      maxIdleTime.setBound(true); 
      maxIdleTime.setConstrained(false);
      maxIdleTime.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_IDLE_TIME"));
      maxIdleTime.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_IDLE_TIME"));

      PropertyDescriptor maxPoolSize = new PropertyDescriptor("maxPoolSize", beanClass_);
      maxPoolSize.setBound(true);
      maxPoolSize.setConstrained(false);
      maxPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_POOL_SIZE"));
      maxPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_POOL_SIZE"));

// Note: We are currently not implementing statement pooling this way,
//       since we already have package caching.
//      PropertyDescriptor maxStatements = new PropertyDescriptor("maxStatements", beanClass_);
//      maxStatements.setBound(true);
//      maxStatements.setConstrained(false);
//      maxStatements.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_STATEMENTS"));
//      maxStatements.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_STATEMENTS"));

      PropertyDescriptor minPoolSize = new PropertyDescriptor("minPoolSize", beanClass_);
      minPoolSize.setBound(true);
      minPoolSize.setConstrained(false);
      minPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MIN_POOL_SIZE"));
      minPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MIN_POOL_SIZE"));

      PropertyDescriptor propertyCycle = new PropertyDescriptor("propertyCycle", beanClass_);
      propertyCycle.setBound(true);
      propertyCycle.setConstrained(false);
      propertyCycle.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_PROP_CYCLE"));
      propertyCycle.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_PROP_CYCLE"));

      propertyDescriptors_ = new PropertyDescriptor[]
      {
        initialPoolSize, maxIdleTime, maxPoolSize, // maxStatements,
        minPoolSize, propertyCycle
      };
    }
    catch(Exception e)
    {
      if(Trace.isTraceOn())
      {
        Trace.log(Trace.ERROR, "Error while loading bean info", e);
      }
      throw new Error(e.toString());
    }
  }


  /**
   * Returns additional bean information from the AS400JDBCConnectionPoolDataSource superclass.
   * @return The bean information.
  **/
  public BeanInfo[] getAdditionalBeanInfo()
  {
    //@B0D return new BeanInfo[] { new AS400JDBCDataSourceBeanInfo() };
    return additionalBeanInfo_; //@B0A
  }

   
  /**
   * Returns the bean descriptor.
   * @return The bean descriptor.
  **/
  public BeanDescriptor getBeanDescriptor()
  {
    //@B0D return new BeanDescriptor(beanClass);
    return beanDescriptor_; //@B0A
  }

  
  /**
   * Returns an image for the icon.
   * @param icon The icon size and color.
   * @return The image.
  **/
  public Image getIcon(int icon)
  {
    Image image = null;
    switch(icon)
    {
      case BeanInfo.ICON_MONO_16x16:
      case BeanInfo.ICON_COLOR_16x16:
        image = loadImage ("AS400JDBCConnectionPoolDataSource16.gif");
        break;
      case BeanInfo.ICON_MONO_32x32:
      case BeanInfo.ICON_COLOR_32x32:
        image = loadImage ("AS400JDBCConnectionPoolDataSource32.gif");
        break;
    }
    return image;
  }


  /**
   * Returns the property descriptors.
   * @return The property descriptors.
   **/
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    return propertyDescriptors_;
  }
}
