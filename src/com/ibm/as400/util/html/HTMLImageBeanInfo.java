///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResetFormInput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;


import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;


/**
*  The HTMLImageBeanInfo class provides bean information for the HTMLImage class.
**/
public class HTMLImageBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = HTMLImage.class;

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

            EventSetDescriptor[] events_ = {changed};

            // ***** PROPERTIES
            PropertyDescriptor align = new PropertyDescriptor("align", beanClass,
                                                              "getAlign", "setAlign");
            align.setBound(true);
            align.setConstrained(false);
            align.setDisplayName(loader_.getText("PROP_NAME_ALIGNMENT"));
            align.setShortDescription(loader_.getText("PROP_DESC_ALIGNMENT"));

            PropertyDescriptor src = new PropertyDescriptor("source", beanClass, "getSrc", "setSrc");
            src.setBound(true);
            src.setConstrained(false);
            src.setDisplayName(loader_.getText("PROP_NAME_SOURCE"));
            src.setShortDescription(loader_.getText("PROP_DESC_SOURCE"));

            PropertyDescriptor alt = new PropertyDescriptor("alt", beanClass, "getAlt", "setAlt");
            alt.setBound(true);
            alt.setConstrained(false);
            alt.setDisplayName(loader_.getText("PROP_NAME_ALT"));
            alt.setShortDescription(loader_.getText("PROP_DESC_ALT"));

            PropertyDescriptor border = new PropertyDescriptor("border", beanClass, "getBorder", "setBorder");   
            border.setBound(true);                                                                                 
            border.setConstrained(false);                                                                          
            border.setDisplayName(loader_.getText("PROP_NAME_BORDER"));                                          
            border.setShortDescription(loader_.getText("PROP_DESC_BORDER"));                                     

            PropertyDescriptor name = new PropertyDescriptor("name", beanClass, "getName", "setName");   
            name.setBound(true);                                                                                  
            name.setConstrained(false);                                                                           
            name.setDisplayName(loader_.getText("PROP_NAME_NAME"));                                          
            name.setShortDescription(loader_.getText("PROP_HI_DESC_NAME"));                                     

            PropertyDescriptor hspace = new PropertyDescriptor("hspace", beanClass, "getHSpace", "setHSpace");   
            hspace.setBound(true);                                                                                  
            hspace.setConstrained(false);                                                                           
            hspace.setDisplayName(loader_.getText("PROP_NAME_HSPACE"));                                          
            hspace.setShortDescription(loader_.getText("PROP_DESC_HSPACE"));

            PropertyDescriptor vspace = new PropertyDescriptor("vspace", beanClass, "getVSpace", "setVSpace");   
            vspace.setBound(true);                                                                                  
            vspace.setConstrained(false);                                                                           
            vspace.setDisplayName(loader_.getText("PROP_NAME_VSPACE"));                                          
            vspace.setShortDescription(loader_.getText("PROP_DESC_VSPACE"));

            PropertyDescriptor width = new PropertyDescriptor("width", beanClass, "getWidth", "setWidth");   
            width.setBound(true);                                                                                  
            width.setConstrained(false);                                                                           
            width.setDisplayName(loader_.getText("PROP_NAME_WIDTH"));                                          
            width.setShortDescription(loader_.getText("PROP_DESC_WIDTH"));

            PropertyDescriptor height = new PropertyDescriptor("height", beanClass, "getHeight", "setHeight");   
            height.setBound(true);                                                                                  
            height.setConstrained(false);                                                                           
            height.setDisplayName(loader_.getText("PROP_NAME_HEIGHT"));                                          
            height.setShortDescription(loader_.getText("PROP_DESC_HEIGHT"));

            properties_ = new PropertyDescriptor[] {align, src, alt, border, name, hspace, vspace, width, height};                              
        }
        catch (Exception e)
        {
            throw new Error(e.toString());
        }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLHeading is a subclass of HTMLTagAttributes, this method
     * will return a HTMLTagAttributesBeanInfo object.
     *
     * @return BeanInfo[] containing this bean's superclass BeanInfo
     **/
    public BeanInfo[] getAdditionalBeanInfo()                            
    {
        return new BeanInfo[] { new HTMLTagAttributesBeanInfo()};        
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
        switch (icon)
        {
        case BeanInfo.ICON_MONO_16x16:
        case BeanInfo.ICON_COLOR_16x16:
            image = loadImage ("HTMLImage16.gif");
            break;
        case BeanInfo.ICON_MONO_32x32:
        case BeanInfo.ICON_COLOR_32x32:
            image = loadImage ("HTMLImage32.gif");
            break;
        }
        return image;
    }


    /**
    *  Returns the descriptors for all properties.
    *  @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
