///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: NeighborAdvertisement.java
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
 * A Neighbor Advertisement Header.<br>
 * Extends Message's methods  to parse, print, and allow easy access to the 
 * Neighbor Advertisement Header
 */
public class NeighborAdvertisement extends Message {
	private Field router= new Flag(rawheader.slice(0, 1), "Yes", "No");
	private Field solicited= new Flag(rawheader.slice(1, 1), "Yes", "No");
	private Field override= new Flag(rawheader.slice(2, 1), "Yes", "No");
	private Field target= new IP6Address(rawheader.slice(32, 128));

	/**
	 * Returns the next header in the packet
	 * @return Will always return a NDOption header
	 */
	public Header getNextHeader() {
		return NDOption.createNDOption(rawpayload);
	}

	/**
	 * Returns the length of this header.
	 * @return Will always return 160
	 */
	public int getHeaderLen() {
		return 160;
	}

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	NeighborAdvertisement(BitBuf data) {
		super(data);
		super.type=NGHADV;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args= { router, solicited, override, target };
		return Formatter.jsprintf(
			FLAGS
				+ ":  "
				+ RTR
				+ ": {0,3,R}     "
				+ SOL
				+ ": {1,3,R}     "
				+ OVR
				+ ": {2,3,R}\n"
				+ "\t\t"
				+ TARGET
				+ ":  {3}\n",
			args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the router flag of this NeighborAdvertisement Message 
	 * @return String containing a Byte.toString(byte) of this flag bit
	 */
	public String getRouter() {
		return router.toString();
	}

	/**
	 * Returns the solicited flag of this NeighborAdvertisement Message 
	 * @return String containing a Byte.toString(byte) of this flag bit
	 */
	public String getSolicited() {
		return solicited.toString();
	}

	/**
	 * Returns the override flag of this NeighborAdvertisement Message 
	 * @return String containing a Byte.toString(byte) of this flag bit
	 */
	public String getOverride() {
		return override.toString();
	}

	/**
	 * Returns the target address of this NeighborAdvertisement Message 
	 * @return String containing a IPv6 address 
	 */
	public String getTarget() {
		return target.toString();
	}
}