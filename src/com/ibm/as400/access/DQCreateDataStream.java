///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQCreateDataStream.java
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

// Create data queue request data stream.
class DQCreateDataStream extends DQDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    DQCreateDataStream(byte[] name, byte[] library, int entryLength, String authority, boolean saveSenderInfo, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, byte[] description)
    {
        super(100);
        setReqRepID(0x0003);

        // Fill in data queue name and library name.
        setQueueAndLibrary(name, library);

        set32bit(entryLength, 40);  // Set max entry length.

        // Set authority.
        data_[44] = authority.equals("*LIBCRTAUT") ? (byte)0xF4 : authority.equals("*ALL") ? (byte)0xF0 : authority.equals("*CHANGE") ? (byte)0xF1 : authority.equals("*EXCLUDE") ? (byte)0xF2 : /* authority.equals("*USE") */ (byte)0xF3;
        // Set save sender info flag.
        data_[45] = (saveSenderInfo) ? (byte)0xF1 : (byte)0xF0;
        // Set Queue Type: F0=FIFO, F1=LIFO, F2=KEYED.
        data_[46] = (keyLength == 0) ? (FIFO) ? (byte)0xF0 : (byte)0xF1 : (byte)0xF2;
        // Set key length.
        set16bit(keyLength, 47);
        // Set force.
        data_[49] = (forceToAuxiliaryStorage) ? (byte)0xF1 : (byte)0xF0;
        // Fill in text description.
        System.arraycopy(description, 0, data_, 50, 50);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending create data queue request...");
        super.write(out);
    }
}
