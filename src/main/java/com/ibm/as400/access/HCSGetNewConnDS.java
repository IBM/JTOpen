///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HCSGetNewConnDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2024 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

public class HCSGetNewConnDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 2024 International Business Machines Corporation and others.";

    public HCSGetNewConnDS(byte[] _connReqID)
    {
      super(new byte[84]);
      setLength(84);
      data_[4] = 0x00;
      setServerID(0xE00B);
      setTemplateLen(64);
      setReqRepID(0x7106);
      
      System.arraycopy(_connReqID, 0, data_, 20, 64);
    }

    void write(OutputStream out) throws IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending get new connection request..."); 
      super.write(out);
    }
}
