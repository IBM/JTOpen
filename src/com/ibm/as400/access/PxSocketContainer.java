///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxSocketContainer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.Socket;



/**
The PxSocketContainer class represents a wrapper around
a socket.
**/
class PxSocketContainer
extends PxSocketContainerAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxSocketContainer object.

@param hostName The host name.
@param port     The port.

@exception IOException  If an error occurs.
**/
    public PxSocketContainer (String hostName, int port)
        throws IOException
    {        
        super (new Socket (hostName, port));
      
    }


}
