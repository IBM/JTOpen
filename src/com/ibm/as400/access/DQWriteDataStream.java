///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DQWriteDataStream.java
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

// Add record to data queue request data stream.
class DQWriteDataStream extends DQDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    DQWriteDataStream(byte[] name, byte[] lib, byte[] key, byte[] entry)
    {
        super((key == null) ? 48 + entry.length : 54 + entry.length + key.length);
        setTemplateLen(22);
        setReqRepID(0x0005);

        // Fill in data queue name and library name.
        setQueueAndLib(name, lib);

        data_[40] = (key == null) ? (byte)0xF0 : (byte)0xF1;
        data_[41] = (byte)0xF1;  // Want reply.

        // Fill in entry data.
        set32bit(6 + entry.length, 42);
        set16bit(0x5001, 46);
        System.arraycopy(entry, 0, data_, 48, entry.length);

        if (key != null)
        {
            // Fill in key.
            set32bit(6 + key.length, 48 + entry.length);
            set16bit(0x5002, 52 + entry.length);
            System.arraycopy(key, 0, data_, 54 + entry.length, key.length);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending add record to data queue request...");
        super.write(out);
    }
}
