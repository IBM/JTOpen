///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

class RfmlData extends PcmlData {
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


    // New attributes should be added to the end of this array
    private static final String DATAATTRIBUTES[] = {
        "name",
        "count",
        "type",
        "length",
        "precision",
        "ccsid",
        "init",
        "struct",
        "bidistringtype"
    };
    // Note: The following PcmlData attributes are irrelevant to this class:
    // usage, minvrm, maxvrm, offset, offsetfrom, outputsize, passby.

    /** Constructor with description. **/
    RfmlData(PcmlAttributeList attrs)
    {
        super(attrs, true);
    }

   /**
    Returns the list of valid attributes for the data element.
    **/
    String[] getAttributeList()
    {
      return DATAATTRIBUTES;
    }
}
