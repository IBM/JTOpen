///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Header.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.io.StringWriter;
import java.util.Properties;

/**
 * Parent header class.<br>
 * Contains both the raw header and the ray payload.<br>
 * Maintains a pointer to the next header.<br>
 */
public abstract class Header {
    BitBuf rawheader;
    BitBuf rawpayload;
    Header next;
    int type;

    /** An ICMPv6 Header */
    public static final int ICMP6 = 0x3a;
    /** An IPv6 Header */
	public static final int	IP6 = 0x29;
	/** An ICMPv4 Header */
	public static final int	ICMP4 = 0x01;
	/** An IPv4 Header */
	public static final int	IP4 = 0x17;
	/** A TCP Header */
	public static final int TCP = 0x06;
	/** An UDP Header */
	public static final int	UDP = 0x11;
	/** An Extended Hop By Hop Header */
	public static final int EXTHOPBYHOP = 0x00;
	/** An Extended Routing Header */
	public static final int EXTROUTE = 0x2b;
	/** An Extended Fragment Header */
	public static final int EXTFRAG = 0x2c;
	/** An Extended ESP Header */
	public static final int EXTESP = 0x32;
	/** An Extended AH Header */
	public static final int EXTAH = 0x33;
	/** An Extended Destination Header */
	public static final int EXTDEST = 0x3c;
	/** An Unknown Header */
	public static final int UNK = 0xFF;

    /**
     * Base constructor which slices the data into header and payload.<br>
     * Creates the next header in this packet.
     * @param data      BitBuf which represents the raw data of this packet.
     */
    Header(BitBuf data) {
		rawheader = data;
		rawheader = data.slice(0, getHeaderLen());
		rawpayload = data.slice(getHeaderLen());

		next = getNextHeader();
    }

    /**
     * Creates the correct header based on the input byte.
     * @param p the protocol of this header.
     * @param data  the raw data of this header.
     * @return The Header
     */
    static Header createHeader(byte p, BitBuf data) {
		int protocol = p & 0xFF;

		if(data.getBitSize() == 0)
		    return null;
		if (protocol == 0x3a)
		    return new ICMP6Header(data);
		if (protocol == 0x29)
		    return new IP6Header(data);
	    if (protocol == 0x01)
            return new ICMP4Header(data);
        if (protocol == 0x17)
		    return new IP4Header(data);     
		if (protocol == 0x06)
		    return new TCPHeader(data);
		if (protocol == 0x11)
		    return new UDPHeader(data);
		// The following are Extension Headers
		if (protocol == 0x00)
		    return new HopByHop(data);
		if (protocol == 0x2b)
		    return new Routing(data);
		if (protocol == 0x2c)
		    return new Fragmentation(data);
		if (protocol == 0x32)
		    return new ESP(data);
		if (protocol == 0x33)
		    return new AH(data);
		if (protocol == 0x3b)
		    return null;
		if (protocol == 0x3c)
		    return new Destination(data);
		return new UnknownHeader(data);
    }

    /**
     * Dumps the raw fields from this header.
     * return	    String representing the raw fields of this header.
     */
    String fielddump() {
		StringWriter out = new StringWriter();
		java.lang.reflect.Field[] fieldlist = getClass().getFields();

		out.write("Fields from " + getName() + ":\n");
		int i;
		for (i = 0; i < fieldlist.length; i++) {
	     // Print the field if it is a Field subtype.
		    if (Field.class.isAssignableFrom(fieldlist[i].getType())) {
				try {
				    out.write(
						"\t" + fieldlist[i].getName() + ":  " + fieldlist[i].get(this) + "\n");
				} catch (Throwable x) {
				    out.write("\n\nfielddump: Caught exception:  " + x.toString() + "\n");
				}
		    }
		}

		return out.toString();
    }

    /**
     * Returns the data of this header.
     * @return byte[] containing the raw data.
     */
    public byte[] getHeaderData() {
        return rawheader.getBytes();
    }
    
    /**
     * Returns the length of this header.
     * @return Will always return 0.
     */
    public int getHeaderLen() {
        return 0;
    }

    /**
     * The name of this header.
     * @return The Name 
     */
    public String getName() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
    }

    /**
     * Returns the next header in this packet.
     * @return Will always return null.
     */
    public Header getNextHeader() {
		return null;
    }

    /**
     * Returns a description of the header along with this header as a hexadecimal string.
     * @return	    String containing this header as a hexadecimal string. 
     */
    public String printHexHeader() {
		String name = getName();
		int i;
		if((i = name.indexOf("$"))!=-1) {
		    name = name.substring(i+1);
		    name += " Header";
		}
		Object[] args = {name,rawheader.toHexString(32,"\n\t\t\t ")};

		return Formatter.jsprintf("\t    {0} :  {1}\n",args);
    }

    /**
     * Returns this header's raw payload.
     * @return	    byte[] containing this header's raw payload. 
     */
    public byte[] getPayLoad() {
        return rawpayload.getBytes();
    }

    /**
     * Returns the next header as a string.
     * @return	    String the next header.
     */
    String printnext() {
		if(next == null)
			return "";
		return next.toString();
    }

    /**
     * Returns the next header as a string.
     * @param filter	    FormatProperties object for filtering of this header.
     * @return	    String the next header.
     */
    String printnext(FormatProperties filter) {
		if(next == null)
			return "";
		return next.toString(filter);
    }

    /**
     * Returns a printable representation of this header. 
     * Without any specific formatting for the particular type of Header.
     * @return	    String this header.
     */
    public String toString() {
	    return fielddump() + printnext();
    }

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
    public String toString(FormatProperties filter) {
	    return fielddump() + printnext(filter);
    }

    /**
     * Returns this header as a hexadecimal string.
     * @return	    A hexadecimal representation of this header.
     */
    public String toHexString() {
		return rawheader.toHexString();
    }

    /**
     * Returns this header's payload as a hexadecimal string.
     * @return	    String a hexadecimal representation of this header's payload.
     */
    public String getPayloadHexString() {
		return rawpayload.toHexString();
    }

    /**
     * Returns this header's payload as a byte array.
     * @return	    byte[] containing this header's raw payload.
     */
    public byte[] getPayloadBytes() {
		return rawpayload.getBytes();
    }
    
    /**
     * Returns this header's payload as an ascii and hexadecimal string.
     * @return	    An ascii and hexadecimal representation of this header's payload.
     */
    public String getPayload() {
		return (new Data(rawpayload)).toString();
    }

    /**
     * Returns this header's type.
     * @return	    This header's type.
     */
    public int getType() {
		return type; 
    }
}
