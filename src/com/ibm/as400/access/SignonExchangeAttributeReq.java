///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SignonExchangeAttributeReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class SignonExchangeAttributeReq extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    SignonExchangeAttributeReq()
    {
        super(new byte[52]);

        setLength(52);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        // setTemplateLen(0x0000);
        setReqRepID(0x7003);

        // Set client version.
        //   LL
        set32bit(10, 20);
        //   CP
        set16bit(0x1101, 24);
        //   Client version.
        set32bit(1, 26);

        // Set client data stream level.
        //   LL
        set32bit(8, 30);
        //   CP
        set16bit(0x1102, 34);
        //   Client level.
        set16bit(1, 36);  // Set to 1 means we support SHA-1 passwords.

        // Set client seed.
        //   LL
        set32bit(14, 38);
        //   CP
        set16bit(0x1103, 42);
        //   Client seed.
        long t = System.currentTimeMillis();

        // Performance: break into 2 ints first and avoid long temporaries.
        int high = (int)(t >>> 32);
        int low = (int)t;

        data_[44] = (byte)(high >>> 24);
        data_[45] = (byte)(high >>> 16);
        data_[46] = (byte)(high >>> 8);
        data_[47] = (byte)high;

        data_[48] = (byte)(low >>> 24);
        data_[49] = (byte)(low >>> 16);
        data_[50] = (byte)(low >>> 8);
        data_[51] = (byte)low;
    }

    byte[] getClientSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 44, seed, 0, 8);
        return seed;
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending signon server exchange client/server attributes request...");
        super.write(out);
    }
}
