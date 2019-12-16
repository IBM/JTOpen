///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Port.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Hashtable;

/**
 * Takes in a port number and returns a description of the port.
 */
class Port {
    private static Hashtable ports = new Hashtable();

	/**
	 * Initializes the port table. 
	 */	
    public static void initialize() {
	    // Add port number and name to the hash table
	    ports.put("1","tcpmux");
	    ports.put("5","RJE");
	    ports.put("7","echo");
	    ports.put("9","discard");
	    ports.put("11","systat");
	    ports.put("13","daytime");
	    ports.put("17","qotd");
	    ports.put("18","MSP");
	    ports.put("20","ftp-data");
	    ports.put("21","ftp");
	    ports.put("22","ssh");
	    ports.put("23","telnet");
	    ports.put("25","smtp");
	    ports.put("37","time");
	    ports.put("39","rlp");
	    ports.put("42","nameserver");
	    ports.put("43","nicname");
	    ports.put("49","login");
	    ports.put("50","re-mail-ck");
	    ports.put("52","xns-time");
	    ports.put("53","domain");
	    ports.put("63","whois++");
	    ports.put("67","bootps");
	    ports.put("68","bootpc");
	    ports.put("69","tftp");
	    ports.put("70","gopher");
	    ports.put("79","finger");
	    ports.put("80","http");
	    ports.put("82","xfer");
	    ports.put("88","kerberbos");
	    ports.put("89","su-mit-tg");
	    ports.put("90","dnsix");
	    ports.put("92","npp");
	    ports.put("93","dcp");
	    ports.put("95","supdup");
	    ports.put("101","hostname");
	    ports.put("102","iso-tsap");
	    ports.put("107","rtelnet");
	    ports.put("108","snagas");
	    ports.put("109","pop2");
	    ports.put("110","pop3");
	    ports.put("111","sunrpc");
	    ports.put("113","ident");
	    ports.put("115","sftp");
	    ports.put("117","uucp-path");
	    ports.put("118","sqlserv");
	    ports.put("119","nntp");
	    ports.put("123","ntp");
	    ports.put("130","cisco-fna");
	    ports.put("131","cisco-tna");
	    ports.put("132","cisco-sys");
	    ports.put("135","loc-srv");
	    ports.put("137","netbios-ns");
	    ports.put("138","netbios-dgm");
	    ports.put("139","netbios-ssn");
	    ports.put("143","imap");
	    ports.put("144","news");
	    ports.put("146","iso-ipo");
	    ports.put("147","iso-ip");
	    ports.put("144","news");
	    ports.put("150","sql-net");
	    ports.put("152","bftp");
	    ports.put("153","sgmp");
	    ports.put("156","sqlsrv");
	    ports.put("158","pcmail-srv");
	    ports.put("159","nss-routing");
	    ports.put("160","sgmp-traps");
	    ports.put("161","snmp");
	    ports.put("162","snmptrap");
	    ports.put("163","cmip-man");
	    ports.put("164","cmip-agent");
	    ports.put("165","xns-courier");
	    ports.put("167","namp");
	    ports.put("179","bgp");
	    ports.put("197","dls");
	    ports.put("198","dls-mon");
	    ports.put("200","src");
	    ports.put("201","at-rtmp");
	    ports.put("202","at-nbp"); 
	    ports.put("204","at-echo");
	    ports.put("206","at-zis");
	    ports.put("209","qmtp");
	    ports.put("213","ipx");
	    ports.put("220","imap3");
	    ports.put("246","dsp3270");
	    ports.put("385","ibm-app");
	    ports.put("397","appc/tcp");
	    ports.put("414","infoseek");
	    ports.put("415","bnet");
	    ports.put("423","opc-job-start");
	    ports.put("424","opc-job-track");
	    ports.put("443","https");
	    ports.put("512","exec");
	    ports.put("513","login");
	    ports.put("514","cmd");
	    ports.put("515","printer");
	    ports.put("517","talk");
	    ports.put("518","ntalk");
	    ports.put("519","utime");
	    ports.put("520","efs");
	    ports.put("525","time");
	    ports.put("526","tempo");
	    ports.put("530","courier");
	    ports.put("531","conference");
	    ports.put("532","netnews");
	    ports.put("533","netwall");
	    ports.put("540","uucp");
	    ports.put("543","klogin");
	    ports.put("544","krcmd");
	    ports.put("550","new-rwho");
	    ports.put("565","whoami");
	    ports.put("574","ftp-agent");
	    ports.put("600","ipcserver");
	    ports.put("749","kerberos-adm");
	    ports.put("754","tell");
	    ports.put("758","nlogin");
	    ports.put("765","webster");
	    ports.put("767","phonebook");
	    ports.put("989","ftps-data");
	    ports.put("990","ftps");
	    ports.put("991","nas");
	    ports.put("992","telnets");
	    ports.put("993","imaps");
	    ports.put("994","ircs");
	    ports.put("995","pop3s");
    }

    /**
     * Given a port number returns a text description of that port number.
     * @param i	    this port number as an int.
     */
    public static Object get(int i) {
	   	if(ports.isEmpty()) { initialize();}
	    return ports.get((new Integer(i)).toString());
    }
   
    /**
     * Given a port number returns a text description of that port number.
     * @param i	    this port number as a String.
     */
    public static Object get(String i) {
    	if(ports.isEmpty()) { initialize();}
    	return ports.get(i);
    }
}
