///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MTU.java
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
 * A MTU Header.<br>
 * Extends NDOption's methods to parse, print, and allow easy access to the MTU Header.
 */
public class MTU extends NDOption {
	private Field mtu= new Dec(rawheader.slice(32, 32));

	/**
	 * Creates and parses the data of this header.  
	 * @param data The raw data of this header. 
	 */
	MTU(BitBuf data) {
		super(data);
		super.type= MTU;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args= { ndtype, length, mtu };
		return Formatter.jsprintf(
			"\t    "
				+ OPTDATA
				+ ":   "
				+ TYPE
				+ ": {0,3,R}     "
				+ LEN
				+ ": {1,3,R}     "
				+ MTUSTR
				+ ": {2,10,R}\n",
			args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the MTU of MTU header. 
	 * @return String containing a decimal representation of the MTU. 
	 */
	public String getMTU() {
		return mtu.toString();
	}

}