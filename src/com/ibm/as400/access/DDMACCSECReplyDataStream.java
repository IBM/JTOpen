///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DDMACCSECReplyDataStream.java
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

class DDMACCSECReplyDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static String getCopyright()
    {
	return Copyright.copyright;
    }

    // Check the reply
    void checkReply(boolean useEncryptedPassword)
    {
	if (getCodePoint() != DDMTerm.ACCSECRD)
	{
	    Trace.log(Trace.ERROR, "DDM: V4R4 or newer system: ACCSEC failed with code point ", getCodePoint());
	    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
	}
	if (useEncryptedPassword)
	{
	    if (get16bit(18) == DDMTerm.SECCHKCD)
	    {
	        // The server didn't like the SECCHKCD
		Trace.log(Trace.ERROR, "DDM: V4R4 or newer system: ACCSEC, SECCHKCD failed: ", get16bit(18));
		throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
	    }
	    if (get16bit(18) != DDMTerm.SECTKN)
	    {
	        // The server didn't like the SECTKN
		Trace.log(Trace.ERROR, "DDM: V4R4 or newer system: ACCSEC, SECTKN failed: ", get16bit(18));
		throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE);
	    }
	}
    }

    byte[] getServerSeed()
    {
	byte[] seed = new byte[8];
	System.arraycopy(data_, 20, seed, 0, 8);
	return seed;
    }

    void read(InputStream in) throws IOException
    {
	Trace.log(Trace.DIAGNOSTIC, "Receiving DDM: V4R4 or newer system: ACCSEC reply...");
	// Receive the header.
	byte[] header = new byte[6];
	if (readFromStream(in, header, 0, 6) < 6)
	{
	    Trace.log(Trace.ERROR, "Failed to read all of the DDM EXCSAT Reply header.");
	    throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
	}

	// Allocate bytes for datastream
	data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
	System.arraycopy(header, 0, data_, 0, 6);

	// read in the rest of the data
	readAfterHeader(in);
    }
}
