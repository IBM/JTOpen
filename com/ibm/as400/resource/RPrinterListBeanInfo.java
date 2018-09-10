///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RPrinterListBeanInfo.java
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
import java.beans.SimpleBeanInfo;
import java.util.ResourceBundle;



/**
The RPrinterListBeanInfo class represents the bean information
for the RPrinterList class.
@deprecated Use <tt>com.ibm.as400.access.PrinterList</tt> instead. 
**/
public class RPrinterListBeanInfo 
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final Class                  beanClass_              = RPrinterList.class;

    private static BeanInfo[]                   additionalBeanInfo_;
    private static BeanDescriptor               beanDescriptor_;
    private static Image                        icon16_;
    private static Image                        icon32_;
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
            additionalBeanInfo_ = new BeanInfo[] { new ResourceListBeanInfo() };

            // Set up the bean descriptor.
            beanDescriptor_ = new BeanDescriptor(beanClass_);

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
Returns the icon.

@param icon The icon kind.  Possible values are:
                <ul>
                <li>BeanInfo.ICON_MONO_16x16
                <li>BeanInfo.ICON_MONO_32x32
                <li>BeanInfo.ICON_COLOR_16x16
                <li>BeanInfo.ICON_COLOR_32x32
                </ul>
@return         The icon.                
**/
    public Image getIcon(int icon)
    {
        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                if (icon16_ == null)
                    icon16_ = loadImage("RPrinterList16.gif");
                return icon16_;

            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                if (icon32_ == null)
                    icon32_ = loadImage("RPrinterList32.gif");
                return icon32_;

            default:
            throw new ExtendedIllegalArgumentException("icon", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

}

