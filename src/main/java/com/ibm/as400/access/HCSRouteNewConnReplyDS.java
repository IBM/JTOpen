///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HCSRouteNewConnReplyDS.java
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

class HCSRouteNewConnReplyDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 2024 International Business Machines Corporation and others.";
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new HCSRouteNewConnReplyDS();
    }

    int getRC()
    {
      return get32bit(20);
    }
    

    byte[] getJobNameBytes()
    {
        int offset = findCP(0x111F);
        if (offset == -1) return new byte[0];

        byte[] jobNameBytes = new byte[get32bit(offset) - 10];
        System.arraycopy(data_, offset + 10, jobNameBytes, 0, jobNameBytes.length);
        return jobNameBytes;
    }

    int findCP(int cp)
    {
        int offset = 24;
        while (offset < data_.length - 1)
        {
            if (get16bit(offset + 4) == cp) return offset;
            offset += get32bit(offset);
        }
        return -1;
    }
    
    @Override
    public int hashCode() {
      return 0xF107;
    }
    
    void read(InputStream in) throws IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving route new connection reply...");

      // Receive the header.
      byte[] header = new byte[20];
      if (readFromStream(in, header, 0, 20) < 20)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the reoute new connection reply header.");
        throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
      }

      // Allocate bytes for datastream.
      data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
      System.arraycopy(header, 0, data_, 0, 20);

      // Read in the rest of the data.
      readAfterHeader(in);
    }
}