///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQReadDataStream.java
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

// Receive record from data queue request data stream.
class DQReadDataStream extends DQDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    DQReadDataStream(byte[] name, byte[] library, byte[] search, int wait, boolean peek, byte[] key)
    {
        super((key == null) ? 48 : 54 + key.length);
        setTemplateLen(28);
        setReqRepID(0x0002);

        // Fill in data queue name and library name.
        setQueueAndLibrary(name, library);

        data_[40] = (key == null) ? (byte)0xF0 : (byte)0xF1;
        System.arraycopy(search, 0, data_, 41, 2);
        set32bit(wait, 43);
        data_[47] = peek  ? (byte)0xF1 : (byte)0xF0;

        if (key != null)
        {
            // Fill in key.
            set32bit(6 + key.length, 48);
            set16bit(0x5002, 52);
            System.arraycopy(key, 0, data_, 54, key.length);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending receive record from data queue request...");
        super.write(out);
    }
}
