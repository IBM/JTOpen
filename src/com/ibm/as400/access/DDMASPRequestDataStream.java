///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMASPRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class DDMASPRequestDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  DDMASPRequestDataStream(byte[] iasp)
  {
    super(new byte[32]);

    // Initialize the header:  Don't continue on error, not chained, GDS id = D0, type = RQSDSS, no same request correlation.
    setGDSId((byte)0xD0);
    // setIsChained(false);
    // setContinueOnError(false);
    // setHasSameRequestCorrelation(false);
    setType(1);
    set16bit(26, 6); // Total length remaining after header.
    set16bit(DDMTerm.SXXASPRQ, 8); // Set ASPRQ code point.
    set16bit(22, 10); // Set RDBNAM length.
    set16bit(DDMTerm.RDBNAM, 12); // Set RDBNAM code point.
    System.arraycopy(iasp, 0, data_, 14, iasp.length);
  }

  void write(OutputStream out) throws IOException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending DDM ASP request...");
    super.write(out);
  }
}
