///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSExchangeAttrReply.java
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

class NLSExchangeAttrReply extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    int primaryRC_=0;            // return code returned by server
    int secondaryRC_=0;          // return code returned by server
    int ccsid_=0;                // host CCSCID

    NLSExchangeAttrReply()
    {
        super();
    }

    public int getCcsid()
    {
        return ccsid_;
    }

    public Object getNewDataStream()
    {
        return new NLSExchangeAttrReply();
    }

    public int hashCode()
    {
        return 0x1301;  // returns the reply ID
    }

    public int readAfterHeader(InputStream in) throws IOException
    {
        // read in rest of data
        int bytes=super.readAfterHeader(in);
        // get return codes
        primaryRC_ = get16bit(HEADER_LENGTH+2);
        secondaryRC_ = get16bit(HEADER_LENGTH+4);
        ccsid_ = get32bit(HEADER_LENGTH+8);
        // Note: chain, datastream level, version, function levels
        // not currently used.
        return bytes;
    }
}
