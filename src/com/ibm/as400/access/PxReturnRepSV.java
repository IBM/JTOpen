///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxReturnRepSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxReturnRepSV class represents the
server view of an return reply.
**/
class PxReturnRepSV
extends PxRepSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    public PxReturnRepSV(PxTable pxTable,
                         Object returnValue,
                         Object[] arguments,
                         boolean[] returnArguments)
    {
        super (ProxyConstants.DS_RETURN_REP);
        addObjectParm (pxTable, returnValue);
        addParm (new PxIntParm (arguments.length));
        for (int i = 0; i < arguments.length; ++i) {
            if (returnArguments[i])
                addObjectParm(pxTable, arguments[i]);
            else
                addParm(new PxNullParm());
        }

        String x = Copyright.copyright;
    }



}
