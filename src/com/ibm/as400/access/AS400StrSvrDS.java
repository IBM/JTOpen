///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400StrSvrDS.java
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

// A class representing a "start server" request data stream.
class AS400StrSvrDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    AS400StrSvrDS(int serverId, byte[] userIDbytes, byte[] authenticationBytes, int byteType)
    {
        super(new byte[(userIDbytes == null) ? 28 + authenticationBytes.length : 44 + authenticationBytes.length]);

        setLength(data_.length);
        // Header ID replaced with Attributes.
        data_[4] = 0x02;  // Client Attributes, 2 means can get job info back.
        // data_[5] = 0x00;  // Server Attributes.
        setServerID(serverId);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(2);
        setReqRepID(0x7002);

        data_[20] = (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ? (authenticationBytes.length == 8) ? (byte)0x01 : (byte)0x03 : (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN) ? (byte)0x05 : (byteType == AS400.AUTHENTICATION_SCHEME_AUTHENTICATION_TOKEN) ? (byte)0x06 : (byte)0x02;
        data_[21] = 0x01;  // Send reply true.

        // Set password or authentication token.
        //   LL
        set32bit(6 + authenticationBytes.length, 22);
        //   CP
        if (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD)
        {
            set16bit(0x1105, 26);
        }
        else
        {
            set16bit(0x1115, 26);
        }
        //   Data.
        System.arraycopy(authenticationBytes, 0, data_, 28, authenticationBytes.length);

        if (userIDbytes != null)
        {
            // Set user ID info.
            //   LL
            set32bit(16, 28 + authenticationBytes.length);
            //   CP
            set16bit(0x1104, 32 + authenticationBytes.length);
            //   EBCDIC user ID.
            System.arraycopy(userIDbytes, 0, data_, 34 + authenticationBytes.length, 10);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending start server request...");
        super.write(out);
    }
}
