///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VMessageQueueBeanInfo.java
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
The VMessageQueueBeanInfo class provides bean information
for the VMessageQueue class.

@see VMessageQueue
**/
public class VMessageQueueBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = VMessageQueue.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;



/**
Static initializer.
**/
    static
    {
        try {

            // Events.
            EventSetDescriptor error = new EventSetDescriptor (beanClass_,
                "error", ErrorListener.class, "errorOccurred");
            error.setDisplayName (ResourceLoader.getText ("EVT_NAME_ERROR"));
            error.setShortDescription (ResourceLoader.getText ("EVT_DESC_ERROR"));

            String[] vobjectMethods = { "objectChanged", "objectCreated", "objectDeleted" };
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

            events_ = new EventSetDescriptor[] { error, vobject, working };

            // Properties.
            PropertyDescriptor path = new PropertyDescriptor ("path", beanClass_);
            path.setBound (true);
            path.setConstrained (true);
            path.setDisplayName (ResourceLoader.getText ("PROP_NAME_PATH"));
            path.setShortDescription (ResourceLoader.getText ("PROP_DESC_PATH"));

            PropertyDescriptor selection = new PropertyDescriptor ("selection", beanClass_);
            selection.setBound (true);
            selection.setConstrained (true);
            selection.setDisplayName (ResourceLoader.getText ("PROP_NAME_SELECTION"));
            selection.setShortDescription (ResourceLoader.getText ("PROP_DESC_SELECTION"));

            PropertyDescriptor severity = new PropertyDescriptor ("severity", beanClass_);
            severity.setBound (true);
            severity.setConstrained (true);
            severity.setDisplayName (ResourceLoader.getText ("PROP_NAME_SEVERITY"));
            severity.setShortDescription (ResourceLoader.getText ("PROP_DESC_SEVERITY"));

            PropertyDescriptor system = new PropertyDescriptor ("system", beanClass_);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText ("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText ("PROP_DESC_SYSTEM"));

            properties_ = new PropertyDescriptor[] { path, selection,
                severity, system };
        }
        catch (Exception e) {
            throw new Error (e.toString ());
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
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
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
        return 0; // path.
    }



/**
Returns the descriptors for all events.

@return The descriptors for all events.
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
                image = loadImage ("VMessageList16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("VMessageList32.gif");
                break;
        }
        return image;
    }



/**
Returns the descriptors for all properties.

@return The descriptors for all properties.
**/
    public PropertyDescriptor[] getPropertyDescriptors ()
    {
        return properties_;
    }



}


