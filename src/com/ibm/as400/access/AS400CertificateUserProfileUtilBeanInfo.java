///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateUserProfileUtilBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;


/**
The AS400CertificateUserProfileUtilBeanInfo class provides
bean information for the AS400CertificateUserProfileUtil class.
**/
public class AS400CertificateUserProfileUtilBeanInfo extends AS400CertificateUtilBeanInfo 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class usrprfBeanClass = AS400CertificateUserProfileUtil.class;

    /**
    Returns the bean descriptor.
    @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(usrprfBeanClass);
    }


    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright.copyright;
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

