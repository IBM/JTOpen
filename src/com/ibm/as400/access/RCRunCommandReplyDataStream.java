///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCRunCommandReplyDataStream.java
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

// Run remote command reply data stream class.
class RCRunCommandReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new RCRunCommandReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8002;
    }

    int getRC()
    {
        return get16bit(20);
    }

    AS400Message[] getMessageList(ConverterImplRemote converter)
    {
        return RemoteCommandImplRemote.parseMessages(data_, converter);
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving run command reply...");
        return super.readAfterHeader(in);
    }
}
