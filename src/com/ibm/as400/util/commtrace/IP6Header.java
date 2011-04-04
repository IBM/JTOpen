///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IP6Header.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.Trace;
import java.util.Properties;

/**
 * An IPv6 Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the IPv6 Header.
 */
public class IP6Header extends Header {
	
	Field version= new Dec(rawheader.slice(0, 4));
	Field trafficclass= new Hex(rawheader.slice(4, 8));
	Field flowlabel= new Hex(rawheader.slice(12, 20));
	Field payloadlength= new Dec(rawheader.slice(32, 16));
	Field nextheaderh= new Hex(rawheader.slice(48, 8));
	Field nextheader= new Dec(rawheader.slice(48, 8));
	Field hoplimit= new Dec(rawheader.slice(56, 8));
	Field src= new IP6Address(rawheader.slice(64, 128));
	Field dst= new IP6Address(rawheader.slice(192, 128));

	private final static String CLASS="IP6Header";
	private final static String ICMPV6= "ICMPv6";
	private final static String TCP= "TCP";
	private final static String UDP= "UDP";
	private final static String EXTHOPHOP= "ExtHeader Hop by Hop";
	private final static String EXTROUTE= "ExtHeader Routing";
	private final static String EXTFRAG= "ExtHeader Fragmentation";
	private final static String EXTESP= "ExtHeader ESP";
	private final static String EXTAH= "ExtHeader AH";
	private final static String EXTDST= "ExtHeader Destination";
	private final static String IPV6DATA= "IPv6 Data";
	private final static String VER= "Ver";
	private final static String TRAFFICCLASS= "TrafficClass";
	private final static String FLWLBL= "FlowLabel";
	private final static String PAYLOADLENGTH= "PayloadLength";
	private final static String NXTHDR= "NextHeader";
	private final static String HOPLIMIT= "HopLimit";
	private final static String SRC= "Src";
	private final static String DST= "Dst";

	/**
	 * Creates and parses the data of this header.  
	 * @param data     The raw data of this header. 
	 */
	IP6Header(BitBuf data) {
		super(data);
		type= IP6;
	}

	/**
	 * Returns the length of this header.
	 * @return	    Will always return 320.
	 */
	public int getHeaderLen() {
		return 320;
	}

	public Header getNextHeader() {
		return Header.createHeader(rawheader.slice(48, 8).toByte(), rawpayload);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		String nextheaders= "";
		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}
		int protocol= Integer.parseInt(nextheader.toString());

		// Check for IP filtering
		if (filter!=null) { // If filter is enabled
			boolean print= false;
			String IPaddr= filter.getIPAddress(),
				IPaddr2= filter.getSecondIPAddress();
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Filter Properties:" + IPaddr + " " + IPaddr2);
			}
			if (IPaddr == null && IPaddr2 == null) {
				print= true; // The filtering doesn't apply to this header
				// If only one address is specified.	
			} else if(IPaddr==null) {
				// If either address matches we print this record
				if (src.toString().equals(IPaddr2) || dst.toString().equals(IPaddr2)) {
					print= true;
				}		
			} else if (IPaddr2 == null) {
				// If either address matches we print this record
				if (src.toString().equals(IPaddr) || dst.toString().equals(IPaddr)) {
					print= true;
				}
				// Both addresses were specified
				// The packet must match both addresses in any order before its 
				// printed
			} else if (
				(src.toString().equals(IPaddr) || src.toString().equals(IPaddr2))
					&& (dst.toString().equals(IPaddr) || dst.toString().equals(IPaddr2))) {
				print= true;
			}
			if (!print) { // Don't print the packet
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION, "Not printing record");
				}
				return ""; // Return empty record because it didn't pass the filter
			}
		}

		// Make the protocol a readable string instead of just a number
		if (protocol == 0x3a)
			nextheaders= ICMPV6;
		if (protocol == 0x06)
			nextheaders= TCP;
		if (protocol == 0x11)
			nextheaders= UDP;
		if (protocol == 0x00)
			nextheaders= EXTHOPHOP;
		if (protocol == 0x2b)
			nextheaders= EXTROUTE;
		if (protocol == 0x2c)
			nextheaders= EXTFRAG;
		if (protocol == 0x32)
			nextheaders= EXTESP;
		if (protocol == 0x33)
			nextheaders= EXTAH;
		if (protocol == 0x3c)
			nextheaders= EXTDST;

		Object[] args=
			{
				version,
				src,
				dst,
				hoplimit,
				flowlabel,
				trafficclass,
				payloadlength,
				nextheaders,
				nextheaderh,
				};

		String next1= printnext(filter);
		if (next1 == "") { // The header didn't pass the filter
			return "";
		} else {
			return Formatter.jsprintf(
				"\t    "
				+ IPV6DATA
				+ " . .:"
				+ "  "
				+ VER
				+ " : {0,2,R} \t\t "
				+ TRAFFICCLASS
				+ ": {5}  \t\t    "
				+ FLWLBL
				+ ": {4}\n"
				+ "\t\t\t    "
				+ PAYLOADLENGTH
				+ ": {6,5,R} \t"
				+ NXTHDR
				+ ": {8,4,L},{7,10,L} "
				+ HOPLIMIT
				+ ": {3,3,R}\n"
				+ "\t\t\t    "
				+ SRC
				+ ": {1}\n"
				+ "\t\t\t    "
				+ DST
				+ ": {2}\n",
			args)
			+ printHexHeader()
			+ next1;
		}
	}

	/**
	 * Returns the version of this Header. 
	 * @return String containing a decimal representation of the version. 
	 */
	public String getVersion() {
		return version.toString();
	}

	/**
	 * Returns the traffic class of this Header.
	 * @return String containing a decimal representation of the traffic class. 
	 */
	public String getTrafficClass() {
		return (new Dec(trafficclass.getData())).toString();
	}

	/**
	 * Returns the flow label of this Header. 
	 * @return String containing a decimal representation of the flow label. 
	 */
	public String getFlowLabel() {
		return (new Dec(flowlabel.getData())).toString();
	}

	/**
	 * Returns the payload length of this Header. 
	 * @return String containing a decimal representation of the payload length. 
	 */
	public String getPayloadLength() {
		return payloadlength.toString();
	}

	/**
	 * Returns the next header identifier. 
	 * @return String containing a decimal representation of the next header identifier. 
	 */
	public String getNextHeaderDec() {
		return nextheader.toString();
	}

	/**
	 * Returns the hop limit of this Header.
	 * @return String containing a decimal representation of the hop limit. 
	 */
	public String getHopLimit() {
		return hoplimit.toString();
	}

	/**
	 * Returns the source IPv6 Address of this Header. 
	 * @return String containing a delimited decimal representation of the source address. 
	 */
	public String getSrcAddr() {
		return src.toString();
	}

	/**
	 * Returns the destination IPv6 Address of this Header 
	 * @return String containing a delimited decimal representation of the destination address 
	 */
	public String getDstAddr() {
		return dst.toString();
	}
}
