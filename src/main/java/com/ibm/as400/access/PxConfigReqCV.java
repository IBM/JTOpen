///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxConfigReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxConfigReqCV class represents the
client view of a configure request.
**/
class PxConfigReqCV
extends PxReqCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxConfigReqCV object.

@param config    The configuration.
**/
    public PxConfigReqCV (PSConfig config)
    {
        super (ProxyConstants.DS_CONFIGURE_REQ);
        addParm (new PxSerializedObjectParm (config.getProperties ()));
        
       
    }



}
