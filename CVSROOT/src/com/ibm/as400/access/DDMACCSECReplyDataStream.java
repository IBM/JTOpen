///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DDMACCSECReplyDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class DDMACCSECReplyDataStream extends DDMDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Check the reply.
    boolean checkReply(int byteType)
    {
        if (getCodePoint() != DDMTerm.ACCSECRD)
        {
            Trace.log(Trace.ERROR, "DDM ACCSEC failed with code point:", data_, 8, 2);
            return false;
        }
        if (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD)
        {
            if (data_.length < 19)
            {
                Trace.log(Trace.ERROR, "DDM ACCSEC failed: system may be set to *KERBEROS while client is not.", data_);
                return false;
            }

            int rc = get16bit(18);
            if (rc == DDMTerm.SECCHKCD)
            {
                // The server didn't like the SECCHKCD.
                Trace.log(Trace.ERROR, "DDM ACCSEC SECCHKCD failed:", data_, 18, 2);
                return false;
            }
            if (rc != DDMTerm.SECTKN)
            {
                // The server didn't like the SECTKN.
                Trace.log(Trace.ERROR, "DDM ACCSEC SECTKN failed:", data_, 18, 2);
                return false;
            }
        }
        return true;
    }

    byte[] getServerSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 20, seed, 0, 8);
        return seed;
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving DDM ACCSEC reply...");
        // Receive the header.
        byte[] header = new byte[6];
        if (readFromStream(in, header, 0, 6) < 6)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the DDM EXCSAT Reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
        System.arraycopy(header, 0, data_, 0, 6);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
