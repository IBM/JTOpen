///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileTreeElementBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;

/**
The FileTreeElementBeanInfo class provides
bean information for the FileTreeElement class.
**/
public class FileTreeElementBeanInfo extends SimpleBeanInfo 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = FileTreeElement.class;


    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;

    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    static
    {
      try
      {
        EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                                                            "propertyChange",
                                                            java.beans.PropertyChangeListener.class,
                                                            "propertyChange");
        changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
        changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

        EventSetDescriptor[] events = { changed };

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor file = new PropertyDescriptor("file", beanClass,           
                                                            "getFile", "setFile");    
        file.setBound(true);                                                              
        file.setConstrained(false);                                                       
        file.setDisplayName(loader_.getText("PROP_NAME_FILE"));                       
        file.setShortDescription(loader_.getText("PROP_DESC_FILE"));                  

        PropertyDescriptor shareName = new PropertyDescriptor("shareName", beanClass,           // @B1A
                                                              "getShareName", "setShareName");  // @B1A
        shareName.setBound(true);                                                               // @B1A
        shareName.setConstrained(false);                                                        // @B1A
        shareName.setDisplayName(loader_.getText("PROP_NAME_SHARE_NAME"));                      // @B1A
        shareName.setShortDescription(loader_.getText("PROP_DESC_SHARE_NAME"));                 // @B1A

        PropertyDescriptor sharePath = new PropertyDescriptor("sharePath", beanClass,           // @B1A
                                                              "getSharePath", "setSharePath");  // @B1A
        sharePath.setBound(true);                                                               // @B1A
        sharePath.setConstrained(false);                                                        // @B1A
        sharePath.setDisplayName(loader_.getText("PROP_NAME_SHARE_PATH"));                      // @B1A
        sharePath.setShortDescription(loader_.getText("PROP_DESC_SHARE_PATH"));                 // @B1A

        properties_ = new PropertyDescriptor[] { file, shareName, sharePath }; // @B1A
      }
      catch (Exception e)
      {
        throw new Error(e.toString());
      }
    }

    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * ButtonFormInput is a subclass of FormInput, this method
     * will return a HTMLTreeElementBeanInfo object.
     *
     * @return BeanInfo[] containing this bean's superclass BeanInfo
     **/
    public BeanInfo[] getAdditionalBeanInfo()
    {
       return new BeanInfo[] { new HTMLTreeElementBeanInfo() };
    }


    /**
    Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }


    /**
    Returns the index of the default event.
      @return The index to the default event.
    **/
    public int getDefaultEventIndex()
    {
        return 0;
    }

    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    /**
      Returns the descriptors for all events.
      @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
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
                image = loadImage ("FileTreeElement16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("FileTreeElement32.gif");
                break;
        }
        return image;
    }

    /**
    Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }
}
