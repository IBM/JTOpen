///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCRunCommandRequestDataStream.java
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

class RCRunCommandRequestDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    RCRunCommandRequestDataStream(byte[] cmdBytes)
    {
        super(new byte[27 + cmdBytes.length]);
        setLength(27 + cmdBytes.length);
        // setHeaderID(0x0000);
        setServerID(0xE008);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);  // Template length is one byte.
        setReqRepID(0x1002);

        // data_[20] = 0x00;  // Always return messages.

        set32bit(6 + cmdBytes.length, 21);  // Set LL = 4 bytes LL, 2 bytes CP + length of command.
        set16bit(0x1101, 25);  // Set CP.
        System.arraycopy(cmdBytes, 0, data_, 27, cmdBytes.length);
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending run command request...");
        super.write(out);
    }
}
