///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ICMP6Header.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Properties;
import com.ibm.as400.access.Trace;

/**
 * A Internet Control Message Protocol v6 Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the ICMPv6 Header.
 */
public class ICMP6Header extends Header {
	Field typeh= new Hex(rawheader.slice(0, 8));
	Field code= new Hex(rawheader.slice(8, 8));
	Field checksum= new Hex(rawheader.slice(16, 16));

	private static final String CLASS="ICMP6Header";
	private static final String ICMPV6= "ICMPv6";
	private static final String DATA= "Data";
	private static final String TYPE= "Type";
	private static final String CODE= "Code";
	private static final String CHKSUM= "Checksum";
	private static final String MSG= "Message";

	/**
	 * Creates and parses the data of this header.
	 * @param data	The raw data of this header.
	 */
	ICMP6Header(BitBuf data) {
		super(data);
		super.type= ICMP6;
	}

	/**
	 * Returns the length of this header.
	 * @return	Will always return 32.
	 */
	public int getHeaderLen() {
		return 32;
	}

	/**
	 * Returns the next header in the packet.
	 * @return Will always return a Message Header.
	 */
	public Header getNextHeader() {
		return Message.createMessage(rawheader.slice(0, 8).toByte(), rawpayload);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}

		// Check for IP filtering
		if (filter!=null) { // If filter is enabled
			boolean print= false;
			String port = filter.getPort();
			
			if(port==null) { // A port isn't specified, print the header
				print=true;
			}
			if (!print) { // Don't print the packet
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Frame doesn't pass IP filter");
				}
				return ""; // Return empty record because it didn't pass the filter
			}
		}		
		
		Object[] args=
			{
				typeh,
				code,
				checksum,
				next.getName().substring(next.getName().lastIndexOf('$') + 1)};

		return Formatter.jsprintf(
			"\t    "
				+ ICMPV6
				+ " "
				+ DATA
				+ " . : "
				+ TYPE
				+ ": {0} "
				+ CODE
				+ ": {1} "
				+ CHKSUM
				+ " : {2}\n"
				+ printHexHeader()
				+ "\t    "
				+ ICMPV6
				+ " {3} "
				+ MSG
				+ " :  ",
			args)
			+ printnext(filter);
	}

	/**
	 * Returns the type of this ICMPv6 Header from the trace. 
	 * @return String containing a decimal representation of the type of this header. 
	 */
	public String getTypeField() {
		return (new Dec(typeh.getData())).toString();
	}

	/**
	 * Returns the code of this ICMPv6 Header.
	 * @return String containing a decimal representation of the code for this header. 
	 */
	public String getCode() {
		return (new Dec(code.getData())).toString();
	}

	/**
	 * Returns the checksum of this ICMPv6 Header. 
	 * @return String containing a decimal representation of the checksum for this header. 
	 */
	public String getChecksum() {
		return (new Dec(checksum.getData())).toString();
	}
}