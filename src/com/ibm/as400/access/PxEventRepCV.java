///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxEventRepCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;



/**
The PxEventRepCV class represents the client
view of an event reply.
**/
class PxEventRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    // Private data.
    private PxEventSupport eventSupport_;



/**
Constructs a PxEventRepCV object.

@param eventSupport The event support.
**/
    public PxEventRepCV (PxEventSupport eventSupport)
    { 
        super (ProxyConstants.DS_EVENT_REP);

        eventSupport_ = eventSupport;

        
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
            PxPxObjectParm eventSourceParm = (PxPxObjectParm) getParm (0);
            long pxId = eventSourceParm.getPxId ();
            String listenerInterfaceName = ((PxStringParm) getParm (1)).getStringValue ();
            String listenerMethodName = ((PxStringParm) getParm (2)).getStringValue ();
            EventObject eventObject = (EventObject) ((PxParm) getParm (3)).getObjectValue ();

            // Note: The event source never gets set!  

            // Fire the event.
            eventSupport_.fireEvent (pxId, listenerInterfaceName, listenerMethodName, eventObject);
            return null;
        }
        catch (Exception e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Exception occured while processing event reply", e);
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        }
    }
      

}
