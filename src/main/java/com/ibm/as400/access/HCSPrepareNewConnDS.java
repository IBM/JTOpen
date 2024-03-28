package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

public class HCSPrepareNewConnDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

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
