///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SignonGenAuthTokenRequestDS.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// The SignonGenAuthTokenRequestDS class represents the data stream for the 'Generate authentication token on behalf of another user' request.
class SignonGenAuthTokenRequestDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    SignonGenAuthTokenRequestDS(byte[] userIdentity, int profileTokenType, int profileTokenTimeout)
    {
        super(new byte[51 + userIdentity.length]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);
        setReqRepID(0x7008);

        // Authentication token return type.
        data_[20] = 0x01;  // 0x01 - profile token.

        // Profile token type.
        set32bit(7, 21);
        set16bit(0x1116, 25);
        data_[27] = (byte)(0xF0 | profileTokenType);

        // Experation interval.
        set32bit(10, 28);
        set16bit(0x1117, 32);
        set32bit(profileTokenTimeout, 34);

        // Type of user identity.
        set32bit(7, 38);
        set16bit(0x1126, 42);
        data_[44] = (byte)0xF1;

        // Set user identity.
        //   LL
        set32bit(6 + userIdentity.length, 45);
        //   CP
        set16bit(0x1127, 49);
        //   Data.
        System.arraycopy(userIdentity, 0, data_, 51, userIdentity.length);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending generate authentication token on behalf of another user request...");
        super.write(out);
    }
}
