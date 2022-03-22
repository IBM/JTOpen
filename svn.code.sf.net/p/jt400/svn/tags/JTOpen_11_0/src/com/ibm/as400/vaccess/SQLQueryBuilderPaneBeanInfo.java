///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryBuilderPaneBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
The SQLQueryBuilderPaneBeanInfo class provides bean
information for the SQLQueryBuilderPane class.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class SQLQueryBuilderPaneBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = SQLQueryBuilderPane.class;

    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    static
    {
        try
        {
            // ***** EVENTS
            EventSetDescriptor error = new EventSetDescriptor(beanClass,
                         "error",
                         com.ibm.as400.vaccess.ErrorListener.class,
                         "errorOccurred");
            error.setDisplayName(ResourceLoader.getText("EVT_NAME_ERROR"));
            error.setShortDescription(ResourceLoader.getText("EVT_DESC_ERROR"));

            EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                         "propertyChange",
                         java.beans.PropertyChangeListener.class,
                         "propertyChange");
            changed.setDisplayName(ResourceLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(ResourceLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor veto = new EventSetDescriptor(beanClass,
            		"propertyChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
            veto.setDisplayName(ResourceLoader.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(ResourceLoader.getText("EVT_DESC_PROPERTY_VETO"));

            events_ = new EventSetDescriptor[]{error, changed, veto};


            // ***** PROPERTIES
            PropertyDescriptor connection = new PropertyDescriptor("connection", beanClass);
            connection.setBound(true);
            connection.setConstrained(true);
            connection.setDisplayName(ResourceLoader.getText("PROP_NAME_CONNECTION"));
            connection.setShortDescription(ResourceLoader.getText("PROP_DESC_CONNECTION"));

            PropertyDescriptor tables = new PropertyDescriptor("tables", beanClass);
            tables.setBound(true);
            tables.setConstrained(true);
            tables.setDisplayName(ResourceLoader.getText("PROP_NAME_TABLES"));
            tables.setShortDescription(ResourceLoader.getText("PROP_DESC_TABLES"));

            PropertyDescriptor select = new PropertyDescriptor("userSelectTables", beanClass);
            select.setBound(true);
            select.setConstrained(true);
            select.setDisplayName(ResourceLoader.getText("PROP_NAME_USER_SET_TABLES"));
            select.setShortDescription(ResourceLoader.getText("PROP_DESC_USER_SET_TABLES"));

            PropertyDescriptor libraries = new PropertyDescriptor("tableSchemas", beanClass);
            libraries.setBound(true);
            libraries.setConstrained(true);
            libraries.setDisplayName(ResourceLoader.getText("PROP_NAME_TABLE_SCHEMAS"));
            libraries.setShortDescription(ResourceLoader.getText("PROP_DESC_TABLE_SCHEMAS"));

            PropertyDescriptor select2 = new PropertyDescriptor("userSelectTableSchemas", beanClass);
            select2.setBound(true);
            select2.setConstrained(true);
            select2.setDisplayName(ResourceLoader.getText("PROP_NAME_USER_SET_SCHEMAS"));
            select2.setShortDescription(ResourceLoader.getText("PROP_DESC_USER_SET_SCHEMAS"));

            properties_ = new PropertyDescriptor[]{connection, tables, select, libraries, select2};
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
        // the index for the error event
        return 0;
    }


    /**
    Returns the index of the default property.
    @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        // the index for the "query" property
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
      * Returns an Image for this bean's icon.
      * @param icon The desired icon size and color.
      * @return The Image for the icon.
      **/
    public Image getIcon(int icon)
    {
        Image image = null;

        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage("SQLQueryBuilderPane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("SQLQueryBuilderPane32.gif");
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
