///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCRunCommandRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1999-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class RCRunCommandRequestDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1999-2003 International Business Machines Corporation and others.";

    RCRunCommandRequestDataStream(byte[] commandBytes, int dataStreamLevel, int messageCount)
    {
        super(new byte[27 + commandBytes.length]);
        setLength(27 + commandBytes.length);
        // setHeaderID(0x0000);
        setServerID(0xE008);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);  // Template length is one byte.
        setReqRepID(0x1002);

        // Return messages.
        if (dataStreamLevel < 7 && messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = AS400Message.MESSAGE_OPTION_UP_TO_10;
        data_[20] = (byte)messageCount;

        set32bit(6 + commandBytes.length, 21);  // Set LL = 4 bytes LL, 2 bytes CP + length of command.
        set16bit(0x1101, 25);  // Set CP.
        System.arraycopy(commandBytes, 0, data_, 27, commandBytes.length);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending run command request...");
        super.write(out);
    }
}
