///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

/**
 The SecureAS400BeanInfo class provides bean information for the SecureAS400 class.
 **/
public class SecureAS400BeanInfo extends AS400BeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
