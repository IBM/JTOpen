///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPResHandle.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * Class NPCPResHandle is an internal class (not public) that is used
 * to hold the handle given back to us on an open of an AFP resource.
 **/
class NPCPResHandle extends NPCodePoint
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;



    NPCPResHandle()
    {
	super(NPCodePoint.RESOURCE_HANDLE);
    }

    // add copyright
    static private String getCopyright()
    {
	return Copyright.copyright;
    }

}
