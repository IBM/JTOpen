///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RSoftwareResourceBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ResourceBundle;



/**
The RSoftwareResourceBeanInfo class represents the bean information
for the RSoftwareResource class.
**/
public class RSoftwareResourceBeanInfo 
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final Class                  beanClass_              = RSoftwareResource.class;

    private static BeanInfo[]                   additionalBeanInfo_;
    private static BeanDescriptor               beanDescriptor_;
    private static Image                        icon16_;
    private static Image                        icon32_;
    private static PropertyDescriptor[]         propertyDescriptors_;
    private static ResourceBundle               resourceBundle_;



/**
Static initializer.
**/
    static
    {
        try
        {
            // Set up the resource bundle.
            resourceBundle_ = ResourceBundle.getBundle("com.ibm.as400.resource.ResourceMRI");

            // Set up the additional bean info.
            additionalBeanInfo_ = new BeanInfo[] { new ResourceBeanInfo() };

            // Set up the bean descriptor.
            beanDescriptor_ = new BeanDescriptor(beanClass_);

            // Set up the property descriptors.
            PropertyDescriptor productID = new PropertyDescriptor("productID", beanClass_);
            productID.setBound(true);
            productID.setConstrained(false);
            productID.setDisplayName(resourceBundle_.getString("PROPERTY_PRODUCT_ID_NAME"));
            productID.setShortDescription(resourceBundle_.getString("PROPERTY_PRODUCT_ID_DESCRIPTION"));

            PropertyDescriptor releaseLevel = new PropertyDescriptor("releaseLevel", beanClass_);
            releaseLevel.setBound(true);
            releaseLevel.setConstrained(false);
            releaseLevel.setDisplayName(resourceBundle_.getString("PROPERTY_RELEASE_LEVEL_NAME"));
            releaseLevel.setShortDescription(resourceBundle_.getString("PROPERTY_RELEASE_LEVEL_DESCRIPTION"));

            PropertyDescriptor productOption = new PropertyDescriptor("productOption", beanClass_);
            productOption.setBound(true);
            productOption.setConstrained(false);
            productOption.setDisplayName(resourceBundle_.getString("PROPERTY_PRODUCT_OPTION_NAME"));
            productOption.setShortDescription(resourceBundle_.getString("PROPERTY_PRODUCT_OPTION_DESCRIPTION"));

            PropertyDescriptor loadID = new PropertyDescriptor("loadID", beanClass_);
            loadID.setBound(true);
            loadID.setConstrained(false);
            loadID.setDisplayName(resourceBundle_.getString("PROPERTY_LOAD_ID_NAME"));
            loadID.setShortDescription(resourceBundle_.getString("PROPERTY_LOAD_ID_DESCRIPTION"));

            propertyDescriptors_ = new PropertyDescriptor[] { productID, releaseLevel, productOption, loadID };

        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error while loading bean info", e);
            throw new Error(e.toString());
        }
    }
    


/**
Returns the additional bean information.

@return The additional bean information.
**/
    public BeanInfo[] getAdditionalBeanInfo()
    {
        return additionalBeanInfo_;
    }


/**
Returns the bean descriptor.

@return The bean descriptor.
**/
    public BeanDescriptor getBeanDescriptor()
    {
        return beanDescriptor_;
    }



/**
Returns the property descriptors.

@return The property descriptors.
**/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
       return propertyDescriptors_;
    }



    // There currently no icons for this bean.


}

