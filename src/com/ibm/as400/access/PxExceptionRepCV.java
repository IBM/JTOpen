///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxExceptionRepCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;



/**
The PxExceptionRepCV class represents the client
view of an excpetion reply.
**/
class PxExceptionRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


/**
Constructs a PxExceptionRepCV object.
**/
    public PxExceptionRepCV ()
    {  
        super (ProxyConstants.DS_EXCEPTION_REP);
        
    }



/**
Processes the reply.  This always throws an exception,
namely the exception contained in the reply.

@return The returned object, or null if none.
**/
    public Object process ()
        throws InvocationTargetException
    {
        Throwable e = ((Throwable) (((PxParm) getParm (0)).getObjectValue ())).fillInStackTrace ();

        if (Trace.isTraceErrorOn ()) {
            String stackTrace = ((PxStringParm) getParm (1)).getStringValue ();
            Trace.log (Trace.ERROR, "Exception thrown on proxy server: " + stackTrace, e);
        }

        throw new InvocationTargetException (e);
    }
}


