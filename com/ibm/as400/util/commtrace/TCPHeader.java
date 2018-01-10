///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: TCPHeader.java
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
 * A TCP Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the TCP Header.
 */
public class TCPHeader extends Header {
	private Field sourceport= new Dec(rawheader.slice(0, 16));
	private Field destport= new Dec(rawheader.slice(16, 16));
	private Field sequence= new Dec(rawheader.slice(32, 32));
	private Field sequencehex= new Hex(rawheader.slice(32, 32));
	private Field acknum= new Dec(rawheader.slice(64, 32));
	private Field acknumhex= new Hex(rawheader.slice(64, 32));
	private Field dataoffset= new Dec(rawheader.slice(96, 4));
	private Field cwr= new Flag(rawheader.slice(104, 1), CWR, "");
	private Field ece= new Flag(rawheader.slice(105, 1), ECE, "");
	private Field urg= new Flag(rawheader.slice(106, 1), URG, "");
	private Field ack= new Flag(rawheader.slice(107, 1), ACK, "");
	private Field psh= new Flag(rawheader.slice(108, 1), PSH, "");
	private Field rst= new Flag(rawheader.slice(109, 1), RST, "");
	private Field syn= new Flag(rawheader.slice(110, 1), SYN, "");
	private Field fin= new Flag(rawheader.slice(111, 1), FIN, "");
	private Field window= new Dec(rawheader.slice(112, 16));
	private Field checksum= new Hex(rawheader.slice(128, 16));
	private Field urgentptr= new Dec(rawheader.slice(144, 16));
	private Field options=
		new Hex(
			rawheader.slice(160, ((Integer.parseInt(dataoffset.toString()) * 8) - 40)));
	private Field option= new Dec(rawheader.slice(160, 8));
	private Field msssize= new Dec(rawheader.slice(168, 8));
	private Field segmentsize=
		new Dec(rawheader.slice(176, ((Integer.parseInt(msssize.toString()) * 8) / 2)));
	
	private final static String CLASS="TCPHeader";
	private final static String CWR= "CWR ";
	private final static String ECE= "ECE ";
	private final static String URG= "URG ";
	private final static String ACK= "ACK ";
	private final static String PSH= "PSH ";
	private final static String RST= "RST ";
	private final static String SYN= "SYN ";
	private final static String FIN= "FIN ";
	private final static String UNASSIGNED= "Unassigned";
	private final static String MSS= "MSS=";
	private final static String NOOP= "NO OP";
	private final static String ENDOP= "END OP";
	private final static String BADOP= "BAD OP";
	private final static String NONE= "NONE";
	private final static String TCPSTR= "TCP";
	private final static String SRC= "SrcPort";
	private final static String DST= "DstPort";
	private final static String SEQ= "SeqNum";
	private final static String ACKNUM= "AckNum";
	private final static String FLAGS= "Flags";
	private final static String DATAOFF= "DataOffset";
	private final static String CHKSUM= "Checksum";
	private final static String WINSIZE= "Window Size";
	private final static String TCPOPT= "TCP Options";

	/**
	 * Creates and parses the data of this header  
	 * @param data  The raw data of this header 
	 */
	TCPHeader(BitBuf data) {
		super(data);

		type= TCP;
	}

	/**
	 * Returns the length of this header.
	 * @return The length of this header.
	 */
	public int getHeaderLen() {
		return rawheader.slice(96, 4).toInt() * 32;
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		
		String portname= (String) Port.get(this.sourceport.toString());
		String portname2= (String) Port.get(this.destport.toString());

		// Make sure we have enough data to parse a full header
		if (rawheader.getBitSize() < getHeaderLen()) {
			return (new Data(rawheader)).toString();
		}

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
					Trace.log(Trace.INFORMATION,CLASS + ".toString()" + "Not printing record");
				}
				return ""; // Return empty record because it didn't pass the filter
			}
		}

		// If we couldn't find the port name, set it to Unassigned
		if (portname == null) {
			portname= UNASSIGNED;
		}
		if (portname2 == null) {
			portname2= UNASSIGNED;
		}
		String sourceport= this.sourceport.toString() + ", " + portname;
		String destport= this.destport.toString() + ", " + portname2;

		Object[] args=
			{
				sourceport,
				destport,
				acknum,
				acknumhex,
				sequence,
				sequencehex,
				dataoffset,
				cwr,
				ece,
				urg,
				ack,
				psh,
				rst,
				syn,
				fin,
				window,
				checksum,
				options,
				segmentsize };

		if (Integer.parseInt(dataoffset.toString()) > 0x05) {
			switch (Integer.parseInt(option.toString())) {
				case 2 :
					args[17]= MSS;
					break;
				case 1 :
					args[18]= "";
					args[17]= NOOP;
					break;
				case 0 :
					args[18]= "";
					args[17]= ENDOP;
					break;
				default :
					args[18]= "";
					args[17]= BADOP;
					break;
			}
		} else {
			args[18]= "";
			args[17]= NONE;
			// 2 = MSS, 1 = NO OP, 0 = END OP
		}

		return Formatter.jsprintf(
			"\t    "
				+ TCPSTR
				+ "  . . . . :  "
				+ SRC
				+ ":  {0,18,L} "
				+ DST
				+ ":  {1,18,L}\n"
				+ "\t\t\t    "
				+ SEQ
				+ ":  {4,10,L} "
				+ ACKNUM
				+ ":  {2,10,L} {3,10,L} {5,10,L} "
				+ FLAGS
				+ ":  {7}{8}{9}{10}{11}{12}{13}{14}\n"
				+ "\t\t\t    "
				+ DATAOFF
				+ ":  {7,2,L} "
				+ CHKSUM
				+ ":  {16} "
				+ WINSIZE
				+ ":  {15,5,L} "
				+ TCPOPT
				+ ":  {17,5,L}  {18,6,L}\n",
			args)
			+ printHexHeader()
			+ printnext(filter)
			+ (new Data(rawpayload)).toString();
	}

	/**
	 * Returns the source port for this TCP header. 
	 * @return String containing a decimal representation of the source port. 
	 */
	public String getSrcPort() {
		return sourceport.toString();
	}

	/**
	 * Returns the destination port for this TCP header. 
	 * @return String containing a decimal representation of the destination port. 
	 */
	public String getDstPort() {
		return destport.toString();
	}

	/**
	 * Returns the sequence for this TCP header.
	 * @return String containing a decimal representation of the sequence.
	 */
	public String getSequence() {
		return sequence.toString();
	}

	/**
	 * Returns the ack number for this TCP header. 
	 * @return String containing a decimal representation of the ack number. 
	 */
	public String getAckNum() {
		return acknum.toString();
	}

	/**
	 * Returns the data offset for this TCP header. 
	 * @return String containing a decimal representation of the data offset. 
	 */
	public String getDataOffset() {
		return dataoffset.toString();
	}

	/**
	 * Returns the cwr Flag for this TCP header. 
	 * @return String if the field is a 1 this string will contain "CWR " otherwise it will be an empty string.
	 */
	public String getCWRFlag() {
		return cwr.toString();
	}

	/**
	 * Returns the ece Flag for this TCP header. 
	 * @return String if the field is a 1 this string will contain "ECE " otherwise it will be an empty string.
	 */
	public String getECEFlag() {
		return ece.toString();
	}

	/**
	 * Returns the urg Flag for this TCP header. 
	 * @return String if the field is a 1 this string will contain "URG " otherwise it will be an empty string.
	 */
	public String getURGFlag() {
		return urg.toString();
	}

	/**
	 * Returns the ack Flag for this TCP header. 
	 * @return String if the field is a 1 this string will contain "ACK " otherwise it will be an empty string.
	 */
	public String getACKFlag() {
		return ack.toString();
	}

	/**
	 * Returns the psh Flag for this TCP header.
	 * @return String if the field is a 1 this string will contain "PSH " otherwise it will be an empty string.
	 */
	public String getPSHFlag() {
		return psh.toString();
	}

	/**
	 * Returns the rst Flag for this TCP header. 
	 * @return String if the field is a 1 this string will contain "RST " otherwise it will be an empty string.
	 */
	public String getRSTFlag() {
		return rst.toString();
	}

	/**
	 * Returns the syn Flag for this TCP header.
	 * @return String if the field is a 1 this string will contain "SYN " otherwise it will be an empty string.
	 */
	public String getSYNFlag() {
		return syn.toString();
	}

	/**
	 * Returns the fin Flag for this TCP header.
	 * @return String if the field is a 1 this string will contain "FIN " otherwise it will be an empty string.
	 */
	public String getFINFlag() {
		return fin.toString();
	}

	/**
	 * Returns the window for this TCP header. 
	 * @return String containing a decimal representation of the window.
	 */
	public String getWindow() {
		return window.toString();
	}

	/**
	 * Returns the checksum for this TCP header. 
	 * @return String containing a decimal representation of the checksum.
	 */
	public String getCheckSum() {
		return (new Dec(checksum.getData())).toString();
	}

	/**
	 * Returns the urgent pointer for this TCP header. 
	 * @return String containing a decimal representation of the urgent pointer. 
	 */
	public String getUrgentPointer() {
		return urgentptr.toString();
	}

	/**
	 * Returns the options for this TCP header. 
	 * @return String containing a decimal representation of the options. 
	 */
	public String getOptions() {
		return (new Dec(options.getData())).toString();
	}

	/**
	 * Returns the first 8 bits of the options for this TCP header. 
	 * @return String containing a decimal representation of the options. 
	 */
	public String getOption() {
		return options.toString();
	}

	/**
	 * Returns the Maximum Segment Size option for this TCP header. 
	 * @return String containing a decimal representation of the Maximum Segment Size option. 
	 */
	public String getMaximumSegmentSize() {
		return msssize.toString();
	}

	/**
	 * Returns the Segment Size option for this TCP header. 
	 * @return String containing a decimal representation of the Segment Size option. 
	 */
	public String getSegmentSize() {
		return segmentsize.toString();
	}
}
