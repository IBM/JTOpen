///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BaseDataQueueBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
 The BaseDataQueueBeanInfo class provides bean information for the BaseDataQueue class.
 **/
public class BaseDataQueueBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class BEAN_CLASS = BaseDataQueue.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor dataQueue = new EventSetDescriptor(BEAN_CLASS, "dataQueue", DataQueueListener.class, new String[] { "cleared", "peeked", "read", "written" }, "addDataQueueListener", "removeDataQueueListener");
            dataQueue.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_DQ_DATA_EVENT"));
            dataQueue.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_DQ_DATA_EVENT"));

            EventSetDescriptor object = new EventSetDescriptor(BEAN_CLASS, "object", ObjectListener.class, new String[] { "objectCreated", "objectDeleted", "objectOpened" }, "addObjectListener", "removeObjectListener");
            object.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_DQ_OBJECT_EVENT"));
            object.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_DQ_OBJECT_EVENT"));

            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { dataQueue, object, propertyChange, vetoableChange };

            PropertyDescriptor path = new PropertyDescriptor("path", BEAN_CLASS);
            path.setBound(true);
            path.setConstrained(true);
            path.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_PATH"));
            path.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_PATH"));

            PropertyDescriptor name = new PropertyDescriptor("name", BEAN_CLASS, "getName", null);
            name.setBound(false);
            name.setConstrained(false);
            name.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_NAME"));
            name.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_NAME"));

            PropertyDescriptor system = new PropertyDescriptor("system", BEAN_CLASS);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SYSTEM"));
            system.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SYSTEM"));

            PropertyDescriptor ccsid = new PropertyDescriptor("ccsid", BEAN_CLASS, "getCcsid", null);
            ccsid.setBound(true);
            ccsid.setConstrained(true);
            ccsid.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AS400_CCSID"));
            ccsid.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AS400_CCSID"));

            propertyDescriptors = new PropertyDescriptor[] { path, name, system, ccsid };
        }
        catch (IntrospectionException e)
        {
            Trace.log(Trace.ERROR, "Unexpected IntrospectionException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Returns the bean descriptor.
     @return  The bean descriptor.
     **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(BEAN_CLASS);
    }

    /**
     Returns the index of the default event.
     @return  Zero (0), the index to the default event.
     **/
    public int getDefaultEventIndex()
    {
        // The index for the "dataQueue" event.
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "path" property.
        return 0;
    }

    /**
     Returns the descriptors for all events.
     @return  The descriptors for all events.
     **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return eventSetDescriptors;
    }

    /**
     Returns the descriptors for all properties.
     @return  The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }
}
