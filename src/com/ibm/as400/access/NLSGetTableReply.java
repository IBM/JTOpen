///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2016 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;

class NLSGetTableReply extends ClientAccessDataStream
{
	  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";

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
	if (( (0xFFFFFFFFl & ll) %2 ) == 1) {
	  // Make sure length is even 
	  ll++; 
	}
	table_ = new char[ll/2];
	int ii = 0;
	int errorCount = 0; 
	for (int i=0; i<ll; i+=2)
	{
	  int offset = HEADER_LENGTH+8+6+i;
	  if (offset + 1 < data_.length) {  
	     table_[ii++] = (char)get16bit(offset);
	  } else {
	    // Just one byte left 
	    if (offset < data_.length) {
	       table_[ii++] = (char) (data_[offset] << 8); 
	    }
	  }
	}
	return bytes;
    }
}
