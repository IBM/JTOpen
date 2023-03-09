///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlAttributeList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.util.Vector;
import java.util.Enumeration;

class PcmlAttributeList extends Object {
    private Vector v;
    //private String value;
    //private boolean specified;

    PcmlAttributeList(int size) 
    {
        v = new Vector(size);
    }
    
    void addAttribute(PcmlAttribute attr)
    {
        v.addElement(attr);
    }
    
    String getAttributeValue(String name)
    {
        for (int i = 0; i < v.size(); i++)
        {
            PcmlAttribute attr = (PcmlAttribute) v.elementAt(i);
            if ( attr.getName().equals(name) )
            {
                return attr.getAttributeValue();
            }
        }
        return null;
    }
}
