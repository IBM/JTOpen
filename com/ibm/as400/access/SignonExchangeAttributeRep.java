///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonExchangeAttributeRep.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2001 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class SignonExchangeAttributeRep extends ClientAccessDataStream
{
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new SignonExchangeAttributeRep();
    }

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
        int offset = findCP(0x1103);
        if (offset == -1) return null;

        byte[] seed = new byte[8];
        System.arraycopy(data_, offset + 6, seed, 0, 8);
        return seed;
    }

    boolean getPasswordLevel()
    {
        int offset = findCP(0x1119);
        if (offset == -1) return false;

        return data_[offset + 6] >= 2;
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
        int offset = 42;
        while (offset < data_.length - 1)
        {
            if (get16bit(offset + 4) == cp) return offset;
            offset += get32bit(offset);
        }
        return -1;
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving signon server exchange client/server attributes reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (readFromStream(in, header, 0, 20) < 20)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the signon server exchange client/server attributes reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }

    /**
     Generates a hash code for this data stream.
     @return the hash code
     **/
    public int hashCode()
    {
      return 0xF003;
    }

}
