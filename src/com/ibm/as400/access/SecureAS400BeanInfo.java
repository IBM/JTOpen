///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SecureAS400BeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 The SecureAS400BeanInfo class provides bean information for the SecureAS400 class.
 **/
public class SecureAS400BeanInfo extends AS400BeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private static final Class BEAN_CLASS = SecureAS400.class;

    private static PropertyDescriptor[] propertyDescriptors;

    static
    {
        try
        {
            PropertyDescriptor keyRingName = new PropertyDescriptor("keyRingName", BEAN_CLASS);
            keyRingName.setBound(true);
            keyRingName.setConstrained(true);
            keyRingName.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SECUREAS400_KEYRINGNAME"));
            keyRingName.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SECUREAS400_KEYRINGNAME"));

            PropertyDescriptor keyRingPassword = new PropertyDescriptor("keyRingPassword", BEAN_CLASS, null, "setKeyRingPassword");
            keyRingPassword.setBound(false);
            keyRingPassword.setConstrained(false);
            keyRingPassword.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SECUREAS400_KEYRINGPASSWORD"));
            keyRingPassword.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SECUREAS400_KEYRINGPASSWORD"));

            PropertyDescriptor proxyEncryptionMode = new PropertyDescriptor("proxyEncryptionMode", BEAN_CLASS);
            proxyEncryptionMode.setBound(true);
            proxyEncryptionMode.setConstrained(true);
            proxyEncryptionMode.setDisplayName(ResourceBundleLoader.getText("PROP_NAME_SECUREAS400_PROXYENCRYPTIONMODE"));
            proxyEncryptionMode.setShortDescription(ResourceBundleLoader.getText("PROP_DESC_SECUREAS400_PROXYENCRYPTIONMODE"));

            propertyDescriptors = new PropertyDescriptor[AS400BeanInfo.propertyDescriptors.length + 3];
            System.arraycopy(AS400BeanInfo.propertyDescriptors, 0, propertyDescriptors, 0, AS400BeanInfo.propertyDescriptors.length);
            propertyDescriptors[propertyDescriptors.length - 3] = keyRingName;
            propertyDescriptors[propertyDescriptors.length - 2] = keyRingPassword;
            propertyDescriptors[propertyDescriptors.length - 1] = proxyEncryptionMode;
        }
        catch (IntrospectionException e)
        {
            Trace.log(Trace.ERROR, "Unexpected IntrospectionException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
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
                return loadImage("SecureAS40016.gif");
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("SecureAS40032.gif");
        }
        return null;
    }
}
