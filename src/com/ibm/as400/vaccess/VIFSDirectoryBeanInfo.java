///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VIFSDirectoryBeanInfo.java
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
The VIFSDirectoryBeanInfo class provides bean information
for the VIFSDirectory class.

@see VIFSDirectory
**/
public class VIFSDirectoryBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = VIFSDirectory.class;
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

            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor (beanClass_,
                "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_VETO"));

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

            events_ = new EventSetDescriptor[] { error, propertyChange,
                vetoableChange, vobject, working };

            // Properties.
            PropertyDescriptor filter = new PropertyDescriptor ("filter", beanClass_);
            filter.setBound (true);
            filter.setConstrained (true);
            filter.setDisplayName (ResourceLoader.getText ("PROP_NAME_FILTER"));
            filter.setShortDescription (ResourceLoader.getText ("PROP_DESC_FILTER"));

            PropertyDescriptor include = new PropertyDescriptor ("include", beanClass_);
            include.setBound (true);
            include.setConstrained (true);
            include.setDisplayName (ResourceLoader.getText ("PROP_NAME_INCLUDE"));
            include.setPropertyEditorClass (VIFSDirectoryIncludeEditor.class);
            include.setShortDescription (ResourceLoader.getText ("PROP_DESC_INCLUDE"));

            PropertyDescriptor path = new PropertyDescriptor ("path", beanClass_);
            path.setBound (true);
            path.setConstrained (true);
            path.setDisplayName (ResourceLoader.getText ("PROP_NAME_PATH"));
            path.setShortDescription (ResourceLoader.getText ("PROP_DESC_PATH"));

            PropertyDescriptor pattern = new PropertyDescriptor ("pattern", beanClass_);
            pattern.setBound (true);
            pattern.setConstrained (true);
            pattern.setDisplayName (ResourceLoader.getText ("PROP_NAME_PATTERN"));
            pattern.setShortDescription (ResourceLoader.getText ("PROP_DESC_PATTERN"));

            PropertyDescriptor system = new PropertyDescriptor ("system", beanClass_);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText ("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText ("PROP_DESC_SYSTEM"));

            properties_ = new PropertyDescriptor[] { filter, include, path,
                pattern, system };
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
        return 2; // path.
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
                image = loadImage ("VIFSDirectory16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("VIFSDirectory32.gif");
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


