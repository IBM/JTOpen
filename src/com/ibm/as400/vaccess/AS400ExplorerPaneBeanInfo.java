///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ExplorerPaneBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.ListSelectionListener;
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
The AS400ExplorerPaneBeanInfo class provides bean information
for the AS400ExplorerPane class.

@see AS400ExplorerPane
**/


public class AS400ExplorerPaneBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = AS400ExplorerPane.class;
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

            EventSetDescriptor listSelection = new EventSetDescriptor (beanClass_,
                "listSelection", ListSelectionListener.class, "valueChanged");
            listSelection.setDisplayName (ResourceLoader.getText ("EVT_NAME_LIST_SELECTION"));
            listSelection.setShortDescription (ResourceLoader.getText ("EVT_DESC_LIST_SELECTION"));

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

            events_ = new EventSetDescriptor[] { error, listSelection, propertyChange,
                treeSelection, vetoableChange };

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

            PropertyDescriptor detailsColumnModel = new PropertyDescriptor ("detailsColumnModel", beanClass_,
                "getDetailsColumnModel", null);
            detailsColumnModel.setBound (false);
            detailsColumnModel.setConstrained (false);
            detailsColumnModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_COLUMN_MODEL"));
            detailsColumnModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_COLUMN_MODEL"));

            PropertyDescriptor detailsModel = new PropertyDescriptor ("detailsModel", beanClass_,
                "getDetailsModel", null);
            detailsModel.setBound (false);
            detailsModel.setConstrained (false);
            detailsModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_MODEL"));
            detailsModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_MODEL"));

            PropertyDescriptor detailsRoot = new PropertyDescriptor ("detailsRoot", beanClass_,
                "getDetailsRoot", null);
            detailsRoot.setBound (true);
            detailsRoot.setConstrained (true);
            detailsRoot.setDisplayName (ResourceLoader.getText ("PROP_NAME_ROOT"));
            detailsRoot.setShortDescription (ResourceLoader.getText ("PROP_DESC_ROOT"));

            PropertyDescriptor detailsSelectionModel = new PropertyDescriptor ("detailsSelectionModel", beanClass_);
            detailsSelectionModel.setBound (false);
            detailsSelectionModel.setConstrained (false);
            detailsSelectionModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_SELECTION_MODEL"));
            detailsSelectionModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_SELECTION_MODEL"));

            PropertyDescriptor root = new PropertyDescriptor ("root", beanClass_);
            root.setBound (true);
            root.setConstrained (true);
            root.setDisplayName (ResourceLoader.getText ("PROP_NAME_ROOT"));
            root.setShortDescription (ResourceLoader.getText ("PROP_DESC_ROOT"));

            PropertyDescriptor treeModel = new PropertyDescriptor ("treeModel", beanClass_,
                "getTreeModel", null);
            treeModel.setBound (false);
            treeModel.setConstrained (false);
            treeModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_MODEL"));
            treeModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_MODEL"));

            PropertyDescriptor treeSelectionModel = new PropertyDescriptor ("treeSelectionModel", beanClass_);
            treeSelectionModel.setBound (false);
            treeSelectionModel.setConstrained (false);
            treeSelectionModel.setDisplayName (ResourceLoader.getText ("PROP_NAME_SELECTION_MODEL"));
            treeSelectionModel.setShortDescription (ResourceLoader.getText ("PROP_DESC_SELECTION_MODEL"));

            properties_ = new PropertyDescriptor[] { allowActions, confirm,
                detailsColumnModel, detailsModel, detailsRoot,
                detailsSelectionModel, root, treeModel, treeSelectionModel };
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
        return 6; // root.
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
                image = loadImage ("AS400ExplorerPane16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("AS400ExplorerPane32.gif");
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


