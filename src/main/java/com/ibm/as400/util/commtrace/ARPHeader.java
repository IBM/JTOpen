///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ARPHeader.java
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
 * An Address Resolution Protocol(ARP)/Reverse Address Resolution Protocol(RARP)
 * Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to 
 * the ARP/RARP Header.<br>
 */
public class ARPHeader extends Header {

    // Fields defined in RFC 826
    private Field hardwaretype = new Dec(rawheader.slice(0,16));
    private Field protocol = new Dec(rawheader.slice(16,16));
    private Field hdwlength = new Dec(rawheader.slice(32,8));
    private Field prtlength = new Dec(rawheader.slice(40,8));
    private Field operation = new Dec(rawheader.slice(48,16));
    private Field srchdwraddr = new Hex(rawheader.slice(64,48));
    private Field srcipaddr = new IP4Address(rawheader.slice(112,32));
    private Field dsthdwraddr = new Hex(rawheader.slice(144,48));
    private Field dstipaddr = new IP4Address(rawheader.slice(192,32));
    private int frmtype; // Parsed from the lan Header this field defines the frame type 
	private final static String CLASS = "ARPHeader";
	private final static String FRAME = "Frame Type";
	private final static String REQUEST = "REQUEST";
	private final static String RESPONSE = "RESPONSE";
	private final static String ARP = "ARP ";
	private final static String RARP = "RARP ";
	private final static String SRC = "Src Addr";
	private final static String DST = "Dst Addr";
	private final static String OPER = "Operation";
	
    /**
     * Creates and parses the data of this header.  
     * @param data   The portion of the frame that contains the arp data and all subsequent data.
     * @param frmtype   used to determine if the frame is ARP or RARP.
     */
    ARPHeader(BitBuf data,int frmtype) {
		super(data); // Allow the header contructor to parse the data
		this.frmtype=frmtype;
    }

    /**
     * Returns the length of this header.
     * @return	    Will always return 224.
     */
    public int getHeaderLen() {
        return 224;
    }

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
    public String toString(FormatProperties filter) {
		String operations = "",arp="";
		int oper = Integer.parseInt(operation.toString());

		// Make sure we have enough data to parse a full header
		if(rawheader.getBitSize() < getHeaderLen()) {
		    return (new Data(rawheader)).toString(); 
		}
	
		// Check for IP filtering
		if(filter!=null) { // If filter is enabled
			boolean print=false;
			String IPaddr = filter.getIPAddress(),
					IPaddr2 = filter.getSecondIPAddress(),
					port = filter.getPort();

			if(IPaddr==null && IPaddr2==null) {
				print=true; 
			// If only one address is specified.			
			} else if(IPaddr2==null) { 
				// If either address matches we print this record
				if(srcipaddr.toString().equals(IPaddr) 
					|| dstipaddr.toString().equals(IPaddr)) {
					print=true;    
				}
			// Both addresses were specified
			// The packet must match both addresses in any order before its 
			// printed
			} else if((srcipaddr.toString().equals(IPaddr) 
						|| srcipaddr.toString().equals(IPaddr2)) &&
						(dstipaddr.toString().equals(IPaddr) 
						|| dstipaddr.toString().equals(IPaddr2))) {
				print=true; 
			}
			if(port!=null) {// Port is specified don't print
				print=false;
			}
			if(!print) { // Don't print the packet
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Frame doesn't pass ARP IP Filter"); 
			    }
				return ""; // Return empty record because it didn't pass the filter
			}
		}

		// Based on the value of operation variable as defined in the RFC, make
		// it so we output text instead of a number 
		if(oper==1 || oper==3 || oper==8) {
			operations=REQUEST;
		} else if(oper==2 || oper==4 || oper==9) {
		    operations=RESPONSE;
		}
	
		// Convert frametype to a readable description
		if(frmtype==0x0806) {
		    arp=ARP;
		} else if(frmtype==0x8035) {
		    arp=RARP;
		}

		Object[] args = {
		    srcipaddr,
		    dstipaddr,
		    operations};

		// Return the header, the next packet, and the Data since data will always follow an ARP header.
		return Formatter.jsprintf(
		    "\t    " + FRAME + "   :  " + arp
            + SRC + ":  {0,16,L} " + DST + ":  {1,16,L} " + OPER + ": {2,8,L}\n",
		    args)
		    + printHexHeader()
		    + printnext(filter) + (new Data(rawpayload)).toString();
    }

    /**
     * Returns the hardware type of this packet. 
     * @return String containing a decimal representation of this hardware type.
     */
    public String getHardwareType() {
		return hardwaretype.toString();
    }

    /**
     * Returns the protocol of this packet 
     * @return String containing a decimal representation of this protocol. 
     */
    public String getProtocol() {
		return protocol.toString();
    }

    /**
     * Returns the byte length of each hardware address of this packet. 
     * @return String containing a decimal representation of the byte length of each hardware address of this packet.
     */
    public String getHardwareLength() {
		return hdwlength.toString();
    }

    /**
     * Returns the byte length of each protocol address of this packet. 
     * @return String containing a decimal representation of the byte length of each protocol address of this packet.
     */
    public String getProtocolLength() {
		return prtlength.toString();
    }

    /**
     * Returns the opcode of this packet.
     * @return String containing a decimal representation of the opcode of this packet.
     */
    public String getOpcode() {
		return operation.toString();
    }

    /**
     * Returns the hardware address of the sender of this packet.
     * @return String containing a hexadecimal representation of the hardware address of the sender of this packet.
     */
    public String getSourceHardwareAddress() {
		return srchdwraddr.toString();
    }

    /**
     * Returns the IP address of the source of this packet. 
     * @return String containing a ASCII delimited representation of the IP address of the source of this packet.
     */
    public String getSourceIPAddress() {
		return srcipaddr.toString();
    }

    /**
     * Returns the hardware address of the destination of this packet.
     * @return String containing a hexadecimal representation of the hardware address of the destination of this packet.
     */
    public String getDestinationHardwareAddress() {
		return dsthdwraddr.toString();
    }

    /**
     * Returns the IP address of the destination of this packet.
     * @return String containing a delimited representation of the IP address of the destination of this packet
     */
    public String getDestinationIPAddress() {
		return dstipaddr.toString();
    }
}
