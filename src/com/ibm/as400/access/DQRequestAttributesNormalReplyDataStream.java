///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DQRequestAttributesNormalReplyDataStream.java
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

// Query data queue attributes reply (normal) data stream.
// If there is an error, the reply to a attribute request is a DQCommonReplyDataStream.
class DQRequestAttributesNormalReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new DQRequestAttributesNormalReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8001;  // Returns the reply ID.
    }

    // Get max entry length.
    int getMaxEntryLength()
    {
        return get32bit(22);
    }

    // Get if sender information is saved.
    boolean getSaveSenderInformation()
    {
        return (data_[26] == (byte)0xF1);
    }

    // Get queue type.
    int getType()
    {
        // 0=FIFO, 1=LIFO, 2=KEYED.
        return data_[27] & 0x0F;
    }

    // Get key length.
    int getKeyLength()
    {
        return get16bit(28);
    }

    // Get force to auxiliary storage.
    boolean getForceToAuxiliaryStorage()
    {
        return (data_[30]  == (byte)0xF1);
    }

    // Get text description.
    byte[] getDescription()
    {
        byte[] description = new byte[50];
        System.arraycopy(data_, 31, description, 0, 50);
        return description;
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Receiving query data queue attributes (normal) reply...");

        // Read in rest of data.
        return super.readAfterHeader(in);
    }
}
