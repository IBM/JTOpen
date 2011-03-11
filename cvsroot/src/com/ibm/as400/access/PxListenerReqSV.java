///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxListenerReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;



/**
The PxListenerReqSV class represents the
server view of a listener request.
**/
class PxListenerReqSV
extends PxReqSV 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private PSConnection    connection_;
    private PxTable         proxyTable_;



/**
Constructs a PxListenerReqSV object.
**/
    public PxListenerReqSV (PSConnection connection, PxTable proxyTable)
    { 
        super (ProxyConstants.DS_LISTENER_REQ);

        connection_ = connection;
        proxyTable_ = proxyTable;
    }



    private static final String operationToString (int operation)
    {
        switch (operation) {
        case ProxyConstants.LISTENER_OPERATION_ADD:
            return "add";
        case ProxyConstants.LISTENER_OPERATION_REMOVE:
            return "remove";
        default:
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Invalid listener operation: " + operation);
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        }            
    }



/**
Processes the request.

@return The corresponding reply, or null if none.
**/
    public PxRepSV process ()
    {      
        try {
            // Get the information from the datastream parameters.
            Object proxy = ((PxPxObjectParm) getParm (0)).getObjectValue ();            
            int operation = ((PxIntParm) getParm (1)).getIntValue ();
            String eventName = ((PxStringParm) getParm (2)).getStringValue ();                         
    
            // Create a PSXxxListener object.
            String listenerName = eventName + "Listener";
            Class listenerInterface = Class.forName ("com.ibm.as400.access." + listenerName);
            Class listenerClass = Class.forName ("com.ibm.as400.access.PS" + listenerName);
            Constructor constructor = listenerClass.getConstructor (new Class[] { PSConnection.class, PxTable.class, Long.TYPE });
            long proxyId = proxyTable_.get (proxy);
            Object listener = constructor.newInstance (new Object[] { connection_, proxyTable_, new Long (proxyId) });
    
            // Add/remove a PSXxxListener to/from the object.                
            Class proxyClass = proxy.getClass ();
            Method xxxListener = proxyClass.getMethod (operationToString (operation) + listenerName, new Class[] { listenerInterface });
            xxxListener.invoke (proxy, new Object[] { listener });
    
            // No reply expected.
            return null;  
        }
        catch (Exception e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.toString (), e);
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        }
    }


}
