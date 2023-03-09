///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ICMP4Header.java
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
 * A Internet Control Message Protocol V4 Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the ICMPv4 Header.
 */
public class ICMP4Header extends Header {
	Field typefield= new Dec(rawheader.slice(0, 8));
	Field code= new Hex(rawheader.slice(8, 8));
	Field checksum= new Hex(rawheader.slice(16, 16));

	private static final String CLASS="ICMP4Header";
	private static final String ECHRPLY= "Echo Reply";
	private static final String DSTURCH= "Destination Unreachable";
	private static final String SRCQCH= "Source Quench";
	private static final String RDR= "Redirect (Change a Route)";
	private static final String ECHREQ= "Echo Request";
	private static final String TIMEEXC= "Time Exceeded for a Datagram";
	private static final String PARAMPROB= "Parameter Problem on a Datagram";
	private static final String TSTMPREQ= "Timestamp Request";
	private static final String TSTMPRPLY= "Timestamp Reply";
	private static final String INFOREQ= "Information Request (Obsolete)";
	private static final String INFORPLY= "Information Reply (Obsolete)";
	private static final String ADDRREQ= "Address Mask Request";
	private static final String ADDRRPLY= "Address Mask Reply";
	private static final String ICMP= "ICMPv4 Header";
	private static final String TYPE= "Type";
	private static final String CODE= "Code";
	private static final String CHKSUM= "Checksum";

	/**
	 * Creates and parses the data of this header.  
	 * @param data  The raw data of this header. 
	 */
	ICMP4Header(BitBuf data) {
		super(data);
		type= ICMP4;
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 32.
	 */
	public int getHeaderLen() {
		return 32;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		String types= "";
		int type= Integer.parseInt(this.typefield.toString());
		// The ICMP message number
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

		// Convert ICMP message from a number into a readable message
		if (type == 0) {
			types= ECHRPLY;
		} else if (type == 3) {
			types= DSTURCH;
		} else if (type == 4) {
			types= SRCQCH;
		} else if (type == 5) {
			types= RDR;
		} else if (type == 8) {
			types= ECHREQ;
		} else if (type == 11) {
			types= TIMEEXC;
		} else if (type == 12) {
			types= PARAMPROB;
		} else if (type == 13) {
			types= TSTMPREQ;
		} else if (type == 14) {
			types= TSTMPRPLY;
		} else if (type == 15) {
			types= INFOREQ;
		} else if (type == 16) {
			types= INFORPLY;
		} else if (type == 17) {
			types= ADDRREQ;
		} else if (type == 18) {
			types= ADDRRPLY;
		}

		Object[] args= { types, code, checksum };

		return Formatter.jsprintf(
			"\t    "
				+ ICMP
				+ " : "
				+ TYPE
				+ ": {0}     "
				+ CODE
				+ ": {1}     "
				+ CHKSUM
				+ ": {2}\n",
			args)
			+ printHexHeader()
			+ printnext(filter)
			+ (new Data(rawpayload)).toString();
	}

	/**
	 * Returns the type of this ICMPv4 Header from the trace.
	 * @return String containing a decimal representation of the type of this header. 
	 */
	public String getTypeField() {
		return typefield.toString();
	}

	/**
	 * Returns the code of this ICMPv4 Header. 
	 * @return String containing a decimal representation of the code for this header. 
	 */
	public String getCode() {
		return (new Dec(code.getData())).toString();
	}

	/**
	 * Returns the checksum of this ICMPv4 Header. 
	 * @return String containing a decimal representation of the checksum for this header. 
	 */
	public String getChecksum() {
		return (new Dec(checksum.getData())).toString();
	}
}