///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPrinterOutputBeanInfo.java
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
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
The VPrinterOutputBeanInfo class provides bean information
for the VPrinterOutput class.

@see VPrinterOutput
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VPrinterOutputBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class              beanClass_      = VPrinterOutput.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;

/**
Static initializer.
**/
    static
    {
        try
        {
            // Events.
            EventSetDescriptor error = new EventSetDescriptor (beanClass_,
                                                               "error", ErrorListener.class, "errorOccurred");
            error.setDisplayName (ResourceLoader.getText ("EVT_NAME_ERROR"));
            error.setShortDescription (ResourceLoader.getText ("EVT_DESC_ERROR"));

            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                                                                        "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor (beanClass_,
            		"propertyChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_VETO"));

            String[] vobjectMethods = { "objectChanged", "objectDeleted" };
            EventSetDescriptor vobject = new EventSetDescriptor (beanClass_,
                                                                 "vobject", VObjectListener.class, vobjectMethods,
                                                                 "addVObjectListener", "removeVObjectListener");
            vobject.setDisplayName (ResourceLoader.getText ("EVT_NAME_VOBJECT"));
            vobject.setShortDescription (ResourceLoader.getText ("EVT_DESC_VOBJECT"));

            String[] workingMethods = { "startWorking", "stopWorking" };
            EventSetDescriptor working = new EventSetDescriptor (beanClass_,
                                                                 "working", WorkingListener.class, workingMethods,
                                                                 "addWorkingListener", "removeWorkingListener");
            working.setDisplayName (ResourceLoader.getText ("EVT_NAME_WORKING"));
            working.setShortDescription (ResourceLoader.getText ("EVT_DESC_WORKING"));

            events_ = new EventSetDescriptor[] { error, propertyChange, vetoableChange, vobject, working };

            // Properties.
            PropertyDescriptor system = new PropertyDescriptor ("system", beanClass_);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText ("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText ("PROP_DESC_SYSTEM"));

            PropertyDescriptor formTypeFilter = new PropertyDescriptor("formTypeFilter", beanClass_);
            formTypeFilter.setBound(true);
            formTypeFilter.setConstrained(true);
            formTypeFilter.setDisplayName(ResourceLoader.getPrintText("PROP_NAME_SPLF_FORMTYPE_FILTER"));
            formTypeFilter.setShortDescription(ResourceLoader.getPrintText("PROP_DESC_SPLF_FORMTYPE_FILTER"));

            PropertyDescriptor outQFilter = new PropertyDescriptor("queueFilter", beanClass_);
            outQFilter.setBound(true);
            outQFilter.setConstrained(true);
            outQFilter.setDisplayName(ResourceLoader.getPrintText("PROP_NAME_SPLF_OUTQ_FILTER"));
            outQFilter.setShortDescription(ResourceLoader.getPrintText("PROP_DESC_SPLF_OUTQ_FILTER"));

            PropertyDescriptor userFilter = new PropertyDescriptor("userFilter", beanClass_);
            userFilter.setBound(true);
            userFilter.setConstrained(true);
            userFilter.setDisplayName(ResourceLoader.getPrintText("PROP_NAME_SPLF_USER_FILTER"));
            userFilter.setShortDescription(ResourceLoader.getPrintText("PROP_DESC_SPLF_USER_FILTER"));

            PropertyDescriptor userDataFilter = new PropertyDescriptor("userDataFilter", beanClass_);
            userDataFilter.setBound(true);
            userDataFilter.setConstrained(true);
            userDataFilter.setDisplayName(ResourceLoader.getPrintText("PROP_NAME_SPLF_USERDATA_FILTER"));
            userDataFilter.setShortDescription(ResourceLoader.getPrintText("PROP_DESC_SPLF_USERDATA_FILTER"));

            properties_ = new PropertyDescriptor[] {system, formTypeFilter, outQFilter, userFilter, userDataFilter };
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
    public BeanDescriptor getBeanDescriptor ()
    {
        return new BeanDescriptor (beanClass_);
    }


/**
Returns the index of the default event.

@return The index of the default event.
**/
    public int getDefaultEventIndex ()
    {
        return 0; // ErrorEvent.
    }


/**
Returns the index of the default property.

@return The index of the default property.
**/
    public int getDefaultPropertyIndex ()
    {
        return 1; // system.
    }

/**
Returns the event set descriptors.

@return The event set descriptors.
**/
    public EventSetDescriptor[] getEventSetDescriptors ()
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
                image = loadImage ("VPrinterOutput16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("VPrinterOutput32.gif");
                break;
        }
        return image;
    }


/**
Returns the property descriptors.

@return The property descriptors.
**/
    public PropertyDescriptor[] getPropertyDescriptors ()
    {
        return properties_;
    }

}
