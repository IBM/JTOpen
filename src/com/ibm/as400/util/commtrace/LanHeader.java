///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: LanHeader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Allows the user to parse, print, and have easy access to the LanHeader.
 */
public class LanHeader {
	//private BitBuf data;
	private Field llc;
	private String protocol, frameformat;
	private Field lanroutel, eth2sourcemac, // ETH V2 fields
	eth2destmac,
		eth2frametypeh,
		eth2frametype,
		ethsourcemac,
	// 802.3 Ethernet fields
		ethdestmac,
		ethsaps,
		ethframetypeh,
		ethframetype,
		trnsourcemac,
	// Tokenring fields 
		trndestmac,
		trnrouteinfo,
		trnsaps,
		trnframetypeh,
		trnframetype,
		frametype,
	// Generic fields
		sourcemac,
		destmac;

	/**
	 * creates and parses the Lan Header. 
	 * @param data	The raw data from this packet.
	 * @param f	Field which tells if this packet is LLC or not.
	 * @param protocol	this packet's protocol.
	 */
	LanHeader(BitBuf data, Field f, String protocol) {
		//this.data= data;
		this.llc= f;
		this.protocol= protocol;
		lanroutel= new Dec(data.slice(160, 8));
		eth2destmac= new Hex(data.slice(176, 48));
		eth2sourcemac= new Hex(data.slice(224, 48));
		eth2frametypeh= new Hex(data.slice(272, 16));
		eth2frametype= new Dec(data.slice(272, 16));
		ethdestmac= new Hex(data.slice(176, 48));
		ethsourcemac= new Hex(data.slice(224, 48));
		ethsaps= new Hex(data.slice(288, 16));
		ethframetypeh= new Hex(data.slice(336, 16));
		ethframetype= new Dec(data.slice(336, 16));
		trndestmac= new Hex(data.slice(192, 48));
		trnsourcemac= new Hex(data.slice(240, 48));
		trnrouteinfo=
			new Hex(data.slice(288, Integer.parseInt(lanroutel.toString()) * 8));
		trnsaps=
			new Hex(data.slice((288 + Integer.parseInt(lanroutel.toString()) * 8), 16));
		trnframetypeh=
			new Hex(
				data.slice((288 + Integer.parseInt(lanroutel.toString()) * 8 + 48), 16));
		trnframetype=
			new Dec(
				data.slice((288 + Integer.parseInt(lanroutel.toString()) * 8 + 48), 16));
	}

	/**
	 * Returns the byte at which the rest of this packet's header starts.
	 * @return	The start of the data.
	 */
	public int getDataStart() {
		if ((llc.toString()).equals("0xFF")) {
			if (protocol.equals("E")) { // Tokenring     
				return ((Integer.parseInt(lanroutel.toString()) * 8) + 176);
			} else { // 802.3 Ethernet
				return 176;
			}
		} else { // Etvernet V2
			return 112;
		}
	}

	/**
	 * Returns the frame type of this packet. 
	 * @return	The frame type.
	 */
	public int getFrameType() {
		if ((llc.toString()).equals("0xFF")) {
			if (protocol.equals("E")) { // Tokenring		
				frametype= trnframetype;
			} else { // 802.3 Ethernet
				frametype= ethframetype;
			}
		} else { // Etvernet V2
			frametype= eth2frametype;
		}
		return Integer.parseInt(frametype.toString());
	}

	/**
	 * Returns the destination MAC address of this packet. 
	 * @return	String MAC address. 
	 */
	public String getMacAddress() {
		if ((llc.toString()).equals("0xFF")) {
			if (protocol.equals("E")) { // Tokenring		
				destmac= trndestmac;
			} else { // 802.3 Ethernet
				destmac= ethdestmac;
			}
		} else { // Etvernet V2
			destmac= eth2destmac;
		}
		return destmac.toString();
	}

	/**
	 * Returns a String with the tokenring routing data.
	 * @return	String routing data. 
	 */
	public String printRoutingData() {
		Object[] args= { trnrouteinfo.toString()};

		return Formatter.jsprintf("     Routing Info: {0,14,L} \n", args);
	}

	/**
	 * Returns a String representation of this lan header with source/destination mac addresses, frame format, and frametype.
     * @return	    Returns a string representation of this header.
	 */
	public String toString() {
		if ((llc.toString()).equals("0xFF")) {
			if (protocol.equals("E")) { // Tokenring		
				sourcemac= trnsourcemac;
				destmac= trndestmac;
				frametype= trnframetypeh;
				frameformat= ("LLC");
			} else { // 802.3 Ethernet
				sourcemac= ethsourcemac;
				destmac= ethdestmac;
				frametype= ethframetypeh;
				frameformat= ("802.3");
			}
		} else { // Etvernet V2
			sourcemac= eth2sourcemac;
			destmac= eth2destmac;
			frametype= eth2frametypeh;
			frameformat= ("ETHV2");
		}

		Object[] args=
			{ destmac.toString(), sourcemac.toString(), frameformat, frametype.toString()};

		return Formatter.jsprintf(
			"\t\t  {0,14,L}  {1,14,L}   {2,5,L}   Type: {3,2,l}\n",
			args);
	}

	/**
	 * Returns the lan routing length. 
	 * @return String containing a decimal representation of the lan routing length.
	 */
	public String getLanRouteLength() {
		return lanroutel.toString();
	}

	/**
	 * Returns the Ethernet2 destination mac of the Lan header.
	 * @return String containing a hexadecimal representation of the destination mac address.
	 */
	public String getEth2DestMacAddress() {
		return eth2destmac.toString();
	}

	/**
	 * Returns the Ethernet2 source mac of the Lan header.
	 * @return String containing a hexadecimal representation of the source mac address.
	 */
	public String getEth2SrcMacAddress() {
		return eth2sourcemac.toString();
	}

	/**
	 * Returns the Ethernet destination mac of the Lan header.
	 * @return String containing a hexadecimal representation of the destination mac address.
	 */
	public String getEthDestMacAddress() {
		return ethdestmac.toString();
	}

	/**
	 * Returns the Ethernet source mac of the Lan header.
	 * @return String containing a hexadecimal representation of the source mac address.
	 */
	public String getEthSrcMacAddress() {
		return ethsourcemac.toString();
	}

	/**
	 * Returns the Service Access Points of the Lan header.
	 * @return String containing a decimal representation of the Service Access Point.
	 */
	public String getEthSaps() {
		return (new Dec(ethsaps.getData())).toString();
	}

	/**
	 * Returns the frame type of the Lan header.
	 * @return String containing a decimal representation of the frame type.
	 */
	public String getEthFrameType() {
		return ethframetype.toString();
	}

	/**
	 * Returns the Tokenring destination mac of the Lan header.
	 * @return String containing a hexadecimal representation of the destination mac address.
	 */
	public String getTrnDestMacAddress() {
		return trndestmac.toString();
	}

	/**
	 * Returns the Tokenring source mac of the Lan header.
	 * @return String containing a hexadecimal representation of the source mac address.
	 */
	public String getTrnSrcMacAddress() {
		return trnsourcemac.toString();
	}

	/**
	 * Returns the Tokenring routing information of the Lan header.
	 * @return String containing a decimal representation of the routing information.
	 */
	public String getTrnRouteInfo() {
		return (new Dec(trnrouteinfo.getData())).toString();
	}

	/**
	 * Returns the Tokenring Service Access Points of the Lan header.
	 * @return String containing a decimal representation of the Service Access Points.
	 */
	public String getTrnSaps() {
		return (new Dec(trnsaps.getData())).toString();
	}

	/**
	 * Returns the Tokenring frame type information of the Lan header.
	 * @return String containing a decimal representation of the routing information.
	 */
	public String getTrnFrameType() {
		return trnframetype.toString();
	}
}
