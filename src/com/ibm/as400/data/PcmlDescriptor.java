///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

/**
  *  The PcmlDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the <pcml> tag.
  *
  **/

class PcmlDescriptor extends DocNodeDescriptor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    /* Constructor */
    public PcmlDescriptor(PcmlDocNode node)
    {
        super(node);
    }
   /**
    * Return list of valid attributes for the <pcml> tag. 
    **/
    public String[] getAttributeList()
    {
        return ((PcmlDocument)getDocNode()).getAttributeList();
    }

   /**
    * Return a String containing the current value for the requested attribute.
    **/
    public String getAttributeValue(String attr)
    {
        if (attr != null && attr.equals("version"))
            return ((PcmlDocument)getDocNode()).getVersion();
        else
            return null;
    }
}
