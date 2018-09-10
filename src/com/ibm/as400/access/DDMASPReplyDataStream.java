///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMASPReplyDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

class DDMASPReplyDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  // Check the reply.
  boolean checkReply()
  {
    if (getCodePoint() != DDMTerm.CMDCMPRM)
    {
      Trace.log(Trace.ERROR, "DDM ASP failed with code point:", data_, 8, 2);
      return false;
    }
    return true;
  }

  void read(InputStream in) throws IOException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving DDM ASP reply...");
    // Receive the header.
    byte[] header = new byte[6];
    if (readFromStream(in, header, 0, 6) < 6)
    {
      Trace.log(Trace.ERROR, "Failed to read all of the DDM ASP Reply header.");
      throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
    }

    // Allocate bytes for datastream.
    data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
    System.arraycopy(header, 0, data_, 0, 6);

    // Read in the rest of the data.
    readAfterHeader(in);
  }
}
