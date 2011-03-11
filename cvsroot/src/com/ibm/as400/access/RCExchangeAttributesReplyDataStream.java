///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCExchangeAttributesReplyDataStream.java
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

class RCExchangeAttributesReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new RCExchangeAttributesReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8001;
    }

    // Get return code.
    int getRC()
    {
        return get16bit(20);
    }

    // Get server CCSID.
    int getCCSID()
    {
        return get32bit(22);
    }

    // Server NLV and server version are not currently used.

    // Server datastream level.
    int getDSLevel()
    {
        return get16bit(34);
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving remote command exchange client/server attributes reply...");
        return super.readAfterHeader(in);
    }
}
