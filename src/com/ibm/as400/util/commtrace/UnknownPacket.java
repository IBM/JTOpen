///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnknownPacket.java
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
 * A Unknown IPPacket.<br>
 * Extends Packets's methods to parse, print, and allow easy access to the Unknown IPPacket.
 */
public class UnknownPacket extends IPPacket {
	/**
	 * Creates and parses the data of this packet. 
	 * @param data  The raw data of this packet. 
	 */
	public UnknownPacket(byte[] data) {
		rawpacket= data;
		header= new UnknownHeader(new BitBuf(rawpacket));
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
