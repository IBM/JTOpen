///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ARPPacket.java
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
 * An Address Resolution Protocol(ARP)/Reverse Address Resolution Protocol(RARP) Packet.<br>
 * Extends Packet's methods to parse, print, and allow easy access to the ARP Packet.
 */
public class ARPPacket extends Packet {
    private int frmtype;

    /**
     * Creates and parses the data of this header.
     * @param data   The packet of data.
     * @param frmtype   The frame type of this packet.
     */
    public ARPPacket(byte[] data,int frmtype) {
		rawpacket = data;
		this.frmtype = frmtype;
		header = new ARPHeader(new BitBuf(rawpacket),frmtype);
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

