///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonInfoReq.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class SignonInfoReq extends ClientAccessDataStream
{
    SignonInfoReq(byte[] userIDbytes, byte[] authenticationBytes, int byteType, int serverLevel)
    {
        super(new byte[37 + authenticationBytes.length + (userIDbytes == null ? 0 : 16) + (serverLevel < 5 ? 0 : 7)]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);
        setReqRepID(0x7004);

        // Password's always encrypted.
        data_[20] = (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ? (authenticationBytes.length == 8) ? (byte)0x01 : (byte)0x03 : (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN) ? (byte)0x05 : (byteType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN) ? (byte)0x06 : (byte)0x02;

        // Client CCSID.
        //   LL
        set32bit(10, 21);
        //   CP
        set16bit(0x1113, 25);
        //   CCSID
        set32bit(1200, 27);    // Client CCSID.

        // Set password or authentication token.
        //   LL
        set32bit(6 + authenticationBytes.length, 31);
        //   CP
        if (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD)
        {
            set16bit(0x1105, 35);
        }
        else
        {
            set16bit(0x1115, 35);
        }
        //   Data.
        System.arraycopy(authenticationBytes, 0, data_, 37, authenticationBytes.length);

        if (userIDbytes != null)
        {
            // Set user ID info.
            //   LL
            set32bit(16, 37 + authenticationBytes.length);
            //   CP
            set16bit(0x1104, 41 + authenticationBytes.length);
            //   EBCDIC user ID.
            System.arraycopy(userIDbytes, 0, data_, 43 + authenticationBytes.length, 10);
        }

        if (serverLevel >= 5)
        {
            int offset = 37 + authenticationBytes.length + (userIDbytes == null ? 0 : 16);
            // Set return error messages.
            //   LL
            set32bit(7, offset);
            //   CP
            set16bit(0x1128, offset + 4);
            //   Data.
            data_[offset + 6] = 0x01;
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending retrieve signon information request...");
        super.write(out);
    }
}
