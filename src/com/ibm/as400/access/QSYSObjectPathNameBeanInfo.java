///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QSYSObjectPathNameBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.awt.Image;


/**
The QSYSObjectPathNameBeanInfo class provides BeanInfo for QSYSObjectPathName class.
**/
public class QSYSObjectPathNameBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Class this bean info represents.
private final static Class beanClass = QSYSObjectPathName.class;

// Handles loading the appropriate resource bundle
private static ResourceBundleLoader loader_;

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
        changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
        changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

        EventSetDescriptor veto = new EventSetDescriptor(beanClass,
                    "propertyVeto",
                    java.beans.VetoableChangeListener.class,
                    "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        EventSetDescriptor[] events = {changed, veto};

        events_ = events;

        PropertyDescriptor path = new PropertyDescriptor("path", beanClass);
        path.setBound(true);
        path.setConstrained(true);
        path.setDisplayName(loader_.getText("PROP_NAME_PATH"));
        path.setShortDescription(loader_.getText("PROP_DESC_PATH"));

        PropertyDescriptor library = new PropertyDescriptor("libraryName", beanClass);
        library.setBound(true);
        library.setConstrained(true);
        library.setDisplayName(loader_.getText("PROP_NAME_LIBRARY"));
        library.setShortDescription(loader_.getText("PROP_DESC_LIBRARY"));

        PropertyDescriptor object = new PropertyDescriptor("objectName", beanClass);
        object.setBound(true);
        object.setConstrained(true);
        object.setDisplayName(loader_.getText("PROP_NAME_OBJECT"));
        object.setShortDescription(loader_.getText("PROP_DESC_OBJECT"));

        PropertyDescriptor member = new PropertyDescriptor("memberName", beanClass);
        member.setBound(true);
        member.setConstrained(true);
        member.setDisplayName(loader_.getText("PROP_NAME_MEMBER"));
        member.setShortDescription(loader_.getText("PROP_DESC_MEMBER"));

        PropertyDescriptor type = new PropertyDescriptor("objectType", beanClass);
        type.setBound(true);
        type.setConstrained(true);
        type.setDisplayName(loader_.getText("PROP_NAME_TYPE"));
        type.setShortDescription(loader_.getText("PROP_DESC_TYPE"));

        PropertyDescriptor properties[] = {path, library, object, member, type};

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


// Returns the copyright.
private static String getCopyright()
{
    return Copyright.copyright;
}


/**
Returns the index of the default event.
@return The index to the default event.
**/
public int getDefaultEventIndex()
{
    // the index for the property change event
    return 0;
}


/**
Returns the index of the default property.
@return The index to the default property.
**/
public int getDefaultPropertyIndex()
{
    // the index for the "path" property
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
                image = loadImage("QSYSObjectPathName16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("QSYSObjectPathName32.gif");
                break;
        }

        return image;
    }

}

