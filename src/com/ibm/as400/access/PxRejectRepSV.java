///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxRejectRepSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxRejectRepSV class represents the
server view of a reject reply.
**/
class PxRejectRepSV
extends PxRepSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxRejectRepSV object.

@param peer     The suggested peer, or "" if none.
@param secure   true if this is rejecting a secure connection,
                false otherwise.
**/
    public PxRejectRepSV (String peer, boolean secure)
    {
        super (ProxyConstants.DS_REJECT_REP);
        addParm (new PxStringParm (peer));
        addParm (new PxBooleanParm (secure));

        String x = Copyright.copyright;
    }



}
