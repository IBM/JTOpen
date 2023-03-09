///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: NDOption.java
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
 * An ICMPv6 Neighbor Discovery Header.<br>
 * Extends Header's methods  to parse, print, and allow easy access to the Neighbor Discovery Header.
 */
public abstract class NDOption extends ICMP6Header {
	Field ndtype= new Dec(rawheader.slice(0, 8));
	Field length= new Dec(rawheader.slice(8, 8));

	/** A Neighbor Discovery Source LLA Header */
	public final static int SOURCELLA= 0x01;
	/** A Neighbor Discovery Target LLA Header */
	public final static int TARGETLLA= 0x02;
	/** A Neighbor Discovery Prefix Info Header */
	public final static int PREFIXINFO= 0x03;
	/** A Neighbor Discovery Redirected Header */
	public final static int REDIRECTED= 0x04;
	/** A Neighbor Discovery MTU Header */
	public final static int MTU= 0x05;

	final static String OPTDATA= "Option Data";
	final static String TYPE= "Type";
	final static String LEN= "Length";
	final static String ADDR= "Address";
	final static String PFXLEN= "PrefixLen";
	final static String PKT= "Packet";
	final static String VLDLIFETIME= "ValidLifeTime";
	final static String PRETIME= "PreferredTime";
	final static String PFX= "Prefix";
	final static String MTUSTR= "MTU";

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header.
	 */
	NDOption(BitBuf data) {
		super(data);
	}

	/**
	 * creates the correct header based off the data.
	 * @param BitBuf     The raw data of this header. 
	 */
	static NDOption createNDOption(BitBuf b) {
		if (b.getBitSize() == 0)
			return null;
		int ndtype= 0xFF & b.getOctet(0);
		if (ndtype == 0x01)
			return new SourceLLA(b);
		if (ndtype == 0x02)
			return new TargetLLA(b);
		if (ndtype == 0x03)
			return new PrefixInfo(b);
		if (ndtype == 0x04)
			return new RedirectedHeader(b);
		if (ndtype == 0x05)
			return new MTU(b);
		return null;
	}

	/**
	 * Returns the length of this header.
	 * @return The length of this header.
	 */
	public int getHeaderLen() {
		return rawheader.getOctet(8) * 64;
	}

	/**
	 * Returns the next header in the packet.
	 * @return Will always return either null or a NDOption header. 
	 */
	public Header getNextHeader() {
		if (rawpayload.getBitSize() == 0)
			return null;
		else
			return createNDOption(rawpayload);
	}

	/**
	 * Returns the Neighbor Discovery type of this NDOption Header. 
	 * @return String containing a decimal representation of the Neighbor Discovery type of this header. 
	 */
	public String getNDType() {
		return ndtype.toString();
	}

	/**
	 * Returns the length of this NDOption Header. 
	 * @return String containing a decimal representation of the length of this header. 
	 */
	public String getLength() {
		return length.toString();
	}
}