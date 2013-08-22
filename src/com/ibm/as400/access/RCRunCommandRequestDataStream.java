///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RCRunCommandRequestDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class RCRunCommandRequestDataStream extends ClientAccessDataStream
{
    RCRunCommandRequestDataStream(byte[] commandBytes, int dataStreamLevel, int messageCount, int ccsid)
    {
        super(new byte[dataStreamLevel >= 10 ? 31 + commandBytes.length : 27 + commandBytes.length]);
        setLength(dataStreamLevel >= 10 ? 31 + commandBytes.length : 27 + commandBytes.length);
        // setHeaderID(0x0000);
        setServerID(0xE008);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);  // Template length is one byte.
        setReqRepID(0x1002);

        // Return messages.
        //@P1A - Start
        // @A Server data stream level 11 is added, and message count sets to 5 if up to 10 msgs returned, and 6 if all returned.  
        if (dataStreamLevel < 7 && messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = AS400Message.MESSAGE_OPTION_UP_TO_10;
        if (dataStreamLevel >= 10 && dataStreamLevel < 11)
        {
            if (messageCount == AS400Message.MESSAGE_OPTION_UP_TO_10) messageCount = 3;
            if (messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = 4;
        }
        if (dataStreamLevel >= 11) {
          if (messageCount == AS400Message.MESSAGE_OPTION_UP_TO_10) messageCount = 5;
          if (messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = 6;
        }
        //@P1A - End
        data_[20] = (byte)messageCount;

        if (dataStreamLevel >= 10)
        {
            set32bit(10 + commandBytes.length, 21);  // Set LL = 4 bytes LL, 2 bytes CP, 4 bytes CCSID + length of command.
            set16bit(0x1104, 25);  // Set CP.
            set32bit(ccsid, 27);
            System.arraycopy(commandBytes, 0, data_, 31, commandBytes.length);
        }
        else
        {
            set32bit(6 + commandBytes.length, 21);  // Set LL = 4 bytes LL, 2 bytes CP + length of command.
            set16bit(0x1101, 25);  // Set CP.
            System.arraycopy(commandBytes, 0, data_, 27, commandBytes.length);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending run command request...");
        super.write(out);
    }
}
