///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Redirect.java
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
 * A Redirect Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the Redirect Header
 */
public class Redirect extends Message {
	private Field target= new IP6Address(rawheader.slice(32, 128));
	private Field destination= new IP6Address(rawheader.slice(160, 128));

	/**
	 * Returns the next header in the packet
	 * @return Will always return a NDOption header
	 */
	public Header getNextHeader() {
		return NDOption.createNDOption(rawpayload);
	}

	/**
	 * Creates and parses the data of this header  
	 * @param data	The raw data of this header 
	 */
	Redirect(BitBuf data) {
		super(data);
		super.type= REDR;
	}

	/**
	 * Returns the length of this header.
	 * @return Will always return 288.
	 */
	public int getHeaderLen() {
		return 288;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args= { target, destination };
		return Formatter.jsprintf(TARGET + ":  {0}\n" + "\t" + DST + ": {1}\n", args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the target address of this Redirect Message. 
	 * @return String containing a IPv6 address. 
	 */
	public String getTarget() {
		return target.toString();
	}

	/**
	 * Returns the destination address of this Redirect Message. 
	 * @return String containing a IPv6 address. 
	 */
	public String getDestination() {
		return destination.toString();
	}
}