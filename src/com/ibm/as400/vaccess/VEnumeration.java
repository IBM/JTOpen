///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.Enumeration;

/**
  * A simple wrapper to enumerate VObject children.
  **/
class VEnumeration
implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    VNode vobj_;
    int pos=0;



    VEnumeration( VNode vobj )
    {
        vobj_ = vobj;
    }



    public boolean hasMoreElements()
    {
        return pos < vobj_.getChildCount();
    }



    /**
    Copyright.
    **/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



    public Object nextElement()
    {
        try {
            return vobj_.getChildAt(pos++);
        } catch (Exception e)
        {
            throw new java.util.NoSuchElementException();
        }
    }
}
