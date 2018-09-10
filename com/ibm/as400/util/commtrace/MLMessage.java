///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MLMessage.java
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
 * A Multicast Listener Message Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the MLMessage Header.
 */
public class MLMessage extends Message {
	private Field maxresponsedelay= new Dec(rawheader.slice(0, 16));
	private Field address= new IP6Address(rawheader.slice(32, 128));

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	MLMessage(BitBuf data) {
		super(data);
	}

	/**
	 * Returns the length of this header.
	 * @return Will always return 160.
	 */
	public int getHeaderLen() {
		return 160;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args= { maxresponsedelay, address };
		return Formatter.jsprintf(MAX + ": {0,5,R}     " + ADDR + ": {1}\n", args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the max response delay of this MLMessage. 
	 * @return String containing a decimal representation of the max response delay. 
	 */
	public String getMaxResponseDelay() {
		return maxresponsedelay.toString();
	}

	/**
	 * Returns the IPv6 Address of this MLMessage. 
	 * @return String containing a delimited IPv6 address. 
	 */
	public String getAddress() {
		return address.toString();
	}
}