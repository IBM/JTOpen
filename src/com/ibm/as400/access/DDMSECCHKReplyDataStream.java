///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

    private static String getCopyright()
    {
	return Copyright.copyright;
    }

    // Check the reply
    void checkReply(boolean useEncryptedPassword)
    {
	// Check the reply
	if (getCodePoint() != DDMTerm.SECCHKRD)
	{
	    Trace.log(Trace.ERROR, "DDM: SECCHK failed with code point: ", getCodePoint());
	    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
	}
	if (useEncryptedPassword)
	{
	    if (get16bit(18) == DDMTerm.SECCHKCD)
	    {
		if (data_[data_.length-1] != 0x00) // the last byte in the stream
		{
		    Trace.log(Trace.ERROR, "DDM: V4R4 or newer system: SECCHK failed with rc: ", data_[data_.length-1]);
		    throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE);
		}
	        // A code of 0x00 means the password was accepted.
		Trace.log(Trace.DIAGNOSTIC, "DDM: V4R4 or newer system: SECCHK succeeded.");
	    }
	    else
	    {
		Trace.log(Trace.ERROR, "DDM: V4R4 or newer system: SECCHK failed with code point: ", get16bit(18));
		throw new InternalErrorException(InternalErrorException.UNKNOWN);
	    }
	}
    }

    void read(InputStream in) throws IOException
    {
	Trace.log(Trace.DIAGNOSTIC, "Receiving DDM: V4R4 or newer system: SECCHK reply...");
	// Receive the header.
	byte[] header = new byte[6];
	if (readFromStream(in, header, 0, 6) < 6)
	{
	    Trace.log(Trace.ERROR, "Failed to read all of the DDM SECCHK Reply header.");
	    throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
	}

	// Allocate bytes for datastream
	data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
	System.arraycopy(header, 0, data_, 0, 6);

	// read in the rest of the data
	readAfterHeader(in);
    }
}
