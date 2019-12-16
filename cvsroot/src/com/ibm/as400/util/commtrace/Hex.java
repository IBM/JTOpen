///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Hex.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents a hexadecimal field.
 */
class Hex extends Field {
	/**
	 * Base constructor which creates a default hex field.  
	 * @param b         BitBuf which represents this hex field.      
	 */
	public Hex(BitBuf b) {
		super(b);
	}

	/**
	 * creates a String representation of this hexadecimal field.
	 *
	 * @return String representing this flag.
	 */
	public String toString() {
		return "0x" + data.toHexStringJustified(0, "");
	}
}