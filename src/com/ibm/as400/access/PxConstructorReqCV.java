///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxConstructorReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.Constructor;



/**
The PxConstructorReqCV class represents the
client view of a constructor request.
**/
class PxConstructorReqCV
extends PxReqCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxConstructorReqCV object.

@param className        The class name.
@param  flag                true to tack on ImplRemote to the class name,
                            false to use the class name, as-is.
**/
    public PxConstructorReqCV (String className, boolean flag)
    {
        super (ProxyConstants.DS_CONSTRUCTOR_REQ);

        addParm (new PxStringParm (className));
        addParm (new PxBooleanParm (flag));

        
    }



}
