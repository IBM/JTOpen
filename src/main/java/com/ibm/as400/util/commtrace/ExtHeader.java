///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ExtHeader.java
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
 * An Extended Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the Extended Header.
 */
public abstract class ExtHeader extends Header {

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	ExtHeader(BitBuf data) {
		super(data);
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 64.
	 */
	public int getHeaderLen() {
		return 64;
	}

	/**
	 * Returns the next header in this packet. 
	 * @return Header
	 */
	public Header getNextHeader() {
		return Header.createHeader(rawheader.getOctet(0), rawpayload);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}

		Object[] args = { getName().replace('$', '-'), rawheader.toHexString()};
		return Formatter.jsprintf("{0}:  {1,64,L}\n", args) + printnext(filter);
	}
}