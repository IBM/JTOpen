///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IP4Address.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents an IPv4 Address.
 */
class IP4Address extends Field {
	/**
	 * Base constructor which creates an IPv4 Address. 
	 * @param data         BitBuf which represents this raw IP address. 
	 */
	public IP4Address(BitBuf data) {
		super(new BitBuf(data, 0, 32));
	}
	/**
	 * creates a String representation of this IP Address.
	 * in presentation format with .'s as delimeters.
	 * @return  String representing this IPv4 Address. 
	 */
	public String toString() {
		String IP4Addr= data.toHexString(1, ".");
		return (new IPAddressConversion(IP4Addr, IPAddressConversion.IPv4Hex)).ntop();
	}
}