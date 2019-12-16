///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RouterAdvertisement.java
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
 * A Router Advertisement Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the 
 * Router Advertisement Header.
 * 
 */
public class RouterAdvertisement extends Message {
	private Field curhoplimit= new Dec(rawheader.slice(0, 8));
	private Field m= new Flag(rawheader.slice(8, 1));
	private Field o= new Flag(rawheader.slice(9, 1));
	private Field routerlifetime= new Dec(rawheader.slice(16, 16));
	private Field reachabletime= new Dec(rawheader.slice(32, 32));
	private Field retranstimer= new Dec(rawheader.slice(64, 32));

	/**
	 * Returns the next header in the packet.
	 * @return Will always return a NDOption header.
	 */
	public Header getNextHeader() {
		return NDOption.createNDOption(rawpayload);
	}

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header.
	 */
	RouterAdvertisement(BitBuf data) {
		super(data);
		super.type= RTRADV;
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 96.
	 */
	public int getHeaderLen() {
		return 96;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args=
			{ curhoplimit, m, o, routerlifetime, reachabletime, retranstimer };
		return Formatter.jsprintf(
			CURHOPLIMIT
				+ ": {0,3,R}     "
				+ M
				+ ": {1}    "
				+ O
				+ ": {2}\n"
				+ "\t\t"
				+ RTRLIFETIME
				+ ": {3,5,R}     "
				+ RCHTIME
				+ ": {4,10,R}     "
				+ RETRANSTIME
				+ ": {5,10,R}\n",
			args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the current hop limit of this RouterAdvertisement Message.
	 * @return String containing a decimal representation of the current hop limit. 
	 */
	public String getCurrentHopLimit() {
		return curhoplimit.toString();
	}

	/**
	 * Returns the "Managed address configuration" flag of this RouterAdvertisement Message. 
	 * @return String containing a Byte.toString(byte) representation of the flag. 
	 */
	public String getMFlag() {
		return m.toString();
	}

	/**
	 * Returns the "Other stateful configuration" flag of this RouterAdvertisement Message. 
	 * @return String containing a Byte.toString(byte) representation of the flag. 
	 */
	public String getOFlag() {
		return o.toString();
	}

	/**
	 * Returns the router life time of this RouterAdvertisement Message. 
	 * @return String containing a decimal representation of the router life time.
	 */
	public String getRouterLifeTime() {
		return routerlifetime.toString();
	}

	/**
	 * Returns the reachable time of this RouterAdvertisement Message. 
	 * @return String containing a decimal representation of the reachable time. 
	 */
	public String getReachableTime() {
		return reachabletime.toString();
	}

	/**
	 * Returns the retrans Time of this RouterAdvertisement Message. 
	 * @return String containing a decimal representation of the retrans timer. 
	 */
	public String getRetransTimer() {
		return retranstimer.toString();
	}
}
