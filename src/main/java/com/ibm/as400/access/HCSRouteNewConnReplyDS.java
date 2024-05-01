package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class HCSRouteNewConnReplyDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
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
    
    @Override
    public int hashCode() {
      return 0xF107;
    }
    
    void read(InputStream in) throws IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving reoute new connection reply...");

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