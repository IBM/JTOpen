///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DQReadNormalReplyDataStream.java
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

// Receive record from data queue reply (normal).
// If there is an error, the reply to a read request is a DQCommonReplyDataStream.
class DQReadNormalReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new DQReadNormalReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8003;  // Returns the reply ID.
    }

    // Get sender information.
    byte[] getSenderInformation()
    {
        byte[] senderInformation = new byte[36];
        System.arraycopy(data_, 22, senderInformation, 0, 36);
        return senderInformation;
    }

    byte[] getEntry()
    {
        return getOptionalParameter(0x5001);
    }

    byte[] getKey()
    {
        return getOptionalParameter(0x5002);
    }

    private byte[] getOptionalParameter(int codePoint)
    {
        int offset = 58;
        byte[] parameter = null;

        while (offset < data_.length - 6)
        {
            int length = get32bit(offset);
            if (get16bit(offset + 4) != codePoint)
            {
                offset += length;
            }
            else
            {
                parameter = new byte[length - 6];
                System.arraycopy(data_, offset + 6, parameter, 0, length - 6);
                break;
            }
        }
        return parameter;
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Receiving receive record from data queue (normal) reply...");

        // Read in rest of data.
        return super.readAfterHeader(in);
    }
}
