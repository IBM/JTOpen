///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VActionAdapterBeanInfo.java
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



/**
The VActionAdapterBeanInfo class provides bean information
for the VActionAdapter class.

@see VActionAdapter
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VActionAdapterBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = VActionAdapter.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;



/**
Static initializer.
**/
    static
    {
        try {

            // Events.
            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            events_ = new EventSetDescriptor[] { propertyChange };

            // Properties.
            PropertyDescriptor action = new PropertyDescriptor ("action", beanClass_);
            action.setBound (true);
            action.setConstrained (false);
            action.setDisplayName (ResourceLoader.getText ("PROP_NAME_ACTION"));
            action.setShortDescription (ResourceLoader.getText ("PROP_DESC_ACTION"));

            PropertyDescriptor actionContext = new PropertyDescriptor ("actionContext", beanClass_);
            actionContext.setBound (true);
            actionContext.setConstrained (false);
            actionContext.setDisplayName (ResourceLoader.getText ("PROP_NAME_ACTION_CONTEXT"));
            actionContext.setShortDescription (ResourceLoader.getText ("PROP_DESC_ACTION_CONTEXT"));

            PropertyDescriptor enabled = new PropertyDescriptor ("enabled", beanClass_);
            enabled.setBound (true);
            enabled.setConstrained (false);
            enabled.setDisplayName (ResourceLoader.getText ("PROP_NAME_ENABLED"));
            enabled.setShortDescription (ResourceLoader.getText ("PROP_DESC_ENABLED"));

            properties_ = new PropertyDescriptor[] { action,
                actionContext, enabled };
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
        return 0; // Property change event.
    }



/**
Returns the index of the default property.

@return The index of the default property.
**/
    public int getDefaultPropertyIndex ()
    {
        return 0; // action.
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
                image = loadImage ("VActionAdapter16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("VActionAdapter32.gif");
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


