///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataQueueAttributesBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;

/**
 The DataQueueAttributesBeanInfo class provides bean information for the DataQueueAttributes class.
 **/
public class DataQueueAttributesBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class BEAN_CLASS = DataQueueAttributes.class;

    private static EventSetDescriptor[] eventSetDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            EventSetDescriptor propertyChange = new EventSetDescriptor(BEAN_CLASS, "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor(BEAN_CLASS, "vetoableChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_VETO"));

            eventSetDescriptors = new EventSetDescriptor[] { propertyChange, vetoableChange };

            PropertyDescriptor entryLength = new PropertyDescriptor("entryLength", BEAN_CLASS);
            entryLength.setBound(true);
            entryLength.setConstrained(true);
            entryLength.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_ENTRYLENGTH"));
            entryLength.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_ENTRYLENGTH"));

            PropertyDescriptor authority = new PropertyDescriptor("authority", BEAN_CLASS);
            authority.setBound(true);
            authority.setConstrained(true);
            authority.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_AUTHORITY"));
            authority.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_AUTHORITY"));
            authority.setPropertyEditorClass(DQAttsAuthorityEditor.class);

            PropertyDescriptor saveSenderInfo = new PropertyDescriptor("saveSenderInfo", BEAN_CLASS);
            saveSenderInfo.setBound(true);
            saveSenderInfo.setConstrained(true);
            saveSenderInfo.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SAVESENDERINFO"));
            saveSenderInfo.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SAVESENDERINFO"));

            PropertyDescriptor FIFO = new PropertyDescriptor("FIFO", BEAN_CLASS);
            FIFO.setBound(true);
            FIFO.setConstrained(true);
            FIFO.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_FIFO"));
            FIFO.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_FIFO"));

            PropertyDescriptor forceToAuxiliaryStorage = new PropertyDescriptor("forceToAuxiliaryStorage", BEAN_CLASS);
            forceToAuxiliaryStorage.setBound(true);
            forceToAuxiliaryStorage.setConstrained(true);
            forceToAuxiliaryStorage.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_FORCETOAUX"));
            forceToAuxiliaryStorage.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_FORCETOAUX"));

            PropertyDescriptor description = new PropertyDescriptor("description", BEAN_CLASS);
            description.setBound(true);
            description.setConstrained(true);
            description.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_DESCRIPTION"));
            description.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_DESCRIPTION"));

            PropertyDescriptor keyLength = new PropertyDescriptor("keyLength", BEAN_CLASS);
            keyLength.setBound(true);
            keyLength.setConstrained(true);
            keyLength.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_KEYLENGTH"));
            keyLength.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_KEYLENGTH"));

            propertyDescriptors = new PropertyDescriptor[] { entryLength, authority, saveSenderInfo, FIFO, forceToAuxiliaryStorage, description, keyLength };
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
        // The index for the "propertyChange" event.
        return 0;
    }

    /**
     Returns the index of the default property.
     @return  Zero (0), the index to the default property.
     **/
    public int getDefaultPropertyIndex()
    {
        // The index for the "entryLength" property
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

    /**
     Returns an Image for this bean's icon.
     @param  icon  The desired icon size and color.
     @return  The Image for the icon.
     **/
    public Image getIcon(int icon)
    {
        switch (icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("DataQueueAttributes16.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("DataQueueAttributes32.gif");
        }
        return null;
    }
}
