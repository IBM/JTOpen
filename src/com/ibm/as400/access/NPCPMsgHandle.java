///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPMsgHandle.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * Class NPCPMsgHandle is an internal class (not public) that is used
  * to hold the handle given back to us on a reply to a message.
  **/
class NPCPMsgHandle extends NPCodePoint
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    NPCPMsgHandle()
    {
        super(NPCodePoint.MESSAGE_HANDLE);
    }

    // add copyright
    static private String getCopyright()
    {
        return Copyright.copyright;
    }

}
