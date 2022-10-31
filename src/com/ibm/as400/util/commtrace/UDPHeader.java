///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UDPHeader.java
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
 * An UDP Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the UDP Header.
 */
public class UDPHeader extends Header {
	private Field sourceport= new Dec(rawheader.slice(0, 16));
	private Field destport= new Dec(rawheader.slice(16, 16));
	private Field length= new Dec(rawheader.slice(32, 16));
	private Field checksum= new Hex(rawheader.slice(48, 16));

	private final static String CLASS = "UDPHeader";
	private final static String UNASSIGNED= "Unassigned";
	private final static String UDPSTR= "UDP";
	private final static String SRC= "SrcPort";
	private final static String DST= "DstPort";
	private final static String LEN= "Length";
	private final static String CHKSUM= "Checksum";

	/**
	 * Creates and parses the data of this header.  
	 * @param data  The raw data of this header.
	 */
	UDPHeader(BitBuf data) {
		super(data);

		type= UDP;
	}

	/**
	 * Returns the length of this header.
	 * @return Will always return 64.
	 */
	public int getHeaderLen() {
		return 64;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {

		// Check for Port filtering
		if (filter!=null) { // If filter is enabled
			boolean print= false;
			String port= filter.getPort();
			if (port == null) {
				print= true; // The filtering doesn't apply to this header
			} else if (
				port.equals(sourceport.toString()) || port.equals(destport.toString())) {
				print= true;
			}
			if (!print) { // Don't print the packet
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Not printing record");
				}
				return ""; // Return empty record because it didn't pass the filter
			}
		}

		String portname= (String) Port.get(this.sourceport.toString());
		String portname2= (String) Port.get(this.destport.toString());

		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}

		if (portname == null) {
			portname= UNASSIGNED;
		}
		if (portname2 == null) {
			portname2= UNASSIGNED;
		}
		String sourceport= this.sourceport.toString() + ", " + portname;
		String destport= this.destport.toString() + ", " + portname2;
		Object[] args= { sourceport, destport, length, checksum };

		return Formatter.jsprintf(
			"\t    "
				+ UDPSTR
				+ "  . . . . :  "
				+ SRC
				+ ":  {0,18,L} "
				+ DST
				+ ":  {1,18,L}\n"
				+ "\t\t\t    "
				+ LEN
				+ ":  {2,5,L} "
				+ CHKSUM
				+ ":  {3}\n",
			args)
			+ printHexHeader()
			+ printnext(filter)
			+ (new Data(rawpayload)).toString();
	}

	/**
	 * Returns the source port of this UDP Header. 
	 * @return String containing a decimal representation of the source port. 
	 */
	public String getSrcPort() {
		return sourceport.toString();
	}

	/**
	 * Returns the destination port of this UDP Header. 
	 * @return String containing a decimal representation of the destination port. 
	 */
	public String getDstPort() {
		return destport.toString();
	}

	/**
	 * Returns the length of this UDP Header.
	 * @return String containing a decimal representation of the length port. 
	 */
	public String getLength() {
		return length.toString();
	}

	/**
	 * Returns the raw data of this record. 
	 * @return String containing a hexadecimal representation of the data.
	 */
	public String getData() {
		return rawpayload.toHexString();
	}

	/**
	 * Returns the raw data of this record. 
	 * @return byte[] containing the data.
	 */
	public byte[] getByteData() {
		return rawpayload.getBytes();
	}
}
