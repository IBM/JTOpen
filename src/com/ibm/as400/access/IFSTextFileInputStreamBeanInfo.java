///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSTextFileInputStreamBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.BeanInfo;
import java.beans.BeanDescriptor;
import java.awt.Image;


/**
 The IFSTextFileInputStreamBeanInfo class provides bean information for
 the IFSTextFileInputStream class.
 **/

public class IFSTextFileInputStreamBeanInfo extends IFSFileInputStreamBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  /**
   Returns the bean descriptor.
   @return The bean descriptor.
   **/
  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(IFSTextFileInputStream.class);
  }

  /**
   Returns the copyright.
   @return The copyright String.
   **/
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

  /**
   Returns an Image for this bean's icon.
   @param icon The desired icon size and color.
   @return The Image for the icon.
   **/
  public Image getIcon(int icon)
  {
    Image image = null;

    switch(icon)
    {
    case BeanInfo.ICON_MONO_16x16:
    case BeanInfo.ICON_COLOR_16x16:
      image = loadImage("IFSTextFileInputStream16.gif");
      break;
    case BeanInfo.ICON_MONO_32x32:
    case BeanInfo.ICON_COLOR_32x32:
      image = loadImage("IFSTextFileInputStream32.gif");
      break;
    }

    return image;
  }
}

