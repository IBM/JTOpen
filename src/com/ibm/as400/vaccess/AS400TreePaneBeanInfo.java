///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400TreePaneBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.TreeSelectionListener;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;



/**
The AS400TreePaneBeanInfo class provides bean information
for the AS400TreePane class.

@see AS400TreePane
**/
public class AS400TreePaneBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = AS400TreePane.class;
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

            EventSetDescriptor treeSelection = new EventSetDescriptor (beanClass_,
                "treeSelection", TreeSelectionListener.class, "valueChanged");
            treeSelection.setDisplayName (ResourceLoader.getText ("EVT_NAME_TREE_SELECTION"));
            treeSelection.setShortDescription (ResourceLoader.getText ("EVT_DESC_TREE_SELECTION"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor (beanClass_,
                "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_VETO"));

            events_ = new EventSetDescriptor[] { error, propertyChange, treeSelection,
                vetoableChange };

            // Properties.
            PropertyDescriptor allowActions = new PropertyDescriptor ("allowActions", beanClass_);
            allowActions.setBound (false);
            allowActions.setConstrained (false);
            allowActions.setDisplayName (ResourceLoader.getText ("PROP_NAME_ALLOW_ACTIONS"));
            allowActions.setShortDescription (ResourceLoader.getText ("PROP_DESC_ALLOW_ACTIONS"));

            PropertyDescriptor confirm = new PropertyDescriptor ("confirm", beanClass_);
            confirm.setBound (false);
            confirm.setConstrained (false);
            confirm.setDisplayName (ResourceLoader.getText ("PROP_NAME_CONFIRM"));
            confirm.setShortDescription (ResourceLoader.getText ("PROP_DESC_CONFIRM"));

            PropertyDescriptor model = new PropertyDescriptor ("model", beanClass_,
                "getModel", null);
            model.setBound (false);
            model.setConstrained (false);
            model.setDisplayName (ResourceLoader.getText ("PROP_NAME_MODEL"));
            model.setShortDescription (ResourceLoader.getText ("PROP_DESC_MODEL"));

            PropertyDescriptor root = new PropertyDescriptor ("root", beanClass_);
            root.setBound (true);
            root.setConstrained (true);
            root.setDisplayName (ResourceLoader.getText ("PROP_NAME_ROOT"));
            root.setShortDescription (ResourceLoader.getText ("PROP_DESC_ROOT"));

            PropertyDescriptor selectionModel = new PropertyDescriptor ("selectionModel", beanClass_);
            selectionModel.setBound (false);
            selectionModel.setConstrained (false);
            selectionModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_SELECTION_MODEL"));
            selectionModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_SELECTION_MODEL"));

            properties_ = new PropertyDescriptor[] { allowActions, confirm, model,
                root, selectionModel };
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
        return 3; // root.
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
                image = loadImage ("AS400TreePane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("AS400TreePane32.gif");
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


