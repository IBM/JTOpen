///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxFinalizeReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxFinalizeReqCV class represents the
client view of a finalize request.
**/
class PxFinalizeReqCV
extends PxReqCV
{  
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    
    
/**
Constructs a PxFinalizeReqCV object.

@param proxyId      The proxy id.
**/
    public PxFinalizeReqCV (long proxyId)
    {  
        super (ProxyConstants.DS_FINALIZE_REQ);
        addParm (new PxPxObjectParm (proxyId));

        
    }



}
