///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: StructDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.data.Descriptor;
import com.ibm.as400.data.DocNodeDescriptor;

/**
  *  The StructDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the <struct> tag.
  *
  **/

class StructDescriptor extends DocNodeDescriptor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    /* Constructor */
    public StructDescriptor(PcmlDocNode node)
    {
        super(node);
    }
   /**
    * Return list of valid attributes for the <pcml> tag. 
    **/
    public String[] getAttributeList()
    {
        return ((PcmlStruct)getDocNode()).getAttributeList();  
    }

   /**
    * Return a String containing the current value for the requested attribute.
    **/
    public String getAttributeValue(String attr)
    {
        if (attr != null)
        {
            if (attr.equals("name"))
            {
                String name = getDocNode().getName();
                if (name.equals(""))
                    return null;
                else
                    return name;
            }
            else if (attr.equals("usage"))
            {
                switch (getDocNode().getUsage()) 
                {
                    case PcmlDocNode.INPUT:
                        return "input";
                    case PcmlDocNode.INPUTOUTPUT:
                        return "inputoutput";
                    case PcmlDocNode.OUTPUT:
                        return "output";
                    default:
                        return "inherit";
                }
            }
            else if (attr.equals("count"))
            {
                String countId = ((PcmlStruct)getDocNode()).getUnqualifiedCountId();
                if (countId != null)
                    return countId;
                else
                {
                    int count = ((PcmlStruct)getDocNode()).getCount();
                    if (count < 1)
                        return null;
                    else
                        return Integer.toString(count);
                }
            }
            else if (attr.equals("minvrm"))
                return ((PcmlStruct)getDocNode()).getMinvrmString();  
            else if (attr.equals("maxvrm"))
                return ((PcmlStruct)getDocNode()).getMaxvrmString();  
            else if (attr.equals("offset")) 
            {
                String offsetId = ((PcmlStruct)getDocNode()).getUnqualifiedOffsetId();
                if (offsetId != null)
                    return offsetId;
                else
                {
                    int offset = ((PcmlStruct)getDocNode()).getOffset();
                    if (offset < 1)
                        return null;
                    else
                        return Integer.toString(offset);
                }
            }
            else if (attr.equals("offsetfrom"))
            {
                String offsetfromId = ((PcmlStruct)getDocNode()).getUnqualifiedOffsetfromId();
                if (offsetfromId != null)
                    return offsetfromId;
                else
                {
                    int offsetfrom = ((PcmlStruct)getDocNode()).getOffsetfrom();
                    if (offsetfrom < 0)
                        return null;
                    else
                        return Integer.toString(offsetfrom);
                }
            }
            else if (attr.equals("outputsize"))
            {
                String outputsizeId = ((PcmlStruct)getDocNode()).getUnqualifiedOutputsizeId();
                if (outputsizeId != null)
                    return outputsizeId;
                else
                {
                    int outputsize = ((PcmlStruct)getDocNode()).getOutputsize();
                    if (outputsize < 1)
                        return null;
                    else
                        return Integer.toString(outputsize);
                }
            }
            else
                return null;
        }
        return null;
    }
}
