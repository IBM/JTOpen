///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IP6Address.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents an IPv6 Address.
 */
class IP6Address extends Field {
	/**
	 * Base constructor which creates a IPv6 Address.  
	 * @param data BitBuf which contains this raw IP address. 
	 */
	public IP6Address(BitBuf data) {
		super(new BitBuf(data, 0, 128));
	}

	/**
	 * Creates a String representation of this IPv6 Address.
	 * in presentation format with :'s as delimeters.   
	 * @return  String representing this IPv6 Address. 
	 */
	public String toString() {
		String IP6Addr= data.toHexString(2, ":");
		return (new IPAddressConversion(IP6Addr, IPAddressConversion.IPv6)).ntop();
	}
}