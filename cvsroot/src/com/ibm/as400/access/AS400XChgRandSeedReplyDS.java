///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400XChgRandSeedReplyDS.java
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

// A class representing a reply to an "exchange random seed" request (class AS400XChgRandSeedDS) data stream.
class AS400XChgRandSeedReplyDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new AS400XChgRandSeedReplyDS();
    }

    int getRC()
    {
        return get32bit(20);
    }

    byte[] getServerSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 24, seed, 0, 8);
        return seed;
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving exchange random seeds reply..."); //@P0C

        // Receive the header.
        byte[] header = new byte[20];
        if (readFromStream(in, header, 0, 20) < 20)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the exchange random seeds reply header."); //@P0C
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
      return 0xF001;
    }

}
