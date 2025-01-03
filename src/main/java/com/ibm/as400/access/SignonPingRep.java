///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonInfoRep.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class SignonPingRep extends ClientAccessDataStream
{
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new SignonPingRep();
    }

    int getPingResponse() {
    	return get32bit(0);
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving signon ping response...");

        // Only data sent is returned...no header.
        byte[] header = new byte[4];
        if (readFromStream(in, header, 0, 4) < 4)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the retrieve signon ping reply.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = header;
    }

    /**
     Generates a hash code for this data stream.
     @return the hash code
     **/
    public int hashCode()
    {
      return 0xFFFE;
    }
}
