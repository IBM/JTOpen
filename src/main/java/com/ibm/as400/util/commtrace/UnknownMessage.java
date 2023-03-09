///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnknownMessage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * A UnknownMessage Header.<br>
 * Extends Message's methods to parse, print, and allow easy access to the UnknownMessage Header.
 */
public class UnknownMessage extends Message {

	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header. 
	 */
	UnknownMessage(BitBuf data) {
		super(data);
		super.type= MSGUNK;
	}

	/**
	 * Returns the next header in this packet.
	 * @return Will always return null.
	 */
	public Header getNextHeader() {
		return null;
	}
}