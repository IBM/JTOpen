///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IP4Header.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400SecurityException;
import java.util.Properties;

/**
 * A Internet Protocol v4 Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the ICMPv4 Header.
 */
public class IP4Header extends Header {
	
	private static final String CLASS="IP4Header";

	// Fields defined in RFC 760
	Field version= new Dec(rawheader.slice(0, 4));
	Field internetheaderl= new Dec(rawheader.slice(4, 4));
	Field typeofservice= new Hex(rawheader.slice(8, 8));
	Field dscp= new Dec(rawheader.slice(8, 6));
	Field ecn= new Dec(rawheader.slice(14, 2)); // RFC 3168
	Field totallength= new Dec(rawheader.slice(16, 16));
	Field identification= new Hex(rawheader.slice(32, 16));
	Field flag= new FragmentFlag(rawheader.slice(48, 2));
	Field flag2= new FragmentFlag(rawheader.slice(50, 4));
	Field fragmentoffset= new Dec(rawheader.slice(52, 12));
	Field timetolive= new Dec(rawheader.slice(64, 8));
	Field protocol= new Dec(rawheader.slice(72, 8));
	Field headerchecksum= new Dec(rawheader.slice(80, 16));
	Field src= new IP4Address(rawheader.slice(96, 32));
	Field dst= new IP4Address(rawheader.slice(128, 32));
	Field options=
		new Hex(
			rawheader.slice(
				160,
				((Integer.parseInt(internetheaderl.toString()) * 8) - 40)));

	// Fields from the RFC
	private final static String RES= "RESERVED";
	private final static String ICMP= "ICMP";
	private final static String IGMP= "IGMP";
	private final static String TCP= "TCP";
	private final static String UDP= "UDP";
	private final static String IPV6= "IPv6";
	private final static String UNK= "Unknown";
	private final static String MAY= "MAY";
	private final static String DONT= "DON'T";
	private final static String LAST= ",LAST";
	private final static String MORE= ",MORE";
	private final static String NONE= "NONE";
	private final static String NECT= "00 - NECT";
	private final static String ECT1= "01 - ECT1";
	private final static String ECT0= "10 - ECT0";
	private final static String CE= "11 - CE";
	private final static String FRM= "Frame Type";
	private final static String IP= "IP";
	private final static String DSCP= "DSCP";
	private final static String ECN= "ECN";
	private final static String LEN= "Length";
	private final static String PROT= "Protocol";
	private final static String DGID= "DataGram ID";
	private final static String SRC= "Src Addr";
	private final static String DST= "Dest Addr";
	private final static String FRAGFLAG= "Fragment Flags";
	private final static String IPOPT= "IP Options";

	/**
	 * Base constructor which creates a IPv4 Header.  
	 * @param data         BitBuf which represents this header's raw data.
	 * @see		Header
	 */
	IP4Header(BitBuf data) {
		super(data);

		type= IP4;
	}

	public int getHeaderLen() {
		if (internetheaderl == null) {
			internetheaderl= new Dec(rawheader.slice(4, 4));
		}
		return Integer.parseInt(internetheaderl.toString()) * 32;
	}

	public Header getNextHeader() {
		return Header.createHeader(rawheader.slice(72, 8).toByte(), rawpayload);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		String protocols= protocol.toString(), flag= "";

		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}

		// Check for IP filtering
		if (filter!=null) { // If filter is enabled
			boolean print= false;
			String IPaddr= filter.getIPAddress(),
				IPaddr2= filter.getSecondIPAddress();

			if (IPaddr == null && IPaddr2 == null) {
				print= true; // The filtering doesn't apply to this part of the header
			} else if(IPaddr==null) {
				// If either address matches we print this record
				if (src.toString().equals(IPaddr2) || dst.toString().equals(IPaddr2)) {
					print= true;
				}
			} else if (IPaddr.indexOf(':') != -1 || IPaddr.indexOf(':') != -1) {
				print= true; // IPv6 Address so don't bother to try and filter ia
				// If only one address is specified.
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
					Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Frame doesn't pass IP filter");
				}
				return ""; // Return empty record because it didn't pass the filter
			}
		}

		char[] flagss= (this.flag.toString()).toCharArray();
		char[] flagss2= (this.flag2.toString()).toCharArray();

		// Convert protocol from number into readable text
		if (protocols.equals("0")) {
			protocols= RES;
		} else if (protocols.equals("1")) {
			protocols= ICMP;
		} else if (protocols.equals("2")) {
			protocols= IGMP;
		} else if (protocols.equals("6")) {
			protocols= TCP;
		} else if (protocols.equals("17")) {
			protocols= UDP;
		} else if (protocols.equals("41")) {
			protocols= IPV6;
		} else if (protocols.equals("01")) {
			protocols= ICMP;
		} else if (protocols.equals("255")) {
			protocols= RES;
		} else {
			protocols= UNK;
		}

		// Flags as defined in RFC 760
		switch (flagss.length) {
			case 1 :
				if (flagss[0] == '0') {
					flag= MAY;
				} else {
					flag= DONT;
				}
				break;
			case 2 :
				if (flagss[0] == '0' && flagss[1] == '0') {
					flag= MAY;
				} else {
					flag= DONT;
				}
				break;
			default :
				break;
		}
		switch (flagss2.length) {
			case 1 :
				if (flagss2[0] == '0') {
					flag += LAST;
				} else {
					flag += MORE;
				}
				break;
			case 2 :
				if (flagss2[0] == '0' && flagss2[1] == '0') {
					flag += LAST;
				} else {
					flag += MORE;
				}
				break;
			default :
				break;
		}

		Object[] args=
			{ totallength, identification, flag, protocols, src, dst, options, dscp, ecn };

		if ((options.toString()).equals("0x")) {
			args[6]= NONE;
		}

		if (Integer.parseInt(dscp.toString()) == 0x00) {
			args[7]= "0";
		}

		switch (Integer.parseInt(ecn.toString())) {
			case 0 :
				args[8]= NECT;
				break;
			case 1 :
				args[8]= ECT1;
				break;
			case 2 :
				args[8]= ECT0;
				break;
			case 3 :
				args[8]= CE;
				break;
		}

		String next= printnext(filter);
		if (next == "") { // The header didn't pass the filter
			return "";
		} else {
			return Formatter.jsprintf(
				"\t    "
					+ FRM
					+ "   :  "
					+ IP
					+ " "
					+ "\t\t"
					+ DSCP
					+ ": {7,2,L}  "
					+ ECN
					+ ": {8,10,L}    "
					+ LEN
					+ ":  {0,4,L}   Protocol:  {3,10,L}   DataGram ID:  {1,6,L}\n"
					+ "\t\t\t    "
					+ SRC
					+ ":  {4,15,L}    "
					+ DST
					+ ":  {5,15,L}       "
					+ FRAGFLAG
					+ ":  {2,10,L}\n"
					+ "\t    "
					+ IPOPT
					+ "   :  {6,80,L}\n",
				args)
				+ printHexHeader()
				+ next;
		}
	}

	/**
	 * Returns the version of this IPv4 Header. 
	 * @return String containing a decimal representation of the version for this header. 
	 */
	public String getVersion() {
		return version.toString();
	}

	/**
	 * Returns the Internet Header length of this IPv4 Header. 
	 * @return String containing a decimal representation of the header length.
	 */
	public String getInternetHeaderLength() {
		return internetheaderl.toString();
	}

	/**
	 * Returns the type of service of this IPv4 Header. 
	 * @return String containing a decimal representation of the type of service for this header. 
	 */
	public String getTypeOfService() {
		return typeofservice.toString();
	}

	/**
	 * Returns the dscp of this IPv4 Header. 
	 * @return String containing a decimal representation of the dscp for this header. 
	 */
	public String getDscp() {
		return dscp.toString();
	}

	/**
	 * Returns the ecn of this IPv4 Header. 
	 * @return String containing a decimal representation of the ecn for this header. 
	 */
	public String getEcn() {
		return ecn.toString();
	}

	/**
	 * Returns the total length of this IPv4 Header. 
	 * @return String containing a decimal representation of the total length for this header. 
	 */
	public String getTotalLength() {
		return totallength.toString();
	}

	/**
	 * Returns the indentification of this IPv4 Header. 
	 * @return String containing a decimal representation of the identification for this header. 
	 */
	public String getIdentification() {
		return identification.toString();
	}

	/**
	 * Returns the first flag of this IPv4 Header. 
	 * @return String containing a decimal representation of the flag. 
	 */
	public String getFlag() {
		return flag.toString();
	}

	/**
	 * Returns the second flag of this IPv4 Header. 
	 * @return String containing a decimal representation of the flag.
	 */
	public String getFlag2() {
		return flag2.toString();
	}

	/**
	 * Returns the fragment offset of this IPv4 Header.
	 * @return String containing a decimal representation of the fragment offest for this header. 
	 */
	public String getFragmentOffset() {
		return fragmentoffset.toString();
	}

	/**
	 * Returns the time to live of this IPv4 Header. 
	 * @return String containing a decimal representation of the time to live for this header. 
	 */
	public String getTimeToLive() {
		return timetolive.toString();
	}

	/**
	 * Returns the protocol of this IPv4 Header. 
	 * @return String containing a decimal representation of the protocol for this header. 
	 */
	public String getProtocol() {
		return protocol.toString();
	}

	/**
	 * Returns the header check sum.
	 * @return String containing a decimal representation of the header check sum.
	 */
	public String getHeaderChecksum() {
		return headerchecksum.toString();
	}

	/**
	 * Returns the source IP address of this IPv4 Header. 
	 * @return String containing a delimited decimal representation of the source IP address.
	 */
	public String getSrcAddr() {
		return src.toString();
	}

	/**
	 * Returns the destination IP address of this IPv4 Header. 
	 * @return String containing a delimited decimal representation of the destination IP address.
	 */
	public String getDstAddr() {
		return dst.toString();
	}

	/**
	 * Returns the option of this IPv4 header. 
	 * @return String containing a decimal representation of the options. 
	 */
	public String getOptions() {
		return (new Dec(options.getData())).toString();
	}
}
