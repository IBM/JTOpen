///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: TargetLLA.java
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
 * A TargetLLA Header.<br>
 * Extends NDOption's methods to parse, print, and allow easy access to the TargetLLA Header.
 */
public class TargetLLA extends NDOption {
	private Field address= new Hex(rawheader.slice(16));

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	TargetLLA(BitBuf data) {
		super(data);
		super.type= TARGETLLA;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		Object[] args= { ndtype, length, address };
		return Formatter.jsprintf(
			"\t    "
				+ OPTDATA
				+ ":   "
				+ TYPE
				+ ": {0,3,R}     "
				+ LEN
				+ ": {1,3,R}     "
				+ ADDR
				+ ": {2}\n",
			args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the address of this TargetLLA header. 
	 * @return A String containing a decimal representation of the address.
	 */
	public String getAddress() {
		return (new Dec(address.getData())).toString();
	}

}