///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListFormPaneBeanInfo.java
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
The RecordListFormPaneBeanInfo class provides bean
information for the RecordListFormPane class.
**/
public class RecordListFormPaneBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = RecordListFormPane.class;

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

            EventSetDescriptor action = new EventSetDescriptor(beanClass,
                         "actionCompleted",
                         com.ibm.as400.access.ActionCompletedListener.class,
                         "actionCompleted");
            action.setDisplayName(ResourceLoader.getText("EVT_NAME_ACTION_COMPLETED"));
            action.setShortDescription(ResourceLoader.getText("EVT_DESC_ACTION_COMPLETED"));

            EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                         "propertyChange",
                         java.beans.PropertyChangeListener.class,
                         "propertyChange");
            changed.setDisplayName(ResourceLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(ResourceLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor veto = new EventSetDescriptor(beanClass,
                         "vetoableChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
            veto.setDisplayName(ResourceLoader.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(ResourceLoader.getText("EVT_DESC_PROPERTY_VETO"));

            events_ = new EventSetDescriptor[]{error, action, changed, veto};


            // ***** PROPERTIES
            PropertyDescriptor fileName = new PropertyDescriptor("fileName", beanClass);
            fileName.setBound(true);
            fileName.setConstrained(true);
            fileName.setDisplayName(ResourceLoader.getText("PROP_NAME_FILE_NAME"));
            fileName.setShortDescription(ResourceLoader.getText("PROP_DESC_FILE_NAME"));

            PropertyDescriptor system = new PropertyDescriptor("system", beanClass);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceLoader.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor keyed = new PropertyDescriptor("keyed", beanClass);
            keyed.setBound(true);
            keyed.setConstrained(true);
            keyed.setDisplayName(ResourceLoader.getText("PROP_NAME_KEYED"));
            keyed.setShortDescription(ResourceLoader.getText("PROP_DESC_KEYED"));

            PropertyDescriptor key = new PropertyDescriptor("key", beanClass);
            key.setBound(true);
            key.setConstrained(true);
            key.setDisplayName(ResourceLoader.getText("PROP_NAME_KEY"));
            key.setShortDescription(ResourceLoader.getText("PROP_DESC_KEY"));

            PropertyDescriptor searchType = new PropertyDescriptor("searchType", beanClass);
            searchType.setBound(true);
            searchType.setConstrained(true);
            searchType.setDisplayName(ResourceLoader.getText("PROP_NAME_SEARCH_TYPE"));
            searchType.setShortDescription(ResourceLoader.getText("PROP_DESC_SEARCH_TYPE"));

            properties_ = new PropertyDescriptor[]{fileName, system, keyed, key, searchType};
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


    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright_v.copyright;
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
        // the index for the "fileName" property
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
                image = loadImage("RecordListFormPane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("RecordListFormPane32.gif");
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
