///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

/**
  *  The RfmlDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the &lt;rfml&gt; tag.
  *
  **/

class RfmlDescriptor extends PcmlDescriptor
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


    /** Constructor. **/
    public RfmlDescriptor(PcmlDocNode node)
    {
        super(node);
    }

   /**
    * Returns a String containing the current value for the requested attribute.
    **/
    public String getAttributeValue(String attr)
    {
        if (attr != null && attr.equals("ccsid"))
            return ((RfmlDocument)getDocNode()).getCcsid();
        else
            return super.getAttributeValue(attr);
    }
}
