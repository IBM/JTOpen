///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxReturnRepCV.java
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
The PxReturnRepCV class represents the client
view of a return reply.
**/
class PxReturnRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    // Private data.



/**
Constructs a PxReturnRepCV object.
**/
    public PxReturnRepCV ()
    { 
        super (ProxyConstants.DS_RETURN_REP);
       
    }



/**
Processes the reply.

@return The returned object, or null if none.
**/
    public Object process ()
        throws InvocationTargetException
    {
        try {
            // Gather the contents of the datastream.
            PxParm returnValue = getParm (0);
            int argumentCount = ((PxIntParm) getParm (1)).getIntValue ();
            PxParm[] arguments = new PxParm[argumentCount];
            for (int i = 0, j = 2; i < argumentCount; ++i, ++j)
                arguments[i] = getParm (j);
            return new ProxyReturnValue (returnValue, arguments);
        }
        catch (Exception e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.getMessage (), e);
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        }
    }
      

}
