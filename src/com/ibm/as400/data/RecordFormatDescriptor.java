///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RecordFormatDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

/**
  *  The RecordFormatDescriptor class implements the methods of the Descriptor interface
  *  that are unique to the &lt;recordformat&gt; tag.
  *
  **/

class RecordFormatDescriptor extends DocNodeDescriptor
{
    /* Constructor */
    public RecordFormatDescriptor(PcmlDocNode node)
    {
        super(node);
    }
   /**
    * Return list of valid attributes for the <recordformat> tag.
    **/
    public String[] getAttributeList()
    {
        return ((RfmlRecordFormat)getDocNode()).getAttributeList();
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
        else if (attr.equals("description"))
          return ((RfmlRecordFormat)getDocNode()).getDescription();
        else return null;
      }
      return null;
    }
}
