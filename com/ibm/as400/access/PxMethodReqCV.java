///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxMethodReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.Method;



/**
The PxMethodReqCV class represents the
client view of a method request.
**/
class PxMethodReqCV
extends PxReqCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    
/**
Constructs a PxMethodReqCV object.

@param proxyId          The proxy id.
@param methodName       The method name.
@param argumentClasses  The argument classes.
@param arguments        The arguments.
@param returnArguments  Whether return arguments are needed, or
                        null if none are needed.
@param asynchronous     true if asynchronous, false otherwise.
@param factory          true if method creates a proxy object, false otherwise.
**/
    public PxMethodReqCV(long proxyId,
                         String methodName,
                         Class[] argumentClasses,
                         Object[] arguments,
                         boolean[] returnArguments,
                         boolean asynchronous,
                         boolean factory)
    {
        super (ProxyConstants.DS_METHOD_REQ, asynchronous);
        addParm (new PxPxObjectParm (proxyId));
        addParm (new PxStringParm (methodName));
        addParm (new PxBooleanParm (factory));

        int argumentCount = argumentClasses.length;
        addParm (new PxIntParm (argumentCount));
        for (int i = 0; i < argumentCount; ++i)
            addParm (new PxClassParm (argumentClasses[i]));
        for (int i = 0; i < argumentCount; ++i) 
            addObjectParm (arguments[i]);
        for (int i = 0; i < argumentCount; ++i)
            if (returnArguments != null)
                addParm(new PxBooleanParm(returnArguments[i]));
            else
                addParm(new PxBooleanParm(false));

        
    }



}
