///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HCSGetNewConnReplyDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2024 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class HCSGetNewConnReplyDS extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 2024 International Business Machines Corporation and others.";
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new HCSGetNewConnReplyDS();
    }

    int getRC()
    {
      return get32bit(20);
    }
    
    @Override
    public int hashCode() {
      return 0xF106;
    }
    
    void read(InputStream in) throws IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving get new connection reply...");

      // Receive the header.
      byte[] header = new byte[20];
      if (readFromStream(in, header, 0, 20) < 20)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the get new connection reply header.");
        throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
      }

      // Allocate bytes for datastream.
      data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
      System.arraycopy(header, 0, data_, 0, 20);

      // Read in the rest of the data.
      readAfterHeader(in);
    }
}