///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RCExchangeAttributesRequestDataStream.java
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

class RCExchangeAttributesRequestDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    RCExchangeAttributesRequestDataStream()
    {
        super(new byte[34]);
        setLength(34);
        // setHeaderID(0x0000);
        setServerID(0xE008);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(14);
        setReqRepID(0x1001);

        set32bit(ExecutionEnvironment.getCcsid(), 20); // Set CCSID.

        char[] nlv = ExecutionEnvironment.getNlv().toCharArray();  // Set NLV.
        data_[24] = (byte)(nlv[0] | 0x00F0);  // Make 0x00C. -> 0xF.
        data_[25] = (byte)(nlv[1] | 0x00F0);
        data_[26] = (byte)(nlv[2] | 0x00F0);
        data_[27] = (byte)(nlv[3] | 0x00F0);

        set32bit(0x00000001, 28);  // Set client version to one.
        // set16bit(0x0000, 32);  // Set client datastream level (leave at zero, so all server versions work).
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending remote command exchange client/server attributes request...");
        super.write(out);
    }
}
