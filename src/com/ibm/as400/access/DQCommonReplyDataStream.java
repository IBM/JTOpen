///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DQCommonReplyDataStream.java
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

// Common reply data stream is used for the following reply data streams:
// - Create data queue reply data stream,
// - Delete data queue reply data stream,
// - Add record to data queue reply data stream,
// - Clear data queue reply data stream,
// - Data queue exchange client/server attributes reply (error) data stream,
// - Query data queue attributes reply (error) data stream,
// - Receive record from data queue reply (error) data stream.
class DQCommonReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new DQCommonReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8002;  // Returns the reply ID.
    }

    int getRC()
    {
        return get16bit(20);
    }

    byte[] getMessage()
    {
        // Get AS/400 message if there is one.
        if (getLength() > 22)
        {
            // Determine length of msg (LL).
            int length = get32bit(22) - 6;

            // Get message.
            byte[] message = new byte[length];
            System.arraycopy(data_, 28, message, 0, length);
            return message;
        }
        return null;
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Receiving data queue common reply...");

        // Read in remaining data.
        return super.readAfterHeader(in);
    }
}
