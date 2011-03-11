///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HopByHop.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * A HopByHop Header.<br>
 * Extends ExtHeader's methods to parse, print, and allow easy access to the HopByHop Header. 
 */
public class HopByHop extends ExtHeader {

	/**
	 * Creates and parses the data of this header.  
	 * @param data  The raw data of this header. 
	 */
	HopByHop(BitBuf data) {
		super(data);
		type= Header.EXTHOPBYHOP;
	}
	
	/**
	 * Returns the length of this header.
	 * @return The length of this header.
	 */	
	public int getHeaderLen() {
		return 64 + ((rawheader.getOctet(8) & 0xFF) * 64);
	}
}