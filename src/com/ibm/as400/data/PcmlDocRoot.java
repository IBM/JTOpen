///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDocRoot.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.util.Hashtable;

// This class implements the root node for the PcmlDocument

abstract class PcmlDocRoot extends PcmlDocNode {
    // Serial verion unique identifier
    static final long serialVersionUID = 8045487976295209373L;      // @C2A

    private Hashtable m_hash;

    PcmlDocRoot() 
    {
        m_hash = new Hashtable();
    }
    
    public Object clone()                                           // @C1A
    {                                                               // @C1A
        PcmlDocRoot node = null;                                    // @C1A
        node = (PcmlDocRoot) super.clone();                         // @C1A
        node.m_hash = new Hashtable();                              // @C1A
 
        return node;                                                // @C1A
    }                                                               // @C1A

    public void addElement(PcmlNode elem) 
    {
        m_hash.put(elem.getQualifiedName(), elem);
    }

    public boolean containsElement(String qName) 
    {
        return m_hash.containsKey(qName);
    }

    public PcmlNode getElement(String qName) 
    {
        return (PcmlNode) m_hash.get(qName);
    }
}
