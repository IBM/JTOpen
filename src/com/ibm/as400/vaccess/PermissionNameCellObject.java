///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionNameCellObject.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.Icon;

/**
 *  The PermissionNameCellObject class provides an object that contains name and icon.
 **/

class PermissionNameCellObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     * Private variable representing user name.
     **/
    private String name_;

    /**
     * Private variable representing the flag of the user icon.
    **/
    private int groupIndicator_;

    /**
     * Constructs  a PermissionNameCellObject object.
     * @param name  The user name.
     * @param group The flag that indicates the icon of the user.
     **/
    public PermissionNameCellObject(String name,int group)
    {
        name_ = name;
        groupIndicator_ = group;
    }

    /**
     * Copyright.
     **/
     private static String getCopyright()
     {
         return Copyright_v.copyright;
     }

    /**
     * Returns the icon.
     * @param  size    The icon size, either 16 or 32.  If any other
     * value is given, then return a default.
     * @param  open    True for the open icon; false for the closed
     * icon.  If there is only one icon, then this
     * parameter has no effect.
     * @return  The icon, or null if there is none.
     **/
    public Icon getIcon (int size, boolean open)
    {
        Icon imageIcon = null;
        if (size == 32)
        {
          if(groupIndicator_ != 1)
               imageIcon = ResourceLoader.getIcon("group32.gif", "");
          else
               imageIcon = ResourceLoader.getIcon("user32.gif", "");
        }
        else
        {
          if(groupIndicator_ != 1)
               imageIcon = ResourceLoader.getIcon("group16.gif", "");
          else
               imageIcon = ResourceLoader.getIcon("user16.gif", "");
        }
        return  imageIcon;
    }

    /**
     * Returns the user name.
     * @return  The user name.
     **/
    public String getText ()
    {
        return name_;
    }
 }
