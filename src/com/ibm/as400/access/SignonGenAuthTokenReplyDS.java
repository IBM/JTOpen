///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SignonGenAuthTokenReplyDS.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

// The SignonGenAuthTokenReplyDS class represents the data stream for the 'Generate authentication token on behalf of another user' reply.
class SignonGenAuthTokenReplyDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    int getRC()
    {
        return get32bit(20);
    }

    byte[] getProfileTokenBytes()
    {
        byte[] token = new byte[32];
        System.arraycopy(data_, 30, token, 0, 32);
        return token;
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving generate authentication token on behalf of another user reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (DataStream.readFromStream(in, header, 0, 20) < 20)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the generate authentication token on behalf of another user reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
