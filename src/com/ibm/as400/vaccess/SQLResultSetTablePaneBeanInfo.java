///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLResultSetTablePaneBeanInfo.java
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
The SQLResultSetTablePaneBeanInfo class provides bean
information for the SQLResultSetTablePane class.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class SQLResultSetTablePaneBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = SQLResultSetTablePane.class;

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

            EventSetDescriptor select = new EventSetDescriptor(beanClass,
                         "listSelection",
                         javax.swing.event.ListSelectionListener.class,
                         "valueChanged");
            select.setDisplayName(ResourceLoader.getText("EVT_NAME_LIST_SELECTION"));
            select.setShortDescription(ResourceLoader.getText("EVT_DESC_LIST_SELECTION"));

            events_ = new EventSetDescriptor[]{error, changed, veto, select};


            // ***** PROPERTIES
            PropertyDescriptor query = new PropertyDescriptor("query", beanClass);
            query.setBound(true);
            query.setConstrained(true);
            query.setDisplayName(ResourceLoader.getText("PROP_NAME_QUERY"));
            query.setShortDescription(ResourceLoader.getText("PROP_DESC_QUERY"));

            PropertyDescriptor connection = new PropertyDescriptor("connection", beanClass);
            connection.setBound(true);
            connection.setConstrained(true);
            connection.setDisplayName(ResourceLoader.getText("PROP_NAME_CONNECTION"));
            connection.setShortDescription(ResourceLoader.getText("PROP_DESC_CONNECTION"));

            PropertyDescriptor gridColor = new PropertyDescriptor("gridColor", beanClass);
            gridColor.setBound(false);
            gridColor.setConstrained(false);
            gridColor.setExpert(true);
            gridColor.setDisplayName(ResourceLoader.getText("PROP_NAME_GRID_COLOR"));
            gridColor.setShortDescription(ResourceLoader.getText("PROP_DESC_GRID_COLOR"));

            PropertyDescriptor showHorizontal = new PropertyDescriptor("showHorizontalLines", beanClass);
            showHorizontal.setBound(false);
            showHorizontal.setConstrained(false);
            showHorizontal.setExpert(true);
            showHorizontal.setDisplayName(ResourceLoader.getText("PROP_NAME_SHOW_H_LINES"));
            showHorizontal.setShortDescription(ResourceLoader.getText("PROP_DESC_SHOW_H_LINES"));

            PropertyDescriptor showVertical = new PropertyDescriptor("showVerticalLines", beanClass);
            showVertical.setBound(false);
            showVertical.setConstrained(false);
            showVertical.setExpert(true);
            showVertical.setDisplayName(ResourceLoader.getText("PROP_NAME_SHOW_V_LINES"));
            showVertical.setShortDescription(ResourceLoader.getText("PROP_DESC_SHOW_V_LINES"));

            properties_ = new PropertyDescriptor[]{query, connection,
                           gridColor, showHorizontal, showVertical};
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
                image = loadImage("SQLResultSetTablePane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("SQLResultSetTablePane32.gif");
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
