///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxLoadRepSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxLoadRepSV class represents the
server view of a load reply.
**/
class PxLoadRepSV
extends PxRepSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxLoadRepSV object.

@param load The load.
**/
    public PxLoadRepSV (PSLoad load)
    {
        super (ProxyConstants.DS_LOAD_REP);
        addParm (new PxIntParm (load.getActiveConnections ()));
        addParm (new PxIntParm (load.getBalanceThreshold ()));
        addParm (new PxIntParm (load.getMaxConnections ()));
    }



}
