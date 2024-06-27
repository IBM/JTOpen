///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HCSPrepareNewConnDS.java
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

public class HCSPrepareNewConnDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 2024 International Business Machines Corporation and others.";

    public HCSPrepareNewConnDS(int connSrvType)
    {
      super(new byte[22]);
      setLength(22);
      data_[4] = 0x00;
      setServerID(0xE00B);
      setTemplateLen(2);
      setReqRepID(0x7105);
      
      set16bit(connSrvType, 20);
    }

    void write(OutputStream out) throws IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending prepare new connection request..."); 
      super.write(out);
    }
}
