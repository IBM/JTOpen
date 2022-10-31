///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IFSPingReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2008-2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

/**
 Note: The File Server doesn't yet support or recognize a "ping" request.
 This class simply provides a dummy request that we can send to the File Server, to elicit a reply.
 We expect the File Server to reject this request with a reply (class IFSReturnCodeRep) indicating "request not supported".
 That's fine, because when we receive the reply, we've verified that the connection is still alive.
 **/
class IFSPingReq extends IFSDataStreamReq
{
  private static final int TEMPLATE_LENGTH = 6;

    IFSPingReq()
    {
        super(20 + TEMPLATE_LENGTH);

        setLength(data_.length);
        setTemplateLen(TEMPLATE_LENGTH);
        // For consistencey, we set the request ID to that of the Signon Server's "Ping Request with Echo" (class SignonPingReq).
        // Note that this request ID isn't recognized by the File Server.
        setReqRepID(0x7FFE);
    }

}
