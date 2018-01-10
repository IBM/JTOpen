///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQExchangeAttributesDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// Data queue exchange client/server attributes request datastream.
class DQExchangeAttributesDataStream extends DQDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    DQExchangeAttributesDataStream()
    {
        super(26);
        // setReqRepID(0x0000);
        set32bit(0x00000001, 20);  // Client version, 1 means we support 64K data queues.
        // set16bit(0x0000, 24);  // Client datastream level, always 0.
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending data queue exchange client/server attributes request...");
        super.write(out);
    }
}
