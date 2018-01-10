///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCDataSourcePaneBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.awt.Image;


/**
The AS400JDBCDataSourcePaneBeanInfo class provides the bean information
for the AS400JDBCDataSourcePane class.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400JDBCDataSourcePaneBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = AS400JDBCDataSourcePane.class;

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
            changed.setDisplayName(ResourceLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(ResourceLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor[] events = {changed};

            events_ = events;

            PropertyDescriptor dataSource = new PropertyDescriptor("dataSource", beanClass);
            dataSource.setBound(true);
            dataSource.setConstrained(false);
            dataSource.setDisplayName(ResourceLoader.getText("PROP_NAME_DATASOURCE"));
            dataSource.setShortDescription(ResourceLoader.getText("PROP_DESC_DATASOURCE"));

            PropertyDescriptor properties[] = {dataSource}; 

            properties_ = properties;
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
        // the index for the actionCompleted event
        return 0;
    }


    /**
    Returns the index of the default property.
    @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        // the index for the "command" property
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
    Returns the descriptors for all properties.
    @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
       return properties_;
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
                image = loadImage("AS400JDBCDataSourcePane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("AS400JDBCDataSourcePane32.gif");
                break;
        }

        return image;
    }

}

