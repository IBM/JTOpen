///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Prolog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.ResourceBundle;
import java.util.Properties;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Parses the 256 byte header of an iSeries trace and allows for printing and 
 * access to the fields of the Prolog<br>
 */
public class Prolog {
    private BitBuf data; // The raw data
    private boolean invalidData = false; // If we are passed invalid data set this flag
    private String IPaddr = null,IPaddr2 = null, fmtBroadcast = "Y"; // The user specified filter options
    private String date = null;
	private static String ALL_="*ALL",
							NO_="*NO";

    private Field ctsftrcd,/* Trace description            */
				    ctsfndob,	    /* ND object name               */
				    ctsfotyp,	    /* Object type and subtype      */
		                            /* (OBJTYPE)                    */
				                    /* '1' - Line description       */
		                            /* '2' - Network interface      */
				                    /* '3' - Network Server         */
				    ctsfpro,	    /* Protocol                    */
		                            /* 'A' - ASYNC                  */
		                            /* 'B' - BSC                    */
		                            /* 'C' - ETHERNET               */
		                            /* 'D' - DDI                    */
		                            /* 'E' - ECL(TOKEN-RING)        */
		                            /* 'E' - ECL(TOKEN-RING)        */ 
		                            /* 'F' - FRAME RELAY            */ 
		                            /* 'H' - HDLC(X.25)             */ 
		                            /* 'I' - ISDN                   */ 
		                            /* 'Q' - IDLC                   */ 
		                            /* 'S' - SDLC                   */ 
       /*                               STRCMNTRC variables          */ 
				    ctsfbuff,	    /* Buffer size in bytes         */ 
				    ctsfdatd,	    /* Data direction 1=Sent,       */ 
								    /* 2=received, 3=both           */ 
				    ctsfstpf,	    /* Stop on buffer full Y/N      */ 
				    ctsfstrb,	    /* Beginning bytes              */ 
				    ctsfendb,	    /* Ending bytes                 */ 
				    ctsfctln,	    /* Controller name              */ 
				    ctsfusrd,	    /* Trace all user data          */ 
		                            /* 'Y' - yes                    */ 
		                            /* 'N' - no                     */ 
		                            /* Trace filter options on strt */ 
				    ctsffilt,	    /* LAN filter option            */ 
		                            /* '0' - All data (no filtering)*/ 
		                            /*   Filter by :                */ 
		                            /* '1' - remote controller      */ 
		                            /* '2' - remote MAC address     */ 
		                            /* '3' - remote SAP             */ 
		                            /* '4' - local SAP              */ 
		                            /* '5' - IP protocol number     */ 
		                            /* '6' - remote IP address      */ 
				    ctsfrctl,	    /* Remote controller name       */ 
				    ctsfrmac,	    /* Remote MAC address           */ 
				    ctsfrsap,	    /* Remote SAP address           */ 
				    ctsflsap,	    /* Local SAP address            */ 
				    ctsfipv6,	    /* Address filtering for IPv6   */ 
								    /* on the start in case the IOP */ 
								    /* adds support later           */ 
				    ctsfipv4,	    /* IP address IpV4              */ 
				    ctsfipid,	    /* IP protocol #                */ 
				    ctsflmid,	    /* Trace LMI data               */ 
		                            /* '0' - Normal trace           */ 
		                            /* '6' - Exclude LMI            */ 
		                            /* '3' - LMI only               */ 
       /*                               ENDCMNTRC variables          */ 
				    ctsfsts,	    /* Status of trace              */
								    /* (STATUS)                     */
		                            /* 'STOP' - Stopped             */
		                            /* 'ERR ' - Error               */
					ctsfsttm,	    /* Start time of trace in       */
		                            /* 8-byte TOD clock value       */
				    ctsfsptm,	    /* Stop time of trace in        */
		                            /* 8-byte TOD clock value       */
				    ctsfbyts,	    /* Bytes collected              */
				    ctsfrecs,	    /* # records in the comm trace  */
				    ctsfmxpt,	    /* Max frame allowed by protocol*/
				    ctsfmxdf,	    /* Max frame allowed by user    */
				    ctsfchls,	    /* # x.25 logical channels      */
				    ctsfbwrp,	    /* Buffer wrapped Y/N           */
				    ctsfrect,	    /* Record timer length 2/4 bytes*/
				    ctsfdtyp;

	// Constants for the format Prolog 	
	private String PTITLE,
		PTRACDES,
		PCFGOBJ,
		POTYPE,
		POTYPEH,
		POBJPROT,
		PSTRTIME,
		PENDTIME,
		PBYTECOL,
		PBUFSIZ,
		PBUFSIZH,
		PDATDIR,
		PDATDIRH,
		PBUFWRP,
		PBUFWRPH,
		PBYTES,
		PBYTESB,
		PBYTESBH,
		PBYTESE,
		PBYTESEH,
		PFORMAT,
		PCDNAME,
		PCDNAMEH,
		PDATTYP,
		PDATTYPH,
		PFMTTCP,
		PIPADDR,
		PIPADDRH,
		PIPPORT,
		PIPPORTH,
		PETHDAT,
		PETHDATH,
		PBDCAST,
		PFMTOH,
		PRMTCD,
		PRMTMAC,
		PRMTSAP,
		PLCLSAP,
		PPIPID,
		PRMTIP,
		PRMTALLH;

	private final static String ETH = "ETHERNET";
	private final static String TOKEN = "TOKENRING";
	private final static String CALC = "*CALC";
	
    /**
     * Creates a prolog. Which parses the 256 bytes of raw data.<br>
     * @param data      BitBuf which contains the raw prolog data.    
     * @param IPaddr    the IP address to be filtered on.
     * @param IPaddr2   the IP address to be filtered on.             
     * @param fmtBroadcast boolean specifying wether or not to format broadcast data.
     */
    Prolog(BitBuf data,FormatProperties filter) {
		this.data = data;
		String IPaddr = filter.getIPAddress();
		if(IPaddr==null) {
		    this.IPaddr = ALL_;
		} else {
		    this.IPaddr = IPaddr;
		}
		String IPaddr2 = filter.getSecondIPAddress();
		if(IPaddr2==null) {
		    this.IPaddr2=ALL_;
		} else {
		    this.IPaddr2 = IPaddr2;
		}
		String fmtBroadcast = filter.getBroadcast();
		if(fmtBroadcast==null) {
			this.fmtBroadcast = NO_;
		} else {
			this.fmtBroadcast = fmtBroadcast;
		}
		
		parseData();
    }

    /**
     * Parses the data so we can easily access variables in the prolog.
     */
    private void parseData() {
		// Get the MRI for the prolog
		PTITLE=ResourceBundleLoader_ct.getText("PTITLE");           
		PTRACDES=ResourceBundleLoader_ct.getText("PTRACDES");
		PCFGOBJ=ResourceBundleLoader_ct.getText("PCFGOBJ"); 
		POTYPE=ResourceBundleLoader_ct.getText("POTYPE"); 
		POTYPEH=ResourceBundleLoader_ct.getText("POTYPEH");
		POBJPROT=ResourceBundleLoader_ct.getText("POBJPROT");
		PSTRTIME=ResourceBundleLoader_ct.getText("PSTRTIME");
		PENDTIME=ResourceBundleLoader_ct.getText("PENDTIME");
		PBYTECOL=ResourceBundleLoader_ct.getText("PBYTECOL");
		PBUFSIZ=ResourceBundleLoader_ct.getText("PBUFSIZ"); 
		PBUFSIZH=ResourceBundleLoader_ct.getText("PBUFSIZH");                       
		PDATDIR=ResourceBundleLoader_ct.getText("PDATDIR"); 
		PDATDIRH=ResourceBundleLoader_ct.getText("PDATDIRH");    
		PBUFWRP=ResourceBundleLoader_ct.getText("PBUFWRP");
		PBUFWRPH=ResourceBundleLoader_ct.getText("PBUFWRPH");                 
		PBYTES=ResourceBundleLoader_ct.getText("PBYTES");      
		PBYTESB=ResourceBundleLoader_ct.getText("PBYTESB");  
		PBYTESBH=ResourceBundleLoader_ct.getText("PBYTESBH");                 
		PBYTESE=ResourceBundleLoader_ct.getText("PBYTESE");  
		PBYTESEH=ResourceBundleLoader_ct.getText("PBYTESEH");                 
		PFORMAT=ResourceBundleLoader_ct.getText("PFORMAT");               
		PCDNAME=ResourceBundleLoader_ct.getText("PCDNAME");
		PCDNAMEH=ResourceBundleLoader_ct.getText("PCDNAMEH");                   
		PDATTYP=ResourceBundleLoader_ct.getText("PDATTYP");
		PDATTYPH=ResourceBundleLoader_ct.getText("PDATTYPH");   
		PFMTTCP=ResourceBundleLoader_ct.getText("PFMTTCP");
		PIPADDR=ResourceBundleLoader_ct.getText("PIPADDR");  
		PIPADDRH=ResourceBundleLoader_ct.getText("PIPADDRH");              
		PIPPORT=ResourceBundleLoader_ct.getText("PIPPORT"); 
		PIPPORTH=ResourceBundleLoader_ct.getText("PIPPORTH");              
		PETHDAT=ResourceBundleLoader_ct.getText("PETHDAT");
		PETHDATH=ResourceBundleLoader_ct.getText("PETHDATH");     
		PBDCAST=ResourceBundleLoader_ct.getText("PBDCAST");
		PFMTOH=ResourceBundleLoader_ct.getText("PFMTOH");                  
		PRMTCD=ResourceBundleLoader_ct.getText("PRMTCD"); 
		PRMTMAC=ResourceBundleLoader_ct.getText("PRMTMAC");
		PRMTSAP=ResourceBundleLoader_ct.getText("PRMTSAP");
		PLCLSAP=ResourceBundleLoader_ct.getText("PLCLSAP");
		PPIPID=ResourceBundleLoader_ct.getText("PPIPID"); 
		PRMTIP=ResourceBundleLoader_ct.getText("PRMTIP"); 
		PRMTALLH=ResourceBundleLoader_ct.getText("PRMTALLH"); 
		// Parse 256 bytes of header data.
		ctsftrcd = new Char(data.slice(0,160));
		ctsfndob = new Char(data.slice(160,80));
		ctsfotyp = new Char(data.slice(240,8));
		ctsfpro = new Char(data.slice(248,8));
		// 28 Bytes not used
		ctsfbuff = new Dec(data.slice(480,32));
		ctsfdatd = new Char(data.slice(512,8));
		ctsfstpf = new Char(data.slice(520,8));
		ctsfstrb = new Dec(data.slice(528,16));
		ctsfendb = new Dec(data.slice(544,16));
		ctsfctln = new Char(data.slice(560,80));
		ctsfusrd = new Char(data.slice(640,8));
		ctsffilt = new Char(data.slice(648,8));
		ctsfrctl = new Char(data.slice(656,80));
		ctsfrmac = new Char(data.slice(736,48));
		ctsfrsap = new Char(data.slice(784,16));
		ctsflsap = new Char(data.slice(800,16));
		// 2 Bytes not used
		ctsfipv6 = new Dec(data.slice(832,16));
		ctsfipv4 = new Dec(data.slice(960,32));
		ctsfipid = new Dec(data.slice(992,16));
		ctsflmid = new Char(data.slice(1008,8));
		// 59 Bytes not used
		ctsfsts = new Char(data.slice(1488,32));
		ctsfsttm = new Dec(data.slice(1520,64));
		ctsfsptm = new Dec(data.slice(1584,64));
		// 2 Bytes not used
		ctsfbyts = new Dec(data.slice(1664,32));
		ctsfrecs = new Dec(data.slice(1696,32));
		ctsfmxpt = new Dec(data.slice(1728,16));
		ctsfmxdf = new Dec(data.slice(1744,16));
		ctsfchls = new Dec(data.slice(1760,16));
		ctsfbwrp = new Char(data.slice(1776,8));
		ctsfrect = new Char(data.slice(1784,8));
		ctsfdtyp = new Char(data.slice(1792,8));
		// 30 Bytes not used
	}

    /**
     * return the number of records in this communication trace. 
     * @return The number of records
     */
    public int getNumRecs() {
		return Integer.parseInt(ctsfrecs.toString());
    }
    
    /**
     * return the title of this communication trace 
     * @return The title
     */
    public String getTitle() {
    	return PTITLE;
    }
    
    /**
     * return the current date in the Date.toString() format. 
     * @return		String 
     */
    public String getDate() {
    	return date;
    }
    
    /**
     * returns if there was an error parsing the data provided 
     * @return	true if this file contains invalid data, else returns false
     */
    public boolean invalidData() {
    	return invalidData;
    }

    /**
     * Returns the number of bytes collected during this communication trace . 
     * @return Number of bytes collected 
     */
    public int getNumBytes() {
		return Integer.parseInt(ctsfbyts.toString());
    }

    /**
     * Returns a printable representation of this Prolog.
     * @return	    Returns a string representation of this Prolog.
     */
    public String toString() {
	    StringBuffer formatedData = new StringBuffer(256000); // The formatted data
		String ctsfpros = ctsfpro.toString(); // The line protocol
		String ctsfprofs;
		if(ctsfpros.equals("E") || ctsfpros.equals("C")) { // Tokenring or Ethernet? 
		    formatedData.append(PTITLE + "\n");
	
		    Calendar cal = new GregorianCalendar(); // Get the current time
		    cal.setTime((new Date(System.currentTimeMillis())));
		    date =(cal.getTime()).toString() + "\n"; 
		    formatedData.append(date); // Append the current time to the file header
	
		    if(ctsfpros.equals("C")) {
				ctsfprofs = (ETH);
		    } else {
				ctsfprofs = (TOKEN);
			}
		    Object[] args =
		    {
				ctsftrcd,
				ctsfndob,
				ctsfotyp,
				ctsfprofs};

			formatedData.append(Formatter.jsprintf(
				PTRACDES + "    {0,20,L}\n" +
				PCFGOBJ + "    {1,10,L}\n" +
				POTYPE + "    {2,13,L}" + POTYPEH + "\n" +
				POBJPROT + "    {3,13,L}\n",args));

		    formatedData.append(PSTRTIME);

		    Time start = new Time(Long.parseLong((ctsfsttm.toString()))); // Convert the timestamp into a readable date
		    formatedData.append("    " + start.toString() + "\n");

		    formatedData.append(PENDTIME);

		    Time end = new Time(Long.parseLong((ctsfsptm.toString())));
		    formatedData.append("    " + end.toString() + "\n");
	  
		    String ctsfstrbs = ctsfstrb.toString(),
				   ctsfendbs = ctsfendb.toString();
		    if(ctsfstrbs.equals("0") || ctsfstrbs.equals("100")) {
				ctsfstrbs=CALC;
		    }
		    if(ctsfendbs.equals("0")) {
				ctsfendbs=CALC;
			}

		    Object [] args2 = {
				ctsfbyts,
				ctsfbuff,
				ctsfdatd,
				ctsfstpf,
				ctsfstrbs, 
				ctsfendbs,
				CALC,
				"Y",
				IPaddr,
				IPaddr2,
				ALL_,
				fmtBroadcast};

		    formatedData.append(Formatter.jsprintf(
				PBYTECOL + "    {0,13,L}\n" +
				PBUFSIZ + "    {1,13,L}" + PBUFSIZH + "\n" +
				PDATDIR + "    {2,13,L}" + PDATDIRH + "\n" +
				PBUFWRP + "    {3,13,L}" + PBUFWRPH + "\n" +
				PBYTES + "\n" +
				PBYTESB + "    {4,13,L}" + PBYTESBH + "\n" +
				PBYTESE + "    {5,13,L}" + PBYTESEH + "\n" +
				PFORMAT + "\n" +
				PDATTYP + "    {6,13,L}" + PDATTYPH + "\n" +
				PFMTTCP + "    {7,13,L}" + PFMTOH + "\n" + 
				PIPADDR + "    {8,13,L}" + PIPADDRH + "\n" +
				PIPADDR + "    {9,13,L}" + PIPADDRH + "\n" +
				PIPPORT + "    {10,13,L}" + PIPADDRH + "\n" +
				PBDCAST + "    {11,13,L}" + PFMTOH + "\n",args2));
		} else {
		    formatedData.append(ResourceBundleLoader_ct.getText("NotSupported"));
		    invalidData = true;
		}
		return formatedData.toString();
    }

    /**
     * Returns the trace description. 
     * @return String containing the trace description. 
     */
    public String getTraceDescription() {
		return ctsftrcd.toString();
    }

    /**
     * Returns the ND object name. 
     * @return String containing ND object name. 
     */
    public String getNDObject() {
		return ctsfndob.toString();
    }

    /**
     * Returns the Object type and subtype:<br>
     * '1' - Line description<br> 
     * '2' - Network interface<br> 
     * '3' - Network Server<br> 
     * @return String containing a the Object type and subtype. 
     */
    public String getObjectType() {
		return ctsfotyp.toString();
    }

    /**
     * Returns the protocol:<br>
     * 'A' - ASYNC<br> 
     * 'B' - BSC   <br> 
     * 'C' - ETHERNET<br> 
     * 'D' - DDI<br>            
     * 'E' - ECL(TOKEN-RING)<br>
     * 'E' - ECL(TOKEN-RING)<br> 
     * 'F' - FRAME RELAY<br>
     * 'H' - HDLC(X.25)<br>
     * 'I' - ISDN<br>
     * 'Q' - IDLC<br>
     * 'S' - SDLC<br>
     * @return String containing the protocol. 
     */
    public String getProtocol() {
		return ctsfpro.toString();
    }

    /**
     * Returns the buffer size in bytes. 
     * @return String containing buffer size in bytes. 
     */
    public String getBuffSize() {
		return ctsfbuff.toString();
    }

    /**
     * Returns the data direction:<br>
     * 1=Sent<br>
     * 2=received<br>
     * 3=both<br>
     * @return String containing the data direction.
     */
    public String getDataDirection() {
		return ctsfdatd.toString();
    }

    /**
     * returns Stop on Buffer full. 
     * @return String containing the code(Y/N). 
     */
    public String getStopFull() {
		return ctsfstpf.toString();
    }

    /**
     * Returns the beginning bytes. 
     * @return String containing the beginning bytes.
     */
    public String getStartBytes() {
		return ctsfstrb.toString();
    }

    /**
     * Returns the ending bytes.
     * @return String containing the ending bytes. 
     */
    public String getEndingBytes() {
		return ctsfendb.toString();
    }

    /**
     * Returns the Controller name.
     * @return String containing the controller name. 
     */
    public String getControllerName() {
		return ctsfctln.toString();
    }

    /**
     * Returns if traced all user data.
     * @return String containing the code(Y/N).
     */
    public String getUserData() {
		return ctsfusrd.toString();
    }

    /**
     * Returns the Lan Filter option.<br>
     * Filter by :<br>   
     * '0' - All data (no filtering)<br>
     * '1' - remote controller<br>  
     * '2' - remote MAC address<br>
     * '3' - remote SAP<br>
     * '4' - local SAP<br>
     * '5' - IP protocol number<br>
     * '6' - remote IP address<br>
     * @return String containing the code.
     */
    public String getLANFilter() {
		return ctsffilt.toString();
    }

    /**
     * Returns the remote controller name.
     * @return String containing the remote controller name.
     */
    public String getRmtContName() {
		return ctsfrmac.toString();
    }
    /**
     * Returns remote MAC address.
     * @return String containing the remote MAC address.
     */
    public String getRmtMacAddr() {
		return ctsfrmac.toString();
    }

    /**
     * Returns remote SAP address.
     * @return String containing the remote SAP address.
     */
    public String getRmtSAP() {
		return ctsfrsap.toString();
    }

    /**
     * Returns local SAP address.
     * @return String containing the local SAP address.
     */
    public String getLocalSAP() {
		return ctsflsap.toString();
    }

    /**
     * Returns address filtering for IPv6.
     * @return String containing the code.
     */
    public String getFilterIPv6() {
		return ctsfipv6.toString();
    }

    /**
     * Returns IP address IPv4.
     * @return String containing the address.
     */
    public String getIPv4() {
		return ctsfipv4.toString();
    }

    /**
     * Returns IP protocol number.
     * @return String containing ip protocol number.
     */
    public String getIPID() {
		return ctsfipid.toString();
    }

    /**
     * Returns trace LMI data"<br>
     * '0' - Normal trace<br>
     * '6' - Exclude LMI<br>
     * '3' - LMI only<br>
     * @return String containing the code.
     */
    public String getLMIData() {
		return ctsflmid.toString();
    }

    /**
     * Returns status of trace:<br>
     * 'STOP' - Stopped<br>
     * 'ERR ' - Error <br>
     * @return String containing the status.
     */
    public String getStatus() {
		return ctsfsts.toString();
    }

    /**
     * Returns Start time of trace in 8-byte TOD clock value.
     * @return String containing the start time.
     */
    public String getStartTime() {
		return ctsfsttm.toString();
    }

    /**
     * Returns stop time of trace in 8-byte TOD clock value.
     * @return String containing the stop time.
     */
    public String getStopTime() {
		return ctsfsptm.toString();
    }

    /**
     * Returns bytes collected.
     * @return String containing the number of bytes collected.
     */
    public String getBytesCollected() {
		return ctsfbyts.toString();
    }

    /**
     * Returns max frame allowed by protocol.
     * @return String containing the max frame.
     */
    public String getMaxFrameProtocol() {
		return ctsfmxpt.toString();
    }

    /**
     * Returns max frame allowed by user.
     * @return String containing the max frame.
     */
    public String getMaxFrameUser() {
		return ctsfmxdf.toString();
    }

    /**
     * Returns number of x.25 logical channels.
     * @return String containing the code.
     */
    public String getChannels() {
		return ctsfchls.toString();
    }

    /**
     * Returns if buffer wrapped Y/N.
     * @return String containing the code.
     */
    public String getBufferWrap() {
		return ctsfbwrp.toString();
    }

    /**
     * Returns record timer length 2/4 bytes.
     * @return String containing the length.
     */
    public String getRecTimer() {
		return ctsfmxpt.toString();
    }

    /**
     * Returns the type.
     * @return String containing the type.
     */
    public String getType() {
		return ctsfdtyp.toString();
    }
}
