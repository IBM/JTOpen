///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IP4Packet.java
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
 * A Internet Protocolv4 IPPacket.<br>
 * Extends Packets' methods to parse, print, and allow easy access to the IPv4 IPPacket<br>
 * It has one constructor which takes a byte array of packet data as its only argument.
 */
public class IP4Packet extends IPPacket {
	/**
	 * Creates and parses the data of this header.  
	 * @param data  The raw data of this header. 
	 */
	public IP4Packet(byte[] data) {
		rawpacket= data;
		header= new IP4Header(new BitBuf(rawpacket));
	}

    /**
     * Returns a printable representation of this packet.
     * @param filter	    FormatProperties object for filtering this packet.
     * @return	    Returns a string representation of this packet.
     */
	public String toString(FormatProperties filter) {
		return header.toString(filter);
	}
}