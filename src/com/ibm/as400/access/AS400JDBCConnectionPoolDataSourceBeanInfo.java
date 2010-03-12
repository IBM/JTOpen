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
import java.beans.SimpleBeanInfo;


/**
 * The AS400JDBCConnectionPoolDataSourceBeanInfo class provides bean information
 * for the AS400JDBCConnectionPoolDataSource class.
**/
public class AS400JDBCConnectionPoolDataSourceBeanInfo extends SimpleBeanInfo
{
  static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";


  // Class this bean info represents.
    private static final Class beanClass = AS400JDBCConnectionPoolDataSource.class;

    //@B1D private static PropertyDescriptor[] propertyDescriptors_; //@B0A
    //@B1D private static final BeanDescriptor beanDescriptor_; //@B0A
    //@B1D private static final BeanInfo[] additionalBeanInfo_; //@B0A


  //@B0A
    //@B1D static
    //@B1D {
    //@B1D   try
    //@B1D   {
    //@B1D     beanDescriptor_ = new BeanDescriptor(beanClass_);
    //@B1D     additionalBeanInfo_ = new BeanInfo[] { new AS400JDBCDataSourceBeanInfo()};

    //@B1D PropertyDescriptor initialPoolSize = new PropertyDescriptor("initialPoolSize", beanClass_);
    //@B1D initialPoolSize.setBound(true);
    //@B1D initialPoolSize.setConstrained(false);
    //@B1D initialPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_INIT_POOL_SIZE"));
    //@B1D initialPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_INIT_POOL_SIZE"));

    //@B1D PropertyDescriptor maxIdleTime = new PropertyDescriptor("maxIdleTime", beanClass_);
    //@B1D maxIdleTime.setBound(true); 
    //@B1D maxIdleTime.setConstrained(false);
    //@B1D maxIdleTime.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_IDLE_TIME"));
    //@B1D maxIdleTime.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_IDLE_TIME"));

    //@B1D PropertyDescriptor maxPoolSize = new PropertyDescriptor("maxPoolSize", beanClass_);
    //@B1D maxPoolSize.setBound(true);
    //@B1D maxPoolSize.setConstrained(false);
    //@B1D maxPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_POOL_SIZE"));
    //@B1D maxPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_POOL_SIZE"));

//@B1D // Note: We are currently not implementing statement pooling this way,
//@B1D //       since we already have package caching.
//@B1D //      PropertyDescriptor maxStatements = new PropertyDescriptor("maxStatements", beanClass_);
//@B1D //      maxStatements.setBound(true);
//@B1D //      maxStatements.setConstrained(false);
//@B1D //      maxStatements.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MAX_STATEMENTS"));
//@B1D //      maxStatements.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MAX_STATEMENTS"));

    //@B1D PropertyDescriptor minPoolSize = new PropertyDescriptor("minPoolSize", beanClass_);
    //@B1D minPoolSize.setBound(true);
    //@B1D minPoolSize.setConstrained(false);
    //@B1D minPoolSize.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_MIN_POOL_SIZE"));
    //@B1D minPoolSize.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_MIN_POOL_SIZE"));

    //@B1D PropertyDescriptor propertyCycle = new PropertyDescriptor("propertyCycle", beanClass_);
    //@B1D propertyCycle.setBound(true);
    //@B1D propertyCycle.setConstrained(false);
    //@B1D propertyCycle.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_CPDS_PROP_CYCLE"));
    //@B1D propertyCycle.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_CPDS_PROP_CYCLE"));

    //@B1D propertyDescriptors_ = new PropertyDescriptor[]
    //@B1D {
    //@B1D   initialPoolSize, maxIdleTime, maxPoolSize, // maxStatements,
    //@B1D   minPoolSize, propertyCycle
    //@B1D };
    //@B1D   }
    //@B1D   catch(Exception e)
    //@B1D   {
    //@B1D     if(Trace.isTraceOn())
    //@B1D     {
    //@B1D       Trace.log(Trace.ERROR, "Error while loading bean info", e);
    //@B1D     }
    //@B1D     throw new Error(e.toString());
    //@B1D   }
    //@B1D }


  /**
   * Returns additional bean information from the AS400JDBCConnectionPoolDataSource superclass.
   * @return The bean information.
  **/
  public BeanInfo[] getAdditionalBeanInfo()
  {
        return new BeanInfo[] { new AS400JDBCDataSourceBeanInfo()};
        //@B1D return additionalBeanInfo_; //@B0A
    }


  /**
   * Returns the bean descriptor.
   * @return The bean descriptor.
  **/
  public BeanDescriptor getBeanDescriptor()
  {
        return new BeanDescriptor(beanClass);
        //@B1D return beanDescriptor_; //@B0A
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


    //@B1D /**
    //@B1D  * Returns the property descriptors.
    //@B1D  * @return The property descriptors.
    //@B1D  **/
    //@B1D public PropertyDescriptor[] getPropertyDescriptors()
    //@B1D {
    //@B1D   return propertyDescriptors_;
    //@B1D }
}
