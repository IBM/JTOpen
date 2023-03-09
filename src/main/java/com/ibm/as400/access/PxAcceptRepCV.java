///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxAcceptRepCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;



/**
The PxAcceptRepCV class represents the client
view of an accept reply.
**/
class PxAcceptRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


/**
Constructs a PxAcceptRepCV object.
**/
    public PxAcceptRepCV ()
    { 
        super (ProxyConstants.DS_ACCEPT_REP);
        
    }



/**
Processes the reply.

@return The returned object, or null if none.
**/
    public Object process ()
        throws InvocationTargetException
    {
        // No processing is needed.
        return null; 
    }
      

}
