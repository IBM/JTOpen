///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxLoadRepCV.java
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
The PxLoadRepCV class represents the client
view of a load reply.
**/
class PxLoadRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


/**
Constructs a PxLoadRepCV object.
**/
    public PxLoadRepCV ()
    { 
        super (ProxyConstants.DS_LOAD_REP);
      
    }


         
/**
Processes the reply.

@return The returned object, or null if none.
**/
    public Object process ()
        throws InvocationTargetException
    {
        int activeConnections   = ((PxIntParm) getParm (0)).getIntValue ();
        int balanceThreshold    = ((PxIntParm) getParm (1)).getIntValue ();
        int maxConnections      = ((PxIntParm) getParm (2)).getIntValue ();

        // Return a load object representing the information returned
        // in the reply.
        return new PSLoad (activeConnections, balanceThreshold, maxConnections);
    }
      

}
