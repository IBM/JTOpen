///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: NeighborSolicitation.java
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
 * A Neighbor Solicitation Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the
 * Neighbor Solicitation Header.
 */
public class NeighborSolicitation extends Message {
	private Field targetaddress= new IP6Address(rawheader.slice(32, 128));

	/**
	 * Returns the next header in the packet.
	 * @return Will always return a NDOption header.
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
	 * @param data The raw data of this header.
	 */
	NeighborSolicitation(BitBuf data) {
		super(data);
		super.type= NGHSOL;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	FormatProperties object for filtering this header.
     * @return Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		Object[] args= { targetaddress };
		return Formatter.jsprintf(TARGET + ":  {0}\n", args)
			+ printHexHeader()
			+ printnext(filter);
	}

	/**
	 * Returns the target address of this NeighborSolicitation Message. 
	 * @return String containing a delimited IPv6 address. 
	 */
	public String getTargetAddress() {
		return targetaddress.toString();
	}
}