///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400EndJobDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// A class representing an "end server job" request data stream.
class AS400EndJobDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    AS400EndJobDS(int serverId)
    {
        super();

        if (serverId == 0xE004)  // Database Server.
        {
            data_ = new byte[40];
        }
        else
        {
            data_ = new byte[20];
        }

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(serverId);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        // setTemplateLen(0x0000);

        switch (serverId)
        {
            case 0xE000:  // Central Server.
                setReqRepID(0x1400);
                break;
            case 0xE004:  // Database Server.
                setReqRepID(0x1FFF);
                setTemplateLen(20);
                break;
            case 0xE008:  // Remote Command Server.
                setReqRepID(0x1004);
                break;
            case 0xE009:  // Signon Server.
                setReqRepID(0x7006);
                break;
        }
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending end job request...");
        super.write(out);
    }
}
