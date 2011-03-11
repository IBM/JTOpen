///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IPPacket.java
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
 * Abstract packet class.<br> 
 * Contains this packet's rawdata and type.<br>
 * Allows for easy access to this packet.
 */
public abstract class IPPacket {
    Header header;
    byte[] rawpacket;
    int type;

    /** Address Resolution Protocol*/
    public static final int ARP = 0x0806;
	/** Reverse Address Resolution Protocol */
	public static final int RARP = 0x8035;
	/** Internet Protocol Version Four */
	public static final int IP4 = 0x0800;
	/** Internet Protocol Version Six */
	public static final int IP6 = 0x86DD;
	/** Unknown IPPacket */
	public static final int UNK = 0x0000;

    /**
     * Default constructor. Creates a simple packet.
     */
    public IPPacket() {
    }
	
    /**
     * Returns a printable representation of this packet.
     * @param filter	    FormatProperties object for filtering this packet.
     * @return	    Returns a string representation of this packet.
     */
	public String toString(FormatProperties filter) {
		return "";
	}

    /**
     * Returns the header of this packet. 
     * @return	    Header the header of this packet. 
     */
    public Header getHeader() {
		return header;
    }

    /**
     * The name of this packet 
     * @return The name
     */
    public String getName() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
    }

    /**
     * Returns the type of this packet. 
     * @return The type of this packet. 
     */
    public int getType() {
		return type;
    }

    /**
     * Sets the type of this packet.
     * @param type the type of this packet. 
     */
    public void setType(int type) {
		this.type=type;
    }
}

