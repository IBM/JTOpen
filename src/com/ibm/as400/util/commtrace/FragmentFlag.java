///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: FragmentFlag.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents a Fragment Flag. 
 */
class FragmentFlag extends Field {
	
	/**
	 * Constructs a FragmentFlag.
	 * @param data The data for this flag.
	 */
	public FragmentFlag(BitBuf data) {
		super(data);
	}

	/**
	 * Returns the data for this flag as a hex string.
	 */
	public String toString() {
		return data.toHexString();
	}
}