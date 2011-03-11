///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RedirectedHeader.java
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
 * A Redirected Header.<br>
 * Extends NDOption's methods to parse, print, and allow easy access to the Redirected Header.
 */
class RedirectedHeader extends NDOption {
	private Field prefixlen= new Dec(rawheader.slice(16, 8));
	private IP6Header packet= new IP6Header(rawheader.slice(64));

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	RedirectedHeader(BitBuf data) {
		super(data);
		super.type= REDIRECTED;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		Object[] args= { ndtype, length, prefixlen, packet };
		return Formatter.jsprintf(
			"\t    "
				+ OPTDATA
				+ ":   "
				+ TYPE
				+ ": {0,3,R}     "
				+ LEN
				+ ": {1,3,R}     "
				+ PFXLEN
				+ ":  {2,3,R}\n"
				+ printHexHeader()
				+ PKT
				+ ":\n",
			args)
			+ packet.toString(filter);
	}

	/**
	 * Returns the prefix length of this RedirectedHeader Header. 
	 * @return String containing a decimal representation of the prefix length.
	 */
	public String getPrefixLength() {
		return prefixlen.toString();
	}

	/**
	 * Returns the redirected IP6Header. 
	 * @return IP6Header
	 */
	public IP6Header getPacket() {
		return packet;
	}
}