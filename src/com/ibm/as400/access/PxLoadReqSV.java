///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxLoadReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxLoadReqSV class represents the
server view of a load request.
**/
class PxLoadReqSV
extends PxReqSV 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private PSLoad load_;



/**
Constructs a PxLoadReqSV object.

@param load The load.
**/
    public PxLoadReqSV (PSLoad load)
    {
        super (ProxyConstants.DS_LOAD_REQ);
        load_ = load;

        String x = Copyright.copyright;
    }


                                  
/**
Processes the request.

@return The corresponding reply, or null if none.
**/
    public PxRepSV process ()
    {
        return new PxLoadRepSV (load_);
    }


}

