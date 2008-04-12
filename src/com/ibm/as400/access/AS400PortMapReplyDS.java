///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400PortMapReplyDS.java
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

// A class representing a reply to a "port map" request (class AS400PortMapDS) data stream.  This class is NOT derived from DataStream.
class AS400PortMapReplyDS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    byte[] data_;
    private int connectionID_;

    // Create space for port mapper reply.
    AS400PortMapReplyDS()
    {
        data_ = new byte[5];
    }

    // Get the port returned from the port mapper.
    int getPort() throws ServerStartupException
    {
        // First check to make sure we got a positive response 0x2B==ASCII '+'
        if (data_[0] == 0x2B) return BinaryConverter.byteArrayToInt(data_, 1);
        throw new ServerStartupException(ServerStartupException.CONNECTION_PORT_CANNOT_CONNECT_TO);
    }

    // Read the reply from the port mapper.
    void read(InputStream in) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Receiving port mapper reply (connID="+connectionID_+") ...");
        if (DataStream.readFromStream(in, data_, 0, 5, connectionID_) < 5)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the port mapper reply.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }
    }

    // Set the connection ID associated with this data stream.
    // @param  connectionID  the connection ID.
    void setConnectionID(int connectionID)
    {
      connectionID_ = connectionID;
    }
}
