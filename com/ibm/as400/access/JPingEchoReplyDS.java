///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JPingEchoReplyDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;

/**
 * The JPingEchoReplyDS is the reply datastream for a ping
 * which involves the server echoing back the datastream sent.
 *
 **/
class JPingEchoReplyDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Constructs a JPingEchoReplyDS object.
     *
     *  @param length The length of the datastream.
     *
     **/
    JPingEchoReplyDS(int length)
    {
       super();
       data_ = new byte[length + 20];
    }

    int read(InputStream in) throws IOException
    {
       // Receive the header.
       byte[] header = new byte[20];
       if (readFromStream(in, header, 0, 20) < 20)
       {
          Trace.log(Trace.ERROR, "Failed to read all of the Retrieve Signon Information Reply header.");
          throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
       }
       
       // Allocate bytes for datastream
       data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
       System.arraycopy(header, 0, data_, 0, 20);
       
       // read in the rest of the data
       return readAfterHeader(in);
    }
}
