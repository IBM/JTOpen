///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSGetTableReply.java
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

class NLSGetTableReply extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    int primaryRC_=0;            // return code returned by server
    int secondaryRC_=0;          // return code returned by server
    char table_[];

    NLSGetTableReply()
    {
	super();
    }

    public Object getNewDataStream()
    {
	return new NLSGetTableReply();
    }

    public int hashCode()
    {
	return 0x1201;  // returns the reply ID
    }

    public int readAfterHeader(InputStream in) throws IOException
    {
	// read in rest of data
	int bytes=super.readAfterHeader(in);
	// get return codes
	primaryRC_ = get16bit(HEADER_LENGTH+2);
	secondaryRC_ = get16bit(HEADER_LENGTH+4);
	if (primaryRC_ != 0) return bytes;

	// Note: chain not currently used.
	int ll=get32bit(HEADER_LENGTH+8) - 6;
	table_ = new char[ll/2];
	int ii = 0;
	for (int i=0; i<ll; i+=2)
	{
	    table_[ii++] = (char)get16bit(HEADER_LENGTH+8+6+i);
	}
	return bytes;
    }
}
