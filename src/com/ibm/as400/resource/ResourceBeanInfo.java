///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.Trace;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;
import java.util.ResourceBundle;


/**
The ResourceBeanInfo class represents the bean information
for Resource objects.
**/
public class ResourceBeanInfo 
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.

            // Technically, this would belong in "ChangeableResourceBeanInfo",
            // but there was no need at this time to separate it.
    private static final Class                  beanClass_              = ChangeableResource.class;

    private static EventSetDescriptor[]         eventSetDescriptors_;
    private static PropertyDescriptor[]         propertyDescriptors_;
    private static ResourceBundle               resourceBundle_;



/**
Static initializer.
**/
    static
    {
        try
        {
            // Set up the resource bundle.
            resourceBundle_ = ResourceBundle.getBundle("com.ibm.as400.resource.ResourceMRI");

            // Set up the property descriptors.
            PropertyDescriptor system = new PropertyDescriptor("system", beanClass_);
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(resourceBundle_.getString("PROPERTY_SYSTEM_NAME"));
            system.setShortDescription(resourceBundle_.getString("PROPERTY_SYSTEM_DESCRIPTION"));

            propertyDescriptors_ = new PropertyDescriptor[] { system };

            // Set up the event set descriptors.
            EventSetDescriptor propertyChange = new EventSetDescriptor(beanClass_, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(resourceBundle_.getString("EVENT_PROPERTY_CHANGE_NAME"));
            propertyChange.setShortDescription(resourceBundle_.getString("EVENT_PROPERTY_CHANGE_DESCRIPTION"));

            // Technically, this would belong in "ChangeableResourceBeanInfo",
            // but there was no need at this time to separate it.
            EventSetDescriptor resource = new EventSetDescriptor(beanClass_, "resource", ResourceListener.class, 
                                                     new String[] { "attributeChangesCanceled", "attributeChangesCommitted", "attributeValuesRefreshed", "attributeValueChanged" },
                                                     "addResourceListener",
                                                     "removeResourceListener");
            resource.setDisplayName(resourceBundle_.getString("EVENT_RESOURCE_NAME"));
            resource.setShortDescription(resourceBundle_.getString("EVENT_RESOURCE_DESCRIPTION"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(beanClass_, "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(resourceBundle_.getString("EVENT_VETOABLE_CHANGE_NAME"));
            vetoableChange.setShortDescription(resourceBundle_.getString("EVENT_VETOABLE_CHANGE_DESCRIPTION"));

            eventSetDescriptors_ = new EventSetDescriptor[] { propertyChange, resource, vetoableChange };            
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error while loading bean info", e);
            throw new Error(e.toString());
        }
    }
    


/**
Returns the default event index.

@return The default event index.
**/
    public int getDefaultEventIndex()
    {
        return 0;
    }


    
/**
Returns the event set descriptors.

@return The event set descriptors.
**/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return eventSetDescriptors_;
    }



/**
Returns the property descriptors.

@return The property descriptors.
**/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
       return propertyDescriptors_;
    }



}

