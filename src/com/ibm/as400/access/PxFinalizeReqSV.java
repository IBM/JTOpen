///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxFinalizeReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxFinalizeReqSV class represents the
server view of a finalize request.
**/
class PxFinalizeReqSV
extends PxReqSV 
{  
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    
    
    // Private data.
    private PxTable proxyTable_;



/**
Constructs a PxFinalizeReqSV object.

@param proxyTable   The proxy table.
**/
    public PxFinalizeReqSV (PxTable proxyTable)
    { 
        super (ProxyConstants.DS_FINALIZE_REQ);
        proxyTable_ = proxyTable;

        String x = Copyright.copyright;
    }



/**
Processes the request.

@return The corresponding reply, or null if none.
**/
    public PxRepSV process ()
    {      
        // Get the information from the datastream parameters.
        Object proxy = ((PxPxObjectParm) getParm (0)).getObjectValue ();            
 
        // Remove the object from the table.
        proxyTable_.remove (proxy);
    
        // Return null (no reply).
        return null;
    }


}
