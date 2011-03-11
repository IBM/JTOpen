///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMSECCHKReplyDataStream.java
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

class DDMSECCHKReplyDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Check the reply.
    boolean checkReply()
    {
    if (getCodePoint() != DDMTerm.SECCHKRD)
    {
            Trace.log(Trace.ERROR, "DDM SECCHK failed with code point:", data_, 8, 2);
            return false;
        }
        if (get16bit(18) != DDMTerm.SECCHKCD)
        {
            Trace.log(Trace.ERROR, "DDM SECCHK failed with rc:", data_, 18, 2);
            return false;
        }
//@G0D        if (data_[data_.length - 1] != 0x00)  // The last byte in the stream.
//@G0D        {
//@G0D            Trace.log(Trace.ERROR, "DDM SECCHK failed with last byte:", data_, data_.length - 1, 1);
//@G0D            return false;
//@G0D        }
        if (data_.length < 21) //@G0A
        {
          Trace.log(Trace.ERROR, "DDM SECCHK failed with length < 21:", data_);
          return false;
        }
        if (data_[20] != 0) //@G0A
        {
            Trace.log(Trace.ERROR, "DDM SECCHK failed with bad SECCHKCD:", data_, 20, 1);
            return false;
        }
        
        // A code of 0x00 means the password was accepted.
        return true;
    }

  void read(InputStream in) throws IOException
  {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving DDM SECCHK reply...");
    // Receive the header.
    byte[] header = new byte[6];
    if (readFromStream(in, header, 0, 6) < 6)
    {
      Trace.log(Trace.ERROR, "Failed to read all of the DDM SECCHK Reply header.");
      throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
    }

        // Allocate bytes for datastream.
    data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
    System.arraycopy(header, 0, data_, 0, 6);

        // Read in the rest of the data.
    readAfterHeader(in);
  }
}
