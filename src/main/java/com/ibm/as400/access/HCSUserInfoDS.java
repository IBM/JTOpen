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

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class HCSUserInfoDS extends ClientAccessDataStream
{
    HCSUserInfoDS()
    {
        super(new byte[44]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE00B);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(24);
        setReqRepID(0x7104);

        // Return function registration timestamps.
        //   LL
        set32bit(7, 20);
        //   CP
        set16bit(0x110F, 24);
        //   1-byte flag. 0x01=return timestamps
        data_[26] = 0x01;
        
        // Client CCSID.
        //   LL
        set32bit(10, 27);
        //   CP
        set16bit(0x1113, 31);
        //   CCSID
        set32bit(1200, 33);    
        
        // Return messages indicator
        set32bit(7, 37);
        //   CP
        set16bit(0x1128, 41);
        //   1-byte flag. 0x01=return messages; 0x00=do not return messages
        data_[43] = 0x01;
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending retrieve user information request...");
        super.write(out);
    }

    public void clear() {
       CredentialVault.clearArray(data_); 
    }
}
