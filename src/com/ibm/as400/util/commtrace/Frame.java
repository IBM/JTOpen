///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Frame.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.io.*;
import com.ibm.as400.access.Trace;
import java.util.Properties;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Encapsulates all the data for one record of the trace.<br>
 * Parses the record data and creates a printable representation of this 
 * record.<br>
 * The data in any packet is mainted in a linked list like structure. The 
 * format of any record will be one of the following:<br>
 *	    <ul>
 *          <li>arp/rarp</li>                                               
 *          <li>ip4,tcp</li>
 *          <li>ip4,udp</li>
 *          <li>ip4,icmp6</li>                                             
 *          <li>ip6,tcp</li>
 *          <li>ip6,udp</li>
 *           <li>ip6,icmp6</li>
 *           <li>The next 3 are for tunneled packets</li>
 *           <li>ip4,ip6,tcp</li>
 *           <li>ip4,ip6,udp</li>
 *           <li>ip4,ip6,icmp6</li>
 *           <li>ip6,eh,tcp</li>
 *           <li>ip6,eh,udp</li>
 *           <li>ip6,eh,icmp6</li>
 *           <li>The next 3 are for tunneled packets</li>
 *           <li>ip4,ip6,eh,tcp</li> 
 *           <li>ip4,ip6,eh,udp</li>
 *           <li>ip4,ip6,eh,icmp6</li>
 *      </ul>
 *          <b>eh</b> is the extended header<br>
 *          <b>Note:</b> Every set of headers is followed by the raw data if 
 *          applicable<br>
 *          The classes in the structure are accessible through the 
 *          Frame.getPacket() and Header.getNextHeader() methods. The type is 
 *          accessible through the Header.getType() method
 */
public class Frame {
	private final static String BROADCAST= "0xFFFFFFFFFFFF", CLASS="Frame";
	private BitBuf data;
	private Prolog pro;
	private int frmhdrl, datalen, frmtype, ifshdr= 176;
	private IPPacket packet; // The parsed packet of data
	private Field IFSRECN, 		/* Frame number              */
					IFSRCTD, 	/* 64 bit TOD                 */
					IFSRECTP, 	/* Frame type                */
					IFSPDULN, 	/* PDU length                 */
					IFSRECST, 	/* Frame status              */
					IFSSLT, 	/* SDLC slot                  */
					IFSPORT,	/* SDLC port                  */
					IFSLLC, 	/* LAN frame is LLC 'FF'x     */
					IFSRTLN, 	/* LAN routing length         */
					IFSTCP; 	/* Frame is TCP Y/N           */
	private Time tod_; // The time of this trace
	private boolean tcp= true; // If this record isn't TCP this field is set

	private LanHeader lnHdr; // The 22 byte LAN header 

	/**
	 * Creates a record which parses the raw data contained in this packet. 
	 * @param pro       Prolog to this trace. 
	 * @param data      BitBuf with the raw data.
	 */
	Frame(Prolog pro, BitBuf data) {
		this.data= data;
		this.pro= pro;
		IFSRECN= new Dec(data.slice(0, 16));
		IFSRCTD= new Dec(data.slice(16, 64));
		IFSRECTP= new Char(data.slice(80, 8));
		IFSPDULN= new Dec(data.slice(88, 16));
		IFSRECST= new Dec(data.slice(104, 32));
		IFSSLT= new Dec(data.slice(136, 8));
		IFSPORT= new Dec(data.slice(144, 8));
		IFSLLC= new Hex(data.slice(152, 8));
		IFSRTLN= new Dec(data.slice(160, 8));
		IFSTCP= new Char(data.slice(168, 8));

		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(
				Trace.INFORMATION,
				CLASS + ".Frame() " + "Creating Frame " + IFSRECN.toString() + "... tcp:" + IFSTCP.toString());
		}

		if ((IFSTCP.toString()).equals("Y")) { // If the record is TCP parse it
			tcp= true;
			lnHdr= new LanHeader(data, IFSLLC, pro.getProtocol());
			frmhdrl= lnHdr.getDataStart() + ifshdr;
			parseIPdata(new BitBuf(data, frmhdrl, (data.getBitSize() - frmhdrl)));
		} else {
			tcp= false;
		}
	}

	/**
	 * Parses the IP data of this record.
	 * @param hdr       BitBuf with this records IP data.
	 */
	private void parseIPdata(BitBuf hdr) {
		frmtype= lnHdr.getFrameType();

		// Create the correct packet based on the frame type
		if (frmtype == 0x86DD) {
			packet= new IP6Packet(hdr.getBytes());
			packet.setType(IPPacket.IP6);
		} else if (frmtype == 0x0800) {
			packet= new IP4Packet(hdr.getBytes());
			packet.setType(IPPacket.IP4);
		} else if (frmtype == 0x0806) {
			packet= new ARPPacket(hdr.getBytes(), frmtype);
			packet.setType(IPPacket.ARP);
		} else if (frmtype == 0x8035) {
			packet= new ARPPacket(hdr.getBytes(), frmtype);
			packet.setType(IPPacket.RARP);
		} else {
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".parseIPData() " + "Unknown IPPacket");
			}
			packet= new UnknownPacket(hdr.getBytes());
			packet.setType(IPPacket.UNK);
		}
	}

    /**
     * Returns a printable representation of this record.
     * @param filter	    FormatProperties object for filtering this record.
     * @return Returns a string representation of this record.
     */
	public String toString(FormatProperties filter) {
		StringBuffer ret= new StringBuffer(); // The return string
		String time;
		if (tcp) { // If its not TCP just return
			// Create our Time object with the timestamp
			if (tod_ == null) {
				tod_= new Time(Long.parseLong((IFSRCTD.toString())));
			}
			// Get a printable time
			time= tod_.getTime();

			// If user doesn't want broadcast and the Mac address is broadcast 
			// then just return

			if (filter!=null) { // If filter is enabled
				boolean print= false;
				String broadcast= filter.getBroadcast();
				String starttime= filter.getStartTime();
				String endtime= filter.getEndTime();
				long timestamp= tod_.getTimeStamp();
				if (starttime == null
					&& endtime == null) {
					// The filter doesn't apply to the two filters for this Frame we will continue on
					print= true;
				} else if (
					starttime != null && endtime == null) { // A start time but no end time
					// Timestamp is greater then the start time we print the record
					if (timestamp >= Long.parseLong(starttime)) {
						print= true;
					}
				} else { // Start and end time both specified
					// Timestamp is less then the end time but greater then the start time we print the record
					if (Long.parseLong(endtime) >= timestamp
						&& timestamp >= Long.parseLong(starttime)) {
						print= true;
					}
				}
				if (broadcast == null) { // Filtering doesn't apply to the Broadcast
				} else if (
					broadcast.equals(FormatProperties.NO)
						&& (lnHdr.getMacAddress()).equals(
							BROADCAST)) { // If the packet is broadcast don't print it
					print= false;
				}
				if (!print) { // Don't print the packet
					if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
						Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Frame doesn't pass filters. Not printing");
					}
					return "";
				}
			}

			Object[] args=
				{
					IFSRECN,
					IFSRECTP,
					new Integer(Integer.parseInt(IFSPDULN.toString()) - (lnHdr.getDataStart() / 8)),
				// Calculate the data length
				time };

			// Add the record data
			ret.append(
				(Formatter
					.jsprintf(
						"{0,6,R}" + "{1,5,R}" + "{2,8,R}" + "{3,16,R}" + lnHdr.toString(),
						args)));

			if ((pro.getProtocol()).equals("E")) { // Token Ring?
				ret.append(lnHdr.printRoutingData()); // Append the Routing data
			}
			String returnpacket= packet.toString(filter);

			if (returnpacket.equals("")) {
				// The packet wasn't printed so return an empty record
				return "";
			} else { // Append the packet
				ret.append(returnpacket);
			}

			// If there is data that wasn't traced append the amount to the 
			// end of the record
			int notTraced= Integer.parseInt(IFSPDULN.toString()) - data.getByteSize() + 22;
			if (notTraced > 0) {
				ret.append(
					"\t\t\t * * * * * * * * * * * * * *     "
						+ notTraced
						+ "  BYTES OF DATA NOT TRACED  "
						+ "* * * * * * * * * * * * * *\n");
			}
			return ret.toString();
		} else {
			return "";
		}
	}

	/**
	 * Returns the packet contained by this record.
	 * @return	    IPPacket this packet. 
	 */
	public IPPacket getPacket() {
		return packet;
	}

	/**
	 * Returns the record number. 
	 * @return	    String containing the record number. 
	 */
	public String getRecNum() {
		return IFSRECN.toString();
	}

	/**
	 * Returns 64 bit Time of Day. 
	 * @return	    String containing the TOD.
	 */
	public String getTOD() {
		return IFSRCTD.toString();
	}

	/**
	 * Return the Time of day in HH:MM:SS.mm format.
	 * @return		String containing the Time.
	 */
	public String getTime() {
		if (tod_ == null) {
			tod_= new Time(Long.parseLong((IFSRCTD.toString())));
		}
		// Get a printable time
		return tod_.getTime();
	}

	/**
	 * Returns the record type. 
	 * @return	    String containing the record type. 
	 */
	public String getRecType() {
		return IFSRECTP.toString();
	}

	/**
	 * Returns the PDU length.    
	 * @return	    String containing the PDU length.
	 */
	public String getPDUType() {
		return IFSPDULN.toString();
	}

	/**
	 * Returns the record status. 
	 * @return	    String containing the record status.
	 */
	public String getRecStatus() {
		return IFSRECST.toString();
	}

	/**
	 * Returns the SDLC slot. 
	 * @return	    String containing the SDLC slot. 
	 */
	public String getSDLCSlot() {
		return IFSSLT.toString();
	}

	/**
	 * Returns the SDLC port. 
	 * @return	    String containing the SDLC port. 
	 */
	public String getSDLCPort() {
		return IFSPORT.toString();
	}

	/**
	 * Returns the code determining if the LAN fame is LLC.<br>
	 * 0xFF = LLC
	 * @return	    String containing the code, 
	 */
	public String getLLC() {
		return IFSLLC.toString();
	}

	/**
	 * Returns the LAN routing length, 
	 * @return	    String containing the LAN routing length,
	 */
	public String getRoutingLength() {
		return IFSRTLN.toString();
	}

	/**
	 * Returns if the frame is TCP Y/N. 
	 * @return	    String containing Y or N. 
	 */
	public String getTCP() {
		return IFSTCP.toString();
	}

	/**
	 * Returns a boolean indicating if the frame is TCP 
	 * @return true if record is TCP. 
	 */
	public boolean isTCP() {
		return tcp;
	}
}
