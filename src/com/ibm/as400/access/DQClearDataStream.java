///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DQClearDataStream.java
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

// Clear data queue request data stream.
class DQClearDataStream extends DQDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    DQClearDataStream(byte[] name, byte[] lib, byte[] key)
    {
        super((key == null) ? 41 : 47 + key.length);
        setTemplateLen(21);
        setReqRepID(0x0006);

        // Fill in data queue name and library name.
        setQueueAndLib(name, lib);

        data_[40] = (key == null) ? (byte)0xF0 : (byte)0xF1;

        if (key != null)
        {
            // Fill in key.
            set32bit(6 + key.length, 41);
            set16bit(0x5002, 45);
            System.arraycopy(key, 0, data_, 47, key.length);
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending clear data queue request...");
        super.write(out);
    }
}
