///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

/**
  *  The ProgramDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the <program> tag.
  *
  **/

class ProgramDescriptor extends DocNodeDescriptor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /* Constructor */
    public ProgramDescriptor(PcmlDocNode node)
    {
        super(node);
    }
   /**
    * Return list of valid attributes for the <pcml> tag.
    **/
    public String[] getAttributeList()
    {
        return ((PcmlProgram)getDocNode()).getAttributeList();
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
            else if (attr.equals("path"))
                return ((PcmlProgram)getDocNode()).getPath();
            else if (attr.equals("parseorder"))
                return ((PcmlProgram)getDocNode()).getParseorder();
            else if (attr.equals("entrypoint"))
                return ((PcmlProgram)getDocNode()).getEntrypoint();
            else if (attr.equals("returnvalue"))
                return ((PcmlProgram)getDocNode()).getReturnvalue();
            else if (attr.equals("threadsafe"))                     // @A1A
                return ((PcmlProgram)getDocNode()).getThreadsafe(); // @A1A
            else
                return null;
        }
        return null;
    }
}
