///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxConnectReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxConnectReqCV class represents the
client view of a connect request.
**/
class PxConnectReqCV
extends PxReqCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxConnectReqCV object.

@param modification     The modification level of the
                        IBM Toolbox for Java.
@param rejections       The number of rejections.  This is the
                        number of times the client has tried
                        to connect to a proxy server and been
                        rejected.
@param clientLocale     The client locale.
**/
    public PxConnectReqCV (String  modification,
                           int     rejections,
                           String  clientLocale,
                           short   normalOrTunnel)         // @D1a
    {
        super (normalOrTunnel);                            // @D1c
        addParm (new PxStringParm (modification));
        addParm (new PxIntParm (rejections));
        addParm (new PxStringParm (clientLocale));


    }



}
