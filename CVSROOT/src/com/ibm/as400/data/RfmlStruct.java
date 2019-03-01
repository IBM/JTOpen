///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlStruct.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

class RfmlStruct extends PcmlStruct {
    /***********************************************************
     Static Members
    ***********************************************************/

    // Serial verion unique identifier
    ///static final long serialVersionUID = 5539999574454926624L;

    private static final String STRUCTATTRIBUTES[] = {
        "name"
    };
    // Note: The following PcmlStruct attributes are irrelevant to this class:
    // usage, count, minvrm, maxvrm, offset, offsetfrom, outputsize.

    /** Constructor. **/
    public RfmlStruct(PcmlAttributeList attrs)
    {
        super(attrs);
    }

   /**
    Returns the list of valid attributes for the data element.
    **/
    String[] getAttributeList()
    {
        return STRUCTATTRIBUTES;
    }
}
