///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQExchangeAttributesNormalReplyDataStream.java
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

// Data queue exchange client/server attributes reply (normal).
// If there is an error, the reply to an exchange attributes request is a DQCommonReplyDataStream.
class DQExchangeAttributesNormalReplyDataStream extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new DQExchangeAttributesNormalReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8000;  // Returns the reply ID.
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving data queue exchange client/server attributes (normal) reply...");
        return super.readAfterHeader(in);
    }
}
