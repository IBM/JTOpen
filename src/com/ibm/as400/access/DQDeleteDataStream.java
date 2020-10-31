///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQDeleteDataStream.java
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

// Delete data queue request data stream.
// The reply to a delete request is a DQCommonReplyDataStream.
class DQDeleteDataStream extends DQDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    DQDeleteDataStream(byte[] name, byte[] library)
    {
        super(40);
        setReqRepID(0x0004);

        // Fill in data queue name and library name.
        setQueueAndLibrary(name, library);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending delete data queue request...");
        super.write(out);
    }
}
