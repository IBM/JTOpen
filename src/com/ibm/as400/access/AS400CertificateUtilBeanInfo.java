///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateUtilBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;


/**
The AS400CertificateUtilBeanInfo class provides
bean information for the AS400CertificateUtil class.
**/
public class AS400CertificateUtilBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = AS400CertificateUtil.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;

    private static EventSetDescriptor[] events_;

    static PropertyDescriptor[] properties_;

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
                         "vetoableChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
            veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

         String[] certListeners = {"added",
                                   "deleted"};
            EventSetDescriptor cert = new EventSetDescriptor(beanClass,
                         "as400Certificate",
                         com.ibm.as400.access.AS400CertificateListener.class,
                         certListeners,
                         "addAS400CertificateListener",
                         "removeAS400CertificateListener");
            cert.setDisplayName(loader_.getText("EVT_NAME_AS400CERTIFICATE_EVENT"));
            cert.setShortDescription(loader_.getText("EVT_DESC_AS400CERTIFICATE_EVENT"));

            EventSetDescriptor[] events = {cert, changed, veto};

            events_ = events;

            // ***** PROPERTIES
            PropertyDescriptor path = new PropertyDescriptor("path", beanClass);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(loader_.getText("PROP_NAME_PATH"));
            path.setShortDescription(loader_.getText("PROP_DESC_PATH"));

            PropertyDescriptor name = new PropertyDescriptor("name", beanClass,
                                        "getName", null);
            name.setBound(false);
            name.setConstrained(false);
            name.setDisplayName(loader_.getText("PROP_NAME_NAME"));
            name.setShortDescription(loader_.getText("PROP_DESC_NAME"));

            PropertyDescriptor system = new PropertyDescriptor("system", beanClass);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(loader_.getText("PROP_NAME_SYSTEM"));
         system.setShortDescription(loader_.getText("PROP_DESC_SYSTEM"));

         properties_ = new PropertyDescriptor[]{path, name, system};

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
        // the index for the actionCompleted event
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
                image = loadImage("AS400CertificateUtil16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage("AS400CertificateUtil32.gif");
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

