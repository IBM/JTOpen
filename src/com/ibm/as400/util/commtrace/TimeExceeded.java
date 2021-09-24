///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: TimeExceeded.java
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
 * A Time Exceeded Header.<br>
 * Extends Message's methods  to parse, print, and allow easy access to the Time Exceeded Header.
 */
public class TimeExceeded extends Message {
	/**
	 * Creates and parses the data of this header.
	 * @param data The raw data of this header .
	 */
	TimeExceeded(BitBuf data) {
		super(data);
		super.type= TIMEEXC;
	}

	/**
	 * Returns the length of this header.
	 * @return Will always return 32.
	 */
	public int getHeaderLen() {
		return 32;
	}

	/**
	 * Returns the next header in the packet.
	 * @return Will always return a IP6Header.
	 */
	public Header getNextHeader() {
		return new IP6Header(rawpayload);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter FormatProperties object for filtering this header.
     * @return Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		return "\t\t\t" + OFFPKT + ":\n" + printnext(filter);
	}

}