///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SignonExchangeAttributeReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class SignonExchangeAttributeReq extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    SignonExchangeAttributeReq(byte[] seed)
    {
        super(new byte[seed != null ? 52 : 38]);

        setLength(data_.length);
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
        set16bit(2, 36);

        if (seed != null)
        {
            // Set client seed.
            //   LL
            set32bit(14, 38);
            //   CP
            set16bit(0x1103, 42);
            //   Client seed.
            System.arraycopy(seed, 0, data_, 44, 8);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending signon server exchange client/server attributes request..."); //@P0C
        super.write(out);
    }
}
