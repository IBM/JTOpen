///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxConstructorReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;



/**
The PxConstructorReqSV class represents the
server view of a constructor request.
**/
class PxConstructorReqSV
extends PxReqSV 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final Object[]   noArguments_        = new Object[0];
    private static final boolean[]  noReturnArguments_  = new boolean[0];

    private PxTable             pxTable_;



    public PxConstructorReqSV(PxTable pxTable)
    { 
        super (ProxyConstants.DS_CONSTRUCTOR_REQ);
        pxTable_    = pxTable;

        String x = Copyright.copyright;
    }



    public PxRepSV process()
    {      
        // Get the information from the datastream parameters.
        String className = ((PxStringParm) getParm (0)).getStringValue ();
        boolean flag = ((PxBooleanParm)getParm(1)).getBooleanValue();

        // Construct the object.
        String classNameToLoad = (flag ? (className + "ImplRemote") : className);
        Object object = AS400.loadImpl (classNameToLoad);

        // Add the object to the object table.
        long proxyId = pxTable_.addClientId (getClientId(), object);  //@A1C
   
        // Return the proxy id.
        return new PxReturnRepSV (pxTable_, object, noArguments_, noReturnArguments_);
    }


}
