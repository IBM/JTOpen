///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListDetailsModelBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import javax.swing.event.TableModelListener;



/**
The ResourceListDetailsModelBeanInfo class provides bean information
for the ResourceListDetailsModel class.

@see ResourceListDetailsModel
**/
public class ResourceListDetailsModelBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = ResourceListDetailsModel.class;
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

            EventSetDescriptor tableModel = new EventSetDescriptor (beanClass_,
                "tableModel", TableModelListener.class, "tableChanged");
            tableModel.setDisplayName (ResourceLoader.getText ("EVT_NAME_TABLE_MODEL"));
            tableModel.setShortDescription (ResourceLoader.getText ("EVT_DESC_TABLE_MODEL"));

            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            String[] workingMethods = { "startWorking", "stopWorking" };
            EventSetDescriptor working = new EventSetDescriptor (beanClass_,
                "working", WorkingListener.class, workingMethods,
                "addWorkingListener", "removeWorkingListener");
            working.setDisplayName (ResourceLoader.getText ("EVT_NAME_WORKING"));
            working.setShortDescription (ResourceLoader.getText ("EVT_DESC_WORKING"));

            events_ = new EventSetDescriptor[] { error, tableModel, propertyChange, working };

            // Properties.
            PropertyDescriptor resourceList = new PropertyDescriptor ("resourceList", beanClass_);
            resourceList.setBound (true);
            resourceList.setConstrained (false);
            resourceList.setDisplayName (ResourceLoader.getText ("PROP_NAME_RESOURCE_LIST"));
            resourceList.setShortDescription (ResourceLoader.getText ("PROP_DESC_RESOURCE_LIST"));

            PropertyDescriptor columnAttributeIDs = new PropertyDescriptor ("columnAttributeIDs", beanClass_);
            columnAttributeIDs.setBound (true);
            columnAttributeIDs.setConstrained (false);
            columnAttributeIDs.setDisplayName (ResourceLoader.getText ("PROP_NAME_COLUMN_ATTRIBUTE_IDS"));
            columnAttributeIDs.setShortDescription (ResourceLoader.getText ("PROP_DESC_COLUMN_ATTRIBUTE_IDS"));

            properties_ = new PropertyDescriptor[] { resourceList, columnAttributeIDs };
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
        return 0; // resource list.
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
                image = loadImage ("ResourceListDetailsModel16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("ResourceListDetailsModel32.gif");
                break;
            default:
                throw new ExtendedIllegalArgumentException("icon", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
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
