///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandCallMenuItemBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ActionCompletedListener;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;



/**
The CommandCallMenuItemBeanInfo class provides bean information
for the CommandCallMenuItem class.

@see CommandCallMenuItem
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class CommandCallMenuItemBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = CommandCallMenuItem.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;



/**
Static initializer.
**/
    static
    {
        try {

            // Events.
            EventSetDescriptor actionCompleted = new EventSetDescriptor (beanClass_,
                "actionCompleted", ActionCompletedListener.class, "actionCompleted");
            actionCompleted.setDisplayName (ResourceLoader.getText ("EVT_NAME_ACTION_COMPLETED"));
            actionCompleted.setShortDescription (ResourceLoader.getText ("EVT_DESC_ACTION_COMPLETED"));

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

            events_ = new EventSetDescriptor[] { actionCompleted, error, propertyChange,
                vetoableChange };

            // Properties.
            PropertyDescriptor command = new PropertyDescriptor ("command", beanClass_);
            command.setBound (true);
            command.setConstrained (true);
            command.setDisplayName (ResourceLoader.getText ("PROP_NAME_COMMAND"));
            command.setShortDescription (ResourceLoader.getText ("PROP_DESC_COMMAND"));

            PropertyDescriptor messageList = new PropertyDescriptor ("messageList", beanClass_,
                "getMessageList", null);
            messageList.setBound (false);
            messageList.setConstrained (false);
            messageList.setDisplayName (ResourceLoader.getText ("PROP_NAME_MESSAGE_LIST"));
            messageList.setShortDescription (ResourceLoader.getText ("PROP_DESC_MESSAGE_LIST"));

            PropertyDescriptor system = new PropertyDescriptor ("system", beanClass_);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText ("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText ("PROP_DESC_SYSTEM"));

            PropertyDescriptor text = new PropertyDescriptor ("text", beanClass_);
            text.setBound (false);
            text.setConstrained (false);
            text.setDisplayName (ResourceLoader.getText ("PROP_NAME_TEXT"));
            text.setShortDescription (ResourceLoader.getText ("PROP_DESC_TEXT"));

            properties_ = new PropertyDescriptor[] { command, messageList,
                system, text };
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
        return 0; // ActionCompletedEvent.
    }



/**
Returns the index of the default property.

@return The index of the default property.
**/
    public int getDefaultPropertyIndex ()
    {
        return 0; // command.
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
                image = loadImage ("CommandCallMenuItem16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("CommandCallMenuItem32.gif");
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


