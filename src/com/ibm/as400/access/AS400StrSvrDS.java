///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400StrSvrDS.java
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

// A class representing a "start server" request data stream.
class AS400StrSvrDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    AS400StrSvrDS(int serverId, byte[] userIDbytes, byte[] encryptedPw)
    {
        data_ = new byte[52];

        setLength(52);
        // setHeaderID(0x0000);
        setServerID(serverId);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(2);
        setReqRepID(0x7002);

        data_[20] = 0x01;  // Password encryption on.
        data_[21] = 0x01;  // Send reply true.

        // Set user ID info.
        //   LL
        set32bit(16, 22);
        //   CP
        set16bit(0x1104, 26);
        //   EBCDIC user ID.
        System.arraycopy(userIDbytes, 0, data_, 28, 10);

        // Set password info.
        //   LL
        set32bit(14, 38);
        //   CP
        set16bit(0x1105, 42);
        //   Password data.
        System.arraycopy(encryptedPw, 0, data_, 44, 8);
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending start server request...");
        super.write(out);
    }
}
