///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * Class NPCPData is an internal class (not public) that is used
 * to get raw data to and from the server (things like reading and
 * writing spooled files use it).
 **/
class NPCPData extends NPCodePoint
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    NPCPData()
    {
        super(NPCodePoint.DATA);
    }

    // add copyright
    static private String getCopyright()
    {
        return Copyright.copyright;
    }

}
