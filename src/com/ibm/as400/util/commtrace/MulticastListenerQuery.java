///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MulticastListenerQuery.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * A Multicast Listener Query Header.<br>
 * Extends MLMessage's methods  to parse, print, and allow easy access to the 
 * Multicast Listener Query Header.
 * 
 */
public class MulticastListenerQuery extends MLMessage {
	/**
	 * Creates and parses the data of this header.  
	 * @param data	The raw data of this header.
	 */
	MulticastListenerQuery(BitBuf data) {
		super(data);
		super.type= MLTLSTQRY;
	}
}