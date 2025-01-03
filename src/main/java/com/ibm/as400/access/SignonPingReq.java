///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SignonPingReq.java
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

class SignonPingReq extends ClientAccessDataStream
{
    SignonPingReq()
    {
        super(new byte[20]);

        setLength(20);
        setServerID(0xE009);
        setReqRepID(0x7FFE);
    }

    public SignonPingReq(int data)
    {
        super(new byte[24]);
        setLength(24);
        setServerID(0xE009);
        setReqRepID(0x7FFE);
        set32bit(data, 20);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending ping request...");
        super.write(out);
    }
}
