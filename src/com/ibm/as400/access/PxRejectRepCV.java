///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxRejectRepCV.java
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
The PxRejectRepCV class represents the client
view of a reject reply.
**/
class PxRejectRepCV
extends PxRepCV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    // Private data.
    private ProxyClientConnection   connection_;



/**
Constructs a PxRejectRepCV object.

@param connection   The connection.
**/
    public PxRejectRepCV (ProxyClientConnection connection)
    { 
        super (ProxyConstants.DS_REJECT_REP);
        connection_ = connection;
      
    }



/**
Processes the reply.

@return The returned object, or null if none.
**/
    public Object process ()
        throws InvocationTargetException
    {
        String peer = ((PxStringParm) getParm (0)).getStringValue ();
        boolean secure = ((PxBooleanParm) getParm (1)).getBooleanValue ();

        connection_.close ();
        if (peer.length() > 0) {
            connection_.open (peer, secure);
            connection_.connect ();
        }
        else
            throw new ProxyException (ProxyException.CONNECTION_REJECTED);

        return null; 
    }
      

}
