///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: EchoReplyRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Properties;

/**
 * A Echo Reply Request Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the
 * Echo Reply Request Header.
 */
public class EchoReplyRequest extends Message {
	private Field identifier= new Dec(rawheader.slice(0, 16));
	private Field sequencenumber= new Dec(rawheader.slice(16, 16));

	/**
	 * Creates and parses the data of this header. 
	 * @param data	The raw data of this header. 
	 */
	EchoReplyRequest(BitBuf data) {
		super(data);
		super.type= ECHQRPL;
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 32.
	 */
	public int getHeaderLen() {
		return 32;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		Object[] args= { identifier, sequencenumber };
		return Formatter.jsprintf(ID + ": {0,5,R} " + SEQNUM + ": {1}\n", args)
			+ printHexHeader()
			+ (new Data(rawpayload)).toString();
	}

	/**
	 * Returns the identifier of this EchoReplyRequest header. 
	 * @return String containing a decimal representation of the identifier. 
	 */
	public String getIdentifier() {
		return identifier.toString();
	}

	/**
	 * Returns the sequence number of this EchoReplyRequest header.
	 * @return String containing a decimal representation of the sequence number.  
	 */
	public String getSequenceNumber() {
		return sequencenumber.toString();
	}
}