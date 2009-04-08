///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlAttribute.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

class PcmlAttribute extends Object {
    private String name;
    private String value;
    private boolean specified;

    PcmlAttribute(String aName, String aValue, boolean aFlag) 
    {
        name = aName;
        value = aValue;
        specified = aFlag;
    }

    String getName()
    {
        return name;
    }

    String getAttributeValue()
    {
        return value;
    }

    boolean getSpecified()
    {
        return specified;
    }
}
