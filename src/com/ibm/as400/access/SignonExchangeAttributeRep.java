///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SignonExchangeAttributeRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class SignonExchangeAttributeRep extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    int getRC()
    {
        return get32bit(20);
    }

    int getServerVersion()
    {
        return get32bit(30);
    }

    int getServerLevel()
    {
        return get16bit(40);
    }

    byte[] getServerSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 48, seed, 0, 8);
        return seed;
    }

    boolean getPasswordLevel()
    {
        if (data_.length <= 56) return false;
        return data_[62] >= 2;
    }

    void read(InputStream in) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Receiving signon server exchange client/server attributes reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (DataStream.readFromStream(in, header, 0, 20) < 20)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the signon server exchange client/server attributes reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
