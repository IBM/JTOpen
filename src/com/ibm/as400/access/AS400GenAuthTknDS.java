///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400GenAuthTknDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// The AS400GenAuthTknDS class represents the data stream for the 'Generate authentication token' request.
class AS400GenAuthTknDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    AS400GenAuthTknDS(byte[] userIDbytes, byte[] authenticationBytes, int byteType, int profileTokenType, int profileTokenTimeout)
    {
        super(new byte[(userIDbytes == null) ? 45 + authenticationBytes.length : 61 + authenticationBytes.length]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0002);
        setReqRepID(0x7007);

        // Type of authentication bytes.
        data_[20] = (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ? (authenticationBytes.length == 8) ? (byte)0x01 : (byte)0x03 : (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN) ? (byte)0x05 : (byteType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN) ? (byte)0x06 : (byte)0x02;
        // Return type, 0x01 = profile token.
        data_[21] = 0x01;

        // Profile token type.
        set32bit(7, 22);
        set16bit(0x1116, 26);
        data_[28] = (byte)(0xF0 | profileTokenType);

        // Experation interval.
        set32bit(10, 29);
        set16bit(0x1117, 33);
        set32bit(profileTokenTimeout, 35);

        // Set password or authentication token.
        //   LL
        set32bit(6 + authenticationBytes.length, 39);
        //   CP
        if (byteType == 0)
        {
            set16bit(0x1105, 43);
        }
        else
        {
            set16bit(0x1115, 43);
        }
        //   Data.
        System.arraycopy(authenticationBytes, 0, data_, 45, authenticationBytes.length);

        if (userIDbytes != null)
        {
            // Set user ID info.
            //   LL
            set32bit(16, 45 + authenticationBytes.length);
            //   CP
            set16bit(0x1104, 49 + authenticationBytes.length);
            //   EBCDIC user ID.
            System.arraycopy(userIDbytes, 0, data_, 51 + authenticationBytes.length, 10);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending generate authentication token request...");
        super.write(out);
    }
}
