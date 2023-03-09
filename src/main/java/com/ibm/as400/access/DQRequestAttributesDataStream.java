///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQRequestAttributesDataStream.java
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

// Query data queue attributes request data stream.
class DQRequestAttributesDataStream extends DQDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    DQRequestAttributesDataStream(byte[] name, byte[] library)
    {
        super(40);
        setReqRepID(0x0001);

        // Fill in data queue name and library name.
        setQueueAndLibrary(name, library);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending query data queue attributes request...");
        super.write(out);
    }
}
