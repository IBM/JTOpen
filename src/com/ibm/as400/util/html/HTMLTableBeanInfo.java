///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLTableBeanInfo.java
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
*  The HTMLTableBeanInfo class provides bean information for the HTMLTable class.
**/
public class HTMLTableBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = HTMLTable.class;

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

        EventSetDescriptor veto = new EventSetDescriptor(beanClass,
                         "vetoableChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        EventSetDescriptor[] events = {changed, veto};

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor alignment = new PropertyDescriptor("alignment", beanClass,
                                        "getAlignment", "setAlignment");
        alignment.setBound(true);
        alignment.setConstrained(true);
        alignment.setDisplayName(loader_.getText("PROP_NAME_ALIGNMENT"));
        alignment.setShortDescription(loader_.getText("PROP_HTBL_DESC_ALIGNMENT"));

        PropertyDescriptor borderWidth = new PropertyDescriptor("borderWidth", beanClass,
                                        "getBorderWidth", "setBorderWidth");
        borderWidth.setBound(true);
        borderWidth.setConstrained(true);
        borderWidth.setDisplayName(loader_.getText("PROP_NAME_BORDERWIDTH"));
        borderWidth.setShortDescription(loader_.getText("PROP_DESC_BORDERWIDTH"));

        //PropertyDescriptor caption = new PropertyDescriptor("caption", beanClass,
        //                                "getCaption", "setCaption");
	Class[] parameterList = { HTMLTableCaption.class };
        PropertyDescriptor caption = new PropertyDescriptor("caption", 
                                        beanClass.getMethod("getCaption", null), beanClass.getMethod("setCaption", parameterList));
        
        caption.setBound(true);
        caption.setConstrained(true);
        caption.setDisplayName(loader_.getText("PROP_NAME_CAPTION"));
        caption.setShortDescription(loader_.getText("PROP_DESC_CAPTION"));

        PropertyDescriptor cellPadding = new PropertyDescriptor("cellPadding", beanClass,
                                        "getCellPadding", "setCellPadding");
        cellPadding.setBound(true);
        cellPadding.setConstrained(true);
        cellPadding.setDisplayName(loader_.getText("PROP_NAME_CELLPADDING"));
        cellPadding.setShortDescription(loader_.getText("PROP_DESC_CELLPADDING"));

        PropertyDescriptor cellSpacing = new PropertyDescriptor("cellSpacing", beanClass,
                                        "getCellSpacing", "setCellSpacing");
        cellSpacing.setBound(true);
        cellSpacing.setConstrained(true);
        cellSpacing.setDisplayName(loader_.getText("PROP_NAME_CELLSPACING"));
        cellSpacing.setShortDescription(loader_.getText("PROP_DESC_CELLSPACING"));
        
        PropertyDescriptor header = new PropertyDescriptor("header", beanClass,
                                    "getHeader", "setHeader");
        header.setBound(true);
        header.setConstrained(true);
        header.setDisplayName(loader_.getText("PROP_NAME_HEADER"));
        header.setShortDescription(loader_.getText("PROP_DESC_HEADER"));

        PropertyDescriptor useHeader = new PropertyDescriptor("headerInUse", beanClass,
                                        "isHeaderInUse", "setHeaderInUse");
        useHeader.setBound(true);
        useHeader.setConstrained(true);
        useHeader.setDisplayName(loader_.getText("PROP_NAME_HEADERINUSE"));
        useHeader.setShortDescription(loader_.getText("PROP_DESC_HEADERINUSE"));

        PropertyDescriptor width = new PropertyDescriptor("width", beanClass,
                                                          "getWidth", "setWidth");
        width.setBound(true);
        width.setConstrained(true);
        width.setDisplayName(loader_.getText("PROP_NAME_WIDTH"));
        width.setShortDescription(loader_.getText("PROP_HTBL_DESC_WIDTH"));

        PropertyDescriptor widthInPercent = new PropertyDescriptor("widthInPercent", beanClass,
                                        "isWidthInPercent", "setWidthInPercent");
        widthInPercent.setBound(true);
        widthInPercent.setConstrained(true);
        widthInPercent.setDisplayName(loader_.getText("PROP_NAME_WPERCENT"));
        widthInPercent.setShortDescription(loader_.getText("PROP_HTBL_DESC_WPERCENT"));

        properties_ = new PropertyDescriptor[] { alignment, borderWidth, caption, cellPadding, cellSpacing, header, useHeader, width, widthInPercent };
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
                image = loadImage ("HTMLTable16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLTable32.gif");
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
