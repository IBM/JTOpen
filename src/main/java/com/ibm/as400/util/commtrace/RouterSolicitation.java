///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RouterSolicitation.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * A Router Solicitation Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the 
 * Router Solicitation Header.
 */
public class RouterSolicitation extends Message {

	/**
	* Creates and parses the data of this header.  
	* @param data	The raw data of this header. 
	*/
	RouterSolicitation(BitBuf data) {
		super(data);
		super.type= RTRSOL;
	}
	
	/**
	 * Returns the next header in the packet.
	 * @return Will always return a NDOption header.
	 */
	public Header getNextHeader() {
		return NDOption.createNDOption(rawpayload);
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 32.
	 */
	public int getHeaderLen() {
		return 32;
	}
}