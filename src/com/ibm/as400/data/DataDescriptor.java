///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.BidiStringType;

/**
  *  The DataDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the <data> tag.
  *
  **/

class DataDescriptor extends DocNodeDescriptor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /* Constructor */
    public DataDescriptor(PcmlDocNode node)
    {
        super(node);
    }

   /**
    * Return the list of valid attributes for the data element.
    **/
    public String[] getAttributeList()
    {
        return ((PcmlData)getDocNode()).getAttributeList();
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
                    case PcmlDocNode.INHERIT:
                        return "inherit";
                }
            }
            else if (attr.equals("count"))
            {
                String countId = ((PcmlData)getDocNode()).getUnqualifiedCountId();
                if (countId != null)
                    return countId;
                else
                {
                    int count = ((PcmlData)getDocNode()).getCount();
                    if (count < 1)
                        return null;
                    else
                        return Integer.toString(count);
                }
            }
            else if (attr.equals("minvrm"))
                return ((PcmlData)getDocNode()).getMinvrmString();
            else if (attr.equals("maxvrm"))
                return ((PcmlData)getDocNode()).getMaxvrmString();
            else if (attr.equals("offset"))
            {
                String offsetId = ((PcmlData)getDocNode()).getUnqualifiedOffsetId();
                if (offsetId != null)
                    return offsetId;
                else
                {
                    int offset = ((PcmlData)getDocNode()).getOffset();
                    if (offset < 1)
                        return null;
                    else
                        return Integer.toString(offset);
                }
            }
            else if (attr.equals("offsetfrom"))
            {
                String offsetfromId = ((PcmlData)getDocNode()).getUnqualifiedOffsetfromId();
                if (offsetfromId != null)
                    return offsetfromId;
                else
                {
                    int offsetfrom = ((PcmlData)getDocNode()).getOffsetfrom();
                    if (offsetfrom < 0)
                        return null;
                    else
                        return Integer.toString(offsetfrom);
                }
            }
            else if (attr.equals("outputsize"))
            {
                String outputsizeId = ((PcmlData)getDocNode()).getUnqualifiedOutputsizeId();
                if (outputsizeId != null)
                    return outputsizeId;
                else
                {
                    int outputsize = ((PcmlData)getDocNode()).getOutputsize();
                    if (outputsize < 1)
                        return null;
                    else
                        return Integer.toString(outputsize);
                }
            }
            else if (attr.equals("type"))
            {
                switch (((PcmlData)getDocNode()).getDataType())
                {
                    case PcmlData.CHAR:
                        return "char";
                    case PcmlData.INT:
                        return "int";
                    case PcmlData.PACKED:
                        return "packed";
                    case PcmlData.ZONED:
                        return "zoned";
                    case PcmlData.FLOAT:
                        return "float";
                    case PcmlData.BYTE:
                        return "byte";
                    case PcmlData.STRUCT:
                        return "struct";
                }
            }
            else if (attr.equals("length"))
            {
                String lengthId = ((PcmlData)getDocNode()).getUnqualifiedLengthId();
                if (lengthId != null)
                    return lengthId;
                else
                    return Integer.toString(((PcmlData)getDocNode()).getLength());
            }
            else if (attr.equals("precision"))
            {
                int precision = ((PcmlData)getDocNode()).getPrecision();
                if (precision < 1)
                    return null;
                else
                    return Integer.toString(precision);
            }
            else if (attr.equals("ccsid"))
            {
                String ccsidId = ((PcmlData)getDocNode()).getUnqualifiedCcsidId();
                if (ccsidId != null)
                    return ccsidId;
                else
                {
                    int ccsid = ((PcmlData)getDocNode()).getCcsid();
                    if (ccsid < 1)
                        return null;
                    else
                        return Integer.toString(ccsid);
                }
            }
            else if (attr.equals("init"))
                return ((PcmlData)getDocNode()).getInit();
            else if (attr.equals("struct"))
                return ((PcmlData)getDocNode()).getStruct();
            else if (attr.equals("passby"))
            {
                if (((PcmlData)getDocNode()).getPassby() == ProgramParameter.PASS_BY_VALUE)
                    return "value";
                else
                    return "reference";
            }
            else if (attr.equals("bidistringtype"))
            {
                return (((PcmlData)getDocNode()).getBidistringtypeStr());   // @A2C
            }
            else
                return null;
        }
        return null;
    }
}
