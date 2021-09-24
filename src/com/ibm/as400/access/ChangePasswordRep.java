///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ChangePasswordRep.java
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

class ChangePasswordRep extends ClientAccessDataStream
{
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new ChangePasswordRep();
    }

    int getRC()
    {
        return get32bit(20);
    }

    AS400Message[] getErrorMessages(ConverterImplRemote converter) throws IOException
    {
        return AS400ImplRemote.parseMessages(data_, 24, converter);
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving change password reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (readFromStream(in, header, 0, 20) < 20)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the change password reply header.");
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
      return 0xF005;
    }

}
