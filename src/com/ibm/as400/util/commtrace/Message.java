///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Message.java
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
 * An ICMPv6 Message Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the ICMPv6 Message Header.
 */
public abstract class Message extends ICMP6Header {

	/** A Unknown Message */
	public final static int MSGUNK= 0x00;
	/** A Router Solicitation Message */
	public final static int RTRSOL= 0x85;
	/** A Router Advertisement Message */
	public final static int RTRADV= 0x86;
	/** A Neighbor Solicitation Message */
	public final static int NGHSOL= 0x87;
	/** A Neighbor Advertisement Message */
	public final static int NGHADV= 0x88;
	/** A Redirect Message */
	public final static int REDR= 0x89;
	/** A Destination Unreachable Message */
	public final static int DSTUNR= 0x01;
	/** A Packet To Big Message */
	public final static int PKTTOBIG= 0x02;
	/** A Time Exceeded Message */
	public final static int TIMEEXC= 0x03;
	/** A Parameter Problem Message */
	public final static int PARMPROB= 0x04;
	/** A Echo Request Message */
	public final static int ECHOREQ= 0x80;
	/** A Echo Reply Message */
	public final static int ECHQRPL= 0x81;
	/** A Multicast Listener Query Message */
	public final static int MLTLSTQRY= 0x82;
	/** A Multicast Listener Report Message */
	public final static int MLTLSTRPT= 0x83;
	/** A Multicast Listener Done Message */
	public final static int MLTLSTDNE= 0x84;

	final static String OFFPKT= "Offending IPPacket";
	final static String PTR= "Pointer";
	final static String ID= "Identifier";
	final static String SEQNUM= "SequenceNumber";
	final static String CURHOPLIMIT= "CurHopLimit";
	final static String RTRLIFETIME= "RouterLifeTime";
	final static String RCHTIME= "ReachableTime";
	final static String RETRANSTIME= "RetransTimer";
	final static String O= "O";
	final static String M= "M";
	final static String TARGET= "Target";
	final static String FLAGS= "Flags";
	final static String RTR= "Router";
	final static String SOL= "Solicited";
	final static String OVR= "Override";
	final static String DST= "Destination";
	final static String MAX= "Max Response Delay";
	final static String ADDR= "Address";

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	Message(BitBuf data) {
		super(data);
	}

	/**
	 * Creates the correct message for this header.
	 * @param type The byte indicating the type of this heade.
	 * @param data The raw data of this header. 
	 */
	static Message createMessage(byte t, BitBuf data) {
		int msgtype= t & 0xFF;

		if (msgtype == 0x85)
			return new RouterSolicitation(data);
		if (msgtype == 0x86)
			return new RouterAdvertisement(data);
		if (msgtype == 0x87)
			return new NeighborSolicitation(data);
		if (msgtype == 0x88)
			return new NeighborAdvertisement(data);
		if (msgtype == 0x89)
			return new Redirect(data);
		if (msgtype == 0x01)
			return new DestinationUnreachable(data);
		if (msgtype == 0x02)
			return new PacketTooBig(data);
		if (msgtype == 0x03)
			return new TimeExceeded(data);
		if (msgtype == 0x04)
			return new ParameterProblem(data);
		if (msgtype == 0x80)
			return new EchoRequest(data);
		if (msgtype == 0x81)
			return new EchoReplyRequest(data);
		if (msgtype == 0x82)
			return new MulticastListenerQuery(data);
		if (msgtype == 0x83)
			return new MulticastListenerReport(data);
		if (msgtype == 0x84)
			return new MulticastListenerDone(data);
		return new UnknownMessage(data);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		return printnext(filter);
	}
}