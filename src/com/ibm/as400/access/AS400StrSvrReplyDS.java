///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400StrSvrReplyDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

// A class representing a reply to a "start server" request (class AS400StrSvrDS) data stream.
class AS400StrSvrReplyDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    int getRC()
    {
        return get32bit(20);
    }

    byte[] getUserIdBytes()
    {
        int offset = findCP(0x1104);
        if (offset == -1) return null;

        byte[] userIdBytes = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40};
        System.arraycopy(data_, offset + 10, userIdBytes, 0, get32bit(offset) - 10);
        return userIdBytes;
    }

    byte[] getJobNameBytes()
    {
        int offset = findCP(0x111F);
        if (offset == -1) return new byte[0];

        byte[] jobNameBytes = new byte[get32bit(offset) - 10];
        System.arraycopy(data_, offset + 10, jobNameBytes, 0, jobNameBytes.length);
        return jobNameBytes;
    }

    int findCP(int cp)
    {
        int offset = 24;
        while (offset < data_.length - 1)
        {
            if (get16bit(offset + 4) == cp) return offset;
            offset += get32bit(offset);
        }
        return -1;
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving start server reply..."); //@P0C

        // Receive the header.
        byte[] header = new byte[20];
        if (DataStream.readFromStream(in, header, 0, 20) < 20)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the start server reply header."); //@P0C
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
