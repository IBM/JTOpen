///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCRowSetBeanInfo.java
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
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
*  The AS400JDBCRowSetBeanInfo class provides bean information for the AS400JDBCRowSet class.
**/
public class AS400JDBCRowSetBeanInfo extends SimpleBeanInfo
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


   // Class this bean info represents.
   private final static Class beanClass = AS400JDBCRowSet.class;

   // Handles loading the appropriate resource bundle
   // private static ResourceBundleLoader loader_;

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
       changed.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
       changed.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

       EventSetDescriptor[] events = { changed };

       events_ = events;


       // ***** PROPERTIES
       PropertyDescriptor command = new PropertyDescriptor("command", beanClass,
                                       "getCommand", "setCommand");
       command.setBound(true);
       command.setConstrained(false);
       command.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_COMMAND"));
       command.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_COMMAND"));

       PropertyDescriptor concurrency = new PropertyDescriptor("concurrency", beanClass,
                                       "getConcurrency", "setConcurrency");
       concurrency.setBound(true);
       concurrency.setConstrained(false);
       concurrency.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_CONCURRENCY"));
       concurrency.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_CONCURRENCY"));

       PropertyDescriptor dataSourceName = new PropertyDescriptor("dataSourceName", beanClass,
                                       "getDataSourceName", "setDataSourceName");
       dataSourceName.setBound(true);
       dataSourceName.setConstrained(false);
       dataSourceName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATASOURCE_NAME"));
       dataSourceName.setShortDescription(AS400JDBCDriver.getResource("DATASOURCE_NAME_DESC"));

       PropertyDescriptor escapeProcessing = new PropertyDescriptor("escapeProcessing", beanClass,
                                       "getEscapeProcessing", "setEscapeProcessing");
       escapeProcessing.setBound(true);
       escapeProcessing.setConstrained(false);
       escapeProcessing.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_ESCAPE_PROCESSING"));
       escapeProcessing.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_ESCAPE_PROCESSING"));

       PropertyDescriptor fetchDirection = new PropertyDescriptor("fetchDirection", beanClass,
                                       "getFetchDirection", "setFetchDirection");
       fetchDirection.setBound(true);
       fetchDirection.setConstrained(false);
       fetchDirection.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_FETCH_DIRECTION"));
       fetchDirection.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_FETCH_DIRECTION"));

       PropertyDescriptor fetchSize = new PropertyDescriptor("fetchSize", beanClass,
                                       "getFetchSize", "setFetchSize");
       fetchSize.setBound(true);
       fetchSize.setConstrained(false);
       fetchSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_FETCH_SIZE"));
       fetchSize.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_FETCH_SIZE"));

       PropertyDescriptor maxFieldSize = new PropertyDescriptor("maxFieldSize", beanClass,
                                       "getMaxFieldSize", "setMaxFieldSize");
       maxFieldSize.setBound(true);
       maxFieldSize.setConstrained(false);
       maxFieldSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_MAX_FIELD_SIZE"));
       maxFieldSize.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_MAX_FIELD_SIZE"));

       PropertyDescriptor maxRows = new PropertyDescriptor("maxRows", beanClass,
                                       "getMaxRows", "setMaxRows");
       maxRows.setBound(true);
       maxRows.setConstrained(false);
       maxRows.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_MAX_ROWS"));
       maxRows.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_MAX_ROWS"));

       PropertyDescriptor password = new PropertyDescriptor("password", beanClass,
                                       "getPassword", "setPassword");
       password.setBound(true);
       password.setConstrained(false);
       password.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PASSWORD"));
       password.setShortDescription(AS400JDBCDriver.getResource("PASSWORD_DESC"));

       PropertyDescriptor queryTimeout = new PropertyDescriptor("queryTimeout", beanClass,
                                       "getQueryTimeout", "setQueryTimeout");
       queryTimeout.setBound(true);
       queryTimeout.setConstrained(false);
       queryTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_QUERY_TIMEOUT"));
       queryTimeout.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_QUERY_TIMEOUT"));

       PropertyDescriptor readOnly = new PropertyDescriptor("readOnly", beanClass,
                                       "isReadOnly", "setReadOnly");
       readOnly.setBound(true);
       readOnly.setConstrained(false);
       readOnly.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_READ_ONLY"));
       readOnly.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_READ_ONLY"));

       PropertyDescriptor transactionIsolation = new PropertyDescriptor("transactionIsolation", beanClass,
                                       "getTransactionIsolation", "setTransactionIsolation");
       transactionIsolation.setBound(true);
       transactionIsolation.setConstrained(false);
       transactionIsolation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSACTION_ISOLATION"));
       transactionIsolation.setShortDescription(AS400JDBCDriver.getResource("TRANSACTION_ISOLATION_DESC"));

       PropertyDescriptor type = new PropertyDescriptor("type", beanClass,
                                       "getType", "setType");
       type.setBound(true);
       type.setConstrained(false);
       type.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_TYPE"));
       type.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_TYPE"));

       PropertyDescriptor url = new PropertyDescriptor("url", beanClass,
                                       "getUrl", "setUrl");
       url.setBound(true);
       url.setConstrained(false);
       url.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_URL"));
       url.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_URL"));

       PropertyDescriptor useDataSource = new PropertyDescriptor("useDataSource", beanClass,
                                       "isUseDataSource", "setUseDataSource");
       useDataSource.setBound(true);
       useDataSource.setConstrained(false);
       useDataSource.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_USE_DATA_SOURCE"));
       useDataSource.setShortDescription(AS400JDBCDriver.getResource("PROP_DESC_RS_USE_DATA_SOURCE"));

       PropertyDescriptor username = new PropertyDescriptor("username", beanClass,
                                       "getUsername", "setUsername");
       username.setBound(true);
       username.setConstrained(false);
       username.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RS_USERNAME"));
       username.setShortDescription(AS400JDBCDriver.getResource("USER_DESC"));

       properties_ = new PropertyDescriptor[] { command, concurrency, dataSourceName, escapeProcessing, fetchDirection, 
          fetchSize, maxFieldSize, maxRows, password, queryTimeout, readOnly, transactionIsolation, type, 
          url, useDataSource, username };

     }
     catch (Exception e)
     {
       throw new Error(e.toString());
     }
   }

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
     Returns the descriptors for all events.
     @return The descriptors for all events.
   **/
   public EventSetDescriptor[] getEventSetDescriptors()
   {
       return events_;
   }

   /**
     Returns an image for the icon.

     @param icon    The icon size and color.
     @return        The image.
   **/
   public Image getIcon (int icon)
   {
       Image image = null;
       switch (icon) {
           case BeanInfo.ICON_MONO_16x16:
           case BeanInfo.ICON_COLOR_16x16:
               image = loadImage ("AS400JDBCRowSet16.gif");
               break;
           case BeanInfo.ICON_MONO_32x32:
           case BeanInfo.ICON_COLOR_32x32:
               image = loadImage ("AS400JDBCRowSet32.gif");
               break;
       }
       return image;
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
