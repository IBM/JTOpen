///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DQCreateDataStream.java
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

// Create data queue request data stream.
class DQCreateDataStream extends DQDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    DQCreateDataStream(byte[] name, byte[] lib, int entryLength, String authority, boolean saveSenderInfo, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, byte[] description)
    {
        super(100);
        setReqRepID(0x0003);

        // Fill in data queue name and library name.
        setQueueAndLib(name, lib);

        set32bit(entryLength, 40);  // Set max entry length.

        // Set authority.
        if (authority.equals("*ALL"))
            data_[44] = (byte)0xF0;
        else if (authority.equals("*CHANGE"))
            data_[44] = (byte)0xF1;
        else if (authority.equals("*EXCLUDE"))
            data_[44] = (byte)0xF2;
        else if (authority.equals("*USE"))
            data_[44] = (byte)0xF3;
        else // (authority.equals("*LIBCRTAUT"))
            data_[44] = (byte)0xF4;

        data_[45] = (saveSenderInfo) ? (byte)0xF1 : (byte)0xF0;  // Set save sender info flag.
        data_[46] = (keyLength == 0) ? (FIFO) ? (byte)0xF0 : (byte)0xF1 : (byte)0xF2;  // Set Queue Type F0=FIFO F1=LIFO F2=KEYED.
        set16bit(keyLength, 47);     // Set key length.
        data_[49] = (forceToAuxiliaryStorage) ? (byte)0xF1 : (byte)0xF0;  // Set force.
        System.arraycopy(description, 0, data_, 50, 50);  // Fill in text description.
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending create data queue request...");
        super.write(out);
    }
}
