///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: PrefixInfo.java
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
 * A PrefixInfo Header.<br>
 * Extends NDOption's methods to parse, print, and allow easy access to the PrefixInfo Header
 */
public class PrefixInfo extends NDOption {
	private Field prefixlen= new Dec(rawheader.slice(16, 8));
	private Field l= new Flag(rawheader.slice(24, 1));
	private Field a= new Flag(rawheader.slice(25, 1));
	private Field validlifetime= new Dec(rawheader.slice(64, 32));
	private Field preferredlifetime= new Dec(rawheader.slice(96, 32));
	private Field prefix= new IP6Address(rawheader.slice(128, 128));

	/**
	 * Creates and parses the data of this header.
	 * @param data	The raw data of this header.
	 */
	PrefixInfo(BitBuf data) {
		super(data);
		super.type= PREFIXINFO;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args=
			{ ndtype, length, prefixlen, l, a, validlifetime, preferredlifetime, prefix };
		return Formatter.jsprintf(
			"\t    "
				+ OPTDATA
				+ ":   "
				+ TYPE
				+ ": {0,3,R}       "
				+ LEN
				+ ": {1,3,R}\n"
				+ "\t\t\t\t      "
				+ PFXLEN
				+ ": {2,3,R}  L: {3}  A: {4}     "
				+ VLDLIFETIME
				+ ": {5,10,R}     "
				+ PRETIME
				+ ": {6,10,R}"
				+ "\t\t\t\t      "
				+ PFX
				+ ": {7}\n",
			args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the prefix length of this PrefixInfo header.
	 * @return String containing a decimal representation of the prefix length.
	 */
	public String getPrefixLength() {
		return prefixlen.toString();
	}

	/**
	 * Returns the on-link flag of this PrefixInfo header.
	 * @return String containing a Byte.toString(byte) representation of this flag bit.
	 */
	public String getLFlag() {
		return l.toString();
	}

	/**
	 * Returns the autonomous address-configuration flag of this PrefixInfo header.
	 * @return String containing a Byte.toString(byte) representation of this flag bit.
	 */
	public String getAFlag() {
		return a.toString();
	}

	/**
	 * Returns the valid life time of this PrefixInfo header.
	 * @return String containing a decimal representation of the valid life time. 
	 */
	public String getValidLifeTime() {
		return validlifetime.toString();
	}

	/**
	 * Returns the preferred life time of this PrefixInfo header.
	 * @return String containing a decimal representation of the preferred life time.
	 */
	public String getPreferredLifeTime() {
		return preferredlifetime.toString();
	}

	/**
	 * Returns the prefix of this PrefixInfo header.
	 * @return String containing a decimal representation of the prefix.
	 */
	public String getPrefix() {
		return prefix.toString();
	}
}